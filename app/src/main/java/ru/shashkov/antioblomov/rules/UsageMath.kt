package ru.shashkov.antioblomov.rules

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Событие из системной статистики Android (UsageStatsManager), без Android-типов. */
data class UsageEvent(val packageName: String, val timeMs: Long, val kind: Kind) {
    enum class Kind {
        /** Приложение вышло на передний план. */
        FOREGROUND,

        /** Ушло с переднего плана. */
        BACKGROUND,

        /** Экран погас — всё, что было открыто, считаем закрытым. */
        SCREEN_OFF,
    }
}

/** Секунды на переднем плане: приложение → секунды за один день. */
typealias DayUsage = Map<String, Int>

/**
 * Время в приложениях по дням из событий «вышло на экран / ушло с экрана» (README, «Было / стало», п. 2).
 * Границы суток — по местному времени, сессия через полночь делится между днями.
 */
object UsageMath {

    /**
     * @param fromMs начало запроса: если приложение ушло с экрана, а прихода в запросе не было,
     *        считаем, что оно было открыто с этого момента
     * @param toMs конец запроса: всё, что ещё открыто, считаем до этого момента
     */
    fun perDay(events: List<UsageEvent>, fromMs: Long, toMs: Long, zone: ZoneId): Map<LocalDate, DayUsage> {
        val totals = mutableMapOf<LocalDate, MutableMap<String, Long>>() // мс
        val open = mutableMapOf<String, Long>()
        val seen = mutableSetOf<String>()

        fun add(pkg: String, start: Long, end: Long) {
            var s = maxOf(start, fromMs)
            val e = minOf(end, toMs)
            while (s < e) {
                val day = Instant.ofEpochMilli(s).atZone(zone).toLocalDate()
                val dayEnd = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val chunkEnd = minOf(e, dayEnd)
                val map = totals.getOrPut(day) { mutableMapOf() }
                map[pkg] = (map[pkg] ?: 0L) + (chunkEnd - s)
                s = chunkEnd
            }
        }

        for (event in events.sortedBy { it.timeMs }) {
            val t = event.timeMs
            if (t < fromMs || t > toMs) continue
            when (event.kind) {
                UsageEvent.Kind.FOREGROUND -> {
                    if (event.packageName !in open) open[event.packageName] = t
                    seen += event.packageName
                }
                UsageEvent.Kind.BACKGROUND -> {
                    val start = open.remove(event.packageName)
                        ?: if (event.packageName !in seen) fromMs else null // открыто ещё до запроса
                    seen += event.packageName
                    if (start != null) add(event.packageName, start, t)
                }
                UsageEvent.Kind.SCREEN_OFF -> {
                    open.forEach { (pkg, start) -> add(pkg, start, t) }
                    open.clear()
                }
            }
        }
        open.forEach { (pkg, start) -> add(pkg, start, toMs) }

        return totals.mapValues { (_, byPkg) -> byPkg.mapValues { (_, ms) -> (ms / 1000).toInt() } }
    }

    /** Дни, за которые в системе вообще есть события, — значит, история за них есть (даже если 0 минут в соцсетях). */
    fun daysWithData(events: List<UsageEvent>, zone: ZoneId): Set<LocalDate> =
        events.map { Instant.ofEpochMilli(it.timeMs).atZone(zone).toLocalDate() }.toSet()
}
