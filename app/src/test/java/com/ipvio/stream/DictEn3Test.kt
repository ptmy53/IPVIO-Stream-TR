package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Test

/** T-044 English rows for the Stalker portal messages. */
class DictEn3Test {
    private fun en(s: String) = run {
        I18nCore.load(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI, DictJ, DictK, DictL, DictM, DictN, DictO, DictP, DictEn1, DictEn2, DictEn3)
        I18nCore.translate(s, 1)
    }

    @Test fun portal_not_responding_correctly() =
        assertEquals("The portal does not respond correctly (3). Check the address and the MAC address.",
            en("Das Portal antwortet nicht richtig (3). Adresse und MAC-Adresse prüfen."))

    @Test fun portal_retry_in_seconds() =
        assertEquals("Portal does not respond, new attempt in 30 s …", en("Portal antwortet nicht, neuer Versuch in 30 s …"))
}
