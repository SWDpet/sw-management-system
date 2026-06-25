# [기획서] DocumentService 커버리지 상향 + dead 정리 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-25
- **트랙**: beyond-A 커버리지 스프린트(coverage-core-services). 선행 dead 정리 2건(`91b24d9`, `dae6a05`) 후속.
- **상태**: ✅ 완료 (2026-06-25). codex 기획 APPROVE-WITH-FIX(보완 반영) + 구현검증 PASS. amendment A(enrichInspectMonth 도달불가 dead → 삭제) 반영. DocumentService LINE 50%→99.1%, 전역 47.34%→47.89%.

---

## 1. 배경 / 목표

`DocumentService`(305줄)는 현재 LINE **50%**(미커버 65줄). export 서비스와 달리 **대부분 살아있는 코드**이며 미커버는 (a) 호출처 0인 public 메서드 3개(소량) + (b) 테스트 안 된 실로직(다수)으로 구성.

**목표**: dead 3개 제거(무손실) + 실로직 mock 단위테스트 추가 → 커버리지 대폭 상향, JaCoCo floor 상향.

기존 `DocumentServiceTest`(13테스트, ReflectionTestUtils 필드주입)에 증분.

---

## 2. Part A — dead code 제거 (재검증 완료)

3개 public 메서드 모두 `src/`(main+controller+template+test) 참조 0건.

| 메서드 | 라인 | cascade |
|---|---|---|
| `getDocumentDTOById(Integer)` | 46–48 | 없음(내부 enrichRegion/getDocumentById 는 타처 사용→유지) |
| `getDocumentsByInfra(Long)` | 189–192 | `DocumentRepository.findByInfra_InfraIdOrderByCreatedAtDesc`(타 사용처 0)도 함께 제거 |
| `countApprovedDocuments(...)` | 302–304 | ⚠ 내부 `DocumentRepository.countApprovedByTypeAndUser` 는 **PerformanceService 가 직접 사용 → 유지**. 래퍼만 제거 |

- 제거 후 미사용 전락 가능 import(`java.util.stream.Collectors` 등) 실측 후 정리. `DocumentDTO` 는 `searchDocuments` 가 계속 사용 → 유지.

---

## 3. Part B — 테스트 추가 대상 (실로직)

| 대상 | 커버 내용 |
|---|---|
| `searchDocuments` | 빈문자열→null 정규화(6필드) + repo 위임 + Page.map 변환 |
| `enrichRegion`(private, searchDocuments 경유) | regionCode→sigunguCode 룩업(시도/시군구), sysType→sysMst 룩업(시스템명), targetDisplay "시도 시군구 - 시스템명" 조립, null/부분 분기 |
| ~~`enrichInspectMonth`/`toMonthLabel`~~ | **삭제됨(테스트 대상 아님) — §9 amendment A 참조**: DocumentType 에 INSPECT 가 없어(OpsDocType 이관) 도달불가 dead 로 판명 → 테스트 대신 삭제. |
| `saveSignature` | 서명 필드 세팅 + 저장(ArgumentCaptor 단언) |
| `getCityNames` / `getDocumentHistory` / `deleteDocument` | repo 위임(deleteById 호출 verify) |

- 테스트에 mock 추가 주입(현재 미주입): `sigunguCodeRepository`·`sysMstRepository`(enrichRegion 용), `documentSignatureRepository`(saveSignature 용).

---

## 4. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | Part A: §2 의 DocumentService 메서드 3개 삭제. `getDocumentsByInfra` cascade 로 `DocumentRepository.findByInfra_InfraIdOrderByCreatedAtDesc` 도 삭제. `countApprovedByTypeAndUser` 는 유지. |
| FR-2 | 삭제 직전 3개 메서드명 + cascade repo 메서드명 전역 재스캔으로 참조 0(또는 유지대상만) 재확인. |
| FR-3 | 삭제로 미사용 전락한 import 만 정리(실측). |
| FR-4 | Part B: §3 대상에 mock 단위테스트 추가. 모두 운영DB 무관. |
| FR-5 | live 동작·시그니처 불변(삭제 대상 외). |

