#!/bin/sh
# ── Snapshot 빌더 ──────────────────────────────────────────────────
# Windows 측 agent-windows 의 New-CheckResult / Build-Snapshot 와 동일한
# JSON shape (schema=snapshot/v1) 를 생성한다.

if [ -n "${__UPIS_SNAPSHOT_SH__:-}" ]; then return 0 2>/dev/null; fi
__UPIS_SNAPSHOT_SH__=1

_here=$(cd "$(dirname "$0")" 2>/dev/null && pwd || pwd)
. "${UPIS_LIB_DIR:-$_here}/common.sh"
. "${UPIS_LIB_DIR:-$_here}/json.sh"

AGENT_VERSION="0.1.0-unix"

# ── 체크 결과 객체 생성 ────────────────────────────────────────────
# 인자는 환경변수로 전달 (sh function 의 인자 개수 제한 회피).
# REQUIRED:
#   CR_ID, CR_LABEL, CR_CATEGORY
# OPTIONAL:
#   CR_METHOD       (auto|manual|semi)         default: auto
#   CR_CMD          (실행한 명령 문자열)
#   CR_VALUE_JSON   (이미 JSON 으로 인코딩된 value fragment)
#   CR_STATUS       (ok|warn|crit|error|pending_manual|n/a) default: ok
#   CR_THRESHOLD_JSON (이미 JSON 객체로 인코딩된 {warn,crit})
#   CR_RAW          (원본 출력 — 너무 크면 truncate)
#   CR_TOOK_MS      (정수)
#   CR_NOTE         (자유 메모)
#
# OUTPUT: 단일 JSON 객체를 stdout 으로
new_check_result() {
    _method=${CR_METHOD:-auto}
    _status=${CR_STATUS:-ok}
    _value=${CR_VALUE_JSON:-null}
    _took=${CR_TOOK_MS:-0}

    _o=$(json_obj_start)
    _o=$(json_obj_add "$_o" id       "$(json_quote "$CR_ID")")
    _o=$(json_obj_add "$_o" label    "$(json_quote "$CR_LABEL")")
    _o=$(json_obj_add "$_o" category "$(json_quote "$CR_CATEGORY")")
    _o=$(json_obj_add "$_o" method   "$(json_quote "$_method")")
    if [ -n "${CR_CMD:-}" ]; then
        _o=$(json_obj_add "$_o" cmd "$(json_quote "$CR_CMD")")
    else
        _o=$(json_obj_add "$_o" cmd "null")
    fi
    _o=$(json_obj_add "$_o" value  "$_value")
    _o=$(json_obj_add "$_o" status "$(json_quote "$_status")")
    _o=$(json_obj_add "$_o" took_ms "$(json_num_or_null "$_took")")
    if [ -n "${CR_THRESHOLD_JSON:-}" ]; then
        _o=$(json_obj_add "$_o" threshold "$CR_THRESHOLD_JSON")
    fi
    if [ -n "${CR_RAW:-}" ]; then
        _o=$(json_obj_add "$_o" raw "$(json_quote "$CR_RAW")")
    fi
    if [ -n "${CR_NOTE:-}" ]; then
        _o=$(json_obj_add "$_o" note "$(json_quote "$CR_NOTE")")
    fi
    json_obj_end "$_o"

    # 다음 호출을 위해 입력 변수 reset (caller 가 안 해도 안전하도록)
    unset CR_ID CR_LABEL CR_CATEGORY CR_METHOD CR_CMD CR_VALUE_JSON \
          CR_STATUS CR_THRESHOLD_JSON CR_RAW CR_TOOK_MS CR_NOTE
}

# ── Threshold 기반 status 결정 ─────────────────────────────────────
# usage: resolve_status VALUE WARN CRIT [INVERTED]
#   INVERTED=1 이면 큰 값이 좋음 (예: free memory).
resolve_status() {
    _v=$1; _w=$2; _c=$3; _inv=${4:-0}
    [ -z "$_w" ] && [ -z "$_c" ] && { echo ok; return; }
    # awk 로 수치 비교 (sh 는 부동소수 불가)
    awk -v v="$_v" -v w="$_w" -v c="$_c" -v inv="$_inv" 'BEGIN{
        if (inv == 1) {
            if (v <= c) { print "crit"; exit }
            if (v <= w) { print "warn"; exit }
            print "ok"
        } else {
            if (v >= c) { print "crit"; exit }
            if (v >= w) { print "warn"; exit }
            print "ok"
        }
    }'
}

