# [기획서] OpsSupportFileService 미커버 경로 보강 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분. 업무지원 지원문서 파일 서비스. 기존 `OpsSupportFileServiceTest` 확장형(테스트만).
- **상태**: ✅ 구현 완료(2026-06-28). `OpsSupportFileServiceTest` +12(T1~T7 + magicOk기본false + 비-main섹션 fallback). **OpsSupportFileService LINE 85.3→93.87%(miss 24→10)·INSTR 90.70%, 전역 LINE 80.53→80.66%·INSTR 65.71→65.83%.** floor 0.77/0.63 유지(게인 작아 버퍼 흡수). `mvnw -o clean verify` 1488 green. 잔여 10줄=정적 Files 예외 catch(L128 symlink realPath·L168-177 move rollback·L233-234 realPath IOException·L184/189/207 ignored catch)로 단위 도달불가 의도 제외(목표 ~95%엔 약간 못미침). codex 기획 APPROVE-WITH-FIX(T5 단언강화: target equals+내용 단언 반영)·구현검증 PASS. dual-review(codex1/Opus5) 합의1 반영(T7b 주석 명확화), 분쟁5 codex 정확 refute(mockRegion 불요·Resource 핸들없음·download not-found 별개람다·.bak- 명명 일치). production 변경 0.

---

## 1. 배경 / 목표

`OpsSupportFileService`(LINE 139/163 = 85.3%, miss **24**)는 업로드 거부 경로·happy-path 경로 일부는 덮였으나, **조회 성공 경로·uploaderSeq 분기·동일경로 교체 backup·findById not-found 람다**가 미커버다. 전부 `@TempDir`(scanDir 주입) + mock repo 로 도달 가능한 파일 I/O 다(실 D드라이브 무관).

JaCoCo 미커버(`OpsSupportFileService.html`):

| 메서드 | LINE miss | 미커버 정체 |
|---|---|---|
| `uploadOrReplace` | 12 | **uploaderSeq != null**(userRepository 조회+setUploadedBy)·**동일 target 존재 시 backup→원자이동→backup 삭제**·(하드: AtomicMove catch·move 실패 rollback·symlink realPath throw = 정적 Files 예외라 의도 제외) |
| `loadForDownload` | 6 | **성공 경로**(path 존재·realPath ok→FileSystemResource)·**path==null throw**·**not-exists throw**(기존 테스트는 !startsWith 거부만) |
| `originalName` | 3 | **전혀 미테스트**(found→origName / not-found→"support-file") |
| lambda$loadForDownload$2 / lambda$delete$1 / lambda$uploadOrReplace$0 | 1×3 | findById **not-found orElseThrow** 람다(기존 테스트는 항상 Optional.of) |
| yearFolder / sectionValue / supportTarget | 1×3 | yearFolder "연도미상"(rd·createdAt 둘 다 null) 등 잔여 분기 |

목표: 위 경로를 박제해 ~95%+ 로 올린다. 잘못된 권한/경로/조회 회귀를 실제 파일·DTO 단언으로 방어(위장통과 차단).

## 2. 범위 (테스트만, production 무변경)
기존 `OpsSupportFileServiceTest` 확장. setUp(@TempDir scanDir·mock 5repo·saveAndFlush/save thenAnswer)·헬퍼(supportDoc/mockSection/withMagic/file) 재사용.

- **T1 originalName**: found(doc.supportFileOrigName="plan.hwpx")→"plan.hwpx" / not-found(findById empty)→"support-file" fallback.
- **T2 loadForDownload 성공**: 실제 파일을 base 하위에 쓰고 doc.supportFilePath 설정 → `FileSystemResource` 반환(exists·경로 일치 단언).
- **T3 loadForDownload 거부 2종**: path==null→IllegalArgumentException("지원문서가 없습니다") / path 설정했으나 파일 부재→IllegalArgumentException("파일이 존재하지 않습니다").
- **T4 uploadOrReplace uploaderSeq**: uploaderSeq 전달+userRepository.findById→User → 성공 후 doc.supportFileUploadedBy == 그 User 단언 + verify(userRepository).findById.
- **T5 uploadOrReplace 동일경로 backup**: 동일 target 경로에 기존 파일 선배치(같은 system/region/target/docNo→동일 파일명) → 교체 성공(backup→원자이동→backup 삭제) + 최종 파일 내용이 새 파일 + .bak 잔존 없음 단언.
- **T6 not-found 람다 3종**: uploadOrReplace/delete/loadForDownload 각각 findById empty → IllegalArgumentException("문서를 찾을 수 없습니다") (orElseThrow 람다 커버).
- **T7 yearFolder 연도미상**(잔여): request_date 없음·createdAt null → 경로에 "연도미상" 포함.

## 3. 검증 방식 (위장통과 차단)
- 파일 I/O 는 **실제 @TempDir 파일 존재/내용** 단언(존재·byte 일치·경로 prefix). 조회는 반환 타입/값 단언.
- 거부는 예외 타입 + 메시지 핵심 토큰 단언. uploaderSeq 는 setUploadedBy 결과 객체 동일성 단언.

## 4. 요건
- **FR-1**: originalName found/not-found 박제.
- **FR-2**: loadForDownload 성공·null·not-exists 박제.
- **FR-3**: uploadOrReplace uploaderSeq!=null·동일경로 backup 박제.
- **FR-4**: findById not-found orElseThrow 람다 3종(upload/delete/download) 박제.
- **FR-5**: yearFolder "연도미상" 잔여 분기 박제.
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, OpsSupportFileService LINE 85.3%→**~95%+**(잔여=정적 Files 예외 catch 의도 제외), 전역 유지/소폭 상향, floor 유지, 구현 후 codex PASS + dual-review → 듀얼푸시.

## 5. 영향 / 리스크
- 변경: `service/ops/OpsSupportFileServiceTest.java`(케이스 추가)만. production 0.
- **R1 동일경로 backup 도달**: target 파일명은 `{supportTarget}_{docNo}.{ext}` 결정적 → 같은 doc/section/region/file 로 선배치하면 동일 target → backup 분기 진입. mockSectionDefault 와 동일 입력 사용.
- **R2 하드 분기 제외**: AtomicMoveNotSupported·move IOException rollback·symlink realPath throw 는 정적 `Files`/FS 예외라 mock 없이 단위 도달 불가 → 의도적 제외(방어 코드).
- production 회귀 0(테스트 추가).
