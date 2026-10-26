package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.shashkov.antioblomov.rules.i18n.PlanDe
import ru.shashkov.antioblomov.rules.i18n.PlanEn
import ru.shashkov.antioblomov.rules.i18n.PlanRu
import ru.shashkov.antioblomov.rules.i18n.PlanTr

/** Тексты раздела «Расписание / таймер» (v1.2): русский по умолчанию, перевод — по параметру. */
class PlanStringsTest {

    @Test fun `ошибки интервала по умолчанию на русском`() {
        assertEquals(IntervalCheck.Error("Конец должен быть позже начала"), ScheduleEditor.check(600, 540))
        assertEquals(IntervalCheck.Error("Время вне суток"), ScheduleEditor.check(-1, 540))
    }

    @Test fun `ошибки интервала на английском`() {
        assertEquals(IntervalCheck.Error(PlanEn.endAfterStart), ScheduleEditor.check(600, 540, s = PlanEn))
    }

    @Test fun `ошибки длительности на выбранном языке`() {
        assertEquals(DurationCheck.Error("От 1 минуты до 12 часов"), TimerRules.parseDuration("13", ""))
        assertEquals(DurationCheck.Error(PlanDe.durationDigitsOnly), TimerRules.parseDuration("1a", "", PlanDe))
    }

    @Test fun `копирование на будни — названия дней`() {
        assertEquals(
            "Расписание на вторник, среда будет заменено расписанием дня «Понедельник».",
            PlanRu.copyWillReplace(listOf("Вторник", "Среда"), "Понедельник"),
        )
        assertEquals(
            "Salı, çarşamba programı, “pazartesi” gününün programıyla değiştirilecek.",
            PlanTr.copyWillReplace(listOf("Salı", "Çarşamba"), "Pazartesi"),
        )
    }

    @Test fun `английский — единственное и множественное число секунд`() {
        assertEquals("1 second left", PlanEn.secondsLeft(1))
        assertEquals("15 seconds left", PlanEn.secondsLeft(15))
    }
}
