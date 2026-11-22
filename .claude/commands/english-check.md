---
description: Report German words in lines added since a base ref (CLAUDE.md section 4). Read only.
---

Run the English check for IPVIO Stream. Do not change any file.

1. Run `.claude/scripts/english-check.sh $ARGUMENTS`. With no argument it compares against `origin/main`. Pass another ref to compare against that. The check includes uncommitted changes and new untracked files.
2. Report the summary line (TEXT, CODE+COMMENT, KEY counts). Then list the findings by class:
   - **TEXT:** an error on GitHub (docs, CHANGELOG, README, release notes). Quote the line and propose the English wording.
   - **COMMENT:** a German code comment. Propose an English wording. These are errors under CLAUDE.md section 4.
   - **CODE:** a German string in code outside `Dict*.kt`. Say whether it is a UI string that should go through `t("German", "English")` or a dictionary row, or whether it is an internal text. Do not change it; the maintainer decides how UI strings are handled (section 5).
   - **KEY:** allowed. Only count them, do not list them.
3. Say clearly that the check is a word list. A German word that is also an English word is not found, and a match is not always German. Judge each line.
4. The check never fails. Do not present its output as a passed test.

Note: the archive `docs/tasks/WORK_STATE-history.md` contains German texts from earlier sessions. It shows up as TEXT. Do not edit it in this task; it is listed as an open item in `docs/tasks/WORK_STATE.md`.
