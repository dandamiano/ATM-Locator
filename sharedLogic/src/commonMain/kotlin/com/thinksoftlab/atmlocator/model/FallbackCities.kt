package com.thinksoftlab.atmlocator.model

/**
 * Named fallbacks when the cities call fails.
 * Coordinates are intentionally absent. Guidelines require fixed lat/lng but do not
 * publish the numbers in-repo; do not invent them or issue a locate from these rows
 * until OpenAPI or the cities sample supplies values into a dedicated confirmed table.
 */
object FallbackCities {
    val namesOnly: List<City> = listOf(
        City(id = "fallback-lilongwe", name = "Lilongwe", slug = "lilongwe"),
        City(id = "fallback-blantyre", name = "Blantyre", slug = "blantyre"),
        City(id = "fallback-mzuzu", name = "Mzuzu", slug = "mzuzu"),
        City(id = "fallback-zomba", name = "Zomba", slug = "zomba"),
    )
}
