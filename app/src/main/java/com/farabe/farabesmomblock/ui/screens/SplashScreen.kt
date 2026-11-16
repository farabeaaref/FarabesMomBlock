package com.farabe.farabesmomblock.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.engine.BlockColor
import com.farabe.farabesmomblock.ui.components.BlockTile
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.WarmCream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gorgeous 3D Animated Entry Splash Cinema!
 * Features spinning sacred Alpona fractal rays, spring-Physics assembling 3D beveled jewel blocks,
 * floating golden sparkles, and a glowing loading bar welcoming Mom.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0.25f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(36f) }
    val loadingProgress = remember { Animatable(0.05f) }

    val inf = rememberInfiniteTransition(label = "splashRays")
    val rayAngle by inf.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "rayAngle"
    )
    val pulse by inf.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            delay(180)
            titleAlpha.animateTo(1f, tween(500))
        }
        launch {
            delay(180)
            titleOffsetY.animateTo(0f, tween(550, easing = FastOutSlowInEasing))
        }
        launch {
            loadingProgress.animateTo(1f, tween(1650, easing = FastOutSlowInEasing))
        }
        delay(1750)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4B2182),
                        Color(0xFF2B144A),
                        Color(0xFF130624)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Rotating Fractal Mandala & Sunburst God-Rays Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.44f
            val maxR = size.minDimension * 0.62f

            // Sunburst rays
            for (r in 0 until 16) {
                val a0 = rayAngle + r * (6.283185f / 16f)
                val a1 = a0 + 0.14f
                val path = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx + cos(a0) * maxR, cy + sin(a0) * maxR)
                    lineTo(cx + cos(a1) * maxR, cy + sin(a1) * maxR)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.20f),
                            RoseAccent.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = maxR
                    )
                )
            }

            // Fractal Spirograph Lotus Ring
            val lotusPath = Path()
            for (s in 0..120) {
                val t = (s / 120f) * 6.283185f
                val rad = size.minDimension * 0.34f * (0.82f + 0.18f * cos(10 * t - rayAngle * 2f))
                val px = cx + rad * cos(t)
                val py = cy + rad * sin(t)
                if (s == 0) lotusPath.moveTo(px, py) else lotusPath.lineTo(px, py)
            }
            lotusPath.close()
            drawPath(
                path = lotusPath,
                color = GoldAccent.copy(alpha = 0.35f),
                style = Stroke(width = 2.5f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            // 2. 3D Beveled Jewel Emblem Crest with Floating Crown & Heart
            Box(
                modifier = Modifier
                    .scale(logoScale.value * pulse)
                    .size(142.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5E2B97), Color(0xFF2B114D))
                        )
                    )
                    .border(3.dp, GoldAccent, RoundedCornerShape(34.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BlockTile(color = BlockColor.SHAPLA_PINK, cornerRadius = 12.dp, modifier = Modifier.size(50.dp))
                        BlockTile(color = BlockColor.JAMDANI_TEAL, cornerRadius = 12.dp, modifier = Modifier.size(50.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BlockTile(color = BlockColor.BELI_CREAM, cornerRadius = 12.dp, modifier = Modifier.size(50.dp))
                        BlockTile(color = BlockColor.KANTHA_GOLD, cornerRadius = 12.dp, modifier = Modifier.size(50.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF31104F).copy(alpha = 0.92f))
                        .border(2.dp, GoldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "❤️", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffsetY.value.dp)
            ) {
                Text(
                    text = "FARABE'S MOM",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp,
                    color = WarmCream
                )

                Text(
                    text = "3D ROYAL BLOCK PUZZLE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    color = GoldAccent,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "মায়ের জন্য ভালোবাসার খেলা • Made with love by Farabe ❤️",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB2C9)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Animated Golden Loading Bar
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A0930))
                        .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(loadingProgress.value)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF5FD63A), GoldAccent, RoseAccent)
                                )
                            )
                    )
                }
            }
        }
    }
}
