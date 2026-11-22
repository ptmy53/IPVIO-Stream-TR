# T-042 – Option A, stage 2a: English patterns for German texts with placeholders

## Goal
Continuation of T-041. Texts with run-time values (`$code`, `${x}`) are looked up as patterns (`{0}`, `{#0}`) by `I18nCore`. Measured: 31 templates from the code were compared with the existing dictionary. 22 already resolve to an English pattern. 9 did not and get a row here.

## Changes
- `app/src/main/java/com/ipvio/stream/DictEn2.kt` (new): 9 rows, `German key¦English`.
- `app/src/main/java/com/ipvio/stream/Lang.kt`: `DictEn2` added to `I18nCore.load(...)`.
- `app/src/test/java/com/ipvio/stream/DictEn2Test.kt` (new): seven runtime texts resolve to English.
- `CHANGELOG.md`: one entry under Unreleased.
- `docs/tasks/WORK_STATE.md`: status.

## Checks done
- Python simulation of `I18nCore` lookup (exact first, then patterns, longest first) for the 9 rows and the runtime examples: each example resolves to its own pattern. No collision with an existing key found.
- The 22 covered templates were checked: each has an English column in `Dict[A-P].kt`.

## Not changed (on purpose)
- Templates with German nouns from variables (`$vodNoun`, `$noun`, "Keine $noun in dieser Playlist"): the variable is German text, so a pattern would produce German words in English. Needs stage 2b (code change: translate the noun first).
- Fragments joined at run time (about 30): stage 2b.
- Templates with function calls in the placeholder (`${hhmm(nowSec)}`, `${epgZone().id}`): stage 2b.
- Other languages: fall back to English as in T-041.

## Test results
- `DictEn2Test`: **not run** (no Android SDK here, CLAUDE.md section 3).
- Simulation: passed for all 9 rows (Python).
- Device test: not run.

## Maintainer test steps
1. Build and run the JVM test `DictEn2Test` (and `DictEn1Test`, `DictConsistencyTest`).
2. Switch to English. Trigger a subtitle search with an error (OpenSubtitles error text), a channel or category load on a Stalker portal ("Loading channels …" / "Loading categories …"), and the search page with a query and each filter ("Search … · Movies / Series / Live TV").
3. Switch to German: the texts are as before.

## Status
Stage 2a done in the branch, not built, not device tested. Stage 2b and 3 open.
