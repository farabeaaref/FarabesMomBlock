package com.farabe.farabesmomblock.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.farabe.farabesmomblock.ui.components.appleGlassBar
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.WarmCream
import kotlin.math.cos
import kotlin.math.sin

/**
 * Luxury 3D Unreal-Style Royal Stage Victory & Strict Objective Modal!
 * Strictly enforces level rules: "Next Stage" is ONLY unlocked and offered when BOTH
 * Target Score and Target Lines are 100% completed.
 */
@Composable
fun GameOverDialog(
    score: Int,
    highScore: Int,
    linesCleared: Int = 0,
    targetScore: Int? = null,
    targetLines: Int? = null,
    rewardCoins: Int = 150,
    levelTitle: String? = null,
    hasVoiceMessage: Boolean = false,
    isPlayingVoice: Boolean = false,
    isLevelWin: Boolean = false,
    currentLevelNumber: Int? = null,
    onNextLevel: (() -> Unit)? = null,
    onReplayVoice: () -> Unit = {},
    onReplay: () -> Unit,
    onOpenGarden: () -> Unit,
    onHome: () -> Unit
) {
    val isChallengeMode = currentLevelNumber != null
    val isOverallWin = if (isChallengeMode) {
        isLevelWin
    } else {
        score >= highScore && score > 0
    }

    val scoreGoal = targetScore ?: 500
    val lineGoal = targetLines ?: 5
    val isScoreMet = score >= scoreGoal
    val isLinesMet = linesCleared >= lineGoal

    // Calculate 3D Star Rating (0 stars if level failed, 1..3 stars on victory)
    val earnedStars = when {
        isChallengeMode && !isLevelWin -> 0
        score >= (scoreGoal * 1.35f).toInt() && linesCleared >= lineGoal -> 3
        score >= (scoreGoal * 1.10f).toInt() -> 3
        isOverallWin -> 2
        score >= (scoreGoal * 0.6f).toInt() -> 1
        else -> 0
    }

    val infiniteTransition = rememberInfiniteTransition(label = "royalModalGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val rimColor = if (isOverallWin) Color(0xFFFFD54F) else Color(0xFFFF5252)
    val nextLevelNum = ((currentLevelNumber ?: 1) + 1).coerceAtMost(TOTAL_CAMPAIGN_LEVELS)

    Dialog(onDismissRequest = { /* Strict modal: player must choose action */ }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(28.dp, RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        colors = if (isOverallWin) {
                            listOf(Color(0xFF33145E), Color(0xFF1A0936), Color(0xFF0E041F))
                        } else {
                            listOf(Color(0xFF3A1228), Color(0xFF1D0918), Color(0xFF10040E))
                        }
                    )
                )
                .border(
                    width = 2.6.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.92f),
                            rimColor,
                            if (isOverallWin) Color(0xFFFF8F00) else Color(0xFFC62828),
                            Color.White.copy(alpha = 0.75f)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .drawBehind {
                    // Top 3D studio radial spotlight bloom
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                rimColor.copy(alpha = 0.28f * glowPulse),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, 70f),
                            radius = size.width * 0.72f
                        )
                    )
                    // Inner 3D chamfer border line
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.14f),
                        topLeft = Offset(8f, 8f),
                        size = Size(size.width - 16f, size.height - 16f),
                        cornerRadius = CornerRadius(72f, 72f),
                        style = Stroke(width = 1.5f)
                    )
                }
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. 3D Royal Emblem & 3-Star Crest
                Royal3DStarCrest(
                    earnedStars = earnedStars,
                    isWin = isOverallWin,
                    glowPulse = glowPulse
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. 3D Embossed Metallic Stage Ribbon Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isOverallWin) {
                                    listOf(Color(0xFFFF8F00), Color(0xFFFFD54F), Color(0xFFFF8F00))
                                } else {
                                    listOf(Color(0xFF8E0E3A), Color(0xFFE53935), Color(0xFF8E0E3A))
                                }
                            )
                        )
                        .border(1.8.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isOverallWin) {
                            com.farabe.farabesmomblock.ui.components.Unreal3DCrownIcon(size = 18.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isChallengeMode && isLevelWin -> "STAGE $currentLevelNumber VICTORY!"
                                isChallengeMode && !isLevelWin -> "STAGE $currentLevelNumber INCOMPLETE"
                                isOverallWin -> "NEW ROYAL RECORD!"
                                else -> "ROUND COMPLETE"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = if (isOverallWin) Color(0xFF2B1000) else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        isChallengeMode && isLevelWin ->
                            "অভিনন্দন আম্মু! Both targets cleared — Stage $nextLevelNum/$TOTAL_CAMPAIGN_LEVELS is now UNLOCKED!"
                        isChallengeMode && !isLevelWin ->
                            "আম্মু, পরের লেভেলে যেতে হলে এই লেভেলের Score ও Lines দুটি লক্ষ্যই পূরণ করতে হবে!"
                        else ->
                            "দারুণ খেলেছো আম্মু! Every move brings joy to our family."
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = WarmCream.copy(alpha = 0.92f),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. 3D Score & Personal Best Vault Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .appleGlassBar(
                            shape = RoundedCornerShape(20.dp),
                            tintColor = Color(0xFF220D44),
                            strokeWidth = 1.6.dp,
                            accentRim = rimColor
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isChallengeMode && levelTitle != null) {
                                "STAGE $currentLevelNumber • ${levelTitle.uppercase()}"
                            } else {
                                "FINAL SCORE"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            text = "$score",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All-Time Best: $highScore",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                            if (isLevelWin) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "• +$rewardCoins ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF69F0AE)
                                )
                                com.farabe.farabesmomblock.ui.components.Unreal3DCoinIcon(size = 14.dp)
                            }
                        }

                        // Strict Dual-Objective Checklist in Challenge Mode
                        if (isChallengeMode && targetScore != null && targetLines != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            ObjectiveProgressRow(
                                label = "Target Score",
                                current = score,
                                target = targetScore,
                                isMet = isScoreMet
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ObjectiveProgressRow(
                                label = "Lines Cleared",
                                current = linesCleared,
                                target = targetLines,
                                isMet = isLinesMet
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Optional Voice Note Button from Farabe
                if (hasVoiceMessage) {
                    Luxury3DGameButton(
                        title = if (isPlayingVoice) "PLAYING FARABE'S VOICE..." else "HEAR FARABE'S VOICE NOTE",
                        subtitle = "ফারাবীর ভয়েস মেসেজ শুনো",
                        icon = if (isPlayingVoice) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
                        topColor = Color(0xFFAB47BC),
                        bottomColor = Color(0xFF6A1B9A),
                        lipColor = Color(0xFF38006B),
                        rimColor = GoldAccent,
                        onClick = onReplayVoice
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 5. STRICT PROGRESSION ACTION BUTTONS
                if (isChallengeMode) {
                    if (isLevelWin && onNextLevel != null) {
                        // WON BOTH OBJECTIVES -> Unlock & Offer Next Level!
                        Luxury3DGameButton(
                            title = "NEXT STAGE: LEVEL $nextLevelNum",
                            subtitle = "পরের লেভেল খেলো • Unlocked!",
                            icon = Icons.Default.PlayArrow,
                            topColor = Color(0xFF66BB6A),
                            bottomColor = Color(0xFF2E7D32),
                            lipColor = Color(0xFF1B5E20),
                            rimColor = Color(0xFFB9F6CA),
                            onClick = onNextLevel
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        // FAILED CURRENT LEVEL -> Strictly locked next level; prominent Retry current level!
                        Luxury3DGameButton(
                            title = "RETRY LEVEL $currentLevelNumber",
                            subtitle = "আবার চেষ্টা করো • Beat Lv.$currentLevelNumber to unlock Lv.$nextLevelNum",
                            icon = Icons.Default.Refresh,
                            topColor = Color(0xFFFF7043),
                            bottomColor = Color(0xFFD84315),
                            lipColor = Color(0xFF870000),
                            rimColor = GoldAccent,
                            onClick = onReplay
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (!isChallengeMode || isLevelWin) {
                    Luxury3DGameButton(
                        title = if (isChallengeMode) "REPLAY LEVEL $currentLevelNumber" else "PLAY AGAIN",
                        subtitle = "আবার খেলো",
                        icon = Icons.Default.Refresh,
                        topColor = Color(0xFFEC407A),
                        bottomColor = Color(0xFFAD1457),
                        lipColor = Color(0xFF6A0032),
                        rimColor = Color(0xFFFF80AB),
                        onClick = onReplay
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Secondary Row: Garden & Home
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Luxury3DCompactButton(
                        title = "GARDEN",
                        icon = Icons.Default.Yard,
                        topColor = Color(0xFF26A69A),
                        bottomColor = Color(0xFF00695C),
                        rimColor = Color(0xFF80CBC4),
                        onClick = onOpenGarden,
                        modifier = Modifier.weight(1f)
                    )
                    Luxury3DCompactButton(
                        title = "HOME",
                        icon = Icons.Default.Home,
                        topColor = Color(0xFF5E35B1),
                        bottomColor = Color(0xFF311B92),
                        rimColor = Color(0xFFB39DDB),
                        onClick = onHome,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ObjectiveProgressRow(
    label: String,
    current: Int,
    target: Int,
    isMet: Boolean
) {
    val ratio = (current.toFloat() / target.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val accent = if (isMet) Color(0xFF69F0AE) else Color(0xFFFF8A80)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmCream
                )
            }
            Text(
                text = if (isMet) "$current / $target [CLEARED]" else "$current / $target (Need ${target - current} more)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = accent
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF120624))
                .border(0.6.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            if (isMet) listOf(Color(0xFF00E676), Color(0xFFB9F6CA))
                            else listOf(Color(0xFFFF5252), Color(0xFFFFAB40))
                        )
                    )
            )
        }
    }
}

@Composable
private fun Royal3DStarCrest(
    earnedStars: Int,
    isWin: Boolean,
    glowPulse: Float
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (starIdx in 1..3) {
            val isEarned = starIdx <= earnedStars
            val starSize = if (starIdx == 2) 48.dp else 38.dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(starSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.5f
                    val outerR = size.minDimension * 0.46f
                    val innerR = outerR * 0.44f

                    if (isEarned) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F).copy(alpha = 0.50f * glowPulse),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = outerR * 1.3f
                            )
                        )
                    }

                    val starPath = Path().apply {
                        for (i in 0 until 10) {
                            val angle = Math.toRadians((i * 36.0) - 90.0)
                            val r = if (i % 2 == 0) outerR else innerR
                            val px = cx + (r * cos(angle)).toFloat()
                            val py = cy + (r * sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }

                    drawPath(
                        path = starPath,
                        brush = Brush.verticalGradient(
                            colors = if (isEarned) {
                                listOf(Color(0xFFFFF59D), Color(0xFFFFCA28), Color(0xFFFF8F00))
                            } else {
                                listOf(Color(0xFF4A3B69), Color(0xFF281C40))
                            }
                        )
                    )
                    drawPath(
                        path = starPath,
                        color = if (isEarned) Color.White else Color.White.copy(alpha = 0.25f),
                        style = Stroke(width = if (isEarned) 2.2f else 1.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun Luxury3DGameButton(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    topColor: Color,
    bottomColor: Color,
    lipColor: Color,
    rimColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 3D Extruded Lip Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(lipColor)
            .clickable { onClick() }
            .padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(topColor, bottomColor)
                    )
                )
                .border(
                    width = 1.6.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.90f), rimColor.copy(alpha = 0.70f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .drawBehind {
                    // Top 3D Glass Specular Dome Highlight
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.36f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        ),
                        topLeft = Offset(6f, 3f),
                        size = Size(size.width - 12f, size.height * 0.44f),
                        cornerRadius = CornerRadius(32f, 32f)
                    )
                }
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                        .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.90f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Luxury3DCompactButton(
    title: String,
    icon: ImageVector,
    topColor: Color,
    bottomColor: Color,
    rimColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .shadow(8.dp, RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onClick() }
            .padding(bottom = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
                .border(1.4.dp, rimColor, RoundedCornerShape(14.dp))
                .drawBehind {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.24f),
                        topLeft = Offset(5f, 3f),
                        size = Size(size.width - 10f, size.height * 0.42f),
                        cornerRadius = CornerRadius(24f, 24f)
                    )
                }
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}
