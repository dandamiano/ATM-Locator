package com.thinksoftlab.atmlocator.api

import platform.Foundation.NSBundle

/**
 * Reads ATM_API_BASE_URL and ATM_APP_KEY from the main bundle Info.plist.
 * Xcode substitutes those keys from iosApp/Configuration/Config.xcconfig.
 * The Kotlin framework is not given a compile-time URL, so a missing key,
 * a unit-test host, and an empty xcconfig value all yield an empty string.
 * Empty is valid. Do not invent a host.
 */
actual fun platformApiConfig(): ApiConfig {
    val bundle = NSBundle.mainBundle
    return ApiConfig(
        baseUrl = bundle.infoString("ATM_API_BASE_URL"),
        appKey = bundle.infoString("ATM_APP_KEY"),
    )
}

private fun NSBundle.infoString(key: String): String {
    val value = objectForInfoDictionaryKey(key) as? String ?: return ""
    return value.trim()
}
