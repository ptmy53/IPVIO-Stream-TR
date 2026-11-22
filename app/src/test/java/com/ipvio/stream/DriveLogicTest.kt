package com.ipvio.stream

import org.junit.Assert.*
import org.junit.Test

class DriveLogicTest {
    @Test fun fileNames() {
        assertEquals("ipvio_sync_ab12cd34.json", DriveLogic.fileName("ab12cd34"))
        assertEquals("ab12cd34", DriveLogic.deviceIdOf("ipvio_sync_ab12cd34.json"))
        assertNull(DriveLogic.deviceIdOf("ipvio_backup.json"))
        assertTrue(DriveLogic.isSyncFile("ipvio_sync_ab12cd34.json"))
        assertFalse(DriveLogic.isSyncFile("ipvio_backup.json"))
        assertFalse(DriveLogic.isSyncFile("ipvio_sync_.json"))
        assertFalse(DriveLogic.isSyncFile("ipvio_sync_a'b.json"))
        assertFalse(DriveLogic.isSyncFile("other.json"))
    }

    @Test fun badDeviceIdIsRefused() {
        var thrown = false
        try { DriveLogic.fileName("x' or name='y") } catch (e: IllegalArgumentException) { thrown = true }
        assertTrue(thrown)
    }

    @Test fun queries() {
        assertEquals("'1AbC_d-9' in parents and name contains 'ipvio_sync_' and trashed = false", DriveLogic.listQuery("1AbC_d-9"))
        assertEquals("'1AbC' in parents and name = 'ipvio_sync_ab12.json' and trashed = false", DriveLogic.fileQuery("1AbC", "ab12"))
        assertTrue(DriveLogic.folderQuery().startsWith("name = 'IPVIO Stream Sync'"))
        var thrown = false
        try { DriveLogic.listQuery("x' or '1'='1") } catch (e: IllegalArgumentException) { thrown = true }
        assertTrue(thrown)
    }

    @Test fun liveFiles() {
        assertEquals("ipvio_live_ab12cd34.json", DriveLogic.liveFileName("ab12cd34"))
        assertEquals("ab12cd34", DriveLogic.liveDeviceIdOf("ipvio_live_ab12cd34.json"))
        assertNull(DriveLogic.liveDeviceIdOf("ipvio_sync_ab12cd34.json"))
        assertNull(DriveLogic.liveDeviceIdOf("ipvio_live_a'b.json"))
        assertNull(DriveLogic.liveDeviceIdOf("ipvio_live_.json"))
        // The backup list never shows live files and the other way round
        assertFalse(DriveLogic.isSyncFile("ipvio_live_ab12cd34.json"))
        assertEquals("'1AbC' in parents and name contains 'ipvio_live_' and trashed = false", DriveLogic.liveListQuery("1AbC"))
        assertEquals("'1AbC' in parents and name = 'ipvio_live_ab12.json' and trashed = false", DriveLogic.liveFileQuery("1AbC", "ab12"))
        var thrown = false
        try { DriveLogic.liveListQuery("x' or '1'='1") } catch (e: IllegalArgumentException) { thrown = true }
        assertTrue(thrown)
    }

    @Test fun driveTimes() {
        assertEquals(1794652530123L, DriveLogic.parseTime("2026-11-14T10:35:30.123Z"))
        assertEquals(1794652530000L, DriveLogic.parseTime("2026-11-14T10:35:30Z"))
        assertEquals(0L, DriveLogic.parseTime("yesterday"))
        assertEquals(0L, DriveLogic.parseTime(null))
    }

    @Test fun pollAnswers() {
        assertEquals(DriveLogic.Poll.WAIT, DriveLogic.pollAnswer("authorization_pending"))
        assertEquals(DriveLogic.Poll.SLOWER, DriveLogic.pollAnswer("slow_down"))
        assertEquals(DriveLogic.Poll.DENIED, DriveLogic.pollAnswer("access_denied"))
        assertEquals(DriveLogic.Poll.EXPIRED, DriveLogic.pollAnswer("expired_token"))
        assertEquals(DriveLogic.Poll.FAILED, DriveLogic.pollAnswer("invalid_client"))
    }
}
