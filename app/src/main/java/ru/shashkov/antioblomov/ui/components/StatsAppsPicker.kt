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
import ru.shashkov.antioblomov.rules.i18n.HomeRu
import ru.shashkov.antioblomov.rules.i18n.HomeStrings
import ru.shashkov.antioblomov.ui.i18n.LocalStrings
import ru.shashkov.antioblomov.ui.theme.Accent
import ru.shashkov.antioblomov.ui.theme.Line
import ru.shashkov.antioblomov.ui.theme.Night1
import ru.shashkov.antioblomov.ui.theme.TextMuted
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults

/** Подпись к кнопке списка: «Как в списке блокировки: 3», «Выбрано: 5», «Все, кроме системных». */
fun statsAppsSummary(choice: StatsChoice, tracked: Set<String>, s: HomeStrings = HomeRu): String = when {
    choice.all -> s.appsAllExceptSystem
    tracked.isEmpty() -> s.appsNone
    choice.chosen == null -> s.appsAsBlockList(tracked.size)
    else -> s.appsChosen(tracked.size)
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
    val s = LocalStrings.current
    var query by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChoice(choice.copy(all = !choice.all)) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(s.home.activateAll, style = MaterialTheme.typography.bodyLarge)
                Text(
                    s.home.activateAllSub,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
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
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(s.home.searchApps, color = TextMuted) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = s.home.clearSearch, tint = TextMuted)
                    }
                }
            },
            // Как поиск на вкладке «Приложения» (R4), только на фоне карточки
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Night1,
                unfocusedContainerColor = Night1,
                focusedBorderColor = TextMuted,
                unfocusedBorderColor = Line,
                cursorColor = Accent,
            ),
        )

        if (apps == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp), color = Accent)
                Text(s.home.loadingApps, style = MaterialTheme.typography.bodyMedium)
            }
            return@Column
        }

        val byPackage = remember(apps) { apps.associateBy { it.entry.packageName } }
        val shown = remember(apps, protected, tracked, query) {
            StatsApps.visible(apps.map { it.entry }, protected, tracked, query)
        }
        if (shown.isEmpty()) {
            Text(
                text = if (query.isBlank()) s.home.nothingChosen
                else s.home.nothingFound,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
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
