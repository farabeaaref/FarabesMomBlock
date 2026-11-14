package com.farabe.farabesmomblock.engine

/**
 * Validates player moves, boundary conditions, cell collisions, and game-over state.
 */
object MoveValidator {

    /**
     * Checks whether the specified [shape] can be placed at ([startRow], [startCol])
     * without exceeding board boundaries or overlapping occupied cells.
     */
    fun isValidPlacement(
        board: Board,
        shape: BlockShape,
        startRow: Int,
        startCol: Int
    ): Boolean {
        // Fast boundary check based on bounding dimensions
        if (startRow < 0 || startCol < 0) return false
        if (startRow + shape.height > board.rows) return false
        if (startCol + shape.width > board.cols) return false

        // Precise cell-by-cell collision and bounds check
        for (pt in shape.points) {
            val r = startRow + pt.y
            val c = startCol + pt.x
            if (!board.isWithinBounds(r, c) || !board.isCellEmpty(r, c)) {
                return false
            }
        }
        return true
    }

    /**
     * Checks if the given [shape] can fit ANYWHERE on the current [board].
     */
    fun canFitAnywhere(board: Board, shape: BlockShape): Boolean {
        val maxRow = board.rows - shape.height
        val maxCol = board.cols - shape.width
        if (maxRow < 0 || maxCol < 0) return false

        for (r in 0..maxRow) {
            for (c in 0..maxCol) {
                if (isValidPlacement(board, shape, r, c)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Finds all legal placement anchor points for a given shape.
     */
    fun findValidPlacements(board: Board, shape: BlockShape): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        val maxRow = board.rows - shape.height
        val maxCol = board.cols - shape.width
        if (maxRow < 0 || maxCol < 0) return emptyList()

        for (r in 0..maxRow) {
            for (c in 0..maxCol) {
                if (isValidPlacement(board, shape, r, c)) {
                    result.add(Pair(r, c))
                }
            }
        }
        return result
    }

    /**
     * Evaluates game-over condition.
     * Game is over IF AND ONLY IF there is at least one active piece in the tray,
     * and NONE of the active pieces can fit anywhere on the board.
     */
    fun isGameOver(board: Board, trayPieces: List<BlockShape?>): Boolean {
        val activePieces = trayPieces.filterNotNull()
        if (activePieces.isEmpty()) {
            // Tray is empty, pieces will be replenished - NOT game over
            return false
        }
        // If even one active piece can fit anywhere, game is NOT over
        return activePieces.none { canFitAnywhere(board, it) }
    }
}
