#!/bin/sh
# Linux — 메모리 사용률
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="free -m"
LINE=$(free -m 2>/dev/null | awk '/^Mem:/{print $0; exit}')
if [ -n "$LINE" ]; then
    TOTAL=$(echo "$LINE" | awk '{print $2}')
    USED=$(echo "$LINE"  | awk '{print $3}')
    AVAIL=$(echo "$LINE" | awk '{print $7}')   # available 컬럼 (없으면 빈 값)
    if [ -n "$AVAIL" ] && [ "$AVAIL" -gt 0 ] 2>/dev/null; then
        USED_PCT=$(awk -v t="$TOTAL" -v a="$AVAIL" 'BEGIN{printf "%.1f", (t-a)*100/t}')
    else
        USED_PCT=$(awk -v t="$TOTAL" -v u="$USED" 'BEGIN{printf "%.1f", u*100/t}')
    fi
else
    USED_PCT=""
fi

ST=$(resolve_status "$USED_PCT" "${CFG_MEM_WARN:-80}" "${CFG_MEM_CRIT:-90}" 0)
TH=$(json_obj_start)
TH=$(json_obj_add "$TH" warn "$(json_num_or_null "${CFG_MEM_WARN:-80}")")
TH=$(json_obj_add "$TH" crit "$(json_num_or_null "${CFG_MEM_CRIT:-90}")")
TH=$(json_obj_end "$TH")

V=$(json_obj_start)
V=$(json_obj_add "$V" used_pct "$(json_num_or_null "$USED_PCT")")
V=$(json_obj_add "$V" total_mb "$(json_num_or_null "$TOTAL")")
V=$(json_obj_end "$V")

CR_ID="ap.perf.mem_pct" CR_LABEL="메모리 사용률" CR_CATEGORY="performance" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_THRESHOLD_JSON="$TH" \
  new_check_result
