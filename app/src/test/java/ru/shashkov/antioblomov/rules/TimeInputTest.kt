package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Проверки ввода времени с клавиатуры. */
class TimeInputTest {

    @Test fun `целые часы одной и двумя цифрами`() {
        assertEquals(9 * 60, TimeInput.parse("9"))
        assertEquals(14 * 60, TimeInput.parse("14"))
        assertEquals(0, TimeInput.parse("0"))
    }

    @Test fun `три цифры — Ч ММ`() {
        assertEquals(9 * 60 + 30, TimeInput.parse("930"))
    }

    @Test fun `четыре цифры — ЧЧ ММ`() {
        assertEquals(9 * 60 + 30, TimeInput.parse("0930"))
        assertEquals(23 * 60 + 59, TimeInput.parse("2359"))
    }

    @Test fun `24_00 — до полуночи`() {
        assertEquals(24 * 60, TimeInput.parse("2400"))
        assertEquals(24 * 60, TimeInput.parse("24"))
    }

    @Test fun `неправильное время`() {
        assertNull(TimeInput.parse(""))
        assertNull(TimeInput.parse("25"))
        assertNull(TimeInput.parse("2401"))
        assertNull(TimeInput.parse("1275"))
        assertNull(TimeInput.parse("960"))
        assertNull(TimeInput.parse("12345"))
        assertNull(TimeInput.parse("1a"))
    }

    @Test fun `цифры для поля`() {
        assertEquals("0930", TimeInput.digitsOf(9 * 60 + 30))
        assertEquals("2400", TimeInput.digitsOf(24 * 60))
    }

    @Test fun `очистка набранного`() {
        assertEquals("1430", TimeInput.clean("14:30"))
        assertEquals("1234", TimeInput.clean("123456"))
    }

    @Test fun `показ с двоеточием`() {
        assertEquals("9", TimeInput.display("9"))
        assertEquals("14", TimeInput.display("14"))
        assertEquals("9:30", TimeInput.display("930"))
        assertEquals("14:30", TimeInput.display("1430"))
    }
}
