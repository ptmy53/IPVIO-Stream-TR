package com.ipvio.stream

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.inspector.MetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Stream check for the detail page (T-033, issue #24): reads the tracks of a movie or episode from the header of the
 * file with Media3's MetadataRetriever (resolution, HDR, Dolby Vision, audio tracks, subtitles). Nothing is decoded.
 * Xtream's get_vod_info leaves these values empty at many providers, so the app reads them itself.
 *
 * The check holds a connection to the provider for a moment. It stops as soon as the detail page closes (e.g. on Play),
 * so accounts with one connection are not blocked. Can be switched off in Settings (PlaybackPrefs.probe).
 * Results stay in memory for the app session, failures too (a broken stream is not asked again on every visit).
 */
object MediaProbe {
    private const val TAG = "MediaProbe"
    private const val TIMEOUT_MS = 20_000L
    private const val MAX_CACHED = 200
    /** Time on the detail page before the check starts. */
    const val START_DELAY_MS = 1_500L

    /** null = check failed or nothing found. */
    private val cache = object : LinkedHashMap<String, MediaProbeLogic.Info?>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, MediaProbeLogic.Info?>?) = size > MAX_CACHED
    }

    /** Pair(known, result): known = false when the URL was not checked yet. */
    @Synchronized
    fun cached(url: String): Pair<Boolean, MediaProbeLogic.Info?> = cache.containsKey(url) to cache[url]

    @Synchronized
    private fun store(url: String, info: MediaProbeLogic.Info?) { cache[url] = info }

    suspend fun probe(ctx: Context, url: String): MediaProbeLogic.Info? {
        cached(url).let { (known, info) -> if (known) return info }
        val t0 = System.currentTimeMillis()
        val info = try {
            withTimeout(TIMEOUT_MS) { withContext(Dispatchers.IO) { read(ctx.applicationContext, url) } }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            if (BuildConfig.DEBUG) Log.d(TAG, "timeout after ${System.currentTimeMillis() - t0} ms")
            null
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e   // page closed: no result, nothing cached, the next visit checks again
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.d(TAG, "failed: " + generateSequence<Throwable>(e) { it.cause }.last().javaClass.simpleName)
            null
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "${System.currentTimeMillis() - t0} ms: $info")
        store(url, info?.takeUnless { it.isEmpty })
        return info?.takeUnless { it.isEmpty }
    }

    private suspend fun read(ctx: Context, url: String): MediaProbeLogic.Info {
        // Same HTTP settings and backup servers as the player (PlayerScreen)
        val http = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(15_000)
        val msf = DefaultMediaSourceFactory(ctx)
            .setDataSourceFactory(HostFailover.DataSourceWrapper.Factory(DefaultDataSource.Factory(ctx, http)))
        val retriever = MetadataRetriever.Builder(ctx, MediaItem.fromUri(url)).setMediaSourceFactory(msf).build()
        try {
            val groups = awaitGroups(retriever)
            val tracks = ArrayList<MediaProbeLogic.Track>()
            for (g in 0 until groups.length) {
                val tg = groups[g]
                for (i in 0 until tg.length) {
                    val f = tg.getFormat(i)
                    tracks.add(MediaProbeLogic.Track(f.sampleMimeType, f.codecs, f.width, f.height, f.colorInfo?.colorTransfer ?: -1, f.channelCount, f.language))
                }
            }
            return MediaProbeLogic.describe(tracks)
        } finally {
            // Also on cancel: closes the connection to the provider
            retriever.close()
        }
    }

    private suspend fun awaitGroups(retriever: MetadataRetriever): TrackGroupArray = suspendCancellableCoroutine { cont ->
        val future = retriever.retrieveTrackGroups()
        future.addListener({
            try {
                cont.resume(future.get())
            } catch (e: ExecutionException) {
                cont.resumeWithException(e.cause ?: e)
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }, Runnable::run)
        cont.invokeOnCancellation { future.cancel(true) }
    }
}

/** Line with the checked tracks on the detail page. Shows nothing while loading, on failure or when switched off. */
@Composable
fun ProbeLine(url: String) {
    val ctx = LocalContext.current
    PlaybackPrefs.init(ctx)
    if (!PlaybackPrefs.probe) return
    val info by produceState(MediaProbe.cached(url).second, url) {
        if (MediaProbe.cached(url).first) return@produceState
        // Only when the page stays open for a moment (1.4.5): a quick look or an immediate Play no longer opens a
        // second connection to the provider next to the player, and fast browsing does not start a check per title
        kotlinx.coroutines.delay(MediaProbe.START_DELAY_MS)
        value = MediaProbe.probe(ctx, url)
    }
    val i = info ?: return
    val text = MediaProbeLogic.line(i, { langLabel(it) }, Lang.tr("Ton"), Lang.tr("Untertitel"))
    if (text.isEmpty()) return
    Text(raw = true, text = text, color = Muted, fontSize = spx(16), maxLines = 2, overflow = TextOverflow.Ellipsis)
}
