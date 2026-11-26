package com.thinksoftlab.atmlocator.present

import com.thinksoftlab.atmlocator.freshness.FreshnessPresentation
import com.thinksoftlab.atmlocator.freshness.presentFreshness
import com.thinksoftlab.atmlocator.model.AtmListItem
import com.thinksoftlab.atmlocator.model.AtmQuery
import com.thinksoftlab.atmlocator.status.StatusPresentation
import com.thinksoftlab.atmlocator.status.presentStatus
import com.thinksoftlab.atmlocator.time.RelativeTimePresentation
import com.thinksoftlab.atmlocator.time.presentRelativeTime

/**
 * List/detail display model. UI renders this; it does not recompute wait minutes.
 * [cardBrands] is the full list; the list row shows at most [MAX_CARD_BRANDS_ON_ROW].
 */
data class AtmRowModel(
    val id: String,
    val bankName: String,
    val atmName: String,
    val status: StatusPresentation,
    val freshness: FreshnessPresentation,
    val cardBrands: List<String>,
    val services: List<String>,
    val distanceMeters: Double?,
    val updated: RelativeTimePresentation,
    val directionsHint: String?,
    val latitude: Double?,
    val longitude: Double?,
    val hours: String?,
    val town: String?,
    val district: String?,
    val logoUrl: String?,
    val queueCount: Int?,
    val lastUpdatedAtMillis: Long?,
) {
    val rowCardBrands: List<String> get() = cardBrands.take(MAX_CARD_BRANDS_ON_ROW)

    val hasDirections: Boolean
        get() = !directionsHint.isNullOrBlank() || (latitude != null && longitude != null)
}

const val MAX_CARD_BRANDS_ON_ROW = 3

fun AtmListItem.toRowModel(
    query: AtmQuery?,
    nowMillis: Long,
): AtmRowModel {
    val distance = when (query) {
        is AtmQuery.Nearby -> distanceMeters
        else -> null
    }
    return AtmRowModel(
        id = id,
        bankName = bankName,
        atmName = atmName,
        status = presentStatus(statusCode),
        freshness = presentFreshness(freshness, wait),
        cardBrands = cardBrands,
        services = services,
        distanceMeters = distance,
        updated = presentRelativeTime(lastUpdatedAtMillis, nowMillis),
        directionsHint = directionsHint,
        latitude = latitude,
        longitude = longitude,
        hours = hours,
        town = town,
        district = district,
        logoUrl = logoUrl,
        queueCount = queueCount,
        lastUpdatedAtMillis = lastUpdatedAtMillis,
    )
}
