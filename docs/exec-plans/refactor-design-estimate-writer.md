# [개발계획서] DesignEstimateExcelService TYPE별 생성 로직 분리 — DesignEstimateWriter

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/refactor-design-estimate-writer.md` (codex 실질검증 + 사용자 최종승인 2026-06-27).
- **상태**: ✅ **구현 완료(2026-06-27)**. DesignEstimateWriter(518줄) 신설, DesignEstimateExcelService **522→87(-435)**. TYPE_A/B/C/D 생성기+헬퍼 static 이동, dead param doc 제거, import 축소. `mvnw -o clean verify` BUILD SUCCESS(1315 green/52 skip, DesignEstimateExcelServiceTest 6 통합 통과, ratchet·JaCoCo·arch 게이트 통과). codex 6번째 오판 실질채택. dual-review→듀얼푸시.

---

## 1. 작업 개요

TYPE_A/B/C/D 순수 POI 생성기 + 헬퍼를 신규 `DesignEstimateWriter`(package-private static, service 패키지)로 이동. `DesignEstimateExcelService`는 generateDesignEstimate(데이터조회·dispatch)만 잔류. **순수 이동(동작 100% 동일)**. dead param doc 제거. 522 → 약 75줄.

---

## 2. 구현 순서 (S-n)

### S-1 DesignEstimateWriter 신설
- `service/DesignEstimateWriter.java` (final, private ctor).
- 이동(본문 바이트 동일), 전부 **package-private static**:
  - `generateFromTemplate`(⚠**dead param `Document doc` 제거** — 시그니처에서 빠짐), `fillCoverSheet`, `fillGapjiSheet`, `fillSummarySheet`, `simplifyMaintFormula`, `setCellKeepStyle`(String/double 2 오버로드), `clearDataRow`
  - `generateFromTypeBTemplate`, `fillTypeBItemRows`
  - `generateFromTypeCTemplate`
  - `generateFromSwTemplate`
- import 이관: POI(`org.apache.poi.ss.usermodel.*`, `XSSFWorkbook`), `ClassPathResource`, IO(`ByteArrayOutputStream`/`IOException`/`InputStream`), `List`/`Map`, static import(ExcelStyleSupport.str/toDouble/toLong/setStringDirect/setNumericDirect, ExcelExportService.normalizeRounddownUnit/toRoundDigits/roundLabel), `Document`/`DocumentDetail`(generateFromTemplate 내 미사용 시 불요 — doc 제거로 Document import 도 Writer 에서 불요 가능, 컴파일로 확인).
- 상호 호출(generateFromTemplate→fillCover/Gapji/Summary, fillSummary→setCellKeepStyle/clearDataRow/simplifyMaintFormula, generateFromTypeB→fillTypeBItemRows) 동일 클래스 내 유지.

### S-2 DesignEstimateExcelService 정리
- TYPE별 생성기·헬퍼 정의 제거. `generateDesignEstimate`(생성자·필드 포함)만 잔류.
- dispatch 치환:
  - `generateFromSwTemplate(...)` → `DesignEstimateWriter.generateFromSwTemplate(...)`
  - `generateFromTypeBTemplate(...)` → `DesignEstimateWriter.generateFromTypeBTemplate(...)`
  - `generateFromTypeCTemplate(...)` → `DesignEstimateWriter.generateFromTypeCTemplate(...)`
  - `generateFromTemplate(doc, projNm, ...)` → `DesignEstimateWriter.generateFromTemplate(projNm, ...)` (**doc 인자 제거**).
- 미사용 import 정리(POI/ClassPathResource/IO/static import 중 generateDesignEstimate 가 안 쓰는 것 제거). 잔류 import: Document/DocumentDetail(파싱), MessageResolver, toDouble(bidRate), normalizeRounddownUnit(rounddownUnit), List/Map, IOException(throws), Service.
- 정의 제거 후 미해결 호출은 컴파일 에러로 즉시 검출 → 전수 치환 보장.

### S-3 검증
- `./mvnw -o clean verify` BUILD SUCCESS. 기존 1315 green 유지.
- `DesignEstimateExcelServiceTest`(generateDesignEstimate 통합 경유) 통과 — 호출 대상 변경 영향 없음(public 진입점 동일).
- GiantClassRatchet green(DesignEstimateExcelService 축소, 신규 Writer < 2000줄). controller-repo/map-debt ratchet·JaCoCo·PIT 불변.
- LOC 확인: DesignEstimateExcelService 약 75, DesignEstimateWriter 약 440.

### S-4 (작업완료)
- dual-review → 합의 반영 → 커밋 `refactor(excel)` + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-3 compile + clean verify green |
| NFR-2 | S-3 DesignEstimateExcelServiceTest 통과(통합 경유, 단언 불변) |
| NFR-3 | S-3 ratchet·JaCoCo·PIT 불변, GiantClassRatchet 신규위반 0 |
| NFR-4 | S-4 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| dispatch 치환 누락 | 정의 제거 후 컴파일 에러로 전수 검출 |
| dead param doc 제거 시 호출부 불일치 | generateDesignEstimate L100 인자 동반 제거, 컴파일 확인 |
| static import 누락 | 컴파일 검출 |
| POI 산출 변경 | 바이트 동일 이동 + 기존 테스트 |
| 상호 호출 깨짐 | 생성기+헬퍼 함께 이동(동일 클래스 내 호출 유지) |

롤백: 단일 커밋 `git revert`. 순수 구조 이동.

---

## 5. 커밋

- `refactor(excel): DesignEstimateExcelService TYPE별 생성 로직 → DesignEstimateWriter 분리 (S4 거대클래스 #4)`.
- 듀얼푸시. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
