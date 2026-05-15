#!/bin/sh
# Linux — 시스템 로그 에러/경고 카운트 (최근 24h)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

if command -v journalctl >/dev/null 2>&1; then
    CMD="journalctl --since=-24h -p err"
    ERR_CNT=$(journalctl --since=-24h -p err --no-pager 2>/dev/null | wc -l | tr -d ' ')
    WARN_CNT=$(journalctl --since=-24h -p warning --no-pager 2>/dev/null | wc -l | tr -d ' ')
elif [ -r /var/log/messages ]; then
    CMD="/var/log/messages (24h)"
    SINCE=$(date -d 'yesterday' '+%b %_d' 2>/dev/null)
    ERR_CNT=$(grep -i -E '(error|fail|fatal)' /var/log/messages 2>/dev/null | wc -l | tr -d ' ')
    WARN_CNT=$(grep -i 'warn' /var/log/messages 2>/dev/null | wc -l | tr -d ' ')
else
    CMD="dmesg"
    ERR_CNT=$(dmesg 2>/dev/null | grep -i -E '(error|fail|fatal)' | wc -l | tr -d ' ')
    WARN_CNT=$(dmesg 2>/dev/null | grep -i 'warn' | wc -l | tr -d ' ')
fi

# 임계: error 가 0 이면 ok, 1+ 이면 warn, 10+ 이면 crit
ST="ok"
if [ -n "$ERR_CNT" ] && [ "$ERR_CNT" -ge 10 ] 2>/dev/null; then
    ST="crit"
elif [ -n "$ERR_CNT" ] && [ "$ERR_CNT" -ge 1 ] 2>/dev/null; then
    ST="warn"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" error_24h   "$(json_num_or_null "$ERR_CNT")")
V=$(json_obj_add "$V" warning_24h "$(json_num_or_null "$WARN_CNT")")
V=$(json_obj_end "$V")

CR_ID="ap.log.system" CR_LABEL="시스템 로그 (24h)" CR_CATEGORY="log" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" new_check_result
