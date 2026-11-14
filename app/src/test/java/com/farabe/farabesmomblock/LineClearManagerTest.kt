package com.farabe.farabesmomblock

import com.farabe.farabesmomblock.engine.BlockShapeLibrary
import com.farabe.farabesmomblock.engine.Board
import com.farabe.farabesmomblock.engine.LineClearManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LineClearManagerTest {

    @Test
    fun testNoLinesClearedOnSparseBoard() {
        val board = Board(8, 8).placeShape(BlockShapeLibrary.LINE_4_H, 0, 0)
        val result = LineClearManager.clearCompletedLines(board)
        assertFalse(result.hasClears)
        assertEquals(0, result.totalLinesCleared)
        assertEquals(4, result.newBoard.occupiedCellCount())
    }

    @Test
    fun testSingleRowClear() {
        var board = Board(8, 8)
        // Fill row 3 with two 4-bar horizontals
        board = board.placeShape(BlockShapeLibrary.LINE_4_H, 3, 0)
        board = board.placeShape(BlockShapeLibrary.LINE_4_H, 3, 4)

        // Also add an unrelated cell at (0, 0)
        board = board.placeShape(BlockShapeLibrary.DOT, 0, 0)

        assertEquals(9, board.occupiedCellCount())

        val result = LineClearManager.clearCompletedLines(board)
        assertTrue(result.hasClears)
        assertEquals(1, result.totalLinesCleared)
        assertTrue(result.clearedRows.contains(3))
        assertEquals(emptySet<Int>(), result.clearedCols)
        assertEquals(8, result.clearedCells.size)

        // Row 3 should now be completely empty
        for (c in 0 until 8) {
            assertNull(result.newBoard.getCell(3, c))
        }
        // (0, 0) must remain intact
        assertEquals(1, result.newBoard.occupiedCellCount())
    }

    @Test
    fun testSingleColumnClear() {
        var board = Board(8, 8)
        // Fill col 2 with two 4-bar verticals
        board = board.placeShape(BlockShapeLibrary.LINE_4_V, 0, 2)
        board = board.placeShape(BlockShapeLibrary.LINE_4_V, 4, 2)

        val result = LineClearManager.clearCompletedLines(board)
        assertTrue(result.hasClears)
        assertEquals(1, result.totalLinesCleared)
        assertTrue(result.clearedCols.contains(2))
        assertEquals(emptySet<Int>(), result.clearedRows)
        assertEquals(8, result.clearedCells.size)
        assertEquals(0, result.newBoard.occupiedCellCount())
    }

    @Test
    fun testSimultaneousRowAndColumnClearWithIntersection() {
        var board = Board(8, 8)
        // Fill row 4
        board = board.placeShape(BlockShapeLibrary.LINE_4_H, 4, 0)
        board = board.placeShape(BlockShapeLibrary.LINE_4_H, 4, 4)

        // Fill col 4 (except intersection 4,4 which is already occupied)
        // row 0..3 on col 4:
        board = board.placeShape(BlockShapeLibrary.LINE_4_V, 0, 4)
        // row 5..7 on col 4:
        board = board.placeShape(BlockShapeLibrary.LINE_3_V, 5, 4)

        // Total occupied before clear: 8 (row) + 7 (col excluding (4,4)) = 15 cells
        assertEquals(15, board.occupiedCellCount())

        val result = LineClearManager.clearCompletedLines(board)
        assertTrue(result.hasClears)
        assertEquals(2, result.totalLinesCleared)
        assertTrue(result.clearedRows.contains(4))
        assertTrue(result.clearedCols.contains(4))

        // Total unique cleared cells: 8 + 8 - 1 (intersection) = 15 cells
        assertEquals(15, result.clearedCells.size)
        assertEquals(0, result.newBoard.occupiedCellCount())
    }

    @Test
    fun testPreviewLinesIfPlaced() {
        var board = Board(8, 8)
        // Fill 7 out of 8 cells in row 0
        board = board.placeShape(BlockShapeLibrary.LINE_4_H, 0, 0)
        board = board.placeShape(BlockShapeLibrary.LINE_3_H, 0, 4)

        // Placing a DOT at (0, 7) should complete row 0
        val (rows, cols) = LineClearManager.previewLinesIfPlaced(board, BlockShapeLibrary.DOT, 0, 7)
        assertEquals(setOf(0), rows)
        assertEquals(emptySet<Int>(), cols)
    }
}
