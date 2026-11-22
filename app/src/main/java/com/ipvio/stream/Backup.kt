package com.ipvio.stream

import android.content.Context
import android.os.Build
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Backup of personal data (settings, favorites, history, profiles) into a file.
 * Playlists and login data are deliberately not included in the file on the device: they stay on the device.
 *
 * Format version 1: file backup, Wi-Fi transfer (playlists optional, plain text, only between own devices).
 * Format version 2 (W-010 device sync): adds device info; playlists and own API keys only in an encrypted part
 * ("secure") that needs the user's sync passphrase. Device-only data (parental control PIN, update state) never
 * travels, see [SyncLogic].
 *
 * Every restore first writes a safety copy of the current data, so the last restore can be undone.
 */
object Backup {
    private val NAMES = listOf(
        "ipvio_general", "ipvio_settings", "ipvio_lib", "ipvio_quality", "ipvio_hist", "ipvio_watch",
        "ipvio_catfavs", "ipvio_variants", "ipvio_guide", "ipvio_browse", "ipvio_cat", "ipvio_lang",
        "ipvio_chlists", "ipvio_tracks", "ipvio_eprog",
        // Round 20: live buffer/recordings, sidebar, phone, updates
        "ipvio_livetv", "ipvio_side", "ipvio_mobile", "ipvio_upd",
        // 1.2: sports (teams, notifications, leagues, channel correction), hidden/ignored groups, learned live quality
        "ipvio_sportfavs", "ipvio_sportalerts", "ipvio_sportleagues", "ipvio_sporthub", "ipvio_sportfix", "ipvio_hidecats", "ipvio_browselang", "ipvio_ignored", "ipvio_livequal",
        // Home block builder and Boomer mode
        "ipvio_homelayout", "ipvio_boomerlayout", "ipvio_boomer",
        // T-006: dual view, last live category, new episodes, imported lists, resume log (profile-scoped like the others)
        "ipvio_dual", "ipvio_live", "ipvio_newep", "ipvio_importedlists", "ipvio_resumelog",
        // 1.4: own order of categories (Live TV, movies, series)
        "ipvio_catorder",
        // T-028: private categories
        "ipvio_privatecats",
        // T-049d: Sports Hub settings
        "ipvio_sportset"
    )

    /** Own API keys (TMDB, OMDb); not profile-scoped. Only travel inside the encrypted part of a sync. */
    private const val KEYS = "ipvio_keys"
    /** Device id and name for the sync; never part of a backup. */
    private const val SYNC_PREFS = "ipvio_sync"

    /** A real backup never gets bigger; larger files are not even read. */
    private const val MAX_BYTES = 4L * 1024 * 1024

    /** Restore results below zero. */
    const val ERR_INVALID = -1
    const val ERR_PASSPHRASE = -2
    const val ERR_SAFETY = -3

    private val ALL = SyncLogic.Category.values().toSet()

    fun file(c: Context): File = File(c.getExternalFilesDir(null) ?: c.filesDir, "ipvio_backup.json")

    /** Copy of the data right before the last restore (internal storage, without playlists). */
    private fun safetyFile(c: Context): File = File(c.filesDir, "ipvio_before_restore.json")

    private fun fileNames(c: Context): List<String> {
        Profiles.init(c)
        return listOf(Profiles.META) + Profiles.list.flatMap { p -> NAMES.map { Profiles.scopedName(it, p.id) } }
    }

    /** Stable random id of this installation, so every device has its own sync file. */
    fun deviceId(c: Context): String {
        val p = c.applicationContext.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
        p.getString("id", null)?.let { return it }
        val id = UUID.randomUUID().toString().replace("-", "").take(16)
        p.edit().putString("id", id).apply()
        return id
    }

    fun deviceName(): String = listOf(Build.MANUFACTURER, Build.MODEL).filter { !it.isNullOrBlank() }.distinct()
        .joinToString(" ").ifBlank { "Android" }.take(60)

    /** Writes the backup and returns the file (or null on error). */
    fun export(c: Context): File? = runCatching {
        val f = file(c)
        f.parentFile?.mkdirs()
        f.writeText(exportText(c, false) ?: return null)
        f
    }.getOrNull()

