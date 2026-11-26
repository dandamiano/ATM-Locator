package com.thinksoftlab.atmlocator.i18n

enum class AppLanguage {
    En,
    Ny,
}

/**
 * First-launch helper from a device language tag.
 * A primary subtag of "ny" (including "ny-MW") selects [AppLanguage.Ny].
 * Every other tag selects [AppLanguage.En].
 * This does not clear results, filters, or any other state.
 */
fun appLanguageForTag(languageTag: String): AppLanguage {
    val primary = languageTag
        .trim()
        .substringBefore('-')
        .substringBefore('_')
        .lowercase()
    return if (primary == "ny") AppLanguage.Ny else AppLanguage.En
}
