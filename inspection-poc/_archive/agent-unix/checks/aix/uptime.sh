#!/bin/sh
# AIX — 가동시간
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="uptime"
RAW=$(uptime 2>/dev/null)
# AIX uptime 출력: "  10:30AM   up  42 days, 12 hrs, 3 users, load average: 0.12, 0.08, 0.05"
DAYS=$(echo "$RAW" | sed -n 's/.*up *\([0-9]\+\) days*.*/\1/p')
HRS=$(echo "$RAW"  | sed -n 's/.*\([0-9]\+\) hrs*.*/\1/p')

V=$(json_obj_start)
V=$(json_obj_add "$V" days  "$(json_num_or_null "$DAYS")")
V=$(json_obj_add "$V" hours "$(json_num_or_null "$HRS")")
V=$(json_obj_end "$V")

CR_ID="ap.os.uptime" CR_LABEL="가동시간" CR_CATEGORY="performance" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="ok" CR_RAW="$RAW" new_check_result
