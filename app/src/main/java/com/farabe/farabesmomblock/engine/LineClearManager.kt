package com.farabe.farabesmomblock.engine

/**
 * Result of line-clearing operations, detailing rows, columns, affected cells, and new board state.
 */
data class LineClearResult(
    val clearedRows: Set<Int>,
    val clearedCols: Set<Int>,
    val clearedCells: Set<Pair<Int, Int>>,
    val totalLinesCleared: Int,
    val newBoard: Board
) {
    val hasClears: Boolean = totalLinesCleared > 0
}

/**
 * Manages detection and clearing of full rows and columns.
 * Supports simultaneous row and column clearing without double counting.
 */
object LineClearManager {

    /**
     * Identifies all fully occupied rows and columns on the given [board].
     */
    fun detectCompletedLines(board: Board): Pair<Set<Int>, Set<Int>> {
        val completedRows = mutableSetOf<Int>()
        val completedCols = mutableSetOf<Int>()

        for (r in 0 until board.rows) {
            if (board.isRowFull(r)) {
                completedRows.add(r)
            }
        }

        for (c in 0 until board.cols) {
            if (board.isColFull(c)) {
                completedCols.add(c)
            }
        }

        return Pair(completedRows, completedCols)
    }

    /**
     * Previews which rows and columns would be completed if [shape] were placed at ([startRow], [startCol]).
     * Essential for real-time ghost glow previews while dragging.
     */
    fun previewLinesIfPlaced(
        board: Board,
        shape: BlockShape,
        startRow: Int,
        startCol: Int
    ): Pair<Set<Int>, Set<Int>> {
        if (!MoveValidator.isValidPlacement(board, shape, startRow, startCol)) {
            return Pair(emptySet(), emptySet())
        }

        // Create hypothetical overlay
        val hypotheticallyFilled = shape.points.map { pt ->
            Pair(startRow + pt.y, startCol + pt.x)
        }.toSet()

        val willCompleteRows = mutableSetOf<Int>()
        val willCompleteCols = mutableSetOf<Int>()

        // Check rows
        for (r in 0 until board.rows) {
            var full = true
            for (c in 0 until board.cols) {
                val isOccupied = board.getCell(r, c) != null || hypotheticallyFilled.contains(Pair(r, c))
                if (!isOccupied) {
                    full = false
                    break
                }
            }
            if (full) willCompleteRows.add(r)
        }

        // Check cols
        for (c in 0 until board.cols) {
            var full = true
            for (r in 0 until board.rows) {
                val isOccupied = board.getCell(r, c) != null || hypotheticallyFilled.contains(Pair(r, c))
                if (!isOccupied) {
                    full = false
                    break
                }
            }
            if (full) willCompleteCols.add(c)
        }

        return Pair(willCompleteRows, willCompleteCols)
    }

    /**
     * Clears all completed lines from [board] and returns a detailed [LineClearResult].
     */
    fun clearCompletedLines(board: Board): LineClearResult {
        val (completedRows, completedCols) = detectCompletedLines(board)
        if (completedRows.isEmpty() && completedCols.isEmpty()) {
            return LineClearResult(
                clearedRows = emptySet(),
                clearedCols = emptySet(),
                clearedCells = emptySet(),
                totalLinesCleared = 0,
                newBoard = board
            )
        }

        val clearedCells = mutableSetOf<Pair<Int, Int>>()
        for (r in completedRows) {
            for (c in 0 until board.cols) {
                clearedCells.add(Pair(r, c))
            }
        }
        for (c in completedCols) {
            for (r in 0 until board.rows) {
                clearedCells.add(Pair(r, c))
            }
        }

        val updatedBoard = board.clearLines(completedRows, completedCols)
        val totalLines = completedRows.size + completedCols.size

        return LineClearResult(
            clearedRows = completedRows,
            clearedCols = completedCols,
            clearedCells = clearedCells,
            totalLinesCleared = totalLines,
            newBoard = updatedBoard
        )
    }
}
