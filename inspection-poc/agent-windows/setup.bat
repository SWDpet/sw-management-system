@echo off
title UPIS Inspection - Setup (GUI)
REM v0.3.0 부터 setup.bat 는 GUI 폼 진입점. 콘솔 wizard 가 필요하면 setup-console.bat 사용.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-gui.ps1" -ConfigDir "%~dp0config" %*
echo.
pause
