package com.ipvio.stream

/**
 * When stored TMDB details can be used without asking TMDB again. Pure logic, no Android, testable.
 *
 * 1.4.4 added the age rating and treated every detail stored before as outdated, so each title that got the focus
 * (slideshow, billboard, rows, search, list preview) was fetched again from TMDB and OMDb. Browsing became slow.
 * Since 1.4.5 only the detail page ([needAge]) fetches a missing age rating.
 */
object MetaFreshLogic {
    fun fresh(extended: Boolean, sameLang: Boolean, checked: Boolean, ageChecked: Boolean, needAge: Boolean): Boolean =
        extended && sameLang && checked && (ageChecked || !needAge)
}
