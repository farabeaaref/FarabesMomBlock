package com.farabe.farabesmomblock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.farabe.farabesmomblock.engine.BangladeshiMotif
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws crisp, authentic, nostalgic Bangladeshi folk motifs inside a block tile.
 * Renders Nakshi Kantha stitches, Shapla petals, Beli garlands, Jamdani weaves,
 * Rickshaw painted flowers, and Alpona spirals.
 */
@Composable
fun BangladeshiMotifCanvas(
    motif: BangladeshiMotif,
    accentColor: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 0.85f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val color = accentColor.copy(alpha = alpha)
        val strokeColor = accentColor.copy(alpha = (alpha * 0.9f).coerceIn(0f, 1f))
        val strokeWidth = (size.minDimension * 0.07f).coerceAtLeast(1.5f)

        when (motif) {
            BangladeshiMotif.SHAPLA -> drawShaplaLily(color, strokeWidth)
            BangladeshiMotif.BELI -> drawBeliJasmine(color, strokeWidth)
            BangladeshiMotif.NAKSHI_KANTHA -> drawNakshiKantha(color, strokeWidth)
            BangladeshiMotif.JAMDANI -> drawJamdaniWeave(color, strokeWidth)
            BangladeshiMotif.RICKSHAW_ART -> drawRickshawFlower(color, strokeWidth)
            BangladeshiMotif.KRISHNACHURA -> drawKrishnachura(color, strokeWidth)
            BangladeshiMotif.MATIR_PRODIP -> drawMatirProdip(color, strokeWidth)
            BangladeshiMotif.ALPONA -> drawAlponaMandala(color, strokeWidth)
            BangladeshiMotif.DOEL -> drawDoelWing(color, strokeWidth)
        }
    }
}

// 1. লাল শাপলা (Water Lily Blossom)
private fun DrawScope.drawShaplaLily(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.55f
    val r = size.minDimension * 0.32f

    // Center tall petal
    val centerPetal = Path().apply {
        moveTo(cx, cy + r * 0.4f)
        cubicTo(cx - r * 0.35f, cy, cx - r * 0.2f, cy - r * 0.9f, cx, cy - r)
        cubicTo(cx + r * 0.2f, cy - r * 0.9f, cx + r * 0.35f, cy, cx, cy + r * 0.4f)
        close()
    }
    drawPath(centerPetal, color, style = Stroke(strokeWidth))

    // Left petal
    val leftPetal = Path().apply {
        moveTo(cx, cy + r * 0.4f)
        cubicTo(cx - r * 0.5f, cy + r * 0.1f, cx - r * 0.85f, cy - r * 0.3f, cx - r * 0.7f, cy - r * 0.65f)
        cubicTo(cx - r * 0.45f, cy - r * 0.6f, cx - r * 0.2f, cy - r * 0.2f, cx, cy + r * 0.4f)
        close()
    }
    drawPath(leftPetal, color, style = Stroke(strokeWidth))

    // Right petal
    val rightPetal = Path().apply {
        moveTo(cx, cy + r * 0.4f)
        cubicTo(cx + r * 0.5f, cy + r * 0.1f, cx + r * 0.85f, cy - r * 0.3f, cx + r * 0.7f, cy - r * 0.65f)
        cubicTo(cx + r * 0.45f, cy - r * 0.6f, cx + r * 0.2f, cy - r * 0.2f, cx, cy + r * 0.4f)
        close()
    }
    drawPath(rightPetal, color, style = Stroke(strokeWidth))

    // Water ripple base
    drawArc(
        color = color,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.65f, cy + r * 0.25f),
        size = Size(r * 1.3f, r * 0.4f),
        style = Stroke(strokeWidth * 0.9f)
    )
}

// 2. বেলি ফুল (Fragrant Star Jasmine)
private fun DrawScope.drawBeliJasmine(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val r = size.minDimension * 0.35f

    // 6-petal jasmine flower
    for (i in 0 until 6) {
        val angle = Math.toRadians((i * 60.0))
        val px = cx + (r * 0.8f * cos(angle)).toFloat()
        val py = cy + (r * 0.8f * sin(angle)).toFloat()
        drawCircle(
            color = color,
            radius = r * 0.28f,
            center = Offset(px, py),
            style = Stroke(strokeWidth)
        )
    }
    // Center pollen dot
    drawCircle(color = color, radius = r * 0.22f, center = Offset(cx, cy), style = Fill)
}

// 3. নকশী কাঁথা (Embroidered Diamond Star Stitch)
private fun DrawScope.drawNakshiKantha(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val r = size.minDimension * 0.36f

    // Central diamond
    val diamond = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r, cy)
        lineTo(cx, cy + r)
        lineTo(cx - r, cy)
        close()
    }
    drawPath(diamond, color, style = Stroke(strokeWidth))

    // Inner cross stitch '+'
    drawLine(color, Offset(cx - r * 0.6f, cy), Offset(cx + r * 0.6f, cy), strokeWidth = strokeWidth)
    drawLine(color, Offset(cx, cy - r * 0.6f), Offset(cx, cy + r * 0.6f), strokeWidth = strokeWidth)

    // Corner stitch dots
    val dotR = strokeWidth * 0.9f
    drawCircle(color, dotR, Offset(cx - r * 0.55f, cy - r * 0.55f))
    drawCircle(color, dotR, Offset(cx + r * 0.55f, cy - r * 0.55f))
    drawCircle(color, dotR, Offset(cx - r * 0.55f, cy + r * 0.55f))
    drawCircle(color, dotR, Offset(cx + r * 0.55f, cy + r * 0.55f))
}

