package ru.shashkov.antioblomov.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import ru.shashkov.antioblomov.ui.theme.Overline
import ru.shashkov.antioblomov.ui.theme.Surface2
import ru.shashkov.antioblomov.ui.theme.TABULAR
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.Warn

/**
 * Кольцо-циферблат на главной (редизайн R2): дуга — какая часть идущей блокировки прошла,
 * в центре — до какого времени. За кольцом мягкое свечение: тёплое во время блокировки,
 * холодное в свободное время, янтарное, если служба не работает.
 *
 * @param progress 0..1 — блокировка идёт; null — свободное время (дуги нет)
 * @param timerLeft «1:24:07», если идёт таймер
 * @param description что прочитает экранный диктор
 */
@Composable
fun StatusRing(
    text: RingText,
    progress: Float?,
    serviceWorks: Boolean,
    timerLeft: String?,
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        // Свечение
        Canvas(Modifier.size(340.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    0f to tone.copy(alpha = 0.24f),
                    0.45f to tone.copy(alpha = 0.06f),
                    0.7f to Color.Transparent,
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text.overline, style = Overline, color = tone)
            Text(text.big, style = BigNumber.copy(fontSize = 52.sp, lineHeight = 56.sp), textAlign = TextAlign.Center)
            Text(text.sub, style = MaterialTheme.typography.bodySmall, color = TextMuted, textAlign = TextAlign.Center)
            if (timerLeft != null) {
                Text(
                    "таймер · осталось $timerLeft",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = TABULAR),
                    color = TextMuted,
                )
            }
        }
    }
}
