package com.ipvio.stream

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** #102: live addresses are never movies (pure, no Android). */
class LiveUrlLogicTest {

    @Test
    fun xtreamLiveAddress_isLive() {
        assertTrue(LiveUrlLogic.isLiveUrl("http://provider.example:8080/live/user/pass/123.ts"))
    }

    @Test
    fun stalkerLiveAddress_isLive() {
        assertTrue(LiveUrlLogic.isLiveUrl("stalker://portal.example/live/abc"))
    }

    @Test
    fun movieAndSeriesAddresses_areNotLive() {
        assertFalse(LiveUrlLogic.isLiveUrl("http://provider.example:8080/movie/user/pass/45.mkv"))
        assertFalse(LiveUrlLogic.isLiveUrl("http://provider.example:8080/series/user/pass/9.mp4"))
    }

    @Test
    fun plainM3uAddress_withoutLiveSegment_isNotLive() {
        assertFalse(LiveUrlLogic.isLiveUrl("http://provider.example/stream/123"))
    }

    @Test
    fun xtreamLiveAddressWithoutLiveSegment_isLive() {
        assertTrue(LiveUrlLogic.isLive("http://provider.example:8080/user/pass/12345"))
        assertTrue(LiveUrlLogic.isLive("http://provider.example:8080/user/pass/12345.ts"))
        assertTrue(LiveUrlLogic.isLive("http://provider.example/user/pass/12345.m3u8?token=x"))
    }

    @Test
    fun movieSeriesAndFileAddresses_areNeverLiveByTail() {
        assertFalse(LiveUrlLogic.isLive("http://provider.example:8080/movie/user/pass/12345.mkv"))
        assertFalse(LiveUrlLogic.isLive("http://provider.example:8080/series/user/pass/12345.mp4"))
        assertFalse(LiveUrlLogic.isLive("http://provider.example:8080/user/pass/12345.mkv"))
        assertFalse(LiveUrlLogic.isLive("http://provider.example/stream/123"))          // no user and password
        assertFalse(LiveUrlLogic.isLive("http://provider.example/user/pass/film-123"))  // not a number
        assertFalse(LiveUrlLogic.isLive("http://provider.example/12345"))
    }
}
