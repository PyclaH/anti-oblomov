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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme

/**
 * Ссылка на страницу проекта на Sponsr.ru.
 * Пока пустая — карточка не показывается. Подставим, когда проект пройдёт модерацию.
 */
const val SPONSR_URL = ""

/**
 * С какой экономии (минут в день) предлагаем поблагодарить автора.
 * Небольшие колебания не считаем экономией.
 */
const val THANK_THRESHOLD_MINUTES = 15

/**
 * Предложение поблагодарить автора. Показывается рядом с блоком «Было / стало»
 * и только если приложение реально сэкономило время.
 *
 * Полные условия показа (7 дней данных, вехи, не чаще раза в 30 дней) —
 * README, «Спецификация: раздел „Было / стало“», п. 6. Здесь проверяется только
 * порог экономии; остальное проверит главный экран на неделе 6.
 *
 * Важно (правила Sponsr п. 5.18 и RuStore п. 6.1): донат ничего не открывает
 * и ни на что в приложении не влияет — приложение полностью бесплатное.
 *
 * @param savedMinutesPerDay на сколько минут в день сократилось время
 *        в заблокированных приложениях (было − стало).
 */
@Composable
fun ThankAuthorCard(
    savedMinutesPerDay: Int,
    modifier: Modifier = Modifier,
    url: String = SPONSR_URL,
) {
    // «Не сейчас» скрывает карточку до перезапуска приложения.
    // TODO: когда появится хранение настроек — скрывать на 30 дней.
    var dismissed by rememberSaveable { mutableStateOf(false) }
    if (dismissed || url.isBlank() || savedMinutesPerDay < THANK_THRESHOLD_MINUTES) return

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
                TextButton(onClick = { dismissed = true }) {
                    Text("Не сейчас")
                }
                Button(onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Не найден браузер, чтобы открыть ссылку", Toast.LENGTH_LONG).show()
                    }
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
            modifier = Modifier.padding(16.dp),
            url = "https://sponsr.ru/",
        )
    }
}
