package ru.shashkov.antioblomov.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.blocking.ServiceState
import ru.shashkov.antioblomov.ui.screens.AppsScreen
import ru.shashkov.antioblomov.ui.screens.HomeScreen
import ru.shashkov.antioblomov.ui.screens.ScheduleScreen
import ru.shashkov.antioblomov.ui.screens.TimerScreen
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme

/** Вкладки нижнего меню. */
enum class Tab(@StringRes val title: Int, val icon: ImageVector) {
    HOME(R.string.tab_home, Icons.Filled.Home),
    SCHEDULE(R.string.tab_schedule, Icons.Filled.DateRange),
    TIMER(R.string.tab_timer, Icons.Filled.PlayArrow),
    APPS(R.string.tab_apps, Icons.Filled.Lock),
}

/**
 * Корневой экран: нижнее меню и содержимое выбранной вкладки.
 */
@Composable
fun AntiOblomovApp(
    serviceState: ServiceState,
    requestedTab: Tab? = null,
    onTabOpened: () -> Unit = {},
) {
    // rememberSaveable — выбранная вкладка не сбросится при повороте экрана
    var current by rememberSaveable { mutableStateOf(Tab.HOME) }

    // Открыть вкладку по просьбе извне (нажатие на уведомление таймера)
    LaunchedEffect(requestedTab) {
        if (requestedTab != null) {
            current = requestedTab
            onTabOpened()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == current,
                        onClick = { current = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.title)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (current) {
            Tab.HOME -> HomeScreen(serviceState = serviceState, modifier = modifier)
            Tab.SCHEDULE -> ScheduleScreen(modifier)
            Tab.TIMER -> TimerScreen(modifier)
            Tab.APPS -> AppsScreen(modifier)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AntiOblomovAppPreview() {
    AntiOblomovTheme { AntiOblomovApp(serviceState = ServiceState.OFF) }
}
