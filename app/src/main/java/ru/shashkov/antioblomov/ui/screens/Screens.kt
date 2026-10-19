package ru.shashkov.antioblomov.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.R

/*
 * Заглушки четырёх разделов. Каждую будем по очереди заменять настоящим экраном
 * (план — в README, раздел «План разработки»).
 */

@Composable
fun HomeScreen(modifier: Modifier = Modifier) = PlaceholderScreen(
    title = stringResource(R.string.app_name),
    description = "Главный экран: идёт ли сейчас блокировка и сколько времени сэкономлено.",
    planned = listOf(
        "Статус: «Сейчас учёба, до 14:00» или «Свободное время»",
        "Сравнение «было / стало»: время в соцсетях до установки и сейчас",
    ),
    modifier = modifier,
)

@Composable
fun ScheduleScreen(modifier: Modifier = Modifier) = PlaceholderScreen(
    title = stringResource(R.string.tab_schedule),
    description = "Время блокировки на каждый день недели, с перерывами.",
    planned = listOf(
        "Выбор дня недели",
        "Интервалы блокировки, например 10:00–14:00 и 15:00–20:00",
        "Копирование расписания на все будни",
    ),
    modifier = modifier,
)

@Composable
fun TimerScreen(modifier: Modifier = Modifier) = PlaceholderScreen(
    title = stringResource(R.string.tab_timer),
    description = "Разовая блокировка, без изменения расписания.",
    planned = listOf(
        "Быстрый выбор: 1, 2, 3 часа или своё время",
        "Обратный отсчёт",
        "Досрочная остановка только после 30 секунд ожидания",
    ),
    modifier = modifier,
)

@Composable
fun AppsScreen(modifier: Modifier = Modifier) = PlaceholderScreen(
    title = stringResource(R.string.tab_apps),
    description = "Какие приложения блокировать.",
    planned = listOf(
        "Список установленных приложений с иконками",
        "Поиск по названию",
        "Переключатель «блокировать» у каждого приложения",
    ),
    modifier = modifier,
)

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    planned: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.soon),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                planned.forEach { item ->
                    Text(text = "•  $item", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
