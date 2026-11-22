# T-026: consent before an unencrypted fallback (issue #69)

**Status:** implemented, not built with Gradle, not tested on a device.

## Goal
A provider HTTPS certificate that does not match its address must not lead to a silent switch to plain http. The user is asked once per host; the answer is stored.

## Affected files
- `TlsFallbackLogic.kt` (new, pure): decision rules (`USE_AS_IS`, `USE_HTTP`, `ASK_CONSENT`), downgrade URL, host of an https address.
- `TlsFallback.kt`: no more silent downgrade. `fallbackFor()` returns the http address only for an agreed host, otherwise it queues the host in `pendingAsk`. Agreed hosts are stored in `ipvio_tls` (key `agreed`, new file).
- `TlsConsentDialog.kt` (new): dialog shown while `pendingAsk` is set. Buttons "Connect without encryption" and "Cancel".
- `LiveTimeshift.kt`: the playback data source uses `fallbackFor()` instead of the old downgrade.
- `DictP.kt` (new): 3 dialog texts in 10 languages. `Lang.kt` loads it, `DictConsistencyTest.kt` checks it.
- `MainActivity.kt`: `TlsFallback.init(this)` at start, `TlsConsentDialog()` at the top level.
- `CHANGELOG.md`.

Unchanged callers: `PlaylistStore` (interceptor), `StalkerApi`, `LiveTap`, `PlayerNet`, `PlayerScreen` (use `isHostMismatch` and the interceptor).

## Acceptance criteria
1. With a mismatched certificate and no agreement, nothing is sent over http.
2. After "Connect without encryption", the same host is reached over http on the next attempt, also after an app restart.
3. "Cancel" changes nothing; the question comes again on the next attempt.
4. Certificates that match, and other TLS errors (expired, untrusted CA), behave as before.

## Checks
- `TlsFallbackLogicTest`: 8 tests, run on the JVM with the Kotlin 2.0.21 compiler and JUnit 4.13.2: **8/8 passed**.
- `DictP` columns: each row has 10 columns (checked with awk).
- **Not run:** Gradle build, `DictConsistencyTest`, device tests.

## Risks
- The dialog appears only after a playlist or stream load fails. The user must then load the playlist again after agreeing (no automatic retry).
- `pendingAsk` is a Compose state written from background threads; it is read only in composition.
- Behaviour change for providers with a broken certificate: they now need one confirmation.

## Test steps (maintainer)
1. Add a playlist from a server whose certificate does not match its address (or use a test host). Expect the dialog "Unencrypted connection?" with the server name.
2. Press "Cancel": no connection, no playlist. Load again: the dialog appears again.
3. Press "Connect without encryption", then load the playlist again: it loads over http. Restart the app and load again: still loads without a new question.
4. Play a channel from that server (live TV): works after agreement; before agreement the player shows the error.
5. Normal https servers with a valid certificate: no dialog.
6. Check the new texts in German and English (Settings > Language).

## Open
- Automatic retry after agreeing (today the user loads again).
- Consent revocation (no settings screen yet).
