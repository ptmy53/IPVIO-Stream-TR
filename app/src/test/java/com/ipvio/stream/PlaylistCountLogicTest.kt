package com.ipvio.stream

import com.ipvio.stream.PlaylistCountLogic.TypeCounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** #111: stored per-type counts of a playlist (pure, no Android). */
class PlaylistCountLogicTest {

    @Test
    fun roundTrip_keepsAllThreeNumbers() {
        val c = TypeCounts(1240, 8500, 2100)
        assertEquals(c, PlaylistCountLogic.decode(PlaylistCountLogic.encode(c)))
    }

    @Test
    fun zeroCounts_areStoredAndRead() {
        assertEquals(TypeCounts(0, 0, 0), PlaylistCountLogic.decode("0,0,0"))
    }

    @Test
    fun missingOrBroken_givesNull() {
        assertNull(PlaylistCountLogic.decode(null))
        assertNull(PlaylistCountLogic.decode(""))
        assertNull(PlaylistCountLogic.decode("1,2"))
        assertNull(PlaylistCountLogic.decode("1,x,3"))
        assertNull(PlaylistCountLogic.decode("1,-2,3"))
    }

    @Test
    fun summary_usesGivenLabels() {
        assertEquals(
            "Live TV: 1240  ·  Filme: 8500  ·  Serien: 2100",
            PlaylistCountLogic.summary(TypeCounts(1240, 8500, 2100), "Live TV", "Filme", "Serien"),
        )
    }
}
