#!/bin/sh
# Solaris — RAM 구성 (prtconf | grep Memory)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="prtconf | grep 'Memory size'"
ST="ok"; TOTAL_MB=""
if command -v prtconf >/dev/null 2>&1; then
    LINE=$(prtconf 2>/dev/null | awk -F: '/Memory size/{print $2; exit}')
    # "16384 Megabytes" 또는 "16 Gigabytes"
    case "$LINE" in
        *Gigabytes*|*GB*)
            GB=$(echo "$LINE" | awk '{print $1}')
            TOTAL_MB=$(awk -v g="$GB" 'BEGIN{print g*1024}')
            ;;
        *Megabytes*|*MB*)
            TOTAL_MB=$(echo "$LINE" | awk '{print $1}')
            ;;
    esac
else
    ST="error"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" total_mb "$(json_num_or_null "$TOTAL_MB")")
V=$(json_obj_end "$V")

CR_ID="ap.hw.memory" CR_LABEL="RAM 구성" CR_CATEGORY="hw_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" new_check_result
