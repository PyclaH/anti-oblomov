package ru.shashkov.antioblomov.rules.i18n

/**
 * Названия «Анти-Обломова» в подсказках мастера — как их покажет телефон (v1.2).
 *
 * Android подписывает приложение и службу в своих настройках на языке ТЕЛЕФОНА (res/values-xx),
 * а интерфейс приложения может быть на другом языке. Чтобы подсказка «Найдите „…“» совпадала
 * с тем, что человек видит в списке, названия в кавычках подменяем на системные.
 * Меняем только название целиком в кавычках или после «→», склонённые формы в тексте не трогаем.
 */
object SystemNames {

    fun appName(lang: Lang): String = when (lang) {
        Lang.RU, Lang.KK -> "Анти-Обломов"
        Lang.ZH -> "反奥勃洛莫夫"
        else -> "Anti-Oblomov"
    }

    /** Как в res/values-xx/strings.xml → accessibility_service_label. */
    fun serviceLabel(lang: Lang): String = when (lang) {
        Lang.RU -> "Анти-Обломов: блокировка"
        Lang.EN -> "Anti-Oblomov: blocking"
        Lang.ES -> "Anti-Oblomov: bloqueo"
        Lang.PT -> "Anti-Oblomov: bloqueio"
        Lang.DE -> "Anti-Oblomov: Sperre"
        Lang.TR -> "Anti-Oblomov: engelleme"
        Lang.KK -> "Анти-Обломов: бұғаттау"
        Lang.UZ -> "Anti-Oblomov: bloklash"
        Lang.ZH -> "反奥勃洛莫夫：屏蔽"
    }

    private val QUOTES = listOf("«" to "»", "“" to "”", "„" to "“", "\"" to "\"", "「" to "」")

    fun fix(text: String, lang: Lang, systemAppName: String, systemServiceLabel: String): String {
        var out = text
        val label = serviceLabel(lang)
        val app = appName(lang)
        for ((open, close) in QUOTES) {
            out = out.replace("$open$label$close", "$open$systemServiceLabel$close")
            out = out.replace("$open$app$close", "$open$systemAppName$close")
        }
        // «Настройки → Приложения → Анти-Обломов» — без кавычек, в конце пути
        out = Regex("→ " + Regex.escape(app) + "(?=$|\\s|[,.;:→])").replace(out) { "→ $systemAppName" }
        return out
    }
}
