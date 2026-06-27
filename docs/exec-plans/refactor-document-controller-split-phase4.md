# [개발계획서] DocumentController 분리 — S4 Phase 4 (서명/첨부/날인본)

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/refactor-document-controller-split-phase4.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27)**. S-1~S-5 수행. DocumentController 661→529·DocumentFileController(7 엔드포인트, 전용 서비스 2 동반). 12 이관+성공경로 6 신규. ratchet 불변(서비스만 사용). clean verify green.

## 후속 백로그 (dual-review Opus 5 — 전부 원본 pre-existing, 순수이동서 미변경)
- uploadSignedScan 성공응답 Map.of NPE(getSignedScanOrigName null) → HashMap/기본값
- saveSignature 광범 catch→500(docId 누락/파싱오류 400 미구분)
- e.getMessage() 클라 노출(saveSignature/uploadAttachment/uploadSignedScan/deleteSignedScan) → 일반메시지+서버로그
- downloadSignedScan 모든 예외→404(서버오류도 404)
- getAttachments 무가드(원본부터) — 첨부 메타 읽기 권한 정책 결정

---

## 1. 작업 개요

서명/첨부/날인본 7 엔드포인트 + 전용 서비스 2개 필드를 신규 `DocumentFileController` 로 이동. 순수 이동 + codex 보완 성공경로 테스트 6건. `DocumentAccessSupport` 재사용. ratchet 무영향(서비스만 사용).

---

## 2. 구현 순서 (S-n)

### S-1 DocumentFileController 신설
- `controller/DocumentFileController.java` (@Controller @RequestMapping("/document"), @Slf4j, 필드주입).
- 주입: DocumentService, DocumentAttachmentService, DocumentSignedScanService, LogService, DocumentAccessSupport access.
- **이동**(본문 불변): saveSignature(POST /api/signature/save), uploadAttachment(POST /api/attachment/upload/{docId}), getAttachments(GET /api/attachments/{docId}), deleteAttachment(POST /api/attachment/delete/{attachId}), uploadSignedScan(POST /api/signed-scan/upload/{docId}), downloadSignedScan(GET /api/signed-scan/{docId} — **EDIT 가드 보존**), deleteSignedScan(POST /api/signed-scan/delete/{docId}).
- 본문 `getAuth()`/`getCurrentUser()` → `access.getAuth()`/`access.getCurrentUser()`.
- import: 기획서 §2-A 체크리스트(AttachmentRow·CustomUserDetails·MenuName·AccessActionType·MultipartFile·Resource·HttpHeaders·MediaType·URLEncoder·StandardCharsets·Map·List 등). FQCN 현행 유지 가능 — 컴파일로 확인.

### S-2 DocumentController 정리
- 위 7 엔드포인트 + 인라인 필드 attachmentService·signedScanService 제거.
- import 제거: DocumentAttachmentService·AttachmentRow(이동 후 미사용 — grep 확인). documentService/logService 잔존.

### S-3 테스트 이관 + 성공경로 신규
- `DocumentFileControllerTest.java` 신설: 기존 서명/첨부/signed-scan 테스트 이관(VIEW 403·admin·anonymous·404 등) + pdf() 헬퍼. 셋업=필드주입 reflection + login 헬퍼 + `new DocumentAccessSupport()`.
- **codex 성공경로 신규(6)**: saveSignature_edit_ok(documentService.saveSignature verify + success), uploadAttachment_edit_ok(attachmentService.saveAttachment stub→attachId/fileName/fileSize), uploadSignedScan_edit_ok(signedScanService.uploadOrReplace stub→success), **downloadSignedScan_edit_ok(loadForDownload+originalName stub → 200 + Content-Disposition(filename*) + APPLICATION_PDF 헤더 단언)**, deleteSignedScan_edit_ok(success), deleteSignedScan_badInput_400(signedScanService.delete throws IllegalArgumentException → 400).
- DocumentControllerTest: 해당 케이스 + 미사용 mock(attachmentService/signedScanService)·import·pdf() 제거(또는 pdf() 가 잔존 테스트 사용 시 유지 확인). 잔존 불변.

### S-4 검증
- `./mvnw -o clean verify` — compile + 전체 테스트 + @SpringBootTest 부팅(ambiguous mapping 검출) + JaCoCo 게이트 green.
- ratchet 불변 확인(서비스만 사용 → GOLDEN_RECORD 불필요 기대). DocumentController LOC 확인(약 530 목표).

### S-5 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-4 compile + clean verify |
| NFR-2 | S-3 이관 동일 단언 green |
| NFR-2c | S-3 성공경로 6 신규 green(다운로드 헤더 단언 포함) |
| NFR-2b | S-4 @SpringBootTest 부팅(매핑 비충돌) |
| NFR-3 | S-4 JaCoCo 게이트(코드 이동→비감소)·ratchet 불변 |
| NFR-4 | S-4 DocumentController LOC + 필드 2 감소 |
| NFR-5 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 7 매핑 잔존 → ambiguous | S-2 제거 후 clean verify(startup 실패) |
| 다운로드 EDIT 가드/헤더 누락 | 가드 동반 이동 + downloadSignedScan VIEW 403 + edit_ok 헤더 단언 |
| 전용 서비스 필드 오제거 | grep(attachmentService 4·signedScanService 5 전부 그룹) 확인 |
| pdf() 헬퍼 이관 누락/중복 | 잔존 테스트의 pdf() 사용 여부 grep 후 결정 |

롤백: 단일 커밋 `git revert`. 순수 구조 이동(+테스트 6건)이라 프로덕션 의미 변화 0.

---

## 5. 커밋

- `refactor(document): DocumentController 서명/첨부/날인본 분리 — S4 Phase 4`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
