---
description: Check that no protected identifier (CLAUDE.md section 5) was removed or renamed
---

Run the protected-identifier check for IPVIO Stream. Do not change any file.

1. Run `.claude/scripts/protected-check.sh $ARGUMENTS`. With no argument it compares against `origin/main`. Pass another ref (for example `origin/dev` if it exists, or a commit) to compare against that.
2. Report the script output as it is, then explain each reported item in one line:
   - **REMOVED or renamed:** stop. Say which SharedPreferences name, backup file name, manifest component or route is gone, and that CLAUDE.md section 5 and 6 need maintainer approval before the change goes on.
   - **ADDED:** for each new name, say where it is used and whether the change needs it. Do not judge the reason alone; ask if unclear.
   - **No change:** say so in one line.
3. The check covers SharedPreferences names and prefixes, the backup `NAMES` list (through the names it uses), manifest components and phone-remote HTTP routes in `PhoneImport.kt`. It does **not** cover the player core or playlist parsers. Say that in one sentence if the change touches them.
4. Exit code 1 means a removed identifier. Report it as a blocking finding.

Known gap: the route `/b/CODE` named in CLAUDE.md section 10 was not found in the code when this command was written (**unverified**). Do not treat its absence as a finding without checking the code first.
