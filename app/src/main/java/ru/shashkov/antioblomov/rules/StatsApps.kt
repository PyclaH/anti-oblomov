package ru.shashkov.antioblomov.rules

/**
 * По каким приложениям считать «Было / стало» (решение от 25.10.2026) — отдельно от списка блокировки.
 * @property all «Активировать все»: все несистемные приложения
 * @property chosen свой список; null — ещё не меняли, берём список блокировки
 */
data class StatsChoice(val all: Boolean = false, val chosen: Set<String>? = null)

/** Логика выпадающего списка «Приложения для статистики» — без Android, проверяется StatsAppsTest. */
object StatsApps {

    /** Итоговый набор приложений для статистики. [nonSystem] — с иконкой на рабочем столе, кроме защищённых. */
    fun tracked(choice: StatsChoice, blocked: Set<String>, nonSystem: Set<String>): Set<String> = when {
        choice.all -> nonSystem
        else -> choice.chosen ?: blocked
    }

    /**
     * Что показать в коротком списке: включённые приложения всегда, а при поиске — ещё и найденные.
     * Включённые — сверху, внутри групп — по алфавиту. Найденных не больше [maxMatches].
     */
    fun visible(
        apps: List<AppEntry>,
        protected: Set<String>,
        tracked: Set<String>,
        query: String,
        maxMatches: Int = 30,
    ): List<AppEntry> {
        val sorted = AppList.sections(apps, protected, "").let { it.blocked + it.distracting + it.others }
        val active = sorted.filter { it.packageName in tracked }
        if (query.isBlank()) return active
        val found = AppList.sections(apps, protected, query).let { it.blocked + it.distracting + it.others }
            .filter { it.packageName !in tracked }
            .sortedWith(compareBy { sorted.indexOf(it) })
            .take(maxMatches)
        return active + found
    }

    /** Включить или выключить приложение в своём списке (с этого момента список свой, не блокировки). */
    fun toggle(choice: StatsChoice, current: Set<String>, packageName: String, on: Boolean): StatsChoice =
        choice.copy(chosen = if (on) current + packageName else current - packageName)
}
