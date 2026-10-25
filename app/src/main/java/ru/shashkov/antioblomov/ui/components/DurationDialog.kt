package ru.shashkov.antioblomov.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.shashkov.antioblomov.rules.DurationCheck
import ru.shashkov.antioblomov.rules.TimerRules
import ru.shashkov.antioblomov.ui.theme.Terracotta

/**
 * Окно «на сколько»: часы и минуты набираются цифрами с клавиатуры.
 * Итог — от 1 минуты до 12 часов (TimerRules.parseDuration).
 * [preview] — подпись под полями для набранной длительности, например «до 19:30».
 */
@Composable
fun DurationDialog(
    title: String,
    confirmText: String,
    initialHours: Int,
    initialMinutes: Int,
    preview: (minutes: Int) -> String,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int) -> Unit,
) {
    var hours by remember { mutableStateOf(selectedAll(initialHours.toString())) }
    var minutes by remember { mutableStateOf(selectedAll(initialMinutes.toString())) }
    val hoursFocus = remember { FocusRequester() }
    val minutesFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val check = TimerRules.parseDuration(hours.text, minutes.text)
    val confirm = { (check as? DurationCheck.Ok)?.let { onConfirm(it.minutes) } }

    LaunchedEffect(Unit) {
        delay(150) // окно диалога должно успеть появиться
        hoursFocus.requestFocus()
        keyboard?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    NumberField(
                        label = "Часы",
                        value = hours,
                        onValueChange = { v ->
                            val typed = v.text != hours.text
                            hours = digitsOnly(v)
                            // Две цифры часов набраны — сразу к минутам
                            if (typed && hours.text.length == 2) {
                                minutes = selectedAll(minutes.text)
                                minutesFocus.requestFocus()
                            }
                        },
                        imeAction = ImeAction.Next,
                        onIme = { minutes = selectedAll(minutes.text); minutesFocus.requestFocus() },
                        modifier = Modifier.weight(1f).focusRequester(hoursFocus),
                    )
                    NumberField(
                        label = "Минуты",
                        value = minutes,
                        onValueChange = { v -> minutes = digitsOnly(v) },
                        imeAction = ImeAction.Done,
                        onIme = { confirm() },
                        modifier = Modifier.weight(1f).focusRequester(minutesFocus),
                    )
                }
                Text(
                    text = "От 1 минуты до 12 часов. Пустое поле — 0.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when (check) {
                    is DurationCheck.Ok -> Text(
                        text = "${TimerRules.durationText(check.minutes)} · ${preview(check.minutes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    is DurationCheck.Error -> Text(
                        text = check.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Terracotta,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = check is DurationCheck.Ok, onClick = { confirm() }) { Text(confirmText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun NumberField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    imeAction: ImeAction,
    onIme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onIme() }, onDone = { onIme() }),
    )
}

/** Текст выделен целиком — первая набранная цифра заменит старое значение. */
private fun selectedAll(text: String) = TextFieldValue(text, TextRange(0, text.length))

/** Только цифры, не больше трёх (на цифровой клавиатуре бывают «.», «,» и «-»). */
private fun digitsOnly(v: TextFieldValue): TextFieldValue {
    val digits = v.text.filter { it.isDigit() }.take(3)
    return if (digits == v.text) v else TextFieldValue(digits, TextRange(digits.length))
}
