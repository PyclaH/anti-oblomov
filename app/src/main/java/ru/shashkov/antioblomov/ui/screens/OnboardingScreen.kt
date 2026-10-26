package ru.shashkov.antioblomov.ui.screens

import java.util.Locale
import ru.shashkov.antioblomov.rules.i18n.SystemNames
import ru.shashkov.antioblomov.ui.components.rememberAccessibilityOpener
import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.ui.components.AppCard
import ru.shashkov.antioblomov.ui.theme.BigNumber
import ru.shashkov.antioblomov.ui.theme.Line
import ru.shashkov.antioblomov.ui.theme.Overline
import ru.shashkov.antioblomov.ui.theme.Surface1
import ru.shashkov.antioblomov.ui.theme.Surface2
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import ru.shashkov.antioblomov.ui.theme.Warn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.blocking.BlockNotification
import ru.shashkov.antioblomov.data.UsageCollector
import ru.shashkov.antioblomov.blocking.openAppSettings
import ru.shashkov.antioblomov.blocking.openGuide
import ru.shashkov.antioblomov.blocking.openNotificationSettings
import ru.shashkov.antioblomov.blocking.requestBatteryUnrestricted
import ru.shashkov.antioblomov.rules.Guide
import ru.shashkov.antioblomov.rules.Onboarding
import ru.shashkov.antioblomov.rules.OnboardingPage
import ru.shashkov.antioblomov.rules.SetupChecklist
import ru.shashkov.antioblomov.rules.SetupGuide
import ru.shashkov.antioblomov.rules.SetupItem
import ru.shashkov.antioblomov.rules.SetupState
import ru.shashkov.antioblomov.rules.i18n.Lang
import ru.shashkov.antioblomov.ui.i18n.LocalSetLanguage
import ru.shashkov.antioblomov.ui.i18n.LocalStrings
import ru.shashkov.antioblomov.ui.components.SetupRow
import ru.shashkov.antioblomov.ui.components.itemTitle
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.Accent

/**
 * Мастер настройки (FR-08): по шагу на каждое разрешение, с объяснением зачем.
 * Любой шаг можно пропустить — пропущенное видно на главной («Настройка телефона»).
 * Что настроено, проверяется при каждом возврате в приложение ([setup] пересчитывает MainActivity).
 *
 * @param onFinish true — сразу перейти к выбору приложений
 */
@Composable
fun OnboardingScreen(
    setup: SetupState,
    onAutostartConfirmed: (Boolean) -> Unit,
    onExtraBatteryConfirmed: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onFinish: (openApps: Boolean) -> Unit,
) {
    val str = LocalStrings.current
    val s = str.setup
    val pages = Onboarding.pages(setup.brand)
    var index by rememberSaveable { mutableIntStateOf(0) }
    val page = pages[index.coerceIn(pages.indices)]
    val done = pageDone(page, setup)

    BackHandler(enabled = index > 0) { index-- }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        StepHeader(index = index, total = pages.size)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PageIcon(page)
            when (page) {
                OnboardingPage.WELCOME -> WelcomePage()
                OnboardingPage.SERVICE -> ServicePage(setup)
                OnboardingPage.NOTIFICATIONS -> NotificationsPage(setup, onRefresh)
                OnboardingPage.BATTERY -> BatteryPage(setup, onExtraBatteryConfirmed)
                OnboardingPage.AUTOSTART -> AutostartPage(setup, onAutostartConfirmed)
                OnboardingPage.USAGE -> UsagePage(setup)
                OnboardingPage.FINISH -> FinishPage(setup)
            }
        }

        if (page == OnboardingPage.FINISH) {
            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                onClick = { onFinish(true) },
            ) { Text(s.chooseApps, style = MaterialTheme.typography.labelLarge) }
            TextButton(modifier = Modifier.fillMaxWidth(), onClick = { onFinish(false) }) {
                Text(s.toHome, color = TextMuted)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (index > 0) {
                    TextButton(onClick = { index-- }, modifier = Modifier.height(56.dp)) { Text(str.common.back, color = TextMuted) }
                }
                Spacer(Modifier.weight(1f))
                if (done || page == OnboardingPage.WELCOME) {
                    Button(
                        onClick = { index++ },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp),
                    ) { Text(if (page == OnboardingPage.WELCOME) s.start else str.common.next, style = MaterialTheme.typography.labelLarge) }
                } else {
                    OutlinedButton(
                        onClick = { index++ },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Line),
                        contentPadding = PaddingValues(horizontal = 28.dp),
                    ) { Text(str.common.skip, color = TextPrimary) }
                }
            }
        }
    }
}

