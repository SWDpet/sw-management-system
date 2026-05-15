#!/bin/sh
# AIX — IP 정보 (ifconfig -a 의 inet 라인)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="ifconfig -a | awk '/^[a-z]/{i=\$1} /inet /{print i\"=\"\$2}'"
LINES=$(ifconfig -a 2>/dev/null | awk '/^[a-z]/{iface=$1; sub(/:$/,"",iface); next} /inet /{print iface"="$2}' | grep -v '=127')

ARR=$(json_arr_start)
TMP=$(mktemp 2>/dev/null || echo "/tmp/netip.$$")
trap 'rm -f "$TMP"' EXIT
printf '%s\n' "$LINES" > "$TMP"
while IFS= read -r line; do
    [ -z "$line" ] && continue
    IFACE=${line%%=*}
    IP=${line#*=}
    V=$(json_obj_start)
    V=$(json_obj_add "$V" iface "$(json_quote "$IFACE")")
    V=$(json_obj_add "$V" ip    "$(json_quote "$IP")")
    V=$(json_obj_end "$V")
    ARR=$(json_arr_add "$ARR" "$V")
done < "$TMP"
ARR=$(json_arr_end "$ARR")

CR_ID="ap.net.ip" CR_LABEL="IP 정보" CR_CATEGORY="network" \
  CR_CMD="$CMD" CR_VALUE_JSON="$ARR" CR_STATUS="ok" new_check_result
