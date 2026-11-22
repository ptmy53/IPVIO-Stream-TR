# Online subtitles: API keys for SubDL and OpenSubtitles

The player can search for subtitles online. The search asks **SubDL** and **OpenSubtitles**; you choose the file, and the app attaches it to the running stream. A subtitle you chose once for a title is attached automatically the next time.

The app ships **without keys**. Each key is free to create and is entered once under **Settings > API keys**. The keys stay on your device.

| Service | Status | Key needed for |
|---|---|---|
| **SubDL** | Recommended. A free key works for normal use. | Searching and downloading |
| **OpenSubtitles** | Limited. Its free API access has been reduced, and regular use may need a paid (Pro) account. The key is still supported in the app. | Searching and downloading, if you have a key |

If you only enter a SubDL key, the search uses SubDL alone. Without any key, the search shows the message "No key for the subtitle search".

## SubDL key

1. Open **subdl.com** and create an account (or log in).
2. Open your account or profile menu and go to the **API** section (the app's own hint says: *Konto > API*). Copy the **API key**.
   *The exact menu names on subdl.com can change. If you cannot find the section, look for the API key on your profile or account page.*
3. On the TV open **Settings > API keys**, select the **SubDL API key** field and enter the key.
4. Press **Save and check**. A green "Key valid" confirms it.
5. Start a film or episode, open **Audio & subtitles > Search subtitles online**, choose a subtitle and press OK.

Tip: type the key with the phone import (*Playlists > Enter from phone*, or *Storage > Transfer over Wi-Fi*) instead of the remote. The key is long, and the remote is slow to type with.

## OpenSubtitles key

OpenSubtitles has reduced its free API access. Recent reports disagree about the daily download limit for free users, and regular use may need a Pro subscription. A key you already have keeps working in the app as long as OpenSubtitles accepts it. Creating a new key may not give you the full free allowance.

If you want to try it:

1. Open **opensubtitles.com** and create an account.
2. Create an app under **API consumers** and copy the **API key**.
3. On the TV open **Settings > API keys**, enter the key under **OpenSubtitles API key** and press **Save and check**.

Without logging in, only a few downloads per day are possible.

## Troubleshooting

| Message | What to do |
|---|---|
| "No key for the subtitle search" | Enter at least one key under *Settings > API keys*. |
| "Key valid" does not appear | Check the key for spaces or a cut-off end. Copy it again. |
| "OpenSubtitles: daily limit reached" (HTTP 406) | The free download limit for today is used up. Try SubDL, or wait until the next day. |
| No results | Try the English title, or check whether the film is known under another title. Results depend on the subtitle database of each service. |

*The app's text for these steps (German and nine other languages) is in `DictL.kt`. This document describes the same steps; the SubDL steps were not checked against subdl.com while writing it.*
