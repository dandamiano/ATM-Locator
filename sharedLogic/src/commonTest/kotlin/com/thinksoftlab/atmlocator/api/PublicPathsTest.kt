package com.thinksoftlab.atmlocator.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class PublicPathsTest {
    @Test
    fun citiesPathIsJoinedOnceUnderThePublicPrefix() {
        assertEquals("/api/v1/public/meta/cities", PublicPaths.CITIES)
        assertEquals("meta/cities", PublicPaths.CITIES.removePrefix(PublicPaths.PREFIX))
        assertEquals(PublicPaths.CITIES, PublicPaths.requirePublicPath(PublicPaths.CITIES))
    }

    @Test
    fun nonPublicRootsAreRejected() {
        val rejected = listOf(
            "/auth/",
            "/auth/session",
            "/bank/",
            "/bank/atms",
            "/queue/",
            "/queue/join",
            "/integrations/",
            "/integrations/webhook",
            "/api/v1/auth/token",
            "/api/v1/bank/atms",
            "/api/v1/queue/join",
            "/api/v1/integrations/webhook",
            "/api/v1/public/../auth/token",
        )
        for (path in rejected) {
            assertFalse(PublicPaths.isPublicPath(path), path)
            assertFailsWith<IllegalArgumentException> {
                PublicPaths.requirePublicPath(path)
            }
        }
    }
}
