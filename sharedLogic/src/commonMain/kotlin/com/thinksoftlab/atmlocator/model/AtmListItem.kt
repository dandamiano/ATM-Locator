package com.thinksoftlab.atmlocator.model

import com.thinksoftlab.atmlocator.freshness.Freshness
import com.thinksoftlab.atmlocator.freshness.WaitSnapshot

/**
 * Domain row after a successful nearby/search (or a test seed).
 * Not a JSON DTO — PublicAtmDto property names wait for OpenAPI.
 * Presentation maps through Phase 0 [com.thinksoftlab.atmlocator.freshness.presentFreshness],
 * [com.thinksoftlab.atmlocator.status.presentStatus], and
 * [com.thinksoftlab.atmlocator.time.presentRelativeTime].
 */
data class AtmListItem(
    val id: String,
    val bankName: String,
    val atmName: String,
    val statusCode: String?,
    val freshness: Freshness?,
    val wait: WaitSnapshot?,
    val cardBrands: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val distanceMeters: Double? = null,
    val lastUpdatedAtMillis: Long? = null,
    val congestion: String? = null,
    /** Prefer over lat/lng for external maps. Not a JSON field name. */
    val directionsHint: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val hours: String? = null,
    val town: String? = null,
    val district: String? = null,
    val logoUrl: String? = null,
    val queueCount: Int? = null,
)
