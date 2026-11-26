package com.thinksoftlab.atmlocator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thinksoftlab.atmlocator.api.createKtorPublicApi
import com.thinksoftlab.atmlocator.filter.AtmFilters
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.location.AndroidLocationProvider
import com.thinksoftlab.atmlocator.location.LocationOutcome
import com.thinksoftlab.atmlocator.model.City
import com.thinksoftlab.atmlocator.store.LocatorState
import com.thinksoftlab.atmlocator.store.LocatorStore
import com.thinksoftlab.atmlocator.store.SharedPreferencesLanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class GpsUiState {
    Idle,
    Loading,
    Denied,
    TimedOut,
}

class LocatorViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val store = LocatorStore(
        api = createKtorPublicApi(),
        languageRepository = SharedPreferencesLanguageRepository(application),
        scope = viewModelScope,
        deviceLanguageTag = Locale.getDefault().toLanguageTag(),
    )
    private val locationProvider = AndroidLocationProvider(application)

    val state: StateFlow<LocatorState> = store.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), store.state.value)

    private val _gpsUi = MutableStateFlow(GpsUiState.Idle)
    val gpsUi: StateFlow<GpsUiState> = _gpsUi.asStateFlow()

    init {
        store.loadCities()
    }

    fun setLanguage(language: AppLanguage) {
        store.setLanguage(language)
    }

    fun loadCities() {
        store.loadCities()
    }

    fun selectCity(city: City) {
        store.selectCity(city)
    }

    fun requestSearch(text: String) {
        store.requestSearch(text.trim())
    }

    fun requestNearby(latitude: Double, longitude: Double) {
        store.requestNearby(latitude, longitude)
    }

    fun selectDetail(atmId: String) {
        store.selectDetail(atmId)
    }

    fun clearSelection() {
        store.clearSelection()
    }

    fun clearToast() {
        store.clearToast()
    }

    fun updateDraftFilters(transform: (AtmFilters) -> AtmFilters) {
        store.updateDraftFilters(transform)
    }

    fun dismissDraftFilters() {
        store.dismissDraftFilters()
    }

    fun applyDraftFilters() {
        store.applyDraftFilters()
        store.reloadActiveQuery()
    }

    fun clearFilters() {
        store.clearFilters()
        store.reloadActiveQuery()
    }

    fun markGpsDenied() {
        _gpsUi.value = GpsUiState.Denied
    }

    fun reloadActiveQuery() {
        store.reloadActiveQuery()
    }

    fun useMyLocation(onLocated: () -> Unit = {}) {
        viewModelScope.launch {
            _gpsUi.value = GpsUiState.Loading
            when (val outcome = locationProvider.currentLocation()) {
                is LocationOutcome.Fixed -> {
                    _gpsUi.value = GpsUiState.Idle
                    store.requestNearby(outcome.latitude, outcome.longitude)
                    onLocated()
                }
                LocationOutcome.Denied -> _gpsUi.value = GpsUiState.Denied
                LocationOutcome.TimedOut -> _gpsUi.value = GpsUiState.TimedOut
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LocatorViewModel(application) as T
                }
            }
    }
}
