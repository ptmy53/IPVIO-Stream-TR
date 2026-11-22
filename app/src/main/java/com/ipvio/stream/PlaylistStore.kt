package com.ipvio.stream

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.RandomAccessFile
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class PL(val name: String, val url: String)

/**
 * Playlists: list of saved logins, loading, cache.
 *
 * Loading flow (designed for providers with 800,000+ entries and devices with little memory):
 * 1. The login is remembered BEFORE the download (crash, power loss, app closed → loading resumes on next start).
 * 2. Download into a file (pl_*.part), not into memory. If the server allows it, an interrupted download is resumed.
 * 3. Parse entry by entry. Hidden languages are only counted. If the list is too big for the device and no
 *    language has been chosen yet, the user is asked which languages to load.
 * 4. The shown titles are stored (pl_*.bin, fast start) and the whole list compressed (pl_*.m3u.gz, ~10 %),
 *    so a different language selection works without a new download.
 */
object PlaylistStore {
    /**
     * The loaded entries – already without the languages hidden by the language filter.
     * Hidden languages are never loaded into memory in the first place (large providers: 283,000 → e.g. 57,000 titles).
     */
    private var rawChannels by mutableStateOf<List<Item>>(emptyList())
    /**
     * Adopt a new list. [shown] = already filtered by parental control (computed in the background), otherwise it is done here –
     * so filtering (regex over all titles) does not have to happen on the main thread while drawing.
     */
    private fun setItems(l: List<Item>, shown: List<Item>? = null) {
        LibMatch.reset()
        filtered = shown ?: shownOf(l); filteredFor = l; filteredVer = filterVer(); filteredBase = baseVer()
        rawChannels = l
    }

    /** State of the filters (parental control, "hide broken titles"): when it changes, the shown list is rebuilt. */
    private fun filterVer(): Int = Parental.version * 10_000 + (if (DeadLinks.hide) DeadLinks.version else 0) + IgnoredGroups.version * 1_000_000

    /** Filter state without the "ignore" part: a change here (e.g. parental control) is applied at once. */
    private fun baseVer(): Int = Parental.version * 10_000 + (if (DeadLinks.hide) DeadLinks.version else 0)

    /** Increased when a background refilter has finished, so readers of [channels] see the new list. */
    private var refilterTick by mutableIntStateOf(0)
    @Volatile private var filteredBase = -1
    @Volatile private var pendingVer = -1
    private var refilterJob: Job? = null
    private val refilterScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Only the "ignore" filter changed (categories or bundles switched in settings): filter the whole list in the background
     * instead of on the main thread while drawing. Until it is done, readers keep the previous list.
     */
    @Synchronized private fun refilterLater(raw: List<Item>, ver: Int) {
        if (pendingVer == ver) return
        pendingVer = ver
        refilterJob?.cancel()
        refilterJob = refilterScope.launch {
            val r = shownOf(raw)
            withContext(Dispatchers.Main) {
                if (rawChannels === raw && filterVer() == ver) {
                    filtered = r; filteredFor = raw; filteredVer = ver; filteredBase = baseVer()
                    refilterTick++
                }
                if (pendingVer == ver) pendingVer = -1
            }
        }
    }

    private fun shownOf(l: List<Item>): List<Item> {
        var r = if (Parental.hideAdult) Parental.filter(l) else l
        if (DeadLinks.hide) r = DeadLinks.filter(r)
        if (!IgnoredGroups.isEmpty) r = IgnoredGroups.filter(r)
        return r
    }
    @Volatile private var filteredFor: List<Item>? = null
    @Volatile private var filteredVer = -1
    @Volatile private var filtered: List<Item> = emptyList()

    /** Number of titles per language in the whole playlist (including hidden ones) – for the language selection in settings. */
    var langCounts by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    /** Amount of data per language of the whole playlist – for the memory estimate ([fits]). */
    var langBytes by mutableStateOf<Map<String, Long>>(emptyMap())
        private set
    /** The language selection with which the shown titles were read. */
    @Volatile private var shownHidden: Set<String> = emptySet()

    /** All entries as the app shows them: without hidden languages and, with parental control active, without adult content. */
    val channels: List<Item>
        get() {
            val raw = rawChannels
            val ver = filterVer()
            @Suppress("UNUSED_VARIABLE") val tick = refilterTick
            if (!Parental.hideAdult && !DeadLinks.hide && IgnoredGroups.isEmpty) return raw
            if (raw === filteredFor && ver != filteredVer && baseVer() == filteredBase) {
                // Only "ignore" changed: keep the previous list for a moment, filter in the background
                refilterLater(raw, ver)
                return filtered
            }
            if (raw !== filteredFor || ver != filteredVer) {
                filtered = shownOf(raw); filteredFor = raw; filteredVer = ver; filteredBase = baseVer()
            }
            return filtered
        }

    /** All loaded entries without the "Ignore" filters, parental control and broken titles – for managing ignored groups. */
    fun unfilteredChannels(): List<Item> = rawChannels

    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    /**
     * Updating failed while the old list is still shown (the old list stays). Before, this stayed silent, so it looked as if
     * the playlist simply did not update. Shown in the playlist settings; cleared by the next successful load.
     */
    var refreshError by mutableStateOf<String?>(null)
        private set
    /** true while downloading or parsing (not during fast reading of the cache) – then show the loading screen. */
    var heavy by mutableStateOf(false)
        private set
    /** Progress for the loading screen: current step, processed bytes and total size (-1 = unknown). */
    var phase by mutableStateOf("")
        private set
    var bytesRead by mutableLongStateOf(0L)
        private set
    var totalBytes by mutableLongStateOf(-1L)
        private set
    /** true as soon as download and parsing are done and only saving remains. */
    var downloadDone by mutableStateOf(false)
        private set
    /** Pending question: which languages should be loaded (very large playlist)? The loading screen then shows the selection. */
    var langAsk by mutableStateOf<LangAsk?>(null)
        private set
    /** Start of the running download/parse (for the hints on long load times in the loading screen), 0 = none. */
    var loadStarted by mutableLongStateOf(0L)
        private set
    /** User wants to choose languages after all while loading: ask after the download or immediately while parsing. */
    var askRequested by mutableStateOf(false)
        private set
    var epgUrls: List<String> = emptyList()
    var catOrder by mutableStateOf<Map<String, Int>>(emptyMap())
    val lists = mutableStateListOf<PL>()
    var active by mutableIntStateOf(0)

    // ------------------------------------------------------------------ several playlists at the same time (round 20)
    /** Further playlists shown in addition to the active one (key = hash of the address, see [MultiLogic]). */
    var multi by mutableStateOf<Set<Int>>(emptySet())
        private set
    /** Merged list of all shown sources (with the duplicates hidden in "All"). */
    var merged by mutableStateOf(MultiLogic.Merged(emptyList(), emptySet(), emptyMap()))
        private set
    /** Names of the shown sources: 0 = active playlist, then the others. */
    var sourceNames by mutableStateOf<List<String>>(emptyList())
        private set
    /** Notice when a further playlist could not be loaded. */
    var extraError by mutableStateOf<String?>(null)
    private class Extra(val items: List<Item>, val epg: List<String>, val order: Map<String, Int>)
    private val extras = LinkedHashMap<String, Extra>()
    @Volatile private var primaryItems: List<Item> = emptyList()
    @Volatile private var primaryUrl: String? = null
    /** Address of the active playlist currently shown (null = none loaded). */
    val shownUrl: String? get() = primaryUrl
    private var primaryEpg: List<String> = emptyList()
    private var primaryOrder: Map<String, Int> = emptyMap()
    /** The active sublist name for the current playlist (null = full playlist). */
    @Volatile private var sublistName: String? = null
    /** The active sublist filter (null = none). Set before show() is called. */
    @Volatile private var sublistFilter: SublistFilter? = null
    /** Number of items that passed the sublist filter on the last load (for UI preview). */
    var sublistMatchCount by mutableStateOf(0)
        private set
    /** Total number of items parsed before sublist filtering (for UI preview). */
    var sublistTotalCount by mutableStateOf(0)
        private set

    /** Unique group/category values found in the loaded playlist at [url], sorted alphabetically. Used by the regular UI to show what is currently loaded. */
    suspend fun playlistGroups(url: String): List<String> = withContext(Dispatchers.IO) {
        if (primaryUrl != url) emptyList() else primaryItems.map { it.group }.distinct().sorted()
    }

    /** Unique language values found in the loaded playlist at [url], sorted alphabetically. Used by the regular UI to show what is currently loaded. */
    suspend fun playlistLanguages(url: String): List<String> = withContext(Dispatchers.IO) {
        if (primaryUrl != url) emptyList() else primaryItems.map { it.lang }.distinct().sorted()
    }

    /** All group/category values in the cached playlist at [url], before any sublist filtering, per kind (LIVE/MOVIE/SERIES). */
    suspend fun cachedPlaylistGroupsByKind(c: Context, url: String): Map<Kind, List<String>> = withContext(Dispatchers.IO) {
        readCache(c, url, emptySet())?.let {
            it.items.groupBy { it.kind }.mapValues { it.value.map { it.group }.distinct().sorted() }
        } ?: emptyMap()
    }

    /** All language values in the cached playlist at [url], before any sublist filtering, per kind (LIVE/MOVIE/SERIES). */
    suspend fun cachedPlaylistLanguagesByKind(c: Context, url: String): Map<Kind, List<String>> = withContext(Dispatchers.IO) {
        readCache(c, url, emptySet())?.let {
            it.items.groupBy { it.kind }.mapValues { it.value.map { it.lang }.distinct().sorted() }
        } ?: emptyMap()
    }

    /** Export all categories and languages from the cached playlist at [url] as CSV text.
     * Returns null if no cache exists for this playlist. No network access. */
    suspend fun exportPlaylistInfo(c: Context, url: String): String? = withContext(Dispatchers.IO) {
        val groups = cachedPlaylistGroupsByKind(c, url)
        val langs = cachedPlaylistLanguagesByKind(c, url)
        if (groups.isEmpty() && langs.isEmpty()) null else {
            val sb = StringBuilder()
            sb.append("Kind,Type,Value\n")
            for (kind in Kind.entries) {
                for (g in groups.getOrDefault(kind, emptyList()).sorted()) {
                    sb.append("${kind.displayName},Category,$g\n")
                }
            }
            for (kind in Kind.entries) {
                for (l in langs.getOrDefault(kind, emptyList()).sorted()) {
                    sb.append("${kind.displayName},Language,$l\n")
                }
            }
            sb.toString()
        }
    }
    private var epgSig = 0L
    @Volatile private var inited = false
    private var initing = false   // T-047: set while [ensure] runs, so a call from inside it returns (as before)
    private val initLock = Any()
    private val loadLock = Mutex()

