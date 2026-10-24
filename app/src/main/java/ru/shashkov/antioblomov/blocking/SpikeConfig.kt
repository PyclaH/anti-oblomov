package ru.shashkov.antioblomov.blocking

/**
 * Настройки пробной блокировки (шаг 1).
 *
 * Пока блокировка включена ВСЕГДА и только для YouTube — без расписания и таймера.
 * Цель шага — проверить, что служба спецвозможностей надёжно работает на HyperOS.
 * На шагах 2–3 этот файл заменят настоящие правила и выбранный пользователем список.
 */
object SpikeConfig {
    const val YOUTUBE = "com.google.android.youtube"

    val BLOCKED_PACKAGES: Set<String> = setOf(YOUTUBE)
}
