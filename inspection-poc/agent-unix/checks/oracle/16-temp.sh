#!/bin/sh
# Oracle — TEMP 테이블스페이스 사용률
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT tablespace_name, used_percent FROM dba_tablespace_usage_metrics WHERE tablespace_name='TEMP';"

if ! ora_available; then
    CR_ID="db.temp" CR_LABEL="TEMP 테이블스페이스 사용률" CR_CATEGORY="dbms_storage" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    PCT=12.0; NOTE="dry-run mock"
else
    PCT=$(echo "SELECT ROUND(used_percent,1) FROM dba_tablespace_usage_metrics WHERE tablespace_name='TEMP';" | ora_scalar)
    NOTE=""
fi

ST=$(resolve_status "$PCT" 80 90 0)
TH=$(json_obj_start)
TH=$(json_obj_add "$TH" warn 80)
TH=$(json_obj_add "$TH" crit 90)
TH=$(json_obj_end "$TH")

V=$(json_obj_start)
V=$(json_obj_add "$V" used_pct "$(json_num_or_null "$PCT")")
V=$(json_obj_end "$V")

CR_ID="db.temp" CR_LABEL="TEMP 테이블스페이스 사용률" CR_CATEGORY="dbms_storage" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_THRESHOLD_JSON="$TH" CR_NOTE="$NOTE" \
  new_check_result
