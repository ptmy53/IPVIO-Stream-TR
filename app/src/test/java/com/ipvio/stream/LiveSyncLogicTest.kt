package com.ipvio.stream

import com.ipvio.stream.LiveSyncLogic.EMPTY
import com.ipvio.stream.LiveSyncLogic.Log
import com.ipvio.stream.LiveSyncLogic.Rec
import org.junit.Assert.*
import org.junit.Test

class LiveSyncLogicTest {
    private val day = 24L * 60 * 60 * 1000

    // ---- Favorites (plain lists) ----

    @Test fun newFavoritesGetTheCurrentTime() {
        val l = LiveSyncLogic.trackSet(EMPTY, listOf("a", "b"), 100)
        assertEquals(mapOf("a" to Rec(100), "b" to Rec(100)), l.items)
        assertTrue(l.gone.isEmpty())
    }

    @Test fun knownFavoritesKeepTheirTimeAndDeletedOnesBecomeTombstones() {
        val base = LiveSyncLogic.trackSet(EMPTY, listOf("a", "b"), 100)
        val l = LiveSyncLogic.trackSet(base, listOf("b", "c"), 200)   // a deleted, c added, b moved
        assertEquals(mapOf("b" to Rec(100), "c" to Rec(200)), l.items)
        assertEquals(mapOf("a" to 200L), l.gone)
    }

    @Test fun reAddedFavoriteLosesItsTombstone() {
        val base = Log(emptyMap(), mapOf("a" to 100L))
        val l = LiveSyncLogic.trackSet(base, listOf("a"), 200)
        assertEquals(mapOf("a" to Rec(200)), l.items)
        assertTrue(l.gone.isEmpty())
    }

    @Test fun favoritesFromBothDevicesAreJoined() {
        val a = LiveSyncLogic.trackSet(EMPTY, listOf("x", "y"), 100)
        val b = LiveSyncLogic.trackSet(EMPTY, listOf("x", "z"), 150)
        val m = LiveSyncLogic.merge(listOf(a, b), 200)
        assertEquals(setOf("x", "y", "z"), m.items.keys)
        assertEquals(Rec(150), m.items["x"])   // the newer time wins
    }

    @Test fun deletedFavoriteDoesNotComeBackFromAnOtherDevice() {
        val shared = LiveSyncLogic.trackSet(EMPTY, listOf("x", "y"), 100)
        val a = LiveSyncLogic.trackSet(shared, listOf("y"), 200)      // device A deletes x
        val b = shared                                                // device B has not changed anything
        val m = LiveSyncLogic.merge(listOf(b, a), 300)
        assertEquals(setOf("y"), m.items.keys)
        assertEquals(mapOf("x" to 200L), m.gone)
    }

    @Test fun addingAgainAfterTheDeleteWins() {
        val a = Log(emptyMap(), mapOf("x" to 200L))
        val b = Log(mapOf("x" to Rec(250)), emptyMap())
        assertEquals(setOf("x"), LiveSyncLogic.merge(listOf(a, b), 300).items.keys)
    }

    @Test fun onATieTheEntryStays() {
        val a = Log(emptyMap(), mapOf("x" to 200L))
        val b = Log(mapOf("x" to Rec(200)), emptyMap())
        val m = LiveSyncLogic.merge(listOf(a, b), 300)
        assertEquals(setOf("x"), m.items.keys)
        assertTrue(m.gone.isEmpty())
    }

    @Test fun localOrderStaysAndNewEntriesComeFirst() {
        val m = Log(mapOf("a" to Rec(100), "b" to Rec(100), "n1" to Rec(300), "n2" to Rec(400)), emptyMap())
        assertEquals(listOf("n2", "n1", "b", "a"), LiveSyncLogic.orderedIds(m, listOf("b", "gone", "a")))
    }

    // ---- Recently watched (recency list) ----

    @Test fun rewatchedEntryMovesUpWithANewTime() {
        val base = Log(mapOf("a" to Rec(30), "b" to Rec(20), "c" to Rec(10)), emptyMap())
        val l = LiveSyncLogic.trackRecent(base, listOf("c", "a", "b"), 100)
        assertEquals(Rec(100), l.items["c"])
        assertEquals(Rec(30), l.items["a"])
        assertEquals(Rec(20), l.items["b"])
        assertTrue(l.gone.isEmpty())
    }

    @Test fun severalNewEntriesKeepTheirOrder() {
        val base = Log(mapOf("a" to Rec(30)), emptyMap())
        val l = LiveSyncLogic.trackRecent(base, listOf("n1", "n2", "a"), 100)
        assertEquals(listOf("n1", "n2", "a"), LiveSyncLogic.recentIds(l, 80))
        assertEquals(Rec(30), l.items["a"])
    }

    @Test fun unchangedRecencyListKeepsAllTimes() {
        val base = Log(mapOf("a" to Rec(30), "b" to Rec(20)), emptyMap())
        assertEquals(base, LiveSyncLogic.trackRecent(base, listOf("a", "b"), 100))
    }

    @Test fun recencyListsOfTwoDevicesAreSortedByTime() {
        val a = Log(mapOf("x" to Rec(100), "y" to Rec(50)), emptyMap())
        val b = Log(mapOf("z" to Rec(80), "y" to Rec(120)), emptyMap())
        val m = LiveSyncLogic.merge(listOf(a, b), 200)
        assertEquals(listOf("y", "x", "z"), LiveSyncLogic.recentIds(m, 80))
        assertEquals(listOf("y", "x"), LiveSyncLogic.recentIds(m, 2))
    }

