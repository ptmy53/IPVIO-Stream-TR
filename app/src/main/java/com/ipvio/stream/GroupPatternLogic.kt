package com.ipvio.stream

/**
 * Finds the provider's category pattern "FR - SPORTS", "DE | Kids", "UK: News" (#96): a short prefix, a separator and
 * the rest. A prefix is only turned into a folder when at least [minSize] categories share it. Everything else stays
 * as it is. Pure logic, no Android, testable.
 */
object GroupPatternLogic {
    private val SPLIT = Regex("""^\s*([A-Za-z]{2,3})\s*(?:-|\||:|–)\s*(.+?)\s*$""")

    /** A category name split into prefix and rest. */
    data class Split(val prefix: String, val rest: String)

    /** Folder for one prefix: [subs] = (rest of the name, original category name) pairs, in playlist order. */
    data class Folder(val prefix: String, val subs: List<Pair<String, String>>)

    fun split(group: String): Split? =
        SPLIT.matchEntire(group)?.let { Split(it.groupValues[1].uppercase(), it.groupValues[2]) }

    /** Folders found, and the categories that were not grouped. */
    fun analyze(groups: List<String>, minSize: Int = 2): Pair<List<Folder>, List<String>> {
        val byPrefix = LinkedHashMap<String, MutableList<Pair<String, String>>>()
        val rest = ArrayList<String>()
        for (g in groups) {
            val s = split(g)
            if (s == null) rest.add(g) else byPrefix.getOrPut(s.prefix) { ArrayList() }.add(s.rest to g)
        }
        val folders = ArrayList<Folder>()
        for ((prefix, subs) in byPrefix) {
            if (subs.size >= minSize) folders.add(Folder(prefix, subs)) else subs.forEach { rest.add(it.second) }
        }
        return folders to rest
    }
}
