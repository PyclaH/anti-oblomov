package ru.shashkov.antioblomov.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Марка телефона, инструкции и пункты настройки (шаг 6). */
class SetupChecklistTest {

    private fun state(
        brand: PhoneBrand = PhoneBrand.XIAOMI,
        service: Boolean = true,
        notifications: Boolean = true,
        battery: Boolean = true,
        extraBattery: Boolean = true,
        autostart: Boolean = true,
        usage: Boolean = true,
    ) = SetupState(brand, service, notifications, battery, extraBattery, autostart, usage)

    @Test fun `марка по производителю`() {
        assertEquals(PhoneBrand.XIAOMI, PhoneBrand.detect("Xiaomi", "POCO"))
        assertEquals(PhoneBrand.XIAOMI, PhoneBrand.detect("Xiaomi", "Redmi"))
        assertEquals(PhoneBrand.SAMSUNG, PhoneBrand.detect("samsung", "samsung"))
        assertEquals(PhoneBrand.OPPO, PhoneBrand.detect("realme", "realme"))
        assertEquals(PhoneBrand.OPPO, PhoneBrand.detect("OnePlus", "OnePlus"))
        assertEquals(PhoneBrand.HONOR, PhoneBrand.detect("HONOR", "HONOR"))
        assertEquals(PhoneBrand.HUAWEI, PhoneBrand.detect("HUAWEI", "HUAWEI"))
        assertEquals(PhoneBrand.VIVO, PhoneBrand.detect("vivo", "iQOO"))
        assertEquals(PhoneBrand.TRANSSION, PhoneBrand.detect("INFINIX MOBILITY LIMITED", "Infinix"))
        assertEquals(PhoneBrand.TRANSSION, PhoneBrand.detect("TECNO MOBILE LIMITED", "TECNO"))
        assertEquals(PhoneBrand.OTHER, PhoneBrand.detect("Google", "google"))
        assertEquals(PhoneBrand.OTHER, PhoneBrand.detect(null, null))
    }

    @Test fun `у каждой оболочки, кроме прочих, есть инструкция автозапуска`() {
        PhoneBrand.entries.filter { it != PhoneBrand.OTHER }.forEach { brand ->
            val guide = SetupGuide.autostart(brand)
            assertNotNull(brand.name, guide)
            assertTrue(brand.name, guide!!.steps.isNotEmpty() && guide.screens.isNotEmpty())
        }
        assertNull(SetupGuide.autostart(PhoneBrand.OTHER))
    }

    @Test fun `шаги мастера зависят от марки`() {
        assertEquals(OnboardingPage.entries, Onboarding.pages(PhoneBrand.XIAOMI))
        assertFalse(OnboardingPage.AUTOSTART in Onboarding.pages(PhoneBrand.OTHER))
    }

    @Test fun `всё настроено — ничего не показываем`() {
        assertTrue(SetupChecklist.missing(state()).isEmpty())
    }

    @Test fun `на Xiaomi батарея — ещё и «Контроль активности»`() {
        assertEquals(listOf(SetupItem.BATTERY), SetupChecklist.missing(state(extraBattery = false)))
    }

    @Test fun `на Samsung дополнительной настройки батареи нет`() {
        assertTrue(SetupChecklist.missing(state(brand = PhoneBrand.SAMSUNG, extraBattery = false)).isEmpty())
    }

    @Test fun `на прочих Android автозапуск не спрашиваем`() {
        assertTrue(SetupChecklist.missing(state(brand = PhoneBrand.OTHER, autostart = false)).isEmpty())
    }

    @Test fun `доступ к статистике — отдельный пункт и шаг мастера`() {
        assertEquals(listOf(SetupItem.USAGE), SetupChecklist.missing(state(usage = false)))
        assertTrue(OnboardingPage.USAGE in Onboarding.pages(PhoneBrand.OTHER))
    }

    @Test fun `пропущенные шаги видны`() {
        assertEquals(
            listOf(SetupItem.SERVICE, SetupItem.NOTIFICATIONS, SetupItem.AUTOSTART),
            SetupChecklist.missing(state(service = false, notifications = false, autostart = false)),
        )
    }
}
