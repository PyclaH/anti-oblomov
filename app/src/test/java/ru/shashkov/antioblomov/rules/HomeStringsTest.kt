package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.shashkov.antioblomov.rules.i18n.HomeEn
import ru.shashkov.antioblomov.rules.i18n.HomeStrings
import ru.shashkov.antioblomov.rules.i18n.HomeTr
import ru.shashkov.antioblomov.rules.i18n.Lang
import ru.shashkov.antioblomov.rules.i18n.CommonEn
import ru.shashkov.antioblomov.rules.i18n.stringsFor
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** Тексты главной на всех языках (v1.2): ничего не пропущено, подстановки на месте. */
class HomeStringsTest {

    private fun texts(h: HomeStrings): List<String> = listOf(
        h.appName, h.serviceDownShort, h.serviceDownWarning, h.noAppsWarning,
        h.freeTime, h.blockedUntil("14:00"), h.bySchedule, h.byTimer, h.byScheduleAndTimer, h.noBlocksScheduled,
        h.nextBlock("", "16:00", false), h.nextBlock("x", "16:00", true), h.nextBlock("Чт", "16:00", false),
        h.ringBlocked, h.ringFree, h.untilEnd, h.nextBlockLabel, h.nextShort,
        h.statsTitle, h.collapseAppList, h.statsAppsTitle, h.accessExplain, h.enableAccess, h.collecting,
        h.chooseAppsHint, h.tileBefore, h.tileAfter, h.tileMore, h.tileSaved,
        h.totalSaved("5", null), h.totalSaved("5", "1"), h.howWeCount, h.collapse, h.details,
        h.beforeExplain("3", "20.10"), h.noHistory("20.10"), h.afterFirstDay, h.afterExplain("3"),
        h.screenBefore, h.screenAfter, h.milestoneOverline("x"),
        h.milestoneGood("7", "10", "70"), h.milestoneBad("7", "10"),
        h.appsAllExceptSystem, h.appsNone, h.appsAsBlockList(3), h.appsChosen(3), h.activateAll, h.activateAllSub,
        h.searchApps, h.clearSearch, h.loadingApps, h.nothingChosen, h.nothingFound,
        h.noBrowser, h.thankAuthor, h.youSave("5"), h.freeNoAds, h.support,
        h.vpnTitle, h.vpnBody, h.vpnSettings, h.vpnOpenAnyway,
        h.system, h.serviceNotWorking, h.serviceTitle, h.serviceRunning, h.serviceRunningBody,
        h.accessibilitySettings, h.serviceStopped, h.serviceStoppedBody("L", null), h.serviceStoppedBody("L", "A"),
        h.accessibilityShort, h.appSettings, h.serviceOff, h.serviceOffBody("L"), h.openSettings,
    ) + h.milestoneTitles

    @Test fun everyLanguageHasAllTexts() {
        for (lang in Lang.entries) {
            val h = stringsFor(lang).home
            texts(h).forEach { assertTrue("$lang: пустая строка", it.isNotBlank()) }
            assertEquals("$lang: 14 вех", 14, h.milestoneTitles.size)
            assertEquals("$lang: вехи не повторяются", 14, h.milestoneTitles.toSet().size)
        }
    }

    @Test fun substitutionsAreUsed() {
        for (lang in Lang.entries) {
            val h = stringsFor(lang).home
            assertTrue(lang.name, h.blockedUntil("14:00").contains("14:00"))
            assertTrue(lang.name, h.nextBlock("", "16:00", false).contains("16:00"))
            assertTrue(lang.name, h.nextBlock("Thu", "16:00", false).contains("Thu"))
            assertTrue(lang.name, h.totalSaved("5 h", "1 d").contains("5 h") && h.totalSaved("5 h", "1 d").contains("1 d"))
            assertTrue(lang.name, h.milestoneGood("7d", "10m", "70m").let { "7d" in it && "10m" in it && "70m" in it })
            assertTrue(lang.name, h.milestoneBad("7d", "10m").let { "7d" in it && "10m" in it })
            assertTrue(lang.name, "L" in h.serviceOffBody("L") && "AUTO" in h.serviceStoppedBody("L", "AUTO"))
            assertFalse(lang.name, "AUTO" in h.serviceStoppedBody("L", null))
            assertTrue(lang.name, h.appsChosen(5).contains("5") && h.appsAsBlockList(5).contains("5"))
            assertTrue(lang.name, h.youSave("40 m").contains("40 m"))
        }
    }

    private val zone = ZoneId.of("Asia/Irkutsk")
    /** 26.10.2026 — понедельник. */
    private fun at(h: Int) = ZonedDateTime.of(2026, 10, 26, h, 0, 0, 0, zone)

    @Test fun ringInEnglish() {
        val now = at(15)
        val free = BlockStatus.Free(nextBlockAt = at(16))
        assertEquals(RingText("FREE TIME", "16:00", "next block"), HomeStatusText.ring(free, now, HomeEn, CommonEn))
        val tomorrow = BlockStatus.Free(nextBlockAt = at(16).plusDays(1))
        assertEquals("next · tomorrow", HomeStatusText.ring(tomorrow, now, HomeEn, CommonEn).sub)
        assertEquals("next block tomorrow at 16:00", HomeStatusText.detail(tomorrow, now, HomeEn, CommonEn))
        val thursday = BlockStatus.Free(nextBlockAt = at(10).with(DayOfWeek.THURSDAY))
        assertEquals("next block on Thu at 10:00", HomeStatusText.detail(thursday, now, HomeEn, CommonEn))
    }

    @Test fun milestoneTitlesFollowLanguage() {
        val start = LocalDate.of(2026, 1, 1)
        assertEquals("One week", Savings.milestones(start, HomeEn).first().first)
        assertEquals("One year" to start.plusMonths(12), Savings.milestones(start, HomeEn).last())
        assertEquals("Неделя", Savings.milestones(start).first().first)
        assertEquals("Год", Savings.milestones(start).last().first)
    }

    @Test fun turkishUppercaseKeepsDottedI() {
        assertEquals("DÖNÜM NOKTASI · İKİ HAFTA", HomeTr.milestoneOverline("İki hafta"))
    }
}
