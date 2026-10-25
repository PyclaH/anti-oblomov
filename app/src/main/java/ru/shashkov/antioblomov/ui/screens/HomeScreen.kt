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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.shashkov.antioblomov.ui.components.ServiceStatusCard
import ru.shashkov.antioblomov.ui.components.SetupCard
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.Night0
import ru.shashkov.antioblomov.ui.theme.Accent
import java.time.ZonedDateTime

/**
 * Главная (FR-02): крупно — идёт ли сейчас блокировка, ниже — карточка службы блокировки
 * и «Настройка телефона» (что пропущено в мастере, FR-08).
 * Если служба не работает, это видно сразу в статусе: блокировка по правилам есть,
 * но приложения на самом деле не блокируются.
 * Под статусом — «Было / стало» (FR-14…FR-16) и панель «Поблагодарить автора» (FR-17).
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
                containerColor = if (status is BlockStatus.Blocked) Accent else Calm,
                contentColor = Night0,
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Сейчас", style = MaterialTheme.typography.titleSmall, color = Night0.copy(alpha = 0.8f))
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
                        color = Night0.copy(alpha = 0.9f),
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
                color = Accent,
            )
        }

        StatsCard(
            hasAccess = setup.usageAccess,
            report = report,
            startDate = history?.startDate,
            noAppsSelected = tracked.isEmpty(),
            onOpenAccess = { UsageCollector.openSettings(context) },
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

        // Благодарность — всегда, под «Было / стало» и над службой (решение от 25.10.2026)
        ThankAuthorCard(savedMinutesPerDay = report?.savedPerDayMinutes)

        ServiceStatusCard(state = serviceState)
        SetupCard(setup = setup, onOpenWizard = onOpenOnboarding)
    }
}
