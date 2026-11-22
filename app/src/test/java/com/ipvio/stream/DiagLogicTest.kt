package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Test

class DiagLogicTest {

    @Test
    fun append_keepsOrderAndCutsMessage() {
        val l = DiagLogic.append(DiagLogic.append(emptyList(), 1L, "a"), 2L, "b".repeat(300))
        assertEquals(listOf(1L, 2L), l.map { it.first })
        assertEquals(160, l[1].second.length)
    }

    @Test
    fun append_keepsOnlyNewestEntries() {
        var l = emptyList<Pair<Long, String>>()
        for (i in 1..30) l = DiagLogic.append(l, i.toLong(), "m$i")
        assertEquals(DiagLogic.MAX, l.size)
        assertEquals("m6", l.first().second)
        assertEquals("m30", l.last().second)
    }
}
