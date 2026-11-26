package com.thinksoftlab.atmlocator.model

/**
 * Conservative city for the home list.
 * [latitude]/[longitude] are optional until the cities payload (or OpenAPI) supplies them.
 * Fallback display cities use name only and must not trigger a locate with guessed coordinates.
 */
data class City(
    val id: String,
    val name: String,
    val slug: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    val hasCoordinates: Boolean get() = latitude != null && longitude != null
}
