---
description: Read the project state and report where work stands (read only)
---

Start a session for IPVIO Stream. Do not change any file.

1. Read `CLAUDE.md` and `docs/tasks/WORK_STATE.md`. Do not read the archive `docs/tasks/WORK_STATE-history.md` unless a task needs it.
2. Run `git status --short` and `git log --oneline -5` on the current branch.
3. List `docs/tasks/` and open only the task file named as current in WORK_STATE.

Then report in at most 15 lines, in the language the maintainer used:

- the current task and its status, as written in WORK_STATE;
- uncommitted changes in the working tree, if any;
- the device tests and maintainer decisions still open (names only, no repeat of the steps);
- the next step from WORK_STATE.

If WORK_STATE and git disagree (for example a branch that WORK_STATE names does not exist, or the latest release differs), say so and do not decide which is right.

Do not re-analyse code that a `docs/` file already describes.
