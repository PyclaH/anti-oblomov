package ru.shashkov.antioblomov.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import ru.shashkov.antioblomov.data.ProtectedApps
import ru.shashkov.antioblomov.data.SettingsStore
import ru.shashkov.antioblomov.data.UsageCollector
import ru.shashkov.antioblomov.rules.BlockRules
import ru.shashkov.antioblomov.rules.BlockStatus
import ru.shashkov.antioblomov.rules.Settings
import ru.shashkov.antioblomov.rules.TimeText
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

/**
 * Служба блокировки на основе специальных возможностей Android.
 *
 * Android сообщает службе о смене окон. Служба узнаёт, какое приложение сейчас
 * на переднем плане (и есть ли на экране мини-плеер «картинка в картинке»),
 * и использует ТОЛЬКО название приложения — текст и содержимое экрана не читаются.
 *
 * Блокировать ли — решают правила (rules/BlockRules) по настройкам из памяти
 * телефона (data/SettingsStore): список приложений, расписание, таймер.
 *
 * Если на переднем плане приложение, которое сейчас нужно блокировать:
 * 1) показываем поверх всего экран блокировки (BlockOverlay);
 * 2) ставим воспроизведение на паузу (забираем аудиофокус);
 * 3) отправляем пользователя на рабочий стол.
 *
 * Если заблокированное приложение висит мини-плеером — ставим его на паузу;
 * а если мини-плеер развернуть, сработает пункт выше.
 *
 * Блокировка может начаться, когда пользователь уже сидит в YouTube и окна
 * не меняются. Поэтому служба заводит проверку на ближайший момент, когда
 * блокировка начнётся или закончится.
 */
class BlockingService : AccessibilityService() {

    private lateinit var overlay: BlockOverlay
    private lateinit var audioManager: AudioManager
    private lateinit var store: SettingsStore

    /** Звонилка, настройки, рабочий стол и т. п. — не блокируются ни при каких настройках (FR-07). */
    private var protectedApps: Set<String> = emptySet()
    private val handler = Handler(Looper.getMainLooper())
    private val boundaryCheck = Runnable { onBoundary() }

    /** «Было / стало»: раз в час дописываем прошедшие дни в журнал — даже если приложение не открывают. */
    private val usageExecutor = Executors.newSingleThreadExecutor()
    private val usageTick = object : Runnable {
        override fun run() {
            usageExecutor.execute { UsageCollector.update(this@BlockingService) }
            handler.postDelayed(this, USAGE_PERIOD_MS)
        }
    }
    private var scheduledFor: ZonedDateTime? = null
    private var focusRequest: AudioFocusRequest? = null
    private var lastPipPauseAt = 0L

    override fun onServiceConnected() {
        audioManager = getSystemService(AudioManager::class.java)
        overlay = BlockOverlay(this, onHidden = ::releaseAudioFocus)
        store = SettingsStore(this)
        protectedApps = ProtectedApps.of(this)
        Log.i(TAG, "Служба блокировки подключена")
        val settings = store.read()
        scheduleBoundaryCheck(settings, ZonedDateTime.now())
        // После перезагрузки уведомление таймера пропадает — возвращаем его
        TimerNotification.update(this, settings)
        handler.post(usageTick)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) return

        val settings = store.read()
        val now = ZonedDateTime.now()
        scheduleBoundaryCheck(settings, now)

