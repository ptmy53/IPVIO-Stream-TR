package com.ipvio.stream

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Phone page, part 2 (issues 17, 18, 20, 21): settings that work well on the phone and the Sports Hub.
 * Every call runs on the server thread of [PhoneImport]; anything that changes app state is posted to the main thread
 * ([post]), so it behaves like the same button on the TV. Answers are JSON (see [RemoteLogic]).
 */
internal object RemoteMore {
    private const val GAME_LIMIT = 150

    /** Games handed to the phone, so pin and watch find them again by key. */
    private val games = java.util.concurrent.ConcurrentHashMap<String, SportData.Game>()

    fun handle(name: String, q: Map<String, String>, c: Context, post: (() -> Unit) -> Unit, onMain: (() -> Boolean) -> Boolean): String? { return when (name) {
        // ───── Playlists (issue 18)
        "pls" -> {
            val act = PlaylistStore.active
            RemoteLogic.arr(PlaylistStore.lists.toList().mapIndexed { i, pl ->
                "{\"i\":$i,\"n\":" + RemoteLogic.q(pl.name) + ",\"h\":" + RemoteLogic.q(PlaylistStore.hostOf(pl.url)) + ",\"a\":" + (i == act) + "}"
            })
        }
        "plmain" -> {
            val i = q["i"]?.toIntOrNull() ?: -1
            if (i !in PlaylistStore.lists.indices) null
            else { if (i != PlaylistStore.active) post { PlaylistStore.switchAsync(c, i) }; OK }
        }
        "plren" -> {
            val i = q["i"]?.toIntOrNull() ?: -1
            val n = q["n"].orEmpty().trim().take(60)
            if (i !in PlaylistStore.lists.indices || n.isEmpty()) null else { post { PlaylistStore.rename(c, i, n) }; OK }
        }
        "pldel" -> {
            val i = q["i"]?.toIntOrNull() ?: -1
            if (i !in PlaylistStore.lists.indices) null else { post { PlaylistStore.removeAsync(c, i) }; OK }
        }
        // ───── Categories: Live TV, movies, series (issues 18, 20)
        "vcats" -> cats(q["a"].orEmpty(), c)
        "vhide" -> {
            val a = q["a"].orEmpty(); val g = q["g"] ?: return null; val h = q["h"] == "1"
            when (a) {
                CatOrder.LIVE -> post { if (h) HiddenCats.hide(c, g) else HiddenCats.show(c, g) }
                CatOrder.MOVIE, CatOrder.SERIES -> post { IgnoredGroups.init(c); IgnoredGroups.set(c, IgnoredGroups.groupKey(kindOf(a), g), h) }
                else -> return null
            }
            OK
        }
        "vfav" -> { val g = q["g"] ?: return null; post { CatFavs.init(c); CatFavs.toggle(c, g) }; OK }
        "vmove" -> {
            val a = q["a"].orEmpty(); val g = q["g"] ?: return null
            if (a != CatOrder.LIVE && a != CatOrder.MOVIE && a != CatOrder.SERIES) return null
            val names = providerNames(a)
            if (g !in names) return null
            // Done on the main thread and waited for, so the next list the phone asks for already shows the new order
            onMain {
                CatOrder.init(c)
                when (q["d"]) {
                    "top" -> CatOrder.toTop(c, a, names, g)
                    "bottom" -> CatOrder.toBottom(c, a, names, g)
                    "up" -> CatOrder.move(c, a, names, g, -1)
                    else -> CatOrder.move(c, a, names, g, 1)
                }
                true
            }
            OK
        }
        // ───── Parental control: adult filter, protected by the PIN (issue 18)
        "adult" -> "{\"pin\":" + Parental.pinSet + ",\"hide\":" + Parental.hideAdult + "}"
        "adultset" -> {
            val on = q["v"] == "1"
            // Showing adult content again needs the PIN (like on the TV); hiding it never does
            val ok = on || !Parental.pinSet || onMain { Parental.verify(c, q["pin"].orEmpty()) }
            if (!ok) "{\"ok\":false,\"wait\":" + Parental.lockSec() + "}"
            else { post { Parental.setHide(c, on) }; OK }
        }
        // ───── App language (issue 18)
        "langs" -> "{\"c\":" + RemoteLogic.q(General.lang) + ",\"l\":" + RemoteLogic.arr(I18nCore.CODES.mapIndexed { i, code ->
            "{\"c\":" + RemoteLogic.q(code) + ",\"n\":" + RemoteLogic.q(I18nCore.NATIVE.getOrElse(i) { code }) + "}"
        }) + "}"
        "setlang" -> {
            val code = q["c"].orEmpty()
            if (code.isNotEmpty() && code !in I18nCore.CODES) null else { post { General.setLang(c, code) }; OK }
        }
        // ───── Sports Hub (issue 21)
        "sport" -> sport(q["v"].orEmpty(), c)
        "sleagues" -> {
            SportLeagues.init(c)
            RemoteLogic.arr(SportLeagues.enabled().filter { it.kind == SportData.KIND_TEAM }.map { "{\"p\":" + RemoteLogic.q(it.path) + ",\"n\":" + RemoteLogic.q(it.name) + "}" })
        }
        "steams" -> {
            val l = SportData.LEAGUES.firstOrNull { it.path == q["p"] } ?: return null
            SportHubFavs.init(c)
            val teams = teamsOf(l)
            RemoteLogic.arr(teams.map { t -> "{\"id\":" + RemoteLogic.q(t.id) + ",\"n\":" + RemoteLogic.q(t.name) + ",\"f\":" + SportFavs.has(t) + "}" })
        }
        "stfav" -> {
            val l = SportData.LEAGUES.firstOrNull { it.path == q["p"] } ?: return null
            val t = teamsOf(l).firstOrNull { it.id == q["id"] } ?: return null
            onMain { SportHubFavs.toggleTeam(c, l, t); true }
            "{\"ok\":true,\"f\":" + SportFavs.has(t) + "}"
        }
        "spin" -> {
            val g = q["k"]?.let { games[it] ?: SportFeed.latest[it] } ?: return null
            onMain { SportAlerts.togglePin(c, g); true }
            "{\"ok\":true,\"p\":" + SportAlerts.isPinned(g) + "}"
        }
        "swatch" -> {
            val g = q["k"]?.let { games[it] ?: SportFeed.latest[it] } ?: return null
            val ch = SportWatch.channels(g, liveChannels()).firstOrNull()
            if (ch == null) "{\"ok\":false}"
            else { RemoteHub.playReq = RemoteHub.Play(ch.id, 0, 0); "{\"ok\":true,\"ch\":" + RemoteLogic.q(ch.title) + "}" }
        }
        else -> null
    } }

