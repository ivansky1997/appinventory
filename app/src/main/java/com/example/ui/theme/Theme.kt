package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = OnForestPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = Color.White,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = OnSageSecondaryContainer,
    tertiary = SlateTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SlateTertiaryContainer,
    onTertiaryContainer = Color(0xFF102A43),
    background = AlabasterBackground,
    onBackground = TextPrimary,
    surface = CrispSurface,
    onSurface = TextPrimary,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = CrispSurface,
    surfaceContainerLow = Color(0xFFF2F7F4),
    surfaceContainer = Color(0xFFECF3EF),
    surfaceContainerHigh = Color(0xFFE5EEE9),
    outline = SubtleOutline,
    outlineVariant = SubtleOutlineVariant,
    error = CrimsonAction,
    onError = Color.White,
    errorContainer = CrimsonSurface,
    onErrorContainer = OnCrimsonSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = Color(0xFF9AC2AE),
    onSecondary = Color(0xFF0E241B),
    secondaryContainer = Color(0xFF21382E),
    onSecondaryContainer = Color(0xFFD5E9DF),
    tertiary = Color(0xFF91BCE6),
    onTertiary = Color(0xFF0E253B),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = Color(0xFF0B120F),
    surfaceContainerLow = DarkSurface,
    surfaceContainer = Color(0xFF1A2821),
    surfaceContainerHigh = Color(0xFF22332A),
    outline = DarkOutline,
    outlineVariant = Color(0xFF26382F),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun PortgearInventoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
