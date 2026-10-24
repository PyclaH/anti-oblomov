package ru.shashkov.antioblomov.blocking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Сюжет фона экрана блокировки. Каждой цитате подобран свой (Quotes.kt).
 */
enum class Scene {
    DAWN,      // рассвет: «сейчас», начало, свет
    NIGHT,     // звёздная ночь: время, жизнь, не спать
    MOUNTAINS, // горы: усилие, подъём, постоянство
    STORM,     // буря над морем: борьба, смелость
    WINTER,    // снег: «лето красное пропела» и прочая ирония
    STUDY,     // лампа и тетрадные линейки: учёба и труд
    WATER,     // капля и круги: упорство, малые шаги
    ROAD,      // дорога к горизонту: путь, первый шаг
}

/**
 * Ненавязчивая картинка-фон, нарисованная кодом (без файлов-картинок).
 * Детали — по верхнему и нижнему краю; середина, где цитата, остаётся тёмной,
 * а поверх неё лежит лёгкое затемнение — текст всегда читается.
 *
 * Все размеры заданы в «единицах макета»: 1 единица = 1/540 ширины экрана
 * (макет рисовался на ширине 540).
 */
class SceneBackgroundView(
    context: Context,
    private val scene: Scene,
    private val seed: Int,
) : View(context) {

    private var bitmap: Bitmap? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w == 0 || h == 0) return
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp ->
            SceneRenderer(Canvas(bmp), w.toFloat(), h.toFloat(), Random(seed)).draw(scene)
        }
    }

    override fun onDraw(canvas: Canvas) {
        bitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmap?.recycle()
        bitmap = null
    }
}

