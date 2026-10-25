package ru.shashkov.antioblomov.rules

import java.time.ZonedDateTime

/** Результат проверки длительности, набранной в часах и минутах. */
sealed interface DurationCheck {
    data class Ok(val minutes: Int) : DurationCheck
    data class Error(val message: String) : DurationCheck
}

/**
 * Логика экрана «Таймер» (FR-09, FR-10) без Android — проверяется юнит-тестами (TimerRulesTest).
 *
 * Таймер — момент окончания (метка времени), а не часы на циферблате, поэтому
 * переход через полночь ему не мешает. Действует поверх расписания и не меняет его.
 */
object TimerRules {

    const val MIN_MINUTES = 1
    const val MAX_MINUTES = 12 * 60

    /** Быстрый выбор: 1, 2, 3 часа. */
    val PRESETS_MINUTES = listOf(60, 120, 180)

    /** Сколько осталось, мс; null — таймер не идёт. */
    fun remainingMillis(settings: Settings, now: ZonedDateTime): Long? {
        val end = settings.timerEndsAt ?: return null
        val left = end - now.toInstant().toEpochMilli()
        return left.takeIf { it > 0 }
    }

    fun isRunning(settings: Settings, now: ZonedDateTime): Boolean = remainingMillis(settings, now) != null

    /** Запустить на [minutes] минут (1 мин – 12 ч). */
    fun start(settings: Settings, minutes: Int, now: ZonedDateTime): Settings {
        require(minutes in MIN_MINUTES..MAX_MINUTES) { "Таймер от 1 минуты до 12 часов" }
        return BlockRules.startTimer(settings, minutes.toLong(), now)
    }

    /** Продлить идущий таймер на [minutes] минут (1 мин – 12 ч за раз); если он уже кончился — запустить. */
    fun extend(settings: Settings, minutes: Int, now: ZonedDateTime): Settings {
        require(minutes in MIN_MINUTES..MAX_MINUTES) { "Продлить можно от 1 минуты до 12 часов" }
        val nowMs = now.toInstant().toEpochMilli()
        val base = maxOf(settings.timerEndsAt ?: nowMs, nowMs)
        return settings.copy(timerEndsAt = base + minutes * 60_000L)
    }

    fun stop(settings: Settings): Settings = BlockRules.stopTimer(settings)

    /**
     * Проверить длительность из двух полей: часы и минуты. Пустое поле = 0.
     * Можно набрать и «0 ч 90 мин» — это 1 ч 30 мин; главное, чтобы итог был от 1 минуты до 12 часов.
     */
    fun parseDuration(hoursText: String, minutesText: String): DurationCheck {
        val hours = if (hoursText.isEmpty()) 0 else hoursText.toIntOrNull()
        val minutes = if (minutesText.isEmpty()) 0 else minutesText.toIntOrNull()
        if (hours == null || minutes == null || hours < 0 || minutes < 0) {
            return DurationCheck.Error("Введите часы и минуты цифрами")
        }
        val total = hours.toLong() * 60 + minutes
        return if (total in MIN_MINUTES..MAX_MINUTES) DurationCheck.Ok(total.toInt())
        else DurationCheck.Error("От 1 минуты до 12 часов")
    }

    /** «1 ч 30 мин», «45 мин», «2 ч». */
    fun durationText(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "$m мин"
            m == 0 -> "$h ч"
            else -> "$h ч $m мин"
        }
    }

    /** Обратный отсчёт: «2:59:59», меньше часа — «59:59». Доли секунды округляем вверх. */
    fun countdown(remainingMillis: Long): String {
        val total = (remainingMillis + 999) / 1000
        val h = total / 3600
        val m = total % 3600 / 60
        val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }
}
