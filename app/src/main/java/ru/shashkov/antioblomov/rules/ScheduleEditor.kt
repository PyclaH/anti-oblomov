package ru.shashkov.antioblomov.rules

import java.time.DayOfWeek

/** Результат проверки интервала перед сохранением (FR-04). */
sealed interface IntervalCheck {
    data class Ok(val interval: TimeInterval) : IntervalCheck
    data class Error(val message: String) : IntervalCheck
}

/**
 * Логика экрана «Расписание» без Android — проверяется юнит-тестами (ScheduleEditorTest).
 */
object ScheduleEditor {

    val WEEKDAYS: List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
    )

    /** Короткие подписи для кнопок дней. */
    fun shortName(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Пн"
        DayOfWeek.TUESDAY -> "Вт"
        DayOfWeek.WEDNESDAY -> "Ср"
        DayOfWeek.THURSDAY -> "Чт"
        DayOfWeek.FRIDAY -> "Пт"
        DayOfWeek.SATURDAY -> "Сб"
        DayOfWeek.SUNDAY -> "Вс"
    }

    fun fullName(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Понедельник"
        DayOfWeek.TUESDAY -> "Вторник"
        DayOfWeek.WEDNESDAY -> "Среда"
        DayOfWeek.THURSDAY -> "Четверг"
        DayOfWeek.FRIDAY -> "Пятница"
        DayOfWeek.SATURDAY -> "Суббота"
        DayOfWeek.SUNDAY -> "Воскресенье"
    }

    /**
     * Проверить интервал перед сохранением: конец должен быть позже начала.
     * Конец 00:00 означает «до полуночи» (24:00).
     *
     * Пересечения с другими интервалами ошибкой не считаются: при сохранении
     * интервалы молча объединяются (put). [existing] и [replacing] оставлены
     * для совместимости вызовов.
     */
    @Suppress("UNUSED_PARAMETER")
    fun check(
        startMinute: Int,
        endMinute: Int,
        existing: List<TimeInterval> = emptyList(),
        replacing: TimeInterval? = null,
    ): IntervalCheck {
        val end = if (endMinute == 0) TimeInterval.MINUTES_IN_DAY else endMinute
        if (startMinute !in 0 until TimeInterval.MINUTES_IN_DAY || end > TimeInterval.MINUTES_IN_DAY) {
            return IntervalCheck.Error("Время вне суток")
        }
        if (end <= startMinute) {
            return IntervalCheck.Error("Конец должен быть позже начала")
        }
        return IntervalCheck.Ok(TimeInterval(startMinute, end))
    }

    /**
     * Добавить интервал или заменить [old] на [new] в день [day].
     * Если новый интервал пересекается или соприкасается с другими (конец одного =
     * начало другого), они молча объединяются в один (решение от 25.10.2026).
     */
    fun put(settings: Settings, day: DayOfWeek, new: TimeInterval, old: TimeInterval? = null): Settings {
        var merged = new
        var rest = settings.intervalsOn(day).filter { it != old }
        while (true) {
            val (touching, apart) = rest.partition {
                it.startMinute <= merged.endMinute && merged.startMinute <= it.endMinute
            }
            if (touching.isEmpty()) break
            merged = TimeInterval(
                minOf(merged.startMinute, touching.minOf { it.startMinute }),
                maxOf(merged.endMinute, touching.maxOf { it.endMinute }),
            )
            rest = apart
        }
        val list = (rest + merged).sortedBy { it.startMinute }
        return settings.copy(schedule = settings.schedule + (day to list))
    }

    fun remove(settings: Settings, day: DayOfWeek, interval: TimeInterval): Settings {
        val list = settings.intervalsOn(day).filter { it != interval }
        return settings.copy(schedule = if (list.isEmpty()) settings.schedule - day else settings.schedule + (day to list))
    }

    /** Перерывы — промежутки между соседними интервалами одного дня. */
    fun breaks(intervals: List<TimeInterval>): List<TimeInterval> =
        intervals.sortedBy { it.startMinute }
            .zipWithNext()
            .filter { (a, b) -> a.endMinute < b.startMinute }
            .map { (a, b) -> TimeInterval(a.endMinute, b.startMinute) }

    /** Скопировать расписание дня [from] на все будни (Пн–Пт). */
    fun copyToWeekdays(settings: Settings, from: DayOfWeek): Settings {
        val source = settings.intervalsOn(from)
        var schedule = settings.schedule
        for (day in WEEKDAYS) {
            schedule = if (source.isEmpty()) schedule - day else schedule + (day to source)
        }
        return settings.copy(schedule = schedule)
    }

    /** Будни, чьё расписание при копировании заменится на другое — о них нужно предупредить. */
    fun weekdaysToOverwrite(settings: Settings, from: DayOfWeek): List<DayOfWeek> {
        val source = settings.intervalsOn(from)
        return WEEKDAYS.filter { it != from && settings.intervalsOn(it).isNotEmpty() && settings.intervalsOn(it) != source }
    }

    /**
     * Что предложить в новом интервале: начать там, где кончается последний, длиной 1 час.
     * В пустой день — 09:00–13:00. Null — день занят до полуночи.
     */
    fun suggestNew(intervals: List<TimeInterval>): Pair<Int, Int>? {
        val last = intervals.maxByOrNull { it.endMinute } ?: return 9 * 60 to 13 * 60
        val start = last.endMinute
        if (start >= TimeInterval.MINUTES_IN_DAY) return null
        return start to minOf(start + 60, TimeInterval.MINUTES_IN_DAY)
    }
}
