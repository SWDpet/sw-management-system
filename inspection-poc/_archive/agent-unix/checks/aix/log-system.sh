#!/bin/sh
# AIX — 시스템 에러 로그 (errpt, 최근 24h)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="errpt -s \"\$(date -u +%m%d%H%M%y)\" | wc -l"
ST="ok"; ERR_CNT=""
if command -v errpt >/dev/null 2>&1; then
    # 24h 전부터의 에러 수 (헤더 1줄 제외)
    SINCE=$(perl -e 'use POSIX; print POSIX::strftime("%m%d%H%M%y", localtime(time()-86400));' 2>/dev/null)
    if [ -n "$SINCE" ]; then
        RAW=$(errpt -s "$SINCE" 2>/dev/null)
        ERR_CNT=$(echo "$RAW" | awk 'NR>1' | grep -c .)
    else
        RAW=$(errpt 2>/dev/null)
        ERR_CNT=$(echo "$RAW" | awk 'NR>1' | grep -c .)
    fi
else
    ST="error"
fi

# 임계: 0=ok, 1~9=warn, 10+=crit
if [ -n "$ERR_CNT" ] && [ "$ERR_CNT" -ge 10 ] 2>/dev/null; then
    ST="crit"
elif [ -n "$ERR_CNT" ] && [ "$ERR_CNT" -ge 1 ] 2>/dev/null; then
    ST="warn"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" error_24h "$(json_num_or_null "$ERR_CNT")")
V=$(json_obj_end "$V")

CR_ID="ap.log.system" CR_LABEL="시스템 에러 로그 (errpt, 24h)" CR_CATEGORY="log" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" new_check_result
