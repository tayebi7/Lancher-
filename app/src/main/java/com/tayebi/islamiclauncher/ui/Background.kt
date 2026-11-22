package com.tayebi.islamiclauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Background: a night mosque scene drawn in vector code (no copyrighted artwork).
 * To use a real photo, add app/src/main/res/drawable/bg_mosque.jpg – it is picked up automatically.
 */
@Composable
fun AppBackground(kind: Int, custom: androidx.compose.ui.graphics.ImageBitmap? = null) {
    val ctx = LocalContext.current
    val resId = remember(kind) {
        val n = when (kind) { 0 -> "bg_blue_gold"; 1 -> "bg_emerald"; else -> "" }
        if (n.isEmpty()) 0 else ctx.resources.getIdentifier(n, "drawable", ctx.packageName)
    }
    Box(Modifier.fillMaxSize()) {
        if (kind == 8 && custom != null) {
            Image(custom, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else if (resId != 0) {
            Image(painterResource(resId), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Canvas(Modifier.fillMaxSize()) {
                when (kind) {
                    9 -> scene(Color(0xFF040A20), Color(0xFF0B2A5B), Color(0xFF14486E), true)
                    10 -> scene(Color(0xFF03130F), Color(0xFF0A3B2E), Color(0xFF117055), true)
                    3 -> scene(Color(0xFF14082B), Color(0xFF4A1F6B), Color(0xFF8A3A6B), true)
                    4 -> scene(Color(0xFF1B1030), Color(0xFF8A4A3B), Color(0xFFE59A4A), true)
                    5 -> { scene(Color(0xFF030818), Color(0xFF0B1F4A), Color(0xFF1C3A6B), false); dunes() }
                    6 -> { drawRect(Brush.verticalGradient(listOf(Color(0xFF03130F), Color(0xFF0A4D3A), Color(0xFF062A21)))); lattice() }
                    7 -> { drawRect(Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF14110A), Color(0xFF000000)))); lattice() }
                    else -> {
                        drawRect(Brush.verticalGradient(listOf(Color(0xFF040A20), Color(0xFF0B2250), Color(0xFF06123A))))
                        lattice()
                    }
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(Color(0x33000000), Color(0x00000000), Color(0x66000000))))
        }
    }
}

private fun DrawScope.scene(top: Color, mid: Color, low: Color, withMosque: Boolean) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(top, mid, low), startY = 0f, endY = h * 0.68f), size = Size(w, h * 0.68f))
    // stars
    val r = Random(11)
    repeat(150) {
        drawCircle(
            Color.White.copy(alpha = 0.2f + r.nextFloat() * 0.6f),
            radius = (0.6f + r.nextFloat() * 1.6f) * w / 1920f,
            center = Offset(r.nextFloat() * w, r.nextFloat() * h * 0.5f)
        )
    }
    // crescent moon
    val c = Offset(w * 0.45f, h * 0.15f)
    val rr = w * 0.034f
    val a = Path().apply { addOval(Rect(c, rr)) }
    val b = Path().apply { addOval(Rect(Offset(c.x + rr * 0.55f, c.y - rr * 0.15f), rr * 0.92f)) }
    drawPath(
        Path.combine(PathOperation.Difference, a, b),
        Brush.linearGradient(listOf(Color(0xFFFFF6D2), Color(0xFFE6C36A)), start = Offset(c.x - rr, c.y - rr), end = Offset(c.x + rr, c.y + rr))
    )
    drawCircle(Color(0x22FFF3C4), radius = rr * 2.2f, center = c)
    val base = h * 0.66f
    // water
    drawRect(Brush.verticalGradient(listOf(low, top), startY = base, endY = h), Offset(0f, base), Size(w, h - base))
    if (withMosque) {
        withTransform({ scale(1f, -1f, Offset(0f, base)) }) { mosque(0.22f) }
        mosque(1f)
        val pc = Color(0xFF020712)
        for (x in listOf(0.03f, 0.10f, 0.17f)) palm(w * x, base, h * (0.30f + x), -w * 0.01f, pc)
        for (x in listOf(0.97f, 0.90f, 0.83f)) palm(w * x, base, h * (0.30f + (1 - x)), w * 0.01f, pc)
        // glints on water
        val rg = Random(5)
        repeat(60) {
            val y = base + rg.nextFloat() * (h - base)
            val x = w * (0.3f + rg.nextFloat() * 0.4f)
            drawLine(Color(0x33FFE2A0), Offset(x, y), Offset(x + w * 0.02f * rg.nextFloat(), y), strokeWidth = 1.5f * w / 1920f)
        }
    }
}

