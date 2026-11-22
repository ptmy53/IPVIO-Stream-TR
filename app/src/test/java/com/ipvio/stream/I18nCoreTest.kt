package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class I18nCoreTest {
    @Before fun setUp() { I18nCore.load(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI) }

    private val EN = 1; private val TR = 2; private val BS = 3; private val HR = 4; private val SR = 5; private val DE = 0

    @Test fun exact() {
        assertEquals("Parental controls", I18nCore.translate("Jugendschutz", EN))
        assertEquals("Ebeveyn kontrolü", I18nCore.translate("Jugendschutz", TR))
    }
    @Test fun germanIsIdentity() {
        assertEquals("Jugendschutz", I18nCore.translate("Jugendschutz", DE))
    }
    @Test fun croatianAndSerbianFallBackToBosnian() {
        assertEquals(I18nCore.translate("Jugendschutz", BS), I18nCore.translate("Jugendschutz", HR))
        assertEquals(I18nCore.translate("Jugendschutz", BS), I18nCore.translate("Jugendschutz", SR))
    }
    @Test fun surroundingSpacesAreKept() {
        assertEquals("  Reload active playlist ", I18nCore.translate("  Aktive Playlist neu laden ", EN))
    }
    @Test fun noEmojisInDictionaries() {
        // Symbols are drawn (AppIcons), texts contain no emojis/pictographs
        for (d in listOf(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI)) for (ch in d) {
            val c = ch.code
            val pictographic = c in 0x2190..0x21FF && ch != '→' && ch != '↑' && ch != '↓' || c in 0x2300..0x23FF || c in 0x2460..0x27BF ||
                c in 0x2B00..0x2BFF || c in 0xFF01..0xFF0F || Character.isSurrogate(ch)
            org.junit.Assert.assertFalse("Zeichen U+" + Integer.toHexString(c), pictographic)
        }
    }
    @Test fun patternWithNumber() {
        assertEquals("every 5 s", I18nCore.translate("alle 5 s", EN))
    }
    @Test fun unknownFallsBackToOriginal() {
        assertEquals("Völlig unbekannter Text 123", I18nCore.translate("Völlig unbekannter Text 123", EN))
    }
    @Test fun guideLegend() {
        assertEquals("watch", I18nCore.translate("ansehen", EN))
        assertEquals("close", I18nCore.translate("schließen", EN))
        assertEquals("NOW", I18nCore.translate("JETZT", EN))
        assertEquals("Subtitle style", I18nCore.translate("Untertitel-Stil", EN))
        assertEquals("Playback speed", I18nCore.translate("Wiedergabe-Tempo", EN))
    }
    @Test fun systemLanguage() {
        assertEquals(TR, I18nCore.indexForSystem("tr"))
        assertEquals(EN, I18nCore.indexForSystem("ja"))
    }
    @Test fun lazyColumnIsReadOnFirstUse() {
        val nl = I18nCore.CODES.indexOf("nl")
        I18nCore.load("Lazy-Spalte Testtext¦Lazy column test text")
        I18nCore.loadColumn(nl, "Lazy-Spalte Testtext¦Luie kolom testtekst")
        assertEquals("Lazy column test text", I18nCore.translate("Lazy-Spalte Testtext", EN))
        assertEquals("Luie kolom testtekst", I18nCore.translate("Lazy-Spalte Testtext", nl))
    }
    @Test fun blockLoadedLaterReachesLanguageInUse() {
        assertEquals("Parental controls", I18nCore.translate("Jugendschutz", EN))   // English now in use
        I18nCore.load("Später geladener Testtext¦Text loaded later¦Sonra yüklenen metin")
        assertEquals("Text loaded later", I18nCore.translate("Später geladener Testtext", EN))
        assertEquals("Sonra yüklenen metin", I18nCore.translate("Später geladener Testtext", TR))
    }
    @Test fun registeringTheSameBlockTwiceIsHarmless() {
        I18nCore.load(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI)
        assertEquals("Parental controls", I18nCore.translate("Jugendschutz", EN))
    }
    @Test fun prepareParsesTheLanguageAhead() {
        val nl = I18nCore.CODES.indexOf("nl")
        I18nCore.load("Vorwärm-Testtext¦Prewarm test text¦¦¦¦¦¦¦¦Voorverwarm testtekst")
        I18nCore.prepare(nl)
        I18nCore.prepare(0)       // German: nothing to do
        I18nCore.prepare(99)      // out of range: ignored
        assertEquals("Voorverwarm testtekst", I18nCore.translate("Vorwärm-Testtext", nl))
    }
    @Test fun prepareOnAnotherThreadGivesTheSameTexts() {
        val t = Thread { I18nCore.prepare(TR) }
        t.start()
        assertEquals("Parental controls", I18nCore.translate("Jugendschutz", EN))
        t.join()
        assertEquals(I18nCore.translate("Jugendschutz", TR), I18nCore.translate("Jugendschutz", TR))
    }
}
