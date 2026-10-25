package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import ru.shashkov.antioblomov.rules.UsageEvent.Kind.BACKGROUND
import ru.shashkov.antioblomov.rules.UsageEvent.Kind.FOREGROUND
import ru.shashkov.antioblomov.rules.UsageEvent.Kind.SCREEN_OFF

class UsageMathTest {

    private val zone = ZoneId.of("Asia/Irkutsk")
    private fun t(day: Int, h: Int, m: Int = 0) = ZonedDateTime.of(2026, 10, day, h, m, 0, 0, zone).toInstant().toEpochMilli()
    private fun d(day: Int) = LocalDate.of(2026, 10, day)
    private val yt = "com.google.android.youtube"
    private val vk = "com.vkontakte.android"

    @Test fun `простая сессия`() {
        val r = UsageMath.perDay(listOf(UsageEvent(yt, t(26, 10), FOREGROUND), UsageEvent(yt, t(26, 10, 30), BACKGROUND)), t(26, 0), t(27, 0), zone)
        assertEquals(30 * 60, r[d(26)]!![yt])
    }

    @Test fun `сессия через полночь делится между днями`() {
        val r = UsageMath.perDay(listOf(UsageEvent(yt, t(26, 23, 40), FOREGROUND), UsageEvent(yt, t(27, 0, 10), BACKGROUND)), t(26, 0), t(28, 0), zone)
        assertEquals(20 * 60, r[d(26)]!![yt])
        assertEquals(10 * 60, r[d(27)]!![yt])
    }

    @Test fun `погасший экран закрывает сессию`() {
        val r = UsageMath.perDay(listOf(UsageEvent(yt, t(26, 10), FOREGROUND), UsageEvent("android", t(26, 10, 5), SCREEN_OFF)), t(26, 0), t(27, 0), zone)
        assertEquals(5 * 60, r[d(26)]!![yt])
    }

    @Test fun `приложение было открыто ещё до начала запроса`() {
        val r = UsageMath.perDay(listOf(UsageEvent(yt, t(26, 0, 15), BACKGROUND)), t(26, 0), t(27, 0), zone)
        assertEquals(15 * 60, r[d(26)]!![yt])
    }

    @Test fun `открытое к концу запроса считается до конца`() {
        val r = UsageMath.perDay(listOf(UsageEvent(vk, t(26, 23, 0), FOREGROUND)), t(26, 0), t(26, 23, 20), zone)
        assertEquals(20 * 60, r[d(26)]!![vk])
    }

    @Test fun `повторные события не удваивают время`() {
        val r = UsageMath.perDay(
            listOf(
                UsageEvent(yt, t(26, 10), FOREGROUND), UsageEvent(yt, t(26, 10, 1), FOREGROUND),
                UsageEvent(yt, t(26, 10, 10), BACKGROUND), UsageEvent(yt, t(26, 10, 11), BACKGROUND),
            ),
            t(26, 0), t(27, 0), zone,
        )
        assertEquals(10 * 60, r[d(26)]!![yt])
    }

    @Test fun `дни с данными`() {
        assertEquals(setOf(d(25), d(26)), UsageMath.daysWithData(listOf(UsageEvent(vk, t(25, 9), FOREGROUND), UsageEvent(vk, t(26, 9), BACKGROUND)), zone))
        assertNull(UsageMath.perDay(emptyList(), t(26, 0), t(27, 0), zone)[d(26)])
    }
}