private fun DrawScope.dunes() {
    val w = size.width
    val h = size.height
    val layers = listOf(0.62f to Color(0xFF1A2340), 0.72f to Color(0xFF111A33), 0.84f to Color(0xFF080E22))
    for ((y0, c) in layers) {
        val p = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * y0)
            cubicTo(w * 0.25f, h * (y0 - 0.12f), w * 0.45f, h * (y0 + 0.06f), w * 0.65f, h * (y0 - 0.05f))
            cubicTo(w * 0.8f, h * (y0 - 0.1f), w * 0.9f, h * y0, w, h * (y0 - 0.04f))
            lineTo(w, h)
            close()
        }
        drawPath(p, c)
    }
}

private fun DrawScope.lattice() {
    val w = size.width
    val g = Color(0xFFD9B44A).copy(alpha = 0.07f)
    val step = w / 20f
    var x = 0f
    while (x < w + step) {
        var y = 0f
        while (y < size.height + step) {
            star8(Offset(x, y), step * 0.36f, g, false)
            y += step
        }
        x += step
    }
}

internal fun DrawScope.star8(center: Offset, radius: Float, color: Color, fill: Boolean = true, stroke: Float = 1.5f) {
    val p = Path()
    for (i in 0 until 16) {
        val rad = if (i % 2 == 0) radius else radius * 0.5f
        val ang = i * PI / 8 - PI / 2
        val pt = Offset(center.x + (cos(ang) * rad).toFloat(), center.y + (sin(ang) * rad).toFloat())
        if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
    }
    p.close()
    if (fill) drawPath(p, color) else drawPath(p, color, style = Stroke(stroke))
}

private fun DrawScope.dome(cx: Float, y: Float, rw: Float, rh: Float, a: Float) {
    val p = Path().apply {
        moveTo(cx - rw, y)
        cubicTo(cx - rw, y - rh * 1.1f, cx - rw * 0.25f, y - rh * 1.3f, cx, y - rh * 1.55f)
        cubicTo(cx + rw * 0.25f, y - rh * 1.3f, cx + rw, y - rh * 1.1f, cx + rw, y)
        close()
    }
    drawPath(
        p,
        Brush.verticalGradient(
            listOf(Color(0xFFFFF3CF).copy(alpha = a), Color(0xFFD9B060).copy(alpha = a)),
            startY = y - rh * 1.55f, endY = y
        )
    )
}

private fun DrawScope.minaret(x: Float, base: Float, hw: Float, hh: Float, a: Float) {
    val top = base - hh
    val light = Color(0xFFF6E3B0).copy(alpha = a)
    val dark = Color(0xFFB98B48).copy(alpha = a)
    drawRect(Brush.horizontalGradient(listOf(dark, light, dark), startX = x - hw, endX = x + hw), Offset(x - hw, top), Size(hw * 2, hh))
    for (f in listOf(0.0f, 0.1f)) drawRect(Color(0xFFFFEFC2).copy(alpha = a), Offset(x - hw * 1.7f, top + hh * (0.08f + f)), Size(hw * 3.4f, hh * 0.014f))
    val cap = Path().apply {
        moveTo(x - hw * 1.5f, top + hh * 0.08f)
        lineTo(x, top - hh * 0.12f)
        lineTo(x + hw * 1.5f, top + hh * 0.08f)
        close()
    }
    drawPath(cap, Color(0xFFFFE08A).copy(alpha = a))
    drawLine(Color(0xFFFFE08A).copy(alpha = a), Offset(x, top - hh * 0.12f), Offset(x, top - hh * 0.17f), strokeWidth = hw * 0.25f)
    var wy = top + hh * 0.3f
    while (wy < base - hh * 0.1f) {
        drawRoundRect(Color(0xFFFFD27A).copy(alpha = a), Offset(x - hw * 0.22f, wy), Size(hw * 0.44f, hh * 0.035f), CornerRadius(hw * 0.2f))
        wy += hh * 0.1f
    }
}

