package com.thinksoftlab.atmlocator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.text
import com.thinksoftlab.atmlocator.status.StatusIconKey
import com.thinksoftlab.atmlocator.status.StatusPresentation

@Composable
fun StatusLine(
    presentation: StatusPresentation,
    language: AppLanguage,
    emphasize: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val heavy = emphasize ||
        presentation.iconKey == StatusIconKey.OutOfCash ||
        presentation.iconKey == StatusIconKey.OutOfService
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = iconFor(presentation.iconKey),
            contentDescription = text(presentation.labelKey, language),
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = text(presentation.labelKey, language),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (heavy) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private fun iconFor(key: StatusIconKey): ImageVector = when (key) {
    StatusIconKey.Operational -> Icons.Filled.CheckCircle
    StatusIconKey.OutOfCash,
    StatusIconKey.OutOfService,
    StatusIconKey.Maintenance,
    StatusIconKey.TemporarilyUnavailable,
    -> Icons.Filled.Warning
    StatusIconKey.Unknown -> Icons.Filled.Info
}
