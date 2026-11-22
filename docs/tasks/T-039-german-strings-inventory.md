# T-039 – Inventory of German text outside the dictionaries

## Goal
Find out how much German text sits in the code outside `Dict*.kt` and in the docs, so the maintainer can decide how to handle it (CLAUDE.md sections 4 and 5). Analysis only. No text was changed.

## Changes
- `.claude/scripts/english-check.sh`: classes split into UI, MSG, CODE, COMMENT, KEY (translation keys with `t()` and `tr()` count as KEY). Whole-tree mode via the empty tree as base. `FULL_OUT=<file>` writes every finding for counting. Summary shows the count per class.
- `docs/quality/german-strings-2026-11-22.md` (new): counts per class and per file, observations, options for the maintainer. Contains no German text (CLAUDE.md section 4).
- `docs/tasks/T-038-english-check.md`: two quoted German labels removed.
- `docs/tasks/WORK_STATE.md`: current task.

## Not changed (on purpose)
- No app source, no German text in the repository was translated or removed.
- The archive `docs/tasks/WORK_STATE-history.md` and the older task files are not edited (TEXT findings).

## Acceptance criteria
1. The whole-tree run prints per-class counts and exits 0.
2. The report lists the counts per file and a decision for the maintainer, without quoting German.
3. No German text is added to the repository by this task (the English-check on the default base reports only the archive).

## Risks
- Heuristic classes. UI, MSG and CODE are estimates; the report says so.
- The word list misses German words that are also English words, and it may count non-German words.

## Test results
- Whole-tree run: TEXT 57, COMMENT 18, UI 100, MSG 96, CODE 435, KEY 261. **Ran, counts as reported.**
- Per-file counts from `FULL_OUT`: computed with a Python script over the output. **Ran.**
- Default base after the report was written: TEXT 7, all in the archive. **Ran.**
- Line-by-line review of the 706 non-key lines: **not run**.
- Gradle, unit tests, device: not applicable.

## Maintainer test steps
1. Run `.claude/scripts/english-check.sh "$(git hash-object -t tree /dev/null)"` and compare the totals with the report.
2. Decide on options A, B or C in the report. Option A needs approval (sections 5 and 6).

## Status
Done on `claude/great-lovelace-tlhb6f`. Open for the maintainer's decision.
