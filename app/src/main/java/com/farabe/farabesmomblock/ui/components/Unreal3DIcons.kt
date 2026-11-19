package com.farabe.farabesmomblock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Unreal-Engine Style Physically Shaded PBR Canvas Emblems & Icons.
 * Completely replaces all 2D flat Unicode emojis with multi-faceted 3D metallic
 * and crystal icons featuring studio rim lighting, ambient occlusion, and specular reflections.
 */

@Composable
fun Unreal3DCrownIcon(
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Ambient 3D gold aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.42f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.55f),
                radius = w * 0.6f
            )
        )

        // 3D Crown Body Path
        val crownPath = Path().apply {
            moveTo(w * 0.14f, h * 0.78f)
            lineTo(w * 0.08f, h * 0.30f)
            lineTo(w * 0.32f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.68f, h * 0.52f)
            lineTo(w * 0.92f, h * 0.30f)
            lineTo(w * 0.86f, h * 0.78f)
            close()
        }

        drawPath(
            path = crownPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF9C4),
                    Color(0xFFFFCA28),
                    Color(0xFFFF8F00),
                    Color(0xFFE65100)
                )
            )
        )
        drawPath(
            path = crownPath,
            brush = Brush.verticalGradient(
                listOf(Color.White, Color(0xFFFFD54F))
            ),
            style = Stroke(width = (w * 0.07f).coerceAtLeast(1.2f))
        )

        // 3D Crown Base Band
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFE65100), Color(0xFFFFE082), Color(0xFFE65100))
            ),
            topLeft = Offset(w * 0.12f, h * 0.74f),
            size = Size(w * 0.76f, h * 0.16f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        // 3D Inset Ruby & Cyan Gems
        drawCircle(Color(0xFFFF1744), radius = w * 0.09f, center = Offset(w * 0.50f, h * 0.60f))
        drawCircle(Color.White.copy(alpha = 0.85f), radius = w * 0.035f, center = Offset(w * 0.48f, h * 0.58f))
        drawCircle(Color(0xFF00E5FF), radius = w * 0.065f, center = Offset(w * 0.27f, h * 0.63f))
        drawCircle(Color(0xFF00E676), radius = w * 0.065f, center = Offset(w * 0.73f, h * 0.63f))
    }
}

@Composable
fun Unreal3DCoinIcon(
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width * 0.5f
        val cy = this.size.height * 0.5f
        val r = this.size.minDimension * 0.46f

        // 3D Drop Shadow
        drawCircle(
            color = Color(0xFF3E1C00).copy(alpha = 0.65f),
            radius = r,
            center = Offset(cx, cy + r * 0.10f)
        )
        // Outer Metallic Gold Bevel
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFE65100)),
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            ),
            radius = r,
            center = Offset(cx, cy)
        )
        // Recessed Inner Coin Dish
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFE65100), Color(0xFFFFCA28), Color(0xFFFFF176)),
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            ),
            radius = r * 0.76f,
            center = Offset(cx, cy)
        )
        // Embossed Center Diamond Star
        val star = Path().apply {
            moveTo(cx, cy - r * 0.48f)
            lineTo(cx + r * 0.22f, cy - r * 0.16f)
            lineTo(cx + r * 0.48f, cy)
            lineTo(cx + r * 0.22f, cy + r * 0.16f)
            lineTo(cx, cy + r * 0.48f)
            lineTo(cx - r * 0.22f, cy + r * 0.16f)
            lineTo(cx - r * 0.48f, cy)
            lineTo(cx - r * 0.22f, cy - r * 0.16f)
            close()
        }
        drawPath(star, Color(0xFFFFFDE7))
        // Rim Specular
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = (r * 0.12f).coerceAtLeast(1.1f))
        )
    }
}

@Composable
fun Unreal3DHeartIcon(
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val heartPath = Path().apply {
            moveTo(w * 0.50f, h * 0.88f)
            cubicTo(w * 0.10f, h * 0.62f, w * 0.04f, h * 0.26f, w * 0.28f, h * 0.15f)
            cubicTo(w * 0.42f, h * 0.09f, w * 0.50f, h * 0.22f, w * 0.50f, h * 0.28f)
            cubicTo(w * 0.50f, h * 0.22f, w * 0.58f, h * 0.09f, w * 0.72f, h * 0.15f)
            cubicTo(w * 0.96f, h * 0.26f, w * 0.90f, h * 0.62f, w * 0.50f, h * 0.88f)
            close()
        }

        drawPath(
            path = heartPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF80AB),
                    Color(0xFFFF1744),
                    Color(0xFFB71C1C),
                    Color(0xFF4A0010)
                ),
                center = Offset(w * 0.38f, h * 0.34f),
                radius = w * 0.65f
            )
        )
        drawPath(
            path = heartPath,
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.92f), Color(0xFFFFCA28).copy(alpha = 0.75f))
            ),
            style = Stroke(width = (w * 0.07f).coerceAtLeast(1.2f))
        )
        // Top-left 3D crystal specular highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.78f),
            radius = w * 0.09f,
            center = Offset(w * 0.29f, h * 0.29f)
        )
    }
}

