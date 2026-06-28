# [개발계획] OpsSupportFileService 미커버 경로 보강

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/coverage-ops-support-file-service.md`
- **상태**: ✅ 구현 완료(2026-06-28). T1~T7 + 보강 2(magicOk false·비-main fallback) green. OpsSupportFileService 93.87%(miss 24→10), 전역 LINE 80.66%. codex 구현검증 PASS·dual-review 합의1(주석)만 반영. 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- `@Value("${file.scan-dir:./uploads/scan}") String scanDir` → `baseDir()`. 기존 setUp 이 `ReflectionTestUtils.setField(service,"scanDir",tempDir)` 로 @TempDir 주입 → 파일 I/O 단위테스트 가능.
- 미커버 메서드/람다: uploadOrReplace(uploaderSeq·동일경로 backup), loadForDownload(성공·null·not-exists), originalName(전무), findById not-found 람다 3종, yearFolder "연도미상".
- 헬퍼 재사용: supportDoc()·mockSectionDefault()·mockRegionSystem()·withMagic()·file()·M_PDF/M_ZIP/M_OLE2.

## 1. 테스트 매트릭스 (`OpsSupportFileServiceTest` 확장)

| # | 테스트 | 픽스처 | 단언 |
|---|---|---|---|
| T1a | originalName_found | docRepo.findById(1)→doc(supportFileOrigName="plan.hwpx") | `originalName(1)`=="plan.hwpx" |
| T1b | originalName_notFound | docRepo.findById(9)→empty | =="support-file"(orElse fallback) |
| T2 | loadForDownload_success | base 하위에 실제 파일 작성, doc.supportFilePath=그 경로 | 반환 `Resource` exists()=true·파일명 일치(FileSystemResource) |
| T3a | loadForDownload_nullPath | doc.supportFilePath=null | IllegalArgumentException "지원문서가 없습니다" |
| T3b | loadForDownload_notExists | doc.supportFilePath=base 하위 미존재 경로 | IllegalArgumentException "파일이 존재하지 않습니다" |
| T4 | upload_uploaderSeq_setsUploadedBy | uploaderSeq=5, userRepo.findById(5)→User(userSeq=5) | 성공 후 doc.getSupportFileUploadedBy()==그 User + verify(userRepo).findById(5) |
| T5 | upload_sameTargetPath_backupRotate | mockSectionDefault+mockRegionSystem 로 1차 업로드 → **동일 입력 2차 업로드**(동일 target) | **1차 target Path == 2차 target Path(equals)**(동일경로 backup 분기 진입 증명)·**최종 파일 bytes == 2차 payload**(원자이동 성공)·해당 dir 에 `.bak-` 잔존 0 (codex 강화) |
| T6a | upload_docNotFound | docRepo.findById(7)→empty | IllegalArgumentException "문서를 찾을 수 없습니다" |
| T6b | delete_docNotFound | docRepo.findById(7)→empty | 동일 예외(delete orElseThrow 람다) |
| T6c | download_docNotFound | docRepo.findById(7)→empty | 동일 예외(loadForDownload orElseThrow 람다) |
| T7 | upload_yearFolder_unknown | doc.createdAt=null, section request_date 없음 | 경로에 "연도미상" 포함 |

- T5 구현: 1차 `uploadOrReplace(1, file("a.pdf",M_PDF), null)` → target 경로 확보. 2차 동일 docId+동일 section/region → 동일 파일명(`{supportTarget}_{docNo}.pdf`) → target 선존재 → backup 분기. 2차 후 그 디렉터리 `Files.list` 로 `.bak-` 패턴 파일 0 단언 + 최종 파일 exists.
- T4: User 픽스처 `new User()` + setUserSeq(5). userRepo.findById(5L)→Optional.of(u).

## 2. 검증 절차
1. `./mvnw -o clean verify` → BUILD SUCCESS, 기존 12 + 신규(~11) green.
2. `jacoco.csv` 점표기 합산 → OpsSupportFileService LINE 85.3%→~95%+ 확인(`OpsSupportFileService.html` 메서드별 miss 재확인). 잔여=정적 Files 예외 catch(의도 제외) 위주.
3. 전역 delta 측정, floor 유지(게인 작아 버퍼 흡수 예상).
4. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 3. 리스크/완화
- **R1**: T5 동일경로 backup 도달은 파일명 결정성에 의존 → mockSectionDefault/mockRegionSystem 동일 입력 2회로 보장. 미도달 시 JaCoCo 로 검출.
- **R2**: 하드 catch(AtomicMove·move rollback·symlink realPath) 정적 Files 예외라 제외 — 명시.
- **R3**: loadForDownload realPath IOException catch(line 233-235)도 정적 예외라 제외 가능 → 성공/null/not-exists 3종만 박제.
- production 회귀 0.
