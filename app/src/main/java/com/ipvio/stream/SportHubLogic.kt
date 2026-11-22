package com.ipvio.stream

/** T-049d: tab choice of the Sports Hub (pure logic, no Android imports, covered by JVM tests). */
object SportHubLogic {
    /** Overview, Live, Schedule, Leagues, Teams, Favorites, Channels. */
    const val TAB_COUNT = 7

    /** Tabs that are shown, in order. At least one stays: if all were hidden, all are shown. */
    fun visible(hidden: Set<Int>): List<Int> =
        (0 until TAB_COUNT).filter { it !in hidden }.ifEmpty { (0 until TAB_COUNT).toList() }

    /** The tab to open: [tab] if it is shown, otherwise the first shown one. */
    fun resolve(tab: Int, hidden: Set<Int>): Int = visible(hidden).let { if (tab in it) tab else it.first() }

    fun encode(hidden: Set<Int>): String = hidden.filter { it in 0 until TAB_COUNT }.sorted().joinToString(",")

    fun decode(text: String?): Set<Int> =
        (text ?: "").split(',').mapNotNull { it.trim().toIntOrNull() }.filter { it in 0 until TAB_COUNT }.toSet()

    /** Is the sport (key) one the user follows? An empty selection means all sports. */
    fun sportShown(selected: Set<String>, sport: String): Boolean = selected.isEmpty() || sport in selected
}
