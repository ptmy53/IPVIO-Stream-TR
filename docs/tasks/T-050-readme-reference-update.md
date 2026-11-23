# T-050: README update (reference layout)

**Status:** done on branch `claude/festive-tesla-n0jepr`, open for review. Documentation only, not built.

## Goal

Take the useful parts of the layout of another open-source IPTV README (`vantaflow/ultra-tv`) into `README.md`, only where they fit this project and are backed by the code or existing docs. No product features were copied.

## Affected files

- `README.md`: four additions, no existing text removed.
- `CHANGELOG.md`: one line under "Unreleased".
- `docs/tasks/T-050-readme-reference-update.md`: this file.

## Changes

1. Quick start: a short "If the install fails" note with a link to `docs/FAQ.md`.
2. New section "Privacy and security", based on `docs/SECURITY.md` (items 1, 13, 16 and the VPN/DNS notes).
3. New section "Known limitations", based on the 1.4.8 CHANGELOG and `docs/SUBTITLES.md`.
4. Building: link to `docs/TESTING.md` for the test list.

## Not taken over, on purpose

- Badges: CLAUDE.md section 12 allows badges only for workflows that succeed. The CI status could not be checked from the session.
- Download table per device, Samsung/LG/Windows/macOS packages, cloud dashboard, sports APIs, media servers, adb install commands: the project does not have these, or they are not verified.
- Feature list and screenshots: unchanged. T-026 already rewrote them.

## Acceptance criteria

- Every new statement matches `docs/SECURITY.md`, `docs/SUBTITLES.md`, `CHANGELOG.md` or the code (`minSdk` 24, Settings paths as named in the CHANGELOG).
- No German text, no German UI labels (CLAUDE.md section 4).

## Risks

- Public text: the VPN and DNS wording follows the 1.4.8 CHANGELOG and is not device tested.

## Test steps for the maintainer

1. Open `README.md` on GitHub and check the four new parts render as expected.
2. Follow the links: `docs/FAQ.md`, `docs/SECURITY.md`, `docs/SUBTITLES.md`, `docs/TESTING.md`, `CHANGELOG.md`.
3. Check that the Settings paths in the text (*Settings > General > VPN*, *Settings > General > DNS server*, *Settings > Sports Hub*) match the app.

## Test results

- Documentation only. No Gradle build and no unit test were run. Links were checked against the files in the repository.
