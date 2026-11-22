package com.ipvio.stream

/**
 * User-Agent for playlist and Xtream requests (#90). Default stays the VLC value that existing playlists use.
 * A playlist can opt into the app's own identifier, because some providers reject the VLC value (HTTP 403).
 * Pure logic, no Android, testable.
 */
object PlaylistUaLogic {
    const val VLC = "VLC/3.0.21 LibVLC/3.0.21"

    /** User-Agent for a request. [native] = the playlist opted into the app identifier. */
    fun pick(native: Boolean, appVersion: String): String =
        if (native) "IPVIOStream/" + appVersion.ifBlank { "unknown" } else VLC

    /** Key in the `ipvio` preferences for one playlist address (urlKey = PlaylistStore's key of the address). */
    fun prefKey(urlKey: Int): String = "ua_native_$urlKey"
}
