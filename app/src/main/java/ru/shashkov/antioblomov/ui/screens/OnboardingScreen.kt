package ru.shashkov.antioblomov.ui.screens

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
import ru.shashkov.antioblomov.blocking.TimerNotification
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
            ) { Text("Выбрать приложения", style = MaterialTheme.typography.labelLarge) }
            TextButton(modifier = Modifier.fillMaxWidth(), onClick = { onFinish(false) }) {
                Text("На главную", color = TextMuted)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (index > 0) {
                    TextButton(onClick = { index-- }, modifier = Modifier.height(56.dp)) { Text("Назад", color = TextMuted) }
                }
                Spacer(Modifier.weight(1f))
                if (done || page == OnboardingPage.WELCOME) {
                    Button(
                        onClick = { index++ },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp),
                    ) { Text(if (page == OnboardingPage.WELCOME) "Начать" else "Далее", style = MaterialTheme.typography.labelLarge) }
                } else {
                    OutlinedButton(
                        onClick = { index++ },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Line),
                        contentPadding = PaddingValues(horizontal = 28.dp),
                    ) { Text("Пропустить", color = TextPrimary) }
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
    Title("Настроим «Анти-Обломов»")
    Body(
        "Приложение закрывает выбранные соцсети и игры в учебное время — по расписанию или таймеру — " +
            "и показывает вместо них цитату."
    )
    Body(
        "Чтобы блокировка работала всегда, телефону нужно разрешить несколько вещей. " +
            "Это займёт 2–3 минуты. Любой шаг можно пропустить и вернуться к нему с главного экрана."
    )
}

@Composable
private fun ServicePage(setup: SetupState) {
    val context = LocalContext.current
    var showRestrictedHelp by rememberSaveable { mutableStateOf(false) }
    // Первый раз — окно согласия (Google Play), потом сразу настройки
    val openService = rememberAccessibilityOpener()

    Title("Служба блокировки")
    Body("Чтобы закрывать выбранные приложения, «Анти-Обломову» нужна служба специальных возможностей Android.")
    InfoBox(
        title = "Что делает служба",
        lines = listOf(
            "Узнаёт название приложения, которое открылось на экране",
            "Если для него сейчас время блокировки — показывает экран с цитатой",
        ),
    )
    InfoBox(
        title = "Чего служба не делает",
        lines = listOf(
            "Не читает текст на экране, сообщения и пароли",
            "Ничего не отправляет в интернет — всё остаётся на телефоне",
        ),
    )
    Steps(
        listOf(
            "Нажмите «Открыть спецвозможности»",
            "Найдите «Анти-Обломов: блокировка» — бывает в разделе «Скачанные приложения» или «Установленные службы»",
            "Включите и подтвердите. Телефон может попросить подождать несколько секунд — это нормально",
        )
    )
    Status(setup.serviceRunning, okText = "Служба работает", notText = "Служба пока не включена")
    if (!setup.serviceRunning) {
        Button(modifier = Modifier.fillMaxWidth(), onClick = openService) {
            Text("Открыть спецвозможности")
        }
        // Android 13+: у приложений не из магазина переключатель бывает серым («Ограниченная настройка»)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            TextButton(onClick = { showRestrictedHelp = !showRestrictedHelp }) {
                Text("Переключатель серый и не включается?")
            }
            if (showRestrictedHelp) {
                Steps(SetupGuide.RESTRICTED_SETTINGS_HELP)
                OutlinedButton(onClick = { openAppSettings(context) }) { Text("Открыть «О приложении»") }
            }
        }
    }
}

@Composable
private fun NotificationsPage(setup: SetupState, onRefresh: () -> Unit) {
    val context = LocalContext.current
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onRefresh() }

    Title("Уведомления")
    Body("Пока идёт блокировка по таймеру, в шторке видно, сколько осталось. Больше уведомлений приложение не присылает.")
    Status(setup.notificationsAllowed, okText = "Уведомления разрешены", notText = "Уведомления выключены")
    if (!setup.notificationsAllowed) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !TimerNotification.allowed(context) &&
                    androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                if (needsPermission) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                else openNotificationSettings(context)
            },
        ) { Text("Разрешить уведомления") }
        // Если отказали дважды, Android больше не показывает запрос — тогда только через настройки
        TextButton(onClick = { openNotificationSettings(context) }) { Text("Открыть настройки уведомлений") }
    }
}

