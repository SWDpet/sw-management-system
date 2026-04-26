#!/usr/bin/env bash
# 팀 상태 갱신 헬퍼
# 사용: bash set-status.sh <team> <state> <progress> [task...]
#   team     : 영문 소문자 + 숫자 + (-_) 1-32자 (^[a-z][a-z0-9_-]{0,31}$)
#              기본 5팀 (planner|db|developer|codex|designer) 권장
#              신규 팀은 .team-workspace/teams.json 에 메타데이터 추가 권장
#   state    : 대기 | 진행중 | 완료 | 오류
#   progress : 0-100
#   task     : (선택) 현재 작업 설명
#
# 예:
#   bash set-status.sh planner 진행중 40 "자동 백업 기획서 작성"
#   bash set-status.sh tester 진행중 30 "신규 팀 — teams.json 미등록 시 default 이모지"
#
# sprint team-monitor-wildcard-watcher: case 검증 → 정규식 검증 + teams.json 등록 권장 경고.

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STATUS_DIR="$SCRIPT_DIR/status"
TEAMS_JSON="$SCRIPT_DIR/teams.json"

usage() {
  sed -n '2,17p' "$0"
  exit 1
}

[ $# -lt 3 ] && usage

team="$1"
state="$2"
progress="$3"
shift 3
task="$*"

# 팀명 형식 검증 (§4-2 SSoT 정규식 — Java/JS 도 동일)
if ! [[ "$team" =~ ^[a-z][a-z0-9_-]{0,31}$ ]]; then
  echo "❌ team 형식 오류 — ^[a-z][a-z0-9_-]{0,31}\$ 부합 필요"
  echo "   소문자로 시작, 영문 소문자/숫자/하이픈/언더스코어, 1-32자"
  exit 1
fi

# teams.json 등록 권장 경고 (jq 사용 가능 시)
if [ -f "$TEAMS_JSON" ] && command -v jq >/dev/null 2>&1; then
  if ! jq -e ".teams.\"$team\"" "$TEAMS_JSON" >/dev/null 2>&1; then
    echo "⚠ '$team' 메타데이터 미등록. 카드는 default 이모지 (📋) 로 표시됩니다." >&2
    echo "  $TEAMS_JSON 에 추가를 권장합니다 (emoji/label/sort_order)." >&2
  fi
fi

# state 검증
case "$state" in
  대기|진행중|완료|오류) ;;
  *) echo "❌ state 는 대기|진행중|완료|오류 중 하나여야 합니다."; exit 1 ;;
esac

# progress 검증
if ! [[ "$progress" =~ ^[0-9]+$ ]] || (( progress < 0 )) || (( progress > 100 )); then
  echo "❌ progress 는 0~100 사이 정수여야 합니다."
  exit 1
fi

mkdir -p "$STATUS_DIR"

# === POSIX-호환 atomic write (sprint team-monitor-dashboard, 기획 §4-10) ===
# - mktemp 으로 같은 디렉토리에 임시파일 생성 (cross-fs rename 회피).
# - mv 는 같은 파일시스템 내에서 rename(2) 으로 atomic.
# - trap 으로 임시파일 누수 방지 (mv 성공 시 trap 해제).
# - heredoc 대신 printf 사용 — 변수 확장 시 줄바꿈/공백 이슈 방지.
# - Windows Git Bash (msys) 의 mv 도 동일 디렉토리 rename 지원.
tmp=$(mktemp "$STATUS_DIR/.${team}.status.XXXXXX") || { echo "❌ mktemp 실패"; exit 1; }
trap 'rm -f "$tmp"' EXIT
{
  printf 'team=%s\n'     "$team"
  printf 'state=%s\n'    "$state"
  printf 'progress=%s\n' "$progress"
  printf 'task=%s\n'     "$task"
  printf 'updated=%s\n'  "$(date +%s)"
} > "$tmp"
mv -f "$tmp" "$STATUS_DIR/$team.status"
trap - EXIT

echo "✓ $team  →  [$state]  ${progress}%  ${task:-(작업 없음)}"
