package ru.shashkov.antioblomov.data

import android.content.Context
import java.time.LocalDate

/**
 * Что пользователь отметил в мастере настройки. Только для процесса интерфейса,
 * поэтому хватает обычных SharedPreferences (служба блокировки это не читает).
 */
class OnboardingStore(context: Context) {

    private val prefs = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    /** Мастер пройден или закрыт — при запуске больше не показываем. */
    var completed: Boolean
        get() = prefs.getBoolean(KEY_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_COMPLETED, value).apply()

    /** «Я включил автозапуск» — из приложения это не проверить. */
    var autostartConfirmed: Boolean
        get() = prefs.getBoolean(KEY_AUTOSTART, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTOSTART, value).apply()

    /** «Я сделал» для настройки батареи оболочки (на Xiaomi — «Контроль активности»). */
    var extraBatteryConfirmed: Boolean
        get() = prefs.getBoolean(KEY_EXTRA_BATTERY, false)
        set(value) = prefs.edit().putBoolean(KEY_EXTRA_BATTERY, value).apply()

    /** Карточка «Поблагодарить автора»: когда пользователь в последний раз на неё ответил. */
    var thankAnsweredAt: LocalDate?
        get() = prefs.getString(KEY_THANK_AT, null)?.let(LocalDate::parse)
        set(value) = prefs.edit().putString(KEY_THANK_AT, value?.toString()).apply()

    /** …и на какой вехе (последний день вехи) — ту же веху второй раз не предлагаем. */
    var thankMilestone: LocalDate?
        get() = prefs.getString(KEY_THANK_MILESTONE, null)?.let(LocalDate::parse)
        set(value) = prefs.edit().putString(KEY_THANK_MILESTONE, value?.toString()).apply()

    private companion object {
        const val KEY_THANK_AT = "thank_answered_at"
        const val KEY_THANK_MILESTONE = "thank_milestone"
        const val KEY_COMPLETED = "completed"
        const val KEY_AUTOSTART = "autostart_confirmed"
        const val KEY_EXTRA_BATTERY = "extra_battery_confirmed"
    }
}
