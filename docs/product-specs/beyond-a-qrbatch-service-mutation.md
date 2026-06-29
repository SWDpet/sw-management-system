# [기획서] InspectionQrBatchService 테스트 강화 — 뮤테이션 81.8%→게이트 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A (PIT 게이트 확장). codex 자문 — pom 의 "44%" 는 stale, 재측정 권고.
- **상태**: ✅ 구현 완료(2026-06-29).

---

## 1. 배경
pom PIT 백로그에 "InspectionQrBatch 44%"로 기록돼 게이트 제외였으나, **codex 자문이 stale 지적** → 재측정. ⚠**원인=2테스트 분리**(InspectionQrBatchServiceTest+InspectionQrBatchFormatTest)인데 한쪽만 측정해 44%로 본 것(WorkPlanService 와 동일 패턴). **둘 다 측정 시 실제 81.8%**(생존 21·NO_COV 5).

## 2. 강화 (PIT 생존 mutator별)
- buildReport 감사필드(inspUserId·createdBy·updatedBy) ArgumentCaptor 단언(L175~177).
- verifyHash 직접 호출: null/blank→skip, 불일치→warn(L384·395 + sha1Hex L419).
- DB_USAGE/AP_USAGE 행 reportId+sortOrder 증가(L251·255·275·279, 드라이브 2개로 diskSort++ 검출).
- upload merge 경로 updatedBy 갱신 + batchId 보존(L117).
- 멱등 alias 폴백(findBySiteCode 빈값→findFirstBySiteCodeAlias, L425 NO_COV 커버).

## 3. 결과
- InspectionQrBatchService 뮤테이션 **81.8→94.4%**(생존 21→5·NO_COV 5→3). 테스트 24→29.
- PIT 게이트 **14→15클래스** 편입, threshold 94 유지(전체 640/664=96.4%).
- ⚠**잔존 NO_COV 3 = verifyHash warn-only ok/예외 경로**: "ok"는 encode.py 호환 해시를 테스트가 재유도하는 **순환·취약 테스트**라 미강제, 예외는 직렬화 실패 필요라 low-value. 94.4%면 codex 기준(≥90~92%) 충족이라 편입(NO_COV 작음).
- 정규 verify green(29테스트+coverage), PIT 96% BUILD SUCCESS. dual-review(vacuous never 제거·batchId 단언 추가). production 0.

## 4. 교훈
- **여러 테스트로 커버되는 클래스는 targetTests 에 전부 추가**(WorkPlan·QrBatch 공통 함정).
- 컨트롤러(SwController 68%)는 PIT 부적합 → MockMvc 별도 트랙(codex·나 합의).
