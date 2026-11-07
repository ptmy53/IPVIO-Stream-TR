# Security audit summary

**Verdict: SAFE WITH CAVEATS** (source audit, 7 November 2026).

- No dynamic DEX loading, process execution, hidden WebView/JS bridge, telemetry/ad SDK, or contacts/SMS/location permissions was found.
- Gradle uses only Google, Maven Central and the Gradle Plugin Portal. Gradle 9.6 distribution SHA-256 matches the official value.
- The repository history includes one deleted 22.8 MB APK; no APK, DEX, native `.so`, or executable is present in the current tree. Build from source instead of trusting old release artifacts.
- IPTV credentials may be embedded in user-supplied URLs. `usesCleartextTraffic=true` means HTTP providers expose traffic; prefer HTTPS.
- In-app updates require explicit Android installation approval and Android package-signature matching, but do not perform an extra APK hash check. Keep custom update URLs blank unless trusted.
- The optional phone-import server listens on the LAN only while its screen is open; it uses a random 4-digit code and locks after five failed attempts. Use only on trusted networks.
- Optional Google Drive backup uses the `drive.appdata` scope, but can include playlist credentials if the user enables that option.

Full Turkish report: [GUVENLIK_DENETIMI.md](GUVENLIK_DENETIMI.md).
