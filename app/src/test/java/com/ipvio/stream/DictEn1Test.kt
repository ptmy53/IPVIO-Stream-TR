package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Test

/** Stage 1 English rows (T-041): exactly two columns per row, unique German keys, English lookup works. */
class DictEn1Test {
    private fun rawRows(): List<List<String>> = DictEn1.lineSequence()
        .filter { it.isNotBlank() }
        .map { it.split('¦') }
        .toList()

    @Test fun every_row_has_german_key_and_english_text() {
        val bad = rawRows().filter { it.size < 2 || it[0].isBlank() || it[1].isBlank() }
        assertEquals("rows without German¦English: $bad", 0, bad.size)
    }

    @Test fun german_keys_are_unique() {
        val keys = rawRows().map { it[0] }
        assertEquals("duplicate keys", keys.size, keys.toSet().size)
    }

    @Test fun english_lookup_returns_the_english_text() {
        I18nCore.load(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI, DictJ, DictK, DictL, DictM, DictN, DictO, DictP, DictEn1)
        assertEquals("Nothing found", I18nCore.translate("Nichts gefunden", 1))
        assertEquals("Error", I18nCore.translate("Fehler", 1))
    }

    private val ph = Regex("""\{#?(\d)\}""")

    @Test fun placeholders_match_in_every_language() {
        val bad = ArrayList<String>()
        for (block in listOf(DictEn1, DictEn2, DictEn3)) for (line in block.lineSequence()) {
            if (!line.contains('¦')) continue
            val c = line.split('¦')
            val want = ph.findAll(c[0]).map { it.groupValues[1] }.toSet()
            for (x in c.drop(1)) if (x.isNotBlank() && ph.findAll(x).map { it.groupValues[1] }.toSet() != want) bad.add(c[0])
        }
        assertEquals("rows with a different placeholder set: $bad", 0, bad.size)
    }

    @Test fun dutch_arabic_polish_columns_load() {
        I18nCore.load(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI, DictJ, DictK, DictL, DictM, DictN, DictO, DictP, DictEn1, DictEn2, DictEn3)
        I18nCore.loadColumn(I18nCore.CODES.indexOf("nl"), DictNl1, DictNl2, DictNl3, DictNl4, DictNl5, DictNl6, DictEnNl)
        I18nCore.loadColumn(I18nCore.CODES.indexOf("pl"), DictPl1, DictPl2, DictPl3, DictPl4, DictPl5, DictPl6, DictPl7, DictPl8, DictEnPl)
        assertEquals("Fout", I18nCore.translate("Fehler", I18nCore.CODES.indexOf("nl")))
        assertEquals("Błąd", I18nCore.translate("Fehler", I18nCore.CODES.indexOf("pl")))
    }
}
