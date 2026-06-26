# [기획서] DocumentController 분리 — S4 거대클래스 (Phase 1: 다운로드/내보내기)

- **작성팀**: 기획팀
- **작성일**: 2026-06-26
- **트랙**: beyond-A 로드맵 [[S4 거대클래스 분리]]. 커버리지(S3) 마무리 후 최대 god-class 착수.
- **상태**: ✅ **Phase 1 완료(2026-06-26)**. DocumentController 1373→1034줄(-339). DocumentDownloadController(366)·DocumentAccessSupport(security 패키지) 신설. 동작 동일(순수 이동), 커버리지·게이트 유지. dual-review codex 0/Opus 7(합의 3=전부 pre-existing 보존, 분쟁 4 refute). 듀얼푸시.

---

## 1. 배경 / 목표

`DocumentController` 는 **1373줄**(저장소 최대 클래스)로 6개 책임이 한 파일에 혼재:
① 문서 CRUD/웹(list/detail/create/save/status/delete/preview/dist-list) ② **다운로드/내보내기(excel-list·api/pdf·hwpx·excel·zip·bulk-zip + zip/bulk 헬퍼)** ③ 서명/첨부/날인본 ④ 조회 API(user/project) ⑤ 착수계 일괄(batch) ⑥ 계획(plan).

**목표(Phase 1)**: 가장 크고 응집된 **②다운로드/내보내기**를 별도 컨트롤러로 추출 + 인증 헬퍼(getAuth/isAdmin/getCurrentUser) 중복을 단일 컴포넌트로 승격. **동작 100% 동일(순수 이동·리팩터)**, baseline(클래스 크기) 감소-only([[QUALITY_CHARTER]]).

비목표: ③~⑥ 추출은 후속 Phase. 로직 변경·신규 기능 0.

---

## 2. 변경 설계

### 2-A 신규: `DocumentAccessSupport` (@Component)
- 메서드: `getCurrentUser()`, `isAdmin()`, `getAuth()` — 현재 DocumentController private 3종과 **바이트 동일 로직**(SecurityContextHolder 기반, getAuth 는 admin→"EDIT" 매핑 유지 — [[project_swmanager_viewer_download_guard]] 정책 보존).
- 효과: 다운로드 컨트롤러와 DocumentController 가 **동일 인증 정책 단일 소스** 공유(현행 중복 제거 = 품질 개선).

### 2-B 신규: `DocumentDownloadController` (@Controller, @RequestMapping("/document"))
- 이동 엔드포인트(매핑·시그니처·본문 불변):
  - `GET /excel-list` (downloadDocumentListExcel)
  - `GET /api/pdf/{id}` (downloadPdf)
  - `GET /api/hwpx/{id}` (downloadHwpx)
  - `GET /api/excel/{id}` (downloadExcel)
  - `GET /api/zip/{id}` (downloadZip)
  - `GET /api/bulk-zip` (downloadBulkZip)
- 이동 private 헬퍼: addZipEntry, bulkErr, bulkAddSingle, bulkAddAll, bulkPut, bulkUnique.
- 이동 **static 헬퍼**: `zipSafe`, `bulkTypeLabel`(bulk-zip 전용) — **DocumentControllerBulkZipTest 의 `DocumentController.zipSafe/bulkTypeLabel` 참조를 `DocumentDownloadController.*` 로 갱신**(codex).
- 주입: documentService, pdfExportService, hwpxExportService, excelExportService, **logService(excel-list 의 logService.log — codex 누락지적 반영)**, DocumentAccessSupport.
- 가드: 각 메서드 진입부 `!"EDIT".equals(access.getAuth())` (현행 그대로 — viewer-download-guard C2).

> ⚠ **Phase 1 제외 명시(codex)**: `/api/signed-scan/{docId}` 다운로드는 다운로드 성격이나 업로드/삭제·첨부/서명 영역과 결합 → **DocumentController 잔존**(후속 Phase 에서 서명/첨부 묶음으로 분리).

### 2-C 수정: `DocumentController`
- 위 6 엔드포인트 + 6 헬퍼 + export 서비스 필드 3(pdf/hwpx/excel) 제거.
- private getCurrentUser/isAdmin/getAuth → **wrapper 유지 + DocumentAccessSupport 위임**(codex 권고: 메서드명 유지 → 잔존 엔드포인트 호출부 무변경, diff 최소·회귀추적 용이). 신규 DocumentDownloadController 는 `access.getAuth()` 직접 호출.
- 결과: 1373 → 약 970줄.

