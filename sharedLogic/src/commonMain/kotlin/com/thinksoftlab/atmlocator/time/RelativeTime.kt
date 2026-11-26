package com.thinksoftlab.atmlocator.time

import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import com.thinksoftlab.atmlocator.i18n.text

private const val MINUTE_MILLIS = 60_000L
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS

data class RelativeTimePresentation(
    val showRelative: Boolean,
    val amount: Long? = null,
    val templateKey: StringKey? = null,
)

/**
 * Relative "Updated … ago" line. [lastUpdatedAtMillis] null returns
 * [RelativeTimePresentation.showRelative] false. Callers that need copy for
 * that case use [StringKey.UpdatedUnknown]. The clock is [nowMillis], not the device clock.
 */
fun presentRelativeTime(lastUpdatedAtMillis: Long?, nowMillis: Long): RelativeTimePresentation {
    if (lastUpdatedAtMillis == null) {
        return RelativeTimePresentation(showRelative = false)
    }
    val elapsed = nowMillis - lastUpdatedAtMillis
    if (elapsed < 0L) {
        return RelativeTimePresentation(showRelative = false)
    }
    val minutes = elapsed / MINUTE_MILLIS
    if (minutes < 60L) {
        return RelativeTimePresentation(
            showRelative = true,
            amount = minutes,
            templateKey = StringKey.UpdatedMinutesAgo,
        )
    }
    val hours = elapsed / HOUR_MILLIS
    if (hours < 24L) {
        return RelativeTimePresentation(
            showRelative = true,
            amount = hours,
            templateKey = StringKey.UpdatedHoursAgo,
        )
    }
    return RelativeTimePresentation(
        showRelative = true,
        amount = elapsed / DAY_MILLIS,
        templateKey = StringKey.UpdatedDaysAgo,
    )
}

fun RelativeTimePresentation.formatted(language: AppLanguage): String? {
    val amount = amount ?: return null
    val templateKey = templateKey ?: return null
    if (!showRelative) return null
    return text(templateKey, language, amount)
}
