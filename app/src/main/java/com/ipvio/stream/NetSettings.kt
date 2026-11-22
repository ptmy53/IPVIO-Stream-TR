package com.ipvio.stream

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** T-049f: Settings > General > DNS server. Presets or an own address (an IP address, or https:// for DNS over HTTPS). */
@Composable
internal fun DnsSection(ctx: Context) {
    val scope = rememberCoroutineScope()
    val presets = listOf("Systemstandard", "Cloudflare (1.1.1.1)", "Google (8.8.8.8)", "Quad9 (9.9.9.9)", "Eigene Adresse")
    val values = listOf("", "1.1.1.1 1.0.0.1", "8.8.8.8 8.8.4.4", "9.9.9.9 149.112.112.112")
    val current = AppDns.text()
    val presetIdx = values.indexOf(current).let { if (it >= 0) it else 4 }
    var custom by remember { mutableStateOf(presetIdx == 4) }
    var text by remember { mutableStateOf(current) }
    var result by remember { mutableStateOf<String?>(null) }

    SectionTitle("DNS-Server")
    Hint("Der DNS-Server löst die Adressen der Anbieter und Dienste auf, mit denen die App spricht. Hilft, wenn ein Anbieter beim Provider gesperrt ist, und für mehr Privatsphäre. Antwortet der Server nicht, nutzt die App den DNS des Geräts. Gilt nicht für den Videostream selbst und nicht für ein VPN.")
    SelectRow("DNS-Server", if (custom) presets[4] else presets[presetIdx], presets, if (custom) 4 else presetIdx, active = AppDns.servers.isNotEmpty()) {
        if (it == 4) { custom = true; text = AppDns.text() }
        else { custom = false; AppDns.set(ctx, values[it]); result = null }
    }
    if (custom) {
        Field(text, { text = it }, "IP-Adresse oder https://…/dns-query (bis zu 3, mit Leerzeichen getrennt)")
        if (text.isNotBlank() && DnsLogic.parseServers(text).isEmpty()) Hint("Ungültig: eine IP-Adresse wie 1.1.1.1 oder eine https-Adresse eingeben.")
        PillButton("DNS speichern", light = true) {
            if (text.isBlank() || DnsLogic.parseServers(text).isNotEmpty()) { AppDns.set(ctx, text); result = null }
        }
    }
    if (AppDns.servers.isNotEmpty()) {
        PillButton("DNS testen") {
            val s = AppDns.servers.first()
            scope.launch {
                val r = withContext(Dispatchers.IO) { AppDns.test(s, "example.com") }
                result = if (r.isNullOrEmpty()) "Keine Antwort von $s" else "OK: example.com → " + r.first().hostAddress
            }
        }
        StatusLine(result)
    }
}