// 4. জামদানি (Jamdani Geometric Floral Weave)
private fun DrawScope.drawJamdaniWeave(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val r = size.minDimension * 0.34f

    // Outer octagon / stepped diamond
    val path = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r * 0.5f, cy - r * 0.5f)
        lineTo(cx + r, cy)
        lineTo(cx + r * 0.5f, cy + r * 0.5f)
        lineTo(cx, cy + r)
        lineTo(cx - r * 0.5f, cy + r * 0.5f)
        lineTo(cx - r, cy)
        lineTo(cx - r * 0.5f, cy - r * 0.5f)
        close()
    }
    drawPath(path, color, style = Stroke(strokeWidth))

    // Inner flower bud
    drawCircle(color, r * 0.25f, Offset(cx, cy), style = Stroke(strokeWidth))
    drawCircle(color, r * 0.12f, Offset(cx, cy), style = Fill)
}

// 5. রিকশা আর্ট (Painted Folk Rose)
private fun DrawScope.drawRickshawFlower(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val r = size.minDimension * 0.36f

    // Outer bold petals
    for (i in 0 until 5) {
        val angle = Math.toRadians((i * 72.0 - 18.0))
        val px = cx + (r * 0.65f * cos(angle)).toFloat()
        val py = cy + (r * 0.65f * sin(angle)).toFloat()
        drawCircle(color, r * 0.32f, Offset(px, py), style = Stroke(strokeWidth))
    }
    // Inner swirl
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 280f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.25f, cy - r * 0.25f),
        size = Size(r * 0.5f, r * 0.5f),
        style = Stroke(strokeWidth * 1.2f)
    )
}

// 6. কৃষ্ণচূড়া (Krishnachura Summer Flame)
private fun DrawScope.drawKrishnachura(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.52f
    val r = size.minDimension * 0.35f

    // 4 flared flame petals
    for (i in 0 until 4) {
        val angle = Math.toRadians((i * 90.0 + 45.0))
        val tipX = cx + (r * cos(angle)).toFloat()
        val tipY = cy + (r * sin(angle)).toFloat()
        val p = Path().apply {
            moveTo(cx, cy)
            quadraticBezierTo(
                cx + (r * 0.6f * cos(angle - 0.4)).toFloat(),
                cy + (r * 0.6f * sin(angle - 0.4)).toFloat(),
                tipX, tipY
            )
            quadraticBezierTo(
                cx + (r * 0.6f * cos(angle + 0.4)).toFloat(),
                cy + (r * 0.6f * sin(angle + 0.4)).toFloat(),
                cx, cy
            )
        }
        drawPath(p, color, style = Stroke(strokeWidth))
    }
    // Stamen center
    drawCircle(color, r * 0.16f, Offset(cx, cy), style = Fill)
}

// 7. মাটির প্রদীপ (Terracotta Oil Lamp)
private fun DrawScope.drawMatirProdip(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.58f
    val r = size.minDimension * 0.34f

    // Clay lamp bowl
    val bowl = Path().apply {
        moveTo(cx - r * 0.85f, cy)
        cubicTo(cx - r * 0.8f, cy + r * 0.7f, cx + r * 0.8f, cy + r * 0.7f, cx + r * 0.85f, cy)
        lineTo(cx + r * 0.6f, cy - r * 0.1f)
        lineTo(cx - r * 0.6f, cy - r * 0.1f)
        close()
    }
    drawPath(bowl, color, style = Stroke(strokeWidth))

    // Teardrop golden flame
    val flame = Path().apply {
        moveTo(cx, cy - r * 0.95f)
        cubicTo(cx + r * 0.35f, cy - r * 0.5f, cx + r * 0.3f, cy - r * 0.1f, cx, cy - r * 0.1f)
        cubicTo(cx - r * 0.3f, cy - r * 0.1f, cx - r * 0.35f, cy - r * 0.5f, cx, cy - r * 0.95f)
        close()
    }
    drawPath(flame, color, style = Fill)
}

// 8. আলপনা (Bengali Folk Paisley / Mandala)
private fun DrawScope.drawAlponaMandala(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val r = size.minDimension * 0.35f

    // Concentric spiral arcs
    drawArc(
        color = color,
        startAngle = 45f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.8f, cy - r * 0.8f),
        size = Size(r * 1.6f, r * 1.6f),
        style = Stroke(strokeWidth)
    )
    drawArc(
        color = color,
        startAngle = 225f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.45f, cy - r * 0.45f),
        size = Size(r * 0.9f, r * 0.9f),
        style = Stroke(strokeWidth * 1.1f)
    )
    drawCircle(color, r * 0.18f, Offset(cx, cy), style = Fill)
}

// 9. দোয়েল (Doel Feather / Wing Motif)
private fun DrawScope.drawDoelWing(color: Color, strokeWidth: Float) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val r = size.minDimension * 0.35f

    // Curved feather spine
    val spine = Path().apply {
        moveTo(cx - r * 0.6f, cy + r * 0.7f)
        cubicTo(cx - r * 0.2f, cy + r * 0.2f, cx + r * 0.2f, cy - r * 0.4f, cx + r * 0.7f, cy - r * 0.8f)
    }
    drawPath(spine, color, style = Stroke(strokeWidth * 1.2f))

    // Feather barbs
    for (i in 1..4) {
        val t = i / 5f
        val sx = cx - r * 0.6f + t * (r * 1.3f)
        val sy = cy + r * 0.7f - t * (r * 1.5f)
        drawLine(
            color = color,
            start = Offset(sx, sy),
            end = Offset(sx + r * 0.3f, sy + r * 0.15f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(sx, sy),
            end = Offset(sx - r * 0.28f, sy - r * 0.1f),
            strokeWidth = strokeWidth
        )
    }
}
