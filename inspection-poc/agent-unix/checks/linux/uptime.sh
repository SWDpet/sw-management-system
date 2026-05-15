#!/bin/sh
# Linux — 가동시간
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="uptime"
RAW=$(uptime 2>/dev/null)
if [ -r /proc/uptime ]; then
    SECS=$(awk '{print int($1)}' /proc/uptime)
    DAYS=$(( SECS / 86400 ))
    HRS=$(( (SECS % 86400) / 3600 ))
else
    SECS=""; DAYS=""; HRS=""
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" seconds "$(json_num_or_null "$SECS")")
V=$(json_obj_add "$V" days    "$(json_num_or_null "$DAYS")")
V=$(json_obj_add "$V" hours   "$(json_num_or_null "$HRS")")
V=$(json_obj_end "$V")

CR_ID="ap.os.uptime" CR_LABEL="가동시간" CR_CATEGORY="performance" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="ok" CR_RAW="$RAW" new_check_result
