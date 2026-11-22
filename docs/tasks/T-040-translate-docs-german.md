# T-040 – Translate German in documentation (option C of T-039)

## Goal
CLAUDE.md section 4: everything in the repository is English, including docs, task files and the archive. T-039 found about 57 German lines in documentation. Translate them, without touching app code or the German UI keys.

## Changes
- 21 files in `docs/` changed: task files (about 20), `docs/RELEASE.md`, `docs/SECURITY.md`, the archive `docs/tasks/WORK_STATE-history.md`.
- Quoted German UI labels are replaced by the **English label from the app's dictionary** (CLAUDE.md section 4), taken from the English column of the `Dict*.kt` row for the same German key. Where the dictionary has no row, a translation was written and is marked below.
- Archive header changed to say that labels were translated; its body is no longer identical to the old WORK_STATE. This breaks the check "body identical" in T-036.
- `docs/quality/german-strings-2026-11-22.md`, `docs/tasks/WORK_STATE.md`: status updated.

## Not changed (on purpose)
- App code: no change. Option A of T-039 (UI texts in `Dict*.kt`) needs approval.
- `CHANGELOG.md`: 7 German lines in released sections (1.2 to 1.4.3). CLAUDE.md section 18 says not to rewrite older sections, so they stay. Maintainer decision.
- `docs/release-notes/`: released notes, not touched.
- Words in the English column of the dictionaries are not changed.

## Translations without a dictionary row (check against the app)
These quoted labels were not found in the dictionary files; the English wording is a translation of mine:
- "Filme werden geladen …" / "Serien werden geladen …" → "Loading movies …" / "Loading series …"
- "Über" (settings page name) → "About"
- "Kennung" (user agent) → "User agent"
- "Teillisten kann weg" (maintainer quote) → "Sublists can go"
- "standardmäßig auf täglich" → "set to daily by default"
- "Filme" / "Serien" / "Kategorien" as column names in task texts → "Movies" / "Series" / "Categories"

Also: the German texts themselves still exist in the app code (about 670 lines, see T-039). Those are not touched here.

## Acceptance criteria
1. `english-check.sh` on the default base prints TEXT 0 (no German in changed or archived documentation).
2. The whole-tree run shows TEXT 7, and all 7 are in `CHANGELOG.md` released sections.
3. Quoted UI labels in task files match the English dictionary column where a row exists.

## Risks
- Translated labels may differ from what the app shows (the dictionary is the reference). Check the list above.
- Archive history changed in wording only; no facts changed. The archive is no longer a byte copy of the old log.

## Test results
- `english-check.sh` default base: TEXT 0. **Ran.**
- `english-check.sh` whole tree: TEXT 7, all in `CHANGELOG.md`. **Ran.**
- Label replacements: checked by `git diff` on the changed lines. **Ran (read through, not exhaustively).**
- Markdown rendering and links: not checked in a renderer. **Not run.**
- Gradle, unit tests, device: not applicable.

## Maintainer test steps
1. Run `.claude/scripts/english-check.sh` and expect TEXT 0.
2. Compare three labels with the app in English: "Check for updates now", "Reload playlist from scratch", "Technical info on the detail page". If they differ, tell the assistant the exact text.
3. Decide about the 7 CHANGELOG lines (released sections). Options: leave them, or translate with a note in the 1.4.8 CHANGELOG entry.

## Status
Done on `claude/great-lovelace-tlhb6f`. Open: the 7 CHANGELOG lines and option A from T-039.
