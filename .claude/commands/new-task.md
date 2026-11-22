---
description: Create a new task file with the next free ID (T-NNN) in docs/tasks
---

Create a task file for IPVIO Stream. The task title is: $ARGUMENTS

If no title is given, ask for one and stop.

1. Find the next free ID. Take the highest `T-NNN` in the file names in `docs/tasks/` and in the IDs named in `docs/tasks/WORK_STATE.md`, and add 1. Use three digits. Do not reuse a number, even one that is only used by an old file.
2. File name: `docs/tasks/T-NNN-<short-kebab-case-title>.md`. The title and all text in the file are in English (`CLAUDE.md` section 4).
3. Write the file with these sections, in this order, and leave the ones you cannot fill as "TBD":
   - `# T-NNN – <title>`
   - `## Goal`
   - `## Changes` (affected files and the reason for each; nothing else)
   - `## Not changed` (protected areas from `CLAUDE.md` section 5 that the task must not touch)
   - `## Acceptance criteria` (numbered, each one checkable)
   - `## Risks`
   - `## Documentation` (which `docs/` file and `CHANGELOG.md` entry will change)
   - `## Test results` (write "not run" for every check that has not run; never "passed" without evidence)
   - `## Status` (`planned`)
4. If the task touches a protected area or an approval item in `CLAUDE.md` section 6, add a line at the top of `## Status`: "Needs maintainer approval before implementation."
5. Do not create a branch, do not commit, and do not change WORK_STATE. Report the new file path and the ID in one line.
