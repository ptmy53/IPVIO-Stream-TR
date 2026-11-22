# T-034 Performance regression in 1.4.4

## Goal
Users and the maintainer report that 1.4.4 is very slow / laggy compared with 1.4.3. Find what 1.4.4 changed and restore the speed without removing the new features.

## Analysis (diff v1.4.3..v1.4.4, read-through, no profiler, no device)
Found in the code, ordered by the expected effect. Effects are **unverified** (nothing measured).

1. **Age rating made all stored TMDB details outdated** (`Meta.kt`, T-032, commit 459f19d). `MetaRepo.extended` returned the stored details only with `ageChecked`, which no entry from before 1.4.4 has. `extended` runs for every focused title: billboard (`Billboard.kt`), home preview (`HomeScreen.kt`), cards (`HomeParts.kt` `rememberFullMeta`), search, and the list preview (`BrowseScreen.kt` embeds `DetailScreen`). So each focus started a TMDB detail request (larger, with `release_dates`/`content_ratings`), often an OMDb request, sometimes an English overview request, and a file write. The same for the provider path: `withOmdbTitle` asked OMDb again for every title that already had ratings.
2. **Xtream lists loaded one after the other** (`XtreamApi.kt`, commit 60e9558, PR from cybergadgets, not in the changelog). The parallel download of movies and series next to the channels (since 1.2, `db941de`) was removed. Loading and every refresh take about as long as the three downloads together; the refresh runs in the background and keeps the CPU and network busy longer.
3. **Stream check on every detail page** (`MediaProbe.kt`, T-033): opened a second provider connection as soon as the page showed, also for a quick look or an immediate Play.
4. **All dictionary columns parsed at start** (`Lang.kt`): Arabic and Polish added about 2,700 rows that were parsed on the main thread at every start, whatever the language.

Checked without finding a cost on the hot path: private categories (`PrivateCats.filter` returns the same list when nothing is private), "Recently added" sort (runs on `Dispatchers.Default`), guide/Live TV wrap-around (focus index only read in key handlers), category folders (#96, remembered), RTL layout direction, `Diag.history`.

## Changes
- `MetaFreshLogic.kt` (new, pure): when stored details can be used. `Meta.kt`: `extended(…, needAge)` and `providerMeta(…, needAge)`; only `needAge = true` asks for the age rating at all (TMDB `release_dates`/`content_ratings`, OMDb `Rated`), also for new titles (maintainer decision 21 Nov: only when the title is opened). OMDb keeps its own "tried" entry for the age check. `DetailScreen.kt`: `needAge` only for the real detail page (not embedded, or embedded with actions open).
- `XtreamApi.kt`: parallel download restored (structure of 1.4.3). Each part keeps the 1.4.4 completeness check (throws `PlaylistError(incomplete = true)`). A part that fails in parallel for any reason except a user abort is fetched once more on its own; if that fails too, the error passes on (then `apiOrList` falls back to get.php as in 1.4.4). Order in the list is back to channels, movies, series.
- `MediaProbe.kt`: the check starts after 1.5 s on the page.
- `I18nCore.kt`: `load` and `loadColumn` only register blocks (the same block twice is ignored). On the first `translate` into a language, the German keys and patterns are read once and then only that language's column (main blocks with the old fallback rules, then its separate column blocks). German never parses anything. Blocks registered later (wishbox texts) go into languages already in use at once. Maintainer decision 21 Nov: all languages load only when used.

## Not changed (risk noted)
- `HostFailover.Interceptor` also fails over on 404 since 1.4.4 (maintainer request of 19 Nov). With backup servers, a missing logo on the provider host may mark the main server as failed. Not changed here, needs a decision.

## Acceptance criteria
- Browsing Home, Movies, Series and search with a large playlist feels like 1.4.3.
- The detail page shows the age rating (after the first visit of a title also in the info lines elsewhere).
- An Xtream playlist loads completely, also from a provider that refuses parallel requests.

## Test results
- Ran here (JDK 21, Kotlin 2.0.21 compiler from the Gradle install, JUnit 4.13.2): `I18nCoreTest` (incl. new `lazyColumnIsReadOnFirstUse`), `MetaFreshLogicTest` (new, 3 tests), `DictConsistencyTest`, `RemoteLogicTest`: 26/26 passed (second round, 21 Nov, incl. new tests for later blocks and double registration).
- Not run: Gradle build, Android files (`XtreamApi.kt`, `Meta.kt`, `DetailScreen.kt`, `MediaProbe.kt`, `Lang.kt`) not compiled, no device test, nothing measured.

## Device test steps (maintainer)
1. Install over 1.4.4 (data stays). Large playlist with TMDB key.
2. Home: move through slideshow and rows for a minute, then Movies grid with the list preview. Compare with 1.4.4: no stutter; `adb logcat` should not show a stream of TMDB requests.
3. Open a movie detail page: age rating appears. Before opening, rows and billboard show no age rating for titles not opened yet (intended).
4. Settings > Playlists > "Reload playlist from scratch" on an Xtream account: time it against 1.4.4; check counts of channels, movies, series.
5. If available: the provider that answered 884/885 to parallel requests (PR #117) must still load completely.
6. Open a detail page and press Play at once: playback starts normally (no second connection).
7. Switch the language to Polish, Arabic and Dutch: texts are translated.

## Status
Implemented on `claude/confident-heisenberg-ulq8q2`, open for build and device test.
