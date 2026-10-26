package ru.shashkov.antioblomov.rules

import ru.shashkov.antioblomov.rules.i18n.CommonRu
import ru.shashkov.antioblomov.rules.i18n.CommonStrings
import ru.shashkov.antioblomov.rules.i18n.HomeRu
import ru.shashkov.antioblomov.rules.i18n.HomeStrings
import java.time.ZonedDateTime

/** Три строки в кольце на главной. */
data class RingText(val overline: String, val big: String, val sub: String)

/** Подписи статуса на главной (FR-02). Тексты — из [HomeStrings] (по умолчанию русские). */
object HomeStatusText {

    /** Крупная строка: «Блокировка до 14:00» или «Свободное время». */
    fun title(
        status: BlockStatus,
        now: ZonedDateTime,
        s: HomeStrings = HomeRu,
        c: CommonStrings = CommonRu,
    ): String = when (status) {
        is BlockStatus.Blocked -> s.blockedUntil(TimeText.at(status.until, now, c))
        is BlockStatus.Free -> s.freeTime
    }

    /** Строка под ней: источник блокировки или когда следующая. */
    fun detail(
        status: BlockStatus,
        now: ZonedDateTime,
        s: HomeStrings = HomeRu,
        c: CommonStrings = CommonRu,
    ): String = when (status) {
        is BlockStatus.Blocked -> when {
            status.bySchedule && status.byTimer -> s.byScheduleAndTimer
            status.byTimer -> s.byTimer
            else -> s.bySchedule
        }
        is BlockStatus.Free -> status.nextBlockAt?.let {
            val day = dayWord(it, now, c)
            s.nextBlock(day, it.format(HHMM), tomorrow = day == c.tomorrow)
        } ?: s.noBlocksScheduled
    }

    /**
     * Подписи кольца на главной (редизайн R2): мелкая строка сверху, крупное время, строка снизу.
     * Блокировка: «ИДЁТ БЛОКИРОВКА» · «14:00» · «до конца · по расписанию»
     * (не сегодня — «завтра · по таймеру»). Свободно: «СВОБОДНОЕ ВРЕМЯ» · «16:00» ·
     * «следующая блокировка» (не сегодня — «следующая · завтра»); без расписания и таймера — «∞».
     */
    fun ring(
        status: BlockStatus,
        now: ZonedDateTime,
        s: HomeStrings = HomeRu,
        c: CommonStrings = CommonRu,
    ): RingText = when (status) {
        is BlockStatus.Blocked -> {
            val day = dayWord(status.until, now, c)
            val source = detail(status, now, s, c)
            RingText(
                overline = s.ringBlocked,
                big = status.until.format(HHMM),
                sub = if (day.isEmpty()) "${s.untilEnd} · $source" else "$day · $source",
            )
        }
        is BlockStatus.Free -> {
            val next = status.nextBlockAt
            if (next == null) {
                RingText(s.ringFree, "∞", s.noBlocksScheduled)
            } else {
                val day = dayWord(next, now, c)
                RingText(
                    overline = s.ringFree,
                    big = next.format(HHMM),
                    sub = if (day.isEmpty()) s.nextBlockLabel else "${s.nextShort} · $day",
                )
            }
        }
    }

    /** «», «завтра», «Чт», «02.11» — то, что TimeText пишет перед временем. */
    private fun dayWord(t: ZonedDateTime, now: ZonedDateTime, c: CommonStrings): String =
        TimeText.at(t, now, c).removeSuffix(t.format(HHMM)).trim()

    private val HHMM = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
}
