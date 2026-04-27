#!/usr/bin/env bash
# =============================================================================
# phase1-guard.sh — phase1 DDL 정식화 스프린트 DB 접속 가드
# (sprint phase1-ddl-formalization, exec-plan v2 Step 0-B)
#
# 목적: 운영DB 우발 접속 + 비운영 단계 운영 좌표 매칭 차단.
#       모든 DB 접속 명령 앞에 source 후 phase1_guard 호출 필수.
#
# 사용법:
#   source .team-workspace/phase1-guard.sh
#   phase1_guard dump      "$ALLOWED_PROD_HOST" "$ALLOWED_PROD_PORT" \
#                          "$ALLOWED_PROD_DB"   "$ALLOWED_PROD_USER"
#   phase1_guard ephemeral "$ALLOWED_EPHEMERAL_HOST" "$ALLOWED_EPHEMERAL_PORT" \
#                          "$ALLOWED_EPHEMERAL_DB"   "$ALLOWED_EPHEMERAL_USER"
#
# 필수 환경변수 (사용자 사전 export):
#   ALLOWED_PROD_HOST, ALLOWED_PROD_PORT, ALLOWED_PROD_DB, ALLOWED_PROD_USER
#   ALLOWED_EPHEMERAL_HOST, ALLOWED_EPHEMERAL_PORT,
#   ALLOWED_EPHEMERAL_DB, ALLOWED_EPHEMERAL_USER
#
# 감사 로그: docs/exec-plans/phase1-audit.log
# =============================================================================

PHASE1_AUDIT_LOG="docs/exec-plans/phase1-audit.log"

phase1_guard() {
    local op="$1" host="$2" port="$3" db="$4" user="$5"
    local ts
    ts=$(date -Iseconds 2>/dev/null || date +%Y-%m-%dT%H:%M:%S%z)

    # 인자 검증
    if [ -z "$op" ] || [ -z "$host" ] || [ -z "$port" ] || [ -z "$db" ] || [ -z "$user" ]; then
        echo "phase1_guard: 사용법 — phase1_guard <op:dump|ephemeral|verify> <host> <port> <db> <user>" >&2
        return 2
    fi

    # 화이트리스트 환경변수 존재 검증
    case "$op" in
        dump)
            if [ -z "${ALLOWED_PROD_HOST:-}" ] || [ -z "${ALLOWED_PROD_PORT:-}" ] \
            || [ -z "${ALLOWED_PROD_DB:-}" ]   || [ -z "${ALLOWED_PROD_USER:-}" ]; then
                echo "phase1_guard: ALLOWED_PROD_* 환경변수 미설정" >&2
                echo "$ts HARD_STOP op=$op reason=missing_env" >> "$PHASE1_AUDIT_LOG"
                return 99
            fi
            if [ "$host" != "$ALLOWED_PROD_HOST" ] || [ "$port" != "$ALLOWED_PROD_PORT" ] \
            || [ "$db" != "$ALLOWED_PROD_DB" ]    || [ "$user" != "$ALLOWED_PROD_USER" ]; then
                echo "HARD STOP: dump 단계는 운영 화이트리스트만 허용" >&2
                echo "  요청: host=$host port=$port db=$db user=$user" >&2
                echo "  허용: host=$ALLOWED_PROD_HOST port=$ALLOWED_PROD_PORT db=$ALLOWED_PROD_DB user=$ALLOWED_PROD_USER" >&2
                echo "$ts HARD_STOP op=$op host=$host db=$db user=$user reason=whitelist_mismatch" >> "$PHASE1_AUDIT_LOG"
                return 99
            fi
            ;;
        ephemeral|verify)
            if [ -z "${ALLOWED_EPHEMERAL_HOST:-}" ] || [ -z "${ALLOWED_EPHEMERAL_PORT:-}" ] \
            || [ -z "${ALLOWED_EPHEMERAL_DB:-}" ]   || [ -z "${ALLOWED_EPHEMERAL_USER:-}" ]; then
                echo "phase1_guard: ALLOWED_EPHEMERAL_* 환경변수 미설정" >&2
                echo "$ts HARD_STOP op=$op reason=missing_env" >> "$PHASE1_AUDIT_LOG"
                return 99
            fi
            # 운영 좌표 매칭 시 즉시 차단 (운영 좌표가 환경변수에 있을 때만 비교)
            if [ -n "${ALLOWED_PROD_HOST:-}" ] && [ "$host" = "$ALLOWED_PROD_HOST" ]; then
                echo "HARD STOP: 비운영 단계에서 운영 호스트 감지: $host" >&2
                echo "$ts HARD_STOP op=$op host=$host reason=prod_host_in_nonprod" >> "$PHASE1_AUDIT_LOG"
                return 99
            fi
            if [ -n "${ALLOWED_PROD_DB:-}" ] && [ "$db" = "$ALLOWED_PROD_DB" ]; then
                echo "HARD STOP: 비운영 단계에서 운영 DB명 감지: $db" >&2
                echo "$ts HARD_STOP op=$op db=$db reason=prod_db_in_nonprod" >> "$PHASE1_AUDIT_LOG"
                return 99
            fi
            # ephemeral 화이트리스트 매칭
            if [ "$host" != "$ALLOWED_EPHEMERAL_HOST" ] || [ "$port" != "$ALLOWED_EPHEMERAL_PORT" ]; then
                echo "HARD STOP: ephemeral 화이트리스트 외 호스트: $host:$port" >&2
                echo "$ts HARD_STOP op=$op host=$host port=$port reason=ephemeral_whitelist_mismatch" >> "$PHASE1_AUDIT_LOG"
                return 99
            fi
            ;;
        *)
            echo "phase1_guard: 알 수 없는 op '$op' (dump|ephemeral|verify 만 허용)" >&2
            echo "$ts HARD_STOP op=$op reason=unknown_op" >> "$PHASE1_AUDIT_LOG"
            return 2
            ;;
    esac

    # 통과 — 감사 로그에 PASS 기록
    echo "$ts PASS op=$op host=$host port=$port db=$db user=$user" >> "$PHASE1_AUDIT_LOG"
    return 0
}

