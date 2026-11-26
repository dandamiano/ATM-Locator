package com.thinksoftlab.atmlocator.model

/**
 * How the results list was requested.
 * Distance on a row is legal only for [Nearby].
 */
sealed class AtmQuery {
    data class Nearby(val latitude: Double, val longitude: Double) : AtmQuery()
    data class ByCity(val city: City) : AtmQuery()
    data class Text(val text: String) : AtmQuery()
}
