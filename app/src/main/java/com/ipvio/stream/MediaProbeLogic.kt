package com.ipvio.stream

/**
 * Pure logic for the stream check on the detail page (T-033, issue #24): turns the tracks that Media3 read from the
 * header of a stream into one short line, e.g. "4K · HEVC · HDR10 · Dolby Vision · Ton: Deutsch (E-AC-3 5.1 Atmos)".
 * No Android imports, so JVM tests cover it. The MIME types and color constants mirror Media3 (guarded by Media3ContractTest).
 */
object MediaProbeLogic {
    // Media3 MimeTypes (mirrored, see Media3ContractTest)
    const val VIDEO_H264 = "video/avc"
    const val VIDEO_H265 = "video/hevc"
    const val VIDEO_AV1 = "video/av01"
    const val VIDEO_VP9 = "video/x-vnd.on2.vp9"
    const val VIDEO_MPEG2 = "video/mpeg2"
    const val VIDEO_DOLBY_VISION = "video/dolby-vision"
    const val AUDIO_AAC = "audio/mp4a-latm"
    const val AUDIO_AC3 = "audio/ac3"
    const val AUDIO_E_AC3 = "audio/eac3"
    const val AUDIO_E_AC3_JOC = "audio/eac3-joc"
    const val AUDIO_AC4 = "audio/ac4"
    const val AUDIO_TRUEHD = "audio/true-hd"
    const val AUDIO_DTS = "audio/vnd.dts"
    const val AUDIO_DTS_HD = "audio/vnd.dts.hd"
    const val AUDIO_DTS_EXPRESS = "audio/vnd.dts.hd;profile=lbr"
    const val AUDIO_DTS_X = "audio/vnd.dts.uhd;profile=p2"
    const val AUDIO_MPEG = "audio/mpeg"
    const val AUDIO_OPUS = "audio/opus"
    const val AUDIO_FLAC = "audio/flac"

    // Media3 C.COLOR_TRANSFER_* (mirrored, see Media3ContractTest)
    const val TRANSFER_ST2084 = 6
    const val TRANSFER_HLG = 7

    /** One track as read from the stream header. [colorTransfer] = Media3 value, or -1 when unknown. */
    data class Track(
        val mime: String?,
        val codecs: String? = null,
        val width: Int = -1,
        val height: Int = -1,
        val colorTransfer: Int = -1,
        val channels: Int = -1,
        val language: String? = null,
    )

    data class Audio(val language: String?, val codec: String, val channels: String?)

    data class Info(
        val quality: String?,
        val videoCodec: String?,
        val hdr: List<String>,
        val audio: List<Audio>,
        val subtitles: List<String>,
    ) {
        val isEmpty: Boolean get() = quality == null && videoCodec == null && hdr.isEmpty() && audio.isEmpty() && subtitles.isEmpty()
    }

    fun isVideo(mime: String?) = mime?.startsWith("video/") == true
    fun isAudio(mime: String?) = mime?.startsWith("audio/") == true

    /** Text tracks: text/... and the subtitle formats Media3 files under application/... */
    fun isSubtitle(mime: String?): Boolean = mime != null && (mime.startsWith("text/") || mime in SUB_APP)

    private val SUB_APP = setOf(
        "application/x-subrip", "application/ttml+xml", "application/x-mp4-vtt", "application/x-quicktime-tx3g",
        "application/pgs", "application/dvbsubs", "application/vobsub", "application/x-ssa", "application/cea-608",
        "application/cea-708", "application/x-media3-cues",
    )

