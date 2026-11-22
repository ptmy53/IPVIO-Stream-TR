package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure rules of the Wishbox round 2: sync offsets (W-005, W-007) and the "What's new" popup (W-004). */
class WishboxRound2Test {
    @Test fun subtitleDelayShiftsTextPositionBack() {
        assertEquals(9_500_000L, AvSyncLogic.textPositionUs(10_000_000L, 500))
        assertEquals(10_500_000L, AvSyncLogic.textPositionUs(10_000_000L, -500))
    }

    @Test fun audioDelayShiftsVideoPositionForward() {
        assertEquals(10_250_000L, AvSyncLogic.videoPositionUs(10_000_000L, 250))
        assertEquals(10_000_000L, AvSyncLogic.videoPositionUs(10_000_000L, 0))
    }

    @Test fun offsetsAreClamped() {
        assertEquals(AvSyncLogic.SUB_MAX_MS, AvSyncLogic.clampSub(99_000))
        assertEquals(-AvSyncLogic.AV_MAX_MS, AvSyncLogic.clampAv(-99_000))
    }

    @Test fun labels() {
        assertEquals("0 s", AvSyncLogic.label(0))
        assertEquals("+0,5 s", AvSyncLogic.label(500))
        assertEquals("−1,25 s", AvSyncLogic.label(-1250))
        assertEquals("+2 s", AvSyncLogic.label(2000))
        assertEquals("+0.1 s", AvSyncLogic.label(100, comma = false))
    }

    @Test fun whatsNewOnlyAfterAnUpdate() {
        assertTrue(WhatsNewLogic.shouldShow(6, 7, freshInstall = false, hasNotes = true))
        assertFalse(WhatsNewLogic.shouldShow(0, 7, freshInstall = true, hasNotes = true))
        assertFalse(WhatsNewLogic.shouldShow(7, 7, freshInstall = false, hasNotes = true))
        assertFalse(WhatsNewLogic.shouldShow(6, 7, freshInstall = false, hasNotes = false))
        // Update from a version before this feature: nothing was stored yet
        assertTrue(WhatsNewLogic.shouldShow(0, 7, freshInstall = false, hasNotes = true))
    }
}