    private fun prefsToJson(all: Map<String, *>, skip: (String) -> Boolean = { false }): JSONObject {
        val o = JSONObject()
        for ((k, v) in all) {
            if (skip(k)) continue
            when (v) {
                is String -> o.put(k, JSONArray().put("s").put(v))
                is Boolean -> o.put(k, JSONArray().put("b").put(v))
                is Int -> o.put(k, JSONArray().put("i").put(v))
                is Long -> o.put(k, JSONArray().put("l").put(v))
                is Float -> o.put(k, JSONArray().put("f").put(v.toDouble()))
                is Set<*> -> o.put(k, JSONArray().put("t").put(JSONArray(v.map { it.toString() })))
            }
        }
        return o
    }

    /**
     * Backup as text. [withPlaylists] = also the playlists with login data (only for transfer to another
     * own device on the Wi-Fi; never in the file on the device).
     */
    fun exportText(c: Context, withPlaylists: Boolean): String? = exportText(c, withPlaylists, false)

    /** [includeEmpty] = also empty files, so restoring this text clears what was empty (safety copy for undo). */
    private fun exportText(c: Context, withPlaylists: Boolean, includeEmpty: Boolean): String? = runCatching {
        val root = JSONObject().put("v", 1).put("at", System.currentTimeMillis())
        val files = JSONObject()
        for (n in fileNames(c)) {
            val all = c.applicationContext.getSharedPreferences(n, Context.MODE_PRIVATE).all
            if (all.isEmpty() && !includeEmpty) continue
            files.put(n, prefsToJson(all))
        }
        root.put("files", files)
        if (withPlaylists) root.put("playlists", PlaylistStore.exportLists(c))
        root.toString()
    }.getOrNull()

    /**
     * Sync text (format 2) for Google Drive. Device-only data is left out. With a valid [passphrase] the playlists
     * (with backup servers) and the own API keys are added in encrypted form; without one they stay on the device.
     * Slow on weak devices (key derivation): call off the main thread.
     */
    fun exportSync(c: Context, passphrase: String?): String? = runCatching {
        val root = JSONObject().put("v", SyncLogic.FORMAT_VERSION).put("at", System.currentTimeMillis())
            .put("device", JSONObject().put("id", deviceId(c)).put("name", deviceName()))
            .put("app", BuildConfig.VERSION_NAME)
        val files = JSONObject()
        for (n in fileNames(c)) {
            if (!SyncLogic.travels(n)) continue
            val all = c.applicationContext.getSharedPreferences(n, Context.MODE_PRIVATE).all
            val o = prefsToJson(all) { SyncLogic.deviceOnlyKey(n, it) }
            if (o.length() > 0) files.put(n, o)
        }
        root.put("files", files)
        if (passphrase != null && SyncLogic.validPassphrase(passphrase)) {
            val keys = c.applicationContext.getSharedPreferences(KEYS, Context.MODE_PRIVATE).all
            val plain = JSONObject().put("playlists", PlaylistStore.exportLists(c)).put("keys", prefsToJson(keys)).toString()
            val s = SyncLogic.seal(plain.toByteArray(Charsets.UTF_8), passphrase)
            root.put("secure", JSONObject().put("kdf", SyncLogic.KDF).put("iter", s.iterations)
                .put("salt", b64(s.salt)).put("iv", b64(s.iv)).put("data", b64(s.data)))
        }
        root.toString()
    }.getOrNull()

    private fun b64(b: ByteArray) = Base64.encodeToString(b, Base64.NO_WRAP)
    private fun unb64(s: String) = Base64.decode(s, Base64.NO_WRAP)

    /** What a backup contains, shown before restoring. */
    class Summary(
        val version: Int,
        val at: Long,
        val deviceId: String?,
        val deviceName: String?,
        val appVersion: String?,
        val profiles: Int,
        val favorites: Int,
        val categories: Set<SyncLogic.Category>,
        /** Encrypted playlists and keys (format 2, needs the passphrase). */
        val hasSecure: Boolean,
        /** Plain playlists (format 1 from the Wi-Fi transfer). */
        val hasPlainPlaylists: Boolean,
    )

