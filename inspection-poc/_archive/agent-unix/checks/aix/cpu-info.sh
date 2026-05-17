#!/bin/sh
# AIX — CPU 구성 (lparstat / prtconf)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="prtconf | grep -E 'Number Of Processors|Processor Clock|Processor Type'"
ST="ok"; CORES=""; MHZ=""; MODEL=""
if command -v prtconf >/dev/null 2>&1; then
    RAW=$(prtconf 2>/dev/null)
    CORES=$(echo "$RAW" | awk -F: '/^Number Of Processors:/ {gsub(/ /,"",$2); print $2; exit}')
    MHZ_LINE=$(echo "$RAW" | awk -F: '/^Processor Clock Speed:/ {print $2; exit}')
    # "2200 MHz" → 2200
    MHZ=$(echo "$MHZ_LINE" | awk '{print $1}')
    MODEL=$(echo "$RAW" | awk -F: '/^Processor Type:/ {sub(/^ */,"",$2); print $2; exit}')
else
    ST="error"
    CR_NOTE="prtconf not available"
fi
CLOCK_GHZ=""
[ -n "$MHZ" ] && CLOCK_GHZ=$(awk -v m="$MHZ" 'BEGIN{printf "%.2f", m/1000}')

V=$(json_obj_start)
V=$(json_obj_add "$V" cores     "$(json_num_or_null "$CORES")")
V=$(json_obj_add "$V" model     "$(json_quote_or_null "$MODEL")")
V=$(json_obj_add "$V" clock_ghz "$(json_num_or_null "$CLOCK_GHZ")")
V=$(json_obj_end "$V")

CR_ID="ap.hw.cpu" CR_LABEL="CPU 구성" CR_CATEGORY="hw_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" new_check_result
