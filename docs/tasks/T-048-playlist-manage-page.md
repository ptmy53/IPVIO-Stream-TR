# T-048 – Manage page per playlist, apply step, refresh plan per playlist

Status: **T-048a implemented** (edit window with Apply, Reload of other lists in the background). **T-048b (refresh plan per playlist) implemented.** Not built, not device tested.

## Decisions (maintainer, 22 Nov 2026)
- The buttons on the row (content mode, rename, backup servers, make main list, stop showing additionally, delete) move into a **window** opened with "Edit" on the row. Each playlist has its own window.
- Order: T-048a first, then T-048b (recommended order, accepted by default).
- Background refresh of other playlists by their own plan: yes, cache only (T-048b).
- Reload on a playlist that is not the main list: refresh in the background, do not make it the main list.

## T-048a as built
- `SettingsHub.kt`: each row shows name, host, counts and two buttons: Reload and Edit. A line "Reloading …" shows while another list reloads.
- `PlaylistEditDialog.kt` (new): name, content mode, "shown additionally" (only for lists already shown), backup servers (Xtream). They are a draft; **Apply** stores them (rename, backup servers after validation, mode with re-parse from the stored list, stop showing additionally). **Cancel** and Back drop the draft. "Make main list" and "Delete" (with the usual second press) are actions and run at once.
- `PlaylistManageLogic.kt` (new, pure): which parts of the draft changed. `DictPm.kt` (new): 7 English rows, other languages fall back to English; registered in `Lang.kt`.
- `PlaylistStore.reloadOtherAsync`: `refreshCacheOnly` for a list that is not the main list; if it is shown next to the main list, its part is rebuilt from the new copy. Does nothing while another load runs.
- No new stored keys in T-048a.

## T-048a test results
- Ran (JDK 21, Kotlin 2.0.21 compiler from the Gradle install, JUnit 4.13.2): `PlaylistManageLogicTest` (5), `I18nCoreTest`, `StartupLogicTest`, `DictConsistencyTest`, `DictEn1Test`, `DictEn3Test`: pass. `DictEn2Test` fails (2 tests, "Krimi" vs "Crime") in this setup with and without this change, so not caused by it; not investigated.
- Not run: Gradle build, `SettingsHub.kt`, `PlaylistEditDialog.kt`, `PlaylistStore.kt` not compiled, no device test.

## T-048a device test steps (maintainer)
1. Settings > Playlists: every row shows only Reload and Edit.
2. Edit: change the content mode and rename, press Cancel: nothing changed. Again, press Apply: name and mode changed, the list is rebuilt without a new download.
3. Edit on a list with backup servers: invalid address then Apply shows the error and stores nothing; valid address stores it.
4. Edit on the main list: no "Make main list". On another list: "Make main list" switches at once.
5. Delete asks a second press; deleting the main list loads the next one; deleting the last one opens "Add playlist".
6. Reload on a list that is not the main list: "Reloading …" shows, the main list stays, counts of that list update afterwards.
7. Back (remote) in the window drops the draft. The fields and the focus order work with the remote and the on-screen keyboard.


## Request (maintainer, 22 Nov 2026, German, summarised)
1. Every playlist has its own reload button.
2. The many buttons on the playlist rows move behind one menu button ("Manage" or similar).
3. Playlist settings (for example "Live TV only") are confirmed at the end, so they apply only then.
4. Every playlist gets its own refresh option (daily and so on), because one list may never need updates, or only weekly.

## Current state (read in `SettingsHub.kt` `PlaylistPage`, `General.kt`, `Updater.kt`, `SettingsLogic.kt`)
- Each row carries up to 7 buttons: Not shown additionally, Make main list, Reload, content mode (cycles on OK and applies at once), Rename, Backup servers, Delete.
- A Reload button per row exists since 1.4.6 (`SmallPill("Reload")`). On a non-main list it makes that list the main list first.
- Content mode applies immediately and re-parses the stored list.
- Refresh plan is global: `General.refreshMode` (0 every start, 1 every N hours, 2 daily, 3 weekly, 4 by hand), interval, hour, minute, weekday, and one `last_refresh`. Used by `MainActivity` (cold start), `UpdateWorker` (hourly job) and the settings page. Only the main list is refreshed by the plan; other lists are refreshed only when made main or reloaded.
- `refreshCacheOnly(c, url)` exists (cache refresh without loading into memory): usable for non-main lists.

## Proposed design
**A. Row: two buttons.** Each row shows name, host, counts, and only **Reload** and **Manage**. "Manage" opens a page (sub-page of the playlist settings, Back returns) for that playlist.

**B. Manage page, with Apply.** Contents: Make main list / Stop showing additionally, Rename, Content mode (Live TV + VOD / Live TV only / VOD only), Backup servers (Xtream only), Refresh plan (D), Delete (confirm). Edits are kept as a draft. Two buttons at the bottom: **Apply** (writes everything, then runs the one reload step needed, for example re-parse when the content mode changed) and **Cancel**. Back with a changed draft asks "Discard changes?". Delete and Make main list stay immediate actions with their own confirmation, because they are not settings of the list.

