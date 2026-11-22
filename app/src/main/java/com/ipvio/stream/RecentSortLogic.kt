package com.ipvio.stream

/**
 * "Neu hinzugefügt" (#22): newest first by the provider's added date. Without any date (older caches, M3U lists)
 * the list keeps the old order (last entry of the playlist first), so nothing changes for those playlists.
 * Pure logic, no Android, testable.
 */
object RecentSortLogic {
    fun <T> newestFirst(items: List<T>, added: (T) -> Long): List<T> =
        if (items.any { added(it) > 0L }) items.sortedByDescending(added) else items.asReversed()
}
