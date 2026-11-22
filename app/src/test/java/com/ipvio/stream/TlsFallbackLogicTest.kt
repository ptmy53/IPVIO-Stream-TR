package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** #69: no silent downgrade to http; only for hosts the user agreed to (pure, no Android). */
class TlsFallbackLogicTest {

    @Test
    fun httpsHost_isLowerCaseHostOnlyForHttps() {
        assertEquals("provider.example", TlsFallbackLogic.httpsHost("https://Provider.Example/get.php?x=1"))
        assertNull(TlsFallbackLogic.httpsHost("http://provider.example/"))
        assertNull(TlsFallbackLogic.httpsHost("not a url"))
    }

    @Test
    fun downgrade_keepsPathAndQuery_andDropsDefaultPort() {
        assertEquals(
            "http://p.example/a/b?u=1&p=2",
            TlsFallbackLogic.downgrade("https://p.example:443/a/b?u=1&p=2"),
        )
        assertEquals("http://p.example:8443/x", TlsFallbackLogic.downgrade("https://p.example:8443/x"))
        assertNull(TlsFallbackLogic.downgrade("http://p.example/x"))
    }

    @Test
    fun matchingCertificate_isUsedAsIs() {
        assertEquals(
            TlsFallbackLogic.Decision.USE_AS_IS,
            TlsFallbackLogic.decide("https://p.example/", mismatch = false, agreed = emptySet()),
        )
    }

    @Test
    fun mismatch_withoutAgreement_asksAndNeverGoesToHttp() {
        assertEquals(
            TlsFallbackLogic.Decision.ASK_CONSENT,
            TlsFallbackLogic.decide("https://p.example/", mismatch = true, agreed = emptySet()),
        )
    }

    @Test
    fun mismatch_withAgreementForThisHost_usesHttp() {
        assertEquals(
            TlsFallbackLogic.Decision.USE_HTTP,
            TlsFallbackLogic.decide("https://P.example/", mismatch = true, agreed = setOf("p.example")),
        )
    }

    @Test
    fun agreement_isPerHost() {
        assertEquals(
            TlsFallbackLogic.Decision.ASK_CONSENT,
            TlsFallbackLogic.decide("https://other.example/", mismatch = true, agreed = setOf("p.example")),
        )
    }

    @Test
    fun plainHttp_isNeverAskedAbout() {
        assertEquals(
            TlsFallbackLogic.Decision.USE_AS_IS,
            TlsFallbackLogic.decide("http://p.example/", mismatch = true, agreed = emptySet()),
        )
    }

    @Test
    fun agree_addsOnlyHttpsHosts() {
        assertEquals(setOf("a.example", "b.example"), TlsFallbackLogic.agree(setOf("a.example"), "https://B.example/x"))
        assertEquals(setOf("a.example"), TlsFallbackLogic.agree(setOf("a.example"), "http://c.example/"))
    }

    @Test
    fun downgrade_keepsOddCharactersExactlyAsWritten() {
        // passwords and tokens with % or | and spaces must not be re-encoded or rejected (live streams use such addresses)
        assertEquals(
            "http://p.example/live/user/pa%40ss/1.ts?token=a%2Fb|x y",
            TlsFallbackLogic.downgrade("https://p.example/live/user/pa%40ss/1.ts?token=a%2Fb|x y"),
        )
        assertEquals("p.example", TlsFallbackLogic.httpsHost("https://p.example/a b/c|d"))
        assertEquals("http://u:pw@p.example/x", TlsFallbackLogic.downgrade("https://u:pw@p.example:443/x"))
        assertEquals("http://[::1]:8443/x", TlsFallbackLogic.downgrade("https://[::1]:8443/x"))
    }

    @Test
    fun preferred_goesStraightToHttpOnlyForAgreedHosts() {
        assertEquals("http://P.example/live/u/p/1.ts", TlsFallbackLogic.preferred("https://P.example/live/u/p/1.ts", setOf("p.example")))
        assertNull(TlsFallbackLogic.preferred("https://other.example/x", setOf("p.example")))
        assertNull(TlsFallbackLogic.preferred("http://p.example/x", setOf("p.example")))
    }
}
