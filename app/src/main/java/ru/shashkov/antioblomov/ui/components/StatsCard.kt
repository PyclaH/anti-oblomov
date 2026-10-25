package ru.shashkov.antioblomov.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.rules.Milestone
import ru.shashkov.antioblomov.rules.Savings
import ru.shashkov.antioblomov.rules.SavingsReport
import ru.shashkov.antioblomov.rules.UsageText
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.Surface1
import ru.shashkov.antioblomov.ui.theme.Accent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE = DateTimeFormatter.ofPattern("dd.MM")

/**
 * «Было / стало» на главной (FR-14…FR-16, решения от 25.10.2026):
 * до установки, после и сэкономлено — всё в среднем за день (в первый день «после» — за сегодня);
 * всего вернул себе; рядом — всё экранное время до и после;
 * с 7 дней — сравнение по вехам. Честно: отрицательная экономия не скрывается.
 *
 * @param report null — данных ещё нет (доступ только что выдан)
 */
@Composable
fun StatsCard(
    hasAccess: Boolean,
    report: SavingsReport?,
    startDate: LocalDate?,
    noAppsSelected: Boolean,
    onOpenAccess: () -> Unit,
    modifier: Modifier = Modifier,
    picker: @Composable () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface1, contentColor = TextPrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Было / стало", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            if (hasAccess) {
                picker()
                HorizontalDivider()
            }

            when {
                !hasAccess -> {
                    Text(
                        "Чтобы видеть, сколько времени вы проводили в соцсетях до «Анти-Обломова» и сколько теперь, " +
                            "включите доступ к статистике. Данные остаются на телефоне.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = onOpenAccess) { Text("Включить доступ") }
                }

                report == null || startDate == null ->
                    Text("Собираем статистику…", style = MaterialTheme.typography.bodyMedium)

                else -> Report(report, startDate, noAppsSelected)
            }
        }
    }
}

@Composable
private fun Report(r: SavingsReport, startDate: LocalDate, noAppsSelected: Boolean) {
    if (noAppsSelected) {
        Text(
            "Выберите приложения для статистики выше — или включите «Активировать все».",
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
        )
    }

    // До установки — после — сэкономлено, всё в среднем за день (решение от 25.10.2026)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Figure("До, в день", r.beforeAvgMinutes?.let(UsageText::minutes) ?: "—", TextPrimary, Modifier.weight(1f))
        Figure("После, в день", UsageText.minutes(r.perDayAfterMinutes), TextPrimary, Modifier.weight(1f))
        val saved = r.perDaySavedMinutes
        Figure(
            title = if (saved != null && saved < 0) "Больше, в день" else "Сэкономлено, в день",
            value = saved?.let { UsageText.minutes(kotlin.math.abs(it)) } ?: "—",
            color = if (saved != null && saved < 0) Accent else Calm,
            modifier = Modifier.weight(1f),
        )
    }
    Caption(
        (if (r.beforeDays > 0) "«До» — в среднем по ${UsageText.days(r.beforeDays)} до ${startDate.format(DATE)}. "
        else "Истории до ${startDate.format(DATE)} в телефоне не нашлось — сравнивать не с чем. ") +
            (if (r.firstDay) "«После» — пока только за сегодня, с полуночи: со второго дня здесь будет среднее."
            else "«После» — в среднем по ${UsageText.days(r.afterDays)} после установки; день установки не считается.")
    )

    r.totalSavedMinutes?.let { total ->
        if (total > 0) {
            HorizontalDivider()
            val days = Savings.schoolDays(total)
            Line(
                "Всего ты вернул себе",
                UsageText.minutes(total) + if (days > 0) " · ${UsageText.schoolDays(days)}" else "",
                Calm,
            )
        }
    }

    // Честность цифр (FR-16): если общее время не снизилось, время просто перетекло в другие приложения
    HorizontalDivider()
    r.screenBeforeAvgMinutes?.let { Line("Всё экранное время до, в день", UsageText.minutes(it)) }
    Line("Всё экранное время после, в день", UsageText.minutes(r.screenPerDayAfterMinutes))

    r.milestone?.let { MilestoneBlock(it) }
}

/** Веха: неделя, две недели, месяц… — сравнение за весь срок (от 7 дней). */
@Composable
private fun MilestoneBlock(m: Milestone) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Calm, contentColor = Surface1),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${m.title} с «Анти-Обломовым»", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("В среднем в день: ${UsageText.minutes(m.afterAvgMinutes)}", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (m.savedPerDayMinutes >= 0) {
                    "Это на ${UsageText.minutes(m.savedPerDayMinutes)} в день меньше, чем до установки. " +
                        "За ${UsageText.days(m.days)} ты вернул себе ${UsageText.minutes(m.totalSavedMinutes.coerceAtLeast(0))}."
                } else {
                    "Это на ${UsageText.minutes(-m.savedPerDayMinutes)} в день больше, чем до установки. " +
                        "Бывает — попробуй добавить интервал в расписание."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun Figure(title: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
        Text(title, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Line(title: String, value: String, color: Color = TextPrimary) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun Caption(text: String) =
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
