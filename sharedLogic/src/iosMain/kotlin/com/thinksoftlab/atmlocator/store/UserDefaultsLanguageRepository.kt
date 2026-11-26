package com.thinksoftlab.atmlocator.store

import com.thinksoftlab.atmlocator.i18n.AppLanguage
import platform.Foundation.NSUserDefaults

/**
 * iOS persistence for the language toggle via UserDefaults.
 */
class UserDefaultsLanguageRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : LanguageRepository {
    override fun load(): AppLanguage? {
        val raw = defaults.stringForKey(KEY_LANGUAGE) ?: return null
        return when (raw) {
            "En" -> AppLanguage.En
            "Ny" -> AppLanguage.Ny
            else -> null
        }
    }

    override fun save(language: AppLanguage) {
        defaults.setObject(language.name, KEY_LANGUAGE)
    }

    companion object {
        const val KEY_LANGUAGE: String = "atm_locator_language"
    }
}
