# T-027: Phone features documentation

**Status:** documentation done in the working branch, open for review. No code changed.

## Goal

Describe the phone features in the public docs: the QR-code connection, quick playlist setup from the phone, phone search, control of the running player, categories, sport, imports and transfer. The features already exist in the code; only the documentation was missing.

## Affected files

- `docs/PHONE.md`: new.
- `README.md`: feature bullet, a "Your phone as the remote" section, Quick start tip, documentation table row.
- `docs/FAQ.md`: new section "Phone".
- `CHANGELOG.md`: entry under "Unreleased".
- `docs/tasks/T-027-phone-docs.md`: this file.

## Sources in the code

- `PhoneImport.kt`: phone pages (`/r`, `/send`, `/upload`, `/listimport`, `/keys`, `/transfer`, `/crash`), 4-digit code, lock after 5 failed attempts for one minute (403 / 429), ports 8765 to 8775, plain HTTP.
- `RemoteHub.kt`, `RemoteMore.kt`, `RemoteLogic.kt`: API endpoints (search, play, control, categories, playlists, sport).
- `RemoteUi.kt`: QR card on the TV ("Mit dem Handy suchen").
- `TransferUi.kt`, `AddPlaylistScreen.kt`, `ListImportUi.kt`, `SettingsHub.kt`: QR entry points "Am Handy eingeben" / "Enter on phone".
- `QrCode.kt`: built-in QR generator.

## Acceptance criteria

- Every described function is found in the code (see the list above).
- Things not verified in the code are not described as features (for example the phone sport tab's favourite-team filtering was not confirmed and is described only as shown in the page text).
- Security is stated as it is: plain HTTP on the home network, 4-digit code, lock after 5 wrong codes.

## Risks

- The phone page text exists in German and in the app languages; the docs describe the English texts.
- Phone search was not tested on a device while writing this.

## Test steps for the maintainer

1. On the TV, open Phone search from the sidebar: the QR card shows, the code is visible.
2. Scan with a phone on the same Wi-Fi: the page opens, enter the code.
3. Enter 5 wrong codes: the page locks for one minute.
4. Send an M3U link from the phone: the TV adds the playlist.
5. Search a movie on the phone and start it: it plays on the TV. Pause, skip and stop from the phone.
6. Hide and move a category from the phone: the change shows on the TV.

## Test results

- Documentation only. No device test and no build were run.
