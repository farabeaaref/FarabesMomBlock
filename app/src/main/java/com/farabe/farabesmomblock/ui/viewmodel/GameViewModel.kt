package com.farabe.farabesmomblock.ui.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farabe.farabesmomblock.audio.SoundManager
import com.farabe.farabesmomblock.data.datastore.PreferencesManager
import com.farabe.farabesmomblock.data.datastore.UserSettings
import com.farabe.farabesmomblock.data.db.AppDatabase
import com.farabe.farabesmomblock.data.repository.GameRepository
import com.farabe.farabesmomblock.engine.BlockColor
import com.farabe.farabesmomblock.engine.GameEngine
import com.farabe.farabesmomblock.engine.GameState
import com.farabe.farabesmomblock.remote.RemoteConfigManager
import com.farabe.farabesmomblock.remote.RemoteGameConfig
import com.farabe.farabesmomblock.remote.RemoteGift
import com.farabe.farabesmomblock.remote.RemoteTask
import com.farabe.farabesmomblock.remote.UpdateSyncWorker
import com.farabe.farabesmomblock.ui.components.FloatingScore
import com.farabe.farabesmomblock.ui.screens.HANDCRAFTED_LEVELS
import com.farabe.farabesmomblock.ui.screens.LevelStage
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.updater.AppUpdater
import com.farabe.farabesmomblock.updater.UpdateInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val preferencesManager = PreferencesManager(application)
    val repository = GameRepository(db, preferencesManager)
    val soundManager = SoundManager(application)
    val appUpdater = AppUpdater(application)
    val remoteConfigManager = RemoteConfigManager(application, preferencesManager)

    // Live Studio Remote Config & Sync States
    val remoteConfig: StateFlow<RemoteGameConfig> = remoteConfigManager.configFlow
    val mediaRevision: StateFlow<Int> = remoteConfigManager.mediaRevision
    val isSyncingRemote = MutableStateFlow(false)
    val pendingGiftToClaim = MutableStateFlow<RemoteGift?>(null)
    val lastSyncBanner = MutableStateFlow<String?>(null)

    // Over-The-Air Update States
    val availableUpdate = MutableStateFlow<UpdateInfo?>(null)
    val isDownloadingUpdate = MutableStateFlow(false)
    val downloadProgress = MutableStateFlow(0f)

    private val engine = GameEngine()

    private val _gameState = MutableStateFlow(engine.currentState)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    // Board geometry mapping
    var boardCoordinates: LayoutCoordinates? = null
        private set

    // Drag Overlay States (Unclipped root screen floating)
    val activeDraggingIndex = MutableStateFlow<Int?>(null)
    val dragTouchPosition = MutableStateFlow(Offset.Zero)
    val candidateAnchor = MutableStateFlow<Pair<Int, Int>?>(null)
    val candidateShapePoints = MutableStateFlow<List<com.farabe.farabesmomblock.engine.Point2D>?>(null)

    // Tap-to-Place Mode: Selected Tray Piece
    val selectedPieceIndex = MutableStateFlow<Int?>(null)

    // Board Ghost & Clear Preview States
    val ghostCells = MutableStateFlow<Map<Pair<Int, Int>, BlockColor>>(emptyMap())
    val willClearRows = MutableStateFlow<Set<Int>>(emptySet())
    val willClearCols = MutableStateFlow<Set<Int>>(emptySet())
    val clearingCells = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())

    // Dynamic Floating Score List
    val floatingScores = MutableStateFlow<List<FloatingScore>>(emptyList())
    private var scoreCounter = 0L

    // Celebratory Combo Banner
    private val _bannerTitle = MutableStateFlow<String?>(null)
    val bannerTitle: StateFlow<String?> = _bannerTitle.asStateFlow()

    private val _bannerSubtitle = MutableStateFlow<String?>(null)
    val bannerSubtitle: StateFlow<String?> = _bannerSubtitle.asStateFlow()

    private var bannerJob: Job? = null

    // Milestone Announcements (e.g. "New Letter from Farabe!")
    private val _milestoneEvent = MutableSharedFlow<String>()
    val milestoneEvent: SharedFlow<String> = _milestoneEvent.asSharedFlow()

    // Active Game Mode
    val activeGameMode = MutableStateFlow("CLASSIC")
    val isLevelCompleted = MutableStateFlow(false)
    val lastClearedBurst = MutableStateFlow(0)
    val isPlayingVoice = MutableStateFlow(false)

    // Live Telemetry Session Tracking & Detailed Event Timeline
    private var sessionActiveSeconds: Int = 0
    private var currentRoundSeconds: Int = 0
    private var isCurrentlyInGame: Boolean = false
    private var isCurrentlyPaused: Boolean = false
    private var isAppInForeground: Boolean = true
    private var currentScreenName: String = "Home Hub (Home)"
    private val recentEvents = ArrayDeque<String>()
    private val hardwareDeviceId: String = try {
        android.provider.Settings.Secure.getString(
            application.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )?.takeIf { it.isNotBlank() } ?: "dev_${android.os.Build.MODEL.replace(" ", "_")}"
    } catch (_: Throwable) {
        "dev_${android.os.Build.MODEL.replace(" ", "_")}"
    }
    private val deviceIdFallback: String get() = hardwareDeviceId

    // Daily surprise message
    val dailyMessage = MutableStateFlow<String?>(null)

    // Current candidate drop coordinate
    private var candidatePlacement: Pair<Int, Int>? = null

    init {
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                _userSettings.value = settings
                soundManager.sfxVolume = settings.soundEffectsVolume
                soundManager.musicVolume = settings.musicVolume
                soundManager.vibrationEnabled = settings.isVibrationEnabled
                if (settings.musicVolume > 0.05f) {
                    soundManager.startBackgroundMusic()
                } else {
                    soundManager.stopBackgroundMusic()
                }
            }
        }

        viewModelScope.launch {
            val saved = repository.loadActiveGame()
            if (saved != null) {
                val (savedState, mode) = saved
                engine.restoreGame(
                    board = savedState.board,
                    trayPieces = savedState.trayPieces,
                    score = savedState.score,
                    highScore = maxOf(savedState.highScore, _userSettings.value.allTimeHighScore),
                    comboStreak = savedState.comboStreak,
                    moveCount = savedState.moveCount,
                    linesClearedTotal = savedState.linesClearedTotal
                )
                activeGameMode.value = mode
                _gameState.value = engine.currentState
            } else {
                engine.startNewGame(initialHighScore = _userSettings.value.allTimeHighScore)
                _gameState.value = engine.currentState
            }

            dailyMessage.value = repository.checkDailySurprise()

            // Schedule periodic background sync & sync immediately on startup (safe offline)
            UpdateSyncWorker.schedule(getApplication())
            syncRemoteConfig(isManual = false)
            checkForUpdate()
        }

        // Real-time Active Player Heartbeat loop (every 3 seconds while app is open)
        viewModelScope.launch {
            delay(1000)
            logPlayerEvent("🟢 Opened Game App (Home Hub)")
            pushTelemetryNow()
            while (true) {
                delay(3000)
                if (isAppInForeground) {
                    sessionActiveSeconds += 3
                    if (isCurrentlyInGame && !isCurrentlyPaused && !_gameState.value.isGameOver && !isLevelCompleted.value) {
                        currentRoundSeconds += 3
                    }
                    preferencesManager.recordActivePlayTime(3, deviceIdFallback)
                    pushTelemetryNow()
                }
            }
        }
    }

    private fun logPlayerEvent(description: String) {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        synchronized(recentEvents) {
            recentEvents.addFirst("$timeStr — $description")
            while (recentEvents.size > 20) {
                recentEvents.removeLast()
            }
        }
    }

    fun onAppForegroundChanged(inForeground: Boolean) {
        isAppInForeground = inForeground
        logPlayerEvent(if (inForeground) "🟢 Resumed App (Foreground)" else "⚪ Minimized App (Background)")
        viewModelScope.launch {
            pushTelemetryNow()
        }
    }

    fun updateCurrentScreenActivity(label: String) {
        currentScreenName = label
        if (!label.startsWith("Playing")) {
            isCurrentlyInGame = false
            isCurrentlyPaused = false
        }
        logPlayerEvent("📍 Screen -> $label")
        viewModelScope.launch {
            pushTelemetryNow()
        }
    }

    fun onPauseStateChanged(paused: Boolean) {
        if (!isCurrentlyInGame || isCurrentlyPaused == paused) return
        isCurrentlyPaused = paused
        val stg = getLevelStage(activeGameMode.value)
        val modeLabel = if (stg != null) "Level ${stg.levelNumber}: ${stg.title}" else activeGameMode.value
        val score = _gameState.value.score
        currentScreenName = if (paused) "Paused in $modeLabel (Score: $score)" else "Playing $modeLabel"
        logPlayerEvent(if (paused) "⏸️ Paused $modeLabel at Score $score" else "▶️ Resumed $modeLabel")
        viewModelScope.launch {
            pushTelemetryNow()
        }
    }

    fun onExitGameToHome() {
        if (!isCurrentlyInGame) return
        val stg = getLevelStage(activeGameMode.value)
        val modeLabel = if (stg != null) "Level ${stg.levelNumber}: ${stg.title}" else activeGameMode.value
        val score = _gameState.value.score
        isCurrentlyInGame = false
        isCurrentlyPaused = false
        currentScreenName = "Home Hub (Home)"
        logPlayerEvent("🚪 Exited $modeLabel -> Home Hub (Played ${currentRoundSeconds}s, Score: $score)")
        viewModelScope.launch {
            pushTelemetryNow()
        }
    }

    fun updateFamilyPlayerName(newName: String) {
        val clean = newName.trim().ifBlank { "Mom" }
        viewModelScope.launch {
            preferencesManager.updateMomName(clean)
            logPlayerEvent("👨‍👩‍👧‍👦 Switched Family Player to: $clean")
            lastSyncBanner.value = "❤️ Active Player: $clean"
            delay(150)
            pushTelemetryNow()
        }
    }

    private suspend fun pushTelemetryNow() {
        val s = _userSettings.value
        val g = _gameState.value
        val nameSlug = s.momName.trim().lowercase().replace(Regex("[^a-z0-9]"), "_").ifBlank { "mom" }
        val pid = "${hardwareDeviceId}_$nameSlug"
        val devModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})"
        val stage = getLevelStage(activeGameMode.value)

        val detailedActivity = when {
            !isAppInForeground -> "⚪ App in Background ($currentScreenName)"
            isCurrentlyInGame && isLevelCompleted.value -> "🏆 Won ${stage?.let { "Lv.${it.levelNumber} ${it.title}" } ?: activeGameMode.value} (Score: ${g.score})"
            isCurrentlyInGame && g.isGameOver -> "💔 Game Over in ${activeGameMode.value} (Score: ${g.score})"
            isCurrentlyInGame && isCurrentlyPaused -> "⏸️ Paused in ${stage?.let { "Lv.${it.levelNumber} ${it.title}" } ?: activeGameMode.value} (Score: ${g.score})"
            isCurrentlyInGame && stage != null -> "🎮 Playing Lv.${stage.levelNumber}: ${stage.title} (${g.score}/${stage.targetScore} pts • ${g.linesClearedTotal}/${stage.targetLines} lines)"
            isCurrentlyInGame -> "🎮 Playing ${activeGameMode.value} (Score: ${g.score} • Lines: ${g.linesClearedTotal})"
            else -> "🏠 $currentScreenName"
        }

        val eventsArr = org.json.JSONArray()
        synchronized(recentEvents) {
            for (ev in recentEvents) eventsArr.put(ev)
        }
        val completedArr = org.json.JSONArray()
        for (lvl in s.completedLevels.sorted()) completedArr.put(lvl)
        val claimedTasksArr = org.json.JSONArray()
        for (tid in s.claimedTaskIds) claimedTasksArr.put(tid)

        val json = org.json.JSONObject().apply {
            put("playerId", pid)
            put("playerName", s.momName)
            put("deviceModel", devModel)
            put("appVersion", "1.2.1")
            put("currentActivity", detailedActivity)
            put("screenState", currentScreenName)
            put("isInGame", isCurrentlyInGame)
            put("isPaused", isCurrentlyPaused)
            put("isLevelCompleted", isLevelCompleted.value)
            put("isGameOver", g.isGameOver)
            put("gameMode", activeGameMode.value)
            put("currentLevelNumber", stage?.levelNumber ?: 0)
            put("currentLevelTitle", stage?.title ?: activeGameMode.value)
            put("levelTargetScore", stage?.targetScore ?: 0)
            put("levelTargetLines", stage?.targetLines ?: 0)
            put("currentScore", g.score)
            put("linesInCurrentRound", g.linesClearedTotal)
            put("movesInCurrentRound", g.moveCount)
            put("currentRoundSeconds", currentRoundSeconds)
            put("highScore", maxOf(g.highScore, s.allTimeHighScore))
            put("coins", s.coins)
            put("hearts", s.hearts)
            put("gems", s.gems)
            put("farabeHelperLevel", s.farabeHelperLevel)
            put("kittenHelperLevel", s.kittenHelperLevel)
            put("highestUnlockedLevel", s.highestUnlockedLevel)
            put("completedLevelsCount", s.completedLevels.size)
            put("completedLevels", completedArr)
            put("claimedGiftsCount", s.claimedGiftsCount)
            put("lastClaimedGiftId", s.lastClaimedGiftId)
            put("lastClaimedGiftTitle", s.lastClaimedGiftTitle)
            put("claimedTasks", claimedTasksArr)
            put("totalGamesPlayed", s.totalGamesPlayed)
            put("totalLinesCleared", s.totalLinesClearedAllTime)
            put("sessionActiveSeconds", sessionActiveSeconds)
            put("totalPlayTimeSeconds", s.totalPlayTimeSeconds)
            put("isForeground", isAppInForeground)
            put("recentEvents", eventsArr)
        }.toString()

        val respStr = remoteConfigManager.sendTelemetryHeartbeat(json)
        if (!respStr.isNullOrBlank()) {
            handleHeartbeatResponseCommands(respStr)
        }
    }

    private suspend fun handleHeartbeatResponseCommands(respStr: String) {
        try {
            val obj = org.json.JSONObject(respStr)
            val cmd = obj.optJSONObject("pendingCommand") ?: return
            val cmdId = cmd.optString("commandId", "")
            if (cmdId.isBlank() || cmdId == _userSettings.value.lastExecutedCommandId) return

            preferencesManager.markCommandExecuted(cmdId)
            val action = cmd.optString("action", "")
            when (action) {
                "SET_LEVEL" -> {
                    val targetLvl = cmd.optInt("level", 1).coerceIn(1, 12)
                    val launchNow = cmd.optBoolean("launchNow", false)
                    preferencesManager.setUnlockedLevelFromAdmin(targetLvl)
                    soundManager.playUnlockReward()
                    logPlayerEvent("👑 Admin unlocked/set Level $targetLvl")
                    lastSyncBanner.value = "👑 Farabe unlocked Challenge Level $targetLvl for you!"
                    if (launchNow) {
                        startNewGame("LEVEL_$targetLvl")
                    }
                }
                "SEND_GIFT" -> {
                    val title = cmd.optString("title", "Personal Gift from Farabe! 🎁")
                    val msg = cmd.optString("message", "আম্মু ও পরিবারের জন্য বিশেষ উপহার ❤️")
                    val coins = cmd.optInt("coins", 300)
                    val hearts = cmd.optInt("hearts", 3)
                    pendingGiftToClaim.value = RemoteGift(
                        giftId = "direct_$cmdId",
                        title = title,
                        messageBengali = msg,
                        coinsReward = coins,
                        heartsReward = hearts
                    )
                    soundManager.playUnlockReward()
                    logPlayerEvent("🎁 Received Direct Gift: $title (+$coins 🪙)")
                }
                "ADD_RESOURCES" -> {
                    val cDelta = cmd.optInt("coinsDelta", 0)
                    val hDelta = cmd.optInt("heartsDelta", 0)
                    val gDelta = cmd.optInt("gemsDelta", 0)
                    preferencesManager.addCoinsAndHearts(cDelta, hDelta, gDelta)
                    soundManager.playUnlockReward()
                    logPlayerEvent("✨ Admin Boost: +$cDelta 🪙, +$hDelta ❤️, +$gDelta 💎")
                    lastSyncBanner.value = "✨ Farabe sent +$cDelta 🪙, +$hDelta ❤️, +$gDelta 💎!"
                }
                "FORCE_SYNC" -> {
                    syncRemoteConfig(isManual = true)
                }
            }
        } catch (_: Exception) {
        }
    }

    // --- Live Game Studio Sync & Gift Handlers ---

    fun syncRemoteConfig(isManual: Boolean = false) {
        if (isSyncingRemote.value) return
        viewModelScope.launch {
            isSyncingRemote.value = true
            try {
                val outcome = remoteConfigManager.syncNow(postNotificationIfUpdated = true)
                if (outcome.songUpdated && _userSettings.value.musicVolume > 0.05f) {
                    soundManager.reloadBackgroundMusic()
                }
                val gift = outcome.config.activeGift
                if (gift != null && gift.giftId.isNotBlank() && gift.giftId != _userSettings.value.lastClaimedGiftId) {
                    pendingGiftToClaim.value = gift
                }
                if (isManual || outcome.hasNewContent) {
                    lastSyncBanner.value = outcome.message
                }
            } finally {
                isSyncingRemote.value = false
            }
        }
    }

    fun claimActiveGift() {
        val gift = pendingGiftToClaim.value ?: remoteConfig.value.activeGift ?: return
        if (gift.giftId.isBlank()) {
            pendingGiftToClaim.value = null
            return
        }
        viewModelScope.launch {
            preferencesManager.addCoinsAndHearts(
                coinsDelta = gift.coinsReward,
                heartsDelta = gift.heartsReward,
                gemsDelta = 1
            )
            preferencesManager.markGiftClaimed(gift.giftId, gift.title)
            soundManager.playUnlockReward()
            pendingGiftToClaim.value = null
            logPlayerEvent("🎁 Claimed Gift: ${gift.title} (+${gift.coinsReward} 🪙, +${gift.heartsReward} ❤️)")
            lastSyncBanner.value = "🎁 Claimed +${gift.coinsReward} coins & +${gift.heartsReward} ❤️!"
            pushTelemetryNow()
        }
    }

    fun dismissPendingGift() {
        pendingGiftToClaim.value = null
    }

    fun clearSyncBanner() {
        lastSyncBanner.value = null
    }

    // --- App Updater Handlers ---

    fun checkForUpdate(onManualResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val update = appUpdater.checkForUpdate()
            availableUpdate.value = update
            onManualResult?.invoke(update != null)
        }
    }

    fun startUpdateDownload() {
        val update = availableUpdate.value ?: return
        if (isDownloadingUpdate.value) return
        isDownloadingUpdate.value = true
        downloadProgress.value = 0f
        viewModelScope.launch {
            appUpdater.downloadAndInstall(
                updateInfo = update,
                onProgress = { progress ->
                    downloadProgress.value = progress
                },
                onError = { error ->
                    isDownloadingUpdate.value = false
                    viewModelScope.launch {
                        _milestoneEvent.emit("Update failed: $error")
                    }
                }
            )
        }
    }

    fun dismissUpdateDialog() {
        availableUpdate.value = null
        isDownloadingUpdate.value = false
    }

    fun setBoardCoordinates(coordinates: LayoutCoordinates) {
        boardCoordinates = coordinates
    }

    // --- Drag Gesture Handlers ---

    fun onDragStart(trayIndex: Int, touchPos: Offset) {
        selectedPieceIndex.value = null // Cancel any tap selection
        activeDraggingIndex.value = trayIndex
        dragTouchPosition.value = touchPos
        candidatePlacement = null
        soundManager.playPiecePickup()
        updateCandidateFromTouch(trayIndex, touchPos)
    }

    fun onDrag(trayIndex: Int, touchPos: Offset) {
        dragTouchPosition.value = touchPos
        updateCandidateFromTouch(trayIndex, touchPos)
    }

    private fun updateCandidateFromTouch(trayIndex: Int, touchPos: Offset) {
        val coords = boardCoordinates ?: return
        val boardPos = coords.positionInRoot()
        val boardSize = coords.size

        val board = engine.currentState.board
        val cellW = boardSize.width.toFloat() / board.cols
        val cellH = boardSize.height.toFloat() / board.rows

        // Lift offset in pixels so Mom's finger is directly below the piece
        val touchLiftPx = cellH * 2.2f

        val pieceCenterX = touchPos.x
        val pieceCenterY = touchPos.y - touchLiftPx

        val relX = pieceCenterX - boardPos.x
        val relY = pieceCenterY - boardPos.y

        val shape = engine.currentState.trayPieces.getOrNull(trayIndex) ?: return

        // Compute anchor (row, col)
        val anchorCol = ((relX - ((shape.width - 1) * cellW * 0.5f)) / cellW).roundToInt()
        val anchorRow = ((relY - ((shape.height - 1) * cellH * 0.5f)) / cellH).roundToInt()

        if (engine.canPlace(trayIndex, anchorRow, anchorCol)) {
            candidatePlacement = Pair(anchorRow, anchorCol)
            candidateAnchor.value = candidatePlacement
            candidateShapePoints.value = shape.points
            // Create ghost overlay
            val overlay = shape.points.associate { pt ->
                Pair(anchorRow + pt.y, anchorCol + pt.x) to shape.color
            }
            ghostCells.value = overlay

            val (rows, cols) = engine.previewGhostLines(trayIndex, anchorRow, anchorCol)
            willClearRows.value = rows
            willClearCols.value = cols
        } else {
            candidatePlacement = null
            candidateAnchor.value = null
            candidateShapePoints.value = null
            ghostCells.value = emptyMap()
            willClearRows.value = emptySet()
            willClearCols.value = emptySet()
        }
    }

    fun onDragEnd(trayIndex: Int) {
        val candidate = candidatePlacement
        activeDraggingIndex.value = null
        candidateAnchor.value = null
        candidateShapePoints.value = null

        if (candidate != null && engine.canPlace(trayIndex, candidate.first, candidate.second)) {
            executePlacement(trayIndex, candidate.first, candidate.second)
        } else {
            soundManager.playInvalidMove()
            ghostCells.value = emptyMap()
            willClearRows.value = emptySet()
            willClearCols.value = emptySet()
            candidatePlacement = null
        }
    }

    fun onDragCancel(trayIndex: Int) {
        activeDraggingIndex.value = null
        candidateAnchor.value = null
        candidateShapePoints.value = null
        ghostCells.value = emptyMap()
        willClearRows.value = emptySet()
        willClearCols.value = emptySet()
        candidatePlacement = null
    }

    // --- Tap-to-Place Accessibility Handlers ---

    fun onPieceTapped(trayIndex: Int) {
        if (engine.currentState.trayPieces.getOrNull(trayIndex) == null) return

        if (selectedPieceIndex.value == trayIndex) {
            // Deselect
            selectedPieceIndex.value = null
            ghostCells.value = emptyMap()
            willClearRows.value = emptySet()
            willClearCols.value = emptySet()
        } else {
            selectedPieceIndex.value = trayIndex
            soundManager.playPiecePickup()
        }
    }

    fun onBoardCellTapped(targetRow: Int, targetCol: Int) {
        val selectedIdx = selectedPieceIndex.value ?: return
        val shape = engine.currentState.trayPieces.getOrNull(selectedIdx) ?: return

        // Attempt anchor placement at (targetRow, targetCol)
        if (engine.canPlace(selectedIdx, targetRow, targetCol)) {
            executePlacement(selectedIdx, targetRow, targetCol)
            selectedPieceIndex.value = null
        } else {
            soundManager.playInvalidMove()
        }
    }

    private fun executePlacement(trayIndex: Int, row: Int, col: Int) {
        val result = engine.placePiece(trayIndex, row, col)
        _gameState.value = engine.currentState

        // Clear preview
        ghostCells.value = emptyMap()
        willClearRows.value = emptySet()
        willClearCols.value = emptySet()
        candidatePlacement = null

        if (result.success) {
            soundManager.playPieceDrop()

            // Spawn floating score text
            val gained = result.scoreBreakdown?.totalPointsGained ?: 0
            if (gained > 0) {
                spawnFloatingScore("+$gained", row, col)
            }

            // Handle line clearing feedback
            val clearRes = result.lineClearResult
            val combo = result.comboEvent
            val helperBonusPerLine = (_userSettings.value.farabeHelperLevel + _userSettings.value.kittenHelperLevel) * 3

            if (clearRes != null && clearRes.hasClears) {
                clearingCells.value = clearRes.clearedCells
                lastClearedBurst.value = lastClearedBurst.value + 1
                soundManager.playLineClear(clearRes.totalLinesCleared)

                viewModelScope.launch {
                    val coinsEarned = clearRes.totalLinesCleared * (15 + helperBonusPerLine)
                    preferencesManager.addCoinsAndHearts(coinsDelta = coinsEarned)
                    preferencesManager.recordMoveStats(
                        linesDelta = clearRes.totalLinesCleared,
                        comboIncrement = if (combo != null && combo.isComboActive) 1 else 0
                    )
                    delay(260)
                    clearingCells.value = emptySet()
                }
            }

            // Handle combo feedback with warm Bangladeshi Bengali praise
            if (combo != null && combo.isComboActive) {
                soundManager.playCombo(combo.streak)
                if (combo.streak >= 3) {
                    playComboVoice()
                }
                val bengaliTitle = when (combo.streak) {
                    2 -> "COMBO x2! দারুণ আম্মু! 🌸"
                    3 -> "COMBO x3! অসাধারণ মা! ✨"
                    4 -> "SUPER COMBO! সাবাশ আম্মু! 🔥"
                    5 -> "UNBELIEVABLE! তুমি সেরা মা! ❤️"
                    else -> "LEGENDARY x${combo.streak}! বিশ্বসেরা আম্মু! 🌟"
                }
                showBanner(bengaliTitle, combo.feedbackMessage)
            } else if (clearRes != null && clearRes.totalLinesCleared > 1) {
                showBanner("MULTI-CLEAR! চমৎকার!", "+${result.scoreBreakdown?.linePoints} Points!")
            }

            val stage = getLevelStage()
            if (stage != null && !isLevelCompleted.value &&
                (engine.currentState.score >= stage.targetScore || engine.currentState.linesClearedTotal >= stage.targetLines)
            ) {
                isLevelCompleted.value = true
                soundManager.playUnlockReward()
                playWinVoice()
                logPlayerEvent("🏆 WON Level ${stage.levelNumber}: ${stage.title}! (Score: ${engine.currentState.score}, Unlocked Lv.${(stage.levelNumber + 1).coerceAtMost(12)})")
                showBanner(
                    "LEVEL ${stage.levelNumber} WON! 🎉🌟",
                    "অভিনন্দন আম্মু! Level ${(stage.levelNumber + 1).coerceAtMost(12)} Unlocked! (+${stage.rewardCoins} 🪙)"
                )
                viewModelScope.launch {
                    preferencesManager.markLevelCompleted(stage.levelNumber, stage.rewardCoins)
                    repository.clearActiveSave()
                    pushTelemetryNow()
                }
            } else if (clearRes != null && clearRes.hasClears) {
                logPlayerEvent("🔥 Cleared ${clearRes.totalLinesCleared} line(s) in ${activeGameMode.value} (Score: ${engine.currentState.score})")
            }

            // Auto save & milestone checks
            viewModelScope.launch {
                if (!isLevelCompleted.value) {
                    repository.saveActiveGame(engine.currentState, activeGameMode.value)
                }
                pushTelemetryNow()
                val newMilestones = repository.checkAndUnlockMilestones(
                    engine.currentState.score,
                    engine.currentState.linesClearedTotal
                )
                for (msg in newMilestones) {
                    soundManager.playUnlockReward()
                    _milestoneEvent.emit(msg)
                }
            }

            // Check game over -> trigger Farabe's Win/Lose Voice Note cleanly & update life/coins
            if (result.isGameOver) {
                soundManager.playGameOver()
                val isNewBest = engine.currentState.score >= engine.currentState.highScore && engine.currentState.score > 0
                logPlayerEvent("💔 Game Over in ${activeGameMode.value} (Final Score: ${engine.currentState.score}, Played ${currentRoundSeconds}s)")
                if (isNewBest || isLevelCompleted.value) {
                    playWinVoice()
                    viewModelScope.launch {
                        preferencesManager.addCoinsAndHearts(coinsDelta = 50, heartsDelta = 1, gemsDelta = 1)
                        pushTelemetryNow()
                    }
                } else {
                    playLoseVoice()
                    viewModelScope.launch {
                        val heartDelta = if (activeGameMode.value == "RELAX") 0 else -1
                        preferencesManager.addCoinsAndHearts(coinsDelta = 20, heartsDelta = heartDelta)
                        pushTelemetryNow()
                    }
                }
                viewModelScope.launch {
                    repository.clearActiveSave()
                }
            }
        }
    }

    private fun spawnFloatingScore(text: String, row: Int, col: Int) {
        val coords = boardCoordinates ?: return
        val pos = coords.positionInRoot()
        val size = coords.size
        val cellW = size.width.toFloat() / 8f
        val cellH = size.height.toFloat() / 8f

        val sx = pos.x + (col * cellW) + (cellW * 0.2f)
        val sy = pos.y + (row * cellH)

        val item = FloatingScore(
            id = ++scoreCounter,
            text = text,
            startX = sx,
            startY = sy,
            color = GoldAccent
        )

        floatingScores.value = floatingScores.value + item
        viewModelScope.launch {
            delay(1200)
            floatingScores.value = floatingScores.value.filter { it.id != item.id }
        }
    }

    private fun showBanner(title: String?, subtitle: String?) {
        bannerJob?.cancel()
        bannerJob = viewModelScope.launch {
            _bannerTitle.value = title
            _bannerSubtitle.value = subtitle
            delay(1500)
            _bannerTitle.value = null
            _bannerSubtitle.value = null
        }
    }

    fun startNewGame(mode: String = "CLASSIC") {
        soundManager.stopVoice()
        isPlayingVoice.value = false
        isLevelCompleted.value = false
        isCurrentlyInGame = true
        isCurrentlyPaused = false
        currentRoundSeconds = 0
        activeGameMode.value = mode
        selectedPieceIndex.value = null
        activeDraggingIndex.value = null
        ghostCells.value = emptyMap()
        willClearRows.value = emptySet()
        willClearCols.value = emptySet()

        val stg = getLevelStage(mode)
        val modeLabel = if (stg != null) "Level ${stg.levelNumber}: ${stg.title}" else mode
        logPlayerEvent("🎮 Entered Game -> $modeLabel")

        engine.startNewGame(initialHighScore = _userSettings.value.allTimeHighScore)
        _gameState.value = engine.currentState
        viewModelScope.launch {
            repository.clearActiveSave()
            repository.saveActiveGame(engine.currentState, mode)
            preferencesManager.incrementGamesPlayed()
            currentScreenName = "Playing $modeLabel"
            pushTelemetryNow()
        }
    }

    fun onShowcasePoked() {
        soundManager.playPiecePickup()
        viewModelScope.launch {
            preferencesManager.addCoinsAndHearts(coinsDelta = 5)
        }
    }

    fun getLevelStage(mode: String = activeGameMode.value): LevelStage? {
        if (!mode.startsWith("LEVEL_")) return null
        val num = mode.removePrefix("LEVEL_").toIntOrNull() ?: return null
        return HANDCRAFTED_LEVELS.firstOrNull { it.levelNumber == num }
    }

    fun getLevelTargetScore(mode: String = activeGameMode.value): Int? {
        return getLevelStage(mode)?.targetScore
    }

    fun startNextChallengeLevel() {
        val currentStage = getLevelStage()
        val nextNum = if (currentStage != null) {
            (currentStage.levelNumber + 1).coerceAtMost(HANDCRAFTED_LEVELS.size)
        } else {
            _userSettings.value.highestUnlockedLevel.coerceAtMost(HANDCRAFTED_LEVELS.size)
        }
        startNewGame("LEVEL_$nextNum")
    }

    fun refillMomHeartsFree() {
        soundManager.playUnlockReward()
        viewModelScope.launch {
            preferencesManager.addCoinsAndHearts(coinsDelta = 0, heartsDelta = 5)
            lastSyncBanner.value = "❤️ Farabe refilled +5 Hearts for Mom!"
        }
    }

    fun claimTask(task: RemoteTask) {
        viewModelScope.launch {
            preferencesManager.markTaskClaimed(
                taskId = task.id,
                rewardCoins = task.rewardCoins,
                rewardGems = task.rewardGems,
                rewardHearts = 1
            )
            soundManager.playUnlockReward()
            lastSyncBanner.value = "🎉 Claimed +${task.rewardCoins} 🪙, +${task.rewardGems} 💎 & +1 ❤️ from ${task.helperName}!"
        }
    }

    fun upgradeHelper(isFarabe: Boolean) {
        viewModelScope.launch {
            val ok = preferencesManager.upgradeHelper(isFarabe, gemCost = 2)
            if (ok) {
                soundManager.playUnlockReward()
                val who = if (isFarabe) "Farabe ❤️" else "Misti Kitten 🐾"
                lastSyncBanner.value = "🌟 Upgraded $who! Extra coin bonus unlocked!"
            } else {
                soundManager.playInvalidMove()
                lastSyncBanner.value = "💎 Need 2 Gems to upgrade! Complete tasks or levels to earn Gems."
            }
        }
    }

    fun hasEndGameVoice(isWin: Boolean): Boolean {
        val v = remoteConfig.value.voice
        return if (isWin) {
            v.enableWinVoice && remoteConfigManager.cachedWinVoiceFile.exists() && remoteConfigManager.cachedWinVoiceFile.length() > 0L
        } else {
            v.enableLoseVoice && remoteConfigManager.cachedLoseVoiceFile.exists() && remoteConfigManager.cachedLoseVoiceFile.length() > 0L
        }
    }

    fun replayEndGameVoice(isWin: Boolean) {
        if (isWin) playWinVoice() else playLoseVoice()
    }

    fun playWinVoice() {
        val v = remoteConfig.value.voice
        if (!v.enableWinVoice) return
        val file = remoteConfigManager.cachedWinVoiceFile
        if (file.exists() && file.length() > 0L) {
            isPlayingVoice.value = true
            soundManager.playVoiceClip(file, duckMusic = v.duckMusicWhileSpeaking) {
                isPlayingVoice.value = false
            }
        }
    }

    fun playLoseVoice() {
        val v = remoteConfig.value.voice
        if (!v.enableLoseVoice) return
        val file = remoteConfigManager.cachedLoseVoiceFile
        if (file.exists() && file.length() > 0L) {
            isPlayingVoice.value = true
            soundManager.playVoiceClip(file, duckMusic = v.duckMusicWhileSpeaking) {
                isPlayingVoice.value = false
            }
        }
    }

    fun playComboVoice() {
        val v = remoteConfig.value.voice
        if (!v.enableComboVoice) return
        val file = remoteConfigManager.cachedComboVoiceFile
        if (file.exists() && file.length() > 0L) {
            isPlayingVoice.value = true
            soundManager.playVoiceClip(file, duckMusic = v.duckMusicWhileSpeaking) {
                isPlayingVoice.value = false
            }
        }
    }

    fun undo() {
        if (engine.undo()) {
            _gameState.value = engine.currentState
            selectedPieceIndex.value = null
            soundManager.playPiecePickup()
            viewModelScope.launch {
                repository.saveActiveGame(engine.currentState, activeGameMode.value)
            }
        }
    }

    val canUndo: Boolean
        get() = engine.canUndo

    fun shuffleTray() {
        if (engine.shuffleTray()) {
            _gameState.value = engine.currentState
            selectedPieceIndex.value = null
            soundManager.playPiecePickup()
            viewModelScope.launch {
                repository.saveActiveGame(engine.currentState, activeGameMode.value)
            }
        }
    }

    fun requestHint() {
        val hint = engine.findHint()
        if (hint != null) {
            val (trayIdx, row, col) = hint
            val shape = engine.currentState.trayPieces.getOrNull(trayIdx) ?: return
            val hintMap = shape.points.associate { pt ->
                Pair(row + pt.y, col + pt.x) to shape.color
            }
            ghostCells.value = hintMap
            val (rows, cols) = engine.previewGhostLines(trayIdx, row, col)
            willClearRows.value = rows
            willClearCols.value = cols
            selectedPieceIndex.value = trayIdx
            soundManager.playPiecePickup()

            viewModelScope.launch {
                delay(2000)
                if (activeDraggingIndex.value == null) {
                    ghostCells.value = emptyMap()
                    willClearRows.value = emptySet()
                    willClearCols.value = emptySet()
                }
            }
        }
    }

    fun updateSfxVolume(volume: Float) {
        viewModelScope.launch {
            repository.preferencesManager.updateSfxVolume(volume)
        }
    }

    fun updateMusicVolume(volume: Float) {
        viewModelScope.launch {
            repository.preferencesManager.updateMusicVolume(volume)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
