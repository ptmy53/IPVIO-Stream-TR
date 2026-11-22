package com.ipvio.stream

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DnsLogicTest {
    @Test fun ipLiterals() {
        assertTrue(DnsLogic.isIpLiteral("1.1.1.1")); assertTrue(DnsLogic.isIpLiteral("2606:4700:4700::1111")); assertTrue(DnsLogic.isIpLiteral("[::1]"))
        assertFalse(DnsLogic.isIpLiteral("256.1.1.1")); assertFalse(DnsLogic.isIpLiteral("example.com")); assertFalse(DnsLogic.isIpLiteral("1.1.1")); assertFalse(DnsLogic.isIpLiteral(""))
    }
    @Test fun serverListIsCleanedAndLimited() {
        assertEquals(listOf("1.1.1.1", "8.8.8.8", "https://dns.google/dns-query"), DnsLogic.parseServers("1.1.1.1, 8.8.8.8;1.1.1.1 https://dns.google/dns-query 9.9.9.9 bad host"))
        assertEquals(emptyList<String>(), DnsLogic.parseServers("dns.example.com https:// https:///x ftp://1.1.1.1"))
        assertEquals(emptyList<String>(), DnsLogic.parseServers(null))
    }
    @Test fun dohIsRecognised() { assertTrue(DnsLogic.isDoh("https://x/dns-query")); assertFalse(DnsLogic.isDoh("1.1.1.1")) }

    @Test fun queryBytes() {
        val q = DnsLogic.buildQuery(0x1234, "ab.cd", DnsLogic.TYPE_A)
        val expect = byteArrayOf(0x12, 0x34, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 2, 'a'.code.toByte(), 'b'.code.toByte(), 2, 'c'.code.toByte(), 'd'.code.toByte(), 0, 0, 1, 0, 1)
        assertArrayEquals(expect, q)
    }

    /** Response for "ab.cd" A: one CNAME-less answer with a compression pointer to the name, TTL 300, 93.184.216.34. */
    private fun response(id: Int, flags: Int = 0x8180, type: Int = 1, rdata: ByteArray = byteArrayOf(93.toByte(), 184.toByte(), 216.toByte(), 34)): ByteArray {
        val q = DnsLogic.buildQuery(id, "ab.cd", DnsLogic.TYPE_A)
        val head = q.copyOf(); head[2] = (flags ushr 8).toByte(); head[3] = flags.toByte(); head[7] = 1
        val ans = byteArrayOf(0xC0.toByte(), 12, 0, type.toByte(), 0, 1, 0, 0, 1, 0x2C, 0, rdata.size.toByte()) + rdata
        return head + ans
    }

    @Test fun parsesAnAnswerWithCompression() {
        val a = DnsLogic.parseResponse(response(7), 7)
        assertNotNull(a)
        assertEquals("93.184.216.34", a!!.addresses[0].hostAddress)
        assertEquals(300, a.ttlSec)
    }
    @Test fun wrongIdTruncatedAndErrorsAreRejected() {
        assertNull(DnsLogic.parseResponse(response(7), 8))
        assertNull(DnsLogic.parseResponse(response(7, 0x8380), 7))     // truncated
        assertNull(DnsLogic.parseResponse(response(7, 0x8183), 7))     // NXDOMAIN
        assertNull(DnsLogic.parseResponse(response(7, 0x0100), 7))     // not a response
    }
    @Test fun damagedDataGivesNullNotACrash() {
        val r = response(7)
        assertNull(DnsLogic.parseResponse(r.copyOf(r.size - 3), 7))
        assertNull(DnsLogic.parseResponse(byteArrayOf(1, 2, 3), 1))
    }
    @Test fun ipv6AnswerIsRead() {
        val v6 = ByteArray(16) { if (it == 15) 1 else 0 }
        val a = DnsLogic.parseResponse(response(9, type = 28, rdata = v6), 9)
        assertNotNull(a); assertEquals(16, a!!.addresses[0].address.size)
    }
}
