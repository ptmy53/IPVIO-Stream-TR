# Changelog

## Unreleased

## 1.4.8 (versionCode 14)
- Settings > General > VPN (new): connect the device through WireGuard. Choose the `.conf` file of your VPN provider, then connect and disconnect there. The video stream goes through the VPN as well. The key is stored encrypted and never shown. There is no kill switch yet. Not device tested yet, and not checked on Fire TV
- Sports: on a kick-off or score popup of your teams, "OK: go to the game" opens a channel that shows the game (switch off in Settings > Sports Hub). Not device tested yet
- Settings > Sports Hub (new page): choose the sports and competitions you follow (switched-off ones are not queried), alerts and delay, the tabs of the Sports Hub and the tab it opens on. 26 more competitions were added (English divisions and cups, Scottish, Dutch, Portuguese, Turkish and Belgian leagues, national cups, Nations League, Euro, World Cup, Libertadores, NWSL, WNBA, college basketball, LPGA, DP World Tour); they start switched off and their ESPN addresses are not verified. Not device tested yet
- Settings > General > DNS server (new): use Cloudflare, Google, Quad9 or an own DNS server (an IP address or an https address) for the app's connections. If the server does not answer, the device's DNS is used. It does not apply to the video stream itself. Not device tested yet
- Settings > Playlists: every playlist row now has only "Reload" and "Edit". "Edit" opens a window with the settings of that playlist (name, content mode, backup servers, Make main list, Delete). Changes count only when you choose "Apply". "Reload" on a playlist that is not the main list now reloads it in the background and does not make it the main list. Not device tested yet
- Settings > Playlists > Edit: every playlist has its own "Update playlist" plan (At every start, Every few hours, Daily, Weekly, By hand only). By default a playlist follows the general setting, so nothing changes until you pick one. A playlist that is not the main list is refreshed in the background only if it has its own plan. Not device tested yet
- Faster start: the texts of the chosen language (English and all other languages except German) are prepared in the background while the first screen is built, and the silent playlist refresh and the catch-up request wait a few seconds after the stored list is shown. The stored playlist is also read in the background during the start. Nothing changes in what you see. Not measured, not device tested yet
- Stalker portal messages about a portal that does not respond (with the address and MAC check hint) now show in English. For German they are unchanged. Not device tested yet
- The new English texts of the last changes (search, portal, Live TV and player messages) are translated into Turkish, Bosnian, Croatian, Serbian, Spanish, French, Italian, Dutch, Arabic and Polish. German is unchanged. Not device tested yet
- The "What's new" list and some player and settings hints (wishbox round 2) were not loaded before: they showed German in English and other languages. They are translated now. German and the existing translations are unchanged. Not device tested yet
- Some status and error lines with numbers or names (for example "OpenSubtitles: error 500", "Loading categories (2/9) …", "Search „…“ · Movies") now show in English. For German they are unchanged. Not device tested yet
- Some messages and hints that had no translation before (for example the portal, provider list and subtitle search messages, the first-start hints of the playlist import) now show in English. For German they are unchanged. For the other languages they used to show German and now show English. Not device tested yet

## 1.4.7 (versionCode 13)
- Live TV: playback over a connection without encryption (certificate does not match the address, you agreed) is repaired. The agreed host now goes straight to http instead of failing over https first before every request and every video segment, and addresses with characters such as "%", "|" or spaces are no longer re-encoded or rejected when switching to http. Reload the channel once if it still shows "connection stalled". Not device tested yet
- Playlist switch: switching back to a playlist shown before replaces the list at once from memory (no empty screen, no loading, no disk read). If the system needed the memory, or the playlist, its languages or its content mode changed, the normal switch runs. Not measured, not device tested yet
- Live TV categories: the channel column switches only after the focus rested for 0.22 s (was 0.15 s), so scrolling through the categories rebuilds the channel list less often
- Diagnostics: all messages in Settings > Diagnostics are English now (certificate, backup server, playback, memory, trailer, screen-on log)

