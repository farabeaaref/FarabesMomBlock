package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.WarmCream
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ComboBanner(
    title: String?,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    val floatOffset = remember { Animatable(0f) }

    LaunchedEffect(title) {
        if (title != null) {
            floatOffset.snapTo(40f)
            floatOffset.animateTo(
                targetValue = -30f,
                animationSpec = tween(durationMillis = 1100)
            )
        }
    }

    AnimatedVisibility(
        visible = !title.isNullOrEmpty(),
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(0, floatOffset.value.roundToInt()) }
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(RoseAccent, Color(0xFFC0392B))
                    )
                )
                .border(2.dp, GoldAccent, RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title ?: "",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = WarmCream
                    )
                }
            }
        }
    }
}
