package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaProbeLogicTest {
    private fun video(mime: String, w: Int, h: Int, transfer: Int = -1, codecs: String? = null) =
        MediaProbeLogic.Track(mime, codecs, w, h, transfer)
    private fun audio(mime: String, ch: Int, lang: String?) = MediaProbeLogic.Track(mime, channels = ch, language = lang)
    private fun sub(mime: String, lang: String?) = MediaProbeLogic.Track(mime, language = lang)
    private val names = mapOf("de" to "Deutsch", "en" to "Englisch")
    private fun line(info: MediaProbeLogic.Info) = MediaProbeLogic.line(info, { names[it] }, "Ton", "Untertitel")

    @Test fun uhdHdr10WithDolbyVisionAndAtmos() {
        val info = MediaProbeLogic.describe(listOf(
            video(MediaProbeLogic.VIDEO_H265, 3840, 2160, MediaProbeLogic.TRANSFER_ST2084),
            video(MediaProbeLogic.VIDEO_DOLBY_VISION, 3840, 2160, codecs = "dvhe.08.06"),
            audio(MediaProbeLogic.AUDIO_E_AC3_JOC, 6, "de"),
            audio(MediaProbeLogic.AUDIO_AC3, 6, "en"),
            sub("application/x-media3-cues", "de"),
        ))
        assertEquals("4K", info.quality)
        assertEquals("HEVC", info.videoCodec)
        assertEquals(listOf("HDR10", "Dolby Vision"), info.hdr)
        assertEquals(
            "4K  ·  HEVC  ·  HDR10  ·  Dolby Vision  ·  Ton: Deutsch (E-AC-3 Atmos 5.1), Englisch (AC-3 5.1)  ·  Untertitel: Deutsch",
            line(info)
        )
    }

    @Test fun widescreen4kCountsAs4k() =
        assertEquals("4K", MediaProbeLogic.describe(listOf(video(MediaProbeLogic.VIDEO_H265, 3840, 1600))).quality)

    @Test fun hlgIsDetected() =
        assertEquals(listOf("HLG"), MediaProbeLogic.describe(listOf(video(MediaProbeLogic.VIDEO_H265, 1920, 1080, MediaProbeLogic.TRANSFER_HLG))).hdr)

    @Test fun sdrShowsNoHdrPart() {
        val info = MediaProbeLogic.describe(listOf(video(MediaProbeLogic.VIDEO_H264, 1920, 1080, 3), audio(MediaProbeLogic.AUDIO_AAC, 2, "de")))
        assertTrue(info.hdr.isEmpty())
        assertEquals("FHD  ·  H.264  ·  Ton: Deutsch (AAC 2.0)", line(info))
    }

    @Test fun dolbyVisionFromCodecsStringOnHevcTrack() =
        assertTrue(MediaProbeLogic.isDolbyVision(video(MediaProbeLogic.VIDEO_H265, 3840, 2160, codecs = "hvc1.2.4.L153, dvh1.08.06")))

    @Test fun dolbyVisionTrackWithAv1BaseLayer() =
        assertEquals("AV1", MediaProbeLogic.videoCodec(video(MediaProbeLogic.VIDEO_DOLBY_VISION, 3840, 2160, codecs = "dav1.10.06")))

    @Test fun plainHevcIsNotDolbyVision() =
        assertFalse(MediaProbeLogic.isDolbyVision(video(MediaProbeLogic.VIDEO_H265, 3840, 2160, codecs = "hvc1.2.4.L153")))

    @Test fun undeterminedLanguageShowsOnlyTechnicalPart() {
        val info = MediaProbeLogic.describe(listOf(audio(MediaProbeLogic.AUDIO_DTS_HD, 8, "und")))
        assertEquals("Ton: DTS-HD 7.1", line(info))
    }

    @Test fun unknownLanguageNameFallsBackToCode() {
        val info = MediaProbeLogic.describe(listOf(audio(MediaProbeLogic.AUDIO_TRUEHD, 8, "ja")))
        assertEquals("Ton: ja (TrueHD 7.1)", line(info))
    }

    @Test fun subtitlesAreDeduplicatedAndUnknownOnesDropped() {
        val info = MediaProbeLogic.describe(listOf(
            sub("application/x-subrip", "de"), sub("text/vtt", "de"), sub("application/pgs", "en"), sub("application/pgs", null),
        ))
        assertEquals(listOf("de", "en"), info.subtitles)
    }

    @Test fun nothingFoundGivesEmptyInfoAndLine() {
        val info = MediaProbeLogic.describe(listOf(MediaProbeLogic.Track("application/id3")))
        assertTrue(info.isEmpty)
        assertEquals("", line(info))
    }

    @Test fun unknownCodecsGetReadableFallbacks() {
        assertEquals("ALAC", MediaProbeLogic.audioCodec("audio/alac"))
        assertNull(MediaProbeLogic.videoCodec(video("video/x-unknown", 640, 480)))
        assertEquals("SD", MediaProbeLogic.describe(listOf(video("video/x-unknown", 640, 480))).quality)
    }

    @Test fun channelLabels() {
        assertNull(MediaProbeLogic.channels(-1))
        assertEquals("1.0", MediaProbeLogic.channels(1))
        assertEquals("5.1", MediaProbeLogic.channels(6))
        assertEquals("3ch", MediaProbeLogic.channels(3))
    }
}
