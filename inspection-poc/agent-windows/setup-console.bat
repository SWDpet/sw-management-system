@echo off
chcp 65001 > nul
title UPIS Inspection - Setup (Console wizard)
REM Console Read-Host wizard. Use when GUI is not desired or for -NonInteractive automation.
REM Default entry is setup.bat (WinForms GUI).
REM Keep this file ASCII-only - CMD batch parser is fragile with non-ANSI bytes.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup.ps1" -ConfigDir "%~dp0config" %*
echo.
pause
