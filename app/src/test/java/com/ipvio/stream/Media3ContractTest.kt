package com.ipvio.stream

import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Test

/** Only test that imports Media3: guards the mirrored error codes in PlaybackErrorCodes. */
class Media3ContractTest {
    @Test fun errorCodes_matchMedia3() {
        assertEquals(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED, PlaybackErrorCodes.IO_NETWORK_CONNECTION_FAILED)
        assertEquals(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT, PlaybackErrorCodes.IO_NETWORK_CONNECTION_TIMEOUT)
        assertEquals(PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED, PlaybackErrorCodes.PARSING_CONTAINER_MALFORMED)
        assertEquals(PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED, PlaybackErrorCodes.PARSING_MANIFEST_MALFORMED)
        assertEquals(PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED, PlaybackErrorCodes.PARSING_CONTAINER_UNSUPPORTED)
        assertEquals(PlaybackException.ERROR_CODE_DECODER_INIT_FAILED, PlaybackErrorCodes.DECODER_INIT_FAILED)
        assertEquals(PlaybackException.ERROR_CODE_DECODING_FAILED, PlaybackErrorCodes.DECODING_FAILED)
        assertEquals(PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED, PlaybackErrorCodes.DECODING_FORMAT_UNSUPPORTED)
    }

    /** MIME types and color constants mirrored in MediaProbeLogic (T-033). */
    @Test fun mediaProbeConstants_matchMedia3() {
        assertEquals(MimeTypes.VIDEO_H264, MediaProbeLogic.VIDEO_H264)
        assertEquals(MimeTypes.VIDEO_H265, MediaProbeLogic.VIDEO_H265)
        assertEquals(MimeTypes.VIDEO_AV1, MediaProbeLogic.VIDEO_AV1)
        assertEquals(MimeTypes.VIDEO_VP9, MediaProbeLogic.VIDEO_VP9)
        assertEquals(MimeTypes.VIDEO_MPEG2, MediaProbeLogic.VIDEO_MPEG2)
        assertEquals(MimeTypes.VIDEO_DOLBY_VISION, MediaProbeLogic.VIDEO_DOLBY_VISION)
        assertEquals(MimeTypes.AUDIO_AAC, MediaProbeLogic.AUDIO_AAC)
        assertEquals(MimeTypes.AUDIO_AC3, MediaProbeLogic.AUDIO_AC3)
        assertEquals(MimeTypes.AUDIO_E_AC3, MediaProbeLogic.AUDIO_E_AC3)
        assertEquals(MimeTypes.AUDIO_E_AC3_JOC, MediaProbeLogic.AUDIO_E_AC3_JOC)
        assertEquals(MimeTypes.AUDIO_AC4, MediaProbeLogic.AUDIO_AC4)
        assertEquals(MimeTypes.AUDIO_TRUEHD, MediaProbeLogic.AUDIO_TRUEHD)
        assertEquals(MimeTypes.AUDIO_DTS, MediaProbeLogic.AUDIO_DTS)
        assertEquals(MimeTypes.AUDIO_DTS_HD, MediaProbeLogic.AUDIO_DTS_HD)
        assertEquals(MimeTypes.AUDIO_DTS_EXPRESS, MediaProbeLogic.AUDIO_DTS_EXPRESS)
        assertEquals(MimeTypes.AUDIO_DTS_X, MediaProbeLogic.AUDIO_DTS_X)
        assertEquals(MimeTypes.AUDIO_MPEG, MediaProbeLogic.AUDIO_MPEG)
        assertEquals(MimeTypes.AUDIO_OPUS, MediaProbeLogic.AUDIO_OPUS)
        assertEquals(MimeTypes.AUDIO_FLAC, MediaProbeLogic.AUDIO_FLAC)
        assertEquals(C.COLOR_TRANSFER_ST2084, MediaProbeLogic.TRANSFER_ST2084)
        assertEquals(C.COLOR_TRANSFER_HLG, MediaProbeLogic.TRANSFER_HLG)
        for (m in listOf(MimeTypes.APPLICATION_SUBRIP, MimeTypes.APPLICATION_TTML, MimeTypes.APPLICATION_PGS, MimeTypes.APPLICATION_DVBSUBS,
            MimeTypes.APPLICATION_VOBSUB, MimeTypes.TEXT_SSA, MimeTypes.TEXT_VTT, MimeTypes.APPLICATION_MEDIA3_CUES)) {
            assertEquals(m, true, MediaProbeLogic.isSubtitle(m))
        }
    }
}
