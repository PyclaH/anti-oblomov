package ru.shashkov.antioblomov.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ru.shashkov.antioblomov.ui.theme.Accent
import ru.shashkov.antioblomov.ui.theme.BigNumber
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.PtSerif
import ru.shashkov.antioblomov.ui.theme.TABULAR
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import ru.shashkov.antioblomov.blocking.Quotes
import ru.shashkov.antioblomov.blocking.SceneBackgroundView
import ru.shashkov.antioblomov.rules.ChangeGuard
import ru.shashkov.antioblomov.ui.theme.Night0
import ru.shashkov.antioblomov.ui.theme.TextPrimary

/**
 * Пауза перед изменением настроек во время блокировки (ChangeGuard):
 * во весь экран — цитата на фоне-картинке, как на экране блокировки,
 * и кольцо обратного отсчёта (редизайн R5: PT Serif, Onest, «стеклянная» кнопка). «Подтвердить» становится доступна через 15 секунд.
 * «Отмена» и кнопка «Назад» — ничего не меняют.
 */
@Composable
fun WaitDialog(title: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val quote = remember { Quotes.random() }
    var left by remember { mutableIntStateOf(ChangeGuard.WAIT_SECONDS) }

    LaunchedEffect(Unit) {
        while (left > 0) {
            delay(1_000)
            left--
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
        ),
    ) {
        // Окно диалога по умолчанию не заходит под строку состояния, панель навигации
        // и вырез камеры — отсюда были полосы. Растягиваем его на весь экран,
        // как окно экрана блокировки (BlockOverlay).
        val view = LocalView.current
        val stretched = remember { BooleanArray(1) }
        SideEffect {
            if (!stretched[0]) {
                (view.parent as? DialogWindowProvider)?.window?.let { stretchToFullScreen(it) }
                stretched[0] = true
            }
        }

        Box(Modifier.fillMaxSize().background(Night0)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx -> SceneBackgroundView(ctx, quote.scene, seed = quote.text.hashCode()) },
            )
            // Затемнение, как на экране блокировки
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0x26080A10),
                            0.45f to Color(0x8C080A10),
                            1f to Color(0xBF080A10),
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Сверху — только «Подумай 15 секунд» (R6b): что меняется и что идёт блокировка,
                // и так понятно. Вопрос (title) зачитывает экранный диктор.
                Text(
                    text = "Подумай ${ChangeGuard.WAIT_SECONDS} секунд — точно нужно?",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .semantics { contentDescription = "$title Подумай ${ChangeGuard.WAIT_SECONDS} секунд — точно нужно?" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(1f))

                // Кольцо — над цитатой, одной группой с ней по центру экрана (R6b, макет «Окно паузы»):
                // внизу, над кнопками, оно закрывало каплю на сцене «вода»
                CountdownRing(left = left, total = ChangeGuard.WAIT_SECONDS)

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "«${quote.text}»",
                    modifier = Modifier.fillMaxWidth(),
                    color = TextPrimary,
                    fontFamily = PtSerif,
                    fontSize = quoteSize(quote.text),
                    lineHeight = quoteSize(quote.text) * 1.2f,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = quote.source,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(1f))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    onClick = onDismiss,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = TextPrimary.copy(alpha = 0.08f),
                        contentColor = TextPrimary,
                    ),
                ) {
                    Text("Не менять", style = MaterialTheme.typography.titleMedium)
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = left == 0,
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = TextPrimary,
                        disabledContentColor = TextPrimary.copy(alpha = 0.45f),
                    ),
                ) {
                    Text(
                        if (left > 0) "Подтвердить через $left с" else "Подтвердить",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = TABULAR),
                    )
                }
            }
        }
    }
}

/**
 * Кольцо обратного отсчёта (R5): дуга плавно убывает за 15 секунд, в центре — секунды.
 * Когда время вышло — вместо цифры галочка, кольцо бирюзовое: «Подтвердить» доступна.
 */
@Composable
private fun CountdownRing(left: Int, total: Int) {
    // Дуга убывает плавно и непрерывно — одна анимация от полной до нуля за все 15 секунд
    val fraction = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        fraction.animateTo(0f, tween(durationMillis = total * 1_000, easing = LinearEasing))
    }
    val done = left == 0
    val tone by animateColorAsState(if (done) Calm else Accent, tween(300), label = "wait-tone")

    Box(
        modifier = Modifier
            .size(88.dp)
            .clearAndSetSemantics {
                contentDescription = if (done) "Можно подтвердить" else "Осталось $left секунд"
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val inset = stroke / 2
            val arc = Size(size.width - stroke, size.height - stroke)
            drawCircle(color = TextPrimary.copy(alpha = 0.15f), radius = (size.minDimension - stroke) / 2, style = Stroke(stroke))
            drawArc(
                color = tone,
                startAngle = -90f,
                sweepAngle = if (done) 360f else 360f * fraction.value,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arc,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        if (done) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Calm, modifier = Modifier.size(34.dp))
        } else {
            Text(
                text = left.toString(),
                style = BigNumber.copy(fontSize = 28.sp, lineHeight = 30.sp),
                color = TextPrimary,
            )
        }
    }
}

/** Окно — на весь экран, под системные панели и вырез; содержимое само отступает (safeDrawingPadding). */
private fun stretchToFullScreen(window: Window) {
    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    window.setDimAmount(0f)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    @Suppress("DEPRECATION")
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    @Suppress("DEPRECATION")
    window.navigationBarColor = android.graphics.Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false
    }
    window.attributes = window.attributes.apply {
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        gravity = Gravity.TOP or Gravity.START
        flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            fitInsetsTypes = 0
            fitInsetsSides = 0
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
}

/** Длинные цитаты — мельче, как на экране блокировки. */
private fun quoteSize(text: String) = when {
    text.length <= 40 -> 32.sp
    text.length <= 90 -> 26.sp
    else -> 22.sp
}
