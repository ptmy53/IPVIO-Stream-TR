package com.ipvio.stream

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * T-015: checks a downloaded APK before the system installer opens, so the user gets a clear text instead of Android's
 * "App not installed". The rules themselves are pure and tested in [UpdateLogic]; this file only reads the APK.
 *
 * Checks, in this order: the file is a readable APK; it belongs to this app (package name); its versionCode is higher than
 * the installed one (and equals the announced one, if there is one); its signer certificate matches the installed app.
 * Android still has the last word at install time. **Unverified on a device:** reading signatures of an APK file on API below 28
 * (`GET_SIGNATURES` with `getPackageArchiveInfo`).
 */
object UpdateVerify {
    private class Signers(val current: Set<String>, val history: Set<String>)

    /** Returns null if the file may be installed, otherwise a short German text for the user. */
    fun check(c: Context, apk: File, info: UpdateInfo): String? {
        val pm = c.packageManager
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
        val archive = runCatching { pm.getPackageArchiveInfo(apk.absolutePath, flags) }.getOrNull()
            ?: return "Die Datei ist keine gültige App-Datei."
        if (archive.packageName != c.packageName) return "Die Datei gehört zu einer anderen App."
        when (UpdateLogic.versionProblem(AppUpdate.currentCode(c), codeOf(archive), info.code)) {
            UpdateLogic.VersionProblem.CODE_MISMATCH -> return "Die Versionsnummer passt nicht zur Update-Datei."
            UpdateLogic.VersionProblem.NOT_NEWER -> return "Die Datei ist nicht neuer als die installierte Version."
            null -> {}
        }
        val have = installedSigners(c, flags)
        val got = archiveSigners(c, apk, archive)
        return when (UpdateLogic.signerVerdict(have.current, got.current, got.history)) {
            UpdateLogic.SignerVerdict.MATCH -> null
            // Could not be read on this device: Android checks the signature itself when installing, so the update goes on.
            // Before 1.4.3 this stopped the update with "Die Signatur konnte nicht geprüft werden." (Fire OS 7).
            UpdateLogic.SignerVerdict.UNKNOWN -> {
                android.util.Log.w("IPVIO-UPD", "signer not readable (installed ${have.current.size}, apk ${got.current.size}, API ${Build.VERSION.SDK_INT}); left to the installer")
                null
            }
            UpdateLogic.SignerVerdict.MISMATCH -> "Die Signatur passt nicht zur installierten App. Android würde das Update ablehnen. " +
                "Erstelle ein Backup, installiere die App neu und stelle das Backup wieder her."
        }
    }

    /** Signers of the installed app: signing certificates, then the older signatures field. */
    @Suppress("DEPRECATION")
    private fun installedSigners(c: Context, flags: Int): Signers {
        val pm = c.packageManager
        val a = signersOf(runCatching { pm.getPackageInfo(c.packageName, flags) }.getOrNull())
        if (a.current.isNotEmpty()) return a
        return signersOf(runCatching { pm.getPackageInfo(c.packageName, PackageManager.GET_SIGNATURES) }.getOrNull(), legacy = true)
    }

    /**
     * Signers of the downloaded APK, three ways: what the package manager read with the first flags, the older signatures
     * field (Android 9 fills that one when signingInfo stays empty), and the v1 signature read directly from the file.
     */
    @Suppress("DEPRECATION")
    private fun archiveSigners(c: Context, apk: File, first: PackageInfo): Signers {
        val a = signersOf(first)
        if (a.current.isNotEmpty()) return a
        val b = signersOf(runCatching { c.packageManager.getPackageArchiveInfo(apk.absolutePath, PackageManager.GET_SIGNATURES) }.getOrNull(), legacy = true)
        if (b.current.isNotEmpty()) return b
        return Signers(jarSigners(apk), emptySet())
    }

    /**
     * v1 (JAR) signature of the APK: certificates of AndroidManifest.xml, available after the entry was read completely.
     * Release APKs are v2-only (minSdk 24, AGP signs v1 only below that), so this finds nothing for them; kept for APKs
     * that carry a v1 signature. An empty result never blocks the update (see [UpdateLogic.signerVerdict]).
     */
    private fun jarSigners(apk: File): Set<String> = runCatching {
        java.util.jar.JarFile(apk, true).use { jf ->
            val e = jf.getJarEntry("AndroidManifest.xml") ?: return@use emptySet<String>()
            jf.getInputStream(e).use { s -> val buf = ByteArray(8192); while (s.read(buf) >= 0) { } }
            e.certificates.orEmpty().map { UpdateLogic.hex(MessageDigest.getInstance("SHA-256").digest(it.encoded)) }.toSet()
        }
    }.getOrDefault(emptySet())

    private fun codeOf(pi: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode else @Suppress("DEPRECATION") pi.versionCode.toLong()

    private fun digest(s: Signature): String = UpdateLogic.hex(MessageDigest.getInstance("SHA-256").digest(s.toByteArray()))

    private fun signersOf(pi: PackageInfo?, legacy: Boolean = false): Signers {
        if (pi == null) return Signers(emptySet(), emptySet())
        return runCatching {
            if (Build.VERSION.SDK_INT >= 28 && !legacy) {
                val si = pi.signingInfo ?: return@runCatching Signers(emptySet(), emptySet())
                val current = si.apkContentsSigners.orEmpty().map(::digest).toSet()
                // The history (key rotation) exists only for a single signer.
                val history = if (si.hasMultipleSigners()) emptySet() else si.signingCertificateHistory.orEmpty().map(::digest).toSet()
                Signers(current, history)
            } else {
                @Suppress("DEPRECATION")
                Signers(pi.signatures.orEmpty().map(::digest).toSet(), emptySet())
            }
        }.getOrDefault(Signers(emptySet(), emptySet()))
    }
}
