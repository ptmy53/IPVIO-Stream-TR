package com.ipvio.stream

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.exoplayer.ForwardingRenderer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.RenderersFactory

/**
 * Pure rules for the sync offsets in the player (W-005, W-007), without Android so they can be tested.
 * Subtitle delay: positive = subtitles appear later. Audio delay: positive = sound comes later than the picture.
 */
object AvSyncLogic {
    const val SUB_MAX_MS = 10_000
    const val AV_MAX_MS = 3_000

    fun clampSub(ms: Int): Int = ms.coerceIn(-SUB_MAX_MS, SUB_MAX_MS)
    fun clampAv(ms: Int): Int = ms.coerceIn(-AV_MAX_MS, AV_MAX_MS)

    /** Position the text renderer works with: earlier position = subtitles later. */
    fun textPositionUs(positionUs: Long, subMs: Int): Long = positionUs - subMs * 1000L

    /** Position the video renderer works with: a later position shows the picture earlier, so the sound seems later. */
    fun videoPositionUs(positionUs: Long, avMs: Int): Long = positionUs + avMs * 1000L

    /** Display text: "0 s", "+0,5 s", "−1,25 s" ([comma] = false gives "+0.5 s" for English). */
    fun label(ms: Int, comma: Boolean = true): String {
        if (ms == 0) return "0 s"
        val sign = if (ms > 0) "+" else "−"
        val a = kotlin.math.abs(ms)
        val whole = a / 1000
        val frac = (a % 1000).toString().padStart(3, '0').trimEnd('0')
        return sign + whole + (if (frac.isEmpty()) "" else (if (comma) "," else ".") + frac) + " s"
    }
}

/**
 * Sync offsets used by the player. The subtitle delay applies to the current title only (reset at every new start);
 * the audio delay is a device setting (e.g. a soundbar with latency) and is stored per profile.
 */
object PlayerSync {
    private const val PREFS = "ipvio_sync"

    /** Read on the playback thread by the renderers. */
    @Volatile private var subNow = 0
    @Volatile private var avNow = 0

    var subMs by mutableIntStateOf(0)
        private set
    var avMs by mutableIntStateOf(0)
        private set

    fun init(c: Context) {
        avMs = AvSyncLogic.clampAv(Profiles.sp(c, PREFS).getInt("av_ms", 0)); avNow = avMs
    }

    /** New title started: the subtitle delay does not carry over to the next movie or episode. */
    fun resetSub() { subMs = 0; subNow = 0 }

    fun setSub(ms: Int) { subMs = AvSyncLogic.clampSub(ms); subNow = subMs }

    /** Label in the UI language (decimal point for English). */
    fun label(ms: Int): String = AvSyncLogic.label(ms, Lang.code() != "en")

    fun setAv(c: Context, ms: Int) {
        avMs = AvSyncLogic.clampAv(ms); avNow = avMs
        Profiles.sp(c, PREFS).edit().putInt("av_ms", avMs).apply()
    }

    /**
     * Wraps the renderers of [base]: the text renderer works with the shifted position (subtitle delay), the video renderer
     * with the audio delay. The audio renderer stays the clock, so nothing else in the player changes.
     */
    fun wrap(base: RenderersFactory): RenderersFactory = RenderersFactory { handler, video, audio, text, meta ->
        base.createRenderers(handler, video, audio, text, meta).map { r ->
            when (r.trackType) {
                C.TRACK_TYPE_TEXT -> ShiftedText(r)
                C.TRACK_TYPE_VIDEO -> ShiftedVideo(r)
                else -> r
            }
        }.toTypedArray()
    }

    private class ShiftedText(r: Renderer) : ForwardingRenderer(r) {
        override fun render(positionUs: Long, elapsedRealtimeUs: Long) {
            super.render(AvSyncLogic.textPositionUs(positionUs, subNow), elapsedRealtimeUs)
        }
    }

    private class ShiftedVideo(r: Renderer) : ForwardingRenderer(r) {
        override fun render(positionUs: Long, elapsedRealtimeUs: Long) {
            super.render(AvSyncLogic.videoPositionUs(positionUs, avNow), elapsedRealtimeUs)
        }
    }
}
