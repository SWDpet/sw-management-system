#!/bin/sh
# Solaris — fmdump (fault management) 또는 messages
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="fmdump -e"
ST="ok"; ERR_CNT=""
if command -v fmdump >/dev/null 2>&1; then
    SINCE=$(perl -e 'use POSIX; print POSIX::strftime("%b %d %H:%M:%S %Y", localtime(time()-86400));' 2>/dev/null)
    if [ -n "$SINCE" ]; then
        ERR_CNT=$(fmdump -e -t "$SINCE" 2>/dev/null | awk 'NR>1' | grep -c .)
    else
        ERR_CNT=$(fmdump -e 2>/dev/null | awk 'NR>1' | grep -c .)
    fi
elif [ -r /var/adm/messages ]; then
    CMD="/var/adm/messages"
    ERR_CNT=$(grep -i -E '(error|fail|fatal)' /var/adm/messages 2>/dev/null | wc -l | tr -d ' ')
else
    ST="error"
fi

if [ -n "$ERR_CNT" ] && [ "$ERR_CNT" -ge 10 ] 2>/dev/null; then
    ST="crit"
elif [ -n "$ERR_CNT" ] && [ "$ERR_CNT" -ge 1 ] 2>/dev/null; then
    ST="warn"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" error_24h "$(json_num_or_null "$ERR_CNT")")
V=$(json_obj_end "$V")

CR_ID="ap.log.system" CR_LABEL="시스템 에러 로그 (24h)" CR_CATEGORY="log" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" new_check_result
