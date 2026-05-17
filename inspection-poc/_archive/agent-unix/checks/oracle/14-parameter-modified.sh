#!/bin/sh
# Oracle — SYSTEM modify 된 파라미터 수
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT COUNT(*) FROM v\$parameter WHERE ismodified<>'FALSE';"

if ! ora_available; then
    CR_ID="db.parameter_modified" CR_LABEL="동적 변경된 파라미터" CR_CATEGORY="dbms_config" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    CNT=0; NOTE="dry-run mock"
else
    CNT=$(echo "SELECT COUNT(*) FROM v\$parameter WHERE ismodified<>'FALSE';" | ora_scalar)
    NOTE=""
fi

V=$(json_obj_start)
V=$(json_obj_add "$V" modified_count "$(json_num_or_null "$CNT")")
V=$(json_obj_end "$V")

CR_ID="db.parameter_modified" CR_LABEL="동적 변경된 파라미터" CR_CATEGORY="dbms_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="ok" CR_NOTE="$NOTE" new_check_result
