package com.ipvio.stream

/**
 * Age rating (e.g. FSK) for the detail line. Pure logic without Android, so JVM tests can cover it.
 *
 * Source: TMDB ratings per country ("release_dates" for movies, "content_ratings" for series).
 * The country comes from the TMDB language of the app ("de-DE" -> DE). If that country has no rating,
 * the US rating is used and marked with "(US)". OMDb's "Rated" field is always the US rating.
 */
object AgeRatingLogic {
    private val NONE = setOf("N/A", "NOT RATED", "UNRATED", "NR")

    /** Country code from a TMDB language like "de-DE"; "US" when there is none. */
    fun country(tmdbLang: String): String = tmdbLang.substringAfter('-', "").trim().uppercase().ifEmpty { "US" }

    /** Certification of a movie in one country from (release type, certification) pairs: theatrical (3) first. */
    fun movieCert(dates: List<Pair<Int, String>>): String? {
        val c = dates.filter { usable(it.second) }
        return (c.firstOrNull { it.first == 3 } ?: c.firstOrNull())?.second?.trim()
    }

    /** Label for the user's [country], falling back to the US rating. Null when neither exists. */
    fun pick(byCountry: Map<String, String>, country: String): String? {
        byCountry[country]?.takeIf { usable(it) }?.let { return label(country, it, country) }
        if (country != "US") byCountry["US"]?.takeIf { usable(it) }?.let { return label("US", it, country) }
        return null
    }

    /** Label from OMDb's "Rated" field (US rating). */
    fun omdb(rated: String?, country: String): String? = rated?.takeIf { usable(it) }?.let { label("US", it, country) }

    /** "FSK 12" for a German number; the source country in brackets when it is not the user's country. */
    fun label(from: String, cert: String, country: String): String {
        val c = cert.trim()
        val base = if (from == "DE" && c.isNotEmpty() && c.all { it.isDigit() }) "FSK $c" else c
        return if (from == country) base else "$base ($from)"
    }

    private fun usable(s: String): Boolean = s.isNotBlank() && s.trim().uppercase() !in NONE
}
