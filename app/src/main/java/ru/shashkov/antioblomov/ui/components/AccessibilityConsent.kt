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
import ru.shashkov.antioblomov.ui.i18n.LocalStrings
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
    val s = LocalStrings.current.setup
    AlertDialog(
        onDismissRequest = onDecline,
        containerColor = Surface1,
        title = { Text(s.consentTitle) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(s.consentIntro, style = MaterialTheme.typography.bodyMedium)
                Text(s.consentData, style = MaterialTheme.typography.bodyMedium)
                Text(s.consentWhy, style = MaterialTheme.typography.bodyMedium)
                Text(s.consentNot, style = MaterialTheme.typography.bodyMedium)
                Text(s.consentOff, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        },
        confirmButton = { Button(onClick = onAccept) { Text(s.consentAccept) } },
        dismissButton = { TextButton(onClick = onDecline) { Text(s.consentLater) } },
    )
}
