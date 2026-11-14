package com.farabe.farabesmomblock.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.farabe.farabesmomblock.audio.SoundManager
import com.farabe.farabesmomblock.data.datastore.PreferencesManager
import com.farabe.farabesmomblock.data.datastore.UserSettings
import com.farabe.farabesmomblock.updater.AppUpdater
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    soundManager: SoundManager? = null,
    appUpdater: AppUpdater? = null,
    onCheckForUpdates: (((Boolean) -> Unit) -> Unit)? = null,
    onSyncRemote: (() -> Unit)? = null,
    lastSyncBanner: String? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val settings by preferencesManager.settingsFlow.collectAsState(initial = UserSettings())
    val coroutineScope = rememberCoroutineScope()
    var showResetConfirm by remember { mutableStateOf(false) }
    var momNameInput by remember { mutableStateOf(settings.momName) }

    val userMusicFile = remember { File(context.filesDir, "custom_bg_music.mp3") }
    var hasCustomUserFile by remember { mutableStateOf(userMusicFile.exists()) }
    var activeMusicSource by remember {
        mutableStateOf(soundManager?.getActiveMusicSource() ?: "Ambient Chimes")
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    userMusicFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                hasCustomUserFile = true
                soundManager?.reloadBackgroundMusic()
                activeMusicSource = soundManager?.getActiveMusicSource() ?: "Custom Song"
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .verticalScroll(scrollState)
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
                    text = "SETTINGS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = WarmCream
                )
                Text(
                    text = "Comfort & Personalization",
                    fontSize = 12.sp,
                    color = GoldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Personalization Card
        SettingsCard(title = "Personalization") {
            Text(
                text = "What should the game call you?",
                fontSize = 13.sp,
                color = WarmCream.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = momNameInput,
                onValueChange = {
                    momNameInput = it
                    coroutineScope.launch { preferencesManager.updateMomName(it) }
                },
                label = { Text("Name / Title") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio Settings Card
        SettingsCard(title = "Audio Controls") {
            Text(
                text = "Sound Effects (${(settings.soundEffectsVolume * 100).toInt()}%)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmCream
            )
            Slider(
                value = settings.soundEffectsVolume,
                onValueChange = { coroutineScope.launch { preferencesManager.updateSfxVolume(it) } },
                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = RoseAccent)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Calming Background Music (${(settings.musicVolume * 100).toInt()}%)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmCream
            )
            Slider(
                value = settings.musicVolume,
                onValueChange = { coroutineScope.launch { preferencesManager.updateMusicVolume(it) } },
                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = RoseAccent)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Background Music Chooser
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkerGrid)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Custom Background Song",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Active: $activeMusicSource",
                        fontSize = 11.sp,
                        color = WarmCream.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                audioPickerLauncher.launch("audio/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎵 Choose Song", fontSize = 11.sp, color = Color.White)
                        }

                        if (hasCustomUserFile) {
                            Button(
                                onClick = {
                                    userMusicFile.delete()
                                    soundManager?.reloadBackgroundMusic()
                                    activeMusicSource = soundManager?.getActiveMusicSource() ?: "Ambient Chimes"
                                    hasCustomUserFile = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset Default", fontSize = 11.sp, color = WarmCream)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Supports any MP3 / WAV / OGG from phone storage or assets/custom_music/",
                        fontSize = 10.sp,
                        color = WarmCream.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tactile & Accessibility Card
        SettingsCard(title = "Haptics & Accessibility") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vibration Feedback",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmCream
                    )
                    Text(
                        text = "Gentle haptic response on block placement",
                        fontSize = 11.sp,
                        color = WarmCream.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = settings.isVibrationEnabled,
                    onCheckedChange = { coroutineScope.launch { preferencesManager.updateVibration(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = RoseAccent)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "High Contrast Visuals",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmCream
                    )
                    Text(
                        text = "Enhanced cell outlines for easier visibility",
                        fontSize = 11.sp,
                        color = WarmCream.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = settings.isHighContrastEnabled,
                    onCheckedChange = { coroutineScope.launch { preferencesManager.updateHighContrast(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = RoseAccent)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Farabe's Live Game Studio (Local Backend & GitHub Cloud Sync)
        SettingsCard(title = "Farabe's Live Game Studio (Backend Sync)") {
            var backendInput by remember(settings.backendUrl) { mutableStateOf(settings.backendUrl) }
            var cloudInput by remember(settings.cloudUrl) { mutableStateOf(settings.cloudUrl) }
            var savedFeedback by remember { mutableStateOf<String?>(null) }

            Text(
                text = "Local PC Studio URL (Emulator: http://10.0.2.2:8787/api/config)",
                fontSize = 11.sp,
                color = WarmCream.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = backendInput,
                onValueChange = { backendInput = it },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { backendInput = "http://10.0.2.2:8787/api/config" },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Emulator Default", color = WarmCream, fontSize = 11.sp)
                }
                Button(
                    onClick = { backendInput = "http://192.168.82.24:8787/api/config" },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Home Wi-Fi IP", color = WarmCream, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "GitHub Cloud Fallback URL (for Mom's real phone away from home)",
                fontSize = 11.sp,
                color = WarmCream.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = cloudInput,
                onValueChange = { cloudInput = it },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            preferencesManager.updateBackendUrl(backendInput)
                            preferencesManager.updateCloudUrl(cloudInput)
                            savedFeedback = "Saved URLs! Syncing..."
                            onSyncRemote?.invoke()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5FD63A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save & Sync Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (savedFeedback != null || lastSyncBanner != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lastSyncBanner ?: savedFeedback ?: "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Over-The-Air App Updates Card
        SettingsCard(title = "App Updates & Surprises") {
            val versionName = appUpdater?.getCurrentVersionName() ?: "1.0.0"
            val versionCode = appUpdater?.getCurrentVersionCode() ?: 1
            var isChecking by remember { mutableStateOf(false) }
            var updateStatusText by remember { mutableStateOf<String?>(null) }

            Text(
                text = "Current: Version $versionName (Build $versionCode)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmCream
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "When Farabe adds new songs or surprises, you can update with 1-tap.",
                fontSize = 11.sp,
                color = WarmCream.copy(alpha = 0.7f)
            )

            if (updateStatusText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = updateStatusText!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    isChecking = true
                    updateStatusText = "Checking for new surprises..."
                    onCheckForUpdates?.invoke { hasUpdate ->
                        isChecking = false
                        updateStatusText = if (hasUpdate) {
                            "New update found! Showing update window..."
                        } else {
                            "আম্মু, তুমি সর্বশেষ সংস্করণটি ব্যবহার করছ! ❤️ (Up to date!)"
                        }
                    }
                },
                enabled = !isChecking,
                colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isChecking) "Checking..." else "🔄 Check for Updates",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset Card
        Button(
            onClick = { showResetConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = RoseAccent)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Reset Game Progress", color = RoseAccent, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "FARABE'S MOM – BLOCK",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarmCream.copy(alpha = 0.8f)
            )
            Text(
                text = "Version 1.0.0 • Offline Native Android",
                fontSize = 11.sp,
                color = WarmCream.copy(alpha = 0.5f)
            )
            Text(
                text = "Crafted with endless love by Farabe ❤️",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = RoseAccent,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (showResetConfirm) {
        Dialog(onDismissRequest = { showResetConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkerGrid,
                modifier = Modifier.fillMaxWidth().border(2.dp, CardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reset All Progress?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmCream
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "This will reset high scores, garden items, and letters back to initial state.",
                        fontSize = 13.sp,
                        color = WarmCream.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showResetConfirm = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = WarmCream)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    preferencesManager.resetAllSettings()
                                    showResetConfirm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkerGrid)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WarmCream
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
