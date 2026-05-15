@echo off
chcp 65001 >nul
cd /d "%~dp0"
title UPIS Inspection - Remote Unix via SSH

echo ============================================================
echo  UPIS Inspection (Remote Unix DB via SSH)
echo  Config: config\site.dyg.json
echo ============================================================
echo.

set REMOTE_KEY=%1
if "%REMOTE_KEY%"=="" set REMOTE_KEY=unix_db

echo [INFO] Remote key: %REMOTE_KEY%
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0inspect-remote.ps1" -ConfigPath "%~dp0config\site.dyg.json" -RemoteKey %REMOTE_KEY%
set RC=%ERRORLEVEL%

echo.
if %RC% NEQ 0 (
    echo [ERROR] inspect-remote.ps1 exit code: %RC%
    echo         Check SSH connectivity / config / agent-unix install on remote.
    pause
    exit /b %RC%
)

if exist "%~dp0out\summary-remote.html" (
    echo [OK] Opening summary-remote.html in browser...
    start "" "%~dp0out\summary-remote.html"
) else (
    echo [WARN] out\summary-remote.html not generated.
)

echo.
echo Remote inspection complete.
echo Scan QR codes with Galaxy Tab PWA scanner.
echo.
pause
