package com.ipvio.stream

/** History of the diagnostics log (T-032): the last messages with their time, oldest first. Pure, no Android. */
object DiagLogic {
    const val MAX = 25

    /** [list] plus one entry; the message is cut to 160 characters and only the newest [max] entries are kept. */
    fun append(list: List<Pair<Long, String>>, at: Long, msg: String, max: Int = MAX): List<Pair<Long, String>> =
        (list + (at to msg.take(160))).takeLast(max)
}
