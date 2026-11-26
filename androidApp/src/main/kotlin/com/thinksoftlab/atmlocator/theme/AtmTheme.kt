package com.thinksoftlab.atmlocator.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Forest = Color(0xFF12352C)
val Gold = Color(0xFFC4A35A)
val Paper = Color(0xFFF7F5F0)
val Ink = Color(0xFF1A1F1C)
val Muted = Color(0xFF5C6560)

val MinTap = 48.dp

private val AtmLightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Paper,
    secondary = Gold,
    onSecondary = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFECE8E0),
    onSurfaceVariant = Muted,
    outline = Muted,
    error = Color(0xFF8B2E2E),
    onError = Paper,
)

@Composable
fun AtmTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Outdoor-first product: always Paper / Forest / Ink. No dark palette in Phase 2.
    MaterialTheme(
        colorScheme = AtmLightColors,
        content = content,
    )
}
