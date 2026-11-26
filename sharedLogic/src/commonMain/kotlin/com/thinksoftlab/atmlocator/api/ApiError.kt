package com.thinksoftlab.atmlocator.api

/**
 * Public-client failures. Toast versus list/detail handling lives in [com.thinksoftlab.atmlocator.store.LocatorStore].
 */
sealed class ApiError {
    /** HTTP 400. Toast-level; previous results stay. */
    data class BadRequest(val message: String? = null) : ApiError()

    /** HTTP 404. Empty detail; do not invent an ATM. */
    data object NotFound : ApiError()

    /**
     * HTTP 429. [retryAfterSeconds] is set only for delta-seconds Retry-After.
     * An HTTP-date or other form stays in [retryAfterUnparsed] and is not treated as zero.
     */
    data class RateLimited(
        val retryAfterSeconds: Long? = null,
        val retryAfterUnparsed: String? = null,
    ) : ApiError()

    /** Connectivity or transport failure with no usable response. */
    data object Offline : ApiError()

    /** Empty [ApiConfig.baseUrl]. Do not invent a host. */
    data object NotConfigured : ApiError()

    /**
     * Nearby, search, or detail was requested before OpenAPI named the path.
     * No HTTP call is made.
     */
    data object UnsupportedUntilOpenApi : ApiError()

    data class Unexpected(val statusCode: Int? = null, val message: String? = null) : ApiError()
}

sealed class ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>()
    data class Err(val error: ApiError) : ApiResult<Nothing>()
}
