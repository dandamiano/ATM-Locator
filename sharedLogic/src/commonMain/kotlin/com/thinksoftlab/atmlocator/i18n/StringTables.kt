package com.thinksoftlab.atmlocator.i18n

/**
 * Chrome, status, and freshness keys. English copy follows Design Guidelines v1.0.
 * Chichewa uses the same keys. Until a person reviews that copy, [text] falls
 * back to the English string. Do not fill [reviewedChichewa] with machine translation.
 */
enum class StringKey {
    BrandName,
    FindUsableAtm,
    HomeSupporting,
    UseMyLocation,
    GettingLocation,
    GpsDeniedTip,
    OpenSettings,
    SearchHint,
    Filters,
    NoAtmsMatch,
    ClearFilters,
    ChangeCity,
    MayBeOutdated,
    AtmCount,
    NearbyUnavailable,
    AtmNotListed,
    Fresh,
    Aging,
    StaleNotLive,
    UpdatedUnknown,
    UpdatedMinutesAgo,
    UpdatedHoursAgo,
    UpdatedDaysAgo,
    OpenDirections,
    Clear,
    Apply,
    FilterBank,
    FilterStatus,
    FilterMaxWait,
    FilterService,
    FilterCongestion,
    MaxWaitHelper,
    StatusOperational,
    StatusOutOfCash,
    StatusOutOfService,
    StatusMaintenance,
    StatusTemporarilyUnavailable,
    StatusUnknown,
    LanguageEnglish,
    LanguageChichewa,
    Offline,
    Retry,
}

private val reviewedChichewa: Map<StringKey, String> = emptyMap()

fun text(key: StringKey, language: AppLanguage, n: Long? = null): String {
    val template = when (language) {
        AppLanguage.En -> english(key)
        AppLanguage.Ny -> reviewedChichewa[key] ?: english(key)
    }
    return if (n == null) template else template.replace("{n}", n.toString())
}

internal fun isChichewaReviewed(key: StringKey): Boolean = reviewedChichewa.containsKey(key)

private fun english(key: StringKey): String = when (key) {
    StringKey.BrandName -> "ATM Locator"
    StringKey.FindUsableAtm -> "Find a usable ATM"
    StringKey.HomeSupporting -> "Choose a city or use your location."
    StringKey.UseMyLocation -> "Use my location"
    StringKey.GettingLocation -> "Getting location…"
    StringKey.GpsDeniedTip -> "Location is off."
    StringKey.OpenSettings -> "Open settings"
    StringKey.SearchHint -> "Search by place or bank"
    StringKey.Filters -> "Filters"
    StringKey.NoAtmsMatch -> "No ATMs match"
    StringKey.ClearFilters -> "Clear filters"
    StringKey.ChangeCity -> "Change city"
    StringKey.MayBeOutdated -> "May be outdated"
    StringKey.AtmCount -> "{n} ATMs"
    StringKey.NearbyUnavailable -> "ATM search is not available yet."
    StringKey.AtmNotListed -> "This ATM is not listed"
    StringKey.Fresh -> "Fresh"
    StringKey.Aging -> "Aging"
    StringKey.StaleNotLive -> "Stale — not live"
    StringKey.UpdatedUnknown -> "Updated unknown"
    StringKey.UpdatedMinutesAgo -> "Updated {n}m ago"
    StringKey.UpdatedHoursAgo -> "Updated {n}h ago"
    StringKey.UpdatedDaysAgo -> "Updated {n}d ago"
    StringKey.OpenDirections -> "Open directions"
    StringKey.Clear -> "Clear"
    StringKey.Apply -> "Apply"
    StringKey.FilterBank -> "Bank"
    StringKey.FilterStatus -> "Status"
    StringKey.FilterMaxWait -> "Max wait"
    StringKey.FilterService -> "Service"
    StringKey.FilterCongestion -> "Congestion"
    StringKey.MaxWaitHelper -> "Live wait only. Stale ATMs stay in the list."
    StringKey.StatusOperational -> "Operational"
    StringKey.StatusOutOfCash -> "Out of cash"
    StringKey.StatusOutOfService -> "Out of service"
    StringKey.StatusMaintenance -> "Maintenance"
    StringKey.StatusTemporarilyUnavailable -> "Temporarily unavailable"
    StringKey.StatusUnknown -> "Unknown"
    StringKey.LanguageEnglish -> "English"
    StringKey.LanguageChichewa -> "Chichewa"
    StringKey.Offline -> "You are offline"
    StringKey.Retry -> "Retry"
}
