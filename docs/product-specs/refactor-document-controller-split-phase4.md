# [기획서] DocumentController 분리 — S4 Phase 4 (서명/첨부/날인본)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 로드맵 S4 거대클래스 분리. Phase 1(다운로드)·2(batch)·3(plan) 후속.
- **상태**: ✅ **완료(2026-06-27)**. DocumentFileController 신설, DocumentController 661→529(원본 1373 대비 -844, ~61%↓), 전용 서비스 2 필드 제거. ratchet 불변(서비스만). 커버리지 LINE 71.93% 상승. dual-review codex 0/Opus 5(전부 pre-existing 보존). 듀얼푸시.

---

## 1. 배경 / 목표

Phase 1~3 으로 DocumentController 1373→661. 잔존 그룹 중 **전자서명/첨부파일/날인본 스캔(signature/attachment/signed-scan)** 이 최대 응집 그룹(≈138줄, 7 엔드포인트)이며 **전용 서비스 2개(DocumentAttachmentService·DocumentSignedScanService)** 를 동반해 추출 시 필드까지 제거된다.

**목표**: 7 엔드포인트를 신규 `DocumentFileController` 로 이동, 전용 서비스 2개 필드 제거. **동작 100% 동일(순수 이동)**, baseline 감소-only. 인증 `DocumentAccessSupport` 재사용.

비목표: 조회 API(user/project)·core CRUD 는 후속/잔존.

---

## 2. 변경 설계

### 2-A 신규 `DocumentFileController` (@Controller, @RequestMapping("/document"))
- 이동 엔드포인트(매핑·시그니처·본문 불변):
  - `POST /api/signature/save` (saveSignature)
  - `POST /api/attachment/upload/{docId}` (uploadAttachment)
  - `GET /api/attachments/{docId}` (getAttachments)
  - `POST /api/attachment/delete/{attachId}` (deleteAttachment)
  - `POST /api/signed-scan/upload/{docId}` (uploadSignedScan)
  - `GET /api/signed-scan/{docId}` (downloadSignedScan) — EDIT 가드(viewer-action-button-guard) 보존
  - `POST /api/signed-scan/delete/{docId}` (deleteSignedScan)
- 주입: DocumentService, DocumentAttachmentService, DocumentSignedScanService, LogService, DocumentAccessSupport.
- 본문 `getAuth()`/`getCurrentUser()` → `access.*`.
- import 체크리스트(codex): AttachmentRow·CustomUserDetails·MenuName·AccessActionType·DocumentAccessSupport·DocumentService·DocumentAttachmentService·DocumentSignedScanService·LogService·MultipartFile·Resource·ResponseEntity·HttpHeaders·MediaType·URLEncoder·StandardCharsets·Map·List·@Slf4j. (현행 FQCN 일부 유지 가능 — 컴파일 확인.)

### 2-B DocumentController 정리
- 위 7 엔드포인트 + 인라인 필드 2개(attachmentService·signedScanService) 제거.
- documentService·logService 잔존(core 사용). import 정리: DocumentAttachmentService·AttachmentRow(이동으로 미사용 — grep 확인).
- 결과: DocumentController 661 → 약 530줄.

> ⚠**ratchet 무영향**: 이 그룹은 Repository 직접접근 없이 서비스만 사용 → ControllerRepositoryRatchet 카운트 불변(GOLDEN_RECORD 불필요).
>
> `/document` 공유 컨트롤러 추가(Controller/Download/Batch/Plan/Lookup/Participant + File). full path 비충돌(signature/attachment/signed-scan 경로는 현재 DocumentController 단독 소유 → 이동 후 원본 제거만 정확하면 안전). @SpringBootTest 부팅이 ambiguous mapping 자동검출.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 7 엔드포인트의 URL·메서드·요청/응답·권한 가드(전부 EDIT, downloadSignedScan 포함)가 이전과 완전 동일. |
| FR-2 | saveSignature(documentService.saveSignature)·attachment(save/get/delete)·signed-scan(uploadOrReplace/loadForDownload/delete) 동작·예외→상태코드·로그 불변. |
| FR-3 | DocumentController 잔존(core·preview·조회 API)·기타 분리 컨트롤러 동작 불변. |
| FR-4 | 신규 기능·로직 변경·UI 변경 0(순수 이동). |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | 기존 DocumentControllerTest 의 saveSignature*/uploadAttachment*/getAttachments*/deleteAttachment*/uploadSignedScan*/downloadSignedScan*/deleteSignedScan* 테스트 + pdf() 헬퍼 → DocumentFileControllerTest 로 이관, 동일 단언(VIEW 403·admin·404 등). 잔존 테스트 불변. |
| NFR-2c | **codex 성공경로 보강(신규)**: saveSignature_edit_ok·uploadAttachment_edit_ok·uploadSignedScan_edit_ok·**downloadSignedScan_edit_ok(200 + Content-Disposition + application/pdf 헤더 단언)**·deleteSignedScan_edit_ok·deleteSignedScan_badInput_400. 다운로드 성공 응답 헤더/미디어타입은 분리 중 깨지기 쉬워 별도 단언. |
| NFR-2b | @SpringBootTest 부팅 성공(매핑 비충돌). |
| NFR-3 | 전역 커버리지 비감소(코드 이동). floor 유지. ratchet 불변. |
| NFR-4 | DocumentController LOC + 필드 2개 감소(baseline 개선). |
| NFR-5 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 Phase 4 = 서명/첨부/날인본** ✅: 잔존 최대 응집 그룹 + 전용 서비스 2개 동반 제거. 7 엔드포인트가 한 도메인(문서 산출물 파일).
- **5-2 documentService/logService 잔존** ✅: saveSignature 가 documentService(1), 로그가 logService(4) 사용하나 둘 다 core 공유 → 양 컨트롤러 주입(제거 아님).
- **5-3 downloadSignedScan EDIT 가드 보존** ✅: viewer-action-button-guard C2 의 EDIT 가드를 그대로 이동(조회자 403 회귀 방지).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller(신규) | `controller/DocumentFileController.java` | 신규 |
| Controller(수정) | `controller/DocumentController.java` | 7 엔드포인트+필드2 제거 |
| Test(신규/이관) | `controller/DocumentFileControllerTest.java` | 이관 |
| Test(수정) | `controller/DocumentControllerTest.java` | 해당 케이스/미사용 mock·import 제거 |

UI/DB/API 계약 변경 0 → 디자인·DB팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 7 매핑 잔존 → ambiguous | 중 | 제거 후 clean verify(startup 실패로 즉시 검출) |
| 다운로드 EDIT 가드 누락 이동 | 높음 | 가드 동반 이동 + downloadSignedScan VIEW 403 테스트 이관 |
| 전용 서비스 필드 오제거(잔존부 사용) | 낮음 | grep(attachmentService 4·signedScanService 5 전부 그룹 내) 확인 |
| 미사용/누락 import | 낮음 | 컴파일 + grep |

---

## 7. 승인 요청

본 기획서(S4 Phase 4)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