private fun pageDone(page: OnboardingPage, setup: SetupState): Boolean = when (page) {
    OnboardingPage.SERVICE -> SetupChecklist.isDone(SetupItem.SERVICE, setup)
    OnboardingPage.NOTIFICATIONS -> SetupChecklist.isDone(SetupItem.NOTIFICATIONS, setup)
    OnboardingPage.BATTERY -> SetupChecklist.isDone(SetupItem.BATTERY, setup)
    OnboardingPage.AUTOSTART -> SetupChecklist.isDone(SetupItem.AUTOSTART, setup)
    OnboardingPage.USAGE -> SetupChecklist.isDone(SetupItem.USAGE, setup)
    OnboardingPage.WELCOME, OnboardingPage.FINISH -> true
}

// ---------- страницы ----------

@Composable
private fun WelcomePage() {
    val s = LocalStrings.current.setup
    Title(s.welcomeTitle)
    Body(s.welcomeIntro)
    Body(s.welcomeHow)
    LanguagePicker()
}

/**
 * Выбор языка на первой странице (v1.2): подпись «ЯЗЫК» и сетка 2 колонки.
 * Нажатие сразу меняет язык — мастер перерисовывается на новом.
 * Не LazyGrid: страница уже прокручивается.
 */
@Composable
private fun LanguagePicker() {
    val str = LocalStrings.current
    val setLanguage = LocalSetLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            str.common.language.uppercase(Locale.forLanguageTag(str.lang.code)),
            style = Overline,
            color = TextMuted,
            modifier = Modifier.padding(top = 8.dp),
        )
        Lang.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { lang ->
                    LanguageCell(
                        lang = lang,
                        selected = lang == str.lang,
                        onClick = { setLanguage(lang) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Нечётное число языков: пустая ячейка, чтобы последняя не растягивалась на всю ширину
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LanguageCell(lang: Lang, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (selected) Surface2 else Surface1)
            .border(1.dp, if (selected) TextMuted else Line, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            lang.nativeName,
            fontSize = 15.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        // Радио-кружок: выбранный — терракотовое кольцо с точкой
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(if (selected) 2.dp else 1.dp, if (selected) Accent else Line, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Accent)
                )
            }
        }
    }
}

@Composable
private fun ServicePage(setup: SetupState) {
    val s = LocalStrings.current.setup
    val context = LocalContext.current
    var showRestrictedHelp by rememberSaveable { mutableStateOf(false) }
    // Первый раз — окно согласия (Google Play), потом сразу настройки
    val openService = rememberAccessibilityOpener()

    Title(s.serviceTitle)
    Body(s.serviceWhy)
    InfoBox(title = s.serviceDoesTitle, lines = s.serviceDoes)
    InfoBox(title = s.serviceDoesNotTitle, lines = s.serviceDoesNot)
    Steps(s.serviceSteps)
    Status(setup.serviceRunning, okText = s.serviceOn, notText = s.serviceOff)
    if (!setup.serviceRunning) {
        Button(modifier = Modifier.fillMaxWidth(), onClick = openService) {
            Text(s.openAccessibility)
        }
        // Android 13+: у приложений не из магазина переключатель бывает серым («Ограниченная настройка»)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            TextButton(onClick = { showRestrictedHelp = !showRestrictedHelp }) {
                Text(s.restrictedQuestion)
            }
            if (showRestrictedHelp) {
                Steps(SetupGuide.restrictedSettingsHelp(s))
                OutlinedButton(onClick = { openAppSettings(context) }) { Text(s.openAppInfo) }
            }
        }
    }
}

