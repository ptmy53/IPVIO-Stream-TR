package com.ipvio.stream

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

/**
 * T-049e: VPN with WireGuard (official `com.wireguard.android:tunnel` library). The user imports the `.conf` file of their VPN provider
 * (Settings > General > VPN); it is stored encrypted with the device's key store and never shown. While the VPN is on, all traffic of
 * the device that goes through the app's VPN service (this includes the video stream) leaves through the tunnel.
 * Not built here: no kill switch, no start with the device, no per-app routing. Device support (Fire OS) is **unverified**.
 */
object AppVpn {
    enum class St { OFF, CONNECTING, ON, ERROR }

    var state by mutableStateOf(St.OFF)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    private var backend: GoBackend? = null
    private val tunnel = object : Tunnel {
        override fun getName(): String = "ipvio"
        override fun onStateChange(newState: Tunnel.State) {
            state = if (newState == Tunnel.State.UP) St.ON else if (state != St.ERROR) St.OFF else state
        }
    }

    /** Device-level file, not part of the backup (it holds a private key). */
    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences("ipvio_vpn", Context.MODE_PRIVATE)

    fun hasConfig(c: Context): Boolean = prefs(c).getString("cfg", null) != null

    private fun text(c: Context): String? = prefs(c).getString("cfg", null)?.let { Secrets.decrypt(it) }

    /** The server of the stored configuration, for the settings page. */
    fun server(c: Context): String? = text(c)?.let { VpnLogic.serverName(it) }

    /** Stores a configuration after checking it. null = stored, otherwise the reason (German settings text). */
    fun save(c: Context, conf: String): String? {
        VpnLogic.check(conf)?.let { return it }
        try { Config.parse(ByteArrayInputStream(conf.toByteArray(Charsets.UTF_8))) } catch (e: Exception) { return "Die WireGuard-Konfiguration ist ungültig." }
        // The private key is never stored in plain text: without a secure key store the file is refused
        val enc = Secrets.encrypt(conf) ?: return "Dieses Gerät hat keinen sicheren Schlüsselspeicher. Die Konfiguration wird nicht gespeichert."
        prefs(c).edit().putString("cfg", enc).apply()
        message = null
        return null
    }

    fun remove(c: Context) {
        prefs(c).edit().remove("cfg").apply()
        state = St.OFF; message = null
    }

    /** null = the app may connect; otherwise the system asks the user once (start it with startActivityForResult). */
    fun consentIntent(c: Context): Intent? = VpnService.prepare(c.applicationContext)

    private fun backend(c: Context): GoBackend = backend ?: GoBackend(c.applicationContext).also { backend = it }

    suspend fun connect(c: Context) {
        val conf = text(c) ?: run { state = St.ERROR; message = "Keine Konfiguration gespeichert."; return }
        state = St.CONNECTING; message = null
        withContext(Dispatchers.IO) {
            try {
                val cfg = Config.parse(ByteArrayInputStream(conf.toByteArray(Charsets.UTF_8)))
                backend(c).setState(tunnel, Tunnel.State.UP, cfg)
                state = St.ON
            } catch (e: Exception) {
                state = St.ERROR
                message = "Verbindung fehlgeschlagen"
            }
        }
    }

    suspend fun disconnect(c: Context) {
        withContext(Dispatchers.IO) {
            try { backend(c).setState(tunnel, Tunnel.State.DOWN, null) } catch (e: Exception) { }
        }
        state = St.OFF; message = null
    }
}
