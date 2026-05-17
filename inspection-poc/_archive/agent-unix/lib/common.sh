#!/bin/sh
# ── 공통 라이브러리 ────────────────────────────────────────────────
# 점검 에이전트 전역 유틸. 모든 체크/콜렉터/렌더러에서 . (dot-source).
# POSIX sh 호환 — AIX ksh, HP-UX sh, Linux bash, Solaris ksh 모두 동작.

# 다중 import 가드
if [ -n "${__UPIS_COMMON_SH__:-}" ]; then return 0 2>/dev/null; fi
__UPIS_COMMON_SH__=1

# perl 가용성 1회 검사 — json_str_oneline 의 UTF-8 sanitizer 가 사용.
if command -v perl >/dev/null 2>&1; then
    UPIS_HAS_PERL=1
    export UPIS_HAS_PERL
fi

# ── OS 감지 ────────────────────────────────────────────────────────
# returns: aix | hpux | linux | solaris | unknown
detect_os() {
    _u=$(uname -s 2>/dev/null)
    case "$_u" in
        AIX)        echo aix ;;
        HP-UX)      echo hpux ;;
        Linux)      echo linux ;;
        SunOS)      echo solaris ;;
        MINGW*|MSYS*|CYGWIN*) echo linux ;;  # 개발용 mock
        *)          echo unknown ;;
    esac
}

# ── 시각 ──────────────────────────────────────────────────────────
# ISO-8601 (with tz). AIX/HP-UX 의 date 는 +%:z 를 지원 안하므로 fallback.
iso_now() {
    if date +%Y-%m-%dT%H:%M:%S%z >/dev/null 2>&1; then
        _t=$(date +%Y-%m-%dT%H:%M:%S%z)
        # 2026-05-15T14:30:00+0900 → 2026-05-15T14:30:00+09:00
        echo "$_t" | sed 's/\([+-][0-9][0-9]\)\([0-9][0-9]\)$/\1:\2/'
    else
        date -u +%Y-%m-%dT%H:%M:%SZ
    fi
}

now_ms() {
    if date +%s%N 2>/dev/null | grep -q '^[0-9]\{16,\}$'; then
        # GNU date — nanoseconds available
        _ns=$(date +%s%N)
        echo "${_ns%??????}"   # 첫 ms 단위로 절삭 (마지막 6자리 = 마이크로/나노 제거)
    else
        # POSIX fallback: 초 단위 ×1000
        echo $(( $(date +%s) * 1000 ))
    fi
}

# ── 로그 ──────────────────────────────────────────────────────────
log() {
    _lvl=$1; shift
    _ts=$(date +%H:%M:%S 2>/dev/null)
    printf '[%s] [%-5s] %s\n' "$_ts" "$_lvl" "$*" >&2
}

log_info()  { log INFO  "$@"; }
log_warn()  { log WARN  "$@"; }
log_error() { log ERROR "$@"; }

# ── 호스트 정보 ────────────────────────────────────────────────────
host_hostname() {
    hostname 2>/dev/null || uname -n 2>/dev/null || echo "unknown"
}

# OS 캡션 (Windows 의 Win32_OperatingSystem.Caption 등가)
host_os_caption() {
    _os=$(detect_os)
    case "$_os" in
        aix)     echo "AIX $(oslevel 2>/dev/null || uname -v)" ;;
        hpux)    echo "HP-UX $(uname -r)" ;;
        linux)
            if [ -r /etc/os-release ]; then
                . /etc/os-release 2>/dev/null
                echo "${PRETTY_NAME:-Linux $(uname -r)}"
            else
                echo "Linux $(uname -r)"
            fi
            ;;
        solaris) echo "Solaris $(uname -r)" ;;
        *)       uname -sr 2>/dev/null || echo "unknown" ;;
    esac
}

host_os_detail() {
    uname -r 2>/dev/null
}

host_model() {
    _os=$(detect_os)
    case "$_os" in
        aix)     prtconf 2>/dev/null | awk -F': *' '/^System Model:/{print $2; exit}' ;;
        hpux)    model 2>/dev/null ;;
        linux)   (cat /sys/class/dmi/id/product_name 2>/dev/null) || echo "" ;;
        solaris) prtdiag 2>/dev/null | head -1 ;;
        *)       echo "" ;;
    esac
}

host_primary_ip() {
    _os=$(detect_os)
    case "$_os" in
        aix)     ifconfig -a 2>/dev/null | awk '/inet /{print $2; exit}' ;;
        hpux)    netstat -in 2>/dev/null | awk 'NR>1 && $4!~/^(127|0\.0\.0\.0)/{print $4; exit}' ;;
        linux)
            if command -v ip >/dev/null 2>&1; then
                ip -4 -o addr show 2>/dev/null | awk '$2!="lo"{split($4,a,"/"); print a[1]; exit}'
            else
                ifconfig 2>/dev/null | awk '/inet /{print $2; exit}' | sed 's/addr://'
            fi
            ;;
        solaris) ifconfig -a 2>/dev/null | awk '/inet /{print $2; exit}' ;;
        *)       echo "" ;;
    esac
}

# ── 시간 측정 ─────────────────────────────────────────────────────
# usage:  invoke_timed CMD_BLOCK_OR_FUNC ARGS...
# stdout: 호출 함수의 stdout
# side effect: $TOOK_MS 환경변수에 경과 ms
invoke_timed() {
    _s=$(now_ms)
    "$@"
    _rc=$?
    _e=$(now_ms)
    TOOK_MS=$(( _e - _s ))
    return $_rc
}
