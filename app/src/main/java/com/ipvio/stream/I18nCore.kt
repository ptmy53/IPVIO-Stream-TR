package com.ipvio.stream

/**
 * Translation core (without Android, testable). Source texts are the German UI texts.
 * Line format: de ¦ en ¦ tr ¦ bs ¦ hr ¦ sr ¦ es ¦ fr ¦ it ¦ nl   (hr/sr empty = same as bs; others empty = same as en)
 * Placeholders: {0} any text, {#0} number. A line break is written as \n.
 */
object I18nCore {
    val CODES = listOf("de", "en", "tr", "bs", "hr", "sr", "es", "fr", "it", "nl", "ar", "pl")
    val NATIVE = listOf("Deutsch", "English", "Türkçe", "Bosanski", "Hrvatski", "Srpski", "Español", "Français", "Italiano", "Nederlands", "العربية", "Polski")

    private class Pat(val src: String, val re: Regex, val tr: Array<String?>, val numeric: BooleanArray)

    private val exact = HashMap<String, Array<String?>>()
    private val byDe = HashMap<String, Array<String?>>()
    private val pats = ArrayList<Pat>()
    private val memo = Array(CODES.size) { java.util.concurrent.ConcurrentHashMap<String, String>() }
    private val PH = Regex("""\{(#?)(\d)\}""")
    // Surrounding spaces and arrow characters (‹ ›) are not part of the text that gets translated
    private val DECO = Regex("""(?s)^([‹›\s]*)(.*?)([\s‹›]*)$""")
    private val DOT = Regex("""\s*·\s*""")

    /*
     * Lazy loading (1.4.5): [load] and [loadColumn] only register their blocks. Nothing is parsed until a text is
     * translated into a language other than German, and then only that language: the German keys (and the patterns)
     * once, the column of the language when it is first used. German needs no dictionary at all.
     */
    /** Main blocks (all columns per line), in registration order. */
    private val mainBlocks = ArrayList<String>()
    /** Main blocks whose German keys are not read yet. */
    private val newMain = ArrayList<String>()
    /** Separate columns ("German¦translation") per language, in registration order. */
    private val columnBlocks = Array(CODES.size) { ArrayList<String>() }
    /** Language whose texts are filled in completely. Index 0 (German) needs nothing. */
    private val ready = BooleanArray(CODES.size)
    @Volatile private var readyMask = 1   // bit i = language i ready; read without the lock in [translate]

    private fun parts(line: String): List<String> = line.split('¦').map { it.trim().replace("\\n", "\n") }

    /** Registers dictionary blocks (lines "de ¦ en ¦ tr ¦ …"). A block registered before is ignored. */
    @Synchronized
    fun load(vararg blocks: String) {
        var added = false
        for (b in blocks) if (mainBlocks.none { it === b }) { mainBlocks.add(b); newMain.add(b); added = true }
        if (!added) return
        // Languages already in use get the new rows at once (e.g. the wishbox texts, loaded later)
        if (readyMask != 1) { readKeys(); for (li in 1 until CODES.size) if (ready[li]) fill(li, newMainOnly = true) }
    }

    /** Registers a single language column (rows "German¦translation") for already registered rows. */
    @Synchronized
    fun loadColumn(li: Int, vararg blocks: String) {
        if (li !in CODES.indices || li == 0) return
        val list = columnBlocks[li]
        var added = false
        for (b in blocks) if (list.none { it === b }) { list.add(b); added = true }
        if (added && ready[li]) { applyColumns(li); memo[li].clear() }
    }

    /** German keys and patterns of the blocks registered since the last call. */
    private fun readKeys() {
        if (newMain.isEmpty()) return
        for (b in newMain) for (line in b.lineSequence()) {
            if (line.isBlank() || line.startsWith("#") || !line.contains('¦')) continue
            val de = line.substringBefore('¦').trim().replace("\\n", "\n")
            val row = arrayOfNulls<String>(CODES.size)
            row[0] = de
            if (PH.containsMatchIn(de)) {
                val sb = StringBuilder("^")
                var last = 0
                val numeric = ArrayList<Boolean>()
                for (m in PH.findAll(de)) {
                    sb.append(Regex.escape(de.substring(last, m.range.first)))
                    sb.append(if (m.groupValues[1] == "#") """([\d.,:]+)""" else "(.+?)")
                    numeric.add(m.groupValues[1] == "#")
                    last = m.range.last + 1
                }
                sb.append(Regex.escape(de.substring(last))).append("$")
                // Placeholder numbers in order of appearance (0,1,2 …)
                pats.removeAll { it.src == de }
                pats.add(Pat(de, Regex(sb.toString(), RegexOption.DOT_MATCHES_ALL), row, numeric.toBooleanArray()))
            } else exact[de] = row
            byDe[de] = row
        }
        newMain.clear()
        pats.sortByDescending { it.re.pattern.length }
    }

