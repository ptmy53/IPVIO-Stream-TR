package com.ipvio.stream

import org.junit.Assert.*
import org.junit.Test

class SublistLogicTest {

    private fun item(
        id: String = "1",
        kind: Kind = Kind.LIVE,
        title: String = "Channel",
        group: String = "General",
        logo: String? = null,
        tvgId: String? = null,
        lang: String = LangTagLogic.UNKNOWN,
        quality: String? = null,
        adult: Boolean = false
    ): Item = Item(
        id = id, kind = kind, title = title, year = null, url = "http://x/$id",
        logo = logo, group = if (adult) "Adult | XXX" else group,
        quality = quality, lang = lang,
        tvgId = tvgId
    )

    @Test fun disabledFilter_matchesEverything() {
        val f = SublistFilter(enabled = false)
        assertTrue(f.matches(item()))
        assertTrue(f.matches(item(kind = Kind.MOVIE)))
        assertTrue(f.matches(item(kind = Kind.SERIES)))
    }

    @Test fun kindFilter_liveOnly() {
        val f = SublistFilter(enabled = true, kind = Kind.LIVE)
        assertTrue(f.matches(item(kind = Kind.LIVE)))
        assertFalse(f.matches(item(kind = Kind.MOVIE)))
        assertFalse(f.matches(item(kind = Kind.SERIES)))
    }

    @Test fun groupsByKind_filterLiveOnly() {
        val f = SublistFilter(enabled = true, groupsByKind = mapOf(Kind.LIVE to listOf("UK")))
        assertTrue(f.matches(item(kind = Kind.LIVE, group = "UK | Entertainment")))
        assertTrue(f.matches(item(kind = Kind.LIVE, group = "uk movies")))
        // Per-kind filter: MOVIE has no group filter, so passes through
        assertTrue(f.matches(item(kind = Kind.MOVIE, group = "UK | Entertainment")))
        assertFalse(f.matches(item(kind = Kind.LIVE, group = "DE | News")))
    }

    @Test fun langsByKind_filterLiveOnly() {
        val f = SublistFilter(enabled = true, langsByKind = mapOf(Kind.LIVE to listOf("de")))
        assertTrue(f.matches(item(kind = Kind.LIVE, lang = "de")))
        assertFalse(f.matches(item(kind = Kind.LIVE, lang = "en")))
        // Per-kind filter: MOVIE has no lang filter, so passes through
        assertTrue(f.matches(item(kind = Kind.MOVIE, lang = "de")))
        assertFalse(f.matches(item(kind = Kind.LIVE, lang = LangTagLogic.UNKNOWN)))
    }

    @Test fun qualityFilter() {
        val f = SublistFilter(enabled = true, quality = "4K")
        assertTrue(f.matches(item(quality = "4K")))
        assertFalse(f.matches(item(quality = "HD")))
        assertFalse(f.matches(item(quality = null)))
    }

    @Test fun hasLogoFilter() {
        val f = SublistFilter(enabled = true, hasLogo = true)
        assertTrue(f.matches(item(logo = "http://x/logo.png")))
        assertFalse(f.matches(item(logo = null)))

        val f2 = SublistFilter(enabled = true, hasLogo = false)
        assertFalse(f2.matches(item(logo = "http://x/logo.png")))
        assertTrue(f2.matches(item(logo = null)))
    }

    @Test fun hasTvgIdFilter() {
        val f = SublistFilter(enabled = true, hasTvgId = true)
        assertTrue(f.matches(item(tvgId = "bbc.one")))
        assertFalse(f.matches(item(tvgId = null)))

        val f2 = SublistFilter(enabled = true, hasTvgId = false)
        assertFalse(f2.matches(item(tvgId = "bbc.one")))
        assertTrue(f2.matches(item(tvgId = null)))
    }

    @Test fun includeAdultFilter() {
        val f = SublistFilter(enabled = true, includeAdult = false)
        assertFalse(f.matches(item(adult = true)))
        assertTrue(f.matches(item(adult = false)))

        val f2 = SublistFilter(enabled = true, includeAdult = true)
        assertTrue(f2.matches(item(adult = true)))
    }

