package ru.shashkov.antioblomov.ui.components

import kotlin.math.sin
import kotlin.math.cos
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.shashkov.antioblomov.rules.RingText
import ru.shashkov.antioblomov.ui.theme.Accent
import ru.shashkov.antioblomov.ui.theme.BigNumber
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.Surface2
import ru.shashkov.antioblomov.ui.theme.Warn

/**
 * Кольцо-циферблат на главной (редизайн R2): дуга — какая часть идущей блокировки прошла,
 * в центре — только время (R6): конец блокировки, следующая блокировка или «∞». За кольцом свечение (R4):
 * - блокировка — спокойная терракота, шире кольца, чуть выходит за циферблат;
 * - свободно, впереди блокировка — светлая бирюза, заметнее прежнего;
 * - свободно и ничего не запланировано («∞») — ещё светлее и ярче;
 * - служба не работает — янтарное, как раньше.
 *
 * @param progress 0..1 — блокировка идёт; null — свободное время (дуги нет)
 * @param nothingPlanned нет ни расписания впереди, ни таймера
 * @param description что прочитает экранный диктор
 */
@Composable
fun StatusRing(
    text: RingText,
    progress: Float?,
    nothingPlanned: Boolean,
    serviceWorks: Boolean,
    description: String,
    modifier: Modifier = Modifier,
) {
    val blocked = progress != null
    val tone by animateColorAsState(
        targetValue = when {
            !serviceWorks -> Warn
            blocked -> Accent
            else -> Calm
        },
        animationSpec = tween(300),
        label = "ring-tone",
    )
    val sweep by animateFloatAsState(progress ?: 0f, tween(600), label = "ring-sweep")

    // Свечение: свой цвет, яркость и размер для каждого состояния, переходы плавные
    val glow by animateColorAsState(
        targetValue = when {
            !serviceWorks -> Warn
            blocked -> GLOW_BLOCK
            nothingPlanned -> GLOW_IDLE
            else -> GLOW_FREE
        },
        animationSpec = tween(400),
        label = "glow-tone",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = when {
            !serviceWorks -> 0.24f
            blocked -> 0.30f
            nothingPlanned -> 0.42f
            else -> 0.34f
        },
        animationSpec = tween(400),
        label = "glow-alpha",
    )
    // Доля радиуса, где свечение гаснет: у кольца радиус ~0.68 от радиуса холста (122 из 180 dp)
    val glowReach by animateFloatAsState(
        targetValue = if (blocked && serviceWorks) 0.98f else 0.74f,
        animationSpec = tween(500),
        label = "glow-reach",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        // Свечение
        Canvas(Modifier.requiredSize(360.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    0f to glow.copy(alpha = glowAlpha),
                    glowReach * 0.6f to glow.copy(alpha = glowAlpha * 0.35f),
                    glowReach to Color.Transparent,
                    center = center,
                    radius = size.minDimension / 2,
                ),
            )
        }
        // Кольцо
        Canvas(Modifier.size(244.dp)) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            drawCircle(color = Surface2, radius = (size.minDimension - stroke) / 2, style = Stroke(stroke))
            if (blocked) {
                drawArc(
                    color = tone,
                    startAngle = -90f,
                    sweepAngle = 360f * sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            } else {
                drawCircle(color = tone.copy(alpha = 0.35f), radius = (size.minDimension - stroke) / 2, style = Stroke(2.dp.toPx()))
            }
        }
        // В кольце — только время (R6): остальное и так видно по дуге, свечению и фону.
        // Полные подписи остаются для экранного диктора (description).
        if (text.big == INFINITY) {
            Infinity(Modifier.size(width = 92.dp, height = 46.dp))
        } else {
            Text(text.big, style = BigNumber.copy(fontSize = 52.sp, lineHeight = 56.sp), textAlign = TextAlign.Center)
        }
    }
}

private const val INFINITY = "∞"

/**
 * Знак бесконечности — нарисован, а не набран шрифтом: в Unbounded он несимметричный.
 * Лемниската Бернулли — обе петли одинаковые, линия одной толщины.
 */
@Composable
private fun Infinity(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = 5.dp.toPx()
        val a = (size.width - stroke) / 2
        val path = Path()
        val steps = 160
        for (i in 0..steps) {
            val t = 2 * Math.PI * i / steps
            val d = 1 + sin(t) * sin(t)
            val x = center.x + (a * cos(t) / d).toFloat()
            val y = center.y + (a * sin(t) * cos(t) / d).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, TextPrimary, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Спокойная терракота — свечение во время блокировки (мягче акцента). */
private val GLOW_BLOCK = Color(0xFFC8643F)

/** Светлая бирюза — свободное время, впереди блокировка. */
private val GLOW_FREE = Color(0xFF7CCFC1)

/** Ещё светлее — свободно и ничего не запланировано. */
private val GLOW_IDLE = Color(0xFFA6E3D8)