## 1.4.6 (versionCode 12)
- Live TV: channels of providers whose live address has no "/live/" part ("http://host/user/pass/12345", also ".ts" or ".m3u8" at the end) stay in Live TV, even when their category is called "Sky Cinema", "Film" or "Movies". Before they showed up in Movies. Movie and series addresses ("/movie/", "/series/", video files) are not affected. Reload the playlist once after the update so it is sorted again. Not device tested yet
- Settings > Playlists: the refresh option "Every few hours" was shown in German in every language (it had no translation); it is now translated in all languages
- Settings > Playlists: every playlist has its own "Reload" button. On the main list it reloads it; on another list it makes that list the main list and downloads it again from the provider. Not device tested yet
- Settings: long pages now have tabs at the top, so you reach a setting without scrolling a long page. Design & color: Color theme, Background, Animations, Display size, Adjust screen, Font. Player: Playback, Intro and next episode, Clock, Network and frame rate, Second picture, Source & quality. Storage: Cache, History, Backup. Not device tested yet
- Settings > Genres & categories: all filters of the playlist are on this page now, in tabs: Content languages (what is loaded at all), Ignore groups (hide whole provider tags such as "TR |"), Show and hide categories (single categories), Automatic genres. Each tab says what it does. The language filter is no longer repeated on Settings > Playlists, which has a button to this page instead. Also fixed: the button "Open content languages" on the filter page opened Live TV & Guide

## 1.4.5 (versionCode 11)
- Live TV: scrolling through the category list is smooth. The highlight follows the focus at once, but the channel column switches to a category only after the focus has rested on it for 0.15 s. Before, holding Down rebuilt the channel list (grouping, quality bundling, all rows) for every category passed. Right or OK on a category still opens its channels at once. Not measured, not device tested yet
- Settings: the "Sublists" page (per-playlist sublist filter) is removed (T-035). Saved sublist definitions stay on the device. An active sublist filter is switched off once after the update, so playlists show all channels again. Categories can still be hidden under "Genres & categories". Not device tested yet
- Settings: the profile circle on the home screen opens the Profiles page again (it opened the sublist page). "About", "Reset all" and "Donate" each open their own page (they were shifted by one) (T-035). Not device tested yet
- Performance (T-034, slow 1.4.4): the age rating is asked for only when a title is opened (detail page). In 1.4.4 it was asked for every title that got the focus (home slideshow, billboard, rows, search, list preview), and every TMDB detail stored before counted as outdated, so each focus started new TMDB and OMDb requests. Browsing no longer fetches it, also not for new titles; the info lines show it once the title was opened. Not device tested, nothing measured
- Performance (T-034): Xtream movies and series are loaded again in parallel with the channels (as in 1.2 to 1.4.3; 1.4.4 loaded the three lists one after the other). A part the provider refuses in parallel (error code or error answer instead of the list) is fetched again on its own, so providers that refuse parallel requests still load completely; the completeness checks of 1.4.4 stay. Not device tested, nothing measured
- Performance (T-034): the stream check on the detail page (T-033) starts only after 1.5 s on the page: a quick look or an immediate Play no longer opens a second connection to the provider
- Performance (T-034): no language is read at start any more. A language is read the first time it is shown, and only that language; German needs no dictionary at all. Before, all twelve languages (about 2,300 rows each) were read at every start
- Player: a pause now keeps the screen on, so the screensaver no longer starts while a video is paused. The sleep timer still releases the screen, so the TV can go to sleep
- Diagnostics: after the stored playlist is loaded, Settings > Diagnostics > Log shows a "Start:" line with the time to read the stored list, to build the screens and to load the translations, to find out what makes the app start slowly on large playlists. Diagnostics only, nothing else changes
- Playlist setup (Xtream): live channels, movies and series load at the same time again, as before 1.4.4 (the sublist change had made them load one after the other, which slowed the first setup). If a provider refuses parallel requests (errors such as 884/885) or a part fails, only that part is fetched again, one by one, after the others have finished. Not measured, not device tested yet

