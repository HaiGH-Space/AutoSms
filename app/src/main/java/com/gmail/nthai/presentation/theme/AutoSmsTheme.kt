package com.gmail.nthai.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AutoSmsColors = lightColorScheme(
    primary = Color(0xFF171717),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5F5F5),
    onPrimaryContainer = Color(0xFF171717),
    inversePrimary = Color(0xFFEBEBEB),
    secondary = Color(0xFF171717),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color(0xFF171717),
    tertiary = Color(0xFF0070F3),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD3E5FF),
    onTertiaryContainer = Color(0xFF0761D1),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF171717),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171717),
    surfaceTint = Color(0xFF171717),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFEBEBEB),
    surfaceContainerHighest = Color(0xFFE0E0E0),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFEBEBEB),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    inverseSurface = Color(0xFF171717),
    inverseOnSurface = Color(0xFFFAFAFA),
    outline = Color(0xFFEBEBEB),
    outlineVariant = Color(0xFFF0F0F0),
    scrim = Color(0xFF000000),
    error = Color(0xFFEE0000),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF7D4D6),
    onErrorContainer = Color(0xFFC50000)
)

private val AutoSmsShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(9999.dp)
)

@Composable
fun AutoSmsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AutoSmsColors,
        shapes = AutoSmsShapes,
        content = content
    )
}
