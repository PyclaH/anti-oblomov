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
     * Проверить интервал [startMinute]–[endMinute] для дня, где уже есть [existing].
     * [replacing] — интервал, который сейчас редактируют (с ним не сравниваем).
     * Конец 00:00 означает «до полуночи» (24:00): в часах Android нельзя выбрать 24:00.
     */
    fun check(
        startMinute: Int,
        endMinute: Int,
        existing: List<TimeInterval>,
        replacing: TimeInterval? = null,
    ): IntervalCheck {
        val end = if (endMinute == 0) TimeInterval.MINUTES_IN_DAY else endMinute
        if (end <= startMinute) {
            return IntervalCheck.Error("Конец должен быть позже начала")
        }
        val candidate = TimeInterval(startMinute, end)
        val conflict = existing
            .filter { it != replacing }
            .sortedBy { it.startMinute }
            .firstOrNull { it.startMinute < candidate.endMinute && candidate.startMinute < it.endMinute }
        if (conflict != null) {
            return IntervalCheck.Error("Пересекается с интервалом $conflict")
        }
        return IntervalCheck.Ok(candidate)
    }

    /** Добавить интервал или заменить [old] на [new] в день [day]. */
    fun put(settings: Settings, day: DayOfWeek, new: TimeInterval, old: TimeInterval? = null): Settings {
        val list = settings.intervalsOn(day).filter { it != old } + new
        return settings.copy(schedule = settings.schedule + (day to list.sortedBy { it.startMinute }))
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
