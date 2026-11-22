package com.ipvio.stream

// T-048a: texts of the playlist edit window. Other languages fall back to English (I18nCore.fill), as for DictEn1.
val DictPm = """
Bearbeiten¦Edit
Playlist bearbeiten¦Edit playlist
Inhalt der Playlist¦Playlist content
Änderungen gelten erst, wenn du „Übernehmen“ wählst.¦Changes only take effect when you choose "Apply".
Wird neu geladen …¦Reloading …
Zusätzlich angezeigt¦Shown additionally
Einstellungen dieser Playlist¦Settings of this playlist
Gilt für alle Playlists ohne eigenen Plan. Einen eigenen Plan stellst du bei „Bearbeiten“ einer Playlist ein.¦Applies to all playlists without their own plan. Set an own plan with "Edit" on a playlist.
Allgemeine Einstellung¦General setting
Diese Playlist folgt der allgemeinen Einstellung unter „Automatisch aktualisieren“. Eine Playlist, die nicht die Hauptliste ist, wird dann nicht automatisch aktualisiert.¦This playlist follows the general setting under "Update automatically". A playlist that is not the main list is then not updated automatically.
Sports Hub¦Sports Hub
Sportarten, Wettbewerbe, Hinweise, Bereiche¦Sports, competitions, alerts, tabs
Wähle, was der Sports Hub zeigt. Ausgeschaltete Sportarten und Wettbewerbe erscheinen nicht und werden nicht abgefragt. Die Ergebnisse kommen von ESPN (inoffizielle Quelle).¦Choose what the Sports Hub shows. Sports and competitions that are switched off do not appear and are not queried. Results come from ESPN (unofficial source).
Live-Ergebnisse¦Live results
Ergebnisse, Tabellen und Spielpläne von ESPN¦Results, tables and schedules from ESPN
Du schaust nur Fußball? Schalte die anderen Sportarten aus.¦Only watch football? Switch the other sports off.
Später hinzugefügte Wettbewerbe (britische Ligen und Pokale, weitere europäische und Weltwettbewerbe, WNBA, College Basketball, LPGA, DP World Tour) sind zunächst aus. Manche zeigen vielleicht keine Spiele: ihre ESPN-Adressen sind nicht geprüft.¦Competitions added later (UK divisions and cups, more European and world competitions, WNBA, college basketball, LPGA, DP World Tour) start switched off. Some may show no games: their ESPN addresses are not verified.
Hinweise¦Alerts
Hinweise bei Spielen deiner Teams¦Alerts for games of your teams
Hinweise bei Anstoß und Toren¦Kick-off and score popups
OK springt zum Spiel¦OK jumps to the game
OK auf dem Hinweis bei Anstoß oder Tor öffnet einen Sender, der das Spiel zeigt¦OK on a kick-off or score popup opens a channel that shows the game
OK: Zum Spiel¦OK: go to the game
Sofort¦Immediately
Bereiche des Sports Hub¦Sports Hub tabs
Blende Bereiche aus, die du nicht brauchst. Mindestens ein Bereich bleibt immer.¦Hide tabs you do not use. At least one tab always stays.
Startbereich¦Start tab
DNS-Server¦DNS server
Systemstandard¦System default
Eigene Adresse¦Own address
Der DNS-Server löst die Adressen der Anbieter und Dienste auf, mit denen die App spricht. Hilft, wenn ein Anbieter beim Provider gesperrt ist, und für mehr Privatsphäre. Antwortet der Server nicht, nutzt die App den DNS des Geräts. Gilt nicht für den Videostream selbst und nicht für ein VPN.¦The DNS server looks up the addresses of the providers and services the app talks to. It helps when a provider is blocked by your internet provider, and for more privacy. If the server does not answer, the app uses the device's DNS. It does not apply to the video stream itself or to a VPN.
IP-Adresse oder https://…/dns-query (bis zu 3, mit Leerzeichen getrennt)¦IP address or https://…/dns-query (up to 3, separated by spaces)
Ungültig: eine IP-Adresse wie 1.1.1.1 oder eine https-Adresse eingeben.¦Invalid: enter an IP address like 1.1.1.1 or an https address.
DNS speichern¦Save DNS
DNS testen¦Test DNS
Keine Antwort von {0}¦No answer from {0}
OK: example.com → {0}¦OK: example.com → {0}
VPN¦VPN
Verbindet das Gerät über WireGuard mit dem Server deines VPN-Anbieters, auch für den Videostream. Lade bei deinem Anbieter eine WireGuard-Konfigurationsdatei (.conf) herunter und wähle sie hier aus. Der Schlüssel wird verschlüsselt gespeichert und nie angezeigt. Es gibt keinen Kill-Switch: bricht die Verbindung ab, läuft der Verkehr ohne VPN weiter. Ob das VPN auf deinem Gerät läuft, ist nicht geprüft.¦Connects the device through WireGuard to the server of your VPN provider, also for the video stream. Download a WireGuard configuration file (.conf) from your provider and choose it here. The key is stored encrypted and never shown. There is no kill switch: if the connection drops, traffic continues without the VPN. Whether the VPN works on your device has not been verified.
WireGuard-Datei auswählen¦Choose WireGuard file
Andere Datei auswählen¦Choose another file
VPN verbunden¦VPN connected
Verbinde …¦Connecting …
VPN aus¦VPN off
VPN trennen¦Disconnect VPN
VPN verbinden¦Connect VPN
VPN-Konfiguration löschen¦Delete VPN configuration
Konfiguration gespeichert.¦Configuration saved.
Die VPN-Berechtigung wurde nicht erteilt.¦The VPN permission was not granted.
Verbindung fehlgeschlagen¦Connection failed
Keine Konfiguration gespeichert.¦No configuration stored.
Die Datei ist leer.¦The file is empty.
Die Datei ist zu groß für eine WireGuard-Konfiguration.¦The file is too large for a WireGuard configuration.
Das ist keine WireGuard-Konfiguration: Abschnitt [Interface] fehlt.¦This is not a WireGuard configuration: the [Interface] section is missing.
Das ist keine WireGuard-Konfiguration: Abschnitt [Peer] fehlt.¦This is not a WireGuard configuration: the [Peer] section is missing.
Der PrivateKey fehlt in der Konfiguration.¦The PrivateKey is missing in the configuration.
Der Endpoint (Server) fehlt in der Konfiguration.¦The Endpoint (server) is missing in the configuration.
Die WireGuard-Konfiguration ist ungültig.¦The WireGuard configuration is invalid.
Dieses Gerät hat keinen sicheren Schlüsselspeicher. Die Konfiguration wird nicht gespeichert.¦This device has no secure key store. The configuration is not stored.
Neu: VPN mit WireGuard (Einstellungen > Allgemein). Datei deines VPN-Anbieters auswählen, verbinden, fertig – auch der Videostream läuft durch das VPN¦New: VPN with WireGuard (Settings > General). Choose the file of your VPN provider and connect – the video stream goes through the VPN too
Neu: eigener DNS-Server (Einstellungen > Allgemein): Cloudflare, Google, Quad9 oder eine eigene Adresse¦New: own DNS server (Settings > General): Cloudflare, Google, Quad9 or your own address
Neu: Einstellungen > Sports Hub – wähle deine Sportarten und Wettbewerbe, die Hinweise und die Bereiche des Sports Hub¦New: Settings > Sports Hub – choose your sports and competitions, the alerts and the tabs of the Sports Hub
Sport: bei einem Tor- oder Anstoß-Hinweis springt OK direkt zu einem Sender mit dem Spiel¦Sports: on a goal or kick-off popup, OK jumps straight to a channel that shows the game
26 weitere Wettbewerbe: englische Ligen und Pokale, Schottland, Niederlande, EM, WM und mehr (zunächst aus)¦26 more competitions: English leagues and cups, Scotland, the Netherlands, Euro, World Cup and more (off at first)
Playlists: pro Playlist ein Fenster „Bearbeiten“ mit „Übernehmen“, und ein eigener Aktualisierungsplan für jede Playlist¦Playlists: an "Edit" window for every playlist with "Apply", and its own update plan for every playlist
Playlists: „Neu laden“ bei einer Nebenliste lädt im Hintergrund und macht sie nicht mehr zur Hauptliste¦Playlists: "Reload" on a playlist that is not the main list loads in the background and no longer makes it the main list
Schnellerer Start der App¦Faster app start
Das VPN und die neuen Sport-Wettbewerbe sind noch nicht auf allen Geräten getestet¦The VPN and the new sports competitions have not been tested on all devices yet
"""
