# [기획서] InspectReportService 잔여 경로 보강 (reset/조회/감사) (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분. 점검내역서 서비스. 기존 `InspectReportServiceTest` 확장형(테스트만).
- **상태**: ✅ 구현 완료(2026-06-28). `InspectReportServiceTest` +7(A1~A5). **InspectReportService LINE 85.8→100%(miss 21→0)·INSTR 97.88%, 전역 LINE 80.78→80.95%·INSTR 65.96→66.11%.** floor LINE 0.77→0.78·INSTR 0.63→0.64 래칫(오늘 5스프린트 누적 봉인). `mvnw -o clean verify` green. **resetAllInspectData 제외 reversal**: mock 기반이라 실삭제 0, count 캡처+6저장소 삭제+**삭제순서(child-first) InOrder** 박제(codex 제안 반영). codex 기획·구현검증 PASS. dual-review(codex1/Opus5) 합의5 중 4 반영(A4 3-arg authenticated 토큰·A5b 연도 정규식매처로 연말 flaky 제거·count map hasSize(6)·"7 repo" 주석 명확화), #5(pom 래칫)은 verify 통과로 확인됨, 분쟁1(7 repo) codex refute=주석만 명확화. production 변경 0(클래스 주석 갱신만).

---

## 1. 배경 / 목표

`InspectReportService`(LINE 85.8%, miss **21**)의 미커버는 외부의존이 아니라 mock 으로 도달 가능한 경로다. 가장 큰 덩어리는 **`resetAllInspectData`(16줄)** — 직전 sprint 가 "파괴적이라 커버 제외(codex)"로 비워둔 부분이다.

**⚠ 핵심 판단(이전 제외 결정 재검토)**: `resetAllInspectData` 의 "파괴성"은 **운영 DB 직결 시**의 우려이고, **본 단위테스트는 7개 repository 를 전부 mock** 한다 → `deleteAllInBatch()`/`deleteAll()` 은 mock 호출이라 **실제 삭제가 일어나지 않는다(완전 무해)**. 오히려 이 위험한 일괄삭제 연산의 **계약(① count 를 삭제 전에 캡처 ② 6개 저장소를 빠짐없이 wipe ③ count map 구성)을 박제**하면, 누군가 삭제 순서를 바꾸거나 저장소를 빠뜨리는 회귀를 방어할 수 있어 **테스트 가치가 크다**. → 제외를 뒤집어 **mock 기반으로 안전하게 커버**한다(클래스 주석의 "제외" 문구도 갱신).

JaCoCo 미커버(`InspectReportService.java.html`, nc):

| 영역 | 라인 | 미커버 정체 |
|---|---|---|
| resetAllInspectData | L251-269 (16) | **일괄삭제 전체**(count 캡처 + 6 저장소 deleteAllInBatch/deleteAll + count map) |
| findByProjectAndMonth | L214-216 (3) | **repo 조회 found→findById(풀 DTO) / not-found→orElse(null)** (기존은 null-guard L213 만) |
| save | L81 (1) | update 모드 **incoming checkResult 가 resultCode 보유 → incomingWithCode.add** |
| currentUser | L294 (1) | SecurityContext **인증 present → getName()** (기존 save 는 미인증→catch "system") |
| delete(pc) | L224·L233 | not-found orElseThrow / inspectMonth null·짧음 → 연도 now() fallback |

목표: 위 박제로 ~99%+. 위험 연산(reset)은 순서·완전성을 InOrder/verify 로 강하게 고정.

## 2. 범위 (테스트만, production 무변경)
`InspectReportServiceTest` 확장. 기존 9 mock 필드(7 repo+OpsDocLinkService+MessageResolver)+`new InspectReportService(...)` 재사용.

- **A1 reset 일괄삭제**: opsDocumentRepository.findByDocType...(INSPECT)→2건, 각 repo.count()→고정수 → `resetAllInspectData()` → **count map 6키 값 단언**(checkResult/visitLog/qrBatch/metricSnapshot/opsDocInspect/report) + **6 삭제 verify**(deleteAllInBatch ×5 + deleteAll(inspectDocs)) + **InOrder: count() 가 deleteAllInBatch() 보다 먼저**(삭제 전 스냅샷 계약).
- **A2 findByProjectAndMonth found**: repo.findByPjtIdAndInspectMonthAndDeletedAtIsNull(pjt,month)→Optional.of(report id=R), findById(R) 경로 mock(report+visits+checkResults) → 풀 DTO 반환(id 단언) / **A2b not-found**: repo empty → null.
- **A3 save L81**: update 모드(report.id!=null) + 기존 existing mock + **incoming checkResults 중 resultCode 보유 항목** → incomingWithCode 에 포함되어 해당 section 은 보호목록에서 제외(=삭제 대상) 검증.
- **A4 currentUser auth**: SecurityContextHolder 에 인증(UsernamePasswordAuthenticationToken "alice") 세팅 후 save(new) → 저장 report.createdBy=="alice"(ArgumentCaptor) → finally clearContext.
- **A5 delete 잔여**: not-found(repo empty→orElseThrow) / inspectMonth null → 연도 now() fallback 경로(opsDoc docNo "INSP-{now년}-{id}" 조회).

## 3. 검증 방식 (위장통과 차단)
- reset 은 단순 라인커버가 아니라 **count map 값 + 삭제 호출 + 순서(InOrder)** 로 계약 고정. save L81 은 보호집합 결과(deleteByReportIdAndSectionIn 인자)로 행위 단언. currentUser 는 캡처된 createdBy 값 단언.

## 4. 요건
- **FR-1**: resetAllInspectData count 캡처+6저장소 삭제+순서 박제.
- **FR-2**: findByProjectAndMonth found/not-found.
- **FR-3**: save incomingWithCode(resultCode 보유) 분기.
- **FR-4**: currentUser 인증 present 경로.
- **FR-5**: delete not-found / 연도 fallback.
- **NFR**: production 변경 0(클래스 주석 1줄 갱신 제외 — "reset 제외" → "reset mock 커버"), `mvnw -o clean verify` SUCCESS, InspectReportService LINE 85.8%→**~99%+**, 전역 유지/소폭 상향, floor 유지, 구현 후 codex PASS + dual-review → 듀얼푸시.

## 5. 영향 / 리스크
- 변경: `service/InspectReportServiceTest.java`(케이스 추가 + 클래스 주석 갱신)만. production 코드 0.
- **R1 reset 제외 reversal**: 이전 codex 제외는 "파괴적" 근거 — mock 테스트엔 무효(실 삭제 없음). 본 sprint 가 명시적으로 뒤집되 **InOrder/verify 로 위험 연산을 안전하게 박제**(회귀 방어 강화). codex 사전검토에서 재확인.
- **R2 findById 경유**: A2 found 는 findByProjectAndMonth→findById 체인 → findById mock(report/visits/checkResults) 필요. 기존 findById 테스트 패턴 재사용.
- production 회귀 0.
