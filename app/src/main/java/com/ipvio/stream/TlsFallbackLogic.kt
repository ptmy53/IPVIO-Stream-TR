package com.ipvio.stream

/**
 * Decision rules for a provider host whose HTTPS certificate does not match its address (#69).
 * No silent downgrade: plain http is used only for a host the user has agreed to. Pure logic, no Android, testable.
 */
object TlsFallbackLogic {
    /** What a connection should do with an https address after a certificate check. */
    enum class Decision {
        /** Not an https address, or the certificate matches: connect as requested. */
        USE_AS_IS,
        /** Certificate does not match and the user has agreed for this host: use the same address over http. */
        USE_HTTP,
        /** Certificate does not match and there is no agreement yet: ask the user, do not connect over http. */
        ASK_CONSENT,
    }

    /** Authority ("user:pw@host:port") and the rest ("/path?query#fragment") of an https address; null for anything else. */
    private fun split(url: String): Pair<String, String>? {
        val t = url.trim()
        if (!t.regionMatches(0, "https://", 0, 8, ignoreCase = true)) return null
        val after = t.substring(8)
        var end = after.length
        for (i in after.indices) if (after[i] == '/' || after[i] == '?' || after[i] == '#') { end = i; break }
        if (end == 0) return null
        return after.substring(0, end) to after.substring(end)
    }

    /** Lower-case host of an https address, null for anything else. */
    fun httpsHost(url: String): String? {
        val (auth, _) = split(url) ?: return null
        val host = hostPort(auth).first
        return host.ifEmpty { null }?.lowercase()
    }

    /** (host, port or -1) of an authority; user info is dropped. IPv6 hosts keep their brackets. */
    private fun hostPort(authority: String): Pair<String, Int> {
        val hp = authority.substringAfterLast('@')
        if (hp.startsWith("[")) {
            val close = hp.indexOf(']')
            if (close < 0) return hp to -1
            val port = hp.substring(close + 1).removePrefix(":").toIntOrNull() ?: -1
            return hp.substring(0, close + 1) to port
        }
        val colon = hp.indexOf(':')
        if (colon < 0) return hp to -1
        return hp.substring(0, colon) to (hp.substring(colon + 1).toIntOrNull() ?: -1)
    }

    /**
     * https address -> same address over http. Null if [url] is not an https address.
     * Everything after the host stays exactly as written (no re-encoding: passwords and tokens with % or | survive).
     * The default port 443 is dropped, a special port stays.
     */
    fun downgrade(url: String): String? {
        val (auth, rest) = split(url) ?: return null
        val userInfo = if (auth.contains('@')) auth.substringBeforeLast('@') + "@" else ""
        val (host, port) = hostPort(auth)
        val portPart = if (port == -1 || port == 443) "" else ":$port"
        return "http://$userInfo$host$portPart$rest"
    }

    /** The http address to use right away for a host the user has agreed to, so no failing https attempt comes first. null = keep [url]. */
    fun preferred(url: String, agreed: Set<String>): String? {
        val host = httpsHost(url) ?: return null
        return if (host in agreed) downgrade(url) else null
    }

    /**
     * [mismatch] = the certificate error is "certificate does not match the host name".
     * [agreed] = hosts the user has already agreed to (lower-case).
     */
    fun decide(url: String, mismatch: Boolean, agreed: Set<String>): Decision {
        val host = httpsHost(url) ?: return Decision.USE_AS_IS
        if (!mismatch) return Decision.USE_AS_IS
        return if (host in agreed) Decision.USE_HTTP else Decision.ASK_CONSENT
    }

    /** Agreed hosts plus the host of [url]. Unchanged if [url] is not https. */
    fun agree(agreed: Set<String>, url: String): Set<String> =
        httpsHost(url)?.let { agreed + it } ?: agreed

}
