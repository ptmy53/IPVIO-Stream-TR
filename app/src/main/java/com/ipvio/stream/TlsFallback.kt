package com.ipvio.stream

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import java.io.IOException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Some providers deliver a certificate that does not belong to their address (wrong or expired domain, shared default
 * certificate: "Hostname x not verified, certificate is for y"). Playlist, live TV and movies then fail although the
 * server itself works.
 *
 * #69: there is no silent downgrade any more. For this error the user is asked once per host (dialog, [TlsConsentDialog]).
 * Only after "connect without encryption" is the host remembered (`ipvio_tls`) and the same address is used over http.
 * Every other TLS error (expired, untrusted CA …) is still refused. Decision rules: [TlsFallbackLogic].
 */
object TlsFallback {
    private const val PREFS = "ipvio_tls"
    private const val KEY_AGREED = "agreed"

    @Volatile private var agreed: Set<String> = emptySet()
    @Volatile private var app: Context? = null

    /** Host of a certificate mismatch the user has not agreed to yet. The consent dialog shows it when not null. */
    var pendingAsk by mutableStateOf<String?>(null)
        private set

    /** Loads the hosts the user agreed to. Call once at start; without it nothing is remembered. */
    fun init(c: Context) {
        if (app != null) return
        val a = c.applicationContext
        app = a
        agreed = runCatching { a.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_AGREED, null)?.toSet() }
            .getOrNull().orEmpty()
    }

    /** The user chose "connect without encryption" for the pending host. */
    fun agreePending() {
        val host = pendingAsk ?: return
        pendingAsk = null
        agreed = agreed + host
        runCatching { app?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putStringSet(KEY_AGREED, agreed)?.apply() }
    }

    /** The user cancelled. The host is asked again on the next attempt. */
    fun dismissPending() { pendingAsk = null }

    /** Is the error (or one of its causes) "certificate does not match the host name"? */
    fun isHostMismatch(e: Throwable?): Boolean {
        var t = e
        var depth = 0
        while (t != null && depth < 8) {
            if (t is SSLPeerUnverifiedException) return true
            t = t.cause
            depth++
        }
        return false
    }

    /** https address -> same address over http (default port 443 becomes 80, a special port stays). null = not applicable. */
    fun downgradeUrl(url: String): String? = TlsFallbackLogic.downgrade(url)

    /**
     * The http address to use at once for a host the user has agreed to (no failing https attempt first), else null.
     * Without this every request and every video segment first failed over https and then retried over http.
     */
    fun preferredUrl(url: String): String? = TlsFallbackLogic.preferred(url, agreed)

    /**
     * Failed request [url] with error [e]: returns the http address if the user agreed for this host, otherwise null.
     * Without agreement the host is queued for the consent dialog (nothing is sent over http).
     */
    fun fallbackFor(url: String, e: Throwable): String? {
        if (!isHostMismatch(e)) return null
        return when (TlsFallbackLogic.decide(url, mismatch = true, agreed = agreed)) {
            TlsFallbackLogic.Decision.USE_HTTP -> TlsFallbackLogic.downgrade(url)
            TlsFallbackLogic.Decision.ASK_CONSENT -> {
                TlsFallbackLogic.httpsHost(url)?.let { pendingAsk = it }
                null
            }
            TlsFallbackLogic.Decision.USE_AS_IS -> null
        }
    }

    /** For OkHttp clients that talk to the provider (playlist, Xtream/Stalker login, live buffer). */
    fun interceptor(): Interceptor = Interceptor { chain ->
        val req = chain.request()
        val url = req.url
        if (!url.isHttps) return@Interceptor chain.proceed(req)
        preferredUrl(url.toString())?.toHttpUrlOrNull()?.let { return@Interceptor chain.proceed(req.newBuilder().url(it).build()) }
        try {
            chain.proceed(req)
        } catch (e: IOException) {
            val low = fallbackFor(url.toString(), e)?.toHttpUrlOrNull() ?: throw e
            Diag.report("Certificate does not match the address: connecting without encryption (you agreed)")
            chain.proceed(req.newBuilder().url(low).build())
        }
    }
}
