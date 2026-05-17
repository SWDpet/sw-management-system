#!/bin/sh
# Linux — IP 정보 (활성 인터페이스의 IPv4)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="ip -4 -o addr show"
if command -v ip >/dev/null 2>&1; then
    LINES=$(ip -4 -o addr show 2>/dev/null | awk '$2!="lo"{split($4,a,"/"); print $2"="a[1]}')
else
    CMD="ifconfig"
    LINES=$(ifconfig 2>/dev/null | awk '/^[a-z]/{iface=$1; sub(/:$/,"",iface); next} /inet /{print iface"="$2}' | sed 's/addr://')
fi

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
