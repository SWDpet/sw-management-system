@echo off
chcp 65001 >nul
echo ========================================
echo  V005 노임단가 테이블 SQL 실행
echo ========================================
echo.

set PSQL="C:\Users\PUJ\PostgreSQL\16\bin\psql.exe"
set HOST=192.168.10.194
set PORT=5880
set DB=SW_Dept
set USER=postgres

echo 접속정보: %HOST%:%PORT%/%DB% (user: %USER%)
echo.

%PSQL% -h %HOST% -p %PORT% -U %USER% -d %DB% -f "%~dp0V005_wage_rate_table.sql"

echo.
if %ERRORLEVEL%==0 (
    echo [성공] SQL 실행이 완료되었습니다!
) else (
    echo [실패] SQL 실행 중 오류가 발생했습니다.
)
echo.
pause
