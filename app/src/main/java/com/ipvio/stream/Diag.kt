package com.ipvio.stream

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Small error log for the diagnostics area. Deliberately stores ONLY short messages:
 * no addresses, no user names, no passwords.
 */
object Diag {
    var lastError by mutableStateOf<String?>(null)
    var lastErrorAt by mutableLongStateOf(0L)
    var errorCount by mutableLongStateOf(0L)

    /** The last [DiagLogic.MAX] messages with their time, oldest first (before, each message replaced the last one). */
    var history by mutableStateOf<List<Pair<Long, String>>>(emptyList())
        private set

    /** A neutral line for the log in Settings > Diagnostics (timings and the like): not counted as an error. */
    @Synchronized
    fun note(msg: String) {
        history = DiagLogic.append(history, System.currentTimeMillis(), msg)
    }

    @Synchronized
    fun report(msg: String) {
        val now = System.currentTimeMillis()
        lastError = msg.take(160)
        lastErrorAt = now
        errorCount++
        history = DiagLogic.append(history, now, msg)
    }
}
