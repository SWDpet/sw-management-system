@echo off
chcp 65001 > nul
title UPIS Inspection - Setup (GUI)
REM v0.3.0 setup.bat = GUI 진입점. 콘솔 wizard 는 setup-console.bat
REM chcp 65001 은 콘솔 한글 cosmetic 용. ps1 파일은 UTF-8 BOM 으로 저장되어 PS 4.0 인코딩 호환.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-gui.ps1" -ConfigDir "%~dp0config" %*
echo.
pause
