package com.ipvio.stream

/**
 * Private categories (T-028), without Android and testable: content of a private category shows only inside that
 * category and is never recorded (history, continue watching). See [PrivateCats] for the stored list.
 */
object PrivateLogic {
    /** Key of one category: kind + provider group, so a Live and a movie category with the same name stay apart. */
    fun key(kind: Kind, group: String): String = kind.name + "\t" + group

    /** True if the item belongs to a private category, or [parental] is on and the parental check flags it. */
    fun isPrivate(item: Item, keys: Set<String>, parental: Boolean): Boolean =
        (keys.isNotEmpty() && key(item.kind, item.group) in keys) || (parental && ParentalLogic.isAdult(item.group, item.title))

    /** The list without private items; the same list object when nothing is private (keeps caches keyed by identity). */
    fun visible(all: List<Item>, keys: Set<String>, parental: Boolean): List<Item> {
        if (keys.isEmpty() && !parental) return all
        val p = matcher(keys, parental)
        return all.filterNot(p)
    }

    /** Ids of all private items in [all]. */
    fun ids(all: List<Item>, keys: Set<String>, parental: Boolean): Set<String> {
        if (keys.isEmpty() && !parental) return emptySet()
        val p = matcher(keys, parental)
        return all.asSequence().filter(p).mapTo(HashSet()) { it.id }
    }

    /** Bulk check for 100,000+ items: groups per kind are looked up without building a key string per item. */
    private fun matcher(keys: Set<String>, parental: Boolean): (Item) -> Boolean {
        val byKind = HashMap<Kind, HashSet<String>>()
        for (k in keys) {
            val i = k.indexOf('\t')
            val kind = if (i > 0) runCatching { Kind.valueOf(k.substring(0, i)) }.getOrNull() else null
            if (kind != null) byKind.getOrPut(kind) { HashSet() }.add(k.substring(i + 1))
        }
        return { it -> byKind[it.kind]?.contains(it.group) == true || (parental && ParentalLogic.isAdult(it.group, it.title)) }
    }

    /** Stored text (one key per line) → keys; blank lines are dropped. */
    fun decode(s: String?): Set<String> = s.orEmpty().split('\n').filter { it.isNotBlank() }.toCollection(LinkedHashSet())

    fun encode(keys: Collection<String>): String = keys.joinToString("\n")
}
