# [기획서] HwpxExportService XML 후처리 분리 — HwpxXmlSupport (S4 거대클래스 #2)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A S4 거대클래스 분리. DocumentController(5 Phase) 완료 후 #2 거대클래스 HwpxExportService(1311줄).
- **상태**: ✅ **완료(2026-06-27)**. XML 후처리 6종 + NL_SENTINEL → HwpxXmlSupport(package-private static). HwpxExportService 1311→1152. removeTcContaining 은 dead code(호출처 0)라 이동 대신 삭제(dual-review 합의). NL_SENTINEL 은 이스케이프 혼란 회피 위해 char-code 구성(런타임 SOH+NL+SOH 동일). HwpxXmlSupportTest 9(값잠금·충돌 포함). golden 4종 통과. 듀얼푸시.

---

## 1. 배경 / 목표

`HwpxExportService`(1311줄)는 저장소 #2 거대클래스. generateHwpx + 템플릿/치환 + **XML 후처리 엔진** + static 포맷 헬퍼가 혼재. 이 중 **순수 String 변환 XML 후처리 7종**은 필드/서비스 의존 0·외부 호출처 0·상호 호출만 → 분리 가장 안전.

| 메서드 | 시그니처 | 성격 |
|---|---|---|
| escapeXml | String→String | XML 이스케이프 |
| escapeXmlMultiline | String→String | 멀티라인 이스케이프 |
| expandMultilineParagraphs | String(xml)→String | 개행→문단 확장 |
| syncTableRowCounts | String(xml)→String | 표 행수 동기화 |
| countTopLevelTags | (String,String,String,String,String)→int | 최상위 태그 카운트 |
| reassignRowAddrs | String(tblBody)→String | 행 주소 재배정 |
| removeTcContaining | (String xml, String token)→String | 토큰 포함 셀 제거 (⚠**현재 미사용** — 호출처 0, codex) |

**확인**(grep): 7종 모두 `documentService`/repo/`this`/필드 참조 0(순수), 상호만 호출, **HwpxExportService 외 호출처 없음**. ⚠**NL_SENTINEL 상수(L1054)는 escapeXmlMultiline·expandMultilineParagraphs 공유 → 함께 이동 필수**(codex). KOREAN_NUMS/UNITS 는 convertToKoreanAmount(포맷헬퍼, 비대상)용 → 잔존. removeTcContaining 은 현재 호출처 없음 → 이동+직접테스트로 보존(기획서의 "processTemplate 호출" 표현 정정).

**목표**: 7종을 신규 `HwpxXmlSupport`(static util)로 이동. HwpxExportService 내부 호출은 `HwpxXmlSupport.xxx(...)` 로. **동작 100% 동일(순수 이동)**, baseline(거대클래스 LOC) 감소. **부수효과: golden(generateHwpx)으로만 간접 커버되던 XML 로직에 직접 단위테스트 확보.**

비목표: generateHwpx/buildReplacements/applyRowMarkers/processTemplate/static 포맷 헬퍼(convertToKoreanAmount 등, 외부 InterimReportExcelService 호출)·getSectionData(Document 의존)는 잔존.

---

## 2. 변경 설계

### 2-A 신규 `HwpxXmlSupport` (final, private ctor)
- `com.swmanager.system.service.HwpxXmlSupport` (ExcelStyleSupport 분리 선례와 동일 패키지).
- 7종 + **NL_SENTINEL 상수**를 **본문 바이트 동일** 이동. **package-private static**(codex — 테스트가 동일 service 패키지라 public 불필요, API 표면 최소). 상호 호출 동일 클래스 내 유지.

### 2-B HwpxExportService 정리
- 7종 메서드 정의 제거.
- 내부 호출처(applyRowMarkers 의 escapeXml, expandMultilineParagraphs 의 escapeXmlMultiline, syncTableRowCounts 의 countTopLevelTags/reassignRowAddrs, buildReplacements/processTemplate 의 expandMultilineParagraphs/syncTableRowCounts/removeTcContaining/escapeXml 등) → `HwpxXmlSupport.메서드(...)` 로 치환. import 추가.
- 결과: HwpxExportService 1311 → 약 1110줄.

> 순수 이동: XML 변환 로직·치환 결과 바이트 동일. generateHwpx 의 산출 HWPX 불변(golden 으로 보증).

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 7종의 입출력 동작이 이동 전과 동일(순수 String 변환). |
| FR-2 | generateHwpx 산출 HWPX(공문/착수계/기성/준공 등)가 이전과 동일 — 기존 golden(HwpxExportServiceGenerate/CommenceBody/Date/Test) 통과. |
| FR-3 | 신규 기능·로직·치환 규칙 변경 0. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. 기존 Hwpx golden 4종 불변 통과. |
| NFR-2 | `HwpxXmlSupportTest` 신규 — 7종 직접 단위테스트(알려진 XML 입력→기대 출력): escapeXml/Multiline 치환, expandMultilineParagraphs 개행처리, **syncTableRowCounts 는 rowCnt/rowAddr/nested table 케이스 최소 1**(codex)+countTopLevelTags+reassignRowAddrs, removeTcContaining 셀제거. |
| NFR-3 | 커버리지 비감소(직접테스트로 오히려 상승 예상). floor 유지. ratchet 불변(repo 접근 없음). |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 순수 7종만 추출** ✅: 필드/외부호출 0 → 무위험·무churn. applyRowMarkers/getSectionData(Document 의존)·static 포맷 헬퍼(외부 호출 있음)는 별도 Phase/잔존.
- **5-2 static util(HwpxXmlSupport)** ✅: 무상태 순수함수 → 빈/주입 불필요. ExcelStyleSupport 선례.
- **5-3 private→public static** ✅: util 노출 필요. 외부 호출처 0 이라 신규 API 표면 최소(테스트만 사용).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Service(신규) | `service/HwpxXmlSupport.java` | 신규 util |
| Service(수정) | `service/HwpxExportService.java` | 7종 제거 + 호출 치환 |
| Test(신규) | `service/HwpxXmlSupportTest.java` | 직접 단위테스트 |

UI/DB/API 변경 0 → 디자인·DB팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 내부 호출 치환 누락(컴파일) | 낮음 | 컴파일 + 정의 제거 후 미해결 호출 즉시 컴파일 에러 |
| XML 동작 미세 변경 | 낮음 | 바이트 동일 이동 + 기존 golden 4종(generateHwpx) 통과로 보증 |
| 상호 호출 깨짐 | 낮음 | 7종 함께 이동(동일 클래스 내 상호 호출 유지) |

---

## 7. 승인 요청

본 기획서(HwpxXmlSupport 분리)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
