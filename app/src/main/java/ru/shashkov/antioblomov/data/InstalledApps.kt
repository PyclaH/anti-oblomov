package ru.shashkov.antioblomov.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import ru.shashkov.antioblomov.rules.AppEntry
import ru.shashkov.antioblomov.rules.AppList

/** Приложение телефона для экрана «Приложения»: данные + иконка. */
data class InstalledApp(val entry: AppEntry, val icon: ImageBitmap?)

/**
 * Приложения, у которых есть иконка на рабочем столе. Системные службы без
 * иконки в список не попадают — их и не нужно блокировать.
 *
 * Медленно (иконки!) — вызывать в фоне (Dispatchers.IO).
 */
object InstalledApps {

    fun load(context: Context, iconSizePx: Int): List<InstalledApp> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryActivities(launcherIntent)
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .map { info ->
                InstalledApp(
                    entry = AppEntry(
                        packageName = info.packageName,
                        label = pm.getApplicationLabel(info).toString(),
                        distracting = isDistracting(info),
                    ),
                    icon = runCatching {
                        pm.getApplicationIcon(info).toBitmap(iconSizePx, iconSizePx).asImageBitmap()
                    }.getOrNull(),
                )
            }
    }

    /** Название приложения (для подписей), или само имя пакета, если не нашли. */
    fun label(context: Context, packageName: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)

    /** Известные «пожиратели времени» плюс игры, видео и соцсети по категории приложения. */
    private fun isDistracting(info: ApplicationInfo): Boolean =
        info.packageName in AppList.KNOWN_DISTRACTING ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && info.category in DISTRACTING_CATEGORIES)

    private val DISTRACTING_CATEGORIES = setOf(
        ApplicationInfo.CATEGORY_GAME,
        ApplicationInfo.CATEGORY_VIDEO,
        ApplicationInfo.CATEGORY_SOCIAL,
    )
}

/**
 * Список приложений в памяти: второй раз вкладка «Приложения» и список статистики
 * открываются сразу.
 */
object AppsCache {
    var apps: List<InstalledApp>? = null
    var protected: Set<String> = emptySet()
}

/** queryIntentActivities с учётом нового API на Android 13+. */
internal fun PackageManager.queryActivities(intent: Intent): List<ResolveInfo> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        queryIntentActivities(intent, 0)
    }
