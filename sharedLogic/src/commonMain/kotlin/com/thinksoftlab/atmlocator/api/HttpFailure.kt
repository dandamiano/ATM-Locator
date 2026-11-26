package com.thinksoftlab.atmlocator.api

/**
 * Pure map from HTTP status, Retry-After header, and connectivity failure.
 * Does not perform I/O.
 */
fun mapHttpFailure(
    statusCode: Int,
    retryAfterHeader: String? = null,
    bodyMessage: String? = null,
): ApiError = when (statusCode) {
    400 -> ApiError.BadRequest(message = bodyMessage)
    404 -> ApiError.NotFound
    429 -> parseRateLimited(retryAfterHeader)
    else -> ApiError.Unexpected(statusCode = statusCode, message = bodyMessage)
}

fun mapConnectivityFailure(): ApiError = ApiError.Offline

/**
 * Parses Retry-After as delta-seconds when the whole header is a non-negative long.
 * Any other form (including HTTP-date) is kept unparsed and not coerced to zero.
 */
fun parseRateLimited(retryAfterHeader: String?): ApiError.RateLimited {
    val raw = retryAfterHeader?.trim().orEmpty()
    if (raw.isEmpty()) {
        return ApiError.RateLimited()
    }
    val seconds = raw.toLongOrNull()
    return if (seconds != null && seconds >= 0L) {
        ApiError.RateLimited(retryAfterSeconds = seconds)
    } else {
        ApiError.RateLimited(retryAfterUnparsed = raw)
    }
}
