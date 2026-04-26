#!/usr/bin/env bash
# T-H: N팀 가정 회귀 차단 grep
# (sprint team-monitor-wildcard-watcher §6-2 T-H)
#
# 향후 누군가 4팀/5팀/N팀 가정 코드를 추가하면 본 스크립트가 ❌ 로 차단.
# CI 통합 권장.
#
# 코드 내장 fallback 블록은 ALLOW_FIVE_TEAM_FALLBACK 센티넬로 예외 처리:
#   Java/JS:   // ALLOW_FIVE_TEAM_FALLBACK  (블록 바로 위 줄)
#   Shell:     # ALLOW_FIVE_TEAM_FALLBACK
#
# 제외 디렉토리: docs/ (예시), .git, target, build, node_modules, src/test/java
# 제외 파일: .team-workspace/teams.json (정의 파일)

set -Euo pipefail   # N21 TH-03

HITS=0

# T-H-LOGSKIP — log_skip 헬퍼 (set -Euo pipefail 상태에서 미정의 호출 방지)
log_skip() { echo "ℹ️  SKIP: $*" >&2; }

# 공통 exclude (S6-02)
EXCLUDE_OPTS="--exclude-dir=docs --exclude-dir=.git --exclude-dir=target --exclude-dir=build --exclude-dir=node_modules --exclude-dir=.idea"
# 자기 자신 (검사 패턴 인자에 fixture 문자열을 사용) 결과에서 제외
SELF_FILTER="grep -v check-no-n-team-assumptions.sh"

# 1) 셸 case 고정 5팀 열거
HITS_SH=$(grep -ErHn $EXCLUDE_OPTS --include='*.sh' \
    'case[^|]*planner.*db.*developer.*codex.*designer' \
    .team-workspace/ src/ 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 2) Java 5팀 명시 열거 (src/main/java 한정 — N21 TH-01)
HITS_CODE_JAVA=$(grep -ErHn $EXCLUDE_OPTS --include='*.java' \
    'planner.*db.*developer.*codex.*designer' \
    src/main/java 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 3) JS/HTML 5팀 명시 열거
HITS_CODE_FRONT=$(grep -ErHn $EXCLUDE_OPTS --include='*.js' --include='*.html' \
    'planner.*db.*developer.*codex.*designer' \
    src/main/resources 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 4) Java size/length == 4 또는 5
HITS_JAVA=$(grep -ErHn $EXCLUDE_OPTS --include='*.java' \
    '\b(size|length)\s*\(\)\s*==\s*[45]\b' \
    src/main/java 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 5) JS .length === 4 또는 5
HITS_JS=$(grep -ErHn $EXCLUDE_OPTS --include='*.js' \
    '\.length\s*===?\s*[45]\b' \
    src/main/resources/static 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 6) CSS — 5팀 명시 grid (4팀 제거 — 오탐 회피)
HITS_CSS=$(grep -ErHn $EXCLUDE_OPTS --include='*.css' \
    'grid-template-columns:\s*repeat\(\s*5\s*,\s*1fr\)' \
    src/main/resources/static 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 7) 명시 상수
HITS_CONST=$(grep -ErHn $EXCLUDE_OPTS --include='*.java' --include='*.js' --include='*.sh' \
    '\b(FourTeams|FiveTeams|TEAMS_4|TEAMS_5)\b' \
    src/ .team-workspace/ 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 8) 멀티라인 case (N21 TH-02 + S3-01 + S3-01-a 기능 감지)
# 본 스크립트 자체는 검사 인자에 case/planner/db/... 문자열을 사용하므로 grep 결과에서 별도 제외
HITS_SH_ML=""
if command -v pcregrep >/dev/null 2>&1; then
    HITS_SH_ML=$(pcregrep -M -rn --include='*.sh' \
        'case[\s\S]*?planner[\s\S]*?db[\s\S]*?developer[\s\S]*?codex[\s\S]*?designer' \
        .team-workspace/ src/ 2>/dev/null \
        | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' \
        | grep -v 'check-no-n-team-assumptions.sh' || true)
elif (printf '' | grep -Pzo '.*' >/dev/null 2>&1) || (grep --version 2>/dev/null | grep -q 'GNU'); then
    HITS_SH_ML=$(grep -Pzo --include='*.sh' \
        'case[\s\S]*?planner[\s\S]*?db[\s\S]*?developer[\s\S]*?codex[\s\S]*?designer' \
        -rn .team-workspace/ src/ 2>/dev/null \
        | tr '\0' '\n' \
        | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' \
        | grep -v 'check-no-n-team-assumptions.sh' || true)
else
    log_skip "멀티라인 case 검사 — pcregrep / GNU grep 부재로 SKIP"
fi

# 결과 집계 — 본 스크립트 자체 (검사 패턴 인자에 fixture 문자열 포함) 제외
filter_self() {
    grep -v 'check-no-n-team-assumptions.sh' || true
}
for h in "$HITS_SH" "$HITS_CODE_JAVA" "$HITS_CODE_FRONT" "$HITS_JAVA" "$HITS_JS" "$HITS_CSS" "$HITS_CONST" "$HITS_SH_ML"; do
    h_filtered=$(echo "$h" | filter_self)
    if [ -n "$h_filtered" ]; then
        echo "❌ N팀 가정 패턴 발견:"
        echo "$h_filtered"
        HITS=1
    fi
done

# 9) 센티넬 직전 라인 자동 점검 (N21 TH-04 / TH-04-S)
# ALLOW_FIVE_TEAM_FALLBACK 센티넬은 fallback 블록 "다음 라인" (즉, 센티넬 라인의 직후) 에 있어야 함
# 본 스크립트 자체는 검사 인자/메타에 센티넬 문자열을 사용하므로 제외 (--exclude)
sentinel_violations=$(
    grep -lr --include='*.java' --include='*.js' --include='*.sh' \
        --exclude=check-no-n-team-assumptions.sh \
        'ALLOW_FIVE_TEAM_FALLBACK' src/ .team-workspace/ 2>/dev/null | while IFS= read -r f; do
        awk '
          /ALLOW_FIVE_TEAM_FALLBACK/ {
            sentinel_line = NR
            if ((getline next_content) > 0) {
              if (next_content !~ /planner|TEAMS|TEAM_FILES|"planner"|new TeamMeta|FALLBACK_META|CODE_BUILTIN/) {
                printf "%s:%d: ALLOW_FIVE_TEAM_FALLBACK 센티넬 다음 라인이 fallback 코드 아님\n", FILENAME, sentinel_line
              }
            }
          }
        ' "$f"
    done
)

if [ -n "$sentinel_violations" ]; then
    echo "❌ ALLOW_FIVE_TEAM_FALLBACK 센티넬 규약 위반:"
    echo "$sentinel_violations"
    HITS=1
fi

[ $HITS -eq 0 ] || exit 1
echo "✅ T-H 통과 — N팀 가정 패턴 0건 + 센티넬 규약 준수"
