package ru.shashkov.antioblomov.blocking

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ru.shashkov.antioblomov.R
import kotlin.math.roundToInt

/**
 * Экран блокировки, который служба рисует поверх всего остального
 * (окно типа TYPE_ACCESSIBILITY_OVERLAY).
 *
 * Почему не отдельная Activity: HyperOS не даёт приложениям открывать экраны
 * из фона (проверка шага 1, раунд 1). Окно спецвозможностей этому запрету
 * не подчиняется и не требует дополнительных разрешений.
 *
 * Окно растянуто на весь экран, включая строку состояния, панель навигации
 * и вырез камеры; содержимое отодвинуто от них отступами.
 *
 * Интерфейс собран из обычных Android View, а не Compose: Compose внутри службы
 * требует лишней обвязки, а экран простой.
 */
class BlockOverlay(
    private val service: AccessibilityService,
    private val onHidden: () -> Unit,
) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var view: View? = null

    val isShowing: Boolean get() = view != null

    /** @param until «до 14:00» — когда закончится блокировка (FR-12); null — не показывать */
    fun show(appName: String, until: String? = null) {
        if (view != null) return
        val v = buildView(appName, until, Quotes.random())
        windowManager.addView(v, fullScreenParams())
        view = v
    }

    fun hide() {
        val v = view ?: return
        view = null
        runCatching { windowManager.removeView(v) }
        onHidden()
    }

    private fun fullScreenParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.OPAQUE,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        // Заходить под вырез камеры
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        // Android 11+: по умолчанию окно ужимается, чтобы не залезать на системные
        // панели, — отсюда были полосы по краям. Отключаем.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fitInsetsTypes = 0
            fitInsetsSides = 0
        }
    }

    private fun buildView(appName: String, until: String?, quote: Quote): View {
        val ctx = service

        val logo = FrameLayout(ctx).apply {
            background = GradientDrawable().apply {
                setColor(TERRACOTTA)
                cornerRadius = dp(28).toFloat()
            }
            addView(
                ImageView(ctx).apply { setImageResource(R.drawable.ic_launcher_foreground) },
                FrameLayout.LayoutParams(dp(104), dp(104)),
            )
        }

        val title = text(listOfNotNull("$appName на паузе", until).joinToString(" "), 18f, PAPER_DIM)

        val quoteView = text("«${quote.text}»", quoteSize(quote.text), PAPER).apply {
            typeface = Typeface.SERIF
            setLineSpacing(0f, 1.15f)
        }
        val author = text(quote.source, 15f, PAPER_DIM)

        val button = text("Вернуться к делу", 17f, INK).apply {
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(PAPER)
                cornerRadius = dp(28).toFloat()
            }
            setPadding(dp(24), dp(16), dp(24), dp(16))
            isClickable = true
            setOnClickListener { hide() }
        }

        val content = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            addView(logo, LinearLayout.LayoutParams(dp(104), dp(104)).apply { topMargin = dp(32) })
            addView(title, fullWidth(topMargin = dp(20)))
            addView(spacer())
            addView(quoteView, fullWidth())
            addView(author, fullWidth(topMargin = dp(16)))
            addView(spacer())
            addView(button, fullWidth(bottomMargin = dp(24)))
        }

        // Отступы = базовые поля + высота системных панелей и выреза,
        // чтобы фон был на весь экран, а текст и кнопка не прятались под панелями.
        content.setOnApplyWindowInsetsListener { v, insets ->
            val (left, top, right, bottom) = systemInsets(insets)
            v.setPadding(dp(28) + left, dp(24) + top, dp(28) + right, dp(24) + bottom)
            insets
        }
        content.setPadding(dp(28), dp(48), dp(28), dp(48)) // до прихода отступов

        // Фон-картинка под цитату (SceneBackground.kt), поверх — прежнее содержимое
        return FrameLayout(ctx).apply {
            setBackgroundColor(INK)
            addView(
                SceneBackgroundView(ctx, quote.scene, seed = quote.text.hashCode()),
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
            )
            addView(
                content,
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
            )
        }
    }

    private fun systemInsets(insets: WindowInsets): List<Int> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val i = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            listOf(i.left, i.top, i.right, i.bottom)
        } else {
            @Suppress("DEPRECATION")
            listOf(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom,
            )
        }

    /** Длинные цитаты — мельче, чтобы помещались. */
    private fun quoteSize(text: String): Float = when {
        text.length <= 40 -> 32f
        text.length <= 90 -> 26f
        else -> 22f
    }

    private fun text(value: String, sizeSp: Float, color: Int) = TextView(service).apply {
        text = value
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        setTextColor(color)
        gravity = Gravity.CENTER
    }

    private fun fullWidth(topMargin: Int = 0, bottomMargin: Int = 0) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply {
        this.topMargin = topMargin
        this.bottomMargin = bottomMargin
    }

    private fun spacer() = View(service).apply {
        layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
    }

    private fun dp(value: Int) = (value * service.resources.displayMetrics.density).roundToInt()

    private companion object {
        // Цвета из ui/theme/Color.kt (здесь нужны в формате Android View)
        val INK = 0xFF1F2A37.toInt()
        val PAPER = 0xFFFAF7F0.toInt()
        val PAPER_DIM = 0x99FAF7F0.toInt()
        val TERRACOTTA = 0xFFB4502A.toInt()
    }
}
