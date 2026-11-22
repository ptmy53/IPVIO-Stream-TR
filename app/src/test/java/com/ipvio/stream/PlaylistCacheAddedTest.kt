package com.ipvio.stream

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test

/** #22: the added date survives a cache round trip (format version 6). Pure JVM, no Android. */
class PlaylistCacheAddedTest {

    @Test
    fun addedDate_isStoredAndReadBack() {
        val item = Item("1", Kind.MOVIE, "Film", 2020, "http://x/movie/1.mkv", null, "G", added = 1700000000L)
        val out = ByteArrayOutputStream()
        PlaylistCacheIO.write(out, "http://a", listOf(item), emptyList(), emptyMap())
        val c = PlaylistCacheIO.read(ByteArrayInputStream(out.toByteArray()), "http://a", emptySet())!!
        assertEquals(1700000000L, c.items.single().added)
        assertEquals("Film", c.items.single().title)
    }

    @Test
    fun defaultDate_isZero() {
        val item = Item("2", Kind.MOVIE, "Ohne Datum", null, "http://x/movie/2.mkv", null, "G")
        val out = ByteArrayOutputStream()
        PlaylistCacheIO.write(out, "http://a", listOf(item), emptyList(), emptyMap())
        val c = PlaylistCacheIO.read(ByteArrayInputStream(out.toByteArray()), "http://a", emptySet())!!
        assertEquals(0L, c.items.single().added)
    }
}
