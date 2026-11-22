# T-049 – Batch of wishes: sports hub, VPN and DNS, Trakt

Status: **T-049a, c, d and f implemented** (decided by the maintainer on 22 Nov 2026: sports alert, more leagues and the Sports Hub settings tab, own DNS). T-049b needed little (see below). **T-049e VPN implemented** (WireGuard; the maintainer left the choice to the assistant: "the most common solution"). **Trakt (g) not started.** Not built with Gradle, not device tested. Source: the maintainer's list of 22 Nov 2026 (user requests, one marked as from a Reddit user). This is several features, not small fixes, so it is split into subtasks that can be built and tested one by one.

## What exists today (read in the code, nothing run)
- Sports hub: `SportHub.kt`, `SportLeague.kt`, `SportData.kt` (ESPN public but unofficial endpoints `site.api.espn.com`, league list with paths), `SportAlerts.kt` (popup when a favourite team is about to play, kicks off or scores), `SportWatch.kt` (finds channels for a game: manual assignment, TV guide, channel name), `SportLogic.kt` (13 sport categories from keywords: `CATS`).
- Trakt: plans only (`T-030` import postponed, `T-031` live sync, status plan, no code). Needs a registered Trakt API app (client ID and secret) from the maintainer.
- VPN or DNS: nothing in the code (grep found no WireGuard or resolver code).

## Subtasks

### T-049a Sports: goal alert opens the game
- The goal popup in `SportAlerts` gets an action: OK jumps to a channel that shows the game (same matching as `SportWatch`), if there is one; otherwise it opens the match page. Only for tracked leagues or teams.
- Risk: low (UI, no storage). Needs a device test with a live game.

### T-049b Sports: find channels for a game
- A button on the match page that lists the channels `SportWatch` finds (hand-picked, guide, name) and a filter for the channel list (provider category, quality). Mostly present; the gap is a visible entry point and the filter. Needs the maintainer's view of what "filters" should be (see questions).

### T-049c Sports: many more leagues and sports
- ESPN has many more paths than the app lists (UK football tiers, Scottish, rugby, cricket, darts-like events are partly missing). Adding paths to the league list is low risk. Rows must be verified one by one against the live endpoint; the session cannot reach ESPN reliably, so each new path is **unverified** until the maintainer's device shows data.
- "Sky Sports leagues": Sky is a broadcaster, not a data source. What can be done: add the leagues Sky shows (Premier League, EFL, Scottish Premiership, F1, golf, cricket, rugby, boxing, darts) and match Sky Sports channels by name for `SportWatch`. No Sky API exists for this.
- "As much as you can find online through APIs": a second data source (for example TheSportsDB, free key with limits) is a new network dependency with its own terms and rate limits. Needs approval; recommended only after ESPN coverage is exhausted.

### T-049d Sports hub: filters, favourite sports, customise
- Filter options in the hub (by league, by day, favourites only, live only).
- Favourite sports and a customisable hub (order and hide sections, like the home page builder `HomeLayout`). New SharedPreferences keys (`ipvio_sport…`), additive.
- "Remove sport types completely" is unclear (see questions): remove the sport categories (`CATS`) from the hub and the guide filter, or only from one screen?

### T-049e VPN with WireGuard
- Needs `VpnService` (new manifest service and permission), the WireGuard tunnel library (`com.wireguard.android:tunnel`, Apache-2.0, compatible with GPL-3.0; **new dependency, needs approval**), a place to import a config (file or phone upload; the private key is a secret: stored only in encrypted preferences like `Secrets`, never logged, CLAUDE.md section 10), and a switch plus status in Settings.
- Fire TV and Android TV: the system VPN consent dialog must work with a remote; some Fire OS versions limit `VpnService`. **Unverified**, needs the device.
- Large change in network and security. Recommended to be its own pull request and release.

### T-049f Own DNS
- A DNS server of the user's choice for the app's own connections: an OkHttp `Dns` implementation with a configurable resolver (plain DNS over UDP/TCP or DNS over HTTPS). Applies to playlist, EPG, metadata, and playback requests that go through OkHttp; ExoPlayer's data source must use the same client (to be checked in `PlayerNet.kt` and `Net.kt`). Independent of the VPN, smaller, and useful alone (blocked provider hosts, privacy).
- With an active VPN the VPN's own DNS setting applies to the whole device; the two settings must not fight (decide: app DNS wins for app traffic).
- New dependency only if DoH is built with `okhttp-dnsoverhttps` (same OkHttp group, **needs approval**); plain DNS needs none.

### T-049g Trakt: mark watched VOD
- Depends on T-031 (login and sync). First step: OAuth device login and "mark watched" for movies and episodes from the app to Trakt (scrobble or history add) and the reverse on the detail page. Needs the Trakt API client ID and secret from the maintainer (secrets handling, section 10) and the official API documentation, which the earlier session could not reach (**unverified**).

## Suggested order
1. T-049a, T-049b (small, no new storage or dependency).
2. T-049c with ESPN paths only, then T-049d.
3. T-049f (DNS, plain first), then T-049e (VPN), each its own release.
4. T-049g after the maintainer registers a Trakt app.

## Decisions needed
See the questions asked in the chat (22 Nov 2026).

## Risks
- Nothing here can be built or run by the assistant; all new data sources and the VPN need device tests.
- New third-party endpoints and a VPN library change the privacy and support picture of the app; both need the maintainer's decision and a mention in `docs/SECURITY.md`.

