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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.SoftPink
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
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkerGrid,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, CardBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Paused",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCream
                )
                Text(
                    text = "Take a breath, Mom! ❤️",
                    fontSize = 14.sp,
                    color = SoftPink,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                if (showRestartConfirm) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftCardBg)
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Restart this game?",
                                fontWeight = FontWeight.Bold,
                                color = WarmCream,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showRestartConfirm = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkerGrid),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel", color = WarmCream)
                                }
                                Button(
                                    onClick = {
                                        showRestartConfirm = false
                                        onRestart()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Restart", color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    // Audio Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftCardBg)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Sound Effects",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmCream
                        )
                        Slider(
                            value = sfxVolume,
                            onValueChange = onSfxVolumeChanged,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldAccent,
                                activeTrackColor = RoseAccent
                            )
                        )

                        Text(
                            text = "Calming Music",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmCream,
                            modifier = Modifier.padding(top = 8.dp)
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons
                    Button(
                        onClick = onResume,
                        colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Resume Playing", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showRestartConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = WarmCream)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Restart", color = WarmCream)
                        }

                        Button(
                            onClick = onHome,
                            colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = WarmCream)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Home", color = WarmCream)
                        }
                    }
                }
            }
        }
    }
}
