# [개발계획] HwpxExportService 커버리지 보강 + dead 제거 (beyond-A)

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/coverage-hwpx-export-service.md`
- **상태**: ✅ 구현 완료(2026-06-28). G1~G9·C1~C8 전부 구현·green. HwpxExportService LINE 97.79%(miss 15), 전역 LINE 80.24%/INSTR 65.61%, floor 0.77/0.63 래칫. codex 구현검증 PASS·dual-review 합의3 반영. 상세는 기획서 상태줄 참조.

---

## 0. 도달성 사전검증 (구현 전 확인 완료)
- `commence_body.hwpx`: `{{제품명}}` 이 **rowCnt=2 `<hp:tbl>` 내부** → U+0001 다중행 확장 도달. `{{인력1_업무}}` 가 **`<hp:p>` 내부** → U+0002 셀 줄바꿈 도달. `{{용역범위1}}`/`{{현대_생년}}`/`{{보안1_성명}}`/`{{참여1_자격}}`/`{{공정명}}` 존재. (`{{서약1_분야}}` 는 템플릿에 없음 → map 에는 채우되 section0.xml 단언 대상에서 제외.)
- `completion_full_v1.hwpx`: `ROW_MAINT_START`/`ROW_INSP_START` + `{{m.category}}`/`{{m.targetName}}`/`{{i.date}}`/`{{i.result}}`/`{{실제준공일}}`/`{{제출일}}` 존재 → applyRowMarkers 복제 도달.
- `UserRepository.findFirstByUsername(String)` → `Optional<User>`, `User.getFieldRole()`/`getCertificate()` 존재.

## 1. Part A — dead code 제거 (production)
- `HwpxExportService.java` line 993~995 `private byte[] processTemplate(String, Map<String,String>)` 오버로드 삭제. 호출처 0(grep 검증: generateHwpx→3-arg, 2-arg 본문→3-arg 뿐, 2-arg 자체 호출 없음). 3-arg live 경로 무변경. 빌드 컴파일로 미사용 회귀 확인.

## 2. Part B — 테스트 케이스 매트릭스 (테스트만)

### 2-1. `HwpxExportServiceGenerateTest` 확장
헬퍼 보강: `doc(...)` 가 섹션 데이터를 가변 주입하도록 `docWithDates(...)`/`sectionDoc(key,map)` 추가(기존 `doc()` 유지). project 에 날짜·금액 세팅.

| # | 테스트 | 픽스처 | section0.xml 단언 |
|---|---|---|---|
| G1 | letter contDt present | COMMENCE, contDt=2026-03-10 | 계약일자 "2026. 3. 10." contains |
| G2 | inspector 리치(분리일자+유효금액) | INTERIM, contDt/startDt/endDt present, contAmt=100000000, inspector={paymentAmount:"85000000", paymentRate:"50", interimYear:"2026",interimMonth:"6",interimDay:"15"} | "금85,000,000원", "금팔천오백만원", 계약년도 "2026년", 기성율 "50" contains |
| G3 | inspector interimDate 정규식 경로 | inspector={interimDate:"2026-06-15"} (분리 미입력) | 기성일자 "2026년    06월    15일" 류 contains(월/일 0패딩) |
| G4 | inspector 비숫자 금액→계약fallback | inspector={paymentAmount:"abc"} | 기성금액 = 계약금액("금100,000,000원") contains(NumberFormatException 무시 분기) |
| G5 | inspector interimDate find 실패 | inspector={interimDate:"미정"} | 기성일자 "미정"(raw) contains |
| G6 | completion_body KRAS 리치 | COMPLETION, contDt/startDt/endDt present, year=2025, completion={actualDate:"2026-05"} | 계약일자(한글)·착수일·준공일 "2026년 05월    일"·점검년도 "2025" contains |
| G7 | completion_body_upis 리치 | 날짜 present, completion={actualDate:"2026-05"} | 계약기간·준공예정일(한글)·준공일 contains |
| G8 | completion_full 리치+행마커 | COMPLETION, contDt/endDt present, completion={actualDate:"2026-05",completionDate:"2026-06"}, **target**={targets:[{category:"SW",productName:"KRAS시스템",specification:"v1.0",quantity:"1"},{category:"",productName:"UPIS시스템",specification:"v2",quantity:"2"}]}, **inspect_summary**={inspections:[{datetime:"2026-06-15T09:00",targetList:"서버A",result:"NORMAL"},{datetime:"2026-07-01",targetList:"서버B",result:"CHECK"},{datetime:"2026-08-01",targetList:"서버C",result:"OTHER"},{datetime:"2026",targetList:"x",result:"NORMAL"}]} | 두 제품명 모두 존재(복제행), "정상 (이상 없음)"·"점검"·"OTHER"(resultCode 3분기), "2026년 6월 15일"(datetime 파싱) contains. category "" → "SW" fallback 행 존재 |
| G9 | completion_full submitDate fallback | completion={actualDate:"2026-05"} (completionDate 미입력) | 제출일 = 실제준공일과 동일값 contains(submitDateK 빈→actualDateK 대체 분기) |

### 2-2. `HwpxExportServiceCommenceBodyTest` 확장
setUp 에 `userRepository = mock(UserRepository.class)` 추가 + `ReflectionTestUtils.setField(service,"userRepository",userRepository)`. 기본 lenient `findFirstByUsername(anyString())→Optional.empty()`.

| # | 테스트 | 픽스처 | 단언 |
|---|---|---|---|
| C1 | site_rep SSN century 20 + plan_personnel U+0002 | site_rep={ssn:**"900615-3000000"**(index7='3', 대시포함 14자)}, plan_personnel={list:[{name:"이사 박욱진",tasks:"분석, 설계, 개발",mobile:"010-1",career:"5년"}]} | 현대_생년 "2090"(century"20"+yy"90"), 생월 "6", 생일 "15" contains; 인력1_성명 "박욱진"(공백→마지막토큰); **인력1_업무 콤마→U+0002 분리**: "분석"/"설계"/"개발" 각각 별 hp:p 로 분리됐는지(section0.xml 에 셋 다 contains + 원본 "분석, 설계, 개발" 결합문자열 부재) |
| C2 | site_rep SSN century 19 + 6자미만 | (a) ssn=**"900615-1000000"**(index7='1')→"19"; (b) 별 케이스 ssn="123"(len<6)→생년/생월/생일 빈값 | 생년 "1990" contains / len<6 케이스 현대_생년 placeholder 가 "" 로 치환(원본 ssn "123" 이 생년칸에 안 들어감) |
| C3 | participants + userRepository present | participants={list:[{name:"이사 박욱진",position:"이사",ssn:"...",techGrade:"특급",tasks:"PM",cert:""}]}, userRepository.findFirstByUsername("박욱진")→User(fieldRole="유지보수책임기술자",certificate="정보처리기사") | 보안1_성명 "박욱진", 참여1_자격 "정보처리기사"(cert 빈→User.certificate 보강), verify findFirstByUsername("박욱진") |
| C4 | participants absent(폴백) 다인 | participants list 2명, userRepository empty | 참여1_분야 "유지보수책임기술자"(i==0 폴백)·참여2_분야 "유지보수참여기술자"(i>0) — map 경유(서약 placeholder 없으니 참여분야는 템플릿에 있으면 단언, 없으면 verify 로 대체) |
| C5 | 다중 PjtTarget U+0001 확장 | PjtTarget 2개(productName "KRAS시스템","UPIS시스템") | section0.xml 에 **두 제품명 모두** contains(다중행 복제), 원본 U+0001 sentinel 부재 |
| C6 | scopeText 멀티라인 + processName projNm 파생 | scopeText="범위A\n범위B\n범위C", projNm="2026년 강릉시 KRAS 유지관리 용역", schedule/PjtSchedule 빈값 | 용역범위1 "범위A"·2 "범위B"·3 "범위C" contains; 공정명 = projNm 정규식 파생값 contains |
| C7 | processName schedule 섹션 경유 | schedule={processName:"커스텀공정"} (PjtSchedule 빈값) | 공정명 "커스텀공정" contains |
| C8 | commData.date present | commence={date:"2026년 03월 절차"} | 제출일자 섹션값 우선 → "2026년 03월 절차" contains(startDt 파생 skip) |

> 단언 대상 placeholder 가 템플릿에 없으면(예 서약1_분야) section0.xml contains 대신 **verify(repo/userRepo 호출) + map 미검증 라인 도달**로 대체. 라인커버만 채우는 단언은 금지(기획 §3).

## 3. 검증 절차
1. `./mvnw -o clean verify` (clean 필수 — jacoco append) → BUILD SUCCESS, 신규 테스트 green.
2. `target/site/jacoco/jacoco.csv` 점표기 `^com\.swmanager` 합산 → 전역 LINE/INSTR delta 측정. `HwpxExportService.html` 메서드별 miss 재확인(buildReplacements/processTemplate/applyRowMarkers).
3. HwpxExportService LINE ≥92% 목표 미달 시 미커버 라인 추가 케이스 보강.
4. floor: 실측−2~2.5pp 버퍼, 게인 충분 시 1pp 래칫(pom.xml jacoco haltOnFailure rule).
5. 구현 후 codex 검증(NEEDS-FIX 시 단언 강화) → dual-review(codex+Opus) → 듀얼푸시.

## 4. 리스크/완화
- **R1 섹션키 불일치**: getSectionData 키(target→"targets", inspect_summary→"inspections", plan_personnel/participants→"list")는 production 정독 기반. 미도달 시 JaCoCo 로 검출 → 키 수정.
- **R2 SSN century 경계**: `siteRepSsn.charAt(7) >= '3'` → 13자리 주민번호 8번째 문자(index 7). 픽스처 ssn 길이 ≥8 확보. **합성 더미값만 사용**(실주민번호 금지, codex mojibake 무관 ASCII 숫자).
- **R3 U+0002 분리 단언**: 분리 결과는 hp:p 복제라 section0.xml 에 "분석"/"설계"/"개발" 모두 존재하나 인접 텍스트 구조는 템플릿 의존 → "각 토큰 contains + U+0002 char 부재"로 단언(정확 XML 구조 비단언).
- **R4 날짜 포맷 공백**: 기성일자/제출일자 등 전각·반각 공백 혼재 → contains 는 **공백 비의존 핵심 토큰**(년/월/일 숫자)만 검사하거나 정규식 매칭.
- production 회귀 0(Part A 미사용 삭제, Part B 테스트 추가).
