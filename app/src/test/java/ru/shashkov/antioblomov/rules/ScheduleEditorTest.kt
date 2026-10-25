package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY

/**
 * Проверки логики экрана «Расписание». Запуск: правая кнопка по файлу → Run 'ScheduleEditorTest'.
 */
class ScheduleEditorTest {

    private val morning = TimeInterval.of(10, 0, 14, 0)
    private val evening = TimeInterval.of(15, 0, 20, 0)
    private val monday = Settings(schedule = mapOf(MONDAY to listOf(morning, evening)))

    private fun m(h: Int, min: Int = 0) = h * 60 + min

    // ---------- проверка интервала ----------

    @Test fun `корректный интервал в свободное время`() {
        assertEquals(IntervalCheck.Ok(TimeInterval.of(20, 0, 22, 0)), ScheduleEditor.check(m(20), m(22), monday.intervalsOn(MONDAY)))
    }

    @Test fun `конец раньше начала — ошибка`() {
        assertTrue(ScheduleEditor.check(m(14), m(10), emptyList()) is IntervalCheck.Error)
    }

    @Test fun `конец равен началу — ошибка`() {
        assertTrue(ScheduleEditor.check(m(10), m(10), emptyList()) is IntervalCheck.Error)
    }

    @Test fun `конец 00_00 означает до полуночи`() {
        assertEquals(IntervalCheck.Ok(TimeInterval(m(22), 24 * 60)), ScheduleEditor.check(m(22), 0, emptyList()))
    }

    @Test fun `пересечение — ошибка с указанием интервала`() {
        val r = ScheduleEditor.check(m(13), m(16), monday.intervalsOn(MONDAY))
        assertEquals(IntervalCheck.Error("Пересекается с интервалом 10:00–14:00"), r)
    }

    @Test fun `интервал внутри другого — ошибка`() {
        assertTrue(ScheduleEditor.check(m(11), m(12), monday.intervalsOn(MONDAY)) is IntervalCheck.Error)
    }

    @Test fun `соприкосновение — не пересечение`() {
        assertTrue(ScheduleEditor.check(m(14), m(15), monday.intervalsOn(MONDAY)) is IntervalCheck.Ok)
    }

    @Test fun `при правке интервал не конфликтует сам с собой`() {
        val r = ScheduleEditor.check(m(9), m(14), monday.intervalsOn(MONDAY), replacing = morning)
        assertEquals(IntervalCheck.Ok(TimeInterval.of(9, 0, 14, 0)), r)
    }

    // ---------- добавление, правка, удаление ----------

    @Test fun `добавление сортирует по времени`() {
        val s = ScheduleEditor.put(Settings(), MONDAY, evening).let { ScheduleEditor.put(it, MONDAY, morning) }
        assertEquals(listOf(morning, evening), s.schedule[MONDAY])
    }

    @Test fun `правка заменяет старый интервал`() {
        val new = TimeInterval.of(9, 0, 13, 0)
        val s = ScheduleEditor.put(monday, MONDAY, new, old = morning)
        assertEquals(listOf(new, evening), s.schedule[MONDAY])
    }

    @Test fun `удаление последнего интервала убирает день из расписания`() {
        val s = ScheduleEditor.remove(ScheduleEditor.remove(monday, MONDAY, morning), MONDAY, evening)
        assertTrue(MONDAY !in s.schedule)
    }

    // ---------- перерывы ----------

    @Test fun `перерыв между интервалами`() {
        assertEquals(listOf(TimeInterval.of(14, 0, 15, 0)), ScheduleEditor.breaks(listOf(evening, morning)))
    }

    @Test fun `у соприкасающихся интервалов перерыва нет`() {
        assertTrue(ScheduleEditor.breaks(listOf(morning, TimeInterval.of(14, 0, 15, 0))).isEmpty())
    }

    // ---------- копирование на будни ----------

    @Test fun `копирование на будни`() {
        val s = ScheduleEditor.copyToWeekdays(monday, MONDAY)
        for (day in ScheduleEditor.WEEKDAYS) assertEquals(listOf(morning, evening), s.intervalsOn(day))
    }

    @Test fun `копирование не трогает выходные`() {
        val sat = listOf(TimeInterval.of(12, 0, 13, 0))
        val s = ScheduleEditor.copyToWeekdays(monday.copy(schedule = monday.schedule + (SATURDAY to sat)), MONDAY)
        assertEquals(sat, s.intervalsOn(SATURDAY))
    }

    @Test fun `копировать можно и с выходного дня`() {
        val sat = listOf(TimeInterval.of(12, 0, 13, 0))
        val s = ScheduleEditor.copyToWeekdays(Settings(schedule = mapOf(SATURDAY to sat)), SATURDAY)
        assertEquals(sat, s.intervalsOn(WEDNESDAY))
    }

    @Test fun `предупреждаем только о буднях с другим расписанием`() {
        val settings = monday.copy(
            schedule = monday.schedule +
                (TUESDAY to listOf(morning, evening)) +     // такое же — не предупреждаем
                (FRIDAY to listOf(TimeInterval.of(8, 0, 9, 0))), // другое — предупреждаем
        )
        assertEquals(listOf(FRIDAY), ScheduleEditor.weekdaysToOverwrite(settings, MONDAY))
    }

    // ---------- предложение нового интервала ----------

    @Test fun `в пустой день предлагаем 09_00–13_00`() {
        assertEquals(m(9) to m(13), ScheduleEditor.suggestNew(emptyList()))
    }

    @Test fun `новый интервал начинается после последнего`() {
        assertEquals(m(20) to m(21), ScheduleEditor.suggestNew(listOf(morning, evening)))
    }

    @Test fun `предложение не заходит за полночь`() {
        assertEquals(m(23, 30) to 24 * 60, ScheduleEditor.suggestNew(listOf(TimeInterval.of(20, 0, 23, 30))))
    }

    @Test fun `день занят до полуночи — предложить нечего`() {
        assertNull(ScheduleEditor.suggestNew(listOf(TimeInterval(m(20), 24 * 60))))
    }
}
