# ADR 0006: Branches main and dev, release pipeline, tests per change

## Status

Accepted, 15 Nov 2026. Rules are in [CLAUDE.md](../../CLAUDE.md) sections 16 and 17. The workflows are `release.yml`, `pr-checks.yml` and `build.yml`.

## Context

- The remote had only `main`. Release work and feature work were mixed, and many branches were never deleted (about 45 remote branches before cleanup on 15 Nov 2026).
- Releases were built by hand: signing, `update.json` with SHA-256, GitHub release. Errors there break the in-app update for all users (see `docs/RELEASE.md`).
- Tests run in CI (`build.yml`), but nothing stopped a release when they failed.

## Decision

- `main` holds releases only. It changes only through a pull request from `dev`, merged with a **merge commit**.
- `dev` is the development branch. Feature and fix branches start from `dev`.
- A push to `main` runs `release.yml`: unit tests first. Only if they pass, the job builds the signed APK, writes `update.json` and publishes the GitHub release `v<versionName>`. An existing tag stops the run.
- Every change in `app/src/main` comes with a test, or the PR lists manual device test steps. `pr-checks.yml` warns but does not block.
- Lint runs in `release.yml` as a report only, until a lint baseline file exists.

## Consequences

- Publishing is automatic. Users see the new version in the app's update check as soon as the release is public. This is intended by the maintainer, and it means a broken release reaches users at once. The release must therefore be correct before merging into `main`.
- Branch protection on GitHub (no direct pushes, required status `build`) is a repository setting, not code. It must be set by an admin. Until then the rule is only a convention.
- Signing secrets live only in GitHub. Losing the key still means every user has to reinstall.
- The test rule is enforced by review and by a warning. A hard block can be added later.
- Open: lint baseline file, `versionCode` increase check (T4 in `docs/TESTING.md`), pinning of `build.yml` actions to SHAs.
