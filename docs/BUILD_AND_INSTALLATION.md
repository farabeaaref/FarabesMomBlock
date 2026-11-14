# BUILD AND INSTALLATION GUIDE
## FARABE'S MOM – BLOCK

**Application Package:** `com.farabe.farabesmomblock`  
**Application Name:** FARABE'S MOM – BLOCK  
**Target Platform:** Android 7.0+ (API 24 to 34)  

---

## 1. Prerequisites

To build and run **FARABE'S MOM – BLOCK**, the following environment tools are required:

1. **JDK (Java Development Kit):** JDK 17 or higher (e.g. Microsoft Build of OpenJDK 17 or Eclipse Temurin 17).
2. **Android SDK:** API Level 34 (`platforms;android-34`) and Build Tools `34.0.0`.
3. **Android Studio:** Android Studio Hedgehog / Iguana / Jellyfish (2023.2+ or 2024.1+) or command-line Gradle.
4. **Gradle:** 8.7 (Included automatically via `./gradlew`).

---

## 2. Project Directory Structure

```
Farabe's Mom - Block/
├── app/
│   ├── build.gradle.kts          # Module build config, Compose, Room, DataStore
│   ├── proguard-rules.pro        # Proguard optimization rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/farabe/farabesmomblock/
│       │   │   ├── MainActivity.kt
│       │   │   ├── audio/SoundManager.kt
│       │   │   ├── data/
│       │   │   │   ├── datastore/PreferencesManager.kt
│       │   │   │   ├── db/AppDatabase.kt, GameEntities.kt, GameDao.kt
│       │   │   │   └── repository/GameRepository.kt
│       │   │   ├── engine/
│       │   │   │   ├── BlockColor.kt, BlockShape.kt, BlockShapeLibrary.kt
│       │   │   │   ├── Board.kt, MoveValidator.kt, LineClearManager.kt
│       │   │   │   ├── ScoringManager.kt, ComboManager.kt, BlockGenerator.kt
│       │   │   │   └── GameState.kt, GameEngine.kt
│       │   │   └── ui/
│       │   │       ├── components/ (BoardView, PieceTrayView, BlockTile, etc.)
│       │   │       ├── screens/ (Home, Game, Garden, Letters, Memory, Settings, Levels)
│       │   │       ├── theme/ (Color, Theme, Type)
│       │   │       └── viewmodel/GameViewModel.kt
│       │   └── res/ (drawable, values, mipmap icons)
│       └── test/ (Unit tests: BoardTest, MoveValidatorTest, LineClearTest, etc.)
├── docs/
│   ├── REFERENCE_GAME_ANALYSIS.md
│   ├── TECHNICAL_ARCHITECTURE.md
│   ├── TESTING_REPORT.md
│   └── BUILD_AND_INSTALLATION.md
├── build.gradle.kts              # Root build script
├── settings.gradle.kts           # Settings & repositories
├── gradle.properties             # Memory and compiler flags
├── local.properties              # Local Android SDK path
└── gradlew, gradlew.bat          # Gradle wrapper executables
```

---

## 3. Opening the Project in Android Studio

1. Launch **Android Studio**.
2. Select **Open** and choose the directory:
   `c:\Users\pasha\Desktop\Farabe's Mom - Block`
3. Allow Android Studio to sync the Gradle project.
4. Once sync completes, select an Android Emulator (API 24 to 34) or a connected physical Android device with USB Debugging enabled.
5. Click the green **Run** button (or press `Shift + F10`).

---

## 4. Command-Line Build Instructions

### 4.1 Running the Automated Test Suite
From the root project directory:
```bash
./gradlew test
```
(On Windows Command Prompt or PowerShell):
```powershell
.\gradlew.bat test
```
Test reports will be generated at:
`app/build/reports/tests/testDebugUnitTest/index.html`

### 4.2 Building the Debug APK
```powershell
.\gradlew.bat assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### 4.3 Building the Release APK
```powershell
.\gradlew.bat assembleRelease
```
The output APK is generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 5. Installing the APK on an Android Device

### Method A: Via Android Debug Bridge (ADB)
1. Enable **Developer Options** and **USB Debugging** on your Android phone.
2. Connect the phone via USB.
3. Run:
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Method B: Direct APK Transfer
1. Copy `app-debug.apk` to your phone's storage (or send via WhatsApp / Google Drive / USB).
2. On the phone, tap the `.apk` file and tap **Install**.
3. If prompted, allow "Install from Unknown Sources".

---

## 6. Offline Verification

Once installed, **FARABE'S MOM – BLOCK** requires **no internet connection**:
- All sound synthesis is procedural via native Android PCM AudioTrack.
- All game saves and records are stored locally in Room SQLite database (`farabes_mom_block.db`).
- All settings and high scores are preserved across reboots via Android Jetpack Preferences DataStore.
