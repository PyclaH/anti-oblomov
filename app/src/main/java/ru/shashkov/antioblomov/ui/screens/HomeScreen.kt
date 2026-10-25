package ru.shashkov.antioblomov.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.blocking.ServiceState
import ru.shashkov.antioblomov.data.SettingsStore
import ru.shashkov.antioblomov.rules.BlockRules
import ru.shashkov.antioblomov.rules.BlockStatus
import ru.shashkov.antioblomov.rules.HomeStatusText
import ru.shashkov.antioblomov.rules.TimerRules
import ru.shashkov.antioblomov.rules.SetupState
import ru.shashkov.antioblomov.ui.components.ServiceStatusCard
import ru.shashkov.antioblomov.ui.components.SetupCard
import ru.shashkov.antioblomov.ui.theme.Lake
import ru.shashkov.antioblomov.ui.theme.Paper
import ru.shashkov.antioblomov.ui.theme.Terracotta
import java.time.ZonedDateTime

/**
 * Главная (FR-02): крупно — идёт ли сейчас блокировка, ниже — карточка службы блокировки
 * и «Настройка телефона» (что пропущено в мастере, FR-08).
 * Если служба не работает, это видно сразу в статусе: блокировка по правилам есть,
 * но приложения на самом деле не блокируются.
 * На шаге 7 здесь появится «Было / стало».
 */
@Composable
fun HomeScreen(
    serviceState: ServiceState,
    setup: SetupState,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    var settings by remember { mutableStateOf(store.read()) }
    var now by remember { mutableStateOf(ZonedDateTime.now()) }

    // Раз в секунду — для отсчёта таймера; настройки перечитываем раз в 5 секунд
    LaunchedEffect(Unit) {
        var tick = 0
        while (true) {
            delay(1_000)
            now = ZonedDateTime.now()
            if (++tick % 5 == 0) settings = store.read()
        }
    }

    val status = BlockRules.status(settings, now)
    val serviceWorks = serviceState == ServiceState.RUNNING

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (status is BlockStatus.Blocked) Terracotta else Lake,
                contentColor = Paper,
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Сейчас", style = MaterialTheme.typography.titleSmall, color = Paper.copy(alpha = 0.8f))
                Text(
                    text = HomeStatusText.title(status, now),
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = HomeStatusText.detail(status, now), style = MaterialTheme.typography.titleMedium)
                TimerRules.remainingMillis(settings, now)?.let { left ->
                    Text(
                        text = "таймер: осталось ${TimerRules.countdown(left)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Paper.copy(alpha = 0.9f),
                    )
                }
                if (!serviceWorks) {
                    Text(
                        text = "Служба блокировки не работает — приложения сейчас не блокируются. Как включить — ниже.",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (settings.blockedPackages.isEmpty()) {
            Text(
                text = "Приложения для блокировки не выбраны — отметьте их на вкладке «Приложения»",
                style = MaterialTheme.typography.bodyMedium,
                color = Terracotta,
            )
        }

        ServiceStatusCard(state = serviceState)
        SetupCard(setup = setup, onOpenWizard = onOpenOnboarding)
    }
}
