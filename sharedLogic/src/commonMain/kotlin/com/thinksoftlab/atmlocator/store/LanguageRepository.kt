package com.thinksoftlab.atmlocator.store

import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.appLanguageForTag

/**
 * Persists the EN/NY choice. Implementations: SharedPreferences (Android),
 * UserDefaults (iOS), memory or localStorage (JS). Saving language must not
 * clear results, filters, or cities — that is the store's responsibility.
 */
interface LanguageRepository {
    fun load(): AppLanguage?
    fun save(language: AppLanguage)
}

/** In-memory / tests / JS default when localStorage is unused. */
class MemoryLanguageRepository(
    initial: AppLanguage? = null,
) : LanguageRepository {
    private var stored: AppLanguage? = initial

    override fun load(): AppLanguage? = stored

    override fun save(language: AppLanguage) {
        stored = language
    }
}

fun LanguageRepository.resolveInitial(deviceLanguageTag: String): AppLanguage =
    load() ?: appLanguageForTag(deviceLanguageTag)
