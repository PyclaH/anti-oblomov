package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.TUESDAY
import java.time.ZoneId
import java.time.ZonedDateTime

/** Главная после редизайна: кольцо, плитки, строка «Система». */
class RedesignHomeTest {

    private val zone = ZoneId.of("Asia/Irkutsk")
    /** 26.10.2026 — понедельник. */
    private fun at(h: Int, m: Int = 0, day: Int = 26) = ZonedDateTime.of(2026, 10, day, h, m, 0, 0, zone)
    private val lesson = TimeInterval.of(10, 0, 14, 0)

    // ---------- кольцо: сколько прошло ----------

    @Test fun `расписание — прошла половина`() {
        val s = Settings(schedule = mapOf(MONDAY to listOf(lesson)))
        assertEquals(at(10), BlockRules.blockedSince(s, at(12)))
        assertEquals(0.5f, BlockRules.blockedProgress(s, at(12))!!, 0.001f)
    }

    @Test fun `свободно — кольца нет`() {
        assertNull(BlockRules.blockedProgress(Settings(schedule = mapOf(MONDAY to listOf(lesson))), at(15)))
    }

    @Test fun `интервал через полночь — начало вчера`() {
        val s = Settings(schedule = mapOf(SUNDAY to listOf(TimeInterval(22 * 60, 24 * 60)), MONDAY to listOf(TimeInterval(0, 2 * 60))))
        assertEquals(at(22, day = 25), BlockRules.blockedSince(s, at(1)))
        assertEquals(0.75f, BlockRules.blockedProgress(s, at(1))!!, 0.001f)
    }

    @Test fun `таймер — от запуска, продление начало не сдвигает`() {
        var s = TimerRules.start(Settings(), 60, at(15))
        s = TimerRules.extend(s, 60, at(15, 30))
        assertEquals(at(15), BlockRules.blockedSince(s, at(16)))
        assertEquals(0.5f, BlockRules.blockedProgress(s, at(16))!!, 0.001f)
    }

    @Test fun `таймер внутри интервала — от более раннего`() {
        val s = TimerRules.start(Settings(schedule = mapOf(MONDAY to listOf(lesson))), 300, at(11))
        assertEquals(at(10), BlockRules.blockedSince(s, at(12)))
    }

    @Test fun `остановка таймера стирает начало`() {
        assertNull(TimerRules.stop(TimerRules.start(Settings(), 60, at(15))).timerStartedAt)
    }

    // ---------- кольцо: подписи ----------

    @Test fun `подписи — блокировка сегодня`() {
        val s = Settings(schedule = mapOf(MONDAY to listOf(lesson)))
        assertEquals(RingText("ИДЁТ БЛОКИРОВКА", "14:00", "до конца · по расписанию"), HomeStatusText.ring(BlockRules.status(s, at(11)), at(11)))
    }

    @Test fun `подписи — таймер до завтра`() {
        val s = TimerRules.start(Settings(), 180, at(23))
        assertEquals(RingText("ИДЁТ БЛОКИРОВКА", "02:00", "завтра · по таймеру"), HomeStatusText.ring(BlockRules.status(s, at(23)), at(23)))
    }

    @Test fun `подписи — свободно`() {
        val today = Settings(schedule = mapOf(MONDAY to listOf(TimeInterval.of(16, 0, 18, 0))))
        assertEquals(RingText("СВОБОДНОЕ ВРЕМЯ", "16:00", "следующая блокировка"), HomeStatusText.ring(BlockRules.status(today, at(11)), at(11)))
        val tomorrow = Settings(schedule = mapOf(TUESDAY to listOf(lesson)))
        assertEquals("следующая · завтра", HomeStatusText.ring(BlockRules.status(tomorrow, at(15)), at(15)).sub)
        assertEquals(RingText("СВОБОДНОЕ ВРЕМЯ", "—", "по расписанию блокировок нет"), HomeStatusText.ring(BlockRules.status(Settings(), at(15)), at(15)))
    }

    // ---------- плитки и «Система» ----------

    @Test fun `компактное время для плиток`() {
        assertEquals("45" to "мин", UsageText.compact(45))
        assertEquals("2:05" to "ч", UsageText.compact(125))
        assertEquals("20" to "мин", UsageText.compact(-20))
    }

    private fun state(service: Boolean = true, notifications: Boolean = true, autostart: Boolean = true, usage: Boolean = true) =
        SetupState(PhoneBrand.XIAOMI, service, notifications, true, true, autostart, usage)

    @Test fun `строка система`() {
        assertEquals("всё в порядке", SetupChecklist.summary(state()))
        assertEquals("служба не работает", SetupChecklist.summary(state(service = false)))
        assertEquals("1 настройка пропущена", SetupChecklist.summary(state(usage = false)))
        assertEquals("3 настройки пропущены", SetupChecklist.summary(state(notifications = false, autostart = false, usage = false)))
    }
}
