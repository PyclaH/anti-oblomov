package ru.shashkov.antioblomov.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.shashkov.antioblomov.data.SettingsStore
import ru.shashkov.antioblomov.rules.BlockRules
import ru.shashkov.antioblomov.rules.BlockStatus
import ru.shashkov.antioblomov.rules.IntervalCheck
import ru.shashkov.antioblomov.rules.ScheduleEditor
import ru.shashkov.antioblomov.rules.Settings
import ru.shashkov.antioblomov.rules.TimeInterval
import ru.shashkov.antioblomov.ui.theme.Lake
import ru.shashkov.antioblomov.ui.theme.Terracotta
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Что редактируем в диалоге: [old] = null — новый интервал. */
private data class EditState(val old: TimeInterval?, val start: Int, val end: Int)

/**
 * Экран «Расписание» (FR-03…FR-05): интервалы блокировки по дням недели,
 * перерывы между ними, копирование на будни, проверка пересечений.
 * Изменения сохраняются сразу; служба подхватывает их без перезапуска.
 */
@Composable
fun ScheduleScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    var settings by remember { mutableStateOf(store.read()) }
    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    var selectedDay by rememberSaveable { mutableStateOf(ZonedDateTime.now().dayOfWeek) }
    var editing by remember { mutableStateOf<EditState?>(null) }
    var confirmCopy by remember { mutableStateOf<List<DayOfWeek>?>(null) }

    // Статус «сейчас» меняется со временем
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            now = ZonedDateTime.now()
            settings = store.read()
        }
    }

    fun save(change: (Settings) -> Settings) {
        settings = store.update(change)
        now = ZonedDateTime.now()
    }

    val intervals = settings.intervalsOn(selectedDay)
    val dayName = ScheduleEditor.fullName(selectedDay)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Расписание", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Когда блокировать выбранные приложения. Между интервалами — перерыв, всё открыто.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        StatusLine(settings, now)

        DayRow(selected = selectedDay, settings = settings, onSelect = { selectedDay = it })

        Text(
            text = dayName + if (selectedDay == now.dayOfWeek) " · сегодня" else "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        if (intervals.isEmpty()) {
            Text(
                text = "В этот день блокировки нет",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val breaks = ScheduleEditor.breaks(intervals)
                intervals.forEach { interval ->
                    IntervalRow(
                        interval = interval,
                        onClick = { editing = EditState(interval, interval.startMinute, interval.endMinute) },
                        onDelete = { save { ScheduleEditor.remove(it, selectedDay, interval) } },
                    )
                    breaks.firstOrNull { it.startMinute == interval.endMinute }?.let { BreakRow(it) }
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val suggestion = ScheduleEditor.suggestNew(intervals)
                if (suggestion == null) {
                    Toast.makeText(context, "$dayName занят до полуночи — измените существующий интервал", Toast.LENGTH_LONG).show()
                } else {
                    editing = EditState(old = null, start = suggestion.first, end = suggestion.second)
                }
            },
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Интервал")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = intervals.isNotEmpty(),
            onClick = {
                val overwrite = ScheduleEditor.weekdaysToOverwrite(settings, selectedDay)
                if (overwrite.isEmpty()) {
                    save { ScheduleEditor.copyToWeekdays(it, selectedDay) }
                    Toast.makeText(context, "Скопировано на будни", Toast.LENGTH_SHORT).show()
                } else {
                    confirmCopy = overwrite
                }
            },
        ) { Text("Копировать на будни (Пн–Пт)") }
    }

    editing?.let { state ->
        IntervalDialog(
            state = state,
            existing = intervals,
            onDismiss = { editing = null },
            onSave = { interval ->
                save { ScheduleEditor.put(it, selectedDay, interval, old = state.old) }
                editing = null
            },
        )
    }

    confirmCopy?.let { days ->
        AlertDialog(
            onDismissRequest = { confirmCopy = null },
            title = { Text("Копировать на будни?") },
            text = {
                Text(
                    "Расписание на " + days.joinToString(", ") { ScheduleEditor.fullName(it).lowercase() } +
                        " будет заменено расписанием дня «$dayName».",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    save { ScheduleEditor.copyToWeekdays(it, selectedDay) }
                    confirmCopy = null
                    Toast.makeText(context, "Скопировано на будни", Toast.LENGTH_SHORT).show()
                }) { Text("Заменить") }
            },
            dismissButton = { TextButton(onClick = { confirmCopy = null }) { Text("Отмена") } },
        )
    }
}

