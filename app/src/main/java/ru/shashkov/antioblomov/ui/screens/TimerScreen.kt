package ru.shashkov.antioblomov.ui.screens

import ru.shashkov.antioblomov.ui.components.Scene
import ru.shashkov.antioblomov.ui.components.SceneBox
import ru.shashkov.antioblomov.ui.components.rememberSceneAnchor
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import ru.shashkov.antioblomov.ui.components.AppCard
import ru.shashkov.antioblomov.ui.theme.BigNumber
import ru.shashkov.antioblomov.ui.theme.Line
import ru.shashkov.antioblomov.ui.theme.Overline
import ru.shashkov.antioblomov.ui.theme.Surface2
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.Warn
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import ru.shashkov.antioblomov.blocking.TimerNotification
import ru.shashkov.antioblomov.blocking.openNotificationSettings
import ru.shashkov.antioblomov.data.SettingsStore
import ru.shashkov.antioblomov.rules.ChangeGuard
import ru.shashkov.antioblomov.rules.Settings
import ru.shashkov.antioblomov.rules.TimeText
import ru.shashkov.antioblomov.rules.TimerRules
import ru.shashkov.antioblomov.ui.components.DurationDialog
import ru.shashkov.antioblomov.ui.components.WaitDialog
import ru.shashkov.antioblomov.ui.i18n.LocalStrings
import ru.shashkov.antioblomov.ui.theme.Surface1
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import ru.shashkov.antioblomov.ui.theme.Accent
import java.time.Instant
import java.time.ZonedDateTime

/** Какое окно открыто: своё время для запуска или продление. */
private enum class TimerDialog { CUSTOM, EXTEND }

/**
 * Экран «Таймер» (FR-09, FR-10): разовая блокировка на 1, 2, 3 часа или своё время.
 * Действует поверх расписания и не меняет его. Пока идёт — обратный отсчёт здесь
 * и в уведомлении; продлить можно сразу, остановить — только после 15 секунд паузы.
 */
@Composable
fun TimerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val s = LocalStrings.current
    val store = remember { SettingsStore(context) }
    var settings by remember { mutableStateOf(store.read()) }
    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    var dialog by remember { mutableStateOf<TimerDialog?>(null) }
    var confirmStop by remember { mutableStateOf(false) }
    var notificationsOn by remember { mutableStateOf(TimerNotification.allowed(context)) }

    // Разрешение на уведомления (Android 13+): спрашиваем при первом запуске таймера
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsOn = TimerNotification.allowed(context)
        TimerNotification.update(context, store.read())
    }

    // Кнопка «Включить уведомления»: сначала системный запрос; если Android его
    // больше не показывает (дважды отказали) — открываем настройки уведомлений
    val enablePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsOn = TimerNotification.allowed(context)
        if (granted) TimerNotification.update(context, store.read()) else openNotificationSettings(context)
    }

    // Отсчёт — раз в секунду; заодно замечаем, если уведомления включили в настройках
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = ZonedDateTime.now()
            notificationsOn = TimerNotification.allowed(context)
        }
    }

    fun commit(change: (Settings) -> Settings) {
        settings = store.update(change)
        now = ZonedDateTime.now()
        TimerNotification.update(context, settings, now)
    }

    fun start(minutes: Int) {
        commit { TimerRules.start(it, minutes, ZonedDateTime.now()) }
        if (needsPermission(context)) askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    val remaining = TimerRules.remainingMillis(settings, now)
    val endsAt = settings.timerEndsAt?.let { Instant.ofEpochMilli(it).atZone(now.zone) }

    // Сколько осталось от всего срока таймера — дуга циферблата убывает
    val fraction = if (remaining != null && endsAt != null) {
        val start = settings.timerStartedAt ?: (settings.timerEndsAt!! - remaining)
        val total = (settings.timerEndsAt!! - start).coerceAtLeast(1L)
        (remaining.toFloat() / total).coerceIn(0f, 1f)
    } else null

    // Фон (R6): шкала вокруг циферблата
    val dialAnchor = rememberSceneAnchor()

    SceneBox(scene = Scene.BEZEL, anchor = dialAnchor, modifier = modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = s.plan.timerTitle, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = s.plan.timerHint,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        if (settings.blockedPackages.isEmpty()) {
            Text(
                text = s.plan.noAppsSelected,
                style = MaterialTheme.typography.bodySmall,
                color = Warn,
            )
        }

        TimerDial(
            fraction = fraction,
            countdown = remaining?.let(TimerRules::countdown),
            until = endsAt?.let { s.plan.until(TimeText.at(it, now, s.common)) },
            ringModifier = dialAnchor.modifier,
        )

        if (remaining != null && endsAt != null) {
            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                onClick = { dialog = TimerDialog.EXTEND },
            ) { Text(s.plan.extend, style = MaterialTheme.typography.labelLarge) }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Line),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                onClick = {
                    val before = store.read()
                    if (ChangeGuard.needsWait(before, TimerRules.stop(before), ZonedDateTime.now())) {
                        confirmStop = true
                    } else {
                        commit { TimerRules.stop(it) }
                    }
                },
            ) { Text(s.plan.stopIn(ChangeGuard.WAIT_SECONDS), style = MaterialTheme.typography.bodyMedium) }
        } else {
            Text(s.plan.howLong, style = Overline, color = TextMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TimerRules.PRESETS_MINUTES.forEach { minutes ->
                    Chip(
                        text = TimerRules.durationText(minutes, s.common),
                        strong = true,
                        modifier = Modifier.weight(1f),
                        onClick = { start(minutes) },
                    )
                }
                Chip(text = s.plan.custom, strong = false, modifier = Modifier.weight(1.3f), onClick = { dialog = TimerDialog.CUSTOM })
            }
        }

        if (!notificationsOn) {
            NotificationsOff(onEnable = {
                if (needsPermission(context)) enablePermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else openNotificationSettings(context)
            })
        }
    }
    }

    when (dialog) {
        TimerDialog.CUSTOM -> DurationDialog(
            title = s.plan.customTitle,
            confirmText = s.plan.startTimer,
            initialHours = 1,
            initialMinutes = 0,
            preview = { minutes -> s.plan.until(TimeText.at(ZonedDateTime.now().plusMinutes(minutes.toLong()), ZonedDateTime.now(), s.common)) },
            onDismiss = { dialog = null },
            onConfirm = { minutes ->
                dialog = null
                start(minutes)
            },
        )

        TimerDialog.EXTEND -> DurationDialog(
            title = s.plan.extendBy,
            confirmText = s.plan.extend,
            initialHours = 0,
            initialMinutes = 30,
            preview = { minutes ->
                val t = ZonedDateTime.now()
                val end = TimerRules.extend(settings, minutes, t).timerEndsAt!!
                s.plan.until(TimeText.at(Instant.ofEpochMilli(end).atZone(t.zone), t, s.common))
            },
            onDismiss = { dialog = null },
            onConfirm = { minutes ->
                dialog = null
                commit { TimerRules.extend(it, minutes, ZonedDateTime.now()) }
                Toast.makeText(context, s.plan.extendedBy(TimerRules.durationText(minutes, s.common)), Toast.LENGTH_SHORT).show()
            },
        )

        null -> Unit
    }

    if (confirmStop) {
        WaitDialog(
            title = s.plan.stopTimerQ,
            onConfirm = {
                confirmStop = false
                commit { TimerRules.stop(it) }
            },
            onDismiss = { confirmStop = false },
        )
    }
}