    fun summary(text: String): Summary? = runCatching {
        if (text.length > MAX_BYTES) return null
        val root = JSONObject(text)
        val v = root.optInt("v", 0)
        if (v != 1 && v != SyncLogic.FORMAT_VERSION) return null
        val files = root.getJSONObject("files")
        val cats = mutableSetOf<SyncLogic.Category>()
        val profileIds = mutableSetOf<String>()
        var favs = 0
        for (n in files.keys()) {
            if (!known(n)) continue
            cats += SyncLogic.categoryOf(n)
            if (n != Profiles.META) profileIds += n.removePrefix(SyncLogic.baseName(n))
            if (SyncLogic.baseName(n) == "ipvio_lib") {
                files.getJSONObject(n).optJSONArray("favs")?.optString(1)?.let { s -> favs += s.split("\n").count { it.isNotBlank() } }
            }
        }
        val dev = root.optJSONObject("device")
        Summary(
            v, root.optLong("at", 0L), dev?.optString("id")?.ifBlank { null }, dev?.optString("name")?.ifBlank { null },
            root.optString("app").ifBlank { null }, maxOf(profileIds.size, 1), favs, cats,
            root.optJSONObject("secure") != null, root.optJSONArray("playlists") != null
        )
    }.getOrNull()

    private fun known(n: String) = n == Profiles.META || NAMES.any { n == it || n.startsWith(it + "_p") }

    /** Reads the backup back. Returns the number of restored files, below zero on error. */
    fun restore(c: Context): Int = runCatching {
        val f = file(c)
        if (!f.exists() || f.length() > MAX_BYTES) return ERR_INVALID
        restoreText(c, f.readText())
    }.getOrDefault(ERR_INVALID)

    /**
     * Restore a backup from text (file, Wi-Fi, Google Drive). Only the [chosen] categories are written; device-only
     * keys (parental control PIN) keep their local value. Playlists are added, never replaced; API keys are only
     * filled in where this device has none. The encrypted part of format 2 is only read with the right [passphrase]
     * (wrong passphrase = [ERR_PASSPHRASE], nothing changed). Before anything is written, the current data is saved
     * to a safety copy ([undoLastRestore]); if that fails, nothing is restored ([ERR_SAFETY]).
     * Returns the number of restored files, below zero on error.
     */
    fun restoreText(
        c: Context,
        text: String,
        chosen: Set<SyncLogic.Category> = ALL,
        withPlaylists: Boolean = true,
        passphrase: String? = null,
        safety: Boolean = true,
    ): Int = runCatching {
        if (text.length > MAX_BYTES) return ERR_INVALID
        val root = JSONObject(text)
        val v = root.optInt("v", 0)
        if (v != 1 && v != SyncLogic.FORMAT_VERSION) return ERR_INVALID
        val files = root.getJSONObject("files")

        // Decrypt first: a wrong passphrase must not change anything.
        var secure: JSONObject? = null
        val sec = root.optJSONObject("secure")
        if (withPlaylists && sec != null && passphrase != null) {
            if (sec.optString("kdf") != SyncLogic.KDF) return ERR_INVALID
            val plain = SyncLogic.open(
                SyncLogic.Sealed(unb64(sec.getString("salt")), unb64(sec.getString("iv")), unb64(sec.getString("data")), sec.getInt("iter")),
                passphrase
            ) ?: return ERR_PASSPHRASE
            secure = JSONObject(String(plain, Charsets.UTF_8))
        }

        if (safety) {
            val now = exportText(c, false, true) ?: return ERR_SAFETY
            val ok = runCatching { safetyFile(c).writeText(now); true }.getOrDefault(false)
            if (!ok) return ERR_SAFETY
        }

        var count = 0
        for (n in files.keys()) {
            // Allow known files only
            if (!known(n) || !SyncLogic.shouldRestore(n, chosen)) continue
            val o = files.getJSONObject(n)
            val sp = c.applicationContext.getSharedPreferences(n, Context.MODE_PRIVATE)
            // Device-only keys keep their local value
            val keep = sp.all.filterKeys { SyncLogic.deviceOnlyKey(n, it) }
            val e = sp.edit().clear()
            for (k in o.keys()) {
                if (SyncLogic.deviceOnlyKey(n, k)) continue
                putTyped(e, k, o.getJSONArray(k))
            }
            for ((k, value) in keep) when (value) {
                is String -> e.putString(k, value)
                is Boolean -> e.putBoolean(k, value)
                is Int -> e.putInt(k, value)
                is Long -> e.putLong(k, value)
                is Float -> e.putFloat(k, value)
            }
            e.commit()
            count++
        }
        if (withPlaylists) {
            root.optJSONArray("playlists")?.let { PlaylistStore.importLists(c, it) }
            secure?.optJSONArray("playlists")?.let { PlaylistStore.importLists(c, it) }
            secure?.optJSONObject("keys")?.let { fillMissingKeys(c, it) }
        }
        count
    }.getOrDefault(ERR_INVALID)

