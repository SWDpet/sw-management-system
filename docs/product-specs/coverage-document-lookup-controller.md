# [기획서] DocumentLookupController 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-27
- **트랙**: beyond-A 커버리지 증분 B. 기존 `DocumentLookupControllerTest` 확장.
- **상태**: ✅ 구현 완료(2026-06-27). DocumentLookupControllerTest +10(12→22), DocumentLookupController **60.9→100% LINE**(92/92)·BRANCH 80.8%, 전역 LINE 76.4→76.7%·INSTR 62.49→62.7%, floor 유지. mvnw -o clean verify 1391 green. 구현 후 codex 검증: NEEDS-FIX(얕은 단언) 5건 반영(InfraNotFound/InfraFindResult 타입+필드, ProcessMaster/ServicePurpose DTO 매핑, ProjectFilterRow/Region/SystemOption 필드, purposeId) → 충족.

---

## 1. 배경 / 목표

`DocumentLookupController`(287줄, **LINE 60.9% / miss 36**)는 문서작성 폼의 지역·시스템·사업 cascade 조회 API. 기존 테스트(12개)는 user/project 정보 조회만 덮어, **cascade/lookup 단순 위임 ~13개 엔드포인트**가 전부 미커버였다.

## 2. 변경 — 케이스 추가(setUp repo 5종 필드참조 보유로 리팩터)
- 사업 cascade(years/cities/districts/systems 위임) + getProjectsFiltered(DTO 매핑 단언)
- region(sidos/sigungus from/systemsAll from) — DTO 필드 단언
- infra cascade(cities/districts/systems) + findInfraByRegion(empty→InfraNotFound found=false, found→InfraFindResult found=true+필드)
- getProcessMasterList(ProcessMasterRow 매핑), getServicePurposeList(purposeType 유/무 분기, ServicePurposeRow 매핑)
- getUserInfoSecure notFound

## 3. 요건
- FR: 위 분기 박제(응답 계약/DTO 매핑 회귀 방어 단언). NFR: production 0, verify SUCCESS, 60.9→100%, floor 유지, 구현 후 codex PASS → 듀얼푸시.

## 4. 영향/리스크
- `controller/DocumentLookupControllerTest.java` 케이스 추가. production 0. setUp 리팩터(인라인 mock→필드참조, 생성자 인자 순서 보존).
