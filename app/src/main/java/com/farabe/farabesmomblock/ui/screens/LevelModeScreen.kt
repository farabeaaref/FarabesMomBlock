package com.farabe.farabesmomblock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    val titleBengali: String,
    val description: String,
    val targetScore: Int,
    val targetLines: Int,
    val rewardCoins: Int,
    val rewardEmoji: String
)

val HANDCRAFTED_LEVELS = listOf(
    LevelStage(1, "Morning Blossom", "ভোরের ফুল", "Score 250 pts AND clear 3 lines to bloom the first flower.", 250, 3, 100, "🌸"),
    LevelStage(2, "Butterfly Meadow", "প্রজাপতির মেলা", "Score 500 pts AND clear 5 lines with graceful moves.", 500, 5, 120, "🦋"),
    LevelStage(3, "Shapla Waterlily Pond", "শাপলা বিল", "Score 800 pts AND clear 7 lines in the waterlily pond.", 800, 7, 150, "🌺"),
    LevelStage(4, "Sunny Mango Orchard", "আমের বাগান", "Reach 1,100 pts AND clear 9 lines to harvest sweet mangoes.", 1100, 9, 180, "🥭"),
    LevelStage(5, "Nakshi Kantha Weave", "নকশী কাঁথা", "Score 1,400 pts AND clear 12 lines to stitch golden patterns.", 1400, 12, 220, "🧵"),
    LevelStage(6, "Mom's Rose Garden", "মায়ের গোলাপ বাগান", "Score 1,800 pts AND clear 15 lines in Mom's rose sanctuary.", 1800, 15, 260, "🌹"),
    LevelStage(7, "Sylhet Tea Valley", "সিলেটের চা বাগান", "Reach 2,200 pts AND clear 18 lines with a warm cup of tea.", 2200, 18, 300, "🍵"),
    LevelStage(8, "Jamdani Royal Loom", "ঢাকাই জামদানি", "Score 2,700 pts AND clear 22 lines of royal silk blocks.", 2700, 22, 350, "👑"),
    LevelStage(9, "Padma Sunset Boat", "পদ্মার গোধূলি", "Reach 3,200 pts AND clear 26 lines under the golden sky.", 3200, 26, 400, "⛵"),
    LevelStage(10, "Festive Alpona Courtyard", "উৎসবের আলপনা", "Score 3,800 pts AND clear 30 lines with combo streaks.", 3800, 30, 450, "🎨"),
    LevelStage(11, "Starlight Jasmine Night", "বেলি ফুলের রাত", "Reach 4,500 pts AND clear 35 lines under twinkling stars.", 4500, 35, 500, "🌌"),
    LevelStage(12, "Queen Ammu's Golden Palace", "সেরা আম্মুর প্রাসাদ", "Score 5,500 pts AND clear 42 lines to claim the Royal Crown!", 5500, 42, 750, "🏆")
)

@Composable
fun LevelModeScreen(
    highestUnlockedLevel: Int = 1,
    completedLevels: Set<Int> = emptySet(),
    hearts: Int = 5,
    onRefillHearts: () -> Unit = {},
    onSelectLevel: (LevelStage) -> Unit,
    onBackClick: () -> Unit
) {
    val completedCount = completedLevels.size
    val overallProgress = (completedCount.toFloat() / HANDCRAFTED_LEVELS.size.toFloat()).coerceIn(0.05f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2B144A), Color(0xFF160829), NavyBackground)
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SoftCardBg)
                        .border(1.5.dp, CardBorder, CircleShape)
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "CHALLENGE ROADMAP",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = WarmCream
                    )
                    Text(
                        text = "Win each stage to unlock Mom's next adventure! 🌟",
                        fontSize = 11.sp,
                        color = Mint
                    )
                }
            }

            // Hearts pill with instant free refill if low
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF4A154B))
                    .border(1.dp, RoseAccent, RoundedCornerShape(16.dp))
                    .clickable { onRefillHearts() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (hearts <= 0) "Refill +5" else "$hearts",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Overall Challenge Campaign Progress Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF381B63).copy(alpha = 0.9f))
                .border(1.5.dp, GoldAccent.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👑 Campaign Progress: $completedCount / ${HANDCRAFTED_LEVELS.size} Stages",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Text(
                    text = "Next: Lv.${highestUnlockedLevel.coerceAtMost(HANDCRAFTED_LEVELS.size)}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFF5FD63A),
                trackColor = Color(0xFF1A0933)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(HANDCRAFTED_LEVELS) { level ->
                val isCompleted = completedLevels.contains(level.levelNumber)
                val isUnlocked = level.levelNumber <= highestUnlockedLevel || isCompleted
                val isCurrentChallenge = level.levelNumber == highestUnlockedLevel && !isCompleted
                LevelCard(
                    level = level,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    isCurrentChallenge = isCurrentChallenge,
                    onPlay = {
                        if (isUnlocked) onSelectLevel(level)
                    }
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelStage,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    isCurrentChallenge: Boolean,
    onPlay: () -> Unit
) {
    val borderCol = when {
        isCurrentChallenge -> GoldAccent
        isCompleted -> Color(0xFF66BB6A)
        isUnlocked -> CardBorder
        else -> Color(0xFF3E2F5B)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isUnlocked) 1f else 0.68f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isCurrentChallenge) Color(0xFF3B1D63) else DarkerGrid
            )
            .border(if (isCurrentChallenge) 2.dp else 1.dp, borderCol, RoundedCornerShape(18.dp))
            .clickable(enabled = isUnlocked) { onPlay() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) SoftCardBg else Color(0xFF1B122C))
                    .border(1.5.dp, borderCol, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(text = level.rewardEmoji, fontSize = 26.sp)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lv.${level.levelNumber}: ${level.title}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmCream
                )
                Text(
                    text = "${level.titleBengali} • Reward: +${level.rewardCoins} 🪙",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isUnlocked) level.description else "🔒 Win Level ${level.levelNumber - 1} to unlock this challenge!",
                    fontSize = 11.sp,
                    color = WarmCream.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isCompleted) {
                            "⭐⭐⭐ CLEARED (${level.targetScore} pts & ${level.targetLines} lines)"
                        } else {
                            "Required: ${level.targetScore} pts AND ${level.targetLines} lines"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCompleted) GoldAccent else Mint
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isUnlocked) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isCurrentChallenge -> Color(0xFF5FD63A)
                            isCompleted -> Color(0xFF2E7D32)
                            else -> RoseAccent
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = if (isCompleted) "Replay" else "Play",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF26173E))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Locked 🔒",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
