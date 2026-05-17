#!/bin/sh
# Solaris — 파일시스템 (df -h)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="df -h -l"
TMP=$(mktemp 2>/dev/null || echo "/tmp/disk.$$")
trap 'rm -f "$TMP"' EXIT

# Solaris df 컬럼: Filesystem  size  used  avail  capacity  Mounted_on
df -h -l 2>/dev/null | awk 'NR>1 && $1!~/^(swap|proc|ctfs|objfs|fd)$/' > "$TMP"

ARR=$(json_arr_start)
while IFS= read -r line; do
    [ -z "$line" ] && continue
    FS=$(echo  "$line" | awk '{print $1}')
    SIZE=$(echo "$line"| awk '{print $2}')
    USED=$(echo "$line"| awk '{print $3}')
    PCT=$(echo "$line" | awk '{print $5}' | tr -d '%')
    MNT=$(echo "$line" | awk '{print $6}')
    [ -z "$PCT" ] && continue

    ST=$(resolve_status "$PCT" "${CFG_DISK_WARN:-80}" "${CFG_DISK_CRIT:-90}" 0)
    TH=$(json_obj_start)
    TH=$(json_obj_add "$TH" warn "$(json_num_or_null "${CFG_DISK_WARN:-80}")")
    TH=$(json_obj_add "$TH" crit "$(json_num_or_null "${CFG_DISK_CRIT:-90}")")
    TH=$(json_obj_end "$TH")

    V=$(json_obj_start)
    V=$(json_obj_add "$V" filesystem "$(json_quote "$FS")")
    V=$(json_obj_add "$V" mount      "$(json_quote "$MNT")")
    V=$(json_obj_add "$V" size       "$(json_quote "$SIZE")")
    V=$(json_obj_add "$V" used       "$(json_quote "$USED")")
    V=$(json_obj_add "$V" used_pct   "$(json_num_or_null "$PCT")")
    V=$(json_obj_end "$V")

    ID_SUFFIX=$(echo "$MNT" | sed 's|^/||; s|/|_|g'); [ -z "$ID_SUFFIX" ] && ID_SUFFIX="root"
    ITEM=$(CR_ID="ap.disk.$ID_SUFFIX" CR_LABEL="디스크 사용률 ($MNT)" CR_CATEGORY="filesystem" \
      CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="$ST" CR_THRESHOLD_JSON="$TH" \
      new_check_result)
    ARR=$(json_arr_add "$ARR" "$ITEM")
done < "$TMP"
ARR=$(json_arr_end "$ARR")
printf '%s' "$ARR"
