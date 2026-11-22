# T-033 – Technical stream info on the detail page (issue #24)

## Goal
The detail page of a movie or series shows the technical data of the stream before playback: resolution, video codec,
HDR10/HLG, Dolby Vision, audio tracks (language, codec, channels, Atmos) and subtitle languages. Issue #24 asked for this.

## Source of the data
- Xtream `get_vod_info` leaves `video`/`audio` empty at the maintainer's providers (tested by the maintainer), so the
  provider cannot be the source.
- ffprobe (FFmpegKit, suggested in the issue) was rejected: project discontinued in 2025, native binaries (+20–60 MB).
- Chosen: Media3 `MetadataRetriever` from the new module `androidx.media3:media3-inspector:1.11.1` (approved by the
  maintainer on 19 Nov 2026). It reads only the container header with the same extractors the player uses; nothing is
  decoded. Its only dependency is `media3-exoplayer`, which the app already has.
- Prototype: branch `feature/media-probe` (`afd55ca`). This task makes it ready for users.

## Changes
- `app/build.gradle.kts`: `media3-inspector:1.11.1`.
- `MediaProbeLogic.kt` (new, no Android): tracks → `Info` → display line. MIME types and color constants mirror Media3.
- `MediaProbe.kt` (new): runs the retriever on `Dispatchers.IO`, 20 s timeout, same HTTP settings and backup servers as
  the player (`HostFailover.DataSourceWrapper`). The check is cancelled and the connection closed when the detail page
  closes (e.g. Play). In-memory cache per URL for the app session (max. 200, failures too). Logs only in debug builds,
  without URLs. `ProbeLine` composable: shows nothing while loading, on failure or when switched off.
- `DetailScreen.kt`: `ProbeLine(playUrl)` below the info line, only on the full detail page, not for live channels or
  demo mode. Series: the first episode of the shown season (the URL the Play button uses).
- `Playback.kt`: `PlaybackPrefs.probe`, key `probe` in `ipvio_playback` (new key, default on).
- `SettingsExtra.kt`: toggle "Technical info on the detail page" under "Source & quality".
- `SettingsReset.kt`: the player page reset switches it back on.
- Dictionaries: 2 new rows in `DictO.kt` (en, tr, bs, sr, es, fr, it), `DictNl6.kt`, `DictAr6.kt`, `DictPl7.kt`.
  "Audio" and "Subtitles" in the line reuse existing rows.

## Storage (CLAUDE.md section 5)
One new key `probe` (Boolean) in `ipvio_playback`. No existing key or file changes. The cache is memory only.

## Risks
- Accounts with one connection: the check holds a connection for a moment (usually under a few seconds, at most 20 s).
  If something else plays at the same time, the provider may refuse one of them. Mitigation: cancelled on leaving the
  page, switch in the settings.
- Providers that limit requests may count the check as a stream start. Unknown, needs device tests.
- MKV files with the index at the end or MP4 with `moov` at the end can take longer. Unknown, needs device tests.
- HDR10+ is not detected (dynamic metadata is not in the header). Atmos is only detected for E-AC-3 JOC, not TrueHD.
- `MediaProbe` marks a server as failed in `HostFailover` like the player does when the main server is down.

## Acceptance criteria
- A 4K HDR movie shows e.g. "4K · HEVC · HDR10 · Audio: German (E-AC-3 5.1), English (AC-3 5.1) · Subtitles: German".
- A failing or slow stream shows no line and no error text; the rest of the page works as before.
- Play right after opening the page starts normally (the check is cancelled).
- Switch off: no line, no request.

## Tests
- `MediaProbeLogicTest` (13 tests) and `Media3ContractTest.mediaProbeConstants_matchMedia3` (new): passed locally on
  19 Nov 2026 with `gradlew.bat testDebugUnitTest` (JDK 21). `DictConsistencyTest`, `I18nCoreTest`, `SettingsResetLogicTest`
  passed as well.
- Full run: 425 tests, 2 failed: `PhoneImportTest.listImportWithoutCodeIsRejectedEvenWhenPhoneSearchIsActive` and
  `PhoneTransferTest.uploadBackupRestoreCrash` with `SocketException: Connection reset`. They fail the same way on
  `upstream/dev` without this change (local Windows socket issue, **existing**, not caused by T-033).
- `assembleDebug` built. Size of the APK content: +32.7 KB (24,692,718 → 24,725,405 bytes in entries; the file size of
  incremental debug APKs is not comparable).
- Not tested on a device.

## Test steps for the maintainer
1. Open a movie on the detail page: after a moment a grey line appears below the info line (resolution, codec, HDR, Audio, Subtitles).
2. A 4K HDR / Dolby Vision movie: "4K", "HDR10" and/or "Dolby Vision" appear. A movie with Atmos: "E-AC-3 Atmos".
3. Open a movie and press Play at once: playback starts normally, also with a one-connection account.
4. Leave the page and open it again: the line appears at once (cache), no new request.
5. Open a series: the line describes the first episode of the shown season.
6. Settings > Player > Source & quality: switch "Technical info on the detail page" off; the line is gone.
7. Reset the player settings page: the switch is on again.
8. Stream that does not load (dead link): no line, no error, page works.
9. Logcat (debug build) tag `MediaProbe`: time per check and the result, no addresses.

## Not done
- Badges on posters in the lists (would need one request per title; rejected).
- Info per source when a movie has several sources (only the source Play uses is checked).
- Persistent cache across app starts.

## Status
Implemented on branch `feat/media-probe` from `upstream/dev` (`3a34845`). JVM tests ran locally (see above).
Not device tested.
