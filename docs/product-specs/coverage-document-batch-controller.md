# [기획서] DocumentBatchController 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-27
- **트랙**: beyond-A 커버리지 증분 B. 기존 `DocumentBatchControllerTest` 확장. 순수 단위(Mockito + SecurityContextHolder).
- **상태**: ✅ 구현 완료(2026-06-27). DocumentBatchControllerTest +4메서드(15→19), DocumentBatchController 69.7→98.1% LINE(152/155)·BRANCH 82.6%, 전역 LINE 76.03→76.4%·INSTR 62.09→62.49%, floor 유지(게인 작음). mvnw -o clean verify 1381 green. 구현 후 codex 검증 PASS.

---

## 1. 배경 / 목표

`DocumentBatchController`(303줄, **LINE 69.7% / miss 47**)는 착수/기성/준공 일괄 작성 컨트롤러. 기존 테스트(15개)는 가드·batchGenerate **COMPLETION** 성공·실패/예외 경로를 덮지만, **batchGenerate INTERIM 전체 경로**(inspector+detail_sheet+KRAS+paymentRate 계산)·**getAllSystemsForYear 데이터 경로**·**getBatchTargets COMPLETION/sysNmEn 필터**·**buildBatchLetterData recipient 분기**가 미커버다(47줄의 대부분).

- 의존성 mock(documentService·swProjectRepository·userRepository·logService·access). 기존 setUp/login* 재사용. production 변경 0.

### 미커버 표면

| 영역 | 분기 |
|---|---|
| `batchGenerate` INTERIM | inspector 섹션(name/amount/contractDate/period) + commonData(interimYear/Month/Day) + **paymentRate→paymentAmount 자동계산**(rate×contAmt/100) + detail_sheet(contAmt/bidRate/periodText/prevRate) + **KRAS 시스템→GeoNURIS 항목 자동추가** |
| `getAllSystemsForYear` | projects 존재 시 year 필터 + sysNmEn dedup(putIfAbsent) + SystemAllRow 매핑 + 정렬 |
| `getBatchTargets` | COMPLETION 분기(completionYn) + sysNmEn 필터 |
| `buildBatchLetterData` | recipient: orgNm / distNm+"청" / cityNm+"청" 3분기 · INTERIM body2/attachList · contDt 포맷 |

---

## 2. 변경 설계 — DocumentBatchControllerTest 케이스 추가

`SwProject`(@Getter@Setter): setContAmt(Long)/setContDt(LocalDate)/setStartDt/setEndDt/setContRt(Double)/setOrgNm/setSysNmEn/setSysNm/setProjNm/setYear/setInterimYn/setCompletionYn.

### A. batchGenerate INTERIM 성공(KRAS + paymentRate)
- SwProject: projId=7, projNm, sysNmEn="KRAS", sysNm, contAmt=100_000_000L, contDt/startDt/endDt(LocalDate), contRt=90.0. createDocument→doc(docId=100). commonData={interimYear/Month/Day, paymentRate:"50", periodText, prevRate}.
- 단언: 200·successCount=1. saveSection("letter",0)/("inspector",1)/("detail_sheet",2) 호출. **inspector ArgumentCaptor: paymentAmount = round(100_000_000×50/100)=50_000_000 문자열** + interimYear 반영. **detail_sheet ArgumentCaptor: items 에 "GeoNURIS for KRAS v1.0"(unitPrice 77_000_000) 존재**(KRAS 분기).

### B. batchGenerate INTERIM — paymentRate 비숫자(NumberFormat ignore)
- paymentRate="abc" → paymentAmount 미설정(catch ignore). 200·success. (분기: try/catch)

### C. getAllSystemsForYear 데이터 dedup/sort
- findAll → [proj(year=2026, sysNmEn="UPIS", sysNm="유피스"), proj(2026,"KRAS","크라스"), proj(2026,"UPIS",중복), proj(2025,"GIS"...제외)]. → result 2건(UPIS/KRAS dedup), sysNmEn 정렬(KRAS<UPIS). sysNmEn null/빈 항목 제외.

### D. getBatchTargets COMPLETION + sysNmEn 필터
- COMPLETION: findByYearAndCompletionYnOrderByCityNmAscDistNmAsc(2026,"Y") → [proj(sysNmEn="KRAS"), proj("UPIS")]. sysNmEn="KRAS" 필터 → 1건. 200.

### E. buildBatchLetterData recipient 분기(batchGenerate 경유)
- orgNm=null, distNm="강진군" → recipient="강진군청"(letter.to). 
- orgNm=null, distNm=null, cityNm="전라남도" → recipient="전라남도청".
- (orgNm 분기는 기존 completion 성공 테스트가 이미 커버.)
- INTERIM letter body2 "기성을 신청" + attachList "기성검사원" 포함(A 테스트의 letter 캡처로 동시 검증).

---

## 3. 기능 / 비기능 요건

| ID | 내용 |
|----|------|
| FR-1 | batchGenerate INTERIM(inspector/detail_sheet/KRAS/paymentRate), getAllSystemsForYear dedup/sort, getBatchTargets COMPLETION/필터, buildBatchLetterData recipient/INTERIM body 박제. |
| FR-2 | EDIT 가드 보존 — 약화 0. |
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS. |
| NFR-2 | DocumentBatchController **69.7 → 90%+ LINE**. |
| NFR-3 | floor 실측 후 상향 검토(게인 작으면 유지). ratchet/PIT 불변. |
| NFR-4 | production 변경 0. 구현 후 codex 검증 PASS → 듀얼푸시. |

---

## 4. 영향 / 리스크

| 파일 | 유형 |
|------|------|
| `controller/DocumentBatchControllerTest.java` | 기존 파일 케이스 추가 |

production 변경 0.

| 리스크 | 수준 | 완화 |
|---|---|---|
| paymentAmount 계산 단언 brittle | 낮음 | 정수 곱셈(round)이라 결정적 — 정확값 단언. |
| SwProject LocalDate/Long 구성 | 낮음 | @Getter@Setter 세터로 구성. |
| KRAS 분기 trigger | 낮음 | sysNmEn="KRAS"(toUpperCase contains "KRAS"). |
| SecurityContext 누수 | 낮음 | 기존 @AfterEach clearContext(). |

---

## 5. 승인 요청

본 기획서(DocumentBatchController 커버리지) 사용자 최종승인을 요청합니다. 사전 codex는 미구현 글리치 예상으로 생략, **구현 후 codex 검증을 실질 게이트**로 사용. 의견: paymentRate 계산/KRAS 분기 단언 범위 충분성.
