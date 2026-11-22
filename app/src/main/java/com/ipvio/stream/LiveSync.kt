package com.ipvio.stream

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Live sync over Google Drive (W-010b): favorites and history follow the user between devices without any button
 * and without any message.
 *
 * - Upload: a few seconds after a change (favorites 3 s; history, which changes every few seconds while playing,
 *   at most once a minute) and when the app goes to the background.
 * - Download: when the app comes to the front and every minute while it is in front. Only the change times of the
 *   live files are asked for; a file is only loaded when it changed.
 * - Merging instead of overwriting, see [LiveSyncLogic]. Before merged data is written, the current data is saved
 *   ([Backup.saveAutoSafety]), so the last automatic merge can be undone.
 *
 * Each device writes only its own live file ([DriveLogic.liveFileName]), apart from the manual backup file.
 * Only the active profile is merged into the app; the logs of other profiles are kept and uploaded unchanged.
 * Everything here is silent: errors are only kept in [lastError] for the Drive section in the settings.
 */
object LiveSync {
    /** Merge bases (last synced state per list), kept on this device and never part of a backup. */
    private const val PREFS = "ipvio_livesync"
    /** Switches of this device (live sync on/off, categories); stay on the device like the Drive sign-in. */
    private const val DRIVE_PREFS = "ipvio_drive"

    const val FORMAT = 3
    private const val FAV_DELAY_MS = 3_000L
    private const val HIST_GAP_MS = 60_000L
    private const val POLL_MS = 60_000L

    private enum class Kind { SET, RECENT, WATCH, FILE }

    /** One synced list: preference file (without profile suffix), key inside it (FILE = every key), rule, category. */
    private class Channel(val file: String, val key: String, val kind: Kind, val cat: SyncLogic.Category, val limit: Int)

    private val CHANNELS = listOf(
        Channel("ipvio_lib", "favs", Kind.SET, SyncLogic.Category.FAVORITES, 0),
        Channel("ipvio_lib", "watch", Kind.SET, SyncLogic.Category.FAVORITES, 0),
        Channel("ipvio_catfavs", "c", Kind.SET, SyncLogic.Category.FAVORITES, 0),
        Channel("ipvio_hist", "ids", Kind.RECENT, SyncLogic.Category.HISTORY, 80),
        Channel("ipvio_watch", "e", Kind.WATCH, SyncLogic.Category.HISTORY, 30),
        Channel("ipvio_eprog", "*", Kind.FILE, SyncLogic.Category.HISTORY, 400),
    )
    private val FILES = CHANNELS.map { it.file }.distinct()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private var app: Context? = null
    private var started = false
    private var foreground = false
    private var pollJob: Job? = null
    private var scheduled: Job? = null
    /** True while merged data is written, so the change listener does not take it for a change by the user. */
    private var applying = false
    private var dirtyFav = false
    private var dirtyHist = false
    private var lastUpload = 0L
    /** Remote file id -> change time already read. */
    private val seen = HashMap<String, Long>()
    /** Device id -> its logs (key "file/key"), so a merge always sees all devices, not only the one that changed. */
    private val remote = HashMap<String, Map<String, LiveSyncLogic.Log>>()
    /** Android keeps the change listeners only weakly: hold them here. */
    private val listeners = ArrayList<Pair<SharedPreferences, SharedPreferences.OnSharedPreferenceChangeListener>>()

    /** Time of the last successful upload or download (ms), 0 = none yet. */
    var lastOk by mutableLongStateOf(0L)
        private set
    /** Short technical reason of the last failure, empty = none. Shown only in the Drive section. */
    var lastError by mutableStateOf("")
        private set

    private fun drivePrefs(c: Context) = c.applicationContext.getSharedPreferences(DRIVE_PREFS, Context.MODE_PRIVATE)
    private fun basePrefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Live sync switched on for this device. Off by default: the user switches it on in the Drive settings of each device. */
    fun isOn(c: Context): Boolean = drivePrefs(c).getBoolean("auto", false)
    fun favoritesOn(c: Context): Boolean = drivePrefs(c).getBoolean("auto_fav", true)
    fun historyOn(c: Context): Boolean = drivePrefs(c).getBoolean("auto_hist", true)

    private fun catOn(c: Context, cat: SyncLogic.Category) = when (cat) {
        SyncLogic.Category.FAVORITES -> favoritesOn(c)
        SyncLogic.Category.HISTORY -> historyOn(c)
        else -> false
    }

