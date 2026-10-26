package ru.shashkov.antioblomov.data

import android.content.Context
import ru.shashkov.antioblomov.rules.i18n.Lang
import ru.shashkov.antioblomov.rules.i18n.Strings
import ru.shashkov.antioblomov.rules.i18n.stringsFor
import java.util.Locale

/**
 * Язык интерфейса (v1.2): выбранный в настройках, иначе язык телефона, иначе английский.
 * Хранится в settings.json вместе с остальными настройками — его читает и служба блокировки.
 */
object Language {
    fun current(context: Context): Lang =
        Lang.resolve(SettingsStore(context).read().language, Locale.getDefault().language)

    fun strings(context: Context): Strings = stringsFor(current(context))

    fun set(context: Context, lang: Lang) {
        SettingsStore(context).update { it.copy(language = lang.code) }
    }
}
