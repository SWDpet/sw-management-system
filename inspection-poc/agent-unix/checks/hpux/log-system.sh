#!/bin/sh
# HP-UX — 시스템 로그 (/var/adm/syslog/syslog.log 24h)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

LOG="/var/adm/syslog/syslog.log"
CMD="tail $LOG | grep -i error"
ST="ok"; ERR_CNT=""; WARN_CNT=""

if [ -r "$LOG" ]; then
    # 어제~오늘만 (BMonth Day 패턴)
    TODAY=$(date '+%b %e' 2>/dev/null)
    YEST=$(perl -e 'use POSIX; print POSIX::strftime("%b %e", localtime(time()-86400));' 2>/dev/null)
    PAT="${TODAY}\\|${YEST}"
    ERR_CNT=$(grep -i "$PAT" "$LOG" 2>/dev/null | grep -i -E '(error|fail|fatal)' | wc -l | tr -d ' ')
    WARN_CNT=$(grep -i "$PAT" "$LOG" 2>/dev/null | grep -i 'warn' | wc -l | tr -d ' ')
else
    ST="error"
fi

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
