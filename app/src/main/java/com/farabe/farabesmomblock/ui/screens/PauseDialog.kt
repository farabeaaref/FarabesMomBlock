package com.farabe.farabesmomblock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.farabe.farabesmomblock.ui.components.appleGlassBar
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.WarmCream

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    sfxVolume: Float,
    musicVolume: Float,
    onSfxVolumeChanged: (Float) -> Unit,
    onMusicVolumeChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var showRestartConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(26.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF33155E), Color(0xFF1A0936), Color(0xFF0E041E))
                    )
                )
                .border(
                    width = 2.4.dp,
                    brush = Brush.linearGradient(
                        listOf(Color.White, GoldAccent, Color(0xFFAB47BC), GoldAccent)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF8F00), Color(0xFFFFD54F), Color(0xFFFF8F00))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(16.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏸️ ROYAL STAGE PAUSED",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2B1000),
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = "একটু বিশ্রাম নাও আম্মু! Take a breath, Mom ❤️",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCream.copy(alpha = 0.88f),
                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                )

                if (showRestartConfirm) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .appleGlassBar(
                                shape = RoundedCornerShape(18.dp),
                                tintColor = Color(0xFF28104E),
                                strokeWidth = 1.5.dp,
                                accentRim = RoseAccent
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Restart this stage from zero?",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Luxury3DCompactButton(
                                title = "Cancel",
                                icon = Icons.Default.PlayArrow,
                                topColor = Color(0xFF5E35B1),
                                bottomColor = Color(0xFF311B92),
                                rimColor = Color(0xFFB39DDB),
                                onClick = { showRestartConfirm = false },
                                modifier = Modifier.weight(1f)
                            )
                            Luxury3DCompactButton(
                                title = "Restart",
                                icon = Icons.Default.Refresh,
                                topColor = Color(0xFFE53935),
                                bottomColor = Color(0xFFB71C1C),
                                rimColor = GoldAccent,
                                onClick = {
                                    showRestartConfirm = false
                                    onRestart()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .appleGlassBar(
                                shape = RoundedCornerShape(18.dp),
                                tintColor = Color(0xFF230E45),
                                strokeWidth = 1.4.dp,
                                accentRim = GoldAccent
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "🔊 Sound Effects",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                        Slider(
                            value = sfxVolume,
                            onValueChange = onSfxVolumeChanged,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldAccent,
                                activeTrackColor = Color(0xFF00E676)
                            )
                        )

                        Text(
                            text = "🎵 Mom's Bangla Song & Music",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                        Slider(
                            value = musicVolume,
                            onValueChange = onMusicVolumeChanged,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldAccent,
                                activeTrackColor = RoseAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Luxury3DGameButton(
                        title = "RESUME PLAYING",
                        subtitle = "খেলা চালিয়ে যাও ▶",
                        icon = Icons.Default.PlayArrow,
                        topColor = Color(0xFF66BB6A),
                        bottomColor = Color(0xFF2E7D32),
                        lipColor = Color(0xFF1B5E20),
                        rimColor = Color(0xFFB9F6CA),
                        onClick = onResume
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Luxury3DCompactButton(
                            title = "Restart 🔄",
                            icon = Icons.Default.Refresh,
                            topColor = Color(0xFFEC407A),
                            bottomColor = Color(0xFFAD1457),
                            rimColor = Color(0xFFFF80AB),
                            onClick = { showRestartConfirm = true },
                            modifier = Modifier.weight(1f)
                        )
                        Luxury3DCompactButton(
                            title = "Home 🏠",
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
}
