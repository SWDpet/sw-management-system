#!/bin/sh
# Oracle — PGA 사용량 (v$pgastat 의 total PGA allocated)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT name, value/1048576 FROM v\$pgastat WHERE name IN ('aggregate PGA target parameter','total PGA allocated');"

if ! ora_available; then
    CR_ID="db.pga" CR_LABEL="PGA 사용량" CR_CATEGORY="dbms_perf" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    TARGET_MB=2048; ALLOC_MB=512; NOTE="dry-run mock"
else
    OUT=$(echo "SELECT ROUND(MAX(DECODE(name,'aggregate PGA target parameter',value))/1048576) || '|' || ROUND(MAX(DECODE(name,'total PGA allocated',value))/1048576) FROM v\$pgastat;" | ora_scalar)
    TARGET_MB=${OUT%%|*}
    ALLOC_MB=${OUT#*|}
    NOTE=""
fi

PCT=""
if [ -n "$TARGET_MB" ] && [ -n "$ALLOC_MB" ] && [ "$TARGET_MB" -gt 0 ] 2>/dev/null; then
    PCT=$(awk -v t="$TARGET_MB" -v a="$ALLOC_MB" 'BEGIN{printf "%.1f", a*100/t}')
fi
ST=$(resolve_status "$PCT" 80 90 0)

V=$(json_obj_start)
V=$(json_obj_add "$V" target_mb    "$(json_num_or_null "$TARGET_MB")")
V=$(json_obj_add "$V" allocated_mb "$(json_num_or_null "$ALLOC_MB")")
V=$(json_obj_add "$V" used_pct     "$(json_num_or_null "$PCT")")
V=$(json_obj_end "$V")

CR_ID="db.pga" CR_LABEL="PGA 사용량" CR_CATEGORY="dbms_perf" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
