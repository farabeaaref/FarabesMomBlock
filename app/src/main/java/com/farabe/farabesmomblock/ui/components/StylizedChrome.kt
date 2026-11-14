package com.farabe.farabesmomblock.ui.components

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.farabe.farabesmomblock.R
import com.farabe.farabesmomblock.remote.RemoteTheme
import com.farabe.farabesmomblock.ui.theme.WarmCream
import kotlin.math.sin

enum class HubTab(val label: String, val icon: ImageVector) {
    GIFTS("Gifts", Icons.Default.CardGiftcard),
    ITEMS("Items", Icons.Default.Backpack),
    HOME("Home", Icons.Default.Home),
    SKETCHBOOK("Album", Icons.Default.MenuBook),
    HELPERS("Helpers", Icons.Default.Group)
}

val CreamModalBg = Color(0xFFFFF6E8)
val CreamCardBg = Color(0xFFFFF1DB)
val AmberRibbonTop = Color(0xFFFFB74D)
val AmberRibbonBottom = Color(0xFFF57C00)
val CyanActiveGlow = Color(0xFF4FC3F7)

/**
 * Dynamic full-screen 3D twilight garden backdrop with optional live-pushed custom image
 * and floating ambient fireflies / golden sparkles / rose petals.
 */
@Composable
fun DynamicGameBackground(
    customBitmap: Bitmap?,
    @DrawableRes fallbackRes: Int = R.drawable.bg_twilight_garden,
    primaryTint: Color,
    ambientEffect: String = "FIREFLIES_AND_PETALS",
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = fallbackRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Subtle atmospheric twilight gradient + center vignette so blocks always stand out cleanly over photos
        val veilAlpha = if (customBitmap != null) 0.72f else 0.55f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryTint.copy(alpha = veilAlpha),
                            primaryTint.copy(alpha = if (customBitmap != null) 0.30f else 0.08f),
                            primaryTint.copy(alpha = veilAlpha + 0.08f)
                        )
                    )
                )
        )

        if (ambientEffect != "NONE") {
            AmbientFirefliesOverlay(
                usePetals = ambientEffect == "FIREFLIES_AND_PETALS" || ambientEffect == "ROSE_PETALS",
                useSparkles = ambientEffect != "ROSE_PETALS"
            )
        }
    }
}

@Composable
fun AmbientFirefliesOverlay(
    usePetals: Boolean = true,
    useSparkles: Boolean = true
) {
    val inf = rememberInfiniteTransition(label = "fireflies")
    val phase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Restart),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (useSparkles) {
            for (i in 0 until 16) {
                val fx = ((i * 193 + sin(phase + i) * 35f) % w + w) % w
                val fy = ((i * 271 + phase * 42f) % h + h) % h
                val alpha = 0.25f + 0.5f * ((sin(phase * 2f + i) + 1f) * 0.5f)
                drawCircle(
                    color = Color(0xFFFFE082).copy(alpha = alpha * 0.45f),
                    radius = 12f + (i % 3) * 4f,
                    center = Offset(fx, fy)
                )
                drawCircle(
                    color = Color(0xFFFFF8E1).copy(alpha = alpha),
                    radius = 4f,
                    center = Offset(fx, fy)
                )
            }
        }
        if (usePetals) {
            for (i in 0 until 8) {
                val px = ((i * 237 + sin(phase * 0.8f + i * 2) * 50f) % w + w) % w
                val py = ((i * 313 + phase * 65f) % h + h) % h
                drawCircle(
                    color = Color(0xFFFF80AB).copy(alpha = 0.45f),
                    radius = 6f,
                    center = Offset(px, py)
                )
            }
        }
    }
}

/**
 * Stylized Top Status Bar matching reference screenshot (Hearts + Coins + Sync + Settings).
 */
