package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** #96: category pattern "PREFIX - rest" is grouped, everything else stays (pure, no Android). */
class GroupPatternLogicTest {

    @Test
    fun split_readsPrefixAndRest_forAllSeparators() {
        assertEquals(GroupPatternLogic.Split("FR", "SPORTS"), GroupPatternLogic.split("FR - SPORTS"))
        assertEquals(GroupPatternLogic.Split("DE", "Kids"), GroupPatternLogic.split("de | Kids"))
        assertEquals(GroupPatternLogic.Split("UK", "News"), GroupPatternLogic.split("UK: News"))
    }

    @Test
    fun split_ignoresNamesWithoutPattern() {
        assertNull(GroupPatternLogic.split("THOR"))
        assertNull(GroupPatternLogic.split("24/7 - Sports"))   // prefix must be letters
        assertNull(GroupPatternLogic.split("FR -"))             // nothing after the separator
    }

    @Test
    fun analyze_groupsSharedPrefixes_andKeepsTheRest() {
        val groups = listOf("FR - SPORTS", "FR - AMAZON", "THOR", "DE | Kids", "UK: News", "fr - ACTION")
        val (folders, rest) = GroupPatternLogic.analyze(groups)
        assertEquals(1, folders.size)
        assertEquals("FR", folders[0].prefix)
        assertEquals(listOf("SPORTS", "AMAZON", "ACTION"), folders[0].subs.map { it.first })
        // Single occurrences (DE, UK) are not folders; they keep their original names
        assertEquals(listOf("THOR", "DE | Kids", "UK: News"), rest)
    }

    @Test
    fun analyze_keepsOriginalNamesForSubs() {
        val (folders, _) = GroupPatternLogic.analyze(listOf("FR - SPORTS", "FR - AMAZON"))
        assertEquals("FR - SPORTS", folders[0].subs[0].second)
    }

    @Test
    fun analyze_minSize_canBeRaised() {
        val (folders, rest) = GroupPatternLogic.analyze(listOf("FR - SPORTS", "FR - AMAZON"), minSize = 3)
        assertEquals(0, folders.size)
        assertEquals(listOf("FR - SPORTS", "FR - AMAZON"), rest)
    }
}
