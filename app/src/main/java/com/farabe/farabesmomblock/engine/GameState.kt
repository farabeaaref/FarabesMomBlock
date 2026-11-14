package com.farabe.farabesmomblock.engine

/**
 * Immutable snapshot of active gameplay state.
 */
data class GameState(
    val board: Board = Board(),
    val trayPieces: List<BlockShape?> = listOf(null, null, null),
    val score: Int = 0,
    val highScore: Int = 0,
    val comboStreak: Int = 0,
    val moveCount: Int = 0,
    val linesClearedTotal: Int = 0,
    val isGameOver: Boolean = false,
    val lastClearResult: LineClearResult? = null,
    val lastComboEvent: ComboEvent? = null,
    val lastPointsGained: Int = 0
) {
    val remainingTrayPiecesCount: Int
        get() = trayPieces.count { it != null }

    val isTrayEmpty: Boolean
        get() = remainingTrayPiecesCount == 0
}
