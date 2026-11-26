package com.thinksoftlab.atmlocator.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import com.thinksoftlab.atmlocator.i18n.text
import com.thinksoftlab.atmlocator.model.AtmQuery
import com.thinksoftlab.atmlocator.store.LocatorState
import com.thinksoftlab.atmlocator.theme.MinTap
import com.thinksoftlab.atmlocator.theme.Muted
import com.thinksoftlab.atmlocator.ui.components.AtmListRow
import com.thinksoftlab.atmlocator.ui.language.LanguageControl

@Composable
fun ResultsScreen(
    state: LocatorState,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    onOpenFilters: () -> Unit,
    onAtmClick: (String) -> Unit,
    onClearFilters: () -> Unit,
    onChangeCity: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = state.lang
    val place = placeLabel(state.query)

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.defaultMinSize(minWidth = MinTap, minHeight = MinTap),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = text(StringKey.ChangeCity, language),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = text(StringKey.AtmCount, language, n = state.results.size.toLong()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            TextButton(
                onClick = onOpenFilters,
                modifier = Modifier.defaultMinSize(minHeight = MinTap),
            ) {
                Text(text(StringKey.Filters, language))
            }
            LanguageControl(
                language = language,
                onLanguageChange = onLanguageChange,
            )
        }

        if (state.resultsOutdated) {
            Text(
                text = text(StringKey.MayBeOutdated, language),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        when {
            state.resultsLoading -> {
                ResultsSkeleton()
            }
            state.nearbyUnsupported -> {
                EmptyMessage(
                    message = text(StringKey.NearbyUnavailable, language),
                    language = language,
                    showEmptyActions = false,
                    showRetry = false,
                    onClearFilters = onClearFilters,
                    onChangeCity = onChangeCity,
                    onRetry = onRetry,
                )
            }
            state.offline && state.results.isEmpty() && !state.resultsOutdated -> {
                EmptyMessage(
                    message = text(StringKey.Offline, language),
                    language = language,
                    showEmptyActions = false,
                    showRetry = true,
                    onClearFilters = onClearFilters,
                    onChangeCity = onChangeCity,
                    onRetry = onRetry,
                )
            }
            state.results.isEmpty() -> {
                EmptyMessage(
                    message = text(StringKey.NoAtmsMatch, language),
                    language = language,
                    showEmptyActions = true,
                    showRetry = false,
                    onClearFilters = onClearFilters,
                    onChangeCity = onChangeCity,
                    onRetry = onRetry,
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.results, key = { it.id }) { row ->
                        AtmListRow(
                            model = row,
                            language = language,
                            onClick = { onAtmClick(row.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(8.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Composable
private fun EmptyMessage(
    message: String,
    language: AppLanguage,
    showEmptyActions: Boolean,
    showRetry: Boolean,
    onClearFilters: () -> Unit,
    onChangeCity: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        if (showEmptyActions) {
            Button(
                onClick = onClearFilters,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = MinTap),
            ) {
                Text(text(StringKey.ClearFilters, language))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onChangeCity,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = MinTap),
            ) {
                Text(text(StringKey.ChangeCity, language))
            }
        }
        if (showRetry) {
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = MinTap),
            ) {
                Text(text(StringKey.Retry, language))
            }
        }
    }
}

private fun placeLabel(query: AtmQuery?): String = when (query) {
    is AtmQuery.ByCity -> query.city.name
    is AtmQuery.Nearby -> "Nearby"
    is AtmQuery.Text -> query.text
    null -> ""
}
