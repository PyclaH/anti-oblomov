package ru.shashkov.antioblomov.data

import android.content.Context

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

    private companion object {
        const val KEY_COMPLETED = "completed"
        const val KEY_AUTOSTART = "autostart_confirmed"
        const val KEY_EXTRA_BATTERY = "extra_battery_confirmed"
    }
}
