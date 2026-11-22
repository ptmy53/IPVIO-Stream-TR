package com.ipvio.stream

/**
 * T-049e: checks of a WireGuard configuration file before it is stored (pure logic, no Android imports).
 * The library parses it again when connecting; this only keeps obviously wrong or huge files out and builds the text shown in the settings.
 * The private key is never shown or logged.
 */
object VpnLogic {
    const val MAX_BYTES = 16 * 1024

    /** null = looks like a WireGuard configuration, otherwise the reason (German settings text). */
    fun check(text: String?): String? {
        if (text.isNullOrBlank()) return "Die Datei ist leer."
        if (text.length > MAX_BYTES) return "Die Datei ist zu groß für eine WireGuard-Konfiguration."
        val lines = text.lines().map { it.trim() }
        if (lines.none { it.equals("[Interface]", ignoreCase = true) }) return "Das ist keine WireGuard-Konfiguration: Abschnitt [Interface] fehlt."
        if (lines.none { it.equals("[Peer]", ignoreCase = true) }) return "Das ist keine WireGuard-Konfiguration: Abschnitt [Peer] fehlt."
        if (value(lines, "PrivateKey") == null) return "Der PrivateKey fehlt in der Konfiguration."
        if (value(lines, "Endpoint") == null) return "Der Endpoint (Server) fehlt in der Konfiguration."
        return null
    }

    private fun value(lines: List<String>, key: String): String? =
        lines.firstNotNullOfOrNull { l ->
            val i = l.indexOf('=')
            if (i > 0 && l.substring(0, i).trim().equals(key, ignoreCase = true)) l.substring(i + 1).trim().takeIf { it.isNotEmpty() } else null
        }

    /** The server of the first peer without the port, for the settings page (for example "se-sto.example.net"). */
    fun serverName(text: String): String? {
        val ep = value(text.lines().map { it.trim() }, "Endpoint") ?: return null
        val host = if (ep.startsWith("[")) ep.substringBefore(']').removePrefix("[") else ep.substringBeforeLast(':', ep)
        return host.takeIf { it.isNotBlank() }
    }
}
