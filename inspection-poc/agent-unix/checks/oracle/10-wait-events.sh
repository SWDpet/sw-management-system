#!/bin/sh
# Oracle — Top wait events (v$session_event 상위 5)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"
. "${UPIS_LIB_DIR}/oracle.sh"

CMD="SELECT event, total_waits, time_waited FROM v\$system_event WHERE wait_class<>'Idle' ORDER BY time_waited DESC FETCH FIRST 5 ROWS ONLY;"

if ! ora_available; then
    CR_ID="db.wait_events" CR_LABEL="Top Wait Events" CR_CATEGORY="dbms_perf" \
      CR_CMD="$CMD" CR_VALUE_JSON=null CR_STATUS="error" CR_NOTE="sqlplus not available" \
      new_check_result
    exit 0
fi

if [ -n "${ORACLE_DRY_RUN:-}" ]; then
    ROWS="db file sequential read|12345|678
log file sync|2345|123
buffer busy waits|234|56"
    NOTE="dry-run mock"
else
    ROWS=$(echo "SELECT event || '|' || total_waits || '|' || time_waited FROM (SELECT event, total_waits, time_waited FROM v\$system_event WHERE wait_class<>'Idle' ORDER BY time_waited DESC) WHERE ROWNUM<=5;" | ora_query)
    NOTE=""
fi

TMP=$(mktemp 2>/dev/null || echo "/tmp/we.$$")
trap 'rm -f "$TMP"' EXIT
printf '%s\n' "$ROWS" > "$TMP"

ARR=$(json_arr_start)
while IFS= read -r line; do
    line=$(echo "$line" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
    [ -z "$line" ] && continue
    EV=${line%%|*}; rest=${line#*|}
    WAITS=${rest%%|*}; TIME=${rest#*|}

    O=$(json_obj_start)
    O=$(json_obj_add "$O" event       "$(json_quote "$EV")")
    O=$(json_obj_add "$O" total_waits "$(json_num_or_null "$WAITS")")
    O=$(json_obj_add "$O" time_waited "$(json_num_or_null "$TIME")")
    O=$(json_obj_end "$O")
    ARR=$(json_arr_add "$ARR" "$O")
done < "$TMP"
ARR=$(json_arr_end "$ARR")

CR_ID="db.wait_events" CR_LABEL="Top Wait Events" CR_CATEGORY="dbms_perf" \
  CR_CMD="$CMD" CR_VALUE_JSON="$ARR" CR_STATUS="ok" CR_NOTE="$NOTE" new_check_result
