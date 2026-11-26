package com.thinksoftlab.atmlocator.api

/**
 * The browser target has no build-time API origin in Phase 0.
 * Empty [ApiConfig.baseUrl] means not configured. Do not invent a host.
 */
actual fun platformApiConfig(): ApiConfig = ApiConfig(baseUrl = "", appKey = "")