    @Test fun combinedFilters_allMustMatch() {
        val f = SublistFilter(
            enabled = true,
            kind = Kind.MOVIE,
            groupsByKind = mapOf(Kind.MOVIE to listOf("Action")),
            langsByKind = mapOf(Kind.MOVIE to listOf("en")),
            quality = "HD",
            includeAdult = false
        )
        assertTrue(f.matches(item(kind = Kind.MOVIE, group = "Action Movies", lang = "en", quality = "HD")))
        assertFalse(f.matches(item(kind = Kind.LIVE, group = "Action Movies", lang = "en", quality = "HD")))
        assertFalse(f.matches(item(kind = Kind.MOVIE, group = "Drama", lang = "en", quality = "HD")))
        assertFalse(f.matches(item(kind = Kind.MOVIE, group = "Action Movies", lang = "de", quality = "HD")))
        assertFalse(f.matches(item(kind = Kind.MOVIE, group = "Action Movies", lang = "en", quality = "4K")))
        assertFalse(f.matches(item(kind = Kind.MOVIE, group = "Action XXX", lang = "en", quality = "HD")))
    }

    /** A title rule alone keeps items whose title contains a pattern, whatever their group. */
    @Test fun titlesByKind_matchesTitleRegardlessOfGroup() {
        val f = SublistFilter(enabled = true, titlesByKind = mapOf(Kind.SERIES to listOf("-eng", "_eng")))
        assertTrue(f.matches(item(kind = Kind.SERIES, title = "Kingdom-eng S01E01", group = "|EN| ✪ SERIES")))
        assertTrue(f.matches(item(kind = Kind.SERIES, title = "Cashero (2025)_eng S01E01", group = "|EN| ✪ NEW RELEASE")))
        assertFalse(f.matches(item(kind = Kind.SERIES, title = "Merlin (2008) S01E01", group = "|EN| ✪ SERIES")))
        // Per-kind: LIVE has no title rule, so it passes through
        assertTrue(f.matches(item(kind = Kind.LIVE, title = "Some Channel", group = "|US| ✪ NFL")))
    }

    /** Group rule OR title rule: English-dubbed dramas inside big English groups must get through. */
    @Test fun titlesByKind_orWithGroupRule() {
        val f = SublistFilter(
            enabled = true,
            groupsByKind = mapOf(Kind.SERIES to listOf("|IN| ✪ SERIES")),
            titlesByKind = mapOf(Kind.SERIES to listOf("Extraordinary Attorney Woo"))
        )
        assertTrue(f.matches(item(kind = Kind.SERIES, title = "IN - Some Show S01E01", group = "|IN| ✪ SERIES")))
        assertTrue(f.matches(item(kind = Kind.SERIES, title = "Extraordinary Attorney Woo-eng S01E01", group = "|EN| ✪ SERIES")))
        assertFalse(f.matches(item(kind = Kind.SERIES, title = "Merlin (2008) S01E01", group = "|EN| ✪ SERIES")))
    }

    /** The title rule is still AND-ed with the other axes. */
    @Test fun titlesByKind_stillAndWithOtherAxes() {
        val f = SublistFilter(
            enabled = true,
            kind = Kind.SERIES,
            titlesByKind = mapOf(Kind.SERIES to listOf("-eng"))
        )
        assertTrue(f.matches(item(kind = Kind.SERIES, title = "Kingdom-eng S01E01")))
        assertFalse(f.matches(item(kind = Kind.MOVIE, title = "Kingdom-eng")))

        val noAdult = f.copy(includeAdult = false)
        assertFalse(noAdult.matches(item(kind = Kind.SERIES, title = "Kingdom-eng S01E01", adult = true)))
        assertTrue(noAdult.matches(item(kind = Kind.SERIES, title = "Kingdom-eng S01E01")))
    }

    /** Title patterns may contain '|', ',' and spaces and must survive both encoding layers. */
    @Test fun encodeDecodeKeepsTitlesWithSpecialCharacters() {
        val titles = mapOf(
            Kind.SERIES to listOf("Kingdom-eng", "Cashero (2025)_eng", "Genie‚ Make a Wish (2025)_eng"),
            Kind.MOVIE to listOf("EN - The Deal - 2022_eng")
        )
        val original = SublistFilter(
            enabled = true,
            groupsByKind = mapOf(Kind.LIVE to listOf("|US| ✪ NFL")),
            titlesByKind = titles
        )
        val decoded = SublistFilter.decode(SublistDef("x", original).encode().let { SublistDef.decode(it)!!.filter.encode() })
        assertEquals(titles, decoded.titlesByKind)
        assertEquals(original.groupsByKind, decoded.groupsByKind)
        assertEquals(original, decoded)
    }

