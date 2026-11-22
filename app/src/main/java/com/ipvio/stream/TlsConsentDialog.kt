package com.ipvio.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * #69: asks once per host before a provider connection is made without encryption, because its certificate does not
 * match its address. Shown while [TlsFallback.pendingAsk] is set. Cancel: nothing is sent over http.
 */
@Composable
fun TlsConsentDialog() {
    val host = TlsFallback.pendingAsk ?: return
    Dialog(
        onDismissRequest = { TlsFallback.dismissPending() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier.width(720.dp).clip(RoundedCornerShape(DS.RadiusL)).background(Tok.GlassStrong).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(Lang.tr("Unverschlüsselte Verbindung?"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(Lang.tr("Das Zertifikat dieses Servers passt nicht zu seiner Adresse. Die Verbindung kann ohne Verschlüsselung fortgesetzt werden. Zugangsdaten und Inhalte können dann im Netzwerk mitgelesen werden."), color = Color.White, fontSize = 17.sp)
            Text("Server: $host", color = Color.White, fontSize = 15.sp)
            PillButton(Lang.tr("Ohne Verschlüsselung verbinden"), modifier = Modifier.fillMaxWidth()) { TlsFallback.agreePending() }
            PillButton(Lang.tr("Abbrechen"), modifier = Modifier.fillMaxWidth(), light = true) { TlsFallback.dismissPending() }
        }
    }
}
