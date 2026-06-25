# [기획서] DesignEstimateExcelService Dead code 제거 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-25
- **선행 맥락**: excel-service-split(§6-5, commit `9ea9b10`) 으로 `ExcelExportService` → `DesignEstimateExcelService` 분리 시 **구(POI 직접생성) 양식 메서드가 호출처를 잃은 채 잔류**.
- **트랙**: beyond-A 커버리지 스프린트(coverage-core-services)의 후속 — 단, 본 건은 "테스트 추가"가 아니라 **도달 불가 코드 삭제**(무손실)로 부채 제거 + 커버리지 분모 정상화.
- **상태**: ✅ 완료 (2026-06-25). codex 기획/개발계획/구현 전단계 APPROVE/PASS. DesignEstimateExcelService LINE 33.8%→95.6%, 전역 LINE 44.24%→46.17%.

---

## 1. 배경 / 목표

`DesignEstimateExcelService.java` 는 **1308줄 중 700줄(53%)이 호출처 0인 미사용 private 메서드**다. JaCoCo 가 이 클래스를 LINE 33.8%(549 미커버) 로 보고하는 주원인이 바로 이 dead code 다.

설계내역서 생성의 **현행 경로**는 전부 "원본 .xlsx 템플릿 로드 후 셀만 채움" 방식이다:

| estimateType | 진입 메서드(live) |
|---|---|
| TYPE_A(기본) | `generateFromTemplate` → `fillCoverSheet`/`fillGapjiSheet`/`fillSummarySheet` |
| TYPE_B | `generateFromTypeBTemplate` → `fillTypeBItemRows` |
| TYPE_C | `generateFromTypeCTemplate` |
| TYPE_D(SW전용) | `generateFromSwTemplate` |

반면 아래 8개 메서드는 **POI 로 시트를 직접 그리던 구버전 잔재**이며, 유일 public 진입점 `generateDesignEstimate` 어디서도 호출되지 않는다. 일부는 코드 자체에 `_OLD` 접미사·`@SuppressWarnings("unused")`·"미사용/대체됨" 주석으로 죽은 코드임을 이미 인정하고 있다.

**목표**: 런타임·빌드·산출 바이트에 영향 0 인 상태로 dead code 8개 메서드(~700줄) 삭제 → 유지보수 부담·오독 가능성 제거, 클래스 커버리지 분모를 살아있는 코드로 정상화.

---

## 2. 삭제 대상 (재검증 완료)

전체 소스(`src/`) 스캔 결과 8개 메서드 모두 **정의부 외 참조 0건**(private + 호출처 없음). 테스트 코드에서의 참조도 0건.

