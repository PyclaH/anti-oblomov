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
import ru.shashkov.antioblomov.rules.TimeText
import ru.shashkov.antioblomov.rules.TimerRules
import ru.shashkov.antioblomov.rules.Settings
import java.time.ZonedDateTime

/**
 * Уведомление о блокировке (v1.2): крупно — обратный отсчёт, мелко — «Блокировка до 14:00».
 *
 * - Показывается при ЛЮБОЙ блокировке — по расписанию и по таймеру; пропадает, когда она кончается.
 * - Отсчёт раз в секунду обновляет служба блокировки, пока экран включён (BlockingService.notificationTick).
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
    private const val EXTRA_SHORT_CRITICAL_TEXT = "android.shortCriticalText" // Notification, API 36

    /** Можно ли показывать уведомления (на Android 13+ нужно разрешение). */
    fun allowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Показать, обновить или убрать уведомление по текущим настройкам.
     * @return момент конца блокировки (мс), если уведомление показано, — чтобы служба тикала отсчёт; иначе null
     */
    fun update(context: Context, settings: Settings, now: ZonedDateTime = ZonedDateTime.now()): Long? {
        val status = BlockRules.status(settings, now)
        // Нечего блокировать — нечего и показывать
        if (status !is BlockStatus.Blocked || settings.blockedPackages.isEmpty()) {
            cancel(context)
            return null
        }
        val endsAt = status.until.toInstant().toEpochMilli()
        val left = endsAt - now.toInstant().toEpochMilli()
        if (left <= 0) {
            cancel(context)
            return null
        }
        if (!allowed(context)) return null
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

        // Крупно — обратный отсчёт «49:52» / «1:24:07»; мелко, в шапке рядом с названием приложения, —
        // «Блокировка до завтра 00:00» (с послезавтра — дата). Отсчёт — обычный текст: его каждую секунду
        // обновляет служба блокировки (tick), потому что «Супер-остров» HyperOS 3 и острова ColorOS / OriginOS
        // системный хронометр не показывают — только заголовок или «короткий текст» (shortCriticalText).
        val countdown = TimerRules.countdown(left)
        val until = s.home.blockedUntil(TimeText.at(status.until, now, s.common, weekdays = false))
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_lock)
            .setContentTitle(countdown)
            .setSubText(until)
            .setTicker(until)
            .setShowWhen(false)
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
            .addExtras(
                Bundle().apply {
                    putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true)
                    // Текст чипа в строке состояния и острова (Android 16+): тот же отсчёт
                    putString(EXTRA_SHORT_CRITICAL_TEXT, countdown)
                }
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(BlockingService.TAG, "Нет разрешения на уведомления", e)
            return null
        }
        return endsAt
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
