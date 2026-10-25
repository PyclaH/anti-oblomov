package ru.shashkov.antioblomov.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import ru.shashkov.antioblomov.rules.DayUsage
import ru.shashkov.antioblomov.rules.UsageEvent
import ru.shashkov.antioblomov.rules.UsageHistory
import ru.shashkov.antioblomov.rules.UsageMath
import java.time.LocalDate
import java.time.ZoneId

/**
 * Сбор статистики из системной истории использования Android (FR-14, README «Было / стало», п. 2).
 *
 * - Первый раз, когда доступ к статистике выдан, — это «день установки»: сохраняем снимок
 *   до 7 полных дней ДО него по всем приложениям (система хранит подробную историю
 *   недолго, поэтому сразу и один раз).
 * - Потом каждый прошедший полный день дописываем в журнал. Вызывают и главный экран,
 *   и служба блокировки (раз в час), так что дни не теряются, даже если приложение не открывать.
 */
object UsageCollector {

    private const val TAG = "AntiOblomov"
    private const val BEFORE_DAYS = 7L

    /** Выдан ли доступ «История использования». */
    fun hasAccess(context: Context): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    /** Экран «Доступ к истории использования» — сразу для нашего приложения, если оболочка умеет. */
    fun openSettings(context: Context) {
        val tries = listOf(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:${context.packageName}")),
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
        )
        for (intent in tries) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (e: Exception) {
                Log.d(TAG, "Не открылся экран доступа к статистике: ${e.javaClass.simpleName}")
            }
        }
    }

    /** Снимок «до» (один раз) и дописать прошедшие дни в журнал. Долго — вызывать не в главном потоке. */
    fun update(context: Context) {
        if (!hasAccess(context)) return
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        try {
            UsageStore(context).update { old ->
                val history = old ?: UsageHistory(
                    startDate = today,
                    before = collect(context, today.minusDays(BEFORE_DAYS), today, zone),
                    after = emptyMap(),
                ).also { Log.i(TAG, "Статистика: начало $today, дней до установки — ${it.before.size}") }

                val lastDay = (history.after.keys + history.startDate).max()
                val from = lastDay.plusDays(1)
                if (from >= today) return@update history
                history.copy(after = history.after + collect(context, from, today, zone))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Не удалось обновить статистику", e)
        }
    }

    /** Сегодня (с полуночи до сейчас), по приложениям. */
    fun today(context: Context): DayUsage {
        if (!hasAccess(context)) return emptyMap()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val from = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        return UsageMath.perDay(events(context, from, now), from, now, zone)[today].orEmpty()
    }

    /** Что не считать экранным временем: рабочий стол и сам «Анти-Обломов». */
    fun notScreenTime(context: Context): Set<String> =
        context.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_ALL,
        ).map { it.activityInfo.packageName }.toSet() + context.packageName

    /** Полные дни [from, to): только те, за которые в системе есть история. */
    private fun collect(context: Context, from: LocalDate, to: LocalDate, zone: ZoneId): Map<LocalDate, DayUsage> {
        val fromMs = from.atStartOfDay(zone).toInstant().toEpochMilli()
        val toMs = to.atStartOfDay(zone).toInstant().toEpochMilli()
        val events = events(context, fromMs, toMs)
        val withData = UsageMath.daysWithData(events, zone)
        val perDay = UsageMath.perDay(events, fromMs, toMs, zone)
        return withData.filter { it >= from && it < to }.associateWith { perDay[it].orEmpty() }
    }

    private fun events(context: Context, fromMs: Long, toMs: Long): List<UsageEvent> {
        val usm = context.getSystemService(UsageStatsManager::class.java) ?: return emptyList()
        val result = mutableListOf<UsageEvent>()
        val iterator = usm.queryEvents(fromMs, toMs) ?: return emptyList()
        val e = UsageEvents.Event()
        while (iterator.hasNextEvent()) {
            iterator.getNextEvent(e)
            val kind = when (e.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> UsageEvent.Kind.FOREGROUND // = ACTIVITY_RESUMED
                UsageEvents.Event.MOVE_TO_BACKGROUND -> UsageEvent.Kind.BACKGROUND // = ACTIVITY_PAUSED
                SCREEN_NON_INTERACTIVE -> UsageEvent.Kind.SCREEN_OFF
                else -> null
            } ?: continue
            result += UsageEvent(e.packageName, e.timeStamp, kind)
        }
        return result
    }

    /** UsageEvents.Event.SCREEN_NON_INTERACTIVE (Android 9+): экран погас. */
    private const val SCREEN_NON_INTERACTIVE = 16
}
