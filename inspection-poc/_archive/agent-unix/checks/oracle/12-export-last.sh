#!/bin/sh
# Oracle — 마지막 export(datapump) 시각.
# v$datapump_job 또는 별도 dba_directories 의 dump 파일 mtime 으로 추정.
# PoC 에서는 dba_datapump_jobs 의 가장 최근 완료 시각.
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT MAX(job_id), MAX(start_time) FROM dba_datapump_jobs;"

if ! ora_available; then
    CR_ID="db.export_last" CR_LABEL="마지막 Datapump Export" CR_CATEGORY="dbms_backup" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    LAST="2026-05-13 23:00:00"; NOTE="dry-run mock"
else
    LAST=$(echo "SELECT TO_CHAR(MAX(start_time),'YYYY-MM-DD HH24:MI:SS') FROM dba_datapump_jobs;" | ora_scalar)
    NOTE=""
fi

ST="ok"
[ -z "$LAST" ] && ST="warn"

V=$(json_obj_start)
V=$(json_obj_add "$V" last_export "$(json_quote_or_null "$LAST")")
V=$(json_obj_end "$V")

CR_ID="db.export_last" CR_LABEL="마지막 Datapump Export" CR_CATEGORY="dbms_backup" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
