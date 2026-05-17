#!/bin/sh
# Oracle — alert log 의 ORA-xxxxx 에러 수 (24h, v$diag_alert_ext)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT COUNT(*) FROM v\$diag_alert_ext WHERE message_level<=8 AND originating_timestamp > systimestamp - 1;"

if ! ora_available; then
    CR_ID="db.alert_errors_24h" CR_LABEL="Alert log 에러 (24h)" CR_CATEGORY="dbms_log" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    CNT=0; NOTE="dry-run mock"
else
    CNT=$(echo "SELECT COUNT(*) FROM v\$diag_alert_ext WHERE message_level<=8 AND originating_timestamp > systimestamp - 1;" | ora_scalar)
    NOTE=""
fi

ST="ok"
if [ -n "$CNT" ] && [ "$CNT" -ge 10 ] 2>/dev/null; then ST="crit"
elif [ -n "$CNT" ] && [ "$CNT" -ge 1 ] 2>/dev/null; then ST="warn"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" error_count "$(json_num_or_null "$CNT")")
V=$(json_obj_end "$V")

CR_ID="db.alert_errors_24h" CR_LABEL="Alert log 에러 (24h)" CR_CATEGORY="dbms_log" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
