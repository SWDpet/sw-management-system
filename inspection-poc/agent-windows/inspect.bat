@echo off
chcp 65001 >nul
cd /d "%~dp0"
title UPIS Inspection - Windows AP Agent

echo ============================================================
echo  UPIS Inspection Automation (Windows AP) v0.2.0
echo  Config: config\active.json (setup.bat 으로 변경 가능)
echo ============================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0inspect.ps1"
set RC=%ERRORLEVEL%

echo.
if %RC% NEQ 0 (
    echo [ERROR] inspect.ps1 exit code: %RC%
    echo         See out\check.log or console output above.
    pause
    exit /b %RC%
)

if exist "%~dp0out\summary.html" (
    echo [OK] Opening summary.html in browser...
    start "" "%~dp0out\summary.html"
) else (
    echo [WARN] out\summary.html not generated. Check inspect.ps1 output.
)

echo.
echo Inspection complete.
echo Scan QR codes with Galaxy Tab PWA scanner.
echo.
pause