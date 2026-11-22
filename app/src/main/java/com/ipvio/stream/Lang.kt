package com.ipvio.stream

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import java.util.Locale

/** UI language. "" in the settings = device language (not supported → English). */
object Lang {
    @Volatile private var loaded = false
    /** How long loading all dictionaries took (ms, 0 = not yet), for the start-up timing in Settings > Diagnostics. */
    @Volatile var loadMs = 0L
        private set

    /** Parsing time of the language in use, done in the background at the start (ms, 0 = not yet), see [prewarm]. */
    @Volatile var prewarmMs = 0L
        private set

    private fun ensureLoaded() {
        if (loaded) return
        // T-047: also called from the start-up thread of [prewarm]; the flag is set only when everything is registered
        synchronized(this) { if (!loaded) register() }
    }

    private fun register() {
        val t0 = android.os.SystemClock.elapsedRealtime()
        I18nCore.load(DictW, DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI, DictJ, DictK, DictL, DictM, DictN, DictO, DictP, DictEn1, DictEn2, DictEn3, DictPm)
        // 1.4.5: only registered here; I18nCore parses a language the first time it is used (German: never)
        I18nCore.loadColumn(I18nCore.CODES.indexOf("nl"), DictNl1, DictNl2, DictNl3, DictNl4, DictNl5, DictNl6, DictEnNl, DictNlW)
        I18nCore.loadColumn(I18nCore.CODES.indexOf("ar"), DictAr1, DictAr2, DictAr3, DictAr4, DictAr5, DictAr6, DictEnAr)
        I18nCore.loadColumn(I18nCore.CODES.indexOf("pl"), DictPlW, DictPl1, DictPl2, DictPl3, DictPl4, DictPl5, DictPl6, DictPl7, DictPl8, DictEnPl)
        loadMs = android.os.SystemClock.elapsedRealtime() - t0
        loaded = true
    }

    /**
     * T-047: parses the dictionary of the language in use on a background thread while the main thread builds the first screen.
     * Without it the first text of the first frame does this on the main thread (German needs nothing). Safe to call
     * early: [I18nCore] parses under a lock, so a text asked for meanwhile just waits for the same result.
     */
    fun prewarm() {
        val t0 = android.os.SystemClock.elapsedRealtime()
        val i = index
        if (i > 0) { I18nCore.prepare(i); prewarmMs = android.os.SystemClock.elapsedRealtime() - t0 }
    }

    /** Index in I18nCore.CODES. Reads the state so texts are redrawn on switching. */
    val index: Int
        get() {
            ensureLoaded()
            val c = General.lang
            return if (c.isBlank()) I18nCore.indexForSystem(Locale.getDefault().language) else I18nCore.CODES.indexOf(c).coerceAtLeast(0)
        }

    fun code(): String = I18nCore.CODES[index]

    fun locale(): Locale = when (code()) {
        "bs" -> Locale.Builder().setLanguage("bs").setRegion("BA").build()
        "hr" -> Locale.Builder().setLanguage("hr").setRegion("HR").build()
        "sr" -> Locale.Builder().setLanguage("sr").setScript("Latn").setRegion("RS").build()
        else -> Locale.forLanguageTag(code())
    }

    /** Language code for TMDB requests, e.g. "de-DE". */
    fun tmdb(): String = when (code()) {
        "en" -> "en-US"; "tr" -> "tr-TR"; "bs" -> "bs-BA"; "hr" -> "hr-HR"; "sr" -> "sr-RS"
        "es" -> "es-ES"; "fr" -> "fr-FR"; "it" -> "it-IT"; "nl" -> "nl-NL"; "ar" -> "ar-SA"; "pl" -> "pl-PL"; else -> "de-DE"
    }

    fun tr(s: String): String {
        val i = index
        return if (i == 0) s else I18nCore.translate(s, i)
    }
}

/**
 * Text for the whole app (replaces material3.Text, so the library is no longer needed):
 * translates UI texts into the chosen language and applies the chosen font.
 * [raw] = true for content (movie titles, channel names, descriptions, TV guide): these are never translated –
 * otherwise e.g. the series "Familie" would become "Family" in the English UI, and every new title would cost dictionary lookups.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = TextStyle.Default,
    raw: Boolean = false
) {
    val merged = style.merge(
        TextStyle(
            color = if (color != Color.Unspecified) color else if (style.color != Color.Unspecified) style.color else Color.Black,
            fontSize = fontSize, fontWeight = fontWeight, fontStyle = fontStyle,
            fontFamily = fontFamily ?: (if (style.fontFamily == null) General.fontFamily() else null),
            letterSpacing = letterSpacing, textDecoration = textDecoration,
            textAlign = textAlign ?: TextAlign.Unspecified, lineHeight = lineHeight
        )
    )
    BasicText(
        text = if (raw) text else Lang.tr(text), modifier = modifier, style = merged,
        onTextLayout = onTextLayout, overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines
    )
}
