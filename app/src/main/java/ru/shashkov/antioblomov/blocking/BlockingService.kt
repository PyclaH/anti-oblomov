package ru.shashkov.antioblomov.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Служба блокировки на основе специальных возможностей Android.
 *
 * Android сообщает службе о смене окон. Служба узнаёт, какое приложение сейчас
 * на переднем плане (и есть ли на экране мини-плеер «картинка в картинке»),
 * и использует ТОЛЬКО название приложения — текст и содержимое экрана не читаются.
 *
 * Если на переднем плане заблокированное приложение:
 * 1) показываем поверх всего экран блокировки (BlockOverlay);
 * 2) ставим воспроизведение на паузу (забираем аудиофокус);
 * 3) отправляем пользователя на рабочий стол.
 *
 * Если заблокированное приложение висит мини-плеером — ставим его на паузу;
 * а если мини-плеер развернуть, сработает пункт выше.
 */
class BlockingService : AccessibilityService() {

    private lateinit var overlay: BlockOverlay
    private lateinit var audioManager: AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var lastPipPauseAt = 0L

    override fun onServiceConnected() {
        audioManager = getSystemService(AudioManager::class.java)
        overlay = BlockOverlay(this, onHidden = ::releaseAudioFocus)
        Log.i(TAG, "Служба блокировки подключена")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) return

        val foreground = foregroundPackage() ?: event.packageName?.toString()
        if (foreground != null && foreground != packageName && foreground in SpikeConfig.BLOCKED_PACKAGES) {
            block(foreground, event)
            return
        }
        checkPictureInPicture()
    }

    /** Приложение активного (основного) окна. */
    private fun foregroundPackage(): String? {
        val root = rootInActiveWindow ?: return null
        return root.packageName?.toString()
    }

    private fun block(pkg: String, event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Сколько прошло от события Android до нашей реакции — для NFR-04
            val reactionMs = SystemClock.uptimeMillis() - event.eventTime
            Log.i(TAG, "Блокирую $pkg, реакция $reactionMs мс")
        } else {
            Log.i(TAG, "Блокирую $pkg (смена окон)")
        }
        overlay.show(appLabel(pkg))
        pausePlayback()
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    /** Мини-плеер заблокированного приложения — ставим на паузу (не чаще раза в 2 с). */
    private fun checkPictureInPicture() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPipPauseAt < PIP_DEBOUNCE_MS) return
        val blockedPip = windows.any { window ->
            window.isInPictureInPictureMode &&
                window.root?.packageName?.toString() in SpikeConfig.BLOCKED_PACKAGES
        }
        if (blockedPip) {
            lastPipPauseAt = now
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
        if (::overlay.isInitialized) overlay.hide()
        Log.i(TAG, "Служба блокировки остановлена")
        super.onDestroy()
    }

    companion object {
        /** Фильтр для Logcat в Android Studio. */
        const val TAG = "AntiOblomov"
        private const val PIP_DEBOUNCE_MS = 2_000L
    }
}
