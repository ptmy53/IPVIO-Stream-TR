package com.ipvio.stream

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Settings > Storage > Google Drive (W-010): sign in with a code, back up this device, restore from any device.
 * Restoring is always manual: pick a backup, see what it contains, choose the categories, confirm.
 * Texts are German source texts, translated through DictN.
 */
@Composable
internal fun DriveSection(ctx: Context) {
    val scope = rememberCoroutineScope()
    remember { DriveSync.init(ctx); 0 }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    SectionTitle("Google Drive")
    Hint("Gleicht Favoriten, Verlauf und Einstellungen zwischen deinen Geräten ab. Die App legt in deinem Google Drive den Ordner „IPVIO Stream Sync“ an und sieht nur diesen Ordner, sonst nichts.")

    if (!DriveSync.connected) {
        DriveConnect(ctx) { status = it }
    } else {
        // Live sync: favorites and history follow this device without a button, only when switched on here
        var liveOn by remember { mutableStateOf(LiveSync.isOn(ctx)) }
        var liveFav by remember { mutableStateOf(LiveSync.favoritesOn(ctx)) }
        var liveHist by remember { mutableStateOf(LiveSync.historyOn(ctx)) }
        // English for now (not yet in the translation dictionaries)
        ToggleRow(
            "Live sync",
            "Syncs favorites and history with your other devices automatically whenever something changes. Switch it on separately on each device.",
            liveOn
        ) { liveOn = it; LiveSync.setSwitches(ctx, it, liveFav, liveHist) }
        if (liveOn) {
            ToggleRow("Favorites and lists", null, liveFav) { liveFav = it; LiveSync.setSwitches(ctx, liveOn, it, liveHist) }
            ToggleRow("History and continue watching", null, liveHist) { liveHist = it; LiveSync.setSwitches(ctx, liveOn, liveFav, it) }
            val at = LiveSync.lastOk
            Text(
                if (at > 0) "Last synced: " + dayLabel(at / 1000) + " " + hhmm(at / 1000)
                else "Not synced yet.",
                color = Muted, fontSize = 15.sp
            )
            if (LiveSync.lastError.isNotEmpty()) Text("Live sync error: " + LiveSync.lastError, color = Muted, fontSize = 14.sp)
        }

        var pass by remember { mutableStateOf(DriveSync.passphrase(ctx).orEmpty()) }
        var withPl by remember { mutableStateOf(pass.isNotEmpty()) }
        var remotes by remember { mutableStateOf<List<DriveSync.Remote>?>(null) }

        ToggleRow(
            "Playlists und API-Schlüssel mitsichern",
            "Verschlüsselt mit deinem Sync-Passwort. Ohne das Passwort kann niemand sie lesen, auch Google nicht.",
            withPl
        ) { withPl = it }
        if (withPl) {
            Field(pass, { pass = it.take(64) }, "Sync-Passwort (mindestens 8 Zeichen)", Modifier.widthIn(max = 760.dp), password = true,
                onDone = { if (SyncLogic.validPassphrase(pass)) DriveSync.savePassphrase(ctx, pass) })
            Hint("Auf allen Geräten das gleiche Passwort verwenden. Auf dem Fire TV kannst du auch mit der Fire-TV-App am Handy tippen. Vergisst du es, lassen sich die Playlists aus Drive nicht mehr lesen.")
        }

        if (DriveSync.lastBackup > 0) {
            val s = DriveSync.lastBackup / 1000
            Text("Letzte Sicherung von diesem Gerät: " + dayLabel(s) + " " + hhmm(s), color = Muted, fontSize = 15.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PillButton("Jetzt in Drive sichern") {
                if (busy) return@PillButton
                if (withPl && !SyncLogic.validPassphrase(pass)) { status = "Das Sync-Passwort braucht mindestens 8 Zeichen."; return@PillButton }
                busy = true
                status = "Sichern …"
                scope.launch {
                    if (withPl) DriveSync.savePassphrase(ctx, pass)
                    val t = withContext(Dispatchers.IO) { Backup.exportSync(ctx, if (withPl) pass else null) }
                    status = if (t == null) "Sicherung fehlgeschlagen."
                    else DriveSync.upload(ctx, t).fold({ "In Google Drive gesichert." }, { driveError(it) })
                    busy = false
                }
            }
            PillButton("Sicherungen in Drive anzeigen") {
                if (busy) return@PillButton
                busy = true
                status = "Laden …"
                scope.launch {
                    DriveSync.list(ctx).onSuccess { remotes = it; status = if (it.isEmpty()) "In Google Drive gibt es noch keine Sicherung." else "" }
                        .onFailure { status = driveError(it) }
                    busy = false
                }
            }
        }
        remotes?.takeIf { it.isNotEmpty() }?.let { list ->
            DriveRestore(ctx, list, pass) { status = it }
        }
        ConfirmPill("Von Google trennen") {
            DriveSync.disconnect(ctx)
            remotes = null
            pass = ""
            status = "Getrennt. Die Sicherungen in Google Drive bleiben erhalten."
        }
    }
    StatusLine(status)
}

/** Sign-in: code and QR code for google.com/device; waits in the background until the user agreed on the phone. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DriveConnect(ctx: Context, onStatus: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf<DriveSync.DeviceCode?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    DisposableEffect(Unit) { onDispose { job?.cancel() } }
    val c = code
    if (c == null) {
        PillButton("Mit Google verbinden") {
            if (job?.isActive == true) return@PillButton
            onStatus("Bitte warten …")
            job = scope.launch {
                DriveSync.start().onSuccess { d ->
                    code = d
                    onStatus("")
                    val r = DriveSync.poll(ctx, d)
                    code = null
                    onStatus(when (r) {
                        DriveSync.SignIn.CONNECTED -> "Mit Google verbunden."
                        DriveSync.SignIn.DENIED -> "Die Anmeldung wurde am Handy abgelehnt."
                        DriveSync.SignIn.EXPIRED -> "Der Code ist abgelaufen. Bitte neu verbinden."
                        DriveSync.SignIn.FAILED -> if (DriveSync.lastError.isNotEmpty()) "Die Anmeldung bei Google ist fehlgeschlagen: " + DriveSync.lastError
                            else "Die Anmeldung bei Google ist fehlgeschlagen."
                    })
                }.onFailure { onStatus(driveError(it)) }
            }
        }
    } else {
        // The connect button disappears: show the whole code panel and put the focus on "Cancel"
        val req = remember { BringIntoViewRequester() }
        val cancelFocus = remember { FocusRequester() }
        LaunchedEffect(c) {
            runCatching { cancelFocus.requestFocus() }
            runCatching { req.bringIntoView() }
        }
        Column(
            Modifier.bringIntoViewRequester(req).clip(RoundedCornerShape(DS.RadiusM)).background(Color(0x14FFFFFF)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Am Handy öffnen und den Code eingeben:", color = Muted, fontSize = 15.sp)
            Text(c.url.removePrefix("https://"), color = Accent2, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, raw = true)
            QrWithLabel(c.url)
            Text(c.user, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, raw = true)
            Text("Warte auf die Bestätigung am Handy …", color = Muted, fontSize = 14.sp)
            PillButton("Abbrechen", Modifier.focusRequester(cancelFocus)) { job?.cancel(); code = null; onStatus("") }
        }
    }
}

/** Pick a backup, see its content, choose the categories and restore (with safety copy, see [Backup.restoreText]). */
@Composable
private fun DriveRestore(ctx: Context, list: List<DriveSync.Remote>, pass: String, onStatus: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf<DriveSync.Remote?>(null) }
    var text by remember { mutableStateOf<String?>(null) }
    var sum by remember { mutableStateOf<Backup.Summary?>(null) }
    var fav by remember { mutableStateOf(true) }
    var hist by remember { mutableStateOf(true) }
    var sett by remember { mutableStateOf(true) }
    var pl by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }

    Text("Sicherung zum Wiederherstellen wählen:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    for (r in list) {
        val s = r.modified / 1000
        val name = r.deviceName ?: Lang.tr("Unbekanntes Gerät")
        val label = name + " · " + (if (s > 0) dayLabel(s) + " " + hhmm(s) else "?") +
            (if (r.isThisDevice(ctx)) " · " + Lang.tr("dieses Gerät") else "")
        PillButton(label, raw = true, light = picked?.id == r.id) {
            if (busy) return@PillButton
            busy = true
            picked = r; text = null; sum = null
            onStatus("Laden …")
            scope.launch {
                DriveSync.download(ctx, r).onSuccess { t ->
                    val su = withContext(Dispatchers.Default) { Backup.summary(t) }
                    if (su == null) onStatus("Diese Sicherung ist ungültig.")
                    else {
                        text = t; sum = su
                        fav = SyncLogic.Category.FAVORITES in su.categories
                        hist = SyncLogic.Category.HISTORY in su.categories
                        sett = SyncLogic.Category.SETTINGS in su.categories
                        pl = su.hasSecure || su.hasPlainPlaylists
                        onStatus("")
                    }
                }.onFailure { onStatus(driveError(it)) }
                busy = false
            }
        }
    }

    val su = sum ?: return
    val t = text ?: return
    Column(
        Modifier.clip(RoundedCornerShape(DS.RadiusM)).background(Color(0x14FFFFFF)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Inhalt der Sicherung", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        if (su.appVersion != null) Text("App-Version " + su.appVersion, color = Muted, fontSize = 14.sp)
        Text("Profile: " + su.profiles, color = Muted, fontSize = 14.sp)
        Text("Favoriten: " + su.favorites, color = Muted, fontSize = 14.sp)
        Text(
            when {
                su.hasSecure -> "Enthält Playlists und API-Schlüssel (verschlüsselt)."
                su.hasPlainPlaylists -> "Enthält Playlists."
                else -> "Ohne Playlists."
            }, color = Muted, fontSize = 14.sp
        )
        if (SyncLogic.Category.FAVORITES in su.categories)
            ToggleRow("Favoriten und Listen", null, fav) { fav = it }
        if (SyncLogic.Category.HISTORY in su.categories)
            ToggleRow("Verlauf und Weiterschauen", null, hist) { hist = it }
        if (SyncLogic.Category.SETTINGS in su.categories)
            ToggleRow("Einstellungen, Profile und Ausblendungen", null, sett) { sett = it }
        if (su.hasSecure || su.hasPlainPlaylists)
            ToggleRow("Playlists ergänzen", "Vorhandene Playlists bleiben. Fehlende API-Schlüssel werden eingetragen.", pl) { pl = it }
        Hint("Die gewählten Bereiche werden auf diesem Gerät ersetzt. Vorher legt die App automatisch eine Sicherung an, die du mit „Letzte Wiederherstellung rückgängig“ zurückholen kannst. Die Jugendschutz-PIN bleibt auf diesem Gerät. Danach startet die App neu.")
        ConfirmPill("Wiederherstellen") {
            if (busy) return@ConfirmPill
            val chosen = buildSet {
                if (fav) add(SyncLogic.Category.FAVORITES)
                if (hist) add(SyncLogic.Category.HISTORY)
                if (sett) add(SyncLogic.Category.SETTINGS)
            }
            if (chosen.isEmpty() && !pl) { onStatus("Bitte mindestens einen Bereich wählen."); return@ConfirmPill }
            if (pl && su.hasSecure && !SyncLogic.validPassphrase(pass)) {
                onStatus("Für die Playlists bitte oben das Sync-Passwort eingeben."); return@ConfirmPill
            }
            busy = true
            onStatus("Wiederherstellen …")
            scope.launch {
                val n = withContext(Dispatchers.IO) {
                    Backup.restoreText(ctx, t, chosen, pl, if (pl && su.hasSecure) pass else null)
                }
                busy = false
                when {
                    n >= 0 -> {
                        if (pl && su.hasSecure) DriveSync.savePassphrase(ctx, pass)
                        Profiles.restart(ctx)
                    }
                    n == Backup.ERR_PASSPHRASE -> onStatus("Falsches Sync-Passwort. Es wurde nichts geändert.")
                    n == Backup.ERR_SAFETY -> onStatus("Die Sicherheitskopie ließ sich nicht anlegen. Es wurde nichts geändert.")
                    else -> onStatus("Diese Sicherung ist ungültig.")
                }
            }
        }
    }
}

