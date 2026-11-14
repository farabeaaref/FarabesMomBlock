package com.farabe.farabesmomblock.engine

/**
 * Breakdown of points awarded for a single player move.
 */
data class ScoreBreakdown(
    val placementPoints: Int,
    val linePoints: Int,
    val comboBonus: Int,
    val allClearBonus: Int,
    val totalPointsGained: Int
)

/**
 * Manages mathematical scoring calculations, multi-line bonuses, and streaks.
 */
object ScoringManager {

    const val POINTS_PER_CELL: Int = 1
    const val BASE_POINTS_PER_LINE: Int = 100
    const val MULTI_LINE_FACTOR: Int = 50
    const val COMBO_MULTIPLIER_BASE: Int = 80
    const val ALL_CLEAR_BONUS: Int = 500

    /**
     * Calculates base placement points for a piece.
     */
    fun calculatePlacementPoints(shape: BlockShape): Int {
        return shape.cellCount * POINTS_PER_CELL
    }

    /**
     * Calculates line clearing points for [linesCleared] simultaneously.
     * Incorporates quadratic scaling to reward multi-clears.
     */
    fun calculateLinePoints(linesCleared: Int): Int {
        if (linesCleared <= 0) return 0
        return (BASE_POINTS_PER_LINE * linesCleared) + (MULTI_LINE_FACTOR * linesCleared * (linesCleared - 1))
    }

    /**
     * Calculates bonus points awarded for consecutive combo streak [comboStreak]
     * on clearing [linesCleared] lines.
     */
    fun calculateComboBonus(comboStreak: Int, linesCleared: Int): Int {
        if (comboStreak < 2 || linesCleared <= 0) return 0
        return (comboStreak - 1) * COMBO_MULTIPLIER_BASE * linesCleared
    }

    /**
     * Computes the complete score breakdown for a move.
     */
    fun computeScore(
        shape: BlockShape,
        linesCleared: Int,
        comboStreak: Int,
        isBoardNowEmpty: Boolean
    ): ScoreBreakdown {
        val placementPts = calculatePlacementPoints(shape)
        val linePts = calculateLinePoints(linesCleared)
        val comboPts = calculateComboBonus(comboStreak, linesCleared)
        val allClearPts = if (isBoardNowEmpty && linesCleared > 0) ALL_CLEAR_BONUS else 0

        val total = placementPts + linePts + comboPts + allClearPts
        return ScoreBreakdown(
            placementPoints = placementPts,
            linePoints = linePts,
            comboBonus = comboPts,
            allClearBonus = allClearPts,
            totalPointsGained = total
        )
    }
}