> ⚠ 두 컨트롤러가 같은 `@RequestMapping("/document")` 베이스 → Spring 은 **전체 경로 충돌 없으면 허용**(full path 상이). SecurityConfig 는 URL 경로 기준 매칭이라 컨트롤러 이동에 불변.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 이동된 6 엔드포인트의 URL·HTTP 메서드·요청/응답·권한 가드(EDIT)·파일명/헤더가 이전과 **완전 동일**. |
| FR-2 | DocumentAccessSupport.getAuth 의 admin→EDIT 매핑·NONE 폴백이 기존 DocumentController.getAuth 와 동일. |
| FR-3 | DocumentController 잔존 엔드포인트(CRUD·서명·첨부·조회·batch·plan)는 동작 불변. |
| FR-4 | 신규 기능·로직 변경·UI 변경 0(순수 구조 이동). |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | 기존 다운로드 테스트(DocumentControllerTest 의 download*/bulk*/excelList*)는 **DocumentDownloadControllerTest 로 이관**해 동일 단언 유지(viewer-guard VIEW 403·admin 200 포함). DocumentControllerBulkZipTest 의 static 참조 갱신. DocumentController 잔존 테스트 불변. |
| NFR-2b | **매핑 충돌 안전망**: `DashboardAndAdminLogsSecurityIntegrationTest`(@SpringBootTest, full context+security 부팅)가 ambiguous mapping 시 startup 실패 → clean verify 로 자동 검출(codex). |
| NFR-3 | 전역 커버리지 비감소(코드 이동이라 floor 유지). |
| NFR-4 | DocumentController LOC 대폭 감소(baseline 개선), 신규 클래스는 단일책임. |
| NFR-5 | 구현 후 dual-review → 듀얼푸시. ⚠다운로드 6종 + 잔존 주요 화면 **브라우저/엔드포인트 스모크 권장**(순수 이동이라 회귀 위험 낮으나 매핑 충돌·빈주입 확인). |

---

## 5. 의사결정

- **5-1 Phase 1 = 다운로드만** ✅: 가장 크고(≈400줄) 응집·외부 의존 단순(export 서비스 3 + documentService). 서명/첨부/batch/plan 은 도메인 결합이 커 후속 Phase 로 분리(리스크 격리).
- **5-2 인증 헬퍼 컴포넌트 승격** ✅: 단순 복붙 대신 단일 컴포넌트 → 중복 제거 + viewer-download-guard 정책 단일화(향후 표류 방지). [[project_swmanager_viewer_download_guard]] 의 getAuth admin→EDIT 패턴 보존.
- **5-3 동작 동일 = 테스트 이관으로 보증** ✅: 신규 로직 0, 기존 단언을 새 테스트 클래스로 그대로 옮겨 회귀 검출.
- **5-4 같은 base path 2 컨트롤러** ✅: Spring 허용(경로 비충돌). 대안(다른 base) 은 URL 변경 → 프론트/북마크 깨짐이라 기각.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller(신규) | `controller/DocumentDownloadController.java` | 신규 |
| Support(신규) | `controller/support/DocumentAccessSupport.java` | 신규 |
| Controller(수정) | `controller/DocumentController.java` | 엔드포인트/헬퍼 제거 + 인증 위임 |
| Test(신규/이관) | `controller/DocumentDownloadControllerTest.java` | 이관 |
| Test(수정) | `controller/DocumentControllerTest.java` | 이동분 제거 |

UI/DB/API 계약 변경 0 → 디자인·DB팀 skip(순수 백엔드 구조).

| 리스크 | 수준 | 완화 |
|---|---|---|
| 매핑 충돌(동 base path) | 중 | full path 상이 확인 + clean verify(스프링 기동 시 ambiguous mapping 즉시 실패) + 스모크 |
| 빈 주입 누락 | 중 | 컴파일 + verify. export 서비스 3 신규 컨트롤러로 이동 |
| 권한 가드 누락 이동 | 높음 | 각 엔드포인트 EDIT 가드 동반 이동 + 테스트 이관(VIEW 403 단언 유지) |
| 인증 위임 회귀 | 중 | DocumentAccessSupport 로직 바이트 동일 + getAuth 단위 테스트 |

---

## 7. 승인 요청

본 기획서(S4 Phase 1)에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 승인 시 개발계획 수립.
