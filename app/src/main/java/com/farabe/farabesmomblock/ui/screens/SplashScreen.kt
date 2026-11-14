package com.farabe.farabesmomblock.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.engine.BlockColor
import com.farabe.farabesmomblock.ui.components.BlockTile
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.WarmCream
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.4f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
        alphaAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(500)
        )
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(Color(0xFF2B144A), Color(0xFF1A0930))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scaleAnim.value)
        ) {
            // Playful animated 2x2 logo tiles with heart center
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF3B1D63))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Row {
                        BlockTile(color = BlockColor.SOFT_PINK, modifier = Modifier.size(44.dp))
                        BlockTile(color = BlockColor.SKY_BLUE, modifier = Modifier.size(44.dp))
                    }
                    Row {
                        BlockTile(color = BlockColor.MINT, modifier = Modifier.size(44.dp))
                        BlockTile(color = BlockColor.SUNSHINE_YELLOW, modifier = Modifier.size(44.dp))
                    }
                }

                // Glowing Center Heart
                Text(
                    text = "❤️",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FARABE'S MOM",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = WarmCream
            )

            Text(
                text = "BLOCK",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = GoldAccent
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Made with love by Farabe",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = RoseAccent
            )
        }
    }
}
