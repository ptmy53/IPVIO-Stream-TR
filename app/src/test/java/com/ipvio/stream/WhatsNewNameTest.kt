package com.ipvio.stream

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Issue 12: the popup also appears when only the version name changes (test build and release share the versionCode). */
class WhatsNewNameTest {
    @Test fun testBuildThenRelease_showsAgain() =
        assertTrue(WhatsNewLogic.shouldShow(7, "1.4.2-test", 7, "1.4.2", freshInstall = false, hasNotes = true))

    @Test fun sameName_notAgain() =
        assertFalse(WhatsNewLogic.shouldShow(7, "1.4.2", 7, "1.4.2", freshInstall = false, hasNotes = true))

    @Test fun nameStoredByOlderApp_onlyCodeCounts() {
        assertFalse(WhatsNewLogic.shouldShow(7, null, 7, "1.4.2", freshInstall = false, hasNotes = true))
        assertTrue(WhatsNewLogic.shouldShow(6, null, 7, "1.4.2", freshInstall = false, hasNotes = true))
    }

    @Test fun freshInstallOrNoNotes_never() {
        assertFalse(WhatsNewLogic.shouldShow(7, "1.4.2-test", 7, "1.4.2", freshInstall = true, hasNotes = true))
        assertFalse(WhatsNewLogic.shouldShow(7, "1.4.2-test", 7, "1.4.2", freshInstall = false, hasNotes = false))
    }
}
