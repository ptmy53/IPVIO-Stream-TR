package com.ipvio.stream

/**
 * Picture area for the aspect modes 0 = fit, 1 = crop (fill the screen, cut the overhang), 2 = stretch (issue 10).
 * Pure, without Android, so it can be tested. [ar] = display aspect ratio of the video (width / height).
 */
object AspectLogic {
    /** Size of the video area inside a [w] × [h] screen, centered by the caller. */
    fun box(mode: Int, ar: Float, w: Int, h: Int): Pair<Int, Int> {
        if (ar <= 0f || w <= 0 || h <= 0 || mode == 2) return w to h
        val screen = w.toFloat() / h
        val wider = ar > screen
        return when (mode) {
            // Crop: the short side fills the screen, the long side runs over
            1 -> if (wider) Math.round(h * ar) to h else w to Math.round(w / ar)
            // Fit: the long side fills the screen, bars on the short side
            else -> if (wider) w to Math.round(w / ar) else Math.round(h * ar) to h
        }
    }

    /** True when the video has (almost) the shape of the screen: then fit, crop and stretch look the same. */
    fun sameShape(ar: Float, screen: Float): Boolean = ar > 0f && kotlin.math.abs(ar / screen - 1f) < 0.02f
}