---

## 5. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile 성공. |
| NFR-2 | `DocumentServiceTest` 전건 green(기존 13 + 신규). |
| NFR-3 | 전체 `./mvnw test` green(특히 `PerformanceServiceTest` 회귀 0 — countApprovedByTypeAndUser 유지 확인). |
| NFR-4 | JaCoCo 게이트 통과 + DocumentService·전역 커버리지 상승. floor 정책: 실측 상승분에서 약 2~2.5pp 버퍼 남기고 1pp 단위 상향(게인 작으면 floor 유지, 버퍼 흡수) — 구체 수치는 구현 후 실측으로 개발계획서에 확정. |
| NFR-5 | dead 삭제 비가역 — git history. |

---

## 6. 의사결정

- **6-1 countApprovedByTypeAndUser 유지** ✅: DocumentService 래퍼는 dead 지만 repo 메서드는 PerformanceService live 사용 → repo 메서드 보존, 래퍼만 제거.
- **6-2 jdbcTemplate SQL 폴백** ⛔ **무효화(§9 amendment A)**: enrichInspectMonth 자체가 도달불가 dead 로 삭제됨 → SQL 폴백 논의 무의미.
- **6-3 private 메서드 테스트** ✅(범위 축소): enrichRegion 은 public `searchDocuments` 경유로 간접 검증(리플렉션 직접호출 불요). ~~enrichInspectMonth/toMonthLabel~~ 은 §9 로 삭제.

---

## 7. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Service(수정) | `DocumentService.java` | 메서드 3 + import 제거 |
| Repository(수정) | `DocumentRepository.java` | 메서드 1 제거(cascade) |
| Test(수정) | `DocumentServiceTest.java` | 신규 테스트 추가 + mock 2개 주입 |

DB/API/UI/템플릿 변경 0. UI 키워드 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| countApprovedByTypeAndUser 오삭제 → PerformanceService 깨짐 | 중간 | FR-1 유지 명시 + NFR-3 PerformanceServiceTest green |
| cascade repo 오판 | 낮음 | FR-2 전역 재스캔 |
| enrich* 분기 mock 누락 | 낮음 | searchDocuments 경유 + ArgumentCaptor |

---

## 8. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

---

## 9. 구현 중 범위 수정 (amendment A — 2026-06-25, 사용자 승인)

**발견**: 구현 중 `enrichInspectMonth` 가 **도달불가 dead code** 임이 판명.
- `DocumentType` enum 은 `COMMENCE/INTERIM/COMPLETION` 3개뿐(`DocumentType.java:22` 주석대로 INSPECT/FAULT/SUPPORT/INSTALL/PATCH 는 **OpsDocType + tb_ops_doc 로 이관**, `ALIASES = emptyMap()`).
- `enrichInspectMonth` 는 `"INSPECT".equals(dto.getDocType().name())` 일 때만 동작 → DocumentDTO.docType 이 절대 INSPECT 일 수 없어 **본문 영구 unreachable**. `toMonthLabel`·필드 `inspectReportRepository`/`jdbcTemplate` 도 이 메서드 전용.
- `DocumentDTO.setInspectMonth` 호출처는 enrichInspectMonth 단 하나 → 제거해도 **inspectMonth 를 채우는 경로가 원래 없었음**(행위 변화 0). `document-list.html` 의 INSPECT 월 배지(`d.docType?.name()=='INSPECT'`)도 이미 항상 미표시인 orphaned 템플릿 로직(별건).

**조치(원 계획 "테스트"→"삭제"로 변경)**: enrichInspectMonth + toMonthLabel + inspectReportRepository/jdbcTemplate 필드 삭제, searchDocuments 는 `enrichRegion` 만 호출. inspect 관련 테스트(원 T-B4~B7)는 작성하지 않음. 나머지 Part A(dead public 3 + cascade) / Part B(나머지 테스트)는 계획대로.

> 참고(별건): `document-list.html` 의 INSPECT 월 배지 블록은 이미 도달불가 → 후속 템플릿 정리 후보.
