package com.ipvio.stream

/**
 * Box model a Stalker login presents itself as (#75). MAG250 is the default and gives the same values as before,
 * so existing logins do not change. Pure logic, no Android, testable.
 */
object StalkerModelLogic {
    const val DEFAULT = "MAG250"

    /** Models offered in the add screen (the ones commonly required by portals). */
    val MODELS = listOf("MAG250", "MAG254", "MAG256", "MAG322", "MAG324", "MAG349", "MAG351", "MAG420", "MAG424", "MAG520", "MAG524")

    /** Known model in upper case, otherwise the default (also for unknown or empty input). */
    fun clean(raw: String?): String = raw?.trim()?.uppercase()?.takeIf { it in MODELS } ?: DEFAULT

    /** Next model in the list (for the add screen: one button, pressed again for the next model). */
    fun next(current: String): String = MODELS[(MODELS.indexOf(clean(current)) + 1) % MODELS.size]

    /** X-User-Agent header for the model. */
    fun xUserAgent(model: String): String = "Model: " + clean(model) + "; Link: WiFi"
}
