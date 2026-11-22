# T-027: GitHub issue batch (19 Nov 2026)

**Status:** all items below are implemented or analysed. **Nothing was built with Gradle and nothing was tested on a device.** Pure logic was run on the JVM (Kotlin 2.0.21 compiler, JUnit 4.13.2). Device tests are pending for the maintainer.

Branch: `claude/jolly-ride-mr9tgm`.

## Status per issue

| Issue | Topic | Status | Commit | JVM tests |
|---|---|---|---|---|
| #69 | No silent http fallback on certificate mismatch | Implemented: consent dialog, stored agreement | `0bad579`, `7fdda3c` | `TlsFallbackLogicTest` 8/8 |
| #90 | Per-playlist user-agent | Implemented: "User agent: VLC / IPVIO" in the add screen | `40faa51` | `PlaylistUaLogicTest` 4/4 |
| #111 | Counts per playlist | Implemented: stored at load, shown in settings | `ff33a00` | `PlaylistCountLogicTest` 4/4 |
| #15 | Trailer inside the app | Implemented: WebView embed of the TMDB trailer | `06030c9` | `TrailerLogicTest` 3/3 |
| #95 | Keep the screen awake while browsing | Implemented as an option (default off) | `a8c752b` | none (flag logic reused) |
| #102 | Live channels in a movie group | Implemented: live address stays live | `9086e1b` | `LiveUrlLogicTest` 4/4 |
| #112 | Playlist vanishes | Partly: unreadable list is no longer overwritten | `bc9e0a9` | none |
| #82 | PIN could be changed without the current PIN | Implemented: "Change PIN" asks for the current PIN first (removing it already did) | see git log | none (UI) |
| #22 | "Recently added" by the provider's date | Implemented (approved 19 Nov): cache format version 6, movies only | `cb9bb32` | RecentSortLogicTest 3/3, PlaylistCacheAddedTest 2/2 |
| #75 | Stalker box model | Implemented (approved 19 Nov): "STB <model>" button, default MAG250 | `c5c093a` | StalkerModelLogicTest 6/6 |
| #107 | TV-guide wrap-around | Reported as done by the maintainer (19 Nov) | — | — |
| #68 | Signed release APK with provenance in CI | Decision: no workflow change without a separate approval. Proposal below | — | — |
| #105 | Notice for new episodes | Implemented (approved 19 Nov): system notification, bell kept. Replaces "Continue watching" idea | `a1f951a` | none (Android) |
| #96 | Provider subcategories ("FR - SPORTS", "FR - AMAZON") | Implemented: pattern analysis (`GroupPatternLogic`) and folder rows "FR  ›" in the provider categories; a folder opens the original categories, Back leaves it. NOT compiled, not on a device | this commit | GroupPatternLogicTest 5/5 |
| #103 | Second EPG source | Plan only (approved 19 Nov): `docs/tasks/T-028-epg-second-source.md` | — | — |
| #88, #21, #99 | Arabic, full UI with RTL | Plan only, staged (approved 19 Nov, full UI): `docs/tasks/T-029-arabic-full-ui.md` | — | — |
| #28 | Resume after leaving a film | No code change: progress is saved and the detail page offers resume | — | — |
| #73 | "Content too large" with one language | Analysed, no change (see below) | — | — |
| #108, #104 | "Connection lost" during playback | Analysed, no change (see below) | — | — |

## #83 (24/7 and adult categories hidden)

Not changed. 24/7 channels fall into the "unknown" language bucket and adult groups into the "xxx" bucket (`LangTagLogic.of`, `ParentalLogic.isAdult`). Which switch the reporter means ("filter off") and where the bucket is dropped is unclear from the code. Needed: the switch's name in the screen and the group name of a hidden channel.

## Open points and risks

