# [기획서] 미사용 repository 쿼리 메서드 30개 일괄 제거 (무손실)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: production 무손실 정리(beyond-A 헌장 "dead code부터 의심·삭제"). [[deadcode-workplan-precontacts]] 후속 — 누적 미사용 Spring Data 쿼리 일괄 제거.
- **상태**: ✅ 구현 완료(2026-06-28). 14 repository 인터페이스에서 dead 쿼리 30개 삭제(+미사용 import 정리: DocumentDetail/QuotationItem List·Optional, OpsDocument Page·Pageable). **`mvnw -o clean verify` BUILD SUCCESS, 1510 tests green**(컴파일=잔여 호출처 0·orphan import 0 정의적 증명), 커버리지 LINE 81.18%/INSTR 66.47% 불변(인터페이스 선언은 LINE 분모 무영향), floor 0.78/0.64 유지. 180 deletions. codex 기획 APPROVE(독립 grep 재검증)·구현검증 PASS(30개 호출 0·live 유사명 보존 독립확인). dual-review(codex22/Opus21) 합의2·분쟁41 **전건 오탐**: 분쟁=diff-blindness("삭제가 호출처 깸")→build green 반증, 합의2(LocalDateTime/Page·Pageable "미사용 import" 우려)=오탐(countCompletedByTypeAndUser·findAccessTab/findMenuTab 등 live 메서드가 여전히 사용). 실결함 0.

---

## 1. 배경 / 목표

repository 인터페이스 전수 스캔(비-license) 결과, **선언만 있고 production·test 통틀어 호출처가 0인 Spring Data 쿼리 메서드 30개**가 누적돼 있다. Spring Data 메서드는 **오직 명시적 Java 호출(`.name(`/`::name`)로만 실행**되므로(리플렉션·XML·SpEL named-query 무관), repo-wide grep 0 = 진짜 dead. 유지보수 표면(누군가 "쓰이는 쿼리"로 오인)만 늘리는 부채다. 무손실 헌장상 일괄 삭제한다.

**탐지/검증**: `(?:\.|::)<name>\b` 정규식으로 src/**/*.java(main+test) 전수 카운트=0. `\b` 로 prefix 충돌 배제(`findByYear` ≠ `findByYearAndStat`, `findByGubun` ≠ `findByGubunAndStatus` 정확 구분 — 직접 grep 확인). SQL 키워드(COALESCE 등) 오탐·license 계열 제외.

## 2. 범위 (production 무손실 삭제) — 30 메서드 / 14 파일

| repository | 삭제 메서드 |
|---|---|
| SwProjectRepository (11) | existsByYearAndDistCdAndSysNmEn, findAllOrderByCustom, findByCityNmAndDistNmOrderByYearDescProjIdDesc, findByProjNmContaining, findBySysNmContaining, findByYear, findByYearAndCompYnOrderByCityNmAscDistNmAsc, findByYearAndPayProgYnOrderByCityNmAscDistNmAsc, findByYearAndStat, findDistinctCityDistByYear, findTop6ByOrderByProjIdDesc |
| InspectMetricSnapshotRepository (3) | findHostsByPjtRole, findRecentByPjtRole, findRecentByPjtRoleHost |
| QuotationItemRepository (2) | deleteByQuotationQuoteId, findByQuotationQuoteIdOrderByItemNoAsc |
| InspectReportRepository (2) | findByBatchIdAndDeletedAtIsNull, findTopByPjtIdAndInspectMonthLessThanAndDeletedAtIsNullOrderByInspectMonthDesc |
| OpsDocumentRepository (2) | findByAuthor_UserSeqOrderByCreatedAtDesc, searchOpsDocuments |
| DocumentDetailRepository (2) | findByDocument_DocIdAndSectionKey, findByDocument_DocIdOrderBySortOrder |
| QuotationRepository (1) | findByCategoryOrderByCreatedAtDesc |
| AccessLogRepository (1) | findAllWithSearch |
| InspectCheckResultRepository (1) | findByReportIdAndSection |
| InspectTemplateRepository (1) | findByTemplateTypeAndSectionAndUseYnOrderBySortOrderAsc |
| UserRepository (1) | findByOrgUnitIdOrderByUsernameAsc |
| OpsKbRepository (1) | findByGubun |
| ContractParticipantRepository (1) | findByProject_ProjIdAndIsSiteRepTrue |
| WorkPlanRepository (1) | findByPlanTypeInOrderByStartDateDesc |

각 메서드 선언 + 부속 `@Query`(있는 경우) + 직전 설명 주석을 함께 삭제. **다른 유사명 메서드(findByYearAnd…, findByGubunAndStatus 등)는 유지**(사용 중).

## 3. 요건
- **FR-1**: dead 30 메서드 삭제. live 메서드·다른 repo API 무변경.
- **NFR**: `mvnw -o clean verify` SUCCESS(컴파일 = 잔여 호출처 0 정의적 증명, 전체 테스트 green), 커버리지/floor 유지(또는 분모 축소로 소폭 상향), 구현 후 codex PASS + dual-review → 듀얼푸시.

## 4. 영향 / 리스크
- 변경: 14 repository 인터페이스 파일.
- **R1 false-negative 탐지**: 호출이 method-reference(`::name`)·multiline 이어도 정규식이 포착(검증). 그래도 **컴파일이 최종 안전망** — 실 호출처가 남으면 BUILD FAIL 로 즉시 검출.
- **R2 import 정리**: 삭제로 미사용이 되는 import(예: 특정 repo 만 쓰던 타입) 정리. 대부분 List/엔티티 등 다수 사용이라 잔존 예상. 컴파일로 검출.
- **R3 향후 API 보존 우려**: 사용자 승인(일괄 제거). 필요 시 git 히스토리에서 복원 가능.
- production 동작 회귀 0(미사용 코드 제거).