    private const val OK = "{\"ok\":true}"

    private fun kindOf(area: String) = when (area) { CatOrder.MOVIE -> Kind.MOVIE; CatOrder.SERIES -> Kind.SERIES; else -> Kind.LIVE }

    private fun liveChannels() = PlaylistStore.channels.filter { it.kind == Kind.LIVE }

    /** Provider categories of the area in provider order (the same base list the category pages on the TV move in). */
    private fun providerNames(area: String): List<String> {
        val kind = kindOf(area)
        val names = LinkedHashSet<String>()
        for (i in PlaylistStore.channels) if (i.kind == kind) names.add(i.group)
        val order = PlaylistStore.catOrder
        return if (order.isEmpty()) names.toList() else names.sortedBy { order[it] ?: 1_000_000 }
    }

    /** Categories of an area in the order shown on the TV, with count, hidden and (Live TV) favorite flag. Live TV also lists the genres. */
    private fun cats(area: String, c: Context): String? {
        if (area != CatOrder.LIVE && area != CatOrder.MOVIE && area != CatOrder.SERIES) return null
        CatOrder.init(c)
        val kind = kindOf(area)
        val counts = HashMap<String, Int>()
        for (i in PlaylistStore.channels) if (i.kind == kind) counts[i.group] = (counts[i.group] ?: 0) + 1
        val shown = CatOrder.sorted(area, providerNames(area))
        val rows = ArrayList<String>()
        if (area == CatOrder.LIVE) {
            HiddenCats.init(c); CatFavs.init(c)
            val hidden = HiddenCats.list.toSet()
            for (gen in LiveGenre.ALL) rows.add("{\"g\":" + RemoteLogic.q(gen.key) + ",\"l\":" + RemoteLogic.q(Lang.tr(gen.label)) + ",\"gen\":true,\"h\":" + (gen.key in hidden) + "}")
            val favs = CatFavs.list.toSet()
            for (g in shown) rows.add("{\"g\":" + RemoteLogic.q(g) + ",\"n\":" + (counts[g] ?: 0) + ",\"h\":" + (g in hidden) + ",\"f\":" + (g in favs) + "}")
        } else {
            IgnoredGroups.init(c)
            for (g in shown) rows.add("{\"g\":" + RemoteLogic.q(g) + ",\"n\":" + (counts[g] ?: 0) + ",\"h\":" + IgnoredGroups.has(IgnoredGroups.groupKey(kind, g)) + "}")
        }
        return "{\"mode\":" + CatOrder.mode(area) + ",\"c\":" + RemoteLogic.arr(rows) + "}"
    }

