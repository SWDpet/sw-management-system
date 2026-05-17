#!/bin/sh
# Oracle — Redo log 그룹 / 멤버 상태
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT group#, members, status, bytes/1048576 FROM v\$log;"

if ! ora_available; then
    CR_ID="db.redo_logs" CR_LABEL="Redo log 그룹" CR_CATEGORY="dbms_config" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    REDO_GROUPS=3; REDO_MEMBERS=6; INACTIVE_INVALID=0; NOTE="dry-run mock"
else
    OUT=$(echo "SELECT COUNT(DISTINCT group#) || '|' || SUM(members) || '|' || SUM(CASE WHEN status NOT IN ('CURRENT','ACTIVE','INACTIVE') THEN 1 ELSE 0 END) FROM v\$log;" | ora_scalar)
    REDO_GROUPS=${OUT%%|*}; OUT=${OUT#*|}
    REDO_MEMBERS=${OUT%%|*}; OUT=${OUT#*|}
    INACTIVE_INVALID=$OUT
    NOTE=""
fi

ST="ok"
[ -n "$INACTIVE_INVALID" ] && [ "$INACTIVE_INVALID" -gt 0 ] 2>/dev/null && ST="warn"

V=$(json_obj_start)
V=$(json_obj_add "$V" groups  "$(json_num_or_null "$REDO_GROUPS")")
V=$(json_obj_add "$V" members "$(json_num_or_null "$REDO_MEMBERS")")
V=$(json_obj_add "$V" invalid_status "$(json_num_or_null "$INACTIVE_INVALID")")
V=$(json_obj_end "$V")

CR_ID="db.redo_logs" CR_LABEL="Redo log 그룹" CR_CATEGORY="dbms_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
