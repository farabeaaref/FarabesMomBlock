package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.farabe.farabesmomblock.engine.BlockColor

// Chocolate wooden gameboard slot colors matching reference
val WoodBoardSlotBg = Color(0xFF1E1513)
val WoodBoardSlotBorder = Color(0xFF33231F)
val WoodBoardSlotShadow = Color(0xFF0F0A09)

/**
 * 3D Gummy / Enamel Dimensional Block Tile with specular pill highlights
 * and embossed nostalgic Bangladeshi motifs (matching reference game art).
 */
@Composable
fun BlockTile(
    color: BlockColor?,
    modifier: Modifier = Modifier,
    isGhost: Boolean = false,
    isClearing: Boolean = false,
    showMotif: Boolean = true,
    cornerRadius: Dp = 8.dp
) {
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isClearing) {
        if (isClearing) {
            scaleAnim.animateTo(0f, animationSpec = tween(durationMillis = 240))
        } else {
            scaleAnim.snapTo(1f)
        }
    }

    if (color == null) {
        // Recessed wooden pocket matching reference image
        Box(
            modifier = modifier
                .padding(2.5.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(WoodBoardSlotBg)
                .border(0.8.dp, WoodBoardSlotBorder, RoundedCornerShape(cornerRadius))
                .drawBehind {
                    // Inner top-left depression shadow
                    drawRoundRect(
                        color = WoodBoardSlotShadow.copy(alpha = 0.6f),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height * 0.35f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
        )
    } else {
        val baseColor = Color(color.primaryHex)
        val highlightColor = Color(color.highlightHex)
        val shadowColor = Color(color.shadowHex)
        val motifColor = Color(color.motifAccentHex)

        val alpha = if (isGhost) 0.45f else 1.0f

        Box(
            modifier = modifier
                .scale(scaleAnim.value)
                .padding(2.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            highlightColor.copy(alpha = alpha),
                            baseColor.copy(alpha = alpha),
                            shadowColor.copy(alpha = alpha)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isGhost) Color.White.copy(alpha = 0.85f) else highlightColor.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .drawBehind {
                    if (!isGhost) {
                        // 1. Curved pill specular reflection on top (matching reference screenshot)
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.65f),
                            topLeft = Offset(size.width * 0.14f, size.height * 0.10f),
                            size = Size(size.width * 0.72f, size.height * 0.28f),
                            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                        )
                        // 2. Extra bright specular glint dot
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = size.minDimension * 0.08f,
                            center = Offset(size.width * 0.25f, size.height * 0.22f)
                        )
                        // 3. Bottom rounded depth shadow bevel
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.35f),
                            topLeft = Offset(size.width * 0.10f, size.height * 0.72f),
                            size = Size(size.width * 0.80f, size.height * 0.20f),
                            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Delicate embossed nostalgic Bangladeshi heritage motif
            if (showMotif) {
                BangladeshiMotifCanvas(
                    motif = color.motif,
                    accentColor = if (isGhost) Color.White else motifColor,
                    alpha = if (isGhost) 0.65f else 0.85f,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                )
            }
        }
    }
}
