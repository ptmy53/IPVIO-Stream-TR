package com.ipvio.stream

import java.io.ByteArrayOutputStream
import java.net.InetAddress

/**
 * T-049f: DNS messages (RFC 1035) and the server list of the own-DNS setting. Pure logic, no Android imports, covered by JVM tests.
 * A server entry is an IP address (plain DNS, port 53) or an https:// address (DNS over HTTPS, RFC 8484).
 */
object DnsLogic {
    const val TYPE_A = 1
    const val TYPE_AAAA = 28
    private const val MAX_SERVERS = 3

    class Answer(val addresses: List<InetAddress>, val ttlSec: Int)

    /** Is [s] a literal IPv4 or IPv6 address (no name lookup)? */
    fun isIpLiteral(s: String): Boolean {
        val t = s.trim().removePrefix("[").removeSuffix("]")
        if (t.isEmpty()) return false
        val v4 = t.split('.')
        if (v4.size == 4 && v4.all { p -> p.isNotEmpty() && p.length <= 3 && p.all { it.isDigit() } && p.toInt() in 0..255 }) return true
        return t.contains(':') && t.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == ':' || it == '.' } && t.count { it == ':' } in 2..7
    }

    /** One server entry: an IP address or an https URL. Anything else is dropped. */
    fun normalizeServer(raw: String): String? {
        val t = raw.trim()
        if (t.isEmpty() || t.length > 200 || t.any { it.isWhitespace() }) return null
        if (t.startsWith("https://", ignoreCase = true)) {
            val rest = t.substring(8)
            return if (rest.isNotEmpty() && !rest.startsWith("/") && rest.substringBefore('/').isNotBlank()) t else null
        }
        return if (isIpLiteral(t)) t.removePrefix("[").removeSuffix("]") else null
    }

    /** Servers from a text (separated by space, comma, semicolon or line break); at most three, no duplicates. */
    fun parseServers(text: String?): List<String> =
        (text ?: "").split(' ', ',', ';', '\n', '\t').mapNotNull { normalizeServer(it) }.distinct().take(MAX_SERVERS)

    fun isDoh(server: String) = server.startsWith("https://", ignoreCase = true)

    /** A query for [host] and record [type] (A or AAAA) with the id [id]. */
    fun buildQuery(id: Int, host: String, type: Int): ByteArray {
        val o = ByteArrayOutputStream()
        o.write(id ushr 8 and 0xFF); o.write(id and 0xFF)
        o.write(0x01); o.write(0x00)                 // flags: recursion desired
        o.write(0); o.write(1)                       // one question
        o.write(0); o.write(0); o.write(0); o.write(0); o.write(0); o.write(0)
        for (label in host.trimEnd('.').split('.')) {
            val b = label.toByteArray(Charsets.US_ASCII)
            require(b.isNotEmpty() && b.size <= 63) { "bad label" }
            o.write(b.size); o.write(b)
        }
        o.write(0)
        o.write(type ushr 8 and 0xFF); o.write(type and 0xFF)
        o.write(0); o.write(1)                       // class IN
        return o.toByteArray()
    }

    /** The addresses of a response, or null if it is damaged, for another query, truncated or an error. */
    fun parseResponse(data: ByteArray, expectedId: Int): Answer? = try {
        parse(data, expectedId)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    private fun u16(d: ByteArray, p: Int) = (d[p].toInt() and 0xFF shl 8) or (d[p + 1].toInt() and 0xFF)

    private fun parse(d: ByteArray, expectedId: Int): Answer? {
        if (d.size < 12 || u16(d, 0) != expectedId) return null
        val flags = u16(d, 2)
        if (flags and 0x8000 == 0) return null       // not a response
        if (flags and 0x0200 != 0) return null       // truncated
        if (flags and 0x000F != 0) return null       // error code
        val qd = u16(d, 4)
        val an = u16(d, 6)
        var p = 12
        repeat(qd) { p = skipName(d, p) + 4 }
        val out = ArrayList<InetAddress>()
        var ttl = Int.MAX_VALUE
        repeat(an) {
            p = skipName(d, p)
            val type = u16(d, p)
            val t = (u16(d, p + 4) shl 16) or u16(d, p + 6)
            val len = u16(d, p + 8)
            p += 10
            if (p + len > d.size) return null
            if (type == TYPE_A && len == 4 || type == TYPE_AAAA && len == 16) {
                out.add(InetAddress.getByAddress(d.copyOfRange(p, p + len)))
                ttl = minOf(ttl, if (t < 0) 0 else t)
            }
            p += len
        }
        return if (out.isEmpty()) null else Answer(out, ttl.coerceIn(30, 3600))
    }

    /** Position after a name (labels, or a pointer of two bytes). */
    private fun skipName(d: ByteArray, start: Int): Int {
        var p = start
        while (true) {
            val len = d[p].toInt() and 0xFF
            when {
                len == 0 -> return p + 1
                len and 0xC0 == 0xC0 -> return p + 2
                else -> p += 1 + len
            }
            if (p >= d.size) throw IndexOutOfBoundsException()
        }
    }
}
