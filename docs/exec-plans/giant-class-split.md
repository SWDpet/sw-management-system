# 개발계획서 — 거대 클래스 분리 (giant-class-split, S4)

- **상태**: v2 (codex 검토 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-19 (v1) / v2 동일자 (codex ❌→반영: getCurrentUser 복제 필수·@Slf4j·sigungu/sysMst 과잉 제거·URL검증 MockMvc 보강)
- **기획서**: `docs/product-specs/giant-class-split.md` (v0.2, codex 승인·사용자 승인)
- **안전망**: S3 green 스위트(`./mvnw test` 390 tests) + JaCoCo 게이트. 각 Step 후 재실행.
- **원칙**: 동작 보존. URL·요청/응답·DB·화면 변경 0. 커밋은 Step 단위 원자.

---

## Step 0 — 인벤토리 확정 + 이동 전 스냅샷

**0-1. 이동 대상 14 엔드포인트 (DocumentController, 검증 완료):**

| # | 매핑 | 메서드 |
|---|---|---|
| 1 | `POST /api/inspect-report` | `saveInspectReport` |
| 2 | `GET /api/inspect-report/{id}` | `getInspectReport` |
| 3 | `GET /api/inspect-reports` | `listInspectReports` |
| 4 | `GET /api/inspect-report/find` | `findInspectReport` |
| 5 | `GET /api/inspect-report/previous-visits` | `getPreviousVisits` |
| 6 | `DELETE /api/inspect-report/{id}` | `deleteInspectReport` |
| 7 | `POST /api/inspect/reset-all` | `resetAllInspect` |
| 8 | `GET /api/inspect-template` | `getInspectTemplate` |
| 9 | `GET /api/infra-servers` | `getInfraServers` |
| 10 | `GET /api/inspect-snapshots` | `getInspectSnapshots` |
| 11 | `GET /api/inspect-pdf/{reportId}` | `downloadInspectPdf` |
| 12 | `GET /api/inspect-chart/preview` | `inspectChartPreview` |
| 13 | `GET /inspect-detail/{reportId}` | `inspectDetail` |
| 14 | `GET /inspect-preview/{reportId}` | `inspectPreview` |

**0-2. 헬퍼 (codex 재검증):**
- `extractSnapshotSpecs(Map)` (현 ~2003) — 점검 전용(`getInspectSnapshots` 만 사용) → **이동**.
- `isAdmin()`(~92), `getAuth()`(~99) — 공용. **+ `getAuth()`/`isAdmin()` 가 `getCurrentUser()`(~82) 를 호출** → `getCurrentUser()` 도 함께 **복제 필수**(누락 시 컴파일 실패). 3종 모두 InspectReportController 에 복제(DocumentController 원본 유지).

**0-3. 의존성 식별 (점검 메서드 본문 grep 실측, codex 정정):**
- 실제 사용: `inspectReportService`(10), `inspectPdfService`(2), `infraRepository`(2), `inspectMetricChartService`(1), `metricSnapshotRepository`(1), `swProjectRepository`(1), `logService`(1), **`log`(12) → `@Slf4j` 필요**.
- **주입하지 않음(미사용·과잉)**: ~~`sigunguCodeRepository`~~, ~~`sysMstRepository`~~ — 14 점검 메서드에서 참조 0.

**0-4. URL diff 기준 스냅샷:**
```
grep -nE '@(Get|Post|Put|Delete)Mapping' DocumentController.java > /tmp/before.txt
```
이동 후 (DocumentController + InspectReportController 합집합)이 before 와 **경로·메서드 집합 동일**해야 함.

**검증:** 없음(준비 단계).

---

## Step 1 (S4-a) — InspectReportController 생성 + 메서드 이동

**1-1.** 신규 `src/main/java/com/swmanager/system/controller/InspectReportController.java`:
```java
@Slf4j                                 // log 12회 사용 (codex)
@Controller
@RequestMapping("/document")          // DocumentController 와 동일 prefix → URL 보존
@RequiredArgsConstructor
public class InspectReportController {
    private final InspectReportService inspectReportService;
    private final InspectPdfService inspectPdfService;
    private final InspectMetricChartService inspectMetricChartService;
    private final InspectMetricSnapshotRepository metricSnapshotRepository;
    private final SwProjectRepository swProjectRepository;
    private final InfraRepository infraRepository;
    private final LogService logService;
    // sigunguCodeRepository/sysMstRepository 는 미사용 → 주입 안 함 (codex)
    // 14 엔드포인트 + extractSnapshotSpecs(이동) + getCurrentUser/isAdmin/getAuth(복제)
}
```
- 14 메서드를 DocumentController 에서 **잘라내어** 이 클래스로 이동(코드 동일).
- `extractSnapshotSpecs` **이동**. `getCurrentUser`+`isAdmin`+`getAuth` **복제**(getAuth/isAdmin→getCurrentUser 의존).
- import 정리(InspectReportDTO, CustomUserDetails 등).

**1-2.** DocumentController 에서 이동한 14 메서드 + `extractSnapshotSpecs` **삭제**. 더 이상 안 쓰는 전용 의존성 4개(`inspectReportService`/`inspectPdfService`/`inspectMetricChartService`/`metricSnapshotRepository`) 필드 제거(grep 으로 잔여 참조 0 확인 후).

**검증(FR-1/FR-5/R-2) — codex 보강(grep만으로 부족):**
- 컴파일 통과.
- 앱 기동 스모크: `DEV_HTTPS_ENABLED=false bash server-restart.sh` 기동 성공(ambiguous mapping 없음).
- **매핑 인벤토리 대조**: 기동 후 actuator `/actuator/mappings`(또는 RequestMappingHandlerMapping 로그)로 **full path + HTTP method + produces + 응답형(@ResponseBody)** 까지 before/after 동일 확인. (annotation grep 은 class prefix·produces·@ResponseBody 변화를 못 잡음)
- 점검 핵심 GET 몇 개 **실제 호출 스모크**(detail/preview 는 produces=text/html charset 보존 확인).
- `./mvnw test` 390 green 유지(NFR-1).

## Step 2 — 신규 회귀 테스트 (NFR-2)

`src/test/java/com/swmanager/system/controller/InspectReportControllerTest.java`:
- S1 패턴(컨트롤러 메서드 직접 호출 또는 MockMvc, DB 불필요 범위) 으로:
  - 권한 가드 동작(있는 엔드포인트) — getAuth/isAdmin 복제분 검증.
  - 주요 GET 1~2개의 정상 경로(서비스 mock).
- `./mvnw -Dtest=InspectReportControllerTest test` green.

## Step 3 (S4-b) — ExcelExportService 스타일 팩토리 추출

**3-1. 골든 픽스처 선확보(R-4):** 리팩토링 **전** `generatePerformanceReport(...)` 출력 바이트(또는 셀 값/스타일 요약)를 테스트 픽스처로 캡처.
**3-2.** 반복 `CellStyle`/폰트 생성 → `private CellStyle headerStyle/dataStyle/totalStyle/titleStyle(Workbook)`, `FONT="맑은 고딕"` 상수. public 시그니처 불변.
**3-3. 검증(FR-4):** 리팩토링 후 출력이 골든과 동일.

## Step 4 — codex 검증 + 커밋 + 푸시

- codex 검증(ambiguous mapping·URL diff·헬퍼 누락·골든 동일).
- 커밋 분리: `refactor: DocumentController→InspectReportController 점검 분리 (S4-a)`, `refactor(excel): 셀 스타일 팩토리 추출 (S4-b)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR-1 URL 보존 | Step 1 (URL diff 0 + 기동 스모크) |
| FR-2 가드/CSRF/로깅 보존 | Step 1 (코드 동일 이동) + Step 2 (가드 테스트) |
| FR-3 헬퍼/주입 정상 | Step 1 (컴파일 + 기동) |
| FR-4 엑셀 출력 동일 | Step 3 (골든 비교) |
| FR-5 매핑 충돌 0 | Step 1 (기동 스모크) |
| NFR-1 회귀 0 | Step 1·2·3 (`./mvnw test` 390 green) |
| NFR-2 신규 테스트 | Step 2 |
| NFR-3 JaCoCo 게이트 | Step 4 (`jacoco:check`) |
| NFR-4 라인 감소 | Step 1 (DocumentController wc -l before/after) |

## 롤백

- 각 Step 원자 커밋 → `git revert <sha>`.
- Step 1 은 순수 이동이므로 revert 시 원상복구(동작 동일).
