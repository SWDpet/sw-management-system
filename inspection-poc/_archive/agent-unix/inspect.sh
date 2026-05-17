#!/bin/sh
# ─────────────────────────────────────────────────────────────────────
# UPIS 점검 자동화 — Unix 계열 (AIX / HP-UX / Linux / Solaris)
# 사용법:
#   ./inspect.sh -c config/site.dyg.json
#   ./inspect.sh -c config/site.dyg.json -o ap.hw.cpu,ap.perf.cpu
# 환경변수:
#   UPIS_OS_OVERRIDE  실제 OS 와 다른 체크 디렉토리를 강제 (mock 검증용)
# ─────────────────────────────────────────────────────────────────────

set -u

ROOT=$(cd "$(dirname "$0")" && pwd)
LIB_DIR="$ROOT/lib"
CHECKS_DIR="$ROOT/checks"
SNAPSHOT_DIR="$ROOT/snapshots"
OUT_DIR="$ROOT/out"

CONFIG=""
ONLY=""

while [ $# -gt 0 ]; do
    case "$1" in
        -c|--config)   CONFIG=$2; shift 2 ;;
        -o|--only)     ONLY=$2;   shift 2 ;;
        -h|--help)
            sed -n '2,9p' "$0"; exit 0 ;;
        *)
            echo "ERR: unknown arg: $1" >&2
            exit 2 ;;
    esac
done

if [ -z "$CONFIG" ]; then
    CONFIG="$ROOT/config/site.dyg.json"
fi
[ -f "$CONFIG" ] || { echo "ERR: config not found: $CONFIG" >&2; exit 2; }

export UPIS_LIB_DIR="$LIB_DIR"
. "$LIB_DIR/common.sh"
. "$LIB_DIR/json.sh"
. "$LIB_DIR/snapshot.sh"

log_info "===== UPIS 점검 자동화 (Unix) v$AGENT_VERSION ====="
log_info "config: $CONFIG"

# ── config 로드 ────────────────────────────────────────────────────
SITE=$(json_get_str       "$CONFIG" site)
SITE_NAME=$(json_get_str  "$CONFIG" site_name)
TIER=$(json_get_str       "$CONFIG" tier)
INSPECTOR=$(json_get_str  "$CONFIG" inspector)
export SITE SITE_NAME TIER INSPECTOR

# 임계값은 환경변수로 후속 체크에 전달
CFG_CPU_WARN=$(json_get_num "$CONFIG" cpu_pct_warn)
CFG_CPU_CRIT=$(json_get_num "$CONFIG" cpu_pct_crit)
CFG_MEM_WARN=$(json_get_num "$CONFIG" mem_pct_warn)
CFG_MEM_CRIT=$(json_get_num "$CONFIG" mem_pct_crit)
CFG_DISK_WARN=$(json_get_num "$CONFIG" disk_pct_warn)
CFG_DISK_CRIT=$(json_get_num "$CONFIG" disk_pct_crit)
export CFG_CPU_WARN CFG_CPU_CRIT CFG_MEM_WARN CFG_MEM_CRIT CFG_DISK_WARN CFG_DISK_CRIT

log_info "site=$SITE tier=$TIER inspector=$INSPECTOR"

# ── OS 결정 ────────────────────────────────────────────────────────
OS=${UPIS_OS_OVERRIDE:-$(detect_os)}
log_info "OS=$OS"
OS_CHECKS_DIR="$CHECKS_DIR/$OS"
if [ ! -d "$OS_CHECKS_DIR" ]; then
    log_error "no checks directory for OS=$OS  (expected: $OS_CHECKS_DIR)"
    exit 3
fi

# ── only 필터 ──────────────────────────────────────────────────────
match_only() {
    _stem=$1
    if [ -z "$ONLY" ]; then return 0; fi
    _IFS=$IFS; IFS=,
    for tok in $ONLY; do
        case "$_stem" in "$tok") IFS=$_IFS; return 0 ;; esac
    done
    IFS=$_IFS; return 1
}

# ── 체크 실행 ──────────────────────────────────────────────────────
TOTAL_START=$(now_ms)
ITEMS=$(json_arr_start)
ERR_COUNT=0
RUN_COUNT=0

