#!/usr/bin/env bash
# 팀 진행율 모니터 — status/*.status 파일을 읽어 5초마다 갱신
# 사용: bash monitor.sh  (또는 더블클릭으로 monitor.cmd)

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STATUS_DIR="$SCRIPT_DIR/status"
INTERVAL=5
BAR_WIDTH=20

# 팀 표시 순서
TEAMS=(planner db developer codex)

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
  case "$team" in
    planner)   label="🧭 PLANNER  " ;;
    db)        label="🗄️  DB       " ;;
    developer) label="🛠️  DEVELOPER" ;;
    codex)     label="🤖 CODEX    " ;;
    *)         label="$team" ;;
  esac

  printf "  %s%s%s  " "$BOLD" "$label" "$RESET"
  state_icon "$state"
  printf "  [%s] %s%3d%%%s\n" "$(render_bar "$progress")" "$BOLD" "$progress" "$RESET"
  printf "    %s작업%s: %s\n" "$DIM" "$RESET" "${task:--}"
  printf "    %s갱신%s: %s\n" "$DIM" "$RESET" "$(ago "$updated")"
  echo
}

mkdir -p "$STATUS_DIR"

while true; do
  clear
  printf "%s%s════════════════════════════════════════════════════════%s\n" "$BOLD" "$CYAN" "$RESET"
  printf "%s%s           팀 진행율 모니터  ·  %s%s\n" "$BOLD" "$CYAN" "$(date '+%Y-%m-%d %H:%M:%S')" "$RESET"
  printf "%s%s════════════════════════════════════════════════════════%s\n" "$BOLD" "$CYAN" "$RESET"
  echo

  for team in "${TEAMS[@]}"; do
    render_team "$team"
  done

  printf "%s갱신 주기 %ds  ·  상태 갱신: bash set-status.sh  ·  Ctrl+C 종료%s\n" "$DIM" "$INTERVAL" "$RESET"
  sleep "$INTERVAL"
done