/** «Сейчас: блокировка до 14:00» или «свободное время». */
@Composable
private fun StatusLine(settings: Settings, now: ZonedDateTime) {
    val (text, color) = when (val status = BlockRules.status(settings, now)) {
        is BlockStatus.Blocked -> "Сейчас: блокировка до ${dayTime(status.until, now)}" to Terracotta
        is BlockStatus.Free -> ("Сейчас: свободное время" +
            (status.nextBlockAt?.let { " · следующая блокировка ${dayTime(it, now)}" } ?: "")) to Lake
    }
    Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    if (settings.blockedPackages.isEmpty()) {
        Text(
            text = "Приложения для блокировки не выбраны — отметьте их на вкладке «Приложения»",
            style = MaterialTheme.typography.bodyMedium,
            color = Terracotta,
        )
    }
}

/** Кнопки Пн…Вс; точка под днём — в этот день есть расписание. */
@Composable
private fun DayRow(selected: DayOfWeek, settings: Settings, onSelect: (DayOfWeek) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DayOfWeek.values().forEach { day ->
            val isSelected = day == selected
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onSelect(day) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ScheduleEditor.shortName(day),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (settings.intervalsOn(day).isNotEmpty()) Terracotta else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun IntervalRow(interval: TimeInterval, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = Terracotta)
        Text(
            text = interval.toString(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Удалить интервал $interval")
        }
    }
}

/** Перерыв — пунктирная рамка: в это время всё открыто. */
@Composable
private fun BreakRow(gap: TimeInterval) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Lake,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx())),
                    ),
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "$gap · перерыв, всё открыто",
            style = MaterialTheme.typography.bodyLarge,
            color = Lake,
        )
    }
}

/** Диалог интервала: начало и конец выбираются в стандартных часах Android. */
@Composable
private fun IntervalDialog(
    state: EditState,
    existing: List<TimeInterval>,
    onDismiss: () -> Unit,
    onSave: (TimeInterval) -> Unit,
) {
    val context = LocalContext.current
    var start by remember { mutableIntStateOf(state.start) }
    var end by remember { mutableIntStateOf(state.end) }
    val check = ScheduleEditor.check(start, end, existing, replacing = state.old)

    fun pick(initial: Int, onPicked: (Int) -> Unit) {
        val h = (initial / 60) % 24
        val m = initial % 60
        TimePickerDialog(context, { _, hour, minute -> onPicked(hour * 60 + minute) }, h, m, true).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.old == null) "Новый интервал" else "Интервал") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { pick(start) { start = it } }) {
                    Text("Начало: ${TimeInterval.hhmm(start)}")
                }
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { pick(end) { end = it } }) {
                    Text("Конец: ${if (end == 0) "24:00" else TimeInterval.hhmm(end)}")
                }
                Text(
                    text = "Конец 00:00 — до полуночи.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (check is IntervalCheck.Error) {
                    Text(text = check.message, style = MaterialTheme.typography.bodyMedium, color = Terracotta)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = check is IntervalCheck.Ok,
                onClick = { (check as? IntervalCheck.Ok)?.let { onSave(it.interval) } },
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private val TIME = DateTimeFormatter.ofPattern("HH:mm")

/** «14:00» сегодня, «Пн 10:00» в другой день. */
private fun dayTime(t: ZonedDateTime, now: ZonedDateTime): String =
    if (t.toLocalDate() == now.toLocalDate()) t.format(TIME)
    else "${ScheduleEditor.shortName(t.dayOfWeek)} ${t.format(TIME)}"
