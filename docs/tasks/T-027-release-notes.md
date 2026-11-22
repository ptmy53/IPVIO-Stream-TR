# T-027 Release notes for every release

## Goal

Maintainer feedback (12 Nov 2026): GitHub releases have no proper release notes. Every release should show users what changed, on GitHub and in the app's update dialog.

## Change

- `CLAUDE.md`: new section 18 "Release notes" (rule for the assistant: close the CHANGELOG section, write `docs/release-notes/vX.Y.md` in `dev` before the merge into `main`); section 13 lists the new folder.
- `docs/release-notes/TEMPLATE.md`: template (Highlights, New, Improved, Fixed, Good to know).
- `.github/workflows/release.yml` (**needs the maintainer's approval**, CLAUDE.md section 6):
  - "Publish GitHub release": uses `docs/release-notes/v<versionName>.md` as the release text (`--notes-file`); without the file it keeps `--generate-notes`.
  - "Create IPVIO-Stream.apk and update.json": adds `notes` to `update.json` from the first four highlight lines of the same file (joined with " · ", cut to 300 characters, built with `jq`). Without the file `update.json` stays as before.
- `docs/release-notes/v1.4.3.md`: notes for the current release. 1.4.2 was published without notes and `CHANGELOG.md` has no 1.4.2 section, so the 1.4.2 items come from the text of the pre-release `V1.4.2-test`. The maintainer should check them.
- `docs/RELEASE.md`, section 2: what `release.yml` does with the notes file; title format for the manual fallback.
- `CHANGELOG.md`: entry under Unreleased.

## Findings used

- `release.yml` (before): `gh release create … --generate-notes`, and `update.json` without `notes`. Release `v1.4.3` therefore only lists pull request titles, and the update dialog shows no text.
- `UpdatePrompt.kt`: the dialog shows `notes` under the version when it is not empty.
- `AppUpdate.kt`: `update.json` is the primary source; the GitHub API fallback shows the first four non-empty lines of the release text that do not start with `#`, joined with " · ".
- `Round17Logic.kt` and `AppUpdate.kt`: notes are cut to 300 characters.

## Not changed

App code, `update.json` fields other than `notes`, `build.yml`.

## Acceptance criteria

1. A release with `docs/release-notes/vX.Y.md` in `main` gets that file as its GitHub release text.
2. Its `update.json` contains `notes` with the four highlights, at most 300 characters; the update dialog shows them.
3. A release without the file behaves as before (`--generate-notes`, no `notes`).

## Risks

- Workflow change: a mistake in the publish step can stop a release. The file check falls back to the old behaviour, and an existing tag still stops the run.
- `jq` and `awk` must be on the runner (`ubuntu-latest` has both).

## Test results

Ran locally (Git Bash): the `awk`/`paste`/`sed` extraction on `v1.4.3.md` gives the four highlights, 238 characters. **Not run:** the workflow itself, `jq` on the runner, a real release. The first real check is the next release.

## Status

Branch `docs/T-027-release-notes`, rebased on `dev`; PR #66 to `Emrodev/IPVIO-Stream` (base should be `dev`).
