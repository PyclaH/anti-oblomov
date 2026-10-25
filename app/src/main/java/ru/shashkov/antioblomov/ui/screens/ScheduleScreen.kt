package ru.shashkov.antioblomov.ui.screens

import ru.shashkov.antioblomov.ui.components.Scene
import ru.shashkov.antioblomov.ui.components.SceneBox
import ru.shashkov.antioblomov.ui.components.rememberSceneAnchor
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import ru.shashkov.antioblomov.rules.TimerRules
import ru.shashkov.antioblomov.ui.components.CardShape
import ru.shashkov.antioblomov.ui.theme.Line
import ru.shashkov.antioblomov.ui.theme.Night0
import ru.shashkov.antioblomov.ui.theme.Surface1
import ru.shashkov.antioblomov.ui.theme.Surface2
import ru.shashkov.antioblomov.ui.theme.TABULAR
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import ru.shashkov.antioblomov.ui.theme.Warn
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.shashkov.antioblomov.data.SettingsStore
import ru.shashkov.antioblomov.rules.BlockRules
import ru.shashkov.antioblomov.rules.BlockStatus
import ru.shashkov.antioblomov.rules.ChangeGuard
import ru.shashkov.antioblomov.rules.IntervalCheck
import ru.shashkov.antioblomov.rules.ScheduleEditor
import ru.shashkov.antioblomov.rules.Settings
import ru.shashkov.antioblomov.rules.TimeInput
import ru.shashkov.antioblomov.rules.TimeText
import ru.shashkov.antioblomov.rules.TimeInterval
import ru.shashkov.antioblomov.ui.components.WaitDialog
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.Accent
import java.time.DayOfWeek
import java.time.ZonedDateTime

/** Что редактируем в диалоге: [old] = null — новый интервал. */
private data class EditState(val old: TimeInterval?, val start: Int, val end: Int)

/** Изменение, ждущее 15-секундной паузы (идёт блокировка — ChangeGuard). */
private class PendingChange(val title: String, val change: (Settings) -> Settings, val done: () -> Unit)

/**
 * Экран «Расписание» (FR-03…FR-05): интервалы блокировки по дням недели,
 * перерывы между ними, копирование на будни. Пересекающиеся и соприкасающиеся
 * интервалы объединяются сами. Изменения сохраняются сразу; служба подхватывает их
 * без перезапуска. Если идёт блокировка, а правка её ослабляет на сегодня, —
 * сначала 15 секунд паузы с цитатой (WaitDialog).
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
    var pending by remember { mutableStateOf<PendingChange?>(null) }

    // Статус «сейчас» меняется со временем
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            now = ZonedDateTime.now()
            settings = store.read()
        }
    }

    fun commit(change: (Settings) -> Settings) {
        settings = store.update(change)
        now = ZonedDateTime.now()
    }

    /** Сохранить сразу или, если идёт блокировка и правка её ослабляет, — после паузы. */
    fun save(title: String, change: (Settings) -> Settings, done: () -> Unit = {}) {
        val before = store.read()
        if (ChangeGuard.needsWait(before, change(before), ZonedDateTime.now())) {
            pending = PendingChange(title, change, done)
        } else {
            commit(change)
            done()
        }
    }

    val copied = { Toast.makeText(context, "Скопировано на будни", Toast.LENGTH_SHORT).show() }

    val intervals = settings.intervalsOn(selectedDay)
    val dayName = ScheduleEditor.fullName(selectedDay)

    // Фон (R6): сетка часов, линии совпадают с полосой суток
    val barAnchor = rememberSceneAnchor()

    SceneBox(scene = Scene.HOURS, anchor = barAnchor, modifier = modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "Расписание", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Когда блокировать выбранные приложения. Между интервалами — перерыв, всё открыто.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        if (settings.blockedPackages.isEmpty()) {
            Text(
                text = "Приложения для блокировки не выбраны — отметь их на вкладке «Приложения»",
                style = MaterialTheme.typography.bodySmall,
                color = Warn,
            )
        }

        DaySegment(selected = selectedDay, settings = settings, onSelect = { selectedDay = it })

        // Название дня и меню «⋯» с копированием на будни (строка статуса убрана в R6 — видно по интервалам)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(dayName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Box {
                var menu by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { menu = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Surface1)
                        .border(1.dp, Line, CircleShape),
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Ещё: копировать на будни", tint = TextPrimary)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Копировать на будни (Пн–Пт)") },
                        enabled = intervals.isNotEmpty(),
                        onClick = {
                            menu = false
                            val overwrite = ScheduleEditor.weekdaysToOverwrite(settings, selectedDay)
                            if (overwrite.isEmpty()) {
                                save("Изменить расписание на сегодня?", { ScheduleEditor.copyToWeekdays(it, selectedDay) }, copied)
                            } else {
                                confirmCopy = overwrite
                            }
                        },
                    )
                }
            }
        }

        DayBar(intervals = intervals, nowMinute = if (selectedDay == now.dayOfWeek) now.hour * 60 + now.minute else null, barModifier = barAnchor.modifier)

        if (intervals.isEmpty()) {
            Text(
                text = "В этот день блокировки нет",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val breaks = ScheduleEditor.breaks(intervals)
                val nowMinute = if (selectedDay == now.dayOfWeek) now.hour * 60 + now.minute else -1
                intervals.forEach { interval ->
                    IntervalRow(
                        interval = interval,
                        active = nowMinute in interval,
                        onClick = { editing = EditState(interval, interval.startMinute, interval.endMinute) },
                        onDelete = {
                            save("Удалить интервал $interval?", { ScheduleEditor.remove(it, selectedDay, interval) })
                        },
                    )
                    breaks.firstOrNull { it.startMinute == interval.endMinute }?.let { BreakRow(it) }
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
            onClick = {
                val suggestion = ScheduleEditor.suggestNew(intervals)
                if (suggestion == null) {
                    Toast.makeText(context, "$dayName занят до полуночи — измени существующий интервал", Toast.LENGTH_LONG).show()
                } else {
                    editing = EditState(old = null, start = suggestion.first, end = suggestion.second)
                }
            },
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Интервал", style = MaterialTheme.typography.labelLarge)
        }
    }
    }

    editing?.let { state ->
        IntervalDialog(
            state = state,
            onDismiss = { editing = null },
            onSave = { interval ->
                editing = null
                save(
                    if (state.old == null) "Добавить интервал $interval?" else "Изменить интервал на $interval?",
                    { ScheduleEditor.put(it, selectedDay, interval, old = state.old) },
                )
            },
        )
    }

    confirmCopy?.let { days ->
        AlertDialog(
            onDismissRequest = { confirmCopy = null },
            containerColor = Surface1,
            title = { Text("Копировать на будни?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Расписание на " + days.joinToString(", ") { ScheduleEditor.fullName(it).lowercase() } +
                        " будет заменено расписанием дня «$dayName».",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmCopy = null
                    save("Изменить расписание на сегодня?", { ScheduleEditor.copyToWeekdays(it, selectedDay) }, copied)
                }) { Text("Заменить") }
            },
            dismissButton = { TextButton(onClick = { confirmCopy = null }) { Text("Отмена") } },
        )
    }

    pending?.let { p ->
        WaitDialog(
            title = p.title,
            onConfirm = {
                pending = null
                commit(p.change)
                p.done()
            },
            onDismiss = { pending = null },
        )
    }
}

