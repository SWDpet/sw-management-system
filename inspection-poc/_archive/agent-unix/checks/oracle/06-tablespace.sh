#!/bin/sh
# Oracle — 테이블스페이스 사용률 (dba_tablespace_usage_metrics)
# 80% 이상이면 warn, 90% 이상이면 crit. 각 ts 별 1 item.
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT tablespace_name, used_percent FROM dba_tablespace_usage_metrics ORDER BY used_percent DESC;"

if ! ora_available; then
    CR_ID="db.tablespace" CR_LABEL="테이블스페이스 사용률" CR_CATEGORY="dbms_storage" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    # mock: USERS=45%, SYSTEM=60%, SYSAUX=72%
    ROWS="USERS|45.0
SYSTEM|60.0
SYSAUX|72.0"
    NOTE="dry-run mock"
else
    ROWS=$(echo "SELECT tablespace_name || '|' || ROUND(used_percent,1) FROM dba_tablespace_usage_metrics ORDER BY used_percent DESC;" | ora_query)
    NOTE=""
fi

TMP=$(mktemp 2>/dev/null || echo "/tmp/ts.$$")
trap 'rm -f "$TMP"' EXIT
printf '%s\n' "$ROWS" > "$TMP"

ARR=$(json_arr_start)
while IFS= read -r line; do
    line=$(echo "$line" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
    [ -z "$line" ] && continue
    TS=${line%%|*}
    PCT=${line#*|}
    ST=$(resolve_status "$PCT" 80 90 0)

    TH=$(json_obj_start)
    TH=$(json_obj_add "$TH" warn 80)
    TH=$(json_obj_add "$TH" crit 90)
    TH=$(json_obj_end "$TH")

    V=$(json_obj_start)
    V=$(json_obj_add "$V" tablespace "$(json_quote "$TS")")
    V=$(json_obj_add "$V" used_pct   "$(json_num_or_null "$PCT")")
    V=$(json_obj_end "$V")

    TS_LOWER=$(echo "$TS" | tr 'A-Z' 'a-z')
    ITEM=$(CR_ID="db.tablespace.$TS_LOWER" CR_LABEL="TS 사용률 ($TS)" \
      CR_CATEGORY="dbms_storage" CR_CMD="$CMD" CR_VALUE_JSON="$V" \
      CR_STATUS="$ST" CR_THRESHOLD_JSON="$TH" CR_NOTE="$NOTE" new_check_result)
    ARR=$(json_arr_add "$ARR" "$ITEM")
done < "$TMP"
ARR=$(json_arr_end "$ARR")
printf '%s' "$ARR"
