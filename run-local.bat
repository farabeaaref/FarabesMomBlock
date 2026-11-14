@echo off
setlocal
REM ==========================================================
REM  FARABE'S MOM - BLOCK : LOCAL TEST (emulator or USB phone)
REM  Double-click to build the latest code and launch it.
REM ==========================================================
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot"
set "ANDROID_SDK_ROOT=C:\Android\Sdk"
set "ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
set "EMU=%ANDROID_SDK_ROOT%\emulator\emulator.exe"
set "AVD=MomPhone"
set "Path=%JAVA_HOME%\bin;%Path%"
cd /d "%~dp0"

echo.
echo [1/4] Checking for a connected phone or running emulator...
"%ADB%" start-server >nul 2>&1
set "DEVICE="
for /f "skip=1 tokens=1,2" %%a in ('"%ADB%" devices') do if "%%b"=="device" set "DEVICE=%%a"

if defined DEVICE goto booted

echo       None found. Starting emulator "%AVD%"...
start "MomPhone Emulator" "%EMU%" -avd %AVD% -no-snapshot-load -no-metrics -crash-report-mode never -gpu host
echo       Waiting for the emulator to boot ^(usually 15-30 seconds^)...
"%ADB%" wait-for-device

:waitboot
for /f %%b in ('"%ADB%" shell getprop sys.boot_completed 2^>nul') do if "%%b"=="1" goto booted
timeout /t 2 /nobreak >nul
goto waitboot

:booted
echo       Device ready.

echo.
echo [2/4] Building latest debug version...
call gradlew.bat installDebug --console=plain
if errorlevel 1 (
    echo.
    echo [ERROR] Build failed - see the messages above.
    pause
    exit /b 1
)

echo.
echo [3/4] Launching the game...
"%ADB%" shell am start -n com.farabe.farabesmomblock/.MainActivity >nul

echo.
echo [4/4] Done! The game is running. Play, test, and tell Antigravity what to change.
echo       Your progress on the test device is kept between rebuilds.
echo.
pause
