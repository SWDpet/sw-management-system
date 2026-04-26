#!/bin/bash
# === SW Manager Server Restart (bash native) ===
# Avoids cmd.exe/PowerShell variable escaping issues entirely

# 스크립트가 위치한 디렉토리를 프로젝트 루트로 자동 인식 (어떤 PC에서도 동작)
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="$PROJECT_DIR/server.log"
PORT=9090

# [환경변수 자동 보정] DB_PASSWORD 가 현재 셸에 없으면 Windows 사용자 환경변수에서 읽어옴.
# (PowerShell 로 [Environment]::GetEnvironmentVariable) — Claude/IDE 가 오래 떠 있을 때 유용.
if [ -z "$DB_PASSWORD" ] && command -v powershell.exe >/dev/null 2>&1; then
    WIN_DB_PWD=$(powershell.exe -NoProfile -Command "[Environment]::GetEnvironmentVariable('DB_PASSWORD', 'User')" 2>/dev/null | tr -d '\r\n')
    if [ -n "$WIN_DB_PWD" ] && [ "$WIN_DB_PWD" != "null" ]; then
        export DB_PASSWORD="$WIN_DB_PWD"
        echo "[RESTART] DB_PASSWORD loaded from Windows User env var"
    fi
fi

if [ -z "$DB_PASSWORD" ]; then
    echo "[RESTART] ⚠ WARNING: DB_PASSWORD 환경변수가 설정되지 않았습니다."
    echo "[RESTART]   application.properties 의 기본값이 제거된 상태면 서버 부팅이 실패합니다."
fi

# [Pre-flight] application-local.properties 존재 + dummy 좌표 검증
# (harness-hardening-v1: prod 좌표 마스킹 이후 신규 PC 셋업 자동화)
LOCAL_PROPS="$PROJECT_DIR/src/main/resources/application-local.properties"
EXAMPLE_PROPS="${LOCAL_PROPS}.example"

if [ ! -f "$LOCAL_PROPS" ]; then
    if [ ! -f "$EXAMPLE_PROPS" ]; then
        echo "[RESTART] ❌ application-local.properties + example 둘 다 없음. 셋업 가이드 확인."
        echo "[RESTART]    docs/references/setup-guide.md §2"
        exit 1
    fi
    cp "$EXAMPLE_PROPS" "$LOCAL_PROPS"
    echo "[RESTART] ⚠ 첫 실행: application-local.properties 가 없어서 example 에서 자동 복사함."
    echo "[RESTART]    → 1Password 에서 prod DB 좌표를 가져와 아래 파일 편집:"
    echo "[RESTART]      $LOCAL_PROPS"
    echo "[RESTART]    → spring.datasource.url 의 host:port/db 를 실제 값으로 교체"
    echo "[RESTART]    → 안내: docs/references/setup-guide.md §2"
    echo "[RESTART]    → 편집 후 './server-restart.sh' 다시 실행"
    exit 1
fi

if grep -q "localhost:5432/swdept_local" "$LOCAL_PROPS" && [ -z "$DB_URL" ]; then
    echo "[RESTART] ❌ application-local.properties 가 dummy 좌표 (localhost:5432/swdept_local) 그대로이고"
    echo "[RESTART]    DB_URL 환경변수도 미설정 → prod DB 접근 불가."
    echo "[RESTART]    → 1Password 에서 prod 좌표를 가져와 아래 파일 편집:"
    echo "[RESTART]      $LOCAL_PROPS"
    exit 1
fi

echo "[RESTART] Stopping existing server on port $PORT..."

# Find and kill process on port using pure bash + Windows netstat
PID=$(netstat -ano 2>/dev/null | grep ":${PORT}" | grep "LISTENING" | awk '{print $5}' | head -1)
if [ -n "$PID" ] && [ "$PID" != "0" ]; then
    echo "[RESTART] Killing PID $PID..."
    taskkill //F //PID "$PID" >/dev/null 2>&1
    sleep 2
else
    echo "[RESTART] No server running on port $PORT"
fi

# Clear old log
> "$LOG_FILE"

echo "[RESTART] Starting server..."
cd "$PROJECT_DIR"
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local > "$LOG_FILE" 2>&1 &
SERVER_PID=$!
echo "[RESTART] Maven PID: $SERVER_PID"

# Wait for startup
echo "[RESTART] Waiting for startup..."
for i in $(seq 1 30); do
    sleep 2
    if grep -q "Started SwManagerApplication" "$LOG_FILE" 2>/dev/null; then
        echo "[RESTART] Server started successfully! (~$((i*2)) seconds)"
        echo "[RESTART] http://localhost:$PORT"
        exit 0
    fi
    if grep -q "APPLICATION FAILED TO START" "$LOG_FILE" 2>/dev/null; then
        echo "[RESTART] ERROR: Application failed to start!"
        grep -A 20 "APPLICATION FAILED TO START" "$LOG_FILE"
        exit 1
    fi
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
        echo "[RESTART] ERROR: Process died unexpectedly"
        tail -30 "$LOG_FILE"
        exit 1
    fi
done

echo "[RESTART] TIMEOUT: Server did not start in 60s"
tail -30 "$LOG_FILE"
exit 1
