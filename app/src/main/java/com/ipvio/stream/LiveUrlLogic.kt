package com.ipvio.stream

/**
 * A live channel is recognised by its address (#102). Xtream and Stalker live streams use a "/live/" path segment;
 * movies "/movie/" and series "/series/". A live address is never a movie, even if its group name says "Movies".
 * Pure logic, no Android, testable.
 */
object LiveUrlLogic {
    fun isLiveUrl(url: String): Boolean = url.contains("/live/", ignoreCase = true)

    /**
     * Xtream live address without the "/live/" segment: "http://host:port/user/pass/12345" or ".../12345.ts" / ".../12345.m3u8".
     * Many panels write their live M3U like this, so a channel in a group called "Sky Cinema" or "Film" ended up in Movies.
     * Movies and series of such panels always have "/movie/" or "/series/" or a video file extension, so a numeric last
     * path segment (with at most .ts or .m3u8) with a user and password before it is a live channel.
     */
    fun isXtreamLiveTail(url: String): Boolean {
        val q = url.indexOf('?').let { if (it < 0) url.length else it }
        val slash = url.lastIndexOf('/', q - 1)
        if (slash < 0 || slash + 1 >= q) return false
        var end = q
        val dot = url.lastIndexOf('.', q - 1)
        if (dot > slash) {
            val ext = url.substring(dot + 1, q)
            if (!ext.equals("ts", ignoreCase = true) && !ext.equals("m3u8", ignoreCase = true)) return false
            end = dot
        }
        if (end <= slash + 1) return false
        for (i in slash + 1 until end) if (url[i] !in '0'..'9') return false
        // host, user and password before the number: at least three more path segments ("http://h/user/pass/")
        var segs = 0
        var i = url.indexOf("://").let { if (it < 0) 0 else it + 3 }
        while (i < slash) { if (url[i] == '/') segs++; i++ }
        return segs >= 2
    }

    /** Live by address: "/live/" or the Xtream form without it, and never a movie or series address. */
    fun isLive(url: String): Boolean =
        !url.contains("/movie/") && !url.contains("/series/") && (isLiveUrl(url) || isXtreamLiveTail(url))
}
