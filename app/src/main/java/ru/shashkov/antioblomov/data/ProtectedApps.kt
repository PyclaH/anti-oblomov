package ru.shashkov.antioblomov.data

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.provider.Telephony
import android.telecom.TelecomManager

/**
 * Приложения, которые нельзя заблокировать (FR-07): иначе можно остаться без
 * звонков, без настроек или вообще без рабочего стола.
 *
 * Определяются на конкретном телефоне — у Samsung, Xiaomi, Honor свои звонилки,
 * рабочие столы и клавиатуры.
 */
object ProtectedApps {

    /** Известные системные приложения разных производителей. */
    private val KNOWN = setOf(
        // настройки
        "com.android.settings",
        // звонки и экстренные вызовы
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.incallui",
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.contacts",
        "com.samsung.android.app.contacts",
        "com.android.emergency",
        "com.google.android.apps.safetyhub",
        "com.samsung.android.emergency",
        // сообщения
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        // системный интерфейс
        "com.android.systemui",
        // «Безопасность» Xiaomi — там автозапуск, без которого служба не переживает перезагрузку
        "com.miui.securitycenter",
    )

    fun of(context: Context): Set<String> {
        val pm = context.packageManager
        val result = KNOWN.toMutableSet()
        result += context.packageName

        // рабочие столы
        result += pm.queryActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
            .map { it.activityInfo.packageName }
        // настройки (у некоторых производителей свой пакет)
        result += pm.queryActivities(Intent(Settings.ACTION_SETTINGS)).map { it.activityInfo.packageName }
        // звонилка и SMS по умолчанию
        runCatching { context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage }
            .getOrNull()?.let { result += it }
        runCatching { Telephony.Sms.getDefaultSmsPackage(context) }.getOrNull()?.let { result += it }
        // клавиатура по умолчанию: «пакет/служба»
        runCatching { Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) }
            .getOrNull()
            ?.substringBefore('/')
            ?.takeIf { it.isNotBlank() }
            ?.let { result += it }

        return result
    }
}