# 명시적 환경 검증 헬퍼 (Step 0-C 자동화)
phase1_preflight_check() {
    local rc=0
    local ts
    ts=$(date -Iseconds 2>/dev/null || date +%Y-%m-%dT%H:%M:%S%z)

    echo "[Step 0-C] 환경 검증 시작 ($ts)"

    # PG 클라이언트 자동 탐색 (Windows 표준 위치)
    if ! command -v psql >/dev/null 2>&1; then
        local pg_bin
        for pg_bin in \
            "$HOME/PostgreSQL"/*/bin \
            "/c/Program Files/PostgreSQL"/*/bin \
            "/c/Program Files (x86)/PostgreSQL"/*/bin; do
            if [ -x "$pg_bin/psql.exe" ]; then
                export PATH="$pg_bin:$PATH"
                echo "  [INFO] PG bin 자동 추가: $pg_bin"
                break
            fi
        done
    fi

    if ! command -v psql >/dev/null 2>&1; then
        echo "  [FAIL] psql 미설치 — winget install PostgreSQL.PostgreSQL.16 또는 PG 설치"; rc=1
    else
        local pg_ver
        pg_ver=$(psql --version | grep -oE '[0-9]+' | head -1)
        if [ -z "$pg_ver" ] || [ "$pg_ver" -lt 14 ]; then
            echo "  [FAIL] psql 버전 < 14 (현재: $(psql --version))"; rc=1
        else
            echo "  [OK] psql $(psql --version)"
        fi
    fi

    if ! command -v pg_dump >/dev/null 2>&1; then
        echo "  [FAIL] pg_dump 미설치"; rc=1
    else
        echo "  [OK] $(pg_dump --version)"
    fi

    # Docker — Step 8 에서만 필요. 미설치 시 WARN
    if ! command -v docker >/dev/null 2>&1; then
        echo "  [WARN] docker 미설치 — Step 8 검증 단계에서 필요 (그때까지 설치)"
    else
        echo "  [OK] $(docker --version)"
    fi

    # PGSSLMODE
    if [ -z "${PGSSLMODE:-}" ]; then
        echo "  [WARN] PGSSLMODE 미설정 — 운영 접속 시 export PGSSLMODE=require 권장"
    else
        echo "  [OK] PGSSLMODE=$PGSSLMODE"
    fi

    # 감사 로그 쓰기 가능 여부
    mkdir -p "$(dirname "$PHASE1_AUDIT_LOG")"
    if ! touch "$PHASE1_AUDIT_LOG" 2>/dev/null; then
        echo "  [FAIL] 감사 로그 작성 불가: $PHASE1_AUDIT_LOG"; rc=1
    else
        echo "  [OK] 감사 로그: $PHASE1_AUDIT_LOG"
    fi

    # 화이트리스트 환경변수
    for v in ALLOWED_PROD_HOST ALLOWED_PROD_PORT ALLOWED_PROD_DB ALLOWED_PROD_USER \
             ALLOWED_EPHEMERAL_HOST ALLOWED_EPHEMERAL_PORT \
             ALLOWED_EPHEMERAL_DB ALLOWED_EPHEMERAL_USER; do
        if [ -z "${!v:-}" ]; then
            echo "  [FAIL] $v 미설정"; rc=1
        else
            echo "  [OK] $v=${!v}"
        fi
    done

    if [ $rc -eq 0 ]; then
        echo "[Step 0-C] 환경 검증 통과 — Step 1 진행 가능"
        echo "$ts PREFLIGHT_PASS" >> "$PHASE1_AUDIT_LOG"
    else
        echo "[Step 0-C] 환경 검증 실패 — 위 [FAIL] 항목 수정 후 재실행"
        echo "$ts PREFLIGHT_FAIL rc=$rc" >> "$PHASE1_AUDIT_LOG"
    fi
    return $rc
}
