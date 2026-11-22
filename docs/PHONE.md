# Phone features: search, quick setup and control

The TV runs a small web page on your home network. You open it on your phone by scanning a QR code on the TV. No app and no account is needed on the phone. Phone and TV must be on the **same Wi-Fi**.

## How to connect

1. On the TV, open **Phone search** (button at the bottom of the sidebar) or a page that shows a QR code, for example **Playlists > Enter on phone**.
2. Scan the QR code with the phone camera, or type the address shown under it.
3. Enter the **4-digit code** shown on the TV. The phone remembers it for the session.

Protection: the code is checked on every request. After **5 wrong codes** the page is locked for one minute. The TV keeps running when you close its QR card, so the phone stays connected.

Note: the connection is plain HTTP inside your home network. Do not use it over public Wi-Fi.

If the app cannot find an address, the TV is not connected to a Wi-Fi network. The server tries the ports 8765 to 8775 and cannot start if all of them are in use.

## 1. Quick setup: add a playlist from the phone

Typing a long Xtream login with the TV remote is slow. From the phone you can send the playlist to the TV in one step:

- **M3U link:** paste the address into the phone page and send it.
- **Xtream login:** enter server, user name and password and send them.
- **Playlist file from the phone:** choose a `.m3u`, `.m3u8` or `.txt` file on the phone and upload it. The TV copies the file into the app and loads it like a playlist.

The TV then loads the playlist just like **Add playlist** on the TV. Use the same screen to add a second playlist later.

## 2. Phone search

The phone page has four tabs: **Movies**, **Series**, **Live TV** and **Sport**.

- **Search** movies, series and channels by typing a title. Tap a result to start it on the TV.
- **Continue watching** shows what you started, with the progress.
- **Series:** choose a season and an episode and start it.
- **Live TV:** search channels and start them. Favourite channels and the show running now are shown.
- **My favourites** and **My list** (watch list) are shown on the phone too.

Starting a title on the phone starts it on the TV. Nothing is streamed to the phone.

## 3. Control the running player from the phone

While something plays on the TV, the phone shows a control panel:

- **Pause / Play** and **Stop**
- **Skip back** 30 s or 10 s, **skip forward** 10 s or 30 s
- **Volume** down and up

Use it when the TV is out of reach, for example from the couch next to the phone or from the kitchen.

## 4. Categories on the phone

The category settings can be changed from the phone, separately for Live TV, movies and series:

- **Show or hide** a category, and **show it again** later.
- **Move** a category up, down, to the top or to the end.
- Mark a live TV category as a **favourite** (star).
- **Genres** and the **parental control** (hide adult content; showing it needs the PIN from the TV).

Changes take effect on the TV at once.

## 5. Playlists on the phone

- See all your playlists and which one is the **main list**. Switch the main list from the phone.
- **Rename** or **delete** a playlist (delete asks first).
- **Add** a playlist (see section 1).

## 6. Sport on the phone

- Browse the sports leagues and today's games. Live scores show when they are switched on in the Sports Hub on the TV.
- **Favourite teams:** choose them under "Favourite teams". The games of these teams are listed under the Favourites view.
- **Keep** (pin) a game: it then also shows as a mini result on the TV.
- **Watch** a game: the TV starts a channel that broadcasts it, if one matches.

## 7. Import data from the phone

- **Lists** from Letterboxd (ZIP or CSV), MyAnimeList (XML) and Trakt (JSON). Titles that are in your playlist go to your watch list or history.
- **API keys** for TMDB, OMDb, OpenSubtitles and SubDL. Paste them from the clipboard instead of typing with the remote. Empty fields keep the stored key. See [SUBTITLES.md](SUBTITLES.md).
- **TV guide address** (XMLTV) for the programme data.

## 8. Transfer and backup from the phone

- **Download a backup** from the TV, with or without the playlists (the file then contains your logins).
- **Restore a backup** on a new TV by uploading the file. See the transfer section in [SETTINGS.md](SETTINGS.md).
- **Crash report**: the last crash report can be read and copied on the phone, for a bug report.

## Troubleshooting

| Problem | What to check |
|---|---|
| Phone shows "Wrong code" or "locked" | Read the 4-digit code again from the TV. After 5 wrong tries, wait one minute. |
| The page does not open | Same Wi-Fi? Phone and TV on the same network, no guest network or VPN on the phone. |
| "The device is not connected to a Wi-Fi" on the TV | Connect the TV to the Wi-Fi first. |
| Button on the phone says "Error. Is the TV still on?" | The TV app must be open and running. |
| Titles do not show up | The playlist must be loaded on the TV first. |
