package ru.shashkov.antioblomov.rules

import java.time.Instant
import java.time.ZonedDateTime

/** Состояние блокировки в конкретный момент. */
sealed interface BlockStatus {

    /**
     * Блокировка идёт.
     * @property until когда закончится (с учётом идущих подряд интервалов и таймера)
     */
    data class Blocked(
        val until: ZonedDateTime,
        val bySchedule: Boolean,
        val byTimer: Boolean,
    ) : BlockStatus

    /**
     * Свободное время.
     * @property nextBlockAt когда начнётся следующая блокировка по расписанию (null — в ближайшую неделю нет)
     */
    data class Free(val nextBlockAt: ZonedDateTime?) : BlockStatus
}

/**
 * Правила блокировки: «блокировать ли сейчас» по расписанию и таймеру.
 * Чистая логика без Android — проверяется юнит-тестами (BlockRulesTest).
 */
object BlockRules {

    /** Блокировать ли приложение [pkg] в момент [now]. */
    fun shouldBlock(pkg: String, settings: Settings, now: ZonedDateTime): Boolean =
        pkg in settings.blockedPackages && status(settings, now) is BlockStatus.Blocked

    fun status(settings: Settings, now: ZonedDateTime): BlockStatus {
        val timerEnd = settings.timerEndsAt
            ?.let { Instant.ofEpochMilli(it).atZone(now.zone) }
            ?.takeIf { it.isAfter(now) }
        val scheduleEnd = scheduleBlockedUntil(settings, now)

        if (timerEnd == null && scheduleEnd == null) {
            return BlockStatus.Free(nextScheduledStart(settings, now))
        }

        // Конец блокировки: позднее из двух, плюс интервалы, которые начинаются
        // ровно тогда, когда кончается предыдущая блокировка (10–14 и 14–15 = до 15).
        var until = listOfNotNull(timerEnd, scheduleEnd).max()
        var steps = 0
        while (steps++ < MAX_CHAIN) {
            val next = scheduleBlockedUntil(settings, until) ?: break
            if (!next.isAfter(until)) break
            until = next
        }
        return BlockStatus.Blocked(until, bySchedule = scheduleEnd != null, byTimer = timerEnd != null)
    }

    /** Когда состояние сменится в следующий раз (начало или конец блокировки). */
    fun nextChange(settings: Settings, now: ZonedDateTime): ZonedDateTime? =
        when (val s = status(settings, now)) {
            is BlockStatus.Blocked -> s.until
            is BlockStatus.Free -> s.nextBlockAt
        }

    /** Запустить таймер на [minutes] минут. */
    fun startTimer(settings: Settings, minutes: Long, now: ZonedDateTime): Settings =
        settings.copy(timerEndsAt = now.plusMinutes(minutes).toInstant().toEpochMilli())

    fun stopTimer(settings: Settings): Settings = settings.copy(timerEndsAt = null)

    /**
     * Пересекающиеся интервалы одного дня (FR-04). Пустой список — всё в порядке.
     * Интервалы, которые только соприкасаются (10–14 и 14–15), не пересекаются.
     */
    fun overlaps(intervals: List<TimeInterval>): List<Pair<TimeInterval, TimeInterval>> {
        val sorted = intervals.sortedBy { it.startMinute }
        return sorted.zipWithNext().filter { (a, b) -> a.endMinute > b.startMinute }
    }

    /** Если сейчас идёт интервал расписания — когда он кончается. */
    private fun scheduleBlockedUntil(settings: Settings, t: ZonedDateTime): ZonedDateTime? {
        val minute = t.hour * 60 + t.minute
        val interval = settings.intervalsOn(t.dayOfWeek).firstOrNull { minute in it } ?: return null
        return t.toLocalDate().atStartOfDay(t.zone).plusMinutes(interval.endMinute.toLong())
    }

    /** Ближайшее начало интервала расписания после [now] — в пределах недели. */
    private fun nextScheduledStart(settings: Settings, now: ZonedDateTime): ZonedDateTime? {
        for (dayOffset in 0..7L) {
            val date = now.toLocalDate().plusDays(dayOffset)
            val dayStart = date.atStartOfDay(now.zone)
            for (interval in settings.intervalsOn(date.dayOfWeek)) {
                val start = dayStart.plusMinutes(interval.startMinute.toLong())
                if (start.isAfter(now)) return start
            }
        }
        return null
    }

    /** Защита от зацикливания при склейке интервалов: с большим запасом на неделю расписания. */
    private const val MAX_CHAIN = 64
}
