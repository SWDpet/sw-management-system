#!/bin/sh
# Oracle — SGA 구성 (v$sga)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT name, value/1048576 FROM v\$sga;"

if ! ora_available; then
    CR_ID="db.sga" CR_LABEL="SGA 구성" CR_CATEGORY="dbms_config" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    TOTAL_MB=4096; NOTE="dry-run mock"
else
    TOTAL_MB=$(echo "SELECT ROUND(SUM(value)/1048576) FROM v\$sga;" | ora_scalar)
    NOTE=""
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" total_mb "$(json_num_or_null "$TOTAL_MB")")
V=$(json_obj_end "$V")

CR_ID="db.sga" CR_LABEL="SGA 구성" CR_CATEGORY="dbms_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="ok" CR_NOTE="$NOTE" new_check_result
