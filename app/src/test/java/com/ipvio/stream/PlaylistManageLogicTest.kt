package com.ipvio.stream

import com.ipvio.stream.PlaylistManageLogic.Draft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistManageLogicTest {
    private val base = Draft("Home", 0, true, listOf("http://a.tv:80"))

    @Test fun unchangedDraftHasNoChanges() = assertFalse(PlaylistManageLogic.changes(base, base.copy()).any)
    @Test fun renameIsDetectedIgnoringSpaces() {
        assertFalse(PlaylistManageLogic.changes(base, base.copy(name = " Home ")).rename)
        assertTrue(PlaylistManageLogic.changes(base, base.copy(name = "Away")).rename)
    }
    @Test fun blankNameIsNotAChange() = assertFalse(PlaylistManageLogic.changes(base, base.copy(name = "  ")).rename)
    @Test fun modeAndShownAreDetected() {
        val c = PlaylistManageLogic.changes(base, base.copy(mode = 1, shown = false))
        assertTrue(c.mode); assertTrue(c.shown); assertFalse(c.rename); assertFalse(c.backups)
    }
    @Test fun emptyBackupEntriesAreIgnored() {
        assertFalse(PlaylistManageLogic.changes(base, base.copy(backups = listOf("http://a.tv:80", "", "  "))).backups)
        assertTrue(PlaylistManageLogic.changes(base, base.copy(backups = listOf("http://a.tv:80", "http://b.tv:80"))).backups)
        assertEquals(true, PlaylistManageLogic.changes(base, base.copy(backups = emptyList())).any)
    }
    @Test fun refreshPlanChangeIsDetected() {
        assertFalse(PlaylistManageLogic.changes(base, base.copy()).refresh)
        assertTrue(PlaylistManageLogic.changes(base, base.copy(refresh = PlaylistRefreshLogic.Spec(2))).refresh)
    }
}
