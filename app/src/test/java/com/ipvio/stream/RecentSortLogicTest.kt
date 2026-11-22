package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Test

/** #22: "Neu hinzugefügt" sorts by the provider's added date, falls back to the old order without dates. */
class RecentSortLogicTest {

    private data class T(val name: String, val added: Long)

    @Test
    fun withDates_newestFirst() {
        val list = listOf(T("a", 100), T("b", 300), T("c", 200))
        assertEquals(listOf("b", "c", "a"), RecentSortLogic.newestFirst(list) { it.added }.map { it.name })
    }

    @Test
    fun withoutDates_keepsOldOrderReversed() {
        val list = listOf(T("a", 0), T("b", 0), T("c", 0))
        assertEquals(listOf("c", "b", "a"), RecentSortLogic.newestFirst(list) { it.added }.map { it.name })
    }

    @Test
    fun mixedDates_unknownDatesGoLast() {
        val list = listOf(T("a", 0), T("b", 50), T("c", 0))
        // Stable sort: the entries without a date keep their order among themselves
        assertEquals(listOf("b", "a", "c"), RecentSortLogic.newestFirst(list) { it.added }.map { it.name })
    }
}
