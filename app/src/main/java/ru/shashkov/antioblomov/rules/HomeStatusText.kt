package ru.shashkov.antioblomov.rules

import java.time.ZonedDateTime

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

    /** «в 16:00», «завтра в 09:00», «в Чт 10:00». */
    private fun whenText(t: ZonedDateTime, now: ZonedDateTime): String {
        val text = TimeText.at(t, now)
        return if (text.startsWith(TOMORROW)) "завтра в ${text.removePrefix(TOMORROW)}" else "в $text"
    }

    private const val TOMORROW = "завтра "
}
