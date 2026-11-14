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
import com.farabe.farabesmomblock.remote.UpdateSyncWorker
import com.farabe.farabesmomblock.ui.components.FloatingScore
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
            preferencesManager.markGiftClaimed(gift.giftId)
            soundManager.playUnlockReward()
            pendingGiftToClaim.value = null
            lastSyncBanner.value = "🎁 Claimed +${gift.coinsReward} coins & +${gift.heartsReward} ❤️!"
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
            if (clearRes != null && clearRes.hasClears) {
                clearingCells.value = clearRes.clearedCells
                lastClearedBurst.value = lastClearedBurst.value + 1
                soundManager.playLineClear(clearRes.totalLinesCleared)

                viewModelScope.launch {
                    val coinsEarned = clearRes.totalLinesCleared * 15
                    preferencesManager.addCoinsAndHearts(coinsDelta = coinsEarned)
                    delay(260)
                    clearingCells.value = emptySet()
                }
            }

            // Handle combo feedback with warm Bangladeshi Bengali praise
            val combo = result.comboEvent
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

            val levelTarget = getLevelTargetScore()
            if (levelTarget != null && !isLevelCompleted.value && engine.currentState.score >= levelTarget) {
                isLevelCompleted.value = true
                soundManager.playUnlockReward()
                playWinVoice()
                showBanner("LEVEL COMPLETE! 🎉🌟", "অভিনন্দন আম্মু! Target $levelTarget Reached!")
                viewModelScope.launch {
                    preferencesManager.addCoinsAndHearts(coinsDelta = 100, gemsDelta = 1)
                }
            }

            // Auto save & milestone checks
            viewModelScope.launch {
                repository.saveActiveGame(engine.currentState, activeGameMode.value)
                val newMilestones = repository.checkAndUnlockMilestones(
                    engine.currentState.score,
                    engine.currentState.linesClearedTotal
                )
                for (msg in newMilestones) {
                    soundManager.playUnlockReward()
                    _milestoneEvent.emit(msg)
                }
            }

            // Check game over -> trigger Farabe's Win/Lose Voice Note cleanly
            if (result.isGameOver) {
                soundManager.playGameOver()
                val isNewBest = engine.currentState.score >= engine.currentState.highScore && engine.currentState.score > 0
                if (isNewBest || isLevelCompleted.value) {
                    playWinVoice()
                } else {
                    playLoseVoice()
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
        activeGameMode.value = mode
        selectedPieceIndex.value = null
        activeDraggingIndex.value = null
        ghostCells.value = emptyMap()
        willClearRows.value = emptySet()
        willClearCols.value = emptySet()

        engine.startNewGame(initialHighScore = _userSettings.value.allTimeHighScore)
        _gameState.value = engine.currentState
        viewModelScope.launch {
            repository.clearActiveSave()
            repository.saveActiveGame(engine.currentState, mode)
            preferencesManager.incrementGamesPlayed()
        }
    }

    fun onShowcasePoked() {
        soundManager.playPiecePickup()
        viewModelScope.launch {
            preferencesManager.addCoinsAndHearts(coinsDelta = 5)
        }
    }

    fun getLevelTargetScore(mode: String = activeGameMode.value): Int? {
        return when (mode) {
            "LEVEL_1" -> 300
            "LEVEL_2" -> 600
            "LEVEL_3" -> 1000
            "LEVEL_4" -> 1500
            "LEVEL_5" -> 2200
            "LEVEL_6" -> 3500
            else -> null
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
