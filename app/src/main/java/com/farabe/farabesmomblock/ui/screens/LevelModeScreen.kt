package com.farabe.farabesmomblock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.Mint
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream

data class LevelStage(
    val levelNumber: Int,
    val title: String,
    val description: String,
    val targetScore: Int,
    val targetLines: Int,
    val rewardEmoji: String
)

val HANDCRAFTED_LEVELS = listOf(
    LevelStage(1, "Morning Blossom", "Clear 3 lines to bloom the first spring flower.", 300, 3, "🌸"),
    LevelStage(2, "Butterfly Meadow", "Clear 6 lines with graceful placements.", 600, 6, "🦋"),
    LevelStage(3, "Sunny Orchard", "Reach 1,000 points and harvest ripe lemons.", 1000, 10, "🍋"),
    LevelStage(4, "Lavender Breeze", "Clear 15 lines with at least 1 Combo streak.", 1500, 15, "🪻"),
    LevelStage(5, "Mom's Rose Garden", "Score 2,200 points in Mom's dedicated rose garden.", 2200, 20, "🌹"),
    LevelStage(6, "Starry Night Haven", "Reach 3,500 points under the twinkling stars.", 3500, 30, "🌌")
)

@Composable
fun LevelModeScreen(
    onSelectLevel: (LevelStage) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftCardBg)
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WarmCream,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "LEVEL CHALLENGES",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = WarmCream
                )
                Text(
                    text = "Handcrafted milestones for Mom",
                    fontSize = 12.sp,
                    color = Mint
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(HANDCRAFTED_LEVELS) { level ->
                LevelCard(level = level, onPlay = { onSelectLevel(level) })
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelStage,
    onPlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkerGrid)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SoftCardBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = level.rewardEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Level ${level.levelNumber}: ${level.title}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCream
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = level.description,
                    fontSize = 12.sp,
                    color = WarmCream.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Goal: ${level.targetScore} pts / ${level.targetLines} lines",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Play", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}
