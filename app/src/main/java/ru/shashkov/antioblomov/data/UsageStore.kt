package ru.shashkov.antioblomov.data

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import org.json.JSONObject
import ru.shashkov.antioblomov.rules.DayUsage
import ru.shashkov.antioblomov.rules.UsageHistory
import java.io.File
import java.io.RandomAccessFile
import java.time.LocalDate

/**
 * История использования для «Было / стало»: файл usage.json в личной папке приложения
 * (README, «Было / стало», п. 8). Всё остаётся на телефоне.
 *
 * Пишут оба процесса — интерфейс и служба блокировки, — поэтому запись идёт под
 * блокировкой файла (FileLock работает между процессами). Пополнение идемпотентно:
 * за прошедший день всегда получаются одни и те же цифры.
 *
 * Формат: {"start":"2026-10-25",
 *          "before":{"2026-10-18":{"com.google.android.youtube":5400,...},...},
 *          "after":{"2026-10-26":{...},...}}  — секунды.
 */
class UsageStore(context: Context) {

    private val dir = context.applicationContext.filesDir
    private val file = AtomicFile(File(dir, FILE_NAME))
    private val lockFile = File(dir, "$FILE_NAME.lock")

    fun read(): UsageHistory? = try {
        fromJson(JSONObject(String(file.readFully(), Charsets.UTF_8)))
    } catch (e: java.io.FileNotFoundException) {
        null // доступ к статистике ещё не выдавали
    } catch (e: Exception) {
        Log.w(TAG, "Не удалось прочитать историю использования", e)
        null
    }

    /** Прочитать, изменить, записать — под блокировкой файла (между процессами). */
    fun update(change: (UsageHistory?) -> UsageHistory?): Unit = synchronized(LOCK) {
        RandomAccessFile(lockFile, "rw").use { raf ->
            raf.channel.lock().use {
                val old = read()
                val new = change(old)
                if (new != null && new != old) write(new)
            }
        }
    }

    private fun write(history: UsageHistory) {
        val out = file.startWrite()
        try {
            out.write(toJson(history).toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(out)
        } catch (e: Exception) {
            file.failWrite(out)
            throw e
        }
    }

    companion object {
        private const val TAG = "AntiOblomov"
        private const val FILE_NAME = "usage.json"
        private val LOCK = Any()

        fun toJson(h: UsageHistory): JSONObject = JSONObject().apply {
            put("start", h.startDate.toString())
            put("before", days(h.before))
            put("after", days(h.after))
        }

        fun fromJson(json: JSONObject) = UsageHistory(
            startDate = LocalDate.parse(json.getString("start")),
            before = days(json.optJSONObject("before")),
            after = days(json.optJSONObject("after")),
        )

        private fun days(map: Map<LocalDate, DayUsage>) = JSONObject().apply {
            for ((day, usage) in map) put(day.toString(), JSONObject().apply { usage.forEach { (pkg, sec) -> put(pkg, sec) } })
        }

        private fun days(json: JSONObject?): Map<LocalDate, DayUsage> {
            json ?: return emptyMap()
            return json.keys().asSequence().associate { key ->
                val obj = json.getJSONObject(key)
                LocalDate.parse(key) to obj.keys().asSequence().associateWith { obj.getInt(it) }
            }
        }
    }
}