**C. Reload.** Stays on the row. Behaviour unchanged (main list reloads where it is; another list becomes main first). Open point: reload a non-main list in the background without switching (`refreshCacheOnly`)? Recommended, but it changes today's behaviour of the 1.4.6 button.

**D. Refresh plan per playlist.** New keys, existing keys untouched:
- Per playlist (key = existing `key(url)` hash): mode, interval, hour, minute, weekday, last refresh, stored in a new SharedPreferences file `ipvio_plrefresh` (new file, nothing renamed). Default "Use the general setting" = today's global plan, so after the update nothing changes for anyone.
- Options per list: Use general setting / At every start / Every few hours / Daily / Weekly / **Never (by hand only)**.
- `SettingsLogic.refreshDue` and `nextDue` already take a plan and a last time: no change. New pure helper chooses the plan for a URL.
- `MainActivity` start: refresh the main list by its own plan (as now, with its own last time). `UpdateWorker` (hourly): for each list whose own plan is due, and which is not "by hand": the main list as now; other lists via `refreshCacheOnly` (needs approval, see questions). The hourly job stays scheduled when any list has a plan in 1..3.
- The global page "Automatically update" stays and becomes "General setting (default for all playlists)".
- Backup (`Backup.kt` `NAMES`): the new file would be added to the list so it is saved and restored. Protected area; included in the plan, not changed before approval.

## Affected files (planned)
`SettingsHub.kt` (row, Manage page, draft/apply), new `PlaylistManageLogic.kt` (draft, what has to be applied: pure), new `PlaylistRefreshLogic.kt` (plan per list, pure) plus store functions, `Updater.kt`, `MainActivity.kt` (own plan for the main list), `PlaylistStore.kt` (per list last refresh, refresh other lists), `Backup.kt` (NAMES), `DictK.kt` or a new dictionary block (English and other languages for the new texts), `CHANGELOG.md`, `docs/`. Tests: `PlaylistManageLogicTest`, `PlaylistRefreshLogicTest`.

## Split into subtasks
1. T-048a Manage page with Apply (UI only, no new storage).
2. T-048b Refresh plan per playlist (storage, logic, worker, backup).
3. T-048c Reload of non-main lists in the background (only if approved).

## Risks
- Visible change on the main settings page for every user (buttons move). Mitigation: same actions, same texts.
- New stored keys: additive, no migration; missing keys mean "general setting".
- Worker refreshing several lists: memory and data use. Mitigation: `refreshCacheOnly` only, one list at a time, not while playing (as now).
- Not buildable or testable here: Gradle build and device test by the maintainer; only pure logic gets JVM tests.

## Decisions needed
1. Order: build 048a first, then 048b (recommended), or everything at once?
2. Background refresh of non-main lists according to their own plan (cache only)?
3. Reload on a non-main list: keep "make main first" or refresh in the background?

## T-048b as built
- Each playlist has its own refresh plan in the edit window (part of the draft, stored with Apply): General setting (default), At every start, Every few hours, Daily, Weekly, By hand only. Without an own plan nothing changes: the general plan and the general "last updated" apply, as before.
- New SharedPreferences file `ipvio_plrefresh` (keys `p_<hash>` plan, `t_<hash>` last refresh; hash of the normalised address like `ipvio_plmode`). Not profile-scoped and **not added to the backup `NAMES`** (the list is profile-scoped, and `ipvio_plmode` is outside it too; whether both belong in the backup stays an open decision, see `docs/TECH-DEBT.md` row 4). Removed with the playlist.
- `PlaylistRefreshLogic.kt` (pure) and `PlaylistRefresh.kt` (storage). `MainActivity`: the main list is refreshed by its own plan; 20 s after the start, other lists with an own plan that is due are refreshed in the background. `UpdateWorker`: main list by its own plan, then `PlaylistStore.refreshOthersDue` (cache only, one list at a time, not while playing). `Updater.apply` keeps the hourly job when any list has an own plan of 1 to 3.
- A list that is not the main list is refreshed only if it has an own plan; "By hand only" never. "At every start" for another list means: at each app start (20 s later).
- The settings page "Automatically update" now says that it is the default for lists without their own plan.
- JVM tests ran: `PlaylistRefreshLogicTest` (8), `PlaylistManageLogicTest` (6), `I18nCoreTest`, `StartupLogicTest`, `DictConsistencyTest`: 37/37. Not run: Gradle build; `PlaylistStore.kt`, `Updater.kt`, `MainActivity.kt`, `PlaylistEditDialog.kt`, `SettingsHub.kt` not compiled; no device test.

## T-048b device test steps (maintainer)
1. Edit a playlist > "Update playlist": the choices show; "General setting" shows the hint. Pick Daily with an hour and minute a few minutes ahead, Apply, close the app. Settings shows "Last updated" and "Next update" for that list.
2. On a list that is not the main list: Daily, "never updated": 20 s after the next start, "Reloading …" shows and its counts update.
3. A list with "By hand only" is never touched, also not by the background job (check with the hourly job after a few hours or `adb shell cmd jobscheduler run`).
4. Main list with an own plan "Weekly" while the general plan is "Daily": it refreshes only weekly.
5. Delete a playlist and add it again: its plan is gone (General setting).
6. The general setting still works for lists without an own plan (daily refresh of the main list as before).
7. While a film plays, no background refresh starts.
