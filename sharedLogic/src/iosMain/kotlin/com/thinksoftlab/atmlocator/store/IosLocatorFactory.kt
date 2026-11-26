package com.thinksoftlab.atmlocator.store

import com.thinksoftlab.atmlocator.api.createKtorPublicApi
import kotlinx.coroutines.MainScope

/**
 * Builds the product [LocatorStore] for SwiftUI. Kotlin default parameters are not
 * visible from Swift, so construction stays on the Kotlin side.
 */
fun createIosLocatorStore(deviceLanguageTag: String): LocatorStore =
    LocatorStore(
        api = createKtorPublicApi(),
        languageRepository = UserDefaultsLanguageRepository(),
        scope = MainScope(),
        deviceLanguageTag = deviceLanguageTag,
    )
