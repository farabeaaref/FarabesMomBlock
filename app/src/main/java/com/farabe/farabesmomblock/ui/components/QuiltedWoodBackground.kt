package com.farabe.farabesmomblock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val CozyLeatherDark = Color(0xFF1D120F)
val CozyLeatherLight = Color(0xFF2C1C17)
val QuiltStitchColor = Color(0xFF3B2721)

/**
 * Quilted diamond-stitched warm chocolate leather/wood background
 * matching the reference game art.
 */
@Composable
fun QuiltedWoodBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(CozyLeatherLight, CozyLeatherDark, Color(0xFF120B09))
            )
        )

        // Subtle diamond stitched grid
        val step = 48f
        val stitchW = 1.0f

        // Diagonal lines /
        var x = -h
        while (x < w + h) {
            drawLine(
                color = QuiltStitchColor.copy(alpha = 0.45f),
                start = Offset(x, 0f),
                end = Offset(x + h, h),
                strokeWidth = stitchW
            )
            x += step
        }

        // Diagonal lines \
        x = -h
        while (x < w + h) {
            drawLine(
                color = QuiltStitchColor.copy(alpha = 0.45f),
                start = Offset(x, h),
                end = Offset(x + h, 0f),
                strokeWidth = stitchW
            )
            x += step
        }
    }
}
