# FAQ

Short answers to common questions. For the settings themselves see [SETTINGS.md](SETTINGS.md), for the subtitle keys see [SUBTITLES.md](SUBTITLES.md).

## General

**Is IPVIO Stream free?**
Yes. It is free and open source (GPL-3.0). It is ad-free, and donations are voluntary and unlock nothing. The developer tries to keep everything free for himself too. Some features may therefore not come at all, if they would cost money to run (for example a paid service). That is a deliberate decision, not an oversight.

**Does the app include channels, films or series?**
No. It is only a player. You add your own legal playlist (Xtream Codes, M3U or Stalker portal).

**Is there a phone version?**
The app is designed for TV and the remote. On phones and tablets it runs in landscape: tap the video to show the controls, double-tap left or right to skip, double-tap in the middle to pause.

**Where can I ask questions or get news?**
On Reddit: [r/ipvio](https://www.reddit.com/r/ipvio). The source code and issues are on GitHub: [Emrodev/IPVIO-Stream](https://github.com/Emrodev/IPVIO-Stream).

**Why is the app GPL-3.0?**
It includes the Jellyfin FFmpeg audio decoder (GPL-3.0), which is needed for AC3/E-AC3/DTS sound. Derived works must stay open source under the same license.

**Can I use the name and logo?**
For your own modified version, please choose another name and logo.

## Installing and updating

**How do I install the APK on Fire TV / Android TV?**
Use the Downloader app with the code **4200542**, or enable "Install unknown apps" for a file manager and open the APK from the Releases page.

**Which Android version do I need?**
Android 7.0 (API 24) or newer.

**Will I lose my data when I update?**
No, as long as the new APK is signed with the same key as the installed one (see [RELEASE.md](RELEASE.md)). The app offers updates itself, from the GitHub release.

**The update does not install and says the signature does not match.**
Install the APK from the official release. An APK signed by someone else cannot replace the installed app without removing your data.

## Playlists

**My Xtream login works in other apps but not here (e.g. "error 884").**
Some providers block or limit `get.php`. The app then loads the account through `player_api.php` automatically.

**Can I use a Stalker portal (MAC address)?**
Yes: *Add playlist > Stalker portal*, enter the portal address and the MAC address from your provider. Live channels and movies are loaded. Series are loaded when you open them, so their episodes appear on the series page.

**Can I use several playlists at the same time?**
Yes: *Settings > Playlists > Also show* adds a second playlist to the active one.

**My playlist is very large and loads slowly.**
Use the language filter while loading, so fewer entries are kept. Large playlists need some time on the first load.

**How often is my playlist refreshed?**
Under *Settings > Playlists* you choose the interval or a fixed time. The refresh runs in the background and waits while you are watching.

## Phone

**Do I need an app on my phone?**
No. Scan the QR code on the TV; the phone opens a web page in its browser.

**The phone page does not open.**
Phone and TV must be on the same Wi-Fi. Turn off a VPN or a guest network on the phone. See [PHONE.md](PHONE.md) for more.

**Is the phone connection safe?**
It is protected by the 4-digit code shown on the TV and locks for one minute after 5 wrong codes. The connection itself is plain HTTP inside your home network, so do not use it over public Wi-Fi.

**Can I add a playlist from the phone?**
Yes: send an M3U link, an Xtream login or a playlist file from the phone to the TV. See [PHONE.md](PHONE.md).

## Metadata, posters and ratings

**Why are there no posters or ratings?**
Add your own free TMDB key (posters, descriptions, cast) and OMDb key (IMDb / Rotten Tomatoes) in *Settings > API keys*. Step-by-step instructions are in the [README](../README.md#your-own-api-keys-optional).

**Where do descriptions and ratings come from without keys?**
With an Xtream Codes login the app reads them from your provider (description, cover, rating, genres, running time, trailer). A plain M3U address has no such data, so you only see what the list contains.

## Subtitles

**Where do I get subtitles from?**
The player can search **SubDL** and **OpenSubtitles** online. Each needs a free key from you. The steps are in [SUBTITLES.md](SUBTITLES.md).

**Which subtitle service should I use?**
SubDL. OpenSubtitles has reduced its free API access, so a key may give only very few downloads per day.

**The search says "No key for the subtitle search".**
Enter at least one key under *Settings > API keys*.

## Live TV, sync and backup

**Can my favourites and history follow me to another device?**
Yes, with **live sync** over your own Google Drive: *Settings > Storage > Google Drive*. Sign in on the TV with the code shown on the screen (confirm it on your phone at google.com/device). Turn live sync on for each device. Favourites and history then update between devices by themselves. Details: [GOOGLE_DRIVE.md](GOOGLE_DRIVE.md).

**Does live sync share my playlists or passwords?**
Playlists and your own API keys are only included if you choose it, and they are encrypted with a sync password. Your parental-control PIN never leaves the device.

**Is there a backup without Google?**
Yes: *Storage > Start transfer over Wi-Fi* moves your data to another device over the local network, no account needed.

**Can I record live TV?**
Yes, the red button in the player records the running show until its end in the TV guide. Recordings go to internal storage or a USB stick, and appear under *Live TV > Recordings*.

**Where do the stored logins live?**
Only on your device, in app-private storage. They are sent only to the playlist server you entered.

## Troubleshooting

**The app does not start after an update.**
Send the crash report from *Storage* (it is shown there) or the logcat part that starts at "FATAL EXCEPTION" when you report the problem on Reddit or GitHub.

**A channel does not play.**
Check the provider and the playlist first (is the channel really in your list?). With Xtream, use *Settings > Diagnostics* for the status. The player tries another source of the same title when the first one fails, if that is enabled in *Settings > Player*.
