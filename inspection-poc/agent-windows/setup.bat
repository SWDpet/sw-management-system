@echo off
chcp 65001 > nul
title UPIS 점검 자동화 — 사이트 설정 wizard
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup.ps1" %*
echo.
pause
