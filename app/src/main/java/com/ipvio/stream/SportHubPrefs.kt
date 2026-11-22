package com.ipvio.stream

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf

/**
 * T-049d: settings of the Sports Hub (Settings > Sports Hub), per profile, file `ipvio_sportset` (new, additive).
 * Which leagues are on lives in [SportLeagues]; favourite sports in [SportHubFavs].
 */
object SportHubPrefs {
    /** OK on a goal or kick-off alert jumps to a channel that shows the game. */
    var jumpOnAlert by mutableStateOf(true)
        private set
    /** Tab shown when the hub is opened for the first time in a session. */
    var startTab by mutableStateOf(0)
        private set
    val hiddenTabs = mutableStateListOf<Int>()
    private var inited = false
    private fun prefs(c: Context) = Profiles.sp(c, "ipvio_sportset")

    fun init(c: Context) {
        if (inited) return
        inited = true
        val p = prefs(c)
        jumpOnAlert = p.getBoolean("jump", true)
        startTab = p.getInt("start", 0).coerceIn(0, SportHubLogic.TAB_COUNT - 1)
        hiddenTabs.clear(); hiddenTabs.addAll(SportHubLogic.decode(p.getString("hidden", "")))
    }

    fun setJump(c: Context, v: Boolean) { init(c); jumpOnAlert = v; prefs(c).edit().putBoolean("jump", v).apply() }
    fun setStart(c: Context, tab: Int) { init(c); startTab = tab.coerceIn(0, SportHubLogic.TAB_COUNT - 1); prefs(c).edit().putInt("start", startTab).apply() }
    fun toggleTab(c: Context, tab: Int) {
        init(c)
        if (!hiddenTabs.remove(tab)) hiddenTabs.add(tab)
        prefs(c).edit().putString("hidden", SportHubLogic.encode(hiddenTabs.toSet())).apply()
    }
    fun hidden(): Set<Int> = hiddenTabs.toSet()
}
