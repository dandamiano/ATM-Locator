package com.thinksoftlab.atmlocator.ui.language

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import com.thinksoftlab.atmlocator.i18n.text
import com.thinksoftlab.atmlocator.theme.MinTap

@Composable
fun LanguageControl(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LanguageOption(
            label = text(StringKey.LanguageEnglish, language),
            selected = language == AppLanguage.En,
            onClick = { onLanguageChange(AppLanguage.En) },
        )
        Text(
            text = "|",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        LanguageOption(
            label = text(StringKey.LanguageChichewa, language),
            selected = language == AppLanguage.Ny,
            onClick = { onLanguageChange(AppLanguage.Ny) },
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minHeight = MinTap),
    ) {
        Text(
            text = label,
            style = if (selected) {
                MaterialTheme.typography.labelLarge
            } else {
                MaterialTheme.typography.labelMedium
            },
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
