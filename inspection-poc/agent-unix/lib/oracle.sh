#!/bin/sh
# ── Oracle 헬퍼 ────────────────────────────────────────────────────
# sqlplus 호출 공통 래퍼. 환경변수:
#   ORACLE_CONNECT    sqlplus 연결 문자열 (e.g. "user/pass@host:1521/SID" 또는 "/ as sysdba")
#   ORACLE_DRY_RUN    1 이면 실제 호출 없이 mock 결과 반환 (PoC 검증용)
#   ORACLE_HOME       sqlplus 위치
# ──────────────────────────────────────────────────────────────────
if [ -n "${__UPIS_ORACLE_SH__:-}" ]; then return 0 2>/dev/null; fi
__UPIS_ORACLE_SH__=1

. "${UPIS_LIB_DIR:?}/common.sh"

# sqlplus 사용 가능 여부.
ora_available() {
    [ -n "${ORACLE_DRY_RUN:-}" ] && return 0
    if command -v sqlplus >/dev/null 2>&1; then return 0; fi
    if [ -n "${ORACLE_HOME:-}" ] && [ -x "$ORACLE_HOME/bin/sqlplus" ]; then return 0; fi
    return 1
}

# SQL 실행. stdin 으로 SQL 받아 stdout 으로 결과 출력.
# DRY_RUN 이면 stdin 무시하고 빈 결과.
ora_query() {
    if [ -n "${ORACLE_DRY_RUN:-}" ]; then
        cat > /dev/null
        return 0
    fi
    _sqlplus=$(command -v sqlplus 2>/dev/null)
    [ -z "$_sqlplus" ] && [ -n "${ORACLE_HOME:-}" ] && _sqlplus="$ORACLE_HOME/bin/sqlplus"
    [ -z "$_sqlplus" ] && return 1

    # -S silent, /NOLOG 으로 시작해 CONNECT 후 SQL 실행
    cat <<EOF | "$_sqlplus" -S /NOLOG 2>/dev/null
WHENEVER OSERROR EXIT 9
WHENEVER SQLERROR EXIT 9
CONNECT ${ORACLE_CONNECT:-/ as sysdba}
SET PAGESIZE 0 HEADING OFF FEEDBACK OFF VERIFY OFF TRIMSPOOL ON LINESIZE 32767
$(cat)
EXIT
EOF
}

# 단일 스칼라 추출 (첫 라인의 trim 결과).
ora_scalar() {
    ora_query | awk 'NR==1{gsub(/^[[:space:]]+|[[:space:]]+$/,""); print; exit}'
}
