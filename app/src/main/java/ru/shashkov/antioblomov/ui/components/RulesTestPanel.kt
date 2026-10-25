package ru.shashkov.antioblomov.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.shashkov.antioblomov.data.InstalledApps
import ru.shashkov.antioblomov.data.SettingsStore
import ru.shashkov.antioblomov.rules.BlockRules
import ru.shashkov.antioblomov.rules.BlockStatus
import ru.shashkov.antioblomov.rules.Settings
import ru.shashkov.antioblomov.rules.TimeInterval
import ru.shashkov.antioblomov.ui.theme.Lake
import ru.shashkov.antioblomov.ui.theme.Terracotta
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Временная панель шага 2: проверить расписание и таймер, пока нет их экранов
 * (шаги 4 и 5). Показывает, что решают правила прямо сейчас, и позволяет
 * поставить короткое тестовое расписание или таймер на минуту.
 */
@Composable
fun RulesTestPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    var settings by remember { mutableStateOf(store.read()) }
    var now by remember { mutableStateOf(ZonedDateTime.now()) }

    // Обновляем раз в секунду: время идёт, статус меняется сам
    LaunchedEffect(Unit) {
        while (true) {
            now = ZonedDateTime.now()
            settings = store.read()
            delay(1_000)
        }
    }

    fun save(change: (Settings) -> Settings) {
        settings = store.update(change)
        now = ZonedDateTime.now()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Проверка расписания и таймера (шаг 2)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            when (val status = BlockRules.status(settings, now)) {
                is BlockStatus.Blocked -> Text(
                    text = "Сейчас: блокировка ${untilText(status.until, now)}" +
                        reason(status),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Terracotta,
                )
                is BlockStatus.Free -> Text(
                    text = "Сейчас: свободное время" +
                        (status.nextBlockAt?.let { ". Следующая блокировка: ${dayTime(it, now)}" } ?: ""),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Lake,
                )
            }

            val today = settings.intervalsOn(now.dayOfWeek)
            Text(
                text = "Расписание на сегодня: " +
                    (if (today.isEmpty()) "нет" else today.joinToString(", ")),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Таймер: " + (settings.timerEndsAt
                    ?.let { java.time.Instant.ofEpochMilli(it).atZone(now.zone) }
                    ?.takeIf { it.isAfter(now) }
                    ?.let { "до ${it.format(TIME_SEC)}" } ?: "не запущен"),
                style = MaterialTheme.typography.bodyMedium,
            )
            val names = remember(settings.blockedPackages) {
                settings.blockedPackages.map { InstalledApps.label(context, it) }.sorted()
            }
            Text(
                text = if (names.isEmpty()) "Блокируется: ничего — выберите на вкладке «Приложения»"
                else "Блокируется: " + names.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = if (names.isEmpty()) Terracotta else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val start = now.hour * 60 + now.minute + 1
                    val end = start + 2
                    if (end > TimeInterval.MINUTES_IN_DAY) {
                        Toast.makeText(context, "Слишком близко к полуночи — попробуйте после 00:00", Toast.LENGTH_LONG).show()
                    } else {
                        save { s -> s.copy(schedule = s.schedule + (now.dayOfWeek to listOf(TimeInterval(start, end)))) }
                    }
                },
            ) { Text("Расписание: со следующей минуты на 2 мин") }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(modifier = Modifier.weight(1f), onClick = { save { BlockRules.startTimer(it, 1, ZonedDateTime.now()) } }) {
                    Text("Таймер 1 мин")
                }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = { save { BlockRules.stopTimer(it) } }) {
                    Text("Стоп таймер")
                }
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { save { it.copy(schedule = emptyMap(), timerEndsAt = null) } },
            ) { Text("Сбросить расписание и таймер") }
        }
    }
}

private val TIME = DateTimeFormatter.ofPattern("HH:mm")
private val TIME_SEC = DateTimeFormatter.ofPattern("HH:mm:ss")
private val DAY_TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm")

private fun untilText(until: ZonedDateTime, now: ZonedDateTime) = "до ${dayTime(until, now)}"

private fun dayTime(t: ZonedDateTime, now: ZonedDateTime): String =
    if (t.toLocalDate() == now.toLocalDate()) t.format(TIME) else t.format(DAY_TIME)

private fun reason(s: BlockStatus.Blocked): String = when {
    s.bySchedule && s.byTimer -> " (расписание и таймер)"
    s.bySchedule -> " (расписание)"
    else -> " (таймер)"
}
