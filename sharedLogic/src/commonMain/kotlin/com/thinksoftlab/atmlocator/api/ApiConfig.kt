package com.thinksoftlab.atmlocator.api

/**
 * Build-supplied public API origin.
 * An empty [baseUrl] means this build is not pointed at a host. Do not substitute one.
 * An empty [appKey] means the client omits the X-App-Key header.
 */
data class ApiConfig(
    val baseUrl: String,
    val appKey: String = "",
) {
    val sendsAppKey: Boolean get() = appKey.isNotEmpty()
}

expect fun platformApiConfig(): ApiConfig
