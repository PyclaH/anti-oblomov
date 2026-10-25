package ru.shashkov.antioblomov.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import ru.shashkov.antioblomov.ui.theme.Surface2
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.Accent
import ru.shashkov.antioblomov.ui.theme.Night0

/** Страница проекта на Sponsr.ru. */
const val SPONSR_URL = "https://sponsr.ru/anti_oblomov/"

/** С какой экономии (минут в день) упоминаем её в панели. */
const val THANK_THRESHOLD_MINUTES = 15

/**
 * «Поблагодарить автора» на главной — всегда, ненавязчиво (решение от 25.10.2026): одна строка
 * под «Было / стало», над «Системой» (редизайн R2). Если экономия ≥ 15 мин в день — упоминаем её.
 *
 * Важно (правила Sponsr п. 5.18 и RuStore п. 6.1, 7.3): донат ничего не открывает,
 * ни на что в приложении не влияет и не связан с отзывом — приложение полностью бесплатное.
 *
 * @param savedMinutesPerDay экономия в среднем в день; null — пока неизвестна
 */
@Composable
fun ThankAuthorCard(
    savedMinutesPerDay: Int?,
    modifier: Modifier = Modifier,
    url: String = SPONSR_URL,
) {
    val context = LocalContext.current
    val open = {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Не найден браузер, чтобы открыть ссылку", Toast.LENGTH_LONG).show()
        }
    }

    AppCard(modifier = modifier, onClick = open, padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Логотип — песочные часы на графите, как иконка приложения
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Night0),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Поблагодарить автора", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = savedMinutesPerDay
                        ?.takeIf { it >= THANK_THRESHOLD_MINUTES }
                        ?.let { "Ты возвращаешь себе ${formatMinutes(it)} в день" }
                        ?: "Приложение бесплатное и без рекламы",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            Text("Sponsr ›", style = MaterialTheme.typography.bodySmall, color = Accent)
        }
    }
}

/** 135 → «2 ч 15 мин», 40 → «40 мин». */
fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "$m мин"
        m == 0 -> "$h ч"
        else -> "$h ч $m мин"
    }
}

@Preview(showBackground = true)
@Composable
private fun ThankAuthorCardPreview() {
    AntiOblomovTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ThankAuthorCard(savedMinutesPerDay = 145)
            ThankAuthorCard(savedMinutesPerDay = null)
        }
    }
}