@Composable
fun Unreal3DDiamondIcon(
    size: Dp = 18.dp,
    tint: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val gemPath = Path().apply {
            moveTo(w * 0.26f, h * 0.14f)
            lineTo(w * 0.74f, h * 0.14f)
            lineTo(w * 0.94f, h * 0.38f)
            lineTo(w * 0.50f, h * 0.90f)
            lineTo(w * 0.06f, h * 0.38f)
            close()
        }

        drawPath(
            path = gemPath,
            brush = Brush.linearGradient(
                colors = listOf(Color.White, tint, Color(0xFF006064), Color(0xFF00263A)),
                start = Offset(w * 0.2f, 0f),
                end = Offset(w * 0.8f, h)
            )
        )

        // Top crown facet
        val topCrown = Path().apply {
            moveTo(w * 0.26f, h * 0.14f)
            lineTo(w * 0.74f, h * 0.14f)
            lineTo(w * 0.66f, h * 0.38f)
            lineTo(w * 0.34f, h * 0.38f)
            close()
        }
        drawPath(topCrown, Color.White.copy(alpha = 0.45f))

        drawPath(
            path = gemPath,
            color = Color.White.copy(alpha = 0.9f),
            style = Stroke(width = (w * 0.065f).coerceAtLeast(1.1f))
        )
    }
}

@Composable
fun Unreal3DTargetIcon(
    size: Dp = 16.dp,
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width * 0.5f
        val cy = this.size.height * 0.5f
        val r = this.size.minDimension * 0.46f

        val outerColors = if (isCompleted) {
            listOf(Color(0xFFB9F6CA), Color(0xFF00E676), Color(0xFF1B5E20))
        } else {
            listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFE65100))
        }

        drawCircle(
            brush = Brush.radialGradient(outerColors, center = Offset(cx - r * 0.2f, cy - r * 0.2f), radius = r * 1.3f),
            radius = r,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFF1A0933),
            radius = r * 0.64f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = if (isCompleted) Color(0xFF00E676) else Color(0xFFFF1744),
            radius = r * 0.36f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White,
            radius = r * 0.14f,
            center = Offset(cx - r * 0.08f, cy - r * 0.08f)
        )
    }
}

@Composable
fun Unreal3DLightningIcon(
    size: Dp = 16.dp,
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val bolt = Path().apply {
            moveTo(w * 0.58f, h * 0.06f)
            lineTo(w * 0.18f, h * 0.54f)
            lineTo(w * 0.48f, h * 0.54f)
            lineTo(w * 0.38f, h * 0.94f)
            lineTo(w * 0.84f, h * 0.42f)
            lineTo(w * 0.54f, h * 0.42f)
            close()
        }

        drawPath(
            path = bolt,
            brush = Brush.verticalGradient(
                if (isCompleted) {
                    listOf(Color.White, Color(0xFF69F0AE), Color(0xFF00C853))
                } else {
                    listOf(Color.White, Color(0xFF00E5FF), Color(0xFF2979FF))
                }
            )
        )
        drawPath(
            path = bolt,
            color = Color.White,
            style = Stroke(width = (w * 0.06f).coerceAtLeast(1.0f))
        )
    }
}

@Composable
fun Unreal3DStageCrestIcon(
    levelNumber: Int = 1,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    val tierColors = when ((levelNumber - 1) / 10) {
        0 -> listOf(Color(0xFF00E5FF), Color(0xFF00838F))
        1 -> listOf(Color(0xFF69F0AE), Color(0xFF1B5E20))
        2 -> listOf(Color(0xFFFF80AB), Color(0xFFAD1457))
        3 -> listOf(Color(0xFFFFD54F), Color(0xFFE65100))
        4 -> listOf(Color(0xFFB388FF), Color(0xFF4A148C))
        5 -> listOf(Color(0xFFFF5252), Color(0xFF8E0000))
        6 -> listOf(Color(0xFF64FFDA), Color(0xFF004D40))
        7 -> listOf(Color(0xFFFFAB40), Color(0xFFBF360C))
        8 -> listOf(Color(0xFF82B1FF), Color(0xFF0D47A1))
        else -> listOf(Color(0xFFFFF59D), Color(0xFFFF6F00))
    }

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width * 0.5f
        val cy = this.size.height * 0.5f
        val r = this.size.minDimension * 0.46f

        val hex = Path().apply {
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60.0) - 90.0)
                val px = cx + (r * cos(angle)).toFloat()
                val py = cy + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }

        drawPath(
            path = hex,
            brush = Brush.linearGradient(
                colors = listOf(Color.White, tierColors[0], tierColors[1]),
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            )
        )
        drawPath(
            path = hex,
            brush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), Color(0xFFFF8F00))),
            style = Stroke(width = (r * 0.16f).coerceAtLeast(1.4f))
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.82f),
            radius = r * 0.25f,
            center = Offset(cx, cy)
        )
    }
}
