package ru.shashkov.antioblomov.rules

/**
 * Ввод времени с клавиатуры: пользователь набирает только цифры, двоеточие
 * подставляется само («1430» → 14:30).
 *
 * Сколько цифр — так и понимаем:
 * - 1–2 цифры — целые часы: «9» → 09:00, «14» → 14:00;
 * - 3 цифры — Ч ММ: «930» → 09:30;
 * - 4 цифры — ЧЧ ММ: «0930» → 09:30, «2400» → 24:00 (до полуночи).
 */
object TimeInput {

    const val MAX_DIGITS = 4

    /** Минуты от полуночи или null, если время неправильное (например, 25:00 или 12:75). */
    fun parse(digits: String): Int? {
        if (digits.isEmpty() || digits.length > MAX_DIGITS || !digits.all { it.isDigit() }) return null
        val (h, m) = when (digits.length) {
            1, 2 -> digits.toInt() to 0
            3 -> digits.substring(0, 1).toInt() to digits.substring(1).toInt()
            else -> digits.substring(0, 2).toInt() to digits.substring(2).toInt()
        }
        if (m > 59) return null
        if (h == 24 && m == 0) return TimeInterval.MINUTES_IN_DAY
        if (h > 23) return null
        return h * 60 + m
    }

    /** Цифры для поля ввода: 570 → «0930», 1440 → «2400». */
    fun digitsOf(minute: Int): String = "%02d%02d".format(minute / 60, minute % 60)

    /** Только цифры, не больше четырёх — то, что оставляем в поле при наборе. */
    fun clean(input: String): String = input.filter { it.isDigit() }.take(MAX_DIGITS)

    /** Как показать набранное: двоеточие перед последними двумя цифрами, начиная с трёх цифр. */
    fun display(digits: String): String =
        if (digits.length >= 3) digits.dropLast(2) + ":" + digits.takeLast(2) else digits
}
