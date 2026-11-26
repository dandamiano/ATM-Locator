package com.thinksoftlab.atmlocator.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thinksoftlab.atmlocator.freshness.FreshnessPresentation
import com.thinksoftlab.atmlocator.freshness.FreshnessStyle
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.text
import com.thinksoftlab.atmlocator.theme.Gold
import com.thinksoftlab.atmlocator.theme.Ink
import com.thinksoftlab.atmlocator.theme.Muted

/**
 * Renders the shared freshness display model. Does not recompute minutes.
 * Stale / null never shows a live pulse or ~N min (those flags stay false in the model).
 */
@Composable
fun FreshnessChip(
    presentation: FreshnessPresentation,
    language: AppLanguage,
    modifier: Modifier = Modifier,
) {
    val label = text(presentation.labelKey, language)
    val minutes = presentation.minutesText.takeIf { presentation.showMinutes }
    val body = if (minutes != null) "$label · $minutes" else label

    when (presentation.style) {
        FreshnessStyle.SolidGold -> {
            Surface(
                modifier = modifier,
                color = Gold,
                contentColor = Ink,
                shape = RoundedCornerShape(6.dp),
            ) {
                ChipText(body)
            }
        }
        FreshnessStyle.OutlineGold -> {
            Surface(
                modifier = modifier.border(1.5.dp, Gold, RoundedCornerShape(6.dp)),
                color = Color.Transparent,
                contentColor = Ink,
                shape = RoundedCornerShape(6.dp),
            ) {
                ChipText(body)
            }
        }
        FreshnessStyle.Muted -> {
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = modifier.padding(vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun ChipText(body: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = body,
            style = MaterialTheme.typography.labelMedium,
            color = Ink,
        )
    }
}
