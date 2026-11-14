package com.farabe.farabesmomblock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.Lavender
import com.farabe.farabesmomblock.ui.theme.Mint
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream

@Composable
fun PowerUpBar(
    canUndo: Boolean,
    onUndoClick: () -> Unit,
    onHintClick: () -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PowerUpButton(
            icon = Icons.Default.Undo,
            label = "Undo",
            enabled = canUndo,
            tint = Lavender,
            onClick = onUndoClick
        )

        PowerUpButton(
            icon = Icons.Default.Lightbulb,
            label = "Hint",
            enabled = true,
            tint = GoldAccent,
            onClick = onHintClick
        )

        PowerUpButton(
            icon = Icons.Default.Autorenew,
            label = "Shuffle",
            enabled = true,
            tint = Mint,
            onClick = onShuffleClick
        )
    }
}

@Composable
private fun PowerUpButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1.0f else 0.4f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SoftCardBg.copy(alpha = alpha))
            .border(1.dp, CardBorder.copy(alpha = alpha), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint.copy(alpha = alpha),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = WarmCream.copy(alpha = alpha)
            )
        }
    }
}
