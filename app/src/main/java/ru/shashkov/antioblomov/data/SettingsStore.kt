package ru.shashkov.antioblomov.data

import android.content.Intent
import ru.shashkov.antioblomov.blocking.BlockNotification
import android.content.Context
import android.util.AtomicFile
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import ru.shashkov.antioblomov.rules.Settings
import ru.shashkov.antioblomov.rules.TimeInterval
import java.io.File
import java.time.DayOfWeek

/**
 * Хранение настроек в памяти телефона: файл settings.json в личной папке приложения.
 *
 * Почему файл, а не DataStore: служба блокировки работает в отдельном процессе
 * (так она пережила проверки на HyperOS), а DataStore рассчитан на один процесс.
 * Схема простая и надёжная:
 * - пишет только интерфейс приложения, атомарно (AtomicFile — файл никогда
 *   не бывает «наполовину записан»);
 * - служба только читает и замечает изменения, сравнивая содержимое файла.
 */
class SettingsStore(context: Context) {

    private val appContext = context.applicationContext
    private val file = AtomicFile(File(context.applicationContext.filesDir, FILE_NAME))
    private var cachedBytes: ByteArray? = null
    private var cached: Settings = DEFAULT

    /** Текущие настройки. Дёшево: файл маленький, разбор — только если он изменился. */
    @Synchronized
    fun read(): Settings {
        val bytes = try {
            file.readFully()
        } catch (e: Exception) {
            return DEFAULT.also { cached = it; cachedBytes = null } // файла ещё нет
        }
        if (cachedBytes?.contentEquals(bytes) == true) return cached
        cached = runCatching { fromJson(JSONObject(String(bytes, Charsets.UTF_8))) }
            .onFailure { Log.w(TAG, "Не удалось прочитать настройки, беру по умолчанию", it) }
            .getOrDefault(DEFAULT)
        cachedBytes = bytes
        return cached
    }

    @Synchronized
    fun write(settings: Settings) {
        val bytes = toJson(settings).toString().toByteArray(Charsets.UTF_8)
        val out = file.startWrite()
        try {
            out.write(bytes)
            file.finishWrite(out)
        } catch (e: Exception) {
            file.failWrite(out)
            throw e
        }
        cached = settings
        cachedBytes = bytes
        // Любая смена настроек (расписание, таймер, приложения, язык) — сразу обновить уведомление
        // «Блокировка до …» (v1.2): иначе оно ждало бы следующей проверки службы
        runCatching { BlockNotification.update(appContext, settings) }
        // …и сказать службе блокировки (другой процесс), чтобы сразу тикала отсчёт, если блокировка началась
        runCatching { appContext.sendBroadcast(Intent(ACTION_SETTINGS_CHANGED).setPackage(appContext.packageName)) }
    }

    /** Прочитать, изменить, записать. */
    @Synchronized
    fun update(change: (Settings) -> Settings): Settings = change(read()).also(::write)

    companion object {
        private const val TAG = "AntiOblomov"
        private const val FILE_NAME = "settings.json"

        /** Внутренний сигнал службе блокировки: настройки изменились (только внутри приложения). */
        const val ACTION_SETTINGS_CHANGED = "ru.shashkov.antioblomov.SETTINGS_CHANGED"

        /** По умолчанию ничего не выбрано — пользователь сам отмечает приложения (решение от 25.10.2026). */
        val DEFAULT = Settings()

        // Формат: {"blocked":[...], "timerEndsAt":123, "timerStartedAt":100, "schedule":{"MONDAY":[[600,840],[900,1200]]}}

        fun toJson(s: Settings): JSONObject = JSONObject().apply {
            put("blocked", JSONArray(s.blockedPackages.sorted()))
            s.timerEndsAt?.let { put("timerEndsAt", it) }
            s.timerStartedAt?.let { put("timerStartedAt", it) }
            s.language?.let { put("language", it) }
            put("schedule", JSONObject().apply {
                for ((day, intervals) in s.schedule) {
                    put(day.name, JSONArray().apply {
                        intervals.sortedBy { it.startMinute }.forEach {
                            put(JSONArray().put(it.startMinute).put(it.endMinute))
                        }
                    })
                }
            })
        }

        fun fromJson(json: JSONObject): Settings {
            val blocked = json.optJSONArray("blocked")
                ?.let { arr -> (0 until arr.length()).map { arr.getString(it) }.toSet() }
                ?: DEFAULT.blockedPackages
            val timer = if (json.has("timerEndsAt")) json.getLong("timerEndsAt") else null
            val timerStarted = if (json.has("timerStartedAt")) json.getLong("timerStartedAt") else null
            val schedule = mutableMapOf<DayOfWeek, List<TimeInterval>>()
            json.optJSONObject("schedule")?.let { obj ->
                for (day in DayOfWeek.values()) {
                    val arr = obj.optJSONArray(day.name) ?: continue
                    schedule[day] = (0 until arr.length()).mapNotNull { i ->
                        val pair = arr.getJSONArray(i)
                        // Испорченный интервал пропускаем, а не роняем приложение
                        runCatching { TimeInterval(pair.getInt(0), pair.getInt(1)) }.getOrNull()
                    }
                }
            }
            val language = if (json.has("language")) json.getString("language") else null
            return Settings(
                schedule = schedule,
                blockedPackages = blocked,
                timerEndsAt = timer,
                timerStartedAt = timerStarted,
                language = language,
            )
        }
    }
}
