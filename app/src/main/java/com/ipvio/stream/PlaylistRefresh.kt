package com.ipvio.stream

import android.content.Context

/**
 * T-048b: refresh plan and last refresh per playlist. New file `ipvio_plrefresh`, keyed by the playlist address like
 * `ipvio_plmode` (not profile-scoped, not part of the backup). Without an entry a list follows the general setting.
 */
object PlaylistRefresh {
    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences("ipvio_plrefresh", Context.MODE_PRIVATE)
    private fun key(url: String) = (PlaylistLogic.normalizeUrl(url) ?: url.trim()).hashCode()

    fun get(c: Context, url: String): PlaylistRefreshLogic.Spec = PlaylistRefreshLogic.decode(prefs(c).getString("p_" + key(url), null))

    fun set(c: Context, url: String, s: PlaylistRefreshLogic.Spec) {
        val e = prefs(c).edit()
        if (PlaylistRefreshLogic.hasOwnPlan(s)) e.putString("p_" + key(url), PlaylistRefreshLogic.encode(s)) else e.remove("p_" + key(url))
        e.apply()
    }

    fun clear(c: Context, url: String) { prefs(c).edit().remove("p_" + key(url)).remove("t_" + key(url)).apply() }

    /** The plan that applies to this list now. */
    fun effective(c: Context, url: String): UpdatePlan = PlaylistRefreshLogic.plan(get(c, url), General.plan())

    /** Last refresh: the general one for a list without an own plan (as before), otherwise its own. */
    fun lastFor(c: Context, url: String): Long =
        if (PlaylistRefreshLogic.hasOwnPlan(get(c, url))) prefs(c).getLong("t_" + key(url), 0L) else General.lastRefresh

    fun markFor(c: Context, url: String) {
        if (PlaylistRefreshLogic.hasOwnPlan(get(c, url))) prefs(c).edit().putLong("t_" + key(url), System.currentTimeMillis()).apply()
        else General.markRefreshed(c)
    }

    /** Is any of the lists refreshed by the background job? */
    fun anyNeedsJob(c: Context, urls: List<String>): Boolean = urls.any { PlaylistRefreshLogic.needsJob(get(c, it)) }
}
