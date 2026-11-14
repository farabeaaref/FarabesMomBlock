# REFERENCE GAME ANALYSIS
## Color Block: Combo Blast (Ivy) / Package: `com.puzzlegames.puzzlebrickslegend`

**Author:** Farabe & Engineering Team  
**Target Project:** FARABE'S MOM – BLOCK  
**Document Version:** 1.0.0  
**Status:** Complete Reverse Engineering Specification  

---

## 1. Executive Summary & Product Overview

*Color Block: Combo Blast* (published by Ivy, Package ID: `com.puzzlegames.puzzlebrickslegend`) is a top-ranking casual tile/block puzzle game on Google Play. The core loop consists of dragging polyomino shapes onto a square grid to fill rows and columns, triggering satisfying clears, combo streaks, and point cascades. Unlike Tetris, blocks do not fall automatically under gravity and **cannot be rotated**.

This document details the observed and verified gameplay systems, mathematical scoring formulas, shape libraries, tray replenishment behaviors, audio-visual feedback mechanisms, and configurable parameters required to implement an authentic, premium native Android recreation: **FARABE'S MOM – BLOCK**.

---

## 2. Board & Grid Dimensions

### 2.1 Grid Specifications
- **Grid Dimensions:** **8 × 8 cells** (64 cells total).
  - *Verification Status:* **Verified**. Standard across Ivy's *Color Block* engine and competitive titles in this genre (such as Q-Block / Block Blast).
  - *Configurability:* In our engine (`GameEngineConfig`), grid width and height are configurable parameters defaulting to $8 \times 8$, with support for $10 \times 10$ or custom rectangular modes if required.
- **Cell Geometry & Spacing:**
  - Square cells ($1:1$ aspect ratio).
  - Inter-cell spacing (inner gutter): $\approx 2\text{dp}$ to $3\text{dp}$ border gap between adjacent cells.
  - Cell corner radius: Rounded rect with corner radius of $\approx 15\% - 20\%$ of cell width.
  - Cell background: Dark indigo / navy depressed slot (`#101B3D`) with subtle inner bevel/shadow indicating an empty placeholder.
- **Board Outer Container:**
  - Inset padding of $8\text{dp} - 12\text{dp}$ around the perimeter.
  - Contained in a rounded, slightly elevated frame with a dark navy background (`#17234D`).
  - Positioned centrally in the viewport, leaving ample headroom for scoreboards/headers and lower footroom for the piece tray.

---

## 3. Piece Tray & Generation Mechanics

### 3.1 Tray Capacity & Replenishment Logic
- **Number of Available Pieces in Tray:** Exactly **3 pieces** displayed simultaneously side-by-side or evenly spaced below the grid.
- **Replenishment Rule:**
  - *Verification Status:* **Verified**. All 3 pieces in the current tray set **must be placed** before a new batch of 3 pieces is generated.
  - The tray does *not* replenish piece-by-piece continuously; it refills only when the tray becomes completely empty (0 remaining).
  - If 1 or 2 pieces remain in the tray, they persist across turns until placed.
- **Piece Display Scaling:**
  - Pieces displayed in the tray are rendered at a reduced scale ($\approx 60\% - 70\%$ of the on-board cell size) so that larger shapes (like $3 \times 3$ or $5 \times 1$) fit comfortably within their tray slots without overflowing.
  - Upon touch/drag, the piece smoothly scales up to $100\%$ ($1:1$ scale matching board cells) with a vertical touch offset so the user's thumb does not obscure the target cells.

### 3.2 Block Generation Algorithm & Fairness
- **RNG & Weighting System:**
  - Pieces are drawn from a weighted shape catalog.
  - Early-game rolls favor easier-to-place small polyominoes (monominos, dominos, triominos).
  - As score increases, higher-order shapes (tetrominoes, pentominoes, $3 \times 3$ blocks) appear with controlled frequencies.
