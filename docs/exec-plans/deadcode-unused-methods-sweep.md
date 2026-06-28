# [개발계획] 미사용 메서드 2종 제거

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/deadcode-unused-methods-sweep.md`
- **상태**: ✅ 구현 완료(2026-06-28). sysList·includes 삭제, BUILD SUCCESS, 전역 LINE 81.203%. codex APPROVE/dual-review 전건 오탐(build green 반증). 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- private 메서드 전수 스캔: 미사용 후보 1(`sysList`). public service 메서드 스캔: license·false-positive 제외 후 1(`includes`). recordHistory 는 내부 bare 호출(L165/201) 확인 → 유지.
- sysList: `model.addAttribute("systems", sysOptions())` 가 live(L88/103/116), sysList 는 0 호출.
- includes: `InspectMaintProfile.includes(` Java 호출 0(테스트 포함).

## 1. 삭제 (2 메서드)
- **D1** `OpsKbController.java` L72-76: `private List<String> sysList() { ... }`.
- **D2** `InspectMaintProfile.java` L63-67: `/** 특정 섹션이 점검 범위 내인지. */` javadoc + `public static boolean includes(String maintType, boolean hasStandard, String section) { return sections(...).contains(section); }`.
- 유지: sysOptions·sections·badgeLabel·badgeTone·wantsStandard 등 live.

## 2. 검증 절차
1. `./mvnw -o clean verify` → BUILD SUCCESS(컴파일=잔여 호출처 0 증명), 전체 green.
2. 커버리지 유지(분모 미세 축소), floor 유지.
3. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 3. 리스크/완화
- **R1**: 삭제 후 컴파일 에러=잔존 호출처 신호 → 즉시 검출. bare 호출도 컴파일이 잡음.
- **R2 import**: 미사용화 시 정리(List 등 다수 사용이라 잔존 예상).
- production 회귀 0.
