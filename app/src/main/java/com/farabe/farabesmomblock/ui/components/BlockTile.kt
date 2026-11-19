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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.farabe.farabesmomblock.engine.BlockColor

// Rich carved mahogany gameboard slot colors
val WoodBoardSlotBg = Color(0xFF1C1311)
val WoodBoardSlotBorder = Color(0xFF3A2823)
val WoodBoardSlotShadow = Color(0xFF0C0706)

/**
 * 3D Blender-Style Faceted Crystal & Enamel Block Tile!
 * Renders 5 physically shaded 3D trapezoid bevel facets (Top Lit Bevel, Left Warm Bevel,
 * Right & Bottom Deep Shadow Bevels, and Recessed Inner Jewel Crown with studio reflection)
 * plus delicate embossed Bangladeshi cultural motifs.
 */
@Composable
fun BlockTile(
    color: BlockColor?,
    modifier: Modifier = Modifier,
    row: Int = 0,
    col: Int = 0,
    isGhost: Boolean = false,
    isClearing: Boolean = false,
    showMotif: Boolean = true,
    cornerRadius: Dp = 8.dp
) {
    val scaleAnim = remember { Animatable(1f) }
    val hasFilledColor = color != null && !isGhost

    LaunchedEffect(hasFilledColor) {
        if (hasFilledColor && !isClearing) {
            scaleAnim.snapTo(1.15f)
            scaleAnim.animateTo(1f, animationSpec = tween(durationMillis = 180))
        }
    }

    LaunchedEffect(isClearing) {
        if (isClearing) {
            scaleAnim.animateTo(0f, animationSpec = tween(durationMillis = 230))
        } else {
            scaleAnim.snapTo(1f)
        }
    }

    if (color == null) {
        // 3D Unreal-Engine PBR Recessed Crystal Socket with Quadrant & Checkerboard Gaming Color Identity!
        val isEven = (row + col) % 2 == 0
        val quadrantIndex = (if (row >= 4) 2 else 0) + (if (col >= 4) 1 else 0)

        val socketTopColor: Color
        val socketBottomColor: Color
        val socketRimGlow: Color
        when (quadrantIndex) {
            0 -> { // Top-Left 4x4: Royal Sapphire & Electric Cyan
                socketTopColor = if (isEven) Color(0xFF1E3A78) else Color(0xFF15295A)
                socketBottomColor = if (isEven) Color(0xFF294F9E) else Color(0xFF1D3978)
                socketRimGlow = Color(0xFF00E5FF)
            }
            1 -> { // Top-Right 4x4: Imperial Amethyst & Neon Violet
                socketTopColor = if (isEven) Color(0xFF3B1F7A) else Color(0xFF2A1559)
                socketBottomColor = if (isEven) Color(0xFF4F2B9E) else Color(0xFF391D75)
                socketRimGlow = Color(0xFFE040FB)
            }
            2 -> { // Bottom-Left 4x4: Cyber Teal & Emerald Jade
                socketTopColor = if (isEven) Color(0xFF124A59) else Color(0xFF0C3540)
                socketBottomColor = if (isEven) Color(0xFF1B6478) else Color(0xFF134857)
                socketRimGlow = Color(0xFF00E676)
            }
            else -> { // Bottom-Right 4x4: Royal Crimson-Plum & Sunset Gold
                socketTopColor = if (isEven) Color(0xFF4F1D4C) else Color(0xFF381336)
                socketBottomColor = if (isEven) Color(0xFF692865) else Color(0xFF4D1B4A)
                socketRimGlow = Color(0xFFFFCA28)
            }
        }

        Box(
            modifier = modifier
                .padding(2.0.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val rPx = cornerRadius.toPx()
                    val bev = (size.minDimension * 0.14f).coerceAtLeast(2.8f)

                    // 1. Outer recessed metallic socket body
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(socketTopColor, socketBottomColor),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        ),
                        cornerRadius = CornerRadius(rPx, rPx)
                    )

                    // 2. Top-left 3D sunken inner occlusion shadow
                    drawRoundRect(
                        color = Color(0xFF070B19).copy(alpha = 0.65f),
                        topLeft = Offset(0f, 0f),
                        size = Size(w, bev * 1.35f),
                        cornerRadius = CornerRadius(rPx, rPx)
                    )

                    // 3. Inner 3D crystal socket dish
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                socketRimGlow.copy(alpha = if (isEven) 0.22f else 0.13f),
                                Color(0xFF0B1026).copy(alpha = 0.45f)
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.62f
                        ),
                        topLeft = Offset(bev, bev),
                        size = Size(w - bev * 2f, h - bev * 2f),
                        cornerRadius = CornerRadius((rPx - 2f).coerceAtLeast(3f), (rPx - 2f).coerceAtLeast(3f))
                    )

                    // 4. Center 3D micro-diamond docking node
                    drawCircle(
                        color = socketRimGlow.copy(alpha = if (isEven) 0.40f else 0.25f),
                        radius = (size.minDimension * 0.065f).coerceAtLeast(1.8f),
                        center = Offset(w * 0.5f, h * 0.5f)
                    )

                    // 5. Crisp 3D rim border with gaming color identity
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                socketRimGlow.copy(alpha = if (isEven) 0.65f else 0.42f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        ),
                        cornerRadius = CornerRadius(rPx, rPx),
                        style = Stroke(width = 1.3f)
                    )
                }
        )
    } else {
        val baseColor = Color(color.primaryHex)
        val highlightColor = Color(color.highlightHex)
        val shadowColor = Color(color.shadowHex)
        val motifColor = Color(color.motifAccentHex)

        if (isGhost) {
            // High-Clarity 3D Translucent Jewel Footprint (Mom clearly sees exact landing cells)
            Box(
                modifier = modifier
                    .padding(1.8.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                highlightColor.copy(alpha = 0.62f),
                                baseColor.copy(alpha = 0.48f),
                                shadowColor.copy(alpha = 0.42f)
                            )
                        )
                    )
                    .border(
                        width = 2.0.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White, highlightColor.copy(alpha = 0.92f))
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .drawBehind {
                        val inset = (size.minDimension * 0.18f).coerceAtLeast(3f)
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.24f),
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - inset * 2f, (size.height - inset * 2f) * 0.45f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
            )
        } else {
            Box(
                modifier = modifier
                    .scale(scaleAnim.value)
                    .padding(1.6.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .drawBehind {
                        val w = size.width
                        val h = size.height
                        val bev = (size.minDimension * 0.17f).coerceAtLeast(3.5f)
                        val rPx = cornerRadius.toPx()

                        // 0. Base jewel body
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(highlightColor, baseColor, shadowColor),
                                start = Offset(0f, 0f),
                                end = Offset(w, h)
                            ),
                            cornerRadius = CornerRadius(rPx, rPx)
                        )

                        // 1. Top 3D Chamfer Bevel (Bright Studio Key Light)
                        val topFacet = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(w, 0f)
                            lineTo(w - bev, bev)
                            lineTo(bev, bev)
                            close()
                        }
                        drawPath(
                            path = topFacet,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.72f),
                                    highlightColor.copy(alpha = 0.85f)
                                ),
                                startY = 0f,
                                endY = bev
                            )
                        )

                        // 2. Left 3D Chamfer Bevel (Warm Fill Light)
                        val leftFacet = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(bev, bev)
                            lineTo(bev, h - bev)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = leftFacet,
                            color = highlightColor.copy(alpha = 0.55f)
                        )

                        // 3. Right 3D Chamfer Bevel (Mid-Shadow)
                        val rightFacet = Path().apply {
                            moveTo(w, 0f)
                            lineTo(w, h)
                            lineTo(w - bev, h - bev)
                            lineTo(w - bev, bev)
                            close()
                        }
                        drawPath(
                            path = rightFacet,
                            color = shadowColor.copy(alpha = 0.70f)
                        )

                        // 4. Bottom 3D Chamfer Bevel (Deep Ambient Occlusion Shadow)
                        val bottomFacet = Path().apply {
                            moveTo(0f, h)
                            lineTo(bev, h - bev)
                            lineTo(w - bev, h - bev)
                            lineTo(w, h)
                            close()
                        }
                        drawPath(
                            path = bottomFacet,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    shadowColor.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0.55f)
                                ),
                                startY = h - bev,
                                endY = h
                            )
                        )

                        // 5. Inner Crown Plateau (Glossy Enamel / Gem Core)
                        val innerW = (w - bev * 2f).coerceAtLeast(2f)
                        val innerH = (h - bev * 2f).coerceAtLeast(2f)
                        val innerR = (rPx * 0.65f).coerceAtLeast(2f)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    highlightColor,
                                    baseColor,
                                    shadowColor.copy(alpha = 0.92f)
                                ),
                                startY = bev,
                                endY = h - bev
                            ),
                            topLeft = Offset(bev, bev),
                            size = Size(innerW, innerH),
                            cornerRadius = CornerRadius(innerR, innerR)
                        )

                        // 6. Crisp Inner Bevel Rim Line
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.42f),
                            topLeft = Offset(bev, bev),
                            size = Size(innerW, innerH),
                            cornerRadius = CornerRadius(innerR, innerR),
                            style = Stroke(width = 1.1f)
                        )

                        // 7. Curved Studio Glass Specular Reflection on Crown
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.68f),
                                    Color.White.copy(alpha = 0.08f)
                                ),
                                startY = bev + 1f,
                                endY = bev + innerH * 0.45f
                            ),
                            topLeft = Offset(bev + innerW * 0.08f, bev + innerH * 0.06f),
                            size = Size(innerW * 0.84f, innerH * 0.36f),
                            cornerRadius = CornerRadius(innerR, innerR)
                        )

                        // 8. Corner Diamond Sparkle Glint
                        drawCircle(
                            color = Color.White.copy(alpha = 0.92f),
                            radius = size.minDimension * 0.065f,
                            center = Offset(bev + innerW * 0.18f, bev + innerH * 0.20f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (showMotif) {
                    BangladeshiMotifCanvas(
                        motif = color.motif,
                        accentColor = motifColor,
                        alpha = 0.90f,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.5.dp)
                    )
                }
            }
        }
    }
}
