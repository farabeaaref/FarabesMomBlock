@echo off
cd /d "%~dp0"
echo Starting Farabe's Mom - Live Game Control Studio...
start "" "http://localhost:8787"
node backend\server.js
pause
