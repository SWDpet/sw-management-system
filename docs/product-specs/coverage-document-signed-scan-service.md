# [기획서] DocumentSignedScanService 미커버 경로 보강 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분. 사업문서 날인본 스캔 PDF 저장 서비스. OpsSupportFileService 자매(동일 패턴). 기존 `DocumentSignedScanServiceUploadTest` 확장형(테스트만).
- **상태**: ✅ 구현 완료(2026-06-28). `DocumentSignedScanServiceUploadTest` +12(D1~D9). **DocumentSignedScanService LINE 81.7→92.06%(miss 23→10)·INSTR 90.53%, 전역 LINE 80.66→80.78%·INSTR 65.83→65.96%.** floor 0.77/0.63 유지. `mvnw -o clean verify` green. 잔여 10줄=정적 Files 예외 catch(L101 symlink·L139-147 AtomicMove/move rollback·L203-204 realPath IOException)+L265 label default(enum exhaustive 도달불가) 의도 제외. codex 기획 검토 실질승인(글리치)·구현검증 PASS. dual-review(codex0/Opus4) 합의0·분쟁4 전건 codex refute(empty 스텁 소비·D9 결정성·Resource 핸들없음·D6 게이트 유일ISE), **#4 제안만 채택**(D6 메시지 단언 "착수/기성/준공" 추가로 게이트 확정). production 변경 0.

---

## 1. 배경 / 목표

`DocumentSignedScanService`(LINE 103/126 = 81.7%, miss **23**)는 OpsSupportFileService([[직전 4f0b196]])의 자매 서비스로 구조가 동일하다. 기존 3 테스트(Upload 12·plain 9·DDrive 1)가 upload happy/reject/replace-다른경로/delete·helper 를 덮었으나, **조회 성공·uploaderSeq·동일경로 backup·findById not-found 람다·null-docType 게이트·empty 파일**이 미커버다. 전부 `@TempDir`(scanDir 주입)+mock repo 로 도달 가능.

JaCoCo 미커버(`DocumentSignedScanService.java.html`):

| 영역 | 라인 | 미커버 정체 |
|---|---|---|
| originalName | L211-213 | **전혀 미테스트**(found→origName / not-found→"signed-scan.pdf") |
| loadForDownload | L193·195·199·206 | **not-found 람다·null path·not-exists·성공(FileSystemResource)** (기존은 outsideBase 거부만) |
| uploadOrReplace | L75·80·125-126·133-134 | **findById not-found·empty 파일·uploaderSeq!=null·동일 target backup** |
| delete | L167 | findById **not-found 람다** |
| requireAllowed | L63 | **docType 불일치 게이트 throw**(DocumentType 3종뿐 → **docType=null 로만 도달**, 방어 가드) |
| **제외(하드/도달불가)** | L101·139-147·154/159/178·203-205·265 | symlink realPath·AtomicMove/move rollback·ignored catch·realPath IOException catch·**label() default(enum 3종 exhaustive → 도달불가)** |

목표: 위 도달가능 경로 박제 → ~93%+ (잔여=정적 Files 예외 catch + label default 도달불가, 의도 제외). 실 파일·DTO 단언으로 위장통과 차단.

## 2. 범위 (테스트만, production 무변경)
`DocumentSignedScanServiceUploadTest` 확장. setUp(@TempDir scanDir·docRepo/userRepo mock·saveAndFlush/save thenAnswer)·헬퍼(doc(type,status)·pdf()) 재사용.

- **D1 originalName**: found(signedScanOrigName="scan.pdf")→"scan.pdf" / not-found→"signed-scan.pdf".
- **D2 loadForDownload 성공·null·not-exists**: 실파일 base 하위 작성+path 설정→`FileSystemResource`(exists·filename) / path==null→"날인본이 없습니다" / 파일부재→"파일이 존재하지 않습니다".
- **D3 loadForDownload not-found**: findById empty→"문서를 찾을 수 없습니다".
- **D4 upload not-found**: findById empty→"문서를 찾을 수 없습니다".
- **D5 delete not-found**: findById empty→동일 예외.
- **D6 requireAllowed null docType**: doc.docType=null → upload 시 IllegalStateException("착수/기성/준공 문서만…").
- **D7 empty 파일**: 0byte MultipartFile → IllegalArgumentException("파일이 비어 있습니다").
- **D8 uploaderSeq**: uploaderSeq+userRepo.findById→User → 성공 후 doc.signedScanUploadedBy==그 User + verify findById.
- **D9 동일 target backup**: 동일 doc/project/type 로 1차→2차 업로드(동일 파일명 `{사업명}_{문서종류}.pdf`) → **target1==target2(equals)+최종 bytes==2차 payload+.bak- 잔존0**(backup→원자이동→삭제 박제).

## 3. 검증 방식 (위장통과 차단)
- 파일 I/O 는 실제 @TempDir 파일 존재/내용(byte) 단언. 조회는 반환타입/값. 거부는 예외타입+메시지 토큰. uploaderSeq 는 setUploadedBy 결과 동일성(assertSame).

## 4. 요건
- **FR-1**: originalName found/not-found.
- **FR-2**: loadForDownload 성공·null·not-exists·not-found.
- **FR-3**: uploadOrReplace not-found·empty·uploaderSeq·동일경로 backup.
- **FR-4**: delete not-found, requireAllowed null-docType 게이트.
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, DocumentSignedScanService LINE 81.7%→**~93%+**(잔여=정적 Files 예외 catch+label default 도달불가, 의도 제외), 전역 유지/소폭 상향, floor 유지, 구현 후 codex PASS + dual-review → 듀얼푸시.

## 5. 영향 / 리스크
- 변경: `service/DocumentSignedScanServiceUploadTest.java`(케이스 추가)만. production 0.
- **R1 D9 동일경로 backup 도달**: 파일명 `{사업명}_{문서종류}.pdf` 결정적 → 동일 doc/project/type 2회로 동일 target → backup 분기. target1.equals(target2)+최종 bytes 단언으로 강증명(OpsSupportFileService T5 패턴).
- **R2 하드 제외**: AtomicMove·move rollback·symlink realPath·realPath IOException catch = 정적 Files 예외라 단위 도달불가. label() default = DocumentType 3종 exhaustive 라 도달불가(switch null→NPE, default 미도달) → 제외 명시.
- **R3 D6 null docType**: DocumentType enum 이 3종(COMMENCE/INTERIM/COMPLETION)뿐이라 "허용외 유효 타입"이 없음 → 게이트 throw 는 **docType=null 로만** 도달(방어 가드의 정직한 커버).
- production 회귀 0.
