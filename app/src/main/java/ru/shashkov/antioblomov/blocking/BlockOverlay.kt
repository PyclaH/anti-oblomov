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
import ru.shashkov.antioblomov.data.Language
import ru.shashkov.antioblomov.rules.i18n.AppsStrings
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
 *
 * Редизайн R5: поверх картинки — затемнение сверху вниз, логотип меньше,
 * цитата — PT Serif, остальное — Onest, кнопка «стеклянная» (полупрозрачная с рамкой),
 * содержимое плавно проявляется.
 */
class BlockOverlay(
    private val service: AccessibilityService,
    private val onHidden: () -> Unit,
) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var view: View? = null

    val isShowing: Boolean get() = view != null

    /**
     * @param appName название приложения; null — не удалось узнать («Это приложение»)
     * @param until «14:00», «завтра 01:30» — когда закончится блокировка (FR-12); null — не показывать
     */
    fun show(appName: String?, until: String? = null) {
        if (view != null) return
        // Тексты — на языке приложения (v1.2); служба читает его из настроек при каждом показе
        val strings = Language.strings(service)
        val s = strings.apps
        // Цитата — на том же языке (v1.2): для каждого языка свой проверенный список
        val v = buildView(appName ?: s.thisApp, until, Quotes.random(strings.lang), s)
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

    private fun buildView(appName: String, until: String?, quote: Quote, s: AppsStrings): View {
        val ctx = service

        val logo = FrameLayout(ctx).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(ICON_TOP, ICON_BOTTOM), // как фон иконки приложения («Тёмная терракота»)
            ).apply {
                cornerRadius = dp(14).toFloat()
            }
            addView(
                ImageView(ctx).apply { setImageResource(R.drawable.ic_launcher_foreground) },
                FrameLayout.LayoutParams(dp(52), dp(52)),
            )
        }

        val title = text(if (until != null) s.pausedUntil(appName, until) else s.paused(appName), 15f, PAPER_70).apply {
            typeface = onest
        }

        val quoteView = text(s.quoted(quote.text), quoteSize(quote.text), PAPER).apply {
            typeface = serif
            setLineSpacing(0f, 1.2f)
        }
        val author = text(quote.source, 14f, PAPER_65).apply { typeface = onest }

        val button = text(s.backToWork, 16f, PAPER).apply {
            typeface = onestBold
            background = GradientDrawable().apply {
                setColor(GLASS)
                setStroke(dp(1), GLASS_LINE)
                cornerRadius = dp(18).toFloat()
            }
            minHeight = dp(56)
            setPadding(dp(24), dp(14), dp(24), dp(14))
            isClickable = true
            setOnClickListener { hide() }
        }

        val content = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            addView(logo, LinearLayout.LayoutParams(dp(52), dp(52)).apply { topMargin = dp(24) })
            addView(title, fullWidth(topMargin = dp(14)))
            addView(spacer())
            addView(quoteView, fullWidth())
            addView(author, fullWidth(topMargin = dp(18)))
            addView(spacer())
            addView(button, fullWidth(bottomMargin = dp(16)))
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
            // Затемнение: картинку видно сверху, внизу темнее — текст и кнопка читаются на любой сцене
            addView(
                View(ctx).apply {
                    background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(SCRIM_TOP, SCRIM_MID, SCRIM_BOTTOM))
                },
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
            )
            addView(
                content,
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
            )
            // Плавное появление текста и кнопки (фон — сразу, чтобы приложение под ним не мелькало)
            content.alpha = 0f
            content.translationY = dp(12).toFloat()
            content.animate().alpha(1f).translationY(0f).setDuration(320).start()
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
        text.length <= 40 -> 34f
        text.length <= 90 -> 27f
        else -> 22f
    }

    // Шрифты из res/font (те же, что в приложении); если не загрузились — системные
    private val serif: Typeface by lazy { font(R.font.pt_serif_regular) ?: Typeface.SERIF }
    private val onest: Typeface by lazy { font(R.font.onest_regular) ?: Typeface.DEFAULT }
    private val onestBold: Typeface by lazy { font(R.font.onest_semibold) ?: Typeface.DEFAULT_BOLD }

    private fun font(id: Int): Typeface? = runCatching { service.resources.getFont(id) }.getOrNull()

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
        val INK = 0xFF0B0D12.toInt() // Night0 — пока картинка не нарисовалась
        val PAPER = 0xFFF2EFE8.toInt() // TextPrimary
        val PAPER_70 = 0xB3F2EFE8.toInt()
        val PAPER_65 = 0xA6F2EFE8.toInt()
        val GLASS = 0x14F2EFE8.toInt() // кнопка: 8 % светлого
        val GLASS_LINE = 0x59F2EFE8.toInt() // рамка кнопки: 35 %
        val SCRIM_TOP = 0x26080A10
        val SCRIM_MID = 0x8C080A10.toInt()
        val SCRIM_BOTTOM = 0xBF080A10.toInt()
        val ICON_TOP = 0xFF6E3019.toInt()
        val ICON_BOTTOM = 0xFF34150A.toInt()
    }
}
