package com.thinksoftlab.atmlocator.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.thinksoftlab.atmlocator.api.ApiError
import com.thinksoftlab.atmlocator.freshness.FreshnessStyle
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import com.thinksoftlab.atmlocator.i18n.text
import com.thinksoftlab.atmlocator.present.AtmRowModel
import com.thinksoftlab.atmlocator.store.LocatorState
import com.thinksoftlab.atmlocator.theme.MinTap
import com.thinksoftlab.atmlocator.theme.Muted
import com.thinksoftlab.atmlocator.time.formatted
import com.thinksoftlab.atmlocator.ui.components.FreshnessChip
import com.thinksoftlab.atmlocator.ui.components.StatusLine
import com.thinksoftlab.atmlocator.ui.language.LanguageControl

@Composable
fun DetailScreen(
    state: LocatorState,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = state.lang
    val detail = state.detail
    val context = LocalContext.current

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
                    contentDescription = null,
                )
            }
            Spacer(Modifier.weight(1f))
            LanguageControl(
                language = language,
                onLanguageChange = onLanguageChange,
            )
        }

        when {
            state.detailError == ApiError.NotFound ||
                (detail == null && state.nearbyUnsupported) ||
                detail == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = text(StringKey.AtmNotListed, language),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        modifier = Modifier.defaultMinSize(minHeight = MinTap),
                    ) {
                        Text(text(StringKey.ChangeCity, language))
                    }
                }
            }
            else -> {
                DetailBody(
                    model = detail,
                    language = language,
                    onOpenDirections = {
                        openDirections(detail)?.let { intent ->
                            context.startActivity(intent)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailBody(
    model: AtmRowModel,
    language: AppLanguage,
    onOpenDirections: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = model.atmName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = model.bankName,
            style = MaterialTheme.typography.titleMedium,
            color = Muted,
        )
        Spacer(Modifier.height(12.dp))
        StatusLine(
            presentation = model.status,
            language = language,
            emphasize = true,
        )
        Spacer(Modifier.height(8.dp))
        FreshnessChip(presentation = model.freshness, language = language)

        val showQueue = model.queueCount != null &&
            model.lastUpdatedAtMillis != null &&
            model.freshness.style != FreshnessStyle.Muted
        val updated = model.updated.formatted(language)
        if (showQueue || updated != null) {
            Spacer(Modifier.height(8.dp))
            val parts = buildList {
                if (showQueue) add("Queue ${model.queueCount}")
                if (updated != null) add(updated)
            }
            Text(
                text = parts.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }

        if (model.services.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = model.services.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (model.cardBrands.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = model.cardBrands.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
        model.hours?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(12.dp))
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
        val place = listOfNotNull(
            model.town?.takeIf { it.isNotBlank() },
            model.district?.takeIf { it.isNotBlank() },
        ).joinToString(", ")
        if (place.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(text = place, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }

        Spacer(Modifier.height(24.dp))
        if (model.hasDirections) {
            Button(
                onClick = onOpenDirections,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = MinTap)
                    .padding(bottom = 16.dp),
            ) {
                Text(text(StringKey.OpenDirections, language))
            }
        }
    }
}

private fun openDirections(model: AtmRowModel): Intent? {
    val hint = model.directionsHint?.trim().orEmpty()
    val uri = when {
        hint.isNotEmpty() -> Uri.parse(hint)
        model.latitude != null && model.longitude != null -> {
            Uri.parse("geo:${model.latitude},${model.longitude}?q=${model.latitude},${model.longitude}")
        }
        else -> return null
    }
    return Intent(Intent.ACTION_VIEW, uri)
}
