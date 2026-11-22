package com.ipvio.stream

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * T-049d: Settings > Sports Hub. Which sports and competitions you follow, goal and kick-off alerts, the tabs of the hub
 * and the tab it opens on. The leagues are those of [SportLeagues]; nothing that is switched off is queried.
 */
@Composable
internal fun SportSettingsPage(ctx: Context) {
    SportLeagues.init(ctx)
    SportAlerts.init(ctx)
    SportHubPrefs.init(ctx)
    var overlay by remember { mutableStateOf(false) }
    if (overlay) ScoreOverlayDialog { overlay = false }

    Hint("Wähle, was der Sports Hub zeigt. Ausgeschaltete Sportarten und Wettbewerbe erscheinen nicht und werden nicht abgefragt. Die Ergebnisse kommen von ESPN (inoffizielle Quelle).")
    ToggleRow("Live-Ergebnisse", "Ergebnisse, Tabellen und Spielpläne von ESPN", General.sportLive) { General.setSportLive(ctx, it) }

    SectionTitle("Sportarten")
    Hint("Du schaust nur Fußball? Schalte die anderen Sportarten aus.")
    for (s in SportData.SPORTS) {
        val ls = SportData.LEAGUES.filter { it.sport == s.key }
        val on = ls.count { SportLeagues.on(it) }
        ToggleRow(s.label, on.toString() + " / " + ls.size + " " + Lang.tr("Wettbewerbe"), on > 0) { SportLeagues.setSport(ctx, s.key, it) }
    }

    SectionTitle("Wettbewerbe")
    Hint("Später hinzugefügte Wettbewerbe (britische Ligen und Pokale, weitere europäische und Weltwettbewerbe, WNBA, College Basketball, LPGA, DP World Tour) sind zunächst aus. Manche zeigen vielleicht keine Spiele: ihre ESPN-Adressen sind nicht geprüft.")
    for (s in SportData.SPORTS) {
        val ls = SportData.LEAGUES.filter { it.sport == s.key }
        for (l in ls) ToggleRow(l.name + "  ·  " + Lang.tr(s.label), null, SportLeagues.on(l)) { SportLeagues.toggle(ctx, l) }
    }

    SectionTitle("Hinweise")
    ToggleRow("Hinweise bei Spielen deiner Teams", "Hinweise bei Anstoß und Toren", SportAlerts.enabled) { SportAlerts.setEnabled(ctx, it) }
    ToggleRow("OK springt zum Spiel", "OK auf dem Hinweis bei Anstoß oder Tor öffnet einen Sender, der das Spiel zeigt", SportHubPrefs.jumpOnAlert) { SportHubPrefs.setJump(ctx, it) }
    val delays = SportAlerts.DELAYS
    SelectRow("Verzögerung", if (SportAlerts.delaySec == 0) Lang.tr("Sofort") else SportAlerts.delaySec.toString() + " s", delays.map { if (it == 0) Lang.tr("Sofort") else "$it s" }, delays.indexOf(SportAlerts.delaySec).coerceAtLeast(0)) {
        while (SportAlerts.delaySec != delays[it]) SportAlerts.cycleDelay(ctx)
    }
    PillButton("Score-Overlay", light = true) { overlay = true }

    SectionTitle("Bereiche des Sports Hub")
    Hint("Blende Bereiche aus, die du nicht brauchst. Mindestens ein Bereich bleibt immer.")
    val names = listOf("Übersicht", "Live", "Spielplan", "Ligen", "Teams", "Favoriten", "Sender")
    names.forEachIndexed { i, n ->
        val shown = i in SportHubLogic.visible(SportHubPrefs.hidden())
        ToggleRow(n, null, shown) { SportHubPrefs.toggleTab(ctx, i) }
    }
    val vis = SportHubLogic.visible(SportHubPrefs.hidden())
    SelectRow("Startbereich", names[SportHubLogic.resolve(SportHubPrefs.startTab, SportHubPrefs.hidden())], vis.map { names[it] }, vis.indexOf(SportHubLogic.resolve(SportHubPrefs.startTab, SportHubPrefs.hidden())).coerceAtLeast(0)) {
        SportHubPrefs.setStart(ctx, vis[it])
    }
}