@Composable
private fun NotificationsPage(setup: SetupState, onRefresh: () -> Unit) {
    val s = LocalStrings.current.setup
    val context = LocalContext.current
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onRefresh() }

    Title(s.notificationsTitle)
    Body(s.notificationsWhy)
    Status(setup.notificationsAllowed, okText = s.notificationsOn, notText = s.notificationsOff)
    if (!setup.notificationsAllowed) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !BlockNotification.allowed(context) &&
                    androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                if (needsPermission) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                else openNotificationSettings(context)
            },
        ) { Text(s.allowNotifications) }
        // Если отказали дважды, Android больше не показывает запрос — тогда только через настройки
        TextButton(onClick = { openNotificationSettings(context) }) { Text(s.openNotificationSettings) }
    }
}

@Composable
private fun BatteryPage(setup: SetupState, onExtraBatteryConfirmed: (Boolean) -> Unit) {
    val s = LocalStrings.current.setup
    val context = LocalContext.current
    val extra = SetupGuide.extraBattery(setup.brand, s)

    Title(s.batteryTitle)
    Body(s.batteryWhy)
    Status(setup.batteryUnrestricted, okText = s.batteryOn, notText = s.batteryOff)
    if (!setup.batteryUnrestricted) {
        Body(s.batteryHint)
        Button(modifier = Modifier.fillMaxWidth(), onClick = { requestBatteryUnrestricted(context) }) {
            Text(s.allowBackground)
        }
    }
    if (extra != null) {
        GuideBlock(extra, confirmed = setup.extraBatteryConfirmed, onConfirmed = onExtraBatteryConfirmed)
    }
}

@Composable
private fun AutostartPage(setup: SetupState, onAutostartConfirmed: (Boolean) -> Unit) {
    val s = LocalStrings.current.setup
    val guide = SetupGuide.autostart(setup.brand, s) ?: return
    Title(guide.title)
    GuideBlock(guide, confirmed = setup.autostartConfirmed, onConfirmed = onAutostartConfirmed, showTitle = false)
}

@Composable
private fun UsagePage(setup: SetupState) {
    val s = LocalStrings.current.setup
    val context = LocalContext.current
    Title(s.usageTitle)
    Body(s.usageWhy)
    InfoBox(title = s.usageReadsTitle, lines = s.usageReads)
    Steps(s.usageSteps)
    Status(setup.usageAccess, okText = s.usageOn, notText = s.usageOff)
    if (!setup.usageAccess) {
        Button(modifier = Modifier.fillMaxWidth(), onClick = { UsageCollector.openSettings(context) }) {
            Text(s.openSettings)
        }
    }
}

@Composable
private fun FinishPage(setup: SetupState) {
    val s = LocalStrings.current.setup
    val missing = SetupChecklist.missing(setup)
    Title(if (missing.isEmpty()) s.finishDone else s.finishAlmost)
    SetupChecklist.items(setup.brand).forEach { item ->
        SetupRow(title = itemTitle(item, setup, s), done = SetupChecklist.isDone(item, setup))
    }
    if (missing.isNotEmpty()) {
        Body(s.finishSkipped)
    }
    Body(s.finishRest)
}

// ---------- общие куски ----------