    /**
     * The parsed list of a playlist as last shown, kept softly (the system may drop it under memory pressure), so switching
     * back to it needs no disk read and no parsing. Valid only while [gen] of the playlist is unchanged (any write of its
     * stored cache bumps it) and the language/content selection is the same.
     */
    private class Snap(
        val items: List<Item>, val epg: List<String>, val order: Map<String, Int>,
        val counts: Map<String, Int>, val bytes: Map<String, Long>, val hidden: Set<String>, val gen: Int
    )
    private val stash = java.util.concurrent.ConcurrentHashMap<String, java.lang.ref.SoftReference<Snap>>()
    private val gens = java.util.concurrent.ConcurrentHashMap<String, Int>()
    private fun bumpGen(url: String) { gens.merge(url, 1, Int::plus); stash.remove(url) }
    @Volatile private var job: Job? = null
    /** Last parse was too big: show the language selection on the next load with a UI. */
    @Volatile private var askNext = false
    @Volatile private var call: Call? = null

    private val client = Net.withTimeouts(20, 120).newBuilder().addInterceptor(TlsFallback.interceptor()).build()

    private val XTREAM = Regex("""^(https?://[^/]+)/get\.php\?.*?username=([^&]+).*?password=([^&]+)""")
    // Common player identifier: some providers throttle unknown identifiers heavily (browser/VLC then load much faster)
    private const val UA = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    /** A fully downloaded but not yet parsed list is reused for this long. */
    private const val RAW_MAX_AGE = 12 * 3_600_000L
    /** Free space that should remain at least after the download. */
    private const val SPACE_RESERVE = 64L * 1024 * 1024

    const val MSG_CANCEL = "Laden abgebrochen. Deine Angaben bleiben gespeichert."
    const val MSG_TOO_BIG = "Die gewählten Inhalte sind zu groß für den Speicher dieses Geräts. Bitte weniger Sprachen wählen."
    const val MSG_INCOMPLETE = "Die Playlist kam nur unvollständig an. Bitte später erneut versuchen."

    /** Question for very large playlists: which languages should be loaded? */
    class LangAsk(val counts: Map<String, Int>, val bytes: Map<String, Long>, val preselect: Set<String>, val budget: Long) {
        internal val answer = CompletableDeferred<Set<String>?>()
        val titles: Int get() = counts.values.sum()
        fun titlesOf(shown: Set<String>): Int = counts.entries.sumOf { if (it.key in shown) it.value else 0 }
        fun estimate(shown: Set<String>): Long = LoadLogic.estimate(bytes, counts.keys - shown)
        fun fits(shown: Set<String>): Boolean = estimate(shown) <= budget
        /** Chosen (shown) languages, null = cancel. */
        fun choose(shown: Set<String>?) { answer.complete(shown) }
        /** Skip: load the whole playlist. */
        fun chooseAll() { answer.complete(counts.keys) }
    }

    /** Show the language selection anyway while loading (hint after 15 minutes). */
    fun requestLanguageChoice() { askRequested = true }

    /** Thrown while parsing when the user requested the language selection in the meantime. */
    private class AskAgain : RuntimeException("Sprachauswahl")

    /** Language of the device (not of the app), e.g. "de". */
    private fun systemLang(): String = try {
        android.content.res.Resources.getSystem().configuration.locales[0].language
    } catch (e: Exception) { java.util.Locale.getDefault().language }

    /** How much memory the playlist may use at most. */
    fun budget(): Long = LoadLogic.budget(Runtime.getRuntime().maxMemory())

    /** Does this language selection fit in memory? (Estimate from the last parse; unknown = yes.) */
    fun fits(hidden: Set<String>): Boolean = LoadLogic.fits(langBytes, hidden, budget())

    /** Cancel the running load (back twice in the loading screen). The data stays saved. */
    fun cancel() {
        error = MSG_CANCEL
        askRequested = false
        langAsk?.choose(null)
        runCatching { call?.cancel() }
        job?.cancel()
    }

    private fun cancelled() = job?.isActive == false

    private fun prefs(c: Context) = c.getSharedPreferences("ipvio", Context.MODE_PRIVATE)

    private fun ensure(c: Context) {
        if (inited) return
        // T-047: also called by the read-ahead thread; other threads wait until the lists are filled
        synchronized(initLock) {
            if (inited || initing) return
            initing = true
            try { ensureOnce(c) } finally { inited = true }
        }
    }

