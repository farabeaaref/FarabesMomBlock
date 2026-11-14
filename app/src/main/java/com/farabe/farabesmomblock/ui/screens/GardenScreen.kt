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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.farabe.farabesmomblock.data.db.GardenItemEntity
import com.farabe.farabesmomblock.data.repository.GameRepository
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.Mint
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream

@Composable
fun GardenScreen(
    repository: GameRepository,
    onBackClick: () -> Unit
) {
    val gardenItems by repository.gardenItems.collectAsState(initial = emptyList())
    val unlockedCount = gardenItems.count { it.isUnlocked }
    val totalCount = gardenItems.size.coerceAtLeast(1)
    val progress = unlockedCount.toFloat() / totalCount

    var selectedItem by remember { mutableStateOf<GardenItemEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftCardBg)
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WarmCream,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "MOM'S GARDEN 🌸",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = WarmCream
                )
                Text(
                    text = "A sanctuary blooming with love",
                    fontSize = 12.sp,
                    color = Mint
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Garden Progress Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DarkerGrid)
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Garden Blooming Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmCream
                    )
                    Text(
                        text = "$unlockedCount / $totalCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Mint,
                    trackColor = SoftCardBg,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Keep playing to unlock new flowers, birds, and peaceful decorations!",
                    fontSize = 11.sp,
                    color = WarmCream.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of Garden Items
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(gardenItems) { item ->
                GardenItemCard(
                    item = item,
                    onClick = { selectedItem = item }
                )
            }
        }
    }

    // Detail Dialog
    selectedItem?.let { item ->
        Dialog(onDismissRequest = { selectedItem = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = DarkerGrid,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, CardBorder, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (item.isUnlocked) item.symbolEmoji else "🔒",
                        fontSize = 44.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isUnlocked) WarmCream else WarmCream.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (item.isUnlocked) item.description
                        else "Locked. Reach ${item.requiredScore} points in puzzle mode to bloom this in Mom's garden!",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = WarmCream.copy(alpha = 0.85f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoseAccent)
                            .clickable { selectedItem = null }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun GardenItemCard(
    item: GardenItemEntity,
    onClick: () -> Unit
) {
    val alpha = if (item.isUnlocked) 1.0f else 0.45f

    Box(
        modifier = Modifier
            .height(105.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkerGrid.copy(alpha = alpha))
            .border(
                1.dp,
                if (item.isUnlocked) Mint.copy(alpha = 0.6f) else CardBorder.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (item.isUnlocked) {
                Text(text = item.symbolEmoji, fontSize = 30.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = WarmCream.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmCream.copy(alpha = alpha),
                maxLines = 1
            )

            if (!item.isUnlocked) {
                Text(
                    text = "${item.requiredScore} pts",
                    fontSize = 10.sp,
                    color = GoldAccent.copy(alpha = 0.8f)
                )
            }
        }
    }
}
