#!/bin/sh
# Oracle — 세션 수 (active vs total vs max_session)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT (SELECT COUNT(*) FROM v\$session WHERE status='ACTIVE'), COUNT(*), (SELECT value FROM v\$parameter WHERE name='sessions') FROM v\$session;"

if ! ora_available; then
    CR_ID="db.sessions" CR_LABEL="세션 수" CR_CATEGORY="dbms_perf" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    ACTIVE=8; TOTAL=42; LIMIT=500; NOTE="dry-run mock"
else
    OUT=$(echo "SELECT (SELECT COUNT(*) FROM v\$session WHERE status='ACTIVE') || '|' || (SELECT COUNT(*) FROM v\$session) || '|' || (SELECT value FROM v\$parameter WHERE name='sessions') FROM dual;" | ora_scalar)
    ACTIVE=${OUT%%|*}; OUT=${OUT#*|}
    TOTAL=${OUT%%|*};
    LIMIT=${OUT#*|}
    NOTE=""
fi

PCT=""
if [ -n "$LIMIT" ] && [ "$LIMIT" -gt 0 ] 2>/dev/null && [ -n "$TOTAL" ]; then
    PCT=$(awk -v t="$TOTAL" -v l="$LIMIT" 'BEGIN{printf "%.1f", t*100/l}')
fi
ST=$(resolve_status "$PCT" 70 90 0)

V=$(json_obj_start)
V=$(json_obj_add "$V" active     "$(json_num_or_null "$ACTIVE")")
V=$(json_obj_add "$V" total      "$(json_num_or_null "$TOTAL")")
V=$(json_obj_add "$V" limit      "$(json_num_or_null "$LIMIT")")
V=$(json_obj_add "$V" used_pct   "$(json_num_or_null "$PCT")")
V=$(json_obj_end "$V")

CR_ID="db.sessions" CR_LABEL="세션 수" CR_CATEGORY="dbms_perf" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
