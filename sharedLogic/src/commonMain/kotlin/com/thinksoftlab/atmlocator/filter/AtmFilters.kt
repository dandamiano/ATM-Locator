package com.thinksoftlab.atmlocator.filter

import com.thinksoftlab.atmlocator.freshness.Freshness
import com.thinksoftlab.atmlocator.model.AtmListItem

/**
 * Product filters. Wire query names wait for OpenAPI; local filtering uses these fields.
 * Default applied status is every known code, including stale and unknown.
 */
data class AtmFilters(
    val banks: Set<String> = emptySet(),
    val statuses: Set<String> = DEFAULT_STATUSES,
    val maxWaitMinutes: Int? = null,
    val services: Set<String> = emptySet(),
    val congestion: String? = null,
) {
    companion object {
        val DEFAULT_STATUSES: Set<String> = setOf(
            "operational",
            "out_of_cash",
            "out_of_service",
            "maintenance",
            "temporarily_unavailable",
            "unknown",
        )
    }
}

/**
 * Max wait keeps stale rows and rows with no waitMinutes.
 * It only drops fresh or aging rows whose visible minutes exceed the cap.
 */
fun AtmFilters.allows(item: AtmListItem): Boolean {
    if (banks.isNotEmpty()) {
        val bank = item.bankName.trim()
        if (banks.none { it.equals(bank, ignoreCase = true) }) return false
    }
    if (statuses.isNotEmpty()) {
        val raw = item.statusCode?.trim()?.lowercase().orEmpty()
        val normalized = if (raw.isEmpty() || raw !in AtmFilters.DEFAULT_STATUSES) {
            "unknown"
        } else {
            raw
        }
        if (normalized !in statuses) return false
    }
    val maxWait = maxWaitMinutes
    if (maxWait != null) {
        val minutes = item.wait?.waitMinutes
        val freshness = item.freshness
        val isLiveBand = freshness == Freshness.Fresh || freshness == Freshness.Aging
        if (isLiveBand && minutes != null && minutes > maxWait) {
            return false
        }
    }
    if (services.isNotEmpty()) {
        val lower = item.services.map { it.lowercase() }.toSet()
        if (services.none { it.lowercase() in lower }) return false
    }
    val congestionFilter = congestion?.trim()?.takeIf { it.isNotEmpty() }
    if (congestionFilter != null) {
        val value = item.congestion?.trim().orEmpty()
        if (!value.equals(congestionFilter, ignoreCase = true)) return false
    }
    return true
}

fun List<AtmListItem>.applyFilters(filters: AtmFilters): List<AtmListItem> =
    filter { filters.allows(it) }
