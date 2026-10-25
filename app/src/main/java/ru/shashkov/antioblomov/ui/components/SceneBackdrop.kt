package ru.shashkov.antioblomov.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import kotlin.math.cos
import kotlin.math.sin

/**
 * Фоны вкладок (редизайн R6, макеты «Фоны вкладок» от 25.10.2026).
 * Главная — сцена по состоянию: горы (блокировка впереди), поле с работниками (идёт блокировка),
 * море (ничего не запланировано). Таймер — шкала вокруг циферблата. Расписание — путь солнца
 * над полосой суток (R6b), Приложения — созвездия (R6b).
 *
 * Минимум цвета, рисунок бледный и плавно гаснет к середине экрана. Неподвижный —
 * рисуется один раз и прокручивается вместе с экраном.
 *
 * Рисунки заданы в координатах макета (экран 390 × 844) и масштабируются по ширине экрана.
 * Чтобы рисунок стоял ровно под своим элементом на любом телефоне, он привязан к нему
 * (якорь): к кольцу на главной, полосе суток в расписании, циферблату таймера.
 */
enum class Scene { MOUNTAINS, FIELD, SEA, SUN_PATH, BEZEL, STARS }

/** Где на экране элемент-якорь (в координатах всего окна). */
class SceneAnchor {
    var rect by mutableStateOf<Rect?>(null)

    /** Повесить на элемент-якорь: кольцо, полосу суток, циферблат. */
    val modifier: Modifier = Modifier.onGloballyPositioned { rect = it.rectInRoot() }
}

@Composable
fun rememberSceneAnchor(): SceneAnchor = remember { SceneAnchor() }

/**
 * Экран с фоном: [scene] позади [content]. Смена сцены — плавная (главная меняет сцену
 * вместе с состоянием блокировки).
 *
 * @param hour который час (для солнца в расписании), 0..24; null — не рисовать солнце
 */
@Composable
fun SceneBox(
    scene: Scene,
    anchor: SceneAnchor?,
    modifier: Modifier = Modifier,
    hour: Float? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val host: MutableState<Offset> = remember { mutableStateOf(Offset.Zero) }
    Box(modifier.fillMaxSize().onGloballyPositioned { host.value = it.positionInRoot() }) {
        Crossfade(targetState = scene, animationSpec = tween(600), modifier = Modifier.matchParentSize(), label = "scene") { s ->
            Canvas(
                Modifier
                    .fillMaxSize()
                    // Отдельный слой — чтобы маска затухания срезала только рисунок
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
            ) {
                val a = anchor?.rect?.translate(-host.value)
                drawScene(s, a, hour)
            }
        }
        content()
    }
}

private fun LayoutCoordinates.rectInRoot(): Rect = Rect(positionInRoot(), size.toSize())

// ─── Общее ────────────────────────────────────────────────────────────────

private const val W = 390f
private val INK = Color(0xFFF2EFE8)

private fun c(hex: Long, alpha: Float) = Color(hex).copy(alpha = alpha)

private fun poly(vararg p: Pair<Float, Float>) = Path().apply {
    moveTo(p[0].first, p[0].second)
    for (i in 1 until p.size) lineTo(p[i].first, p[i].second)
    close()
}

/** Рисовать в координатах макета: [origin] — где на экране точка (0, 0) макета, [s] — масштаб. */
private inline fun DrawScope.mock(origin: Offset, s: Float, block: DrawScope.() -> Unit) =
    withTransform({
        translate(origin.x, origin.y)
        scale(s, s, pivot = Offset.Zero)
    }, block)

/** Затухание сверху вниз: до [opaqueY] рисунок целиком, к [goneY] — исчезает. */
private fun DrawScope.fade(opaqueY: Float, goneY: Float, soft: Boolean = false) {
    val stops = if (soft) arrayOf(0f to Color.White, 0.45f to Color.White.copy(alpha = 0.85f), 1f to Color.Transparent)
    else arrayOf(0f to Color.White, 1f to Color.Transparent)
    drawRect(Brush.verticalGradient(*stops, startY = opaqueY, endY = goneY), blendMode = BlendMode.DstIn)
}

