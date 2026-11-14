package com.farabe.farabesmomblock.engine

import java.util.ArrayDeque

/**
 * Result data class when a piece is placed.
 */
data class PlacePieceResult(
    val success: Boolean,
    val scoreBreakdown: ScoreBreakdown? = null,
    val lineClearResult: LineClearResult? = null,
    val comboEvent: ComboEvent? = null,
    val isGameOver: Boolean = false,
    val newPiecesGenerated: Boolean = false
)

/**
 * Pure Kotlin gameplay engine managing all core block puzzle rules,
 * validation, scoring, combos, tray replenishment, undo, and solvability checks.
 * Completely independent of the Android UI layer.
 */
class GameEngine(
    seed: Long? = null,
    private val boardRows: Int = 8,
    private val boardCols: Int = 8
) {
    private var generator: BlockGenerator = BlockGenerator(seed)
    private val comboManager: ComboManager = ComboManager()
    private val undoStack: ArrayDeque<GameState> = ArrayDeque()

    var currentState: GameState = GameState(
        board = Board(rows = boardRows, cols = boardCols),
        trayPieces = listOf(null, null, null)
    )
        private set

    init {
        startNewGame(seed)
    }

    /**
     * Initializes or resets the game to a clean starting state.
     */
    fun startNewGame(seed: Long? = null, initialHighScore: Int = currentState.highScore) {
        if (seed != null) {
            generator = BlockGenerator(seed)
        }
        comboManager.reset()
        undoStack.clear()

        val cleanBoard = Board(rows = boardRows, cols = boardCols)
        val initialTray = generator.generateTray(board = cleanBoard, count = 3)

        currentState = GameState(
            board = cleanBoard,
            trayPieces = initialTray,
            score = 0,
            highScore = initialHighScore,
            comboStreak = 0,
            moveCount = 0,
            linesClearedTotal = 0,
            isGameOver = false,
            lastClearResult = null,
            lastComboEvent = null,
            lastPointsGained = 0
        )
    }

    /**
     * Restores state from persistent storage.
     */
    fun restoreGame(
        board: Board,
        trayPieces: List<BlockShape?>,
        score: Int,
        highScore: Int,
        comboStreak: Int,
        moveCount: Int,
        linesClearedTotal: Int
    ) {
        comboManager.restore(comboStreak, comboStreak)
        undoStack.clear()

        val gameOver = MoveValidator.isGameOver(board, trayPieces)

        currentState = GameState(
            board = board,
            trayPieces = trayPieces,
            score = score,
            highScore = maxOf(score, highScore),
            comboStreak = comboStreak,
            moveCount = moveCount,
            linesClearedTotal = linesClearedTotal,
            isGameOver = gameOver
        )
    }

    /**
     * Checks if piece at [trayIndex] can legally be placed at ([row], [col]).
     */
    fun canPlace(trayIndex: Int, row: Int, col: Int): Boolean {
        if (trayIndex !in currentState.trayPieces.indices) return false
        val shape = currentState.trayPieces[trayIndex] ?: return false
        return MoveValidator.isValidPlacement(currentState.board, shape, row, col)
    }

    /**
     * Previews lines that would be completed if piece at [trayIndex] is placed at ([row], [col]).
     */
    fun previewGhostLines(trayIndex: Int, row: Int, col: Int): Pair<Set<Int>, Set<Int>> {
        if (trayIndex !in currentState.trayPieces.indices) return Pair(emptySet(), emptySet())
        val shape = currentState.trayPieces[trayIndex] ?: return Pair(emptySet(), emptySet())
        return LineClearManager.previewLinesIfPlaced(currentState.board, shape, row, col)
    }

    /**
     * Places the piece from [trayIndex] onto the board at ([row], [col]).
     */
    fun placePiece(trayIndex: Int, row: Int, col: Int): PlacePieceResult {
        if (!canPlace(trayIndex, row, col)) {
            return PlacePieceResult(success = false)
        }

        val shape = currentState.trayPieces[trayIndex]!!

        // Save previous state to undo stack
        if (undoStack.size >= 5) {
            undoStack.removeLast()
        }
        undoStack.push(currentState)

        // 1. Place piece on board
        val boardWithPiece = currentState.board.placeShape(shape, row, col)

        // 2. Consume piece from tray
        val updatedTray = currentState.trayPieces.toMutableList()
        updatedTray[trayIndex] = null

        // 3. Detect and clear completed lines
        val clearResult = LineClearManager.clearCompletedLines(boardWithPiece)
        val finalBoard = clearResult.newBoard

        // 4. Update combo tracking
        val comboEvent = comboManager.onMoveCompleted(clearResult.totalLinesCleared)

        // 5. Calculate score
        val isBoardNowEmpty = finalBoard.isEmpty()
        val scoreBreakdown = ScoringManager.computeScore(
            shape = shape,
            linesCleared = clearResult.totalLinesCleared,
            comboStreak = comboEvent.streak,
            isBoardNowEmpty = isBoardNowEmpty
        )

        val newScore = currentState.score + scoreBreakdown.totalPointsGained
        val newHighScore = maxOf(newScore, currentState.highScore)
        val newLinesClearedTotal = currentState.linesClearedTotal + clearResult.totalLinesCleared
        val newMoveCount = currentState.moveCount + 1

        // 6. Check if tray is empty -> replenish with 3 new pieces
        var newPiecesGenerated = false
        val piecesForNextTurn = if (updatedTray.all { it == null }) {
            newPiecesGenerated = true
            generator.generateTray(board = finalBoard, count = 3)
        } else {
            updatedTray
        }

        // 7. Check game-over condition
        val isGameOver = MoveValidator.isGameOver(finalBoard, piecesForNextTurn)

        // 8. Update active state
        currentState = GameState(
            board = finalBoard,
            trayPieces = piecesForNextTurn,
            score = newScore,
            highScore = newHighScore,
            comboStreak = comboEvent.streak,
            moveCount = newMoveCount,
            linesClearedTotal = newLinesClearedTotal,
            isGameOver = isGameOver,
            lastClearResult = clearResult,
            lastComboEvent = comboEvent,
            lastPointsGained = scoreBreakdown.totalPointsGained
        )

        return PlacePieceResult(
            success = true,
            scoreBreakdown = scoreBreakdown,
            lineClearResult = clearResult,
            comboEvent = comboEvent,
            isGameOver = isGameOver,
            newPiecesGenerated = newPiecesGenerated
        )
    }

    /**
     * Undoes the last move, reverting board, tray, score, and combo.
     */
    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        val previousState = undoStack.pop()
        currentState = previousState
        comboManager.restore(previousState.comboStreak, previousState.comboStreak)
        return true
    }

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    /**
     * Optional power-up: shuffles/rerolls remaining active pieces in tray.
     */
    fun shuffleTray(): Boolean {
        val activeCount = currentState.remainingTrayPiecesCount
        if (activeCount == 0) return false

        val freshPieces = generator.generateTray(board = currentState.board, count = activeCount)
        val newTray = currentState.trayPieces.toMutableList()
        var freshIdx = 0
        for (i in newTray.indices) {
            if (newTray[i] != null) {
                newTray[i] = freshPieces[freshIdx++]
            }
        }

        val gameOver = MoveValidator.isGameOver(currentState.board, newTray)
        currentState = currentState.copy(trayPieces = newTray, isGameOver = gameOver)
        return true
    }

    /**
     * Optional power-up / accessibility: finds a legal placement hint.
     * Returns Triple(trayIndex, row, col) or null if none available.
     */
    fun findHint(): Triple<Int, Int, Int>? {
        for (i in currentState.trayPieces.indices) {
            val shape = currentState.trayPieces[i] ?: continue
            val valid = MoveValidator.findValidPlacements(currentState.board, shape)
            if (valid.isNotEmpty()) {
                val anchor = valid.first()
                return Triple(i, anchor.first, anchor.second)
            }
        }
        return null
    }
}