| # | 메서드 | 줄수 | 죽은 이유 |
|---|---|---|---|
| 1 | `createCoverSheet` | 36 | POI 직접생성 구(舊)표지 — 템플릿 방식으로 대체 |
| 2 | `createSummaryCoverSheet` | 93 | 구 갑지 직접생성 |
| 3 | `createSummaryTableSheet` | 166 | 구 총괄표 직접생성 |
| 4 | `createGradeSheet` | 127 | 구 유지보수등급측정표 직접생성 |
| 5 | `replacePlaceholder` | 13 | 미사용 placeholder 치환 헬퍼 |
| 6 | `createTypeDCoverSheet_OLD` | 30 | 코드에 `_OLD`+`@SuppressWarnings("unused")` 명시 |
| 7 | `createTypeDGapjiSheet` | 65 | "템플릿 방식으로 대체됨, 미사용" 주석 명시 |
| 8 | `createTypeDSummarySheet` | 170 | 〃 |
| | **합계** | **700** | |

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | `DesignEstimateExcelService.java` 에서 위 §2 의 8개 private 메서드를 본문·메서드 주석 포함 전부 삭제. |
| FR-2 | 삭제 전 최종 스캔: 각 메서드명에 대해 `src/` 전역 grep 결과가 **자기 정의부 1건뿐**임을 재확인. 예기치 못한 참조(리플렉션 문자열 호출 등) 발견 시 해당 메서드 삭제 중단 후 별도 판단. |
| FR-3 | 삭제로 인해 **import / 필드 / static import 가 미사용으로 전락하는지** 점검 후, 살아있는 코드에서 더는 참조되지 않는 것만 정리. (live 경로가 `ExcelStyleSupport.*`·`ExcelExportService.*` static·`CellRangeAddress`·`CellType` 등을 여전히 쓰는지 확인 — 쓰면 유지.) |
| FR-4 | live 경로(`generateFromTemplate`/`generateFromTypeBTemplate`/`generateFromTypeCTemplate`/`generateFromSwTemplate` 및 그 헬퍼)와 public API `generateDesignEstimate(Integer)` 시그니처·동작은 **불변**. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 삭제 후 `./mvnw -q -DskipTests compile` 성공 — 컴파일 오류 0. |
| NFR-2 | **회귀 없음**: 기존 단위테스트 `DesignEstimateExcelServiceTest`(TYPE_A 상세 셀 단언 + TYPE_B/C/D dispatch smoke + 섹션누락 예외) 전건 green. 본 삭제는 dead code 한정이라 산출 .xlsx 바이트가 분리 前後 동일해야 함. |
| NFR-3 | 전체 `./mvnw test`(DB 미설정·순수 단위) green 유지 — 다른 테스트 회귀 0. |
| NFR-4 | 삭제는 **비가역** — 복구는 git history(`git log --diff-filter=D` / revert)로만. 본 문서에 삭제 내역 명시. |
| NFR-5 | JaCoCo 게이트(`com/swmanager/**` LINE floor 0.42 / INSTR 0.35) 통과 — 분모 축소로 전역·클래스 커버리지 **상승**해야 정상(하락 시 원인 분석). |

---

## 5. 의사결정 / 우려사항

### 5-1. 리플렉션·문자열 기반 간접 호출 가능성 — ✅ 배제
- 8개 모두 `private`. Spring 빈 메서드 호출/AOP 대상도 아님(생성 헬퍼). `getMethod("createXxx")` 식 문자열 참조도 grep 0건.

### 5-2. "과거 양식 레퍼런스로 남겨둘 가치" — ✅ 삭제 우위
- 구 POI 직접생성 양식은 현행 템플릿(.xlsx) 방식으로 완전 대체됨. 직접생성 양식이 다시 필요하면 git history 에서 복원 가능. 죽은 채 방치하면 오히려 "어느 게 진짜 경로냐" 오독 비용만 발생.

### 5-3. 삭제 방식 — ✅ 확정
- `@Deprecated` 경유 단계적 폐기 **스킵**(이미 호출자 0이라 무의미). 파일 내 Edit 로 메서드 블록 단위 제거.

### 5-4. `vatSeparate` 등 live 경로의 미사용 파라미터 — ⏸ 본 스프린트 범위 외
- live 메서드 시그니처는 손대지 않는다(공개 API·내부 계약 보존 원칙). 파라미터 클린업은 별도 판단.

---

## 6. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Service (수정) | `src/main/java/com/swmanager/system/service/DesignEstimateExcelService.java` | private 메서드 8개(~700줄) + 미사용 import 제거 |
| Docs (수정) | 본 기획서 + 개발계획서 | 신규 |

**수정 1 파일. 엔티티/DB/API 계약/템플릿 리소스 변경 0. UI 변경 0(디자인팀 skip).**

---

## 7. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 미사용 판정 오판(실은 어딘가 호출) | 낮음 | FR-2 전역 grep 재확인 + 컴파일/테스트 게이트(NFR-1~3) |
| import/필드 과잉 제거로 컴파일 깨짐 | 중간 | 단계 진행: 메서드 삭제 → `compile` → import 정리 → `compile` 재확인 |
| 산출 바이트 변동(회귀) | 낮음 | dead code 한정이라 이론상 0. NFR-2 기존 테스트 green 으로 확인 |
| 양식 레퍼런스 소실 | 낮음 | git history 보존 (§5-2) |

---

## 8. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 승인 후 개발계획서를 작성합니다.
