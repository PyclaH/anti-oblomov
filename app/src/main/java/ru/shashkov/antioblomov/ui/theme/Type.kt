package ru.shashkov.antioblomov.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.shashkov.antioblomov.R

// Шрифты вшиты в приложение (res/font, лицензия SIL OFL — assets/licenses), интернет не нужен.

/** Заголовки и большие цифры — широкий геометрический Unbounded. */
val Unbounded = FontFamily(
    Font(R.font.unbounded_medium, FontWeight.Medium),
    Font(R.font.unbounded_semibold, FontWeight.SemiBold),
)

/** Текст, кнопки, подписи — Onest. */
val Onest = FontFamily(
    Font(R.font.onest_regular, FontWeight.Normal),
    Font(R.font.onest_medium, FontWeight.Medium),
    Font(R.font.onest_semibold, FontWeight.SemiBold),
)

/** Цитаты классиков — PT Serif. */
val PtSerif = FontFamily(Font(R.font.pt_serif_regular, FontWeight.Normal))

/** Цифры одинаковой ширины — отсчёт не «прыгает». */
const val TABULAR = "tnum"

/** Крупная цифра статуса и отсчёта. */
val BigNumber = TextStyle(
    fontFamily = Unbounded,
    fontWeight = FontWeight.Medium,
    fontSize = 48.sp,
    lineHeight = 52.sp,
    fontFeatureSettings = TABULAR,
)

/** Подпись раздела мелкими прописными: «СЕГОДНЯ», «БЫЛО / СТАЛО». */
val Overline = TextStyle(
    fontFamily = Onest,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.14.em,
)

private val base = Typography()

/**
 * Шкала: заголовок экрана 28 · заголовок карточки 17 · текст 15 · подпись 13.
 * headline* — Unbounded, всё остальное — Onest.
 */
val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Unbounded, fontWeight = FontWeight.Medium),
    displayMedium = base.displayMedium.copy(fontFamily = Unbounded, fontWeight = FontWeight.Medium),
    displaySmall = base.displaySmall.copy(fontFamily = Unbounded, fontWeight = FontWeight.Medium),
    headlineLarge = TextStyle(fontFamily = Unbounded, fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = Unbounded, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = Unbounded, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp, fontFeatureSettings = TABULAR),
    titleLarge = TextStyle(fontFamily = Onest, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Onest, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = Onest, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Onest, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontFamily = Onest, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Onest, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Onest, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Onest, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Onest, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)
