#!/bin/sh
# Linux — 라우팅 테이블
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="ip route"
if command -v ip >/dev/null 2>&1; then
    LINES=$(ip route 2>/dev/null)
else
    CMD="netstat -rn"
    LINES=$(netstat -rn 2>/dev/null)
fi

# 디폴트 게이트웨이 / 라우트 수만 추출 (raw 는 별도 보존)
DEFAULT=$(printf '%s\n' "$LINES" | awk '/^default/ || /^0\.0\.0\.0/{print; exit}')
COUNT=$(printf '%s\n' "$LINES" | grep -c .)

V=$(json_obj_start)
V=$(json_obj_add "$V" default_route "$(json_quote_or_null "$DEFAULT")")
V=$(json_obj_add "$V" route_count   "$(json_num_or_null "$COUNT")")
V=$(json_obj_end "$V")

# raw 는 너무 길면 자름 (4KB)
RAW=$(printf '%s' "$LINES" | head -c 4096)

CR_ID="ap.net.routes" CR_LABEL="라우팅 테이블" CR_CATEGORY="network" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="ok" CR_RAW="$RAW" new_check_result
