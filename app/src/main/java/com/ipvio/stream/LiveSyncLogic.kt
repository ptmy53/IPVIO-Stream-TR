package com.ipvio.stream

import org.json.JSONArray
import org.json.JSONObject

/**
 * Pure merge logic for the live sync (W-010b): several devices change the same favorites and history, and every device
 * merges all sync files instead of overwriting its own data.
 *
 * The stores (Library, History, WatchStore, EpProgress, ...) stay as they are: they only know their current lists.
 * The times needed for merging are worked out here, by comparing the current list with the state of the last sync
 * (the "base", kept on this device):
 * - an entry that is new or changed since the base gets the current time,
 * - an entry that was in the base and is now missing becomes a tombstone with the current time, so a deleted
 *   favorite does not come back from another device.
 *
 * Merging: per id the newest action wins (added/changed vs. deleted); on a tie the entry stays.
 * Device clocks are trusted; a clock that is far off can make an old action win.
 *
 * No Android classes here (org.json is a real library in the unit tests), so all rules are covered by JVM tests.
 */
object LiveSyncLogic {
    /** Tombstones older than this are forgotten (a device offline for longer may bring a deleted entry back). */
    const val TOMBSTONE_MS = 90L * 24 * 60 * 60 * 1000
    const val MAX_TOMBSTONES = 500

    /** One entry: time of the last change and, for records, its value (empty for plain lists). */
    class Rec(val ts: Long, val value: String = "") {
        override fun equals(other: Any?) = other is Rec && other.ts == ts && other.value == value
        override fun hashCode() = ts.hashCode() * 31 + value.hashCode()
        override fun toString() = "Rec($ts, $value)"
    }

    /** State of one list: present entries and tombstones (id -> time of deletion). */
    class Log(val items: Map<String, Rec>, val gone: Map<String, Long>) {
        override fun equals(other: Any?) = other is Log && other.items == items && other.gone == gone
        override fun hashCode() = items.hashCode() * 31 + gone.hashCode()
        override fun toString() = "Log(items=$items, gone=$gone)"
    }

    val EMPTY = Log(emptyMap(), emptyMap())

    private fun tombstones(base: Log, present: Set<String>, now: Long): Map<String, Long> {
        val g = HashMap<String, Long>()
        for ((id, t) in base.gone) if (id !in present) g[id] = t
        for (id in base.items.keys) if (id !in present) g[id] = now
        return g
    }

    /**
     * Plain list where new entries are added (favorites, watch list, category favorites). Only adding and deleting
     * count; moving an entry inside the list does not change its time.
     */
    fun trackSet(base: Log, current: List<String>, now: Long): Log {
        val items = LinkedHashMap<String, Rec>()
        for (id in current) if (id.isNotEmpty() && id !in items) items[id] = base.items[id] ?: Rec(now)
        return Log(items, tombstones(base, items.keys, now))
    }

    /**
     * Recency list, newest first (recently watched). Watching moves an entry to the top, so the entries above the
     * unchanged rest get new times (now, now - 1, ... to keep their order). The unchanged rest is the longest end of
     * the list that is in the base in the same order.
     */
    fun trackRecent(base: Log, current: List<String>, now: Long): Log {
        val list = current.filter { it.isNotEmpty() }.distinct()
        var k = list.size
        // Walk from the bottom: the oldest entries must be in the base with rising times towards the top
        var i = list.size - 1
        var after = Long.MIN_VALUE
        while (i >= 0) {
            val t = base.items[list[i]]?.ts ?: break
            if (t <= after) break
            after = t
            k = i
            i--
        }
        val items = LinkedHashMap<String, Rec>()
        for ((idx, id) in list.withIndex()) {
            items[id] = if (idx >= k) base.items.getValue(id) else Rec(now - idx)
        }
        return Log(items, tombstones(base, items.keys, now))
    }

    /** Records with a value (resume points, episode progress): a changed value gets the current time. */
    fun trackRecords(base: Log, current: Map<String, String>, now: Long): Log {
        val items = LinkedHashMap<String, Rec>()
        for ((id, v) in current) {
            if (id.isEmpty()) continue
            val b = base.items[id]
            items[id] = if (b != null && b.value == v) b else Rec(now, v)
        }
        return Log(items, tombstones(base, items.keys, now))
    }