    /**
     * Fills column [li] from the main blocks (hr/sr empty = same as bs; others empty = same as en). The rows of a German
     * text that appears twice belong to the last block, as when everything was read at once.
     */
    private fun fill(li: Int, newMainOnly: Boolean = false) {
        val blocks = if (newMainOnly) mainBlocks.takeLast(mainBlocks.size - filledUpTo[li]) else mainBlocks
        for (b in blocks) for (line in b.lineSequence()) {
            if (line.isBlank() || line.startsWith("#") || !line.contains('¦')) continue
            val p = parts(line)
            val de = p[0]
            val en = p.getOrElse(1) { "" }.ifEmpty { de }
            val v = when (li) {
                1 -> en
                4, 5 -> p.getOrElse(li) { "" }.ifEmpty { p.getOrElse(3) { "" }.ifEmpty { en } }
                else -> p.getOrElse(li) { "" }.ifEmpty { en }
            }
            byDe[de]?.set(li, v)
        }
        filledUpTo[li] = mainBlocks.size
        applyColumns(li)
        memo[li].clear()
    }
    /** Number of main blocks already filled per language. */
    private val filledUpTo = IntArray(CODES.size)

    private fun applyColumns(li: Int) {
        for (b in columnBlocks[li]) for (line in b.lineSequence()) {
            if (line.isBlank() || line.startsWith("#")) continue
            val p = parts(line)
            if (p.size < 2 || p[1].isEmpty()) continue
            byDe[p[0]]?.set(li, p[1])
        }
    }

    /** Parses what language [li] needs, once. */
    @Synchronized
    private fun ensure(li: Int) {
        readKeys()
        if (!ready[li]) { fill(li); ready[li] = true; readyMask = readyMask or (1 shl li) }
    }

    /** Parses the dictionary of language [li] now (for a background thread at the start); no effect for German. */
    fun prepare(li: Int) {
        if (li <= 0 || li >= CODES.size) return
        if (readyMask and (1 shl li) == 0) ensure(li)
    }

    @Synchronized
    fun size(): Int { readKeys(); return exact.size + pats.size }

    fun translate(s: String, li: Int): String {
        if (li <= 0 || li >= CODES.size || s.isEmpty()) return s
        if (readyMask and (1 shl li) == 0) ensure(li)
        val m = memo[li]
        m[s]?.let { return it }
        val r = lookup(s, li, 0) ?: s
        if (m.size > 6000) m.clear()
        m[s] = r
        return r
    }

    private fun lookup(s: String, li: Int, depth: Int): String? {
        if (depth > 4) return null
        exact[s]?.let { return it[li] }
        val d = DECO.matchEntire(s)
        if (d != null) {
            val pre = d.groupValues[1]; val core = d.groupValues[2]; val suf = d.groupValues[3]
            if (core.isNotEmpty() && (pre.isNotEmpty() || suf.isNotEmpty()) && core != s) {
                lookup(core, li, depth + 1)?.let { return pre + it + suf }
            }
        }
        for (p in pats) {
            val m = p.re.matchEntire(s) ?: continue
            var out = p.tr[li] ?: continue
            for (i in 0 until m.groupValues.size - 1) {
                val g = m.groupValues[i + 1]
                val v = if (i < p.numeric.size && p.numeric[i]) g else (lookup(g, li, depth + 1) ?: g)
                out = out.replace("{" + i + "}", v).replace("{#" + i + "}", v)
            }
            return out
        }
        // Composite texts: "A · B" and "Name: Value"
        val seps = DOT.findAll(s).toList()
        if (seps.isNotEmpty()) {
            val parts = ArrayList<String>()
            var last = 0
            for (m in seps) { parts.add(s.substring(last, m.range.first)); last = m.range.last + 1 }
            parts.add(s.substring(last))
            var any = false
            val tr = parts.map { part -> lookup(part, li, depth + 1)?.also { any = true } ?: part }
            if (any) {
                val sb = StringBuilder(tr[0])
                for (i in seps.indices) sb.append(seps[i].value).append(tr[i + 1])
                return sb.toString()
            }
        }
        val c = s.indexOf(": ")
        if (c > 0) {
            val a = lookup(s.substring(0, c), li, depth + 1)
            val b = lookup(s.substring(c + 2), li, depth + 1)
            if (a != null || b != null) return (a ?: s.substring(0, c)) + ": " + (b ?: s.substring(c + 2))
        }
        return null
    }

    /** Language index for a device language code (e.g. "tr"); unknown → English. */
    fun indexForSystem(code: String): Int {
        val i = CODES.indexOf(code.lowercase())
        return if (i >= 0) i else 1
    }
}
