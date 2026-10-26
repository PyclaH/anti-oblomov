package ru.shashkov.antioblomov.rules

import java.time.DayOfWeek

/**
 * Интервал блокировки внутри одних суток: с [startMinute] включительно
 * до [endMinute] не включительно, в минутах от полуночи.
 * Через полночь интервалы не переходят (решение от 24.10.2026), поэтому
 * конец — не позже 24:00 (1440).
 */
data class TimeInterval(val startMinute: Int, val endMinute: Int) {
    init {
        require(startMinute in 0 until MINUTES_IN_DAY) { "Начало вне суток: $startMinute" }
        require(endMinute in 1..MINUTES_IN_DAY) { "Конец вне суток: $endMinute" }
        require(startMinute < endMinute) { "Начало должно быть раньше конца" }
    }

    operator fun contains(minute: Int): Boolean = minute in startMinute until endMinute

    /** «10:00–14:00» */
    override fun toString(): String = "${hhmm(startMinute)}–${hhmm(endMinute)}"

    companion object {
        const val MINUTES_IN_DAY = 24 * 60

        fun of(startHour: Int, startMin: Int, endHour: Int, endMin: Int) =
            TimeInterval(startHour * 60 + startMin, endHour * 60 + endMin)

        fun hhmm(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)
    }
}

/**
 * Все настройки, от которых зависит блокировка.
 *
 * @property schedule интервалы блокировки по дням недели (перерыв — промежуток между ними)
 * @property blockedPackages приложения, которые блокируются
 * @property timerEndsAt когда закончится разовая блокировка по таймеру (мс с 1970 г.), null — таймер не запущен
 * @property timerStartedAt когда таймер запустили (мс) — для кольца на главной (редизайн R2); продление его не меняет
 */
data class Settings(
    val schedule: Map<DayOfWeek, List<TimeInterval>> = emptyMap(),
    val blockedPackages: Set<String> = emptySet(),
    val timerEndsAt: Long? = null,
    val timerStartedAt: Long? = null,
    /** Язык интерфейса (код из Lang); null — не выбран, берём язык телефона (v1.2). */
    val language: String? = null,
) {
    fun intervalsOn(day: DayOfWeek): List<TimeInterval> =
        schedule[day].orEmpty().sortedBy { it.startMinute }
}
