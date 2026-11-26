package com.thinksoftlab.atmlocator.i18n

/**
 * Chrome, status, and freshness keys. English copy follows Design Guidelines v1.0.
 * Chichewa uses the same keys. Until a person reviews that copy, [text] falls
 * back to the English string. Do not fill [reviewedChichewa] with machine translation.
 */
enum class StringKey {
    FindUsableAtm,
    UseMyLocation,
    GettingLocation,
    GpsDeniedTip,
    NoAtmsMatch,
    MayBeOutdated,
    AtmCount,
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
    StringKey.FindUsableAtm -> "Find a usable ATM"
    StringKey.UseMyLocation -> "Use my location"
    StringKey.GettingLocation -> "Getting location…"
    StringKey.GpsDeniedTip -> "Location is off. Open settings"
    StringKey.NoAtmsMatch -> "No ATMs match"
    StringKey.MayBeOutdated -> "May be outdated"
    StringKey.AtmCount -> "{n} ATMs"
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
