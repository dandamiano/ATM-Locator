package com.thinksoftlab.atmlocator.store

import android.content.Context
import com.thinksoftlab.atmlocator.i18n.AppLanguage

/**
 * Android persistence for the language toggle. Prefs name and key are stable.
 */
class SharedPreferencesLanguageRepository(
    context: Context,
) : LanguageRepository {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): AppLanguage? {
        val raw = prefs.getString(KEY_LANGUAGE, null) ?: return null
        return when (raw) {
            "En" -> AppLanguage.En
            "Ny" -> AppLanguage.Ny
            else -> null
        }
    }

    override fun save(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
    }

    companion object {
        const val PREFS_NAME: String = "atm_locator_settings"
        const val KEY_LANGUAGE: String = "language"
    }
}
