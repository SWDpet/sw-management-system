@echo off
chcp 65001 > nul
title UPIS Inspection - Setup (GUI)
REM v0.3.0 setup.bat = GUI 진입점. 콘솔 wizard 는 setup-console.bat
REM ps1 파일은 UTF-8 BOM (PS 4.0 / Server 2012 R2 인코딩 호환).
REM 실행 결과/에러는 setup-gui.log 로 떨굼 — GUI 안 뜨면 그 로그 파일 1개만 보내면 진단 가능.
set "_LOG=%~dp0setup-gui.log"
> "%_LOG%" echo [%date% %time%] setup-gui.ps1 start
>>"%_LOG%" echo.
>>"%_LOG%" echo --- console host info ---
powershell.exe -NoProfile -Command "$PSVersionTable | Out-String; [Threading.Thread]::CurrentThread.GetApartmentState()" >> "%_LOG%" 2>&1
>>"%_LOG%" echo.
>>"%_LOG%" echo --- setup-gui.ps1 stdout/stderr ---
powershell.exe -NoProfile -ExecutionPolicy Bypass -STA -File "%~dp0setup-gui.ps1" -ConfigDir "%~dp0config" %* >> "%_LOG%" 2>&1
set "_RC=%ERRORLEVEL%"
>>"%_LOG%" echo.
>>"%_LOG%" echo [%date% %time%] setup-gui.ps1 exit code=%_RC%
echo.
echo setup-gui.ps1 종료 (exit code %_RC%). 로그: %_LOG%
echo.
pause
