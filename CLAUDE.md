# CLAUDE.md – IPVIO Stream

Working rules for AI-assisted changes to this repository. Read this file first in every session.

## 1. Project

- **IPVIO Stream**: free, open-source IPTV player (GPL-3.0) for Android TV, Google TV and Fire TV.
- Stack (from project notes, re-verify in the code): Kotlin, Jetpack Compose without material3, Media3/ExoPlayer, OkHttp, Coil, WorkManager. Package `com.ipvio.stream`.
- The app is in active use. Existing features and user data must be preserved.
- Persistence seen so far: SharedPreferences and app files. Verify whether a database exists before writing anything about migrations.
- Google Drive backup and sync are in the code again since 1.4.2 (`DriveSync.kt`, `drive.file` scope). Keep the BuildConfig fields and `docs/GOOGLE_DRIVE.md` in line with the code.

## 2. Principles

1. Refactor, do not rewrite.
2. Protect user data and user-visible behaviour first.
3. Every change needs a technical reason. Do not restyle working code.
4. Do not invent classes, dependencies or architecture. Mark anything unverified as **unverified**.
5. Prefer the smallest change that solves the problem.
6. One task, one problem. Do not mix unrelated issues.

## 3. Environment limits

- The assistant cannot run Gradle, has no Android SDK, and cannot test on a device.
- Checks that can run: syntax checks of changed files and pure-Kotlin logic tests (JUnit, no Android imports).
- The maintainer builds in Android Studio and tests on device or Fire TV. Every change ends with concrete test steps for the maintainer.
- Never report tests as passed unless they were actually run. State which checks ran and which did not.
- Work reaches the repository through feature branches and pull requests into `dev`. Nothing goes straight to `main` (section 16). Commit messages are in English.

## 4. Language

- Everything in the repository is in **English**: comments, KDoc, identifiers, file names, test names, commit messages, PR text, README, CHANGELOG, `docs/`.
- Conversation with the maintainer may be in German.
- **No German terms on GitHub, ever.** This covers CHANGELOG, README, `docs/`, release notes, release text, PR and issue text, commit messages and code comments. When text refers to something on screen, quote the English label from the dictionaries (the English column in `Dict*.kt`), never the German key. Exception: the German source strings inside the `Dict*.kt` dictionary files themselves (they are the translation keys).
- Existing German identifiers and comments are translated in stages (see `docs/REFACTORING-PROGRESS.md`), not all at once.

## 5. Protected areas (no change without an approved plan)

- **SharedPreferences names and keys** (e.g. `ipvio_sportfavs`, `ipvio_sportleagues`, `ipvio_sporthub`). Renaming loses user data.
- **Stored file names, cache formats, backup JSON keys** (`Backup.kt`, list `NAMES`). Profile files use the suffix `_p<ID>`; profile 0 uses the original names.
- **Manifest components** and any class referenced by name (receivers, services, activities).
- **Phone-remote routes and parameters** (`/r`, `/b/CODE`).
- **UI strings**: the German text is the translation key (`I18nCore`, `DictJ`, `DictNl5`). Do not change without a decision by the maintainer.
- **Player and IPTV core**: `PlayerScreen.kt`, playlist sources (Xtream, Stalker, M3U), EPG.

## 6. Approval required before implementation

- Storage format changes with any risk to user data.
- Changes to signing, authentication, secrets handling, or phone-to-TV transfer.
- Removing any feature.
- Large changes to the player or IPTV core.
- Adding, removing or upgrading dependencies.
- Gradle restructuring or new modules.
- New CI workflows, or automatic formatting of the whole project.
- Anything with a visible effect on existing users.

## 7. Tasks and workflow

- Every task has an ID (`T-001`, `T-002`, …) and lives in `docs/tasks/`. Each task file states: goal, affected files, acceptance criteria, risks, documentation needed, test results, status.
- Split large work into subtasks. Work only on the assigned task. List dependencies between tasks.
- Large features and refactorings: plan first, implement only after approval. Simple bug fixes: no planning phase.

`ANALYZE → PLAN → APPROVAL → IMPLEMENT → VALIDATE → DOCUMENT → REPORT`

- **Analyze:** read the relevant code, find callers, note risks. Do not assume a technology is present. Do not analyse the whole codebase for a single task.
- **Plan:** list affected files and the reason for each change.
- **Implement:** keep unrelated code untouched.
- **Validate:** run what is possible (section 3); list the rest as test steps.
- **Document:** update the affected file under `docs/` and `CHANGELOG.md`.
- **Report:** what changed, which files, why, which checks ran and passed, what remains open, what the risks are.

## 8. Session handoff

- Before ending a session or switching chats, update `docs/tasks/WORK_STATE.md`: current task, completed steps, changed files, test results, open problems, next step.
- Start a new session by reading `CLAUDE.md` and `docs/tasks/WORK_STATE.md`. Do not repeat analyses already recorded in `docs/`.
- Use separate sessions for independent tasks.

## 9. Subagents

- Use subagents only for large, clearly separated read-only questions, e.g. "list every place that reads `ipvio_sportfavs`" or "review this file for security issues".
- Each subagent gets a precise brief and only the relevant files.
- No parallel edits to the same file.
- Results are checked by the main agent before use. The main agent owns architecture and integration.
- Simple tasks: no subagents.

## 10. Security rules

- No API keys, passwords, tokens or payment links in source code or logs.
- Do not print secrets in reports. Never commit `secrets.properties`, `keystore.properties` or key files.
- Validate input from playlists, phone-remote requests and backups.
- Do not weaken security settings to make a build pass.
- Analyse the impact before any change to authentication, storage or network code.
- Known open items (from project notes, verify before acting):
  - Phone-remote pages use plain HTTP on the local network.
  - `Secrets` falls back to plaintext on devices without an Android keystore.
  - `PhoneImport` failure counter is not atomic; the access code has 4 digits.

