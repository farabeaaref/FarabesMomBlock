package com.farabe.farabesmomblock.engine

/**
 * Reusable library of all puzzle polyomino shapes.
 * All shapes are strictly rotation-locked to honor reference game mechanics.
 */
object BlockShapeLibrary {

    // --- 1-cell (Monomino) ---
    val DOT = BlockShape(
        id = "dot_1x1",
        name = "Single Tile",
        points = listOf(Point2D(0, 0)),
        color = BlockColor.WARM_CREAM,
        weight = 8
    )

    // --- 2-cell (Dominoes) ---
    val LINE_2_H = BlockShape(
        id = "line_2_h",
        name = "2-Bar Horizontal",
        points = listOf(Point2D(0, 0), Point2D(1, 0)),
        color = BlockColor.SOFT_PINK,
        weight = 12
    )

    val LINE_2_V = BlockShape(
        id = "line_2_v",
        name = "2-Bar Vertical",
        points = listOf(Point2D(0, 0), Point2D(0, 1)),
        color = BlockColor.SOFT_PINK,
        weight = 12
    )

    // --- 3-cell (Triominoes) ---
    val LINE_3_H = BlockShape(
        id = "line_3_h",
        name = "3-Bar Horizontal",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(2, 0)),
        color = BlockColor.SKY_BLUE,
        weight = 10
    )

    val LINE_3_V = BlockShape(
        id = "line_3_v",
        name = "3-Bar Vertical",
        points = listOf(Point2D(0, 0), Point2D(0, 1), Point2D(0, 2)),
        color = BlockColor.SKY_BLUE,
        weight = 10
    )

    val CORNER_TL = BlockShape(
        id = "corner_tl",
        name = "Corner Top-Left",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(0, 1)),
        color = BlockColor.SUNSHINE_YELLOW,
        weight = 10
    )

    val CORNER_TR = BlockShape(
        id = "corner_tr",
        name = "Corner Top-Right",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(1, 1)),
        color = BlockColor.SUNSHINE_YELLOW,
        weight = 10
    )

    val CORNER_BL = BlockShape(
        id = "corner_bl",
        name = "Corner Bottom-Left",
        points = listOf(Point2D(0, 0), Point2D(0, 1), Point2D(1, 1)),
        color = BlockColor.SUNSHINE_YELLOW,
        weight = 10
    )

    val CORNER_BR = BlockShape(
        id = "corner_br",
        name = "Corner Bottom-Right",
        points = listOf(Point2D(1, 0), Point2D(0, 1), Point2D(1, 1)),
        color = BlockColor.SUNSHINE_YELLOW,
        weight = 10
    )

    // --- 4-cell (Tetrominoes) ---
    val LINE_4_H = BlockShape(
        id = "line_4_h",
        name = "4-Bar Horizontal",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(2, 0), Point2D(3, 0)),
        color = BlockColor.MINT,
        weight = 8
    )

    val LINE_4_V = BlockShape(
        id = "line_4_v",
        name = "4-Bar Vertical",
        points = listOf(Point2D(0, 0), Point2D(0, 1), Point2D(0, 2), Point2D(0, 3)),
        color = BlockColor.MINT,
        weight = 8
    )

    val SQUARE_2X2 = BlockShape(
        id = "square_2x2",
        name = "2x2 Square",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(0, 1), Point2D(1, 1)),
        color = BlockColor.LAVENDER,
        weight = 10
    )

    val L_NORMAL = BlockShape(
        id = "l_normal",
        name = "L-Shape Normal",
        points = listOf(Point2D(0, 0), Point2D(0, 1), Point2D(0, 2), Point2D(1, 2)),
        color = BlockColor.CORAL,
        weight = 8
    )

    val L_FLIPPED = BlockShape(
        id = "l_flipped",
        name = "L-Shape Flipped",
        points = listOf(Point2D(1, 0), Point2D(1, 1), Point2D(1, 2), Point2D(0, 2)),
        color = BlockColor.CORAL,
        weight = 8
    )

    val L_UP_LEFT = BlockShape(
        id = "l_up_left",
        name = "L-Shape Up Left",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(0, 1), Point2D(0, 2)),
        color = BlockColor.CORAL,
        weight = 8
    )

    val L_UP_RIGHT = BlockShape(
        id = "l_up_right",
        name = "L-Shape Up Right",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(1, 1), Point2D(1, 2)),
        color = BlockColor.CORAL,
        weight = 8
    )

    val T_DOWN = BlockShape(
        id = "t_down",
        name = "T-Shape Down",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(2, 0), Point2D(1, 1)),
        color = BlockColor.ROSE_GOLD,
        weight = 7
    )

    val T_UP = BlockShape(
        id = "t_up",
        name = "T-Shape Up",
        points = listOf(Point2D(1, 0), Point2D(0, 1), Point2D(1, 1), Point2D(2, 1)),
        color = BlockColor.ROSE_GOLD,
        weight = 7
    )

    val T_RIGHT = BlockShape(
        id = "t_right",
        name = "T-Shape Right",
        points = listOf(Point2D(0, 0), Point2D(0, 1), Point2D(0, 2), Point2D(1, 1)),
        color = BlockColor.ROSE_GOLD,
        weight = 7
    )

    val T_LEFT = BlockShape(
        id = "t_left",
        name = "T-Shape Left",
        points = listOf(Point2D(1, 0), Point2D(1, 1), Point2D(1, 2), Point2D(0, 1)),
        color = BlockColor.ROSE_GOLD,
        weight = 7
    )

    val Z_HORIZ = BlockShape(
        id = "z_horiz",
        name = "Z-Shape Horizontal",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(1, 1), Point2D(2, 1)),
        color = BlockColor.SOFT_PINK,
        weight = 6
    )

    val S_HORIZ = BlockShape(
        id = "s_horiz",
        name = "S-Shape Horizontal",
        points = listOf(Point2D(1, 0), Point2D(2, 0), Point2D(0, 1), Point2D(1, 1)),
        color = BlockColor.MINT,
        weight = 6
    )

    // --- 5-cell (Pentominoes & Large Shapes) ---
    val LINE_5_H = BlockShape(
        id = "line_5_h",
        name = "5-Bar Horizontal",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(2, 0), Point2D(3, 0), Point2D(4, 0)),
        color = BlockColor.SKY_BLUE,
        weight = 4
    )

    val LINE_5_V = BlockShape(
        id = "line_5_v",
        name = "5-Bar Vertical",
        points = listOf(Point2D(0, 0), Point2D(0, 1), Point2D(0, 2), Point2D(0, 3), Point2D(0, 4)),
        color = BlockColor.SKY_BLUE,
        weight = 4
    )

    val LARGE_L_BL = BlockShape(
        id = "large_l_bl",
        name = "Large L-Corner BL",
        points = listOf(Point2D(0, 0), Point2D(0, 1), Point2D(0, 2), Point2D(1, 2), Point2D(2, 2)),
        color = BlockColor.CORAL,
        weight = 4
    )

    val LARGE_L_TL = BlockShape(
        id = "large_l_tl",
        name = "Large L-Corner TL",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(2, 0), Point2D(0, 1), Point2D(0, 2)),
        color = BlockColor.CORAL,
        weight = 4
    )

    val LARGE_L_TR = BlockShape(
        id = "large_l_tr",
        name = "Large L-Corner TR",
        points = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(2, 0), Point2D(2, 1), Point2D(2, 2)),
        color = BlockColor.CORAL,
        weight = 4
    )

    val LARGE_L_BR = BlockShape(
        id = "large_l_br",
        name = "Large L-Corner BR",
        points = listOf(Point2D(2, 0), Point2D(2, 1), Point2D(0, 2), Point2D(1, 2), Point2D(2, 2)),
        color = BlockColor.CORAL,
        weight = 4
    )

    // --- 9-cell (Giant Square) ---
    val SQUARE_3X3 = BlockShape(
        id = "square_3x3",
        name = "3x3 Giant Square",
        points = listOf(
            Point2D(0, 0), Point2D(1, 0), Point2D(2, 0),
            Point2D(0, 1), Point2D(1, 1), Point2D(2, 1),
            Point2D(0, 2), Point2D(1, 2), Point2D(2, 2)
        ),
        color = BlockColor.LAVENDER,
        weight = 3
    )

    /**
     * Master catalog of all standard playable shapes.
     */
    val ALL_SHAPES: List<BlockShape> = listOf(
        DOT,
        LINE_2_H, LINE_2_V,
        LINE_3_H, LINE_3_V,
        CORNER_TL, CORNER_TR, CORNER_BL, CORNER_BR,
        LINE_4_H, LINE_4_V,
        SQUARE_2X2,
        L_NORMAL, L_FLIPPED, L_UP_LEFT, L_UP_RIGHT,
        T_DOWN, T_UP, T_RIGHT, T_LEFT,
        Z_HORIZ, S_HORIZ,
        LINE_5_H, LINE_5_V,
        LARGE_L_BL, LARGE_L_TL, LARGE_L_TR, LARGE_L_BR,
        SQUARE_3X3
    )

    fun getById(id: String): BlockShape? = ALL_SHAPES.firstOrNull { it.id == id }
}
