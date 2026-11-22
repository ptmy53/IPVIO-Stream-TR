package com.ipvio.stream

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Pure logic for the device sync (W-010, Google Drive): which data travels in which category, which keys
 * stay on the device, and the passphrase encryption of the sensitive part (playlists with logins, API keys).
 * No Android classes here, so everything is covered by plain JVM unit tests.
 */
object SyncLogic {
    /** Version 1 = old backup (file, Wi-Fi, V1.1 Drive). Version 2 = sync format with device info and encrypted part. */
    const val FORMAT_VERSION = 2

    enum class Category { FAVORITES, HISTORY, SETTINGS }

    private val FAVORITES = setOf("ipvio_lib", "ipvio_catfavs", "ipvio_chlists", "ipvio_sportfavs", "ipvio_importedlists")
    private val HISTORY = setOf("ipvio_hist", "ipvio_watch", "ipvio_eprog", "ipvio_resumelog", "ipvio_newep")

    /** Preference files that only make sense on this device and never travel (update state). */
    private val DEVICE_ONLY_FILES = setOf("ipvio_upd")

    /** Keys that always stay on the device: parental control PIN and its switch (decided 14.11.2026). */
    private val DEVICE_ONLY_KEYS = mapOf("ipvio_general" to setOf("parental_hash", "parental_hide", "parental_salt"))

    private val PROFILE_SUFFIX = Regex("_p\\d+$")

    /** File name without the profile suffix ("ipvio_hist_p2" -> "ipvio_hist"). */
    fun baseName(file: String): String = PROFILE_SUFFIX.replace(file, "")

    fun categoryOf(file: String): Category = when (baseName(file)) {
        in FAVORITES -> Category.FAVORITES
        in HISTORY -> Category.HISTORY
        else -> Category.SETTINGS
    }

    /** False for files that are never exported to or restored from a sync. */
    fun travels(file: String): Boolean = baseName(file) !in DEVICE_ONLY_FILES

    /** True if this key keeps its local value on restore and is left out of a sync export. */
    fun deviceOnlyKey(file: String, key: String): Boolean = DEVICE_ONLY_KEYS[baseName(file)]?.contains(key) == true

    /** Should this file be written when restoring the chosen categories? */
    fun shouldRestore(file: String, chosen: Set<Category>): Boolean = travels(file) && categoryOf(file) in chosen

    // ---- Passphrase encryption (AES-256-GCM, key from PBKDF2-HMAC-SHA256) ----

    const val KDF = "pbkdf2-sha256"
    const val ITERATIONS = 100_000
    const val MIN_PASSPHRASE = 8
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val KEY_BYTES = 32
    /** Binds the ciphertext to this format, so it cannot be reused in another context. */
    private val AAD = "ipvio-sync-v2".toByteArray(Charsets.UTF_8)

    fun validPassphrase(p: String): Boolean = p.length >= MIN_PASSPHRASE

    class Sealed(val salt: ByteArray, val iv: ByteArray, val data: ByteArray, val iterations: Int)

    /**
     * PBKDF2 with HMAC-SHA256 (RFC 8018). Written by hand because the JCA name PBKDF2WithHmacSHA256 only exists from
     * Android 8 (API 26) and the app starts at API 24; the result is identical on every device.
     */
    fun pbkdf2(passphrase: String, salt: ByteArray, iterations: Int, keyLen: Int = KEY_BYTES): ByteArray {
        require(passphrase.isNotEmpty() && iterations > 0 && keyLen > 0)
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(passphrase.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val hLen = mac.macLength
        val out = ByteArray(keyLen)
        var block = 1
        var pos = 0
        while (pos < keyLen) {
            mac.update(salt)
            mac.update(byteArrayOf((block ushr 24).toByte(), (block ushr 16).toByte(), (block ushr 8).toByte(), block.toByte()))
            var u = mac.doFinal()
            val t = u.copyOf()
            for (i in 1 until iterations) {
                u = mac.doFinal(u)
                for (j in t.indices) t[j] = (t[j].toInt() xor u[j].toInt()).toByte()
            }
            val n = minOf(hLen, keyLen - pos)
            System.arraycopy(t, 0, out, pos, n)
            pos += n
            block++
        }
        return out
    }

    fun seal(plain: ByteArray, passphrase: String, rnd: SecureRandom = SecureRandom(), iterations: Int = ITERATIONS): Sealed {
        require(validPassphrase(passphrase))
        val salt = ByteArray(SALT_BYTES).also { rnd.nextBytes(it) }
        val iv = ByteArray(IV_BYTES).also { rnd.nextBytes(it) }
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, SecretKeySpec(pbkdf2(passphrase, salt, iterations), "AES"), GCMParameterSpec(128, iv))
        c.updateAAD(AAD)
        return Sealed(salt, iv, c.doFinal(plain), iterations)
    }

    /** Decrypts; null = wrong passphrase or damaged data (GCM checks both). */
    fun open(s: Sealed, passphrase: String): ByteArray? = try {
        if (passphrase.isEmpty() || s.iterations !in 1..10_000_000 || s.iv.size != IV_BYTES) null
        else {
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.DECRYPT_MODE, SecretKeySpec(pbkdf2(passphrase, s.salt, s.iterations), "AES"), GCMParameterSpec(128, s.iv))
            c.updateAAD(AAD)
            c.doFinal(s.data)
        }
    } catch (e: Exception) {
        null
    }
}