    private fun teamsOf(l: SportData.League): List<SportData.Team> = runBlocking(Dispatchers.IO) {
        runCatching { withTimeoutOrNull(15_000) { SportStandings.fetch(l) } }.getOrNull().orEmpty()
    }.flatMap { it.rows.map { r -> r.team } }.filter { it.name.isNotBlank() }.distinctBy { it.id + it.name }.sortedBy { it.name.lowercase() }

    /** Games for the phone: v = "today" (all active leagues), "fav" (favorite teams, the next days) or "mine" (games picked by hand). */
    private fun sport(v: String, c: Context): String {
        if (!General.sportLive) return "{\"off\":true}"
        SportLeagues.init(c); SportHubFavs.init(c); SportAlerts.init(c)
        val zone = General.zone()
        val list: List<SportData.Game> = runBlocking(Dispatchers.IO) {
            withTimeoutOrNull(20_000) {
                when (v) {
                    "fav" -> {
                        if (SportHubFavs.isEmpty()) return@withTimeoutOrNull emptyList<SportData.Game>()
                        val paths = HashSet<String>()
                        paths.addAll(SportHubFavs.leagues)
                        SportHubFavs.teamList().forEach { f ->
                            if (f.path.isNotBlank()) paths.add(f.path)
                            else SportLeagues.enabled().filter { it.kind == SportData.KIND_TEAM }.forEach { paths.add(it.path) }
                        }
                        SportData.LEAGUES.filter { SportHubFavs.sports.contains(it.sport) }.forEach { paths.add(it.path) }
                        val ls = SportData.LEAGUES.filter { it.path in paths }
                        coroutineScope {
                            (0..3).flatMap { d -> ls.map { l -> async { SportData.fetch(l, SportData.ymd(d, zone), if (d == 0) 40_000 else 300_000) } } }.awaitAll()
                        }.flatten().filter { SportHubFavs.isFavGame(it) }
                    }
                    "mine" -> {
                        val pins = SportAlerts.pinned.toSet()
                        val names = pins.map { it.substringBefore('|') }.toSet()
                        val ls = SportData.LEAGUES.filter { it.name in names }
                        val fresh = coroutineScope { ls.map { l -> async { SportData.fetchCurrent(l) } }.awaitAll() }.flatten()
                        (fresh + pins.mapNotNull { games[it] ?: SportFeed.latest[it] }).filter { it.key in pins }
                    }
                    else -> coroutineScope { SportLeagues.enabled().map { l -> async { SportData.fetchCurrent(l) } }.awaitAll() }.flatten()
                }
            } ?: emptyList()
        }
        val sorted = list.distinctBy { it.key }.sortedWith(compareBy<SportData.Game>({ when (it.state) { 1 -> 0; 0 -> 1; else -> 2 } }, { if (it.state == 2) -it.start else it.start })).take(GAME_LIMIT)
        val live = liveChannels()
        sorted.forEach { games[it.key] = it }
        if (games.size > 2000) games.clear()
        return "{\"g\":" + RemoteLogic.arr(sorted.map { g ->
            RemoteLogic.game(
                g.key, g.groupName, if (g.solo) g.title else "", g.home.name, g.away.name, g.home.score.orEmpty(), g.away.score.orEmpty(),
                g.state, g.detail, g.start, SportWatch.channels(g, live).isNotEmpty(), SportHubFavs.isFavGame(g), SportAlerts.isPinned(g)
            )
        }) + ",\"nofav\":" + SportHubFavs.isEmpty() + "}"
    }
}
