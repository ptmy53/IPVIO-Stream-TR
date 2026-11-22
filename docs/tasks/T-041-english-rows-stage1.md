# T-041 – Option A, stage 1: English rows for German texts without a dictionary row

## Goal
T-039 found German UI and message strings in the code. Measurement (T-039 correction): 1051 of 1254 German literals already have a dictionary row and work. 184 unique texts have no row; for them the English UI and all other languages show German. Stage 1 gives the plain texts an English row. Approved by the maintainer ("A", then "Ja").

## Changes
- `app/src/main/java/com/ipvio/stream/DictEn1.kt` (new): 16 rows, format `German key¦English`. Other languages fall back to the English text (`I18nCore.fill`, column logic: an empty column takes the English text).
- `app/src/main/java/com/ipvio/stream/Lang.kt`: `DictEn1` added to `I18nCore.load(...)`.
- `app/src/test/java/com/ipvio/stream/DictEn1Test.kt` (new): two columns per row, unique keys, English lookup.
- `CHANGELOG.md`: one entry under Unreleased.
- `docs/tasks/WORK_STATE.md`: status.

## Behaviour change
- German UI: unchanged (the German text is the text).
- English UI: these 16 texts were German before; now English.
- Other languages (Turkish, Spanish, French, Italian, Dutch, Arabic, Polish, Bosnian, Croatian, Serbian): these 16 texts were German before; now English, because the fallback is English. This is the app's existing rule for missing columns. Tell the maintainer if German should stay the fallback for these.

## Not changed (on purpose)
- Texts with variables (`$code`, `%s`, `{0}`), fragments joined at run time, and texts in `RemoteLogic.kt` and `PhoneImport.kt` (they use their own `tr(li, de, en)` helper). These are stage 2.
- Other-language translations of the 16 texts: stage 3, needs review.
- Long help texts that are built from several literals (for example `SettingsExtra.kt`, `UpdateVerify.kt`): stage 2, they need one key per complete text.
- German in comments: not in this stage.
- `Dict*.kt` existing rows: not changed.

## The 16 texts
Listed in `DictEn1.kt`. Each German key was checked to occur exactly once as a literal in the code (script check at creation), and not in any dictionary.

## Acceptance criteria
1. German UI: all 16 texts unchanged.
2. English UI: each of the 16 texts shows its English text where it appears (device test).
3. `DictEn1Test` passes on the JVM (CI or the maintainer).
4. No duplicate German keys across all dictionaries for these texts.

## Risks
- The German key must match the runtime text exactly. A text that is built with a different space or punctuation keeps showing German. The device test covers the listed screens.
- Other-language users see English instead of German for these 16 texts (see Behaviour change).
- Literals that the app shows through a path other than `Text(...)` may not be translated at all; that is independent of this change.

## Documentation
- CHANGELOG entry under Unreleased (user-visible).
- No `docs/` change beyond this task file.

## Test results
- Creation check (script, not Gradle): 16 keys, each found once in the code, none in a dictionary, two columns each, no `$`/`{` in keys. **Ran (Python).**
- `DictEn1Test`: **not run** (Gradle needs the Android SDK, which is not available here; CLAUDE.md section 3).
- Device test: **not run**.

## Maintainer test steps
1. Build the debug APK in Android Studio. Run the JVM tests: `DictEn1Test` and `DictConsistencyTest` should pass.
2. Switch the app to English. Trigger these texts and check English is shown: "Fehler" (Settings > Diagnostics or a failed load), "Nichts gefunden" (subtitle search with no hit), "Zu viele Anfragen, bitte später erneut versuchen" (subtitle search rate limit), the three "The provider did not deliver …" texts (provider list errors), "Kategorien werden geladen …" (Stalker portal load), "Zuletzt geschaut · Filme" / "· Serien" (search page).
3. Switch to German: the same texts are German as before.
4. Switch to Turkish or Spanish: check that the texts are English (not German). Tell the assistant if German is preferred there.

## Status
Stage 1 done in the branch, not built, not device tested. Stage 2 (templated and fragmented texts) and stage 3 (translations) are open.
