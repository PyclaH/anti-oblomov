package ru.shashkov.antioblomov

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.shashkov.antioblomov.blocking.isBlockingServiceEnabled
import ru.shashkov.antioblomov.ui.AntiOblomovApp
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme

/**
 * Единственный экран-«окно» приложения. Всё остальное рисует Compose.
 */
class MainActivity : ComponentActivity() {

    /** Включена ли служба блокировки. Обновляется при каждом возврате в приложение. */
    private var serviceEnabled by mutableStateOf(false)

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
                AntiOblomovApp(serviceEnabled = serviceEnabled)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Пользователь мог только что включить службу в настройках
        serviceEnabled = isBlockingServiceEnabled(this)
    }
}
