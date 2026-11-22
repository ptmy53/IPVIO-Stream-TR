package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupSearchTest {
    private fun m(q: String) = GroupSearch.matcher(q)!!

    @Test fun normalizeIgnoresCaseAndSeparators() {
        assertEquals("eu de sport", GroupSearch.normalize("  [EU] | DE:  Sport "))
        assertEquals("ä ö", GroupSearch.normalize("Ä|Ö"))
        assertEquals("", GroupSearch.normalize(" | - "))
    }

    @Test fun emptyQueryMeansNoSearch() {
        assertNull(GroupSearch.matcher(""))
        assertNull(GroupSearch.matcher(" | "))
        assertEquals(listOf("a", "b"), GroupSearch.hits(listOf("a", "b"), " ") { it })
    }

    @Test fun separatorsDoNotMatter() {
        val q = m("EU | DE")
        assertTrue(q("EU | DE Sport"))
        assertTrue(q("EU|DE"))
        assertTrue(q("eu - de kino"))
        assertTrue(q("[EU] DE: Cinema"))
    }

    @Test fun shortCodesMatchWhole() {
        val q = m("EU | DE")
        assertFalse(q("EU | DEPORTES"))
        assertFalse(q("DE | EU"))
        assertFalse(q("TR | DE"))
    }

    @Test fun longerLastWordMatchesWordStart() {
        assertTrue(m("EU | Spo")("EU | Sports HD"))
        assertTrue(m("sport")("DE | Sport 1"))
        assertFalse(m("port")("DE | Sport 1"))
    }

    @Test fun hitsFilterTheList() {
        val l = listOf("EU | DE Kino", "EU | FR Cinéma", "TR | Spor", "EU | DE Sport")
        assertEquals(listOf("EU | DE Kino", "EU | DE Sport"), GroupSearch.hits(l, "eu de") { it })
    }
}
