#!/bin/sh
# Oracle — Data Guard standby lag (v$dataguard_stats). DG 환경 아니면 n/a.
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT name, value FROM v\$dataguard_stats WHERE name LIKE '%lag%';"

if ! ora_available; then
    CR_ID="db.standby_lag" CR_LABEL="Standby Lag (DG)" CR_CATEGORY="dbms_dr" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    APPLY_LAG="+00 00:00:05"; TRANSPORT_LAG="+00 00:00:01"; NOTE="dry-run mock"
else
    OUT=$(echo "SELECT MAX(DECODE(name,'apply lag',value)) || '|' || MAX(DECODE(name,'transport lag',value)) FROM v\$dataguard_stats;" | ora_scalar 2>/dev/null)
    APPLY_LAG=${OUT%%|*}
    TRANSPORT_LAG=${OUT#*|}
    NOTE=""
fi

ST="ok"
[ -z "$APPLY_LAG" ] && [ -z "$TRANSPORT_LAG" ] && ST="n/a"

V=$(json_obj_start)
V=$(json_obj_add "$V" apply_lag     "$(json_quote_or_null "$APPLY_LAG")")
V=$(json_obj_add "$V" transport_lag "$(json_quote_or_null "$TRANSPORT_LAG")")
V=$(json_obj_end "$V")

CR_ID="db.standby_lag" CR_LABEL="Standby Lag (DG)" CR_CATEGORY="dbms_dr" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_NOTE="$NOTE" new_check_result
