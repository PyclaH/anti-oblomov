package ru.shashkov.antioblomov.rules.i18n

import java.time.DayOfWeek

/**
 * Общие тексты: дни недели, «завтра», единицы времени, склонения, кнопки.
 * Одинаковые для всех разделов — здесь, чтобы не переводить по нескольку раз.
 */
interface CommonStrings {
    /** «Пн» */
    fun dayShort(d: DayOfWeek): String
    /** «Понедельник» */
    fun dayFull(d: DayOfWeek): String
    /** «завтра» — перед временем: «завтра 01:30» */
    val tomorrow: String
    /** Короткие единицы для плиток: «мин», «ч» */
    val unitMin: String
    val unitHour: String
    /** 90 → «1 ч 30 мин», 45 → «45 мин», 120 → «2 ч» */
    fun duration(minutes: Int): String
    /** «1 день», «3 дня», «5 дней» */
    fun days(n: Int): String
    /** «1 учебный день» (учебный день = 8 ч) */
    fun schoolDays(n: Int): String

    val cancel: String
    val ok: String
    val save: String
    val close: String
    val next: String
    val back: String
    val skip: String
    val done: String
    val delete: String
    /** Подпись выбора языка */
    val language: String
}

private fun ruPlural(n: Int, one: String, few: String, many: String) = when {
    n % 100 in 11..14 -> many
    n % 10 == 1 -> one
    n % 10 in 2..4 -> few
    else -> many
}

private fun hm(minutes: Int, h: String, m: String, sep: String = " "): String {
    val hh = minutes / 60
    val mm = minutes % 60
    return when {
        hh == 0 -> "$mm$sep$m"
        mm == 0 -> "$hh$sep$h"
        else -> "$hh$sep$h $mm$sep$m"
    }
}

private fun week(vararg names: String): (DayOfWeek) -> String = { names[it.value - 1] }

