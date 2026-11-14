# TECHNICAL ARCHITECTURE SPECIFICATION
## FARABE'S MOM – BLOCK

**Application Package:** `com.farabe.farabesmomblock`  
**Target Platform:** Android 7.0+ (API 24 to 34)  
**Primary Architecture:** Jetpack Compose + MVVM + Clean Engine Separation + Room + Preferences DataStore  
**Document Version:** 1.0.0  

---

## 1. Architectural Philosophy & Layer Separation

The application architecture enforces strict decoupling between pure puzzle game logic and Android UI frameworks. This separation guarantees 100% deterministic unit testability, zero UI latency, and bug-free state persistence.

```
┌─────────────────────────────────────────────────────────────┐
│                    Jetpack Compose UI                       │
│  MainActivity ── AppNavigation ── Screens (Game, Home, etc.) │
└──────────────────────────────▲──────────────────────────────┘
                               │ StateFlow / Events
┌──────────────────────────────┴──────────────────────────────┐
│                    GameViewModel (MVVM)                     │
│  Touch Drag Mapping ── Audio Triggers ── Milestone Checks   │
└───────────────▲──────────────────────────────▲──────────────┘
                │                              │
┌───────────────┴─────────────┐ ┌──────────────┴──────────────┐
│     Core Gameplay Engine    │ │     Data & Storage Layer    │
│  - Board & BlockShapeLibrary│ │  - AppDatabase (Room)       │
│  - MoveValidator (Bounds)   │ │  - PreferencesManager       │
│  - LineClearManager         │ │  - GameRepository           │
│  - Scoring & ComboManager   │ └─────────────────────────────┘
│  - BlockGenerator (Fair RNG)│
└─────────────────────────────┘
```

---

## 2. Core Gameplay Engine (`com.farabe.farabesmomblock.engine`)

The engine is written in pure Kotlin without any Android platform dependencies (`android.*` or `androidx.*`).

### 2.1 `Board.kt`
- Implements an immutable grid state representing an $8 \times 8$ matrix of nullable `BlockColor`.
- `placeShape(shape, startRow, startCol)`: Returns a new `Board` instance with the placed polyomino.
- `clearLines(rowsToClear, colsToClear)`: Erases completed rows and columns simultaneously.
- `serialize()` / `deserialize()`: Ultra-compact string encoding for zero-overhead Room and DataStore persistence.

### 2.2 `BlockShapeLibrary.kt` & `BlockShape.kt`
- Master catalog of all 19 standard polyomino shapes:
  - 1-cell (Dot)
  - 2-cell (Domino H & V)
  - 3-cell (Triomino H, V, Mini-L / Corners in 4 orientations)
  - 4-cell (Tetromino 4-Bar H & V, Square 2x2, L-shapes in 4 orientations, T-shapes in 4 orientations, Z and S shapes)
  - 5-cell (Pentomino 5-Bar H & V, Large L-corners in 4 orientations)
  - 9-cell (Giant Square 3x3)
- **Strict Rotation Lock:** As dictated by reference game mechanics, polyomino rotation is strictly prohibited.

### 2.3 `MoveValidator.kt`
- `isValidPlacement(board, shape, startRow, startCol)`: Verifies board boundary containment and cell-by-cell non-collision.
- `canFitAnywhere(board, shape)`: Exhaustively verifies whether a candidate polyomino has at least one valid cell anchor on the board.
- `isGameOver(board, trayPieces)`: Checks whether **all** remaining active pieces in the tray have zero valid moves on the board. **Guarantees zero false game-over triggers.**

### 2.4 `LineClearManager.kt`
- Detects fully occupied rows and columns.
- Supports simultaneous cross clears (row and column intersection) without double-counting or glitching.
- `previewLinesIfPlaced()`: Pre-calculates rows and columns that will clear when a piece is placed at candidate $(r, c)$, enabling real-time glowing shimmer lines during dragging.

### 2.5 `ScoringManager.kt` & `ComboManager.kt`
- Mathematical formula:
  $$\text{Score} = \text{Placement} + \text{LineClear} + \text{ComboBonus} + \text{AllClear}$$
  - $\text{Placement} = 1 \times N_{\text{cells}}$
  - $\text{LineClear}(L) = 100 \times L + 50 \times L \times (L - 1)$
  - $\text{ComboBonus}(C, L) = (C - 1) \times 80 \times L \quad (\text{for } C \ge 2)$
  - $\text{AllClear} = +500\text{ pts}$ if board is completely cleared.
- Combo manager tracks consecutive turns. If a turn clears lines, streak increases and triggers celebratory audio pitch steps. If no lines clear, combo resets to zero.

### 2.6 `BlockGenerator.kt`
- Generates triplets of 3 pieces with weighted probability.
- **Fairness Guarantee:** Validates that at least one generated piece can legally fit on the current board (unless board is fully saturated), preventing unfair instantaneous losses upon tray replenishment.

---

## 3. UI Layer & Drag-and-Drop Interaction

### 3.1 Touch Lift Offset
To resolve thumb occlusion on mobile touchscreens:
- When a user touches a piece in `PieceTrayView`, a vertical lift offset ($-210\text{px} \approx -70\text{dp}$) is applied.
- The piece scales smoothly from tray scale ($0.72\times$) to board scale ($1.05\times$).
- The player sees both the piece and the cells directly underneath their fingertip.

### 3.2 Coordinate Projection & Snapping
- Touch screen coordinates are converted into root window coordinates via Compose `LayoutCoordinates.positionInRoot()`.
- The relative offset against `BoardView` bounds is converted into cell indices:
  $$\text{col} = \left\lfloor \frac{X_{\text{rel}} - \frac{(W_{\text{shape}} - 1) \cdot W_{\text{cell}}}{2}}{W_{\text{cell}}} \right\rfloor, \quad \text{row} = \left\lfloor \frac{Y_{\text{rel}} - \frac{(H_{\text{shape}} - 1) \cdot H_{\text{cell}}}{2}}{H_{\text{cell}}} \right\rfloor$$
- If legal, `ghostCells` and `willClearRows`/`willClearCols` trigger instant visual feedback.
- If dropped outside or illegal, an elastic spring animation (`Animatable` with `Spring.DampingRatioMediumBouncy`) snaps the piece back to its tray slot.

---

## 4. Mom's Personal Edition Systems

1. **Mom's Garden:**
   - 18 collectible flora, fauna, and decorations (Pink Roses, Sunflowers, Monarch Butterflies, Water Fountain, Starlight Dreams).
   - Milestone progression: Unlocked automatically as Mom achieves scores and line totals.
2. **Secret Letters:**
   - Heartfelt, editable letters from Farabe unlocked at milestone tiers.
   - Includes reading envelope dialog with full content editing support.
3. **Family Memory:**
   - Locally stored family photo and personal tribute note.
4. **Daily Surprises:**
   - Friendly greeting and garden seed gifts each new day.

---

## 5. Procedural Sound & Tactile Haptics (`SoundManager.kt`)

- Uses native Android `AudioTrack` with real-time PCM synthesis:
  - Line Clear: Rising melodic major chord (C5, G5, C6).
  - Combo: Ascending pentatonic arpeggio with pitch scaling ($+12\%$ per streak tier).
  - Placement: Crisp low-frequency percussive snap.
  - Background Music: Calming ambient harmonic pads in Cmaj7 and Fmaj7.
- Uses Android `Vibrator` / `VibratorManager` for subtle tactile haptic pulses on touches and clears.
- Fully offline, 100% zero network dependency.
