#!/bin/sh
# Linux — RAM 구성
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="free -m"
RAW=$(free -m 2>/dev/null)
if [ -n "$RAW" ]; then
    LINE=$(echo "$RAW" | awk '/^Mem:/{print $0; exit}')
    TOTAL_MB=$(echo "$LINE" | awk '{print $2}')
    USED_MB=$(echo "$LINE"  | awk '{print $3}')
    FREE_MB=$(echo "$LINE"  | awk '{print $4}')
else
    CMD="/proc/meminfo"
    TOTAL_KB=$(awk '/^MemTotal:/{print $2; exit}' /proc/meminfo 2>/dev/null)
    FREE_KB=$(awk '/^MemAvailable:/{print $2; exit}' /proc/meminfo 2>/dev/null)
    [ -n "$TOTAL_KB" ] && TOTAL_MB=$(( TOTAL_KB / 1024 ))
    [ -n "$FREE_KB"  ] && FREE_MB=$(( FREE_KB  / 1024 ))
    [ -n "$TOTAL_MB" ] && [ -n "$FREE_MB" ] && USED_MB=$(( TOTAL_MB - FREE_MB ))
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" total_mb "$(json_num_or_null "$TOTAL_MB")")
V=$(json_obj_add "$V" used_mb  "$(json_num_or_null "$USED_MB")")
V=$(json_obj_add "$V" free_mb  "$(json_num_or_null "$FREE_MB")")
V=$(json_obj_end "$V")

CR_ID="ap.hw.memory" CR_LABEL="RAM 구성" CR_CATEGORY="hw_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="ok" new_check_result
