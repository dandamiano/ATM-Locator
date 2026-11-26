package com.thinksoftlab.atmlocator.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.present.AtmRowModel
import com.thinksoftlab.atmlocator.theme.MinTap
import com.thinksoftlab.atmlocator.theme.Muted
import com.thinksoftlab.atmlocator.time.formatted
import kotlin.math.roundToInt

@Composable
fun AtmListRow(
    model: AtmRowModel,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinTap)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = model.bankName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f, fill = false),
            )
            model.distanceMeters?.let { meters ->
                Text(
                    text = formatDistance(meters),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = model.atmName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        StatusLine(presentation = model.status, language = language)
        Spacer(Modifier.height(6.dp))
        FreshnessChip(presentation = model.freshness, language = language)
        if (model.rowCardBrands.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = model.rowCardBrands.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
        val updated = model.updated.formatted(language)
        if (updated != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = updated,
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        val km = (meters / 100.0).roundToInt() / 10.0
        "$km km"
    } else {
        "${meters.roundToInt()} m"
    }
}
