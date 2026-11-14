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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.Mint
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream

@Composable
fun GameOverDialog(
    score: Int,
    highScore: Int,
    hasVoiceMessage: Boolean = false,
    isPlayingVoice: Boolean = false,
    isLevelWin: Boolean = false,
    onReplayVoice: () -> Unit = {},
    onReplay: () -> Unit,
    onOpenGarden: () -> Unit,
    onHome: () -> Unit
) {
    val isNewBest = (score >= highScore && score > 0) || isLevelWin

    Dialog(onDismissRequest = { /* forced action */ }) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = DarkerGrid,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, CardBorder, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isNewBest) "🎉 NEW HIGH SCORE!" else "Great Game, Mom! ❤️",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isNewBest) GoldAccent else RoseAccent
                )

                Text(
                    text = "You played with such elegance and thought.",
                    fontSize = 13.sp,
                    color = WarmCream.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // Score Display Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SoftCardBg)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "FINAL SCORE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmCream.copy(alpha = 0.65f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$score",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmCream
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "👑 Personal Best: ",
                                fontSize = 13.sp,
                                color = GoldAccent
                            )
                            Text(
                                text = "$highScore",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }
                }

                // Heartfelt note from Farabe
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkerGrid)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "“Every puzzle you solve brings a smile to my face. I'm so proud of you, Mom!”\n— Love, Farabe",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = WarmCream.copy(alpha = 0.9f),
                        lineHeight = 17.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (hasVoiceMessage) {
                    Button(
                        onClick = onReplayVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            if (isPlayingVoice) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = if (isPlayingVoice) "🔊 Playing Farabe's Voice..." else "🎙️ Hear Farabe's Voice Note",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                // Action Buttons
                Button(
                    onClick = onReplay,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Play Again", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onOpenGarden,
                    colors = ButtonDefaults.buttonColors(containerColor = Mint),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Yard, contentDescription = null, tint = DarkerGrid)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Visit Mom's Garden 🌸", fontWeight = FontWeight.Bold, color = DarkerGrid)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = WarmCream)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Return Home", color = WarmCream)
                }
            }
        }
    }
}
