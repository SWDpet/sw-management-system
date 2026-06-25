# [개발계획서] InterimReportExcelService Dead code 제거 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-25
- **기획서**: `docs/product-specs/deadcode-interim-report-excel.md` (codex APPROVE, 사용자 최종승인 완료)
- **상태**: ✅ 완료 (2026-06-25). 826→426줄(메서드 4 삭제 + import 2). compile×2/InterimReportExcelServiceTest 6/6/`./mvnw verify`(테스트+JaCoCo) green. codex 구현검증 PASS.

---

## 1. 작업 개요

`InterimReportExcelService.java`(826줄)의 미사용 private 메서드 4개(~399줄)와 unused import 2개를 제거. 단일 파일 수정, 동작·산출 바이트 불변. `deadcode-design-estimate-excel`(`91b24d9`) 과 동일 절차.

---

## 2. 삭제 대상 (현 라인 기준)

| # | 메서드 | 라인 |
|---|---|---|
| 1 | `createInterimCoverSheet` | 427–449 |
| 2 | `createInterimDetailSheet` | 450–626 |
| 3 | `createInterimSummarySheet` | 627–739 |
| 4 | `createInterimItemDetailSheet` | 740–825 |

- 삭제 라인: **426–825**(426행 `// --- 기성 Sheet 1: 표지 ---` 주석 ~ 825행 4번째 메서드 닫는 `}`). 826행 클래스 닫는 `}` 보존.
- 직전 live 헬퍼 `buildPeriodText` 는 424행에서 끝남(보존).

### 2-1. import 제거 2개

| import | 근거 |
|---|---|
| `import org.apache.poi.ss.util.CellRangeAddress;` | live(1~424) 사용 0, 전 32회 dead 전용. |
| `import static …ExcelStyleSupport.setCellValue;` | live 사용 0, dead 전용. |

**유지**: static `FONT`(live `applyFontSize` 사용), `str/toDouble/toLong/setStringDirect/setNumericDirect`, wildcard `usermodel.*`, `XSSFWorkbook`, `ClassPathResource`, `SwProject`, `Document/DocumentDetail/MessageResolver` 등.

---

## 3. 구현 순서 (T)

| ID | 단계 |
|----|------|
| T-1 | 가드: 4개 메서드명 `Grep` 으로 `src/` 전역 재스캔 → 각 정의부 1건만 확인. |
| T-2 | 라인 426–825 제거(라인-레인지 필터, UTF-8 no-BOM 저장). |
| T-3 | `Grep` 으로 4개 메서드명 0건 + live 메서드(generateInterimReport/populateHwInterimTemplate/헬퍼 6) 잔존 + 중괄호 균형 확인. |
| T-4 | `./mvnw -q -DskipTests compile` → 성공. |
| T-5 | import 2개 제거(`CellRangeAddress`, static `setCellValue`). 잔여 import body 사용 0 재grep 정리. |
| T-6 | `./mvnw -q -DskipTests compile` 재실행 → 성공. |
| T-7 | `./mvnw test -Dtest=InterimReportExcelServiceTest` → 전건 green. |
| T-8 | `./mvnw verify`(전체 순수 단위 + JaCoCo 게이트) green. |
| T-9 | JaCoCo csv 로 InterimReportExcelService·전역 커버리지 상승 확인. floor 유지(분모축소). |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | T-4/T-6 compile |
| NFR-2 | T-7 InterimReportExcelServiceTest green |
| NFR-3 | T-8 전체 test green |
| NFR-4 | git history |
| NFR-5 | T-8 게이트 + T-9 커버리지 상승 |

---

## 5. 롤백

단일 커밋. 문제 시 `git revert`. 작업 중 실패 시 Edit 되돌리고 원인 분석.

---

## 6. 커밋 (작업완료 후)

- 메시지: `refactor(deadcode): InterimReportExcelService 미사용 POI 직접생성 메서드 4개(~399줄) 제거 + stale import 2 정리 (beyond-A)`
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`.
- 기획서·개발계획서 상태 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
