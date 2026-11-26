package com.thinksoftlab.atmlocator.store

import com.thinksoftlab.atmlocator.api.ApiError
import com.thinksoftlab.atmlocator.api.ApiResult
import com.thinksoftlab.atmlocator.api.PublicApi
import com.thinksoftlab.atmlocator.cache.SoftCache
import com.thinksoftlab.atmlocator.filter.AtmFilters
import com.thinksoftlab.atmlocator.filter.applyFilters
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.model.AtmListItem
import com.thinksoftlab.atmlocator.model.AtmQuery
import com.thinksoftlab.atmlocator.model.City
import com.thinksoftlab.atmlocator.model.FallbackCities
import com.thinksoftlab.atmlocator.present.AtmRowModel
import com.thinksoftlab.atmlocator.present.toRowModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeoPoint(val latitude: Double, val longitude: Double)

data class LocatorState(
    val lang: AppLanguage,
    val cities: List<City> = emptyList(),
    val citiesOutdated: Boolean = false,
    val citiesFromFallback: Boolean = false,
    val citiesLoading: Boolean = false,
    val query: AtmQuery? = null,
    val draftFilters: AtmFilters = AtmFilters(),
    val appliedFilters: AtmFilters = AtmFilters(),
    /** Unfiltered last success; UI rows come from [results]. */
    val rawResults: List<AtmListItem> = emptyList(),
    val results: List<AtmRowModel> = emptyList(),
    val resultsOutdated: Boolean = false,
    val resultsLoading: Boolean = false,
    val selectedDetailId: String? = null,
    val detail: AtmRowModel? = null,
    val detailError: ApiError? = null,
    val toastError: ApiError? = null,
    val offline: Boolean = false,
    val rateLimited: ApiError.RateLimited? = null,
    /** Accepted for a future nearby call; OS location is not requested here. */
    val lastKnownLocation: GeoPoint? = null,
    val nearbyUnsupported: Boolean = false,
)

/**
 * Shared store both UIs will bind to. Language writes do not clear results,
 * filters, cities, or selection.
 */
