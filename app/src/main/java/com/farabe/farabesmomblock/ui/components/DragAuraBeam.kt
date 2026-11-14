package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Renders the blazing upward vertical beam of fire / light trail
 * directly above the dragged piece (matching the reference game image).
 */
@Composable
fun DragAuraBeam(
    beamColor: Color = Color(0xFFFF5722),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fireBeam")
    val beamPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beamPulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Vertical glowing flare gradient fading upwards
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    beamColor.copy(alpha = 0.35f * beamPulse),
                    Color(0xFFFF9800).copy(alpha = 0.70f * beamPulse),
                    Color(0xFFFFF176).copy(alpha = 0.95f * beamPulse)
                ),
                startY = 0f,
                endY = h
            )
        )

        // Center bright light core
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.4f * beamPulse),
                    Color.White.copy(alpha = 0.9f * beamPulse)
                ),
                startY = h * 0.2f,
                endY = h
            ),
            topLeft = Offset(w * 0.30f, 0f),
            size = androidx.compose.ui.geometry.Size(w * 0.40f, h)
        )
    }
}
