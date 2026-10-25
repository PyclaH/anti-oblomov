package ru.shashkov.antioblomov.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.rules.Milestone
import ru.shashkov.antioblomov.rules.Savings
import ru.shashkov.antioblomov.rules.SavingsReport
import ru.shashkov.antioblomov.rules.UsageText
import ru.shashkov.antioblomov.ui.theme.Accent
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.Overline
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE = DateTimeFormatter.ofPattern("dd.MM")

/**
 * «Было / стало» на главной (FR-14…FR-16; редизайн R2): заголовок мелкими прописными
 * и кнопка списка приложений; три плитки «До / После / Сэкономлено, в день»; строка
 * «Всего ты вернул себе»; подписи, экранное время и веха — по «Подробнее».
 * Честно: отрицательная экономия не скрывается.
 *
 * @param report null — данных ещё нет (доступ только что выдан)
 * @param appsSummary подпись кнопки списка: «Как в списке блокировки: 3»
 * @param picker раскрытый список приложений для статистики
 */
@Composable
fun StatsCard(
    hasAccess: Boolean,
    report: SavingsReport?,
    startDate: LocalDate?,
    noAppsSelected: Boolean,
    onOpenAccess: () -> Unit,
    appsSummary: String,
    modifier: Modifier = Modifier,
    picker: @Composable () -> Unit = {},
) {
    var appsOpen by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("БЫЛО / СТАЛО", style = Overline, color = TextMuted, modifier = Modifier.weight(1f))
            if (hasAccess) {
                Row(
                    modifier = Modifier
                        .clickable { appsOpen = !appsOpen }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(appsSummary, style = MaterialTheme.typography.bodySmall, color = if (noAppsSelected) Accent else TextMuted)
                    Icon(
                        if (appsOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (appsOpen) "Свернуть список приложений" else "Приложения для статистики",
                        tint = TextMuted,
                    )
                }
            }
        }

        AnimatedVisibility(visible = hasAccess && appsOpen) {
            AppCard { picker() }
        }

        when {
            !hasAccess -> AppCard {
                Text(
                    "Чтобы видеть, сколько времени ты проводил в соцсетях до «Анти-Обломова» и сколько теперь, " +
                        "включи доступ к статистике. Данные остаются на телефоне.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onOpenAccess) { Text("Включить доступ") }
            }

            report == null || startDate == null ->
                Text("Собираем статистику…", style = MaterialTheme.typography.bodyMedium, color = TextMuted)

            else -> Report(report, startDate, noAppsSelected)
        }
    }
}

@Composable
private fun Report(r: SavingsReport, startDate: LocalDate, noAppsSelected: Boolean) {
    var details by rememberSaveable { mutableStateOf(false) }

    if (noAppsSelected) {
        Text(
            "Выбери приложения для статистики выше — или включи «Активировать все».",
            style = MaterialTheme.typography.bodySmall,
            color = Accent,
        )
    }

    // Три плитки: до — после — сэкономлено, всё в среднем за день
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tile("До, в день", r.beforeAvgMinutes, TextPrimary, Modifier.weight(1f))
        Tile("После, в день", r.perDayAfterMinutes, TextPrimary, Modifier.weight(1f))
        val saved = r.perDaySavedMinutes
        Tile(
            label = if (saved != null && saved < 0) "Больше, в день" else "Сэкономлено, в день",
            minutes = saved,
            color = if (saved != null && saved < 0) Accent else Calm,
            modifier = Modifier.weight(1f),
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { details = !details }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val total = r.totalSavedMinutes
        Text(
            text = if (total != null && total > 0) {
                val days = Savings.schoolDays(total)
                "Всего ты вернул себе ${UsageText.minutes(total)}" + if (days > 0) " · ${UsageText.schoolDays(days)}" else ""
            } else {
                "Как считаем"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (total != null && total > 0) Calm else TextMuted,
            modifier = Modifier.weight(1f),
        )
        Text(if (details) "Свернуть" else "Подробнее ›", style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }

    AnimatedVisibility(visible = details) {
        AppCard {
            Text(
                (if (r.beforeDays > 0) "«До» — в среднем по ${UsageText.days(r.beforeDays)} до ${startDate.format(DATE)}. "
                else "Истории до ${startDate.format(DATE)} в телефоне не нашлось — сравнивать не с чем. ") +
                    (if (r.firstDay) "«После» — пока только за сегодня, с полуночи: со второго дня здесь будет среднее."
                    else "«После» — в среднем по ${UsageText.days(r.afterDays)} после установки; день установки не считается."),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            HorizontalDivider()
            // Честность цифр (FR-16): если общее время не снизилось, время просто перетекло в другие приложения
            r.screenBeforeAvgMinutes?.let { Line("Всё экранное время до, в день", UsageText.minutes(it)) }
            Line("Всё экранное время после, в день", UsageText.minutes(r.screenPerDayAfterMinutes))
        }
    }

    r.milestone?.let { MilestoneBlock(it) }
}

/** Плитка: крупная цифра, единица мельче, подпись снизу. */
@Composable
private fun Tile(label: String, minutes: Int?, color: Color, modifier: Modifier) {
    AppCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 14.dp), spacing = 6.dp) {
        val (value, unit) = minutes?.let(UsageText::compact) ?: ("—" to "")
        Text(
            buildAnnotatedString {
                append(value)
                if (unit.isNotEmpty()) withStyle(SpanStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize, color = TextMuted)) { append(" $unit") }
            },
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            maxLines = 1,
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }
}

/** Веха: неделя, две недели, месяц… — сравнение за весь срок (от 7 дней). */
@Composable
private fun MilestoneBlock(m: Milestone) {
    AppCard {
        Text("ВЕХА · ${m.title.uppercase()}", style = Overline, color = Calm)
        Text(
            text = if (m.savedPerDayMinutes >= 0) {
                "За ${UsageText.days(m.days)} с «Анти-Обломовым» — на ${UsageText.minutes(m.savedPerDayMinutes)} в день меньше, " +
                    "чем до установки. Ты вернул себе ${UsageText.minutes(m.totalSavedMinutes.coerceAtLeast(0))}."
            } else {
                "За ${UsageText.days(m.days)} — на ${UsageText.minutes(-m.savedPerDayMinutes)} в день больше, чем до установки. " +
                    "Бывает — попробуй добавить интервал в расписание."
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun Line(title: String, value: String, color: Color = TextPrimary) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