    @Test fun removedFromRecentlyWatchedStaysRemoved() {
        val base = Log(mapOf("a" to Rec(30), "b" to Rec(20)), emptyMap())
        val a = LiveSyncLogic.trackRecent(base, listOf("a"), 100)    // b removed on device A
        val m = LiveSyncLogic.merge(listOf(base, a), 200)
        assertEquals(listOf("a"), LiveSyncLogic.recentIds(m, 80))
    }

    // ---- Records (resume points, episode progress) ----

    @Test fun changedRecordGetsTheCurrentTime() {
        val base = Log(mapOf("s1" to Rec(10, "p=5"), "s2" to Rec(10, "p=9")), emptyMap())
        val l = LiveSyncLogic.trackRecords(base, mapOf("s1" to "p=5", "s2" to "p=20"), 100)
        assertEquals(Rec(10, "p=5"), l.items["s1"])
        assertEquals(Rec(100, "p=20"), l.items["s2"])
    }

    @Test fun newerRecordWins() {
        val a = Log(mapOf("s1" to Rec(100, "p=20")), emptyMap())
        val b = Log(mapOf("s1" to Rec(150, "p=40")), emptyMap())
        val m = LiveSyncLogic.merge(listOf(a, b), 200)
        assertEquals(Rec(150, "p=40"), m.items["s1"])
        assertEquals(listOf("s1" to "p=40"), LiveSyncLogic.newestRecords(m, 30))
    }

    @Test fun finishedResumePointIsRemovedOnAllDevices() {
        val base = Log(mapOf("m1" to Rec(10, "p=5")), emptyMap())
        val a = LiveSyncLogic.trackRecords(base, emptyMap(), 100)    // watched to the end on device A
        val m = LiveSyncLogic.merge(listOf(base, a), 200)
        assertTrue(m.items.isEmpty())
    }

    @Test fun sameTimeTwoValuesGivesTheSameResultEverywhere() {
        val a = Log(mapOf("s1" to Rec(100, "aaa")), emptyMap())
        val b = Log(mapOf("s1" to Rec(100, "bbb")), emptyMap())
        assertEquals(LiveSyncLogic.merge(listOf(a, b), 200), LiveSyncLogic.merge(listOf(b, a), 200))
        assertEquals("bbb", LiveSyncLogic.merge(listOf(a, b), 200).items["s1"]?.value)
    }

    // ---- General ----

    @Test fun mergeIsStableAndOrderFree() {
        val a = Log(mapOf("x" to Rec(100), "y" to Rec(50)), mapOf("z" to 90L))
        val b = Log(mapOf("z" to Rec(80), "y" to Rec(120)), mapOf("x" to 60L))
        val m1 = LiveSyncLogic.merge(listOf(a, b), 200)
        assertEquals(m1, LiveSyncLogic.merge(listOf(b, a), 200))
        assertEquals(m1, LiveSyncLogic.merge(listOf(m1, a, b), 200))
        assertEquals(m1, LiveSyncLogic.merge(listOf(m1), 200))
    }

    @Test fun oldTombstonesAreForgotten() {
        val now = 1000 * day
        val l = Log(emptyMap(), mapOf("old" to now - 91 * day, "new" to now - 89 * day))
        assertEquals(setOf("new"), LiveSyncLogic.merge(listOf(l), now).gone.keys)
    }

    @Test fun tombstonesAreCapped() {
        val g = (1..LiveSyncLogic.MAX_TOMBSTONES + 20).associate { "id$it" to it.toLong() }
        val m = LiveSyncLogic.merge(listOf(Log(emptyMap(), g)), 1000)
        assertEquals(LiveSyncLogic.MAX_TOMBSTONES, m.gone.size)
        assertFalse(m.gone.containsKey("id1"))   // the oldest go first
    }

    @Test fun keepOnlyDropsEntriesButKeepsTombstones() {
        val m = Log(mapOf("a" to Rec(3), "b" to Rec(2), "c" to Rec(1)), mapOf("d" to 5L))
        val k = LiveSyncLogic.keepOnly(m, LiveSyncLogic.recentIds(m, 2))
        assertEquals(setOf("a", "b"), k.items.keys)
        assertEquals(mapOf("d" to 5L), k.gone)
    }

    @Test fun jsonRoundTrip() {
        val l = Log(mapOf("a" to Rec(100), "b" to Rec(200, "p|1:2")), mapOf("c" to 300L))
        assertEquals(l, LiveSyncLogic.fromJson(LiveSyncLogic.toJson(l)))
        assertEquals(l, LiveSyncLogic.fromJsonText(LiveSyncLogic.toJson(l).toString()))
    }

    @Test fun brokenJsonIsEmptyOrSkipped() {
        assertEquals(EMPTY, LiveSyncLogic.fromJsonText(null))
        assertEquals(EMPTY, LiveSyncLogic.fromJsonText("not json"))
        val l = LiveSyncLogic.fromJsonText("""{"i":{"a":[100],"b":"x","c":[]},"g":{"d":"x","e":5}}""")
        assertEquals(mapOf("a" to Rec(100)), l.items)
        assertEquals(mapOf("e" to 5L), l.gone)
    }

    @Test fun emptyIdsAreIgnored() {
        assertTrue(LiveSyncLogic.trackSet(EMPTY, listOf("", "a", "a"), 1).items.keys == setOf("a"))
        assertTrue(LiveSyncLogic.trackRecent(EMPTY, listOf("", "a"), 1).items.keys == setOf("a"))
    }
}
