package ru.shashkov.antioblomov.ui.screens

import ru.shashkov.antioblomov.ui.components.Scene
import ru.shashkov.antioblomov.ui.components.SceneBox
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.shashkov.antioblomov.R
import ru.shashkov.antioblomov.data.AppsCache
import ru.shashkov.antioblomov.data.InstalledApp
import ru.shashkov.antioblomov.data.InstalledApps
import ru.shashkov.antioblomov.data.ProtectedApps
import ru.shashkov.antioblomov.data.SettingsStore
import ru.shashkov.antioblomov.rules.AppEntry
import ru.shashkov.antioblomov.rules.AppList
import ru.shashkov.antioblomov.ui.components.AppRow
import ru.shashkov.antioblomov.rules.ChangeGuard
import ru.shashkov.antioblomov.ui.components.WaitDialog
import java.time.ZonedDateTime
import androidx.compose.foundation.border
import androidx.compose.material3.OutlinedTextFieldDefaults
import ru.shashkov.antioblomov.ui.theme.Accent
import ru.shashkov.antioblomov.ui.theme.Line
import ru.shashkov.antioblomov.ui.theme.Overline
import ru.shashkov.antioblomov.ui.theme.Surface1
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.TextPrimary
import ru.shashkov.antioblomov.ui.theme.Warn
import androidx.compose.ui.res.stringResource

/**
 * Экран «Приложения»: какие приложения блокировать (FR-06, FR-07).
 *
 * Сверху — поиск и счётчик выбранных, ниже разделы: «Заблокированные» (отмеченные),
 * «Часто отвлекают» (известные соцсети, видео, игры) и «Все приложения». Звонилка, настройки,
 * рабочий стол и другие важные приложения не показываются — их блокировать нельзя.
 * Выбор сохраняется сразу; служба подхватывает его без перезапуска.
 */
@Composable
fun AppsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    val iconPx = with(LocalDensity.current) { 40.dp.roundToPx() }

    var apps by remember { mutableStateOf(AppsCache.apps) }
    var protected by remember { mutableStateOf(AppsCache.protected) }
    var blocked by remember { mutableStateOf(store.read().blockedPackages) }
    var query by rememberSaveable { mutableStateOf("") }

    // Загрузка в фоне: иконки читаются небыстро. Если список уже был — показываем его сразу
    // и тихо обновляем (вдруг что-то установили или удалили).
    LaunchedEffect(Unit) {
        val (loaded, prot) = withContext(Dispatchers.IO) {
            InstalledApps.load(context, iconPx) to ProtectedApps.of(context)
        }
        AppsCache.apps = loaded
        AppsCache.protected = prot
        apps = loaded
        protected = prot
    }

    // Изменение, ждущее 15-секундной паузы (идёт блокировка — ChangeGuard)
    var pending by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    fun commit(packageName: String, block: Boolean) {
        blocked = store.update { AppList.toggle(it, packageName, block, protected) }.blockedPackages
    }

    fun toggle(packageName: String, block: Boolean) {
        val before = store.read()
        val after = AppList.toggle(before, packageName, block, protected)
        if (ChangeGuard.needsWait(before, after, ZonedDateTime.now())) {
            pending = packageName to block
        } else {
            commit(packageName, block)
        }
    }

    pending?.let { (packageName, block) ->
        WaitDialog(
            title = if (block) "Добавить приложение в блокировку?" else "Убрать приложение из блокировки?",
            onConfirm = {
                commit(packageName, block)
                pending = null
            },
            onDismiss = { pending = null },
        )
    }

    // Фон (R6b): созвездия, гаснут к середине
    SceneBox(scene = Scene.STARS, anchor = null, modifier = modifier) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Сколько выбрано (null — список ещё грузится)
        val count = apps?.let { loaded -> AppList.selectedCount(loaded.map { it.entry }, protected, blocked) }

        // Шапка закреплена: заголовок, счётчик, поиск — список прокручивается под ними
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Заголовок — во всю ширину строки, счётчик — под ним (R4, замечание п. 1: «я» переносилась)
            Text(
                text = stringResource(R.string.tab_apps),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            count?.let { CountChip(it) }
            Text(
                text = if (count == 0)
                    "Ничего не выбрано — расписание и таймер ничего не блокируют. Отметь, что блокировать."
                else "Отметь, что блокировать по расписанию и таймеру.",
                style = MaterialTheme.typography.bodySmall,
                color = if (count == 0) Warn else TextMuted,
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("Поиск", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Очистить поиск", tint = TextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Surface1,
                    unfocusedContainerColor = Surface1,
                    focusedBorderColor = TextMuted,
                    unfocusedBorderColor = Line,
                    cursorColor = Accent,
                ),
            )
        }

        val loaded = apps
        if (loaded == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator()
                    Text("Загружаю список приложений…", style = MaterialTheme.typography.bodyMedium)
                }
            }
            return@Column
        }

        val byPackage = remember(loaded) { loaded.associateBy { it.entry.packageName } }
        val sections = AppList.sections(loaded.map { it.entry }, protected, query, blocked)

        if (sections.isEmpty) {
            Text(
                text = "Ничего не найдено",
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        // Ключ — имя пакета: при отметке строка плавно переезжает в «Заблокированные»
        // и обратно (animateItem), а не исчезает и появляется заново.
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            section("Заблокированные", sections.blocked, byPackage, blocked, ::toggle)
            section("Часто отвлекают", sections.distracting, byPackage, blocked, ::toggle)
            section("Все приложения", sections.others, byPackage, blocked, ::toggle)
        }
    }
    }
}

private fun LazyListScope.section(
    title: String,
    entries: List<AppEntry>,
    byPackage: Map<String, InstalledApp>,
    blocked: Set<String>,
    onToggle: (String, Boolean) -> Unit,
) {
    if (entries.isEmpty()) return
    item(key = "header-$title") {
        SectionHeader(title, Modifier.animateItem())
    }
    items(entries, key = { it.packageName }) { entry ->
        AppRow(
            app = byPackage[entry.packageName],
            label = entry.label,
            checked = entry.packageName in blocked,
            modifier = Modifier.animateItem(),
        ) { onToggle(entry.packageName, it) }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp),
        style = Overline,
        color = TextMuted,
    )
}

/** «Заблокировано: 3» — или янтарное «Ничего не выбрано». */
@Composable
private fun CountChip(count: Int) {
    Text(
        text = if (count == 0) "Ничего не выбрано" else "Заблокировано: $count",
        style = MaterialTheme.typography.labelMedium,
        color = if (count == 0) Warn else TextPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, if (count == 0) Warn.copy(alpha = 0.5f) else Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
