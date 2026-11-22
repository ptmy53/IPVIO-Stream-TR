# T-047 – Faster first screen after a start

The request named "T036". That ID is already used on `dev` (workflow files), so this task is T-047.

## Goal
Reduce the time until the first screen is usable after a cold start, without changing what the app shows or stores.

## Analysis (read-through of the start path, no profiler, nothing measured)
Start path: `MainActivity.onCreate` → `App` → `AppContent` (`remember` inits, then a `LaunchedEffect` loads the stored playlist from the cache file on `Dispatchers.IO`, then `show` → `rebuild`).
1. **Dictionary of the UI language parsed on the main thread.** For every language except German, the first translated text runs `I18nCore.ensure`: it reads all German keys of all blocks (a regex per placeholder row) and fills the column. This happens inside the first composition, so the first frame waits for it. English is the default for any device language the app does not support, so it affects most first starts. `Lang.loadMs` only measures the registration, not this parse.
2. **Refresh starts at once after the stored list is shown.** `loadInner` continues straight into `downloadWithRetry` and the parse of the whole provider list, on the same CPU and network as the first screens. Same for the Xtream archive request in `Catchup.startAll`.
3. Already deferred before this task (no change): update check 3 s, sport alerts 10 s, genre classifier 8 s, TV guide 6 s, WorkManager setup off the main thread.
4. The stored list was read only after the first composition (`LaunchedEffect`), single-threaded. Done in the second round (see Changes). The cache format is untouched.

## Changes
- `I18nCore.prepare(li)`: parses a language ahead (same lock and result as the lazy path). `Lang.prewarm()` is started on a background thread in `MainActivity.onCreate` right after `General.init`.
- `Lang.ensureLoaded` made thread safe (the flag was set before the blocks were registered; with a second thread a text could be looked up too early and the empty result memoized).
- `DictNlW` registered in `Lang` (last in the Dutch list, the same position as before). `loadWishboxTexts()` now only repeats registrations that are ignored. Without this, a language already parsed in the background would be re-applied completely on the main thread.
- `StartupLogic.refreshDelayMs`: 4 s wait before the silent refresh, only when the stored list was shown and the load was not started by hand (`save` false). Used in `PlaylistStore.loadInner`. `Catchup.startAll`: 5 s wait before the archive request.
- **Stored list read ahead (second round).** `PlaylistStore.preread` runs on a background thread from `onCreate` (not with the profile picker, not when an interrupted add resumes). It reads the stored list with the same conditions as `fromLocal` (header matches the language selection and content mode). `fromLocal` takes the result once if address, language selection and file state (size, time) still match; if the read is still running it waits for it instead of reading twice; otherwise it reads as before. An unused result is dropped when the load ends. `PlaylistStore.ensure` is now thread safe (it set its flag before the lists were filled; with a second thread the main thread could have seen an empty list and opened the add screen).
- Diagnostics line "Start: …" now also shows the background parse time.

- Review round (22 Nov 2026): the delay before the refresh applies only to the silent refresh at the app start (`load(..., startup = true)` from `MainActivity`), not to a reload, switch or add by the user. `refreshOthersDue` (T-048b) waits up to 3 minutes while the main list holds the load lock.

## Risks
- Playlist freshness: a due refresh starts 4 s later. The list on screen is the stored one either way.
- While the refresh waits, `PlaylistStore.loading` is already true and the load lock is held (as before while it downloaded). A reload pressed in these 4 s is ignored as it was during a download.
- Profile picker: the prewarm uses the language of the profile loaded in `onCreate`; after picking another profile with another language the right one is parsed on first use (no wrong texts, only no gain).
- Read-ahead: one extra copy of the list in memory only while two reads would overlap; the code waits for the running read instead. A stale result is rejected by the file stamp.
- Protected area: `PlaylistStore.kt` (timing only, no storage or format change), `Catchup.kt` (timing only).

## Test results
- Ran here (JDK 21, Kotlin 2.0.21 compiler from the Gradle install, JUnit 4.13.2): `I18nCoreTest` (incl. 2 new tests) and `StartupLogicTest` (new, 4 tests): 18/18 passed.
- Not run: Gradle build, `Lang.kt`, `MainActivity.kt`, `PlaylistStore.kt`, `Catchup.kt` not compiled, no device test, nothing measured.

## Device test steps (maintainer)
1. Install over the current version. Set the UI language to English (or Polish), force stop, start. Settings > Diagnostics: the "Start:" line shows `translations … ms registered, … ms parsed in the background`. Compare the time to the first home screen with the previous build (stopwatch or `adb shell am start -W`).
2. German UI: start works as before.
3. Start with a stored playlist whose refresh is due: the home screen shows the stored list at once; the refresh runs a few seconds later and the list updates.
4. Texts of the Dutch "What's new" entries (wishbox) are translated.
5. Xtream playlist: archive (catch-up) still works after a minute.
6. Kill the app while a playlist is loading for the first time, start again: the add screen still resumes. Start with several profiles and the picker on: the picked profile's list shows.
7. Pressing "Reload" within the first seconds still reloads (or is ignored while a refresh waits; no crash).

## Status
Implemented on `claude/pensive-galileo-i3tza8`, open for build and device test.