object CommonRu : CommonStrings {
    private val s = week("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    private val f = week("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "завтра"
    override val unitMin = "мин"
    override val unitHour = "ч"
    override fun duration(minutes: Int) = hm(minutes, "ч", "мин")
    override fun days(n: Int) = "$n " + ruPlural(n, "день", "дня", "дней")
    override fun schoolDays(n: Int) = "$n " + ruPlural(n, "учебный день", "учебных дня", "учебных дней")
    override val cancel = "Отмена"
    override val ok = "OK"
    override val save = "Сохранить"
    override val close = "Закрыть"
    override val next = "Далее"
    override val back = "Назад"
    override val skip = "Пропустить"
    override val done = "Готово"
    override val delete = "Удалить"
    override val language = "Язык"
}

object CommonEn : CommonStrings {
    private val s = week("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val f = week("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "tomorrow"
    override val unitMin = "min"
    override val unitHour = "h"
    override fun duration(minutes: Int) = hm(minutes, "h", "min")
    override fun days(n: Int) = if (n == 1) "1 day" else "$n days"
    override fun schoolDays(n: Int) = if (n == 1) "1 school day" else "$n school days"
    override val cancel = "Cancel"
    override val ok = "OK"
    override val save = "Save"
    override val close = "Close"
    override val next = "Next"
    override val back = "Back"
    override val skip = "Skip"
    override val done = "Done"
    override val delete = "Delete"
    override val language = "Language"
}

object CommonEs : CommonStrings {
    private val s = week("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    private val f = week("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "mañana"
    override val unitMin = "min"
    override val unitHour = "h"
    override fun duration(minutes: Int) = hm(minutes, "h", "min")
    override fun days(n: Int) = if (n == 1) "1 día" else "$n días"
    override fun schoolDays(n: Int) = if (n == 1) "1 día lectivo" else "$n días lectivos"
    override val cancel = "Cancelar"
    override val ok = "Aceptar"
    override val save = "Guardar"
    override val close = "Cerrar"
    override val next = "Siguiente"
    override val back = "Atrás"
    override val skip = "Omitir"
    override val done = "Listo"
    override val delete = "Eliminar"
    override val language = "Idioma"
}

object CommonPt : CommonStrings {
    private val s = week("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
    private val f = week("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "amanhã"
    override val unitMin = "min"
    override val unitHour = "h"
    override fun duration(minutes: Int) = hm(minutes, "h", "min")
    override fun days(n: Int) = if (n == 1) "1 dia" else "$n dias"
    override fun schoolDays(n: Int) = if (n == 1) "1 dia letivo" else "$n dias letivos"
    override val cancel = "Cancelar"
    override val ok = "OK"
    override val save = "Salvar"
    override val close = "Fechar"
    override val next = "Próximo"
    override val back = "Voltar"
    override val skip = "Pular"
    override val done = "Concluído"
    override val delete = "Excluir"
    override val language = "Idioma"
}

object CommonDe : CommonStrings {
    private val s = week("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
    private val f = week("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "morgen"
    override val unitMin = "Min."
    override val unitHour = "Std."
    override fun duration(minutes: Int) = hm(minutes, "Std.", "Min.")
    override fun days(n: Int) = if (n == 1) "1 Tag" else "$n Tage"
    override fun schoolDays(n: Int) = if (n == 1) "1 Schultag" else "$n Schultage"
    override val cancel = "Abbrechen"
    override val ok = "OK"
    override val save = "Speichern"
    override val close = "Schließen"
    override val next = "Weiter"
    override val back = "Zurück"
    override val skip = "Überspringen"
    override val done = "Fertig"
    override val delete = "Löschen"
    override val language = "Sprache"
}

object CommonTr : CommonStrings {
    private val s = week("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    private val f = week("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "yarın"
    override val unitMin = "dk"
    override val unitHour = "sa"
    override fun duration(minutes: Int) = hm(minutes, "sa", "dk")
    override fun days(n: Int) = "$n gün"
    override fun schoolDays(n: Int) = "$n okul günü"
    override val cancel = "İptal"
    override val ok = "Tamam"
    override val save = "Kaydet"
    override val close = "Kapat"
    override val next = "İleri"
    override val back = "Geri"
    override val skip = "Atla"
    override val done = "Bitti"
    override val delete = "Sil"
    override val language = "Dil"
}

object CommonKk : CommonStrings {
    private val s = week("Дс", "Сс", "Ср", "Бс", "Жм", "Сб", "Жс")
    private val f = week("Дүйсенбі", "Сейсенбі", "Сәрсенбі", "Бейсенбі", "Жұма", "Сенбі", "Жексенбі")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "ертең"
    override val unitMin = "мин"
    override val unitHour = "сағ"
    override fun duration(minutes: Int) = hm(minutes, "сағ", "мин")
    override fun days(n: Int) = "$n күн"
    override fun schoolDays(n: Int) = "$n оқу күні"
    override val cancel = "Болдырмау"
    override val ok = "OK"
    override val save = "Сақтау"
    override val close = "Жабу"
    override val next = "Келесі"
    override val back = "Артқа"
    override val skip = "Өткізіп жіберу"
    override val done = "Дайын"
    override val delete = "Жою"
    override val language = "Тіл"
}

object CommonUz : CommonStrings {
    private val s = week("Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya")
    private val f = week("Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba", "Yakshanba")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "ertaga"
    override val unitMin = "daq"
    override val unitHour = "soat"
    override fun duration(minutes: Int) = hm(minutes, "soat", "daq")
    override fun days(n: Int) = "$n kun"
    override fun schoolDays(n: Int) = "$n o‘quv kuni"
    override val cancel = "Bekor qilish"
    override val ok = "OK"
    override val save = "Saqlash"
    override val close = "Yopish"
    override val next = "Keyingi"
    override val back = "Orqaga"
    override val skip = "O‘tkazib yuborish"
    override val done = "Tayyor"
    override val delete = "O‘chirish"
    override val language = "Til"
}

object CommonZh : CommonStrings {
    private val s = week("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    private val f = week("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    override fun dayShort(d: DayOfWeek) = s(d)
    override fun dayFull(d: DayOfWeek) = f(d)
    override val tomorrow = "明天"
    override val unitMin = "分钟"
    override val unitHour = "小时"
    override fun duration(minutes: Int) = hm(minutes, "小时", "分钟", sep = "").replace(" ", "")
    override fun days(n: Int) = "$n 天"
    override fun schoolDays(n: Int) = "$n 个上学日"
    override val cancel = "取消"
    override val ok = "确定"
    override val save = "保存"
    override val close = "关闭"
    override val next = "下一步"
    override val back = "返回"
    override val skip = "跳过"
    override val done = "完成"
    override val delete = "删除"
    override val language = "语言"
}
