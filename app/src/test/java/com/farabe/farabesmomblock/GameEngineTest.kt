package com.farabe.farabesmomblock

import com.farabe.farabesmomblock.engine.BlockShapeLibrary
import com.farabe.farabesmomblock.engine.Board
import com.farabe.farabesmomblock.engine.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun testEngineInitialization() {
        val engine = GameEngine(seed = 42L)
        val state = engine.currentState

        assertEquals(8, state.board.rows)
        assertEquals(8, state.board.cols)
        assertEquals(0, state.score)
        assertEquals(0, state.comboStreak)
        assertEquals(3, state.remainingTrayPiecesCount)
        assertFalse(state.isGameOver)
    }

    @Test
    fun testDeterministicSeedGeneratesSameTray() {
        val engine1 = GameEngine(seed = 12345L)
        val engine2 = GameEngine(seed = 12345L)

        val tray1 = engine1.currentState.trayPieces
        val tray2 = engine2.currentState.trayPieces

        assertEquals(tray1.size, tray2.size)
        for (i in tray1.indices) {
            assertEquals(tray1[i]?.id, tray2[i]?.id)
            assertEquals(tray1[i]?.color, tray2[i]?.color)
        }
    }

    @Test
    fun testPlacePieceAndTrayConsumption() {
        val engine = GameEngine(seed = 42L)
        val pieceToPlace = engine.currentState.trayPieces[0]!!

        val result = engine.placePiece(trayIndex = 0, row = 0, col = 0)
        assertTrue(result.success)

        val newState = engine.currentState
        // Tray slot 0 should now be null, active count = 2
        assertNull(newState.trayPieces[0])
        assertEquals(2, newState.remainingTrayPiecesCount)
        assertTrue(newState.score >= pieceToPlace.cellCount)
        assertEquals(1, newState.moveCount)
    }

    @Test
    fun testTrayReplenishmentWhenEmpty() {
        val engine = GameEngine(seed = 42L)

        // Find placements for all 3 pieces
        for (i in 0 until 3) {
            val shape = engine.currentState.trayPieces[i]!!
            // Place at distinct safe positions
            val r = (i * 2) % 6
            val c = (i * 2) % 6
            if (engine.canPlace(i, r, c)) {
                engine.placePiece(i, r, c)
            } else {
                val hint = engine.findHint()
                assertNotNull(hint)
                engine.placePiece(hint!!.first, hint.second, hint.third)
            }
        }

        // After placing 3 pieces, tray should automatically replenish with 3 fresh pieces!
        assertEquals(3, engine.currentState.remainingTrayPiecesCount)
        assertTrue(engine.currentState.trayPieces.all { it != null })
    }

    @Test
    fun testUndoMove() {
        val engine = GameEngine(seed = 42L)
        assertFalse(engine.canUndo)

        val initialScore = engine.currentState.score
        val piece = engine.currentState.trayPieces[0]!!

        val result = engine.placePiece(0, 0, 0)
        assertTrue(result.success)
        assertTrue(engine.canUndo)
        assertTrue(engine.currentState.score > initialScore)

        val undone = engine.undo()
        assertTrue(undone)
        assertEquals(initialScore, engine.currentState.score)
        assertEquals(0, engine.currentState.board.occupiedCellCount())
        assertEquals(piece.id, engine.currentState.trayPieces[0]?.id)
        assertFalse(engine.canUndo)
    }

    @Test
    fun testShuffleTray() {
        val engine = GameEngine(seed = 42L)
        val success = engine.shuffleTray()
        assertTrue(success)
        assertEquals(3, engine.currentState.remainingTrayPiecesCount)
    }

    @Test
    fun testFindHint() {
        val engine = GameEngine(seed = 42L)
        val hint = engine.findHint()
        assertNotNull(hint)
        val (trayIdx, row, col) = hint!!
        assertTrue(engine.canPlace(trayIdx, row, col))
    }

    @Test
    fun testRestoreGame() {
        val engine = GameEngine(seed = 42L)
        val testBoard = Board(8, 8).placeShape(BlockShapeLibrary.DOT, 1, 1)
        val testTray = listOf(BlockShapeLibrary.LINE_2_H, null, BlockShapeLibrary.SQUARE_2X2)

        engine.restoreGame(
            board = testBoard,
            trayPieces = testTray,
            score = 1500,
            highScore = 2000,
            comboStreak = 2,
            moveCount = 15,
            linesClearedTotal = 8
        )

        val state = engine.currentState
        assertEquals(1500, state.score)
        assertEquals(2000, state.highScore)
        assertEquals(2, state.comboStreak)
        assertEquals(15, state.moveCount)
        assertEquals(8, state.linesClearedTotal)
        assertEquals(2, state.remainingTrayPiecesCount)
        assertEquals(1, state.board.occupiedCellCount())
    }
}
