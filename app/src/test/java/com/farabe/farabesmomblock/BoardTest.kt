package com.farabe.farabesmomblock

import com.farabe.farabesmomblock.engine.BlockColor
import com.farabe.farabesmomblock.engine.BlockShapeLibrary
import com.farabe.farabesmomblock.engine.Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun testBoardInitialization() {
        val board = Board(8, 8)
        assertEquals(8, board.rows)
        assertEquals(8, board.cols)
        assertEquals(0, board.occupiedCellCount())
        assertTrue(board.isEmpty())

        for (r in 0 until 8) {
            for (c in 0 until 8) {
                assertNull(board.getCell(r, c))
                assertTrue(board.isCellEmpty(r, c))
            }
        }
    }

    @Test
    fun testBoardBoundaries() {
        val board = Board(8, 8)
        assertTrue(board.isWithinBounds(0, 0))
        assertTrue(board.isWithinBounds(7, 7))
        assertFalse(board.isWithinBounds(-1, 0))
        assertFalse(board.isWithinBounds(0, -1))
        assertFalse(board.isWithinBounds(8, 0))
        assertFalse(board.isWithinBounds(0, 8))
    }

    @Test
    fun testPlaceShape() {
        val board = Board(8, 8)
        val shape = BlockShapeLibrary.SQUARE_2X2
        val updated = board.placeShape(shape, 2, 3)

        assertEquals(4, updated.occupiedCellCount())
        assertFalse(updated.isEmpty())

        // Check occupied cells
        assertEquals(shape.color, updated.getCell(2, 3))
        assertEquals(shape.color, updated.getCell(2, 4))
        assertEquals(shape.color, updated.getCell(3, 3))
        assertEquals(shape.color, updated.getCell(3, 4))

        // Check other cell remains null
        assertNull(updated.getCell(0, 0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPlaceShapeOutOfBoundsThrows() {
        val board = Board(8, 8)
        val shape = BlockShapeLibrary.LINE_4_H
        board.placeShape(shape, 0, 5) // width is 4, 5 + 4 = 9 > 8
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPlaceShapeOverlapThrows() {
        val board = Board(8, 8)
        val shape = BlockShapeLibrary.SQUARE_2X2
        val updated = board.placeShape(shape, 0, 0)
        // Trying to place on top of occupied cells
        updated.placeShape(BlockShapeLibrary.DOT, 0, 0)
    }

    @Test
    fun testSerializationAndDeserialization() {
        val board = Board(8, 8).placeShape(BlockShapeLibrary.DOT, 4, 4)
        val serialized = board.serialize()
        assertNotNull(serialized)

        val deserialized = Board.deserialize(serialized)
        assertNotNull(deserialized)
        assertEquals(board.rows, deserialized!!.rows)
        assertEquals(board.cols, deserialized.cols)
        assertEquals(board.occupiedCellCount(), deserialized.occupiedCellCount())
        assertEquals(board.getCell(4, 4), deserialized.getCell(4, 4))
    }
}
