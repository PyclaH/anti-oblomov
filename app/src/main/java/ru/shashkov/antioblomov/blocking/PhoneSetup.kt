package ru.shashkov.antioblomov.blocking

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import ru.shashkov.antioblomov.data.OnboardingStore
import ru.shashkov.antioblomov.rules.Guide
import ru.shashkov.antioblomov.rules.PhoneBrand
import ru.shashkov.antioblomov.rules.SetupState

/** Марка этого телефона. */
val phoneBrand: PhoneBrand by lazy { PhoneBrand.detect(Build.MANUFACTURER, Build.BRAND) }

/** Собрать, что настроено на телефоне (для мастера и главной). */
fun readSetupState(context: Context, onboarding: OnboardingStore = OnboardingStore(context)) = SetupState(
    brand = phoneBrand,
    serviceRunning = blockingServiceState(context) == ServiceState.RUNNING,
    notificationsAllowed = TimerNotification.allowed(context),
    batteryUnrestricted = isBatteryUnrestricted(context),
    extraBatteryConfirmed = onboarding.extraBatteryConfirmed,
    autostartConfirmed = onboarding.autostartConfirmed,
)

/** Android не ограничивает приложению работу в фоне («Батарея: без ограничений»). */
fun isBatteryUnrestricted(context: Context): Boolean =
    context.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) == true

/**
 * Системный вопрос «Разрешить приложению всегда работать в фоне?».
 * Если его нет на этой оболочке — список «Оптимизация батареи», затем страница приложения.
 */
@SuppressLint("BatteryLife") // приложение-блокировщик: без работы в фоне основная функция не работает
fun requestBatteryUnrestricted(context: Context) {
    val tries = listOf(
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}")),
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
    )
    if (tries.none { start(context, it) }) openAppSettings(context)
}

/**
 * Открыть экран из инструкции [guide]: пробуем известные варианты по очереди,
 * если ни один не открылся — страницу приложения в настройках.
 * @return true, если открылся экран из инструкции (а не запасная страница)
 */
fun openGuide(context: Context, guide: Guide): Boolean {
    val opened = guide.screens.any { screen ->
        start(
            context,
            Intent()
                .setComponent(ComponentName(screen.packageName, screen.className))
                // «Контроль активности» Xiaomi открывается сразу для нашего приложения
                .putExtra("package_name", context.packageName)
                .putExtra("package_label", context.applicationInfo.loadLabel(context.packageManager).toString()),
        )
    }
    if (!opened) openAppSettings(context)
    return opened
}

/** Запустить экран; false — его нет на этом телефоне или он закрыт для чужих приложений. */
private fun start(context: Context, intent: Intent): Boolean = try {
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: Exception) {
    Log.d(BlockingService.TAG, "Не открылся ${intent.component ?: intent.action}: ${e.javaClass.simpleName}")
    false
}

/** Настройки уведомлений «Анти-Обломова». */
fun openNotificationSettings(context: Context) {
    if (!start(
            context,
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
        )
    ) openAppSettings(context)
}
