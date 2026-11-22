# T-036 – Workflow state cleanup and project commands

## Goal
The state file `docs/tasks/WORK_STATE.md` had grown to 111 lines with several "current task" headers, duplicated entries and no release state after 1.4.4. Task IDs were used by more than one file. Add two project commands so a session starts from the same facts and new tasks get a free ID. No app code changes.

## Changes
- `docs/tasks/WORK_STATE-history.md` (new): the old WORK_STATE, unchanged content, with a header saying it is archived.
- `docs/tasks/WORK_STATE.md`: rewritten short (current task, release state, open device tests and decisions, task ID status, conventions). Release state taken from the git log, not from device reports.
- `.claude/commands/start.md` (new): `/start`, read-only status report.
- `.claude/commands/new-task.md` (new): `/new-task <title>`, next free ID, task file from the section list in `CLAUDE.md` section 7.
- `docs/tasks/T-036-workflow-state-and-commands.md` (this file).

## Not changed (on purpose)
- No app code, no Gradle, no CI workflow.
- No renaming of task files with duplicate IDs (T-009, T-014, T-026, T-027, T-032). Other files refer to them by name; renaming needs a decision.
- No CHANGELOG entry: the CHANGELOG is for users, and this change has no user-visible effect.
- German UI labels that appear in the archive (quoted from the old log) are not translated. Rewriting an archive is not a task of this change; open for the maintainer.
- `/protected-check` and `/release-notes` are not part of this task. Planned as later tasks.

## Acceptance criteria
1. `docs/tasks/WORK_STATE.md` names one current task and one next step, and states the latest release (1.4.7).
2. `docs/tasks/WORK_STATE-history.md` body is identical to the old WORK_STATE (checked with `diff`).
3. `/start` and `/new-task` exist as files in `.claude/commands/` with a `description` line.
4. `/new-task` computes the next ID from the file names and from WORK_STATE; before this task's file existed it gives T-036, afterwards T-037.

## Risks
- Facts in the new WORK_STATE come from the git log and the old log. Device test results after 21 Nov 2026 are not known and are marked as open.
- The commands are Markdown instructions. They depend on Claude Code reading `.claude/commands/*.md`; not yet run as slash commands in a session (see Test results).

## Documentation
- `docs/tasks/WORK_STATE.md` (updated), `docs/tasks/WORK_STATE-history.md` (archive).
- `CLAUDE.md` is not changed.

## Test results
- ID check on the repository: before this task the highest number in `docs/tasks/` was T-035, so the next free ID was T-036. After this task's file existed, the same check returns T-037 (run on 2026-11-22 with a shell command over the file names).
- Body of the archive equals the old WORK_STATE: **passed** (`diff`).
- Invoking `/start` and `/new-task` as slash commands: **not run** (the assistant cannot start a slash command in its own session). Maintainer test steps below.
- Gradle, unit tests, device: not applicable (no app change).

## Maintainer test steps
1. Open a new Claude Code session in the repository and type `/start`. Expected: at most 15 lines, current task T-036 or the next task named in WORK_STATE, no file changes.
2. Type `/new-task Test title`. Expected: a file `docs/tasks/T-036-test-title.md` (or the next free number if T-036 exists by then) with the section list above. Delete the test file afterwards.
3. Check that `docs/tasks/WORK_STATE-history.md` is in the repository and that WORK_STATE is short.

## Status
Done in the working tree, committed on `claude/great-lovelace-tlhb6f`. Open for the maintainer's test steps above.
