package com.thinksoftlab.atmlocator.freshness

import com.thinksoftlab.atmlocator.i18n.StringKey

enum class Freshness {
    Fresh,
    Aging,
    Stale,
}

enum class FreshnessStyle {
    SolidGold,
    OutlineGold,
    Muted,
}

/**
 * Server wait object. A null [waitMinutes] is missing data, not zero.
 * A null [WaitSnapshot] means the server sent no wait object.
 */
data class WaitSnapshot(
    val waitMinutes: Int?,
)

data class FreshnessPresentation(
    val style: FreshnessStyle,
    val labelKey: StringKey,
    val showMinutes: Boolean,
    val minutesText: String?,
    val showLivePulse: Boolean,
)

fun freshnessFromCode(code: String?): Freshness? = when (code?.trim()?.lowercase()) {
    "fresh" -> Freshness.Fresh
    "aging" -> Freshness.Aging
    "stale" -> Freshness.Stale
    else -> null
}

/**
 * Sole place that may show or hide wait minutes.
 * Minutes render as "~N min" only for [Freshness.Fresh] and [Freshness.Aging]
 * when [WaitSnapshot.waitMinutes] is present and non-negative.
 * Stale or null freshness hides minutes, uses [FreshnessStyle.Muted], and
 * never sets [FreshnessPresentation.showLivePulse].
 * Only [Freshness.Fresh] may set the pulse flag. This does not draw UI.
 */
fun presentFreshness(freshness: Freshness?, wait: WaitSnapshot?): FreshnessPresentation {
    val minutes = wait?.waitMinutes
    val canShowMinutes = freshness == Freshness.Fresh || freshness == Freshness.Aging
    val showMinutes = minutes != null && minutes >= 0 && canShowMinutes
    return FreshnessPresentation(
        style = when (freshness) {
            Freshness.Fresh -> FreshnessStyle.SolidGold
            Freshness.Aging -> FreshnessStyle.OutlineGold
            Freshness.Stale, null -> FreshnessStyle.Muted
        },
        labelKey = when (freshness) {
            Freshness.Fresh -> StringKey.Fresh
            Freshness.Aging -> StringKey.Aging
            Freshness.Stale, null -> StringKey.StaleNotLive
        },
        showMinutes = showMinutes,
        minutesText = if (showMinutes) "~$minutes min" else null,
        showLivePulse = freshness == Freshness.Fresh,
    )
}
