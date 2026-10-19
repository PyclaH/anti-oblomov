package ru.shashkov.antioblomov

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.shashkov.antioblomov.ui.AntiOblomovApp
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme

/**
 * Единственный экран-«окно» приложения. Всё остальное рисует Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Интерфейс на всю высоту экрана; тёмные значки в строке состояния,
        // потому что фон приложения светлый.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            AntiOblomovTheme {
                AntiOblomovApp()
            }
        }
    }
}
