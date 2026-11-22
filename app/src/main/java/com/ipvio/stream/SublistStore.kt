package com.ipvio.stream

import android.content.Context
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistence of sublist definitions per playlist URL.
 *
 * Each playlist URL (identified by its hash, same as [PlaylistStore.key]) may have several
 * named sublists (e.g. "UK channels", "4K movies"). Only the filter definition is stored;
 * the actual filtered list is rebuilt on every playlist load.
 *
 * Stored under the key "sublists" in SharedPreferences "ipvio", as a JSON object mapping
 * urlHash → JSON array of SublistDef encoded forms.
 */
object SublistStore {
    private const val PREFS = "ipvio"
    private const val KEY = "sublists"
    private const val ACTIVE_PREFIX = "sublist_active_"
    private const val CLEARED_FLAG = "sublists_active_cleared"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ------------------------------------------------------------------ Pure helpers (testable without Android)

    /** The SharedPreferences keys that hold an active sublist selection (one per playlist). */
    fun activeKeys(keys: Collection<String>): List<String> = keys.filter { it.startsWith(ACTIVE_PREFIX) }

    // ------------------------------------------------------------------ Pure JSON encoding (testable without Android)

    /** Encode all sublists to the JSON string stored in SharedPreferences. */
    fun encodeAll(all: Map<Int, List<SublistDef>>): String {
        val o = JSONObject()
        all.forEach { (hash, list) ->
            val arr = JSONArray()
            list.forEach { arr.put(it.encode()) }
            o.put(hash.toString(), arr)
        }
        return o.toString()
    }

    /** Decode the JSON string back into per-hash sublists. Invalid JSON yields an empty map. */
    fun decodeAll(json: String?): Map<Int, List<SublistDef>> {
        return runCatching {
            val o = JSONObject(json ?: "{}")
            o.keys().asSequence().mapNotNull { k ->
                val hash = k.toIntOrNull() ?: return@mapNotNull null
                val a = o.optJSONArray(k) ?: return@mapNotNull null
                val list = (0 until a.length()).mapNotNull { i -> SublistDef.decode(a.optString(i)) }
                hash to list
            }.toMap()
        }.getOrDefault(emptyMap())
    }

    // ------------------------------------------------------------------ SharedPreferences persistence

    /** All sublists for one playlist (urlHash), in saved order. */
    fun allFor(c: Context, urlHash: Int): List<SublistDef> {
        return decodeAll(prefs(c).getString(KEY, null)).getOrDefault(urlHash, emptyList())
    }

    /** Save (replace) all sublists for one playlist. */
    fun saveFor(c: Context, urlHash: Int, sublists: List<SublistDef>) {
        val all = decodeAll(prefs(c).getString(KEY, null)).toMutableMap()
        all[urlHash] = sublists
        prefs(c).edit().putString(KEY, encodeAll(all)).apply()
    }

    /** Add or replace one named sublist for a playlist. */
    fun upsert(c: Context, urlHash: Int, def: SublistDef) {
        val list = allFor(c, urlHash)
        val idx = list.indexOfFirst { it.name == def.name }
        val next = if (idx >= 0) list.toMutableList().apply { set(idx, def) } else list + def
        saveFor(c, urlHash, next)
    }

    /** Remove a named sublist. */
    fun remove(c: Context, urlHash: Int, name: String) {
        val next = allFor(c, urlHash).filter { it.name != name }
        saveFor(c, urlHash, next)
    }

    /** The currently active sublist name for a playlist, or null if none. */
    fun active(c: Context, urlHash: Int): String? {
        return prefs(c).getString("$ACTIVE_PREFIX$urlHash", null)
    }

    /**
     * Run once after the sublist tab was removed: drop every active selection, so no playlist stays
     * filtered without a way to switch the filter off. The sublist definitions themselves are kept.
     */
    fun clearActiveOnce(c: Context) {
        val p = prefs(c)
        if (p.getBoolean(CLEARED_FLAG, false)) return
        val e = p.edit()
        activeKeys(p.all.keys).forEach { e.remove(it) }
        e.putBoolean(CLEARED_FLAG, true).apply()
    }

    /** Set the active sublist name (null = full playlist). */
    fun setActive(c: Context, urlHash: Int, name: String?) {
        val e = prefs(c).edit()
        if (name == null) e.remove("$ACTIVE_PREFIX$urlHash")
        else e.putString("$ACTIVE_PREFIX$urlHash", name)
        e.apply()
    }

    // ------------------------------------------------------------------ Export / Import (edit on computer, import back on TV)

    /** Export all sublists for one playlist as a pretty-printed JSON string. */
    fun exportFor(c: Context, urlHash: Int): String? {
        val list = allFor(c, urlHash)
        if (list.isEmpty()) return null
        val o = JSONObject()
        val arr = JSONArray()
        list.forEach { arr.put(it.encode()) }
        o.put(urlHash.toString(), arr)
        return o.toString(2) // 2-space indent for easy editing
    }

    /** Import sublists from a JSON string exported by [exportFor]. Replaces existing sublists for this playlist. */
    fun importFor(c: Context, urlHash: Int, json: String): Boolean {
        return runCatching {
            val o = JSONObject(json)
            val k = urlHash.toString()
            val a = o.optJSONArray(k) ?: return@runCatching false
            val list = (0 until a.length()).mapNotNull { i -> SublistDef.decode(a.optString(i)) }
            saveFor(c, urlHash, list)
            true
        }.getOrDefault(false)
    }

    /** Export all sublists across all playlists as a pretty-printed JSON string. */
    fun exportAll(c: Context): String? {
        val all = decodeAll(prefs(c).getString(KEY, null))
        if (all.isEmpty()) return null
        val o = JSONObject()
        all.forEach { (hash, list) ->
            val arr = JSONArray()
            list.forEach { arr.put(it.encode()) }
            o.put(hash.toString(), arr)
        }
        return o.toString(2)
    }

    /** Import all sublists from a JSON string exported by [exportAll]. Merges with existing data. */
    fun importAll(c: Context, json: String): Boolean {
        return runCatching {
            val incoming = decodeAll(json)
            val existing = decodeAll(prefs(c).getString(KEY, null)).toMutableMap()
            incoming.forEach { (hash, list) -> existing[hash] = list }
            prefs(c).edit().putString(KEY, encodeAll(existing)).apply()
            true
        }.getOrDefault(false)
    }
}