        val foreground = foregroundPackage() ?: event.packageName?.toString()
        if (foreground != null && foreground !in protectedApps &&
            BlockRules.shouldBlock(foreground, settings, now)
        ) {
            block(foreground, settings, now, event)
            return
        }
        checkPictureInPicture(settings, now)
    }

    /** Наступил момент, когда блокировка начинается или заканчивается. */
    private fun onBoundary() {
        scheduledFor = null
        val settings = store.read()
        val now = ZonedDateTime.now()
        Log.i(TAG, "Проверка по времени: ${BlockRules.status(settings, now)}")
        val foreground = foregroundPackage()
        if (foreground != null && foreground !in protectedApps &&
            BlockRules.shouldBlock(foreground, settings, now)
        ) {
            block(foreground, settings, now, event = null)
        } else {
            checkPictureInPicture(settings, now)
        }
        scheduleBoundaryCheck(settings, now)
    }

    /** Завести проверку на ближайшую смену состояния (если она ещё не заведена на то же время). */
    private fun scheduleBoundaryCheck(settings: Settings, now: ZonedDateTime) {
        val next = BlockRules.nextChange(settings, now)
        if (next == scheduledFor) return
        handler.removeCallbacks(boundaryCheck)
        scheduledFor = next
        if (next == null) return
        val delayMs = Duration.between(now, next).toMillis().coerceAtLeast(0) + BOUNDARY_SLACK_MS
        handler.postDelayed(boundaryCheck, delayMs)
        Log.d(TAG, "Следующая проверка в ${next.format(TIME)}")
    }

    /** Приложение активного (основного) окна. */
    private fun foregroundPackage(): String? {
        val root = rootInActiveWindow ?: return null
        return root.packageName?.toString()
    }

    private fun block(pkg: String, settings: Settings, now: ZonedDateTime, event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Сколько прошло от события Android до нашей реакции — для NFR-04
            val reactionMs = SystemClock.uptimeMillis() - event.eventTime
            Log.i(TAG, "Блокирую $pkg, реакция $reactionMs мс")
        } else {
            Log.i(TAG, "Блокирую $pkg")
        }
        val until = (BlockRules.status(settings, now) as? BlockStatus.Blocked)?.until
        overlay.show(appLabel(pkg), until?.let { untilText(it, now) })
        pausePlayback()
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    /** Мини-плеер заблокированного приложения — ставим на паузу (не чаще раза в 2 с). */
    private fun checkPictureInPicture(settings: Settings, now: ZonedDateTime) {
        if (BlockRules.status(settings, now) !is BlockStatus.Blocked) return
        val t = SystemClock.elapsedRealtime()
        if (t - lastPipPauseAt < PIP_DEBOUNCE_MS) return
        val blockedPip = windows.any { window ->
            window.isInPictureInPictureMode &&
                window.root?.packageName?.toString()
                    ?.let { it in settings.blockedPackages && it !in protectedApps } == true
        }
        if (blockedPip) {
            lastPipPauseAt = t
            Log.i(TAG, "Мини-плеер заблокированного приложения — пауза")
            pausePlayback()
        }
    }

    /**
     * Забираем аудиофокус: по правилам Android приложение-проигрыватель
     * должно при этом остановить воспроизведение.
     */
    private fun pausePlayback() {
        val request = focusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )
            .setOnAudioFocusChangeListener { }
            .build()
            .also { focusRequest = it }
        audioManager.requestAudioFocus(request)
    }

    private fun releaseAudioFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }

    private fun appLabel(pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault("Это приложение")

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Служба блокировки отключена")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        handler.removeCallbacks(boundaryCheck)
        handler.removeCallbacks(usageTick)
        usageExecutor.shutdown()
        if (::overlay.isInitialized) overlay.hide()
        Log.i(TAG, "Служба блокировки остановлена")
        super.onDestroy()
    }

    companion object {
        /** Фильтр для Logcat в Android Studio. */
        const val TAG = "AntiOblomov"
        private const val PIP_DEBOUNCE_MS = 2_000L
        private const val USAGE_PERIOD_MS = 60 * 60 * 1000L

        /** Проверяем чуть позже границы, чтобы точно попасть внутрь интервала. */
        private const val BOUNDARY_SLACK_MS = 500L

        private val TIME = DateTimeFormatter.ofPattern("HH:mm")

        /** «до 14:00», «до завтра 01:30», «до Чт 10:00». */
        fun untilText(until: ZonedDateTime, now: ZonedDateTime): String = "до ${TimeText.at(until, now)}"
    }
}
