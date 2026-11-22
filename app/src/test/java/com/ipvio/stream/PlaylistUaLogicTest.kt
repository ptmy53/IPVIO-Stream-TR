package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** #90: default user-agent stays VLC; the app identifier is used only when a playlist opted in (pure, no Android). */
class PlaylistUaLogicTest {

    @Test
    fun default_isUnchangedVlcValue() {
        assertEquals("VLC/3.0.21 LibVLC/3.0.21", PlaylistUaLogic.pick(native = false, appVersion = "1.4.3"))
    }

    @Test
    fun nativeOptIn_usesAppIdentifierWithVersion() {
        assertEquals("IPVIOStream/1.4.3", PlaylistUaLogic.pick(native = true, appVersion = "1.4.3"))
    }

    @Test
    fun blankVersion_getsPlaceholder() {
        assertEquals("IPVIOStream/unknown", PlaylistUaLogic.pick(native = true, appVersion = " "))
    }

    @Test
    fun prefKey_isStableAndPerAddress() {
        assertEquals("ua_native_42", PlaylistUaLogic.prefKey(42))
        assertTrue(PlaylistUaLogic.prefKey(1) != PlaylistUaLogic.prefKey(2))
    }
}