    private fun ensureOnce(c: Context) {
        cleanupStale(c)
        val p = prefs(c)
        // Prefers the encrypted version; the old plain-text version is adopted and replaced on first start.
        val enc = p.getString("lists_enc", null)
        val dec = if (enc != null) Secrets.decrypt(enc) else null
        // #112: the saved list exists but cannot be decrypted (e.g. the key was lost). It is kept, see persist()
        if (enc != null && dec == null) Diag.report("The saved playlist list could not be decrypted")
        val json = dec ?: p.getString("lists", "[]")
        try {
            val a = JSONArray(json)
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                lists.add(PL(o.getString("name"), o.getString("url")))
            }
        } catch (e: Exception) {
        }
        val old = p.getString("url", null)
        if (lists.isEmpty() && old != null) lists.add(PL(hostOf(old), old))
        active = p.getInt("active", 0).coerceIn(0, maxOf(lists.size - 1, 0))
        multi = MultiLogic.decodeKeys(p.getString("multi", null))
        initHealth(c)
        syncFailover(c)
        if (enc == null && lists.isNotEmpty()) persist(c)   // taking over the old plaintext list (after "active" is set)
    }

    /** Delete leftovers of aborted downloads older than two days (a raw file can be over 200 MB). */
    private fun cleanupStale(c: Context) {
        runCatching {
            val now = System.currentTimeMillis()
            c.filesDir.listFiles { f ->
                f.name.startsWith("pl_") && (f.name.endsWith(".m3u") || f.name.endsWith(".part") || f.name.endsWith(".info") || f.name.endsWith(".tmp"))
            }?.filter { now - it.lastModified() > 2 * 86_400_000L }?.forEach { it.delete() }
        }
    }

    private fun persist(c: Context) {
        val a = JSONArray()
        for (l in lists) a.put(JSONObject().put("name", l.name).put("url", l.url))
        val e = prefs(c).edit().putInt("active", active).putString("multi", MultiLogic.encodeKeys(multi)).remove("url")
        val token = Secrets.encrypt(a.toString())
        // #112: never replace an unreadable saved list without keeping it (the link and login may still be recoverable)
        val prev = prefs(c).getString("lists_enc", null)
        if (prev != null && Secrets.decrypt(prev) == null && !prefs(c).contains("lists_enc_unreadable")) e.putString("lists_enc_unreadable", prev)
        if (token != null) e.putString("lists_enc", token).remove("lists")
        else e.putString("lists", a.toString()).remove("lists_enc")
        e.apply()
    }

    // ------------------------------------------------------------------ remembered load job

    /** The playlist currently being added (or not finished last time), otherwise null. */
    fun pending(c: Context): LoadLogic.Pending? {
        val p = prefs(c)
        val enc = p.getString("pending_enc", null)
        return LoadLogic.Pending.decode((if (enc != null) Secrets.decrypt(enc) else null) ?: p.getString("pending", null))
    }

    private fun writePending(c: Context, v: LoadLogic.Pending?) {
        val e = prefs(c).edit()
        if (v == null) e.remove("pending_enc").remove("pending")
        else {
            val token = Secrets.encrypt(v.encode())
            if (token != null) e.putString("pending_enc", token).remove("pending") else e.putString("pending", v.encode()).remove("pending_enc")
        }
        e.commit()   // straight to disk: a crash right afterwards must not lose anything
    }

    fun clearPending(c: Context) {
        writePending(c, null)
        // A finished or cancelled add no longer needs its own failover group (see [adding])
        if (adding != null) { adding = null; if (inited) syncFailover(c) }
    }

    private fun failPending(c: Context, msg: String) {
        runCatching { pending(c)?.let { writePending(c, it.copy(state = LoadLogic.FAILED, msg = msg)) } }
    }

    /** Playlists for transfer to another device (plain text – only via the code-protected route). */
    fun exportLists(c: Context): JSONArray {
        ensure(c)
        val a = JSONArray()
        for (l in lists) {
            val o = JSONObject().put("name", l.name).put("url", l.url)
            backups(c, l.url).takeIf { it.isNotEmpty() }?.let { o.put("backups", JSONArray(it)) }
            a.put(o)
        }
        return a
    }

    /** Add transferred playlists (existing addresses stay as they are). Without an active list the first becomes active. */
    fun importLists(c: Context, a: JSONArray) {
        ensure(c)
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            val u = PlaylistLogic.normalizeUrl(o.optString("url")) ?: continue
            if (u.startsWith("file://")) continue   // files exist only on the old device
            if (lists.none { it.url == u }) {
                lists.add(PL(o.optString("name").ifBlank { hostOf(u) }, u))
                o.optJSONArray("backups")?.let { b -> setBackups(c, u, (0 until b.length()).map { b.optString(it) }) }
            }
        }
        active = active.coerceIn(0, maxOf(lists.size - 1, 0))
        persist(c)
        syncFailover(c)
        // Write to disk immediately (the app restarts right away)
        prefs(c).edit().commit()
    }

    fun hostOf(url: String): String {
        if (url.startsWith("file://")) return url.substringAfterLast('/').substringBeforeLast('.').ifBlank { "Playlist" }
        return try {
            val u = StalkerLogic.parse(url)?.portal ?: url
            android.net.Uri.parse(u).host ?: "Playlist"
        } catch (e: Exception) { "Playlist" }
    }

    fun savedUrl(c: Context): String? { ensure(c); return lists.getOrNull(active)?.url }

    /** Resolve the active sublist filter for [url] (null = load the full playlist). */
    fun activeSublist(c: Context, url: String): SublistFilter? {
        ensure(c)
        val name = SublistStore.active(c, key(url))
        if (name == null) return null
        return SublistStore.allFor(c, key(url)).firstOrNull { it.name == name }?.filter
    }

    /** How many entries of [url] match the active sublist filter: (matching, total). Computes by parsing once without storing. */
    suspend fun previewCount(c: Context, url: String): Pair<Int, Int> {
        ensure(c)
        val u = PlaylistLogic.normalizeUrl(url) ?: return Pair(0, 0)
        val f = activeSublist(c, u)
        if (f == null || !f.enabled) return Pair(0, 0)
        return previewCount(c, u, f)
    }

    /** How many entries of [url] match [f]: (matching, total). Uses already-loaded items when possible, otherwise reads from cache (fast). */
    suspend fun previewCount(c: Context, url: String, f: SublistFilter): Pair<Int, Int> {
        ensure(c)
        if (!f.enabled) return Pair(0, 0)
        val u = PlaylistLogic.normalizeUrl(url) ?: return Pair(0, 0)
        return withContext(Dispatchers.IO) {
            try {
                val items: List<Item> = if (primaryUrl == u) {
                    primaryItems
                } else {
                    val cached = readCache(c, u, emptySet())
                    if (cached != null) cached.items else {
                        val (src, cs) = downloadWithRetry(c, u)
                        val b = build(c, u, src, false, cs, ask = false, null)
                        b.parsed.items
                    }
                }
                val total = items.size
                val matching = items.count { f.matches(it) }
                Pair(matching, total)
            } catch (e: Exception) { Pair(0, 0) }
        }
    }

    /** Xtream address (user and password URL-encoded) or null if a value is missing or invalid. */
    fun xtreamUrl(host: String, user: String, pass: String): String? = PlaylistLogic.xtreamUrl(host, user, pass)

    // ------------------------------------------------------------------ Backup servers (Xtream)

    private const val BACKUP_PREFIX = "xt_backup_"
    private fun backupKey(u: String) = BACKUP_PREFIX + key(u)

    /** Saved backup servers of an Xtream playlist (without the main server), otherwise empty. */
    fun backups(c: Context, url: String): List<String> {
        val s = prefs(c).getString(backupKey(url), null) ?: return emptyList()
        return runCatching { JSONArray(s).let { a -> (1 until a.length()).map { a.getString(it) } } }.getOrDefault(emptyList())
    }

    /**
     * Save the backup servers of an Xtream playlist (empty entries are dropped, any number). Stored as
     * [main server, backup…] so failover also applies to a playlist that is still being added ([adding]).
     */
    fun setBackups(c: Context, url: String, backups: List<String>) {
        ensure(c)
        val base = XtreamLogic.fromUrl(url)?.base ?: return
        adding = url
        val g = HostFailoverLogic.group(base, backups)
        val e = prefs(c).edit()
        if (g.size > 1) e.putString(backupKey(url), JSONArray(g).toString()) else e.remove(backupKey(url))
        e.apply()
        syncFailover(c)
    }

    private const val HEALTH_KEY = "xt_host_health"

    /** Load the saved server state and save future changes (once at start). */
    private fun initHealth(c: Context) {
        val app = c.applicationContext
        runCatching {
            val o = JSONObject(prefs(app).getString(HEALTH_KEY, null) ?: "{}")
            HostFailover.loadHealth(o.keys().asSequence().associateWith { k ->
                val a = o.getJSONArray(k); HostFailover.Health(a.getInt(0) == 1, a.getLong(1))
            })
        }
        HostFailover.onHealthChange = { m ->
            val o = JSONObject()
            for ((k, h) in m) o.put(k, JSONArray().put(if (h.ok) 1 else 0).put(h.at))
            prefs(app).edit().putString(HEALTH_KEY, o.toString()).apply()
        }
    }

    /** Servers of this playlist (main and backups) that were not reachable last time. */
    fun downServers(c: Context, url: String): List<String> {
        HostFailover.healthVersion   // recompose when the state changes
        val base = XtreamLogic.fromUrl(url)?.base ?: return emptyList()
        return (listOf(base) + backups(c, url)).filter { HostFailover.health(it)?.ok == false }
    }

    /** Playlist URL passed to [setBackups] that may not be in [lists] yet (new playlist while it loads). */
    @Volatile private var adding: String? = null

    /**
     * Hand the groups to [HostFailover]: one per Xtream playlist in [lists] (plus [adding]), also for playlists without
     * backups, so their requests never go to another playlist's servers. Saved groups of playlists that no longer
     * exist (e.g. a failed add, or a changed password) are ignored.
     */
    private fun syncFailover(c: Context) {
        val p = prefs(c)
        val urls = lists.map { it.url } + listOfNotNull(adding?.takeIf { a -> lists.none { it.url == a } })
        HostFailover.setGroups(urls.mapNotNull { u ->
            val base = XtreamLogic.fromUrl(u)?.base ?: return@mapNotNull null
            val saved = p.getString(backupKey(u), null)?.let { v ->
                runCatching { JSONArray(v).let { a -> (0 until a.length()).map { a.getString(it) } } }.getOrNull()
            }
            saved?.takeIf { it.isNotEmpty() } ?: listOf(base)
        })
    }

    fun activate(c: Context, i: Int) { ensure(c); if (i in lists.indices) { active = i; persist(c) } }

    fun rename(c: Context, i: Int, name: String) {
        ensure(c)
        if (i in lists.indices && name.isNotBlank()) { lists[i] = PL(name.trim(), lists[i].url); persist(c) }
    }

    fun remove(c: Context, i: Int) {
        ensure(c)
        if (i !in lists.indices) return
        deleteFiles(c, lists[i].url)
        PlaylistRefresh.clear(c, lists[i].url)
        prefs(c).edit().remove(backupKey(lists[i].url)).apply()
        if (adding == lists[i].url) adding = null
        extras.remove(lists[i].url)
        multi = multi - key(lists[i].url)
        lists.removeAt(i)
        active = active.coerceIn(0, maxOf(lists.size - 1, 0))
        persist(c)
        syncFailover(c)
        if (lists.isEmpty()) setItems(emptyList())
    }

    private fun fetchOrder(url: String): Map<String, Int> {
        val m = XTREAM.find(url) ?: return emptyMap()
        val base = "${m.groupValues[1]}/player_api.php?username=${m.groupValues[2]}&password=${m.groupValues[3]}&action="
        val out = LinkedHashMap<String, Int>()
        for (act in listOf("get_vod_categories", "get_series_categories", "get_live_categories")) {
            try {
                val body = client.newCall(Request.Builder().url(base + act)
                    .header("User-Agent", UA)
                    .header("Accept", "*/*")
                    .header("Accept-Language", "en-US,en;q=0.9,de;q=0.8")
                    .header("Connection", "keep-alive")
                    .build()).execute().use { r ->
                    if (r.isSuccessful) r.body?.string() else null
                } ?: continue
                val a = JSONArray(body)
                for (i in 0 until a.length()) {
                    val n = a.getJSONObject(i).optString("category_name")
                    if (n.isNotBlank() && !out.containsKey(n)) out[n] = i
                }
            } catch (e: Exception) {
            }
        }
        return out
    }

    // ------------------------------------------------------------------ files per playlist

    internal fun key(url: String) = url.trim().hashCode()
    private fun cacheFile(c: Context, url: String) = File(c.filesDir, "pl_" + key(url) + ".bin")

    /** #90: this playlist address sends the app's own user-agent (the user opted in when adding it). */
    fun nativeUa(c: Context, url: String): Boolean = prefs(c).getBoolean(PlaylistUaLogic.prefKey(key(url)), false)
    fun setNativeUa(c: Context, url: String, on: Boolean) { prefs(c).edit().putBoolean(PlaylistUaLogic.prefKey(key(url)), on).apply() }
    private fun uaFor(c: Context, url: String): String = PlaylistUaLogic.pick(nativeUa(c, url), BuildConfig.VERSION_NAME)

    /** #111: number of live channels, movies and series of a loaded playlist (stored at load time). */
    fun saveTypeCounts(c: Context, url: String, items: List<Item>) {
        val t = PlaylistCountLogic.TypeCounts(
            live = items.count { it.kind == Kind.LIVE },
            movies = items.count { it.kind == Kind.MOVIE },
            series = items.count { it.kind == Kind.SERIES },
        )
        prefs(c).edit().putString("cnt_" + key(url), PlaylistCountLogic.encode(t)).apply()
    }

    fun typeCounts(c: Context, url: String): PlaylistCountLogic.TypeCounts? =
        PlaylistCountLogic.decode(prefs(c).getString("cnt_" + key(url), null))
    private fun gzFile(c: Context, url: String) = File(c.filesDir, "pl_" + key(url) + ".m3u.gz")
    private fun rawFile(c: Context, url: String) = File(c.filesDir, "pl_" + key(url) + ".m3u")
    /** Stalker portal: categories of which only the newest titles were fetched while loading (see [StalkerMore]). */
    fun stalkerMoreFile(c: Context, url: String) = File(c.filesDir, "stalker_more_" + key(url) + ".tsv")
    private fun partFile(c: Context, url: String) = File(c.filesDir, "pl_" + key(url) + ".part")
    private fun infoFile(c: Context, url: String) = File(c.filesDir, "pl_" + key(url) + ".info")
    private fun tmp(f: File) = File(f.path + ".tmp")

    private fun deleteFiles(c: Context, url: String) {
        bumpGen(url)
        if (url.startsWith("file://")) runCatching { File(java.net.URI(url)).takeIf { it.path.contains("/imports/") }?.delete() }
        val main = listOf(cacheFile(c, url), gzFile(c, url), rawFile(c, url), partFile(c, url), infoFile(c, url), Catchup.file(c, url), stalkerMoreFile(c, url))
        (main + main.map { tmp(it) }).forEach { runCatching { it.delete() } }
    }

    private fun writeCache(
        c: Context, url: String, list: List<Item>, epg: List<String>, order: Map<String, Int>,
        counts: Map<String, Int>, bytes: Map<String, Long>, hidden: Set<String>
    ) {
        bumpGen(url)
        try {
            val f = cacheFile(c, url)
            val t = tmp(f)
            BufferedOutputStream(FileOutputStream(t), 1 shl 16).use { PlaylistCacheIO.write(it, url, list, epg, order, counts, bytes, hidden) }
            f.delete()
            t.renameTo(f)
        } catch (e: Exception) {
        }
    }

    private fun readHeader(c: Context, url: String): PlaylistCacheIO.Cached? {
        val f = cacheFile(c, url)
        if (!f.exists()) return null
        return try {
            BufferedInputStream(FileInputStream(f), 1 shl 12).use { PlaylistCacheIO.readHeader(it, url) }
        } catch (e: Exception) {
            runCatching { f.delete() }
            null
        }
    }

    private fun readCache(c: Context, url: String, hidden: Set<String>): PlaylistCacheIO.Cached? {
        val f = cacheFile(c, url)
        if (!f.exists()) return null
        return try {
            BufferedInputStream(FileInputStream(f), 1 shl 16).use { PlaylistCacheIO.read(it, url, hidden, guard(askable = false)) }
        } catch (e: ParseStop) {
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { f.delete() }
            null
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    /** T-047: stored list read ahead on a background thread at the start (result for one address, language selection and file state). */
    private class Pre(val url: String, val hidden: Set<String>, val stamp: Long, val task: java.util.concurrent.FutureTask<PlaylistCacheIO.Cached?>)
    @Volatile private var pre: Pre? = null

    private fun stampOf(f: File): Long = f.length() * 31 + f.lastModified()

    /**
     * Starts reading the stored list of the active playlist while the first screen is built (call from a background thread).
     * Same conditions as [fromLocal]; if anything differs there, the result is simply not used. Nothing is shown from here.
     */
    fun preread(c: Context) {
        try {
            val u = savedUrl(c)?.let { PlaylistLogic.normalizeUrl(it) } ?: return
            if (pending(c)?.autoResume == true) return   // the add screen continues loading instead
            val hidden = PlaylistMode.withMode(c, u, LangFilter.hidden)
            val head = readHeader(c, u) ?: return
            if (head.hidden != hidden || head.counts.isEmpty()) return
            val f = cacheFile(c, u)
            val task = java.util.concurrent.FutureTask<PlaylistCacheIO.Cached?> { readCache(c, u, hidden) }
            pre = Pre(u, hidden, stampOf(f), task)
            task.run()
        } catch (e: Throwable) {
            pre = null
        }
    }

    /** The read-ahead result if it still fits (waits if it is still running), otherwise null. Used once. */
    private fun takePreread(c: Context, u: String, hidden: Set<String>): PlaylistCacheIO.Cached? {
        val p = pre ?: return null
        pre = null
        if (p.url != u || p.hidden != hidden || p.stamp != stampOf(cacheFile(c, u))) return null
        return try { p.task.get() } catch (e: Exception) { null }
    }

    private fun sig(l: List<Item>): Long {
        var s = l.size.toLong()
        for (i in l) s = s * 31 + i.id.hashCode()
        return s
    }

    /**
     * Guard for long reads (every 2,000 entries): abort by the user, and stop before memory runs out.
     * A controlled abort with a notice is better than a crash in the middle of parsing.
     */
    private fun guard(askable: Boolean = true): () -> Unit {
        val j = job
        return {
            if (j != null && !j.isActive) throw CancellationException("abgebrochen")
            if (askable && askRequested) throw AskAgain()
            if (Mem.heapTight(0.85)) {
                System.gc()
                if (Mem.heapTight(0.85)) throw ParseStop("Speicher")
            }
        }
    }

    // ------------------------------------------------------------------ Download

    /** Downloads the list into a file, with up to two retries on connection loss (resumed if the server allows it). */
    private suspend fun downloadWithRetry(c: Context, u: String): Pair<File, Charset> {
        var last: IOException? = null
        for (attempt in 0 until 3) {
            try {
                return download(c, u)
            } catch (e: IOException) {
                if (cancelled()) throw CancellationException("abgebrochen")
                if (!PlaylistLogic.retryable(e)) throw e
                last = e
                if (attempt < 2) { phase = "Verbindung unterbrochen – neuer Versuch …"; delay(3000) }
            }
        }
        if (last is IncompleteDownload) throw PlaylistError(MSG_INCOMPLETE)
        throw last ?: IOException("Download")
    }

    /** The list arrived only in part. Never parsed, never stored as cache; retried like a lost connection. */
    private class IncompleteDownload : IOException("Download unvollständig")

    /** true if the size of the last list download was unknown (then [download] checks it for completeness). */
    @Volatile private var lastUnsized = false

    private fun charsetOf(name: String?): Charset = try { Charset.forName(name ?: "UTF-8") } catch (e: Exception) { Charsets.UTF_8 }

    /**
     * Downloads the list into a file – not into working memory. Returns the finished file and its charset.
     * If a fully downloaded but unparsed file exists (crash/power loss while parsing), it is used.
     */
    private fun download(c: Context, u: String): Pair<File, Charset> {
        // Uploaded file (phone page or file picker): already on the device
        if (u.startsWith("file://")) {
            val f = File(java.net.URI(u))
            if (!f.exists() || f.length() == 0L) throw PlaylistError("Die Playlist-Datei ist nicht mehr da. Bitte erneut hochladen.")
            phase = "Playlist wird gelesen …"
            return f to Charsets.UTF_8
        }
        StalkerLogic.parse(u)?.let { return downloadStalker(c, u, it) }
        val acct = XtreamLogic.fromUrl(u) ?: return downloadList(c, u)
        // If get.php has already failed once for this login, use the API right away
        if (useApi(c, u)) return apiOrList(c, u, acct)
        val got = try {
            downloadList(c, u)
        } catch (e: PlaylistError) {
            return apiInstead(c, u, acct, e)
        } catch (e: IncompleteDownload) {
            // get.php was cut off: the API delivers JSON, where a cut always fails instead of looking like a short list
            return apiInstead(c, u, acct, PlaylistError(MSG_INCOMPLETE))
        }
        val head = runCatching { got.first.inputStream().use { s -> val b = ByteArray(512); val k = s.read(b); if (k > 0) String(b, 0, k, got.second) else "" } }.getOrDefault("")
        if (XtreamLogic.looksLikeM3u(head)) {
            if (!lastUnsized || !missesCategories(acct, got.first, got.second)) return got
            // Most of the provider's live categories are missing: the list arrived only in part
            got.first.delete(); infoFile(c, u).delete()
            return apiInstead(c, u, acct, PlaylistError(MSG_INCOMPLETE))
        }
        // 200, but no list (error page, empty response): try the API
        got.first.delete(); infoFile(c, u).delete()
        return apiInstead(c, u, acct, PlaylistError("Der Server hat keine gültige Playlist geliefert."))
    }

    private fun apiKey(u: String) = "xt_api_" + key(u)
    private fun useApi(c: Context, u: String) = prefs(c).getBoolean(apiKey(u), false)

    /**
     * The API is the known-good path for this login. If it delivers an incomplete list (e.g. no films although the
     * provider has film categories), the marker is dropped and the complete M3U (get.php) is tried once.
     */
    private fun apiOrList(c: Context, u: String, acct: XtreamLogic.Acct): Pair<File, Charset> = try {
        downloadApi(c, u, acct)
    } catch (e: PlaylistError) {
        if (!e.incomplete) throw e
        prefs(c).edit().remove(apiKey(u)).apply()
        downloadList(c, u)
    }

    /** get.php failed: load via player_api.php and remember that for this login. If that fails too, the clearer error counts. */
    private fun apiInstead(c: Context, u: String, acct: XtreamLogic.Acct, first: PlaylistError): Pair<File, Charset> {
        if (cancelled()) throw CancellationException("abgebrochen")
        return try {
            val r = downloadApi(c, u, acct)
            prefs(c).edit().putBoolean(apiKey(u), true).apply()
            r
        } catch (e: CancellationException) {
            throw e
        } catch (e: PlaylistError) {
            throw if (e.message?.startsWith("Zugang abgelehnt") == true) e else first
        } catch (e: IOException) {
            throw first
        } catch (e: Exception) {
            throw first
        }
    }

    /** API access for the episodes of a series that are only loaded when it is opened ([LazySeries]). */
    /** #90: the user-agent of the active playlist (the series belong to it), VLC if unknown. */
    internal fun xtreamApi(c: Context? = null): XtreamApi {
        val u = lists.getOrNull(active)?.url
        return XtreamApi(client, if (c != null && u != null) uaFor(c, u) else UA)
    }

    /** Build the list via the provider's API (see [XtreamApi]) and store it like a finished download. */
    private fun downloadApi(c: Context, u: String, acct: XtreamLogic.Acct): Pair<File, Charset> {
        val raw = rawFile(c, u)
        val info = infoFile(c, u)
        val part = partFile(c, u)
        val pi = LoadLogic.PartInfo.decode(runCatching { info.readText() }.getOrNull())?.takeIf { it.urlHash == key(u) }
        if (pi != null && raw.exists() && raw.length() > 0 && System.currentTimeMillis() - raw.lastModified() < RAW_MAX_AGE) {
            return raw to Charsets.UTF_8
        }
        raw.delete(); part.delete()
        bytesRead = 0L; totalBytes = -1L; downloadDone = false
        val api = XtreamApi(client, uaFor(c, u))
        api.onPhase = { phase = it }
        api.check = { if (cancelled()) throw CancellationException("abgebrochen") }
        // Shown bytes = written list + movies/series still in temporary files (fetched in parallel). Never goes backwards:
        // when a temporary file is appended to the list, its part moves from one counter to the other.
        val lock = Any()
        var listN = 0L
        var tempN = 0L
        fun show() { val v = listN + tempN; if (v > bytesRead) bytesRead = v }
        api.onTempBytes = { k -> synchronized(lock) { tempN += k; show() } }
        val counter = CountingOut(FileOutputStream(part)) { k -> synchronized(lock) { listN = k; show() } }
        try {
            val n = OutputStreamWriter(BufferedOutputStream(counter, 1 shl 16), Charsets.UTF_8).use { w -> api.write(acct, w) }
            PlaylistDiag.log("download(api)", "entries=$n bytes=${part.length()}")
            if (n == 0) throw PlaylistError("Keine Sender in der Playlist gefunden.")
        } catch (e: Throwable) {
            part.delete()
            throw e
        }
        info.writeText(LoadLogic.PartInfo(key(u), null, null, "UTF-8").encode())
        if (!part.renameTo(raw)) throw IOException("Datei konnte nicht umbenannt werden")
        return raw to Charsets.UTF_8
    }

    /** Stalker portal: build the list via the portal's API (see [StalkerApi]). */
    private fun downloadStalker(c: Context, u: String, src: StalkerLogic.Source): Pair<File, Charset> {
        val raw = rawFile(c, u)
        val info = infoFile(c, u)
        val part = partFile(c, u)
        val pi = LoadLogic.PartInfo.decode(runCatching { info.readText() }.getOrNull())?.takeIf { it.urlHash == key(u) }
        if (pi != null && raw.exists() && raw.length() > 0 && System.currentTimeMillis() - raw.lastModified() < RAW_MAX_AGE) return raw to Charsets.UTF_8
        raw.delete(); part.delete()
        bytesRead = 0L; totalBytes = -1L; downloadDone = false
        val counter = CountingOut(FileOutputStream(part)) { bytesRead = it }
        var more: List<StalkerLogic.CatRef> = emptyList()
        try {
            OutputStreamWriter(BufferedOutputStream(counter, 1 shl 16), Charsets.UTF_8).use { w ->
                StalkerApi.write(src, w, { phase = it }, { if (cancelled()) throw CancellationException("abgebrochen") }) { more = it }
            }
        } catch (e: Throwable) {
            part.delete()
            throw e
        }
        info.writeText(LoadLogic.PartInfo(key(u), null, null, "UTF-8").encode())
        if (!part.renameTo(raw)) throw IOException("Datei konnte nicht umbenannt werden")
        // Only after a successful load: which categories are loaded further on opening
        runCatching { stalkerMoreFile(c, u).writeText(StalkerLogic.encodeCats(more)) }
        StalkerMore.reset()
        return raw to Charsets.UTF_8
    }

    private fun downloadList(c: Context, u: String): Pair<File, Charset> {
        val raw = rawFile(c, u)
        val info = infoFile(c, u)
        val part = partFile(c, u)
        val pi = LoadLogic.PartInfo.decode(runCatching { info.readText() }.getOrNull())?.takeIf { it.urlHash == key(u) }
        if (pi != null && raw.exists() && raw.length() > 0 && System.currentTimeMillis() - raw.lastModified() < RAW_MAX_AGE) {
            lastUnsized = false   // was checked when it was downloaded
            return raw to charsetOf(pi.charset)
        }
        raw.delete()
        lastUnsized = false
        var offset = if (pi != null && part.exists()) part.length() else 0L
        // After a power loss the last written bytes may be missing or corrupt: go back a bit
        if (offset > 0) { offset = maxOf(0L, offset - 65_536); RandomAccessFile(part, "rw").use { it.setLength(offset) } }
        val extra = if (pi != null) LoadLogic.resumeHeaders(offset, pi.etag, pi.lastModified) else emptyMap()
        if (extra.isEmpty()) offset = 0L
        val rb = Request.Builder().url(u)
            .header("User-Agent", uaFor(c, u))
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.9,de;q=0.8")
            .header("Connection", "keep-alive")
        extra.forEach { (k, v) -> rb.header(k, v) }
        phase = if (offset > 0) "Download wird fortgesetzt …" else "Verbindung zum Server wird aufgebaut …"
        bytesRead = offset; totalBytes = -1L; downloadDone = false
        val cl = client.newCall(rb.build())
        call = cl
        try {
            cl.execute().use { r ->
                var append = false
                when (LoadLogic.resumeAction(r.code, r.header("Content-Range"), offset)) {
                    LoadLogic.APPEND -> append = true
                    LoadLogic.RESTART -> offset = 0L
                    else -> {
                        if (offset > 0) { part.delete(); info.delete() }   // resuming went wrong: start from the beginning on the next attempt
                        throw PlaylistError(if (r.code == 206 || r.code == 416) "Download konnte nicht fortgesetzt werden. Bitte erneut versuchen." else PlaylistLogic.httpError(r.code))
                    }
                }
                val body = r.body ?: throw PlaylistError("Der Server hat keine Daten geliefert.")
                val len = body.contentLength()
                val total = when {
                    append && len >= 0 -> offset + len
                    append -> LoadLogic.rangeTotal(r.header("Content-Range"))
                    else -> len
                }
                totalBytes = total
                if (len > 0 && c.filesDir.usableSpace < len + SPACE_RESERVE) throw PlaylistError(PlaylistLogic.NO_SPACE)
                val cs = body.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
                info.writeText(LoadLogic.PartInfo(key(u), r.header("ETag"), r.header("Last-Modified"), cs.name()).encode())
                phase = if (append) "Download wird fortgesetzt …" else "Playlist wird heruntergeladen …"
                FileOutputStream(part, append).use { fo ->
                    val out = BufferedOutputStream(fo, 1 shl 16)
                    val buf = ByteArray(1 shl 16)
                    val ins = body.byteStream()
                    var n = offset
                    var last = 0L
                    while (true) {
                        val k = ins.read(buf)
                        if (k < 0) break
                        out.write(buf, 0, k)
                        n += k
                        if (n - last >= 262_144) {
                            last = n; bytesRead = n
                            if (cancelled()) throw CancellationException("abgebrochen")
                        }
                    }
                    out.flush()
                    bytesRead = n
                }
                if (total > 0 && part.length() < total) throw IncompleteDownload()
                lastUnsized = total <= 0
            }
        } finally {
            call = null
        }
        if (part.length() == 0L) throw PlaylistError("Der Server hat keine Daten geliefert.")
        // Without a size the end of a cut-off download looks normal: check the end of the file
        val cut = lastUnsized && LoadLogic.tailLooksCut(tailOf(part))
        PlaylistDiag.log("download", "bytes=${part.length()} sized=${!lastUnsized} tailCut=$cut")
        if (cut) {
            part.delete(); info.delete()   // start from the beginning on the next attempt
            throw IncompleteDownload()
        }
        raw.delete()
        if (!part.renameTo(raw)) throw IOException("Datei konnte nicht umbenannt werden")
        return raw to charsetOf(LoadLogic.PartInfo.decode(runCatching { info.readText() }.getOrNull())?.charset)
    }

    /** The last bytes of a file as text (only the line structure matters, so the charset does not). */
    private fun tailOf(f: File, max: Int = 4096): String = RandomAccessFile(f, "r").use { r ->
        val n = minOf(r.length(), max.toLong()).toInt()
        val b = ByteArray(n)
        r.seek(r.length() - n)
        r.readFully(b)
        String(b, Charsets.ISO_8859_1)
    }

    /**
     * Xtream list from get.php without a known size: compare its categories with the provider's live categories.
     * true = most of them are missing (list cut off). If the provider does not answer, the list is accepted.
     */
    private fun missesCategories(acct: XtreamLogic.Acct, f: File, cs: Charset): Boolean {
        val api = runCatching { XtreamApi(client, UA).liveCategoryNames(acct) }.getOrNull().orEmpty()
        if (api.size < 5) return false
        phase = "Playlist wird geprüft …"
        val found = HashSet<String>()
        var k = 0
        f.bufferedReader(cs).useLines { lines ->
            for (l in lines) {
                if (l.startsWith("#EXTINF")) LoadLogic.groupTitle(l)?.let { found.add(LoadLogic.normGroup(it)) }
                if (++k % 20_000 == 0 && cancelled()) throw CancellationException("abgebrochen")
            }
        }
        val missing = LoadLogic.missingCategories(api, found)
        PlaylistDiag.log("categories", "provider=${api.size} inList=${found.size} cut=$missing")
        return missing
    }

    // ------------------------------------------------------------------ Reading

    private class Built(val parsed: ParsedPlaylist, val hidden: Set<String>)

    /** Reads a file (downloaded or stored compressed) with progress; [teeTo] = store compressed at the same time. */
    private fun <T> readFile(src: File, gz: Boolean, cs: Charset, teeTo: File?, body: (Reader) -> T): T {
        totalBytes = src.length(); bytesRead = 0L; downloadDone = false
        val fin = CountingStream(FileInputStream(src)) { bytesRead = it }
        val ins: InputStream = if (gz) GZIPInputStream(BufferedInputStream(fin, 1 shl 16), 1 shl 16) else BufferedInputStream(fin, 1 shl 16)
        var reader: Reader = InputStreamReader(ins, if (gz) Charsets.UTF_8 else cs)
        var w: Writer? = null
        try {
            if (teeTo != null) {
                w = OutputStreamWriter(FastGzip(FileOutputStream(teeTo)), Charsets.UTF_8)
                reader = TeeReader(reader, w)
            }
            val r = body(reader)
            w?.close()
            w = null
            return r
        } catch (e: Throwable) {
            runCatching { w?.close() }
            teeTo?.delete()
            throw e
        } finally {
            runCatching { reader.close() }
        }
    }

    /**
     * Parses a list. If no language has been chosen yet (and not deliberately "all") and the list is large for this device,
     * it is first counted quickly and then asked ([ask]) – without a question (background) it is aborted.
     * If the user requests the language selection meanwhile, it asks again and parses again.
     */
    private suspend fun build(c: Context, url: String, src: File, gz: Boolean, cs: Charset, ask: Boolean, teeTo: File?, forceAsk: Boolean = false): Built {
        var force = forceAsk
        while (true) {
            try {
                return buildOnce(c, url, src, gz, cs, ask, teeTo, force)
            } catch (e: AskAgain) {
                if (!ask) throw PlaylistError(MSG_TOO_BIG)
                force = true
            }
        }
    }

    private suspend fun buildOnce(c: Context, url: String, src: File, gz: Boolean, cs: Charset, ask: Boolean, teeTo: File?, forceAsk: Boolean): Built {
        // Hidden languages plus the content mode of this playlist (pseudo codes, never stored in the language filter)
        var hidden = PlaylistMode.withMode(c, url, LangFilter.hidden)
        val budget = budget()
        val wantAsk = ask && (forceAsk || askRequested)
        askRequested = false
        val unchosen = LangFilter.hidden.isEmpty() && !LangFilter.allChosen
        if (wantAsk || unchosen) {
            val size = if (gz) src.length() * 12 else src.length()
            if (wantAsk || size > budget) {
                phase = "Playlist wird geprüft …"
                val scan = readFile(src, gz, cs, null) { r -> M3uParser.scanStream(r, guard()) }
                if (wantAsk || LoadLogic.estimate(scan.bytes, hidden) > budget) {
                    if (!ask) {
                        // #73 diagnostics: memory limit and language filter at the moment the list is refused (numbers only)
                        Diag.report("Memory budget ${budget / 1_000_000} MB, heap ${Runtime.getRuntime().maxMemory() / 1_000_000} MB, ${hidden.size} languages hidden")
                        throw PlaylistError(MSG_TOO_BIG)
                    }
                    val now = scan.counts.keys - hidden
                    val pre = if (LangFilter.hidden.isNotEmpty() && now.isNotEmpty()) now else LoadLogic.systemPreselect(scan.counts, systemLang())
                    val shown = askLanguages(scan.counts, scan.bytes, budget, pre) ?: throw PlaylistError(MSG_CANCEL)
                    LangFilter.set(c, scan.counts.keys - shown)
                    hidden = PlaylistMode.withMode(c, url, scan.counts.keys - shown)
                    loadStarted = System.currentTimeMillis()
                }
            }
        }
        phase = "Titel werden eingelesen …"
        val parsed = readFile(src, gz, cs, teeTo) { r -> M3uParser.parseStream(r, hidden, guard()) }
        return Built(parsed, hidden)
    }

    private suspend fun askLanguages(counts: Map<String, Int>, bytes: Map<String, Long>, budget: Long, preselect: Set<String>): Set<String>? {
        val a = LangAsk(counts, bytes, preselect, budget)
        langAsk = a
        phase = "Sprachen auswählen"
        try {
            return a.answer.await()
        } finally {
            langAsk = null
        }
    }

    private fun epgFor(u: String, fromList: List<String>): List<String> {
        val urls = ArrayList(fromList)
        XTREAM.find(u)?.let { urls.add("${it.groupValues[1]}/xmltv.php?username=${it.groupValues[2]}&password=${it.groupValues[3]}") }
        return urls.distinct()
    }

    /** The compressed copy is done: adopt it and delete the large raw file. */
    private fun commitGz(c: Context, u: String, gzTmp: File) {
        val gz = gzFile(c, u)
        if (gzTmp.exists() && gzTmp.length() > 0) { gz.delete(); gzTmp.renameTo(gz) }
        rawFile(c, u).delete(); infoFile(c, u).delete()
    }

    private fun show(
        c: Context, u: String, items: List<Item>, epg: List<String>, order: Map<String, Int>,
        counts: Map<String, Int>, bytes: Map<String, Long>, hidden: Set<String>
    ) {
        stash[u] = java.lang.ref.SoftReference(Snap(items, epg, order, counts, bytes, hidden, gens[u] ?: 0))
        langCounts = counts
        langBytes = bytes
        shownHidden = hidden
        // Apply sublist filter before storing: only matching items enter primaryItems / rawChannels.
        val filteredBySublist = if (sublistFilter?.enabled == true) {
            val f = sublistFilter!!
            sublistTotalCount = items.size
            val kept = items.filter { f.matches(it) }
            sublistMatchCount = kept.size
            kept
        } else {
            sublistTotalCount = items.size
            sublistMatchCount = items.size
            items
        }
        primaryItems = filteredBySublist; primaryUrl = u; primaryEpg = epg; primaryOrder = order
        rebuild(c)
    }

    /** Set the active sublist for the playlist at [url] before it is loaded. [null] = full playlist. */
    fun setSublistForLoad(c: Context, url: String, filter: SublistFilter?, name: String?) {
        ensure(c)
        sublistFilter = filter
        sublistName = name
        val h = key(url)
        SublistStore.setActive(c, h, name)
    }

    /** Clear any pending sublist so the next load uses the full playlist. */
    fun clearSublistForLoad() {
        sublistFilter = null
        sublistName = null
    }

    /** Further shown playlists (without the active one), in list order. */
    fun extraUrls(c: Context): List<String> {
        ensure(c)
        return lists.filterIndexed { i, pl -> i != active && key(pl.url) in multi }.map { it.url }
    }

    fun isShown(i: Int): Boolean = i == active || (i in lists.indices && key(lists[i].url) in multi)

    /** Position of playlist [i] among the shown sources (0 = active, then the others in list order), -1 = not shown. */
    fun shownIndex(i: Int): Int {
        if (i !in lists.indices) return -1
        if (i == active) return 0
        if (!isShown(i)) return -1
        return 1 + lists.indices.count { it != active && it < i && isShown(it) }
    }

    /**
     * Switch playlist (Live TV, header, settings): makes [i] the active playlist and shows the copy stored on the device;
     * the server is only asked when there is no stored copy yet. The old list is dropped first: keeping both lists in memory
     * at once ran weak devices (Fire TV Stick) out of memory.
     */
    /**
     * Switching back to a playlist shown before: its parsed list is still in memory, so it replaces the current one at once
     * (no empty screen, no loading overlay, no disk read). false = not possible, the normal switch runs.
     */
    private suspend fun switchFromMemory(c: Context, i: Int): Boolean {
        val u = PlaylistLogic.normalizeUrl(lists[i].url) ?: return false
        val snap = stash[u]?.get() ?: return false
        if (snap.gen != (gens[u] ?: 0)) return false
        if (snap.hidden != PlaylistMode.withMode(c, u, LangFilter.hidden)) return false
        if (SublistStore.active(c, key(u)) != null) return false   // a saved sublist filter is applied by the normal load
        if (!loadLock.tryLock()) return false
        try {
            clearSublistForLoad()
            activate(c, i)
            error = null; refreshError = null
            withContext(Dispatchers.IO) {
                show(c, u, snap.items, snap.epg, snap.order, snap.counts, snap.bytes, snap.hidden)
                runCatching { loadExtras(c, network = false) }.onFailure { if (it is CancellationException) throw it }
            }
            PlaylistDiag.log("switch", "from memory")
            return true
        } finally {
            loadLock.unlock()
        }
    }

    suspend fun switchTo(c: Context, i: Int, fresh: Boolean = false) {
        ensure(c)
        if (i !in lists.indices) return
        if (loadLock.isLocked) return   // a load is already running: do not interfere
        if (!fresh && switchFromMemory(c, i)) return
        activate(c, i)
        // Mark the load before the list is emptied: otherwise one frame shows "No channels yet" instead of the loading screen.
        // No suspension point between here and load(), so its lock is still free.
        loading = true
        primaryItems = emptyList(); primaryUrl = null
        merged = MultiLogic.Merged(emptyList(), emptySet(), emptyMap()); sourceNames = emptyList()
        setItems(emptyList())
        // Read the playlist stored on the device; download only when there is none yet (a switch back must not reload
        // the whole list from the server). Refreshing follows the refresh plan at app start or "Refresh" in the settings.
        load(c, lists[i].url, false, skipNetworkWhenCached = !fresh)
    }

    /**
     * Runs [switchTo] in a scope that outlives the screen that started it. The switch empties the list, the home screen then
     * replaces Live TV / the movie or series page by the loading screen, and a scope remembered there was cancelled with it:
     * the load stopped half way and "No channels yet" stayed until a restart (issues 1 and 16).
     */
    fun switchAsync(c: Context, i: Int) {
        val app = c.applicationContext
        switchScope.launch { switchTo(app, i) }
    }

    /**
     * "Reload" on a playlist of the list: makes it the main list and downloads it again from the provider
     * (the stored copy is not used). Same scope as [switchAsync], so leaving the screen does not stop it.
     */
    fun reloadAsync(c: Context, i: Int) {
        val app = c.applicationContext
        switchScope.launch { switchTo(app, i, fresh = true) }
    }

    /** Address of the playlist that [refreshOtherNow] is reloading right now (null = none). */
    var reloadingOther by mutableStateOf<String?>(null)
        private set

    /** "Reload" on a playlist that is not the main list (T-048): see [refreshOtherNow]. */
    fun reloadOtherAsync(c: Context, i: Int) {
        val app = c.applicationContext
        switchScope.launch { refreshOtherNow(app, i) }
    }

    /**
     * Downloads a playlist that is not the main list again into its stored copy, without making it the main list. If it is
     * shown next to the main list, that part is rebuilt from the new copy. A list with its own refresh plan notes the time.
     * false = nothing done (a load runs, wrong list) or the download failed.
     */
    suspend fun refreshOtherNow(c: Context, i: Int): Boolean {
        ensure(c)
        if (i !in lists.indices || i == active || reloadingOther != null || loadLock.isLocked) return false
        val u = lists[i].url
        reloadingOther = u
        try {
            val ok = refreshCacheOnly(c, u)
            if (ok) {
                if (PlaylistRefreshLogic.hasOwnPlan(PlaylistRefresh.get(c, u))) PlaylistRefresh.markFor(c, u)
                if (isShown(i)) { extras.remove(u); applyMulti(c) }
            }
            return ok
        } finally {
            reloadingOther = null
        }
    }

    /**
     * Refreshes the lists that are not the main list and whose own plan is due (T-048b); one after the other, not while
     * something is playing. Called by the background job and some seconds after the start.
     */
    suspend fun refreshOthersDue(c: Context) {
        ensure(c)
        // The main list may still be loading (it holds the load lock): wait for it, at most 3 minutes, instead of skipping this round
        var waited = 0
        while (loadLock.isLocked && waited < 36) { kotlinx.coroutines.delay(5_000); waited++ }
        val now = System.currentTimeMillis()
        val due = lists.indices.filter { i ->
            i != active && PlaylistRefreshLogic.otherDue(PlaylistRefresh.get(c, lists[i].url), PlaylistRefresh.lastFor(c, lists[i].url), now, General.zone())
        }
        for (i in due) {
            if (AppNav.playing) return
            refreshOtherNow(c, i)
        }
    }

    /** Addresses of all playlists (for the background job). */
    fun allUrls(c: Context): List<String> { ensure(c); return lists.map { it.url } }

    private val switchScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Add a playlist sent from the phone settings (issue 18): loaded and adopted like "Add playlist" on the TV. */
    fun addAsync(c: Context, url: String) {
        val app = c.applicationContext
        switchScope.launch { if (!loadLock.isLocked) load(app, url, true) }
    }

    /** Delete a playlist from the phone settings, like "Delete" in Settings > Playlists: the next one is loaded if it was the main list. */
    fun removeAsync(c: Context, i: Int) {
        val app = c.applicationContext
        switchScope.launch {
            if (i !in lists.indices || loadLock.isLocked) return@launch
            val wasActive = i == active
            remove(app, i)
            if (wasActive && lists.isNotEmpty()) switchTo(app, active)
        }
    }

    /** Show a playlist additionally (or no longer). Call [applyMulti] afterwards. */
    fun setShown(c: Context, i: Int, on: Boolean) {
        ensure(c)
        if (i !in lists.indices || i == active) return
        val k = key(lists[i].url)
        multi = if (on) multi + k else multi - k
        persist(c)
    }

    /** Content mode of a playlist was changed: rebuild the shown list (no new download). */
    suspend fun applyMode(c: Context) {
        // While a load runs, it rebuilds at its end anyway and then picks up the new mode
        if (!loadLock.tryLock()) return
        try { withContext(Dispatchers.Default) { rebuild(c) } } finally { loadLock.unlock() }
    }

    /**
     * Add titles of a category loaded on demand (Stalker portal, see [StalkerMore]) to the active playlist [u].
     * IDs already present and hidden languages are skipped. Return value: the titles that are really new.
     */
    @Volatile private var idsFor: List<Item>? = null
    private var ids = HashSet<String>()

    @Synchronized
    fun appendItems(c: Context, u: String, add: List<Item>): List<Item> {
        if (add.isEmpty() || primaryUrl != u) return emptyList()
        val base = primaryItems
        if (idsFor !== base) { ids = HashSet<String>(base.size * 2).also { s -> base.forEach { s.add(it.id) } } }
        val fresh = add.filter { it.lang !in shownHidden && ids.add(it.id) }
        // Titles loaded on demand are older than the loaded ones: insert in front (the app reads "new" from the end of the list)
        val next = if (fresh.isEmpty()) base else fresh + base
        primaryItems = next; idsFor = next
        if (fresh.isEmpty()) return fresh
        if (extras.isEmpty()) {
            merged = MultiLogic.Merged(next, emptySet(), emptyMap())
            // Extend the shown list by the new titles only instead of running all filters over the whole list
            val raw = rawChannels
            val shown = if (filteredFor === raw && filteredVer == filterVer()) filtered else shownOf(raw)
            val all = fresh + raw
            setItems(all, shownOf(fresh) + shown)
        } else rebuild(c)
        return fresh
    }

    /** Build and show the list from the active and the loaded further playlists (background thread). */
    private fun rebuild(c: Context) {
        val u = primaryUrl ?: return
        IgnoredGroups.init(c)
        val urls = extraUrls(c)
        extras.keys.retainAll(urls.toSet())
        val ex = urls.mapNotNull { url -> extras[url]?.let { url to it } }
        val names = listOf(nameOf(u)) + ex.map { nameOf(it.first) }
        // Content mode per playlist (Live only / VOD only) is applied here, before merging
        val m = MultiLogic.merge(listOf(PlaylistMode.filter(c, u, primaryItems)) + ex.map { PlaylistMode.filter(c, it.first, it.second.items) }, names)
        val vis = m.visible()
        val epg = (primaryEpg + ex.flatMap { it.second.epg }).distinct()
        twins = if (m.shadow.isEmpty()) emptyMap() else HashMap<String, Item>().also { t -> for (it in m.items) if (it.id in m.shadow) t.putIfAbsent("${it.src}|" + it.mk, it) }
        merged = m
        sourceNames = names
        epgUrls = epg
        catOrder = MultiLogic.mergeOrder(listOf(primaryOrder) + ex.map { it.second.order })
        setItems(vis, shownOf(vis))
        Catchup.startAll(c, listOf(u to primaryItems) + ex.map { it.first to it.second.items })
        // Only refetch the TV guide if addresses or channels have changed
        val es = epg.hashCode().toLong() * 31 + sig(vis.filter { it.kind == Kind.LIVE })
        if (es != epgSig) { val first = epgSig == 0L; epgSig = es; Xmltv.start(c, epg, vis, if (first) 6_000L else 0L) }
    }

    private fun restartCatchup(c: Context) {
        val u = primaryUrl ?: return
        Catchup.startAll(c, listOf(u to primaryItems) + extraUrls(c).mapNotNull { url -> extras[url]?.let { url to it.items } })
    }

    private fun nameOf(u: String): String = lists.firstOrNull { it.url == u }?.name ?: hostOf(u)

    @Volatile private var twins: Map<String, Item> = emptyMap()

    /** Same movie from source [k] that is hidden in "All" (for the source filter), otherwise null. */
    fun twin(i: Item, k: Int): Item? = if (i.kind == Kind.MOVIE) twins["$k|" + i.mk] else null

    /** Does the title belong to source [k]? */
    fun inSource(i: Item, k: Int): Boolean = merged.inSource(i, k)

    /** Titles of one source (0 = active playlist), with parental-control filter. */
    fun bySource(k: Int): List<Item> {
        val m = merged
        return shownOf(m.items.filter { m.inSource(it, k) })
    }

    /** Load further playlists: [network] = from the server (otherwise from the cache, only missing ones from the server). */
    private suspend fun loadExtras(c: Context, network: Boolean) {
        val urls = extraUrls(c)
        extraError = null
        if (urls.isEmpty()) { if (extras.isNotEmpty()) { extras.clear(); rebuild(c) }; return }
        heavy = false
        var changed = false
        for (url in urls) {
            if (!network && extras.containsKey(url)) continue
            if (cancelled()) throw CancellationException("abgebrochen")
            if (Mem.heapTight(0.6)) { extraError = "Zu wenig Speicher für eine weitere Playlist auf diesem Gerät."; break }
            phase = "Weitere Playlist wird geladen: " + nameOf(url)
            val r = try {
                loadExtra(c, url, network)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Server unreachable: take the old state if there is one
                val old = if (network) runCatching { loadExtra(c, url, false) }.getOrNull() else null
                if (old == null) extraError = nameOf(url) + ": " + (if (e is ParseStop) MSG_TOO_BIG else PlaylistLogic.friendlyError(e))
                old
            } catch (e: OutOfMemoryError) {
                extraError = nameOf(url) + ": " + MSG_TOO_BIG
                null
            }
            if (r != null) { extras[url] = r; changed = true }
        }
        if (changed || extras.keys.any { it !in urls }) rebuild(c)
    }

    private suspend fun loadExtra(c: Context, u: String, network: Boolean): Extra? {
        val hidden = PlaylistMode.withMode(c, u, LangFilter.hidden)
        if (!network) {
            val head = readHeader(c, u)
            if (head != null && (head.hidden == null || head.hidden == hidden)) {
                readCache(c, u, hidden)?.let { if (it.items.isNotEmpty()) return Extra(it.items, it.epg, it.order) }
            }
            val gz = gzFile(c, u)
            if (gz.exists()) {
                val b = build(c, u, gz, true, Charsets.UTF_8, false, null)
                val p = b.parsed
                if (p.items.isNotEmpty()) {
                    val epg = head?.epg?.takeIf { it.isNotEmpty() } ?: epgFor(u, p.epgUrls)
                    val order = head?.order ?: emptyMap()
                    writeCache(c, u, p.items, epg, order, p.counts, p.bytes, b.hidden)
                    Catchup.saveParsed(c, u, p.catchup)
                    return Extra(p.items, epg, order)
                }
            }
        }
        val (src, cs) = downloadWithRetry(c, u)
        val gzTmp = tmp(gzFile(c, u))
        val b = build(c, u, src, false, cs, false, gzTmp)
        val p = b.parsed
        if (p.counts.isEmpty()) throw PlaylistError("Keine Sender in der Playlist gefunden.")
        val urls = epgFor(u, p.epgUrls)
        val order = fetchOrder(u)
        writeCache(c, u, p.items, urls, order, p.counts, p.bytes, b.hidden)
        Catchup.saveParsed(c, u, p.catchup)
        commitGz(c, u, gzTmp)
        return Extra(p.items, urls, order)
    }

    /**
     * Selection "show at the same time" changed: load missing playlists (cache, otherwise server) and merge again.
     * false = currently busy.
     */
    suspend fun applyMulti(c: Context): Boolean {
        ensure(c)
        if (primaryUrl == null) return true
        if (!loadLock.tryLock()) return false
        try {
            job = currentCoroutineContext()[Job]
            loading = true; error = null
            withContext(Dispatchers.IO) { loadExtras(c, network = false) }
            return true
        } finally {
            loading = false; heavy = false; job = null
            loadLock.unlock()
        }
    }

    private enum class Local { OK, NONE, FAILED }

    /**
     * Load without network: from the cache if it matches the current language selection, otherwise from the compressed
     * stored whole list, read in again ([allowGz]).
     */
    private suspend fun fromLocal(c: Context, u: String, allowGz: Boolean, ask: Boolean): Local {
        val hidden = PlaylistMode.withMode(c, u, LangFilter.hidden)
        val head = readHeader(c, u)
        if (head != null) {
            // Old version (whole list without language counts) with a very large file and no language selection: do not load everything into memory
            val legacyTooBig = head.hidden == null && (hidden - LangTagLogic.PSEUDO).isEmpty() && cacheFile(c, u).length() > budget()
            if (!legacyTooBig && (head.hidden == null || head.hidden == hidden)) {
                val t0 = android.os.SystemClock.elapsedRealtime()
                val cached = takePreread(c, u, hidden) ?: readCache(c, u, hidden)
                val t1 = android.os.SystemClock.elapsedRealtime()
                if (cached != null && cached.counts.isNotEmpty()) {
                    PlaylistDiag.log("cache", PlaylistDiag.summary(cached.items) + " hidden=${hidden.sorted()}")
                    show(c, u, cached.items, cached.epg, cached.order, cached.counts, cached.bytes, hidden)
                    // Start-up timing for Settings > Diagnostics: reading the stored list, then building it for the screens
                    Diag.note("Start: stored list ${cached.items.size} items read in ${t1 - t0} ms, shown in ${android.os.SystemClock.elapsedRealtime() - t1} ms, translations ${Lang.loadMs} ms registered, ${Lang.prewarmMs} ms parsed in the background")
                    return Local.OK
                }
            }
        }
        val gz = gzFile(c, u)
        if (!allowGz || !gz.exists()) return Local.NONE
        heavy = true
        if (loadStarted == 0L) loadStarted = System.currentTimeMillis()
        return try {
            val b = build(c, u, gz, true, Charsets.UTF_8, ask, null)
            val p = b.parsed
            if (p.counts.isEmpty()) return Local.NONE
            PlaylistDiag.log("stored list", PlaylistDiag.summary(p.items) + " hidden=${b.hidden.sorted()}")
            val epg = head?.epg?.takeIf { it.isNotEmpty() } ?: epgFor(u, p.epgUrls)
            val order = head?.order ?: emptyMap()
            phase = "Playlist wird gespeichert …"; downloadDone = true
            writeCache(c, u, p.items, epg, order, p.counts, p.bytes, b.hidden)
            Catchup.saveParsed(c, u, p.catchup)
            show(c, u, p.items, epg, order, p.counts, p.bytes, b.hidden)
            Local.OK
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cancelled()) throw CancellationException("abgebrochen")
            error = if (e is ParseStop) MSG_TOO_BIG else PlaylistLogic.friendlyError(e)
            Local.FAILED
        } catch (e: OutOfMemoryError) {
            error = MSG_TOO_BIG
            Local.FAILED
        }
    }

    /**
     * Loads a playlist. [save] = newly added (only adopted into the list on success, but remembered beforehand),
     * [ask] = may ask for languages on very large lists (not in the background), [resumed] = continued automatically.
     */
    suspend fun load(
        c: Context, url: String, save: Boolean, skipNetworkWhenCached: Boolean = false,
        ask: Boolean = true, resumed: Boolean = false, startup: Boolean = false
    ): Boolean {
        if (!loadLock.tryLock()) return rawChannels.isNotEmpty()
        try {
            return loadInner(c, url, save, skipNetworkWhenCached, ask, resumed, startup)
        } finally {
            pre = null   // T-047: an unused read-ahead must not keep the list in memory
            loading = false   // also on cancel: otherwise the loading state (and the screen keep-awake) would stay stuck
            heavy = false
            langAsk = null
            loadStarted = 0L
            askRequested = false
            job = null
            loadLock.unlock()
        }
    }

    private suspend fun loadInner(c: Context, url: String, save: Boolean, skipNetworkWhenCached: Boolean, ask: Boolean, resumed: Boolean, startup: Boolean = false): Boolean {
        ensure(c)
        val u = PlaylistLogic.normalizeUrl(url) ?: run { error = "Bitte eine gültige Adresse eingeben (beginnt mit http:// oder https://)."; return false }
        // Restore the saved sublist for this playlist so the filter is applied while loading.
        val h = key(u)
        val savedName = SublistStore.active(c, h)
        val savedFilter = savedName?.let { SublistStore.allFor(c, h).firstOrNull { it.name == savedName }?.filter }
        if (savedFilter != null) setSublistForLoad(c, u, savedFilter, savedName)
        job = currentCoroutineContext()[Job]
        loading = true; error = null; heavy = false
        phase = "Gespeicherte Playlist wird gelesen …"; bytesRead = 0L; totalBytes = -1L; downloadDone = false
        var forceAsk = false
        if (save) {
            val prev = pending(c)
            // Last attempt crashed or was too big, now restarted by hand: show the languages again for a large list
            forceAsk = !resumed && prev != null && prev.url == u && (prev.interrupted || prev.msg == MSG_TOO_BIG)
            writePending(c, LoadLogic.started(prev, u, resumed))
        }
        if (askNext && ask) forceAsk = true
        var usedCache = false
        if (rawChannels.isEmpty()) {
            val r = withContext(Dispatchers.IO) { fromLocal(c, u, allowGz = skipNetworkWhenCached, ask = ask) }
            usedCache = r == Local.OK
        }
        // Take further simultaneously shown playlists straight from the cache (fast start)
        if (usedCache) withContext(Dispatchers.IO) { runCatching { loadExtras(c, network = false) }.onFailure { if (it is CancellationException) throw it } }
        if (usedCache && skipNetworkWhenCached) {
            if (save) finishSave(c, u)
            return true
        }
        // T-047: the stored list is on screen: start the silent refresh a few seconds later, so the first screens are not slowed down
        val wait = StartupLogic.refreshDelayMs(usedCache, userStarted = !startup)
        if (wait > 0) kotlinx.coroutines.delay(wait)
        error = null
        val before = sig(primaryItems)
        heavy = true
        if (loadStarted == 0L) loadStarted = System.currentTimeMillis()
        val ok = withContext(Dispatchers.IO) {
            try {
                val (src, cs) = downloadWithRetry(c, u)
                val gzTmp = tmp(gzFile(c, u))
                val b = build(c, u, src, false, cs, ask, gzTmp, forceAsk)
                val parsed = b.parsed
                PlaylistDiag.log("parsed", PlaylistDiag.summary(parsed.items) + " languages=${parsed.counts.size} hidden=${b.hidden.sorted()}")
                if (parsed.counts.isEmpty()) throw PlaylistError("Keine Sender in der Playlist gefunden.")
                downloadDone = true
                val urls = epgFor(u, parsed.epgUrls)
                phase = "Kategorien werden sortiert …"
                val order = fetchOrder(u)
                phase = "Playlist wird gespeichert …"
                writeCache(c, u, parsed.items, urls, order, parsed.counts, parsed.bytes, b.hidden)
                saveTypeCounts(c, u, parsed.items)
                Catchup.saveParsed(c, u, parsed.catchup)
                commitGz(c, u, gzTmp)
                val list = parsed.items
                if (sig(list) != before || !usedCache || b.hidden != shownHidden) show(c, u, list, urls, order, parsed.counts, parsed.bytes, b.hidden)
                else { primaryEpg = urls; primaryOrder = order; langCounts = parsed.counts; langBytes = parsed.bytes; restartCatchup(c) }
                // Renew further shown playlists from the server as well
                loadExtras(c, network = true)
                if (save) finishSave(c, u)
                askNext = false
                refreshError = null
                true
            } catch (e: CancellationException) {
                if (save) failPending(c, MSG_CANCEL)
                throw e
            } catch (e: Exception) {
                if (cancelled()) { if (save) failPending(c, MSG_CANCEL); throw CancellationException("abgebrochen") }
                val msg = if (e is ParseStop) MSG_TOO_BIG else PlaylistLogic.friendlyError(e)
                if (e is ParseStop) askNext = true
                PlaylistDiag.log("load failed", e.javaClass.simpleName)
                if (rawChannels.isEmpty() || save) error = msg else refreshError = msg
                if (save) failPending(c, msg)
                usedCache
            } catch (e: OutOfMemoryError) {
                askNext = true
                if (save) failPending(c, MSG_TOO_BIG)
                PlaylistDiag.log("load failed", "OutOfMemoryError")
                if (rawChannels.isEmpty() || save) error = MSG_TOO_BIG else refreshError = MSG_TOO_BIG
                usedCache
            }
        }
        loading = false
        return ok
    }

    /** Newly added playlist loaded successfully: adopt it into the list and delete the remembered job. */
    private fun finishSave(c: Context, u: String) {
        val idx = lists.indexOfFirst { it.url == u }
        if (idx >= 0) active = idx else { lists.add(PL(hostOf(u), u)); active = lists.size - 1 }
        persist(c)
        syncFailover(c)
        clearPending(c)
    }

    /**
     * Language filter changed: re-parse without network (from the stored whole list, otherwise from an old cache).
     * If neither exists, load normally. If the new selection does not fit in memory, the old one stays active.
     * Return value false = currently busy (try again later), otherwise true.
     */
    suspend fun reapplyLanguages(c: Context): Boolean {
        val u = savedUrl(c) ?: return true
        if (!loadLock.tryLock()) return false
        try {
            job = currentCoroutineContext()[Job]
            loading = true; error = null
            phase = "Sprachauswahl wird angewendet …"; bytesRead = 0L; totalBytes = -1L; downloadDone = false
            val prev = shownHidden
            PlaylistDiag.log("languages", "hidden=${LangFilter.hidden.sorted()}")
            // Little memory free: release the old list first, otherwise old and new list would be in memory at once
            if (Mem.heapTight(0.45)) setItems(emptyList())
            val r = withContext(Dispatchers.IO) { fromLocal(c, u, allowGz = true, ask = false) }
            when (r) {
                // Re-parse further playlists with the new language selection
                Local.OK -> { extras.clear(); withContext(Dispatchers.IO) { runCatching { loadExtras(c, network = false) }.onFailure { if (it is CancellationException) throw it } } }
                Local.NONE -> return loadInner(c, u, false, false, false, false).let { true }
                Local.FAILED -> {
                    // New selection does not fit: restore the old selection (and, if already released, parse again)
                    val msg = error
                    LangFilter.set(c, prev - LangTagLogic.PSEUDO)
                    if (rawChannels.isEmpty()) withContext(Dispatchers.IO) { fromLocal(c, u, allowGz = true, ask = false) }
                    error = msg
                }
            }
            return true
        } finally {
            loading = false
            heavy = false
            loadStarted = 0L
            askRequested = false
            job = null
            loadLock.unlock()
        }
    }

    /**
     * Background refresh while the app is closed: download, parse with the stored language selection and
     * write the cache. It does not ask; if the list is too big without a language selection, the old state stays.
     */
    suspend fun refreshCacheOnly(c: Context, url: String): Boolean {
        ensure(c)
        val u = PlaylistLogic.normalizeUrl(url) ?: return false
        if (!loadLock.tryLock()) return false
        try {
            job = currentCoroutineContext()[Job]
            return withContext(Dispatchers.IO) {
                try {
                    val (src, cs) = downloadWithRetry(c, u)
                    val gzTmp = tmp(gzFile(c, u))
                    val b = build(c, u, src, false, cs, false, gzTmp)
                    if (b.parsed.counts.isEmpty()) return@withContext false
                    writeCache(c, u, b.parsed.items, epgFor(u, b.parsed.epgUrls), fetchOrder(u), b.parsed.counts, b.parsed.bytes, b.hidden)
                    saveTypeCounts(c, u, b.parsed.items)
                    Catchup.saveParsed(c, u, b.parsed.catchup)
                    commitGz(c, u, gzTmp)
                    true
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dropRaw(c, u)
                    false
                } catch (e: OutOfMemoryError) {
                    dropRaw(c, u)
                    false
                }
            }
        } finally {
            job = null
            loadLock.unlock()
        }
    }

    /**
     * "Reload playlist from scratch" (playlist settings): forgets everything stored for the active playlist – cache,
     * stored whole list, downloaded and partial files, and the remembered "load via API" – and downloads it again.
     * The old list is released first, so old and new list are never in memory at once.
     * Kept: the playlist itself, its name, backup servers, content mode and language selection. false = a load is running.
     */
    suspend fun reloadFromScratch(c: Context): Boolean {
        ensure(c)
        val u = savedUrl(c) ?: return false
        if (loading || loadLock.isLocked) return false
        val files = listOf(cacheFile(c, u), gzFile(c, u), rawFile(c, u), partFile(c, u), infoFile(c, u))
        bumpGen(u)
        withContext(Dispatchers.IO) { (files + files.map { tmp(it) }).forEach { runCatching { it.delete() } } }
        prefs(c).edit().remove(apiKey(u)).apply()
        PlaylistDiag.log("reload from scratch", "stored files deleted")
        refreshError = null
        primaryItems = emptyList()
        setItems(emptyList())
        return load(c, u, save = false)
    }

    /** Delete a fully downloaded raw file that is unusable in the background (space); a partial file stays for resuming. */
    private fun dropRaw(c: Context, u: String) {
        if (rawFile(c, u).exists()) { rawFile(c, u).delete(); infoFile(c, u).delete() }
    }

    /** true if the UI is currently showing a playlist (app runs in the same process). */
    val hasData: Boolean get() = rawChannels.isNotEmpty()

    /** Demo mode: show sample entries without a playlist. Memory only, nothing is saved (see [DemoMode]). */
    fun showDemo(items: List<Item>) { error = null; setItems(items) }

    /** Leaving demo mode: drop the sample entries. */
    fun clearDemo() { setItems(emptyList()) }

    fun clear(c: Context) {
        ensure(c)
        lists.forEach { deleteFiles(c, it.url) }
        extras.clear(); multi = emptySet(); primaryItems = emptyList(); primaryUrl = null
        merged = MultiLogic.Merged(emptyList(), emptySet(), emptyMap()); sourceNames = emptyList()
        lists.clear(); persist(c); setItems(emptyList()); langCounts = emptyMap(); langBytes = emptyMap()
    }
}

