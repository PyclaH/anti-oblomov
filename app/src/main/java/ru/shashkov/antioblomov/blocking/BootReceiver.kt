package ru.shashkov.antioblomov.blocking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Получает сигнал о загрузке телефона.
 *
 * Сам по себе службу не включает (это делает система, если служба включена
 * в спецвозможностях). Нужен, чтобы HyperOS показала у приложения настройку
 * «Автозапуск», и чтобы процесс службы поднимался после перезагрузки.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(BlockingService.TAG, "Телефон загрузился")
        }
    }
}
