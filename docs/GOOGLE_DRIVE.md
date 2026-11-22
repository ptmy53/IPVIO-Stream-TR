# Google Drive device sync (optional)

Since 1.4.2 IPVIO Stream can sync its data between devices over the user's Google Drive
(Settings > Storage > Google Drive). Every device uploads only its own file, so no device overwrites another one.
Restoring is always a manual step: pick a backup, see what it contains, choose the areas, confirm.

What travels:

- Favorites and lists, history and continue watching, settings, profiles and hidden items
- Optionally the playlists (with backup servers) and own API keys, **encrypted** with a sync password chosen by the
  user (AES-256-GCM, key from PBKDF2-HMAC-SHA256). Without the password nobody can read them, not even Google.
- Never: the parental control PIN and the update state stay on the device.

Before every restore the app saves the current data on the device; "Undo last restore" brings it back.

Sign-in works like on other TV apps: the TV shows a code (and a QR code for the page), you confirm it on your phone
at google.com/device. This also works on Fire TV, which has no Google services.

The app only uses the scope `https://www.googleapis.com/auth/drive.file`: it creates the folder **IPVIO Stream Sync**
in the user's Drive and can only see the folder and files it created itself, nothing else.
(The hidden app folder, scope `drive.appdata`, is refused by Google in the TV sign-in with `invalid_scope`.)

Without the two values below the Google Drive section stays hidden. The transfer over Wi-Fi
(Settings > Storage > Transfer over Wi-Fi) always works without any setup.

## Setup (once, free)

1. Open the [Google Cloud Console](https://console.cloud.google.com/) and create a project (e.g. "IPVIO Stream").
   Do not add a billing account, it is not needed.
2. **APIs & Services > Library**: enable the **Google Drive API**.
3. **Google Auth Platform** (OAuth consent screen):
   - User type **External**, app name "IPVIO Stream", your e-mail as support and developer contact.
   - Data access / scopes: add `.../auth/drive.file` (non-sensitive, no security assessment needed).
   - While the app is in "Testing", only the **test users** you add here can sign in (up to 100). Add your own Google account.
     To let everybody sign in, publish the app (Google may check name, logo and privacy policy).
4. **Clients > Create client**
   - Application type: **TVs and Limited Input devices**
   - Copy the **Client ID** and **Client secret**.
5. In the project root, in `secrets.properties` (ignored by Git):

```properties
GOOGLE_CLIENT_ID=1234567890-abc.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-...
```

6. Build the app again. Settings > Storage now shows **Google Drive > Connect with Google**.

For this client type Google does not treat the client secret as a real secret (it is part of every APK), but keep it
out of the repository anyway. Since 1.4.3 the release build gets the two values from the GitHub secrets
`GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`, so the official release APK shows the Google Drive section. Your own builds need the values above.