private fun DrawScope.drawScene(scene: Scene, anchor: Rect?, hour: Float?) {
    val s = size.width / W
    when (scene) {
        Scene.MOUNTAINS, Scene.FIELD, Scene.SEA -> {
            // Центр кольца на главной в макете — (195, 240)
            val center = anchor?.center ?: Offset(size.width / 2, 240f * s)
            val origin = center - Offset(195f * s, 240f * s)
            mock(origin, s) {
                when (scene) {
                    Scene.MOUNTAINS -> mountains()
                    Scene.FIELD -> field()
                    else -> sea()
                }
            }
            fade(origin.y + 360f * s, origin.y + 540f * s)
        }

        Scene.SUN_PATH -> {
            // Путь солнца над полосой суток: восход над 6:00, закат над 18:00, горизонт — по полосе
            val left = anchor?.left ?: (20f * s)
            val width = anchor?.width ?: (350f * s)
            val base = anchor?.center?.y ?: (240f * s)
            sunPath(left, width, base, s, hour)
            fade(0f, base + 304f * s, soft = true)
        }

        Scene.BEZEL -> {
            // Шкала вокруг циферблата таймера: в макете центр (195, 296), радиус 186
            val center = anchor?.center ?: Offset(size.width / 2, 296f * s)
            val origin = center - Offset(195f * s, 296f * s)
            mock(origin, s) { bezel() }
            fade(0f, origin.y + 600f * s, soft = true)
        }

        Scene.STARS -> {
            mock(Offset.Zero, s) { stars() }
            fade(0f, 520f * s, soft = true)
        }
    }
}

// ─── Главная: горы ────────────────────────────────────────────────────────

