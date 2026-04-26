# 컴포넌트 디자인 결정 (디자인팀 SSoT)

본 디렉토리는 가상 디자인팀(🎨 designer) 의 컴포넌트별 결정 누적 위치입니다.

- **파일명 규칙**: `<component>.md` (예: `card.md`, `badge.md`, `button.md`)
- **디자인 토큰 SSoT**: `../DESIGN.md` (있으면)
- **결정 시점**: 기획 단계에서 디자인팀 자문 발생 시 즉시 작성 + commit (memory 가 아닌 git history 가 진실원)

## 부트스트랩 메모 (2026-04-26 designer-team-onboarding)

본 디렉토리는 `designer-team-onboarding` 스프린트로 신설됨. 그 시점에는 자체 디자인팀 자문 트리거가 없었으므로 codex 가 1회 디자인 게이트 대행. 이후 UI 변경 스프린트부터는 정식 자문 산출물이 본 디렉토리에 누적될 예정.

## i18n SSoT 진입점 (2026-04-26 team-monitor-wildcard-watcher)

team-monitor 의 안내 문자열 ("활성 팀 없음", "팀 진행율 카드", "활성 팀이 없습니다..." 등) 의 현재 SSoT — `src/main/resources/static/js/team-monitor.js` 의 `createEmptyPlaceholder()` + `createCard()` 인라인 + template 의 `aria-label`. 향후 i18n (다국어) 도입 시 본 위치를 단일 진입점으로 정리. (기획서 §자문-디자인 D6 / N16 비차단 메모)
