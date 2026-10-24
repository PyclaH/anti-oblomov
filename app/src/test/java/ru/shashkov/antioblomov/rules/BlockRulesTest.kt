package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Проверки правил блокировки. Запуск в Android Studio:
 * правая кнопка по файлу → Run 'BlockRulesTest'.
 */
class BlockRulesTest {

    private val zone = ZoneId.of("Asia/Irkutsk")
    private val youtube = "com.google.android.youtube"

    /** Понедельник, 26 октября 2026 года, hh:mm. */
    private fun mon(h: Int, m: Int = 0) = ZonedDateTime.of(2026, 10, 26, h, m, 0, 0, zone)
    private fun tue(h: Int, m: Int = 0) = ZonedDateTime.of(2026, 10, 27, h, m, 0, 0, zone)

    /** Пример из концепции: понедельник 10–14 и 15–20, перерыв 14–15. */
    private val school = Settings(
        schedule = mapOf(MONDAY to listOf(TimeInterval.of(10, 0, 14, 0), TimeInterval.of(15, 0, 20, 0))),
        blockedPackages = setOf(youtube),
    )

    @Test fun `до начала расписания — свободно, следующая блокировка в 10`() {
        val s = BlockRules.status(school, mon(9, 30))
        assertEquals(BlockStatus.Free(mon(10)), s)
        assertFalse(BlockRules.shouldBlock(youtube, school, mon(9, 30)))
    }

    @Test fun `внутри интервала — блокировка до его конца`() {
        assertEquals(BlockStatus.Blocked(mon(14), bySchedule = true, byTimer = false), BlockRules.status(school, mon(11)))
        assertTrue(BlockRules.shouldBlock(youtube, school, mon(11)))
    }

    @Test fun `начало интервала включается, конец нет`() {
        assertTrue(BlockRules.shouldBlock(youtube, school, mon(10, 0)))
        assertFalse(BlockRules.shouldBlock(youtube, school, mon(14, 0)))
    }

    @Test fun `перерыв 14–15 — свободно, следующая блокировка в 15`() {
        assertEquals(BlockStatus.Free(mon(15)), BlockRules.status(school, mon(14, 30)))
    }

    @Test fun `другое приложение не блокируется даже во время блокировки`() {
        assertFalse(BlockRules.shouldBlock("org.telegram.messenger", school, mon(11)))
    }

    @Test fun `в другой день расписание понедельника не действует`() {
        assertFalse(BlockRules.shouldBlock(youtube, school, tue(11)))
    }

    @Test fun `после последнего интервала следующая блокировка — через неделю`() {
        assertEquals(BlockStatus.Free(mon(10).plusWeeks(1)), BlockRules.status(school, mon(21)))
    }

    @Test fun `пустое расписание — свободно и следующей блокировки нет`() {
        val s = BlockRules.status(Settings(blockedPackages = setOf(youtube)), mon(11))
        assertEquals(BlockStatus.Free(null), s)
    }

    @Test fun `соприкасающиеся интервалы склеиваются в одну блокировку`() {
        val settings = school.copy(
            schedule = mapOf(MONDAY to listOf(TimeInterval.of(10, 0, 14, 0), TimeInterval.of(14, 0, 15, 0))),
        )
        assertEquals(mon(15), (BlockRules.status(settings, mon(11)) as BlockStatus.Blocked).until)
    }

    @Test fun `интервал до полуночи склеивается с утренним интервалом следующего дня`() {
        val settings = school.copy(
            schedule = mapOf(
                MONDAY to listOf(TimeInterval.of(22, 0, 24, 0)),
                TUESDAY to listOf(TimeInterval.of(0, 0, 1, 0)),
            ),
        )
        assertEquals(tue(1), (BlockRules.status(settings, mon(23)) as BlockStatus.Blocked).until)
    }

    @Test fun `таймер блокирует в свободное время`() {
        val settings = BlockRules.startTimer(school, minutes = 30, now = mon(9, 0))
        assertEquals(
            BlockStatus.Blocked(mon(9, 30), bySchedule = false, byTimer = true),
            BlockRules.status(settings, mon(9, 10)),
        )
    }

    @Test fun `таймер, заходящий в расписание, продлевается до конца интервала`() {
        val settings = BlockRules.startTimer(school, minutes = 90, now = mon(9, 0)) // до 10:30
        assertEquals(mon(14), (BlockRules.status(settings, mon(9, 10)) as BlockStatus.Blocked).until)
    }

    @Test fun `таймер длиннее интервала — блокировка до конца таймера`() {
        val settings = BlockRules.startTimer(school, minutes = 240, now = mon(13, 0)) // до 17:00
        // 13–14 расписание, 14–15 таймер закрывает перерыв, 15–20 расписание → до 20
        assertEquals(mon(20), (BlockRules.status(settings, mon(13, 30)) as BlockStatus.Blocked).until)
    }

    @Test fun `истёкший таймер не блокирует`() {
        val settings = BlockRules.startTimer(school, minutes = 10, now = mon(8, 0))
        assertEquals(BlockStatus.Free(mon(10)), BlockRules.status(settings, mon(8, 15)))
    }

    @Test fun `остановка таймера`() {
        val settings = BlockRules.stopTimer(BlockRules.startTimer(school, 60, mon(8, 0)))
        assertNull(settings.timerEndsAt)
        assertFalse(BlockRules.shouldBlock(youtube, settings, mon(8, 30)))
    }

    @Test fun `следующая смена состояния`() {
        assertEquals(mon(14), BlockRules.nextChange(school, mon(11)))
        assertEquals(mon(15), BlockRules.nextChange(school, mon(14, 30)))
    }

    @Test fun `пересечения интервалов находятся, соприкосновения — нет`() {
        val a = TimeInterval.of(10, 0, 14, 0)
        val b = TimeInterval.of(13, 0, 15, 0)
        val c = TimeInterval.of(15, 0, 16, 0)
        assertEquals(listOf(a to b), BlockRules.overlaps(listOf(c, b, a)))
        assertTrue(BlockRules.overlaps(listOf(a, c)).isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `интервал с концом раньше начала недопустим`() {
        TimeInterval.of(14, 0, 10, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `интервал через полночь недопустим`() {
        TimeInterval(23 * 60, 25 * 60)
    }

    @Test fun `формат интервала`() {
        assertEquals("09:05–24:00", TimeInterval.of(9, 5, 24, 0).toString())
    }
}
