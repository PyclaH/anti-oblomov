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
import ru.shashkov.antioblomov.ui.i18n.LocalStrings
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
    val s = LocalStrings.current
    var appsOpen by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(s.home.statsTitle, style = Overline, color = TextMuted, modifier = Modifier.weight(1f))
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
                        contentDescription = if (appsOpen) s.home.collapseAppList else s.home.statsAppsTitle,
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
                    s.home.accessExplain,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onOpenAccess) { Text(s.home.enableAccess) }
            }

            report == null || startDate == null ->
                Text(s.home.collecting, style = MaterialTheme.typography.bodyMedium, color = TextMuted)

            else -> Report(report, startDate, noAppsSelected)
        }
    }
}

@Composable
private fun Report(r: SavingsReport, startDate: LocalDate, noAppsSelected: Boolean) {
    val s = LocalStrings.current
    val c = s.common
    var details by rememberSaveable { mutableStateOf(false) }

    if (noAppsSelected) {
        Text(
            s.home.chooseAppsHint,
            style = MaterialTheme.typography.bodySmall,
            color = Accent,
        )
    }

    // Три плитки: до — после — сэкономлено, всё в среднем за день
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tile(s.home.tileBefore, r.beforeAvgMinutes, TextPrimary, Modifier.weight(1f))
        Tile(s.home.tileAfter, r.perDayAfterMinutes, TextPrimary, Modifier.weight(1f))
        val saved = r.perDaySavedMinutes
        Tile(
            label = if (saved != null && saved < 0) s.home.tileMore else s.home.tileSaved,
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
                s.home.totalSaved(UsageText.minutes(total, c), if (days > 0) UsageText.schoolDays(days, c) else null)
            } else {
                s.home.howWeCount
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (total != null && total > 0) Calm else TextMuted,
            modifier = Modifier.weight(1f),
        )
        Text(if (details) s.home.collapse else s.home.details + " ›", style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }

    AnimatedVisibility(visible = details) {
        AppCard {
            Text(
                (if (r.beforeDays > 0) s.home.beforeExplain(UsageText.days(r.beforeDays, c), startDate.format(DATE))
                else s.home.noHistory(startDate.format(DATE))) +
                    (if (r.firstDay) s.home.afterFirstDay
                    else s.home.afterExplain(UsageText.days(r.afterDays, c))),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            HorizontalDivider()
            // Честность цифр (FR-16): если общее время не снизилось, время просто перетекло в другие приложения
            r.screenBeforeAvgMinutes?.let { Line(s.home.screenBefore, UsageText.minutes(it, c)) }
            Line(s.home.screenAfter, UsageText.minutes(r.screenPerDayAfterMinutes, c))
        }
    }

    r.milestone?.let { MilestoneBlock(it) }
}

/** Плитка: крупная цифра, единица мельче, подпись снизу. */
@Composable
private fun Tile(label: String, minutes: Int?, color: Color, modifier: Modifier) {
    val c = LocalStrings.current.common
    AppCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 14.dp), spacing = 6.dp) {
        val (value, unit) = minutes?.let { UsageText.compact(it, c) } ?: ("—" to "")
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
    val s = LocalStrings.current
    val c = s.common
    AppCard {
        Text(s.home.milestoneOverline(m.title), style = Overline, color = Calm)
        Text(
            text = if (m.savedPerDayMinutes >= 0) {
                s.home.milestoneGood(
                    days = UsageText.days(m.days, c),
                    perDay = UsageText.minutes(m.savedPerDayMinutes, c),
                    total = UsageText.minutes(m.totalSavedMinutes.coerceAtLeast(0), c),
                )
            } else {
                s.home.milestoneBad(UsageText.days(m.days, c), UsageText.minutes(-m.savedPerDayMinutes, c))
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
