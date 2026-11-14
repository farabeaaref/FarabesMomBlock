@echo off
cd /d "%~dp0"
echo.
echo ====================================================
echo   PUSHING FARABE'S MOM - BLOCK TO GITHUB
echo ====================================================
echo If a browser / GitHub sign-in window pops up, sign in once.
echo.
git push -u origin main
echo.
if errorlevel 1 (
    echo [ERROR] Push failed. Please check your GitHub login and try again.
) else (
    echo [SUCCESS] Everything is live on GitHub! Mom's phone will auto-sync from now on.
)
echo.
pause