/** Counts the bytes read (for the progress bar), reports at most every 128 KB. */
private class CountingStream(inp: InputStream, private val onCount: (Long) -> Unit) : FilterInputStream(inp) {
    private var n = 0L
    private var last = 0L
    override fun read(): Int { val b = super.read(); if (b >= 0) add(1); return b }
    override fun read(b: ByteArray, off: Int, len: Int): Int { val r = super.read(b, off, len); if (r > 0) add(r.toLong()); return r }
    private fun add(k: Long) { n += k; if (n - last >= 131072) { last = n; onCount(n) } }
    override fun close() { onCount(n); super.close() }
}

/** Counts the bytes written (progress when building via the API), reports at most every 256 KB. */
private class CountingOut(out: OutputStream, private val onCount: (Long) -> Unit) : java.io.FilterOutputStream(out) {
    private var n = 0L
    private var last = 0L
    override fun write(b: Int) { out.write(b); add(1) }
    override fun write(b: ByteArray, off: Int, len: Int) { out.write(b, off, len); add(len.toLong()) }
    private fun add(k: Long) { n += k; if (n - last >= 262_144) { last = n; onCount(n) } }
    override fun close() { onCount(n); super.close() }
}

/** Also passes everything read on to [copy] (parse and compressed storage in a single pass). */
private class TeeReader(private val inner: Reader, private val copy: Writer) : Reader() {
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        val n = inner.read(cbuf, off, len)
        if (n > 0) copy.write(cbuf, off, n)
        return n
    }
    override fun close() = inner.close()
}

/** gzip at the fastest level: 230 MB → ~20 MB in just over a second (level 6 would be hardly smaller, but twice as slow). */
private class FastGzip(out: OutputStream) : GZIPOutputStream(BufferedOutputStream(out, 1 shl 16), 1 shl 16) {
    init { def.setLevel(Deflater.BEST_SPEED) }
}
