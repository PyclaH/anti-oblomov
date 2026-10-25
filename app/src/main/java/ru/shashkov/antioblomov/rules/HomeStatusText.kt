package ru.shashkov.antioblomov.rules

import java.time.ZonedDateTime

/** Три строки в кольце на главной. */
data class RingText(val overline: String, val big: String, val sub: String)

/** Подписи статуса на главной (FR-02). */
object HomeStatusText {

    /** Крупная строка: «Блокировка до 14:00» или «Свободное время». */
    fun title(status: BlockStatus, now: ZonedDateTime): String = when (status) {
        is BlockStatus.Blocked -> "Блокировка до ${TimeText.at(status.until, now)}"
        is BlockStatus.Free -> "Свободное время"
    }

    /** Строка под ней: источник блокировки или когда следующая. */
    fun detail(status: BlockStatus, now: ZonedDateTime): String = when (status) {
        is BlockStatus.Blocked -> when {
            status.bySchedule && status.byTimer -> "по расписанию и таймеру"
            status.byTimer -> "по таймеру"
            else -> "по расписанию"
        }
        is BlockStatus.Free -> status.nextBlockAt?.let { "следующая ${whenText(it, now)}" }
            ?: "по расписанию блокировок нет"
    }

    /**
     * Подписи кольца на главной (редизайн R2): мелкая строка сверху, крупное время, строка снизу.
     * Блокировка: «ИДЁТ БЛОКИРОВКА» · «14:00» · «до конца · по расписанию»
     * (не сегодня — «завтра · по таймеру»). Свободно: «СВОБОДНОЕ ВРЕМЯ» · «16:00» ·
     * «следующая блокировка» (не сегодня — «следующая · завтра»); без расписания — «—».
     */
    fun ring(status: BlockStatus, now: ZonedDateTime): RingText = when (status) {
        is BlockStatus.Blocked -> {
            val day = dayWord(status.until, now)
            val source = detail(status, now)
            RingText(
                overline = "ИДЁТ БЛОКИРОВКА",
                big = status.until.format(HHMM),
                sub = if (day.isEmpty()) "до конца · $source" else "$day · $source",
            )
        }
        is BlockStatus.Free -> {
            val next = status.nextBlockAt
            if (next == null) {
                RingText("СВОБОДНОЕ ВРЕМЯ", "—", "по расписанию блокировок нет")
            } else {
                val day = dayWord(next, now)
                RingText(
                    overline = "СВОБОДНОЕ ВРЕМЯ",
                    big = next.format(HHMM),
                    sub = if (day.isEmpty()) "следующая блокировка" else "следующая · $day",
                )
            }
        }
    }

    /** «», «завтра», «Чт», «02.11» — то, что TimeText пишет перед временем. */
    private fun dayWord(t: ZonedDateTime, now: ZonedDateTime): String =
        TimeText.at(t, now).removeSuffix(t.format(HHMM)).trim()

    private val HHMM = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

    /** «в 16:00», «завтра в 09:00», «в Чт 10:00». */
    private fun whenText(t: ZonedDateTime, now: ZonedDateTime): String {
        val text = TimeText.at(t, now)
        return if (text.startsWith(TOMORROW)) "завтра в ${text.removePrefix(TOMORROW)}" else "в $text"
    }

    private const val TOMORROW = "завтра "
}
