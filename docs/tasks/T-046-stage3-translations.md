# T-046 – Option A, stage 3: translations of the English-only rows

## Goal
The 27 texts added in T-041 to T-044 (`DictEn1`–`DictEn3`) had only German and English. Add Turkish, Bosnian, Croatian, Serbian, Spanish, French, Italian, Dutch, Arabic and Polish. Approved by the maintainer ("Los").

## Changes
- `DictEn1.kt`, `DictEn2.kt`, `DictEn3.kt`: rows now have nine columns (German, English, Turkish, Bosnian, Croatian, Serbian, Spanish, French, Italian), the format of the other main dictionaries.
- `DictEnLang.kt` (new): Dutch, Arabic and Polish, two columns each, loaded with `loadColumn` in `Lang.kt`.
- `Lang.kt`: the three new blocks registered for Dutch, Arabic and Polish.
- `DictEn1Test.kt`: placeholder check for all languages, and Dutch/Polish loading.
- `CHANGELOG.md`: one entry under Unreleased.

## Translation quality
- **Drafted by the assistant, not reviewed by a native speaker.** The maintainer decides whether these go into the release or are reviewed first.
- Placeholders `{0}` and `{1}` are checked against the German text for every language (script check, all 27 rows, no mismatch).
- Terms follow the app's dictionary where a term exists (for example "Later", "Reset", "Movies", "Series" in the other languages). Other terms were translated freely.
- Serbian is written in Latin script, as in the app's other Serbian rows (to be confirmed).

## Not changed
- German and English texts.
- Texts with other translations already (for example the "What's new" texts in `DictW`).

## Test results
- Placeholder check (script, all languages): **passed** (0 mismatches). **Ran.**
- Column count (9 per main row): **passed**. **Ran.**
- Lookup port, English column: unchanged. **Ran.**
- JVM tests (`DictEn1Test` including the new tests, `DictConsistencyTest`): **not run** (no Android SDK here).
- Device test: **not run**.

## Maintainer test steps
1. Run the JVM tests.
2. Switch to Turkish, then Arabic (right-to-left layout), then Polish: open the search page with a query and each filter, the subtitle search with an error, the Stalker loading texts, the player hints (subtitle delay, Left/Right).
3. Review the drafted translations (the list is `DictEn1.kt`–`DictEn3.kt` and `DictEnLang.kt`) and send corrections.

## Status
Done on the branch, not built, not device tested. Translations need a native review.
