package ru.shashkov.antioblomov.blocking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.shashkov.antioblomov.MainActivity
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.data.Language
import ru.shashkov.antioblomov.rules.BlockRules
import ru.shashkov.antioblomov.rules.BlockStatus
import ru.shashkov.antioblomov.rules.HomeStatusText
import ru.shashkov.antioblomov.rules.Settings
import java.time.ZonedDateTime

/**
 * Уведомление «Блокировка до 14:00» с маленьким обратным отсчётом (v1.2).
 *
 * - Показывается при ЛЮБОЙ блокировке — по расписанию и по таймеру; пропадает, когда она кончается.
 * - Отсчёт тикает сама система (хронометр уведомления), отдельная фоновая работа не нужна.
 * - Android 16+: уведомление просит показ как Live Update («живое обновление») — система выносит его
 *   чипом с отсчётом в строку состояния; на HyperOS 3 такие уведомления попадают в «Супер-остров»
 *   (динамический остров) — без закрытых API производителей. Где ОС это не поддерживает или
 *   пользователь отключил, остаётся обычное уведомление.
 * - Обновляется при каждой записи настроек (SettingsStore) — в том числе при смене языка, —
 *   и службой блокировки на границах интервалов и после перезагрузки.
 */
object BlockNotification {

    private const val CHANNEL_ID = "block"
    private const val OLD_CHANNEL_ID = "timer" // до v1.2 — только для таймера
    private const val NOTIFICATION_ID = 1001
    private const val EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing" // Notification, API 36

    /** Можно ли показывать уведомления (на Android 13+ нужно разрешение). */
    fun allowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Показать, обновить или убрать уведомление по текущим настройкам. */
    fun update(context: Context, settings: Settings, now: ZonedDateTime = ZonedDateTime.now()) {
        val status = BlockRules.status(settings, now)
        // Нечего блокировать — нечего и показывать
        if (status !is BlockStatus.Blocked || settings.blockedPackages.isEmpty()) {
            cancel(context)
            return
        }
        val endsAt = status.until.toInstant().toEpochMilli()
        val left = endsAt - now.toInstant().toEpochMilli()
        if (left <= 0) {
            cancel(context)
            return
        }
        if (!allowed(context)) return
        val s = Language.strings(context)
        ensureChannel(context)

        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                if (status.byTimer) putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_TIMER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        // «Блокировка до 14:00» — тот же текст, что на главной; отсчёт — системный хронометр рядом
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_lock)
            .setContentTitle(HomeStatusText.title(status, now, s.home, s.common))
            .setWhen(endsAt)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setTimeoutAfter(left)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(open)
            // Android 16+: просьба показать как Live Update (чип в строке состояния, «Супер-остров» HyperOS 3).
            // Ровно то же делает NotificationCompat.setRequestPromotedOngoing в новых AndroidX — флаг в extras;
            // на старых Android он просто игнорируется. Требования соблюдены: постоянное, с заголовком,
            // стандартный шаблон, без цвета фона, канал не «минимальной» важности.
            .addExtras(Bundle().apply { putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true) })
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(BlockingService.TAG, "Нет разрешения на уведомления", e)
        }
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        // Канал старой версии («только таймер») больше не нужен
        if (manager.getNotificationChannel(OLD_CHANNEL_ID) != null) manager.deleteNotificationChannel(OLD_CHANNEL_ID)
        // Создаём каждый раз: если канал уже есть, Android только обновит название и описание —
        // так они следуют за языком приложения. Важность и прочие настройки пользователя не меняются.
        val s = Language.strings(context).plan
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, s.notificationTitle, NotificationManager.IMPORTANCE_LOW).apply {
                description = s.channelDescription
                setShowBadge(false)
            }
        )
    }
}
