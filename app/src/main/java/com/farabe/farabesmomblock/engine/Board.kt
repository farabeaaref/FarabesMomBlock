package com.farabe.farabesmomblock.engine

/**
 * Represents the puzzle board grid.
 * Grid dimensions are configurable (defaulting to 8x8).
 * Cells are represented as nullable [BlockColor] (null = empty).
 */
data class Board(
    val rows: Int = 8,
    val cols: Int = 8,
    val grid: List<List<BlockColor?>> = List(rows) { List(cols) { null } }
) {
    init {
        require(rows > 0 && cols > 0) { "Board dimensions must be positive" }
        require(grid.size == rows && grid.all { it.size == cols }) {
            "Grid dimensions must match rows and cols specifications"
        }
    }

    /**
     * Checks if coordinates are within the board boundaries.
     */
    fun isWithinBounds(row: Int, col: Int): Boolean {
        return row in 0 until rows && col in 0 until cols
    }

    /**
     * Retrieves the color at the specified cell, or null if empty or out of bounds.
     */
    fun getCell(row: Int, col: Int): BlockColor? {
        if (!isWithinBounds(row, col)) return null
        return grid[row][col]
    }

    /**
     * Returns true if cell is within bounds and unoccupied.
     */
    fun isCellEmpty(row: Int, col: Int): Boolean {
        return isWithinBounds(row, col) && grid[row][col] == null
    }

    /**
     * Checks if an entire horizontal row is filled.
     */
    fun isRowFull(row: Int): Boolean {
        if (row !in 0 until rows) return false
        return grid[row].all { it != null }
    }

    /**
     * Checks if an entire vertical column is filled.
     */
    fun isColFull(col: Int): Boolean {
        if (col !in 0 until cols) return false
        return (0 until rows).all { r -> grid[r][col] != null }
    }

    /**
     * Total number of occupied cells currently on the board.
     */
    fun occupiedCellCount(): Int {
        return grid.sumOf { row -> row.count { it != null } }
    }

    /**
     * Checks if the board is completely clear.
     */
    fun isEmpty(): Boolean = occupiedCellCount() == 0

    /**
     * Places a shape at the anchor (startRow, startCol), returning a new immutable Board.
     * Throws IllegalArgumentException if placement violates bounds or overlaps.
     */
    fun placeShape(shape: BlockShape, startRow: Int, startCol: Int): Board {
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        for (pt in shape.points) {
            val r = startRow + pt.y
            val c = startCol + pt.x
            require(isWithinBounds(r, c)) { "Placement point ($r, $c) out of bounds" }
            require(newGrid[r][c] == null) { "Cell ($r, $c) is already occupied" }
            newGrid[r][c] = shape.color
        }
        return Board(rows = rows, cols = cols, grid = newGrid.map { it.toList() })
    }

    /**
     * Clears full rows and columns simultaneously.
     * Intersections are cleared cleanly without double-erasing.
     */
    fun clearLines(rowsToClear: Set<Int>, colsToClear: Set<Int>): Board {
        if (rowsToClear.isEmpty() && colsToClear.isEmpty()) return this
        val newGrid = grid.mapIndexed { r, rowList ->
            if (rowsToClear.contains(r)) {
                List(cols) { null }
            } else {
                rowList.mapIndexed { c, cellColor ->
                    if (colsToClear.contains(c)) null else cellColor
                }
            }
        }
        return Board(rows = rows, cols = cols, grid = newGrid)
    }

    /**
     * Serializes board state to a compact string for Room/DataStore.
     * e.g. "8,8,0,-1,2,..."
     */
    fun serialize(): String {
        val builder = java.lang.StringBuilder()
        builder.append("$rows,$cols;")
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val color = grid[r][c]
                builder.append(color?.ordinal ?: -1)
                if (r < rows - 1 || c < cols - 1) builder.append(",")
            }
        }
        return builder.toString()
    }

    companion object {
        fun deserialize(data: String): Board? {
            return try {
                val parts = data.split(";")
                val dims = parts[0].split(",")
                val rows = dims[0].toInt()
                val cols = dims[1].toInt()
                val cellValues = parts[1].split(",")
                val colors = BlockColor.values()
                val newGrid = List(rows) { r ->
                    List(cols) { c ->
                        val code = cellValues[r * cols + c].toInt()
                        if (code in colors.indices) colors[code] else null
                    }
                }
                Board(rows, cols, newGrid)
            } catch (e: Exception) {
                null
            }
        }
    }
}