class LocatorStore(
    private val api: PublicApi,
    private val languageRepository: LanguageRepository,
    private val scope: CoroutineScope,
    deviceLanguageTag: String = "en",
    private val clockMillis: () -> Long = { defaultClockMillis() },
    private val citiesCache: SoftCache<List<City>> = SoftCache(),
    private val resultsCache: SoftCache<List<AtmListItem>> = SoftCache(),
) {
    private val _state = MutableStateFlow(
        LocatorState(lang = languageRepository.resolveInitial(deviceLanguageTag)),
    )
    val state: StateFlow<LocatorState> = _state.asStateFlow()

    private var citiesJob: Job? = null
    private var resultsJob: Job? = null
    private var detailJob: Job? = null

    fun setLanguage(language: AppLanguage) {
        languageRepository.save(language)
        _state.update { it.copy(lang = language) }
    }

    fun acceptLocation(latitude: Double, longitude: Double) {
        _state.update {
            it.copy(lastKnownLocation = GeoPoint(latitude, longitude))
        }
    }

    fun clearToast() {
        _state.update { it.copy(toastError = null) }
    }

    fun updateDraftFilters(transform: (AtmFilters) -> AtmFilters) {
        _state.update { it.copy(draftFilters = transform(it.draftFilters)) }
    }

    fun applyDraftFilters() {
        _state.update { current ->
            val applied = current.draftFilters
            current.copy(
                appliedFilters = applied,
                results = current.rawResults.applyFilters(applied).map {
                    it.toRowModel(current.query, clockMillis())
                },
            )
        }
    }

    /** Restores the draft from applied filters without refetching. */
    fun dismissDraftFilters() {
        _state.update { it.copy(draftFilters = it.appliedFilters) }
    }

    fun clearFilters() {
        val empty = AtmFilters()
        _state.update { current ->
            current.copy(
                draftFilters = empty,
                appliedFilters = empty,
                results = current.rawResults.map {
                    it.toRowModel(current.query, clockMillis())
                },
            )
        }
    }

    /**
     * Test / Phase-1 seed: installs results without HTTP.
     * Product nearby/search still go through [requestNearby]/[requestSearch].
     */
    fun seedResults(items: List<AtmListItem>, query: AtmQuery? = null) {
        val now = clockMillis()
        resultsCache.put(items, now)
        _state.update { current ->
            val applied = current.appliedFilters
            current.copy(
                query = query ?: current.query,
                rawResults = items,
                results = items.applyFilters(applied).map { it.toRowModel(query ?: current.query, now) },
                resultsOutdated = false,
                offline = false,
                nearbyUnsupported = false,
            )
        }
    }

    fun loadCities() {
        citiesJob?.cancel()
        citiesJob = scope.launch {
            _state.update { it.copy(citiesLoading = true, toastError = null) }
            when (val result = api.cities()) {
                is ApiResult.Ok -> {
                    val now = clockMillis()
                    citiesCache.put(result.value, now)
                    _state.update {
                        it.copy(
                            cities = result.value,
                            citiesOutdated = false,
                            citiesFromFallback = false,
                            citiesLoading = false,
                            offline = false,
                        )
                    }
                }
                is ApiResult.Err -> handleCitiesError(result.error)
            }
        }
    }

    fun requestNearby(latitude: Double, longitude: Double) {
        acceptLocation(latitude, longitude)
        val query = AtmQuery.Nearby(latitude, longitude)
        runResultsQuery(query) {
            api.nearby(latitude, longitude, _state.value.appliedFilters)
        }
    }

    fun requestSearch(text: String) {
        val query = AtmQuery.Text(text)
        runResultsQuery(query) {
            api.search(text, _state.value.appliedFilters)
        }
    }

    /**
     * City row. With coordinates, runs nearby under [AtmQuery.ByCity] (no distance chips).
     * Without coordinates (fallback names), does not invent lat/lng or call nearby.
     */
    fun selectCity(city: City) {
        val lat = city.latitude
        val lng = city.longitude
        if (lat != null && lng != null) {
            acceptLocation(lat, lng)
            runResultsQuery(AtmQuery.ByCity(city)) {
                api.nearby(lat, lng, _state.value.appliedFilters)
            }
            return
        }
        resultsJob?.cancel()
        _state.update {
            it.copy(
                query = AtmQuery.ByCity(city),
                resultsLoading = false,
                nearbyUnsupported = true,
                rawResults = emptyList(),
                results = emptyList(),
                toastError = null,
            )
        }
    }

    /** Re-run the active query after filters change. No-op when there is no query. */
    fun reloadActiveQuery() {
        when (val query = _state.value.query) {
            is AtmQuery.Nearby -> requestNearby(query.latitude, query.longitude)
            is AtmQuery.Text -> requestSearch(query.text)
            is AtmQuery.ByCity -> selectCity(query.city)
            null -> Unit
        }
    }

    fun selectDetail(atmId: String) {
        detailJob?.cancel()
        _state.update {
            it.copy(
                selectedDetailId = atmId,
                detailError = null,
                detail = it.results.find { row -> row.id == atmId },
            )
        }
        detailJob = scope.launch {
            when (val result = api.detail(atmId)) {
                is ApiResult.Ok -> {
                    val row = result.value.toRowModel(_state.value.query, clockMillis())
                    _state.update {
                        it.copy(detail = row, detailError = null, nearbyUnsupported = false)
                    }
                }
                is ApiResult.Err -> {
                    when (result.error) {
                        ApiError.NotFound -> _state.update {
                            it.copy(detail = null, detailError = ApiError.NotFound)
                        }
                        ApiError.UnsupportedUntilOpenApi -> {
                            // Keep list-derived detail if present; mark unsupported for refetch.
                            _state.update { it.copy(nearbyUnsupported = true) }
                        }
                        is ApiError.BadRequest -> _state.update {
                            it.copy(toastError = result.error)
                        }
                        is ApiError.RateLimited -> _state.update {
                            it.copy(rateLimited = result.error, toastError = result.error)
                        }
                        ApiError.Offline, ApiError.NotConfigured -> _state.update {
                            it.copy(offline = true, toastError = result.error)
                        }
                        is ApiError.Unexpected -> _state.update {
                            it.copy(toastError = result.error)
                        }
                    }
                }
            }
        }
    }

    fun clearSelection() {
        detailJob?.cancel()
        _state.update {
            it.copy(selectedDetailId = null, detail = null, detailError = null)
        }
    }

    private fun runResultsQuery(
        query: AtmQuery,
        call: suspend () -> ApiResult<List<AtmListItem>>,
    ) {
        resultsJob?.cancel()
        resultsJob = scope.launch {
            _state.update {
                it.copy(
                    query = query,
                    resultsLoading = true,
                    toastError = null,
                    nearbyUnsupported = false,
                )
            }
            when (val result = call()) {
                is ApiResult.Ok -> {
                    val now = clockMillis()
                    resultsCache.put(result.value, now)
                    _state.update { current ->
                        val applied = current.appliedFilters
                        current.copy(
                            rawResults = result.value,
                            results = result.value.applyFilters(applied).map {
                                it.toRowModel(query, now)
                            },
                            resultsOutdated = false,
                            resultsLoading = false,
                            offline = false,
                            rateLimited = null,
                        )
                    }
                }
                is ApiResult.Err -> handleResultsError(result.error, query)
            }
        }
    }

    private fun handleCitiesError(error: ApiError) {
        when (error) {
            ApiError.Offline, ApiError.NotConfigured -> {
                val cached = citiesCache.markOutdated()
                if (cached != null) {
                    _state.update {
                        it.copy(
                            cities = cached.value,
                            citiesOutdated = true,
                            citiesFromFallback = false,
                            citiesLoading = false,
                            offline = true,
                            toastError = error,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            cities = FallbackCities.namesOnly,
                            citiesOutdated = false,
                            citiesFromFallback = true,
                            citiesLoading = false,
                            offline = true,
                            toastError = error,
                        )
                    }
                }
            }
            is ApiError.BadRequest -> {
                _state.update {
                    it.copy(
                        citiesLoading = false,
                        toastError = error,
                        cities = if (it.cities.isEmpty()) FallbackCities.namesOnly else it.cities,
                        citiesFromFallback = it.cities.isEmpty(),
                    )
                }
            }
            is ApiError.RateLimited -> {
                _state.update {
                    it.copy(
                        citiesLoading = false,
                        rateLimited = error,
                        toastError = error,
                        cities = if (it.cities.isEmpty()) FallbackCities.namesOnly else it.cities,
                        citiesFromFallback = it.cities.isEmpty(),
                    )
                }
            }
            ApiError.NotFound,
            ApiError.UnsupportedUntilOpenApi,
            is ApiError.Unexpected,
            -> {
                _state.update {
                    it.copy(
                        cities = FallbackCities.namesOnly,
                        citiesOutdated = false,
                        citiesFromFallback = true,
                        citiesLoading = false,
                        toastError = error,
                    )
                }
            }
        }
    }

    private fun handleResultsError(error: ApiError, query: AtmQuery) {
        when (error) {
            ApiError.UnsupportedUntilOpenApi -> {
                _state.update {
                    it.copy(
                        resultsLoading = false,
                        nearbyUnsupported = true,
                        toastError = error,
                    )
                }
            }
            is ApiError.BadRequest -> {
                _state.update {
                    it.copy(resultsLoading = false, toastError = error)
                }
            }
            ApiError.NotFound -> {
                _state.update {
                    it.copy(
                        resultsLoading = false,
                        rawResults = emptyList(),
                        results = emptyList(),
                        detailError = ApiError.NotFound,
                    )
                }
            }
            is ApiError.RateLimited -> {
                _state.update {
                    it.copy(
                        resultsLoading = false,
                        rateLimited = error,
                        toastError = error,
                    )
                }
            }
            ApiError.Offline, ApiError.NotConfigured -> {
                val cached = resultsCache.markOutdated()
                if (cached != null) {
                    val now = clockMillis()
                    _state.update { current ->
                        current.copy(
                            query = query,
                            rawResults = cached.value,
                            results = cached.value.applyFilters(current.appliedFilters).map {
                                it.toRowModel(query, now)
                            },
                            resultsOutdated = true,
                            resultsLoading = false,
                            offline = true,
                            toastError = error,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            resultsLoading = false,
                            offline = true,
                            toastError = error,
                        )
                    }
                }
            }
            is ApiError.Unexpected -> {
                _state.update {
                    it.copy(resultsLoading = false, toastError = error)
                }
            }
        }
    }
}

internal expect fun defaultClockMillis(): Long
