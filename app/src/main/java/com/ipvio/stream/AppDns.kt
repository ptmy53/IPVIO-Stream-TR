package com.ipvio.stream

import android.content.Context
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * T-049f: own DNS server for the app's connections (playlists, TV guide, metadata, sports, updates), set in Settings > General.
 * Plain DNS (an IP address) or DNS over HTTPS (an https:// address). If the chosen server does not answer, the system DNS is used,
 * so a wrong entry never leaves the app offline. Addresses given as IP numbers and "localhost" are not looked up.
 * Not covered: the video stream itself (the player's own HTTP stack uses the system DNS) and a VPN's own DNS.
 */
object AppDns : Dns {
    @Volatile var servers: List<String> = emptyList()
        private set
    @Volatile private var raw = ""
    private val cache = ConcurrentHashMap<String, Pair<Long, List<InetAddress>>>()
    /** For the HTTPS servers: a client with the system DNS (it must not resolve through itself). */
    private val doh: OkHttpClient by lazy { OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).readTimeout(4, TimeUnit.SECONDS).build() }
    private var nextId = (System.nanoTime() and 0x7FFF).toInt()

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences("ipvio_dns", Context.MODE_PRIVATE)

    fun init(c: Context) { apply(prefs(c).getString("servers", "") ?: "") }

    /** The text as entered (for the settings page). */
    fun text(): String = raw

    fun set(c: Context, text: String) {
        prefs(c).edit().putString("servers", text.trim()).apply()
        apply(text.trim())
    }

    private fun apply(text: String) { raw = text; servers = DnsLogic.parseServers(text); cache.clear() }

    override fun lookup(hostname: String): List<InetAddress> {
        val list = servers
        if (list.isEmpty() || DnsLogic.isIpLiteral(hostname) || hostname.equals("localhost", ignoreCase = true)) return Dns.SYSTEM.lookup(hostname)
        val now = System.currentTimeMillis()
        cache[hostname]?.let { if (it.first > now) return it.second }
        for (s in list) {
            val r = try { query(s, hostname) } catch (e: Exception) { null } ?: continue
            cache[hostname] = (now + r.ttlSec * 1000L) to r.addresses
            return r.addresses
        }
        return Dns.SYSTEM.lookup(hostname)   // the chosen server did not answer: never break the connection
    }

    /** Looks [host] up through [server] only (used by the test button); null = no answer. */
    fun test(server: String, host: String): List<InetAddress>? = try { query(server, host)?.addresses } catch (e: Exception) { null }

    private fun query(server: String, host: String): DnsLogic.Answer? {
        val a = ask(server, host, DnsLogic.TYPE_A)
        val aaaa = ask(server, host, DnsLogic.TYPE_AAAA)
        val all = (a?.addresses.orEmpty() + aaaa?.addresses.orEmpty())
        if (all.isEmpty()) return null
        return DnsLogic.Answer(all, minOf(a?.ttlSec ?: Int.MAX_VALUE, aaaa?.ttlSec ?: Int.MAX_VALUE).coerceIn(30, 3600))
    }

    @Synchronized private fun id(): Int { nextId = (nextId + 1) and 0xFFFF; return nextId }

    private fun ask(server: String, host: String, type: Int): DnsLogic.Answer? {
        val id = id()
        val q = DnsLogic.buildQuery(id, host, type)
        val data: ByteArray? = if (DnsLogic.isDoh(server)) {
            val req = Request.Builder().url(server).header("Accept", "application/dns-message")
                .post(q.toRequestBody("application/dns-message".toMediaType())).build()
            doh.newCall(req).execute().use { r -> if (r.isSuccessful) r.body?.bytes() else null }
        } else {
            DatagramSocket().use { s ->
                s.soTimeout = 3000
                s.send(DatagramPacket(q, q.size, InetAddress.getByName(server), 53))
                val buf = ByteArray(1232)
                val p = DatagramPacket(buf, buf.size)
                s.receive(p)
                buf.copyOf(p.length)
            }
        }
        if (data == null) return null
        return DnsLogic.parseResponse(data, id)
    }
}
