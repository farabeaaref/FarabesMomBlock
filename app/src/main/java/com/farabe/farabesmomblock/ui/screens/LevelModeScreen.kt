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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
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
import com.farabe.farabesmomblock.ui.components.Unreal3DCoinIcon
import com.farabe.farabesmomblock.ui.components.Unreal3DCrownIcon
import com.farabe.farabesmomblock.ui.components.Unreal3DHeartIcon
import com.farabe.farabesmomblock.ui.components.Unreal3DLightningIcon
import com.farabe.farabesmomblock.ui.components.Unreal3DStageCrestIcon
import com.farabe.farabesmomblock.ui.components.Unreal3DTargetIcon
import com.farabe.farabesmomblock.ui.components.appleGlassBar
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.Mint
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream

const val TOTAL_CAMPAIGN_LEVELS = 100

data class LevelStage(
    val levelNumber: Int,
    val chapterTitle: String,
    val title: String,
    val titleBengali: String,
    val description: String,
    val targetScore: Int,
    val targetLines: Int,
    val rewardCoins: Int,
    val rewardEmoji: String = ""
)

private val CHAPTER_THEMES = listOf(
    Triple("Chapter I: Shapla Dawn", "Morning Blossom", "শাপলা ভোরের ফুল"),
    Triple("Chapter II: Sylhet Tea Valley", "Emerald Tea Sanctuary", "সিলেটের চা বাগান"),
    Triple("Chapter III: Padma River Horizon", "Golden River Breeze", "পদ্মার সোনালী ঢেউ"),
    Triple("Chapter IV: Nakshi Kantha Artistry", "Royal Silk Tapestry", "নকশী কাঁথার বুনন"),
    Triple("Chapter V: Dhakai Jamdani Loom", "Imperial Gold Weave", "ঢাকাই জামদানি প্রাসাদ"),
    Triple("Chapter VI: Sundarbans Mystic Grove", "Royal Bengal Sanctuary", "সুন্দরবনের সবুজ ছায়া"),
    Triple("Chapter VII: Cox's Bazar Twilight", "Sapphire Ocean Crest", "সাগর পাড়ের গোধূলি"),
    Triple("Chapter VIII: Sonargaon Royal Court", "Heritage Golden Citadel", "সোনারগাঁও রাজপ্রাসাদ"),
    Triple("Chapter IX: Starlight Jasmine Palace", "Celestial Beli Garden", "বেলি ফুলের তারার রাত"),
    Triple("Chapter X: Queen Ammu's Sovereign Throne", "Eternal Diamond Crown", "সেরা আম্মুর রাজমুকুট")
)

private val STAGE_VARIANTS = listOf(
    "Dawn Awakening" to "প্রথম আলো",
    "Crystal Cascade" to "স্ফটিক ঝর্ণা",
    "Lotus Sanctuary" to "পদ্ম সরোবর",
    "Golden Orchard" to "সোনার বাগান",
    "Starlight Path" to "তারার পথ",
    "Emerald Harmony" to "সবুজ ছন্দ",
    "Sapphire Horizon" to "নীল দিগন্ত",
    "Ruby Pavilion" to "মানিক্য মহল",
    "Diamond Bastion" to "হীরক দুর্গ",
    "Grand Sovereign" to "রাজকীয় বিজয়"
)