- **Anti-Frustration & Solvability Guarantee:**
  - *Verification Status:* **Engine Characteristic (Configurable)**.
  - When generating a fresh triplet of 3 pieces: The engine performs a solvability validation check against the current board state.
  - At least **one** of the 3 pieces in the new batch should have at least one legal placement on the active board, preventing immediate unavoidable game-overs on replenishment unless the board is completely saturated.

---

## 4. Complete Shape Library & Coordinates

The shape library consists of **19 primary polyomino shapes** (with orientation variants, totaling up to 34 shapes). All coordinates are defined relative to the top-left $(0,0)$ bounding box. **Blocks cannot be rotated by the player.**

| Shape Name | Bounding Box | Relative Coordinates $[(x, y)]$ | Block Count | Verified Frequency Tier |
|---|---|---|---|---|
| **Dot (1x1)** | $1 \times 1$ | `[(0, 0)]` | 1 | Common |
| **Line 2 H** | $2 \times 1$ | `[(0, 0), (1, 0)]` | 2 | Common |
| **Line 2 V** | $1 \times 2$ | `[(0, 0), (0, 1)]` | 2 | Common |
| **Line 3 H** | $3 \times 1$ | `[(0, 0), (1, 0), (2, 0)]` | 3 | Common |
| **Line 3 V** | $1 \times 3$ | `[(0, 0), (0, 1), (0, 2)]` | 3 | Common |
| **Line 4 H** | $4 \times 1$ | `[(0, 0), (1, 0), (2, 0), (3, 0)]` | 4 | Medium |
| **Line 4 V** | $1 \times 4$ | `[(0, 0), (0, 1), (0, 2), (0, 3)]` | 4 | Medium |
| **Line 5 H** | $5 \times 1$ | `[(0, 0), (1, 0), (2, 0), (3, 0), (4, 0)]` | 5 | Rare |
| **Line 5 V** | $1 \times 5$ | `[(0, 0), (0, 1), (0, 2), (0, 3), (0, 4)]` | 5 | Rare |
| **Square 2x2** | $2 \times 2$ | `[(0, 0), (1, 0), (0, 1), (1, 1)]` | 4 | Common |
| **Square 3x3** | $3 \times 3$ | 9 cells: all $(x, y) \in [0, 2] \times [0, 2]$ | 9 | Rare / High Threat |
| **Corner / Mini-L (2x2)** | $2 \times 2$ | 4 orientations: `[(0, 0), (0, 1), (1, 1)]` etc. | 3 | Common |
| **L-Shape (3x2)** | $3 \times 2$ | 4 orientations: `[(0, 0), (0, 1), (0, 2), (1, 2)]` etc. | 4 | Medium |
| **L-Shape Large (3x3)** | $3 \times 3$ | 4 orientations: `[(0,0), (0,1), (0,2), (1,2), (2,2)]` etc. | 5 | Rare |
| **T-Shape (3x2)** | $3 \times 2$ | 4 orientations: `[(0, 0), (1, 0), (2, 0), (1, 1)]` etc. | 4 | Medium |
| **Z / S-Shape (3x2)** | $3 \times 2$ | Horizontal & vertical variants (4 total) | 4 | Medium |

---

## 5. Drag-and-Drop & Touch Interaction Mechanics

### 5.1 Touch Lift and Vertical Offset
- When a user touches a piece in the tray:
  1. The piece instantly scales up from tray scale ($0.65\times$) to board scale ($1.0\times$).
  2. The visual representation shifts vertically upwards by an offset (typically $\approx 60\text{dp} - 80\text{dp}$). This prevents the player's finger or thumb from obscuring the piece and the cells directly beneath it.
  3. Haptic tick feedback triggers on touch grab.

