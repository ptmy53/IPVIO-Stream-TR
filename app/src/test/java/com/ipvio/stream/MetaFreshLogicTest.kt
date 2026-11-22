package com.ipvio.stream

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetaFreshLogicTest {
    @Test
    fun oldDetailsWithoutAgeAreUsedWhileBrowsing() {
        assertTrue(MetaFreshLogic.fresh(extended = true, sameLang = true, checked = true, ageChecked = false, needAge = false))
    }

    @Test
    fun detailPageFetchesMissingAgeOnce() {
        assertFalse(MetaFreshLogic.fresh(extended = true, sameLang = true, checked = true, ageChecked = false, needAge = true))
        assertTrue(MetaFreshLogic.fresh(extended = true, sameLang = true, checked = true, ageChecked = true, needAge = true))
    }

    @Test
    fun otherLanguageOrNotExtendedIsFetchedAgain() {
        assertFalse(MetaFreshLogic.fresh(extended = true, sameLang = false, checked = true, ageChecked = true, needAge = false))
        assertFalse(MetaFreshLogic.fresh(extended = false, sameLang = true, checked = true, ageChecked = true, needAge = false))
        assertFalse(MetaFreshLogic.fresh(extended = true, sameLang = true, checked = false, ageChecked = true, needAge = false))
    }
}