val HANDCRAFTED_LEVELS: List<LevelStage> = (1..TOTAL_CAMPAIGN_LEVELS).map { lvl ->
    val chapterIdx = ((lvl - 1) / 10).coerceIn(0, CHAPTER_THEMES.lastIndex)
    val stageInChapter = (lvl - 1) % 10
    val (chapterName, baseTitle, baseBn) = CHAPTER_THEMES[chapterIdx]
    val (variantEn, variantBn) = STAGE_VARIANTS[stageInChapter]

    val targetScore = when {
        lvl == 1 -> 250
        lvl <= 10 -> 250 + (lvl - 1) * 220
        lvl <= 30 -> 2230 + (lvl - 10) * 280
        lvl <= 60 -> 7830 + (lvl - 30) * 350
        else -> 18330 + (lvl - 60) * 450
    }
    val targetLines = when {
        lvl == 1 -> 3
        lvl <= 10 -> 3 + (lvl - 1) * 2
        else -> 21 + ((lvl - 10) * 3) / 2
    }
    val rewardCoins = 100 + (lvl * 25)

    val fullTitle = if (lvl <= 4) {
        when (lvl) {
            1 -> "Morning Blossom"
            2 -> "Butterfly Meadow"
            3 -> "Shapla Waterlily Pond"
            else -> "Sunny Mango Orchard"
        }
    } else {
        "$baseTitle • $variantEn"
    }
    val fullBn = "$baseBn ($variantBn)"

    LevelStage(
        levelNumber = lvl,
        chapterTitle = chapterName,
        title = fullTitle,
        titleBengali = fullBn,
        description = "Score $targetScore pts AND clear $targetLines lines to conquer Stage $lvl.",
        targetScore = targetScore,
        targetLines = targetLines,
        rewardCoins = rewardCoins
    )
}

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
    val overallProgress = (completedCount.toFloat() / TOTAL_CAMPAIGN_LEVELS.toFloat()).coerceIn(0.02f, 1f)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (highestUnlockedLevel - 2).coerceIn(0, TOTAL_CAMPAIGN_LEVELS - 1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), NavyBackground)
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 3D Header
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
                        text = "100-STAGE ROYAL CAMPAIGN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = WarmCream
                    )
                    Text(
                        text = "Strict Dual-Objective Rules • Beat Both Targets to Advance",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Mint
                    )
                }
            }

            // 3D Hearts Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF380E3B))
                    .border(1.2.dp, RoseAccent, RoundedCornerShape(16.dp))
                    .clickable { onRefillHearts() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Unreal3DHeartIcon(size = 16.dp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (hearts <= 0) "REFILL +5" else "$hearts",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 100-Stage Campaign Progress Vault
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .appleGlassBar(
                    shape = RoundedCornerShape(18.dp),
                    tintColor = Color(0xFF1E295E),
                    strokeWidth = 1.6.dp,
                    accentRim = GoldAccent
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Unreal3DCrownIcon(size = 16.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Campaign Mastery: $completedCount / $TOTAL_CAMPAIGN_LEVELS Stages",
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "Active: Stage ${highestUnlockedLevel.coerceAtMost(TOTAL_CAMPAIGN_LEVELS)}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFF00E676),
                trackColor = Color(0xFF0B1026)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(HANDCRAFTED_LEVELS, key = { it.levelNumber }) { level ->
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
        isCompleted -> Color(0xFF00E676)
        isUnlocked -> Color(0xFF38BDF8)
        else -> Color(0xFF334155)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isUnlocked) 1f else 0.65f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    when {
                        isCurrentChallenge -> listOf(Color(0xFF2E1A68), Color(0xFF1E295E))
                        isCompleted -> listOf(Color(0xFF0F382C), Color(0xFF132A4A))
                        else -> listOf(Color(0xFF131C38), Color(0xFF0F172A))
                    }
                )
            )
            .border(if (isCurrentChallenge) 2.2.dp else 1.2.dp, borderCol, RoundedCornerShape(18.dp))
            .clickable(enabled = isUnlocked) { onPlay() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Hexagonal Royal Stage Medallion
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color(0xFF1E295E) else Color(0xFF0F172A))
                    .border(1.5.dp, borderCol, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Unreal3DStageCrestIcon(levelNumber = level.levelNumber, size = 22.dp)
                        Text(
                            text = "#${level.levelNumber}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.chapterTitle.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "Stage ${level.levelNumber}: ${level.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmCream
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${level.titleBengali} • Reward: +${level.rewardCoins}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Unreal3DCoinIcon(size = 13.dp)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isUnlocked) level.description else "LOCKED: Complete Stage ${level.levelNumber - 1} (Score & Lines) to unlock.",
                    fontSize = 11.sp,
                    color = WarmCream.copy(alpha = 0.82f)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Unreal3DTargetIcon(size = 13.dp, isCompleted = isCompleted)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${level.targetScore} PTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCompleted) GoldAccent else Mint
                        )
                    }
                    Text(
                        text = "AND",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Unreal3DLightningIcon(size = 13.dp, isCompleted = isCompleted)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${level.targetLines} LINES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCompleted) GoldAccent else Mint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isUnlocked) {
                Luxury3DCompactButton(
                    title = if (isCompleted) "REPLAY" else "PLAY",
                    icon = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                    topColor = when {
                        isCurrentChallenge -> Color(0xFF00E676)
                        isCompleted -> Color(0xFF2E7D32)
                        else -> Color(0xFFEC407A)
                    },
                    bottomColor = when {
                        isCurrentChallenge -> Color(0xFF00A152)
                        isCompleted -> Color(0xFF1B5E20)
                        else -> Color(0xFFAD1457)
                    },
                    rimColor = GoldAccent,
                    onClick = onPlay,
                    modifier = Modifier.width(88.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "LOCKED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}
