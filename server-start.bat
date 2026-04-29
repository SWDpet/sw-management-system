@echo off
REM === SW Manager Server Start Script ===
cd /d "%~dp0"

REM [Pre-flight] application-local.properties 존재 + dummy 좌표 검증
REM (harness-hardening-v1: prod 좌표 마스킹 이후 신규 PC 셋업 자동화)
set LOCAL_PROPS=src\main\resources\application-local.properties
set EXAMPLE_PROPS=%LOCAL_PROPS%.example
if not exist "%LOCAL_PROPS%" (
    if not exist "%EXAMPLE_PROPS%" (
        echo [START] ERROR: application-local.properties + example 둘 다 없음.
        echo [START]   docs/references/setup-guide.md §2
        exit /b 1
    )
    copy "%EXAMPLE_PROPS%" "%LOCAL_PROPS%" >nul
    echo [START] WARN: 첫 실행 — application-local.properties 가 없어서 example 에서 자동 복사함.
    echo [START]   - 1Password 에서 prod DB 좌표를 가져와 다음 파일 편집:
    echo [START]     %LOCAL_PROPS%
    echo [START]   - spring.datasource.url 의 host:port/db 를 실제 값으로 교체
    echo [START]   - 안내: docs/references/setup-guide.md §2
    echo [START]   - 편집 후 server-restart.bat 다시 실행
    exit /b 1
)
findstr /c:"localhost:5432/swdept_local" "%LOCAL_PROPS%" >nul 2>&1
if %ERRORLEVEL%==0 if "%DB_URL%"=="" (
    echo [START] ERROR: application-local.properties 가 dummy 좌표 (localhost:5432/swdept_local) 그대로이고
    echo [START]   DB_URL 환경변수도 미설정 → prod DB 접근 불가.
    echo [START]   1Password 에서 prod 좌표를 가져와 다음 파일 편집:
    echo [START]     %LOCAL_PROPS%
    exit /b 1
)

REM Stop existing server first
call server-stop.bat

echo [START] Starting SW Manager on port 8080...
start /b mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local > server.log 2>&1

REM Wait for startup
echo [START] Waiting for server to start...
set RETRY=0
:WAIT_LOOP
if %RETRY% GEQ 30 (
    echo [START] ERROR: Server did not start within 60 seconds.
    echo [START] Check server.log for details.
    exit /b 1
)
timeout /t 2 /nobreak >nul
findstr /c:"Started SwManagerApplication" server.log >nul 2>&1
if %ERRORLEVEL%==0 (
    echo [START] Server started successfully!
    echo [START] Access: http://localhost:8080
    exit /b 0
)
set /a RETRY+=1
goto WAIT_LOOP
