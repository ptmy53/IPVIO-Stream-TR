<!--
Release notes template. Copy to docs/release-notes/vX.Y.md and delete this comment.
release.yml uses the file as the GitHub release text. Rules: CLAUDE.md section 18.
- Users, not developers: no task IDs, class names, file paths.
- The first four bullet lines under "Highlights" are what the app shows in the
  update dialog: release.yml copies them into update.json "notes", and the GitHub
  API fallback (AppUpdate.kt) reads them too. Together, joined with " · ", at most
  300 characters, or the app cuts them off (Round17Logic.kt).
- Leave out empty sections.
-->
## Highlights
- Most important change, one short line
- Second
- Third
- Fourth

## New
- 

## Improved
- 

## Fixed
- 

## Good to know
- Install over the old version; playlists, favorites, history and settings are kept (same signature).
- Known limits or things that need setup (e.g. own API keys).

**Full list of changes:** see `CHANGELOG.md`, section X.Y.
