package com.ipvio.stream

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Sublist filter rules for a playlist (without Android, testable).
 *
 * A sublist lets a user load only a subset of a big M3U playlist, so the app does not
 * have to keep thousands of unwanted titles in memory. The filter runs after parsing and
 * before the items are stored ([PlaylistStore.show]).
 *
 * All fields are optional; null means "no filter on this axis". A title passes the filter
 * only if it matches every non-null criterion.
 */
data class SublistFilter(
    /** Master switch. When false, the full playlist is loaded as before. */
    val enabled: Boolean = false,
    /** Keep items of this kind only (LIVE / MOVIE / SERIES). Null = all kinds. */
    val kind: Kind? = null,
    /**
     * Groups required per kind, e.g. {LIVE: ["UK"], MOVIE: ["News"]}. Empty = no group filter for that kind.
     * A group rule and a [titlesByKind] rule for the same kind are OR-ed: an item passes when its
     * group matches OR its title contains one of the patterns.
     */
    val groupsByKind: Map<Kind, List<String>> = emptyMap(),
    /**
     * Title patterns per kind, e.g. {SERIES: ["-eng", "_eng"]}. Empty = no title filter for that kind.
     * Needed for content the provider does not separate by group, such as English-dubbed Asian dramas
     * that sit inside large English groups.
     */
    val titlesByKind: Map<Kind, List<String>> = emptyMap(),
    /** Languages required per kind, e.g. {LIVE: ["en"], MOVIE: ["de"]}. Empty = no lang filter for that kind. */
    val langsByKind: Map<Kind, List<String>> = emptyMap(),
    /** Keep items with this quality ("HD", "FHD", "4K"). Null = any quality. */
    val quality: String? = null,
    /** true = keep only items with a logo, false = keep only items without a logo, null = any. */
    val hasLogo: Boolean? = null,
    /** true = keep only items with a tvg-id, false = keep only items without one, null = any. */
    val hasTvgId: Boolean? = null,
    /** Whether adult content may pass the filter. */
    val includeAdult: Boolean = true
) {
    /**
     * Encode to a simple "key=value" text form (no org.json, so JVM tests work).
     *
     * Group and language names are arbitrary user/provider text: real IPTV group names look like
     * "|US| ✪ NFL" and may contain '|' and ','. Those two characters therefore cannot be used as
     * separators – doing so split the names on decode and silently dropped every group filter
     * (the whole playlist stayed visible). Control characters cannot appear in a group name.
     */
    fun encode(): String =
        kv(
            "e" to enabled.toString(),
            "k" to kind?.name,
            "g" to encodeMap(groupsByKind),
            "t" to encodeMap(titlesByKind),
            "l" to encodeMap(langsByKind),
            "q" to quality,
            "h" to hasLogo?.toString(),
            "v" to hasTvgId?.toString(),
            "a" to includeAdult.toString()
        )

    companion object {
        /** Marks the current value format: separator-safe group names. */
        private const val MAP_V2 = "v2"
        /** Separates the per-kind blocks ("LIVE:…", "MOVIE:…"). */
        private const val KIND_SEP = '\u001F'
        /** Separates the values inside one per-kind block. */
        private const val VALUE_SEP = '\u001E'

        fun decode(s: String?): SublistFilter {
            val o = unkv(s) ?: return SublistFilter()
            return SublistFilter(
                enabled = o["e"]?.toBoolean() ?: false,
                kind = o["k"]?.let { runCatching { Kind.valueOf(it) }.getOrNull() },
                groupsByKind = decodeMap(o["g"]),
                titlesByKind = decodeMap(o["t"]),
                langsByKind = decodeMap(o["l"]),
                quality = o["q"],
                hasLogo = o["h"]?.toBoolean(),
                hasTvgId = o["v"]?.toBoolean(),
                includeAdult = o["a"]?.toBoolean() ?: true
            )
        }

        private fun encodeMap(m: Map<Kind, List<String>>): String? {
            val parts = m.entries.filter { it.value.isNotEmpty() }
            if (parts.isEmpty()) return null
            return MAP_V2 + KIND_SEP + parts.joinToString(KIND_SEP.toString()) { "${it.key.name}:${it.value.joinToString(VALUE_SEP.toString())}" }
        }

        private fun decodeMap(s: String?): Map<Kind, List<String>> {
            if (s.isNullOrBlank()) return emptyMap()
            // Old saved data had no marker and used '|' (kinds) and ',' (values).
            val head = MAP_V2 + KIND_SEP
            val body = if (s.startsWith(head)) s.substring(head.length) else s
            val kindSep = if (s.startsWith(head)) KIND_SEP else '|'
            val valueSep = if (s.startsWith(head)) VALUE_SEP else ','
            return body.split(kindSep).mapNotNull { part ->
                val i = part.indexOf(':')
                if (i <= 0) null else {
                    val k = runCatching { Kind.valueOf(part.substring(0, i)) }.getOrNull() ?: return@mapNotNull null
                    val vals = part.substring(i + 1).split(valueSep).filter { it.isNotEmpty() }
                    k to vals
                }
            }.filter { it.second.isNotEmpty() }.toMap()
        }
    }

    /** Does [item] pass every active criterion? */
    fun matches(item: Item): Boolean {
        if (!enabled) return true
        if (!includeAdult && ParentalLogic.isAdult(item.group, item.title)) return false
        if (kind != null && item.kind != kind) return false
        val groups = groupsByKind[item.kind].orEmpty()
        val titles = titlesByKind[item.kind].orEmpty()
        if (groups.isNotEmpty() || titles.isNotEmpty()) {
            // OR on purpose: a title rule exists to pull a few items out of big groups that the
            // group rule would otherwise reject (e.g. English-dubbed dramas inside |EN| groups).
            // The title check is skipped when the group already matched (65k series × 46 patterns).
            val groupOk = groups.any { item.group.contains(it, ignoreCase = true) }
            if (!groupOk && !titles.any { item.title.contains(it, ignoreCase = true) }) return false
        }
        val langs = langsByKind[item.kind]
        if (langs != null && langs.isNotEmpty() && !langs.contains(item.lang)) return false
        if (quality != null && item.quality != quality) return false
        if (hasLogo != null && (item.logo != null) != hasLogo) return false
        if (hasTvgId != null && (item.tvgId != null) != hasTvgId) return false
        return true
    }

    /** How many of [items] pass the filter (for a preview count). */
    fun countMatching(items: List<Item>): Int = items.count { matches(it) }
}

/** A named sublist saved for one playlist URL. */
data class SublistDef(
    val name: String,          // user-chosen name, e.g. "UK channels" or "4K movies"
    val filter: SublistFilter
) {
    fun encode(): String =
        kv("n" to name, "f" to filter.encode())

    companion object {
        fun decode(s: String?): SublistDef? {
            val o = unkv(s) ?: return null
            val n = o["n"] ?: return null
            return SublistDef(n, SublistFilter.decode(o["f"]))
        }
    }
}

/** Simple "key=value" text encoding (one per line, values URL-encoded). No org.json so JVM tests work. */
private fun kv(vararg p: Pair<String, String?>): String =
    p.filter { it.second != null }.joinToString("\n") { it.first + "=" + URLEncoder.encode(it.second, "UTF-8") }

private fun unkv(s: String?): Map<String, String>? {
    if (s.isNullOrBlank()) return null
    return try {
        s.split('\n').mapNotNull { l ->
            val i = l.indexOf('=')
            if (i <= 0) null else l.substring(0, i) to URLDecoder.decode(l.substring(i + 1), "UTF-8")
        }.toMap()
    } catch (e: Exception) { null }
}