@Composable
fun StylizedTopBar(
    hearts: Int,
    coins: Int,
    theme: RemoteTheme,
    isSyncing: Boolean = false,
    onHeartsClick: () -> Unit,
    onCoinsClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(theme.primaryColor.copy(alpha = 0.96f), theme.primaryColor.copy(alpha = 0.78f))
                )
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hearts Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A0A2E).copy(alpha = 0.85f))
                    .border(1.5.dp, theme.borderColor, RoundedCornerShape(20.dp))
                    .clickable { onHeartsClick() }
                    .padding(start = 8.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Hearts",
                    tint = Color(0xFFFF3B5C),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "$hearts",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(theme.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }

            // Gold Coins Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A0A2E).copy(alpha = 0.85f))
                    .border(1.5.dp, theme.borderColor, RoundedCornerShape(20.dp))
                    .clickable { onCoinsClick() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFFFFE57F), Color(0xFFFFA000)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFF7A4800),
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "%,d".format(coins),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Backend Sync Pill
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceColor)
                    .border(1.5.dp, theme.borderColor, RoundedCornerShape(12.dp))
                    .clickable { onSyncClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync from Farabe",
                    tint = if (isSyncing) CyanActiveGlow else theme.goldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Settings Gear Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceColor)
                    .border(1.5.dp, theme.borderColor, RoundedCornerShape(12.dp))
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 5-Tab Floating Glossy Bottom Navigation Bar matching the uploaded reference.
 */
@Composable
fun StylizedBottomNavBar(
    selectedTab: HubTab,
    hasUnreadGiftOrLetter: Boolean,
    theme: RemoteTheme,
    onTabSelected: (HubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(theme.surfaceColor, theme.primaryColor)
                )
            )
            .border(1.5.dp, theme.borderColor.copy(alpha = 0.8f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            HubTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val lift by animateDpAsState(targetValue = if (isSelected) (-6).dp else 0.dp, label = "tabLift")

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = lift)
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) {
                                Brush.verticalGradient(listOf(CyanActiveGlow, Color(0xFF0288D1)))
                            } else {
                                Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            } else Modifier
                        )
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) Color.White else WarmCream.copy(alpha = 0.82f),
                                modifier = Modifier.size(if (isSelected) 26.dp else 22.dp)
                            )
                            if (hasUnreadGiftOrLetter && (tab == HubTab.GIFTS || tab == HubTab.HELPERS)) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF2E4C))
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else WarmCream.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Giant 3D Beveled Glossy Green "PLAY" Button matching the uploaded reference.
 */
@Composable
fun GlossyPlayButton(
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darker = Color(
        red = (accentColor.red * 0.65f).coerceIn(0f, 1f),
        green = (accentColor.green * 0.65f).coerceIn(0f, 1f),
        blue = (accentColor.blue * 0.65f).coerceIn(0f, 1f)
    )
    val lighter = Color(
        red = (accentColor.red * 1.18f).coerceIn(0f, 1f),
        green = (accentColor.green * 1.18f).coerceIn(0f, 1f),
        blue = (accentColor.blue * 1.18f).coerceIn(0f, 1f)
    )

    Box(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(darker)
            .padding(bottom = 5.dp) // 3D bottom lip
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.verticalGradient(listOf(lighter, accentColor, darker)))
            .border(2.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .padding(horizontal = 42.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.5.sp
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}

/**
 * Glossy Cream & Gold Modal Dialog matching Screen #8 in the uploaded reference.
 */
@Composable
fun CreamGoldModalDialog(
    ribbonTitle: String,
    speechBubbleText: String? = null,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Main Cream Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(4.dp, Color(0xFFFFB74D), RoundedCornerShape(28.dp)),
                color = CreamModalBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 34.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (speechBubbleText != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.portrait_farabe),
                                contentDescription = "Farabe",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, AmberRibbonBottom, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE0C8A8), RoundedCornerShape(14.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = speechBubbleText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4A2511),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }

                    content()
                }
            }

            // Floating Orange-Gold Ribbon Header
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(AmberRibbonTop, AmberRibbonBottom)))
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = ribbonTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Top-Right Close Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 4.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
