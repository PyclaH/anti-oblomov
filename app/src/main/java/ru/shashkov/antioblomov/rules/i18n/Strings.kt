package ru.shashkov.antioblomov.rules.i18n

/**
 * Все тексты интерфейса на одном языке (v1.2). Разбиты по разделам; у каждого раздела —
 * интерфейс и по объекту на язык, так что компилятор не даст забыть перевод.
 * Русский — основной: логика и тесты по умолчанию используют русские тексты.
 */
interface Strings {
    val lang: Lang
    val common: CommonStrings
    val home: HomeStrings
    val plan: PlanStrings
    val setup: SetupStrings
    val apps: AppsStrings
}

private class LangStrings(
    override val lang: Lang,
    override val common: CommonStrings,
    override val home: HomeStrings,
    override val plan: PlanStrings,
    override val setup: SetupStrings,
    override val apps: AppsStrings,
) : Strings

private val ALL: Map<Lang, Strings> = mapOf(
    Lang.RU to LangStrings(Lang.RU, CommonRu, HomeRu, PlanRu, SetupRu, AppsRu),
    Lang.EN to LangStrings(Lang.EN, CommonEn, HomeEn, PlanEn, SetupEn, AppsEn),
    Lang.ES to LangStrings(Lang.ES, CommonEs, HomeEs, PlanEs, SetupEs, AppsEs),
    Lang.PT to LangStrings(Lang.PT, CommonPt, HomePt, PlanPt, SetupPt, AppsPt),
    Lang.DE to LangStrings(Lang.DE, CommonDe, HomeDe, PlanDe, SetupDe, AppsDe),
    Lang.TR to LangStrings(Lang.TR, CommonTr, HomeTr, PlanTr, SetupTr, AppsTr),
    Lang.KK to LangStrings(Lang.KK, CommonKk, HomeKk, PlanKk, SetupKk, AppsKk),
    Lang.UZ to LangStrings(Lang.UZ, CommonUz, HomeUz, PlanUz, SetupUz, AppsUz),
    Lang.ZH to LangStrings(Lang.ZH, CommonZh, HomeZh, PlanZh, SetupZh, AppsZh),
)

fun stringsFor(lang: Lang): Strings = ALL.getValue(lang)
