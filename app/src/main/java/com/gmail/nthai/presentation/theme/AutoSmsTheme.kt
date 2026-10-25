package com.gmail.nthai.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val AutoSmsColors = lightColorScheme(
    primary = Color(0xFF176B55),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9F1E8),
    onPrimaryContainer = Color(0xFF103D32),
    inversePrimary = Color(0xFF85D6BB),
    secondary = Color(0xFF8A5A20),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF6E6CF),
    onSecondaryContainer = Color(0xFF3E2A10),
    tertiary = Color(0xFF476B8C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCEAF4),
    onTertiaryContainer = Color(0xFF142E42),
    background = Color(0xFFF4F7F5),
    onBackground = Color(0xFF17211D),
    surface = Color(0xFFFEFFFE),
    onSurface = Color(0xFF17211D),
    surfaceTint = Color(0xFF176B55),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBFCFB),
    surfaceContainer = Color(0xFFF4F7F5),
    surfaceContainerHigh = Color(0xFFE8EFEB),
    surfaceContainerHighest = Color(0xFFDCE5E0),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE1E6E3),
    surfaceVariant = Color(0xFFE8EFEB),
    onSurfaceVariant = Color(0xFF52615A),
    inverseSurface = Color(0xFF2C3430),
    inverseOnSurface = Color(0xFFF0F3F1),
    outline = Color(0xFFBBC8C1),
    outlineVariant = Color(0xFFDCE5E0),
    scrim = Color(0xFF000000),
    error = Color(0xFFBA3D3D),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE1DE),
    onErrorContainer = Color(0xFF5F1414)
)

private val AutoSmsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun AutoSmsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AutoSmsColors,
        shapes = AutoSmsShapes,
        content = content
    )
}
