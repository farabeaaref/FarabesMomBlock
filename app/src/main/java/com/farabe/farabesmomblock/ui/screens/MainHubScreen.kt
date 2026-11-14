package com.farabe.farabesmomblock.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.R
import com.farabe.farabesmomblock.data.db.GardenItemEntity
import com.farabe.farabesmomblock.data.db.SecretLetterEntity
import com.farabe.farabesmomblock.remote.RemoteGameConfig
import com.farabe.farabesmomblock.remote.RemoteTask
import com.farabe.farabesmomblock.remote.RemoteTheme
import com.farabe.farabesmomblock.ui.components.AmberRibbonBottom
import com.farabe.farabesmomblock.ui.components.AmberRibbonTop
import com.farabe.farabesmomblock.ui.components.CreamCardBg
import com.farabe.farabesmomblock.ui.components.CreamGoldModalDialog
import com.farabe.farabesmomblock.ui.components.CyanActiveGlow
import com.farabe.farabesmomblock.ui.components.DynamicGameBackground
import com.farabe.farabesmomblock.ui.components.GlossyPlayButton
import com.farabe.farabesmomblock.ui.components.HubTab
import com.farabe.farabesmomblock.ui.components.StylizedBottomNavBar
import com.farabe.farabesmomblock.ui.components.StylizedTopBar
import com.farabe.farabesmomblock.ui.components3d.Filament3DShowcase
import com.farabe.farabesmomblock.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun MainHubScreen(
    viewModel: GameViewModel,
    onPlayMode: (String) -> Unit,
    onOpenLevels: () -> Unit,
    onOpenFamilyMemory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val remoteConfig by viewModel.remoteConfig.collectAsState()
    val mediaRevision by viewModel.mediaRevision.collectAsState()
    val isSyncing by viewModel.isSyncingRemote.collectAsState()
    val pendingGift by viewModel.pendingGiftToClaim.collectAsState()
    val syncBanner by viewModel.lastSyncBanner.collectAsState()
    val dailyMessage by viewModel.dailyMessage.collectAsState()

    val gardenItems by viewModel.repository.gardenItems.collectAsState(initial = emptyList())
    val secretLetters by viewModel.repository.secretLetters.collectAsState(initial = emptyList())
    val familyMemory by viewModel.repository.familyMemory.collectAsState(initial = null)

    var selectedTab by remember { mutableStateOf(HubTab.HOME) }
    var selectedPlayMode by remember { mutableStateOf("CLASSIC") }
    var inspectedGardenItem by remember { mutableStateOf<GardenItemEntity?>(null) }
    var inspectedLetter by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showDailyModal by remember { mutableStateOf(false) }
    var showSketchbookModal by remember { mutableStateOf(false) }
    var showStatusInfoModal by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val theme = remoteConfig.theme

    val cachedHomeBitmap = remember(mediaRevision) {
        viewModel.remoteConfigManager.loadCachedBitmap(viewModel.remoteConfigManager.cachedHomeBgFile)
    }

    val unreadLettersCount = secretLetters.count { it.isUnlocked && !it.isRead }
    val hasUnclaimedGift = remoteConfig.activeGift != null &&
        remoteConfig.activeGift!!.giftId.isNotBlank() &&
        remoteConfig.activeGift!!.giftId != userSettings.lastClaimedGiftId

    val highScore = maxOf(gameState.highScore, userSettings.allTimeHighScore)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Live 3D Garden Twilight Background (or remote pushed wallpaper)
        DynamicGameBackground(
            customBitmap = cachedHomeBitmap,
            fallbackRes = R.drawable.bg_twilight_garden,
            primaryTint = theme.primaryColor,
            ambientEffect = remoteConfig.background.ambientEffect
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // 2. Top Status Bar (Hearts, Gold Coins, Sync, Settings)
            StylizedTopBar(
                hearts = userSettings.hearts,
                coins = userSettings.coins,
                theme = theme,
                isSyncing = isSyncing,
                onHeartsClick = { showStatusInfoModal = true },
                onCoinsClick = { showStatusInfoModal = true },
                onSyncClick = { viewModel.syncRemoteConfig(isManual = true) },
                onSettingsClick = onOpenSettings
            )

            // Sync confirmation ribbon when Farabe pushes a new update
            if (syncBanner != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surfaceColor.copy(alpha = 0.94f))
                        .border(1.5.dp, theme.goldAccent, RoundedCornerShape(14.dp))
                        .clickable { viewModel.clearSyncBanner() }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = syncBanner ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "OK",
                        color = theme.goldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // 3. Active Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    HubTab.HOME -> HomeTabContent(
                        momName = userSettings.momName,
                        highScore = highScore,
                        activeMode = selectedPlayMode,
                        theme = theme,
                        hasUnclaimedGift = hasUnclaimedGift,
                        isCelebrating = pendingGift != null,
                        songTitle = remoteConfig.music.activeSongTitle,
                        onModeSelected = { selectedPlayMode = it },
                        onPlayClick = {
                            if (selectedPlayMode == "LEVELS") {
                                onOpenLevels()
                            } else {
                                onPlayMode(selectedPlayMode)
                            }
                        },
                        onDailyGiftClick = {
                            if (hasUnclaimedGift) {
                                viewModel.pendingGiftToClaim.value = remoteConfig.activeGift
                            } else {
                                showDailyModal = true
                            }
                        },
                        onRelaxQuickClick = { onPlayMode("RELAX") },
                        onLevelsClick = onOpenLevels,
                        onMusicClick = { selectedTab = HubTab.GIFTS },
                        onLettersClick = { selectedTab = HubTab.HELPERS }
                    )

                    HubTab.ITEMS -> ItemsTabContent(
                        gardenItems = gardenItems,
                        theme = theme,
                        onItemClick = { inspectedGardenItem = it }
                    )

                    HubTab.SKETCHBOOK -> SketchbookTabContent(
                        viewModel = viewModel,
                        mediaRevision = mediaRevision,
                        familyPhotoUri = familyMemory?.photoUri,
                        memoryTitle = familyMemory?.title ?: "A Special Memory with Mom",
                        memoryNote = familyMemory?.personalNote ?: "",
                        theme = theme,
                        onOpenSketchbookModal = { showSketchbookModal = true },
                        onEditFamilyMemory = onOpenFamilyMemory
                    )

                    HubTab.HELPERS -> HelpersTabContent(
                        viewModel = viewModel,
                        mediaRevision = mediaRevision,
                        remoteConfig = remoteConfig,
                        secretLetters = secretLetters,
                        coins = userSettings.coins,
                        gems = userSettings.gems,
                        onOpenLetter = { letterId, title, body ->
                            inspectedLetter = title to body
                            if (letterId.isNotBlank()) {
                                coroutineScope.launch {
                                    viewModel.repository.markLetterRead(letterId)
                                }
                            }
                        },
                        onClaimTask = { task ->
                            coroutineScope.launch {
                                viewModel.repository.preferencesManager.addCoinsAndHearts(
                                    coinsDelta = task.rewardCoins,
                                    gemsDelta = task.rewardGems
                                )
                                viewModel.soundManager.playUnlockReward()
                                viewModel.lastSyncBanner.value = "✨ Claimed +${task.rewardCoins} coins from ${task.helperName}!"
                            }
                        }
                    )

                    HubTab.GIFTS -> GiftsShopTabContent(
                        viewModel = viewModel,
                        remoteConfig = remoteConfig,
                        isSyncing = isSyncing,
                        hasUnclaimedGift = hasUnclaimedGift,
                        onOpenClaimDialog = {
                            if (remoteConfig.activeGift != null) {
                                viewModel.pendingGiftToClaim.value = remoteConfig.activeGift
                            }
                        },
                        onOpenSettings = onOpenSettings
                    )
                }
            }

            // 4. Persistent 5-Tab Bottom Navigation Bar
            StylizedBottomNavBar(
                selectedTab = selectedTab,
                hasUnreadGiftOrLetter = unreadLettersCount > 0 || hasUnclaimedGift,
                theme = theme,
                onTabSelected = { selectedTab = it }
            )
        }
    }

    // --- CREAM & GOLD POPUP MODALS ---

    if (pendingGift != null) {
        val gift = pendingGift!!
        CreamGoldModalDialog(
            ribbonTitle = gift.title,
            speechBubbleText = "আম্মু, তোমার জন্য একটি বিশেষ উপহার পাঠিয়েছি! ❤️",
            onDismiss = { viewModel.dismissPendingGift() }
        ) {
            Text(
                text = gift.messageBengali,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A2511),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RewardBadgePill("🪙 +${gift.coinsReward}", Color(0xFFFFD54F), Color(0xFF5D3A00))
                RewardBadgePill("❤️ +${gift.heartsReward}", Color(0xFFFF8A80), Color(0xFF680014))
            }
            Spacer(modifier = Modifier.height(18.dp))
            GlossyPlayButton(
                title = "CLAIM GIFT",
                subtitle = "সংগ্রহ করো",
                accentColor = theme.accentColor,
                onClick = { viewModel.claimActiveGift() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (inspectedGardenItem != null) {
        val item = inspectedGardenItem!!
        CreamGoldModalDialog(
            ribbonTitle = item.name,
            speechBubbleText = if (item.isUnlocked) "তুমি এটি আনলক করেছো আম্মু! 🌸" else "আরও খেললে এটি খুলে যাবে!",
            onDismiss = { inspectedGardenItem = null }
        ) {
            Text(text = item.symbolEmoji, fontSize = 54.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                fontSize = 14.sp,
                color = Color(0xFF4A2511),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (item.isUnlocked) "Unlocked ✓" else "Unlock at ${item.requiredScore} pts or ${item.requiredLines} lines",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isUnlocked) Color(0xFF2E7D32) else Color(0xFFB71C1C)
            )
        }
    }

    if (inspectedLetter != null) {
        val (title, body) = inspectedLetter!!
        CreamGoldModalDialog(
            ribbonTitle = title,
            speechBubbleText = "আমার প্রিয় আম্মুর জন্য একটি চিঠি 💌",
            onDismiss = { inspectedLetter = null }
        ) {
            Text(
                text = body,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Color(0xFF3E2723),
                textAlign = TextAlign.Start
            )
        }
    }

    if (showDailyModal) {
        CreamGoldModalDialog(
            ribbonTitle = "Daily Blessing 🌸",
            speechBubbleText = "শুভ সকাল আম্মু! আজকের দিনটি অনেক সুন্দর কাটুক।",
            onDismiss = { showDailyModal = false }
        ) {
            Text(
                text = dailyMessage ?: "প্রতিটি ব্লক সাজানোর সাথে আমার ভালোবাসা রইল। ❤️",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4A2511),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            GlossyPlayButton(
                title = "PLAY NOW",
                subtitle = "চলো খেলি!",
                accentColor = theme.accentColor,
                onClick = {
                    showDailyModal = false
                    onPlayMode("CLASSIC")
                }
            )
        }
    }

    if (showSketchbookModal) {
        CreamGoldModalDialog(
            ribbonTitle = familyMemory?.title ?: "Shapla Garden Memory",
            speechBubbleText = "আম্মু, এই ছবি ও স্মৃতিটা তোমার জন্য সাজিয়েছি 🎨",
            onDismiss = { showSketchbookModal = false }
        ) {
            val cachedSketch = remember(mediaRevision) {
                viewModel.remoteConfigManager.loadCachedBitmap(viewModel.remoteConfigManager.cachedSketchbookFile)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, AmberRibbonTop, RoundedCornerShape(16.dp))
            ) {
                if (cachedSketch != null) {
                    Image(
                        bitmap = cachedSketch.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.sketchbook_shapla),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = familyMemory?.personalNote ?: "",
                fontSize = 13.sp,
                color = Color(0xFF4A2511),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    showSketchbookModal = false
                    onOpenFamilyMemory()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3FE4))
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Mom's Photo", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showStatusInfoModal) {
        CreamGoldModalDialog(
            ribbonTitle = "Mom's Wallet ❤️",
            speechBubbleText = "লাইন ক্লিয়ার করলে আর উপহার খুললেই কয়েন বাড়বে!",
            onDismiss = { showStatusInfoModal = false }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RewardBadgePill("❤️ ${userSettings.hearts} Hearts", Color(0xFFFFCDD2), Color(0xFF880E4F))
                RewardBadgePill("🪙 ${userSettings.coins} Coins", Color(0xFFFFE082), Color(0xFF5D3A00))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "All-time High Score: $highScore • Gems: ${userSettings.gems} 💎",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A2511)
            )
        }
    }
}