@Composable
private fun BatteryPage(setup: SetupState, onExtraBatteryConfirmed: (Boolean) -> Unit) {
    val context = LocalContext.current
    val extra = SetupGuide.extraBattery(setup.brand)

    Title("Батарея")
    Body("Ради экономии заряда система останавливает приложения, которые работают в фоне, — и блокировка тихо перестаёт работать.")
    Status(setup.batteryUnrestricted, okText = "Работа в фоне разрешена", notText = "Работа в фоне ограничена")
    if (!setup.batteryUnrestricted) {
        Body("Нажмите кнопку и в окне выберите «Разрешить». Если вместо окна открылся список — найдите «Анти-Обломов» и выберите «Не экономить» или «Без ограничений».")
        Button(modifier = Modifier.fillMaxWidth(), onClick = { requestBatteryUnrestricted(context) }) {
            Text("Разрешить работу в фоне")
        }
    }
    if (extra != null) {
        GuideBlock(extra, confirmed = setup.extraBatteryConfirmed, onConfirmed = onExtraBatteryConfirmed)
    }
}

@Composable
private fun AutostartPage(setup: SetupState, onAutostartConfirmed: (Boolean) -> Unit) {
    val guide = SetupGuide.autostart(setup.brand) ?: return
    Title(guide.title)
    GuideBlock(guide, confirmed = setup.autostartConfirmed, onConfirmed = onAutostartConfirmed, showTitle = false)
}

@Composable
private fun UsagePage(setup: SetupState) {
    val context = LocalContext.current
    Title("Статистика «Было / стало»")
    Body(
        "На главной будет видно, сколько времени вы проводили в выбранных приложениях до «Анти-Обломова» " +
            "и сколько теперь — и сколько времени вернули себе."
    )
    InfoBox(
        title = "Что для этого читается",
        lines = listOf(
            "Сколько минут каждое приложение было на экране — та же статистика, что в «Цифровом благополучии»",
            "Данные остаются на телефоне и никуда не отправляются",
        ),
    )
    Steps(
        listOf(
            "Нажмите «Открыть настройки»",
            "Найдите «Анти-Обломов» и включите «Доступ к истории использования»",
        )
    )
    Status(setup.usageAccess, okText = "Доступ к статистике есть", notText = "Доступа к статистике пока нет")
    if (!setup.usageAccess) {
        Button(modifier = Modifier.fillMaxWidth(), onClick = { UsageCollector.openSettings(context) }) {
            Text("Открыть настройки")
        }
    }
}

@Composable
private fun FinishPage(setup: SetupState) {
    val missing = SetupChecklist.missing(setup)
    Title(if (missing.isEmpty()) "Телефон настроен" else "Почти готово")
    SetupChecklist.items(setup.brand).forEach { item ->
        SetupRow(title = itemTitle(item, setup), done = SetupChecklist.isDone(item, setup))
    }
    if (missing.isNotEmpty()) {
        Body("Пропущенное можно настроить позже: на главной — карточка «Настройка телефона».")
    }
    Body("Осталось выбрать, что блокировать, и задать расписание или запустить таймер.")
}

// ---------- общие куски ----------

/** Инструкция оболочки: зачем, что нажать, кнопка и отметка «Сделано» (проверить из приложения нельзя). */
@Composable
private fun GuideBlock(guide: Guide, confirmed: Boolean, onConfirmed: (Boolean) -> Unit, showTitle: Boolean = true) {
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
        "Если открылась страница «О приложении» — ищи эту настройку там.",
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
        Text("Сделано", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Title(text: String) =
    Text(text = text, style = MaterialTheme.typography.headlineMedium)

@Composable
private fun Body(text: String) =
    Text(text = text, style = MaterialTheme.typography.bodyLarge)

/** Шаги инструкции — номер в кружке и текст. */
@Composable
private fun Steps(lines: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        lines.forEachIndexed { i, line ->
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
        Text(title.uppercase(), style = Overline, color = TextMuted)
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
