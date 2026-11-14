package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Festive glowing fairy string lights along the side borders (as in the reference image).
 */
@Composable
fun FairyLights(
    isLeft: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fairyGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val bulbColors = listOf(
        Color(0xFFFF5252), // Red
        Color(0xFF40C4FF), // Cyan
        Color(0xFFFFD740), // Yellow
        Color(0xFF69F0AE), // Lime
        Color(0xFFFF4081), // Pink
        Color(0xFFFFAB40)  // Amber
    )

    Canvas(modifier = modifier.width(18.dp).fillMaxHeight()) {
        val h = size.height
        val numBulbs = 14
        val spacing = h / numBulbs
        val xAnchor = if (isLeft) 6f else size.width - 6f

        // Draw string wire
        val wirePath = Path().apply {
            moveTo(xAnchor, 0f)
            for (i in 0 until numBulbs) {
                val y = i * spacing
                val midY = y + spacing * 0.5f
                val dipX = if (isLeft) xAnchor + 6f else xAnchor - 6f
                quadraticBezierTo(dipX, midY, xAnchor, y + spacing)
            }
        }
        drawPath(wirePath, Color(0xFF422D24), style = Stroke(1.5f))

        // Draw glowing bulbs
        for (i in 0 until numBulbs) {
            val y = (i + 0.5f) * spacing
            val color = bulbColors[i % bulbColors.size]
            val flicker = if (i % 2 == 0) glowAlpha else (1.5f - glowAlpha).coerceIn(0.4f, 1f)

            // Outer soft glow halo
            drawCircle(
                color = color.copy(alpha = 0.28f * flicker),
                radius = 9f,
                center = Offset(xAnchor, y)
            )
            // Mid glow
            drawCircle(
                color = color.copy(alpha = 0.6f * flicker),
                radius = 5.5f,
                center = Offset(xAnchor, y)
            )
            // Bright bulb core
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 2.5f,
                center = Offset(xAnchor, y)
            )
        }
    }
}
