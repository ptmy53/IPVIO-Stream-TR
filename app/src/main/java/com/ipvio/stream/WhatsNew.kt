package com.ipvio.stream

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Pure rules for the "What's new" popup after an update (W-004), without Android so they can be tested. */
object WhatsNewLogic {
    /**
     * Show once per version and only after an update: not on a fresh install ([freshInstall]), not when the version was
     * already seen, and not when there are no notes for it.
     */
    fun shouldShow(lastSeenCode: Long, currentCode: Long, freshInstall: Boolean, hasNotes: Boolean): Boolean =
        hasNotes && !freshInstall && currentCode > 0 && lastSeenCode < currentCode

    /**
     * Same, and also when only the version name changed (issue 12): a test build and its release share the versionCode,
     * so after "1.4.2-test" the release "1.4.2" counted as seen. [lastSeenName] null = stored by an older app version
     * (only the code is known then).
     */
    fun shouldShow(lastSeenCode: Long, lastSeenName: String?, currentCode: Long, currentName: String, freshInstall: Boolean, hasNotes: Boolean): Boolean =
        shouldShow(lastSeenCode, currentCode, freshInstall, hasNotes) ||
            (hasNotes && !freshInstall && currentCode > 0 && lastSeenCode == currentCode && lastSeenName != null && lastSeenName != currentName)
}

/**
 * "What's new" popup (W-004): appears once after the app was updated to a new version. The notes are built in per
 * version name (German UI text, translated like every other text); a version without notes shows nothing.
 * Update [NOTES] for every release.
 */
object WhatsNew {
    private const val PREFS = "ipvio_whatsnew"

    /** Entries starting with "## " are section headings (shown as they are, not translated). */
    const val BUG_FIXES = "## Bug fixes (hopefully lol)"

    val NOTES: Map<String, List<String>> = mapOf(
        "1.4.8" to listOf(
            "Neu: VPN mit WireGuard (Einstellungen > Allgemein). Datei deines VPN-Anbieters auswählen, verbinden, fertig – auch der Videostream läuft durch das VPN",
            "Neu: eigener DNS-Server (Einstellungen > Allgemein): Cloudflare, Google, Quad9 oder eine eigene Adresse",
            "Neu: Einstellungen > Sports Hub – wähle deine Sportarten und Wettbewerbe, die Hinweise und die Bereiche des Sports Hub",
            "Sport: bei einem Tor- oder Anstoß-Hinweis springt OK direkt zu einem Sender mit dem Spiel",
            "26 weitere Wettbewerbe: englische Ligen und Pokale, Schottland, Niederlande, EM, WM und mehr (zunächst aus)",
            "Playlists: pro Playlist ein Fenster „Bearbeiten“ mit „Übernehmen“, und ein eigener Aktualisierungsplan für jede Playlist",
            "Playlists: „Neu laden“ bei einer Nebenliste lädt im Hintergrund und macht sie nicht mehr zur Hauptliste",
            "Schnellerer Start der App",
            "Das VPN und die neuen Sport-Wettbewerbe sind noch nicht auf allen Geräten getestet"
        ),
        "1.4.3" to listOf(
            "Handy: neue Einstellungen über das Zahnrad oben – Playlists, Kategorien sortieren und ausblenden, Jugendschutz, Sprache",
            "Handy: neuer Reiter „Sport“ mit den Spielen von heute, deinen Lieblingsteams und „Meine Liste“; Spiele direkt am Fernseher starten",
            "Filme und Serien: Kategorien mit Hoch/Runter verschieben, das Menü bleibt offen, neu „Ans Ende verschieben“",
            "Playlist wechseln geht schneller: die gespeicherte Liste wird gelesen statt neu heruntergeladen",
            "Bildformat: Hinweis, wenn das Bild schon das Format des Fernsehers hat",
            BUG_FIXES,
            "Playlist wechseln zeigt nicht mehr „Noch keine Sender“",
            "Live TV: Links springt zur gewählten Kategorie, Rechts öffnet die Senderliste",
            "Player: „Zuschneiden“ und „Strecken“ wirken wieder",
            "Einstellungen: Meldungen unter den Knöpfen sind sofort zu sehen",
            "WLAN-Übertragung: der Fokus bleibt nach Start und Stopp auf dem Knopf",
            "„Was ist neu“ erscheint auch nach einer Test-Version",
            "Update in der App: „Die Signatur konnte nicht geprüft werden“ stoppt das Update nicht mehr"
        ),
        "1.4.2" to listOf(
            "Google Drive: Favoriten, Verlauf, Einstellungen und Playlists zwischen Geräten sichern und wiederherstellen (Einstellungen > Speicher)",
            "Filme und Serien: auf Wunsch direkt zu den Kategorien (Einstellungen > Allgemein)",
            "Player: Untertitel- und Ton-Verzögerung im Menü einstellbar",
            "Live TV: OK gedrückt halten entfernt einen Sender aus „Zuletzt gesehen“",
            "Live TV: Größe der Senderzeilen wählbar (Klein, Normal, Groß)",
            "Jugendschutz: Inhalte für Erwachsene auf Wunsch nie auf den Startseiten",
            "Filter: Kategorien suchen, Änderungen mit „Übernehmen“ auf einmal speichern",
            "API-Schlüssel bequem am Handy eingeben (QR-Code oben auf der Seite)",
            "Die App startet schneller und ist kleiner",
            "Nach jedem Update: dieses Fenster mit allen Neuigkeiten",
            BUG_FIXES,
            "Kategorien abhaken ruckelt nicht mehr und bringt die App nicht mehr zum Absturz",
            "24/7- und Adult-Kanäle erscheinen wieder, wenn der Filter aus ist",
            "Live TV: Genre-Kategorien lassen sich wieder per OK gedrückt halten ausblenden",
            "Player: das Menü schließt sich nur noch mit Zurück",
            "Jugendschutz: die PIN lässt sich nur noch mit der PIN entfernen",
            "Suche am Handy: nur noch „Schließen“, Zurück schließt mit einem Druck",
            "Startseite: der Playlist-Knopf zeigt „Playlist“ statt eines Download-Symbols, „Zusätzlich anzeigen“ entfernt",
            "Startseite: Reihenfolge und ausgeblendete Kategorien gelten auch für die Reihen",
            "Untertitel-Suche (SubDL) findet bei Serien wieder Treffer",
            "Bildformate heißen so, wie sie wirken („Zuschneiden“)",
            "Viele fehlende Übersetzungen ergänzt"
        )
    )

