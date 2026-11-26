package com.thinksoftlab.atmlocator.store

import com.thinksoftlab.atmlocator.api.ApiError
import com.thinksoftlab.atmlocator.api.ApiResult
import com.thinksoftlab.atmlocator.api.PublicApi
import com.thinksoftlab.atmlocator.filter.AtmFilters
import com.thinksoftlab.atmlocator.freshness.Freshness
import com.thinksoftlab.atmlocator.freshness.WaitSnapshot
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.model.AtmListItem
import com.thinksoftlab.atmlocator.model.AtmQuery
import com.thinksoftlab.atmlocator.model.City
import com.thinksoftlab.atmlocator.model.FallbackCities
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocatorStoreTest {
    @Test
    fun citiesSuccessCachesList() = runTest {
        val api = FakePublicApi(
            citiesResult = ApiResult.Ok(
                listOf(City(id = "ll", name = "Lilongwe", latitude = -13.0, longitude = 33.0)),
            ),
        )
        val store = store(api)
        store.loadCities()
        advanceUntilIdle()
        val state = store.state.value
        assertEquals(1, state.cities.size)
        assertEquals("Lilongwe", state.cities[0].name)
        assertFalse(state.citiesFromFallback)
        assertFalse(state.citiesOutdated)
    }

    @Test
    fun citiesFailUsesFourNamedFallbacksWithoutCoordinates() = runTest {
        val api = FakePublicApi(citiesResult = ApiResult.Err(ApiError.Unexpected(500)))
        val store = store(api)
        store.loadCities()
        advanceUntilIdle()
        val state = store.state.value
        assertEquals(FallbackCities.namesOnly.map { it.name }, state.cities.map { it.name })
        assertTrue(state.citiesFromFallback)
        assertTrue(state.cities.all { !it.hasCoordinates })
    }

    @Test
    fun offlineWithCacheMarksCitiesOutdated() = runTest {
        val api = FakePublicApi(
            citiesResult = ApiResult.Ok(listOf(City(id = "ll", name = "Lilongwe"))),
        )
        val store = store(api)
        store.loadCities()
        advanceUntilIdle()
        api.citiesResult = ApiResult.Err(ApiError.Offline)
        store.loadCities()
        advanceUntilIdle()
        val state = store.state.value
        assertEquals(listOf("Lilongwe"), state.cities.map { it.name })
        assertTrue(state.citiesOutdated)
        assertTrue(state.offline)
        assertFalse(state.citiesFromFallback)
    }

    @Test
    fun languageChangeDoesNotClearResultsOrFilters() = runTest {
        val languages = MemoryLanguageRepository(AppLanguage.En)
        val store = LocatorStore(
            api = FakePublicApi(),
            languageRepository = languages,
            scope = this,
            deviceLanguageTag = "en",
            clockMillis = { 1_700_000_000_000L },
        )
        val seeded = listOf(
            AtmListItem(
                id = "a1",
                bankName = "NBS",
                atmName = "City Mall",
                statusCode = "operational",
                freshness = Freshness.Fresh,
                wait = WaitSnapshot(5),
            ),
        )
        store.seedResults(seeded, AtmQuery.Text("mall"))
        store.updateDraftFilters { it.copy(maxWaitMinutes = 10) }
        store.applyDraftFilters()
        val before = store.state.value
        store.setLanguage(AppLanguage.Ny)
        val after = store.state.value
        assertEquals(AppLanguage.Ny, after.lang)
        assertEquals(AppLanguage.Ny, languages.load())
        assertEquals(before.results.map { it.id }, after.results.map { it.id })
        assertEquals(before.rawResults, after.rawResults)
        assertEquals(before.appliedFilters, after.appliedFilters)
        assertEquals(before.query, after.query)
    }

    @Test
    fun badRequestLeavesResultsUnchanged() = runTest {
        val api = FakePublicApi(
            nearbyResult = ApiResult.Err(ApiError.BadRequest("nope")),
        )
        val store = store(api)
        val seeded = listOf(
            AtmListItem(
                id = "a1",
                bankName = "NBS",
                atmName = "Keep Me",
                statusCode = "operational",
                freshness = Freshness.Aging,
                wait = WaitSnapshot(8),
            ),
        )
        store.seedResults(seeded, AtmQuery.Nearby(-13.0, 33.0))
        val beforeIds = store.state.value.results.map { it.id }
        store.requestNearby(-13.0, 33.0)
        advanceUntilIdle()
        assertEquals(beforeIds, store.state.value.results.map { it.id })
        assertIs<ApiError.BadRequest>(store.state.value.toastError)
    }

    @Test
    fun detailNotFoundYieldsNoInventedAtm() = runTest {
        val api = FakePublicApi(detailResult = ApiResult.Err(ApiError.NotFound))
        val store = store(api)
        store.selectDetail("missing")
        advanceUntilIdle()
        assertNull(store.state.value.detail)
        assertEquals(ApiError.NotFound, store.state.value.detailError)
    }

    @Test
    fun nearbyDoesNotHitHttpWhenUnsupported() = runTest {
        val api = FakePublicApi()
        val store = store(api)
        store.requestNearby(-13.96, 33.78)
        advanceUntilIdle()
        assertEquals(1, api.nearbyCalls)
        assertTrue(store.state.value.nearbyUnsupported)
        assertEquals(ApiError.UnsupportedUntilOpenApi, store.state.value.toastError)
    }

    @Test
    fun dismissDraftRestoresAppliedWithoutRefetch() = runTest {
        val api = FakePublicApi()
        val store = store(api)
        store.updateDraftFilters { it.copy(maxWaitMinutes = 5) }
        store.applyDraftFilters()
        store.updateDraftFilters { it.copy(maxWaitMinutes = 99) }
        store.dismissDraftFilters()
        assertEquals(5, store.state.value.draftFilters.maxWaitMinutes)
        assertEquals(0, api.nearbyCalls)
        assertEquals(0, api.searchCalls)
    }

    @Test
    fun acceptLocationStoresLatLngOnly() = runTest {
        val store = store(FakePublicApi())
        store.acceptLocation(-14.0, 34.0)
        assertEquals(GeoPoint(-14.0, 34.0), store.state.value.lastKnownLocation)
    }

    private fun TestScope.store(api: PublicApi): LocatorStore =
        LocatorStore(
            api = api,
            languageRepository = MemoryLanguageRepository(),
            scope = this,
            deviceLanguageTag = "en",
            clockMillis = { 1_700_000_000_000L },
        )
}

