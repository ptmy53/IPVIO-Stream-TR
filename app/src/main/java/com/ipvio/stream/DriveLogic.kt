package com.ipvio.stream

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Pure logic for the Google Drive sync (W-010): file names, Drive times, sign-in poll answers. No Android here. */
object DriveLogic {
    /** One file per device in the app's folder, so no device overwrites another one. */
    const val PREFIX = "ipvio_sync_"
    const val SUFFIX = ".json"
    /**
     * Visible folder in the user's Drive. The hidden app folder (scope drive.appdata) is refused by Google in the TV
     * sign-in ("invalid_scope"), so the app uses drive.file: it only sees the folder and files it created itself.
     */
    const val FOLDER_NAME = "IPVIO Stream Sync"
    const val FOLDER_MIME = "application/vnd.google-apps.folder"

    private val ID = Regex("^[A-Za-z0-9]{1,32}$")
    private val DRIVE_ID = Regex("^[A-Za-z0-9_-]{1,128}$")

    fun fileName(deviceId: String): String {
        require(ID.matches(deviceId)) { "bad device id" }
        return PREFIX + deviceId + SUFFIX
    }

    fun isSyncFile(name: String): Boolean =
        name.startsWith(PREFIX) && name.endsWith(SUFFIX) && ID.matches(deviceIdOf(name) ?: "")

    /** Device id from a sync file name, null for other names. */
    fun deviceIdOf(name: String): String? =
        if (name.startsWith(PREFIX) && name.endsWith(SUFFIX)) name.removePrefix(PREFIX).removeSuffix(SUFFIX).ifEmpty { null } else null

    /**
     * Live sync (W-010b): a second, small file per device with only the merge logs. It is kept apart from the backup
     * file above, so the manual backup (with the encrypted playlists) is never replaced by the automatic upload.
     */
    const val LIVE_PREFIX = "ipvio_live_"

    fun liveFileName(deviceId: String): String {
        require(ID.matches(deviceId)) { "bad device id" }
        return LIVE_PREFIX + deviceId + SUFFIX
    }

    fun liveDeviceIdOf(name: String): String? =
        if (name.startsWith(LIVE_PREFIX) && name.endsWith(SUFFIX))
            name.removePrefix(LIVE_PREFIX).removeSuffix(SUFFIX).takeIf { ID.matches(it) }
        else null

    fun liveListQuery(folderId: String): String {
        require(validDriveId(folderId)) { "bad folder id" }
        return "'$folderId' in parents and name contains '$LIVE_PREFIX' and trashed = false"
    }

    fun liveFileQuery(folderId: String, deviceId: String): String {
        require(validDriveId(folderId)) { "bad folder id" }
        return "'$folderId' in parents and name = '${liveFileName(deviceId)}' and trashed = false"
    }

    fun validDriveId(id: String): Boolean = DRIVE_ID.matches(id)

    /** Drive queries: only fixed names and checked ids go into them, nothing typed by the user. */
    fun folderQuery(): String = "name = '$FOLDER_NAME' and mimeType = '$FOLDER_MIME' and trashed = false"

    fun listQuery(folderId: String): String {
        require(validDriveId(folderId)) { "bad folder id" }
        return "'$folderId' in parents and name contains '$PREFIX' and trashed = false"
    }

    fun fileQuery(folderId: String, deviceId: String): String {
        require(validDriveId(folderId)) { "bad folder id" }
        return "'$folderId' in parents and name = '${fileName(deviceId)}' and trashed = false"
    }

    /** Drive time "2026-11-14T10:15:30.123Z" (also without milliseconds) in ms, 0 = unreadable. Works on API 24. */
    fun parseTime(s: String?): Long {
        if (s.isNullOrBlank()) return 0L
        for (p in listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")) {
            val f = SimpleDateFormat(p, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC"); isLenient = false }
            val pos = ParsePosition(0)
            val d = f.parse(s, pos)
            if (d != null && pos.index == s.length) return d.time
        }
        return 0L
    }

    enum class Poll { WAIT, SLOWER, DENIED, EXPIRED, FAILED }

    /** Meaning of the "error" field while waiting for the user's consent on the phone (OAuth device flow). */
    fun pollAnswer(error: String): Poll = when (error) {
        "authorization_pending" -> Poll.WAIT
        "slow_down" -> Poll.SLOWER
        "access_denied" -> Poll.DENIED
        "expired_token" -> Poll.EXPIRED
        else -> Poll.FAILED
    }
}
