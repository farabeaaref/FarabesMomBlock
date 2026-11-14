@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot
set Path=%JAVA_HOME%\bin;%Path%
cd /d "%~dp0"

echo ====================================================
echo   FARABE'S MOM - BLOCK: BUILD NEW UPDATE FOR MOM ❤️
echo ====================================================
echo.
echo Building release APK...
call gradlew.bat assembleRelease

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed. Please check the error above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ====================================================
echo   BUILD COMPLETED SUCCESSFULLY!
echo ====================================================
echo.
echo Your updated APK is ready at:
echo   app\build\outputs\apk\release\app-release.apk
echo.
echo Reminder for releasing:
echo   1. Upload app-release.apk to your GitHub Release
echo   2. Update version.json with the new versionCode and download link
echo   3. Next time Mom opens the game, she gets the surprise update prompt!
echo ====================================================
pause
