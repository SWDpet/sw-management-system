#!/bin/sh
# Oracle — 데이터파일 상태 (OFFLINE 검출)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT status, COUNT(*) FROM dba_data_files GROUP BY status;"

if ! ora_available; then
    CR_ID="db.datafile_status" CR_LABEL="데이터파일 상태" CR_CATEGORY="dbms_storage" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    TOTAL=12; OFFLINE=0; NOTE="dry-run mock"
else
    OUT=$(echo "SELECT COUNT(*) || '|' || SUM(CASE WHEN status<>'AVAILABLE' THEN 1 ELSE 0 END) FROM dba_data_files;" | ora_scalar)
    TOTAL=${OUT%%|*}
    OFFLINE=${OUT#*|}
    NOTE=""
fi

ST="ok"
[ -n "$OFFLINE" ] && [ "$OFFLINE" -gt 0 ] 2>/dev/null && ST="crit"

V=$(json_obj_start)
V=$(json_obj_add "$V" total   "$(json_num_or_null "$TOTAL")")
V=$(json_obj_add "$V" offline "$(json_num_or_null "$OFFLINE")")
V=$(json_obj_end "$V")

CR_ID="db.datafile_status" CR_LABEL="데이터파일 상태" CR_CATEGORY="dbms_storage" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
