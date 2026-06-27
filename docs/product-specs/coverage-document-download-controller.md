# [기획서] DocumentDownloadController 커버리지 보강 (B)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 커버리지 증분 B. 기존 `DocumentDownloadControllerTest`(부분) 확장. 순수 단위(Mockito + SecurityContextHolder, DB·MockMvc 불요).
- **상태**: ✅ 구현 완료(2026-06-27). DocumentDownloadControllerTest +16(33 total), DocumentDownloadController 48.5→90.9% LINE, 전역 LINE 74.65→75.38%·INSTR 60.84→61.5%, floor 유지. mvnw -o clean verify 1367 green. 구현 후 codex 검증 PASS.

---

## 1. 배경 / 목표

`DocumentDownloadController`(331줄, **LINE 48.5% / miss 102**)는 문서 다운로드/내보내기(목록 Excel·단건 PDF/HWPX/Excel/ZIP·검색 일괄 ZIP) 컨트롤러다. 기존 테스트(`DocumentDownloadControllerTest`, 17개)는 **가드(403)·단건 성공 일부·500 경로·정적 헬퍼**만 덮어 48.5%에 머문다. 미커버 102줄은 대부분 **ZIP 빌드 성공 경로**(`downloadZip`·`downloadBulkZip` + `bulkAddAll`/`bulkAddSingle`/`bulkUnique`)와 **분기 라벨**(HWPX typeLabel switch·excelList prefix·excel INTERIM)이다.

- 의존성 6종 전부 mock: `documentService`·`pdfExportService`·`hwpxExportService`·`excelExportService`·`logService`·`access`(실제 `DocumentAccessSupport` 주입, 기존 패턴 유지).
- **기존 테스트에 케이스 추가**(파일 신규 아님). production 변경 0 (test-only).
- **동작 박제만**, 권한 가드 약화 0([[feedback_all_pages_require_permission]]).

### 미커버 표면(기존 테스트가 안 덮는 분기)

| 영역 | 미커버 분기 |
|---|---|
| `downloadZip` 성공 | COMMENCE(공문+착수계+설계내역서)·INTERIM(공문+기성검사원+기성내역서)·COMPLETION(공문+준공계 KRAS/UPIS, try/ignore)·projNm null 폴백 |
| `downloadBulkZip` 성공 | type="all"(`bulkAddAll`)·단일 type 6종(`bulkAddSingle` switch: 매칭→entry, 비매칭→`fails`)·`bulkUnique` 중복 dedup·`_실패목록.txt`·파일명 라벨 |
| `downloadHwpx` | typeLabel switch 7분기(letter/inspector/commence_body/completion_body/completion_body_upis/completion_full/default)·projNm·docType label null |
| `downloadExcel` | INTERIM 분기(`generateInterimReport`) — 기존은 design(null docType)만 |
| `downloadDocumentListExcel` | prefix 라벨: 유효 docType→label()·잘못된 docType→IllegalArgumentException ignore→"사업문서목록"·blank |

---

## 2. 변경 설계 — DocumentDownloadControllerTest 케이스 추가

기존 `setUp`/`inject`/`login*` 헬퍼 재사용(EDIT 권한 전제는 `loginEdit()`/`loginAdmin()`). export 서비스 mock 은 `new byte[]{...}` 반환. `Document`는 `@Getter@Setter`(`setDocType`·`setProject`), `DocumentDTO`는 `@Data`(`setDocId`/`setDocType`/`setCityNm`/`setDistNm`/`setProjNm`), `SwProject.setProjNm` 으로 구성.

### A. downloadZip 성공 3종 + projNm 폴백
- COMMENCE: `doc.docType=COMMENCE`, project.projNm="사업A" → `generateHwpx(id,"letter")`·`generateHwpx(id,"commence_body")`·`generateDesignEstimate(id)` 각 mock → 200·Content-Type=application/zip·body 비어있지 않음. ZIP 엔트리 수 3 검증(ByteArrayInputStream→ZipInputStream 으로 entry 카운트).
- INTERIM: → 공문+기성검사원+기성내역서(3 entry).
- COMPLETION: → 공문 + 준공계 KRAS/UPIS(generateHwpx 정상 → 3 entry). 추가로 **KRAS 생성이 throw 해도 try/ignore 로 나머지 진행**(UPIS만 들어가 200) 1건.
- **projNm 폴백**: `doc.project=null` → 파일명 "문서" 경로(200, 예외 없음).

