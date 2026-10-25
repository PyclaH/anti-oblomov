package ru.shashkov.antioblomov.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import ru.shashkov.antioblomov.ui.theme.Night0
import ru.shashkov.antioblomov.ui.theme.Night1
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import kotlin.random.Random

/**
 * Фон всех экранов (план редизайна): графитовый градиент сверху вниз и едва заметное
 * «зерно» — светлые точки с прозрачностью 0–3%, чтобы фон не выглядел плоской заливкой.
 * Зерно — одна плитка 128×128, нарисованная один раз и повторённая по экрану.
 */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val grain = remember { ShaderBrush(ImageShader(grainTile(), TileMode.Repeated, TileMode.Repeated)) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Night0, Night1)))
            .drawBehind { drawRect(grain) },
    ) {
        // Текст без явного цвета — светлый (иначе на прозрачных контейнерах он был бы чёрным)
        CompositionLocalProvider(LocalContentColor provides TextPrimary) { content() }
    }
}

/** Плитка шума: случайная прозрачность белого 0–3%. Сид постоянный — зерно одно и то же. */
private fun grainTile(size: Int = 128): ImageBitmap {
    val random = Random(25)
    val pixels = IntArray(size * size) {
        val alpha = random.nextInt(0, 8) // 0..7 из 255 ≈ 0–3%
        (alpha shl 24) or 0xFFFFFF
    }
    return android.graphics.Bitmap.createBitmap(pixels, size, size, android.graphics.Bitmap.Config.ARGB_8888)
        .asImageBitmap()
}