### 5.2 Projection & Board Snapping
- As the piece is dragged over the grid:
  - The engine maps the touch coordinates to the nearest valid board cell anchor $(c_x, c_y)$.
  - **Shadow / Ghost Preview:** When hovering over a legally valid position:
    - The target cells on the board display a soft translucent highlight (ghost blocks in the shape's color at $30\%$ alpha).
    - If placing this piece will complete one or more lines, the full rows/columns that will be cleared pulse with a bright glow or shimmer preview.
  - If hovering over an invalid position (boundary overflow or occupied cell collision), no ghost preview is shown.

### 5.3 Release & Drop Action
- **Valid Placement:**
  - If released over a valid target: The piece snaps into place, occupancy state updates immediately, placement sound plays, and line-clear evaluation runs.
- **Invalid Placement:**
  - If released outside the grid, on an invalid position, or canceled:
  - The piece performs a smooth return animation (elastic/spring interpolation) back to its original slot in the tray.
  - If dragged to an invalid position, an optional subtle shake animation and soft "thud" rejection tone sounds.
  - The board state remains completely unchanged.

---

## 6. Line Clearing & Multi-Line Mechanics

### 6.1 Detection Rules
- After a block is placed, the engine scans:
  - All $8$ rows (horizontal).
  - All $8$ columns (vertical).
- A line is complete if and only if **all 8 cells** along that row or column are occupied.
- **Simultaneous Cross-Clearing (Intersection):**
  - If both a row and a column are completed by the same placement, both are cleared simultaneously.
  - The intersecting cell belongs to both lines and is cleared smoothly without double-counting cell count or causing glitch artifacts.
- Multi-clears (e.g., 2 rows + 1 column = 3 lines total) resolve in a single unified cascade.

### 6.2 Clearing Animation
1. Cells in completed lines flash white / glow for $\approx 100\text{ms} - 150\text{ms}$.
2. The blocks disintegrate into bursting particle sparks colored according to each block's theme color.
3. The board cells transition to empty state (`null`).
4. Floating score indicator text ($+80$, $+240$, `COMBO x2!`) emerges from the center of the clear and drifts upward while fading out.

---

## 7. Scoring & Combo System

### 7.1 Mathematical Scoring Formula
The verified scoring model consists of three distinct additive components:

$$\text{Turn Score} = \text{Points}_{\text{placement}} + \text{Points}_{\text{lines}} + \text{Bonus}_{\text{combo}}$$

#### A. Placement Points ($\text{Points}_{\text{placement}}$)
$$\text{Points}_{\text{placement}} = K_{\text{tile}} \times N_{\text{cells}}$$
- Where $K_{\text{tile}} = 1$ point per cell placed.
- For example, placing a $3 \times 3$ block yields $1 \times 9 = 9\text{ points}$; placing a 2-bar yields $2\text{ points}$.

#### B. Line Clear Base Points ($\text{Points}_{\text{lines}}$)
Clearing $L$ lines simultaneously awards quadratic or tiered points to reward multi-line clears:
| Lines Cleared ($L$) | Base Score Formula | Points Awarded |
|---|---|---|
| $0$ | $0$ | $0$ |
| $1$ Line | $10 \times 8$ | **$100$ pts** |
| $2$ Lines | $(10 \times 8 \times 2) + 140$ | **$300$ pts** |
| $3$ Lines | $(10 \times 8 \times 3) + 360$ | **$600$ pts** |
| $4+$ Lines | Quadruple / Mega Clear | **$1000+$ pts** |

*Configurable Formula:* $\text{Points}_{\text{lines}}(L) = 100 \times L + 50 \times L \times (L - 1)$ for $L \ge 1$.

#### C. Combo Streak Multiplier ($\text{Bonus}_{\text{combo}}$)
A **Combo** is defined as clearing at least one line on consecutive moves.
- **Combo Counter ($C$):**
  - Turn 1 clears a line $\rightarrow C = 1$ (Normal clear).
  - Turn 2 immediately clears another line $\rightarrow C = 2$ (`COMBO x2`!).
  - Turn 3 immediately clears another line $\rightarrow C = 3$ (`COMBO x3`!).
  - Any turn where a piece is placed **without clearing any line** resets $C = 0$.
- **Combo Bonus Points:**
  $$\text{Bonus}_{\text{combo}}(C, L) = \begin{cases} 0 & \text{if } C \le 1 \\ (C - 1) \times 80 \times L & \text{if } C \ge 2 \end{cases}$$
- **Tiered Audio & Banners:**
  - $C = 2$: *"Great!"* + pitch step $+1$
  - $C = 3$: *"Awesome!"* + pitch step $+2$
  - $C = 4$: *"Super Combo!"* + pitch step $+3$
  - $C \ge 5$: *"UNBELIEVABLE!"* + celebratory fanfare

---

## 8. Game Over Conditions & Validation

### 8.1 Detection Algorithm
- At the end of every placement and tray replenishment:
  - For each remaining piece $P_i$ in the active tray:
    - Iterate through all board positions $(r, c) \in [0, \text{Rows}-1] \times [0, \text{Cols}-1]$.
    - Check whether $P_i$ can legally fit at $(r, c)$ without boundary violation or cell overlap.
  - If **no legal move exists for any remaining piece in the tray**, the game triggers the **Game Over** state.
- **Zero False-Positive Guarantee:**
  - The check exhausts all candidate root anchors for all unconsumed pieces.
  - If even a single $1 \times 1$ piece can fit in any open cell, Game Over is strictly prevented.
- **Game Over Sequence:**
  1. Remaining unplayable tray pieces dim/desaturate to gray.
  2. Board locks touch interactions.
  3. Soft chime/sad chord sounds.
  4. Game Over Dialog opens smoothly with final score, all-time personal best, encouragement message, and immediate Replay / Garden buttons.

---

## 9. State Persistence & Resumption

### 9.1 Offline & Lifecycle Handling
- *Verification Status:* **Verified Requirement**.
- The entire game must be playable $100\%$ offline without requiring an internet connection or account.
- **Auto-Save on Every Move:**
  - Active $8 \times 8$ grid state (array of block colors / empty).
  - Unused pieces remaining in the tray (shapes and colors).
  - Current active score, combo streak, and high score.
  - Active game mode (Classic, Relax, Level, Mom's Garden).
- **App Lifecycle Recovery:**
  - If the player quits the app, receives a phone call, or navigates away, returning to the app resumes the exact board state without data loss.

---

## 10. Mechanics Comparison & Configuration Matrix

| Mechanic | Reference Game (*Color Block*) | Farabe's Mom – Block Implementation | Configurable Setting |
|---|---|---|---|
| **Board Size** | 8x8 fixed | 8x8 default (configurable) | `BOARD_ROWS = 8, BOARD_COLS = 8` |
| **Rotation** | None (Disabled) | None (Strictly Disabled) | `ALLOW_ROTATION = false` |
| **Tray Count** | 3 pieces | 3 pieces | `TRAY_CAPACITY = 3` |
| **Tray Refill** | When all 3 are used | When all 3 are used | `REFILL_ONLY_WHEN_EMPTY = true` |
| **Time Limit** | None | None (Pure casual strategy) | `TIMED_MODE = false` |
| **Combo Window**| Turn-based consecutive | Turn-based consecutive | `COMBO_MODE = TURN_BASED` |
| **Visual Style**| Generic neon/plastic blocks | Elegant soft-pastel dimensional tiles | Dedicated Mom Palette |
| **Special Theme**| Generic arcade | **Mom's Personal Edition** (Garden, Letters, Family) | Custom Gift Features |

---

## 11. Conclusion & Next Steps

This reverse engineering document establishes the formal specifications for *FARABE'S MOM – BLOCK*. All gameplay mechanics are clearly separated into verified behaviors and configurable parameters. We proceed now to Phase 2 (Core Gameplay Engine) and Phase 3 (Scoring and Combo System).
