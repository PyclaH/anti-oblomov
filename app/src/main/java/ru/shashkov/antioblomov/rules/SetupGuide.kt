package ru.shashkov.antioblomov.rules

/** Экран настроек другого приложения: пакет и класс экрана. */
data class SettingsScreen(val packageName: String, val className: String)

/**
 * Инструкция для шага мастера: что нажать и какие экраны пробовать открыть по очереди.
 * Если ни один не откроется — открываем страницу «О приложении» (там тоже обычно всё есть).
 */
data class Guide(
    val title: String,
    val why: String,
    val steps: List<String>,
    val screens: List<SettingsScreen>,
    val buttonText: String,
)

/**
 * Где на разных оболочках разрешить приложению работать в фоне (FR-08, NFR-01).
 *
 * Эти экраны производители не документируют, и от версии к версии они меняются,
 * поэтому у каждой марки — несколько известных вариантов по очереди, а текстовая
 * подсказка остаётся, даже если кнопка открыла не тот экран. Проверено на Poco
 * (HyperOS); остальные марки — по известным путям, проверка на шаге 8.
 */
object SetupGuide {

    /** «Автозапуск» или его аналог. Null — у марки такой настройки нет. */
    fun autostart(brand: PhoneBrand): Guide? = when (brand) {
        PhoneBrand.XIAOMI -> Guide(
            title = "Автозапуск",
            why = "Без него HyperOS и MIUI не запускают службу блокировки после перезагрузки.",
            steps = listOf(
                "Откроется список «Автозапуск» приложения «Безопасность»",
                "Найдите «Анти-Обломов» и включите переключатель",
            ),
            screens = listOf(
                SettingsScreen("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            ),
            buttonText = "Открыть «Автозапуск»",
        )

        PhoneBrand.SAMSUNG -> Guide(
            title = "Не усыплять приложение",
            why = "Samsung усыпляет приложения, которыми давно не пользовались, — служба блокировки перестаёт работать.",
            steps = listOf(
                "Настройки → Обслуживание устройства (или «Батарея») → Ограничения фонового использования",
                "«Приложения, которые никогда не переходят в спящий режим» → «+» → «Анти-Обломов»",
                "Если «Анти-Обломов» есть в списке «Спящие приложения» — уберите оттуда",
            ),
            screens = listOf(
                SettingsScreen("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
                SettingsScreen("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"),
            ),
            buttonText = "Открыть настройки батареи",
        )

        PhoneBrand.OPPO -> Guide(
            title = "Автозапуск",
            why = "ColorOS и Realme UI не запускают службу после перезагрузки без этого разрешения.",
            steps = listOf(
                "Найдите «Анти-Обломов» в списке автозапуска и включите",
                "Если списка нет: Настройки → Приложения → Анти-Обломов → «Использование батареи» → разрешить «Автозапуск» и «Работу в фоне»",
            ),
            screens = listOf(
                SettingsScreen("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                SettingsScreen("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
                SettingsScreen("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
            ),
            buttonText = "Открыть автозапуск",
        )

        PhoneBrand.HUAWEI -> Guide(
            title = "Запуск приложений",
            why = "EMUI сама решает, каким приложениям работать в фоне, и может остановить службу блокировки.",
            steps = listOf(
                "Найдите «Анти-Обломов» и выключите «Управлять автоматически»",
                "В появившемся окне включите все три: «Автозапуск», «Косвенный запуск», «Работа в фоне»",
            ),
            screens = listOf(
                SettingsScreen("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                SettingsScreen("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
                SettingsScreen("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"),
            ),
            buttonText = "Открыть «Запуск приложений»",
        )

        PhoneBrand.HONOR -> Guide(
            title = "Запуск приложений",
            why = "MagicOS сама решает, каким приложениям работать в фоне, и может остановить службу блокировки.",
            steps = listOf(
                "Найдите «Анти-Обломов» и выключите «Управлять автоматически»",
                "В появившемся окне включите все три: «Автозапуск», «Косвенный запуск», «Работа в фоне»",
            ),
            screens = listOf(
                SettingsScreen("com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                SettingsScreen("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
            ),
            buttonText = "Открыть «Запуск приложений»",
        )

        PhoneBrand.VIVO -> Guide(
            title = "Автозапуск",
            why = "Funtouch OS и OriginOS не запускают службу после перезагрузки без этого разрешения.",
            steps = listOf(
                "Найдите «Анти-Обломов» в списке автозапуска и включите",
                "Ещё: Настройки → Батарея → «Высокое энергопотребление в фоне» → разрешить «Анти-Обломову»",
            ),
            screens = listOf(
                SettingsScreen("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
                SettingsScreen("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
                SettingsScreen("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
            ),
            buttonText = "Открыть автозапуск",
        )

        PhoneBrand.TRANSSION -> Guide(
            title = "Автозапуск",
            why = "HiOS и XOS не запускают службу после перезагрузки без этого разрешения.",
            steps = listOf(
                "Откроется «Phone Master» → «Автозапуск» — включите «Анти-Обломов»",
                "Если не открылось: Phone Master → Управление приложениями → Автозапуск",
            ),
            screens = listOf(
                SettingsScreen("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
            ),
            buttonText = "Открыть автозапуск",
        )

        PhoneBrand.OTHER -> null
    }

    /**
     * Дополнительная настройка батареи оболочки — поверх стандартной «без ограничений» Android.
     * Null — хватает стандартной.
     */
    fun extraBattery(brand: PhoneBrand): Guide? = when (brand) {
        PhoneBrand.XIAOMI -> Guide(
            title = "Контроль активности",
            why = "У HyperOS своя экономия батареи — она может останавливать службу блокировки.",
            steps = listOf(
                "Откроется «Контроль активности» для «Анти-Обломова»",
                "Выберите «Нет ограничений»",
            ),
            screens = listOf(
                SettingsScreen("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
            ),
            buttonText = "Открыть «Контроль активности»",
        )
        else -> null
    }

    /** Если включить службу не даёт серый переключатель «Ограниченная настройка» (Android 13+). */
    val RESTRICTED_SETTINGS_HELP = listOf(
        "Настройки → Приложения → Анти-Обломов",
        "⋮ вверху справа → «Разрешить ограниченные настройки»",
        "Вернитесь сюда и снова нажмите «Открыть спецвозможности»",
    )
}
