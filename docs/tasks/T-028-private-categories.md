# T-028 – Private categories

**Status:** implemented 2026-11-19, compiles, JVM tests pass (`PrivateLogicTest` 6/6). **Not device tested.**

## Goal

The user marks a category as private: a Live TV category, a movie category or a series category. Content from a private category:

- is never recorded: no "Recently watched", no "Continue watching", no last channel, no resume log;
- is never shown outside that category: home rows, the player's recent-channels strip, search, "All channels", Multi view, Boomer mode and the phone remote;
- stays fully usable inside the category itself.

## What exists today (analysis 2026-11-19)

| Area | Store / code | Today |
|---|---|---|
| Recently watched | `History` (`Store.kt:416`, prefs `ipvio_hist`, key `ids`); written by `History.add` from `MainActivity.kt:260`, `PlayerScreen.kt:349`, `PlayerScreen.kt:578` | Records everything |
| Continue watching / resume | `WatchStore.save` (`Store.kt:476`, prefs `ipvio_watch`), called from `PlayerScreen.kt:570` | Records everything |
| Resume log (diagnostics, contains titles) | `ResumeLog.add` (`Store.kt:452`) | Records everything |
| Last Live TV category | `LiveMemory.setSel` (`LiveTab.kt:48`, prefs `ipvio_live`, key `sel`) | Remembers any category |
| Start-up: resume last channel | `MainActivity.kt:300`, `386`, `432` read `History.ids` | Can start a private channel |
| Player recent-channels strip | `RecentChannelsRow` (`PlayerLive.kt:420`) | **Not filtered at all** (parental-flagged channels show there today) |
| Live TV "recent" list, first channel | `LiveTab.kt:228`, `LiveTab.kt:697` | Parental filter only |
| Home rows, history and resume rows | `HomeScreen.kt` `buildHome` + `homeSafe` | Parental filter when `Parental.homeHide` is on |
| Search: results and history row | `SearchScreen.kt:88` | Not filtered |
| Multi view start channels | `MultiView.kt:59` | Not filtered |
| Boomer mode tiles | `Boomer.kt:246`, `549` | Not filtered |
| Phone remote "continue watching" | `RemoteHub.kt:97` | Not filtered |
| Existing similar feature | `HiddenCats` (`HiddenCats.kt`, prefs `ipvio_hidecats`): hides a Live category from the list, with the menu at `LiveTab.kt:349/361` | Pattern to copy |

The existing parental "never on the home pages" setting (`Parental.homeHide`) is a partial version of this for parental-flagged content. It still writes history and does not cover the player strip, search, Multi view, Boomer or the phone remote.

## Plan

1. **Pure logic** – new `PrivateLogic.kt`: `isPrivate(kind, group, private: Set<String>)`, plus a filter helper. No Android imports. JUnit test `PrivateLogicTest.kt`.
2. **Store** – new `PrivateCats.kt`, per profile, prefs `ipvio_privatecats`. It is a copy of the `HiddenCats` pattern, and its keys are kind + group name, so a movie category and a Live category with the same name stay apart.
3. **Stop recording** (one guard at each writer, no caller changes where avoidable):
   - `History.add`: skip a private item. The three callers already have the `Item`, so add an overload that takes the `Item`.
   - `WatchStore.save`: skip a private item.
   - `ResumeLog.add`: skip a private item.
   - `LiveMemory.setSel`: do not remember a private category.
4. **Stop showing outside the category** – add the private filter next to the existing parental filter in:
   - `buildHome` / `homeSafe`;
   - `RecentChannelsRow`;
   - `LiveTab` recent list, "All channels" and the first channel;
   - `SearchScreen`;
   - `MultiViewLogic.startChannels`;
   - Boomer tiles;
   - `RemoteHub`;
   - the start-up resume in `MainActivity`.
5. **Clean up when marking**: marking a category private removes its existing entries from history and continue watching (a confirm dialog says so).
6. **UI**:
   - "Make private" / "No longer private" in the category menu of Live TV, Movies and Series, next to "Hide";
   - a small list under Settings > Parental controls to undo it.
7. **Backup**: add `ipvio_privatecats` to `Backup.NAMES` (new name only, no existing key changes).
8. **Docs**: `FEATURES.md`, `CHANGELOG.md`, `WORK_STATE.md`.

## Decisions (confirmed by the maintainer, 2026-11-19)

