package ru.shashkov.antioblomov.rules

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * История использования для «Было / стало».
 * @property startDate день, когда выдан доступ к статистике (= «установка»); сам он не считается — он неполный
 * @property before полные дни до [startDate] (до 7), снимок сделан один раз
 * @property after полные дни после [startDate], журнал пополняется каждый день
 */
data class UsageHistory(
    val startDate: LocalDate,
    val before: Map<LocalDate, DayUsage>,
    val after: Map<LocalDate, DayUsage>,
)

/** Веха: неделя, две недели, месяц, … год с «Анти-Обломовым» — сравнение за этот срок. */
data class Milestone(
    val title: String,
    /** Последний день вехи (включительно). */
    val date: LocalDate,
    val days: Int,
    val afterAvgMinutes: Int,
    val savedPerDayMinutes: Int,
    val totalSavedMinutes: Int,
)

/** Всё, что показывает блок «Было / стало». Минуты; null — посчитать не из чего. */
data class SavingsReport(
    /** Сколько полных дней «до» есть в истории. */
    val beforeDays: Int,
    /** В среднем в день до установки — в заблокированных приложениях. */
    val beforeAvgMinutes: Int?,
    /** Сегодня (пока) — в заблокированных приложениях. */
    val todayMinutes: Int,
    /** Сегодня сэкономлено: было в среднем − сегодня. Может быть отрицательным. */
    val savedTodayMinutes: Int?,
    /** Полных дней после установки. */
    val afterDays: Int,
    /** В среднем в день после установки. */
    val afterAvgMinutes: Int?,
    /** Было − стало, в среднем за день. */
    val savedPerDayMinutes: Int?,
    /** Всего вернул себе с установки: Σ(было − день). */
    val totalSavedMinutes: Int?,
    /** Общее экранное время в среднем в день: до / после / сегодня (FR-16). */
    val screenBeforeAvgMinutes: Int?,
    val screenAfterAvgMinutes: Int?,
    val screenTodayMinutes: Int,
    /** Последняя достигнутая веха (от 7 дней). */
    val milestone: Milestone?,
) {
    /**
     * «После, в день» на главной (решение от 25.10.2026): среднее по полным дням после установки;
     * в первый день, пока полных дней нет, — время за сегодня.
     */
    val perDayAfterMinutes: Int get() = afterAvgMinutes ?: todayMinutes

    /** «Сэкономлено, в день»: до − после в среднем; в первый день — до − сегодня. */
    val perDaySavedMinutes: Int? get() = savedPerDayMinutes ?: savedTodayMinutes

    /** Экранное время «после, в день» — так же. */
    val screenPerDayAfterMinutes: Int get() = screenAfterAvgMinutes ?: screenTodayMinutes

    /** Пока полных дней после установки нет — «после» считается по сегодняшнему дню. */
    val firstDay: Boolean get() = afterDays == 0
}

/**
 * Расчёт «Было / стало» (FR-14…FR-16, решения от 25.10.2026):
 * - «до» и «после» — среднее за доступные полные дни, даже если их меньше 7;
 * - каждый день: сегодня, в среднем до установки, сэкономлено сегодня;
 * - сравнение по вехам — от 7 дней: неделя, 2 недели, 1–12 месяцев.
 * Считаем по ТЕКУЩЕМУ списку приложений для статистики (StatsApps) — поэтому история
 * хранится по всем приложениям.
 */
object Savings {

    /** Учебный день — 8 часов (для «вернул себе N учебных дней»). */
    const val SCHOOL_DAY_MINUTES = 8 * 60

