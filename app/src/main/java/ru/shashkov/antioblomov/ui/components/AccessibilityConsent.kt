package ru.shashkov.antioblomov.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.blocking.openAccessibilitySettings
import ru.shashkov.antioblomov.data.OnboardingStore
import ru.shashkov.antioblomov.ui.theme.Surface1
import ru.shashkov.antioblomov.ui.theme.TextMuted

/**
 * Открыть настройки спецвозможностей — но в первый раз только после явного согласия.
 *
 * Требование Google Play к приложениям, которые используют службу спецвозможностей не как
 * инструмент для людей с инвалидностью: внутри приложения, отдельно от прочего, объяснить, какие
 * данные служба получает и зачем, и получить согласие нажатием кнопки. «Не сейчас» — ничего не
 * включаем, приложение работает дальше (без блокировки).
 *
 * Возвращает действие для кнопки «Открыть спецвозможности»; окно согласия рисует сама.
 */
@Composable
fun rememberAccessibilityOpener(): () -> Unit {
    val context = LocalContext.current
    val store = remember { OnboardingStore(context) }
    var asking by remember { mutableStateOf(false) }

    if (asking) {
        AccessibilityConsentDialog(
            onAccept = {
                store.serviceConsent = true
                asking = false
                openAccessibilitySettings(context)
            },
            onDecline = { asking = false },
        )
    }
    return remember<() -> Unit> {
        {
            if (store.serviceConsent) {
                openAccessibilitySettings(context)
            } else {
                asking = true
            }
        }
    }
}

@Composable
private fun AccessibilityConsentDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDecline,
        containerColor = Surface1,
        title = { Text("Служба блокировки: что она видит") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "«Анти-Обломов» использует службу специальных возможностей Android (Accessibility Service), " +
                        "чтобы закрывать выбранные приложения во время блокировки.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Какие данные: служба узнаёт, какое приложение сейчас открыто на экране (его системное название).",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Зачем: если это приложение в вашем списке блокировки и сейчас время блокировки, " +
                        "поверх него показывается экран с цитатой.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Служба не читает текст на экране, сообщения и пароли, ничего не сохраняет о ваших действиях " +
                        "и ничего не передаёт: у приложения нет доступа к интернету.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Выключить службу можно в любой момент в настройках спецвозможностей телефона.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
        },
        confirmButton = { Button(onClick = onAccept) { Text("Принимаю") } },
        dismissButton = { TextButton(onClick = onDecline) { Text("Не сейчас") } },
    )
}
