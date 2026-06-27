# [개발계획서] batchGenerate·uploadSignedScan 응답 null-안전 하드닝

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/harden-document-batch-upload-nullsafe.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27)**. 구현+dual-review. safeErrorMessage 는 dual-review 합의로 사설 헬퍼 2개 대신 **`com.swmanager.system.util.ExceptionMessages.safe()` 공유 util** 로 추출(중복 제거). 실패 branch 도 HashMap. clean verify green, 커버리지 LINE 72.11%.

## 후속 백로그 (dual-review pre-existing/scope-out)
- error 메시지 클라 노출(ExceptionMessages.safe 가 message/클래스명 반환) — 정보노출 별건, 일반메시지+서버로그는 별도 보안 sprint
- projIds **원소** null/non-Number(ClassCastException) → 여전히 500(본 스프린트 명시 범위 외)
- saveSignature/uploadAttachment/deleteSignedScan catch 의 e.getMessage() → ExceptionMessages.safe 일괄 적용(error-message 정리 sweep)

---

## 1. 작업 개요

DocumentBatchController.batchGenerate(성공/실패 result Map.of null NPE·projIds null/empty·외부 catch) + DocumentFileController.uploadSignedScan(origName null) null-안전화. 응답 키·정상 경로 불변. 테스트 4 신규.

---

## 2. 구현 순서 (S-n)

### S-1 DocumentBatchController.batchGenerate
- **projIds 선검증**: docType 검증 직후 추가
  `if (projIds == null || projIds.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "대상 사업이 없습니다."));`
- **성공 result → HashMap**(키 동일, null 허용):
  ```java
  Map<String,Object> ok = new HashMap<>();
  ok.put("projId", projId); ok.put("success", true); ok.put("docId", doc.getDocId());
  ok.put("projNm", p.getProjNm()); ok.put("cityNm", p.getCityNm()); ok.put("distNm", p.getDistNm());
  results.add(ok);
  ```
- **개별 실패 result**: `Map.of("projId", projId, "success", false, "error", safeErrorMessage(e))` (projId·success 는 non-null 이라 Map.of 유지 가능, error 만 coalesce).
- **외부 catch**: `Map.of("error", e.getMessage())` → `Map.of("error", safeErrorMessage(e))`.
- **헬퍼**: `private static String safeErrorMessage(Exception e) { return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(); }`

### S-2 DocumentFileController.uploadSignedScan
- 성공 result origName null-coalesce:
  `String origName = doc.getSignedScanOrigName() != null ? doc.getSignedScanOrigName() : "";` 후 Map.of 에 origName 사용. (나머지 키 동일.)
- **uploadSignedScan 400/500 catch 의 `Map.of("error", e.getMessage())` 도 safeErrorMessage 적용**(codex — 손대는 메서드 전체 null-안전). DocumentFileController 에 동일 private static safeErrorMessage 헬퍼 추가.
  > 범위: 본 스프린트는 uploadSignedScan 한정. saveSignature/uploadAttachment/deleteSignedScan 의 catch e.getMessage() coalesce 는 후속(일관 error-message 정리 백로그).

### S-3 테스트
- DocumentBatchControllerTest:
  - batchGenerate_nullNameFields_success: COMPLETION + SwProject(projId만, projNm/cityNm/distNm null) + createDocument→doc(docId) → 200, successCount=1·failCount=0, results[0].success=true(NPE 없음).
  - batchGenerate_nullProjIds_badRequest: docType 유효 + projIds 키 없음(또는 null) → 400, verifyNoInteractions(documentService). + 별도 assertion/케이스: projIds=List.of()(empty) → 400.
  - batchGenerate_itemException_nullMessage_failsOne: SwProject 정상 + createDocument→doc, documentService.saveSection(...) **doThrow(new NullPointerException())**(message null) → 전체 200, failCount=1(배치 500 아님).
  - (기존 success/none/invalid/missingProject 테스트 불변.)
- DocumentFileControllerTest:
  - uploadSignedScan_nullOrigName_ok: loginEdit + uploadOrReplace→Document mock(getSignedScanOrigName null, getSignedScanSize 0L) → 200.

### S-4 검증
- `./mvnw -o clean verify` green. ratchet 불변·커버리지 비감소.

### S-5 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-4 compile + clean verify |
| NFR-2 | S-3 신규 4 green + 기존 batch/upload 테스트 불변 |
| NFR-3 | S-4 커버리지 비감소·ratchet 불변 |
| NFR-4 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| HashMap 키 변동 | 동일 키만 put(테스트로 success/docId 확인) |
| 빈배열 400 회귀 | 기존 테스트에 빈배열 200 기대 없음(grep 확인). 신규로 400 고정 |
| safeErrorMessage 누락 적용처 | 내부+외부 catch 모두 적용(grep 확인) |
| saveSection 예외 stub | ⚠saveSection 은 void 아님(DocumentDetail 반환, codex). Mockito `doThrow(new NullPointerException()).when(documentService).saveSection(...)` 는 반환 메서드에도 유효 |

롤백: 단일 커밋 `git revert`. 버그 수정이라 되돌리면 원래 결함.

---

## 5. 커밋

- `fix(document): batchGenerate·uploadSignedScan 응답 null-안전화 (harden-batch-upload-nullsafe)`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
