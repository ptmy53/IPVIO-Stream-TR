package com.ipvio.stream

/**
 * Text search over category names in the filter editor (pure logic, no Android).
 *
 * Case and separators are ignored, so "EU | DE" also finds "EU|DE", "eu - de" or "[EU] DE: Cinema".
 * The words of the query must appear in this order and each must start a word in the name.
 * Inner words must match whole; the last word may be the start of a longer word ("EU | Spo" finds "EU | Sports"),
 * except for codes of up to two letters ("DE", "EN"), which match whole so "EU | DE" does not find "EU | DEPORTES".
 */
object GroupSearch {
    /** Lowercase, every run of non-letters/non-digits becomes one space, no leading or trailing space. */
    fun normalize(s: String): String {
        val b = StringBuilder(s.length)
        var sep = true
        for (ch in s) {
            if (ch.isLetterOrDigit()) { b.append(ch.lowercaseChar()); sep = false }
            else if (!sep) { b.append(' '); sep = true }
        }
        if (b.isNotEmpty() && b[b.length - 1] == ' ') b.setLength(b.length - 1)
        return b.toString()
    }

    /** Matcher for [query], or null when the query is empty (no search active). */
    fun matcher(query: String): ((String) -> Boolean)? {
        val q = normalize(query)
        if (q.isEmpty()) return null
        val last = q.substringAfterLast(' ')
        val needle = if (last.length <= 2) " $q " else " $q"
        return { name -> (" " + normalize(name) + " ").contains(needle) }
    }

    /** Entries of [list] whose name matches [query]; the whole list when the query is empty. */
    fun <T> hits(list: List<T>, query: String, name: (T) -> String): List<T> {
        val m = matcher(query) ?: return list
        return list.filter { m(name(it)) }
    }
}
