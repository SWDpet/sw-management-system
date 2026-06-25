# [개발계획서] DocumentService 커버리지 상향 + dead 정리 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-25
- **기획서**: `docs/product-specs/coverage-document-service.md` (codex APPROVE-WITH-FIX → 보완 반영, 사용자 최종승인 완료)
- **상태**: ✅ 완료 (2026-06-25). codex 기획/개발계획 APPROVE(-WITH-FIX 보완) + 구현검증 PASS(B~F). amendment A 반영. 4파일 수정(서비스·리포지토리·테스트·pom floor).

---

## 1. 작업 개요

DocumentService 의 dead public 3개(+cascade repo 1) + 도달불가 enrichInspectMonth/toMonthLabel(amendment A) 제거 + 실로직 mock 단위테스트 추가. **4파일 수정**(DocumentService·DocumentRepository·DocumentServiceTest·pom.xml floor).

---

## 2. Part A — dead 제거

| 파일 | 제거 | 비고 |
|---|---|---|
| `DocumentService.java` | `getDocumentDTOById`, `getDocumentsByInfra`, `countApprovedDocuments`(직전 `// === 성과통계용 ===` 주석 포함) | 전부 호출처 0 |
| `DocumentService.java` | **(amendment A)** `enrichInspectMonth`·`toMonthLabel` + 필드 `inspectReportRepository`·`jdbcTemplate` | 도달불가 dead(§ 하단 amendment) — searchDocuments 는 enrichRegion 만 호출하도록 정리 |
| `DocumentRepository.java` | `findByInfra_InfraIdOrderByCreatedAtDesc`(L26) | cascade(타 사용처 0) |
| (유지) | `DocumentRepository.countApprovedByTypeAndUser` | PerformanceService live 사용 |

- 제거 후 import 정리: `getDocumentsByInfra` 제거로 `java.util.stream.Collectors` 미사용 전락 가능 → 실측 후 제거. `DocumentDTO` 는 searchDocuments 가 사용 → 유지.

---

## 3. Part B — 테스트 추가 (DocumentServiceTest)

setUp 에 mock 3개 추가 주입: `documentSignatureRepository`, `sigunguCodeRepository`, `sysMstRepository`.

| ID | 테스트 | 검증 |
|----|--------|------|
| T-B1 | `searchDocuments_blankParamsNormalizedToNull` | 빈문자열 6필드 → repo 에 null 전달(ArgumentCaptor) + Page.map 결과 |
| T-B2 | `searchDocuments_enrichRegion_buildsTargetDisplay` | regionCode→sigunguCode(시도/시군구), sysType→sysMst(시스템명), targetDisplay "시도 시군구 - 시스템명" 조립 |
| T-B3 | `searchDocuments_enrichRegion_sysTypeFallbackWhenNoSysNm` | sysNm 없을 때 sysType 코드로 폴백 표기 |
| ~~T-B4~B7~~ | **삭제(amendment A)** — enrichInspectMonth/toMonthLabel 가 도달불가 dead 로 판명되어 테스트 대신 메서드 삭제. inspect 관련 테스트 미작성. | — |
| T-B8 | `saveSignature_setsFieldsAndSaves` | signerType/Name/Org/image 세팅 + documentSignatureRepository.save(ArgumentCaptor) |
| T-B9 | `getCityNames_delegates` | documentRepository.findDistinctCityNames 위임 |
| T-B10 | `getDocumentHistory_delegates` | documentHistoryRepository.findByDocument_DocIdOrderByCreatedAtDesc 위임 |
| T-B11 | `deleteDocument_callsDeleteById` | documentRepository.deleteById(verify) |

- 신규 7 테스트(T-B1/B2/B3/B8/B9/B10/B11). enrichRegion 은 public `searchDocuments` 경유 간접 검증.

---

## 4. 구현 순서 (T)

| ID | 단계 |
|----|------|
| T-1 | 가드: 3개 메서드명 + `findByInfra_InfraIdOrderByCreatedAtDesc` 재스캔으로 **production(main) 호출처 0건** 재확인(문서/테스트 언급은 제외 — 구현 후엔 테스트·문서에 이름이 남는 게 정상). 유지대상 `countApprovedByTypeAndUser` 는 PerformanceService 만 사용 재확인. |
| T-2 | Part A 삭제(DocumentService 3 + DocumentRepository 1). |
| T-3 | import 정리(Collectors 등 실측) → `./mvnw -q -DskipTests compile`. |
| T-4 | Part B: DocumentServiceTest 에 mock 3 주입 + T-B1~T-B11 추가. |
| T-5 | `./mvnw test -Dtest=DocumentServiceTest` green. |
| T-6 | `./mvnw test -Dtest=PerformanceServiceTest` green(countApprovedByTypeAndUser 유지 회귀 확인). |
| T-7 | `./mvnw verify`(전체 + JaCoCo 게이트) green. |
| T-8 | JaCoCo csv 로 DocumentService·전역 커버리지 상승 확인 → floor 상향 수치 확정(기획 NFR-4 정책: 실측−2~2.5pp 버퍼, 게인 작으면 유지). |

---

## 5. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | T-3 compile |
| NFR-2 | T-5 DocumentServiceTest green |
| NFR-3 | T-6/T-7 전체 + PerformanceServiceTest green |
| NFR-4 | T-7 게이트 + T-8 상승·floor 확정 |
| NFR-5 | git history |

---

## 6. 롤백

단일 커밋. 문제 시 `git revert`. 작업 중 실패 시 Edit 되돌리고 원인 분석.

---

## 7. 커밋 (작업완료 후)

- 메시지: `test(coverage): DocumentService 단위테스트 7 추가 + dead 제거(public 3·cascade repo 1·도달불가 enrichInspectMonth/toMonthLabel·필드 2) + JaCoCo floor 상향 (beyond-A)`
- floor 상향분 동봉(pom.xml) 시 메시지에 반영.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획서·개발계획서 ✅ 완료 갱신 동봉.

---

## 8. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

---

## 9. amendment A (구현 중, 사용자 승인 "A")

enrichInspectMonth/toMonthLabel 가 도달불가 dead 로 판명(기획서 §9 참조) → 테스트 대신 삭제. 원 T-B4~B7 미작성. 신규 테스트 7개(T-B1/B2/B3/B8/B9/B10/B11)로 진행.

**최종 결과**: DocumentService LINE 50%→**99.1%**(미커버 1), 전역 LINE 47.34%→**47.89%**·INSTR 39.66%→**40.10%**. DocumentServiceTest **20/20 green**(기존 13+신규 7). PerformanceServiceTest 회귀 0. JaCoCo floor LINE 0.42→**0.45**·INSTR 0.35→**0.38** 상향(게이트 통과). codex 구현검증 후 듀얼푸시.