private class FakePublicApi(
    var citiesResult: ApiResult<List<City>> = ApiResult.Ok(emptyList()),
    var nearbyResult: ApiResult<List<AtmListItem>> = ApiResult.Err(ApiError.UnsupportedUntilOpenApi),
    var searchResult: ApiResult<List<AtmListItem>> = ApiResult.Err(ApiError.UnsupportedUntilOpenApi),
    var detailResult: ApiResult<AtmListItem> = ApiResult.Err(ApiError.UnsupportedUntilOpenApi),
) : PublicApi {
    var citiesCalls = 0
    var nearbyCalls = 0
    var searchCalls = 0
    var detailCalls = 0

    override suspend fun cities(): ApiResult<List<City>> {
        citiesCalls++
        return citiesResult
    }

    override suspend fun nearby(
        latitude: Double,
        longitude: Double,
        filters: AtmFilters,
    ): ApiResult<List<AtmListItem>> {
        nearbyCalls++
        return nearbyResult
    }

    override suspend fun search(
        text: String,
        filters: AtmFilters,
    ): ApiResult<List<AtmListItem>> {
        searchCalls++
        return searchResult
    }

    override suspend fun detail(atmId: String): ApiResult<AtmListItem> {
        detailCalls++
        return detailResult
    }
}
