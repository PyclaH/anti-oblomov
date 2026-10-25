package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/** Проверки логики экрана «Таймер». */
class TimerRulesTest {

    private val zone = ZoneId.of("Asia/Irkutsk")
    private fun at(h: Int, m: Int = 0, day: Int = 26) = ZonedDateTime.of(2026, 10, day, h, m, 0, 0, zone)
    private fun ms(t: ZonedDateTime) = t.toInstant().toEpochMilli()

    @Test fun `запуск на 2 часа`() {
        val s = TimerRules.start(Settings(), 120, at(10))
        assertEquals(ms(at(12)), s.timerEndsAt)
        assertTrue(TimerRules.isRunning(s, at(11, 59)))
        assertFalse(TimerRules.isRunning(s, at(12)))
    }

    @Test fun `таймер через полночь`() {
        val s = TimerRules.start(Settings(), 180, at(23))
        assertEquals(ms(at(2, 0, day = 27)), s.timerEndsAt)
        assertTrue(BlockRules.status(s, at(1, 0, day = 27)) is BlockStatus.Blocked)
    }

    @Test fun `таймер не меняет расписание`() {
        val schedule = mapOf(java.time.DayOfWeek.MONDAY to listOf(TimeInterval.of(10, 0, 14, 0)))
        val s = TimerRules.start(Settings(schedule = schedule), 60, at(15))
        assertEquals(schedule, s.schedule)
    }

    @Test fun `продление идущего таймера прибавляет к концу`() {
        val s = TimerRules.extend(TimerRules.start(Settings(), 60, at(10)), 30, at(10, 20))
        assertEquals(ms(at(11, 30)), s.timerEndsAt)
    }

    @Test fun `продление кончившегося таймера считает от текущего момента`() {
        val s = TimerRules.extend(TimerRules.start(Settings(), 60, at(10)), 30, at(12))
        assertEquals(ms(at(12, 30)), s.timerEndsAt)
    }

    @Test fun `продлевать можно несколько раз, итог больше 12 часов`() {
        var s = TimerRules.start(Settings(), 720, at(8))
        s = TimerRules.extend(s, 720, at(8))
        assertEquals(ms(at(8, 0, day = 27)), s.timerEndsAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `больше 12 часов за раз нельзя`() {
        TimerRules.start(Settings(), 721, at(10))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ноль минут нельзя`() {
        TimerRules.extend(Settings(), 0, at(10))
    }

    @Test fun `остановка`() {
        val s = TimerRules.stop(TimerRules.start(Settings(), 60, at(10)))
        assertNull(s.timerEndsAt)
        assertNull(TimerRules.remainingMillis(s, at(10)))
    }

    @Test fun `ввод часов и минут`() {
        assertEquals(DurationCheck.Ok(90), TimerRules.parseDuration("1", "30"))
        assertEquals(DurationCheck.Ok(45), TimerRules.parseDuration("", "45"))
        assertEquals(DurationCheck.Ok(120), TimerRules.parseDuration("2", ""))
        assertEquals(DurationCheck.Ok(1), TimerRules.parseDuration("0", "1"))
        assertEquals(DurationCheck.Ok(720), TimerRules.parseDuration("12", "0"))
        assertEquals(DurationCheck.Ok(90), TimerRules.parseDuration("0", "90"))
    }

    @Test fun `неверная длительность`() {
        assertTrue(TimerRules.parseDuration("", "") is DurationCheck.Error)
        assertTrue(TimerRules.parseDuration("0", "0") is DurationCheck.Error)
        assertTrue(TimerRules.parseDuration("12", "1") is DurationCheck.Error)
        assertTrue(TimerRules.parseDuration("13", "") is DurationCheck.Error)
        assertTrue(TimerRules.parseDuration("1a", "") is DurationCheck.Error)
    }

    @Test fun `подпись длительности`() {
        assertEquals("45 мин", TimerRules.durationText(45))
        assertEquals("2 ч", TimerRules.durationText(120))
        assertEquals("1 ч 30 мин", TimerRules.durationText(90))
    }

    @Test fun `обратный отсчёт`() {
        assertEquals("2:59:59", TimerRules.countdown((2 * 3600 + 59 * 60 + 59) * 1000L))
        assertEquals("59:59", TimerRules.countdown(3599_000))
        assertEquals("00:01", TimerRules.countdown(1))
        assertEquals("1:00:00", TimerRules.countdown(3600_000))
    }
}
