package com.ipvio.stream

import android.webkit.ConsoleMessage
import android.media.MediaCodecList
import android.net.http.SslError
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Diagnose lines about the WebView version and the video decoders (T-032: sound plays, picture stays black). */
private suspend fun reportDeviceForTrailer() = withContext(Dispatchers.Default) {
    runCatching {
        val ver = if (Build.VERSION.SDK_INT >= 26) android.webkit.WebView.getCurrentWebViewPackage()?.let { "${it.packageName} ${it.versionName}" } else null
        Diag.report("Trailer: WebView ${ver ?: "unknown"}, Android ${Build.VERSION.SDK_INT}")
    }
    runCatching {
        val dec = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.filter { !it.isEncoder }
            .map { TrailerLogic.Decoder(it.name, it.supportedTypes.toList()) }
        Diag.report(TrailerLogic.decoderLine(dec))
    }
}

/** Seconds a way gets to report "playing" before the next way is tried (T-032). */
private const val ATTEMPT_MS = 10_000L

/**
 * Trailer inside the app (#15). Full screen; Back or the dialog close ends it and destroys the WebView, so no sound
 * keeps playing. Only the embed page is shown: navigation of the main frame is blocked.
 *
 * T-032 (20 Nov): some titles stayed black. The cause is unknown, so the dialog tries [TrailerLogic.ATTEMPTS] ways in
 * turn; a way that does not report "playing" within 10 s is replaced by the next one, and the result of each is
 * written to Settings > Diagnose. If none plays, or YouTube says the video cannot be embedded, the trailer opens
 * outside the app ([onOpenExternal]) and the dialog closes.
 */
@Composable
fun TrailerDialog(key: String?, onClose: () -> Unit, onOpenExternal: () -> Unit = {}) {
    if (!TrailerLogic.validKey(key)) return
    var attempt by remember(key) { mutableStateOf(0) }
    val playing = remember(key, attempt) { mutableStateOf(false) }
    val external = {
        onOpenExternal()
        onClose()
    }
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        // On Android TV the remote only reaches Compose views that have focus: the WebView must not take it,
        // and the "Im YouTube öffnen" button gets focus when the dialog opens (19 Nov feedback: button not clickable)
        val buttonFocus = remember { FocusRequester() }
        LaunchedEffect(key) { delay(200); runCatching { buttonFocus.requestFocus() } }
        LaunchedEffect(Unit) { reportDeviceForTrailer() }
        // No sign of playback in time: next way, or outside the app after the last one
        LaunchedEffect(key, attempt) {
            delay(ATTEMPT_MS)
            if (!playing.value) {
                Diag.report("Trailer: way ${attempt + 1} no playback after 10 s")
                if (attempt + 1 < TrailerLogic.ATTEMPTS) attempt++ else { Diag.report("Trailer: no way played, opening outside"); external() }
            }
        }
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            // key(attempt): every way gets a new WebView; the old one is destroyed
            androidx.compose.runtime.key(attempt) {
                val n = attempt
                val holder = remember { arrayOfNulls<WebView>(1) }
                DisposableEffect(Unit) {
                    onDispose {
                        holder[0]?.let { w -> w.stopLoading(); w.loadUrl("about:blank"); w.destroy() }
                        holder[0] = null
                    }
                }
                val way = TrailerLogic.attempt(attempt, key)
                if (way != null) AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            isFocusable = false
                            isFocusableInTouchMode = false
                            setBackgroundColor(android.graphics.Color.BLACK)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                                    request?.isForMainFrame != false
                                // Diagnostics for the black-picture trailers (T-032): codes only, no addresses
                                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                    Diag.report("Trailer: WebView error ${error?.errorCode} (main page=${request?.isForMainFrame != false})")
                                }
                                // Same behaviour as the default (connection is refused); only reports the reason
                                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                    Diag.report("Trailer: SSL error ${error?.primaryError}")
                                    handler?.cancel()
                                }
                                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                                    Diag.report("Trailer: HTTP ${errorResponse?.statusCode} (main page=${request?.isForMainFrame == true})")
                                }
                            }
                            // Player errors/states of the embed page; only lines of TrailerLogic.diagLine are written down
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(m: ConsoleMessage?): Boolean {
                                    val msg = m?.message()
                                    TrailerLogic.diagLine(msg)?.let { Diag.report("$it (way ${n + 1})") }
                                    if (TrailerLogic.isPlaying(msg)) playing.value = true
                                    if (TrailerLogic.isVideoBlocked(TrailerLogic.playerError(msg))) external()
                                    return true
                                }
                            }
                            Diag.report("Trailer: way ${n + 1} started")
                            loadDataWithBaseURL(way.baseUrl, way.html, "text/html", "utf-8", null)
                            holder[0] = this
                        }
                    },
                )
            }
            // Fallback (19 Nov feedback: black picture, no sound on some titles): the same trailer in YouTube
            PillButton(Lang.tr("Im YouTube öffnen"), modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).focusRequester(buttonFocus), light = true) { onOpenExternal() }
        }
    }
}
