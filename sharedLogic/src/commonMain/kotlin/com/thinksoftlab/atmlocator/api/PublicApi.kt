package com.thinksoftlab.atmlocator.api

import com.thinksoftlab.atmlocator.filter.AtmFilters
import com.thinksoftlab.atmlocator.model.AtmListItem
import com.thinksoftlab.atmlocator.model.City

/**
 * Anonymous public operations only. No auth, bank, queue, or integrations calls.
 *
 * Cities uses the confirmed [PublicPaths.CITIES] path.
 * Nearby, search, and detail stay gated until OpenAPI names their paths;
 * implementations must not guess `/nearby`, `/search`, or `/detail` strings.
 */
interface PublicApi {
    suspend fun cities(): ApiResult<List<City>>

    suspend fun nearby(
        latitude: Double,
        longitude: Double,
        filters: AtmFilters = AtmFilters(),
    ): ApiResult<List<AtmListItem>>

    suspend fun search(
        text: String,
        filters: AtmFilters = AtmFilters(),
    ): ApiResult<List<AtmListItem>>

    suspend fun detail(atmId: String): ApiResult<AtmListItem>
}
