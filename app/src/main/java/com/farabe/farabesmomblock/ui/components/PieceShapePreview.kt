package com.farabe.farabesmomblock.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.farabe.farabesmomblock.engine.BlockShape

/**
 * Renders a polyomino block shape with authentic Bangladeshi embossed tiles.
 * Automatically adapts cell size for wide shapes (like 5-bars) to fit comfortably.
 */
@Composable
fun PieceShapePreview(
    shape: BlockShape,
    cellSize: Dp = 22.dp,
    showMotifs: Boolean = true,
    isGhost: Boolean = false,
    modifier: Modifier = Modifier
) {
    val totalWidth = cellSize * shape.width
    val totalHeight = cellSize * shape.height

    Box(
        modifier = modifier.size(width = totalWidth, height = totalHeight)
    ) {
        for (pt in shape.points) {
            BlockTile(
                color = shape.color,
                cornerRadius = (cellSize * 0.2f).coerceAtLeast(3.dp),
                showMotif = showMotifs && cellSize >= 16.dp,
                isGhost = isGhost,
                modifier = Modifier
                    .size(cellSize)
                    .offset(x = cellSize * pt.x, y = cellSize * pt.y)
            )
        }
    }
}