    var show by mutableStateOf(false)
        private set
    var name by mutableStateOf("")
        private set

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Once at start: decides whether the popup appears. The version counts as seen when the popup is closed. */
    fun check(c: Context) {
        val code = AppUpdate.currentCode(c)
        // Test builds ("1.4.3-test") use the notes of their release version
        val n = AppUpdate.currentName(c).substringBefore('-')
        val fresh = runCatching {
            val pi = c.packageManager.getPackageInfo(c.packageName, 0)
            pi.firstInstallTime == pi.lastUpdateTime
        }.getOrDefault(true)
        val full = AppUpdate.currentName(c)
        val p = prefs(c)
        val last = p.getLong("seen_code", 0L)
        val lastName = p.getString("seen_name", null)
        show = WhatsNewLogic.shouldShow(last, lastName, code, full, fresh, NOTES[n].orEmpty().isNotEmpty())
        name = n
        // Fresh install or no notes: nothing to show, remember the version so a later update compares against it
        if (!show && (last < code || lastName != full)) markSeen(c)
    }

    fun dismiss(c: Context) {
        show = false
        markSeen(c)
    }

    /** Remembers code and full name ("1.4.2-test" differs from "1.4.2"), see [WhatsNewLogic.shouldShow]. */
    private fun markSeen(c: Context) {
        prefs(c).edit().putLong("seen_code", AppUpdate.currentCode(c)).putString("seen_name", AppUpdate.currentName(c)).apply()
    }
}

@Composable
fun WhatsNewHost() {
    if (!WhatsNew.show) return
    val ctx = LocalContext.current
    val notes = WhatsNew.NOTES[WhatsNew.name].orEmpty()
    val focus = remember { FocusRequester() }
    // The list can be longer than the screen: Up/Down on the OK button scrolls it (texts cannot take the focus)
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { delay(150); runCatching { focus.requestFocus() } }
    Dialog(onDismissRequest = { WhatsNew.dismiss(ctx) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color(0xE6000000)), contentAlignment = Alignment.Center) {
            Column(
                Modifier.widthIn(max = 760.dp).fillMaxHeight(0.9f).clip(RoundedCornerShape(20.dp)).background(Tok.GlassStrong).padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Was ist neu", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text(Lang.tr("Version") + " " + WhatsNew.name, color = Tok.TextMid, fontSize = 18.sp, raw = true)
                Column(Modifier.weight(1f).verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (n in notes) {
                        if (n.startsWith("## ")) Text(n.removePrefix("## "), color = Accent2, fontSize = 20.sp, fontWeight = FontWeight.Bold, raw = true, modifier = Modifier.padding(top = 12.dp))
                        else Text("•  " + Lang.tr(n), color = Color.White, fontSize = 16.sp, lineHeight = 22.sp, raw = true)
                    }
                }
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    PillButton("OK", light = true, modifier = Modifier.focusRequester(focus).onPreviewKeyEvent { e ->
                        if (e.type != KeyEventType.KeyDown) false
                        else when (e.key) {
                            Key.DirectionDown -> { scope.launch { scroll.animateScrollBy(240f) }; true }
                            Key.DirectionUp -> { scope.launch { scroll.animateScrollBy(-240f) }; true }
                            else -> false
                        }
                    }) { WhatsNew.dismiss(ctx) }
                    if (scroll.maxValue > 0) Text("Hoch/Runter blättert", color = Muted, fontSize = 14.sp)
                }
            }
        }
    }
}