    /**
     * @param blocked приложения для статистики (StatsApps.tracked)
     * @param notScreenTime что не считать в общем экранном времени: рабочий стол, сам «Анти-Обломов»
     */
    fun report(
        history: UsageHistory,
        today: LocalDate,
        todayUsage: DayUsage,
        blocked: Set<String>,
        notScreenTime: Set<String>,
    ): SavingsReport {
        val before = history.before.filterKeys { it < history.startDate }
        val after = history.after.filterKeys { it > history.startDate && it < today }

        fun blockedSec(day: DayUsage) = day.filterKeys { it in blocked }.values.sum()
        fun screenSec(day: DayUsage) = day.filterKeys { it !in notScreenTime }.values.sum()

        val beforeAvgSec = before.values.map(::blockedSec).averageOrNull()
        val afterAvgSec = after.values.map(::blockedSec).averageOrNull()
        val todaySec = blockedSec(todayUsage)

        return SavingsReport(
            beforeDays = before.size,
            beforeAvgMinutes = beforeAvgSec?.toMinutes(),
            todayMinutes = todaySec.toDouble().toMinutes(),
            savedTodayMinutes = beforeAvgSec?.let { (it - todaySec).toMinutes() },
            afterDays = after.size,
            afterAvgMinutes = afterAvgSec?.toMinutes(),
            savedPerDayMinutes = if (beforeAvgSec != null && afterAvgSec != null) (beforeAvgSec - afterAvgSec).toMinutes() else null,
            totalSavedMinutes = beforeAvgSec?.takeIf { after.isNotEmpty() }
                ?.let { b -> after.values.sumOf { b - blockedSec(it) }.toMinutes() },
            screenBeforeAvgMinutes = before.values.map(::screenSec).averageOrNull()?.toMinutes(),
            screenAfterAvgMinutes = after.values.map(::screenSec).averageOrNull()?.toMinutes(),
            screenTodayMinutes = screenSec(todayUsage).toDouble().toMinutes(),
            milestone = beforeAvgSec?.let { b -> lastMilestone(history.startDate, today, after, b, ::blockedSec) },
        )
    }

    /** Вехи: (название, последний день вехи). */
    fun milestones(startDate: LocalDate): List<Pair<String, LocalDate>> =
        listOf("Неделя" to startDate.plusDays(7), "Две недели" to startDate.plusDays(14)) +
            MONTHS.mapIndexed { i, title -> title to startDate.plusMonths(i + 1L) }

    private fun lastMilestone(
        start: LocalDate,
        today: LocalDate,
        after: Map<LocalDate, DayUsage>,
        beforeAvgSec: Double,
        blockedSec: (DayUsage) -> Int,
    ): Milestone? {
        // Веха достигнута, когда её последний день уже закончился
        val (title, date) = milestones(start).lastOrNull { (_, date) -> date < today } ?: return null
        val window = after.filterKeys { it <= date }.values.map(blockedSec)
        if (window.isEmpty()) return null
        val avg = window.average()
        return Milestone(
            title = title,
            date = date,
            days = ChronoUnit.DAYS.between(start, date).toInt(),
            afterAvgMinutes = avg.toMinutes(),
            savedPerDayMinutes = (beforeAvgSec - avg).toMinutes(),
            totalSavedMinutes = window.sumOf { beforeAvgSec - it }.toMinutes(),
        )
    }

    /** «вернул себе 63 ч — это 7 учебных дней». */
    fun schoolDays(minutes: Int): Int = minutes / SCHOOL_DAY_MINUTES

    private val MONTHS = listOf(
        "Месяц", "Два месяца", "Три месяца", "Четыре месяца", "Пять месяцев", "Полгода",
        "Семь месяцев", "Восемь месяцев", "Девять месяцев", "Десять месяцев", "Одиннадцать месяцев", "Год",
    )

    private fun List<Int>.averageOrNull(): Double? = if (isEmpty()) null else average()
    private fun Double.toMinutes(): Int = (this / 60).roundToInt()
}

/** Подписи времени для «Было / стало». */
object UsageText {
    /** 135 → «2 ч 15 мин», 0 → «0 мин». */
    fun minutes(m: Int): String = TimerRules.durationText(m)

    /** «Сэкономлено 40 мин» или честно «на 20 мин больше». */
    fun saved(m: Int): String = if (m >= 0) minutes(m) else "−${minutes(-m)}"

    /** Для плиток: 45 → («45», «мин»), 135 → («2:15», «ч»), −20 → («20», «мин») — знак показывает подпись. */
    fun compact(m: Int): Pair<String, String> {
        val a = kotlin.math.abs(m)
        return if (a < 60) a.toString() to "мин" else "%d:%02d".format(a / 60, a % 60) to "ч"
    }

    /** 1 учебный день, 2 учебных дня, 5 учебных дней. */
    fun schoolDays(n: Int): String = "$n " + when {
        n % 100 in 11..14 -> "учебных дней"
        n % 10 == 1 -> "учебный день"
        n % 10 in 2..4 -> "учебных дня"
        else -> "учебных дней"
    }

    /** 1 день, 3 дня, 5 дней. */
    fun days(n: Int): String = "$n " + when {
        n % 100 in 11..14 -> "дней"
        n % 10 == 1 -> "день"
        n % 10 in 2..4 -> "дня"
        else -> "дней"
    }
}
