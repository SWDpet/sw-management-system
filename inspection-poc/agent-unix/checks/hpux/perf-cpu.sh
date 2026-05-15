#!/bin/sh
# HP-UX — CPU 사용률 (sar 또는 vmstat)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="sar -u 1 2"
USED=""; USR=""; SYS=""
if command -v sar >/dev/null 2>&1; then
    # sar -u 1 2 출력 마지막 행에 Average 가 있음
    LINE=$(sar -u 1 2 2>/dev/null | awk '/Average/{print; exit}')
    if [ -z "$LINE" ]; then
        LINE=$(sar -u 1 2 2>/dev/null | awk 'END{print}')
    fi
    USR=$(echo "$LINE" | awk '{print $2}')
    SYS=$(echo "$LINE" | awk '{print $3}')
    ID=$(echo  "$LINE" | awk '{print $5}')
    [ -n "$ID" ] && USED=$(awk -v i="$ID" 'BEGIN{print 100 - i}')
elif command -v vmstat >/dev/null 2>&1; then
    CMD="vmstat 1 2 | tail -1"
    LINE=$(vmstat 1 2 2>/dev/null | awk 'END{print}')
    USR=$(echo "$LINE" | awk '{print $(NF-3)}')
    SYS=$(echo "$LINE" | awk '{print $(NF-2)}')
    ID=$(echo  "$LINE" | awk '{print $(NF-1)}')
    [ -n "$ID" ] && USED=$(awk -v i="$ID" 'BEGIN{print 100 - i}')
fi

ST=$(resolve_status "$USED" "${CFG_CPU_WARN:-70}" "${CFG_CPU_CRIT:-85}" 0)
TH=$(json_obj_start)
TH=$(json_obj_add "$TH" warn "$(json_num_or_null "${CFG_CPU_WARN:-70}")")
TH=$(json_obj_add "$TH" crit "$(json_num_or_null "${CFG_CPU_CRIT:-85}")")
TH=$(json_obj_end "$TH")

V=$(json_obj_start)
V=$(json_obj_add "$V" used_pct "$(json_num_or_null "$USED")")
V=$(json_obj_add "$V" user_pct "$(json_num_or_null "$USR")")
V=$(json_obj_add "$V" sys_pct  "$(json_num_or_null "$SYS")")
V=$(json_obj_end "$V")

CR_ID="ap.perf.cpu_pct" CR_LABEL="CPU 사용률" CR_CATEGORY="performance" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_THRESHOLD_JSON="$TH" \
  new_check_result
