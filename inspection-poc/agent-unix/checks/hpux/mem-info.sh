#!/bin/sh
# HP-UX — RAM 구성 (machinfo 또는 dmesg)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="machinfo | grep -i memory"
ST="ok"; TOTAL_MB=""
if command -v machinfo >/dev/null 2>&1; then
    # "Memory: 16384 MB (16 GB)"
    LINE=$(machinfo 2>/dev/null | awk -F: '/^[ \t]*[Mm]emory/{print $2; exit}')
    TOTAL_MB=$(echo "$LINE" | awk '{for(i=1;i<=NF;i++) if($i=="MB"){print $(i-1); exit}}')
else
    ST="error"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" total_mb "$(json_num_or_null "$TOTAL_MB")")
V=$(json_obj_end "$V")

CR_ID="ap.hw.memory" CR_LABEL="RAM 구성" CR_CATEGORY="hw_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" new_check_result
