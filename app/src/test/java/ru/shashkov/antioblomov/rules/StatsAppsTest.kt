package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsAppsTest {

    private val yt = AppEntry("com.google.android.youtube", "YouTube", distracting = true)
    private val vk = AppEntry("com.vkontakte.android", "ВКонтакте", distracting = true)
    private val tg = AppEntry("org.telegram.messenger", "Telegram", distracting = true)
    private val calc = AppEntry("com.miui.calculator", "Калькулятор", distracting = false)
    private val settings = AppEntry("com.android.settings", "Настройки", distracting = false)
    private val apps = listOf(yt, vk, tg, calc, settings)
    private val protected = setOf(settings.packageName)
    private val nonSystem = apps.map { it.packageName }.toSet() - protected

    @Test fun `пока не меняли — как список блокировки`() {
        assertEquals(setOf(yt.packageName), StatsApps.tracked(StatsChoice(), setOf(yt.packageName), nonSystem))
    }

    @Test fun `свой список не зависит от блокировки`() {
        val choice = StatsChoice(chosen = setOf(vk.packageName))
        assertEquals(setOf(vk.packageName), StatsApps.tracked(choice, setOf(yt.packageName), nonSystem))
    }

    @Test fun `активировать все — все несистемные`() {
        val choice = StatsChoice(all = true, chosen = setOf(vk.packageName))
        assertEquals(nonSystem, StatsApps.tracked(choice, emptySet(), nonSystem))
    }

    @Test fun `без поиска — только включённые`() {
        assertEquals(listOf(vk), StatsApps.visible(apps, protected, setOf(vk.packageName), ""))
    }

    @Test fun `при поиске — включённые и найденные, системных нет`() {
        val shown = StatsApps.visible(apps, protected, setOf(vk.packageName), "т")
        // русская «т»: «Калькулятор» найден, «Telegram» (латиница) — нет; «Настройки» защищённые — не показываем
        assertEquals(listOf(vk, calc), shown)
    }

    @Test fun `найденных не больше лимита`() {
        assertEquals(listOf(tg), StatsApps.visible(apps, protected, emptySet(), "e", maxMatches = 1))
    }

    @Test fun `переключение делает список своим`() {
        val c = StatsApps.toggle(StatsChoice(), setOf(yt.packageName), vk.packageName, on = true)
        assertEquals(setOf(yt.packageName, vk.packageName), c.chosen)
        assertEquals(emptySet<String>(), StatsApps.toggle(c, c.chosen!!, yt.packageName, false).chosen!! - vk.packageName)
    }
}
