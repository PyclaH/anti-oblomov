package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SavingsTest {

    private val yt = "com.google.android.youtube"
    private val vk = "com.vkontakte.android"
    private val maps = "ru.yandex.maps"
    private val launcher = "com.miui.home"
    private val start = LocalDate.of(2026, 10, 26)
    private fun day(offset: Long) = start.plusDays(offset)
    private fun min(m: Int) = m * 60

    /** До установки: YouTube 3 ч, ВК 1 ч в день; карты 30 мин — не в списке. */
    private val beforeDay = mapOf(yt to min(180), vk to min(60), maps to min(30), launcher to min(20))
    private val before = (1L..7L).associate { day(-it) to beforeDay }
    private val afterDay = mapOf(yt to min(60), vk to min(30), maps to min(40), launcher to min(20))
    private fun history(afterDays: Int, before: Map<LocalDate, DayUsage> = this.before) =
        UsageHistory(start, before, (1..afterDays).associate { day(it.toLong()) to afterDay })

    private fun report(afterDays: Int, today: LocalDate = day(afterDays + 1L), todayUsage: DayUsage = mapOf(yt to min(20))) =
        Savings.report(history(afterDays), today, todayUsage, blocked = setOf(yt, vk), notScreenTime = setOf(launcher))

    @Test fun `в первый день — до установки, сегодня и сэкономлено сегодня`() {
        val r = Savings.report(history(0), start, mapOf(yt to min(20)), setOf(yt, vk), setOf(launcher))
        assertEquals(7, r.beforeDays)
        assertEquals(240, r.beforeAvgMinutes)
        assertEquals(20, r.todayMinutes)
        assertEquals(220, r.savedTodayMinutes)
        assertEquals(0, r.afterDays)
        assertNull(r.afterAvgMinutes)
        assertNull(r.totalSavedMinutes)
        assertNull(r.milestone)
    }

    @Test fun `меньше 7 дней — просто среднее по доступным`() {
        val r = report(3)
        assertEquals(3, r.afterDays)
        assertEquals(90, r.afterAvgMinutes)
        assertEquals(150, r.savedPerDayMinutes)
        assertEquals(450, r.totalSavedMinutes)
        assertNull("веха — только с 7 дней", r.milestone)
    }

    @Test fun `история до установки короче недели`() {
        val short = before.filterKeys { it >= day(-2) }
        val r = Savings.report(history(1, short), day(2), emptyMap(), setOf(yt, vk), setOf(launcher))
        assertEquals(2, r.beforeDays)
        assertEquals(240, r.beforeAvgMinutes)
    }

    @Test fun `без истории до — сравнивать не с чем`() {
        val r = Savings.report(history(3, emptyMap()), day(4), emptyMap(), setOf(yt, vk), setOf(launcher))
        assertNull(r.beforeAvgMinutes)
        assertNull(r.savedTodayMinutes)
        assertNull(r.savedPerDayMinutes)
        assertEquals(90, r.afterAvgMinutes)
    }

    @Test fun `день установки и сегодняшний не входят в после`() {
        val h = UsageHistory(start, before, mapOf(start to afterDay, day(1) to afterDay, day(2) to afterDay))
        val r = Savings.report(h, day(2), emptyMap(), setOf(yt, vk), setOf(launcher))
        assertEquals(1, r.afterDays)
    }

    @Test fun `считаем по текущему списку блокировки`() {
        val r = Savings.report(history(3), day(4), emptyMap(), setOf(yt), setOf(launcher))
        assertEquals(180, r.beforeAvgMinutes)
        assertEquals(60, r.afterAvgMinutes)
    }

    @Test fun `экранное время без рабочего стола`() {
        val r = report(3, todayUsage = mapOf(yt to min(20), launcher to min(5)))
        assertEquals(270, r.screenBeforeAvgMinutes)
        assertEquals(130, r.screenAfterAvgMinutes)
        assertEquals(20, r.screenTodayMinutes)
    }

    @Test fun `отрицательная экономия не скрывается`() {
        val r = Savings.report(history(0), start, mapOf(yt to min(300)), setOf(yt, vk), setOf(launcher))
        assertEquals(-60, r.savedTodayMinutes)
    }

    @Test fun `веха неделя — когда седьмой день закончился`() {
        assertNull(report(7, today = day(7)).milestone)
        val m = report(7, today = day(8)).milestone
        assertNotNull(m)
        assertEquals("Неделя", m!!.title)
        assertEquals(7, m.days)
        assertEquals(150, m.savedPerDayMinutes)
        assertEquals(1050, m.totalSavedMinutes)
    }

    @Test fun `веха две недели и месяц`() {
        assertEquals("Две недели", report(14, today = day(15)).milestone!!.title)
        assertEquals("Месяц", report(31, today = day(32)).milestone!!.title)
    }

    @Test fun `вехи до года`() {
        val all = Savings.milestones(start)
        assertEquals(14, all.size)
        assertEquals("Год" to start.plusYears(1), all.last())
        assertEquals("Полгода" to start.plusMonths(6), all[7])
    }

    @Test fun `учебные дни`() {
        assertEquals(7, Savings.schoolDays(63 * 60))
        assertEquals("1 учебный день", UsageText.schoolDays(1))
        assertEquals("3 учебных дня", UsageText.schoolDays(3))
        assertEquals("11 учебных дней", UsageText.schoolDays(11))
        assertEquals("21 учебный день", UsageText.schoolDays(21))
        assertEquals("−20 мин", UsageText.saved(-20))
    }

    // ---------- карточка «Поблагодарить автора» ----------

    private val week = Milestone("Неделя", day(7), 7, 90, 150, 1050)

    @Test fun `благодарность на вехе`() {
        assertTrue(ThankRules.shouldShow("https://sponsr.ru/x/", week, null, null, day(8)))
    }

    @Test fun `без ссылки, вехи или экономии — не показываем`() {
        assertFalse(ThankRules.shouldShow("", week, null, null, day(8)))
        assertFalse(ThankRules.shouldShow("u", null, null, null, day(8)))
        assertFalse(ThankRules.shouldShow("u", week.copy(savedPerDayMinutes = 10), null, null, day(8)))
    }

    @Test fun `ту же веху второй раз не показываем`() {
        assertFalse(ThankRules.shouldShow("u", week, day(7), day(8), day(40)))
    }

    @Test fun `не чаще раза в 30 дней`() {
        val twoWeeks = week.copy(title = "Две недели", date = day(14))
        assertFalse(ThankRules.shouldShow("u", twoWeeks, day(7), day(8), day(15)))
        val month = week.copy(title = "Месяц", date = start.plusMonths(1))
        // веха «Месяц» наступила, но с прошлого показа меньше 30 дней — ждём
        assertFalse(ThankRules.shouldShow("u", month, day(7), day(8), start.plusMonths(1).plusDays(2)))
        // 30 дней прошло — показываем ту же веху
        assertTrue(ThankRules.shouldShow("u", month, day(7), day(8), day(38)))
    }
}
