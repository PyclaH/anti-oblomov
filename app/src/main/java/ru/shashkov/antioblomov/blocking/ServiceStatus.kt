package ru.shashkov.antioblomov.blocking

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

/** Включена ли наша служба в «Специальных возможностях». */
fun isBlockingServiceEnabled(context: Context): Boolean {
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