- **#112:** the cause is a hypothesis from the code (key lost, the encrypted list cannot be read). A device log would confirm it. Recovery is not automatic: the kept copy (`lists_enc_unreadable`) is only saved, not restored.
- **#90:** series episodes now use the user-agent of the active playlist (`LazySeries` → `PlaylistStore.xtreamApi(ctx)`). The series belong to the active playlist; a series from another playlist of the same kind may use the other playlist's setting.
- **#102:** this changes the IPTV classifier (protected area, CLAUDE.md section 5). A live channel with an episode pattern in its name (S01E01) is now a live channel, not a series.
- **#111:** counts show only after the next load of a playlist with this version. They reflect what was loaded (language and mode filters).
- **#15:** D-pad control of the embedded YouTube player on Fire TV is unverified. YouTube may refuse embedding for some videos (error page).
- **#95:** unverified that `FLAG_KEEP_SCREEN_ON` also blocks the Fire TV screensaver (not only the screen timeout).
- **#69 and #108/#104:** the consent dialog appears after a certificate error. For a movie or episode the player retries a few times; if the user agrees during those retries, playback continues. Otherwise the error remains. Check whether a provider's episode host has a certificate mismatch (see logs).
- **#73:** the message "The selected content is too large for this device's memory" comes from the memory budget of the app (`LoadLogic.budget`, based on the maximum heap), not from free storage. The check cannot be judged without the device's memory limit and the playlist type. A diagnostic line now shows the memory budget, the maximum heap and the number of hidden languages when a list is refused without asking (Settings > Diagnose). Needed: that line, the device model, the playlist type (Xtream, Stalker, M3U).
- **#108 and #104:** no logs yet. Possible causes to check: the change in `TlsFallback` (#69, a certificate mismatch on a stream host), the R8 build of 1.4.3 (T-024, not confirmed), the retry rules (`IptvLoadErrorPolicy`, `PlayerScreen.onPlayerError`). Logcat from the moment of the error is needed.

## Regression check

All 41 pure JVM test classes that compile without Android (281 tests) pass, after the batch. Tests that need Android, OkHttp or org.json were not run.

## Test steps for the maintainer

1. **#69:** see `docs/tasks/T-026-tls-consent.md`.
2. **#90:** add an Xtream or M3U playlist with "User agent: VLC" (default) and check it loads as before. Then add a second one with "User agent: IPVIO" and check it loads (the provider that gave 403 is the test case).
3. **#111:** open Settings > Playlists after a load: each playlist shows "Live TV: n · Movies: n · Series: n".
4. **#15:** open a movie with a trailer (TMDB key set): Trailer plays in the app; Back closes it; no sound after closing. A movie without a trailer opens the YouTube search as before.
5. **#95:** Settings > "Keep the screen awake while browsing" on, leave the app idle on a screen for longer than the screensaver delay: the screen stays on. Off: as before.
6. **#102:** in a playlist with a "Movies" group that contains live channels (e.g. HBO Comedy): the channel is in Live TV, not under Movies.
7. **#112:** no test possible without the broken state. Check that a normal load, saving and restarting still keeps the playlists.

## #68 (release provenance) — decision

The maintainer left the choice to the assistant. CI workflows need approval (CLAUDE.md section 6), so nothing was changed. Proposal for a later, separately approved change: a tag-triggered workflow that builds the signed release APK, `actions/attest-build-provenance` on it, and the SHA-256 of the signing certificate in the README.

## Decisions (19 Nov 2026, maintainer answers)

- **Arabic and Polish:** PR #116 (`feat/i18n-ar-pl` into `dev`) with the translations from #93 and #106. #93 and #106 stay open. Xtream part of #106 is not included.
- **#10 MKV seeking:** approved as "umsetzen", but NOT implemented in this session. The fix changes the media source for every MKV, and it cannot be tested here. Plan: a wrapper around the Matroska extractor, active only for files without cues, tested on a device first. Still open.
- **#24 stream info:** not now (no ffprobe dependency).

## Wishes

- #110 Trakt: the maintainer prefers the live API sync (T-031) over the one-time ZIP import (T-030, postponed).

## Not done

- Automatic restore of an unreadable playlist list.
- Logs for #108, #104, #112 and the device memory data for #73.
