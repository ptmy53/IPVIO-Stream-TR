package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SportHubLogicTest {
    @Test fun allTabsShownByDefault() = assertEquals((0..6).toList(), SportHubLogic.visible(emptySet()))
    @Test fun hiddenTabsAreLeftOut() = assertEquals(listOf(0, 1, 6), SportHubLogic.visible(setOf(2, 3, 4, 5)))
    @Test fun hidingEverythingShowsEverything() = assertEquals((0..6).toList(), SportHubLogic.visible((0..6).toSet()))
    @Test fun resolveKeepsAShownTab() = assertEquals(3, SportHubLogic.resolve(3, setOf(1)))
    @Test fun resolveMovesOffAHiddenTab() = assertEquals(0, SportHubLogic.resolve(3, setOf(3)))
    @Test fun resolveWhenFirstIsHiddenTakesTheNextShown() = assertEquals(1, SportHubLogic.resolve(0, setOf(0)))
    @Test fun encodeDecodeRoundTripAndBadInput() {
        assertEquals(setOf(1, 4), SportHubLogic.decode(SportHubLogic.encode(setOf(4, 1))))
        assertEquals(setOf(2), SportHubLogic.decode("2,x,99,-1, "))
        assertEquals(emptySet<Int>(), SportHubLogic.decode(null))
    }
    @Test fun emptySportSelectionMeansAll() {
        assertTrue(SportHubLogic.sportShown(emptySet(), "tennis"))
        assertTrue(SportHubLogic.sportShown(setOf("soccer"), "soccer"))
        assertFalse(SportHubLogic.sportShown(setOf("soccer"), "tennis"))
    }
}