/**
 * Дни недели — одна строка-«пилюля» (редизайн R3): выбранный день светлый,
 * точка под буквами — в этот день есть расписание.
 */
@Composable
private fun DaySegment(selected: DayOfWeek, settings: Settings, onSelect: (DayOfWeek) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface1)
            .border(1.dp, Line, RoundedCornerShape(22.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DayOfWeek.values().forEach { day ->
            val isSelected = day == selected
            val weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
            val hasSchedule = settings.intervalsOn(day).isNotEmpty()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) TextPrimary else Color.Transparent)
                    .clickable { onSelect(day) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = ScheduleEditor.shortName(day),
                    style = MaterialTheme.typography.labelLarge,
                    color = when {
                        isSelected -> Night0
                        weekend -> TextMuted
                        else -> TextPrimary
                    },
                )
                Box(
                    Modifier
                        .padding(top = 3.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !hasSchedule -> Color.Transparent
                                isSelected -> Night0
                                else -> Accent
                            }
                        )
                )
            }
        }
    }
}

/**
 * Полоса суток 0–24 ч (редизайн R3): интервалы — терракотовыми отрезками,
 * текущий момент (для сегодня) — тонкой светлой меткой. Те же данные, но день виден целиком.
 */
@Composable
private fun DayBar(intervals: List<TimeInterval>, nowMinute: Int?, barModifier: Modifier = Modifier) {
    val day = TimeInterval.MINUTES_IN_DAY.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .then(barModifier)
                .semantics {
                    contentDescription = if (intervals.isEmpty()) "Полоса суток: блокировки нет"
                    else "Полоса суток: " + intervals.joinToString(", ")
                },
        ) {
            val barH = 12.dp.toPx()
            val top = (size.height - barH) / 2
            val r = CornerRadius(barH / 2)
            drawRoundRect(Surface2, topLeft = Offset(0f, top), size = Size(size.width, barH), cornerRadius = r)
            intervals.forEach { iv ->
                val x0 = size.width * iv.startMinute / day
                val x1 = size.width * iv.endMinute / day
                val done = nowMinute != null && nowMinute >= iv.endMinute
                drawRoundRect(
                    color = if (done) Accent.copy(alpha = 0.45f) else Accent,
                    topLeft = Offset(x0, top),
                    size = Size((x1 - x0).coerceAtLeast(barH / 2), barH),
                    cornerRadius = r,
                )
            }
            if (nowMinute != null) {
                val x = size.width * nowMinute / day
                drawRoundRect(
                    color = TextPrimary,
                    topLeft = Offset(x - 1.dp.toPx(), 0f),
                    size = Size(2.dp.toPx(), size.height),
                    cornerRadius = CornerRadius(1.dp.toPx()),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("0", "6", "12", "18", "24").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = TABULAR), color = TextMuted)
            }
        }
    }
}