    private fun putTyped(e: android.content.SharedPreferences.Editor, k: String, a: JSONArray) {
        when (a.getString(0)) {
            "s" -> e.putString(k, a.getString(1))
            "b" -> e.putBoolean(k, a.getBoolean(1))
            "i" -> e.putInt(k, a.getInt(1))
            "l" -> e.putLong(k, a.getLong(1))
            "f" -> e.putFloat(k, a.getDouble(1).toFloat())
            "t" -> { val arr = a.getJSONArray(1); e.putStringSet(k, (0 until arr.length()).map { arr.getString(it) }.toSet()) }
        }
    }

    /** Own API keys from a sync: only where this device has no key yet (never overwrite). */
    private fun fillMissingKeys(c: Context, o: JSONObject) {
        val sp = c.applicationContext.getSharedPreferences(KEYS, Context.MODE_PRIVATE)
        val e = sp.edit()
        for (k in o.keys()) {
            val a = o.optJSONArray(k) ?: continue
            if (a.optString(0) != "s") continue
            val v = a.optString(1)
            if (v.isNotBlank() && sp.getString(k, null).isNullOrBlank()) e.putString(k, v)
        }
        e.commit()
    }

    /** Copy of the data right before the last automatic merge of the live sync (W-010b); kept apart from the restore copy. */
    private fun autoSafetyFile(c: Context): File = File(c.filesDir, "ipvio_before_autosync.json")

    /** Saves the current data before the live sync writes merged data. False = could not save (then nothing is merged). */
    fun saveAutoSafety(c: Context): Boolean = runCatching {
        val now = exportText(c, false, true) ?: return false
        val f = autoSafetyFile(c)
        val tmp = File(f.path + ".tmp")
        tmp.writeText(now)
        tmp.renameTo(f) || run { f.writeText(now); true }
    }.getOrDefault(false)

    /** Time of the safety copy from the last automatic merge (ms), 0 = none. */
    fun autoUndoAvailableAt(c: Context): Long = autoSafetyFile(c).let { if (it.exists()) it.lastModified() else 0L }

    /** Puts back the data from right before the last automatic merge. Returns the number of files, below zero on error. */
    fun undoLastAutoSync(c: Context): Int = runCatching {
        val f = autoSafetyFile(c)
        if (!f.exists() || f.length() > MAX_BYTES) return ERR_INVALID
        restoreText(c, f.readText(), safety = false)
    }.getOrDefault(ERR_INVALID)

    /** Time of the safety copy from the last restore (ms), 0 = none. */
    fun undoAvailableAt(c: Context): Long = safetyFile(c).let { if (it.exists()) it.lastModified() else 0L }

    /** Puts back the data from right before the last restore. Returns the number of files, below zero on error. */
    fun undoLastRestore(c: Context): Int = runCatching {
        val f = safetyFile(c)
        if (!f.exists() || f.length() > MAX_BYTES) return ERR_INVALID
        restoreText(c, f.readText(), safety = false)
    }.getOrDefault(ERR_INVALID)
}
