#!/bin/sh
# Oracle — UNDO tablespace 사용률
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT tablespace_name, used_percent FROM dba_tablespace_usage_metrics WHERE tablespace_name LIKE 'UNDOTBS%';"

if ! ora_available; then
    CR_ID="db.undo" CR_LABEL="UNDO 테이블스페이스 사용률" CR_CATEGORY="dbms_storage" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    TS="UNDOTBS1"; PCT=35.5; NOTE="dry-run mock"
else
    OUT=$(echo "SELECT MAX(tablespace_name) || '|' || ROUND(MAX(used_percent),1) FROM dba_tablespace_usage_metrics WHERE tablespace_name LIKE 'UNDOTBS%';" | ora_scalar)
    TS=${OUT%%|*}
    PCT=${OUT#*|}
    NOTE=""
fi

ST=$(resolve_status "$PCT" 80 90 0)
TH=$(json_obj_start)
TH=$(json_obj_add "$TH" warn 80)
TH=$(json_obj_add "$TH" crit 90)
TH=$(json_obj_end "$TH")

V=$(json_obj_start)
V=$(json_obj_add "$V" tablespace "$(json_quote_or_null "$TS")")
V=$(json_obj_add "$V" used_pct   "$(json_num_or_null "$PCT")")
V=$(json_obj_end "$V")

CR_ID="db.undo" CR_LABEL="UNDO 테이블스페이스 사용률" CR_CATEGORY="dbms_storage" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_THRESHOLD_JSON="$TH" CR_NOTE="$NOTE" \
  new_check_result
