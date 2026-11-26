package com.thinksoftlab.atmlocator.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class HttpFailureTest {
    @Test
    fun status400MapsToBadRequest() {
        val error = mapHttpFailure(400, bodyMessage = "bad filters")
        assertIs<ApiError.BadRequest>(error)
        assertEquals("bad filters", error.message)
    }

    @Test
    fun status404MapsToNotFound() {
        assertEquals(ApiError.NotFound, mapHttpFailure(404))
    }

    @Test
    fun status429ReadsDeltaSecondsRetryAfter() {
        val error = mapHttpFailure(429, retryAfterHeader = "12")
        assertIs<ApiError.RateLimited>(error)
        assertEquals(12L, error.retryAfterSeconds)
        assertNull(error.retryAfterUnparsed)
    }

    @Test
    fun status429HttpDateStaysUnparsedNotZero() {
        val error = parseRateLimited("Wed, 21 Oct 2015 07:28:00 GMT")
        assertNull(error.retryAfterSeconds)
        assertEquals("Wed, 21 Oct 2015 07:28:00 GMT", error.retryAfterUnparsed)
    }

    @Test
    fun connectivityMapsToOffline() {
        assertEquals(ApiError.Offline, mapConnectivityFailure())
    }

    @Test
    fun otherStatusIsUnexpected() {
        val error = mapHttpFailure(500)
        assertIs<ApiError.Unexpected>(error)
        assertEquals(500, error.statusCode)
    }
}
