package ru.shashkov.antioblomov.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.data.InstalledApp
import ru.shashkov.antioblomov.rules.StatsApps
import ru.shashkov.antioblomov.rules.StatsChoice
import ru.shashkov.antioblomov.ui.theme.Accent

/** Подпись к кнопке списка: «Как в списке блокировки: 3», «Выбрано: 5», «Все, кроме системных». */
fun statsAppsSummary(choice: StatsChoice, tracked: Set<String>): String = when {
    choice.all -> "Все, кроме системных"
    tracked.isEmpty() -> "Не выбраны"
    choice.chosen == null -> "Как в списке блокировки: ${tracked.size}"
    else -> "Выбрано: ${tracked.size}"
}

/**
 * Раскрытая часть списка «Приложения для статистики» (кнопку-заголовок рисует «Было / стало»).
 * Как на вкладке «Приложения», но короче: только включённые и те, что нашлись поиском.
 * «Активировать все» — все несистемные приложения; список тогда скрывается.
 * Поиск сбрасывается, когда список сворачивают: этот блок тогда уходит с экрана вместе со своим состоянием.
 *
 * @param apps установленные приложения; null — ещё загружаются
 */
@Composable
fun StatsAppsPicker(
    apps: List<InstalledApp>?,
    protected: Set<String>,
    choice: StatsChoice,
    tracked: Set<String>,
    onChoice: (StatsChoice) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChoice(choice.copy(all = !choice.all)) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Активировать все", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Считать все приложения, кроме системных",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = choice.all, onCheckedChange = { onChoice(choice.copy(all = it)) })
        }

        if (choice.all) return@Column

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Найти и добавить приложение") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Clear, contentDescription = "Очистить поиск") }
                }
            },
        )

        if (apps == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                Text("Загружаю список приложений…", style = MaterialTheme.typography.bodyMedium)
            }
            return@Column
        }

        val byPackage = remember(apps) { apps.associateBy { it.entry.packageName } }
        val shown = remember(apps, protected, tracked, query) {
            StatsApps.visible(apps.map { it.entry }, protected, tracked, query)
        }
        if (shown.isEmpty()) {
            Text(
                text = if (query.isBlank()) "Пока ничего не выбрано — найдите приложение поиском и включите"
                else "Ничего не найдено",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // Список короткий (включённые + найденные), поэтому обычная колонка, без ленивого списка
        shown.forEach { entry ->
            AppRow(
                app = byPackage[entry.packageName],
                label = entry.label,
                checked = entry.packageName in tracked,
                horizontalPadding = 0.dp,
            ) { on -> onChoice(StatsApps.toggle(choice, tracked, entry.packageName, on)) }
        }
    }
}
