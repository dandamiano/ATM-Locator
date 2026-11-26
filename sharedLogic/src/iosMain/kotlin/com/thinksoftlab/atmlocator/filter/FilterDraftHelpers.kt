package com.thinksoftlab.atmlocator.filter

/**
 * Swift-friendly draft mutations. Avoids Set construction from SwiftUI.
 * Product filter rules stay in [AtmFilters]; these only reshape the draft.
 */
fun AtmFilters.replacingBanks(bank: String?): AtmFilters =
    copy(
        banks = bank?.trim()?.takeIf { it.isNotEmpty() }?.let { setOf(it) } ?: emptySet(),
    )

fun AtmFilters.replacingServices(service: String?): AtmFilters =
    copy(
        services = service?.trim()?.takeIf { it.isNotEmpty() }?.let { setOf(it) } ?: emptySet(),
    )

fun AtmFilters.replacingCongestion(value: String?): AtmFilters =
    copy(congestion = value?.trim()?.takeIf { it.isNotEmpty() })

fun AtmFilters.replacingMaxWait(minutes: Int?): AtmFilters =
    copy(maxWaitMinutes = minutes)

fun AtmFilters.togglingStatus(code: String): AtmFilters {
    val next = statuses.toMutableSet()
    if (code in next) next.remove(code) else next.add(code)
    return copy(statuses = if (next.isEmpty()) AtmFilters.DEFAULT_STATUSES else next)
}

fun AtmFilters.containsStatus(code: String): Boolean = code in statuses

fun AtmFilters.defaultStatusCodes(): List<String> = AtmFilters.DEFAULT_STATUSES.toList()

fun AtmFilters.firstBankOrEmpty(): String = banks.firstOrNull().orEmpty()

fun AtmFilters.firstServiceOrEmpty(): String = services.firstOrNull().orEmpty()

fun AtmFilters.congestionOrEmpty(): String = congestion.orEmpty()

fun AtmFilters.maxWaitTextOrEmpty(): String = maxWaitMinutes?.toString().orEmpty()

/** Empty draft / Clear action. Uses Kotlin defaults Swift cannot see. */
fun emptyAtmFilters(): AtmFilters = AtmFilters()
