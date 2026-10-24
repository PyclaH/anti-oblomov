package ru.shashkov.antioblomov.blocking

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

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
    BlockingService.isRunning -> ServiceState.RUNNING
    else -> ServiceState.STOPPED
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