private fun DrawScope.mountains() {
    drawRect(Brush.verticalGradient(0f to Color.Transparent, 1f to c(0xFF5FB4A6, 0.14f), endY = 300f), size = Size(W, 300f))
    drawCircle(c(0xFFF2EFE8, 0.10f), radius = 15f, center = Offset(336f, 96f))
    drawPath(
        poly(0f to 300f, 40f to 268f, 80f to 284f, 130f to 226f, 172f to 262f, 215f to 212f, 262f to 250f, 302f to 226f, 350f to 262f, 390f to 238f, 390f to 600f, 0f to 600f),
        c(0xFF7CCFC1, 0.08f),
    )
    // Снег на вершинах — короткие светлые штрихи
    val snow = Stroke(1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    for ((x, y) in listOf(130f to 226f, 215f to 212f, 302f to 226f)) {
        drawPath(Path().apply { moveTo(x - 11f, y + 14f); lineTo(x, y); lineTo(x + 12f, y + 16f) }, INK.copy(alpha = 0.16f), style = snow)
    }
    drawPath(
        poly(0f to 338f, 52f to 298f, 98f to 326f, 152f to 284f, 204f to 324f, 258f to 290f, 312f to 330f, 360f to 300f, 390f to 316f, 390f to 600f, 0f to 600f),
        c(0xFF4F8F87, 0.14f),
    )
    drawPath(
        poly(0f to 392f, 62f to 352f, 122f to 380f, 190f to 344f, 252f to 384f, 322f to 352f, 390f to 378f, 390f to 600f, 0f to 600f),
        c(0xFF2E5A55, 0.30f),
    )
}

// ─── Главная: поле, идёт работа ───────────────────────────────────────────

private const val HORIZON = 288f

private fun DrawScope.field() {
    drawRect(Brush.verticalGradient(0f to Color.Transparent, 1f to c(0xFFE2683C, 0.16f), endY = HORIZON), size = Size(W, HORIZON))
    drawCircle(c(0xFFF0A04B, 0.16f), radius = 26f, center = Offset(300f, HORIZON))
    // Холмы на горизонте
    drawPath(
        Path().apply {
            moveTo(0f, HORIZON)
            quadraticTo(60f, 268f, 120f, 282f)
            quadraticTo(180f, 296f, 250f, 278f)
            quadraticTo(320f, 260f, 390f, 280f)
            lineTo(390f, 296f); lineTo(0f, 296f); close()
        },
        c(0xFF6E3019, 0.35f),
    )
    drawRect(c(0xFFC8643F, 0.09f), topLeft = Offset(0f, 294f), size = Size(W, 306f))
    // Борозды сходятся к горизонту
    for (k in -7..7) {
        val bx = 195f + k * 70f
        val t = (296f - HORIZON) / (600f - HORIZON)
        drawLine(c(0xFFE2683C, 0.14f), Offset(195f + (bx - 195f) * t, 296f), Offset(bx, 600f), strokeWidth = 1f)
    }
    workers()
}

/** Три силуэта за работой и стог. Общая прозрачность — на весь слой, чтобы стыки не светились. */
private fun DrawScope.workers() {
    val fig = Color(0xFFEDB894)
    drawContext.canvas.saveLayer(Rect(0f, 280f, W, 380f), Paint().apply { alpha = 0.34f })
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, w: Float) =
        drawLine(fig, Offset(x1, y1), Offset(x2, y2), strokeWidth = w, cap = StrokeCap.Round)
    fun bent(w: Float, vararg p: Pair<Float, Float>) = drawPath(
        Path().apply { moveTo(p[0].first, p[0].second); for (i in 1 until p.size) lineTo(p[i].first, p[i].second) },
        fig, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    // 1 — работает тяпкой
    drawCircle(fig, 5.5f, Offset(40f, 318f))
    line(41f, 325f, 50f, 346f, 7f)
    line(50f, 346f, 43f, 368f, 5f); line(50f, 346f, 59f, 368f, 5f)
    bent(3.5f, 43f to 330f, 58f to 338f, 66f to 334f)
    line(72f, 322f, 60f, 366f, 2.2f)
    line(53f, 367f, 64f, 369f, 3f)

    // 2 — нагнулся, собирает в корзину
    line(101f, 368f, 104f, 349f, 5f); line(110f, 368f, 107f, 349f, 5f)
    line(105f, 348f, 89f, 337f, 7f)
    drawCircle(fig, 5.5f, Offset(84f, 336f))
    line(92f, 340f, 88f, 357f, 3.5f)
    drawRoundRect(fig, topLeft = Offset(76f, 357f), size = Size(16f, 11f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f))

    // 3 — несёт мешок на плече
    drawCircle(fig, 5.5f, Offset(340f, 317f))
    line(340f, 324f, 339f, 347f, 7f)
    line(339f, 347f, 332f, 368f, 5f); line(339f, 347f, 347f, 368f, 5f)
    rotate(-20f, pivot = Offset(328f, 318f)) {
        drawOval(fig, topLeft = Offset(317f, 310.5f), size = Size(22f, 15f))
    }
    line(341f, 329f, 331f, 324f, 3.5f)

    // Стог
    drawPath(Path().apply { moveTo(354f, 368f); quadraticTo(367f, 336f, 380f, 368f); close() }, fig.copy(alpha = 0.7f))
    drawContext.canvas.restore()
}

// ─── Главная: море ────────────────────────────────────────────────────────

private const val SEA_LINE = 282f

private fun DrawScope.sea() {
    val light = Color(0xFFA6E3D8)
    drawRect(Brush.verticalGradient(0f to Color.Transparent, 1f to light.copy(alpha = 0.16f), endY = SEA_LINE), size = Size(W, SEA_LINE))
    drawCircle(INK.copy(alpha = 0.12f), radius = 17f, center = Offset(320f, 112f))
    drawRect(c(0xFF7CCFC1, 0.07f), topLeft = Offset(0f, SEA_LINE), size = Size(W, 320f))
    drawLine(light.copy(alpha = 0.22f), Offset(0f, SEA_LINE), Offset(W, SEA_LINE), strokeWidth = 1f)
    // Лунная дорожка
    for ((y, w, a) in listOf(Triple(292f, 14f, .20f), Triple(300f, 10f, .16f), Triple(309f, 12f, .13f), Triple(319f, 7f, .11f), Triple(330f, 9f, .09f), Triple(342f, 5f, .07f))) {
        drawLine(INK.copy(alpha = a), Offset(320f - w, y), Offset(320f + w, y), strokeWidth = 1.6f, cap = StrokeCap.Round)
    }
    // Волны — короткие отрезки, к низу бледнее
    val rows = listOf(
        Triple(306f, .16f, listOf(20f to 90f, 150f to 230f, 280f to 360f)),
        Triple(336f, .13f, listOf(60f to 140f, 210f to 300f)),
        Triple(372f, .11f, listOf(0f to 80f, 140f to 210f, 270f to 370f)),
        Triple(414f, .09f, listOf(40f to 130f, 200f to 290f)),
        Triple(462f, .07f, listOf(0f to 90f, 150f to 240f, 300f to 390f)),
    )
    for ((y, a, segs) in rows) for ((x0, x1) in segs) {
        val p = Path().apply {
            moveTo(x0, y)
            var x = x0
            while (x < x1) { relativeQuadraticTo(7f, -4f, 14f, 0f); x += 14f }
        }
        drawPath(p, light.copy(alpha = a), style = Stroke(1.2f, cap = StrokeCap.Round))
    }
    // Парусник
    drawPath(poly(52f to 276f, 52f to 252f, 66f to 274f), INK.copy(alpha = 0.16f))
    drawPath(poly(48f to 266f, 52f to 252f, 52f to 274f), INK.copy(alpha = 0.10f))
    drawPath(poly(42f to 277f, 70f to 277f, 65f to 282f, 46f to 282f), INK.copy(alpha = 0.16f))
    // Чайки
    val gull = Stroke(1.3f, cap = StrokeCap.Round)
    drawPath(Path().apply { moveTo(80f, 170f); relativeQuadraticTo(5f, -5f, 9f, 0f); relativeQuadraticTo(4f, -5f, 9f, 0f) }, INK.copy(alpha = 0.16f), style = gull)
    drawPath(Path().apply { moveTo(104f, 156f); relativeQuadraticTo(4f, -4f, 7f, 0f); relativeQuadraticTo(3f, -4f, 7f, 0f) }, INK.copy(alpha = 0.16f), style = gull)
}

// ─── Таймер: шкала вокруг циферблата ──────────────────────────────────────

private fun DrawScope.bezel() {
    val cx = 195f
    val cy = 296f
    val r = 186f
    drawCircle(INK.copy(alpha = 0.05f), radius = r + 40f, center = Offset(cx, cy), style = Stroke(1f))
    drawCircle(INK.copy(alpha = 0.035f), radius = r + 80f, center = Offset(cx, cy), style = Stroke(1f))
    for (i in 0 until 120) {
        val a = Math.toRadians(i * 3.0)
        val major = i % 10 == 0
        val mid = i % 5 == 0
        val len = if (major) 16f else if (mid) 10f else 5f
        val alpha = if (major) 0.16f else if (mid) 0.11f else 0.07f
        val ca = cos(a).toFloat()
        val sa = sin(a).toFloat()
        drawLine(
            INK.copy(alpha = alpha),
            Offset(cx + r * ca, cy + r * sa),
            Offset(cx + (r - len) * ca, cy + (r - len) * sa),
            strokeWidth = if (major) 1.4f else 1f,
        )
    }
}

// ─── Расписание: путь солнца ──────────────────────────────────────────────

private const val ARC = 118f // высота дуги в полдень, в единицах макета
private val SUN = Color(0xFFF0A04B)

/**
 * Дуга из точек — по точке на каждый час с 6:00 до 18:00, точно над делениями полосы суток;
 * пунктир вниз к полосе в 6, 9, 12, 15, 18 ч; тёплое свечение у горизонта; солнце — там,
 * где оно сейчас (ночью солнца нет).
 */
private fun DrawScope.sunPath(left: Float, width: Float, base: Float, s: Float, hour: Float?) {
    fun x(h: Float) = left + width * h / 24f
    fun y(h: Float) = base - ARC * s * sin(Math.PI * (h - 6f) / 12f).toFloat()

    // Тёплый край неба у горизонта
    drawRect(
        Brush.verticalGradient(0f to SUN.copy(alpha = 0f), 1f to SUN.copy(alpha = 0.07f), startY = base - 96f * s, endY = base),
        topLeft = Offset(0f, base - 96f * s),
        size = Size(size.width, 96f * s),
    )
    val dash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(2f * s, 4f * s))
    for (h in listOf(6f, 9f, 12f, 15f, 18f)) {
        drawLine(INK.copy(alpha = 0.05f), Offset(x(h), y(h) + 6f * s), Offset(x(h), base), strokeWidth = s, pathEffect = dash)
    }
    for (h in 6..18) drawCircle(INK.copy(alpha = 0.34f), radius = 1.7f * s, center = Offset(x(h.toFloat()), y(h.toFloat())))
    drawLine(INK.copy(alpha = 0.08f), Offset(0f, base), Offset(size.width, base), strokeWidth = s)

    if (hour != null && hour in 6f..18f) {
        val c = Offset(x(hour), y(hour))
        drawCircle(
            Brush.radialGradient(0f to SUN.copy(alpha = 0.45f), 1f to SUN.copy(alpha = 0f), center = c, radius = 34f * s),
            radius = 34f * s,
            center = c,
        )
        drawCircle(SUN.copy(alpha = 0.55f), radius = 8f * s, center = c)
    }
}

