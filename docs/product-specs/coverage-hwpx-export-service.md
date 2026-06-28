# [기획서] HwpxExportService generateHwpx 데이터분기·행마커 커버리지 보강 + dead 제거 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분. 비-license 최대 미커버 핫스팟(LINE miss 185, 72.8%). 기존 `HwpxExportServiceGenerateTest`/`HwpxExportServiceCommenceBodyTest` 확장형 + dead code 제거(무손실 헌장).
- **상태**: ✅ 구현 완료(2026-06-28). Part A(dead 2-arg processTemplate 삭제)+Part B(GenerateTest +9 G1~G9·CommenceBodyTest +8 C1~C8). **HwpxExportService LINE 72.8%→97.79%(665/680, miss 185→15)·INSTR 94.53%, 전역 LINE 78.82→80.24%(80% 돌파)·INSTR 64.28→65.61%.** floor LINE 0.75→0.77·INSTR 0.61→0.63 래칫. `mvnw -o clean verify` 1474 green. codex 기획/개발계획 NEEDS-FIX(구현전 글리치, 실질 채택)·구현검증 PASS. dual-review(codex2/Opus9) 합의4 중 3 반영(ArrayList 미사용 import 제거·sec() 홀수인자 fail-fast·C2b 양성단언 contains("123") 보강), 합의1(GenerateTest DocumentDetail import 누락)은 오탐(line6 기존 존재·build green)으로 미반영, 분쟁7 전건 상호 refute(private 삭제 안전·build가 컴파일 증거).

---

## 1. 배경 / 목표

`HwpxExportService`(LINE 496/681 = 72.8%, miss **185**)는 license 계열을 제외하면 **단일 최대 미커버 핫스팟**이다. 미커버는 외부 의존(generatePdf 의 msedge 등)이 아니라 **전부 단위테스트로 도달 가능한 순수 XML 문자열 치환 로직**이며, 미커버 사유는 "테스트 부족"이 아니라 **기존 테스트가 빈/최소 픽스처(날짜·섹션데이터 비움)만 줘서 '데이터 채워진 분기'가 전부 else 경로로 빠지기 때문**이다.

JaCoCo 메서드별 miss(`HwpxExportService.html`):

| 메서드 | LINE miss | 미커버 정체 |
|---|---|---|
| `buildReplacements(Document,String)` | 101 | inspector/completion_*/commence_body 의 **데이터 present 분기**(날짜·금액·섹션 list) |
| `processTemplate(String,Map,Document)` | 50 | `U+0001` 다중행 표 확장 정규식 블록 + `U+0002` 셀 줄바꿈 분리 블록 |
| `applyRowMarkers(String,Document)` | 32 | ROW_MAINT/ROW_INSP 마커 행 복제 루프(target·inspect_summary) |
| `processTemplate(String,Map)` 2-arg | 1 | **호출처 0 dead code** |
| 기타(getStr 등) | 1 | 방어 분기 |

목표: 빈 픽스처 대신 **날짜+실섹션데이터를 채운 픽스처**로 데이터 분기를 탐하고, **생성된 section0.xml 내용 단언**(zip 유효성만이 아니라 실제 치환값/복제행 검증)으로 위장통과를 차단한다. dead 2-arg 오버로드는 삭제한다.

## 2. 범위

### Part A — dead code 제거 (production, 무손실)
- `private byte[] processTemplate(String templatePath, Map<String,String> replacements)`(line 993~995) 삭제. **호출처 0 확인**(grep: 유일 호출은 generateHwpx→3-arg(181), 2-arg 본문→3-arg(994). 2-arg 오버로드 자체를 호출하는 코드 없음). live 경로(generateHwpx→3-arg) 무변경.

### Part B — 테스트 보강 (테스트만, production 무변경)
기존 2개 테스트 클래스 확장. **모두 documentService(+commence_body 는 pjt*Repository·userRepository) mock + classpath 실 템플릿 + 결과 zip 재오픈→Contents/section0.xml 문자열 단언.** 운영DB 무관.

**B-1. `HwpxExportServiceGenerateTest` 확장 — letter/inspector/completion 데이터분기 + 행마커**
- `letter` contDt present: 계약일자 `formatDate` 치환 단언(현재 null→else 만 커버).
- `inspector` 리치: project 날짜 3종(contDt/startDt/endDt) present(계약년도/착수년/준공년 분기) + inspector 섹션
  - `paymentAmount` 유효("85000000")→기성금액 치환 / `paymentAmount` 비숫자("abc")→NumberFormatException 무시→계약금액 fallback / 미입력→계약금액 fallback
  - 기성일자 **분리입력 경로**(interimYear/Month/Day) + **interimDate 정규식 경로**(find 성공: "2026-06-15" / find 실패: "미정"→raw)
  - `paymentRate` 치환.
