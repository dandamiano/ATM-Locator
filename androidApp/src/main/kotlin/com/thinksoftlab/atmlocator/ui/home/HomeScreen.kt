package com.thinksoftlab.atmlocator.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.thinksoftlab.atmlocator.GpsUiState
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import com.thinksoftlab.atmlocator.i18n.text
import com.thinksoftlab.atmlocator.model.City
import com.thinksoftlab.atmlocator.store.LocatorState
import com.thinksoftlab.atmlocator.theme.MinTap
import com.thinksoftlab.atmlocator.theme.Muted
import com.thinksoftlab.atmlocator.ui.language.LanguageControl

@Composable
fun HomeScreen(
    state: LocatorState,
    gpsUi: GpsUiState,
    onLanguageChange: (AppLanguage) -> Unit,
    onUseMyLocation: () -> Unit,
    onGpsDenied: () -> Unit,
    onCityClick: (City) -> Unit,
    onSearch: (String) -> Unit,
    onRetryCities: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var searchText by remember { mutableStateOf("") }
    val language = state.lang
    val locationLoading = gpsUi == GpsUiState.Loading

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onUseMyLocation()
        } else {
            onGpsDenied()
        }
    }

    fun requestLocation() {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            onUseMyLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text(StringKey.BrandName, language),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            LanguageControl(
                language = language,
                onLanguageChange = onLanguageChange,
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = text(StringKey.FindUsableAtm, language),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = text(StringKey.HomeSupporting, language),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text(StringKey.SearchHint, language), color = Muted)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    val query = searchText.trim()
                    if (query.isNotEmpty()) {
                        focusManager.clearFocus()
                        onSearch(query)
                    }
                },
            ),
        )

        if (state.offline) {
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = text(StringKey.Offline, language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = onRetryCities,
                    modifier = Modifier.defaultMinSize(minHeight = MinTap),
                ) {
                    Text(text(StringKey.Retry, language))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        if (state.citiesLoading && state.cities.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterHorizontally),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                items(state.cities, key = { it.id }) { city ->
                    CityRow(name = city.name, onClick = { onCityClick(city) })
                }
            }
        }

        if (gpsUi == GpsUiState.Denied || gpsUi == GpsUiState.TimedOut) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = text(StringKey.GpsDeniedTip, language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.defaultMinSize(minHeight = MinTap),
                ) {
                    Text(text(StringKey.OpenSettings, language))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { requestLocation() },
            enabled = !locationLoading,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MinTap)
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = text(
                    if (locationLoading) StringKey.GettingLocation else StringKey.UseMyLocation,
                    language,
                ),
            )
        }
    }
}

@Composable
private fun CityRow(
    name: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinTap)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