// ─── Приложения: созвездия ────────────────────────────────────────────────

/** Россыпь звёзд — одна и та же при каждом запуске (постоянный сид). */
private val STAR_FIELD: List<Triple<Offset, Float, Float>> = run {
    val r = kotlin.random.Random(7)
    List(70) {
        Triple(
            Offset(r.nextFloat() * W, r.nextFloat() * 480f),
            listOf(0.7f, 0.9f, 1.1f, 1.4f)[r.nextInt(4)],
            listOf(0.10f, 0.14f, 0.18f, 0.24f)[r.nextInt(4)],
        )
    }
}

private val CONSTELLATIONS = listOf(
    listOf(40f to 60f, 78f to 46f, 112f to 70f, 150f to 58f, 170f to 96f),
    listOf(250f to 40f, 290f to 64f, 330f to 52f, 352f to 92f, 318f to 120f, 290f to 64f),
    listOf(60f to 230f, 100f to 210f, 132f to 250f, 176f to 236f),
    listOf(268f to 300f, 300f to 270f, 342f to 286f, 360f to 250f),
)

private fun DrawScope.stars() {
    for ((p, r, a) in STAR_FIELD) drawCircle(INK.copy(alpha = a), radius = r, center = p)
    for (c in CONSTELLATIONS) {
        val path = Path().apply {
            moveTo(c[0].first, c[0].second)
            for (i in 1 until c.size) lineTo(c[i].first, c[i].second)
        }
        drawPath(path, INK.copy(alpha = 0.10f), style = Stroke(1f, join = StrokeJoin.Round))
        for ((x, y) in c) drawCircle(INK.copy(alpha = 0.30f), radius = 2f, center = Offset(x, y))
    }
}
