# [기획서] 조회자(VIEW) 액션 버튼 + 전(全) 다운로드 차단 (권한 UI/가드 버그 수정)

- **작성팀**: 기획팀 (+ 디자인팀 가상자문)
- **작성일**: 2026-06-26
- **유형**: 권한 버그 수정 (긴급). 조회 전용 사용자(VIEW)에게 등록/수정/삭제 버튼 + **모든 다운로드**가 노출·실행되는 회귀.
- **상태**: ✅ **완료(2026-06-26)**. C1(UI)+C2(서버가드) 구현·dual-review·듀얼푸시. 다운로드=EDIT 정책 전면 적용(UI 숨김 + 서버 403). admin 은 getAuth()→EDIT 매핑으로 허용(테스트 증명). 사용자 지시: "조회자는 모든 다운로드를 막아줘".

---

## 1. 배경 / 목표

조회 권한(VIEW)만 가진 사용자에게 (A) 편집/삭제/등록 버튼, (B) **모든 다운로드 버튼**이 노출된다. 일부 다운로드는 서버 가드도 VIEW 를 허용(또는 무가드)하여 URL 직접 접근 가능.

**목표**: 조회자에게 (A) 액션 버튼 숨김 + (B) **모든 다운로드를 UI 숨김 + 서버 가드 EDIT 강화**로 완전 차단. 기준: 각 모듈의 EDIT 권한(또는 ADMIN). `document-detail` 의 다운로드가 이미 `userAuth=='EDIT'` 로 가드된 패턴을 전 모듈로 통일.

---

## 2-A. 액션 버튼 (조회자 숨김)

| # | 화면/파일 | 버튼 | 노출 조건 |
|---|---|---|---|
| A1 | 대시보드 `main-dashboard.html` | 사업 등록 | `#authentication.principal.user.authProject=='EDIT'` (웹 버튼 기준) |
| A2 | 운영문서 상세 `ops-doc/doc-fault.html`·`doc-support.html` | 수정/삭제(+지원파일 편집) | `mode=='view' and canEdit` (canEdit=ADMIN∥authDocument EDIT) |
| A3 | 담당자 상세 `person-form.html` | 저장/취소/삭제(page-footer) | `userAuth=='EDIT'` |
| A4 | 서버관리 `infra-detail.html`·`infra-list.html` | 수정/삭제 | `userAuth=='EDIT'` |

## 2-B. 다운로드 전면 차단 (조회자 = EDIT 아니면 UI숨김 + 서버 403)

| # | 모듈 | 다운로드 버튼(UI) | 서버 엔드포인트 | 현재 서버 가드 | 변경 후 |
|---|---|---|---|---|---|
| B1 | 사업문서 목록 `document-list.html` | 엑셀 다운로드·일괄(ZIP)·날인본 다운로드 | `/document/excel-list`·`/api/bulk-zip`·`/api/signed-scan/{id}` | excel-list/bulk-zip=NONE 차단(VIEW허용), **signed-scan=로그인만(authDocument NONE 도 통과)** | UI `userAuth=='EDIT'` + 서버 EDIT (signed-scan 도 EDIT) |
| B2 | 사업문서 상세 `document-detail.html` | HWP/Excel/ZIP | `/document/api/{hwpx,excel,zip}/{id}` | **무가드(권한 체크 없음)** | **UI 이미 EDIT 가드(유지)** + 서버 EDIT 신설 |
| B3 | 점검내역서 `inspect-detail.html`·`inspect-preview.html` | PDF 다운로드 | `/document/api/inspect-pdf/{id}` | **무가드** | UI `userAuth=='EDIT'` + 서버 EDIT |
| B4 | 성과 리포트 `performance-report.html` | 엑셀 다운로드 | `/performance/api/excel` | NONE 차단(VIEW 허용) | UI `userAuth=='EDIT'` + 서버 EDIT |
| B5 | 운영문서 지원파일 `ops-doc/doc-support.html` | 지원 문서 다운로드 | `/ops-doc/api/support-file/{id}` | requireDocView(VIEW 허용) | UI canEdit + 서버 requireDocEditOrAdmin |
| B6 | 점검 수집모듈 `ops-doc/inspect-agent.html` | 다운로드(.zip) | `/ops-doc/inspect-agent/download` | NONE 차단(VIEW 허용) | UI canEdit + 서버 EDIT |
| B7 | license 영역 `geonuris-list/detail`·`license/registry-list.html` | geonuris CSV(전체/유형별/현재필터)·geonuris 개별 다운로드·registry CSV | geonuris `/download/csv`·`/{id}/download`=hasView / registry `/download/csv`=hasView / **registry `/download/{id}`·UI개별=이미 hasEdit(유지)** | UI: geonuris CSV·개별 + registry CSV 를 `hasEditPermission`. 서버: geonuris csv·{id}/download + registry csv 를 hasView→hasEdit |

