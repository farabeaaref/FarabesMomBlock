package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.farabe.farabesmomblock.engine.BlockColor
import com.farabe.farabesmomblock.engine.Board
import com.farabe.farabesmomblock.ui.theme.GoldAccent

val WoodBoardBevel = Color(0xFF4A342D)
val WoodBoardBg = Color(0xFF281C18)
val NeonLandingOrange = Color(0xFFFF6D00)
val NeonLandingGlow = Color(0xFFFF9E40)

/**
 * Rich Chocolate Wooden Board matching the reference game art,
 * featuring neon glowing target footprints and line-clear rays.
 */
@Composable
fun BoardView(
    board: Board,
    ghostCells: Map<Pair<Int, Int>, BlockColor> = emptyMap(),
    willClearRows: Set<Int> = emptySet(),
    willClearCols: Set<Int> = emptySet(),
    clearingCells: Set<Pair<Int, Int>> = emptySet(),
    candidateAnchor: Pair<Int, Int>? = null,
    candidateShapePoints: List<com.farabe.farabesmomblock.engine.Point2D>? = null,
    onCellClick: ((Int, Int) -> Unit)? = null,
    onBoardPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(WoodBoardBevel, WoodBoardBg, Color(0xFF1B110F))
                )
            )
            .border(2.5.dp, Color(0xFF5C4035), RoundedCornerShape(22.dp))
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { onBoardPositioned(it) }
                .drawBehind {
                    val cellW = size.width / board.cols
                    val cellH = size.height / board.rows

                    // 1. Highlight rows that will be cleared with golden glow
                    for (r in willClearRows) {
                        drawRoundRect(
                            color = GoldAccent.copy(alpha = shimmerAlpha * 0.7f),
                            topLeft = Offset(0f, r * cellH),
                            size = Size(size.width, cellH),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }

                    // 2. Highlight cols that will be cleared with golden glow
                    for (c in willClearCols) {
                        drawRoundRect(
                            color = GoldAccent.copy(alpha = shimmerAlpha * 0.7f),
                            topLeft = Offset(c * cellW, 0f),
                            size = Size(cellW, size.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }

                    // 3. 3D Holographic Golden-Flame Landing Target (exact inner grid coordinates)
                    if (candidateAnchor != null && candidateShapePoints != null && ghostCells.isNotEmpty()) {
                        for (pt in candidateShapePoints) {
                            val cr = candidateAnchor.first + pt.y
                            val cc = candidateAnchor.second + pt.x
                            if (board.isWithinBounds(cr, cc)) {
                                val rx = cc * cellW
                                val ry = cr * cellH

                                drawRoundRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            GoldAccent.copy(alpha = 0.50f * shimmerAlpha),
                                            NeonLandingGlow.copy(alpha = 0.24f * shimmerAlpha),
                                            Color.Transparent
                                        ),
                                        center = Offset(rx + cellW * 0.5f, ry + cellH * 0.5f),
                                        radius = cellW * 0.78f
                                    ),
                                    topLeft = Offset(rx - 4f, ry - 4f),
                                    size = Size(cellW + 8f, cellH + 8f),
                                    cornerRadius = CornerRadius(14f, 14f)
                                )
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFFFFF59D), GoldAccent, NeonLandingOrange)
                                    ),
                                    topLeft = Offset(rx + 2f, ry + 2f),
                                    size = Size(cellW - 4f, cellH - 4f),
                                    cornerRadius = CornerRadius(10f, 10f),
                                    style = Stroke(width = 3.0f)
                                )
                            }
                        }
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until board.rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in 0 until board.cols) {
                        val placedColor = board.getCell(r, c)
                        val ghostColor = ghostCells[Pair(r, c)]
                        val isClearing = clearingCells.contains(Pair(r, c))

                        val displayColor = placedColor ?: ghostColor
                        val isGhost = placedColor == null && ghostColor != null

                        BlockTile(
                            color = displayColor,
                            isGhost = isGhost,
                            isClearing = isClearing,
                            showMotif = true,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(enabled = onCellClick != null) {
                                    onCellClick?.invoke(r, c)
                                }
                        )
                    }
                }
            }
        }
        }
    }
}