private class SceneRenderer(
    private val c: Canvas,
    private val w: Float,
    private val h: Float,
    private val rnd: Random,
) {
    private val u = w / 540f // единица макета
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun draw(scene: Scene) {
        when (scene) {
            Scene.DAWN -> dawn()
            Scene.NIGHT -> night()
            Scene.MOUNTAINS -> mountains()
            Scene.STORM -> storm()
            Scene.WINTER -> winter()
            Scene.STUDY -> study()
            Scene.WATER -> water()
            Scene.ROAD -> road()
        }
        scrim()
    }

    // ---------- сюжеты ----------

    private fun dawn() {
        sky(0f to 0xFF141B33, .55f to 0xFF3A2A4F, .80f to 0xFF9A4E4A, .86f to 0xFFE39A5B, 1f to 0xFFE39A5B)
        glow(w * .5f, h * .86f, w * .6f, 0xFFF6C27A, .5f)
        fill(0xFFF6C27A, .9f) { c.drawCircle(w * .5f, h * .86f, w * .16f, it) }
        val ground = Path().apply {
            moveTo(0f, h * .87f)
            var x = 0f
            while (x <= w) {
                lineTo(x, h * .86f + sin(x / w * PI * 2.2).toFloat() * h * .012f)
                x += 6 * u
            }
            lineTo(w, h); lineTo(0f, h); close()
        }
        fill(0xFF12172A) { c.drawPath(ground, it) }
    }

    private fun night() {
        sky(0f to 0xFF0E1430, .6f to 0xFF1F2A4A, 1f to 0xFF2B3557)
        dots(count = 110, maxY = .8f, minR = .6f, spreadR = 1.6f, color = 0xFFFFF6E0, minA = .25f, spreadA = .65f, topHeavy = true)
        // полумесяц
        val r = w * .07f
        val moon = Path().apply { addCircle(w * .8f, h * .12f, r, Path.Direction.CW) }
        val bite = Path().apply { addCircle(w * .8f + w * .03f, h * .12f - w * .02f, r, Path.Direction.CW) }
        moon.op(bite, Path.Op.DIFFERENCE)
        fill(0xFFF3E6C4, .9f) { c.drawPath(moon, it) }
    }

    private fun mountains() {
        sky(0f to 0xFF13213A, .6f to 0xFF284159, .75f to 0xFF4C6A7A, 1f to 0xFF4C6A7A)
        glow(w * .5f, h * .72f, w * .7f, 0xFF9FB7C2, .25f)
        ridge(base = h * .72f, amp = h * .10f, seed = 1, color = 0xFF2C3E50)
        ridge(base = h * .79f, amp = h * .08f, seed = 2, color = 0xFF1E2C3D)
        ridge(base = h * .86f, amp = h * .06f, seed = 3, color = 0xFF121C29)
    }

    private fun storm() {
        sky(0f to 0xFF10161F, .6f to 0xFF24303D, 1f to 0xFF1A2530)
        // море с мягким краем
        paint.reset(); paint.isAntiAlias = true
        paint.shader = LinearGradient(
            0f, h * .74f, 0f, h * .80f,
            argb(0xFF121A24, 0f), argb(0xFF121A24, 1f), Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, h * .74f, w, h, paint)
        paint.shader = null
        // волны
        listOf(.80f, .85f, .90f, .95f).forEachIndexed { i, yy ->
            val wave = Path()
            var x = 0f
            while (x <= w) {
                val y = h * yy + sin(x / (w * .25f) * 2 * PI + i).toFloat() * h * .010f
                if (x == 0f) wave.moveTo(x, y) else wave.lineTo(x, y)
                x += 4 * u
            }
            stroke(0xFF6F8597, .35f - .05f * i, 3 * u) { c.drawPath(wave, it) }
        }
        // буревестники
        gull(w * .70f, h * .24f, w * .09f)
        gull(w * .58f, h * .29f, w * .05f)
    }

    private fun winter() {
        sky(0f to 0xFF1A2433, .7f to 0xFF2E3B4E, 1f to 0xFF3D4B5E)
        fill(0xFFC9D3DD, .16f) { c.drawOval(RectF(-w * .3f, h * .82f, w * .7f, h * 1.1f), it) }
        fill(0xFFC9D3DD, .24f) { c.drawOval(RectF(w * .3f, h * .85f, w * 1.3f, h * 1.12f), it) }
        dots(count = 130, maxY = .95f, minR = .8f, spreadR = 2.2f, color = 0xFFFFFFFF, minA = .2f, spreadA = .5f, topHeavy = false)
    }

    private fun study() {
        sky(0f to 0xFF2A2019, .5f to 0xFF221A14, 1f to 0xFF1A1410)
        glow(w * .5f, 0f, w * 1.0f, 0xFFE8B76A, .55f)
        var y = h * .30f
        while (y < h * .97f) {
            stroke(0xFFE8D9BF, .07f, 2 * u) { c.drawLine(0f, y, w, y, it) }
            y += h * .045f
        }
        stroke(0xFFB4502A, .18f, 2 * u) { c.drawLine(w * .12f, h * .25f, w * .12f, h, it) }
    }

    private fun water() {
        sky(0f to 0xFF0F2027, .6f to 0xFF16323A, 1f to 0xFF1E434B)
        val cx = w * .5f
        val cy = h * .86f
        for (i in 0 until 7) {
            val rx = w * (.08f + i * .085f)
            val ry = rx * .28f
            stroke(0xFF9CC3C9, .42f * (1 - i / 7f), 2 * u) { c.drawOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), it) }
        }
        // капля
        val dx = w * .5f
        val dy = h * .76f
        val r = w * .022f
        val drop = Path().apply {
            addCircle(dx, dy, r, Path.Direction.CW)
            moveTo(dx - r * .95f, dy - r * .3f)
            lineTo(dx, dy - r * 2.6f)
            lineTo(dx + r * .95f, dy - r * .3f)
            close()
        }
        fill(0xFF9CC3C9, .7f) { c.drawPath(drop, it) }
    }

    private fun road() {
        val hz = h * .70f
        sky(0f to 0xFF121931, .62f to 0xFF2C2C4A, .70f to 0xFF7A4E5A, 1f to 0xFF7A4E5A)
        glow(w * .5f, hz, w * .55f, 0xFFE8A27F, .3f)
        fill(0xFF15182A) { c.drawRect(0f, hz, w, h, it) }
        val road = Path().apply {
            moveTo(w * .49f, hz); lineTo(w * .51f, hz); lineTo(w * .95f, h); lineTo(w * .05f, h); close()
        }
        fill(0xFF262A40) { c.drawPath(road, it) }
        // разметка: штрихи растут к зрителю
        var t = .04f
        while (t < 1f) {
            val t2 = min(t + .05f + .1f * t, 1f)
            val y1 = hz + (h - hz) * t
            val y2 = hz + (h - hz) * t2
            val w1 = (1 + 6 * t) * u
            val w2 = (1 + 6 * t2) * u
            val dash = Path().apply {
                moveTo(w * .5f - w1 / 2, y1); lineTo(w * .5f + w1 / 2, y1)
                lineTo(w * .5f + w2 / 2, y2); lineTo(w * .5f - w2 / 2, y2); close()
            }
            fill(0xFFE8C98A, .6f) { c.drawPath(dash, it) }
            t = t2 + .04f + .08f * t2
        }
    }

    // ---------- общие детали ----------

    /** Затемнение середины, где цитата. */
    private fun scrim() {
        paint.reset()
        val ink = 0xFF0B1020
        paint.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(argb(ink, 0f), argb(ink, 0f), argb(ink, .35f), argb(ink, .35f), argb(ink, 0f), argb(ink, 0f)),
            floatArrayOf(0f, .2f, .38f, .66f, .78f, 1f),
            Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
    }

    private fun sky(vararg stops: Pair<Float, Long>) {
        paint.reset()
        paint.shader = LinearGradient(
            0f, 0f, 0f, h,
            stops.map { argb(it.second, 1f) }.toIntArray(),
            stops.map { it.first }.toFloatArray(),
            Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
    }

    private fun glow(cx: Float, cy: Float, r: Float, color: Long, alpha: Float) {
        paint.reset(); paint.isAntiAlias = true
        paint.shader = RadialGradient(
            cx, cy, r,
            intArrayOf(argb(color, alpha), argb(color, alpha * .35f), argb(color, 0f)),
            floatArrayOf(0f, .45f, 1f),
            Shader.TileMode.CLAMP,
        )
        c.drawCircle(cx, cy, r, paint)
        paint.shader = null
    }

    private fun ridge(base: Float, amp: Float, seed: Int, color: Long, n: Int = 7) {
        val r = Random(seed)
        val path = Path().apply {
            moveTo(0f, h)
            for (i in 0..n) {
                val x = i * w / n
                val y = base - if (i % 2 == 1) amp * (.35f + r.nextFloat() * .65f) else amp * r.nextFloat() * .3f
                lineTo(x, y)
            }
            lineTo(w, h); close()
        }
        fill(color) { c.drawPath(path, it) }
    }

    private fun dots(
        count: Int, maxY: Float, minR: Float, spreadR: Float,
        color: Long, minA: Float, spreadA: Float, topHeavy: Boolean,
    ) {
        repeat(count) {
            val x = rnd.nextFloat() * w
            val fy = if (topHeavy) rnd.nextFloat().pow(1.4f) else rnd.nextFloat()
            val y = fy * h * maxY
            val r = (minR + rnd.nextFloat() * spreadR) * u
            fill(color, minA + rnd.nextFloat() * spreadA) { c.drawCircle(x, y, r, it) }
        }
    }

    private fun gull(x: Float, y: Float, s: Float) {
        val p = Path().apply {
            moveTo(x - s, y)
            quadTo(x - s * .5f, y - s * .5f, x, y)
            quadTo(x + s * .5f, y - s * .5f, x + s, y)
        }
        stroke(0xFFD8D2C4, .85f, 4 * u * (s / (w * .09f)).coerceAtLeast(.6f)) { c.drawPath(p, it) }
    }

    private inline fun fill(color: Long, alpha: Float = 1f, block: (Paint) -> Unit) {
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = argb(color, alpha)
        block(paint)
    }

    private inline fun stroke(color: Long, alpha: Float, width: Float, block: (Paint) -> Unit) {
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = argb(color, alpha)
        block(paint)
    }

    /** Цвет 0xFFRRGGBB с заданной прозрачностью 0..1. */
    private fun argb(color: Long, alpha: Float): Int =
        ((alpha.coerceIn(0f, 1f) * 255).toInt() shl 24) or (color and 0xFFFFFF).toInt()
}
