package ru.shashkov.antioblomov.rules

import ru.shashkov.antioblomov.rules.i18n.GuideText
import ru.shashkov.antioblomov.rules.i18n.SetupRu
import ru.shashkov.antioblomov.rules.i18n.SetupStrings

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
 *
 * Тексты инструкций — в [SetupStrings] (v1.2), по умолчанию русские.
 */
object SetupGuide {

    /** «Автозапуск» или его аналог. Null — у марки такой настройки нет. */
    fun autostart(brand: PhoneBrand, s: SetupStrings = SetupRu): Guide? = when (brand) {
        PhoneBrand.XIAOMI -> guide(
            s.autostartXiaomi,
            screens = listOf(
                SettingsScreen("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            ),
        )

        PhoneBrand.SAMSUNG -> guide(
            s.autostartSamsung,
            screens = listOf(
                SettingsScreen("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
                SettingsScreen("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"),
            ),
        )

        PhoneBrand.OPPO -> guide(
            s.autostartOppo,
            screens = listOf(
                SettingsScreen("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                SettingsScreen("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
                SettingsScreen("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
            ),
        )

        PhoneBrand.HUAWEI -> guide(
            s.autostartHuawei,
            screens = listOf(
                SettingsScreen("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                SettingsScreen("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
                SettingsScreen("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"),
            ),
        )

        PhoneBrand.HONOR -> guide(
            s.autostartHonor,
            screens = listOf(
                SettingsScreen("com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                SettingsScreen("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
            ),
        )

        PhoneBrand.VIVO -> guide(
            s.autostartVivo,
            screens = listOf(
                SettingsScreen("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
                SettingsScreen("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
                SettingsScreen("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
            ),
        )

        PhoneBrand.TRANSSION -> guide(
            s.autostartTranssion,
            screens = listOf(
                SettingsScreen("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
            ),
        )

        PhoneBrand.OTHER -> null
    }

    /**
     * Дополнительная настройка батареи оболочки — поверх стандартной «без ограничений» Android.
     * Null — хватает стандартной.
     */
    fun extraBattery(brand: PhoneBrand, s: SetupStrings = SetupRu): Guide? = when (brand) {
        PhoneBrand.XIAOMI -> guide(
            s.batteryXiaomi,
            screens = listOf(
                SettingsScreen("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
            ),
        )
        else -> null
    }

    /** Если включить службу не даёт серый переключатель «Ограниченная настройка» (Android 13+). */
    fun restrictedSettingsHelp(s: SetupStrings = SetupRu): List<String> = s.restrictedHelp

    private fun guide(text: GuideText, screens: List<SettingsScreen>) = Guide(
        title = text.title,
        why = text.why,
        steps = text.steps,
        screens = screens,
        buttonText = text.button,
    )
}
