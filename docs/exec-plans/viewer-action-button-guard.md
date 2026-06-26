# [개발계획서] 조회자(VIEW) 액션 버튼 + 전 다운로드 차단

- **작성팀**: 개발팀
- **작성일**: 2026-06-26
- **기획서**: `docs/product-specs/viewer-action-button-guard.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-26)**. C1(UI 숨김, `43b92bc`) + C2(서버 가드 EDIT + 테스트 갱신). 두 커밋 모두 dual-review 반영·듀얼푸시. ⚠운영배포는 C2 포함 최신본으로(C1 단독 금지). 브라우저 QA(VIEW/EDIT)는 배포 전 권장.

---

## 1. 작업 개요 / 커밋 분할

규모가 커서 **2커밋**으로 분할(각각 verify+dual-review):
- **C1 (액션버튼 + UI 다운로드 숨김)**: 템플릿 th:if + 모델 보강(OpsDoc/Inspect canEdit·userAuth). 순수 UI.
- **C2 (서버 다운로드 가드 EDIT 강화 + 테스트 갱신)**: 7 컨트롤러 다운로드 엔드포인트 EDIT + 기존 테스트 기대 갱신.

> 분할 이유: 리뷰/롤백 단위 분리. **⚠ C1 은 UI 숨김만이라 URL 직접 접근은 여전히 가능 → C1 단독 운영배포 금지. 운영 반영은 C2 까지 완료 후 함께(보안 완성)**. (codex 보완) C1/C2 는 내부 리뷰·커밋 단위.

---

## 2. C1 — UI 숨김 (템플릿 + 모델 보강)

### 2-1. 모델 보강 (컨트롤러, UI용 권한값 전달)
| 컨트롤러 | 추가 |
|---|---|
| OpsDocController.detail | `@AuthenticationPrincipal CustomUserDetails` 파라미터 + `model.addAttribute("canEdit", requireDocEdit(cu)==null)` |
| InspectReportController.inspectDetail | `model.addAttribute("userAuth", getAuth())` (PDF 버튼 가드용) |

### 2-2. 템플릿 th:if (액션버튼)
| 파일 | 위치 | 조건 |
|---|---|---|
| main-dashboard.html | 사업등록 a(L117) | `th:if="${#authentication.principal.user.authProject == 'EDIT'}"` |
| ops-doc/doc-fault.html, doc-support.html | 수정/삭제(view 모드) | 기존 `mode=='view'` → `mode=='view' and canEdit` |
| person-form.html | page-footer(L197~203) | div 에 `th:if="${userAuth == 'EDIT'}"` (저장/취소/삭제 일괄) |
| infra-detail.html | 수정/삭제(L182~183) | 각 `th:if="${userAuth == 'EDIT'}"` (목록으로 유지) |
| infra-list.html | 행 삭제(L159) | `th:if="${userAuth == 'EDIT'}"` |

### 2-3. 템플릿 th:if (다운로드 UI)
| 파일 | 버튼 | 조건 |
|---|---|---|
| document-list.html | 엑셀 다운로드(L146)·일괄 산출물 dropdown(L148~)·날인본 다운로드(L348) | `userAuth=='EDIT'` |
| document-detail.html | (이미 L70 `userAuth=='EDIT'` 가드) | 변경 없음(확인만) |
| inspect-detail.html | PDF 다운로드(L112) | `userAuth=='EDIT'` |
| performance-report.html | 엑셀 다운로드(L112) | `userAuth=='EDIT'` (controller 가 userAuth 전달함) |
| ops-doc/doc-support.html | 지원파일 다운로드/삭제(L119,121)·업로드영역 | `canEdit` |
| ops-doc/inspect-agent.html | 다운로드(.zip)(L88) | `canEdit`(템플릿에 canEdit 전달; InspectAgentController.page 가 모델에 추가) |
| geonuris/geonuris-list.html | 전체/유형별/현재필터 CSV(L175,200~224,244~248)·개별 다운로드(L312) | `hasEditPermission` |
| geonuris/geonuris-detail.html | 개별 다운로드(L219) | `hasEditPermission` (모델 hasEditPermission 전달 확인; 없으면 추가) |
| license/registry-list.html | CSV 다운로드(L238) | `hasEditPermission` (개별 L297 은 이미 가드) |

---

## 3. C2 — 서버 다운로드 가드 EDIT 강화

**원칙(codex)**: 각 엔드포인트 **최상단에서 권한 확인 → 통과 못하면 403**(파일 조회/생성보다 먼저). 기존 모듈 가드 패턴 재사용.

| 컨트롤러 | 엔드포인트 | 현재 | 변경 |
|---|---|---|---|
| DocumentController | excel-list | NONE차단 | `!"EDIT".equals(getAuth())`→403 |
| DocumentController | api/pdf/{id}, api/hwpx/{id}, api/excel/{id}, api/zip/{id} | 무가드 | EDIT 가드 신설(메서드 진입부) |
| DocumentController | api/bulk-zip | NONE차단 | EDIT |
| DocumentController | api/signed-scan/{docId}(download) | 로그인만 | EDIT |
| InspectReportController | api/inspect-pdf/{reportId} | 무가드 | EDIT 신설(진입부) |
| PerformanceController | api/excel | NONE차단 | EDIT |
| OpsDocController | api/support-file/{docId}(download) | requireDocView | requireDocEditOrAdmin |
| InspectAgentController | inspect-agent/download | NONE차단 | EDIT(getAuth=="EDIT") |
| GeonurisLicenseController | download/csv, {id}/download | hasView | hasEditPermission |
| LicenseRegistryController | download/csv | hasView | hasEditPermission ({id} 은 이미 EDIT) |

---

## 4. 테스트 (C2 동반)

기존 다운로드 VIEW→200/무가드 단언을 **VIEW→403 / EDIT→200** 으로 갱신. **⚠ 권한이 파일조회/생성보다 먼저라, 기존 `*_throws_500`(예외경로) 테스트는 반드시 EDIT 로그인으로 세팅해야 500 경로가 유지됨(아니면 403). (codex)**

| 테스트(기존명) | 변경 |
|---|---|
| DocumentControllerTest: `downloadPdf_ok`·`downloadHwpx_letter_ok`·`downloadExcel_design_ok`·excelList(EDIT 이미)·bulkZip | EDIT 로그인 추가(현재 무로그인/무가드) → 200 유지. + 각 `..._viewForbidden`(VIEW→403) 신규 |
| DocumentControllerTest: `downloadPdf_throws_500`·`downloadHwpx_throws_500`·`downloadExcel_throws_500`·`downloadZip_throws_500` | **EDIT 로그인 세팅**으로 500 경로 유지(403 회피) |
| DocumentControllerTest: signed-scan | EDIT→200·VIEW→403·**authDocument=NONE 로그인→403** 신규 |
| InspectReportControllerTest: `downloadPdf_inspectMonthSuffix_ok`·`downloadPdf_noMonth_emptySuffix_ok` | EDIT 로그인 추가 → 200 유지. + VIEW/미로그인→403 신규 |
| InspectReportControllerTest: `downloadPdf_throws_500` | EDIT 세팅으로 500 유지 |
| PerformanceControllerTest: `excel_view_ok` | → `excel_view_forbidden`(403) + `excel_edit_ok`(200). `excel_serviceThrows_500` 은 EDIT 세팅 유지 |
| OpsDocControllerTest: `downloadSupportFile_ok`·`downloadSupportFile_admin_ok` | EDIT/admin→200 유지(이미 EDIT/admin), **VIEW→403** 추가. `downloadSupportFile_view_ok` 가 있으면 403 으로 수정 |
| InspectAgentControllerTest: `download_view_metaPresentButZipMissing_notFound` | VIEW→**403**(EDIT 가드 신설로). 별도 EDIT→404(zip 없음) 케이스로 분리 |
| Geonuris/Registry | 컨트롤러 테스트 부재 → 서버 가드 변경 + **최소 가드 회귀 테스트 신규 1~2건**(VIEW csv→403, EDIT→정상) 추가 권장 |

---

## 5. 구현 순서 (S-n)

| ID | 단계 |
|----|------|
| S-1 | C1: 2-1 모델 보강 → 2-2/2-3 템플릿 th:if. |
| S-2 | `./mvnw -o clean verify` green(템플릿 변경은 컴파일/렌더 영향 적음, 기존 테스트 green 유지). |
| S-3 | 브라우저 QA(VIEW/EDIT 2계정) — 버튼 숨김 육안. → C1 커밋+dual-review+듀얼푸시. |
| S-4 | C2: 3장 서버 가드 + 4장 테스트 갱신. |
| S-5 | `./mvnw -o clean verify` green(갱신 테스트 포함). |
| S-6 | C2 커밋+dual-review+듀얼푸시. |

---

## 6. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-2/S-5 compile·기동 |
| NFR-2 | S-5 테스트 green |
| NFR-3 | S-2/S-5 clean verify |
| NFR-4 | S-3 QA + C2 서버 403 |
| NFR-5 | 각 커밋 dual-review + QA |

---

## 7. 롤백

C1/C2 각 커밋 단위 `git revert`. C1(UI)·C2(서버) 독립 롤백 가능.

---

## 8. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
