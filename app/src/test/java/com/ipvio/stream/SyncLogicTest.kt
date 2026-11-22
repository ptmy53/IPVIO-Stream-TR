package com.ipvio.stream

import org.junit.Assert.*
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SyncLogicTest {
    private fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }

    @Test fun pbkdf2MatchesRfc7914Vector() {
        // RFC 7914 section 11: P="passwd", S="salt", c=1, dkLen=64
        assertEquals(
            "55ac046e56e3089fec1691c22544b605f94185216dde0465e68b9d57c20dacbc49ca9cccf179b645991664b39d77ef317c71b845b1e30bd509112041d3a19783",
            hex(SyncLogic.pbkdf2("passwd", "salt".toByteArray(), 1, 64))
        )
    }

    @Test fun pbkdf2MatchesJdk() {
        val salt = ByteArray(16) { it.toByte() }
        val jdk = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec("correct horse".toCharArray(), salt, 1000, 256)).encoded
        assertArrayEquals(jdk, SyncLogic.pbkdf2("correct horse", salt, 1000))
    }

    @Test fun sealAndOpen() {
        val plain = "[{\"url\":\"http://host/get.php?username=a&password=b\"}]".toByteArray()
        val s = SyncLogic.seal(plain, "my secret pass", SecureRandom(), iterations = 1000)
        assertFalse(String(s.data).contains("password"))
        assertArrayEquals(plain, SyncLogic.open(s, "my secret pass"))
        assertNull(SyncLogic.open(s, "my secret Pass"))
        assertNull(SyncLogic.open(s, ""))
        val broken = s.data.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        assertNull(SyncLogic.open(SyncLogic.Sealed(s.salt, s.iv, broken, s.iterations), "my secret pass"))
    }

    @Test fun passphraseRules() {
        assertFalse(SyncLogic.validPassphrase("short"))
        assertTrue(SyncLogic.validPassphrase("12345678"))
    }

    @Test fun categories() {
        assertEquals("ipvio_hist", SyncLogic.baseName("ipvio_hist_p2"))
        assertEquals("ipvio_hist", SyncLogic.baseName("ipvio_hist"))
        assertEquals(SyncLogic.Category.FAVORITES, SyncLogic.categoryOf("ipvio_lib_p3"))
        assertEquals(SyncLogic.Category.HISTORY, SyncLogic.categoryOf("ipvio_watch"))
        assertEquals(SyncLogic.Category.SETTINGS, SyncLogic.categoryOf("ipvio_hidecats"))
        assertEquals(SyncLogic.Category.SETTINGS, SyncLogic.categoryOf("ipvio_profiles"))
    }

    @Test fun deviceOnlyData() {
        assertFalse(SyncLogic.travels("ipvio_upd"))
        assertFalse(SyncLogic.travels("ipvio_upd_p1"))
        assertTrue(SyncLogic.travels("ipvio_general"))
        assertTrue(SyncLogic.deviceOnlyKey("ipvio_general_p2", "parental_hash"))
        assertTrue(SyncLogic.deviceOnlyKey("ipvio_general", "parental_salt"))
        assertFalse(SyncLogic.deviceOnlyKey("ipvio_general", "lang"))
        assertFalse(SyncLogic.deviceOnlyKey("ipvio_settings", "parental_hash"))
    }

    @Test fun restoreSelection() {
        val onlyFav = setOf(SyncLogic.Category.FAVORITES)
        assertTrue(SyncLogic.shouldRestore("ipvio_catfavs_p1", onlyFav))
        assertFalse(SyncLogic.shouldRestore("ipvio_hist", onlyFav))
        assertFalse(SyncLogic.shouldRestore("ipvio_upd", SyncLogic.Category.values().toSet()))
    }
}