## 11. Quality tools

ktlint, detekt, Android Lint, mobsfscan, OSV-Scanner. Rules:

- Baseline first. Existing findings are recorded as **existing**; new findings must not slip in unnoticed.
- No blanket suppressions. Suppress a single finding only with a written reason.
- No project-wide auto-formatting without approval.
- Reports go to `docs/quality/` (one baseline file per date).
- Tool versions are checked for compatibility before they are added.

## 12. CI

- `.github/workflows/build.yml`: unit tests and a debug build on push to `main` and `dev` and on pull requests.
- `.github/workflows/release.yml`: runs on push to `main` (a merge from `dev`). Steps: unit tests, lint report, signed release build, then a GitHub release `v<versionName>` with `IPVIO-Stream.apk` and `update.json`. The publish step runs only when the tests are green. An existing tag stops the run.
- Changing either workflow needs the maintainer's approval (section 6).
- Actions in `release.yml` are pinned to commit SHAs. `build.yml` still uses `@v4` tags (open item).
- The signing key and its passwords are GitHub secrets only. Never in the repository or in logs.
- README badges only show workflows that exist and succeed.

## 13. Documentation

Maintained in `docs/` (English), kept in line with the code:

- `ARCHITECTURE.md` – arc42 overview (12 chapters). Start here when you need the structure. Facts are checked against the code; unknowns are marked TODO.
- `DIAGRAMS.md` – C4 level 1 and 2 in Mermaid
- `adr/NNNN-title.md` – one decision record per architectural decision (not for every small change)
- `FEATURES.md`, `IPTV.md`, `PLAYER.md`, `SECURITY.md`, `TESTING.md`, `TECH-DEBT.md`, `REFACTORING-PROGRESS.md`
- `RELEASE.md` (exists); `CHANGELOG.md` at repo root, updated for every task
- `release-notes/vX.Y.md` – user-facing notes for every GitHub release (section 18)

Documentation must describe the actual code. Mark unknowns as unknown.

Task state and progress live in `docs/tasks/WORK_STATE.md` and `docs/REFACTORING-PROGRESS.md` (the restructuring steps U-1 to U-5 are listed there).

## 14. Uncertainty

If you do not know how something works: read the code first. Do not guess. Ask before any risky decision. When several options exist, compare maintainability, stability, complexity, performance, security impact and migration risk, and recommend the option with the best balance.

## 15. Current state

The status list lives only in `docs/tasks/WORK_STATE.md` and `docs/REFACTORING-PROGRESS.md`. It is not repeated here, because it went stale.

## 16. Branches and releases

- `main`: releases only. It changes only through a merge commit from `dev`, via a pull request. No direct commits.
- `dev`: development. Feature and fix branches start from `dev`; pull requests go into `dev`.
- Merge `dev` into `main` with **"Create a merge commit"**, not squash. This keeps `dev` related to `main`.
- Prepare a release in `dev`: raise `versionCode` and `versionName` in `app/build.gradle.kts`, add the CHANGELOG entry. After the merge into `main`, `release.yml` publishes `v<versionName>`. Manual fallback: `docs/RELEASE.md`.
- No force pushes to `main` or `dev`.

## 17. Tests for every change

- Every feature, bug fix or change of existing behaviour in `app/src/main` comes with a test in `app/src/test/java/com/ipvio/stream/`. If that is not possible (device-only behaviour), the pull request says why and lists the manual test steps.
- Test names are in English. Put logic that can run without Android into `*Logic.kt` files without Android imports, so JVM tests can cover it.
- The test list (planned and existing checks) is in `docs/TESTING.md`.
- CI warns when `app/src/main` changes without a change under `app/src/test`. The warning does not block.
- Claude cannot run Gradle here. Report a test as passed only when CI or the maintainer shows it.

## 18. Release notes

Whenever a release is prepared (version bump in `app/build.gradle.kts`, a `release/*` branch, or the maintainer says "release"), the assistant always writes the release notes as part of the same change. A release without notes is not ready.

1. **CHANGELOG.md:** rename `## Unreleased` to `## X.Y` (the new `versionName`) and start a new empty `## Unreleased` above it. Do not rewrite older sections.
2. **Release notes:** create `docs/release-notes/vX.Y.md` from `docs/release-notes/TEMPLATE.md`. Source is the CHANGELOG section of this version (and of any version users may skip, e.g. a broken release). Rules:
   - Written for users, not developers: no task IDs, class names, file paths or build internals. Internal-only changes go into one line "Internal changes" or are left out.
   - English only (section 4). Never quote a German UI label. Use the English label from the app's English column (`Dict*.kt`), for example "Make private", not "Privat machen". The same applies to the CHANGELOG, README, docs, PR text, issue text and the GitHub release text.
   - The first four bullet lines under the first heading are the highlights. The update dialog shows them: `release.yml` copies them into the `notes` of `update.json`, and the app's GitHub API fallback reads the same lines (`AppUpdate.kt`). Joined with " · " they must stay within 300 characters (`Round17Logic.kt` cuts the rest).
   - State known limits honestly (e.g. "not measured", "needs your own API key").
   - Say whether the update keeps user data and whether it must be installed by hand.
3. **Publishing:** the file must be in `dev` before the merge into `main`. `release.yml` then uses it as the GitHub release text; without the file it falls back to GitHub's list of pull requests.
4. **Report:** tell the maintainer which notes file the release will use.

The assistant does not publish releases; the maintainer does.
