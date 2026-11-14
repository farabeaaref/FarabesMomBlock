package com.farabe.farabesmomblock.engine

/**
 * Immutable representation of a block puzzle shape.
 * Shapes are strictly rotation-locked to match reference game mechanics.
 */
data class BlockShape(
    val id: String,
    val name: String,
    val points: List<Point2D>,
    val color: BlockColor,
    val weight: Int = 10
) {
    val width: Int = (points.maxOfOrNull { it.x } ?: 0) + 1
    val height: Int = (points.maxOfOrNull { it.y } ?: 0) + 1
    val cellCount: Int = points.size

    init {
        require(points.isNotEmpty()) { "BlockShape must contain at least one point" }
        require(points.all { it.x >= 0 && it.y >= 0 }) { "Shape coordinates must be non-negative" }
    }

    /**
     * Checks if this shape occupies the given relative coordinate.
     */
    fun hasPoint(x: Int, y: Int): Boolean = points.any { it.x == x && it.y == y }

    /**
     * Creates a copy of this shape with a customized color.
     */
    fun withColor(newColor: BlockColor): BlockShape = copy(color = newColor)
}