## 1.4.4 (versionCode 10)
- Trailer (T-032): the Trailer button now opens YouTube at once (the app or the browser), without the in-app player: on test devices the in-app trailer showed no picture and the cause was not found. YouTube starts in the app's own task, so Back should return to the detail page (the trailer id is now checked before it goes into the address). The in-app player code (`TrailerDialog.kt`) stays in the repository, unused. Not device tested yet
- Trailer (T-032): Settings > Diagnostics shows the player's own values (playing time, volume, muted, quality) at the first "playing" and 4 s and 9 s later, to see why a trailer with player state "playing" shows no picture or sound. At the first "playing" the player is also told to unmute and set the volume to 100. Not device tested yet
- Diagnostics: Settings > Diagnostics now has a "Log" row with the last 25 messages and their time (before, each message replaced the previous one, so only the last was visible). It is part of the copied report. Not device tested yet
- Trailer (T-032): Settings > Diagnostics now shows the WebView version and which video decoders the device has (H.264, VP9, AV1, HEVC; hardware or software), because on one test device the trailer plays sound but the picture stays black. Diagnostics only, playback unchanged. Not device tested yet
- Trailer (T-032): if the in-app trailer shows no playback within 10 s, the dialog tries two more ways to load the YouTube player (other page origin, other embed host) and writes the result of each to Settings > Diagnostics ("Trailer: way N …"). If none plays, or YouTube says the video cannot be embedded, the trailer opens outside the app instead of staying black. Cause of the black picture is still unverified. Not device tested yet
- Trailer (T-032): Settings > Diagnostics now also lists load errors of the YouTube frame, SSL errors and "iframe loaded", to see why the player never starts (20 Nov: only "no playback after 15 s" was reported). Diagnostics only, playback unchanged. Not device tested yet
- Trailer (T-032): the trailer page now also reports the YouTube player's own error code, state and a 15 s no-playback timeout to Settings > Diagnostics ("Trailer: player error …"). Diagnostics only, playback unchanged. Not device tested yet
- Trailer (T-032): WebView errors are written to Settings > Diagnostics (error codes only), to find the cause of black trailers. Not device tested yet
- Playlist import (19 Nov feedback): when the playlist is too big for the device's memory, the add screen now shows a "Choose languages" button next to "Add". It opens the language selection directly, so no second press of "Add" is needed. Not device tested yet
- Trailer (#15, 19 Nov feedback): the "Open in YouTube" button gets focus when the trailer opens and the WebView no longer takes the remote's focus, so the button can be reached and pressed on Android TV. Black picture on some titles is not fixed yet (cause unverified). Not device tested yet
- Security (#69): if a provider's HTTPS certificate does not match its address, the app no longer switches to an unencrypted connection on its own. It asks once per server ("Unencrypted connection?"); only after you agree is that server reached over http, and the choice is remembered on the device. Not device tested yet
- Playlist import (#90): new button "Identifier: VLC / IPVIO" in the add screen. Some providers reject the VLC user-agent (HTTP 403); with IPVIO the playlist sends the app's own identifier. Default stays VLC. Not device tested yet
- Playlists (#111): each playlist shows its channel, movie and series counts in Settings > Playlists (after it was loaded once). Not device tested yet
- Detail page (#15): the Trailer button plays the TMDB trailer inside the app (WebView), instead of opening YouTube. Without a trailer it still searches YouTube. Not device tested yet
- Settings (#95): new option "Keep the screen awake while browsing" (default off): the screen stays awake while the app is open. Not device tested yet
- Live TV (#102): live channels whose group name says "Movies" stay in Live TV (movie groups no longer catch live addresses). Not device tested yet
- Playlists (#112): a saved playlist list that cannot be decrypted is no longer overwritten by the next save; it is kept under a separate key and the app reports it. Not device tested yet
- Donate: the Ko-fi link now points to https://ko-fi.com/ipviodev (app page and README)
- Docs: README rewritten. Free and open-source notice and a developer note at the top, quick start with the Downloader code, key features, a "Cool features" section (live sync, API keys, subtitle search, Boomer mode, dual and multi view, list import, favourites order, demo mode), remote-control tips, Reddit community link. Screenshots still the old ones (T-026)
- Docs: new `docs/SUBTITLES.md` with step-by-step keys for SubDL and OpenSubtitles (T-026)
- Docs: `docs/FAQ.md` extended (free, installing, playlists, subtitles, live sync, troubleshooting); Stalker series now described as loaded when opened (T-026)
- Docs: `docs/GOOGLE_DRIVE.md` corrected: since 1.4.3 the release APK shows the Google Drive section (T-026)
- Docs: new `docs/PHONE.md` (phone search, quick playlist setup by QR code, phone control, categories, sport, imports, troubleshooting); README and FAQ link it (T-027)
- Live sync (Google Drive) is confirmed working on the devices by the maintainer
- Detail page: a new line shows the technical data of the stream before playback – resolution, video codec, HDR10/HLG, Dolby Vision, audio tracks with language, format and channels (incl. Atmos) and subtitle languages (issue #24). The app reads only the header of the file with Media3 (`media3-inspector`, new dependency); it stops when the page closes. Can be switched off in Settings > Player > Source & quality (T-033)
- Movies and series: the info line shows the age rating, e.g. "FSK 12" (from TMDB for the country of the app language; otherwise the US rating marked "(US)"). Needs your own TMDB key; with only an OMDb key, Xtream titles show the US rating. Titles opened before load it once more (T-032)
- Release notes: every release gets user-facing notes in `docs/release-notes/vX.Y.md` (template, rule in `CLAUDE.md` section 18). `release.yml` uses the file as the GitHub release text and puts its four highlights into `update.json` `notes`, so the update dialog shows what is new; without the file it falls back to GitHub's pull request list. Notes written for 1.4.3, covering 1.4.2 (T-027)
- Live TV: channels with a live address stay in Live TV even when their category is called "Movies", "Kino", "VOD" and so on (e.g. "EU | DE | Movies and Series"); before they were sorted into Movies and hidden with "Live TV only" (T-026)
- Playlist loading: a download without a known size whose end looks cut off is no longer stored as the playlist; Xtream lists from get.php are also checked against the provider's live categories and loaded through the API when most are missing (T-026)
- Settings > Playlists: a failed refresh is shown instead of staying silent, and the new button "Reload playlist from scratch" deletes everything stored for the active playlist and downloads it again (T-026)
- Diagnostics: logcat tag `IPVIO-PL` with counts per step of playlist loading (no addresses or passwords) (T-026)
- Live TV channel list: Up on the first channel jumps to the last channel, Down on the last channel jumps to the first (issue #107). Held keys keep scrolling normally
- TV guide: Up on the first channel jumps to the last channel, Down on the last channel jumps to the first (issue #107). Held keys keep scrolling normally
- Movies and series filter: the language filter has its own icon (speech bubble) instead of the country globe (issue #26)
- Private categories (T-028): press the menu key (or hold OK) on a category in Live TV, Movies or Series and choose "Make private". Titles from a private category show only inside that category: they are not added to Recently watched, Continue watching or the resume position, and they do not appear in "All channels" / "All movies", genre lists, search (TV and phone), the home rows, Multi view or Boomer mode. Marking a category removes its titles from history and Continue watching. Settings > Parental controls lists the private categories ("No longer private") and has an option to treat parental-flagged categories as private (off by default). Part of the backup. Not device tested yet

## 1.4.3 (versionCode 9)
- Live sync (Google Drive): favorites and history follow your devices automatically. Switch it on in Settings > Storage > Google Drive on each device; it is off by default. Upload a few seconds after a change, download when the app is in front (about every minute). Merged on the device, a safety copy is kept before each merge. Not device tested yet
- Live TV: a provider category can float like in Movies and Series ("Mit Hoch/Runter verschieben"): Up/Down move it, OK or Back puts it down; new menu entry "Ans Ende verschieben" (move to the end)
- Live TV: BACK in the channel list goes to the chosen category, like Left; BACK from a stream returns to the channel list with the last channel and category; OK and Right enter the channel list again after returning to the categories (Fire TV focus fix)
- Search: the background image is darker behind the info text, and the results column lines up with the search field (including the Sports Hub button)
- Update card: smaller, placed at the bottom right so it does not cover the content
- Build: the release build receives the Google client keys from the GitHub secrets GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET

## 1.4.1
- Fix: 1.4 did not open after the update on user devices. R8 over the whole app (T-024) is switched off again (`enable = false` in app/build.gradle.kts, the rollback from ADR 0005); versionCode 6. Cause not confirmed by a crash log (unverified). Install 1.4.1 over 1.4; data is kept (same signature)

## 1.4
- Sport: the tab no longer matches games to channels on the main thread; the channels of a game appear shortly after the page. Not measured (T-023)
- Performance (not measured): home poster cards no longer compute their screen position on every scroll frame (T-020); small devices decode two images at a time instead of four (T-021); the settings files needed at start are loaded in parallel (T-022)
- Live TV: moving the focus through the channel list no longer rebuilds the content of the rows (guide times, status icons); only the highlight frame changes. Not measured (T-019)
>- Settings > Playlists: switching the content mode no longer jumps back to the first settings page. When the switch re-read the stored playlist copy, the loading screen briefly replaced the settings; it now stays on the settings tab unless a language choice is waiting
- Remote: a long OK (menu on channels, categories and favorites, marking an episode as seen) now triggers after 0.35 s instead of the system long press (about 0.5 s or more)
- Playlist refresh: existing installs that still use "at every start" switch once to "daily"; choosing "at every start" again later is kept
- Donate page and README: new support text, Ko-fi and PayPal links in the README
- Settings: the reset button at the bottom of each settings page (General, Language & region, View, Design, Menu, Player, Sound & subtitles, Genres, Live TV) now resets only that page. Before, every page ran the same global reset, so resetting for example Genres also reset the design. The design page's own "Design zurücksetzen" button is replaced by the page reset, which also covers font, text size, clock and screen margin
- Settings: new page "Alles zurücksetzen" (Reset all) with a confirmation dialog; it resets all settings at once and replaces the reset button in "Über IPVIO Stream". Playlists, favorites, history, resume positions, API keys, the parental-control PIN, the custom XMLTV address and the reminder list are not touched
- Favorites: sort your favorite channels, movies and series by hand. In Favorites (and in Live TV > Favorites) press the menu key or hold OK on an entry and choose Move up / Move down / Move to top; the focus follows the entry. In the Favorites tab this menu also holds "Remove from favorites" (before, the menu key removed it at once). The order is saved with the favorites, part of the backup, and used for zapping (T-018)
- Onboarding: "Weiter ohne Playlist" now opens a demo mode with sample channels, movies and series so the UI can be explored without connecting anything; artwork is replaced by neutral placeholder blocks, no images or metadata are requested, playback is disabled, and nothing is saved (leaves when a playlist is added)
- App update: the app reads `update.json` from the latest release first (GitHub API as fallback; a custom address still works). Before the installer opens it checks the SHA-256, the package name, the version number and that the new APK is signed with the same key as the installed app, and shows a clear message instead of Android's "App not installed". `update.json` can mark an update as required (T-015, not device tested yet)
- First start: a small card at the bottom checks for an app update once and shows the result (checking / up to date / update available / error with "Retry"), with the switch for the automatic check. It appears only once and only on a start without a playlist, never takes the focus from the add-playlist screen, and works with the TV remote (T-014)
- First start: the update check card now closes by itself as soon as a playlist is added (loading starts) or "Weiter ohne Playlist" is chosen; it no longer stays on top of the home screen. (T-025)
- Favorites: sort your favorite channels, movies and series by hand. In Favorites (and in Live TV > Favorites) press the menu key or hold OK on an entry and choose Move up / Move down / Move to top; the focus follows the entry. In the Favorites tab this menu also holds "Remove from favorites" (before, the menu key removed it at once). The order is saved with the favorites, part of the backup, and used for zapping (T-014)
- Onboarding: "Weiter ohne Playlist" now opens a demo mode with sample channels, movies and series so the UI can be explored without connecting anything; artwork is replaced by neutral placeholder blocks, no images or metadata are requested, playback is disabled, and nothing is saved (leaves when a playlist is added)
>- Internal: player helper code split from Media3 (no user-visible change).
- Build: the release build uses R8 again, now over the whole app, with the app's own code kept unchanged and no renaming or repackaging of classes. Expected: smoother UI and a smaller APK. Rollback is `enable = false` in `app/build.gradle.kts` (T-024)
- Search: the query box above the app keyboard can now be selected; OK or a tap opens the device keyboard for typing
- TV guide in the player: a playback error box (for example "check username, password or subscription") no longer sits on top of the guide
- TV guide search: channel hits show the show running right now with its times (also for providers without XMLTV, via the guide query)
- Live TV: when a playlist has no live channels, the empty list points to Settings > Playlists (content mode)
- Movies, series and home: when a playlist has none (content mode "live TV only"), the empty page points to Settings > Playlists
- Live TV: the tab always opens on "Start" (home) instead of the last category or "Alle Sender"; the hint above also shows there
- Search: Back from a selected result goes to the input (keyboard); Back again opens the sidebar, also when the Search entry is hidden in the menu (the default) (T-013)
- Backup: dual view, new-episode marks, imported lists and the resume log are now part of the backup export and restore (T-006)
- Playlist refresh defaults to "daily" instead of "at every start" for users who never chose a mode; the first start still loads once (T-009, refresh default)
- Live TV, movies and series: sort categories by provider order, A-Z or an own order; move categories with the menu key or a long OK (T-011)
- Player: search subtitles online (OpenSubtitles and SubDL) under Audio & subtitles; the chosen subtitle is attached while playing and remembered per title. Needs your own API keys (Settings > API keys) (PR #9)
- Settings > Playlists: switching the content mode (live TV only / movies & series only) keeps the settings page open instead of showing the empty-list screen, and cancelling the add-playlist screen no longer reloads a playlist that is only empty because of the mode
**Playlists (T-009)**
- Xtream: any number of backup server addresses per login (same username and password). If the main server stops answering, the app switches to the next one by itself, for the playlist, TV guide, catch-up and streams
- Each server shows whether it is reachable or since when it is not; the playlist row in Settings > Playlists lists servers that are down
- Backup servers are included in the backup export and import
**Playlists (Stalker)**
- Stalker/Ministra portals load movie and series categories on demand: the first load fetches all channels plus the newest page of each category, further titles come when a category is opened, previewed or scrolled (PR #7)
- Stalker: movies with their own files (`has_files`) play, relative covers are shown, channels in movie-named genres stay in Live TV (PR #7)
- Stalker: a portal that only echoes the requested command no longer yields a broken play address, and a portal that ignores `movie_id` no longer plays the first movie of a category instead; movie and series categories with the same name keep separate title counts (follow-up to PR #7)
- Stalker: movies and episodes play on more portals: several `create_link` request forms, addresses from `url`, relative paths or `play_token`, file lookup for episodes, a new login when the portal rejects the session, a short wait on HTTP 429; when nothing plays, the error shows a masked summary of the portal's answer (Stalker part of PR #19)
- Stalker: a `create_link` answer with an error (e.g. `nothing_to_play`) or a bare video club command no longer counts as a play address, so the file lookup still runs; a file list that is really a whole category no longer starts another title; after a timeout, "429 Too Many Requests" or the account's stream limit no further request forms are sent; live channels log in again once after a failed start, as before PR #19 (follow-up to PR #19)

**Playlists (T-008)**
- Xtream logins loaded via the API: the loading screen now counts movies and series while they download. Before, it stood still at the size of the channel list (often "256 KB") until everything was there

## 1.3
**Playlists and loading**
- Large playlists load faster: reading, unpacking and parsing now overlap, and the category and quality checks are cheaper
- Providers whose certificate does not match their address ("certificate is for ...") no longer fail: for this one error the app tries the same address over http once and remembers the host. Every other certificate error is still refused
- Stalker portals: optional serial number and device IDs (as in TiviMate or STB Emu) for providers that ask for them; empty means the old behaviour
- The setup screen is more compact (everything visible at once, no animated scrolling) and offers "Continue without playlist" on the first start

**Playback**
- Short network drops are retried up to 6 times with a growing pause before an error is shown (no retries where they cannot help: wrong login, subscription or address)
- After a stall, playback resumes only with a few seconds in the buffer, so it does not stall again right away

**Watched marks and search**
- Mark a movie, season or whole series as watched or unwatched (also per episode: hold OK or the menu key); single titles can be removed from "Recently watched"
- Option "Device keyboard in search" in the settings (other languages, voice input, paste)

**Updates**
- "Update available" dialog at app start with one button to download and install, also on the "add playlist" screen. "Later" hides it until the next start

## 1.2
**Removed**
- Optional backup to Google Drive (sign-in with a code). Planned again for 1.4 as LiveSync. The old code is in tag V1.1

**Stalker portals**
- More robust login (several portal address variants, tolerant JSON), fallback paging for channel lists, clearer error messages
- Series support (own series area and series inside VOD); the second stream can play Stalker channels

**Sports hub (rebuilt)**
- Sports sidebar, day tabs, game cards with scores (unofficial ESPN data, can be switched off), matching of games to your channels, leagues on and off
- Favourite teams, alerts when a favourite team plays or scores (with optional delay), mini scoreboard in a corner (position, size, logos or names, game time)
- Long press a channel to correct which channel belongs to a game

**Live TV**
- New info bar when zapping (logo, LIVE, number, channel, quality, show, time left, progress, next)
- Genre filters (Sport, Movies, Documentaries, Kids, News, Music) and a "Start" page
- Quality and frame rate shown in the channel list (learned while watching)
- Hide live categories (favourite key in Live TV, "Hidden" at the bottom of the list)
- Ignore groups by country or language tag (Settings > Genres & categories), effective at once

**Interface**
- Seven ready-made designs, one click each
- Clock next to the profile icon (can be switched off)
- "Fit to your screen": margin for sides and top/bottom (0-10 %)
- Own on-screen keyboard in search with live results while typing
- Switch the playlist from the header when there are two or more
- On phones the app stays in landscape; the "add playlist" screen and the player button row can be swiped

**Boomer mode**
- Mode for older relatives, set up by someone else (Settings > Boomer Mode, PIN of the parental control): big channel tiles, only left/right zaps in the player, hidden exit (hold Back for 10 seconds, then PIN), start after boot
- Look is adjustable with a live preview (card size, text size, highlight colour, border, animations, logos) and the tiles can be chosen by hand

**Backup and search**
- Backup can be saved to and loaded from a file (Settings > Backup); more settings are included
- Phone search shows "Continue watching" when the search is empty
- Playlist download identifies itself as a common player (some providers slowed unknown clients)

## 1.1
**Updates and your data**
- In-app updates from GitHub Releases: the app checks once a day and shows "Update available" in the bell; one click downloads and installs
- Release signing via `keystore.properties` (see docs/RELEASE.md) so updates install over the old version and keep history, favorites and settings

**Playlists**
- Xtream: if `get.php` fails or returns no playlist (e.g. custom error codes like 884), the app loads the account through `player_api.php` automatically
- Several playlists at the same time: "Also show" in Settings > Playlists; movies and series merged (duplicate movies once, same series as one series with several sources), "Source" filter in Movies/Series, source switch in Live TV
- Stalker/Ministra portals (portal address + MAC): live channels and movies
- M3U file from the device (file picker) or uploaded from the phone page

**Live TV**
- Rewind live TV (optional on-device buffer, 256 MB – 2 GB), pause without missing anything, "Back to live"
- Record the current show (from the buffer, no second connection), storage internal or USB, recordings list in Live TV
- Endless reconnect for live streams, automatic reconnect when the server ends a stream
- New "Sports" section: sports live now and coming up (from the TV guide), all sports channels

**Player**
- MPEG-DASH (.mpd) streams; unsupported formats show a message instead of crashing

**Interface**
- View options moved from the header to Settings > View, with a live preview of card size and artwork
- Sidebar on the left, right or top; icons only or always with text
- Phones: landscape by default, tap for controls, double-tap left/right to skip

**Sports, playlists, second stream**
- Sports hub with filters: sport (football, basketball, tennis, F1, MMA, ...) and time (now, today, tomorrow, next 48 h)
- In the Live TV list you can switch the playlist (or "All playlists") without going into the settings
- Second stream in the live player: picture-in-picture or split screen, sound from either stream, swap streams. Arrangement, window size (4 sizes) and corner are saved (Settings > Player > Second stream)

**Phone search**
- New button at the bottom of the sidebar: shows a QR code. Scan it and search movies, series and live channels on your phone (a web page served by the TV, same Wi-Fi, nothing to install). Tap a result to play it on the TV; pause, skip, stop and volume buttons included

**Languages**
- Dutch (Nederlands) is the 10th language

**Metadata**
- Own API keys for TMDB and OMDb in Settings > API keys
- Without keys: description, cover, rating, genres, running time and trailer straight from the Xtream provider
- OMDb ratings also without TMDB (lookup by title and year)
- Rating order for sorting and "Top rated": IMDb, Rotten Tomatoes, TMDB, provider

**Interface (new)**
- Settings > View rebuilt: 5 tabs (cards, sidebar, grid, home, categories) with a live preview of every option
- New "Donate" card in the settings (PayPal link and QR code)

**Transfer**
- Transfer history, favorites and settings to another device over Wi-Fi (optionally with playlists)
- QR codes: scan instead of typing the address (add playlist from phone, Wi-Fi transfer, Google sign-in)
- Settings > About: optional donation link (PayPal) with QR code
- Much faster loading of large Xtream accounts: series episodes are fetched when a series is opened instead of 50,000 requests while loading
- Optional backup to Google Drive (sign-in with a code, see docs/GOOGLE_DRIVE.md)
- Read the last crash report on the phone (no clipboard needed)

## 1.0 - first open-source release
- Android TV / Google TV / Fire TV IPTV player for Xtream Codes and M3U playlists
- Home, Movies, Series, Live TV with TV guide and catch-up
- Cinema and grid browse views, filters, search with typo tolerance
- Player with audio/subtitle selection, FFmpeg audio decoder, skip intro/credits, source fallback
- Profiles, parental controls, history, my list, favourites, backup and restore
- 9 languages, 12 colour themes
- Licensed under GPL-3.0
