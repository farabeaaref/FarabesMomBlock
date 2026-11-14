package com.farabe.farabesmomblock.ui.screens

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.farabe.farabesmomblock.ui.components.AmbientPetalsCanvas
import com.farabe.farabesmomblock.ui.components.BoardView
import com.farabe.farabesmomblock.ui.components.ComboBanner
import com.farabe.farabesmomblock.ui.components.CuteMascot
import com.farabe.farabesmomblock.ui.components.DragAuraBeam
import com.farabe.farabesmomblock.ui.components.DynamicGameBackground
import com.farabe.farabesmomblock.ui.components.FairyLights
import com.farabe.farabesmomblock.ui.components.ParticleOverlay
import com.farabe.farabesmomblock.ui.components.PieceShapePreview
import com.farabe.farabesmomblock.ui.components.PieceTrayView
import com.farabe.farabesmomblock.ui.components.PowerUpBar
import com.farabe.farabesmomblock.ui.components.QuiltedWoodBackground
import com.farabe.farabesmomblock.ui.components.ScoreHeader
import com.farabe.farabesmomblock.ui.components3d.Filament3DShowcase
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.WarmCream
import com.farabe.farabesmomblock.ui.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onHomeClick: () -> Unit,
    onOpenGarden: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val ghostCells by viewModel.ghostCells.collectAsState()
    val willClearRows by viewModel.willClearRows.collectAsState()
    val willClearCols by viewModel.willClearCols.collectAsState()
    val clearingCells by viewModel.clearingCells.collectAsState()

    val candidateAnchor by viewModel.candidateAnchor.collectAsState()
    val candidateShapePoints by viewModel.candidateShapePoints.collectAsState()

    val activeDraggingIndex by viewModel.activeDraggingIndex.collectAsState()
    val dragTouchPosition by viewModel.dragTouchPosition.collectAsState()
    val selectedPieceIndex by viewModel.selectedPieceIndex.collectAsState()
    val floatingScores by viewModel.floatingScores.collectAsState()

    val bannerTitle by viewModel.bannerTitle.collectAsState()
    val bannerSubtitle by viewModel.bannerSubtitle.collectAsState()
    val remoteConfig by viewModel.remoteConfig.collectAsState()
    val mediaRevision by viewModel.mediaRevision.collectAsState()
    val remoteTheme = remoteConfig.theme

    val cachedGameBg = remember(mediaRevision) {
        viewModel.remoteConfigManager.loadCachedBitmap(viewModel.remoteConfigManager.cachedGameBgFile)
    }

    var isPaused by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        viewModel.milestoneEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = remoteTheme.surfaceColor,
                    contentColor = Color.White,
                    actionColor = remoteTheme.goldAccent,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        containerColor = remoteTheme.primaryColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Dynamic 3D/Custom Gameplay Background or Quilted Warm Wood
            if (cachedGameBg != null) {
                DynamicGameBackground(
                    customBitmap = cachedGameBg,
                    primaryTint = remoteTheme.primaryColor,
                    ambientEffect = remoteConfig.background.ambientEffect
                )
            } else {
                DynamicGameBackground(
                    customBitmap = null,
                    primaryTint = remoteTheme.primaryColor,
                    ambientEffect = remoteConfig.background.ambientEffect
                )
            }

            // 2. Festive Fairy String Lights on Left & Right
            FairyLights(isLeft = true, modifier = Modifier.align(Alignment.CenterStart))
            FairyLights(isLeft = false, modifier = Modifier.align(Alignment.CenterEnd))

            // 3. Ambient Starlight & Rose Petals
            AmbientPetalsCanvas()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Mascot Medallion, Score, Best Crown, Coins, Settings Gear)
                ScoreHeader(
                    score = gameState.score,
                    highScore = gameState.highScore,
                    comboStreak = gameState.comboStreak,
                    seedsCount = userSettings.coins,
                    onPauseClick = { isPaused = true }
                )

                // Power-Ups Toolbar (Undo, Hint, Shuffle)
                PowerUpBar(
                    canUndo = viewModel.canUndo,
                    onUndoClick = { viewModel.undo() },
                    onHintClick = { viewModel.requestHint() },
                    onShuffleClick = { viewModel.shuffleTray() }
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Board View with Glowing Neon Landing Target Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BoardView(
                        board = gameState.board,
                        ghostCells = ghostCells,
                        willClearRows = willClearRows,
                        willClearCols = willClearCols,
                        clearingCells = clearingCells,
                        candidateAnchor = candidateAnchor,
                        candidateShapePoints = candidateShapePoints,
                        onCellClick = { r, c ->
                            viewModel.onBoardCellTapped(r, c)
                        },
                        onBoardPositioned = { coords ->
                            viewModel.setBoardCoordinates(coords)
                        }
                    )

                    // Floating Score Text (+100, +300) Overlay
                    ParticleOverlay(activeScores = floatingScores)

                    // Combo celebration banner overlay
                    ComboBanner(
                        title = bannerTitle,
                        subtitle = bannerSubtitle,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Bottom 3-Piece Tray
                PieceTrayView(
                    trayPieces = gameState.trayPieces,
                    activeDraggingIndex = activeDraggingIndex,
                    selectedPieceIndex = selectedPieceIndex,
                    onPieceClick = { idx -> viewModel.onPieceTapped(idx) },
                    onDragStart = { idx, pos -> viewModel.onDragStart(idx, pos) },
                    onDrag = { idx, pos -> viewModel.onDrag(idx, pos) },
                    onDragEnd = { idx -> viewModel.onDragEnd(idx) },
                    onDragCancel = { idx -> viewModel.onDragCancel(idx) },
                    surfaceColor = remoteTheme.surfaceColor,
                    borderColor = remoteTheme.borderColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Encouragement Cloud Balloon with Mascot (matching reference)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFB6C1), Color(0xFFFF8DA1))
                            )
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .offset(y = (-4).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CuteMascot(modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ENDLESS LOVE FOR MOM! 🥰",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF5A1827),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "মায়ের জন্য ভালোবাসা • Made with love by Farabe",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF882038)
                                )
                            }
                        }

                        Text(text = "❤️", fontSize = 20.sp)
                    }
                }
            }

            // --- FULLSCREEN UNCLIPPED ROOT DRAG OVERLAY WITH FIRE BEAM ---
            if (activeDraggingIndex != null) {
                val draggingShape = gameState.trayPieces.getOrNull(activeDraggingIndex!!)
                val boardCoords = viewModel.boardCoordinates
                if (draggingShape != null && boardCoords != null) {
                    val boardWidthPx = boardCoords.size.width.toFloat()
                    val boardCellPx = boardWidthPx / 8f
                    val boardCellDp = with(density) { boardCellPx.toDp() }
                    val touchLiftPx = boardCellPx * 2.2f

                    val shapeWidthPx = draggingShape.width * boardCellPx
                    val shapeHeightPx = draggingShape.height * boardCellPx

                    val overlayX = dragTouchPosition.x - (shapeWidthPx * 0.5f)
                    val overlayY = dragTouchPosition.y - touchLiftPx - (shapeHeightPx * 0.5f)

                    Box(
                        modifier = Modifier
                            .zIndex(999f)
                            .offset {
                                IntOffset(
                                    overlayX.roundToInt(),
                                    overlayY.roundToInt()
                                )
                            }
                    ) {
                        // 1. Rising Fire/Light Beam above the piece (exact match to reference screenshot)
                        Box(
                            modifier = Modifier
                                .width(with(density) { shapeWidthPx.toDp() })
                                .height(90.dp)
                                .offset(y = (-80).dp)
                        ) {
                            DragAuraBeam(beamColor = Color(draggingShape.color.primaryHex))
                        }

                        // 2. The 3D Gummy Piece
                        PieceShapePreview(
                            shape = draggingShape,
                            cellSize = boardCellDp,
                            showMotifs = true
                        )
                    }
                }
            }

            // Pause Dialog
            if (isPaused) {
                PauseDialog(
                    onResume = { isPaused = false },
                    onRestart = {
                        isPaused = false
                        viewModel.startNewGame()
                    },
                    onHome = {
                        isPaused = false
                        onHomeClick()
                    },
                    sfxVolume = userSettings.soundEffectsVolume,
                    musicVolume = userSettings.musicVolume,
                    onSfxVolumeChanged = { viewModel.updateSfxVolume(it) },
                    onMusicVolumeChanged = { viewModel.updateMusicVolume(it) },
                    onDismiss = { isPaused = false }
                )
            }

            // Game Over Dialog
            if (gameState.isGameOver) {
                GameOverDialog(
                    score = gameState.score,
                    highScore = gameState.highScore,
                    onReplay = {
                        viewModel.startNewGame()
                    },
                    onOpenGarden = onOpenGarden,
                    onHome = onHomeClick
                )
            }
        }
    }
}
