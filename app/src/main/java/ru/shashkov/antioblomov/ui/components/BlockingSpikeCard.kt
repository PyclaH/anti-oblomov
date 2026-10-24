package ru.shashkov.antioblomov.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.blocking.ServiceState
import ru.shashkov.antioblomov.blocking.openAccessibilitySettings
import ru.shashkov.antioblomov.blocking.openAppSettings
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme
import ru.shashkov.antioblomov.ui.theme.Lake
import ru.shashkov.antioblomov.ui.theme.Terracotta

/**
 * Временная карточка шага 1: работает ли служба блокировки и как её включить.
 * На шаге 6 её заменит полноценный онбординг.
 */
@Composable
fun BlockingSpikeCard(state: ServiceState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Пробная блокировка (шаг 1)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            when (state) {
                ServiceState.RUNNING -> {
                    Status("Служба работает", ok = true)
                    Body("Сейчас YouTube блокируется всегда. Откройте его, чтобы проверить.")
                    OutlinedButton(onClick = { openAccessibilitySettings(context) }) {
                        Text("Настройки спецвозможностей")
                    }
                }

                ServiceState.STOPPED -> {
                    Status("Служба включена, но не работает", ok = false)
                    Body(
                        "Её остановила система. Выключите и снова включите " +
                            "«Анти-Обломов: блокировка» в спецвозможностях. Чтобы это не повторялось: " +
                            "закрепите «Анти-Обломов» замком в списке недавних и, если есть, " +
                            "включите «Автозапуск» в настройках приложения."
                    )
                    Button(onClick = { openAccessibilitySettings(context) }) {
                        Text("Спецвозможности")
                    }
                    OutlinedButton(onClick = { openAppSettings(context) }) {
                        Text("Настройки приложения")
                    }
                }

                ServiceState.OFF -> {
                    Status("Служба выключена", ok = false)
                    Body(
                        "В «Специальных возможностях» найдите в списке служб " +
                            "«Анти-Обломов: блокировка» и включите её."
                    )
                    Button(onClick = { openAccessibilitySettings(context) }) {
                        Text("Открыть настройки")
                    }
                }
            }
        }
    }
}

@Composable
private fun Status(text: String, ok: Boolean) = Text(
    text = text,
    style = MaterialTheme.typography.bodyLarge,
    fontWeight = FontWeight.Bold,
    color = if (ok) Lake else Terracotta,
)

@Composable
private fun Body(text: String) = Text(text = text, style = MaterialTheme.typography.bodyMedium)

@Preview(showBackground = true)
@Composable
private fun BlockingSpikeCardPreview() {
    AntiOblomovTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BlockingSpikeCard(state = ServiceState.OFF)
            BlockingSpikeCard(state = ServiceState.STOPPED)
            BlockingSpikeCard(state = ServiceState.RUNNING)
        }
    }
}