### B. downloadBulkZip 성공
- searchDocuments → `PageImpl<DocumentDTO>`(total ≤ 200) 반환. DocumentDTO 2건(docId/docType/cityNm/distNm/projNm 세팅).
- **type="all"**: 서로 다른 docType(COMMENCE·INTERIM) 2건 → `bulkAddAll` 경로 → 200·zip entry 다수.
- **단일 type 매칭/비매칭**: type="inspector"+docType=INTERIM(매칭→기성검사원 entry) vs docType=COMMENCE(비매칭→`fails`). type="design"+COMMENCE(매칭). type="completion"+COMPLETION(KRAS/UPIS). type="letter"(전 docType 공통). → `_실패목록.txt` 엔트리 존재 검증(fails 비어있지 않을 때).
- **bulkUnique 중복**: 동일 cityNm/distNm/projNm 2건+type="letter" → base 동일 → `_2` suffix 엔트리 생성(zip entry 이름에 "_2" 포함).
- 파일명: `bulkTypeLabel(type)` prefix(이미 정적 테스트 있음 — 통합 경로로 간접 재확인).

### C. downloadHwpx typeLabel switch + 폴백
- type 별로 호출(letter/inspector/commence_body/completion_body/completion_body_upis/completion_full/그외="custom") → 각 200, 파일명 suffix 분기 실행. doc.project=null·docType=null 폴백 1건(200).

### D. downloadExcel INTERIM
- `doc.docType=INTERIM` → `generateInterimReport(id)` 호출 verify + 200(기존은 design만).

### E. downloadDocumentListExcel prefix 라벨
- 유효 docType("COMMENCE") → prefix=label() (기존 ok 테스트가 이미 통과하나 prefix 분기 명시 단언 추가 가능). **잘못된 docType("ZZZ")** → IllegalArgumentException ignore → 200(prefix 기본). blank docType → 기본.

---

## 3. 기능 / 비기능 요건

| ID | 내용 |
|----|------|
| FR-1 | downloadZip 3 docType + projNm 폴백, downloadBulkZip all/단일/dedup/실패목록, downloadHwpx switch 7, downloadExcel INTERIM, excelList prefix 분기를 단위테스트로 박제. |
| FR-2 | 권한 가드(EDIT/admin→EDIT) 동작 유지 — 약화 0(기존 가드 테스트 보존). |
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS(DB 불필요). |
| NFR-2 | DocumentDownloadController **48.5 → 90%+ LINE**. |
| NFR-3 | floor 실측 후 상향 검토(전역 영향 작으면 생략). ratchet/PIT 불변. |
| NFR-4 | production 변경 0. dual-review → 듀얼푸시. |

---

## 4. 영향 / 리스크

| 파일 | 유형 |
|------|------|
| `controller/DocumentDownloadControllerTest.java` | 기존 파일 케이스 추가 |

production 변경 0.

| 리스크 | 수준 | 완화 |
|---|---|---|
| ZIP 바이트 검증 brittle | 낮음 | 바이트 내용 비단언 — ZipInputStream 으로 **엔트리 이름/개수**만 구조 단언. |
| mock byte[] 가 실제 산출물과 다름 | 낮음 | 컨트롤러는 byte[] 를 ZIP 에 넣을 뿐 — 산출물 정합은 각 서비스 테스트 책임(범위 외). |
| SecurityContextHolder 누수 | 낮음 | 기존 `@AfterEach clearContext()` 유지. |
| bulkAddSingle 내부 catch 도달 | 낮음 | 매칭 docType + generate throw 로 inner catch→fails 도달(필요 시). |

---

## 5. 승인 요청

본 기획서(DocumentDownloadController 커버리지)에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 특히
1. **ZIP 엔트리 구조 단언**(이름/개수)이 바이트 단언보다 안정적인가,
2. **bulkAddSingle/bulkAddAll 분기 커버 범위**(전 type·매칭/비매칭·dedup·실패목록)가 충분한가,
3. **누락 분기**(completion try/ignore, projNm/docType null 폴백) 의견 요청.
