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
    secondary = Color(0xFFB86C19),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE6C7),
    onSecondaryContainer = Color(0xFF482600),
    tertiary = Color(0xFF476B8C),
    background = Color(0xFFF5F8F6),
    onBackground = Color(0xFF17211D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17211D),
    surfaceVariant = Color(0xFFE8EFEB),
    onSurfaceVariant = Color(0xFF52615A),
    outline = Color(0xFFBBC8C1),
    error = Color(0xFFBA3D3D),
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