    /**
     * Merges the logs of all devices (this device first). Per id the newest action wins; on a tie between adding and
     * deleting the entry stays, between two values the larger value wins (the same result on every device).
     * Old tombstones are forgotten.
     */
    fun merge(logs: List<Log>, now: Long): Log {
        val best = HashMap<String, Rec>()
        val gone = HashMap<String, Long>()
        for (l in logs) {
            for ((id, r) in l.items) {
                val b = best[id]
                if (b == null || r.ts > b.ts || (r.ts == b.ts && r.value > b.value)) best[id] = r
            }
            for ((id, t) in l.gone) if (t > (gone[id] ?: Long.MIN_VALUE)) gone[id] = t
        }
        val items = HashMap<String, Rec>()
        for ((id, r) in best) if (r.ts >= (gone[id] ?: Long.MIN_VALUE)) items[id] = r
        val g = gone.filter { (id, t) -> id !in items && now - t <= TOMBSTONE_MS }
            .entries.sortedByDescending { it.value }.take(MAX_TOMBSTONES).associate { it.key to it.value }
        return Log(items, g)
    }

    /**
     * Order for a plain list after merging: the local order stays; entries new to this device come first, newest first.
     */
    fun orderedIds(merged: Log, localOrder: List<String>): List<String> {
        val local = localOrder.filter { it in merged.items }.distinct()
        val known = local.toHashSet()
        val fresh = merged.items.entries.filter { it.key !in known }.sortedWith(byTime).map { it.key }
        return fresh + local
    }

    /** Order for a recency list after merging: newest first, at most [limit] entries. */
    fun recentIds(merged: Log, limit: Int): List<String> =
        merged.items.entries.sortedWith(byTime).take(limit).map { it.key }

    /** The newest [limit] records after merging. */
    fun newestRecords(merged: Log, limit: Int): List<Pair<String, String>> =
        merged.items.entries.sortedWith(byTime).take(limit).map { it.key to it.value.value }

    /** Newest first; same time: by id, so every device gets the same order. */
    private val byTime = compareByDescending<Map.Entry<String, Rec>> { it.value.ts }.thenBy { it.key }

    /** Limits a merged log to the entries that are kept locally (after [recentIds] or [newestRecords]). */
    fun keepOnly(merged: Log, ids: Collection<String>): Log {
        val keep = ids.toHashSet()
        return Log(merged.items.filterKeys { it in keep }, merged.gone)
    }

    // ---- JSON (part of the sync file format 3 and of the base kept on the device) ----

    /** {"i":{"id":[ts,"value"]},"g":{"id":ts}}; the value is left out when empty. */
    fun toJson(l: Log): JSONObject {
        val i = JSONObject()
        for ((id, r) in l.items) i.put(id, JSONArray().put(r.ts).also { if (r.value.isNotEmpty()) it.put(r.value) })
        val g = JSONObject()
        for ((id, t) in l.gone) g.put(id, t)
        return JSONObject().put("i", i).put("g", g)
    }

    /** Reads a log; broken entries are skipped, a broken log is [EMPTY]. */
    fun fromJson(o: JSONObject?): Log {
        if (o == null) return EMPTY
        val items = LinkedHashMap<String, Rec>()
        o.optJSONObject("i")?.let { i ->
            for (id in i.keys()) {
                val a = i.optJSONArray(id) ?: continue
                val ts = a.optLong(0, Long.MIN_VALUE)
                if (ts == Long.MIN_VALUE || id.isEmpty()) continue
                items[id] = Rec(ts, a.optString(1, ""))
            }
        }
        val gone = HashMap<String, Long>()
        o.optJSONObject("g")?.let { g ->
            for (id in g.keys()) {
                val t = g.optLong(id, Long.MIN_VALUE)
                if (t != Long.MIN_VALUE && id.isNotEmpty()) gone[id] = t
            }
        }
        return Log(items, gone)
    }

    fun fromJsonText(s: String?): Log =
        if (s.isNullOrBlank()) EMPTY else runCatching { fromJson(JSONObject(s)) }.getOrDefault(EMPTY)
}
