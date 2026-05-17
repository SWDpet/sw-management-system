@echo off
chcp 65001 > nul
title UPIS Inspection - Setup (GUI)
REM v0.3.0 setup.bat = GUI entry. Console wizard = setup-console.bat
REM ps1 files saved as UTF-8 BOM for PS 4.0 (Server 2012 R2) compatibility.
REM stdout/stderr captured to setup-gui.log (log-first diagnosis).
REM Keep this file ASCII-only - CMD batch parser is fragile with non-ANSI bytes.
set "_LOG=%~dp0setup-gui.log"
echo [%date% %time%] setup-gui.ps1 start > "%_LOG%"
echo. >> "%_LOG%"
echo --- console host info --- >> "%_LOG%"
powershell.exe -NoProfile -Command "$PSVersionTable | Out-String; [Threading.Thread]::CurrentThread.GetApartmentState()" >> "%_LOG%" 2>&1
echo. >> "%_LOG%"
echo --- setup-gui.ps1 stdout/stderr --- >> "%_LOG%"
powershell.exe -NoProfile -ExecutionPolicy Bypass -STA -File "%~dp0setup-gui.ps1" -ConfigDir "%~dp0config" %* >> "%_LOG%" 2>&1
set "_RC=%ERRORLEVEL%"
echo. >> "%_LOG%"
echo [%date% %time%] setup-gui.ps1 exit code=%_RC% >> "%_LOG%"
echo.
echo setup-gui.ps1 finished (exit code %_RC%).
echo Log file: %_LOG%
echo.
pause