/** Инструкция оболочки: зачем, что нажать, кнопка и отметка «Сделано» (проверить из приложения нельзя). */
@Composable
private fun GuideBlock(guide: Guide, confirmed: Boolean, onConfirmed: (Boolean) -> Unit, showTitle: Boolean = true) {
    val s = LocalStrings.current.setup
    val context = LocalContext.current
    if (showTitle) Text(guide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Body(guide.why)
    Steps(guide.steps)
    Button(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(18.dp),
        onClick = { openGuide(context, guide) },
    ) { Text(guide.buttonText) }
    Text(
        s.guideFallback,
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onConfirmed(!confirmed) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = confirmed, onCheckedChange = onConfirmed)
        Text(s.markDone, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Title(text: String) =
    Text(text = text, style = MaterialTheme.typography.headlineMedium)

@Composable
private fun Body(text: String) =
    Text(text = systemNames(text), style = MaterialTheme.typography.bodyLarge)

/**
 * Названия приложения и службы в подсказке — как их покажет телефон (на языке телефона,
 * а не приложения), см. SystemNames.
 */
@Composable
private fun systemNames(text: String): String {
    val context = LocalContext.current
    return SystemNames.fix(
        text,
        LocalStrings.current.lang,
        context.getString(R.string.app_name),
        context.getString(R.string.accessibility_service_label),
    )
}

/** Шаги инструкции — номер в кружке и текст. */
@Composable
private fun Steps(lines: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        lines.map { systemNames(it) }.forEachIndexed { i, line ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.dp, Line, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                }
                Text(line, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Блок «что делает / чего не делает» — карточка с подписью мелкими прописными. */
@Composable
private fun InfoBox(title: String, lines: List<String>) {
    AppCard(spacing = 6.dp) {
        Text(title.uppercase(Locale.forLanguageTag(LocalStrings.current.lang.code)), style = Overline, color = TextMuted)
        lines.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
    }
}

/** Статус шага: ✓ бирюзовая галочка или янтарный значок. */
@Composable
private fun Status(ok: Boolean, okText: String, notText: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(
            imageVector = if (ok) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
            contentDescription = null,
            tint = if (ok) Calm else Warn,
        )
        Text(
            text = if (ok) okText else notText,
            style = MaterialTheme.typography.titleSmall,
            color = if (ok) Calm else Warn,
        )
    }
}

/** «02 / 07» крупно и тонкая полоса прогресса (редизайн R4). */
@Composable
private fun StepHeader(index: Int, total: Int) {
    val progress by animateFloatAsState((index + 1f) / total, tween(300), label = "steps")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("%02d".format(index + 1), style = BigNumber.copy(fontSize = 36.sp, lineHeight = 40.sp))
            Text(
                " / %02d".format(total),
                style = MaterialTheme.typography.titleMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Surface2)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(3.dp)
                    .background(Accent)
            )
        }
    }
}

/** Линейная иконка шага в скруглённом квадрате. */
@Composable
private fun PageIcon(page: OnboardingPage) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Surface1)
            .border(1.dp, Line, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        when (page) {
            OnboardingPage.WELCOME -> Image(
                painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(84.dp),
            )
            OnboardingPage.SERVICE -> Icon(Icons.Outlined.Lock, null, tint = TextPrimary)
            OnboardingPage.NOTIFICATIONS -> Icon(Icons.Outlined.Notifications, null, tint = TextPrimary)
            OnboardingPage.BATTERY -> Canvas(Modifier.size(24.dp)) {
                // Батарейка: корпус, «плюс» сверху, заряд
                val w = size.width * 0.5f
                val h = size.height * 0.8f
                val left = (size.width - w) / 2
                val top = size.height * 0.15f
                drawRoundRect(TextPrimary, Offset(left, top), Size(w, h), CornerRadius(3.dp.toPx()), style = Stroke(1.6.dp.toPx()))
                drawRect(TextPrimary, Offset(size.width / 2 - w / 4, top - 2.5.dp.toPx()), Size(w / 2, 2.5.dp.toPx()))
                drawRect(Accent, Offset(left + 3.dp.toPx(), top + h * 0.45f), Size(w - 6.dp.toPx(), h * 0.55f - 3.dp.toPx()))
            }
            OnboardingPage.AUTOSTART -> Icon(Icons.Outlined.Refresh, null, tint = TextPrimary)
            OnboardingPage.USAGE -> Canvas(Modifier.size(24.dp)) {
                // Столбики статистики
                val bw = size.width / 6
                listOf(0.45f, 0.8f, 0.6f).forEachIndexed { i, k ->
                    val x = size.width * 0.15f + i * bw * 1.6f
                    val bh = size.height * k
                    drawRoundRect(
                        if (i == 1) Accent else TextPrimary,
                        Offset(x, size.height - bh),
                        Size(bw, bh),
                        CornerRadius(1.5.dp.toPx()),
                    )
                }
            }
            OnboardingPage.FINISH -> Icon(Icons.Outlined.CheckCircle, null, tint = Calm)
        }
    }
}
