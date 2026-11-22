# T-045 – Wishbox dictionaries DictW and DictPlW are never loaded

## Finding
- `DictW.kt` (122 rows, "Wishbox round 2" and later texts) and `DictPlW` are not registered in `Lang.kt` (`I18nCore.load(...)` and the Polish `loadColumn(...)`). A grep of `app/src/main` finds no use outside the files themselves.
- `DictConsistencyTest` loads `DictW` and `DictPlW` itself, so the tests pass while the app does not use these rows.
- Effect in the app now: the English UI and all other languages show German for these texts. Measured with the lookup port: all 31 candidate texts from the "What's new" list and the player and settings hints that still show German are rows of `DictW`. The other texts of these groups are covered by other rows.
- Git history: `DictW` was added in `f78cf0b` (Wishbox round 2), and the test was extended then. `Lang.kt` never referenced it (`git log -S"DictW" -- Lang.kt` finds nothing). So this looks like a missing registration, not a removal. **Not confirmed by the maintainer.**

## Why no change here
- Registering `DictW` changes visible text for every language that has a `DictW` column (German is unchanged). The texts are the maintainer's own translations, so the choice belongs to the maintainer (CLAUDE.md section 6: anything with a visible effect on existing users).
- The one-line fix: add `DictW` to the `I18nCore.load(...)` call in `Lang.kt`, and `DictPlW` to the Polish `loadColumn(...)`. Then run the JVM tests and check the "What's new" dialog in English and Polish.

## Test results
- Port (Python): 31 texts resolve to German; each is a `DictW` row. **Ran.**
- Build, JVM tests, device: **not run**.

## Decision needed
Should `DictW` and `DictPlW` be registered (activating about 122 rows for all languages)? Recommended: yes, after a review of the `DictW` texts in English and Polish on the device. Until then the affected texts stay German in English and other languages.

## Maintainer test steps
1. Register the two files, build, run `DictConsistencyTest` and `DictEn1Test` to `DictEn3Test`.
2. Open "What's new" (Settings > About) in English, then in Polish: the entries are translated.
3. Check the player hints (subtitle delay, Back closes the menu) and the adult content hint on the settings page.

## Decision (maintainer, 22 Nov 2026: "Ja")
- `DictW` and `DictPlW` are registered in `Lang.kt`: `DictW` first, `DictPlW` first in the Polish column, so the existing rows of `DictA`–`DictP` and `DictPl1`–`DictPl8` keep priority.
- Checked with the lookup port (before/after, all keys): no existing English value changes; 46 English keys are new.
- The four keys that exist in both (`Version`, `Später`, `Aktuell`, `Zurücksetzen`) keep their current English values.
- CHANGELOG entry added under Unreleased.
- Not built, no JVM test run, not device tested. The maintainer tests the steps below.

## Status
Registered in the branch. Not built, not device tested.
