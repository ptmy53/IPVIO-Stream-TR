# T-030: import of a Trakt export ZIP (issue #110) — plan only

**Status:** postponed (19 Nov 2026). Wish: the maintainer prefers the live API sync (T-031) and skips the one-time import. Plan kept for reference. No code. Needs the maintainer's approval before implementation (CLAUDE.md section 6: new data handling, storage).

The export file itself is personal data and is not in the repository. This plan only lists its structure and counts.

## What the export contains (checked 19 Nov 2026, structure and counts only)
- `watched-history-1…32.json`: 7,866 entries (7,269 episodes, 597 movies). Fields: `watched_at`, `action` (watch, scrobble, checkin), `type`, `movie` or `show` + `episode` (season, number, title, ids).
- `watched-movies-1…3.json`: 513 watched movies, each with `plays`, `last_watched_at`, `movie.ids`.
- `watched-shows.json`: 250 shows with `plays`, `last_watched_at`, `show.ids`.
- `collection-episodes-1…4.json`: 1,000 collected episodes. `collection-shows.json`: 4 shows.
- `ratings-*.json`, `lists-watchlist.json`, `lists-favorites.json`: empty.
- Identifiers on the entries: `imdb`, `tmdb`, `tvdb`, `trakt`, `slug`, `plex`.
- `user-profile.json`, `user-settings.json`: account data, not needed for the import.

## What the app has today (read in the code)
- Items from the playlist carry no IMDb or TMDB id (`Item` in `Models.kt`). Matching is by title and year (`MatchLogic`, `TitleText`).
- Watch progress is stored per title and per episode (`EpProgress`, `WatchStore`, key `ipvio_…`). The detail page shows "watched" and "play again" from it.
- A backup JSON (`Backup.kt`) carries these keys; the key list must not change without approval (CLAUDE.md section 5).

## Options to decide
1. **What to import:** watched movies and episodes only (recommended), or also collection (1,000 episodes) and the favourites/watchlist (empty in this export).
2. **How to match:** by TMDB/IMDb id via the metadata already fetched (`MetaRepo`), or by title and year as a fallback. Recommended: title and year first, id only when the metadata already has it; unmatched entries are listed, not dropped.
3. **Where to store:** reuse the existing progress store (marks an item as watched) instead of a new key. Recommended, so existing backups and restore keep working.
4. **Overwrite or merge:** only mark titles that have no local progress (recommended), never reduce existing progress.
5. **Input:** the ZIP is read on the device (file picker or phone upload), validated (ZIP size limit, JSON parse, field types) before anything is written (CLAUDE.md section 10).

## Risks
- Personal data: the ZIP never goes into the repository, the logs or any external request.
- Wrong matches mark the wrong titles as watched. Mitigation: a report of matched and unmatched counts, and a dry run before writing.
- Large files: about 4.6 MB of JSON in this export; the parser must not load everything at once (memory limit of the device).
- Storage: any new key or changed format needs a separate approval.

## Tests (later, after implementation)
- JVM tests for the parser and the matcher with a small synthetic export (no real titles).
- On the device: import the test ZIP, check counts in the report, check that matched titles show "watched" and unmatched ones are listed, check that existing progress is not lowered.
