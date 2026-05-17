#!/bin/sh
# Oracle — 아카이브 모드 (v$database.log_mode)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT log_mode FROM v\$database;"

if ! ora_available; then
    CR_ID="db.archive_mode" CR_LABEL="아카이브 모드" CR_CATEGORY="dbms_config" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" \
      CR_NOTE="sqlplus not available" new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    MODE="ARCHIVELOG"
    NOTE="dry-run mock"
else
    MODE=$(echo "SELECT log_mode FROM v\$database;" | ora_scalar)
    NOTE=""
fi

ST="ok"
case "$MODE" in
    ARCHIVELOG)   ST="ok" ;;
    NOARCHIVELOG) ST="crit" ;;
    *)            ST="warn" ;;
esac

V=$(json_obj_start)
V=$(json_obj_add "$V" log_mode "$(json_quote_or_null "$MODE")")
V=$(json_obj_end "$V")

CR_ID="db.archive_mode" CR_LABEL="아카이브 모드" CR_CATEGORY="dbms_config" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
