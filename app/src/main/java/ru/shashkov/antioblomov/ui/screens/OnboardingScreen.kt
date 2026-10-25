package ru.shashkov.antioblomov.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import ru.shashkov.antioblomov.blocking.openAccessibilitySettings
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
import ru.shashkov.antioblomov.ui.theme.Lake
import ru.shashkov.antioblomov.ui.theme.Terracotta

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
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Шаг ${index + 1} из ${pages.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { (index + 1f) / pages.size },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
            Button(modifier = Modifier.fillMaxWidth().height(52.dp), onClick = { onFinish(true) }) {
                Text("Выбрать приложения")
            }
            TextButton(modifier = Modifier.fillMaxWidth(), onClick = { onFinish(false) }) { Text("На главную") }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (index > 0) TextButton(onClick = { index-- }) { Text("Назад") }
                Spacer(Modifier.weight(1f))
                if (done || page == OnboardingPage.WELCOME) {
                    Button(onClick = { index++ }) { Text(if (page == OnboardingPage.WELCOME) "Начать" else "Далее") }
                } else {
                    OutlinedButton(onClick = { index++ }) { Text("Пропустить") }
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
        Button(modifier = Modifier.fillMaxWidth(), onClick = { openAccessibilitySettings(context) }) {
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
    Button(modifier = Modifier.fillMaxWidth(), onClick = { openGuide(context, guide) }) { Text(guide.buttonText) }
    Body("Если открылась страница «О приложении» — ищите эту настройку там.")
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

@Composable
private fun Steps(lines: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEachIndexed { i, line ->
            Text("${i + 1}. $line", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun InfoBox(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            lines.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun Status(ok: Boolean, okText: String, notText: String) {
    Text(
        text = if (ok) "✓ $okText" else notText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (ok) Lake else Terracotta,
    )
}
