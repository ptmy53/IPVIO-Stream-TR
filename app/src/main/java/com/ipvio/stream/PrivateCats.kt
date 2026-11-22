package com.ipvio.stream

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Private categories (T-028), per profile. Content of a private category shows only inside that category: it is not
 * recorded ([History], [WatchStore], [EpProgress]) and not shown in "All …", genre lists, search or the home rows.
 * Rules: [PrivateLogic].
 */
object PrivateCats {
    private const val PREFS = "ipvio_privatecats"
    /** Bumped on every change; screens use it as a key to rebuild their lists. */
    var version by mutableIntStateOf(0)
        private set
    /** Parental-flagged categories count as private too (off by default). */
    var parental by mutableStateOf(false)
        private set
    /** Read from background threads, so plain immutable snapshots instead of Compose state lists. */
    @Volatile var keys: Set<String> = emptySet()
        private set
    @Volatile private var ids: Set<String> = emptySet()
    /** Bumped together with [version], but readable from any thread (phone search runs on the server thread). */
    @Volatile private var gen = 0
    /** Cache of [visible] for the full playlist: one object, written in one step, so threads never mix fields. */
    private class Snap(val all: List<Item>, val gen: Int, val out: List<Item>)
    @Volatile private var snap: Snap? = null
    /** Id refresh and clean-up run off the main thread, one after the other (a later toggle never loses to an earlier one). */
    private val worker = java.util.concurrent.Executors.newSingleThreadExecutor()
    private val main = android.os.Handler(android.os.Looper.getMainLooper())
    private var inited = false

    private fun prefs(c: Context) = Profiles.sp(c, PREFS)

    fun init(c: Context) {
        if (inited) return
        inited = true
        val p = prefs(c)
        keys = PrivateLogic.decode(p.getString("k", null))
        parental = p.getBoolean("parental", false)
    }

    val active: Boolean get() = keys.isNotEmpty() || parental

    fun isPrivate(kind: Kind, group: String): Boolean = PrivateLogic.key(kind, group) in keys
    /** For writers that only know the id (history, resume); filled by [refresh] after each playlist load. */
    fun isPrivateId(id: String): Boolean = id in ids

    /** Any list without private items (not cached). */
    fun filter(list: List<Item>): List<Item> = if (!active) list else PrivateLogic.visible(list, keys, parental)

    /**
     * The full playlist without private items. Cached, so the same object comes back until the playlist or the private
     * list changes: search keys its index by list identity ([SearchCache]). Only for the full playlist, use [filter] elsewhere.
     */
    fun visible(all: List<Item>): List<Item> {
        if (!active) return all
        val g = gen
        snap?.let { if (it.all === all && it.gen == g) return it.out }
        return PrivateLogic.visible(all, keys, parental).also { snap = Snap(all, g, it) }
    }

    /** Recomputes the private ids after a playlist load, in the background. */
    fun reload(all: List<Item>) { worker.execute { ids = PrivateLogic.ids(all, keys, parental) } }

    /** Marks a category private (or not). Marking also removes its titles from history and continue watching. */
    fun set(c: Context, kind: Kind, group: String, on: Boolean) {
        init(c)
        val k = PrivateLogic.key(kind, group)
        keys = if (on) keys + k else keys - k
        prefs(c).edit().putString("k", PrivateLogic.encode(keys)).apply()
        changed(c, on)
    }

    fun setParental(c: Context, on: Boolean) {
        init(c)
        parental = on
        prefs(c).edit().putBoolean("parental", on).apply()
        changed(c, on)
    }

    /** Screens rebuild at once; the ids (a pass over the whole playlist) and the clean-up follow from the background. */
    private fun changed(c: Context, purge: Boolean) {
        gen++
        version++
        val all = PlaylistStore.channels
        val app = c.applicationContext
        worker.execute {
            val now = PrivateLogic.ids(all, keys, parental)
            ids = now
            // History and continue watching are Compose state lists: change them on the main thread
            if (purge && now.isNotEmpty()) main.post {
                History.removeAll(app, now)
                WatchStore.removeAll(app, now)
                ResumeLog.removeFor(app, now)
            }
        }
    }
}

/** Menu action "Privat machen" / "Nicht mehr privat" for one category, with a short confirmation. */
internal fun privateToggle(c: Context, kind: Kind, group: String) {
    val on = !PrivateCats.isPrivate(kind, group)
    PrivateCats.set(c, kind, group, on)
    android.widget.Toast.makeText(
        c, Lang.tr(if (on) "Kategorie ist privat: kein Verlauf, nur in dieser Kategorie sichtbar." else "Kategorie ist nicht mehr privat."),
        android.widget.Toast.LENGTH_LONG
    ).show()
}
