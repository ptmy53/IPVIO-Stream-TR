package com.ipvio.stream

import org.junit.Assert.*
import org.junit.Test

class SublistStoreTest {

    @Test fun encodeDecodeAllRoundTrip() {
        val map = mapOf(
            123 to listOf(
                SublistDef("UK channels", SublistFilter(enabled = true, groupsByKind = mapOf(Kind.LIVE to listOf("UK")), kind = Kind.LIVE)),
                SublistDef("4K movies", SublistFilter(enabled = true, kind = Kind.MOVIE, quality = "4K"))
            ),
            456 to listOf(
                SublistDef("German", SublistFilter(enabled = true, langsByKind = mapOf(Kind.LIVE to listOf("de")), includeAdult = false))
            )
        )
        val json = SublistStore.encodeAll(map)
        val back = SublistStore.decodeAll(json)
        assertEquals(map.size, back.size)
        assertEquals(2, back[123]!!.size)
        assertEquals("UK channels", back[123]!![0].name)
        assertEquals(mapOf(Kind.LIVE to listOf("UK")), back[123]!![0].filter.groupsByKind)
        assertEquals(Kind.LIVE, back[123]!![0].filter.kind)
        assertEquals("4K movies", back[123]!![1].name)
        assertEquals("4K", back[123]!![1].filter.quality)
        assertEquals("German", back[456]!![0].name)
        assertEquals(mapOf(Kind.LIVE to listOf("de")), back[456]!![0].filter.langsByKind)
        assertFalse(back[456]!![0].filter.includeAdult)
    }

    @Test fun encodeDecodeEmptyRoundTrip() {
        val original = SublistDef("test", SublistFilter())
        val decoded = SublistDef.decode(original.encode())!!
        assertEquals(original, decoded)
    }

    @Test fun decodeNull() {
        assertNull(SublistDef.decode(null))
        assertNull(SublistDef.decode(""))
    }

    @Test fun decodeAllNull() {
        assertTrue(SublistStore.decodeAll(null).isEmpty())
        assertTrue(SublistStore.decodeAll("").isEmpty())
    }

    @Test fun encodeDecodeMapRoundTrip() {
        val all = mapOf(
            1 to listOf(SublistDef("a", SublistFilter(enabled = true))),
            2 to listOf(SublistDef("b", SublistFilter(kind = Kind.SERIES)))
        )
        val json = SublistStore.encodeAll(all)
        val back = SublistStore.decodeAll(json)
        assertEquals(all.keys.toSet(), back.keys.toSet())
        assertEquals("a", back[1]!![0].name)
        assertTrue(back[1]!![0].filter.enabled)
        assertEquals("b", back[2]!![0].name)
        assertEquals(Kind.SERIES, back[2]!![0].filter.kind)
    }

    @Test fun activeKeysPicksOnlyActiveSelections() {
        val keys = listOf("sublists", "sublist_active_123", "sublist_active_-45", "sublists_active_cleared", "ipvio_filter")
        assertEquals(listOf("sublist_active_123", "sublist_active_-45"), SublistStore.activeKeys(keys))
    }
}
