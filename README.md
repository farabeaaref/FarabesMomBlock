# FARABE'S MOM – BLOCK ❤️
### Complete Native Android Puzzle Game & Personal Gift

**Project Owner:** Farabe  
**Platform:** Android (API 24+)  
**Technology Stack:** Kotlin, Jetpack Compose, Material3, Room Database, Preferences DataStore, Procedural AudioTrack  
**Reverse Engineered Reference:** *Color Block: Combo Blast* by Ivy (`com.puzzlegames.puzzlebrickslegend`)  

---

## About The Project

**FARABE'S MOM – BLOCK** is a complete, production-grade native Android puzzle game crafted as a heartfelt personal gift from Farabe to his mother. 

The game combines the addictive, strategic polyomino block puzzle mechanics of the reference title with a warm, elegant visual identity, calming sound design, and personalized features designed specifically for Mom.

---

## Key Highlights

- **Authentic Block-Puzzle Gameplay & Fixes:**
  - $8 \times 8$ board dimensions with dimensional, softened pastel tiles.
  - **Unclipped Full-Screen Drag-and-Drop:** Smooth floating root drag overlay with upward finger lift offset so Mom's finger never obscures the pieces or target cells.
  - **Tap-to-Place Mode:** Complete accessibility option — tap any piece to select, then tap any board cell to place instantly!
  - 3-piece tray with guaranteed automatic replenishment when empty.
  - Real-time ghost preview and line-clear glow feedback.
  - True simultaneous row and column clearing with zero double-counting.
- **Nostalgic Bangladeshi Heritage Art on Every Block:**
  - **লাল শাপলা (Shapla):** Bangladesh's iconic water lily blossom.
  - **বেলি ফুল (Beli Phool):** Sweet fragrant jasmine garland.
  - **নকশী কাঁথা (Nakshi Kantha):** Traditional embroidered diamond quilt star stitch.
  - **জামদানি নকশা (Jamdani Weave):** Heritage royal geometric loom weave.
  - **রিকশা আর্ট (Rickshaw Art):** Vivid painted Dhaka folk art rose & flourishes.
  - **কৃষ্ণচূড়া (Krishnachura):** Fiery summer scarlet blossom of Bengal.
  - **মাটির প্রদীপ (Matir Prodip):** Traditional terracotta clay oil lamp with gentle flame.
  - **আলপনা (Alpona):** Traditional festive Bengali folk mandala swirl.
  - **দোয়েল (Doel):** National bird feather and wing motif.
- **Dynamic Juice & Visual Animations (Not Static!):**
  - **Ambient Living Breeze:** Gentle falling rose petals and glowing golden dust drifting across the background.
  - **Floating Score Numbers:** Dynamic `+100`, `+300`, `+600` score points floating upward on clears.
  - **Celebration Banners with Heartfelt Bengali Praise:** "অসাধারণ আম্মু! (Amazing Ammu!)", "COMBO x2! দারুণ!", "SUPER COMBO! সাবাশ মা!", "মায়ের জন্য ভালোবাসা ❤️".
- **Mom's Personal Edition:**
  - **Personal Welcome:** Dedicated warm greeting on launch.
  - **Mom's Garden:** Unlocks 18 beautiful flowers (Roses, Daisies, Tulips, Sunflowers, Lotus), plants, butterflies, and garden decorations through puzzle achievements.
  - **Secret Letters:** Heartwarming milestone letters from Farabe to Mom that unlock as she clears lines and scores points. All letters are fully editable.
  - **Family Memory:** A personal family tribute and note screen.
  - **Daily Surprises:** Daily positive messages and garden bloom seeds without punishment for missing days.
- **Game Modes:**
  - **Classic Mode:** Authentic endless puzzle experience.
  - **Relax Mode:** Stress-free casual puzzling with soothing pacing.
  - **Level Mode:** Handcrafted objective challenges.
  - **Mom's Garden:** Interactive progression garden.
- **100% Offline & Lightweight:**
  - Zero internet connection required.
  - Procedural harmonic chime synthesis (no missing MP3 assets).
  - Full local state preservation via Room SQLite and DataStore.
- **Quality Assured:**
  - Automated JUnit test suite verifying board boundaries, placement validation, line clearing, combo formulas, and game-over detection.

---

## Documentation

- [Reference Game Reverse Engineering Analysis](docs/REFERENCE_GAME_ANALYSIS.md)
- [Technical Architecture Specification](docs/TECHNICAL_ARCHITECTURE.md)
- [Testing Report](docs/TESTING_REPORT.md)
- [Build and Installation Instructions](docs/BUILD_AND_INSTALLATION.md)

---

## Local Testing (before sending to Mom)

Double-click **`run-local.bat`**. It will:
1. Use a plugged-in phone (USB debugging on) if one is connected, otherwise boot the **MomPhone** emulator (Pixel 6, Android 14).
2. Build the latest code and install it.
3. Launch the game.

Run it again after every change; progress on the test device is kept between rebuilds.
When the version is approved, use **`build-update.bat`** to build the final release APK.

## Quick Start (Build & Run)

```powershell
# Run the automated unit test suite
.\gradlew.bat test

# Build the debug APK
.\gradlew.bat assembleDebug

# Install on a connected Android device or emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

*Made with love, just for Mom. ❤️*
