# T-032 – Show the age rating (FSK) of movies and series

## Goal
Movies and series show their age rating (e.g. "FSK 12") in the info line next to year, genre and runtime.
Display only. A filter by age rating is out of scope (see "Not done").

## Source
- TMDB, in the detail request the app already makes (`MetaRepo.extended`): `append_to_response` gets `release_dates` (movies)
  or `content_ratings` (series). No extra request, same TMDB key.
- Country from the TMDB language of the app (`Lang.tmdb()`, e.g. "de-DE" -> DE). A German number is shown as "FSK n".
- No rating for that country: the US rating, marked "(US)".
- Still none: OMDb's `Rated` field from the OMDb request that already runs (US rating, marked "(US)" outside the US).
- Without a TMDB key but with an OMDb key: the existing OMDb title lookup for provider info (`withOmdbTitle`, Xtream titles)
  also reads `Rated`. Entries that already have an IMDb rating are asked once more for the age rating.
- No key at all: no age rating. The app ships no TMDB or OMDb key (`Config.kt`), and the release build gets none.

## Changes
- `AgeRatingLogic.kt` (new, no Android): country, choice of the certification, label.
- `Meta.kt`: `Meta.age`, `Meta.ageChecked`; TMDB request extended; `ageByCountry` reads the answer; OMDb fallback;
  cache file gets the keys `age` and `agec` (additive).
- `HomeParts.kt`: `HeroMetaLine` shows `age` as the last part (detail page, billboard, home preview, search).

## Storage (CLAUDE.md section 5)
The per-title cache in `files/meta3/` gets two new JSON keys. Old files load as before (missing keys = no rating, not asked).
Entries without `agec` are fetched once again when a detail page opens, then stored with the rating. The slim index
(`_slim`) is unchanged. No backup or preference keys change.

## Acceptance criteria
- A German movie with FSK at TMDB shows "FSK n" in the info line (UI language German).
- UI language English shows the US rating without a marker.
- A title without any rating shows no extra part, and nothing else changes.

## Tests
- `AgeRatingLogicTest` (JVM): written, **not run locally** (no JDK on this machine). CI runs it on the pull request.
- Not built with Gradle, not tested on a device.

## Test steps for the maintainer
1. TMDB key set, UI language German. Open a known movie (e.g. a Marvel film): info line ends with "FSK 12".
2. Open a series (e.g. "Breaking Bad"): "FSK 16".
3. Open a title that was opened before this version: the rating appears (one extra TMDB request on this first open).
4. Switch the UI language to English, open the same movie: "PG-13".
5. Without a TMDB key, with an OMDb key, Xtream playlist: a movie shows e.g. "PG-13 (US)".
6. Without any key: nothing changes.

## Not done
- Filter by age rating: ratings are only known for titles whose detail page was opened, so a catalog-wide filter would need one
  request per title.
- Parental control by age rating.

## Status
Implemented, not built, not device tested.
