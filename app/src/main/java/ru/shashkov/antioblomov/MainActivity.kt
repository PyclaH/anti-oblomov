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
import ru.shashkov.antioblomov.blocking.readSetupState
import ru.shashkov.antioblomov.data.OnboardingStore
import ru.shashkov.antioblomov.data.UsageCollector
import kotlin.concurrent.thread
import ru.shashkov.antioblomov.rules.SetupState
import ru.shashkov.antioblomov.ui.AntiOblomovApp
import ru.shashkov.antioblomov.ui.Tab
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme

/**
 * Единственный экран-«окно» приложения. Всё остальное рисует Compose.
 */
class MainActivity : ComponentActivity() {

    /** Состояние службы блокировки. Обновляется при каждом возврате в приложение. */
    private var serviceState by mutableStateOf(ServiceState.OFF)

    private lateinit var onboarding: OnboardingStore

    /** Что настроено на телефоне — для мастера и главной. Пересчитывается при каждом возврате. */
    private var setup by mutableStateOf<SetupState?>(null)

    /** Показывать мастер настройки: при первом запуске и по кнопке на главной. */
    private var showOnboarding by mutableStateOf(false)

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
        onboarding = OnboardingStore(this)
        showOnboarding = !onboarding.completed
        refreshSetup()
        requestedTab = tabFrom(intent)
        setContent {
            AntiOblomovTheme {
                AntiOblomovApp(
                    serviceState = serviceState,
                    setup = setup ?: readSetupState(this, onboarding),
                    showOnboarding = showOnboarding,
                    onOpenOnboarding = { showOnboarding = true },
                    onAutostartConfirmed = { onboarding.autostartConfirmed = it; refreshSetup() },
                    onExtraBatteryConfirmed = { onboarding.extraBatteryConfirmed = it; refreshSetup() },
                    onRefreshSetup = ::refreshSetup,
                    onOnboardingFinished = { openApps ->
                        onboarding.completed = true
                        showOnboarding = false
                        if (openApps) requestedTab = Tab.APPS
                    },
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
        refreshSetup()
    }

    private fun refreshSetup() {
        serviceState = blockingServiceState(this)
        setup = readSetupState(this, onboarding)
        // Доступ к статистике могли только что выдать — сразу делаем снимок «до установки»
        if (setup?.usageAccess == true) thread { UsageCollector.update(applicationContext) }
    }

    companion object {
        const val EXTRA_TAB = "tab"
        const val TAB_TIMER = "timer"
    }
}
