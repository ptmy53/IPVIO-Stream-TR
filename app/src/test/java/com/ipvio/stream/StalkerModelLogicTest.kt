package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Test

/** #75: Stalker box model; the default MAG250 keeps the old identity values (pure, no Android). */
class StalkerModelLogicTest {

    @Test
    fun defaultModel_isMag250_andHeaderUnchanged() {
        assertEquals("MAG250", StalkerModelLogic.clean(null))
        assertEquals("Model: MAG250; Link: WiFi", StalkerModelLogic.xUserAgent(StalkerModelLogic.DEFAULT))
    }

    @Test
    fun knownModel_isAcceptedCaseInsensitive() {
        assertEquals("MAG424", StalkerModelLogic.clean(" mag424 "))
        assertEquals("Model: MAG424; Link: WiFi", StalkerModelLogic.xUserAgent("MAG424"))
    }

    @Test
    fun unknownModel_fallsBackToDefault() {
        assertEquals("MAG250", StalkerModelLogic.clean("MAG999"))
        assertEquals("MAG250", StalkerModelLogic.clean(""))
    }

    @Test
    fun defaultModel_isNotWrittenSoOldKeysStay() {
        val src = StalkerLogic.source("http://p.example", "00:1A:79:AB:CD:EF")!!
        assertEquals(false, src.encode().contains("#stb="))
    }

    @Test
    fun chosenModel_isWrittenAndReadBack() {
        val src = StalkerLogic.source("http://p.example", "00:1A:79:AB:CD:EF", modelRaw = "MAG424")!!
        assertEquals("MAG424", StalkerLogic.parse(src.encode())!!.model)
        assertEquals(true, src.encode().endsWith("#stb=MAG424"))
    }

    @Test
    fun next_cyclesThroughAllModels() {
        assertEquals("MAG254", StalkerModelLogic.next("MAG250"))
        assertEquals("MAG250", StalkerModelLogic.next(StalkerModelLogic.MODELS.last()))
        // An unknown current value counts as the default, so the next one is MAG254
        assertEquals("MAG254", StalkerModelLogic.next("unbekannt"))
    }
}
