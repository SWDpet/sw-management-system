#!/usr/bin/env bash
# 팀 상태 갱신 헬퍼
# 사용: bash set-status.sh <team> <state> <progress> [task...]
#   team     : planner | db | developer | codex
#   state    : 대기 | 진행중 | 완료 | 오류
#   progress : 0-100
#   task     : (선택) 현재 작업 설명
#
# 예:
#   bash set-status.sh planner 진행중 40 "자동 백업 기획서 작성"
#   bash set-status.sh db 완료 100 "인덱스 튜닝 검토"
#   bash set-status.sh developer 대기 0

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STATUS_DIR="$SCRIPT_DIR/status"

usage() {
  sed -n '2,14p' "$0"
  exit 1
}

[ $# -lt 3 ] && usage

team="$1"
state="$2"
progress="$3"
shift 3
task="$*"

# 팀 검증
case "$team" in
  planner|db|developer|codex) ;;
  *) echo "❌ team 은 planner|db|developer|codex 중 하나여야 합니다."; exit 1 ;;
esac

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
cat > "$STATUS_DIR/$team.status" <<EOF
team=$team
state=$state
progress=$progress
task=$task
updated=$(date +%s)
EOF

echo "✓ $team  →  [$state]  ${progress}%  ${task:-(작업 없음)}"
