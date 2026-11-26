package com.thinksoftlab.atmlocator.navigation

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thinksoftlab.atmlocator.LocatorViewModel
import com.thinksoftlab.atmlocator.api.ApiError
import com.thinksoftlab.atmlocator.filter.AtmFilters
import com.thinksoftlab.atmlocator.ui.detail.DetailScreen
import com.thinksoftlab.atmlocator.ui.filters.FiltersSheet
import com.thinksoftlab.atmlocator.ui.home.HomeScreen
import com.thinksoftlab.atmlocator.ui.results.ResultsScreen

private object Routes {
    const val Home = "home"
    const val Results = "results"
    const val Detail = "detail/{atmId}"

    fun detail(atmId: String) = "detail/$atmId"
}

@Composable
fun LocatorApp() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val viewModel: LocatorViewModel = viewModel(factory = LocatorViewModel.factory(app))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gpsUi by viewModel.gpsUi.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilters by remember { mutableStateOf(false) }

    fun goToResults() {
        navController.navigate(Routes.Results) {
            launchSingleTop = true
        }
    }

    LaunchedEffect(state.toastError) {
        val error = state.toastError ?: return@LaunchedEffect
        Toast.makeText(context, toastMessage(error), Toast.LENGTH_SHORT).show()
        viewModel.clearToast()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    state = state,
                    gpsUi = gpsUi,
                    onLanguageChange = viewModel::setLanguage,
                    onUseMyLocation = {
                        viewModel.useMyLocation(onLocated = { goToResults() })
                    },
                    onGpsDenied = viewModel::markGpsDenied,
                    onCityClick = { city ->
                        viewModel.selectCity(city)
                        goToResults()
                    },
                    onSearch = { text ->
                        viewModel.requestSearch(text)
                        goToResults()
                    },
                    onRetryCities = viewModel::loadCities,
                )
            }
            composable(Routes.Results) {
                ResultsScreen(
                    state = state,
                    onLanguageChange = viewModel::setLanguage,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Routes.Home) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onOpenFilters = { showFilters = true },
                    onAtmClick = { id ->
                        viewModel.selectDetail(id)
                        navController.navigate(Routes.detail(id))
                    },
                    onClearFilters = viewModel::clearFilters,
                    onChangeCity = {
                        navController.popBackStack(Routes.Home, inclusive = false)
                    },
                    onRetry = viewModel::reloadActiveQuery,
                )
            }
            composable(
                route = Routes.Detail,
                arguments = listOf(navArgument("atmId") { type = NavType.StringType }),
            ) {
                DetailScreen(
                    state = state,
                    onLanguageChange = viewModel::setLanguage,
                    onBack = {
                        viewModel.clearSelection()
                        navController.popBackStack()
                    },
                )
            }
        }
    }

    if (showFilters) {
        FiltersSheet(
            draft = state.draftFilters,
            language = state.lang,
            onDismiss = {
                showFilters = false
                viewModel.dismissDraftFilters()
            },
            onDraftChange = viewModel::updateDraftFilters,
            onClear = {
                viewModel.updateDraftFilters { AtmFilters() }
            },
            onApply = {
                viewModel.applyDraftFilters()
                showFilters = false
            },
        )
    }
}

private fun toastMessage(error: ApiError): String = when (error) {
    is ApiError.BadRequest -> error.message?.takeIf { it.isNotBlank() } ?: "Request could not be completed"
    is ApiError.RateLimited -> {
        val wait = error.retryAfterSeconds
        if (wait != null) "Too many requests. Try again in ${wait}s"
        else "Too many requests. Try again later"
    }
    ApiError.Offline -> "You are offline"
    ApiError.NotConfigured -> "API is not configured"
    ApiError.UnsupportedUntilOpenApi -> "ATM search is not available yet"
    ApiError.NotFound -> "Not found"
    is ApiError.Unexpected -> error.message?.takeIf { it.isNotBlank() } ?: "Something went wrong"
}