private fun DrawScope.mosque(a: Float) {
    val w = size.width
    val h = size.height
    val base = h * 0.66f
    val cx = w * 0.5f
    val wall = Brush.verticalGradient(listOf(Color(0xFFF6E3B0).copy(alpha = a), Color(0xFFC79A50).copy(alpha = a)), startY = base - h * 0.1f, endY = base)
    // main hall
    drawRect(wall, Offset(cx - w * 0.17f, base - h * 0.1f), Size(w * 0.34f, h * 0.1f))
    drawRect(Color(0xFFFFEFC2).copy(alpha = a), Offset(cx - w * 0.17f, base - h * 0.106f), Size(w * 0.34f, h * 0.009f))
    for (s in listOf(-1f, 1f)) {
        dome(cx + s * w * 0.105f, base - h * 0.106f, w * 0.03f, h * 0.045f, a)
        dome(cx + s * w * 0.15f, base - h * 0.106f, w * 0.021f, h * 0.032f, a)
        minaret(cx + s * w * 0.215f, base, w * 0.0085f, h * 0.38f, a)
    }
    // drum + main dome
    drawRect(wall, Offset(cx - w * 0.052f, base - h * 0.145f), Size(w * 0.104f, h * 0.04f))
    dome(cx, base - h * 0.145f, w * 0.058f, h * 0.085f, a)
    drawLine(Color(0xFFFFE08A).copy(alpha = a), Offset(cx, base - h * 0.27f), Offset(cx, base - h * 0.31f), strokeWidth = w * 0.0013f, cap = StrokeCap.Round)
    drawCircle(Color(0xFFFFE08A).copy(alpha = a), w * 0.0035f, Offset(cx, base - h * 0.315f))
    // glowing arches
    val glow = Color(0xFFFFD27A).copy(alpha = a)
    for (i in -7..7) {
        if (i == 0) continue
        val x = cx + i * w * 0.0215f
        drawRoundRect(glow, Offset(x - w * 0.0055f, base - h * 0.075f), Size(w * 0.011f, h * 0.055f), CornerRadius(w * 0.0055f))
    }
    val iwan = Path().apply {
        moveTo(cx - w * 0.026f, base)
        lineTo(cx - w * 0.026f, base - h * 0.055f)
        cubicTo(cx - w * 0.026f, base - h * 0.105f, cx + w * 0.026f, base - h * 0.105f, cx + w * 0.026f, base - h * 0.055f)
        lineTo(cx + w * 0.026f, base)
        close()
    }
    drawPath(iwan, Color(0xFFFFB84D).copy(alpha = a))
    // forecourt
    drawRect(Color(0xFFE8CF98).copy(alpha = a * 0.6f), Offset(cx - w * 0.22f, base - h * 0.004f), Size(w * 0.44f, h * 0.006f))
}

private fun DrawScope.palm(x: Float, base: Float, ht: Float, lean: Float, col: Color) {
    val top = Offset(x + lean, base - ht)
    val trunk = Path().apply {
        moveTo(x - ht * 0.014f, base)
        quadraticBezierTo(x + lean * 0.1f, base - ht * 0.5f, top.x - ht * 0.007f, top.y)
        lineTo(top.x + ht * 0.007f, top.y)
        quadraticBezierTo(x + lean * 0.1f + ht * 0.02f, base - ht * 0.5f, x + ht * 0.014f, base)
        close()
    }
    drawPath(trunk, col)
    val fronds = listOf(-1f to 0.30f, -0.75f to 0.05f, -0.4f to -0.12f, 0f to -0.18f, 0.4f to -0.12f, 0.75f to 0.05f, 1f to 0.30f)
    val len = ht * 0.42f
    for ((dx, dy) in fronds) {
        val p = Path().apply {
            moveTo(top.x, top.y)
            quadraticBezierTo(top.x + dx * len * 0.5f, top.y - len * 0.32f, top.x + dx * len, top.y + dy * len)
        }
        drawPath(p, col, style = Stroke(ht * 0.012f, cap = StrokeCap.Round))
    }
}

/** Fine golden geometric frame around the whole screen. */
@Composable
fun OrnamentFrame() {
    val orn = androidx.compose.ui.res.imageResource(com.tayebi.islamiclauncher.R.drawable.ornament_gold)
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val u = w / 1920f
        val g = Color(0xFFD9B44A)
        drawRoundRect(g.copy(alpha = 0.9f), Offset(8 * u, 8 * u), Size(w - 16 * u, h - 16 * u), CornerRadius(40 * u), style = Stroke(3 * u))
        drawRoundRect(g.copy(alpha = 0.4f), Offset(20 * u, 20 * u), Size(w - 40 * u, h - 40 * u), CornerRadius(30 * u), style = Stroke(1.5f * u))
        val tile = (34 * u).toInt().coerceAtLeast(8)
        var x = (90 * u).toInt()
        while (x < w - 120 * u) {
            drawImage(orn, IntOffset.Zero, IntSize(96, 96), IntOffset(x, (12 * u).toInt()), IntSize(tile, tile), alpha = 0.75f)
            drawImage(orn, IntOffset.Zero, IntSize(96, 96), IntOffset(x, (h - 12 * u).toInt() - tile), IntSize(tile, tile), alpha = 0.75f)
            x += tile
        }
        var y = 120 * u
        while (y < h - 120 * u) {
            star8(Offset(14 * u, y), 6 * u, g.copy(alpha = 0.7f))
            star8(Offset(w - 14 * u, y), 6 * u, g.copy(alpha = 0.7f))
            y += 56 * u
        }
        for (p in listOf(Offset(40 * u, 40 * u), Offset(w - 40 * u, 40 * u), Offset(40 * u, h - 40 * u), Offset(w - 40 * u, h - 40 * u))) {
            star8(p, 26 * u, g.copy(alpha = 0.25f))
            star8(p, 26 * u, g, false, 2 * u)
            star8(p, 12 * u, g)
        }
    }
}
