package ru.shashkov.antioblomov.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Только тёмная тема (решение от 25.10.2026, NFR-06). */
private val AppColors = darkColorScheme(
    primary = Accent,
    onPrimary = Night0,
    primaryContainer = Color(0xFF2A1A14),
    onPrimaryContainer = TextPrimary,
    secondary = Calm,
    onSecondary = Night0,
    secondaryContainer = Surface2,
    onSecondaryContainer = TextPrimary,
    tertiary = Warn,
    onTertiary = Night0,
    background = Night0,
    onBackground = TextPrimary,
    surface = Night0,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMuted,
    surfaceContainerLowest = Night0,
    surfaceContainerLow = Surface1,
    surfaceContainer = Surface1,
    surfaceContainerHigh = Surface1,
    surfaceContainerHighest = Surface2,
    surfaceBright = Surface2,
    surfaceDim = Night0,
    outline = Line,
    outlineVariant = Line,
    error = Warn,
    onError = Night0,
    inverseSurface = TextPrimary,
    inverseOnSurface = Night0,
    scrim = Color(0xCC000000),
)

/** Скругления: поля и кнопки 16, карточки 24. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun AntiOblomovTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
