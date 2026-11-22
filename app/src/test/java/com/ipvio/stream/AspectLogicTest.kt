package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Issue 10: picture area for fit / crop / stretch on a 1920 × 1080 screen. */
class AspectLogicTest {
    @Test fun fourThree_fitHasSideBars_cropFillsWidth() {
        assertEquals(1440 to 1080, AspectLogic.box(0, 4f / 3f, 1920, 1080))
        assertEquals(1920 to 1440, AspectLogic.box(1, 4f / 3f, 1920, 1080))
        assertEquals(1920 to 1080, AspectLogic.box(2, 4f / 3f, 1920, 1080))
    }

    @Test fun cinema_fitHasTopBars_cropFillsHeight() {
        assertEquals(1920 to 800, AspectLogic.box(0, 2.4f, 1920, 1080))
        assertEquals(2592 to 1080, AspectLogic.box(1, 2.4f, 1920, 1080))
    }

    @Test fun unknownRatio_fillsScreen() = assertEquals(1920 to 1080, AspectLogic.box(1, 0f, 1920, 1080))

    @Test fun sameShape() {
        assertTrue(AspectLogic.sameShape(16f / 9f, 16f / 9f))
        assertFalse(AspectLogic.sameShape(4f / 3f, 16f / 9f))
        assertFalse(AspectLogic.sameShape(0f, 16f / 9f))
    }
}
