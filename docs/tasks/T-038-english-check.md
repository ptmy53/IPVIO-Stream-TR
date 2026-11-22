# T-038 – English check for added lines (CLAUDE.md section 4)

## Goal
CLAUDE.md section 4 forbids German in everything in the repository outside the German keys of the `Dict*.kt` files. Add a check that finds German words in lines added since a base ref, sorted by where they are, so a reader can judge them. Report only.

## Changes
- `.claude/scripts/english-check.sh` (new): scans added lines of tracked files (against a base ref, default `origin/main`, working tree included) and of new untracked files. Classes: TEXT (docs, CHANGELOG, README), COMMENT (German in code comments), CODE (German string in code outside `Dict*.kt`), KEY (German as the first argument of `t("…", "…")`, allowed). Exit code is always 0.
- `.claude/commands/english-check.md` (new): `/english-check [ref]`, runs the script and explains the classes.
- `docs/tasks/T-038-english-check.md` (this file), `docs/tasks/WORK_STATE.md`.

## Not changed (on purpose)
- No German text in the app was changed. Replacing hard-coded German UI strings is a UI-string decision for the maintainer (CLAUDE.md section 5).
- The archive `docs/tasks/WORK_STATE-history.md` is not edited; it shows up as TEXT findings.
- No CI gate. A blocking check needs approval (section 6), and a word list gives false positives.

## Known limits
- Word list only (`WORDS` in the script). Words that are also English are left out (for example "die", "in", "war" are not in the list). German without a listed word is not found.
- Identifiers in camelCase are not split; a German word inside a longer identifier is not found.
- A match can be a false positive (a German word used as a name, a place name, a quoted foreign word). The reader decides.
- Counts over the whole tree are word-list estimates, not checked line by line.

## Acceptance criteria
1. `english-check.sh <base>` runs without error and prints the summary line with the four class counts.
2. Against an older base with code changes (`1835e93~20`), CODE and COMMENT findings appear with file and line; KEY findings are counted only.
3. A German comment is reported as COMMENT, a German `t("…", "…")` first argument as KEY.
4. The check does not report `Dict*.kt` files.

## Risks
- Noise: a reader who trusts every line will change correct code. The command says to judge each line.
- Speed: the first version ran a `grep` per line and timed out on a large diff; the prefilter in one pass takes about 2 seconds on the test range.

## Documentation
- `.claude/commands/english-check.md` describes the use. CLAUDE.md is not changed.

## Test results
- `bash -n` syntax check of the script: **passed**.
- Base `09fcc2a` (start of the T-036 work): TEXT 7, all in the archive file. **Passed as expected.**
- Base `1835e93~20`: TEXT 25, CODE+COMMENT 125, KEY 33. The CODE lines include hard-coded German labels in `AddPlaylistScreen.kt` and `CategoriesScreen.kt` (quoted texts omitted, see the German-strings report). **Passed as expected** (no review of all 125 lines done).
- Whole `app/src/main`, outside `Dict*.kt`: 859 lines match the word list, 187 of them are `t()` keys. **Estimate, not verified line by line.**
- Slash command loading in a Claude Code session: **not run**.
- Gradle, unit tests, device: not applicable.

## Maintainer test steps
1. Run `.claude/scripts/english-check.sh` on the branch. Expected: a summary line and no error.
2. Run `.claude/scripts/english-check.sh 1835e93~20`. Expected: CODE lines for the hard-coded German labels named above; judge two or three of them.
3. Type `/english-check` in a Claude Code session to see the command load.

## Status
Done on `claude/great-lovelace-tlhb6f`. Open for the maintainer: how hard-coded German UI strings are handled (section 5), and the archive file.
