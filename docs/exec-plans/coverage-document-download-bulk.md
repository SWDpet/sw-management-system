# 개발계획서 — DocumentDownloadController 일괄ZIP 분기 커버리지 보강 (B)

- **상태**: ✅ 구현 완료(2026-06-28). +7테스트 green, DocumentDownloadController 90.9→100% LINE. 구현 codex PASS + dual-review 합의6 반영. `mvnw -o clean verify` 1453 green.
- **작성일**: 2026-06-28
- **기획서**: `docs/product-specs/coverage-document-download-bulk.md` (codex APPROVE-WITH-FIX)
- **안전망**: 현 green 스위트 + JaCoCo ratchet/PIT 게이트. **production 변경 0 (test-only)**.
- **원칙**: 기존 `DocumentDownloadControllerTest`(33) 확장. helper(`dto`/`zipEntryNames`/`stubBulkExports`/`loginEdit`) 재사용. 커밋 1개.

---

## Step 0 — baseline 고정(완료)

- 전역 LINE 78.57%·INSTR 64.13%(floor 0.75/0.61). 대상 DocumentDownloadController LINE 90.9%(miss 18).
- **확정 패턴**: `downloadBulkZip(docType,cityNm,distNm,keyword,authorName,type)`, `loginEdit()`, `when(documentService.searchDocuments(any×10)).thenReturn(new PageImpl<>(List.of(dto(...)), PageRequest.of(0,201), n))`, `zipEntryNames(res.getBody())`. mismatch 는 export 미호출(else fails.add). 단일 비매칭 문서 → ZIP 에 `_실패목록.txt`만.

**검증:** 없음(준비).

## Step 1 — C1~C4: bulkAddSingle 유형불일치 (fails.add)

각 1 테스트: loginEdit + searchDocuments→단일 비매칭 문서 → status 200 + `zipEntryNames` 에 `_실패목록.txt` 포함 + 해당 산출물 엔트리 부재.
- C1 `bulkZip_interim_mismatchWritesFailList`: type="interim", 문서 COMMENCE → 기성내역서 없음.
- C2 `bulkZip_commenceBody_mismatchWritesFailList`: type="commence_body", 문서 INTERIM → (filename bulkTypeLabel("commence_body") line360 동반).
- C3 `bulkZip_design_mismatchWritesFailList`: type="design", 문서 INTERIM → 설계내역서 없음.
- C4 `bulkZip_completion_mismatchWritesFailList`: type="completion", 문서 COMMENCE → 준공계 없음.
- (mismatch 라 export 미호출 → stubBulkExports 생략 가능, 단 호출 시 무해.)

## Step 2 — C5/C6: outer catch + COMPLETION 일괄

- C5 `bulkZip_letterExportThrows_writesFailList`: type="letter" + `when(hwpxExportService.generateHwpx(anyInt(), anyString())).thenThrow(new RuntimeException("x"))` + 문서 1건 → 200 + `_실패목록.txt`(bulkAddSingle outer catch 313-314).
- C6 `bulkZip_all_completionDoc_buildsKrasUpis`: type="all" + COMPLETION 문서 + stubBulkExports → 200 + KRAS·UPIS 엔트리(bulkAddAll completion 328-329).

## Step 3 — C7: downloadBulkZip outer catch (null 문서)

- C7 `bulkZip_nullDocInPage_returns500`: loginEdit + searchDocuments → `new PageImpl<>(java.util.Arrays.asList((DocumentDTO) null), PageRequest.of(0,201), 1)` → 루프 `d.getDocId()` NPE(try 내) → status 500.

**검증:** `./mvnw -o -Dtest=DocumentDownloadControllerTest test` 전체 green.

## Step 4 — 전체 검증 + floor 검토

- `./mvnw -o clean verify` BUILD SUCCESS(1446 → +7).
- DocumentDownloadController LINE 90.9 → **~100%** 확인(JaCoCo html).
- 전역 재측정 → 게인 작으면 floor 유지. ratchet·PIT 불변.

## Step 5 — codex 검증 + dual-review + 커밋 + 듀얼푸시

- codex 검증(분기·brittle).
- dual-review(codex+Opus) → 수렴.
- 커밋: `test(coverage): DocumentDownloadController 일괄ZIP 유형불일치·COMPLETION·outer catch 보강 (beyond-A)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR C1~C4 유형불일치 fails | Step 1 |
| FR C5 outer catch·C6 COMPLETION 일괄 | Step 2 |
| FR C7 downloadBulkZip catch 500 | Step 3 |
| NFR verify·90.9→~100% | Step 4 |
| NFR floor/ratchet/PIT 불변 | Step 4 |
| NFR production 0·듀얼푸시 | Step 5 |

## 롤백

- 단일 테스트 파일 케이스 추가 → 문제 시 제거. production 무영향.
