package com.ipvio.stream

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** #15: trailer ids are checked before they go into the embed page (pure, no Android). */
class TrailerLogicTest {

    @Test
    fun validYoutubeIds_areAccepted() {
        assertTrue(TrailerLogic.validKey("dQw4w9WgXcQ"))
        assertTrue(TrailerLogic.validKey("abc-_123XYZ"))
    }

    @Test
    fun invalidIds_areRejected() {
        assertFalse(TrailerLogic.validKey(null))
        assertFalse(TrailerLogic.validKey(""))
        assertFalse(TrailerLogic.validKey("short"))
        assertFalse(TrailerLogic.validKey("x\"><script>alert(1)</script>"))
        assertFalse(TrailerLogic.validKey("https://youtu.be/dQw4w9WgXcQ"))
    }

    @Test
    fun embedHtml_onlyForValidIds_andUsesEmbedHost() {
        assertNull(TrailerLogic.embedHtml("bad id!"))
        val html = TrailerLogic.embedHtml("dQw4w9WgXcQ")!!
        assertTrue(html.contains("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ?"))
        assertTrue(html.contains("allowfullscreen"))
    }

    @Test
    fun diagLine_acceptsOnlyKnownConsoleLines() {
        assertTrue(TrailerLogic.diagLine("IPVIO_TRAILER_ERR 153") == "Trailer: player error 153")
        assertTrue(TrailerLogic.diagLine("IPVIO_TRAILER_STATE -1") == "Trailer: player state -1")
        assertTrue(TrailerLogic.diagLine("IPVIO_TRAILER_LOAD") == "Trailer: iframe loaded")
        assertNull(TrailerLogic.diagLine(null))
        assertNull(TrailerLogic.diagLine("IPVIO_TRAILER_ERR abc"))
        assertNull(TrailerLogic.diagLine("IPVIO_TRAILER_ERR 12345"))
        assertNull(TrailerLogic.diagLine("Uncaught TypeError: x"))
    }

    @Test
    fun embedHtml_enablesPlayerReports() {
        val html = TrailerLogic.embedHtml("dQw4w9WgXcQ")!!
        assertTrue(html.contains("enablejsapi=1"))
        assertTrue(html.contains("IPVIO_TRAILER_ERR"))
    }

    @Test
    fun attempts_useDifferentOrigins_andRejectBadInput() {
        val a0 = TrailerLogic.attempt(0, "dQw4w9WgXcQ")!!
        val a1 = TrailerLogic.attempt(1, "dQw4w9WgXcQ")!!
        val a2 = TrailerLogic.attempt(2, "dQw4w9WgXcQ")!!
        assertTrue(a0.baseUrl == "https://www.youtube-nocookie.com")
        assertTrue(a1.baseUrl == "https://com.ipvio.stream" && a1.html.contains("origin=https://com.ipvio.stream"))
        assertTrue(a1.html.contains("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ?"))
        assertTrue(a2.html.contains("https://www.youtube.com/embed/dQw4w9WgXcQ?"))
        assertNull(TrailerLogic.attempt(TrailerLogic.ATTEMPTS, "dQw4w9WgXcQ"))
        assertNull(TrailerLogic.attempt(-1, "dQw4w9WgXcQ"))
        assertNull(TrailerLogic.attempt(0, "bad id!"))
    }

    @Test
    fun consoleLines_playingAndBlockedErrors() {
        assertTrue(TrailerLogic.isPlaying("IPVIO_TRAILER_STATE 1"))
        assertFalse(TrailerLogic.isPlaying("IPVIO_TRAILER_STATE 3"))
        assertFalse(TrailerLogic.isPlaying(null))
        assertTrue(TrailerLogic.playerError("IPVIO_TRAILER_ERR 153") == 153)
        assertNull(TrailerLogic.playerError("IPVIO_TRAILER_STATE 1"))
        assertTrue(TrailerLogic.isVideoBlocked(101))
        assertTrue(TrailerLogic.isVideoBlocked(150))
        assertFalse(TrailerLogic.isVideoBlocked(153))
        assertFalse(TrailerLogic.isVideoBlocked(null))
    }

    @Test
    fun decoderLine_tellsHardwareSoftwareAndMissing() {
        val line = TrailerLogic.decoderLine(listOf(
            TrailerLogic.Decoder("OMX.amlogic.avc.decoder", listOf("video/avc")),
            TrailerLogic.Decoder("OMX.google.vp9.decoder", listOf("video/x-vnd.on2.vp9")),
            TrailerLogic.Decoder("c2.android.hevc.decoder", listOf("video/hevc")),
        ))
        assertTrue(line == "Trailer: decoders H.264=hw VP9=sw AV1=no HEVC=sw")
        assertTrue(TrailerLogic.decoderLine(emptyList()) == "Trailer: decoders H.264=no VP9=no AV1=no HEVC=no")
    }

    @Test
    fun diagLine_info_isParsedAndRejectsOddInput() {
        assertTrue(TrailerLogic.diagLine("IPVIO_TRAILER_INFO t=3.4 v=100 m=0 q=hd720") == "Trailer: info t=3.4s vol=100 muted=0 quality=hd720")
        assertTrue(TrailerLogic.diagLine("IPVIO_TRAILER_INFO t=0 v=0 m=1 q=na") == "Trailer: info t=0s vol=0 muted=1 quality=na")
        assertNull(TrailerLogic.diagLine("IPVIO_TRAILER_INFO t=x v=1 m=0 q=na"))
        assertNull(TrailerLogic.diagLine("IPVIO_TRAILER_INFO t=1 v=1 m=0 q=a b"))
    }

    @Test
    fun externalUrl_usesValidIdOtherwiseSearch() {
        assertTrue(TrailerLogic.externalUrl("dQw4w9WgXcQ", "X") == "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        assertTrue(TrailerLogic.externalUrl(null, "Avatar 2") == "https://www.youtube.com/results?search_query=Avatar+2+Trailer")
        assertTrue(TrailerLogic.externalUrl("bad id&x=1", "A").startsWith("https://www.youtube.com/results?search_query="))
    }
}
