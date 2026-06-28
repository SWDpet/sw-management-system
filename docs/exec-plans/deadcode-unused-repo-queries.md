# [개발계획] 미사용 repository 쿼리 메서드 30개 일괄 제거

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/deadcode-unused-repo-queries.md`
- **상태**: ✅ 구현 완료(2026-06-28). 30 메서드/14파일 삭제+import 정리. BUILD SUCCESS·1510 green(컴파일=호출처 0 증명), 커버리지 불변. codex 구현검증 PASS·dual-review 합의2·분쟁41 전건 오탐(build green/live import 사용 반증). 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- 탐지 스크립트(`(?:\.|::)<name>\b`, src/**/*.java 전수) → 30 메서드 main+test 호출 0.
- 스폿체크 직접 grep: searchOpsDocuments/findAllWithSearch = 0 occurrence(검색기능은 findAllByKeyword 등 별도), findByYear/findByGubun = 유사명(findByYearAnd…/findByGubunAndStatus)만 사용 → 바레명 dead 확정.

## 1. 삭제 절차 (14 파일, 30 메서드)
- 각 메서드: 선언(`<반환> name(...);`) + 부속 `@Query("...")`(있으면) + 직전 1줄 설명 주석을 Edit 으로 제거.
- **유지**: 동일 repo 의 유사명 live 메서드(findByYearAndInterimYn…, findByGubunAndStatus 등)·다른 모든 메서드.
- 파일별 순서: SwProjectRepository(11)·InspectMetricSnapshot(3)·QuotationItem(2)·InspectReport(2)·OpsDocument(2)·DocumentDetail(2)·나머지 6파일 1씩.

## 2. 검증 절차
1. `./mvnw -o clean verify` → **BUILD SUCCESS**(컴파일 = 잔여 호출처 0·orphan import 0 정의적 증명). 실 호출처 잔존 시 즉시 컴파일 FAIL → 해당 메서드 복원/재검토.
2. 전체 테스트 green(기능 회귀 0 — 미사용 제거라 동작 무변경).
3. `jacoco.csv` → 커버리지 유지/소폭 상향(repo 인터페이스 메서드는 커버리지 분모 영향 미미), floor 유지.
4. 구현 후 codex 검증(독립 재확인) → dual-review → 듀얼푸시.

## 3. 리스크/완화
- **R1**: 삭제 후 컴파일 에러 = 실 호출처 신호 → 즉시 검출·해당 1개만 복원. 일괄이라도 안전(컴파일 게이트).
- **R2 import**: 미사용화 import 정리(예상 잔존). 컴파일 확인.
- **R3 derived-name 충돌**: `findByYear` 삭제가 `findByYearAndStat` 등 유사명에 영향 없음(별개 메서드). Edit old_string 에 시그니처 전체 포함해 정확 타겟.
- production 회귀 0.
