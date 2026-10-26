package ru.shashkov.antioblomov.ui

import ru.shashkov.antioblomov.ui.components.WaitRequest
import ru.shashkov.antioblomov.ui.components.WaitOverlay
import ru.shashkov.antioblomov.ui.components.LocalWaitHost
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.ui.components.AppBackground
import ru.shashkov.antioblomov.ui.theme.Line
import ru.shashkov.antioblomov.ui.theme.Surface1
import ru.shashkov.antioblomov.ui.theme.Surface2
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ru.shashkov.antioblomov.ui.i18n.LocalStrings
import ru.shashkov.antioblomov.rules.i18n.AppsStrings
import androidx.compose.ui.tooling.preview.Preview
import ru.shashkov.antioblomov.blocking.ServiceState
import ru.shashkov.antioblomov.ui.screens.AppsScreen
import ru.shashkov.antioblomov.rules.PhoneBrand
import ru.shashkov.antioblomov.rules.SetupState
import ru.shashkov.antioblomov.ui.screens.HomeScreen
import ru.shashkov.antioblomov.ui.screens.OnboardingScreen
import ru.shashkov.antioblomov.ui.screens.ScheduleScreen
import ru.shashkov.antioblomov.ui.screens.TimerScreen
import ru.shashkov.antioblomov.ui.theme.AntiOblomovTheme

/** Вкладки нижнего меню. Подпись — на языке приложения (v1.2): `tab.title(s.apps)`. */
enum class Tab(val title: (AppsStrings) -> String, val icon: ImageVector) {
    HOME({ it.tabHome }, Icons.Outlined.Home),
    SCHEDULE({ it.tabSchedule }, Icons.Outlined.DateRange),
    TIMER({ it.tabTimer }, Icons.Outlined.PlayArrow),
    APPS({ it.tabApps }, Icons.Outlined.Lock),
}

/**
 * Корневой экран: нижнее меню и содержимое выбранной вкладки.
 */
@Composable
fun AntiOblomovApp(
    serviceState: ServiceState,
    setup: SetupState,
    showOnboarding: Boolean = false,
    onOpenOnboarding: () -> Unit = {},
    onAutostartConfirmed: (Boolean) -> Unit = {},
    onExtraBatteryConfirmed: (Boolean) -> Unit = {},
    onRefreshSetup: () -> Unit = {},
    onOnboardingFinished: (openApps: Boolean) -> Unit = {},
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

    // Мастер настройки — во весь экран, без нижнего меню
    if (showOnboarding) {
        AppBackground {
            OnboardingScreen(
                setup = setup,
                onAutostartConfirmed = onAutostartConfirmed,
                onExtraBatteryConfirmed = onExtraBatteryConfirmed,
                onRefresh = onRefreshSetup,
                onFinish = onOnboardingFinished,
            )
        }
        return
    }

    // Пауза перед ослаблением блокировки — слоем поверх всего, на весь экран (см. WaitDialog)
    val wait = remember { mutableStateOf<WaitRequest?>(null) }

    AppBackground {
        CompositionLocalProvider(LocalWaitHost provides wait) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = TextPrimary,
            bottomBar = { BottomCapsule(current = current, onSelect = { current = it }) },
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            AnimatedContent(
                targetState = current,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "tab",
            ) { tab ->
                when (tab) {
                    Tab.HOME -> HomeScreen(serviceState = serviceState, setup = setup, onOpenOnboarding = onOpenOnboarding, modifier = modifier)
                    Tab.SCHEDULE -> ScheduleScreen(modifier)
                    Tab.TIMER -> TimerScreen(modifier)
                    Tab.APPS -> AppsScreen(modifier)
                }
            }
        }
        }
        WaitOverlay(wait)
    }
}

/**
 * Нижнее меню — плавающая «капсула» (план редизайна): те же 4 вкладки;
 * у выбранной — светлая иконка с подписью на подложке, у остальных только иконки.
 */
@Composable
private fun BottomCapsule(current: Tab, onSelect: (Tab) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 32.dp, end = 32.dp, bottom = 12.dp, top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        val s = LocalStrings.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Surface1.copy(alpha = 0.94f))
                .border(1.dp, Line, RoundedCornerShape(30.dp))
                .selectableGroup()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tab.entries.forEach { tab ->
                val selected = tab == current
                val title = tab.title(s.apps)
                // Подсветка нажатия — та же подложка, что у выбранной вкладки, во всю кнопку.
                // Стандартная «волна» не годится: при нажатии вкладка расширяется под подпись,
                // а волна остаётся размером со старую кнопку — края не подсвечивались.
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val fill by animateColorAsState(
                    targetValue = when {
                        selected -> Surface2
                        pressed -> Surface2.copy(alpha = 0.7f)
                        else -> Color.Transparent
                    },
                    animationSpec = tween(150),
                    label = "tab-fill",
                )
                Row(
                    modifier = Modifier
                        .animateContentSize(tween(200))
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(fill)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            interactionSource = interaction,
                            indication = null,
                            onClick = { onSelect(tab) },
                        )
                        .padding(horizontal = if (selected) 16.dp else 14.dp)
                        .semantics { contentDescription = title },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (selected) TextPrimary else TextMuted,
                        modifier = Modifier.size(22.dp),
                    )
                    if (selected) {
                        Text(text = title, style = MaterialTheme.typography.labelMedium, color = TextPrimary, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AntiOblomovAppPreview() {
    AntiOblomovTheme {
        AntiOblomovApp(
            serviceState = ServiceState.OFF,
            setup = SetupState(PhoneBrand.XIAOMI, false, false, false, false, false, false),
        )
    }
}