> 모듈별 EDIT 기준: 사업문서/점검/지원파일/수집모듈=authDocument, 성과=authPerformance, license=authLicense. 각 ADMIN 우회 기존 규칙 유지.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-A | 2-A 액션 버튼 4종을 각 EDIT 조건으로 UI 숨김. |
| FR-B-UI | 2-B 모든 다운로드 버튼을 해당 모듈 EDIT 조건으로 UI 숨김(이미 가드된 document-detail 유지). |
| FR-B-SVR | 2-B 모든 다운로드 **서버 엔드포인트를 EDIT(또는 hasEditPermission) 로 강화** — VIEW URL 직접 접근 403. inspect-pdf·document hwpx/excel/zip 무가드 → EDIT 신설. signed-scan(로그인만) → EDIT. **권한 확인을 파일존재/생성보다 먼저 배치(VIEW 가 404/500 아닌 403 보장, codex).** |
| FR-C | OpsDocController.detail 이 canEdit 모델 전달(@AuthenticationPrincipal 추가). doc-support.html 의 다운로드/업로드/삭제도 canEdit 기준 정리. |
| FR-D | 기존 컨트롤러 테스트 중 다운로드 VIEW→200 기대를 **VIEW→403 / EDIT→200** 으로 갱신(DocumentControllerTest·InspectReportControllerTest·PerformanceControllerTest·OpsDocControllerTest·InspectAgent·Geonuris/Registry 영향분). signed-scan 은 **authDocument=NONE 로그인 사용자 403** 케이스도 추가(codex). |
| FR-E | EDIT/ADMIN 사용자는 전부 기존대로 노출·다운로드(회귀 없음). |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + 앱 기동 정상. |
| NFR-2 | 영향 컨트롤러 테스트 전건 green(다운로드 권한 기대 갱신 포함). |
| NFR-3 | 전체 `./mvnw -o clean verify`(+ JaCoCo 게이트) green. |
| NFR-4 | VIEW 계정: 전 화면 다운로드/액션 버튼 미노출 + 엔드포인트 직접접근 403. EDIT/ADMIN: 정상. |
| NFR-5 | 브라우저 QA(디자인팀 자문) VIEW/EDIT 2계정 육안. 스타일/토큰/다크모드 무변경(숨김만). |

---

## 5. 디자인팀 가상자문 (A+D)

- 토큰/CSS/레이아웃 변경 0 — `th:if` 노출 조건만. 대비비/다크모드 영향 없음.
- 버튼 숨김으로 비는 영역: 액션바/푸터는 '목록' 등 네비는 유지, 편집성만 숨김.

---

## 6. 의사결정

- **6-1 다운로드 = EDIT 정책 통일** ✅(사용자 지시): 조회자는 어떤 다운로드도 불가. document-detail 의 기존 EDIT 가드를 전 모듈 표준으로. UI 숨김+서버 가드 동시(URL 우회 차단).
- **6-2 서버 가드까지 강화** ✅: UI만 숨기면 VIEW 가 URL 직접 호출 가능 → 모든 다운로드 엔드포인트 EDIT. inspect-pdf 는 현재 무가드라 신설.
- **6-3 테스트 갱신 동반** ✅: 다운로드 VIEW 허용을 단언하던 기존 테스트를 새 정책(VIEW 403/EDIT 200)으로 수정. 회귀 게이트 유지.
- **6-4 person 취소도 숨김** ✅(사용자 명시).

---

## 7. 영향 범위 / 리스크

| 계층 | 파일 |
|------|------|
| Template(수정) | main-dashboard·person-form·infra-detail·infra-list·document-list·inspect-detail·inspect-preview·performance-report·ops-doc/{doc-fault,doc-support,inspect-agent}·geonuris/{geonuris-list,geonuris-detail}·license/registry-list |
| Controller(수정·서버 가드) | DocumentController(excel-list/pdf/hwpx/excel/zip/bulk-zip/signed-scan)·InspectReportController(inspect-pdf 신설)·PerformanceController(excel)·OpsDocController(detail canEdit + support-file download EDIT)·InspectAgentController(download)·GeonurisLicenseController(csv/id download)·LicenseRegistryController(csv/id download) |
| Test(수정) | 위 컨트롤러 테스트의 다운로드 권한 기대 갱신 |

| 리스크 | 수준 | 완화 |
|---|---|---|
| 과차단(정당한 VIEW 다운로드까지 막음) | 중 | 사용자 명시 정책. EDIT/ADMIN 회귀 0 확인 + QA |
| 서버 가드 광범위 변경 → 회귀 | 중 | 모듈별 기존 가드 패턴 그대로 EDIT 로 상향(신규 로직 최소), 테스트로 고정 |
| inspect-pdf 무가드였음(정보노출) | — | 본 작업으로 EDIT 신설(보안 개선) |

---

## 8. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다. (범위가 큼: 액션버튼 4 + 다운로드 7모듈 UI/서버 + 테스트 갱신)
