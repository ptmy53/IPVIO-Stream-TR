package com.ipvio.stream

/**
 * Number of live channels, movies and series of one loaded playlist (#111). Stored once per load, not recounted on
 * every settings screen. Pure logic, no Android, testable.
 */
object PlaylistCountLogic {
    data class TypeCounts(val live: Int, val movies: Int, val series: Int)

    /** Stored form: "live,movies,series". */
    fun encode(c: TypeCounts): String = "${c.live},${c.movies},${c.series}"

    /** Reads the stored form; null if missing or broken (then nothing is shown). */
    fun decode(s: String?): TypeCounts? {
        val parts = s?.split(',') ?: return null
        if (parts.size != 3) return null
        val n = parts.map { it.trim().toIntOrNull() ?: return null }
        if (n.any { it < 0 }) return null
        return TypeCounts(n[0], n[1], n[2])
    }

    /** Line for the settings list, e.g. "Live TV: 1240 · Filme: 8500 · Serien: 2100" (labels come from the caller). */
    fun summary(c: TypeCounts, liveLabel: String, moviesLabel: String, seriesLabel: String): String =
        "$liveLabel: ${c.live}  ·  $moviesLabel: ${c.movies}  ·  $seriesLabel: ${c.series}"
}
