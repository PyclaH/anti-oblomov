package ru.shashkov.antioblomov.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import ru.shashkov.antioblomov.rules.i18n.Lang
import ru.shashkov.antioblomov.rules.i18n.Strings
import ru.shashkov.antioblomov.rules.i18n.stringsFor

/**
 * Тексты текущего языка для экранов (v1.2): `val s = LocalStrings.current`, затем `s.home.…`.
 * Раздаёт MainActivity; при смене языка всё дерево перерисовывается.
 */
val LocalStrings = staticCompositionLocalOf<Strings> { stringsFor(Lang.RU) }

/** Сменить язык: сохраняет выбор в настройках и перерисовывает интерфейс. */
val LocalSetLanguage = staticCompositionLocalOf<(Lang) -> Unit> { {} }
