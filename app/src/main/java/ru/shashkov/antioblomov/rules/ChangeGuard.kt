package ru.shashkov.antioblomov.rules

import java.time.ZonedDateTime

/**
 * Пауза перед ослаблением блокировки, пока она идёт (решения от 25.10.2026).
 *
 * Если блокировка уже идёт, а изменение её ослабляет — из сегодняшнего расписания
 * пропадает хоть одна ещё не прошедшая минута блокировки или из списка убирается
 * приложение, — сначала 15 секунд ожидания с цитатой, потом подтверждение.
 * Усиление (новый интервал, удлинение, новое приложение) сохраняется сразу.
 */
object ChangeGuard {

    const val WAIT_SECONDS = 15

    /** Нужна ли пауза, чтобы перейти от настроек [before] к [after] в момент [now]. */
    fun needsWait(before: Settings, after: Settings, now: ZonedDateTime): Boolean {
        if (BlockRules.status(before, now) !is BlockStatus.Blocked) return false
        val appRemoved = !after.blockedPackages.containsAll(before.blockedPackages)
        return appRemoved || todayWeakened(before, after, now)
    }

    /** Есть ли минута с текущей до полуночи, которая была под блокировкой, а теперь нет. */
    private fun todayWeakened(before: Settings, after: Settings, now: ZonedDateTime): Boolean {
        val day = now.dayOfWeek
        val fromMinute = now.hour * 60 + now.minute
        val kept = after.intervalsOn(day)
        return (fromMinute until TimeInterval.MINUTES_IN_DAY).any { minute ->
            before.intervalsOn(day).any { it.contains(minute) } && kept.none { it.contains(minute) }
        }
    }
}
