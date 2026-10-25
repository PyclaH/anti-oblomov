package ru.shashkov.antioblomov.blocking

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/** Состояние службы блокировки. */
enum class ServiceState {
    /** Выключена в «Специальных возможностях». */
    OFF,

    /** Включена в настройках, но не работает — обычно её остановила система (HyperOS). */
    STOPPED,

    /** Работает. */
    RUNNING,
}

fun blockingServiceState(context: Context): ServiceState = when {
    !isEnabledInSettings(context) -> ServiceState.OFF
    isConnected(context) -> ServiceState.RUNNING
    else -> ServiceState.STOPPED
}

/**
 * Подключена ли служба к системе прямо сейчас.
 * Служба живёт в отдельном процессе, поэтому спрашиваем у системы:
 * этот список содержит только реально подключённые службы.
 */
private fun isConnected(context: Context): Boolean {
    val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
    val ours = ComponentName(context, BlockingService::class.java)
    return manager
        .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { info ->
            val s = info.resolveInfo.serviceInfo
            ComponentName(s.packageName, s.name) == ours
        }
}

private fun isEnabledInSettings(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    val ours = ComponentName(context, BlockingService::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == ours }
}

/** Открыть системный экран «Специальные возможности». */
fun openAccessibilitySettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/** Открыть страницу «О приложении»: там на HyperOS «Автозапуск» и «Контроль активности». */
fun openAppSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
