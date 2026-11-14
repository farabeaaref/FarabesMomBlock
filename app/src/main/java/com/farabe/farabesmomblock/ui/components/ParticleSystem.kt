package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.WarmCream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class VisualParticle(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val radius: Float,
    var alpha: Float = 1.0f,
    val isPetal: Boolean = false
)

data class FloatingScore(
    val id: Long,
    val text: String,
    val startX: Float,
    val startY: Float,
    val color: Color
)

/**
 * High-performance dynamic particle canvas for ambient falling rose petals,
 * line clear celebration sparks, and floating score numbers.
 */
@Composable
fun ParticleOverlay(
    activeScores: List<FloatingScore>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        for (score in activeScores) {
            FloatingScoreItem(score)
        }
    }
}

@Composable
private fun FloatingScoreItem(score: FloatingScore) {
    val offsetY = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(score.id) {
        launch {
            offsetY.animateTo(-120f, tween(1000, easing = LinearEasing))
        }
        launch {
            delay(400)
            alphaAnim.animateTo(0f, tween(600))
        }
    }

    Text(
        text = score.text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        color = score.color.copy(alpha = alphaAnim.value),
        modifier = Modifier.offset {
            IntOffset(
                score.startX.roundToInt(),
                (score.startY + offsetY.value).roundToInt()
            )
        }
    )
}

/**
 * Ambient floating golden dust & peaceful petals in the background.
 */
@Composable
fun AmbientPetalsCanvas(
    modifier: Modifier = Modifier
) {
    val random = remember { Random(42) }
    val particles = remember {
        List(18) {
            VisualParticle(
                id = it.toLong(),
                x = random.nextFloat() * 1000f,
                y = random.nextFloat() * 1800f,
                vx = (random.nextFloat() - 0.5f) * 0.8f,
                vy = random.nextFloat() * 1.2f + 0.4f,
                color = if (it % 2 == 0) RoseAccent.copy(alpha = 0.35f) else GoldAccent.copy(alpha = 0.25f),
                radius = random.nextFloat() * 4.5f + 2.5f,
                isPetal = it % 3 == 0
            )
        }
    }

    var tick by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(32) // ~30 FPS ambient animation
            tick++
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (p in particles) {
            p.x += p.vx
            p.y += p.vy
            if (p.y > h) {
                p.y = -20f
                p.x = random.nextFloat() * w
            }
            if (p.x < 0) p.x = w
            if (p.x > w) p.x = 0f

            if (p.isPetal) {
                // Draw soft oval petal
                drawOval(
                    color = p.color,
                    topLeft = Offset(p.x, p.y),
                    size = androidx.compose.ui.geometry.Size(p.radius * 2.2f, p.radius * 1.4f)
                )
            } else {
                // Soft glowing dust dot
                drawCircle(
                    color = p.color,
                    radius = p.radius,
                    center = Offset(p.x, p.y),
                    style = Fill
                )
            }
        }
    }
}