    @Test fun countMatching() {
        val f = SublistFilter(enabled = true, kind = Kind.MOVIE)
        val items = listOf(
            item(kind = Kind.MOVIE),
            item(kind = Kind.MOVIE),
            item(kind = Kind.LIVE),
            item(kind = Kind.SERIES)
        )
        assertEquals(2, f.countMatching(items))
    }

    @Test fun encodeDecodeRoundTrip() {
        val original = SublistFilter(
            enabled = true,
            groupsByKind = mapOf(Kind.MOVIE to listOf("UK")),
            kind = Kind.MOVIE,
            langsByKind = mapOf(Kind.MOVIE to listOf("en")),
            quality = "HD",
            hasLogo = true,
            hasTvgId = false,
            includeAdult = false
        )
        val decoded = SublistFilter.decode(original.encode())
        assertEquals(original.enabled, decoded.enabled)
        assertEquals(original.kind, decoded.kind)
        assertEquals(original.groupsByKind, decoded.groupsByKind)
        assertEquals(original.langsByKind, decoded.langsByKind)
        assertEquals(original.quality, decoded.quality)
        assertEquals(original.hasLogo, decoded.hasLogo)
        assertEquals(original.hasTvgId, decoded.hasTvgId)
        assertEquals(original.includeAdult, decoded.includeAdult)
    }

    /** Real IPTV group names contain '|' and ',' – they must survive encode/decode unchanged. */
    @Test fun encodeDecodeKeepsGroupNamesWithPipesAndCommas() {
        val groups = mapOf(
            Kind.LIVE to listOf("|KO| ✪ KOREA", "|US| ✪ HAITI | DOMINICAN", "|US| ✪ LOCAL - ABC"),
            Kind.MOVIE to listOf("|IN| ✪ CAM & NEW RELEASE", "|IN| ✪ TELUGU  2022/2024"),
            Kind.SERIES to listOf("|IN| ✪ SERIES WEB")
        )
        val original = SublistFilter(enabled = true, groupsByKind = groups, includeAdult = true)
        val decoded = SublistFilter.decode(original.encode())
        assertEquals(groups, decoded.groupsByKind)
        assertEquals(original, decoded)

        // And after the second encoding layer used by SublistDef
        val def = SublistDef("Korean + USA + India", original)
        val roundTripped = SublistDef.decode(def.encode())!!
        assertEquals(groups, roundTripped.filter.groupsByKind)
    }

    /** After a round trip the filter must really exclude other groups. */
    @Test fun decodedFilterStillFiltersLiveGroups() {
        val f = SublistFilter(
            enabled = true,
            groupsByKind = mapOf(Kind.LIVE to listOf("|KO| ✪ KOREA", "|US| ✪ NFL"))
        )
        val live = SublistFilter.decode(SublistDef.decode(SublistDef("x", f).encode())!!.filter.encode())
        assertTrue(live.matches(item(kind = Kind.LIVE, group = "|KO| ✪ KOREA")))
        assertTrue(live.matches(item(kind = Kind.LIVE, group = "|US| ✪ NFL")))
        assertFalse(live.matches(item(kind = Kind.LIVE, group = "|BE| ✪ CINEMA")))
        assertFalse(live.matches(item(kind = Kind.LIVE, group = "|CA| ✪ CRAVE MOVIES")))
    }

    @Test fun encodeDecodeEmptyRoundTrip() {
        val original = SublistFilter()
        val decoded = SublistFilter.decode(original.encode())
        assertEquals(original, decoded)
    }

    @Test fun sublistDefEncodeDecodeRoundTrip() {
        val original = SublistDef(
            name = "UK 4K Movies",
            filter = SublistFilter(enabled = true, groupsByKind = mapOf(Kind.MOVIE to listOf("UK")), kind = Kind.MOVIE, quality = "4K")
        )
        val decoded = SublistDef.decode(original.encode())!!
        assertEquals(original.name, decoded.name)
        assertEquals(original.filter, decoded.filter)
    }

    @Test fun sublistDefDecodeNull() {
        assertNull(SublistDef.decode(null))
        assertNull(SublistDef.decode(""))
    }
}
