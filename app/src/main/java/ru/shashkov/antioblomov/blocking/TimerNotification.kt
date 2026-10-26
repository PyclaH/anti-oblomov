package ru.shashkov.antioblomov.blocking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.shashkov.antioblomov.MainActivity
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.data.Language
import ru.shashkov.antioblomov.rules.Settings
import ru.shashkov.antioblomov.rules.TimeText
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Уведомление «Блокировка по таймеру» с обратным отсчётом в шторке.
 *
 * Отсчёт тикает сама система (хронометр уведомления), поэтому отдельная фоновая
 * служба не нужна; когда таймер кончается, уведомление пропадает само (setTimeoutAfter).
 * Вызывается при запуске, продлении и остановке таймера, а ещё при подключении
 * службы блокировки — чтобы уведомление вернулось после перезагрузки.
 */
object TimerNotification {

    private const val CHANNEL_ID = "timer"
    private const val NOTIFICATION_ID = 1001

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
        val endsAt = settings.timerEndsAt
        val left = endsAt?.let { it - now.toInstant().toEpochMilli() }
        if (endsAt == null || left == null || left <= 0) {
            cancel(context)
            return
        }
        if (!allowed(context)) return
        val s = Language.strings(context)
        ensureChannel(context)

        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_TIMER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val until = TimeText.at(Instant.ofEpochMilli(endsAt).atZone(now.zone), now, s.common)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_lock)
            .setContentTitle(s.plan.notificationTitle)
            .setContentText(s.plan.until(until))
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
        // Создаём каждый раз: если канал уже есть, Android только обновит название и описание —
        // так они следуют за языком приложения (v1.2). Важность и прочие настройки пользователя не меняются.
        val s = Language.strings(context).plan
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, s.timerTitle, NotificationManager.IMPORTANCE_LOW).apply {
                description = s.channelDescription
                setShowBadge(false)
            }
        )
    }
}
