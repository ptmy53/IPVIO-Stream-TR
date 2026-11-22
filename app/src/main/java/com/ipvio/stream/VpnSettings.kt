package com.ipvio.stream

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** T-049e: Settings > General > VPN: import a WireGuard file, connect, disconnect, remove. */
@Composable
internal fun VpnSection(ctx: Context) {
    val scope = rememberCoroutineScope()
    var info by remember { mutableStateOf<String?>(null) }
    var has by remember { mutableStateOf(AppVpn.hasConfig(ctx)) }

    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) scope.launch { AppVpn.connect(ctx) } else info = "Die VPN-Berechtigung wurde nicht erteilt."
    }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { r ->
                        // Read until the end, but never more than one byte over the limit (the check then refuses the file)
                        val sb = StringBuilder()
                        val buf = CharArray(4096)
                        while (sb.length <= VpnLogic.MAX_BYTES) {
                            val n = r.read(buf)
                            if (n < 0) break
                            sb.append(buf, 0, n)
                        }
                        sb.toString()
                    }
                }.getOrNull()
            }
            val err = AppVpn.save(ctx, text.orEmpty())
            info = err ?: "Konfiguration gespeichert."
            has = AppVpn.hasConfig(ctx)
        }
    }

    SectionTitle("VPN")
    Hint("Verbindet das Gerät über WireGuard mit dem Server deines VPN-Anbieters, auch für den Videostream. Lade bei deinem Anbieter eine WireGuard-Konfigurationsdatei (.conf) herunter und wähle sie hier aus. Der Schlüssel wird verschlüsselt gespeichert und nie angezeigt. Es gibt keinen Kill-Switch: bricht die Verbindung ab, läuft der Verkehr ohne VPN weiter. Ob das VPN auf deinem Gerät läuft, ist nicht geprüft.")
    if (!has) {
        PillButton("WireGuard-Datei auswählen", light = true) { pick.launch(arrayOf("*/*")) }
    } else {
        AppVpn.server(ctx)?.let { Text("Server: $it", color = Muted, fontSize = 15.sp, raw = true) }
        val st = AppVpn.state
        StatusLine(
            when (st) {
                AppVpn.St.ON -> "VPN verbunden"
                AppVpn.St.CONNECTING -> "Verbinde …"
                AppVpn.St.ERROR -> AppVpn.message ?: "Fehler"
                AppVpn.St.OFF -> "VPN aus"
            }
        )
        if (st == AppVpn.St.ON || st == AppVpn.St.CONNECTING) {
            PillButton("VPN trennen") { scope.launch { AppVpn.disconnect(ctx) } }
        } else {
            PillButton("VPN verbinden", light = true) {
                val i = AppVpn.consentIntent(ctx)
                if (i == null) scope.launch { AppVpn.connect(ctx) } else consent.launch(i)
            }
        }
        PillButton("Andere Datei auswählen") { pick.launch(arrayOf("*/*")) }
        ConfirmPill("VPN-Konfiguration löschen") { scope.launch { AppVpn.disconnect(ctx); AppVpn.remove(ctx); has = false; info = null } }
    }
    StatusLine(info)
}
