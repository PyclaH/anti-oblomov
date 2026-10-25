package ru.shashkov.antioblomov.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.rules.SetupChecklist
import ru.shashkov.antioblomov.rules.SetupItem
import ru.shashkov.antioblomov.rules.SetupState
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.Warn
import ru.shashkov.antioblomov.ui.theme.Accent

/**
 * «Настройка телефона» на главной: что сделано в мастере, а что пропущено (FR-08).
 * Пропущенное — с кнопкой «Настроить», которая снова открывает мастер.
 */
@Composable
fun SetupCard(setup: SetupState, onOpenWizard: () -> Unit, modifier: Modifier = Modifier) {
    val missing = SetupChecklist.missing(setup)
    run {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Настройка телефона", style = MaterialTheme.typography.titleSmall)
            SetupChecklist.items(setup.brand).forEach { item ->
                SetupRow(title = itemTitle(item, setup), done = SetupChecklist.isDone(item, setup))
            }
            if (missing.isNotEmpty()) {
                Text(
                    text = "Без этих настроек блокировка может отключаться сама, а «Было / стало» — не считаться.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Button(onClick = onOpenWizard) { Text("Настроить") }
            } else {
                OutlinedButton(onClick = onOpenWizard) { Text("Пройти настройку заново") }
            }
        }
    }
}

@Composable
fun SetupRow(title: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(
            imageVector = if (done) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = if (done) "Сделано" else "Не сделано",
            tint = if (done) Calm else Warn,
        )
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

/** На разных марках «автозапуск» называется по-разному. */
fun itemTitle(item: SetupItem, setup: SetupState): String =
    if (item == SetupItem.AUTOSTART) SetupChecklist.autostartTitle(setup.brand) else item.title
