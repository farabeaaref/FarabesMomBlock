package com.farabe.farabesmomblock.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.farabe.farabesmomblock.R

/**
 * 3D Unreal-Engine Rendered Royal Companion Medallion!
 * Replaces the old 2D flat vector drawing with the high-resolution 3D rendered companion portrait
 * encased in a 3D specular gold and crystal bezel with studio rim lighting.
 */
@Composable
fun CuteMascot(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF4A148C), Color(0xFF1A0933))
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFF59D),
                        Color(0xFFFFB300),
                        Color(0xFF00E5FF),
                        Color(0xFFFFCA28)
                    )
                ),
                shape = CircleShape
            )
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.portrait_kitten),
            contentDescription = "3D Royal Companion",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    // 3D Glass Lens Specular Reflection & Inner Rim Shadow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.32f, size.height * 0.25f),
                            radius = size.minDimension * 0.45f
                        )
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = size.minDimension * 0.48f,
                        style = Stroke(width = 1.5f)
                    )
                }
        )
    }
}
