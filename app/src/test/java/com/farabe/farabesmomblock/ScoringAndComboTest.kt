package com.farabe.farabesmomblock

import com.farabe.farabesmomblock.engine.BlockShapeLibrary
import com.farabe.farabesmomblock.engine.ComboManager
import com.farabe.farabesmomblock.engine.ScoringManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringAndComboTest {

    @Test
    fun testPlacementPoints() {
        assertEquals(1, ScoringManager.calculatePlacementPoints(BlockShapeLibrary.DOT))
        assertEquals(4, ScoringManager.calculatePlacementPoints(BlockShapeLibrary.SQUARE_2X2))
        assertEquals(9, ScoringManager.calculatePlacementPoints(BlockShapeLibrary.SQUARE_3X3))
    }

    @Test
    fun testLineClearingPoints() {
        assertEquals(0, ScoringManager.calculateLinePoints(0))
        // 1 line: 100
        assertEquals(100, ScoringManager.calculateLinePoints(1))
        // 2 lines: 200 + 50*2*1 = 300
        assertEquals(300, ScoringManager.calculateLinePoints(2))
        // 3 lines: 300 + 50*3*2 = 600
        assertEquals(600, ScoringManager.calculateLinePoints(3))
        // 4 lines: 400 + 50*4*3 = 1000
        assertEquals(1000, ScoringManager.calculateLinePoints(4))
    }

    @Test
    fun testComboBonusPoints() {
        // Streak 0 or 1 awards 0 combo bonus
        assertEquals(0, ScoringManager.calculateComboBonus(0, 1))
        assertEquals(0, ScoringManager.calculateComboBonus(1, 1))

        // Streak 2 on 1 line: (2-1) * 80 * 1 = 80
        assertEquals(80, ScoringManager.calculateComboBonus(2, 1))
        // Streak 3 on 2 lines: (3-1) * 80 * 2 = 320
        assertEquals(320, ScoringManager.calculateComboBonus(3, 2))
    }

    @Test
    fun testFullScoreBreakdown() {
        val breakdown = ScoringManager.computeScore(
            shape = BlockShapeLibrary.LINE_4_H, // 4 cells
            linesCleared = 2, // 300 pts
            comboStreak = 3,  // (3-1)*80*2 = 320 pts
            isBoardNowEmpty = false
        )

        assertEquals(4, breakdown.placementPoints)
        assertEquals(300, breakdown.linePoints)
        assertEquals(320, breakdown.comboBonus)
        assertEquals(0, breakdown.allClearBonus)
        assertEquals(624, breakdown.totalPointsGained)
    }

    @Test
    fun testAllClearBonus() {
        val breakdown = ScoringManager.computeScore(
            shape = BlockShapeLibrary.DOT,
            linesCleared = 1,
            comboStreak = 1,
            isBoardNowEmpty = true
        )

        assertEquals(1, breakdown.placementPoints)
        assertEquals(100, breakdown.linePoints)
        assertEquals(500, breakdown.allClearBonus)
        assertEquals(601, breakdown.totalPointsGained)
    }

    @Test
    fun testComboManagerProgression() {
        val combo = ComboManager()
        assertEquals(0, combo.currentStreak)

        // Turn 1: Clears 1 line -> streak 1
        val event1 = combo.onMoveCompleted(1)
        assertEquals(1, combo.currentStreak)
        assertFalse(event1.isComboActive)
        assertEquals("GREAT CLEAR!", event1.celebratoryTitle)

        // Turn 2: Clears 1 line -> streak 2 (Combo x2!)
        val event2 = combo.onMoveCompleted(1)
        assertEquals(2, combo.currentStreak)
        assertTrue(event2.isComboActive)
        assertEquals("COMBO x2!", event2.celebratoryTitle)

        // Turn 3: Clears 2 lines -> streak 3 (Combo x3!)
        val event3 = combo.onMoveCompleted(2)
        assertEquals(3, combo.currentStreak)
        assertTrue(event3.isComboActive)
        assertEquals("COMBO x3!", event3.celebratoryTitle)

        // Turn 4: Move placed without clearing any line -> resets to 0
        val event4 = combo.onMoveCompleted(0)
        assertEquals(0, combo.currentStreak)
        assertFalse(event4.isComboActive)
        assertNull(event4.celebratoryTitle)
    }
}
