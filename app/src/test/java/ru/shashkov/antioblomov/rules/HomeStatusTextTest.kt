package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.THURSDAY
import java.time.ZoneId
import java.time.ZonedDateTime

class HomeStatusTextTest {

    private val zone = ZoneId.of("Asia/Irkutsk")
    /** 26.10.2026 — понедельник. */
    private fun at(h: Int, m: Int = 0) = ZonedDateTime.of(2026, 10, 26, h, m, 0, 0, zone)
    private val lesson = TimeInterval.of(10, 0, 14, 0)

    private fun texts(settings: Settings, now: ZonedDateTime): Pair<String, String> {
        val status = BlockRules.status(settings, now)
        return HomeStatusText.title(status, now) to HomeStatusText.detail(status, now)
    }

    @Test fun `блокировка по расписанию`() {
        val s = Settings(schedule = mapOf(MONDAY to listOf(lesson)))
        assertEquals("Блокировка до 14:00" to "по расписанию", texts(s, at(11)))
    }

    @Test fun `блокировка по таймеру через полночь`() {
        val s = TimerRules.start(Settings(), 180, at(23))
        assertEquals("Блокировка до завтра 02:00" to "по таймеру", texts(s, at(23, 30)))
    }

    @Test fun `расписание и таймер вместе`() {
        val s = TimerRules.start(Settings(schedule = mapOf(MONDAY to listOf(lesson))), 300, at(11))
        assertEquals("Блокировка до 16:00" to "по расписанию и таймеру", texts(s, at(11)))
    }

    @Test fun `свободно, следующая сегодня`() {
        val s = Settings(schedule = mapOf(MONDAY to listOf(lesson, TimeInterval.of(16, 0, 18, 0))))
        assertEquals("Свободное время" to "следующая в 16:00", texts(s, at(15)))
    }

    @Test fun `свободно, следующая завтра`() {
        val s = Settings(schedule = mapOf(TUESDAY to listOf(TimeInterval.of(9, 0, 13, 0))))
        assertEquals("Свободное время" to "следующая завтра в 09:00", texts(s, at(15)))
    }

    @Test fun `свободно, следующая в другой день`() {
        val s = Settings(schedule = mapOf(THURSDAY to listOf(lesson)))
        assertEquals("Свободное время" to "следующая в Чт 10:00", texts(s, at(15)))
    }

    @Test fun `расписания нет`() {
        assertEquals("Свободное время" to "по расписанию блокировок нет", texts(Settings(), at(15)))
    }
}