@Composable
private fun HomeTabContent(
    momName: String,
    highScore: Int,
    activeMode: String,
    theme: RemoteTheme,
    hasUnclaimedGift: Boolean,
    isCelebrating: Boolean,
    songTitle: String,
    onModeSelected: (String) -> Unit,
    onPlayClick: () -> Unit,
    onDailyGiftClick: () -> Unit,
    onRelaxQuickClick: () -> Unit,
    onLevelsClick: () -> Unit,
    onMusicClick: () -> Unit,
    onLettersClick: () -> Unit
) {
    val milestoneTarget = ((highScore / 1000) + 1) * 1000
    val progress = ((highScore % 1000).toFloat() / 1000f).coerceIn(0.08f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Milestone Key & Treasure Chest Track (matching top of Screen #1 in reference)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(theme.primaryColor.copy(alpha = 0.78f))
                .border(1.5.dp, theme.borderColor.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = theme.goldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$highScore / $milestoneTarget",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "Welcome, $momName ❤️",
                    color = theme.goldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF170B2A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(listOf(theme.accentColor, theme.goldAccent))
                        )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🌱 500", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                Text("🎁 750", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                Text("👑 $milestoneTarget", color = theme.goldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 2. Center Interactive 3D Filament PBR Stage + Left/Right Event Medallions
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Medallion Column
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EventMedallion(
                    icon = Icons.Default.CardGiftcard,
                    label = if (hasUnclaimedGift) "NEW GIFT!" else "Daily",
                    badgeDot = hasUnclaimedGift,
                    tint = Color(0xFFFF8F00),
                    theme = theme,
                    onClick = onDailyGiftClick
                )
                EventMedallion(
                    icon = Icons.Default.Spa,
                    label = "Relax",
                    tint = Color(0xFF66BB6A),
                    theme = theme,
                    onClick = onRelaxQuickClick
                )
                EventMedallion(
                    icon = Icons.Default.MailOutline,
                    label = "Notes",
                    tint = Color(0xFFEC407A),
                    theme = theme,
                    onClick = onLettersClick
                )
            }

            // Center Real-Time Google Filament 3D PBR Crystal Block Showcase
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Filament3DShowcase(
                    accentColor = theme.accentColor,
                    goldColor = theme.goldAccent,
                    isCelebrationActive = isCelebrating || hasUnclaimedGift,
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    text = "✦ Drag 3D Jewel ✦",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            // Right Medallion Column
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EventMedallion(
                    icon = Icons.Default.Star,
                    label = "Levels",
                    tint = Color(0xFF29B6F6),
                    theme = theme,
                    onClick = onLevelsClick
                )
                EventMedallion(
                    icon = Icons.Default.MusicNote,
                    label = "Music",
                    tint = Color(0xFFAB47BC),
                    theme = theme,
                    onClick = onMusicClick
                )
                EventMedallion(
                    icon = Icons.Default.PlayArrow,
                    label = "Classic",
                    tint = theme.accentColor,
                    theme = theme,
                    onClick = onPlayClick
                )
            }
        }

        // 3. Mode Selector Pills + Giant 3D Glossy Green PLAY Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                listOf("CLASSIC" to "Classic", "RELAX" to "Relax 🌸", "LEVELS" to "Levels ⭐").forEach { (key, title) ->
                    val selected = activeMode == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) theme.goldAccent else theme.primaryColor.copy(alpha = 0.85f)
                            )
                            .border(
                                1.dp,
                                if (selected) Color.White else theme.borderColor,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onModeSelected(key) }
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selected) Color(0xFF3E1F00) else Color.White
                        )
                    }
                }
            }

            GlossyPlayButton(
                title = "PLAY",
                subtitle = when (activeMode) {
                    "RELAX" -> "PEACEFUL MODE • চলো খেলি"
                    "LEVELS" -> "ADVENTURE STAGES"
                    else -> "HIGH SCORE: $highScore"
                },
                accentColor = theme.accentColor,
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth(0.76f)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🎵 Now Playing: $songTitle",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EventMedallion(
    icon: ImageVector,
    label: String,
    tint: Color,
    theme: RemoteTheme,
    badgeDot: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(listOf(tint.copy(alpha = 0.95f), theme.primaryColor))
                    )
                    .border(2.dp, theme.goldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            if (badgeDot) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF1744))
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A0930).copy(alpha = 0.78f))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun ItemsTabContent(
    gardenItems: List<GardenItemEntity>,
    theme: RemoteTheme,
    onItemClick: (GardenItemEntity) -> Unit
) {
    val unlockedCount = gardenItems.count { it.isUnlocked }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Items • Mom's Treasures ($unlockedCount/${gardenItems.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(gardenItems, key = { it.id }) { item ->
                Box(
                    modifier = Modifier
                        .aspectRatio(0.84f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(theme.surfaceColor.copy(alpha = 0.94f), theme.primaryColor.copy(alpha = 0.96f))
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (item.isUnlocked) theme.goldAccent else theme.borderColor.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onItemClick(item) }
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (item.isUnlocked) Color(0xFF4CAF50) else Color(0xFF4A316B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Text(
                            text = item.symbolEmoji,
                            fontSize = 34.sp
                        )

                        Text(
                            text = item.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SketchbookTabContent(
    viewModel: GameViewModel,
    mediaRevision: Int,
    familyPhotoUri: String?,
    memoryTitle: String,
    memoryNote: String,
    theme: RemoteTheme,
    onOpenSketchbookModal: () -> Unit,
    onEditFamilyMemory: () -> Unit
) {
    val context = LocalContext.current
    var selectedChapter by remember { mutableIntStateOf(0) }
    val chapters = listOf("Chapter 1", "Chapter 2", "Mom Photo")

    val cachedSketch = remember(mediaRevision) {
        viewModel.remoteConfigManager.loadCachedBitmap(viewModel.remoteConfigManager.cachedSketchbookFile)
    }
    val cachedMomPhoto = remember(mediaRevision) {
        viewModel.remoteConfigManager.loadCachedBitmap(viewModel.remoteConfigManager.cachedMomPhotoFile)
    }
    val localFamilyBitmap = remember(familyPhotoUri) {
        if (familyPhotoUri.isNullOrBlank()) null
        else try {
            context.contentResolver.openInputStream(Uri.parse(familyPhotoUri))?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (_: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sketchbook",
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chapters.forEachIndexed { idx, label ->
                val isSelected = selectedChapter == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) Brush.verticalGradient(listOf(AmberRibbonTop, AmberRibbonBottom))
                            else Brush.verticalGradient(listOf(theme.surfaceColor, theme.primaryColor))
                        )
                        .border(1.5.dp, if (isSelected) Color.White else theme.borderColor, RoundedCornerShape(14.dp))
                        .clickable { selectedChapter = idx }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spiral-Bound Sketchbook Frame matching Screen #4 in uploaded reference
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable { onOpenSketchbookModal() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .shadow(10.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFFF8ED))
                    .border(4.dp, Color(0xFFD84315), RoundedCornerShape(22.dp))
                    .padding(14.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    val bmpToShow = when (selectedChapter) {
                        2 -> cachedMomPhoto ?: localFamilyBitmap
                        else -> cachedSketch
                    }
                    if (bmpToShow != null) {
                        Image(
                            bitmap = bmpToShow.asImageBitmap(),
                            contentDescription = "Sketchbook Memory",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                id = if (selectedChapter == 1) R.drawable.bg_twilight_garden else R.drawable.sketchbook_shapla
                            ),
                            contentDescription = "Sketchbook Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = memoryTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4A2511)
                )
                Text(
                    text = memoryNote,
                    fontSize = 12.sp,
                    color = Color(0xFF6D4C41),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Spiral rings along top edge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(9) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E))))
                            .border(1.dp, Color(0xFF616161), RoundedCornerShape(6.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onOpenSketchbookModal,
                colors = ButtonDefaults.buttonColors(containerColor = CyanActiveGlow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("📖 Read Story", color = Color.White, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onEditFamilyMemory,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3FE4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("📸 Pick Mom Photo", color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun HelpersTabContent(
    viewModel: GameViewModel,
    mediaRevision: Int,
    remoteConfig: RemoteGameConfig,
    secretLetters: List<SecretLetterEntity>,
    coins: Int,
    gems: Int,
    onOpenLetter: (letterId: String, title: String, content: String) -> Unit,
    onClaimTask: (RemoteTask) -> Unit
) {
    val theme = remoteConfig.theme
    var subTab by remember { mutableIntStateOf(0) } // 0 = Family, 1 = Tasks

    val cachedHelperBitmap = remember(mediaRevision) {
        viewModel.remoteConfigManager.loadCachedBitmap(viewModel.remoteConfigManager.cachedHelperAvatarFile)
    }

    val tasksToShow = remember(remoteConfig.customTasks) {
        if (remoteConfig.customTasks.isNotEmpty()) remoteConfig.customTasks
        else listOf(
            RemoteTask("t1", "Farabe", "Clear 5 lines today for Mom ❤️", 5, 100, 1),
            RemoteTask("t2", "Misti Kitten", "Play a relaxing round 🌸", 1, 75, 1)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Helpers & Letters",
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Family / Tasks Segmented Toggle (matching Reference #5-#7)
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(18.dp))
                .background(theme.primaryColor.copy(alpha = 0.9f))
                .padding(4.dp)
        ) {
            listOf("Family", "Tasks").forEachIndexed { idx, label ->
                val active = subTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (active) Brush.verticalGradient(listOf(Color(0xFFBA68C8), Color(0xFF7B1FA2)))
                            else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { subTab = idx }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (active) Color.White else Color.White.copy(alpha = 0.72f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (subTab == 0) {
            // Farabe Helper Cream Card
            HelperCreamCard(
                name = "Farabe ❤️",
                description = "Sends new songs, letters, and doubles your daily coin gifts.",
                badgeText = "🪙 $coins",
                progressText = "💎 $gems/5",
                avatarRes = R.drawable.portrait_farabe,
                customAvatarBitmap = cachedHelperBitmap,
                borderColor = AmberRibbonTop
            )
            Spacer(modifier = Modifier.height(10.dp))
            HelperCreamCard(
                name = "Misti Kitten 🐾",
                description = "Cheers Mom on every combo streak in Classic & Relax mode!",
                badgeText = "ACTIVE",
                progressText = "⭐ Lv.2",
                avatarRes = R.drawable.portrait_kitten,
                customAvatarBitmap = null,
                borderColor = AmberRibbonTop
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Secret Letters from Farabe 💌",
                color = theme.goldAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            remoteConfig.customLetters.forEach { remoteLetter ->
                CreamLetterRow(
                    title = "✨ ${remoteLetter.title}",
                    subtitle = "Live message from Farabe's Studio",
                    isUnlocked = true,
                    isUnread = true,
                    onClick = { onOpenLetter("", remoteLetter.title, remoteLetter.content) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            secretLetters.forEach { letter ->
                CreamLetterRow(
                    title = letter.title,
                    subtitle = if (letter.isUnlocked) {
                        if (letter.isRead) "Read ❤️ • ${letter.milestoneLabel}" else "NEW! Tap to read 💌"
                    } else {
                        "🔒 ${letter.milestoneLabel}"
                    },
                    isUnlocked = letter.isUnlocked,
                    isUnread = letter.isUnlocked && !letter.isRead,
                    onClick = {
                        if (letter.isUnlocked) {
                            onOpenLetter(letter.id, letter.title, letter.content)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        } else {
            // Tasks Sub-Tab (matching Reference #7 with purple CLAIM buttons)
            tasksToShow.forEach { task ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CreamCardBg)
                        .border(3.dp, AmberRibbonTop, RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(
                                id = if (task.helperName.contains("Kitten", ignoreCase = true)) R.drawable.portrait_kitten else R.drawable.portrait_farabe
                            ),
                            contentDescription = task.helperName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(2.dp, AmberRibbonBottom, RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.helperName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4A2511)
                            )
                            Text(
                                text = task.title,
                                fontSize = 12.sp,
                                color = Color(0xFF6D4C41)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF8E24AA),
                                trackColor = Color(0xFFE0C8A8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { onClaimTask(task) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "CLAIM +${task.rewardCoins} 🪙",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelperCreamCard(
    name: String,
    description: String,
    badgeText: String,
    progressText: String,
    avatarRes: Int,
    customAvatarBitmap: android.graphics.Bitmap?,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CreamCardBg)
            .border(3.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, AmberRibbonBottom, RoundedCornerShape(16.dp))
            ) {
                if (customAvatarBitmap != null) {
                    Image(
                        bitmap = customAvatarBitmap.asImageBitmap(),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4A2511)
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF6D4C41)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RewardBadgePill(badgeText, Color(0xFFAED581), Color(0xFF1B5E20))
                    RewardBadgePill(progressText, Color(0xFF81D4FA), Color(0xFF01579B))
                }
            }
        }
    }
}

@Composable
private fun CreamLetterRow(
    title: String,
    subtitle: String,
    isUnlocked: Boolean,
    isUnread: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUnlocked) CreamCardBg else Color(0xFF281244).copy(alpha = 0.85f))
            .border(
                1.5.dp,
                if (isUnread) Color(0xFFFF8F00) else Color(0xFF7D48C4),
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isUnlocked) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (isUnlocked) Color(0xFF4A2511) else Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isUnlocked) Color(0xFF6D4C41) else Color.White.copy(alpha = 0.55f)
            )
        }
        Icon(
            imageVector = if (isUnlocked) Icons.Default.Favorite else Icons.Default.Lock,
            contentDescription = null,
            tint = if (isUnread) Color(0xFFE91E63) else Color(0xFF9E9E9E),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun GiftsShopTabContent(
    viewModel: GameViewModel,
    remoteConfig: RemoteGameConfig,
    isSyncing: Boolean,
    hasUnclaimedGift: Boolean,
    onOpenClaimDialog: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val theme = remoteConfig.theme
    val activeSong = remoteConfig.music.activeSongTitle
    val activeSource = remember(activeSong) { viewModel.soundManager.getActiveMusicSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Farabe's Live Gifts & Music",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Active Remote Gift Card
        val gift = remoteConfig.activeGift
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(CreamCardBg)
                .border(3.dp, AmberRibbonTop, RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = gift?.title ?: "No Active Gift Yet 🎁",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4A2511)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = gift?.messageBengali ?: "যখন ফারাবী স্টুডিও থেকে উপহার পাঠাবে, এখানে সাথে সাথে চলে আসবে!",
                    fontSize = 13.sp,
                    color = Color(0xFF6D4C41),
                    textAlign = TextAlign.Center
                )
                if (gift != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenClaimDialog,
                        enabled = hasUnclaimedGift,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.accentColor,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFD7CCC8),
                            disabledContentColor = Color(0xFF4A2511)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (hasUnclaimedGift) "🎁 Claim +${gift.coinsReward} Coins & +${gift.heartsReward} ❤️" else "Claimed ✓",
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Music Controller Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(theme.surfaceColor.copy(alpha = 0.92f))
                .border(1.5.dp, theme.borderColor, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "🎵 Background Music",
                    color = theme.goldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Studio Song: $activeSong", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = "Active Audio: $activeSource", color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3FE4)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Change Song or Server URL in Settings", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Sync Button
        Button(
            onClick = { viewModel.syncRemoteConfig(isManual = true) },
            enabled = !isSyncing,
            colors = ButtonDefaults.buttonColors(containerColor = CyanActiveGlow),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSyncing) "Syncing with Farabe's Studio..." else "🔄 Sync Latest Theme, Song & Gifts Now",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Config v${remoteConfig.configVersion} • App v${remoteConfig.appVersionName}",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RewardBadgePill(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
    }
}
