# T-035 – Remove the "Sublists" settings page

## Goal
The settings page "Sublists" (sublists) is removed. It was a per-playlist filter applied while loading, with no
clear purpose next to "Genres & categories". Approved by the maintainer ("Sublists can go", steps 1–4).

## Changes
- `SettingsHub.kt`: settings card "Sublists" removed; `SublistsPage` and the two chip helpers
  (`SelectRowSublist`, `MultiSelectRowSublist`) removed; `SettingsNav.SUBLISTS` removed.
- `SettingsHub.kt`: page numbers after the removed card move down by one, so every page number again equals its card
  position (`SettingsNav.PROFILES`, `HOME`, `RESET_ALL` already had the post-removal numbers). Before, the profile
  circle opened the sublist page (10), and "About", "Reset all" and "Spende" opened the wrong pages (see the
  mapping in the `when`).
- `SublistStore.kt`: `clearActiveOnce(c)` removes every active selection (`sublist_active_<hash>`) once, guarded by the
  flag `sublists_active_cleared`. Pure helper `activeKeys(...)`. Saved definitions (`sublists`) are not touched.
- `MainActivity.kt`: `SublistStore.clearActiveOnce(this)` after `Profiles.init`.
- `SublistStoreTest.kt`: test for `activeKeys`.

## Not changed (on purpose)
- Load filter in `PlaylistStore.kt`, `DetailScreen.kt`, `HomeParts.kt`, `HostFailover.kt`, and `SublistLogic.kt`:
  IPTV core, protected by `CLAUDE.md` section 5. Removing it needs its own approval.
- The "Sublists" field in the add-playlist dialog: no such field exists in the code any more. The translation strings
  in `DictA.kt` and `DictPl8.kt` are left untouched (UI strings need the maintainer's decision).
- Backup format: sublist keys are not part of `Backup.kt`.

## Acceptance criteria
1. The settings list has no "Sublists" card; the remaining cards open their own pages.
2. Profile circle on the home screen (one profile or more) opens the Profiles page.
3. After the first start with this version no playlist stays filtered by an old sublist selection.
4. Saved sublist definitions are still in SharedPreferences (`sublists`), nothing is deleted.

## Risks
- Renumbering the settings pages: a mistake sends a card to the wrong page. Covered by criterion 1 and the test steps.
- `clearActiveOnce` runs before the playlist loads. Users who had an active sublist see the full playlist after the
  update, which is the intended effect.

## Test results
- Unit test `SublistStoreTest.activeKeysPicksOnlyActiveSelections`: **not run** (no Gradle or Kotlin compiler here).
- Brace/parenthesis balance of the changed files: the same counts as before the change for `SettingsHub.kt`
  (the crude check reports a constant offset from string literals). Other changed files balance.

## Test steps (maintainer)
1. Build the debug APK in Android Studio and run `./gradlew test` (or the CI unit test job).
2. Open Settings: the card "Sublists" is gone. Open each of the last cards (Profiles, API keys, Parental, Storage,
   Diagnose, Import lists, Boomer, Home screen, About, Reset all, Donate) and check that the page title matches the card.
3. Press the profile circle on the home screen: the Profiles page opens.
4. If an active sublist existed before the update: after the first start, the playlist shows all channels.
5. Start the app a second time: nothing else changes.

## Status
Implemented, not device tested. Committed on `claude/clever-lovelace-eeerp5`.
