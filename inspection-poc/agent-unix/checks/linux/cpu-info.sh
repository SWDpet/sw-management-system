#!/bin/sh
# Linux — CPU 구성 (cores, clock, model)
. "${UPIS_LIB_DIR:?UPIS_LIB_DIR 미설정}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="lscpu"
RAW=$(lscpu 2>/dev/null) || RAW=""
if [ -n "$RAW" ]; then
    CORES=$(echo "$RAW"   | awk -F: '/^CPU\(s\):/{gsub(/ /,"",$2); print $2; exit}')
    SOCKETS=$(echo "$RAW" | awk -F: '/^Socket\(s\):/{gsub(/ /,"",$2); print $2; exit}')
    MODEL=$(echo "$RAW"   | awk -F: '/^Model name:/{sub(/^ */,"",$2); print $2; exit}')
    MHZ=$(echo "$RAW"     | awk -F: '/^CPU max MHz:/{gsub(/ /,"",$2); print $2; exit}')
    [ -z "$MHZ" ] && MHZ=$(echo "$RAW" | awk -F: '/^CPU MHz:/{gsub(/ /,"",$2); print $2; exit}')
else
    CMD="/proc/cpuinfo"
    CORES=$(grep -c '^processor' /proc/cpuinfo 2>/dev/null)
    SOCKETS=$(awk '/^physical id/{print $4}' /proc/cpuinfo 2>/dev/null | sort -u | wc -l | tr -d ' ')
    MODEL=$(awk -F: '/^model name/{sub(/^ */,"",$2); print $2; exit}' /proc/cpuinfo)
    MHZ=$(awk -F: '/^cpu MHz/{gsub(/ /,"",$2); print $2; exit}' /proc/cpuinfo)
fi

CLOCK_GHZ=""
if [ -n "$MHZ" ]; then
    CLOCK_GHZ=$(awk -v m="$MHZ" 'BEGIN{ printf "%.2f", m/1000 }')
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" cores     "$(json_num_or_null "$CORES")")
V=$(json_obj_add "$V" sockets   "$(json_num_or_null "$SOCKETS")")
V=$(json_obj_add "$V" model     "$(json_quote_or_null "$MODEL")")
V=$(json_obj_add "$V" clock_ghz "$(json_num_or_null "$CLOCK_GHZ")")
V=$(json_obj_end "$V")

CR_ID="ap.hw.cpu" CR_LABEL="CPU 구성" CR_CATEGORY="hw_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="ok" new_check_result
