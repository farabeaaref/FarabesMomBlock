package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.engine.BlockShape
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.CellSlotColor
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.WarmCream

/**
 * 3-Piece Tray with unclipped drag-and-drop, tap-to-select, and entrance animations.
 */
@Composable
fun PieceTrayView(
    trayPieces: List<BlockShape?>,
    activeDraggingIndex: Int?,
    selectedPieceIndex: Int?,
    onPieceClick: (Int) -> Unit,
    onDragStart: (Int, Offset) -> Unit,
    onDrag: (Int, Offset) -> Unit,
    onDragEnd: (Int) -> Unit,
    onDragCancel: (Int) -> Unit,
    surfaceColor: Color = Color(0xFF2B144A),
    borderColor: Color = Color(0xFF7D48C4),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .background(surfaceColor.copy(alpha = 0.90f), RoundedCornerShape(20.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (index in 0 until 3) {
                val shape = trayPieces.getOrNull(index)
                TraySlot(
                    index = index,
                    shape = shape,
                    isBeingDragged = activeDraggingIndex == index,
                    isSelected = selectedPieceIndex == index,
                    onClick = { onPieceClick(index) },
                    onDragStart = onDragStart,
                    onDrag = onDrag,
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TraySlot(
    index: Int,
    shape: BlockShape?,
    isBeingDragged: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDragStart: (Int, Offset) -> Unit,
    onDrag: (Int, Offset) -> Unit,
    onDragEnd: (Int) -> Unit,
    onDragCancel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var slotCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var currentTouchInRoot by remember { mutableStateOf(Offset.Zero) }

    // Pulsing selection animation
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            pulseScale.animateTo(
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) GoldAccent.copy(alpha = 0.15f) else CellSlotColor.copy(alpha = 0.6f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GoldAccent else CardBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .onGloballyPositioned { slotCoords = it }
            .clickable(enabled = shape != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (shape != null) {
            val maxDim = maxOf(shape.width, shape.height)
            val slotCellSize = when {
                maxDim >= 5 -> 17.dp
                maxDim == 4 -> 20.dp
                else -> 24.dp
            }

            Box(
                modifier = Modifier
                    .scale(pulseScale.value)
                    .alpha(if (isBeingDragged) 0.20f else 1.0f)
                    .pointerInput(shape.id) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                val rootOrigin = slotCoords?.positionInRoot() ?: Offset.Zero
                                currentTouchInRoot = rootOrigin + startOffset
                                onDragStart(index, currentTouchInRoot)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentTouchInRoot += dragAmount
                                onDrag(index, currentTouchInRoot)
                            },
                            onDragEnd = {
                                onDragEnd(index)
                            },
                            onDragCancel = {
                                onDragCancel(index)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                PieceShapePreview(
                    shape = shape,
                    cellSize = slotCellSize,
                    showMotifs = true
                )
            }
        } else {
            // Empty placeholder slot
            Text(
                text = "✓",
                fontSize = 18.sp,
                color = WarmCream.copy(alpha = 0.2f)
            )
        }
    }
}