/** Undo of the last restore (file, Wi-Fi or Google Drive): puts back the safety copy written right before it. */
@Composable
internal fun UndoRestoreRow(ctx: Context) {
    val at = remember { Backup.undoAvailableAt(ctx) }
    if (at <= 0L) return
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val s = at / 1000
    Hint("Vor der letzten Wiederherstellung (" + dayLabel(s) + " " + hhmm(s) + ") hat die App automatisch eine Sicherung angelegt.")
    ConfirmPill("Letzte Wiederherstellung rückgängig") {
        scope.launch {
            val n = withContext(Dispatchers.IO) { Backup.undoLastRestore(ctx) }
            if (n >= 0) Profiles.restart(ctx) else status = "Rückgängig machen ist fehlgeschlagen."
        }
    }
    StatusLine(status)
}

private fun driveError(e: Throwable): String {
    val d = e as? DriveSync.DriveException ?: return "Unbekannter Fehler: " + e.javaClass.simpleName
    return when (d.reason) {
        DriveSync.Reason.NOT_CONNECTED -> "Nicht mit Google verbunden. Bitte neu verbinden."
        DriveSync.Reason.NETWORK -> if (d.detail.isNotEmpty()) "Keine Verbindung zu Google (" + d.detail + "). Bitte Internet prüfen."
            else "Keine Verbindung zu Google. Bitte Internet prüfen."
        DriveSync.Reason.GOOGLE -> when {
            d.detail.isNotEmpty() && d.code > 0 -> "Google Drive meldet einen Fehler (" + d.code + "): " + d.detail
            d.detail.isNotEmpty() -> "Google Drive meldet einen Fehler: " + d.detail
            d.code > 0 -> "Google Drive meldet einen Fehler (" + d.code + ")."
            else -> "Google Drive meldet einen Fehler."
        }
        DriveSync.Reason.NOT_FOUND -> "Diese Sicherung gibt es in Google Drive nicht mehr."
        DriveSync.Reason.TOO_BIG -> "Diese Sicherung ist zu groß."
    }
}
