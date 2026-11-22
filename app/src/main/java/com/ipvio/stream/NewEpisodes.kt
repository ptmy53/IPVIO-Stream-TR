package com.ipvio.stream

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateListOf

/**
 * Report new episodes for favorite series (bell at the top right). The state per series is remembered on the device;
 * it is checked when the playlist is loaded. Opening the series dismisses the notice.
 */
object NewEpisodes {
    val list = mutableStateListOf<NewEpisodeLogic.Notice>()
    private val known = HashMap<String, Int>()
    private var inited = false
    private fun prefs(c: Context) = Profiles.sp(c, "ipvio_newep")

    fun init(c: Context) {
        if (inited) return
        inited = true
        val p = prefs(c)
        (p.getString("known", "") ?: "").split("\n").forEach { l ->
            val t = l.split("\t"); if (t.size == 2) t[1].toIntOrNull()?.let { known[t[0]] = it }
        }
        (p.getString("list", "") ?: "").split("\n").forEach { l ->
            val t = l.split("\t")
            if (t.size == 5) list.add(NewEpisodeLogic.Notice(t[0], t[1], t[2].toIntOrNull() ?: 0, t[3].toIntOrNull() ?: 0, t[4].toIntOrNull() ?: 1))
        }
    }

    private fun save(c: Context) {
        prefs(c).edit()
            .putString("known", known.entries.joinToString("\n") { it.key + "\t" + it.value })
            .putString("list", list.joinToString("\n") { listOf(it.id, it.title.replace('\t', ' ').replace('\n', ' '), it.season, it.num, it.added).joinToString("\t") })
            .apply()
    }

    /** Check favorite series (after loading the playlist and when the favorites change). */
    fun check(c: Context, favSeries0: List<Item>) {
        init(c)
        // Series with a placeholder do not know their episodes yet: no comparison (otherwise false notices)
        val favSeries = favSeries0.filter { !XtreamLogic.isLazy(it) }
        if (favSeries.isEmpty()) return
        val r = NewEpisodeLogic.detect(known, favSeries.map { s -> NewEpisodeLogic.Show(s.id, s.title, s.episodes.map { it.season to it.num }) })
        val ids = favSeries.map { it.id }.toSet()
        known.putAll(r.known)
        for (n in r.notices) {
            val old = list.firstOrNull { it.id == n.id }
            if (old != null) list.remove(old)
            val shown = if (old != null) n.copy(added = n.added + old.added) else n
            list.add(0, shown)
            // #105: a system notification as well as the bell ("Serie XY hat eine neue Folge")
            systemNotice(c, shown)
        }
        // Remove notices for series that are no longer favorites
        list.removeAll { it.id !in ids }
        save(c)
    }

    private const val CHANNEL = "ipvio_newep"

    /** Android notification for a new episode of a favorite series (#105). Skipped without the permission (Android 13+). */
    private fun systemNotice(c: Context, n: NewEpisodeLogic.Notice) {
        runCatching {
            val nm = c.getSystemService(NotificationManager::class.java) ?: return
            if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(
                NotificationChannel(CHANNEL, Lang.tr("Neue Folgen"), NotificationManager.IMPORTANCE_DEFAULT)
            )
            if (Build.VERSION.SDK_INT >= 33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
            val open = Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pi = PendingIntent.getActivity(c, n.id.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            @Suppress("DEPRECATION")
            val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(c, CHANNEL) else Notification.Builder(c)
            val note = b.setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(n.title)
                .setContentText(Lang.tr(NewEpisodeLogic.text(n)))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            nm.notify(("newep:" + n.id).hashCode(), note)
        }
    }

    /** Series opened: notice dismissed. */
    fun dismiss(c: Context, id: String) {
        init(c)
        if (list.removeAll { it.id == id }) save(c)
    }
}
