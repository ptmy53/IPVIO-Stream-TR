# T-028: second EPG source per channel (issue #103) — plan only

**Status:** plan. No code changed. Approved on 19 Nov 2026 as "plan only" (the feature itself needs a separate approval after the plan is read).

## Goal (from the issue)
1. Keep the provider's EPG as the default source.
2. Allow a second, custom EPG list (XMLTV address) whose guide data is assigned to specific channels.
3. The TV guide mirrors the channel list, including favorites and their order.

## What exists today (read in the code, 19 Nov 2026)
- `Xmltv` (`Epg.kt`): one custom XMLTV address (`customUrl` / `setCustom`, prefs file `ipvio_epg`, key `custom`). The loaded programmes are kept in `progs`, keyed by the channel id (`tvgId`, lower case). `Xmltv.has(id)` and `forChannel(id)` look them up.
- `Epg.now(item)` (`Epg.kt`): order is cache, loaded XMLTV, then the Xtream query (`get_short_epg`). The provider's EPG is therefore already the first choice when `tvgId` matches.
- `EpgFull.kt`: full guide for the detail view, same order.
- `GuideSearch.kt`: search in the guide uses the loaded XMLTV data.
- The XMLTV address comes from settings and is also set from the phone (`PhoneImport.kt`).

## What is missing
- No mapping "channel -> EPG source". Today a channel is matched only by its `tvgId`.
- Only one XMLTV address exists; a second one would need its own store and its own loading.

## Proposed design (for review, not implemented)
1. **Second source stored separately**: a second XMLTV address in a new key (for example `custom2`) in `ipvio_epg`. Existing keys stay as they are; no change of the backup format without approval (CLAUDE.md section 5).
2. **Per-channel mapping**: a map `channel id -> source` (`provider` or `custom2`) and the EPG channel id to use in that source. Stored in a new preference key. Default: no mapping, so nothing changes for existing users.
3. **Lookup order**: mapped channel: the chosen source only. Unmapped: unchanged order (provider first).
4. **Guide order**: the TV guide keeps the channel list order and favorites as they are; only the programme data comes from the mapped source.
5. **Loading**: the second XMLTV is parsed with the same `parse()` as the first, only for the mapped channels (`wanted` set), so memory stays limited.

## Risks
- Storage: a new key is a format addition; the backup JSON keys (`Backup.kt`) need a decision whether the mapping is part of the backup.
- Memory: a second full XMLTV can be large. Loading only mapped channels is required.
- Player and guide are protected areas (CLAUDE.md section 5). The change must be reviewed before implementation.

## Open questions for the maintainer
1. Should the mapping be part of the backup (`Backup.kt`)?
2. Is a single second XMLTV address enough, or several?
3. Should the mapping be editable on the phone (`PhoneImport.kt`) as well?

## Test steps (later, after implementation)
1. Provider EPG only: guide unchanged.
2. Map one channel to the second source: its programme shows the second source's data, the others keep the provider's.
3. Restart: mapping kept.
4. Favorites order in the guide unchanged.
