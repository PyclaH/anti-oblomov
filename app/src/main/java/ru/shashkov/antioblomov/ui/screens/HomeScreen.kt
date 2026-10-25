package ru.shashkov.antioblomov.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import ru.shashkov.antioblomov.rules.SetupChecklist
import ru.shashkov.antioblomov.ui.components.StatusRing
import ru.shashkov.antioblomov.ui.components.SystemBlock
import ru.shashkov.antioblomov.ui.components.statsAppsSummary
import ru.shashkov.antioblomov.ui.theme.Warn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.blocking.ServiceState
import ru.shashkov.antioblomov.data.AppsCache
import ru.shashkov.antioblomov.data.InstalledApps
import ru.shashkov.antioblomov.data.ProtectedApps
import ru.shashkov.antioblomov.data.SettingsStore
import ru.shashkov.antioblomov.data.StatsChoiceStore
import ru.shashkov.antioblomov.rules.StatsApps
import ru.shashkov.antioblomov.ui.components.StatsAppsPicker
import ru.shashkov.antioblomov.data.UsageCollector
import ru.shashkov.antioblomov.data.UsageStore
import ru.shashkov.antioblomov.rules.DayUsage
import ru.shashkov.antioblomov.rules.Savings
import ru.shashkov.antioblomov.rules.UsageHistory
import ru.shashkov.antioblomov.ui.components.StatsCard
import ru.shashkov.antioblomov.ui.components.ThankAuthorCard
import ru.shashkov.antioblomov.rules.BlockRules
import ru.shashkov.antioblomov.rules.BlockStatus
import ru.shashkov.antioblomov.rules.HomeStatusText
import ru.shashkov.antioblomov.rules.TimerRules
import ru.shashkov.antioblomov.rules.SetupState
import java.time.ZonedDateTime

/**
 * Главная (FR-02; редизайн R2): кольцо-циферблат со свечением — идёт ли блокировка и до
 * какого времени; «Было / стало» плитками (FR-14…FR-16); строка «Поблагодарить автора» (FR-17);
 * блок «Система» — служба и «Настройка телефона» (FR-08) в одну строку. Если со службой или
 * настройками что-то не так, «Система» раскрыта и стоит сразу под кольцом.
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

    // «Было / стало»: журнал пополняем и перечитываем раз в минуту (в фоне — долго)
    var history by remember { mutableStateOf<UsageHistory?>(null) }
    var todayUsage by remember { mutableStateOf<DayUsage>(emptyMap()) }
    var notScreenTime by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(setup.usageAccess) {
        if (!setup.usageAccess) return@LaunchedEffect
        notScreenTime = withContext(Dispatchers.IO) { UsageCollector.notScreenTime(context) }
        while (true) {
            val (h, t) = withContext(Dispatchers.IO) {
                UsageCollector.update(context)
                UsageStore(context).read() to UsageCollector.today(context)
            }
            history = h
            todayUsage = t
            delay(60_000)
        }
    }

    // Приложения для статистики — свой список (по умолчанию как список блокировки) или все несистемные
    val statsStore = remember { StatsChoiceStore(context) }
    var statsChoice by remember { mutableStateOf(statsStore.read()) }
    var apps by remember { mutableStateOf(AppsCache.apps) }
    var protectedApps by remember { mutableStateOf(AppsCache.protected) }
    val iconPx = with(LocalDensity.current) { 40.dp.roundToPx() }
    LaunchedEffect(Unit) {
        val (loaded, prot) = withContext(Dispatchers.IO) {
            InstalledApps.load(context, iconPx) to ProtectedApps.of(context)
        }
        AppsCache.apps = loaded
        AppsCache.protected = prot
        apps = loaded
        protectedApps = prot
    }
    val nonSystem = remember(apps, protectedApps) {
        apps.orEmpty().map { it.entry.packageName }.filterNot { it in protectedApps }.toSet()
    }
    val tracked = StatsApps.tracked(statsChoice, settings.blockedPackages, nonSystem)

    val today = now.toLocalDate()
    val report = remember(history, todayUsage, tracked, notScreenTime, today) {
        history?.let { Savings.report(it, today, todayUsage, tracked, notScreenTime) }
    }

    val status = BlockRules.status(settings, now)
    val serviceWorks = serviceState == ServiceState.RUNNING
    // Что-то не так со службой или настройками — блок «Система» раскрыт и стоит сразу под кольцом
    val systemProblem = !serviceWorks || SetupChecklist.missing(setup).isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Шапка: значок и название
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF6E3019), Color(0xFF34150A)))),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(42.dp))
            }
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleSmall)
        }

        StatusRing(
            text = HomeStatusText.ring(status, now),
            progress = BlockRules.blockedProgress(settings, now),
            nothingPlanned = status is BlockStatus.Free && status.nextBlockAt == null,
            serviceWorks = serviceWorks,
            timerLeft = TimerRules.remainingMillis(settings, now)?.let(TimerRules::countdown),
            description = HomeStatusText.title(status, now) + ", " + HomeStatusText.detail(status, now) +
                if (serviceWorks) "" else ". Служба блокировки не работает",
        )

        if (!serviceWorks) {
            Text(
                "Служба блокировки не работает — приложения сейчас не блокируются.",
                style = MaterialTheme.typography.bodySmall,
                color = Warn,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        if (settings.blockedPackages.isEmpty()) {
            Text(
                "Приложения для блокировки не выбраны — отметь их на вкладке «Приложения».",
                style = MaterialTheme.typography.bodySmall,
                color = Warn,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        if (systemProblem) SystemBlock(serviceState, setup, onOpenOnboarding)

        StatsCard(
            hasAccess = setup.usageAccess,
            report = report,
            startDate = history?.startDate,
            noAppsSelected = tracked.isEmpty(),
            onOpenAccess = { UsageCollector.openSettings(context) },
            appsSummary = statsAppsSummary(statsChoice, tracked),
            picker = {
                StatsAppsPicker(
                    apps = apps,
                    protected = protectedApps,
                    choice = statsChoice,
                    tracked = tracked,
                    onChoice = { statsChoice = it; statsStore.write(it) },
                )
            },
        )

        // Благодарность — всегда, под «Было / стало» и над «Системой» (решение от 25.10.2026)
        ThankAuthorCard(savedMinutesPerDay = report?.savedPerDayMinutes)

        if (!systemProblem) SystemBlock(serviceState, setup, onOpenOnboarding)
    }
}