# ── Snapshot 래퍼 ──────────────────────────────────────────────────
# inputs:
#   $1   ITEMS_JSON  — 이미 빌드된 items 배열 (e.g. [{"id":...},{"id":...}])
#   $2   TOTAL_MS    — 전체 소요 ms
#   기타 SITE/SITE_NAME/TIER/INSPECTOR 환경변수
build_snapshot() {
    _items=$1; _total_ms=$2

    # 카운트 (각 status 별 등장 횟수). 단순 grep -o 로 충분.
    _total=$(printf '%s' "$_items" | grep -o '"id":' | wc -l | tr -d ' ')
    _ok=$(printf   '%s' "$_items" | grep -o '"status":"ok"'             | wc -l | tr -d ' ')
    _warn=$(printf '%s' "$_items" | grep -o '"status":"warn"'           | wc -l | tr -d ' ')
    _crit=$(printf '%s' "$_items" | grep -o '"status":"crit"'           | wc -l | tr -d ' ')
    _err=$(printf  '%s' "$_items" | grep -o '"status":"error"'          | wc -l | tr -d ' ')
    _man=$(printf  '%s' "$_items" | grep -o '"status":"pending_manual"' | wc -l | tr -d ' ')

    _round=$(date +%Y-%m)
    _round_date=$(date +%Y-%m-%d)

    # host
    _h=$(json_obj_start)
    _h=$(json_obj_add "$_h" hostname  "$(json_quote_or_null "$(host_hostname)")")
    _h=$(json_obj_add "$_h" os        "$(json_quote_or_null "$(host_os_caption)")")
    _h=$(json_obj_add "$_h" os_detail "$(json_quote_or_null "$(host_os_detail)")")
    _h=$(json_obj_add "$_h" model     "$(json_quote_or_null "$(host_model)")")
    _h=$(json_obj_add "$_h" ip        "$(json_quote_or_null "$(host_primary_ip)")")
    _h=$(json_obj_end "$_h")

    # summary
    _s=$(json_obj_start)
    _s=$(json_obj_add "$_s" total_items    "$_total")
    _s=$(json_obj_add "$_s" ok             "$_ok")
    _s=$(json_obj_add "$_s" warn           "$_warn")
    _s=$(json_obj_add "$_s" crit           "$_crit")
    _s=$(json_obj_add "$_s" errors         "$_err")
    _s=$(json_obj_add "$_s" pending_manual "$_man")
    _s=$(json_obj_end "$_s")

    _o=$(json_obj_start)
    _o=$(json_obj_add "$_o" schema        "$(json_quote 'snapshot/v1')")
    _o=$(json_obj_add "$_o" site          "$(json_quote "${SITE:-unknown}")")
    _o=$(json_obj_add "$_o" site_name     "$(json_quote "${SITE_NAME:-unknown}")")
    _o=$(json_obj_add "$_o" round         "$(json_quote "$_round")")
    _o=$(json_obj_add "$_o" round_date    "$(json_quote "$_round_date")")
    _o=$(json_obj_add "$_o" tier          "$(json_quote "${TIER:-unknown}")")
    _o=$(json_obj_add "$_o" host          "$_h")
    _o=$(json_obj_add "$_o" taken_at      "$(json_quote "$(iso_now)")")
    _o=$(json_obj_add "$_o" took_ms       "$(json_num_or_null "$_total_ms")")
    _o=$(json_obj_add "$_o" agent_version "$(json_quote "$AGENT_VERSION")")
    _o=$(json_obj_add "$_o" inspector     "$(json_quote_or_null "${INSPECTOR:-}")")
    _o=$(json_obj_add "$_o" items         "$_items")
    _o=$(json_obj_add "$_o" summary       "$_s")
    json_obj_end "$_o"
}

# ── 저장 ──────────────────────────────────────────────────────────
save_snapshot() {
    _dir=$1; _snap=$2
    _site=$(printf '%s' "$_snap" | sed -n 's/.*"site":"\([^"]*\)".*/\1/p' | head -1)
    _round=$(printf '%s' "$_snap" | sed -n 's/.*"round":"\([^"]*\)".*/\1/p' | head -1)
    _tier=$(printf '%s' "$_snap" | sed -n 's/.*"tier":"\([^"]*\)".*/\1/p' | head -1)
    _stamp=$(date +%Y-%m-%dT%H%M%S)
    _name="${_site}-${_round}-${_tier}-${_stamp}.json"
    _path="$_dir/$_name"
    mkdir -p "$_dir"
    printf '%s' "$_snap" > "$_path"
    echo "$_path"
}

# ── 직전 회차 조회 ─────────────────────────────────────────────────
# stdout: 이전 스냅샷의 절대경로 (없으면 빈 출력)
get_latest_previous_snapshot() {
    _dir=$1; _site=$2; _tier=$3; _current_round=$4
    [ -d "$_dir" ] || return 0
    # 동일 site+tier 의 모든 파일 중 round 가 current 와 다른 것 중 최신
    ls -1 "$_dir"/${_site}-*-${_tier}-*.json 2>/dev/null | sort -r | while read -r f; do
        _r=$(sed -n 's/.*"round":"\([^"]*\)".*/\1/p' "$f" | head -1)
        if [ "$_r" != "$_current_round" ]; then
            echo "$f"
            break
        fi
    done | head -1
}
