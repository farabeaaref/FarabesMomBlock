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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.engine.BlockColor
import com.farabe.farabesmomblock.ui.components.AmbientPetalsCanvas
import com.farabe.farabesmomblock.ui.components.BlockTile
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.Lavender
import com.farabe.farabesmomblock.ui.theme.Mint
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SkyBlue
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream

@Composable
fun HomeScreen(
    momName: String,
    highScore: Int,
    dailyMessage: String?,
    onPlayClassic: () -> Unit,
    onPlayRelax: () -> Unit,
    onPlayLevel: () -> Unit,
    onOpenGarden: () -> Unit,
    onOpenLetters: () -> Unit,
    onOpenFamilyMemory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // Living gentle background with floating rose petals
        AmbientPetalsCanvas()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FARABE'S MOM",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = WarmCream
                    )
                    Text(
                        text = "BLOCK PUZZLE • মায়ের জন্য ❤️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GoldAccent
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SoftCardBg)
                        .border(1.dp, CardBorder, CircleShape)
                        .clickable { onOpenSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = WarmCream,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Personal Welcome Card for Mom with Bengali tribute
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF351744), DarkerGrid)
                        )
                    )
                    .border(1.5.dp, RoseAccent.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Welcome, $momName! ❤️",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmCream
                        )
                    }

                    Text(
                        text = "মা তোমাকে অনেক ভালোবাসি",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldAccent,
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )

                    Text(
                        text = "I made this little game especially for you.\nEvery block comes with nostalgic Bangladeshi memories and endless love from Farabe.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = WarmCream.copy(alpha = 0.9f)
                    )

                    if (highScore > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "👑 Personal Best: ",
                                fontSize = 13.sp,
                                color = GoldAccent
                            )
                            Text(
                                text = "$highScore",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }
                }
            }

            // Daily surprise card if present
            if (!dailyMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftCardBg)
                        .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = dailyMessage,
                            fontSize = 12.sp,
                            color = WarmCream,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bangladeshi Block Art Showcase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkerGrid.copy(alpha = 0.8f))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NOSTALGIC BENGALI TILES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "শাপলা • কাঁথা • জামদানি • রিকশা",
                            fontSize = 11.sp,
                            color = WarmCream.copy(alpha = 0.7f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BlockTile(color = BlockColor.SHAPLA_PINK, modifier = Modifier.size(28.dp))
                        BlockTile(color = BlockColor.KANTHA_GOLD, modifier = Modifier.size(28.dp))
                        BlockTile(color = BlockColor.JAMDANI_TEAL, modifier = Modifier.size(28.dp))
                        BlockTile(color = BlockColor.RICKSHAW_ROSE, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Big Play Button
            Button(
                onClick = onPlayClassic,
                colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "PLAY CLASSIC",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Alternative Game Modes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeCard(
                    title = "Relax Mode",
                    subtitle = "Stress-Free",
                    icon = Icons.Default.Spa,
                    tint = SkyBlue,
                    onClick = onPlayRelax,
                    modifier = Modifier.weight(1f)
                )

                ModeCard(
                    title = "Level Mode",
                    subtitle = "Puzzles",
                    icon = Icons.Default.WorkspacePremium,
                    tint = Mint,
                    onClick = onPlayLevel,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mom's Special Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MOM'S SPECIAL EDITION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Lavender
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mom's Garden Card
            SpecialFeatureCard(
                title = "Mom's Garden 🌸",
                description = "Grow beautiful flowers and butterflies with puzzle achievements.",
                icon = Icons.Default.Yard,
                accentColor = Mint,
                onClick = onOpenGarden
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Secret Letters from Farabe Card
            SpecialFeatureCard(
                title = "Secret Letters ❤️",
                description = "Heartwarming messages unlocked as you achieve new milestones.",
                icon = Icons.Default.Mail,
                accentColor = RoseAccent,
                onClick = onOpenLetters
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Family Memory Card
            SpecialFeatureCard(
                title = "Family Memory 📸",
                description = "A cherished family note and memory dedicated just to Mom.",
                icon = Icons.Default.PhotoAlbum,
                accentColor = GoldAccent,
                onClick = onOpenFamilyMemory
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Made with ❤️ by Farabe for Mom",
                fontSize = 12.sp,
                color = WarmCream.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkerGrid)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCream
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = WarmCream.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SpecialFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkerGrid)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCream
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = WarmCream.copy(alpha = 0.75f)
                )
            }
        }
    }
}
