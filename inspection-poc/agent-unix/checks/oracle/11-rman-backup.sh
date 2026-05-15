#!/bin/sh
# Oracle — RMAN 백업 상태 (v$rman_backup_job_details 최근 7일)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT MAX(end_time), MAX(CASE WHEN status='COMPLETED' THEN end_time END) FROM v\$rman_backup_job_details WHERE start_time > sysdate - 7;"

if ! ora_available; then
    CR_ID="db.rman_backup" CR_LABEL="RMAN 백업 상태 (7d)" CR_CATEGORY="dbms_backup" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    LAST="2026-05-14 02:30:00"; SUCCESS="2026-05-14 02:30:00"; NOTE="dry-run mock"
else
    OUT=$(echo "SELECT TO_CHAR(MAX(end_time),'YYYY-MM-DD HH24:MI:SS') || '|' || TO_CHAR(MAX(CASE WHEN status='COMPLETED' THEN end_time END),'YYYY-MM-DD HH24:MI:SS') FROM v\$rman_backup_job_details WHERE start_time > sysdate - 7;" | ora_scalar)
    LAST=${OUT%%|*}
    SUCCESS=${OUT#*|}
    NOTE=""
fi

# 마지막 성공 백업이 없거나 7일 이상 지났으면 crit
ST="ok"
[ -z "$SUCCESS" ] && ST="crit"

V=$(json_obj_start)
V=$(json_obj_add "$V" last_job     "$(json_quote_or_null "$LAST")")
V=$(json_obj_add "$V" last_success "$(json_quote_or_null "$SUCCESS")")
V=$(json_obj_end "$V")

CR_ID="db.rman_backup" CR_LABEL="RMAN 백업 상태 (7d)" CR_CATEGORY="dbms_backup" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
