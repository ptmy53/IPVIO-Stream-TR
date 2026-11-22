package com.ipvio.stream

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * "Ignore": titles and channels you do not need disappear everywhere (home, movies, series, Live TV, search),
 * without reloading the playlist. Grouping is by the provider's tag – "TR |", "[EN]", "DE:" – first from the
 * category name, otherwise from the title. This complements the language filter for cases where its detection is not enough.
 * Keys: bundle = "t:KIND|TAG" (KIND = LIVE, MOVIE, SERIES), single category = "g:KIND|CATEGORY".
 */
object IgnoredGroups {
    private const val PREFS = "ipvio_ignored"
    @Volatile private var keys: Set<String> = emptySet()

    /** [keys] split by kind, so filtering needs no string building per title. */
    private class Index(val groups: Map<Kind, Set<String>>, val tags: Map<Kind, Set<String>>)
    @Volatile private var idx = Index(emptyMap(), emptyMap())

    private fun indexOf(s: Set<String>): Index {
        val g = HashMap<Kind, MutableSet<String>>()
        val t = HashMap<Kind, MutableSet<String>>()
        for (k in s) {
            val bar = k.indexOf('|')
            if (bar < 3 || k[1] != ':') continue
            val kind = runCatching { Kind.valueOf(k.substring(2, bar)) }.getOrNull() ?: continue
            val v = k.substring(bar + 1)
            when (k[0]) {
                'g' -> g.getOrPut(kind) { HashSet() }.add(v)
                't' -> t.getOrPut(kind) { HashSet() }.add(v)
            }
        }
        return Index(g, t)
    }
    /** Changes with every switch (trigger to rebuild the shown list). */
    var version by mutableIntStateOf(0)
        private set

    val isEmpty: Boolean get() = keys.isEmpty()
    val size: Int get() = keys.size
    fun has(key: String) = key in keys

    fun init(c: Context) {
        val s = (Profiles.sp(c, PREFS).getString("k", "") ?: "").split("\n").filter { it.isNotBlank() }.toSet()
        if (s != keys) { keys = s; idx = indexOf(s); version++ }
    }

    private fun save(c: Context, s: Set<String>) {
        keys = s; idx = indexOf(s)
        Profiles.sp(c, PREFS).edit().putString("k", s.joinToString("\n")).apply()
        version++
    }

    fun set(c: Context, key: String, ignored: Boolean) = save(c, if (ignored) keys + key else keys - key)
    fun setMany(c: Context, add: Collection<String>, ignored: Boolean) = save(c, if (ignored) keys + add else keys - add.toSet())
    fun clear(c: Context) = save(c, emptySet())

    /** Current keys (immutable copy), e.g. as the start of a draft in settings. */
    fun snapshot(): Set<String> = keys

    /** Applies a draft in one step: one save and one version change, however many switches were changed. */
    fun applyDraft(c: Context, add: Set<String>, remove: Set<String>) {
        if (add.isEmpty() && remove.isEmpty()) return
        save(c, keys - remove + add)
    }

    private val START = Regex("""^\W*([A-Za-z]{2,5})\s*(?:[|:\]\)]|[-–]\s)""")
    private val END = Regex("""[\[(|]\s*([A-Za-z]{2,4})\s*[\])|]?\s*$""")

    /** Tag like "TR" or "EN" from a text (start, otherwise end) or null. */
    fun tagIn(s: String): String? =
        START.find(s)?.groupValues?.get(1)?.uppercase() ?: END.find(s)?.groupValues?.get(1)?.uppercase()

    /** Tag of an entry: category first, otherwise title. Empty = no tag. */
    fun tagOf(i: Item): String = tagIn(i.group) ?: tagIn(i.title) ?: ""

    fun bundleKey(kind: Kind, tag: String) = "t:" + kind.name + "|" + tag
    fun groupKey(kind: Kind, group: String) = "g:" + kind.name + "|" + group

    fun isIgnored(i: Item): Boolean {
        if (keys.isEmpty()) return false
        val x = idx
        if (x.groups[i.kind]?.contains(i.group) == true) return true
        val tags = x.tags[i.kind] ?: return false
        val t = tagOf(i)
        return t.isNotEmpty() && t in tags
    }

    /**
     * Same result as filtering with [isIgnored], but faster for large playlists: the tag regex runs only for kinds
     * with ignored bundles, and once per category instead of once per title (the title is checked only if the category has no tag).
     */
    fun filter(l: List<Item>): List<Item> {
        if (keys.isEmpty()) return l
        val x = idx
        val groupTag = HashMap<String, String>()
        return l.filter { i ->
            if (x.groups[i.kind]?.contains(i.group) == true) return@filter false
            val tags = x.tags[i.kind] ?: return@filter true
            val t = groupTag.getOrPut(i.group) { tagIn(i.group) ?: "" }.ifEmpty { tagIn(i.title) ?: "" }
            !(t.isNotEmpty() && t in tags)
        }
    }
}
