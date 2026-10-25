package ru.shashkov.antioblomov.rules

import java.text.Collator
import java.util.Locale

/**
 * Приложение в списке выбора.
 * @property distracting попадает в раздел «Часто отвлекают»
 */
data class AppEntry(
    val packageName: String,
    val label: String,
    val distracting: Boolean,
)

/** Список, разбитый на разделы экрана «Приложения». */
data class AppSections(
    val distracting: List<AppEntry>,
    val others: List<AppEntry>,
) {
    val isEmpty: Boolean get() = distracting.isEmpty() && others.isEmpty()
}

/**
 * Логика экрана «Приложения» без Android — проверяется юнит-тестами (AppListTest).
 */
object AppList {

    /**
     * Популярные приложения, которые чаще всего отвлекают. Дополнительно в раздел
     * попадают игры, видео и соцсети по категории, которую приложение указывает о себе.
     */
    val KNOWN_DISTRACTING: Set<String> = setOf(
        "com.zhiliaoapp.musically",       // TikTok
        "com.ss.android.ugc.trill",       // TikTok (другие регионы)
        "com.google.android.youtube",     // YouTube
        "com.vkontakte.android",          // ВКонтакте
        "com.vk.vkvideo",                 // VK Видео
        "ru.ok.android",                  // Одноклассники
        "ru.rutube.app",                  // RUTUBE
        "ru.zen.android",                 // Дзен
        "org.telegram.messenger",         // Telegram
        "org.telegram.messenger.web",     // Telegram (сайт)
        "com.instagram.android",          // Instagram
        "com.facebook.katana",            // Facebook
        "com.twitter.android",            // X (Twitter)
        "com.snapchat.android",           // Snapchat
        "com.discord",                    // Discord
        "com.reddit.frontpage",           // Reddit
        "tv.twitch.android.app",          // Twitch
        "com.pinterest",                  // Pinterest
        "video.like",                     // Likee
        "com.whatsapp",                   // WhatsApp
    )

    private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("ru-RU")).apply {
        strength = Collator.SECONDARY // без учёта регистра
    }

    /**
     * Разделы для показа: без защищённых приложений (FR-07), с фильтром по поиску,
     * по алфавиту (по-русски: «ё» рядом с «е», латиница отдельно).
     */
    fun sections(apps: List<AppEntry>, protected: Set<String>, query: String): AppSections {
        val q = query.trim()
        val visible = apps
            .asSequence()
            .filter { it.packageName !in protected }
            .filter { q.isEmpty() || it.label.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true) }
            .distinctBy { it.packageName }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
            .toList()
        return AppSections(
            distracting = visible.filter { it.distracting },
            others = visible.filterNot { it.distracting },
        )
    }

    /** Сколько из выбранных приложений реально установлено и не защищено. */
    fun selectedCount(apps: List<AppEntry>, protected: Set<String>, blocked: Set<String>): Int =
        apps.count { it.packageName in blocked && it.packageName !in protected }

    /** Включить или выключить блокировку приложения. Защищённое включить нельзя. */
    fun toggle(settings: Settings, packageName: String, block: Boolean, protected: Set<String>): Settings {
        if (block && packageName in protected) return settings
        val updated = if (block) settings.blockedPackages + packageName else settings.blockedPackages - packageName
        return settings.copy(blockedPackages = updated)
    }
}
