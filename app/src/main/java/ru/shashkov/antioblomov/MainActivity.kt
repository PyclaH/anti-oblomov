package ru.shashkov.antioblomov

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.shashkov.antioblomov.blocking.ServiceState
import ru.shashkov.antioblomov.blocking.blockingServiceState
import ru.shashkov.antioblomov.ui.AntiOblomovApp
import ru.shashkov.antioblomov.ui.Tab
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme

/**
 * Единственный экран-«окно» приложения. Всё остальное рисует Compose.
 */
class MainActivity : ComponentActivity() {

    /** Состояние службы блокировки. Обновляется при каждом возврате в приложение. */
    private var serviceState by mutableStateOf(ServiceState.OFF)

    /** Вкладка, которую нужно открыть (например, «Таймер» по нажатию на уведомление). */
    private var requestedTab by mutableStateOf<Tab?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Интерфейс на всю высоту экрана; тёмные значки в строке состояния,
        // потому что фон приложения светлый.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        requestedTab = tabFrom(intent)
        setContent {
            AntiOblomovTheme {
                AntiOblomovApp(
                    serviceState = serviceState,
                    requestedTab = requestedTab,
                    onTabOpened = { requestedTab = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        tabFrom(intent)?.let { requestedTab = it }
    }

    private fun tabFrom(intent: Intent?): Tab? =
        if (intent?.getStringExtra(EXTRA_TAB) == TAB_TIMER) Tab.TIMER else null

    override fun onResume() {
        super.onResume()
        // Пользователь мог только что включить службу в настройках
        serviceState = blockingServiceState(this)
    }

    companion object {
        const val EXTRA_TAB = "tab"
        const val TAB_TIMER = "timer"
    }
}
