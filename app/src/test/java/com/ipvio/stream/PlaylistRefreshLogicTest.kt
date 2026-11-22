package com.ipvio.stream

import com.ipvio.stream.PlaylistRefreshLogic.GENERAL
import com.ipvio.stream.PlaylistRefreshLogic.Spec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class PlaylistRefreshLogicTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val now = 1_800_000_000_000L

    @Test fun encodeDecodeRoundTrip() {
        val s = Spec(3, 24, 6, 30, 5)
        assertEquals(s, PlaylistRefreshLogic.decode(PlaylistRefreshLogic.encode(s)))
    }
    @Test fun damagedTextGivesGeneral() {
        for (t in listOf(null, "", "x", "2,1,1", "9,12,4,0,2", "a,b,c,d,e")) assertEquals(GENERAL, PlaylistRefreshLogic.decode(t).mode)
    }
    @Test fun numbersStayInRange() {
        val s = PlaylistRefreshLogic.decode("1,999,99,99,99")
        assertEquals(168, s.intervalH); assertEquals(23, s.hour); assertEquals(59, s.minute); assertEquals(7, s.dow)
    }
    @Test fun generalFollowsTheGeneralPlan() {
        val g = UpdatePlan(2, 12, 3, 0)
        assertEquals(g, PlaylistRefreshLogic.plan(Spec(), g))
        assertEquals(3, PlaylistRefreshLogic.plan(Spec(3), g).mode)
    }
    @Test fun otherListWithoutOwnPlanIsNeverDue() = assertFalse(PlaylistRefreshLogic.otherDue(Spec(), 0L, now, utc))
    @Test fun byHandIsNeverDue() = assertFalse(PlaylistRefreshLogic.otherDue(Spec(4), 0L, now, utc))
    @Test fun everyXHoursIsDueAfterTheInterval() {
        val s = Spec(1, 6)
        assertFalse(PlaylistRefreshLogic.otherDue(s, now - 5 * 3_600_000L, now, utc))
        assertTrue(PlaylistRefreshLogic.otherDue(s, now - 7 * 3_600_000L, now, utc))
        assertTrue(PlaylistRefreshLogic.otherDue(s, 0L, now, utc))
    }
    @Test fun dailyAndWeeklyAreDueWhenNeverRefreshed() {
        assertTrue(PlaylistRefreshLogic.otherDue(Spec(2), 0L, now, utc))
        assertTrue(PlaylistRefreshLogic.otherDue(Spec(3), 0L, now, utc))
        assertFalse(PlaylistRefreshLogic.otherDue(Spec(2), now, now, utc))
    }
    @Test fun onlyScheduledModesNeedTheJob() {
        assertEquals(listOf(false, false, true, true, true, false), listOf(GENERAL, 0, 1, 2, 3, 4).map { PlaylistRefreshLogic.needsJob(Spec(it)) })
    }
}
