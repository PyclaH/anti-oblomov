package ru.shashkov.antioblomov.rules

import java.time.ZonedDateTime

/**
 * Пауза перед изменением настроек во время блокировки (решение от 25.10.2026).
 *
 * Если блокировка уже идёт, а изменение касается её — расписания на сегодня или списка
 * блокируемых приложений, — сначала 15 секунд ожидания с цитатой, потом подтверждение.
 * Порыв «только на минутку» за это время часто проходит.
 */
object ChangeGuard {

    const val WAIT_SECONDS = 15

    /** Нужна ли пауза, чтобы перейти от настроек [before] к [after] в момент [now]. */
    fun needsWait(before: Settings, after: Settings, now: ZonedDateTime): Boolean {
        if (BlockRules.status(before, now) !is BlockStatus.Blocked) return false
        val today = now.dayOfWeek
        val todayChanged = before.intervalsOn(today) != after.intervalsOn(today)
        val appsChanged = before.blockedPackages != after.blockedPackages
        return todayChanged || appsChanged
    }
}
