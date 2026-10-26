package ru.shashkov.antioblomov.rules

import ru.shashkov.antioblomov.rules.i18n.CommonRu
import ru.shashkov.antioblomov.rules.i18n.CommonStrings
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Подписи моментов времени для интерфейса. */
object TimeText {

    private val TIME = DateTimeFormatter.ofPattern("HH:mm")
    private val DATE_TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm")

    /**
     * «14:00» — сегодня, «завтра 01:30», «Пн 10:00» — в ближайшую неделю,
     * дальше — «03.11 10:00». [weekdays] = false — с послезавтра сразу дата (уведомление о блокировке).
     */
    fun at(t: ZonedDateTime, now: ZonedDateTime, s: CommonStrings = CommonRu, weekdays: Boolean = true): String {
        val days = java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), t.toLocalDate())
        return when {
            days == 0L -> t.format(TIME)
            days == 1L -> "${s.tomorrow} ${t.format(TIME)}"
            days in 2..6 && weekdays -> "${s.dayShort(t.dayOfWeek)} ${t.format(TIME)}"
            else -> t.format(DATE_TIME)
        }
    }
}