1. Search: private content is hidden from search results.
2. "All channels" / "All movies" and the automatic genre lists: private content is hidden there too. It stays reachable through its own category in the category list. No separate "Private" entry.
3. Resume position inside a private category: nothing is saved.
4. Parental-flagged categories: an option to treat them as private automatically, off by default.
5. PIN: a PIN lock for opening a private category is **not** part of this task (possible follow-up).

## Affected files (expected)

New: `PrivateLogic.kt`, `PrivateCats.kt`, `PrivateLogicTest.kt`.
Changed:
- stores and backup: `Store.kt` (History, WatchStore, ResumeLog), `Backup.kt`;
- Live TV and player: `LiveTab.kt`, `PlayerLive.kt`, `MultiView.kt` / `MultiViewLogic.kt`;
- other screens: `HomeScreen.kt`, `SearchScreen.kt`, `Boomer.kt`, `RemoteHub.kt`, `MainActivity.kt`;
- category menus: the Movies and Series category menus (file to confirm);
- settings: `SettingsHub.kt`;
- dictionaries: one dictionary file with the new UI strings in 10 languages.

## Risks

- **Protected areas (CLAUDE.md §5):** this needs approval.
  - New UI strings (German key + 10 languages).
  - A new backup name.
  - Touches `PlayerScreen.kt` callers only through `History`/`WatchStore` (no player logic change).
- **Group names change:** if the provider renames a group, the private mark no longer matches. This is the same limit as hidden categories and sublists.
- **Visible change for existing users:** none until a category is marked private.
- **Performance:** the filter is a set lookup per item. Home and search already filter large lists on a background thread.

## Testing (planned)

- **JVM:** `PrivateLogicTest` (kind separation, filter, empty set).
- **Device (maintainer):**
  1. Mark a Live category private.
  2. Play a channel from it.
  3. Check that it does not appear in Recently watched, the player strip, Home, search, All channels, Multi view or the phone remote.
  4. Restart: the app does not start on that channel.
  5. Unmark: it behaves normally again.
  6. Repeat with a movie and a series category (no continue-watching entry).
  7. Backup and restore keeps the private list.

## Result (2026-11-19)

Implemented as planned. Deviations:

- **Confirmation:** a toast after "Privat machen" instead of a confirm dialog. The toast says that history is cleared for that category.
- **Movies and series:** the "Privat machen" entry is in the list view of the category page (menu key / long OK), where the move entries already were. The tile view has no menu.
- **Favorites:** private favorites are kept in the Favorites tab, but they no longer show in the home rows ("My channels", "Live TV right now").
- **Own channel lists:** a private channel drops out of an own channel list, because those lists resolve against the visible channels.
- **Sport tab:** the game-to-channel matching is unchanged and can still offer a private channel (follow-up if needed).

Files:
- New: `PrivateLogic.kt`, `PrivateCats.kt`, `DictP.kt`, `PrivateLogicTest.kt`.
- Changed:
  - stores and backup: `Store.kt` (History, WatchStore, EpProgress guards), `Backup.kt` (`ipvio_privatecats`), `Lang.kt`;
  - screens: `LiveTab.kt`, `HomeScreen.kt`, `SearchScreen.kt`, `Boomer.kt`, `RemoteHub.kt`, `CategoriesScreen.kt`, `MainActivity.kt`;
  - settings: `SettingsHub.kt`.

Checks:
- `./gradlew :app:compileDebugKotlin` passes.
- `./gradlew :app:testDebugUnitTest`: 394 tests, 4 failures. All 4 also fail on the previous commit without this change, so they existed before: `PhoneImportTest.listImportWithoutCodeIsRejectedEvenWhenPhoneSearchIsActive`, `PhoneTransferTest.uploadBackupRestoreCrash`, `SubSearchLogicTest.subdlHitsDropKeyAndOtherEpisodes`, `LoadLogicTest.vorauswahlNachGeraetesprache`.

## Review fixes (2026-11-19)

- **Search cache:** `PrivateCats.visible` (cached) is now used only for the full playlist (TV and phone search). The cache is a single object written in one step. All other lists use the uncached `PrivateCats.filter`. Before, one shared cache slot could return another caller's list across threads, and it made search rebuild its index again and again.
- **Background work:** the id refresh and the clean-up run on a single background thread. History and continue-watching changes are posted back to the main thread. Turning on the parental option no longer runs the check over the whole playlist on the UI thread.
- **Resume log:** marking a category also removes resume-log lines about its titles (`ResumeLog.removeFor`).
- **Test:** `PrivateLogicTest` now has a flagged sample ("FSK 18"), so the parental option test can fail.
