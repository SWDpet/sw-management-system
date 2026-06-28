# [기획서] DocumentDownloadController 일괄ZIP 유형불일치·COMPLETION 분기 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분 B. 일괄 다운로드 컨트롤러. 기존 `DocumentDownloadControllerTest` 확장형.
- **상태**: ✅ 구현 완료(2026-06-28). DocumentDownloadControllerTest +7(33→40) → DocumentDownloadController **90.9→100% LINE**(198/198). 전역 LINE 78.57→78.72%·INSTR 64.13→64.23%, floor 유지. `mvnw -o clean verify` 1453 green. codex 기획 APPROVE-WITH-FIX(outer catch는 null문서로 도달)·개발계획 PASS·구현 PASS. dual-review(codex2/Opus7) 합의6 반영(BULK_PAGE_SIZE 상수·zipEntryNames null가드·generateHwpx 호출 verify·completion stub 협소화·outer catch 테스트 계약 재서술), 분쟁1 미반영(noneMatch가 실패목록 거부=Opus refute).
- **codex 보완**: outer catch(263-265)를 잔여 제외하지 말고 커버 — ⚠codex 제안(searchDocuments throw)은 부정확(searchDocuments 는 try **밖** line 239 → throw 시 전파). **올바른 도달법: page.content 에 null 문서 → 루프 내 `d.getDocId()` NPE(try 안) → outer catch 500.** C7 추가 → ~100%.

---

## 1. 배경 / 목표

`DocumentDownloadController`(LINE 90.9% / miss 18)는 일괄ZIP happy-path·가드·dedup·일부 mismatch(inspector)를 덮었으나, **bulkAddSingle 의 나머지 유형불일치 else 분기(interim/commence_body/design/completion)·bulkAddSingle outer catch·bulkAddAll COMPLETION 분기·bulkTypeLabel commence_body** 가 미커버다. 산출물 유형 가드(문서유형과 요청 type 불일치 시 "_실패목록.txt" 기록)는 실 비즈니스 로직이라 박제한다.

JaCoCo 미커버(`DocumentDownloadController.java.html` nc): 263-265, 296-297, 300-301, 304-305, 310, 313-314, 328-329, 360.

## 2. 대상 분기

| # | 영역(라인) | 내용 |
|---|---|---|
| C1 | bulkAddSingle interim 불일치(296-297) | type="interim" + 非INTERIM 문서 → "기성계 아님" fails.add |
| C2 | bulkAddSingle commence_body 불일치(300-301) | type="commence_body" + 非COMMENCE → fails.add (+ bulkTypeLabel commence_body **360** 동반) |
| C3 | bulkAddSingle design 불일치(304-305) | type="design" + 非COMMENCE → fails.add |
| C4 | bulkAddSingle completion 불일치(310) | type="completion" + 非COMPLETION → fails.add |
| C5 | bulkAddSingle outer catch(313-314) | type="letter" + generateHwpx throw → catch fails.add(예외 삼킴, 200 유지) |
| C6 | bulkAddAll COMPLETION(328-329) | type="all" + COMPLETION 문서 → 준공계 KRAS/UPIS 엔트리 |
| C7 | downloadBulkZip outer catch(263-265) | page.content 에 null 문서 → 루프 `d.getDocId()` NPE(try 내) → 500 |

## 3. 변경 — `DocumentDownloadControllerTest` 케이스 추가 (테스트만)
- 기존 helper(`dto(id,type,city,dist,proj)`·`zipEntryNames(byte[])`·`stubBulkExports()`·`loginEdit()`)·searchDocuments Page mock 패턴 재사용. 신규 6 테스트.
- **C1~C4**: `downloadBulkZip(..., type)` + searchDocuments→단일 불일치 문서 Page → status 200 + `zipEntryNames` 에 `_실패목록.txt` 포함·해당 산출물 엔트리 없음 단언.
- **C5**: type="letter" + `hwpxExportService.generateHwpx` thenThrow → 200 + `_실패목록.txt`(공문 생성 실패 기록).
- **C6**: type="all" + COMPLETION 문서 → KRAS·UPIS 엔트리 단언.
- **C7**: searchDocuments → `new PageImpl<>(Arrays.asList((DocumentDTO) null), …, 1)` → 루프 첫 원소 null → `d.getDocId()` NPE → outer catch → status 500.

## 4. 요건
- **FR**: C1~C7 분기 박제(유형가드 실패목록·COMPLETION 일괄·outer catch). 산출물 선택 회귀 방어.
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, DocumentDownloadController 90.9→~100% LINE, JaCoCo floor 유지, 구현 후 codex PASS → 듀얼푸시.

## 5. 영향/리스크
- `controller/DocumentDownloadControllerTest.java` 케이스 추가. production 0.
- 리스크: mismatch 테스트는 export 서비스 미호출이나 `stubBulkExports()` 가 plain mock(strict 아님)이라 미사용 stub 무해. C5 는 generateHwpx 전건 throw 라 동일 stub 충돌 주의(별도 thenThrow). residual 263-265(outer catch) 의도적 제외.
