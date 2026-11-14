package com.farabe.farabesmomblock

import com.farabe.farabesmomblock.engine.BlockColor
import com.farabe.farabesmomblock.engine.BlockShapeLibrary
import com.farabe.farabesmomblock.engine.Board
import com.farabe.farabesmomblock.engine.MoveValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveValidatorTest {

    @Test
    fun testValidPlacementOnEmptyBoard() {
        val board = Board(8, 8)
        assertTrue(MoveValidator.isValidPlacement(board, BlockShapeLibrary.DOT, 0, 0))
        assertTrue(MoveValidator.isValidPlacement(board, BlockShapeLibrary.SQUARE_3X3, 0, 0))
        assertTrue(MoveValidator.isValidPlacement(board, BlockShapeLibrary.SQUARE_3X3, 5, 5))
    }

    @Test
    fun testInvalidPlacementOutOfBounds() {
        val board = Board(8, 8)
        // 3x3 square at 6, 6 would require cells up to 8, 8
        assertFalse(MoveValidator.isValidPlacement(board, BlockShapeLibrary.SQUARE_3X3, 6, 6))
        assertFalse(MoveValidator.isValidPlacement(board, BlockShapeLibrary.LINE_5_H, 0, 4))
        assertFalse(MoveValidator.isValidPlacement(board, BlockShapeLibrary.LINE_5_V, 4, 0))
        assertFalse(MoveValidator.isValidPlacement(board, BlockShapeLibrary.DOT, -1, 0))
    }

    @Test
    fun testInvalidPlacementCollision() {
        val board = Board(8, 8).placeShape(BlockShapeLibrary.DOT, 2, 2)
        // Trying to place another dot at 2, 2
        assertFalse(MoveValidator.isValidPlacement(board, BlockShapeLibrary.DOT, 2, 2))
        // Trying to place a 2x2 square overlapping 2, 2
        assertFalse(MoveValidator.isValidPlacement(board, BlockShapeLibrary.SQUARE_2X2, 1, 1))
        assertFalse(MoveValidator.isValidPlacement(board, BlockShapeLibrary.SQUARE_2X2, 2, 2))
        // Non-overlapping placement should succeed
        assertTrue(MoveValidator.isValidPlacement(board, BlockShapeLibrary.SQUARE_2X2, 3, 3))
    }

    @Test
    fun testCanFitAnywhere() {
        val board = Board(8, 8)
        assertTrue(MoveValidator.canFitAnywhere(board, BlockShapeLibrary.SQUARE_3X3))

        // Create a checkerboard-like saturated board where 2x2 cannot fit
        var fullBoard = Board(8, 8)
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                fullBoard = fullBoard.placeShape(BlockShapeLibrary.DOT, r, c)
            }
        }
        assertFalse(MoveValidator.canFitAnywhere(fullBoard, BlockShapeLibrary.DOT))
        assertFalse(MoveValidator.canFitAnywhere(fullBoard, BlockShapeLibrary.LINE_2_H))
    }

    @Test
    fun testGameOverDetection() {
        // Empty board -> not game over
        val emptyBoard = Board(8, 8)
        val tray = listOf(BlockShapeLibrary.DOT, BlockShapeLibrary.LINE_2_H, BlockShapeLibrary.SQUARE_2X2)
        assertFalse(MoveValidator.isGameOver(emptyBoard, tray))

        // Empty tray -> not game over (replenishment pending)
        val emptyTray = listOf(null, null, null)
        assertFalse(MoveValidator.isGameOver(emptyBoard, emptyTray))

        // Fully saturated board -> game over if pieces remain
        var saturatedBoard = Board(8, 8)
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                saturatedBoard = saturatedBoard.placeShape(BlockShapeLibrary.DOT, r, c)
            }
        }
        assertTrue(MoveValidator.isGameOver(saturatedBoard, tray))

        // If even a single cell is open and DOT is in tray -> NOT game over
        var nearlyFullBoard = Board(8, 8)
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                if (!(r == 7 && c == 7)) {
                    nearlyFullBoard = nearlyFullBoard.placeShape(BlockShapeLibrary.DOT, r, c)
                }
            }
        }
        // DOT fits at (7, 7)
        assertFalse(MoveValidator.isGameOver(nearlyFullBoard, listOf(BlockShapeLibrary.DOT, null, null)))
        // SQUARE_2X2 cannot fit -> game over if only SQUARE_2X2 is present
        assertTrue(MoveValidator.isGameOver(nearlyFullBoard, listOf(BlockShapeLibrary.SQUARE_2X2, null, null)))
    }
}
