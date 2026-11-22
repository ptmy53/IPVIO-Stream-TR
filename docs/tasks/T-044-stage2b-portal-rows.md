# T-044 – Option A, stage 2b: rows for Stalker portal messages

## Goal
Continuation of T-043. The lookup port showed two portal messages that still show German in English. Fill them with rows (no code change).

## Changes
- `app/src/main/java/com/ipvio/stream/DictEn3.kt` (new): 2 rows, German key with `{0}` placeholder, English text.
- `app/src/main/java/com/ipvio/stream/Lang.kt`: `DictEn3` added to `I18nCore.load(...)`.
- `app/src/test/java/com/ipvio/stream/DictEn3Test.kt` (new).
- `.claude/scripts/i18n-lookup.py`: reads all `DictEn*.kt` files.
- `CHANGELOG.md`: one entry under Unreleased.

## Checked, no row needed
- The "Kalibrierung" paragraph (FitCalibration) and the "Bibliothek hat … Filme" paragraph (CategoriesScreen) already have full dictionary rows; the port returns English.
- "Das Portal hat keine Sender geliefert" and "Lade … %": already translated.

## Not changed
- Remaining gaps listed in T-043, apart from these two.
- Messages from `RemoteLogic.kt` and `PhoneImport.kt` (own helper).

## Test results
- Port check (Python): both runtime texts resolve to English. **Ran.**
- `DictEn3Test` and the JVM suite: **not run** (no Android SDK here).

## Maintainer test steps
1. Run the JVM tests `DictEn1Test`, `DictEn2Test`, `DictEn3Test`.
2. On a Stalker portal with a wrong address or a portal that does not answer, check the English texts.

## Status
Done on the branch, not built, not device tested.
