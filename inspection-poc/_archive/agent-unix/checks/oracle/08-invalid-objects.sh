#!/bin/sh
# Oracle — INVALID 상태 객체 수
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT COUNT(*) FROM dba_objects WHERE status='INVALID';"

if ! ora_available; then
    CR_ID="db.invalid_objects" CR_LABEL="INVALID 객체 수" CR_CATEGORY="dbms_health" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    CNT=0; NOTE="dry-run mock"
else
    CNT=$(echo "SELECT COUNT(*) FROM dba_objects WHERE status='INVALID';" | ora_scalar)
    NOTE=""
fi

ST="ok"
if [ -n "$CNT" ] && [ "$CNT" -ge 50 ] 2>/dev/null; then ST="crit"
elif [ -n "$CNT" ] && [ "$CNT" -ge 1 ] 2>/dev/null; then ST="warn"
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" count "$(json_num_or_null "$CNT")")
V=$(json_obj_end "$V")

CR_ID="db.invalid_objects" CR_LABEL="INVALID 객체 수" CR_CATEGORY="dbms_health" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
