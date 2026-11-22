package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Live channels in "movie" categories, and detection of cut-off playlist downloads. */
class PlaylistCompletenessTest {

    @Test fun liveAddressStaysLiveInMovieCategory() {
        val m3u = """#EXTM3U
#EXTINF:-1 group-title="EU | DE | Movies and Series",DE: Film 24/7
http://h/live/u/p/1.ts
#EXTINF:-1 group-title="DE | Kino",DE: Kino Kanal
http://h/live/u/p/2.m3u8
#EXTINF:-1 group-title="DE | Movies",DE - Matrix (1999)
http://h/movie/u/p/3.mkv
#EXTINF:-1 group-title="Movies",Old Film
http://web/live/old.mp4
#EXTINF:-1 group-title="DE | Live",DE: Show S01E05
http://h/live/u/p/4.ts
"""
        val items = M3uParser.parse(m3u)
        val kind = items.associate { it.title to it.kind }
        assertEquals(Kind.LIVE, kind["DE: Film 24/7"])
        assertEquals(Kind.LIVE, kind["DE: Kino Kanal"])
        assertEquals(Kind.MOVIE, items.first { it.url.endsWith("3.mkv") }.kind)
        // a video file in a folder named "live" is still a movie
        assertEquals(Kind.MOVIE, items.first { it.url.endsWith("old.mp4") }.kind)
        // a live address never becomes a series, even with an episode pattern in the name
        assertEquals(Kind.LIVE, kind["DE: Show S01E05"])
        assertEquals("de", items.first { it.title == "DE: Film 24/7" }.lang)
    }

    @Test fun completeTailIsAccepted() {
        assertFalse(LoadLogic.tailLooksCut("#EXTINF:-1 group-title=\"A\",X\nhttp://h/live/u/p/1.ts\n"))
        assertFalse(LoadLogic.tailLooksCut("#EXTINF:-1 group-title=\"A\",X\r\nhttp://h/live/u/p/1.ts\r\n"))
        // no line break at the very end, but the last line is an address
        assertFalse(LoadLogic.tailLooksCut("#EXTINF:-1,X\nhttp://h/live/u/p/1.ts"))
        assertFalse(LoadLogic.tailLooksCut(""))
    }

    @Test fun cutTailIsDetected() {
        // entry without its address
        assertTrue(LoadLogic.tailLooksCut("http://h/live/u/p/1.ts\n#EXTINF:-1 group-title=\"AS | News\",Y\n"))
        // cut inside the #EXTINF line
        assertTrue(LoadLogic.tailLooksCut("http://h/live/u/p/1.ts\n#EXTINF:-1 tvg-id=\"ab"))
        // cut at the start of an address line
        assertTrue(LoadLogic.tailLooksCut("#EXTINF:-1,X\nht"))
    }

    @Test fun groupTitleIsRead() {
        assertEquals("EU | DE | Movies and Series", LoadLogic.groupTitle("#EXTINF:-1 tvg-id=\"x\" group-title=\"EU | DE | Movies and Series\",Name"))
        assertEquals(null, LoadLogic.groupTitle("#EXTINF:-1,Name"))
        assertEquals("eu | de", LoadLogic.normGroup("  EU | DE "))
    }

    @Test fun missingCategoriesNeedsMostOfThemGone() {
        val api = listOf("AM | CA", "AR | News", "AS | Sports", "EU | DE | Movies and Series", "EU | DE | News", "EU | FR", "EU | UK", "US | News")
        val all = api.map { LoadLogic.normGroup(it) }.toSet()
        assertFalse(LoadLogic.missingCategories(api, all))
        // some empty categories at the provider are fine
        assertFalse(LoadLogic.missingCategories(api, all - LoadLogic.normGroup("US | News") - LoadLogic.normGroup("EU | FR")))
        // list cut after "AS": most categories missing
        assertTrue(LoadLogic.missingCategories(api, setOf("am | ca", "ar | news", "as | sports")))
        // too few categories to decide
        assertFalse(LoadLogic.missingCategories(listOf("A", "B", "C"), emptySet()))
    }
}
