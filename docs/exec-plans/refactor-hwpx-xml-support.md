# [개발계획서] HwpxExportService XML 후처리 분리 — HwpxXmlSupport

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/refactor-hwpx-xml-support.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27)**. HwpxExportService 1311→1152(-159), 호출처 9개 HwpxXmlSupport. 한정. removeTcContaining dead code 삭제(7→6종, dual-review 합의). NL_SENTINEL char-code 구성(`String.valueOf((char)1)+"NL"+(char)1`=SOH+NL+SOH, 이스케이프 회피·값잠금 테스트). HwpxXmlSupport LINE 93%+. clean verify green(golden 4종 통과), 전역 LINE 72.44%·INSTR 59.08%. ratchet 불변.

---

## 1. 작업 개요

순수 XML 후처리 7종 + NL_SENTINEL 을 신규 `HwpxXmlSupport`(package-private static)로 이동. HwpxExportService 내부 호출 치환. 순수 이동 + HwpxXmlSupportTest 신규. 기존 generateHwpx golden 4종으로 동작 보증.

---

## 2. 구현 순서 (S-n)

### S-1 HwpxXmlSupport 신설
- `service/HwpxXmlSupport.java` (final, private ctor).
- 이동(본문 바이트 동일): escapeXml·escapeXmlMultiline·expandMultilineParagraphs·syncTableRowCounts·countTopLevelTags·reassignRowAddrs·removeTcContaining + `NL_SENTINEL` 상수.
- 접근제어: `static`(package-private, modifier 없음) — 현 private→제거. 동일 package(com.swmanager.system.service) 라 HwpxExportService·테스트 접근 가능.

### S-2 HwpxExportService 정리
- 7종 정의 + NL_SENTINEL 제거.
- 내부 호출 치환(`메서드(` → `HwpxXmlSupport.메서드(`) — **실제 직접 호출처는 거의 applyRowMarkers 내부**(codex): escapeXml ×5(894/895/897/933/935), escapeXmlMultiline ×2(896/934), expandMultilineParagraphs·syncTableRowCounts(942/944). countTopLevelTags/reassignRowAddrs 는 syncTableRowCounts 가 호출 → **HwpxXmlSupport 내부 호출로 함께 이동**(치환 불필요). removeTcContaining 호출처 0. ⚠processTemplate 은 escapeXml/sync 를 직접 호출하지 않고 applyRowMarkers 만 호출(기획 표현 정정).
- import 불필요(동일 package). 정의 제거 후 미해결 호출은 컴파일 에러로 즉시 검출 → 전수 치환 보장.

### S-3 테스트
- `service/HwpxXmlSupportTest.java`(동일 package):
  - escapeXml: **`&`,`<`,`>` 만 치환**(codex — 현 구현은 쌍/홑따옴표 미치환 → 기대값도 미치환으로).
  - escapeXmlMultiline: 개행 → NL_SENTINEL 보존(+ escapeXml).
  - expandMultilineParagraphs: NL_SENTINEL 포함 문단 → 다중 문단/공백 처리(파라그래프 마커 있는 입력/없는 fallback).
  - syncTableRowCounts: rowCnt 속성 + rowAddr 재배정 + nested table 1케이스 → 행수 동기화 결과.
  - countTopLevelTags: 중첩 포함 최상위 카운트.
  - reassignRowAddrs: rowAddr 0,1,2 재배정.
  - removeTcContaining: 토큰 포함 tc 제거 / 미포함 보존.

### S-4 검증
- `./mvnw -o clean verify` — compile + 전체 + **기존 Hwpx 회귀 스모크(Generate/CommenceBody/Date/Test) 통과**(zip magic·section0 contains 수준의 generateHwpx 회귀 안전망, codex — 바이트동일 보증은 아님. 순수 이동+컴파일 누락검출로 실무 리스크 낮음) + JaCoCo 게이트 green.
- HwpxExportService LOC 확인(약 1110).

### S-5 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-4 compile + clean verify + Hwpx golden 4종 green |
| NFR-2 | S-3 HwpxXmlSupportTest green(7종, syncTableRowCounts rowCnt/rowAddr/nested 포함) |
| NFR-3 | S-4 커버리지 비감소(직접테스트 상승)·ratchet 불변 |
| NFR-4 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 내부 호출 치환 누락 | 정의 제거 후 컴파일 에러로 전수 검출 |
| NL_SENTINEL 미이동 | S-1 동반 이동(escapeXmlMultiline/expandMultilineParagraphs 컴파일로 확인) |
| XML 동작 변경 | 바이트 동일 이동 + golden 4종 |
| package-private 접근 | HwpxXmlSupport/Service/Test 모두 동일 package |

롤백: 단일 커밋 `git revert`. 순수 구조 이동.

---

## 5. 커밋

- `refactor(hwpx): HwpxExportService XML 후처리 → HwpxXmlSupport 분리 (S4 거대클래스 #2)`.
- 듀얼푸시. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
