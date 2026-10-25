package ru.shashkov.antioblomov.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import ru.shashkov.antioblomov.ui.theme.Ink
import ru.shashkov.antioblomov.ui.theme.Paper
import ru.shashkov.antioblomov.ui.theme.Terracotta
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Таймер", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Разовая блокировка выбранных приложений. Действует поверх расписания и не меняет его.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (settings.blockedPackages.isEmpty()) {
            Text(
                text = "Приложения для блокировки не выбраны — отметьте их на вкладке «Приложения»",
                style = MaterialTheme.typography.bodyMedium,
                color = Terracotta,
            )
        }

        if (remaining != null && endsAt != null) {
            Countdown(remaining = remaining, until = TimeText.at(endsAt, now))
            Button(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = { dialog = TimerDialog.EXTEND }) {
                Text("Продлить", fontSize = 17.sp)
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                onClick = {
                    val before = store.read()
                    if (ChangeGuard.needsWait(before, TimerRules.stop(before), ZonedDateTime.now())) {
                        confirmStop = true
                    } else {
                        commit { TimerRules.stop(it) }
                    }
                },
            ) { Text("Остановить", fontSize = 17.sp) }
        } else {
            Text("На сколько заблокировать?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                TimerRules.PRESETS_MINUTES.forEach { minutes ->
                    Button(modifier = Modifier.weight(1f).height(64.dp), onClick = { start(minutes) }) {
                        Text(TimerRules.durationText(minutes), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            OutlinedButton(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = { dialog = TimerDialog.CUSTOM }) {
                Text("Своё время", fontSize = 17.sp)
            }
        }

        if (!notificationsOn) {
            NotificationsOff(onEnable = {
                if (needsPermission(context)) enablePermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else openNotificationSettings(context)
            })
        }
    }

    when (dialog) {
        TimerDialog.CUSTOM -> DurationDialog(
            title = "Своё время",
            confirmText = "Запустить",
            initialHours = 1,
            initialMinutes = 0,
            preview = { minutes -> "до ${TimeText.at(ZonedDateTime.now().plusMinutes(minutes.toLong()), ZonedDateTime.now())}" },
            onDismiss = { dialog = null },
            onConfirm = { minutes ->
                dialog = null
                start(minutes)
            },
        )

        TimerDialog.EXTEND -> DurationDialog(
            title = "Продлить на",
            confirmText = "Продлить",
            initialHours = 0,
            initialMinutes = 30,
            preview = { minutes ->
                val t = ZonedDateTime.now()
                val end = TimerRules.extend(settings, minutes, t).timerEndsAt!!
                "до ${TimeText.at(Instant.ofEpochMilli(end).atZone(t.zone), t)}"
            },
            onDismiss = { dialog = null },
            onConfirm = { minutes ->
                dialog = null
                commit { TimerRules.extend(it, minutes, ZonedDateTime.now()) }
                Toast.makeText(context, "Продлено на ${TimerRules.durationText(minutes)}", Toast.LENGTH_SHORT).show()
            },
        )

        null -> Unit
    }

    if (confirmStop) {
        WaitDialog(
            title = "Остановить таймер?",
            onConfirm = {
                confirmStop = false
                commit { TimerRules.stop(it) }
            },
            onDismiss = { confirmStop = false },
        )
    }
}

/** Крупный обратный отсчёт. */
@Composable
private fun Countdown(remaining: Long, until: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Ink, contentColor = Paper),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Блокировка по таймеру", style = MaterialTheme.typography.titleMedium, color = Paper.copy(alpha = 0.75f))
            Text(
                text = TimerRules.countdown(remaining),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text("до $until", style = MaterialTheme.typography.titleMedium, color = Paper.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun NotificationsOff(onEnable: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Уведомление с обратным отсчётом выключено — таймер работает и без него.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onEnable) { Text("Включить уведомления") }
    }
}

/** Android 13+ и разрешение на уведомления ещё не выдано. */
private fun needsPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
