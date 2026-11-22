package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuideWrapLogicTest {

    @Test
    fun upOnFirstRowWrapsToLast() {
        assertEquals(9, GuideWrapLogic.wrapTarget(focused = 0, size = 10, down = false))
    }

    @Test
    fun downOnLastRowWrapsToFirst() {
        assertEquals(0, GuideWrapLogic.wrapTarget(focused = 9, size = 10, down = true))
    }

    @Test
    fun middleRowsKeepNormalMovement() {
        assertNull(GuideWrapLogic.wrapTarget(focused = 4, size = 10, down = true))
        assertNull(GuideWrapLogic.wrapTarget(focused = 4, size = 10, down = false))
    }

    @Test
    fun upOnLastAndDownOnFirstDoNotWrap() {
        assertNull(GuideWrapLogic.wrapTarget(focused = 9, size = 10, down = false))
        assertNull(GuideWrapLogic.wrapTarget(focused = 0, size = 10, down = true))
    }

    @Test
    fun singleOrEmptyListDoesNotWrap() {
        assertNull(GuideWrapLogic.wrapTarget(focused = 0, size = 1, down = false))
        assertNull(GuideWrapLogic.wrapTarget(focused = 0, size = 1, down = true))
        assertNull(GuideWrapLogic.wrapTarget(focused = 0, size = 0, down = true))
    }

    @Test
    fun unknownFocusDoesNotWrap() {
        assertNull(GuideWrapLogic.wrapTarget(focused = -1, size = 10, down = true))
        assertNull(GuideWrapLogic.wrapTarget(focused = 10, size = 10, down = true))
    }
}
