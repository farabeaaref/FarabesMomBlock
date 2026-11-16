package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.RoseAccent

/**
 * Inside-Game Top Apple Liquid Glass Capsule Header with Linear Specular Stroke Lines!
 * Features frosted glass mascot medallion, live score, high score crown, coins/seeds pill,
 * and glass pause/settings button.
 */
@Composable
fun ScoreHeader(
    score: Int,
    highScore: Int,
    comboStreak: Int,
    seedsCount: Int = 89,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Apple Frosted Glass Capsule Bar with Diagonal Linear Stroke Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(12.dp, RoundedCornerShape(26.dp))
                .appleGlassBar(
                    shape = RoundedCornerShape(26.dp),
                    tintColor = Color(0xFF2E1552),
                    strokeWidth = 1.6.dp,
                    accentRim = Color(0xFF80D8FF)
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Frosted Glass Companion Mascot Medallion
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color(0xFF3F1D6B).copy(alpha = 0.72f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.9f), GoldAccent.copy(alpha = 0.6f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CuteMascot(modifier = Modifier.size(44.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 2. Score & High Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$score",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👑 BEST: $highScore",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 3. Apple Glass Gold Coin / Seed Pill
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .appleGlassBar(
                            shape = RoundedCornerShape(19.dp),
                            tintColor = Color(0xFF230D3E),
                            strokeWidth = 1.3.dp,
                            accentRim = GoldAccent
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF6B3E00),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$seedsCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 4. Apple Glass Settings / Pause Button with Linear Stroke
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .appleGlassBar(
                            shape = RoundedCornerShape(14.dp),
                            tintColor = Color(0xFF4A217A),
                            strokeWidth = 1.4.dp,
                            accentRim = Color.White
                        )
                        .clickable { onPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pause & Settings",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }

        // Active Combo Streak Pill
        AnimatedVisibility(
            visible = comboStreak >= 2,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .appleGlassBar(
                        shape = RoundedCornerShape(18.dp),
                        tintColor = RoseAccent,
                        strokeWidth = 1.5.dp,
                        accentRim = GoldAccent
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🔥 COMBO x$comboStreak STREAK! দারুণ!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
