package ru.shashkov.antioblomov.ui.theme

import androidx.compose.ui.graphics.Color

// Палитра редизайна «тихий фокус» (план редизайна, 25.10.2026): только тёмная тема,
// один акцент — терракота. Контраст основного текста на фоне — около 17:1,
// второстепенного — около 6:1 (не ниже 4,5:1).

/** Фон: сверху вниз от [Night0] к [Night1]. */
val Night0 = Color(0xFF0B0D12)
val Night1 = Color(0xFF11141B)

/** Карточки, нижнее меню. */
val Surface1 = Color(0xFF161A22)

/** Поля ввода, вложенные блоки, выбранная вкладка. */
val Surface2 = Color(0xFF1E232D)

/** Границы 1 dp, разделители. */
val Line = Color(0xFF2A303C)

/** Основной текст — тёплый белый. */
val TextPrimary = Color(0xFFF2EFE8)

/** Подписи, подсказки. */
val TextMuted = Color(0xFF8E96A4)

/** Акцент: идёт блокировка, главная кнопка. Терракота, светлее для тёмного фона. */
val Accent = Color(0xFFE2683C)

/** Спокойный: свободное время, «сэкономлено», ✓. */
val Calm = Color(0xFF5FB4A6)

/** Тревога: служба не работает, пропущенная настройка. */
val Warn = Color(0xFFF0A04B)
