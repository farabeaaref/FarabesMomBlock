package com.farabe.farabesmomblock

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.farabe.farabesmomblock.ui.components.UpdateDialog
import com.farabe.farabesmomblock.ui.screens.FamilyMemoryScreen
import com.farabe.farabesmomblock.ui.screens.GameScreen
import com.farabe.farabesmomblock.ui.screens.GardenScreen
import com.farabe.farabesmomblock.ui.screens.LevelModeScreen
import com.farabe.farabesmomblock.ui.screens.MainHubScreen
import com.farabe.farabesmomblock.ui.screens.SecretLettersScreen
import com.farabe.farabesmomblock.ui.screens.SettingsScreen
import com.farabe.farabesmomblock.ui.screens.SplashScreen
import com.farabe.farabesmomblock.ui.theme.FarabesMomBlockTheme
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            FarabesMomBlockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NavyBackground
                ) {
                    AppNavigation(gameViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.onAppForegroundChanged(true)
        if (gameViewModel.soundManager.musicVolume > 0.05f) {
            gameViewModel.soundManager.startBackgroundMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.onAppForegroundChanged(false)
        gameViewModel.soundManager.stopBackgroundMusic()
    }
}

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val userSettings by viewModel.userSettings.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val dailyMessage by viewModel.dailyMessage.collectAsState()

    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                MainHubScreen(
                    viewModel = viewModel,
                    onPlayMode = { mode ->
                        viewModel.startNewGame(mode)
                        navController.navigate("game")
                    },
                    onOpenLevels = {
                        viewModel.updateCurrentScreenActivity("Challenge Roadmap (Levels)")
                        navController.navigate("levels")
                    },
                    onOpenFamilyMemory = {
                        viewModel.updateCurrentScreenActivity("Mom's Photo Album")
                        navController.navigate("memory")
                    },
                    onOpenSettings = {
                        viewModel.updateCurrentScreenActivity("Settings")
                        navController.navigate("settings")
                    }
                )
            }

            composable("game") {
                GameScreen(
                    viewModel = viewModel,
                    onHomeClick = {
                        viewModel.onExitGameToHome()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onOpenGarden = {
                        viewModel.updateCurrentScreenActivity("Mom's Garden")
                        navController.navigate("garden")
                    }
                )
            }

            composable("garden") {
                GardenScreen(
                    repository = viewModel.repository,
                    onBackClick = {
                        viewModel.updateCurrentScreenActivity("Home Hub (Home)")
                        navController.popBackStack()
                    }
                )
            }

            composable("letters") {
                SecretLettersScreen(
                    repository = viewModel.repository,
                    onBackClick = {
                        viewModel.updateCurrentScreenActivity("Home Hub (Helpers)")
                        navController.popBackStack()
                    }
                )
            }

            composable("memory") {
                FamilyMemoryScreen(
                    repository = viewModel.repository,
                    onBackClick = {
                        viewModel.updateCurrentScreenActivity("Home Hub (Album)")
                        navController.popBackStack()
                    }
                )
            }

            composable("levels") {
                LevelModeScreen(
                    highestUnlockedLevel = userSettings.highestUnlockedLevel,
                    completedLevels = userSettings.completedLevels,
                    hearts = userSettings.hearts,
                    onRefillHearts = { viewModel.refillMomHeartsFree() },
                    onSelectLevel = { level ->
                        viewModel.startNewGame("LEVEL_${level.levelNumber}")
                        navController.navigate("game")
                    },
                    onBackClick = {
                        viewModel.updateCurrentScreenActivity("Home Hub (Home)")
                        navController.popBackStack()
                    }
                )
            }

            composable("settings") {
                val lastSyncBanner by viewModel.lastSyncBanner.collectAsState()
                SettingsScreen(
                    preferencesManager = viewModel.repository.preferencesManager,
                    soundManager = viewModel.soundManager,
                    appUpdater = viewModel.appUpdater,
                    onCheckForUpdates = { onResult -> viewModel.checkForUpdate(onResult) },
                    onSyncRemote = { viewModel.syncRemoteConfig(isManual = true) },
                    lastSyncBanner = lastSyncBanner,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // Global OTA Update Dialog for Mom
        if (availableUpdate != null) {
            UpdateDialog(
                updateInfo = availableUpdate!!,
                isDownloading = isDownloadingUpdate,
                downloadProgress = downloadProgress,
                onConfirmUpdate = { viewModel.startUpdateDownload() },
                onDismiss = { viewModel.dismissUpdateDialog() }
            )
        }
    }
}
