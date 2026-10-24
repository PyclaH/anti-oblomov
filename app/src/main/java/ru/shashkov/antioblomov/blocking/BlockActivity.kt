package ru.shashkov.antioblomov.blocking

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme
import ru.shashkov.antioblomov.ui.theme.Ink
import ru.shashkov.antioblomov.ui.theme.Paper
import ru.shashkov.antioblomov.ui.theme.Terracotta

/**
 * Экран блокировки: показывается вместо заблокированного приложения.
 * Под ним уже домашний экран телефона (служба отправила туда пользователя),
 * поэтому «Вернуться к делу» и кнопка «Назад» просто закрывают этот экран.
 */
class BlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val pkg = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        val appName = appLabel(pkg)

        setContent {
            AntiOblomovTheme {
                BlockScreen(appName = appName, onBack = { finish() })
            }
        }
    }

    private fun appLabel(pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault("Это приложение")

    companion object {
        const val EXTRA_PACKAGE = "package"
    }
}

@Composable
private fun BlockScreen(appName: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .safeDrawingPadding()
            .padding(horizontal = 28.dp, vertical = 32.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Terracotta),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(104.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "$appName на паузе",
                style = MaterialTheme.typography.titleMedium,
                color = Paper.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            // Шаг 1: одна цитата. Подборку из 40 цитат подключим на шаге 6.
            Text(
                text = "«Теперь или никогда!»",
                fontFamily = FontFamily.Serif,
                fontSize = 34.sp,
                lineHeight = 42.sp,
                color = Paper,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "И. А. Гончаров, «Обломов»",
                style = MaterialTheme.typography.bodyMedium,
                color = Paper.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Paper, contentColor = Ink),
            ) {
                Text("Вернуться к делу", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun BlockScreenPreview() {
    AntiOblomovTheme { BlockScreen(appName = "YouTube", onBack = {}) }
}
