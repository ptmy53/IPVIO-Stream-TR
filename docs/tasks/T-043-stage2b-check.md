# T-043 – Option A, stage 2b: check what still shows German in English

## Goal
Stage 2b was planned as code changes for texts that are built at run time (fragments, nouns in templates). Before changing code, measure what the existing lookup already does.

## Findings
- `I18nCore.translate` already handles the cases that looked like gaps:
  - nouns inserted into templates (`Alle $vodNoun`, `Keine $noun in dieser Playlist`, `$vodNoun · Kategorien`): captured groups and "A · B" parts are translated, so "Alle Filme" gives "All Movies" (checked with the port).
  - "Name: value" texts (`Keine Verbindung: $x`, `Uhrzeit jetzt: $t`, `Ton: …`): the part before ": " is translated.
  - Several multi-literal texts that appeared as fragments are complete dictionary rows already (for example the playlist size hint, the "Alles laden" line, the signature message).
- The earlier count of 184 texts without a row counted fragments and templates by exact literal; most of them resolve at run time.
- With the port (`.claude/scripts/i18n-lookup.py`), 122 of the 184 candidates still return German. Most of those are not real gaps: templates with `$variables` (checked at run time, not by literal), texts in `RemoteLogic.kt` and `PhoneImport.kt` (own `tr(li, de, en)` helper), and fragments.

## Changes
- `.claude/scripts/i18n-lookup.py` (new): the lookup port, English column only.
- `docs/tasks/T-043-stage2b-check.md` (this file).
- No app code changed in this task.

## Real remaining gaps (to check on the device and to fix with rows or code), by file
- `SettingsExtra.kt`: the long help paragraphs, built from several literals (about 8 pieces in 5 paragraphs). Needs full-paragraph rows built from the exact concatenated text.
- `StalkerApi.kt`: portal messages with run-time values, and the two "keine …" fragments.
- `PlayerLogic.kt`, `PlaylistLogic.kt`, `Epg.kt`, `Parental.kt`, `SettingsHub.kt` ("Ein größerer Puffer …", the Classifier progress line): messages with run-time values, to be checked per call site.
- `DriveUi.kt`: messages with "Keine Verbindung zu Google (…)" and the sign-in failure text: check the composite.
- `CategoriesScreen.kt` (two literals of one sentence), `FitCalibration.kt` (one long sentence), `MainActivity.kt` (one fragment).
- `PhoneImport.kt`, `RemoteLogic.kt`: not through `I18nCore` (own helper). Not part of this list.

## Not changed (on purpose)
- No rows and no code changes in this task. The gaps above need a check per call site, which needs the device.

## Test results
- Port smoke test: "Alle Filme" → "All Movies", "Fehler 403" → "Error 403", "Keine Verbindung: SocketException" → "No connection: SocketException". **Ran (Python).**
- Device and JVM tests: **not run**.

## Maintainer test steps
1. Switch to English. Open Settings > Diagnostics, Settings > Playlists and Settings > Player: the long help paragraphs. Note any German text and send it.
2. Run `python3 .claude/scripts/i18n-lookup.py "<text>"` on a German text you see to check the dictionary lookup.

## Status
Measurement done. Stage 2b is open: about 40 real gaps, to work through file by file with the device.
