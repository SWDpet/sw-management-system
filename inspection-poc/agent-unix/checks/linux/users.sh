#!/bin/sh
# Linux — 로컬 사용자 계정 (root 제외 UID>=1000)
. "${UPIS_LIB_DIR:?}/common.sh"
. "${UPIS_LIB_DIR}/json.sh"
. "${UPIS_LIB_DIR}/snapshot.sh"

CMD="getent passwd | awk -F: '\$3>=1000 && \$3<65534'"
USERS=$(getent passwd 2>/dev/null | awk -F: '$3>=1000 && $3<65534 {print $1}' | tr '\n' ',' | sed 's/,$//')
[ -z "$USERS" ] && USERS=$(awk -F: '$3>=1000 && $3<65534 {print $1}' /etc/passwd 2>/dev/null | tr '\n' ',' | sed 's/,$//')
COUNT=$(printf '%s' "$USERS" | awk -F, '{print NF}')
[ -z "$USERS" ] && COUNT=0

V=$(json_obj_start)
V=$(json_obj_add "$V" count "$(json_num_or_null "$COUNT")")
V=$(json_obj_add "$V" users "$(json_quote_or_null "$USERS")")
V=$(json_obj_end "$V")

CR_ID="ap.security.users" CR_LABEL="로컬 사용자 계정" CR_CATEGORY="security" \
  CR_CMD="$CMD" CR_VALUE_JSON="$V" CR_STATUS="ok" new_check_result
