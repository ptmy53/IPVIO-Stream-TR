package com.ipvio.stream

import java.util.Calendar
import java.util.TimeZone

/**
 * T-048b: refresh plan of one playlist (pure logic, no Android imports).
 * [GENERAL] = no own plan: the general setting applies (the behaviour before this feature).
 * Modes 0 to 4 are those of [UpdatePlan]: at every start, every X hours, daily, weekly, by hand only.
 */
object PlaylistRefreshLogic {
    const val GENERAL = -1

    data class Spec(
        val mode: Int = GENERAL,
        val intervalH: Int = 12,
        val hour: Int = 4,
        val minute: Int = 0,
        val dow: Int = Calendar.MONDAY
    )

    fun encode(s: Spec): String = "${s.mode},${s.intervalH},${s.hour},${s.minute},${s.dow}"

    /** Unknown or damaged text gives the general setting; numbers are kept inside their ranges. */
    fun decode(text: String?): Spec {
        val p = text?.split(',')?.map { it.trim().toIntOrNull() } ?: return Spec()
        if (p.size != 5 || p.any { it == null }) return Spec()
        val mode = p[0]!!
        if (mode !in 0..4) return Spec()
        return Spec(mode, p[1]!!.coerceIn(1, 168), p[2]!!.coerceIn(0, 23), p[3]!!.coerceIn(0, 59), p[4]!!.coerceIn(1, 7))
    }

    fun hasOwnPlan(s: Spec) = s.mode != GENERAL

    /** The plan that applies to the list: its own, otherwise [general]. */
    fun plan(s: Spec, general: UpdatePlan): UpdatePlan =
        if (s.mode == GENERAL) general else UpdatePlan(s.mode, s.intervalH, s.hour, s.minute, s.dow)

    /** Does the background job have to wake up for this list? (every X hours, daily, weekly) */
    fun needsJob(s: Spec) = s.mode in 1..3

    /**
     * A list that is not the main list is refreshed only by its own plan. Without one (general setting) nothing happens,
     * as before. "By hand only" never refreshes.
     */
    fun otherDue(s: Spec, lastMs: Long, nowMs: Long, zone: TimeZone): Boolean =
        hasOwnPlan(s) && SettingsLogic.refreshDue(plan(s, UpdatePlan(4)), lastMs, nowMs, zone)
}
