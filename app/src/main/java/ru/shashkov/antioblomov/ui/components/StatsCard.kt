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
import ru.shashkov.antioblomov.ui.theme.Ink
import ru.shashkov.antioblomov.ui.theme.Lake
import ru.shashkov.antioblomov.ui.theme.Paper
import ru.shashkov.antioblomov.ui.theme.Terracotta
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE = DateTimeFormatter.ofPattern("dd.MM")

/**
 * «Было / стало» на главной (FR-14…FR-16, решения от 25.10.2026):
 * каждый день — сегодня, в среднем до установки и сэкономлено сегодня;
 * с первого полного дня — в среднем после и всего вернул себе; рядом — всё экранное время;
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
        colors = CardDefaults.cardColors(containerColor = Paper, contentColor = Ink),
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
            color = Terracotta,
        )
    }

    // Каждый день: сегодня — до установки — сэкономлено
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Figure("Сегодня", UsageText.minutes(r.todayMinutes), Ink, Modifier.weight(1f))
        Figure("До установки, в день", r.beforeAvgMinutes?.let(UsageText::minutes) ?: "—", Ink, Modifier.weight(1f))
        val saved = r.savedTodayMinutes
        Figure(
            title = if (saved != null && saved < 0) "Больше обычного" else "Сэкономлено",
            value = saved?.let { if (it < 0) UsageText.minutes(-it) else UsageText.minutes(it) } ?: "—",
            color = if (saved != null && saved < 0) Terracotta else Lake,
            modifier = Modifier.weight(1f),
        )
    }
    Caption(
        when {
            r.beforeDays > 0 ->
                "«До установки» — в среднем по ${UsageText.days(r.beforeDays)} до ${startDate.format(DATE)}. " +
                    "«Сегодня» — с полуночи, к вечеру цифра вырастет."
            else -> "Истории до ${startDate.format(DATE)} в телефоне не нашлось — сравнивать не с чем, считаем только «после»."
        }
    )

    if (r.afterDays > 0) {
        HorizontalDivider()
        Line("С установки, в среднем в день", r.afterAvgMinutes?.let(UsageText::minutes) ?: "—")
        r.savedPerDayMinutes?.let { saved ->
            Line(
                if (saved >= 0) "Сэкономлено в среднем в день" else "В среднем в день больше, чем до установки",
                UsageText.minutes(kotlin.math.abs(saved)),
                if (saved >= 0) Lake else Terracotta,
            )
        }
        r.totalSavedMinutes?.let { total ->
            if (total > 0) {
                val days = Savings.schoolDays(total)
                Line(
                    "Всего ты вернул себе",
                    UsageText.minutes(total) + if (days > 0) " · ${UsageText.schoolDays(days)}" else "",
                    Lake,
                )
            }
        }
        Caption("По ${UsageText.days(r.afterDays)} после установки; день установки не считается.")
    }

    // Честность цифр (FR-16): если общее время не снизилось, время просто перетекло в другие приложения
    HorizontalDivider()
    Line("Всё экранное время сегодня", UsageText.minutes(r.screenTodayMinutes))
    r.screenBeforeAvgMinutes?.let { Line("…до установки, в среднем в день", UsageText.minutes(it)) }
    r.screenAfterAvgMinutes?.let { Line("…после установки, в среднем в день", UsageText.minutes(it)) }

    r.milestone?.let { MilestoneBlock(it) }
}

/** Веха: неделя, две недели, месяц… — сравнение за весь срок (от 7 дней). */
@Composable
private fun MilestoneBlock(m: Milestone) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Lake, contentColor = Paper),
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
private fun Line(title: String, value: String, color: Color = Ink) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun Caption(text: String) =
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
