package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.farabe.farabesmomblock.engine.BlockShape
import kotlin.math.cos
import kotlin.math.sin

/**
 * Contour-Hugging 3D Edge Flame & Magical Ember Shader Effect!
 * Replaces the old rectangular box beam with organic, per-tile rising flame tongues,
 * volumetric rim glow, and floating golden embers that wrap strictly around the
 * actual polyomino block edges.
 */
@Composable
fun PieceEdgeFlameEffect(
    shape: BlockShape,
    cellSize: Dp,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "edgeFlame")
    val phase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flamePhase"
    )

    val baseColor = Color(shape.color.primaryHex)
    val brightColor = Color(shape.color.highlightHex)
    val goldFlame = Color(0xFFFFD54F)
    val coreFlame = Color(0xFFFFF9C4)

    val totalW = cellSize * shape.width
    val totalH = cellSize * shape.height

    Canvas(modifier = modifier.size(width = totalW, height = totalH)) {
        val cellPx = cellSize.toPx()

        for ((idx, pt) in shape.points.withIndex()) {
            val cx = pt.x * cellPx
            val cy = pt.y * cellPx

            // 1. Soft radial volumetric aura around each individual tile
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        goldFlame.copy(alpha = 0.55f),
                        baseColor.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = Offset(cx + cellPx * 0.5f, cy + cellPx * 0.5f),
                    radius = cellPx * 0.92f
                ),
                topLeft = Offset(cx - cellPx * 0.28f, cy - cellPx * 0.28f),
                size = Size(cellPx * 1.56f, cellPx * 1.56f),
                cornerRadius = CornerRadius(cellPx * 0.35f, cellPx * 0.35f)
            )

            // 2. Organic flickering flame tongues along top & outer edges of this tile
            val hasTopNeighbor = shape.points.any { it.x == pt.x && it.y == pt.y - 1 }
            if (!hasTopNeighbor) {
                val tongues = 4
                val tongueSpan = cellPx / tongues
                for (t in 0 until tongues) {
                    val wave = sin(phase * 2f + idx * 1.3f + t * 1.7f)
                    val flameH = cellPx * (0.34f + 0.16f * ((wave + 1f) * 0.5f))
                    val sx = cx + t * tongueSpan + tongueSpan * 0.5f
                    val sy = cy + 2f
                    val sway = cos(phase + t * 1.4f) * (tongueSpan * 0.35f)

                    val flamePath = Path().apply {
                        moveTo(sx - tongueSpan * 0.48f, sy)
                        quadraticBezierTo(
                            sx - tongueSpan * 0.22f + sway * 0.4f,
                            sy - flameH * 0.55f,
                            sx + sway,
                            sy - flameH
                        )
                        quadraticBezierTo(
                            sx + tongueSpan * 0.22f + sway * 0.4f,
                            sy - flameH * 0.55f,
                            sx + tongueSpan * 0.48f,
                            sy
                        )
                        close()
                    }

                    drawPath(
                        path = flamePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                baseColor.copy(alpha = 0.65f),
                                goldFlame.copy(alpha = 0.92f),
                                coreFlame
                            ),
                            startY = sy - flameH,
                            endY = sy
                        )
                    )
                }
            }

            // 3. Glowing neon-gold bevel rim around the tile perimeter
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(coreFlame, goldFlame, brightColor)
                ),
                topLeft = Offset(cx + 1.5f, cy + 1.5f),
                size = Size(cellPx - 3f, cellPx - 3f),
                cornerRadius = CornerRadius(cellPx * 0.22f, cellPx * 0.22f),
                style = Stroke(width = 2.6f)
            )

            // 4. Rising golden ember sparks above the tile
            for (s in 0..1) {
                val sparkProg = ((phase / 6.283185f) + (idx * 0.21f) + (s * 0.47f)) % 1f
                val sparkX = cx + cellPx * (0.25f + 0.5f * s) + sin(sparkProg * 6.28f + idx) * 6f
                val sparkY = cy - (sparkProg * cellPx * 0.75f)
                val sparkAlpha = (1f - sparkProg).coerceIn(0f, 1f)
                drawCircle(
                    color = coreFlame.copy(alpha = sparkAlpha * 0.9f),
                    radius = (1f - sparkProg * 0.5f) * 3.5f,
                    center = Offset(sparkX, sparkY)
                )
            }
        }
    }
}

@Composable
fun DragAuraBeam(
    beamColor: Color = Color(0xFFFF5722),
    modifier: Modifier = Modifier
) {
    // Retained for backwards compatibility if referenced elsewhere
}
