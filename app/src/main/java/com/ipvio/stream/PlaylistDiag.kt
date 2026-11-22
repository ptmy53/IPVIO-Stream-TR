package com.ipvio.stream

/**
 * Diagnostic output for loading playlists (logcat tag "IPVIO-PL"). Only numbers and category names:
 * no addresses, no user names, no passwords.
 * Read on the PC with: adb logcat -s IPVIO-PL
 */
object PlaylistDiag {
    const val TAG = "IPVIO-PL"

    /** Short summary of a list: entries per kind and the live categories (number, first and last in alphabetical order). */
    fun summary(items: List<Item>): String {
        var live = 0
        var movies = 0
        var series = 0
        val liveCats = HashSet<String>()
        for (i in items) when (i.kind) {
            Kind.LIVE -> { live++; liveCats.add(i.group) }
            Kind.MOVIE -> movies++
            Kind.SERIES -> series++
        }
        val sorted = liveCats.sorted()
        return "live=$live movies=$movies series=$series liveCategories=${liveCats.size}" +
            " first=\"${sorted.firstOrNull().orEmpty()}\" last=\"${sorted.lastOrNull().orEmpty()}\""
    }

    fun log(step: String, msg: String) {
        runCatching { android.util.Log.i(TAG, "$step: $msg") }
    }
}
