#!/bin/sh
# AIX — RAM 구성 (svmon -G 의 memory line)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="svmon -G -O unit=MB"
ST="ok"; TOTAL=""; USED=""; FREE=""
if command -v svmon >/dev/null 2>&1; then
    LINE=$(svmon -G -O unit=MB 2>/dev/null | awk '/^memory/{print; exit}')
    # 컬럼: size inuse free pin virtual ...
    TOTAL=$(echo "$LINE" | awk '{print $2}')
    USED=$(echo  "$LINE" | awk '{print $3}')
    FREE=$(echo  "$LINE" | awk '{print $4}')
else
    ST="error"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" total_mb "$(json_num_or_null "$TOTAL")")
V=$(json_obj_add "$V" used_mb  "$(json_num_or_null "$USED")")
V=$(json_obj_add "$V" free_mb  "$(json_num_or_null "$FREE")")
V=$(json_obj_end "$V")

CR_ID="ap.hw.memory" CR_LABEL="RAM 구성" CR_CATEGORY="hw_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" new_check_result