## Built (22 Nov 2026)
- **T-049a** `SportAlerts.kt`, `MainActivity.kt`: kick-off and score alerts carry their game. While one is on screen and a channel is known for the game (`SportWatch`, same matching as Watch Live), the popup shows "OK: go to the game" and OK opens that channel (`AppNav.openChannel`). The OK key is only taken while such a popup is visible. Can be switched off in Settings > Sports Hub.
- **T-049b** Watch Live (`WatchPage`) already lists the channels for a game with the reason (hand-picked, TV guide, channel name), availability and a manual assignment. No new filter was built; say which filter is missing.
- **T-049c** `SportData.kt`: 26 more competitions (English divisions and cups, Scottish, Dutch, Portuguese, Turkish, Belgian leagues, Conference League, national cups, Nations League, Euro, World Cup, Libertadores, NWSL, WNBA, college basketball, LPGA, DP World Tour). Their ESPN paths are **unverified**. They start switched off (`SportLeagues` seed), so nothing new is queried until they are switched on. Sky Sports is a broadcaster, not a data source; the leagues Sky shows are covered by these ESPN entries and the channel matching by name.
- **T-049d** New page Settings > Sports Hub (`SportSettings.kt`, `SportHubPrefs.kt`, `SportHubLogic.kt`): sports on/off (switches all leagues of a sport), each competition, live results on/off, alerts (on/off, delay, OK jumps to the game), score overlay, which hub tabs are shown, start tab. New file `ipvio_sportset` (profile-scoped, added to the backup list). The settings page numbers moved: About is 19, Reset all 20.
- **T-049f** `DnsLogic.kt` (pure, DNS messages and server list), `AppDns.kt` (OkHttp `Dns`), `NetSettings.kt` (Settings > General > DNS server: presets Cloudflare, Google, Quad9 or an own IP address or https DoH address, a test button). Applies to all OkHttp traffic (playlists, TV guide, metadata, sports, updates). The system DNS is used if the chosen server does not answer. New file `ipvio_dns` (device-level, not in the backup).
- Not covered by the DNS setting: the video stream itself (the player uses its own HTTP stack with the system DNS; routing it through OkHttp needs `media3-datasource-okhttp`, a new dependency) and traffic through a VPN.

## Tests
- JVM (Kotlin 2.0.21 compiler of the Gradle install, JUnit 4.13.2): `DnsLogicTest` (8), `SportHubLogicTest` (8), with the earlier tests 67/67. Not run: Gradle build and the Android files (`AppDns.kt`, `NetSettings.kt`, `SportSettings.kt`, `SportHubPrefs.kt`, `SportAlerts.kt`, `SportHub.kt`, `SettingsHub.kt`, `Net.kt`), device tests.

## Device test steps (maintainer)
1. Settings > Sports Hub: switch all sports off except football, open the hub: only football is listed, tabs you hid are gone, the start tab opens first.
2. Switch on a new competition (for example Championship): games or an empty list; tell which paths show nothing.
3. Follow a team, wait for a kick-off or score popup: "OK: go to the game" shows if a channel matches; OK opens it. Without a match no hint and OK works as usual.
4. Settings > General > DNS server: pick Cloudflare, press "Test DNS" (OK with an address). Load a playlist; then enter a wrong address: the app still loads (fallback).
5. An https address such as https://dns.google/dns-query works for the test.

## T-049e VPN (22 Nov 2026)
- **Choice:** WireGuard. It is the common protocol of consumer VPN providers (they offer `.conf` files for it), there is an official Android library (`com.wireguard.android:tunnel`, Apache-2.0, compatible with GPL-3.0), and the app needs no server of its own. OpenVPN was not chosen: its Android library is GPL-2.0 and the app is GPL-3.0. The maintainer delegated the choice; the new dependency (CLAUDE.md section 6) is covered by that decision.
- **Built:** `AppVpn.kt` (connect, disconnect, stored configuration), `VpnLogic.kt` (checks of the `.conf` text, pure), `VpnSettings.kt` (Settings > General > VPN: choose the file, connect, disconnect, delete). The configuration is stored **encrypted** with the device key store in `ipvio_vpn` (device-level, not in the backup); without a key store it is refused, never stored in plain text. The key is never shown or logged. Keep rule `-keep class com.wireguard.** { *; }` for R8 (JNI).
- **Not built:** kill switch, start with the device, per-app routing, several configurations, a built-in VPN provider.
- **Effect:** while connected, all traffic that goes through the VpnService leaves through the tunnel, the video stream included (this also covers what the DNS setting does not).
- **Unverified:** the library version (`1.0.20230706`) and its API are from memory and not built here; the system VPN consent dialog with a remote control and the VpnService on Fire OS; extra APK size (native Go library per ABI); whether `minSdk 24` needs core library desugaring. The CI build is the first check.
- **Tests:** `VpnLogicTest` (5) passed on the JVM. Not run: Gradle build, device.
- **Device test steps:** 1. Settings > General > VPN: choose a `.conf` from a provider (USB stick or the file picker). Wrong file: a clear message. 2. Connect: the system asks once for permission; "VPN connected"; check the public IP in a browser app or a "what is my IP" site. 3. Play a live channel: it plays through the VPN. 4. Disconnect: IP back to normal. 5. Delete the configuration. 6. A release build starts (R8 keep rule).

## Open
- **T-049g Trakt (mark watched VOD): open, postponed by the maintainer (22 Nov 2026).** Needs a registered Trakt API app (client ID and secret) and the plan of `T-031`.
- VPN: no kill switch, no start with the device, no per-app routing.
- Sports: the ESPN paths of the 26 new competitions are unverified; a possible filter for the channel list of a game (T-049b).
