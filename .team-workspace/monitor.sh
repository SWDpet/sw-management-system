#!/usr/bin/env bash
# 팀 진행율 모니터 — status/*.status 파일을 읽어 5초마다 갱신
# 사용: bash monitor.sh  (또는 더블클릭으로 monitor.cmd)
#
# sprint team-monitor-wildcard-watcher (FR-6-b/c):
#   - TEAMS 하드코딩 → 디렉토리 스캔 (*.status, dot-prefix 제외)
#   - 라벨/이모지/sort_order: teams.json (jq) 우선, fallback: 대문자 + 📋

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STATUS_DIR="$SCRIPT_DIR/status"
TEAMS_JSON="$SCRIPT_DIR/teams.json"
INTERVAL=5
BAR_WIDTH=20

# ANSI
RESET=$'\e[0m'
BOLD=$'\e[1m'
DIM=$'\e[2m'
CYAN=$'\e[36m'
GREEN=$'\e[32m'
YELLOW=$'\e[33m'
RED=$'\e[31m'
GRAY=$'\e[90m'

render_bar() {
  local pct=${1:-0}
  (( pct < 0 )) && pct=0
  (( pct > 100 )) && pct=100
  local filled=$(( pct * BAR_WIDTH / 100 ))
  local bar=""
  for ((i=0; i<BAR_WIDTH; i++)); do
    if (( i < filled )); then bar="${bar}█"; else bar="${bar}·"; fi
  done
  printf "%s" "$bar"
}

state_icon() {
  case "$1" in
    진행중|running)  printf "%s🟡 진행중%s" "$YELLOW" "$RESET" ;;
    완료|done)       printf "%s🟢 완료  %s" "$GREEN"  "$RESET" ;;
    오류|error)      printf "%s🔴 오류  %s" "$RED"    "$RESET" ;;
    *)               printf "%s⚪ 대기  %s" "$GRAY"   "$RESET" ;;
  esac
}

ago() {
  local epoch=$1
  [ -z "$epoch" ] && { printf "-"; return; }
  local now=$(date +%s)
  local diff=$(( now - epoch ))
  (( diff < 0 )) && diff=0
  if   (( diff < 60 ));    then printf "%d초 전" "$diff"
  elif (( diff < 3600 ));  then printf "%d분 전" $(( diff / 60 ))
  elif (( diff < 86400 )); then printf "%d시간 전" $(( diff / 3600 ))
  else                          printf "%d일 전" $(( diff / 86400 ))
  fi
}

# 디렉토리 스캔 + 정렬 (FR-6-b)
build_teams() {
  if [ ! -d "$STATUS_DIR" ]; then
    mkdir -p "$STATUS_DIR"
  fi

  # 1차: 디렉토리 스캔 (*.status, dot-prefix 제외)
  local raw_teams
  raw_teams=$(find "$STATUS_DIR" -maxdepth 1 -type f -name '*.status' ! -name '.*' \
              -print 2>/dev/null | xargs -n1 basename 2>/dev/null | sed 's/\.status$//')

  if [ -z "$raw_teams" ]; then
    TEAMS=()
    return
  fi

  # 2차: teams.json sort_order 적용 (jq 있으면)
  if [ -f "$TEAMS_JSON" ] && command -v jq >/dev/null 2>&1; then
    TEAMS=()
    while IFS=$'\t' read -r team _; do
      [ -n "$team" ] && TEAMS+=("$team")
    done < <(echo "$raw_teams" | while read -r t; do
      so=$(jq -r ".teams.\"$t\".sort_order // 99" "$TEAMS_JSON")
      printf "%s\t%s\n" "$t" "$so"
    done | sort -t$'\t' -k2 -n -k1)
  else
    # fallback: 알파벳순
    mapfile -t TEAMS < <(echo "$raw_teams" | sort)
  fi
}

# 라벨/이모지 lookup (FR-6-b)
team_label() {
  local team="$1"
  if [ -f "$TEAMS_JSON" ] && command -v jq >/dev/null 2>&1; then
    local emoji label
    emoji=$(jq -r ".teams.\"$team\".emoji // \"📋\"" "$TEAMS_JSON")
    label=$(jq -r ".teams.\"$team\".label // \"$(echo "$team" | tr '[:lower:]' '[:upper:]')\"" "$TEAMS_JSON")
    # null 응답 처리
    [ "$emoji" = "null" ] && emoji="📋"
    [ "$label" = "null" ] && label="$(echo "$team" | tr '[:lower:]' '[:upper:]')"
    printf "%s %s" "$emoji" "$label"
  else
    # fallback: 📋 + 대문자
    printf "📋 %s" "$(echo "$team" | tr '[:lower:]' '[:upper:]')"
  fi
}

render_team() {
  local team="$1"
  local f="$STATUS_DIR/$team.status"
  local task="" state="" progress="0" updated=""
  if [ -f "$f" ]; then
    while IFS='=' read -r k v; do
      case "$k" in
        task)     task="$v" ;;
        state)    state="$v" ;;
        progress) progress="$v" ;;
        updated)  updated="$v" ;;
      esac
    done < "$f"
  fi

  local label
  label=$(team_label "$team")

  printf "  %s%s%s  " "$BOLD" "$label" "$RESET"
  state_icon "$state"
  printf "  [%s] %s%3d%%%s\n" "$(render_bar "$progress")" "$BOLD" "$progress" "$RESET"
  printf "    %s작업%s: %s\n" "$DIM" "$RESET" "${task:--}"
  printf "    %s갱신%s: %s\n" "$DIM" "$RESET" "$(ago "$updated")"
  echo
}

while true; do
  clear
  printf "%s%s════════════════════════════════════════════════════════%s\n" "$BOLD" "$CYAN" "$RESET"
  printf "%s%s           팀 진행율 모니터  ·  %s%s\n" "$BOLD" "$CYAN" "$(date '+%Y-%m-%d %H:%M:%S')" "$RESET"
  printf "%s%s════════════════════════════════════════════════════════%s\n" "$BOLD" "$CYAN" "$RESET"
  echo

  build_teams
  if [ ${#TEAMS[@]} -eq 0 ]; then
    printf "  %s활성 팀 없음 — bash set-status.sh <팀명> ... 으로 첫 팀 추가 권장%s\n\n" "$DIM" "$RESET"
  else
    for team in "${TEAMS[@]}"; do
      render_team "$team"
    done
  fi

  printf "%s갱신 주기 %ds  ·  상태 갱신: bash set-status.sh  ·  Ctrl+C 종료%s\n" "$DIM" "$INTERVAL" "$RESET"
  sleep "$INTERVAL"
done
