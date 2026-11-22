# T-029: Arabic as UI language with right-to-left layout (issues #88, #21, #99)

**Status:** plan. The maintainer chose "complete interface with RTL" on 19 Nov 2026. The translation work is large (about 1,900 German keys, see `DictA`…`DictP`), so it is done in stages. No dictionary rows are added yet.

## Facts from the code (19 Nov 2026)
- `I18nCore.CODES` lists the UI languages; Arabic would be a new code appended after the existing ones so the indices of the hr/sr fallback stay as they are (issue #88 uses index 10).
- Translation rows: `German ¦ English ¦ Turkish ¦ Bosnian ¦ Croatian ¦ Serbian ¦ Spanish ¦ French ¦ Italian ¦ Dutch`. Arabic would be one more column. `Lang.kt` loads each column with `I18nCore.load` / `loadColumn`; Dutch uses `DictNl1…6` as a separate column loader, which is the pattern to copy.
- Pattern rows with `{0}` / `{#0}` are compiled separately (`pats`); `loadColumn` only fills exact rows. The issue asks for a small extension of `loadColumn` for them. The placeholder test (`DictConsistencyTest`) checks that numbers appear in every language.
- Layout direction: `MainActivity` / `App` would provide `LayoutDirection.Rtl` when Arabic is selected (as in the pilot of #88).
- Metadata language for TMDB (`Lang.tmdb()`) maps `ar` to `ar-SA` (issue #21 scope).

## Phases
1. **Infrastructure** (small): `ar` code and name in `I18nCore`, column loader, `LayoutDirection` in `App`, `Lang.tmdb()` mapping. Unit test for the code list (index stability) and a placeholder test for Arabic.
2. **Strings, staged**: `DictAr1…N`, split by the existing dictionary blocks. Each part is checked with `DictConsistencyTest`. The pattern rows get the `loadColumn` extension.
3. **RTL audit** (device needed): paddings and margins that assume left-to-right, the player controls, the TV focus order, the sidebar, the chips in search.
4. **Coverage test**: fails when a German key has no Arabic entry (like the Dutch coverage test).

## Decisions (defaults used unless the maintainer says otherwise)
- Digits: Western digits 0–9 (the issue's recommendation), so no Arabic-Indic numbers in the TV UI.
- Tone: Modern Standard Arabic, short labels.
- Metadata language (#21): can be selected separately in settings; it is part of phase 1 only if the maintainer asks for it.

## Risks
- Scope: about 1,900 rows. Machine translation without review is not good enough for a UI; a native reviewer is needed.
- RTL changes the layout of every screen; only a device test shows the result. Nothing here is verified by build or tests yet.
- Translation rows are UI strings: CLAUDE.md section 5 says German keys are the translation keys and must not change without a decision.

## Test steps (later)
1. Settings > Language > Arabic: the whole interface switches, labels show Arabic, layout mirrors.
2. Navigate with the remote through home, live TV, movies, series, search, settings, player: focus order is correct, no text cut off.
3. Switch back to German: nothing is left in Arabic.
4. Run `DictConsistencyTest` and the new coverage test (on the maintainer's machine).
