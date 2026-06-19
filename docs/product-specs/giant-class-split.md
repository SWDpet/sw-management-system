# 기획서 — 거대 클래스 분리 (giant-class-split, S4)

- **상태**: v0.2 (codex 검토 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-19 (v0.1) / v0.2 동일자 (codex: PUT 미존재·총량 ≈14·공용헬퍼 getAuth/isAdmin·extractSnapshotSpecs)
- **스프린트명**: `giant-class-split`
- **요청자**: 박욱진 (사용자) — "전 항목 A" 로드맵 S4
- **선행**: S1(보안 A)·S2(문서 B+)·S3(테스트 A, green 스위트 + JaCoCo 게이트) 완료. 본 건의 **안전망 = S3 green 스위트**.
- **성격**: **동작 보존 리팩토링** (기능·URL·DB·화면 변경 0). UI 키워드 없음 → 디자인팀 자문 대상 아님.

---

## 0. 한 줄 요약

코드품질 C → A 의 핵심 부채인 **거대 클래스**를 쪼갠다. `DocumentController`(2,183줄·엔드포인트 53개)에서 응집된 **점검내역서 클러스터(≈12 엔드포인트)** 를 신규 `InspectReportController` 로 분리하고, `ExcelExportService`(2,461줄)의 셀 스타일 중복을 팩토리로 추출한다. **외부 동작(URL·요청/응답·화면)은 100% 그대로.**

## 1. 배경 / 목적

- codex×Claude 평가(2026-06-19): 코드품질 **C**, 아키텍처 **B−**. 근거 = `DocumentController` 2,183줄(엔드포인트 53개, 사업문서+점검 혼재), `ExcelExportService` 2,461줄(셀 스타일 코드 메서드마다 중복).
- 한 클래스가 너무 많은 책임을 지면 변경 시 회귀 위험·인지부하가 크다. 책임을 가르면 변경 영향 범위가 좁아지고 테스트도 쉬워진다.
- 단, 동작을 바꾸지 않는 **순수 구조 개선**이어야 한다(기능 추가·수정 아님).

## 2. 범위 (In / Out)

**In**
- **S4-a (주): `DocumentController` → `InspectReportController` 분리.** 점검내역서 전용 엔드포인트 **≈14개**(codex 실측)를 신규 컨트롤러로 이동. 클래스 레벨 `@RequestMapping("/document")` 유지 → **모든 URL 경로 그대로**.
  - 이동 대상(현 라인 기준, codex 대조): `/api/inspect-report`(**POST**), `/api/inspect-report/{id}`(**GET, DELETE**), `/api/inspect-reports`, `/api/inspect-report/find`, `/api/inspect-report/previous-visits`, `/api/inspect/reset-all`, `/api/inspect-template`, `/api/inspect-snapshots`, `/api/inspect-pdf/{reportId}`, `/api/inspect-chart/preview`, `/inspect-detail/{reportId}`, `/inspect-preview/{reportId}`, `/api/infra-servers`(점검 컨텍스트). *(※ `PUT /api/inspect-report` 는 실제 매핑에 없음 — codex 정정)*
  - 함께 이동할 전용 의존성: `inspectReportService`, `inspectPdfService`, `inspectMetricChartService`, `metricSnapshotRepository`. 공용 의존성(`swProjectRepository`, `infraRepository`, `sigunguCodeRepository`, `sysMstRepository`, `logService`)은 양쪽에 각자 주입.
  - **함께 이동/복제할 private 헬퍼(codex 식별, R-3 핵심)**: `extractSnapshotSpecs()` = 점검 전용 → **이동**. `getAuth()`·`isAdmin()` = 공용 인증 헬퍼(사업문서도 사용) → InspectReportController 에 **동일 복제**(중복 최소 원칙상 1차는 복제, 공용 util 추출은 후속 검토).
- **S4-b: `ExcelExportService` 셀 스타일 팩토리 추출.** 메서드마다 반복되는 `CellStyle`/폰트("맑은 고딕") 생성을 private 헬퍼(예: `headerStyle()`/`dataStyle()`/`totalStyle()`)로 모은다. 출력물(엑셀 바이트) 동일.

**Out (이번 미포함 — 후속 분리)**
- `Map<String,Object>` 동적 조립 → DTO/Command 전환: 범위 넓고 위험 높아 **별도 스프린트**(`dto-migration`)로.
- `DocumentController` 의 사업문서·batch·lookup API 추가 분리: S4-a 안정화 후 2차 검토.
- 신규 기능·필드·검증 로직 변경 일절 없음.

## 3. 개발자 스토리

> 유지보수자로서, 점검내역서 로직을 고칠 때 2,183줄 컨트롤러 전체가 아니라 점검 전용 컨트롤러만 보고, 변경이 사업문서 기능에 영향 없음을 확신하고 싶다.

## 4. 기능 요구사항 (FR)

| ID | 요구사항 |
|---|---|
| FR-1 (불변식·핵심) | 분리 후에도 **모든 URL 경로·HTTP 메서드·요청 파라미터·응답 본문/상태코드가 100% 동일**. 프론트엔드(JS fetch/폼) **무수정**. 클래스 레벨 `@RequestMapping("/document")` 보존. |
| FR-2 | 이동한 엔드포인트의 **권한 가드·CSRF·로깅(LogService) 동작 보존**. (S1 가드 패턴 유지) |
| FR-3 | 이동 메서드가 쓰던 private 헬퍼·상수는 함께 이동하거나 공용 위치로. 컴파일·런타임 빈 주입 정상. |
| FR-4 | `ExcelExportService` 리팩토링 후 **생성 엑셀의 셀 값·서식·시트 구성 동일**(골든 비교). |
| FR-5 | 분리 후 두 컨트롤러 모두 매핑 충돌 없음(같은 path 중복 매핑 0) — 기동 시 Spring 매핑 등록 정상. |

## 5. 설계

### 5-1. InspectReportController (S4-a)
```
@Controller
@RequestMapping("/document")           // ← DocumentController 와 동일 prefix (URL 보존)
@RequiredArgsConstructor
public class InspectReportController {
    // 점검 전용 의존성 + 공용(swProject/infra/sigungu/sysMst/log)
    // 이동한 ≈12 엔드포인트
}
```
- DocumentController 에서 해당 메서드·전용 의존성 **삭제**(이동). 공용 의존성은 양쪽 유지.
- 점검 메서드 전용 private 헬퍼는 InspectReportController 로 이동. 사업문서와 공유하는 헬퍼가 있으면 → 우선 그대로 두고(중복 최소), 2차에서 공용 util 검토.

### 5-2. ExcelExportService (S4-b)
- 반복 `CellStyle` 생성 → `private CellStyle headerStyle(Workbook)`, `dataStyle`, `totalStyle`, `titleStyle` 등으로 추출. 폰트명 상수화(`private static final String FONT = "맑은 고딕"`).
- public 메서드 시그니처·반환 불변.

## 6. 비기능 요구사항 (NFR)

| ID | 요구사항 |
|---|---|
| NFR-1 (안전망) | 작업 전/후 `./mvnw test` (S3 green 스위트, 390 tests) **동일 통과**. 회귀 0. |
| NFR-2 | 분리 후 신규 `InspectReportController` 에 **MockMvc 또는 메서드 단위 회귀 테스트** 최소 1벌 추가(권한 가드 + 주요 GET). |
| NFR-3 | JaCoCo 게이트(LINE≥18%/INSTRUCTION≥14%, com/swmanager) **유지 통과**. |
| NFR-4 | `DocumentController` 라인 수 **유의하게 감소**(목표: 점검 분리분 ≈400~500줄 ↓). |
| NFR-5 | commit 원자 단위: S4-a, S4-b 분리 커밋. 각 커밋 후 빌드 green. |

## 7. 리스크 / 함정

| ID | 리스크 | 완화 |
|---|---|---|
| R-1 | 매핑 경로 누락/오타로 URL 변동 → 프론트 깨짐 | FR-1 불변식. 이동 전후 엔드포인트 목록 `grep` diff 로 1:1 대조. |
| R-2 | 같은 path 가 두 컨트롤러에 중복 등록 → 기동 실패 | 이동(복사 아님). 기동 스모크 + 매핑 충돌 확인. |
| R-3 | 점검 메서드의 private 헬퍼 의존 → 분리 시 컴파일 깨짐 | codex 식별 완료: `extractSnapshotSpecs()`(점검전용→이동), `getAuth()`/`isAdmin()`(공용→복제). 사업문서 전용 헬퍼 의존은 없음 확인. |
| R-4 | ExcelExportService 스타일 추출 중 미세 서식 변경 | 리팩토링 전 현재 출력으로 골든 픽스처 확보 → 후 비교(FR-4). |
| R-5 | `Map<String,Object>` 손대고 싶은 유혹 | 본 스프린트 Out. 건드리지 않음(별도 스프린트). |

## 8. 단계 (개발계획서에서 상세화)

1. **Step 0**: 이동 대상 엔드포인트·의존성·private 헬퍼 인벤토리 확정 + 현재 엔드포인트 목록 스냅샷(URL diff 기준).
2. **Step 1 (S4-a)**: `InspectReportController` 생성 → 점검 ≈12 엔드포인트·전용 의존성·헬퍼 이동. DocumentController 에서 제거.
3. **Step 2**: 기동 스모크 + URL diff 0 확인 + `./mvnw test` green + 신규 회귀 테스트(NFR-2).
4. **Step 3 (S4-b)**: ExcelExportService 골든 픽스처 → 스타일 팩토리 추출 → 골든 비교.
5. **Step 4**: codex 검증 → 커밋(S4-a, S4-b 분리) → 푸시.

## 9. 완료 기준 (DoD)

- DocumentController 점검 클러스터 → InspectReportController 이동, **URL diff 0**.
- `./mvnw test` 390 green 유지 + 신규 회귀 테스트 통과 + JaCoCo 게이트 통과.
- DocumentController 라인 수 감소(NFR-4). ExcelExport 출력 동일(FR-4).
- codex 검토 통과 + 사용자 승인.

---

### codex 검토 라인 (workflow)
> 요청 → **기획서(본 문서)** → codex 검토 → 사용자 최종승인 → 개발계획서 → codex 검토 → 승인 → 구현 → codex 검증 → 작업완료.