/** Интервал: замок, время крупно, длительность, удалить. Идущий сейчас — с терракотовым замком. */
@Composable
private fun IntervalRow(interval: TimeInterval, active: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Surface1)
            .border(1.dp, if (active) Accent.copy(alpha = 0.6f) else Line, CardShape)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.Lock, contentDescription = null, tint = if (active) Accent else TextMuted, modifier = Modifier.size(20.dp))
        Text(
            text = "${TimeInterval.hhmm(interval.startMinute)} — ${TimeInterval.hhmm(interval.endMinute)}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
            maxLines = 1,
        )
        Text(
            TimerRules.durationText(interval.endMinute - interval.startMinute),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = "Удалить интервал $interval", tint = TextMuted)
        }
    }
}

/** Перерыв — тонкая пунктирная строка между интервалами: в это время всё открыто. */
@Composable
private fun BreakRow(gap: TimeInterval) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Line,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
                    ),
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = "$gap · перерыв, всё открыто",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}

/**
 * Диалог интервала: начало и конец набираются цифрами на клавиатуре,
 * двоеточие подставляется само («930» → 9:30, «14» → 14:00). После четырёх цифр
 * начала курсор сам переходит в поле конца.
 */
@Composable
private fun IntervalDialog(
    state: EditState,
    onDismiss: () -> Unit,
    onSave: (TimeInterval) -> Unit,
) {
    var startField by remember { mutableStateOf(selectedAll(TimeInput.digitsOf(state.start))) }
    var endField by remember { mutableStateOf(selectedAll(TimeInput.digitsOf(state.end))) }
    val startFocus = remember { FocusRequester() }
    val endFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val start = TimeInput.parse(startField.text)
    val end = TimeInput.parse(endField.text)
    val check: IntervalCheck = when {
        start == null -> IntervalCheck.Error("Введите время начала, например 930 или 0930")
        end == null -> IntervalCheck.Error("Введите время конца, например 1400")
        else -> ScheduleEditor.check(start, end)
    }

    LaunchedEffect(Unit) {
        delay(150) // окно диалога должно успеть появиться
        startFocus.requestFocus()
        keyboard?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface1,
        shape = RoundedCornerShape(28.dp),
        title = { Text(if (state.old == null) "Новый интервал" else "Интервал", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    TimeField(
                        label = "Начало",
                        value = startField,
                        onValueChange = { v ->
                            val typed = v.text != startField.text
                            startField = cleaned(v)
                            if (typed && startField.text.length == TimeInput.MAX_DIGITS) {
                                endField = selectedAll(endField.text)
                                endFocus.requestFocus()
                            }
                        },
                        imeAction = ImeAction.Next,
                        onIme = { endField = selectedAll(endField.text); endFocus.requestFocus() },
                        modifier = Modifier.weight(1f).focusRequester(startFocus),
                    )
                    Text("–", style = MaterialTheme.typography.titleLarge)
                    TimeField(
                        label = "Конец",
                        value = endField,
                        onValueChange = { v -> endField = cleaned(v) },
                        imeAction = ImeAction.Done,
                        onIme = { (check as? IntervalCheck.Ok)?.let { onSave(it.interval) } },
                        modifier = Modifier.weight(1f).focusRequester(endFocus),
                    )
                }
                Text(
                    text = "Только цифры: 930 → 9:30, 14 → 14:00. Конец 2400 или 0000 — до полуночи.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when (check) {
                    is IntervalCheck.Ok -> Text(
                        text = "Блокировка ${check.interval}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    is IntervalCheck.Error -> Text(
                        text = check.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Warn,
                    )
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

@Composable
private fun TimeField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    imeAction: ImeAction,
    onIme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineSmall,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = ColonTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onIme() }, onDone = { onIme() }),
    )
}

/** Текст выделен целиком — первая набранная цифра заменит старое время. */
private fun selectedAll(digits: String) = TextFieldValue(digits, TextRange(0, digits.length))

/** Оставить только цифры (на цифровой клавиатуре бывают «.», «,» и «-»). */
private fun cleaned(v: TextFieldValue): TextFieldValue {
    val digits = TimeInput.clean(v.text)
    return if (digits == v.text) v else TextFieldValue(digits, TextRange(digits.length))
}

/** Показывает «1430» как «14:30»: двоеточие перед двумя последними цифрами. */
private object ColonTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.length < 3) return TransformedText(text, OffsetMapping.Identity)
        val colonAt = digits.length - 2
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = if (offset <= colonAt) offset else offset + 1
            override fun transformedToOriginal(offset: Int) = if (offset <= colonAt) offset else (offset - 1).coerceAtMost(digits.length)
        }
        return TransformedText(AnnotatedString(TimeInput.display(digits)), mapping)
    }
}

/** «14:00» сегодня, «завтра 09:00», «Пн 10:00» в другой день. */
private fun dayTime(t: ZonedDateTime, now: ZonedDateTime): String = TimeText.at(t, now)
