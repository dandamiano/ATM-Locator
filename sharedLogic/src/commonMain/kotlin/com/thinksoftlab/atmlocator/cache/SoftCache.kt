package com.thinksoftlab.atmlocator.cache

/**
 * In-memory last-success payloads. Offline marks [outdated] so UI can show
 * "May be outdated". Disk persistence is a later CacheStore swap.
 */
data class CacheEntry<T>(
    val value: T,
    val fetchedAtMillis: Long,
    val outdated: Boolean = false,
)

class SoftCache<T> {
    private var entry: CacheEntry<T>? = null

    fun get(): CacheEntry<T>? = entry

    fun put(value: T, fetchedAtMillis: Long) {
        entry = CacheEntry(value = value, fetchedAtMillis = fetchedAtMillis, outdated = false)
    }

    fun markOutdated(): CacheEntry<T>? {
        val current = entry ?: return null
        val marked = current.copy(outdated = true)
        entry = marked
        return marked
    }

    fun clear() {
        entry = null
    }
}
