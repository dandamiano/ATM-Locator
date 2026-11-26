package com.thinksoftlab.atmlocator.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thinksoftlab.atmlocator.filter.AtmFilters
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import com.thinksoftlab.atmlocator.i18n.text
import com.thinksoftlab.atmlocator.status.presentStatus
import com.thinksoftlab.atmlocator.theme.MinTap
import com.thinksoftlab.atmlocator.theme.Muted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltersSheet(
    draft: AtmFilters,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onDraftChange: ((AtmFilters) -> AtmFilters) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bankText by remember(draft.banks) {
        mutableStateOf(draft.banks.firstOrNull().orEmpty())
    }
    var serviceText by remember(draft.services) {
        mutableStateOf(draft.services.firstOrNull().orEmpty())
    }
    var congestionText by remember(draft.congestion) {
        mutableStateOf(draft.congestion.orEmpty())
    }
    var maxWaitText by remember(draft.maxWaitMinutes) {
        mutableStateOf(draft.maxWaitMinutes?.toString().orEmpty())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = text(StringKey.Filters, language),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = bankText,
                onValueChange = { value ->
                    bankText = value
                    onDraftChange { filters ->
                        filters.copy(
                            banks = value.trim().takeIf { it.isNotEmpty() }?.let { setOf(it) }
                                ?: emptySet(),
                        )
                    }
                },
                label = { Text(text(StringKey.FilterBank, language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = text(StringKey.FilterStatus, language),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AtmFilters.DEFAULT_STATUSES.forEach { code ->
                    val selected = code in draft.statuses
                    val label = text(presentStatus(code).labelKey, language)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            onDraftChange { filters ->
                                val next = filters.statuses.toMutableSet()
                                if (selected) next.remove(code) else next.add(code)
                                filters.copy(
                                    statuses = if (next.isEmpty()) {
                                        AtmFilters.DEFAULT_STATUSES
                                    } else {
                                        next
                                    },
                                )
                            }
                        },
                        label = { Text(label) },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = maxWaitText,
                onValueChange = { value ->
                    maxWaitText = value.filter { it.isDigit() }
                    onDraftChange { filters ->
                        filters.copy(maxWaitMinutes = maxWaitText.toIntOrNull())
                    }
                },
                label = { Text(text(StringKey.FilterMaxWait, language)) },
                supportingText = {
                    Text(
                        text = text(StringKey.MaxWaitHelper, language),
                        color = Muted,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = serviceText,
                onValueChange = { value ->
                    serviceText = value
                    onDraftChange { filters ->
                        filters.copy(
                            services = value.trim().takeIf { it.isNotEmpty() }?.let { setOf(it) }
                                ?: emptySet(),
                        )
                    }
                },
                label = { Text(text(StringKey.FilterService, language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = congestionText,
                onValueChange = { value ->
                    congestionText = value
                    onDraftChange { filters ->
                        filters.copy(congestion = value.trim().takeIf { it.isNotEmpty() })
                    }
                },
                label = { Text(text(StringKey.FilterCongestion, language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        bankText = ""
                        serviceText = ""
                        congestionText = ""
                        maxWaitText = ""
                        onClear()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = MinTap),
                ) {
                    Text(text(StringKey.Clear, language))
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = MinTap),
                ) {
                    Text(text(StringKey.Apply, language))
                }
            }
        }
    }
}
