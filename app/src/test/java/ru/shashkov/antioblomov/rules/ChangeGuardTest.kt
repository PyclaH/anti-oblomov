package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.ZoneId
import java.time.ZonedDateTime

/** Когда перед изменением настроек нужна пауза 15 секунд: только ослабление во время блокировки. */
class ChangeGuardTest {

    private val zone = ZoneId.of("Asia/Irkutsk")
    /** 26.10.2026 — понедельник. */
    private fun at(h: Int, m: Int = 0) = ZonedDateTime.of(2026, 10, 26, h, m, 0, 0, zone)

    private val morning = TimeInterval.of(10, 0, 14, 0)
    private val settings = Settings(
        schedule = mapOf(MONDAY to listOf(morning), TUESDAY to listOf(morning)),
        blockedPackages = setOf("com.google.android.youtube"),
    )

    @Test fun `во время блокировки правка сегодняшнего расписания — пауза`() {
        val after = ScheduleEditor.remove(settings, MONDAY, morning)
        assertTrue(ChangeGuard.needsWait(settings, after, at(11)))
    }

    @Test fun `во время блокировки правка другого дня — без паузы`() {
        val after = ScheduleEditor.remove(settings, TUESDAY, morning)
        assertFalse(ChangeGuard.needsWait(settings, after, at(11)))
    }

    @Test fun `во время блокировки удаление приложения из списка — пауза`() {
        val after = settings.copy(blockedPackages = emptySet())
        assertTrue(ChangeGuard.needsWait(settings, after, at(11)))
    }

    @Test fun `добавление приложения — без паузы`() {
        val after = settings.copy(blockedPackages = settings.blockedPackages + "com.vkontakte.android")
        assertFalse(ChangeGuard.needsWait(settings, after, at(11)))
    }

    @Test fun `новый интервал на сегодня — без паузы`() {
        val after = ScheduleEditor.put(settings, MONDAY, TimeInterval.of(18, 0, 20, 0))
        assertFalse(ChangeGuard.needsWait(settings, after, at(11)))
    }

    @Test fun `удлинение интервала — без паузы`() {
        val after = ScheduleEditor.put(settings, MONDAY, TimeInterval.of(10, 0, 16, 0), old = morning)
        assertFalse(ChangeGuard.needsWait(settings, after, at(11)))
    }

    @Test fun `укорачивание интервала — пауза`() {
        val after = ScheduleEditor.put(settings, MONDAY, TimeInterval.of(10, 0, 13, 0), old = morning)
        assertTrue(ChangeGuard.needsWait(settings, after, at(11)))
    }

    @Test fun `сдвиг интервала, после которого часть оставшегося времени свободна, — пауза`() {
        val after = ScheduleEditor.put(settings, MONDAY, TimeInterval.of(12, 0, 16, 0), old = morning)
        assertTrue(ChangeGuard.needsWait(settings, after, at(11)))
    }

    @Test fun `удаление уже прошедшего интервала — без паузы`() {
        val withEarly = ScheduleEditor.put(settings, MONDAY, TimeInterval.of(7, 0, 8, 0))
        val after = ScheduleEditor.remove(withEarly, MONDAY, TimeInterval.of(7, 0, 8, 0))
        assertFalse(ChangeGuard.needsWait(withEarly, after, at(11)))
    }

    @Test fun `в свободное время — без паузы`() {
        val after = ScheduleEditor.remove(settings, MONDAY, morning).copy(blockedPackages = emptySet())
        assertFalse(ChangeGuard.needsWait(settings, after, at(15)))
    }

    @Test fun `блокировка по таймеру тоже считается`() {
        val timer = BlockRules.startTimer(settings, 30, at(15))
        val after = timer.copy(blockedPackages = emptySet())
        assertTrue(ChangeGuard.needsWait(timer, after, at(15, 10)))
    }

    @Test fun `ничего не поменялось — без паузы`() {
        assertFalse(ChangeGuard.needsWait(settings, settings, at(11)))
    }

    // ---------- таймер ----------

    @Test fun `остановка идущего таймера — пауза`() {
        val running = TimerRules.start(settings, 60, at(15))
        assertTrue(ChangeGuard.needsWait(running, TimerRules.stop(running), at(15, 10)))
    }

    @Test fun `запуск таймера — без паузы`() {
        assertFalse(ChangeGuard.needsWait(settings, TimerRules.start(settings, 60, at(11)), at(11)))
    }

    @Test fun `продление таймера — без паузы`() {
        val running = TimerRules.start(settings, 60, at(15))
        assertFalse(ChangeGuard.needsWait(running, TimerRules.extend(running, 30, at(15, 10)), at(15, 10)))
    }

    @Test fun `сброс уже кончившегося таймера — без паузы`() {
        val ended = TimerRules.start(settings, 60, at(15))
        assertFalse(ChangeGuard.needsWait(ended, TimerRules.stop(ended), at(17)))
    }
}
