package ru.shashkov.antioblomov.data

import android.content.Context
import ru.shashkov.antioblomov.rules.StatsChoice

/**
 * Какие приложения считать в «Было / стало» (StatsChoice). Нужен только интерфейсу —
 * служба собирает статистику по всем приложениям, — поэтому хватает SharedPreferences.
 */
class StatsChoiceStore(context: Context) {

    private val prefs = context.getSharedPreferences("stats_apps", Context.MODE_PRIVATE)

    fun read() = StatsChoice(
        all = prefs.getBoolean(KEY_ALL, false),
        chosen = prefs.getStringSet(KEY_CHOSEN, null)?.toSet(),
    )

    fun write(choice: StatsChoice) {
        prefs.edit().apply {
            putBoolean(KEY_ALL, choice.all)
            if (choice.chosen == null) remove(KEY_CHOSEN) else putStringSet(KEY_CHOSEN, choice.chosen)
        }.apply()
    }

    private companion object {
        const val KEY_ALL = "all"
        const val KEY_CHOSEN = "chosen"
    }
}
