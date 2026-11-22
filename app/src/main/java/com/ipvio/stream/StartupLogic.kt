package com.ipvio.stream

/** Timing rules for the first seconds after a cold start (T-047). Pure logic, no Android imports, covered by JVM tests. */
object StartupLogic {
    /** Wait before the background refresh of the playlist starts after the stored list is already on screen. */
    const val REFRESH_DELAY_MS = 4_000L

    /** Wait before the archive details of an Xtream server are fetched (they are only needed for catch-up). */
    const val CATCHUP_DELAY_MS = 5_000L

    /**
     * [shownFromStore] = the stored list is on screen now. [userStarted] = not the silent refresh at the app start: the user
     * reloaded, switched or added the list (no waiting). Only the silent refresh at a start is moved back, so the
     * first screens get the CPU and the network connection.
     */
    fun refreshDelayMs(shownFromStore: Boolean, userStarted: Boolean): Long =
        if (shownFromStore && !userStarted) REFRESH_DELAY_MS else 0L
}
