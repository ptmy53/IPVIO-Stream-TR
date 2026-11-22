package com.ipvio.stream

/**
 * Wrap-around at the ends of the TV guide channel list (issue #107): Up on the first channel goes to the last one,
 * Down on the last channel goes to the first one. Pure logic, no Android imports, so it can be tested on the JVM.
 */
object GuideWrapLogic {
    /**
     * Row index to jump to, or null when the key should keep its normal behaviour.
     * [focused] is the index of the focused row (-1 when unknown), [size] the number of rows.
     */
    fun wrapTarget(focused: Int, size: Int, down: Boolean): Int? {
        if (size < 2 || focused < 0 || focused >= size) return null
        return when {
            !down && focused == 0 -> size - 1
            down && focused == size - 1 -> 0
            else -> null
        }
    }
}