    private fun enabled(c: Context) = DriveSync.available() && DriveSync.connected && isOn(c)

    /** Switches from the settings; a switch turned on syncs at once. */
    fun setSwitches(c: Context, on: Boolean, fav: Boolean, hist: Boolean) {
        drivePrefs(c).edit().putBoolean("auto", on).putBoolean("auto_fav", fav).putBoolean("auto_hist", hist).apply()
        if (on && started) { dirtyFav = true; dirtyHist = true; schedule(0L); scope.launch { pull() } }
    }

    /** Starts the live sync once the data of the active profile is loaded (AppContent). Main thread. */
    fun start(c: Context) {
        if (started) return
        started = true
        val a = c.applicationContext
        app = a
        DriveSync.init(a)
        for (f in FILES) {
            val sp = Profiles.sp(a, f)
            val cat = CHANNELS.first { it.file == f }.cat
            val l = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onLocalChange(cat) }
            sp.registerOnSharedPreferenceChangeListener(l)
            listeners += sp to l
        }
        onForeground()
    }

    /** App in front (MainActivity.onStart): fetch now and then every minute. */
    fun onForeground() {
        if (!started) return
        foreground = true
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive && foreground) {
                pull()
                delay(POLL_MS)
            }
        }
    }

    /** App in the background (MainActivity.onStop): stop fetching, upload pending changes at once. */
    fun onBackground() {
        if (!started) return
        foreground = false
        pollJob?.cancel()
        pollJob = null
        if (dirtyFav || dirtyHist) schedule(0L)
    }

    private fun onLocalChange(cat: SyncLogic.Category) {
        if (applying) return
        val c = app ?: return
        if (!enabled(c) || !catOn(c, cat)) return
        if (cat == SyncLogic.Category.FAVORITES) dirtyFav = true else dirtyHist = true
        val now = System.currentTimeMillis()
        val wait = if (dirtyFav) FAV_DELAY_MS else maxOf(FAV_DELAY_MS, lastUpload + HIST_GAP_MS - now)
        schedule(wait)
    }

    /** Upload after [wait] ms; a new change before that starts the wait again (only the last one counts). */
    private fun schedule(wait: Long) {
        scheduled?.cancel()
        scheduled = scope.launch {
            if (wait > 0) delay(wait)
            scheduled = null
            // Own job: a later schedule() must not cancel an upload that is already running
            scope.launch { upload() }
        }
    }

    private fun logKey(scopedFile: String, key: String) = "$scopedFile/$key"

    // ---- Reading and writing the app's own lists (main thread) ----

    private fun lines(s: String?) = (s ?: "").split("\n").filter { it.isNotBlank() }

    /** Current value of a channel: ids (SET, RECENT) or id -> value (WATCH, FILE). */
    private fun readList(sp: SharedPreferences, ch: Channel): List<String> = lines(sp.getString(ch.key, ""))

    private fun readRecords(sp: SharedPreferences, ch: Channel): LinkedHashMap<String, String> {
        val m = LinkedHashMap<String, String>()
        if (ch.kind == Kind.FILE) {
            for ((k, v) in sp.all) if (v is String && k.isNotEmpty()) m[k] = v
        } else {
            runCatching {
                val a = JSONArray(sp.getString(ch.key, null) ?: "[]")
                for (i in 0 until a.length()) {
                    val o = a.optJSONObject(i) ?: continue
                    val id = o.optString("i")
                    if (id.isNotEmpty() && id !in m) m[id] = o.toString()
                }
            }
        }
        return m
    }

    private fun track(sp: SharedPreferences, ch: Channel, base: LiveSyncLogic.Log, now: Long): LiveSyncLogic.Log = when (ch.kind) {
        Kind.SET -> LiveSyncLogic.trackSet(base, readList(sp, ch), now)
        Kind.RECENT -> LiveSyncLogic.trackRecent(base, readList(sp, ch), now)
        Kind.WATCH, Kind.FILE -> LiveSyncLogic.trackRecords(base, readRecords(sp, ch), now)
    }

    private fun loadBase(c: Context, k: String) = LiveSyncLogic.fromJsonText(basePrefs(c).getString(k, null))

    /** Brings the bases of the active profile up to date with the current lists (records local changes). */
    private fun trackActive(c: Context, now: Long) {
        val e = basePrefs(c).edit()
        for (ch in CHANNELS) {
            if (!catOn(c, ch.cat)) continue
            val k = logKey(Profiles.scopedName(ch.file, Profiles.active), ch.key)
            val log = track(Profiles.sp(c, ch.file), ch, loadBase(c, k), now)
            e.putString(k, LiveSyncLogic.toJson(log).toString())
        }
        e.apply()
    }

    /** The live file of this device: all stored logs (all profiles) of the switched-on categories. */
    private fun liveText(c: Context, now: Long): String {
        val logs = JSONObject()
        for ((k, v) in basePrefs(c).all) {
            if (v !is String) continue
            val ch = channelOf(k) ?: continue
            if (!catOn(c, ch.cat)) continue
            logs.put(k, LiveSyncLogic.fromJsonText(v).let { LiveSyncLogic.toJson(it) })
        }
        return JSONObject().put("v", FORMAT).put("kind", "live").put("at", now)
            .put("device", JSONObject().put("id", Backup.deviceId(c)).put("name", Backup.deviceName()))
            .put("app", BuildConfig.VERSION_NAME).put("logs", logs).toString()
    }

    /** Channel of a log key "ipvio_hist_p2/ids"; null for unknown keys. */
    private fun channelOf(k: String): Channel? {
        val i = k.lastIndexOf('/')
        if (i <= 0) return null
        val file = SyncLogic.baseName(k.substring(0, i))
        val key = k.substring(i + 1)
        return CHANNELS.firstOrNull { it.file == file && it.key == key }
    }

    private fun parseLive(text: String): Map<String, LiveSyncLogic.Log>? = runCatching {
        val root = JSONObject(text)
        if (root.optInt("v") != FORMAT || root.optString("kind") != "live") return null
        val logs = root.optJSONObject("logs") ?: return null
        val m = HashMap<String, LiveSyncLogic.Log>()
        for (k in logs.keys()) if (channelOf(k) != null) m[k] = LiveSyncLogic.fromJson(logs.optJSONObject(k))
        m
    }.getOrNull()

    /** Result of merging one channel: what to write and the new base. */
    private class Plan(val ch: Channel, val key: String, val base: LiveSyncLogic.Log, val write: Any, val changed: Boolean)

    private fun plan(c: Context, ch: Channel, now: Long): Plan {
        val sp = Profiles.sp(c, ch.file)
        val k = logKey(Profiles.scopedName(ch.file, Profiles.active), ch.key)
        val local = track(sp, ch, loadBase(c, k), now)
        val merged = LiveSyncLogic.merge(listOf(local) + remote.values.mapNotNull { it[k] }, now)
        return when (ch.kind) {
            Kind.SET -> {
                val cur = readList(sp, ch)
                val out = LiveSyncLogic.orderedIds(merged, cur)
                Plan(ch, k, merged, out, out != cur)
            }
            Kind.RECENT -> {
                val cur = readList(sp, ch)
                val out = LiveSyncLogic.recentIds(merged, ch.limit)
                Plan(ch, k, LiveSyncLogic.keepOnly(merged, out), out, out != cur)
            }
            Kind.WATCH -> {
                val cur = readRecords(sp, ch)
                // Only entries that are real resume points of this id are written
                val out = byOwnTime(merged, ch) { id, v -> runCatching { JSONObject(v).optString("i") == id }.getOrDefault(false) }
                Plan(ch, k, LiveSyncLogic.keepOnly(merged, out.map { it.first }), out, out != cur.toList())
            }
            Kind.FILE -> {
                val cur = readRecords(sp, ch)
                val out = byOwnTime(merged, ch) { _, _ -> true }.toMap()
                Plan(ch, k, LiveSyncLogic.keepOnly(merged, out.keys), out, out != cur)
            }
        }
    }

    /**
     * The newest [Channel.limit] records, ordered by the time stored in the record itself (resume point "ts", episode
     * progress "ts|..."), like the stores do. The merge time only decides which value wins, not the order: on the first
     * sync all entries get the same merge time and the list must not be shuffled.
     */
    private fun byOwnTime(merged: LiveSyncLogic.Log, ch: Channel, ok: (String, String) -> Boolean): List<Pair<String, String>> {
        fun own(v: String): Long = if (ch.kind == Kind.WATCH) runCatching { JSONObject(v).optLong("ts") }.getOrDefault(0L)
        else v.substringBefore('|').toLongOrNull() ?: 0L
        return merged.items.entries.filter { ok(it.key, it.value.value) }
            .sortedWith(compareByDescending<Map.Entry<String, LiveSyncLogic.Rec>> { own(it.value.value) }.thenBy { it.key })
            .take(ch.limit).map { it.key to it.value.value }
    }

    /** Merges the remote logs into the active profile and writes what changed. True = something was written. */
    private fun mergeAndApply(c: Context, now: Long): Boolean {
        val plans = CHANNELS.filter { catOn(c, it.cat) }.map { plan(c, it, now) }
        val be = basePrefs(c).edit()
        applying = true
        try {
            for (p in plans) {
                be.putString(p.key, LiveSyncLogic.toJson(p.base).toString())
                if (!p.changed) continue
                val e = Profiles.sp(c, p.ch.file).edit()
                @Suppress("UNCHECKED_CAST")
                when (p.ch.kind) {
                    Kind.SET, Kind.RECENT -> e.putString(p.ch.key, (p.write as List<String>).joinToString("\n"))
                    Kind.WATCH -> {
                        val a = JSONArray()
                        for ((_, v) in p.write as List<Pair<String, String>>) a.put(JSONObject(v))
                        e.putString(p.ch.key, a.toString())
                    }
                    Kind.FILE -> {
                        val out = p.write as Map<String, String>
                        val sp = Profiles.sp(c, p.ch.file)
                        for (k in sp.all.keys) if (k !in out) e.remove(k)
                        for ((k, v) in out) if (sp.getString(k, null) != v) e.putString(k, v)
                    }
                }
                e.apply()
            }
            be.apply()
        } finally {
            applying = false
        }
        val changed = plans.filter { it.changed }
        if (changed.isEmpty()) return false
        // The lists on screen follow the files
        if (changed.any { it.ch.file == "ipvio_lib" }) Library.reload(c)
        if (changed.any { it.ch.file == "ipvio_catfavs" }) CatFavs.reload(c)
        if (changed.any { it.ch.file == "ipvio_hist" }) History.reload(c)
        if (changed.any { it.ch.file == "ipvio_watch" }) WatchStore.reload(c)
        if (changed.any { it.ch.file == "ipvio_eprog" }) EpProgress.reload()
        return true
    }

    // ---- Network ----

    private fun failed(e: Throwable) {
        lastError = (e as? DriveSync.DriveException)?.let { it.reason.name + (if (it.code != 0) " " + it.code else "") + " " + it.detail }?.trim()
            ?: e.javaClass.simpleName
    }

    private suspend fun upload() {
        val c = app ?: return
        if (!enabled(c)) return
        mutex.withLock {
            val now = System.currentTimeMillis()
            val text = withContext(Dispatchers.Main) {
                trackActive(c, now)
                dirtyFav = false
                dirtyHist = false
                liveText(c, now)
            }
            DriveSync.uploadLive(c, text)
                .onSuccess { lastUpload = System.currentTimeMillis(); lastOk = lastUpload; lastError = "" }
                .onFailure { failed(it); dirtyHist = true }   // try again with the next change or when leaving the app
        }
    }

    private suspend fun pull() {
        val c = app ?: return
        if (!enabled(c)) return
        mutex.withLock {
            val listed = DriveSync.listLive(c)
            val files = listed.getOrNull()
            if (files == null) { listed.exceptionOrNull()?.let { failed(it) }; return }
            val me = Backup.deviceId(c)
            var news = false
            val present = HashSet<String>()
            for (r in files) {
                val dev = r.deviceId ?: continue
                if (dev == me) continue
                present += dev
                if (seen[r.id] == r.modified && remote.containsKey(dev)) continue
                val got = DriveSync.download(c, r)
                val text = got.getOrNull()
                if (text == null) { got.exceptionOrNull()?.let { failed(it) }; continue }
                val logs = parseLive(text) ?: continue
                remote[dev] = logs
                seen[r.id] = r.modified
                news = true
            }
            // A device whose live file is gone (deleted in Drive) no longer counts
            if (remote.keys.retainAll(present)) news = true
            lastOk = System.currentTimeMillis()
            lastError = ""
            if (!news) return
            if (!withContext(Dispatchers.IO) { Backup.saveAutoSafety(c) }) { lastError = "safety copy"; return }
            val wrote = withContext(Dispatchers.Main) { mergeAndApply(c, System.currentTimeMillis()) }
            // Our own file should show the merged state too (keeps tombstones of other devices alive)
            if (wrote) withContext(Dispatchers.Main) { dirtyHist = true; schedule(maxOf(FAV_DELAY_MS, lastUpload + HIST_GAP_MS - System.currentTimeMillis())) }
        }
    }
}
