# [기획서] InspectReportService 커버리지 상향 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-25
- **트랙**: beyond-A 커버리지 스프린트(coverage-core-services). 선행 `coverage-document-service`(`c396941`) 후속.
- **상태**: ✅ 완료 (2026-06-25). codex APPROVE-WITH-FIX(보완 반영) + 구현검증. 신규 4테스트(T-1/T-2 통합). InspectReportService LINE 56.1%→85.8%, 전역 47.89%→48.46%. 프로덕션 변경 0.

---

## 1. 배경 / 목표

`InspectReportService`(299줄)는 현재 LINE **56.1%**(미커버 약 65줄). dead code 없음(전 메서드 컨트롤러/서비스에서 사용). 미커버 핵심은 **`save()` 의 update(수정) 경로**(L61~104, QR 적재 데이터 보호 로직) + visits sortOrder 배정(L109~119) + checkResults skip(L121~131) + COMPLETED→opsDocLink(L134~136) 분기다.

기존 `InspectReportServiceTest`(9테스트)는 save 의 **신규(draft) 경로만** 커버.

**목표**: save 의 미커버 분기에 mock 단위테스트 추가 → 커버리지 상향, JaCoCo floor 유지/상향 검토. **프로덕션 코드 변경 0**(테스트 추가만).

---

## 2. 미커버 분기 (save 메서드)

| 분기 | 라인 | 현재 |
|---|---|---|
| update: 기존 createdBy/createdAt 보존 + visitLog 삭제 | 61~68 | ✗ |
| QR 섹션 보호 없음 → `deleteByReportId`(전체 삭제) | 91~92 | ✗ |
| QR 섹션 보호 있음 → 비보호만 `deleteByReportIdAndSectionIn` | 93~103 | ✗ |
| visits 저장 + sortOrder 자동 배정 | 109~119 | ✗ |
| checkResults 저장 + 보호 섹션 skip | 121~131 | ✗ |
| COMPLETED → opsDocLinkService.linkInspectReport | 134~136 | ✗ |

> QR 보호 로직: 기존 checkResult 중 resultCode 있는 섹션(QR 적재)이 incoming 에 resultCode 없이 오면 그 섹션은 **삭제·덮어쓰기 방지**(보존). 핵심 데이터 보호 로직이라 회귀 위험 높아 테스트 가치 큼.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | `InspectReportServiceTest` 에 save update/COMPLETED/visits 분기 mock 단위테스트 추가. 운영DB 무관. |
| FR-2 | QR 섹션 보호: 보호 발생/미발생 두 경로 모두 단언(deleteByReportId vs deleteByReportIdAndSectionIn, incoming skip). |
| FR-3 | 프로덕션 코드(`InspectReportService` 등) **무변경**. 테스트 파일만 수정. |
| FR-4 | `resetAllInspectData`(파괴적) 는 커버 대상 제외(기존 정책 일관). |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `InspectReportServiceTest` 전건 green(기존 9 + 신규). |
| NFR-2 | 전체 `./mvnw test` green. |
| NFR-3 | JaCoCo 게이트 통과 + InspectReportService·전역 커버리지 상승. floor 상향은 구현 후 실측으로 판단(정책: 실측−2~2.5pp 버퍼). |

---

## 5. 의사결정

- **5-1 private 헬퍼 없음**: save 는 단일 public 메서드 내 분기라 public `save` 직접 호출로 전 분기 커버 가능.
- **5-2 mock 범위**: 기존 setUp 의 9개 mock(reportRepository/visitLogRepository/checkResultRepository/opsDocLinkService 등) 재사용. 추가 mock 불요(생성자 주입 전부 이미 mock).
- **5-3 currentUser 폴백**: SecurityContext 없으면 "system" — 기존 테스트와 동일 전제.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(수정) | `InspectReportServiceTest.java` | 신규 테스트 추가 |
| Docs | 본 기획서 + 개발계획서 | 신규 |

프로덕션/DB/API/UI 변경 0. UI 키워드 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| QR 보호 로직 mock 시나리오 오구성 | 낮음 | 기존/incoming 섹션·resultCode 조합 명시 + ArgumentCaptor 단언 |
| 최종 return findById 경유 NPE | 낮음 | reportRepository.findById/visitLog/checkResult mock 완비 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
