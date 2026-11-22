# T-026 – Live TV shows only some categories, cut-off playlist kept as valid cache

## Goal
Maintainer report (13 Nov 2026, after a reset and the update to 1.4.1): Live TV shows only the categories "AM | CA" to "AS".
Switching the language filter ("Deutsch" or all languages) changes nothing, "EU | DE | Movies and Series" is missing, and refreshing
the playlist seems to do nothing.

## Causes found in the code
1. `M3uParser.classify` (`Models.kt`): a channel became a movie when its category name contains film/movie/vod/kino/cinema, even with
   a live address (`/live/…ts`). "EU | DE | Movies and Series" therefore landed in Movies, and with "Live TV only" it was hidden.
2. `PlaylistStore.downloadList`: completeness was checked only when the server sent a size. `get.php` often sends none; a connection
   the provider closes early then looks like a normal end, and the cut list was parsed, cached (`pl_*.bin`) and stored as `pl_*.m3u.gz`.
   (Likely cause of "AM | CA" to "AS"; **unverified** until the maintainer's logcat or channel count confirms it.)
3. Changing languages re-reads the cache or `pl_*.m3u.gz`. Both come from the same download, so missing parts cannot come back.
4. `PlaylistStore.loadInner`: when a refresh failed while the old list was shown, the error was dropped (`error` only set when the
   list was empty), so a failed refresh was silent.

## Changes
- `Models.kt`: `isLiveUrl` (address with `/live/`, not a video file) is always `Kind.LIVE`. Replaces the Stalker-only rule.
- `LoadLogic.kt`: pure helpers `tailLooksCut`, `groupTitle`, `normGroup`, `missingCategories`.
- `PlaylistStore.kt`:
  - downloads without a size are rejected when their end looks cut (`IncompleteDownload`, retried like a lost connection,
    final message `MSG_INCOMPLETE`); a cut file is never parsed or stored.
  - Xtream `get.php` without a size: live categories compared with `get_live_categories`; if most are missing, or the end is cut,
    the API path is used (JSON fails on a cut instead of looking short).
  - `refreshError`: a failed refresh is now shown in Settings > Playlists.
  - `reloadFromScratch`: deletes cache, stored whole list, raw/part/info files and the remembered "use API" flag of the active
    playlist, releases the old list and downloads again. Playlist entry, name, backup servers, content mode and language selection stay.
  - diagnostic logs (`PlaylistDiag`, logcat tag `IPVIO-PL`): numbers and category names only.
- `XtreamApi.kt`: `liveCategoryNames`.
- `SettingsHub.kt`: refresh error line, button "Reload playlist from scratch" (with confirmation) and an English hint.
- `DictM.kt`: 3 rows (incomplete message, refresh error, button label).
- Storage format unchanged (cache version stays 5, no new files or keys).

## Acceptance criteria
- A live channel in a category with "Movies" in its name appears in Live TV.
- A cut-off download is never stored as cache; the old list stays and an error is shown.
- "Reload playlist from scratch" downloads the active playlist again from scratch.

## Risks
- Behaviour change: 24/7 movie channels with live addresses move from Movies to Live TV.
- A complete list whose last line is a tag (`#…`) would be rejected; not seen in practice (unverified).
- The category check costs one extra small request and one pass over the file, only for Xtream `get.php` without a size.
- Old caches with wrong kinds stay until the next refresh or the new button.

## Test results
- Ran on the JVM (Kotlin 2.0.21 compiler from the Gradle distribution, stand-in for JUnit): `PlaylistCompletenessTest` 5/5,
  `LoadLogicTest` 12/12, `PlaylistCacheTest` 7/7, `DictConsistencyTest` 3/3.
- **Not run:** Gradle build (`PlaylistStore.kt`, `SettingsHub.kt`, `PlaylistDiag.kt` use Android/Compose and were not compiled), device test.

## Status
**Done, open for device test.** Implemented on branch `fix/playlist-cache`, PR #71. CI (unit tests and debug build) green on 13 Nov 2026.
The maintainer could not test yet (13 Nov 2026). Merge into `main` only after the device test.

## Open: device test steps for the maintainer
1. Build the release APK from `fix/playlist-cache` (copy `keystore.properties` and `local.properties` from `ipvio-release`), install over 1.4.1.
2. Settings > Playlists > "Reload playlist from scratch", confirm.
3. Live TV: categories after "AS" are there, "EU | DE | Movies and Series" is in Live TV.
4. If categories are still missing: `adb logcat -d -s IPVIO-PL` and send the output.
5. Optional: refresh with no network; the line "Aktualisierung fehlgeschlagen: …" appears in Settings > Playlists and the old list stays.
