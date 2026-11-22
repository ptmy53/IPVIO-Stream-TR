# T-031: live sync with Trakt through its API — plan only

**Status:** plan. No code. Needs the maintainer's approval (CLAUDE.md section 6: authentication, secrets, network, storage). The one-time import (T-030) stays separate and does not depend on this.

**Verification status:** the facts below come from community sources (apis.io overview, trakt-rs and trakt.py examples). The official Trakt documentation (trakt.docs.apiary.io) could not be reached from the session, so every endpoint, header, token lifetime and rate limit is **unverified** until it is checked against that documentation.

## Goal
Keep the watch history, watchlist and favorites of a user in step with their Trakt account, in both directions, without a manual export.

## Model (to verify)
- **One API application for the project.** The maintainer registers the app at Trakt once and gets a client ID and client secret. Users do not create their own keys.
- **Per-user login with OAuth.** Device code flow, suited to TV: the app shows a code, the user enters it on trakt.tv/activate on a phone. The app then holds an access token and a refresh token for that account.
- **Sync:** history, watchlist, collection, favorites, ratings and playback progress. `last_activities` tells the app what changed since the last sync, so it does not download everything each time.

## Decisions the maintainer must make
1. **Client secret in an open-source app.** It is visible to anyone who reads the APK or the source. Need to check whether Trakt allows the device flow without it, or accept the exposure for a non-commercial app. Recommendation: check first; do not commit the secret to the repository (CLAUDE.md section 10). Store it in GitHub secrets for the build if needed.
2. **Token storage.** Access and refresh tokens per profile, encrypted with `Secrets`. Known weakness: `Secrets` falls back to plain text on devices without a keystore (CLAUDE.md section 10, open item). Recommendation: refuse to store the tokens when no keystore is available, instead of falling back.
3. **Direction of sync.** Both ways, or Trakt → app only at first (lower risk, no writes to a user's account). Recommendation: start with read-only (Trakt → app), then add writes after a test.
4. **Conflicts.** Local watch progress and Trakt history can disagree. Recommendation: a timestamp rule (the newer change wins) and never lowering progress.
5. **When to sync.** At start and when the app is in front, with a rate limit. No background service for the first version.
6. **Settings and privacy.** A visible switch "Trakt-Konto verbinden" and "Trennen" (delete the tokens). The tokens never go into logs, backups or error reports.

## Affected code (to read before implementation)
- Watch progress and "watched" state (`EpProgress`, `WatchStore`, `Playback`).
- Backup keys (`Backup.kt`, list `NAMES`): tokens must be excluded or handled by a decision.
- Network: a new OkHttp client with the API headers; the existing `TlsFallback` must not apply to Trakt (it is not a provider host).
- Settings page and the dictionary (new German keys need translation rows).

## Risks
- Account security: a leaked secret or token allows writes to the user's Trakt account.
- Trakt terms of use and rate limits: to check in the official documentation.
- Data loss: a wrong merge could mark titles unwatched. Mitigation: read-only first, dry run, never lower progress.
- Offline use: the app must work when Trakt is unreachable.

## Steps (after approval)
1. Verify the official Trakt documentation (endpoints, headers, token lifetime, rate limits, terms) — needs trakt.docs.apiary.io to be allowed in the session network, or the maintainer checks it.
2. Register the app at Trakt (maintainer).
3. Device-flow login and token storage, with JVM tests for the token state logic.
4. Read-only sync of history with `last_activities`, JVM tests for the merge rule.
5. Settings screen and texts.
6. Device test (login on Fire TV via phone code, sync, disconnect).
7. Write-back only after a separate decision.
