#!/bin/sh
# Solaris — CPU 구성 (psrinfo -v / prtdiag)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="psrinfo -v | head -3"
ST="ok"; CORES=""; CLOCK_GHZ=""; MODEL=""
if command -v psrinfo >/dev/null 2>&1; then
    CORES=$(psrinfo 2>/dev/null | wc -l | tr -d ' ')
    LINE=$(psrinfo -v 2>/dev/null | awk '/operates at/{print; exit}')
    # "       The sparcv9 processor operates at 1593 MHz"
    MHZ=$(echo "$LINE" | sed -n 's/.*operates at \([0-9]\+\) MHz.*/\1/p')
    [ -n "$MHZ" ] && CLOCK_GHZ=$(awk -v m="$MHZ" 'BEGIN{printf "%.2f", m/1000}')
    MODEL=$(psrinfo -pv 2>/dev/null | awk '/SPARC|x86/{print; exit}' | sed 's/^[[:space:]]*//')
    [ -z "$MODEL" ] && MODEL=$(prtdiag 2>/dev/null | head -1)
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
