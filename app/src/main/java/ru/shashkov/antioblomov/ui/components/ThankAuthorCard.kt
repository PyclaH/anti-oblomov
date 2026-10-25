package ru.shashkov.antioblomov.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import ru.shashkov.antioblomov.ui.theme.Ink
import ru.shashkov.antioblomov.ui.theme.Sand
import ru.shashkov.antioblomov.ui.theme.Slate
import ru.shashkov.antioblomov.ui.theme.Terracotta

/** Страница проекта на Sponsr.ru. */
const val SPONSR_URL = "https://sponsr.ru/anti_oblomov/"

/** С какой экономии (минут в день) упоминаем её в панели. */
const val THANK_THRESHOLD_MINUTES = 15

/**
 * Панель «Поблагодарить автора» на главной — всегда, ненавязчиво: под «Было / стало»,
 * над карточкой службы (решение от 25.10.2026, вместо показа только на вехах).
 * Если экономия уже заметна (≥ 15 мин в день) — упоминаем её.
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

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Sand, contentColor = Ink),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Логотип — песочные часы на терракотовом фоне, как иконка приложения
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Terracotta),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Поблагодарить автора", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = savedMinutesPerDay
                        ?.takeIf { it >= THANK_THRESHOLD_MINUTES }
                        ?.let { "Ты возвращаешь себе ${formatMinutes(it)} в день. " }
                        .orEmpty() +
                        "«Анти-Обломов» бесплатный и без рекламы — таким и останется. " +
                        "Если он помогает, можно поддержать школьный проект Руслана Шашкова.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate,
                )
                OutlinedButton(
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "Не найден браузер, чтобы открыть ссылку", Toast.LENGTH_LONG).show()
                        }
                    },
                    border = BorderStroke(1.dp, Terracotta),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
                ) {
                    Text("Поддержать на Sponsr")
                }
            }
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