/**
 * Циферблат таймера (редизайн R3): пока таймер идёт, терракотовая дуга убывает,
 * в центре крупно отсчёт и «до 18:30»; не идёт — пустое кольцо.
 *
 * @param fraction какая часть срока осталась, 0..1; null — таймер не идёт
 */
@Composable
private fun TimerDial(fraction: Float?, countdown: String?, until: String?, ringModifier: Modifier = Modifier) {
    val running = fraction != null
    val s = LocalStrings.current
    // Дуга меняется раз в секунду — плавно, без рывков
    val sweep by animateFloatAsState(fraction ?: 0f, tween(1000, easing = LinearEasing), label = "dial")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clearAndSetSemantics {
                contentDescription = if (running) s.plan.dialRunning(countdown.orEmpty(), until.orEmpty()) else s.plan.dialIdle
            },
        contentAlignment = Alignment.Center,
    ) {
        if (running) {
            Canvas(Modifier.size(350.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to Accent.copy(alpha = 0.20f),
                        0.45f to Accent.copy(alpha = 0.05f),
                        0.7f to Color.Transparent,
                        center = center,
                        radius = size.minDimension / 2,
                    ),
                )
            }
        }
        Canvas(Modifier.size(286.dp).then(ringModifier)) {
            val stroke = 8.dp.toPx()
            val radius = (size.minDimension - stroke) / 2
            drawCircle(Surface2, radius = radius, style = Stroke(stroke))
            // Четыре метки по краю — как на циферблате
            val tick = 8.dp.toPx()
            for (i in 0 until 4) {
                val angle = Math.toRadians(i * 90.0 - 90.0)
                val outer = size.minDimension / 2
                val inner = outer - tick
                drawLine(
                    color = Line,
                    start = Offset(center.x + (inner * cos(angle)).toFloat(), center.y + (inner * sin(angle)).toFloat()),
                    end = Offset(center.x + (outer * cos(angle)).toFloat(), center.y + (outer * sin(angle)).toFloat()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            if (running) {
                drawArc(
                    color = Accent,
                    startAngle = -90f,
                    sweepAngle = 360f * sweep,
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (running) {
                // Только обратный отсчёт (R6); «до 18:30» остаётся для экранного диктора
                Text(countdown.orEmpty(), style = BigNumber.copy(fontSize = 44.sp, lineHeight = 48.sp))
            } else {
                Text(s.plan.notRunning, style = Overline, color = TextMuted)
                Text(s.plan.pickBelow, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}

/** Чип выбора: 1 ч · 2 ч · 3 ч (крупно, на поверхности) и «Своё» (тише, только рамка). */
@Composable
private fun Chip(text: String, strong: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (strong) Surface1 else Color.Transparent)
            .border(1.dp, Line, RoundedCornerShape(26.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = if (strong) MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp) else MaterialTheme.typography.bodyMedium,
            color = if (strong) TextPrimary else TextMuted,
        )
    }
}

@Composable
private fun NotificationsOff(onEnable: () -> Unit) {
    val s = LocalStrings.current
    AppCard {
        Text(
            text = s.plan.notificationsOff,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
        TextButton(onClick = onEnable) { Text(s.plan.enableNotifications) }
    }
}

/** Android 13+ и разрешение на уведомления ещё не выдано. */
private fun needsPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
