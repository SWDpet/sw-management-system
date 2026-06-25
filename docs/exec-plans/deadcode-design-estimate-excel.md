# [개발계획서] DesignEstimateExcelService Dead code 제거 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-25
- **기획서**: `docs/product-specs/deadcode-design-estimate-excel.md` (codex APPROVE, 사용자 최종승인 완료)
- **상태**: ✅ 완료 (2026-06-25). 720줄 삭제(메서드 8 + import 3), 추가 0. compile×2/DesignEstimateExcelServiceTest 6/6/`./mvnw verify`(테스트+JaCoCo 게이트) 전부 green. codex 구현검증 PASS.

---

## 1. 작업 개요

`DesignEstimateExcelService.java` 의 미사용 private 메서드 8개(~700줄)와 그로 인해 unused 로 전락하는 import 3개를 제거. 단일 파일 수정, 동작·산출 바이트 불변.

---

## 2. 삭제 대상 (현 파일 라인 기준)

| # | 메서드 | 라인(현재) |
|---|---|---|
| 1 | `createCoverSheet` | 339–374 |
| 2 | `createSummaryCoverSheet` | 377–469 |
| 3 | `createSummaryTableSheet` | 472–637 |
| 4 | `createGradeSheet` | 640–766 |
| 5 | `replacePlaceholder` | 1021–1033 |
| 6 | `createTypeDCoverSheet_OLD` | 1039–1068 |
| 7 | `createTypeDGapjiSheet` | 1071–1135 |
| 8 | `createTypeDSummarySheet` | 1138–1307 |

> 각 메서드 **직전 주석 블록**(`// --- Sheet N: ... ---`, `// =====` 구분선, "미사용/대체됨" 설명)도 함께 제거하되, **live 메서드용 구분 주석은 보존**.

### 2-1. unused 전락 import 3개 제거 (실측 근거)

| import | 근거 |
|---|---|
| `import org.apache.poi.ss.util.CellRangeAddress;` (line 8) | 전체 25개 사용처가 모두 dead 메서드 내부. live 경로 미사용. |
| `import static …ExcelStyleSupport.FONT;` (line 20) | body 사용처 0건(현재도 stale). |
| `import static …ExcelStyleSupport.setCellValue;` (line 24) | static `setCellValue(row,col,val,style)` 는 dead 메서드 전용. live 코드의 `setCellValue` 는 전부 POI 멤버 `Cell.setCellValue(..)`(다른 메서드)이라 무관. |

**유지 import**: wildcard `org.apache.poi.ss.usermodel.*`(Cell/Row/Sheet/CellStyle 등 live 사용 — `CellType` 만 dead 였으나 wildcard 라 라인 변경 없음), `XSSFWorkbook`, `ClassPathResource`, static `str/toDouble/toLong/setStringDirect/setNumericDirect/normalizeRounddownUnit/toRoundDigits/roundLabel`(전부 live 사용 확인).

---

## 3. 구현 순서 (T)

| ID | 단계 |
|----|------|
| T-1 | 삭제 직전 가드: 8개 메서드명 `Grep` 으로 `src/` 전역 재스캔 → 각 정의부 1건만 존재 재확인(예기치 못한 참조 시 중단). |
| T-2 | `Edit` 로 8개 메서드 + 부속 주석 블록 제거(아래→위 순서로 라인 밀림 방지: #8 → #1). |
| T-3 | `./mvnw -q -DskipTests compile` → 컴파일 성공 확인. |
| T-4 | §2-1 import 3개 제거. 추가로 남은 import/static import 를 `DesignEstimateExcelService.java` 한정 재grep 하여 body 사용처 0 인 것 추가 정리(codex FR-3 주의 반영). |
| T-5 | `./mvnw -q -DskipTests compile` 재실행 → 성공 확인. |
| T-6 | `./mvnw test -Dtest=DesignEstimateExcelServiceTest` → 전건 green(TYPE_A 셀 단언·B/C/D dispatch·예외). |
| T-7 | (codex 위임) `./mvnw test` 전체 순수 단위 green 재확인(회귀 0). |
| T-8 | JaCoCo: 게이트(`./mvnw verify` 또는 jacoco:check) 통과 + 클래스/전역 커버리지 상승 확인. floor 상향은 별도 판단(이번엔 분모축소 효과라 floor 유지 가능). |

---

## 4. 검증 기준 (기획서 NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | T-3/T-5 compile 성공 |
| NFR-2 | T-6 `DesignEstimateExcelServiceTest` green (산출 바이트 회귀 0의 대리지표) |
| NFR-3 | T-7 전체 `./mvnw test` green |
| NFR-4 | 비가역 — git history 보존(별도 조치 불요) |
| NFR-5 | T-8 JaCoCo 게이트 통과 + 커버리지 상승 |

---

## 5. 롤백

단일 커밋. 문제 시 `git revert <commit>` 한 방. 작업 중 컴파일/테스트 실패 시 해당 Edit 되돌리고 원인 분석.

---

## 6. 커밋 (작업완료 후)

- 메시지(의도 중심): `refactor(deadcode): DesignEstimateExcelService 미사용 POI 직접생성 메서드 8개(~700줄) 제거 + stale import 3 정리 (beyond-A)`
- 듀얼푸시(`SWDpet` + `ukjin914`), author `ukjin_park@jungdouit.com`.
- 기획서·개발계획서 상태 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
