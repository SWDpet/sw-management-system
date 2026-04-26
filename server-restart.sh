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
