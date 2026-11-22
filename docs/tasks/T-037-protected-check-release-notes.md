# T-037 – Project checks: protected identifiers and release notes

## Goal
Two checks from the plan of T-036 that apply CLAUDE.md rules by a fixed procedure instead of by memory:
1. Protected identifiers (section 5): a removed or renamed SharedPreferences name, backup file name, manifest component or phone-remote route is found before commit.
2. Release notes (section 18): the notes file is checked for the rules that `release.yml` depends on (highlight length, no task IDs or file paths, data statement, CHANGELOG rename).

No app code changed.

## Changes
- `.claude/scripts/protected-check.sh` (new): compares the identifiers of a base ref (default `origin/main`) with the working tree. Exit 1 on a removed identifier.
- `.claude/scripts/check-release-notes.sh` (new): checks `docs/release-notes/vX.Y.md` (or a given file). Exit 1 on an error, warnings do not fail.
- `.claude/commands/protected-check.md` (new): `/protected-check [ref]`, runs the script and explains the output.
- `.claude/skills/release-notes/SKILL.md` (new): steps for writing the notes (CLAUDE.md section 18), ends with the check script.
- `docs/tasks/T-037-protected-check-release-notes.md` (this file).
- `docs/tasks/WORK_STATE.md`: current task.

## Not changed (on purpose)
- No CI workflow. Both checks are local tools; putting them into `build.yml` or `release.yml` needs approval (section 6).
- The release workflow is not changed; the check mirrors its highlight rule (first four bullets, 300 characters).
- `CHANGELOG.md`: no entry, no user-visible change.

## Known gaps
- The protected-check covers SharedPreferences names and key prefixes, `Backup.kt` names (through the literals), manifest components and the routes in `PhoneImport.kt`. It does not cover the player core or the playlist parsers (section 5), and names built at run time are not seen.
- The route `/b/CODE` named in CLAUDE.md section 10 was not found in the code (**unverified**). The command tells the reader to check before reporting it.
- `origin/dev` is not on the remote this clone sees (only `origin/main` and the working branch). The default base is therefore `origin/main`. Pass `origin/dev` when it exists.
- The release check is a rule check. It does not judge tone or whether the notes are true.

## Acceptance criteria
1. `protected-check.sh HEAD` on a clean tree prints "No change" and exits 0.
2. On a copy where one SharedPreferences name that occurs once is renamed, the script lists it under REMOVED and exits 1.
3. `check-release-notes.sh 1.4.7` passes; a sample with a task ID, a file path, no data statement and a too-long highlight fails with those errors.

## Risks
- False positives: a name used in another string (for example a test) can show as added. The script only reports; the reader decides.
- Pattern gaps: a renamed key built from a constant in another file is found only through its literal.

## Documentation
- `.claude/commands/protected-check.md`, `.claude/skills/release-notes/SKILL.md` describe the use.
- `CLAUDE.md` is not changed.

## Test results
- Protected check, HEAD against working tree: **passed** (no change, exit 0).
- Protected check, detection of an added name (base before `ipvio_privatecats` was introduced): **passed**; the output lists the added names.
- Protected check, detection of a removed name (throwaway worktree, `ipvio_plmode` renamed once): **passed** (REMOVED listed, exit 1). Worktree removed afterwards.
- Output format after the fix for word splitting: re-run after the fix showed one entry per line (the detection run before the fix did not).
- Release check on `docs/release-notes/v1.4.7.md`: **passed** (226 of 300 characters, no errors).
- Release check on the template and on a constructed bad sample: **as expected** (errors reported for the bad sample: task ID, file path, no data statement, missing CHANGELOG section).
- Slash commands and skill loading in a Claude Code session: **not run** (not possible from the assistant's own session). Maintainer test steps below.
- Gradle, unit tests, device: not applicable.

## Maintainer test steps
1. Run `.claude/scripts/protected-check.sh` on the branch. Expected: "No change in protected identifiers" or only ADDED lines you can explain.
2. Rename one SharedPreferences name in a scratch copy (not in the branch) and run the script. Expected: REMOVED with that name, exit 1.
3. Run `.claude/scripts/check-release-notes.sh 1.4.7`. Expected: "OK: no errors."
4. Type `/protected-check` and `/start` in a Claude Code session to see the commands load.

## Status
Done on `claude/great-lovelace-tlhb6f`. Test steps above for the maintainer.
