package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivateLogicTest {
    private fun item(id: String, kind: Kind, group: String, title: String = "T$id") = Item(id, kind, title, null, "u$id", null, group)

    private val liveNews = item("1", Kind.LIVE, "UK| NEWS")
    private val movieNews = item("2", Kind.MOVIE, "UK| NEWS")
    private val liveSport = item("3", Kind.LIVE, "UK| SPORTS")
    private val all = listOf(liveNews, movieNews, liveSport)
    private val keys = setOf(PrivateLogic.key(Kind.LIVE, "UK| NEWS"))

    @Test fun sameGroupNameOfAnotherKindStaysVisible() {
        assertTrue(PrivateLogic.isPrivate(liveNews, keys, false))
        assertFalse(PrivateLogic.isPrivate(movieNews, keys, false))
    }

    @Test fun visibleDropsOnlyPrivateItems() =
        assertEquals(listOf(movieNews, liveSport), PrivateLogic.visible(all, keys, false))

    @Test fun visibleKeepsSameListWhenNothingIsPrivate() =
        assertSame(all, PrivateLogic.visible(all, emptySet(), false))

    @Test fun idsOfPrivateItems() {
        assertEquals(setOf("1"), PrivateLogic.ids(all, keys, false))
        assertEquals(emptySet<String>(), PrivateLogic.ids(all, emptySet(), false))
    }

    @Test fun parentalOptionHidesFlaggedCategories() {
        // "FSK 18" is an age rating the parental check flags
        val rated = item("4", Kind.MOVIE, "DE| FSK 18")
        val list = all + rated
        assertEquals(all, PrivateLogic.visible(list, emptySet(), true))
        assertEquals(list, PrivateLogic.visible(list, emptySet(), false))
        assertEquals(setOf("4"), PrivateLogic.ids(list, emptySet(), true))
    }

    @Test fun groupNamesWithSeparatorsRoundTrip() {
        val k = setOf(PrivateLogic.key(Kind.SERIES, "|EN| ASIAN SERIES"), PrivateLogic.key(Kind.LIVE, "ASIA | INDIA"))
        assertEquals(k, PrivateLogic.decode(PrivateLogic.encode(k)))
        assertEquals(emptySet<String>(), PrivateLogic.decode(null))
    }
}
