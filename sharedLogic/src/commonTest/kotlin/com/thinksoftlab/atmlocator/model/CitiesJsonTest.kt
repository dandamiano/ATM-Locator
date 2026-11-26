package com.thinksoftlab.atmlocator.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CitiesJsonTest {
    @Test
    fun parsesBareArray() {
        val cities = parseCitiesJson("""[{"name":"Zomba","slug":"zomba"}]""")
        assertEquals(1, cities.size)
        assertEquals("Zomba", cities[0].name)
        assertEquals("zomba", cities[0].slug)
        assertNull(cities[0].latitude)
    }

    @Test
    fun parsesWrappedCitiesObject() {
        val cities = parseCitiesJson(
            """{"cities":[{"id":1,"name":"Mzuzu","latitude":-11.9,"longitude":34.0}]}""",
        )
        assertEquals("1", cities[0].id)
        assertEquals(-11.9, cities[0].latitude)
        assertEquals(34.0, cities[0].longitude)
    }
}
