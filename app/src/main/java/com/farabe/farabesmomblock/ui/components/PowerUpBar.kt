package com.farabe.farabesmomblock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.Lavender
import com.farabe.farabesmomblock.ui.theme.Mint

/**
 * Apple Liquid Glass Power-Up Bar (Undo, Hint, Shuffle) with Linear Specular Stroke Lines!
 */
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PowerUpButton(
            icon = Icons.Default.Undo,
            label = "Undo",
            enabled = canUndo,
            tint = Lavender,
            onClick = onUndoClick,
            modifier = Modifier.weight(1f)
        )

        PowerUpButton(
            icon = Icons.Default.Lightbulb,
            label = "Hint",
            enabled = true,
            tint = GoldAccent,
            onClick = onHintClick,
            modifier = Modifier.weight(1f)
        )

        PowerUpButton(
            icon = Icons.Default.Autorenew,
            label = "Shuffle",
            enabled = true,
            tint = Mint,
            onClick = onShuffleClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PowerUpButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) 1.0f else 0.45f

    // 3D Extruded Luxury Power-Up Capsule
    Box(
        modifier = modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF17072B))
            .clickable(enabled = enabled) { onClick() }
            .padding(bottom = 2.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .appleGlassBar(
                    shape = RoundedCornerShape(17.dp),
                    tintColor = Color(0xFF33185A),
                    strokeWidth = 1.5.dp,
                    accentRim = tint
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.22f))
                        .border(
                            1.dp,
                            tint.copy(alpha = 0.85f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = label.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.4.sp,
                    color = Color.White
                )
            }
        }
    }
}
