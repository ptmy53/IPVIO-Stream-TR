---
name: release-notes
description: Write and check the user-facing release notes for a new IPVIO Stream version (CLAUDE.md section 18). Use when the maintainer prepares a release, bumps versionName, opens a release/* branch or says "release".
---

# Release notes for IPVIO Stream

Follow `CLAUDE.md` section 18. The notes are the GitHub release text and the update dialog text. A release without a notes file is not ready.

## Steps

1. **Read the version.** `versionName` in `app/build.gradle.kts`. Read the CHANGELOG section for this version (`## Unreleased` before the rename, or `## X.Y` after it). Also read the sections of any version users may skip (for example a broken release).
2. **CHANGELOG rename (section 18, step 1).** Rename `## Unreleased` to `## X.Y (versionCode N)`. Start a new empty `## Unreleased` above it. Do not change older sections.
3. **Write `docs/release-notes/vX.Y.md`** from `docs/release-notes/TEMPLATE.md`. Rules:
   - Users, not developers. No task IDs, class names, file paths or build internals. Internal-only changes go into one line "Internal changes" or are left out.
   - English only. For anything on screen, quote the English label from the app's English column (`Dict*.kt`). Never a German label.
   - The first four bullet lines of the whole file are the highlights. They are joined with " · " and must be at most 300 characters in total. Write them under `## Highlights` as the first section.
   - Name known limits honestly ("not device tested", "not measured", "needs your own API key").
   - Say whether the update keeps user data and whether it must be installed by hand.
4. **Check.** Run `.claude/scripts/check-release-notes.sh X.Y`. Fix every ERROR. Read each WARN and fix it or state why it stays.
5. **Report** to the maintainer: the notes file that the release will use, the highlight length, the check result, and the known limits you named. Do not publish the release. The maintainer does that.

## Facts to keep straight

- `release.yml` takes the first four bullets of `docs/release-notes/vX.Y.md` for `update.json` and cuts them at 300 characters. The file must be in `dev` before the merge into `main`. Without it, the release text falls back to GitHub's list of pull requests.
- Keep the English labels of the app. Check the label text in the `Dict*.kt` English column, not from memory.
- Do not state a test as passed unless it ran. Say "not device tested" where that is the case.