    fun describe(tracks: List<Track>): Info {
        val videos = tracks.filter { isVideo(it.mime) }
        val main = videos.maxByOrNull { it.width.toLong() * it.height }
        // Same rule as the player: widescreen 4K (3840x1600) counts as 4K
        val quality = main?.takeIf { it.width > 0 && it.height > 0 }?.let { qualityName(maxOf(it.height, it.width * 9 / 16)) }
        val hdr = ArrayList<String>()
        when (main?.colorTransfer) {
            TRANSFER_ST2084 -> hdr.add("HDR10")
            TRANSFER_HLG -> hdr.add("HLG")
        }
        if (videos.any { isDolbyVision(it) }) hdr.add("Dolby Vision")
        val audio = tracks.filter { isAudio(it.mime) }.map { Audio(lang(it.language), audioCodec(it.mime), channels(it.channels)) }
        val subs = tracks.filter { isSubtitle(it.mime) }.mapNotNull { lang(it.language) }.distinct()
        return Info(quality, main?.let { videoCodec(it) }, hdr, audio, subs)
    }

    /** Dolby Vision: own MIME type, or a codecs string "dvhe.08.06", "dvh1…", "dav1…", "dva1…", "dvav…". */
    fun isDolbyVision(t: Track): Boolean {
        if (t.mime == VIDEO_DOLBY_VISION) return true
        return t.codecs?.split(',')?.any { c -> c.trim().let { it.startsWith("dvh") || it.startsWith("dav1") || it.startsWith("dva") } } == true
    }

    fun videoCodec(t: Track): String? = when (t.mime) {
        VIDEO_H264 -> "H.264"
        VIDEO_H265 -> "HEVC"
        VIDEO_AV1 -> "AV1"
        VIDEO_VP9 -> "VP9"
        VIDEO_MPEG2 -> "MPEG-2"
        // Dolby Vision as its own track type: the base layer is HEVC or AV1
        VIDEO_DOLBY_VISION -> if (t.codecs?.trim()?.startsWith("dav1") == true) "AV1" else "HEVC"
        else -> null
    }

    fun audioCodec(mime: String?): String = when (mime) {
        AUDIO_AAC -> "AAC"
        AUDIO_AC3 -> "AC-3"
        AUDIO_E_AC3 -> "E-AC-3"
        AUDIO_E_AC3_JOC -> "E-AC-3 Atmos"
        AUDIO_AC4 -> "AC-4"
        AUDIO_TRUEHD -> "TrueHD"
        AUDIO_DTS -> "DTS"
        AUDIO_DTS_HD -> "DTS-HD"
        AUDIO_DTS_EXPRESS -> "DTS Express"
        AUDIO_DTS_X -> "DTS:X"
        AUDIO_MPEG -> "MP3"
        AUDIO_OPUS -> "Opus"
        AUDIO_FLAC -> "FLAC"
        else -> mime?.substringAfter('/')?.uppercase() ?: "?"
    }

    fun channels(n: Int): String? = when {
        n <= 0 -> null
        n == 1 -> "1.0"
        n == 2 -> "2.0"
        n == 6 -> "5.1"
        n == 8 -> "7.1"
        else -> "${n}ch"
    }

    /** Language code of a track; "und" (undetermined) and blanks count as unknown. */
    private fun lang(code: String?): String? = code?.trim()?.takeIf { it.isNotEmpty() && !it.equals("und", true) }

    /**
     * The line for the detail page. [langName] turns a language code into a display name (the app passes [langLabel]),
     * [audioWord] / [subWord] are the translated words for "Ton" and "Untertitel". Empty string = nothing to show.
     */
    fun line(info: Info, langName: (String) -> String?, audioWord: String, subWord: String): String {
        val parts = ArrayList<String>()
        info.quality?.let { parts.add(it) }
        info.videoCodec?.let { parts.add(it) }
        parts.addAll(info.hdr)
        if (info.audio.isNotEmpty()) {
            parts.add(audioWord + ": " + info.audio.joinToString(", ") { a ->
                val tech = listOfNotNull(a.codec, a.channels).joinToString(" ")
                val name = a.language?.let { langName(it) ?: it }
                if (name == null) tech else "$name ($tech)"
            })
        }
        if (info.subtitles.isNotEmpty()) parts.add(subWord + ": " + info.subtitles.joinToString(", ") { langName(it) ?: it })
        return parts.joinToString("  ·  ")
    }
}
