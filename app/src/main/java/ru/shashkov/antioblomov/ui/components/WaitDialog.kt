package ru.shashkov.antioblomov.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import ru.shashkov.antioblomov.blocking.Quotes
import ru.shashkov.antioblomov.blocking.SceneBackgroundView
import ru.shashkov.antioblomov.rules.ChangeGuard
import ru.shashkov.antioblomov.ui.theme.Ink
import ru.shashkov.antioblomov.ui.theme.Paper

/**
 * Пауза перед изменением настроек во время блокировки (ChangeGuard):
 * во весь экран — цитата на фоне-картинке, как на экране блокировки,
 * и обратный отсчёт. «Подтвердить» становится доступна через 15 секунд.
 * «Отмена» и кнопка «Назад» — ничего не меняют.
 */
@Composable
fun WaitDialog(title: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val quote = remember { Quotes.random() }
    var left by remember { mutableIntStateOf(ChangeGuard.WAIT_SECONDS) }

    LaunchedEffect(Unit) {
        while (left > 0) {
            delay(1_000)
            left--
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(Modifier.fillMaxSize().background(Ink)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx -> SceneBackgroundView(ctx, quote.scene, seed = quote.text.hashCode()) },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    color = Paper,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Сейчас идёт блокировка. Подумай ${ChangeGuard.WAIT_SECONDS} секунд — точно нужно?",
                    modifier = Modifier.fillMaxWidth(),
                    color = Paper.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "«${quote.text}»",
                    modifier = Modifier.fillMaxWidth(),
                    color = Paper,
                    fontFamily = FontFamily.Serif,
                    fontSize = quoteSize(quote.text),
                    lineHeight = quoteSize(quote.text) * 1.2f,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = quote.source,
                    modifier = Modifier.fillMaxWidth(),
                    color = Paper.copy(alpha = 0.7f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(1f))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Paper, contentColor = Ink),
                ) {
                    Text("Не менять", fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = left == 0,
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Paper,
                        disabledContentColor = Paper.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(if (left > 0) "Подтвердить через $left с" else "Подтвердить", fontSize = 16.sp)
                }
            }
        }
    }
}

/** Длинные цитаты — мельче, как на экране блокировки. */
private fun quoteSize(text: String) = when {
    text.length <= 40 -> 32.sp
    text.length <= 90 -> 26.sp
    else -> 22.sp
}
