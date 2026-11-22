package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Test

/** Stage 2a English patterns (T-041): runtime texts resolve to the English pattern. */
class DictEn2Test {
    private fun en(s: String) = run {
        I18nCore.load(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI, DictJ, DictK, DictL, DictM, DictN, DictO, DictP, DictEn1, DictEn2)
        I18nCore.translate(s, 1)
    }

    @Test fun opensubtitles_error() = assertEquals("OpenSubtitles: error 500", en("OpenSubtitles: Fehler 500"))
    @Test fun server_error() = assertEquals("Server error 500", en("Server-Fehler 500"))
    @Test fun series_genre() = assertEquals("Series · Crime", en("Serien · Krimi"))
    @Test fun movies_genre() = assertEquals("Movies · Crime", en("Filme · Krimi"))
    @Test fun loading_channels() = assertEquals("Loading channels (5000) …", en("Sender werden geladen (5000) …"))
    @Test fun loading_categories() = assertEquals("Loading categories (2/9) …", en("Kategorien werden geladen (2/9) …"))
    @Test fun search_movies() = assertEquals("Search „Batman“ · Movies", en("Suche „Batman“ · Filme"))
}
