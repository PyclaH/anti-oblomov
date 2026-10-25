package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Проверки логики экрана «Приложения». Запуск: правая кнопка по файлу → Run 'AppListTest'.
 */
class AppListTest {

    private val youtube = AppEntry("com.google.android.youtube", "YouTube", distracting = true)
    private val tiktok = AppEntry("com.zhiliaoapp.musically", "TikTok", distracting = true)
    private val camera = AppEntry("com.android.camera", "Камера", distracting = false)
    private val calc = AppEntry("com.miui.calculator", "Калькулятор", distracting = false)
    private val notes = AppEntry("com.example.notes", "ёжик-заметки", distracting = false)
    private val settingsApp = AppEntry("com.android.settings", "Настройки", distracting = false)
    private val self = AppEntry("ru.shashkov.antioblomov", "Анти-Обломов", distracting = false)

    private val all = listOf(youtube, camera, tiktok, settingsApp, calc, self, notes)
    private val protected = setOf("com.android.settings", "ru.shashkov.antioblomov")

    @Test fun `защищённые приложения не показываются`() {
        val s = AppList.sections(all, protected, "")
        val shown = (s.distracting + s.others).map { it.packageName }
        assertTrue(settingsApp.packageName !in shown)
        assertTrue(self.packageName !in shown)
    }

    @Test fun `отвлекающие — в отдельном разделе, остальные — во втором`() {
        val s = AppList.sections(all, protected, "")
        assertEquals(listOf(tiktok, youtube), s.distracting)
        // «ёжик» по-русски стоит рядом с «е» — раньше «Калькулятора»
        assertEquals(listOf(notes, calc, camera), s.others)
    }

    @Test fun `сортировка по-русски без учёта регистра, ё рядом с е`() {
        val e = AppEntry("a.e", "Еда", false)
        val yo = AppEntry("a.yo", "ёлка", false)
        val zh = AppEntry("a.zh", "Жук", false)
        assertEquals(listOf(e, yo, zh), AppList.sections(listOf(zh, yo, e), emptySet(), "").others)
    }

    @Test fun `поиск по названию без учёта регистра`() {
        val s = AppList.sections(all, protected, "кам")
        assertEquals(listOf(camera), s.others)
        assertTrue(s.distracting.isEmpty())
    }

    @Test fun `поиск по имени пакета`() {
        assertEquals(listOf(tiktok), AppList.sections(all, protected, "musically").distracting)
    }

    @Test fun `поиск не находит защищённые`() {
        assertTrue(AppList.sections(all, protected, "Настройки").isEmpty)
    }

    @Test fun `пробелы вокруг поиска не мешают`() {
        assertEquals(listOf(youtube), AppList.sections(all, protected, "  you ").distracting)
    }

    @Test fun `счётчик выбранных учитывает только установленные и незащищённые`() {
        val blocked = setOf(youtube.packageName, "com.uninstalled.app", settingsApp.packageName)
        assertEquals(1, AppList.selectedCount(all, protected, blocked))
    }

    @Test fun `включение и выключение блокировки`() {
        val on = AppList.toggle(Settings(), youtube.packageName, block = true, protected = protected)
        assertEquals(setOf(youtube.packageName), on.blockedPackages)
        val off = AppList.toggle(on, youtube.packageName, block = false, protected = protected)
        assertTrue(off.blockedPackages.isEmpty())
    }

    @Test fun `защищённое приложение включить нельзя`() {
        val s = AppList.toggle(Settings(), settingsApp.packageName, block = true, protected = protected)
        assertTrue(s.blockedPackages.isEmpty())
    }

    @Test fun `по умолчанию ничего не выбрано`() {
        assertTrue(Settings().blockedPackages.isEmpty())
    }
}