# Oracle 체크는 tier=db 일 때만 자동 포함
if [ "$TIER" = "db" ] && [ -d "$CHECKS_DIR/oracle" ]; then
    CHECK_FILES=$(ls -1 "$OS_CHECKS_DIR"/*.sh "$CHECKS_DIR/oracle"/*.sh 2>/dev/null | sort)
else
    CHECK_FILES=$(ls -1 "$OS_CHECKS_DIR"/*.sh 2>/dev/null | sort)
fi

for f in $CHECK_FILES; do
    [ -f "$f" ] || continue
    stem=$(basename "$f" .sh)
    if ! match_only "$stem"; then continue; fi

    log_info "check: $(basename "$(dirname "$f")")/$stem"
    _s=$(now_ms)
    if RESULT=$(sh "$f" 2>>"$OUT_DIR/check.log"); then
        :
    else
        ERR_COUNT=$(( ERR_COUNT + 1 ))
        log_error "  $stem failed"
        # error 항목으로 추가
        RESULT=$(CR_ID="error.$stem" CR_LABEL="Check failed: $stem" \
                 CR_CATEGORY="error" CR_STATUS="error" \
                 CR_NOTE="see check.log" new_check_result)
    fi
    _e=$(now_ms)
    _took=$(( _e - _s ))

    # RESULT 가 한 개 객체일 수도, 여러 객체일 수도 있다.
    # 우리 contract: 체크 스크립트는 객체 또는 객체배열을 출력.
    case "$RESULT" in
        \[*)
            # 배열 → 각 element 를 items 에 추가
            # 최상위 {..} 만 분리. 중첩 {} 가 있을 수 있으므로 depth 추적.
            EACH=$(printf '%s' "$RESULT" | awk '
                BEGIN { depth=0; buf="" }
                {
                    for (i=1; i<=length($0); i++) {
                        c=substr($0,i,1)
                        if (depth==0) { if (c == "[") depth=1; continue }
                        if (depth==1) {
                            if (c == "{") { buf=c; depth=2 }
                            else if (c == "]") { exit }
                            # 그 외 (공백/콤마) 무시
                            continue
                        }
                        buf = buf c
                        if (c == "{") depth++
                        else if (c == "}") {
                            depth--
                            if (depth==1) { print buf "\037"; buf="" }
                        }
                    }
                }
            ')
            _IFS=$IFS; IFS=$(printf '\037')
            for elem in $EACH; do
                [ -z "$elem" ] && continue
                ITEMS=$(json_arr_add "$ITEMS" "$elem")
                RUN_COUNT=$(( RUN_COUNT + 1 ))
            done
            IFS=$_IFS
            ;;
        \{*)
            ITEMS=$(json_arr_add "$ITEMS" "$RESULT")
            RUN_COUNT=$(( RUN_COUNT + 1 ))
            ;;
        *)
            log_warn "  $stem produced no output"
            ;;
    esac
done

ITEMS=$(json_arr_end "$ITEMS")
TOTAL_END=$(now_ms)
TOTAL_MS=$(( TOTAL_END - TOTAL_START ))
log_info "collected $RUN_COUNT items, errors $ERR_COUNT in $TOTAL_MS ms"

# ── snapshot ─────────────────────────────────────────────────────
SNAP=$(build_snapshot "$ITEMS" "$TOTAL_MS")
SNAP_PATH=$(save_snapshot "$SNAPSHOT_DIR" "$SNAP")
log_info "snapshot saved: $(basename "$SNAP_PATH")"

# out/ 에 최신 단일 파일 보관
mkdir -p "$OUT_DIR"
printf '%s' "$SNAP" > "$OUT_DIR/snapshot.json"

# ── 사람용 요약 (옵션 A: QR 인코딩은 Windows 측에서) ────────────────
TOTAL_ITEMS=$(printf '%s' "$SNAP" | grep -o '"id":' | wc -l | tr -d ' ')
OK_CNT=$(printf  '%s' "$SNAP" | grep -o '"status":"ok"' | wc -l | tr -d ' ')
WRN_CNT=$(printf '%s' "$SNAP" | grep -o '"status":"warn"' | wc -l | tr -d ' ')
CRT_CNT=$(printf '%s' "$SNAP" | grep -o '"status":"crit"' | wc -l | tr -d ' ')
MAN_CNT=$(printf '%s' "$SNAP" | grep -o '"status":"pending_manual"' | wc -l | tr -d ' ')

log_info "===== DONE ====="
echo ""
echo "결과:"
echo "  snapshot : $SNAP_PATH"
echo "  out      : $OUT_DIR/snapshot.json"
echo ""
echo "자동: ok=$OK_CNT warn=$WRN_CNT crit=$CRT_CNT | 수동 대기: $MAN_CNT | 에러: $ERR_COUNT | 총 $TOTAL_ITEMS"
