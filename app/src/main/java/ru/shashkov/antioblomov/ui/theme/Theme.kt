package ru.shashkov.antioblomov.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val AntiOblomovColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Paper,
    primaryContainer = Sand,
    onPrimaryContainer = Ink,
    secondary = Lake,
    onSecondary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Slate,
    surfaceContainer = Sand,
)

// Заголовки — с засечками, как в презентации; остальной текст — стандартный.
private val base = Typography()
private val AntiOblomovTypography = base.copy(
    headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
)

/**
 * Тема приложения. Пока только светлая — тёмную добавим позже, если понадобится.
 */
@Composable
fun AntiOblomovTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AntiOblomovColors,
        typography = AntiOblomovTypography,
        content = content,
    )
}
