@echo off
chcp 65001 > nul
title UPIS Inspection - Setup (Console wizard)
REM 콘솔 Read-Host 기반 wizard. 한글 IME 제약이 있는 환경 또는 -NonInteractive 자동화용.
REM GUI 가 기본이므로 일반 사용자는 setup.bat 더블클릭 권장.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup.ps1" -ConfigDir "%~dp0config" %*
echo.
pause
