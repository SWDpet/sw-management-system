@echo off
chcp 65001 > nul
title UPIS Inspection - Setup Wizard
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup.ps1" -ConfigDir "%~dp0config" %*
echo.
pause
