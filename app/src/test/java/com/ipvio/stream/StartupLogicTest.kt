package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupLogicTest {
    @Test fun silentRefreshAfterStoredListWaits() {
        assertEquals(StartupLogic.REFRESH_DELAY_MS, StartupLogic.refreshDelayMs(shownFromStore = true, userStarted = false))
    }
    @Test fun refreshWithoutStoredListStartsAtOnce() {
        assertEquals(0L, StartupLogic.refreshDelayMs(shownFromStore = false, userStarted = false))
    }
    @Test fun refreshStartedByTheUserStartsAtOnce() {
        assertEquals(0L, StartupLogic.refreshDelayMs(shownFromStore = true, userStarted = true))
        assertEquals(0L, StartupLogic.refreshDelayMs(shownFromStore = false, userStarted = true))
    }
    @Test fun delaysStayShort() {
        assertTrue(StartupLogic.REFRESH_DELAY_MS in 1_000L..10_000L)
        assertTrue(StartupLogic.CATCHUP_DELAY_MS in 1_000L..10_000L)
    }
}
