package ru.shashkov.antioblomov.rules

/** Что настроено на телефоне — собирается в MainActivity при каждом возврате в приложение. */
data class SetupState(
    val brand: PhoneBrand,
    val serviceRunning: Boolean,
    val notificationsAllowed: Boolean,
    /** Стандартная настройка Android «Батарея: без ограничений». */
    val batteryUnrestricted: Boolean,
    /** Пользователь отметил, что сделал настройку батареи оболочки (её не проверить из приложения). */
    val extraBatteryConfirmed: Boolean,
    /** Пользователь отметил, что включил автозапуск (его не проверить из приложения). */
    val autostartConfirmed: Boolean,
    /** Выдан доступ к истории использования — для «Было / стало» (шаг 7). */
    val usageAccess: Boolean,
)

/** Пункт настройки телефона. */
enum class SetupItem(val title: String) {
    SERVICE("Служба блокировки"),
    NOTIFICATIONS("Уведомления таймера"),
    BATTERY("Батарея без ограничений"),
    AUTOSTART("Автозапуск"),
    USAGE("Доступ к статистике"),
}

/**
 * Шаги мастера и пункты «Настройка телефона» на главной (FR-08).
 * Без Android — проверяется юнит-тестами (SetupChecklistTest).
 */
object SetupChecklist {

    /** Пункты для этой марки: у «прочих» Android автозапуска нет. */
    fun items(brand: PhoneBrand): List<SetupItem> =
        SetupItem.entries.filter { it != SetupItem.AUTOSTART || SetupGuide.autostart(brand) != null }

    fun isDone(item: SetupItem, state: SetupState): Boolean = when (item) {
        SetupItem.SERVICE -> state.serviceRunning
        SetupItem.NOTIFICATIONS -> state.notificationsAllowed
        SetupItem.BATTERY -> state.batteryUnrestricted &&
            (SetupGuide.extraBattery(state.brand) == null || state.extraBatteryConfirmed)
        SetupItem.AUTOSTART -> state.autostartConfirmed
        SetupItem.USAGE -> state.usageAccess
    }

    /** Что ещё не сделано — показываем на главной. */
    fun missing(state: SetupState): List<SetupItem> = items(state.brand).filterNot { isDone(it, state) }

    /** Подпись к названию пункта для главной: что именно нужно, если не сделано. */
    fun autostartTitle(brand: PhoneBrand): String = SetupGuide.autostart(brand)?.title ?: SetupItem.AUTOSTART.title
}

/** Страницы мастера по порядку. */
enum class OnboardingPage { WELCOME, SERVICE, NOTIFICATIONS, BATTERY, AUTOSTART, USAGE, FINISH }

object Onboarding {
    fun pages(brand: PhoneBrand): List<OnboardingPage> =
        OnboardingPage.entries.filter { it != OnboardingPage.AUTOSTART || SetupGuide.autostart(brand) != null }
}
