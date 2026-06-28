# [개발계획] DocumentSignedScanService 미커버 경로 보강

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/coverage-document-signed-scan-service.md`
- **상태**: ✅ 구현 완료(2026-06-28). D1~D9 green. DocumentSignedScanService 92.06%(miss 23→10), 전역 LINE 80.78%. codex 구현검증 PASS·dual-review 분쟁4 refute(#4 D6 메시지 단언만 채택). 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- `@Value scanDir`→baseDir, 기존 setUp 이 ReflectionTestUtils 로 @TempDir 주입. `new DocumentSignedScanService(docRepo, userRepo)`(생성자주입 2). docId=**Integer**.
- DocumentType = COMMENCE/INTERIM/COMPLETION 3종(exhaustive). ALLOWED_TYPES = 3종 전부 → requireAllowed throw(L63)는 **docType=null 로만** 도달. label() default(L265) 도달불가(제외).
- 헬퍼 재사용: doc(type,status)·pdf(). 동일경로/내용 구분 위해 `pdf(byte tag)` 추가.

## 1. 테스트 매트릭스 (`DocumentSignedScanServiceUploadTest` 확장)

| # | 테스트 | 픽스처 | 단언 |
|---|---|---|---|
| D1a | originalName_found | docRepo.findById(1)→doc(signedScanOrigName="scan.pdf") | =="scan.pdf" |
| D1b | originalName_notFound | findById(9)→empty | =="signed-scan.pdf" |
| D2 | loadForDownload_success | base 하위 실파일 작성, doc.signedScanPath=경로 | Resource.exists()=true·filename 일치 |
| D3a | loadForDownload_nullPath | doc.signedScanPath=null | IAE "날인본이 없습니다" |
| D3b | loadForDownload_notExists | path=base 하위 미존재 | IAE "파일이 존재하지 않습니다" |
| D3c | loadForDownload_notFound | findById empty | IAE "문서를 찾을 수 없습니다" |
| D4 | upload_notFound | findById empty | IAE "문서를 찾을 수 없습니다" |
| D5 | delete_notFound | findById empty | IAE 동일 |
| D6 | upload_nullDocType_guard | doc.docType=null | IllegalStateException "착수/기성/준공" |
| D7 | upload_emptyFile | MockMultipartFile 0byte | IAE "파일이 비어 있습니다" |
| D8 | upload_uploaderSeq | uploaderSeq=5, userRepo.findById(5)→User(seq5) | doc.getSignedScanUploadedBy()==그 User + verify(userRepo).findById(5) |
| D9 | upload_sameTarget_backupRotate | 동일 doc/project/type 1차→2차(동일 파일명) | target1.equals(target2)·최종 bytes==2차 payload·dir 내 `.bak-` 0 |

- `pdf(byte tag)`: `"%PDF-1.7\n".getBytes` 뒤에 tag 바이트 1개 붙여 내용 구분. MockMultipartFile contentType "application/pdf".
- D9: 1차 `uploadOrReplace(1, pdf(1), null)` → target 확보, 2차 `pdf(2)` → 동일 target → backup 분기. 2차 후 `Files.readAllBytes(target)`==pdf(2) payload + `Files.list(target.getParent())` 에 `.bak-` 0.
- D8: `User u=new User(); u.setUserSeq(5L); when(userRepo.findById(5L)).thenReturn(Optional.of(u));` uploaderSeq=5L.
- D6: `doc(null, COMPLETED)` 또는 doc 후 `d.setDocType(null)`.

## 2. 검증 절차
1. `./mvnw -o clean verify` → BUILD SUCCESS, 기존 12 + 신규(~12) green.
2. `jacoco.csv` 합산 → DocumentSignedScanService LINE 81.7%→~93%+ 확인(`.java.html` 미커버 재확인, 잔여=하드 catch+label default).
3. 전역 delta, floor 유지(게인 작아 버퍼 흡수 예상).
4. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 3. 리스크/완화
- **R1**: D9 동일 target 도달은 파일명 결정성(`{사업명}_{문서종류}.pdf`) 의존 → 동일 doc/project/type 2회로 보장. target equals 단언으로 명시.
- **R2**: 하드 catch(AtomicMove·move rollback·symlink realPath·realPath IOException) 정적 Files 예외 + label default(enum exhaustive) 도달불가 → 제외.
- **R3**: loadForDownload 는 requireAllowed 미호출(upload/delete 와 차이) → not-found/null/not-exists 만 박제.
- production 회귀 0.