- `completion_body`(KRAS) 리치: contDt/endDt/startDt present + completion.actualDate("2026-06") → 준공일/계약일자/착수일/점검년도 치환 단언.
- `completion_body_upis` 리치: 날짜 present + actualDate.
- `completion_full` 리치 **+ 행마커**: contDt/endDt present + completion(actualDate+completionDate) → 실제준공일/제출일/준공예정일 + `target` 섹션(targets ≥2: category SW/빈→SW fallback, productName/spec/qty) + `inspect_summary` 섹션(inspections: result NORMAL/CHECK/그외 3종 + datetime "2026-06-15..." 파싱 성공 + 짧은 dt 파싱 실패) → **applyRowMarkers 복제 루프·resultCode 라벨 3분기·날짜파싱 try/catch** 커버. section0.xml 에 제품명/점검결과라벨 행이 실제 복제됐는지 단언.
- `completion_full` submitDate fallback: completionDate 미입력→actualDate 로 대체되는 분기.

**B-2. `HwpxExportServiceCommenceBodyTest` 확장 — commence_body 심화 필드 + 다중행/줄바꿈 마커**
- setUp 에 `userRepository` mock 추가(현재 미와이어 → @Autowired(required=false) 필드 주입).
- **site_rep SSN**: 유효 13자리(뒷자리 '3'≥ → century "20" / '1' → "19") + 6자 미만(else 빈값) 2케이스 → 생년/생월/생일 추출 분기.
- **plan_personnel**: list 1~3명, name "이사 박욱진"(공백→마지막 토큰) + tasks "A, B, C"(콤마→`U+0002`) → 인력N_* 치환 + **processTemplate `U+0002` 셀 줄바꿈 분리 블록** 커버. section0.xml 에 분리 결과 단언.
- **participants**: list 1~3명 + userRepository.findFirstByUsername present(fieldRole·certificate 보강) / absent(폴백 i==0 책임 vs i>0 참여) → 보안N_*/참여N_*/서약N_* 치환.
- **다중 PjtTarget(≥2)**: 제품명 `String.join("U+0001",...)` → commence_body.hwpx 의 `{{제품명}}` tbl 에서 **processTemplate `U+0001` 다중행 표 확장 정규식 블록**(rowCnt 갱신·rowAddr 재할당) 커버. 복제된 두 제품명이 section0.xml 에 모두 존재 단언.
- **scopeText 멀티라인**(3줄) → 용역범위1/2/3 분할 단언.
- **processName 파생**: schedule 섹션 processName 경유 / projNm 정규식 파생("2026년 강릉시 …용역"→middle) / sysNm fallback — 분기별 케이스.
- **commData.date present**(제출일자 섹션값 우선, project.startDt 파생 skip).

## 3. 검증 방식 (위장통과 차단)
- zip 유효성(PK 매직) **+ section0.xml 실치환값 단언**. 행마커/다중행은 **복제된 행 수 또는 복제값 존재**를 단언(라인커버만 X).
- 날짜/금액 분기는 **포맷된 결과 문자열**(예 "2026. 6. 15.", "금85,000,000원")을 contains 단언.
- repo/userRepository 호출은 `verify`(인자 eq)로 잘못된 조회 회귀 방어.

## 4. 요건
- **FR-1**: buildReplacements 의 letter/inspector/completion_body/completion_body_upis/completion_full 데이터 present 분기 박제.
- **FR-2**: applyRowMarkers 의 target/inspect_summary 복제 루프 + resultCode 3분기 + datetime 파싱 성공/실패 박제(completion_full).
- **FR-3**: processTemplate 의 `U+0001` 다중행 확장·`U+0002` 셀 줄바꿈 블록 박제(commence_body 다중 target / plan_personnel 콤마 tasks).
- **FR-4**: commence_body 심화 필드(site_rep SSN century·plan_personnel·participants+userRepository·scopeText·processName 파생) 박제.
- **FR-5**(Part A): dead 2-arg `processTemplate` 삭제, live 경로 무변경.
- **NFR**: production 변경 = Part A 삭제 1건뿐(테스트는 무변경 production 대상). `mvnw -o clean verify` SUCCESS, HwpxExportService LINE 72.8%→**목표 ≥92%**(잔여 = linesegarray replaceAll 등 방어/도달난), 전역 LINE/INSTR 상향, JaCoCo floor 유지(게인에 따라 1pp 래칫 검토), 구현 후 codex PASS + dual-review → 듀얼푸시.

## 5. 영향 / 리스크
- 변경 파일: `service/HwpxExportService.java`(2-arg 삭제), `service/HwpxExportServiceGenerateTest.java`·`HwpxExportServiceCommenceBodyTest.java`(케이스 추가). 신규 클래스 없음(확장형).
- **리스크 R1**: 템플릿 placeholder/마커가 실제 .hwpx 에 존재해야 분기 도달. **확인 완료** — ROW_MAINT/ROW_INSP 는 `completion_full_v1.hwpx` 에만, `{{제품명}}` 은 `commence_body.hwpx` 에 존재(unzip grep 검증).
- **리스크 R2**: 섹션 list 구조(`{"list":[...]}`/`{"targets":[...]}`/`{"inspections":[...]}`)·키명은 production buildReplacements/applyRowMarkers 가 읽는 그대로 사용(코드 정독 기반). 불일치 시 분기 미도달 → JaCoCo 재측정으로 검출.
- **리스크 R3**: dead 2-arg 삭제가 reflection/외부 호출 대상일 가능성 → private 메서드라 무관, grep 0 확인.
- production 동작 회귀 0(Part A 는 미사용 오버로드 제거, Part B 는 테스트 추가).
