#!/bin/sh
# Oracle — 컨트롤파일 multiplex 상태 (개수, 권장 2+)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT COUNT(*) FROM v\$controlfile;"

if ! ora_available; then
    CR_ID="db.controlfile" CR_LABEL="컨트롤파일 multiplex" CR_CATEGORY="dbms_config" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    CNT=3; NOTE="dry-run mock"
else
    CNT=$(echo "SELECT COUNT(*) FROM v\$controlfile;" | ora_scalar)
    NOTE=""
fi

ST="ok"
if [ -n "$CNT" ] && [ "$CNT" -lt 2 ] 2>/dev/null; then ST="warn"; fi

V=$(json_obj_start)
V=$(json_obj_add "$V" count "$(json_num_or_null "$CNT")")
V=$(json_obj_end "$V")

CR_ID="db.controlfile" CR_LABEL="컨트롤파일 multiplex" CR_CATEGORY="dbms_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
