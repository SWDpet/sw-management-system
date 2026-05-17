#!/bin/sh
# Linux — CPU 사용률 (1초 샘플)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="vmstat 1 2 | tail -1"
# vmstat 1 2 의 두 번째 라인은 1초 평균 (첫 라인은 부팅 이래 평균이라 의미 없음)
LINE=$(vmstat 1 2 2>/dev/null | awk 'END{print}')
if [ -n "$LINE" ]; then
    ID=$(echo "$LINE" | awk '{print $(NF-2)}')      # idle 컬럼 (id)
    USR=$(echo "$LINE" | awk '{print $(NF-4)}')
    SYS=$(echo "$LINE" | awk '{print $(NF-3)}')
    USED=$(awk -v i="$ID" 'BEGIN{print 100 - i}')
else
    USED=""
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
