package com.farabe.farabesmomblock.engine

import java.util.Random

/**
 * Generates triplets of puzzle pieces with weighted distribution,
 * deterministic seeding capability, and board solvability safeguards.
 */
class BlockGenerator(seed: Long? = null) {

    private val random: Random = if (seed != null) Random(seed) else Random()

    /**
     * Generates a batch of 3 pieces.
     * If a [board] is provided, validates that at least one piece in the batch
     * is playable on the board (unless the board is fully occupied).
     */
    fun generateTray(board: Board? = null, count: Int = 3): List<BlockShape> {
        val result = mutableListOf<BlockShape>()

        for (i in 0 until count) {
            val baseShape = selectWeightedShape()
            val color = selectHarmoniousColor(result.map { it.color })
            result.add(baseShape.withColor(color))
        }

        // Anti-frustration safeguard: if board has open space, ensure at least one piece can fit
        if (board != null && board.occupiedCellCount() < (board.rows * board.cols)) {
            val hasValidMove = result.any { MoveValidator.canFitAnywhere(board, it) }
            if (!hasValidMove) {
                // Find a shape from the catalog that CAN fit
                val candidate = BlockShapeLibrary.ALL_SHAPES.firstOrNull { MoveValidator.canFitAnywhere(board, it) }
                if (candidate != null) {
                    val color = selectHarmoniousColor(result.take(count - 1).map { it.color })
                    result[result.size - 1] = candidate.withColor(color)
                }
            }
        }

        return result
    }

    /**
     * Weighted random selection from the shape library.
     */
    fun selectWeightedShape(): BlockShape {
        val shapes = BlockShapeLibrary.ALL_SHAPES
        val totalWeight = shapes.sumOf { it.weight }
        val roll = random.nextInt(totalWeight)

        var cumulative = 0
        for (shape in shapes) {
            cumulative += shape.weight
            if (roll < cumulative) {
                return shape
            }
        }
        return shapes.first()
    }

    /**
     * Selects a color that preferably differs from already picked colors in this tray batch.
     */
    private fun selectHarmoniousColor(alreadyUsedColors: List<BlockColor>): BlockColor {
        val allColors = BlockColor.values().filter { it != BlockColor.WARM_CREAM } // reserve cream for highlights
        val available = allColors.filter { it !in alreadyUsedColors }
        val pool = if (available.isNotEmpty()) available else allColors
        return pool[random.nextInt(pool.size)]
    }
}
