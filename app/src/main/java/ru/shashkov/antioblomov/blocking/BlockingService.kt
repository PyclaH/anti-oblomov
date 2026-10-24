package ru.shashkov.antioblomov.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Служба блокировки на основе специальных возможностей Android.
 *
 * Android сообщает службе, когда на экране открывается новое окно, и называет
 * приложение, которому оно принадлежит. Содержимое экрана служба НЕ читает
 * (canRetrieveWindowContent = false в res/xml/accessibility_service_config.xml).
 *
 * Если открылось заблокированное приложение:
 * 1) отправляем пользователя на домашний экран — это срабатывает всегда;
 * 2) показываем поверх экран блокировки с цитатой.
 * Если HyperOS запретит шаг 2, блокировка всё равно сработает за счёт шага 1.
 */
class BlockingService : AccessibilityService() {

    private var lastScreenShownAt = 0L

    override fun onServiceConnected() {
        Log.i(TAG, "Служба блокировки подключена")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return // своё окно (экран блокировки) не трогаем

        Log.d(TAG, "Открыто: $pkg")
        if (pkg !in SpikeConfig.BLOCKED_PACKAGES) return

        // Сколько прошло от события Android до нашей реакции — для NFR-04.
        val reactionMs = SystemClock.uptimeMillis() - event.eventTime
        Log.i(TAG, "Блокирую $pkg, реакция $reactionMs мс")

        performGlobalAction(GLOBAL_ACTION_HOME)

        // Приложение при открытии присылает несколько событий подряд —
        // экран блокировки показываем не чаще раза в 1,5 секунды.
        val now = SystemClock.elapsedRealtime()
        if (now - lastScreenShownAt < SCREEN_DEBOUNCE_MS) return
        lastScreenShownAt = now

        val intent = Intent(this, BlockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(BlockActivity.EXTRA_PACKAGE, pkg)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Не удалось показать экран блокировки", e)
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        Log.i(TAG, "Служба блокировки остановлена")
        super.onDestroy()
    }

    companion object {
        /** Фильтр для Logcat в Android Studio. */
        const val TAG = "AntiOblomov"
        private const val SCREEN_DEBOUNCE_MS = 1_500L
    }
}
