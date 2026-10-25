package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeTextTest {

    private val zone = ZoneId.of("Asia/Irkutsk")
    /** 26.10.2026 — понедельник. */
    private fun at(day: Int, h: Int, m: Int = 0) = ZonedDateTime.of(2026, 10, day, h, m, 0, 0, zone)
    private val now = at(26, 11)

    @Test fun `сегодня — только время`() = assertEquals("14:00", TimeText.at(at(26, 14), now))
    @Test fun `завтра`() = assertEquals("завтра 01:30", TimeText.at(at(27, 1, 30), now))
    @Test fun `в эту неделю — день недели`() = assertEquals("Чт 10:00", TimeText.at(at(29, 10), now))
    @Test fun `дальше недели — дата`() = assertEquals("02.11 10:00", TimeText.at(ZonedDateTime.of(2026, 11, 2, 10, 0, 0, 0, zone), now))
}
