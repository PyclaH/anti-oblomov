package ru.shashkov.antioblomov.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme

/**
 * Страница проекта на Sponsr.ru (прислана 25.10.2026, проект на модерации).
 * Пустая строка — карточка не показывается.
 */
const val SPONSR_URL = "https://sponsr.ru/anti_olblomov/"

/**
 * Предложение поблагодарить автора — рядом с «Было / стало» (FR-17).
 * КОГДА показывать, решает ThankRules (веха, экономия ≥ 15 мин/день, не чаще раза в 30 дней);
 * карточка видна, пока пользователь не ответит: «Поблагодарить» или «Не сейчас».
 *
 * Важно (правила Sponsr п. 5.18 и RuStore п. 6.1, 7.3): донат ничего не открывает,
 * ни на что в приложении не влияет и не связан с отзывом — приложение полностью бесплатное.
 */
@Composable
fun ThankAuthorCard(
    savedMinutesPerDay: Int,
    onAnswered: () -> Unit,
    modifier: Modifier = Modifier,
    url: String = SPONSR_URL,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Ты возвращаешь себе ${formatMinutes(savedMinutesPerDay)} в день",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Если «Анти-Обломов» помогает, можно поблагодарить автора. " +
                    "Это необязательно: приложение бесплатное и таким останется.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onAnswered) {
                    Text("Не сейчас")
                }
                Button(onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Не найден браузер, чтобы открыть ссылку", Toast.LENGTH_LONG).show()
                    }
                    onAnswered()
                }) {
                    Text("Поблагодарить автора")
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
        ThankAuthorCard(
            savedMinutesPerDay = 145,
            onAnswered = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
