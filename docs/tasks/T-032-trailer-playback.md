# T-032 Trailer playback (in-app, black picture)

**Status:** open. Handoff for the next session (Sonnet).
**Branch:** `claude/jolly-ride-mr9tgm` (PR #123 into `dev`, not merged).
**Related:** #15 (trailer in the app), 19 Nov device feedback.

## Goal

Trailers play inside the app on Android TV / Fire TV. Today some titles show a **black picture with no sound** in the in-app player. The goal is to find the cause and fix it, or to decide on a clean fallback.

## What the user sees (19 Nov)

- Some titles: black picture, no sound.
- Some titles: "no app found" when the external fallback is used.
- The button "Open in YouTube" (top right in the trailer dialog) was not reachable with the TV remote. **Fixed in commit `ad589a4`** (focus), not device tested yet.

## Current code (verified by reading)

| File | What it does |
|---|---|
| `app/src/main/java/com/ipvio/stream/DetailScreen.kt` | `trailerKey` state (line ~151); `TrailerDialog(...)` (line ~152); the trailer button calls `TrailerLogic.validKey(meta?.trailerKey)` and opens the dialog, otherwise `openTrailer(...)` (line ~533). `openTrailer` opens `https://www.youtube.com/watch?v=KEY` or a YouTube search. |
| `app/src/main/java/com/ipvio/stream/TrailerDialog.kt` | Full-screen `Dialog` with a `WebView`. Loads HTML with `loadDataWithBaseURL("https://www.youtube-nocookie.com", html, ...)`. `shouldOverrideUrlLoading` blocks main-frame navigation. `settings.javaScriptEnabled = true`, `domStorageEnabled = true`, `mediaPlaybackRequiresUserGesture = false`. WebView is `isFocusable = false` since `ad589a4`. Button "Open in YouTube" has focus on open. |
| `app/src/main/java/com/ipvio/stream/TrailerLogic.kt` | `validKey` (regex `^[A-Za-z0-9_-]{6,20}$`), `embedHtml(key)`: an iframe `https://www.youtube-nocookie.com/embed/KEY?autoplay=1&playsinline=1&rel=0`, `allow="autoplay; encrypted-media; picture-in-picture"`. |
| `app/src/main/java/com/ipvio/stream/Meta.kt` | Trailer key comes from TMDB (`trailerKey` in `Meta`, line ~24). Meta lookup only runs when `Config.TMDB_KEY` is set (`Meta.enabled()`, line ~162). |
| `app/src/test/java/com/ipvio/stream/TrailerLogicTest.kt` | Tests for `validKey` and `embedHtml` (JVM). |

## Facts

1. **The trailer ID comes from TMDB.** Without a TMDB API key the app has no trailer ID, so it only opens a YouTube search outside the app. The user asked for trailers without API keys. **Not possible with TMDB** (see the decision in chat, 20 Nov). Only option without a key: keep the external search. Any other source is unreliable or against YouTube's terms, and needs a decision by the maintainer.
2. **Cause of the black picture: not known.** Hypotheses, none verified on a device:
   - (a) The embed refuses playback in a WebView (YouTube "embedded player" errors, shown inside the iframe as an error message; black screen, no sound).
   - (b) The base URL `https://www.youtube-nocookie.com` given to `loadDataWithBaseURL` makes YouTube see an unexpected origin/referrer.
   - (c) Missing WebView settings (user agent, third-party cookies, hardware acceleration) on some Android TV builds.
   - (d) Some titles have the trailer blocked in the region or for embedding.
3. **Errors inside the iframe are not visible to the app.** `WebViewClient.onReceivedError` only sees failures of the outer page, not of the YouTube iframe.

## Acceptance criteria

- The user can tell from the app (or from the log) why a title stays black.
- Titles that work in YouTube's own app open and play in the trailer dialog, **or** the dialog switches to the external fallback automatically when playback fails (no black screen).
- Back closes the dialog and stops all sound.
- No API key is added or changed.
- No SharedPreferences names or keys change (CLAUDE.md section 5).

## Suggested steps

1. Add diagnostics first, no behaviour change: `WebViewClient.onReceivedError` and `onReceivedHttpError` call `Diag.report(...)` with the error code (no URL with keys). Check the `Diag` API before using it.
2. Test with 3 to 5 titles that are black today (note the title and TMDB key present yes/no). Read the diagnostics screen (Settings > Diagnose).
3. Try one change at a time, each one with a JVM test where possible (`TrailerLogic`):
   - base URL: try `loadData` without the base URL, or `loadUrl` of the embed URL directly.
   - add an automatic fallback to the external fallback if the page reports an error.
4. Keep the focus fix from `ad589a4` unchanged.

## Test steps for the maintainer (device or Fire TV)

1. Open a movie with a trailer (TMDB key set).
2. Press Trailer. Check: the dialog opens; the button "Open in YouTube" is highlighted.
3. Pick one title that was black before. Note: picture yes/no, sound yes/no.
4. Press Back. Check: the dialog closes, no sound keeps playing.
5. Press "Open in YouTube". Check: the YouTube app or browser opens the same trailer.
6. Open Settings > Diagnose and copy the lines containing "Trailer".

## Open decisions (ask the maintainer)

- Trailers without a TMDB key: keep the external search only (recommended), or a different source (needs a decision).
- If the embed cannot be fixed on Android TV: make the external player the default for all trailers.

## Documentation

- `CHANGELOG.md`: add an entry per change, state "not device tested yet".
- `docs/TESTING.md`: add the manual test steps above.
