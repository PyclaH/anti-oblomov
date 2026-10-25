package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.ZoneId
import java.time.ZonedDateTime

/** Когда перед изменением настроек нужна пауза 15 секунд. */
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

    @Test fun `во время блокировки изменение списка приложений — пауза`() {
        val after = settings.copy(blockedPackages = emptySet())
        assertTrue(ChangeGuard.needsWait(settings, after, at(11)))
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
}
