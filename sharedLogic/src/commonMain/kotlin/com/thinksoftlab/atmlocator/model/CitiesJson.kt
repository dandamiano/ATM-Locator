package com.thinksoftlab.atmlocator.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Flexible cities response parse for Phase 1.
 *
 * ASSUMED until OpenAPI confirms the cities DTO:
 * - Root may be a JSON array, or an object with `cities` / `data` / `items` array.
 * - Each element may carry `id` and/or `slug` and/or `name` (string or number id).
 * - Optional coordinates under `lat`/`latitude` and `lng`/`lon`/`longitude`.
 *
 * This is isolated here so ATM DTOs stay unguessed. Replace with a kotlinx.serialization
 * data class once OpenAPI v1 is in the repo.
 */
private val citiesJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun parseCitiesJson(raw: String): List<City> {
    val root = citiesJson.parseToJsonElement(raw)
    val array = citiesArray(root) ?: return emptyList()
    return array.mapNotNull { element -> cityFromElement(element) }
}

private fun citiesArray(root: JsonElement): JsonArray? = when (root) {
    is JsonArray -> root
    is JsonObject -> {
        val nested = root["cities"] ?: root["data"] ?: root["items"]
        nested?.let { runCatching { it.jsonArray }.getOrNull() }
    }
    else -> null
}

private fun cityFromElement(element: JsonElement): City? {
    val obj = element as? JsonObject ?: return null
    val name = stringField(obj, "name", "cityName", "label") ?: return null
    val id = stringField(obj, "id", "cityId", "code")
        ?: stringField(obj, "slug")
        ?: name.lowercase().replace(' ', '-')
    val slug = stringField(obj, "slug")
    val lat = doubleField(obj, "lat", "latitude")
    val lng = doubleField(obj, "lng", "lon", "longitude")
    return City(
        id = id,
        name = name,
        slug = slug,
        latitude = lat,
        longitude = lng,
    )
}

private fun stringField(obj: JsonObject, vararg keys: String): String? {
    for (key in keys) {
        val value = obj[key] ?: continue
        val primitive = value as? JsonPrimitive ?: continue
        val text = primitive.contentOrNull?.trim().orEmpty()
        if (text.isNotEmpty()) return text
    }
    return null
}

private fun doubleField(obj: JsonObject, vararg keys: String): Double? {
    for (key in keys) {
        val value = obj[key] ?: continue
        val primitive = value as? JsonPrimitive ?: continue
        primitive.doubleOrNull?.let { return it }
        primitive.contentOrNull?.trim()?.toDoubleOrNull()?.let { return it }
    }
    return null
}
