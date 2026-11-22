package com.ipvio.stream

/**
 * T-048a: draft of the settings of one playlist in the edit window. Nothing is stored until "Apply" (pure logic, no Android imports).
 * [backups] = backup servers as typed (empty entries are ignored); [shown] = shown additionally next to the main list.
 */
object PlaylistManageLogic {
    data class Draft(
        val name: String, val mode: Int, val shown: Boolean, val backups: List<String>,
        /** T-048b: own refresh plan of the playlist. */
        val refresh: PlaylistRefreshLogic.Spec = PlaylistRefreshLogic.Spec()
    )

    private fun clean(b: List<String>) = b.map { it.trim() }.filter { it.isNotEmpty() }

    /** What differs between the stored state and the draft. */
    data class Changes(val rename: Boolean, val mode: Boolean, val shown: Boolean, val backups: Boolean, val refresh: Boolean = false) {
        val any get() = rename || mode || shown || backups || refresh
    }

    fun changes(orig: Draft, now: Draft): Changes = Changes(
        // A blank name is not applied (the store ignores it), so it does not count as a change
        rename = now.name.isNotBlank() && now.name.trim() != orig.name.trim(),
        mode = now.mode != orig.mode,
        shown = now.shown != orig.shown,
        backups = clean(now.backups) != clean(orig.backups),
        refresh = now.refresh != orig.refresh
    )
}
