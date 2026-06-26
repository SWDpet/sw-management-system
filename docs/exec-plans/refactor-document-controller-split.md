# [개발계획서] DocumentController 분리 — S4 Phase 1 (다운로드/내보내기)

- **작성팀**: 개발팀
- **작성일**: 2026-06-26
- **기획서**: `docs/product-specs/refactor-document-controller-split.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-26)**. S-1~S-6 수행. DocumentController 1373→1034(-339)·DocumentDownloadController 366. ⚠`DocumentAccessSupport` 는 ArchUnit R5(`..controller..`=*Controller)로 `security` 패키지에 배치(controller.support 아님). 다운로드 19테스트 이관·access 주입. clean verify green(매핑 충돌 없음). 후속 Phase: 서명/첨부/batch/plan 분리.

---

## 1. 작업 개요

DocumentController(1373) 의 다운로드/내보내기 6 엔드포인트 + 헬퍼를 신규 `DocumentDownloadController` 로 이동, 인증 헬퍼는 `DocumentAccessSupport` @Component 로 승격. **순수 이동 — 로직/시그니처/매핑/가드 불변.**

---

## 2. 구현 순서 (S-n)

### S-1 DocumentAccessSupport 신설
- `controller/support/DocumentAccessSupport.java` (@Component). 메서드 3: getCurrentUser()/isAdmin()/getAuth() — DocumentController 현재 본문 **바이트 동일** 복사(SecurityContextHolder; getAuth: admin→"EDIT", null→"NONE", else authDocument). import: CustomUserDetails, Authentication, SecurityContextHolder, SimpleGrantedAuthority.

### S-2 DocumentDownloadController 신설
- `controller/DocumentDownloadController.java` (@Controller @RequestMapping("/document"), @RequiredArgsConstructor 또는 @Autowired 필드 — DocumentController 스타일 따라 필드주입).
- 주입: DocumentService, PdfExportService, HwpxExportService, ExcelExportService, LogService, DocumentAccessSupport.
- **이동**(DocumentController 에서 잘라내기): downloadDocumentListExcel(/excel-list), downloadPdf(/api/pdf/{id}), downloadHwpx(/api/hwpx/{id}), downloadExcel(/api/excel/{id}), downloadZip(/api/zip/{id}), downloadBulkZip(/api/bulk-zip).
- **이동 private**: addZipEntry, bulkErr, bulkAddSingle, bulkAddAll, bulkPut, bulkUnique.
- **이동 static**: zipSafe, bulkTypeLabel.
- 본문 내 `getAuth()` → `access.getAuth()` 로 치환(엔드포인트 진입 가드 포함). 나머지 본문 불변.

### S-3 DocumentController 정리
- 위 6 엔드포인트 + 6 private + 2 static **제거**.
- export 서비스 필드: **hwpxExportService·excelExportService 만 제거**. ⚠**pdfExportService 는 잔존**(`previewDocument`/`renderDocumentToHtml` 에서 사용 — codex 지적). 잔존 여부 grep 확인 후 제거 결정.
- private getCurrentUser/isAdmin/getAuth → **wrapper 유지하며 DocumentAccessSupport 위임**(필드 `@Autowired DocumentAccessSupport access;` 추가, 본문을 `return access.xxx();` 로). 잔존 호출부(getAuth() 등) 무변경.
- LogService 필드는 잔존(다른 엔드포인트도 사용) — 제거 금지 확인.

### S-4 테스트 이관
- `DocumentDownloadControllerTest.java` 신설: DocumentControllerTest 의 download*/bulk*/excelList* 케이스 이관(동일 단언 — viewer-guard VIEW 403·EDIT/admin 200·throws_500 등). 셋업은 DocumentControllerTest 패턴(필드주입 reflection + SecurityContext login 헬퍼) 복제, **주입: documentService·pdf/hwpx/excelExportService·logService·`new DocumentAccessSupport()`**.
- DocumentControllerTest:
  - 이관된 download*/bulk*/excelList* 케이스 **제거**.
  - **mock 정리: hwpxExportService·excelExportService mock 제거. ⚠pdfExportService mock 은 유지**(`preview_rendersHtml` 잔존 — codex).
  - **setUp 에 `inject access = new DocumentAccessSupport()`** 추가 — 잔존 권한 테스트가 wrapper→access 위임 경로라 미주입 시 NPE(codex).
  - 잔존 케이스(preview·CRUD·signed-scan·조회·batch·plan) 불변.
- DocumentControllerBulkZipTest: `DocumentController.zipSafe/bulkTypeLabel` → `DocumentDownloadController.*` (static 참조만).

### S-5 검증
- `./mvnw -o clean verify` — compile + 전체 테스트 + **@SpringBootTest 컨텍스트 부팅(ambiguous mapping 자동검출)** + JaCoCo 게이트 green.
- LOC 확인: DocumentController 대폭 감소.

### S-6 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시. ⚠다운로드 6종 스모크(가능 시) 권장.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-5 compile + clean verify green |
| NFR-2 | S-4 이관 테스트 동일 단언 green / BulkZipTest 참조 갱신 |
| NFR-2b | S-5 DashboardAndAdminLogsSecurityIntegrationTest 부팅 성공(매핑 비충돌) |
| NFR-3 | S-5 JaCoCo 게이트 통과(코드 이동→커버리지 비감소) |
| NFR-4 | S-5 DocumentController LOC 감소 |
| NFR-5 | S-6 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 6 매핑 잔존 → ambiguous | S-3 제거 후 clean verify(startup 실패로 즉시 검출) |
| getAuth 본문 비동일 → 가드 회귀 | S-1 바이트 동일 복사 + 이관 테스트 VIEW 403 단언 |
| export 서비스 빈 주입 누락 | S-2 주입 + 컴파일 |
| static 헬퍼 테스트 미갱신 → 컴파일 실패 | S-4 BulkZipTest 참조 갱신 |

롤백: 단일 커밋 `git revert`. 순수 구조 이동이라 프로덕션 의미 변화 0.

---

## 5. 커밋

- `refactor(document): DocumentController 다운로드/내보내기 분리 — S4 Phase 1`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
