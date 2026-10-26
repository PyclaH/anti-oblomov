package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.shashkov.antioblomov.rules.i18n.Lang
import ru.shashkov.antioblomov.rules.i18n.SystemNames

class SystemNamesTest {
    @Test
    fun `same language - nothing changes`() {
        val t = "Найдите «Анти-Обломов: блокировка» — бывает в разделе «Скачанные приложения»"
        assertEquals(t, SystemNames.fix(t, Lang.RU, "Анти-Обломов", "Анти-Обломов: блокировка"))
    }

    @Test
    fun `english app on russian phone shows russian system names`() {
        assertEquals(
            "Find “Анти-Обломов: блокировка” — usually under “Downloaded apps”",
            SystemNames.fix("Find “Anti-Oblomov: blocking” — usually under “Downloaded apps”", Lang.EN, "Анти-Обломов", "Анти-Обломов: блокировка"),
        )
        assertEquals(
            "Settings → Apps → Анти-Обломов",
            SystemNames.fix("Settings → Apps → Anti-Oblomov", Lang.EN, "Анти-Обломов", "Анти-Обломов: блокировка"),
        )
    }

    @Test
    fun `declined forms are left alone`() {
        val t = "разрешить «Анти-Обломову»"
        assertEquals(t, SystemNames.fix(t, Lang.RU, "Anti-Oblomov", "Anti-Oblomov: blocking"))
    }
}
