package ru.shashkov.antioblomov.blocking

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
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
 * из фона (проверка шага 1). Окно спецвозможностей этому запрету не подчиняется
 * и не требует дополнительных разрешений.
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

    fun show(appName: String) {
        if (view != null) return
        val v = buildView(appName)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE,
        )
        windowManager.addView(v, params)
        view = v
    }

    fun hide() {
        val v = view ?: return
        view = null
        runCatching { windowManager.removeView(v) }
        onHidden()
    }

    private fun buildView(appName: String): View {
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

        val title = text("$appName на паузе", 18f, PAPER_DIM)

        val quote = text("«Теперь или никогда!»", 32f, PAPER).apply {
            typeface = Typeface.SERIF
            setLineSpacing(0f, 1.15f)
        }
        val author = text("И. А. Гончаров, «Обломов»", 15f, PAPER_DIM)

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

        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(INK)
            setPadding(dp(28), dp(72), dp(28), dp(56))

            addView(logo, LinearLayout.LayoutParams(dp(104), dp(104)))
            addView(title, wrap(topMargin = dp(20)))
            addView(spacer())
            addView(quote, wrap())
            addView(author, wrap(topMargin = dp(12)))
            addView(spacer())
            addView(
                button,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT),
            )
        }
    }

    private fun text(value: String, sizeSp: Float, color: Int) = TextView(service).apply {
        text = value
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        setTextColor(color)
        gravity = Gravity.CENTER
    }

    private fun wrap(topMargin: Int = 0) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply { this.topMargin = topMargin }

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
