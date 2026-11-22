package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class VpnLogicTest {
    private val ok = """
        [Interface]
        PrivateKey = AAAA
        Address = 10.0.0.2/32
        DNS = 10.0.0.1

        [Peer]
        PublicKey = BBBB
        AllowedIPs = 0.0.0.0/0
        Endpoint = vpn.example.net:51820
    """.trimIndent()

    @Test fun validConfigPasses() = assertNull(VpnLogic.check(ok))
    @Test fun emptyAndHugeAreRejected() {
        assertNotNull(VpnLogic.check("")); assertNotNull(VpnLogic.check(null))
        assertNotNull(VpnLogic.check("x".repeat(VpnLogic.MAX_BYTES + 1)))
    }
    @Test fun missingPartsAreNamed() {
        assertNotNull(VpnLogic.check(ok.replace("[Peer]", "[Other]")))
        assertNotNull(VpnLogic.check(ok.replace("[Interface]", "[Nope]")))
        assertNotNull(VpnLogic.check(ok.replace("PrivateKey", "Key")))
        assertNotNull(VpnLogic.check(ok.replace("Endpoint", "Ep")))
    }
    @Test fun keysAreCaseInsensitive() = assertNull(VpnLogic.check(ok.replace("PrivateKey", "privatekey").replace("[Peer]", "[peer]")))
    @Test fun serverNameWithoutPort() {
        assertEquals("vpn.example.net", VpnLogic.serverName(ok))
        assertEquals("2001:db8::1", VpnLogic.serverName(ok.replace("vpn.example.net:51820", "[2001:db8::1]:51820")))
        assertNull(VpnLogic.serverName("[Interface]\nPrivateKey = A"))
    }
}
