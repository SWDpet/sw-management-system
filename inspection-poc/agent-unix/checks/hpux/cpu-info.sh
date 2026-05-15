#!/bin/sh
# HP-UX — CPU 구성 (machinfo)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="machinfo"
ST="ok"; CORES=""; MODEL=""; CLOCK_GHZ=""
if command -v machinfo >/dev/null 2>&1; then
    RAW=$(machinfo 2>/dev/null)
    CORES=$(echo "$RAW" | awk '/Number of CPUs/{for(i=1;i<=NF;i++) if($i ~ /^[0-9]+$/){print $i; exit}}')
    MODEL=$(echo "$RAW" | awk -F: '/Family:/{sub(/^ */,"",$2); print $2; exit}')
    MHZ=$(echo "$RAW"   | awk '/Clock speed/{for(i=1;i<=NF;i++) if($i ~ /^[0-9]+$/){print $i; exit}}')
    [ -n "$MHZ" ] && CLOCK_GHZ=$(awk -v m="$MHZ" 'BEGIN{printf "%.2f", m/1000}')
else
    ST="error"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" cores     "$(json_num_or_null "$CORES")")
V=$(json_obj_add "$V" model     "$(json_quote_or_null "$MODEL")")
V=$(json_obj_add "$V" clock_ghz "$(json_num_or_null "$CLOCK_GHZ")")
V=$(json_obj_end "$V")

CR_ID="ap.hw.cpu" CR_LABEL="CPU 구성" CR_CATEGORY="hw_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" new_check_result
