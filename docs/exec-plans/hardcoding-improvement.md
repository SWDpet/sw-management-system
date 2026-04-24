---
tags: [dev-plan, sprint, refactor, hardcoding]
sprint: "refactor-01-hardcoding"
status: draft-v2
created: "2026-04-20"
---

# [개발계획서] 소스 하드코딩 개선 — 리팩터 스프린트 #1

- **작성팀**: 개발팀
- **작성일**: 2026-04-20
- **근거 기획서**: [[hardcoding-improvement]] (v6 — 사용자 최종승인 완료)
- **Pre-flight 첨부**: [[hardcoding-preflight-result]] (2026-04-20 실행)
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 5건 반영)
1. T1 / NFR-4 grep 정규식에서 `\|` 이스케이프 제거 → 리터럴 `|` 사용
2. 컴파일 커맨드를 `./mvnw clean compile`로 통일 (NFR-1과 일치)
3. NFR-9 시나리오 ⑤(오류 핸들링)에 **서비스 계층 테스트** 1건 추가 (`DocumentStatusBindingServiceTest`)
4. Step 1에 **JPQL/@Query 리터럴 스캔 결과(0건) 확인 명령** 추가 (FR-A4 근거)
5. `PerformanceService` 전체 파일 제외 대신 **`WorkPlan` 관련 라인만 제외하는 이중 검증** 전략으로 변경

---

## 0. 전제 / 범위 확정

### 0-1. 스프린트 대상 (기획서 v6 §2)
- **#1-A Enum 전환** (본 문서가 다루는 범위): `DocumentStatus`, `DocumentType`
- **#1-B 메시지·설정 외부화**: #1-A 머지·안정화 후 별도 PR에서 진행 (아래 §7에 스텁 정의만 포함)

### 0-2. 명명 조정 — `WorkPlanType` → **`DocumentType`** (개발팀 제안)
기획서 v6의 Enum 명 `WorkPlanType`이 실제 매핑 필드(`tb_document.doc_type`) 및 Java 필드(`Document.docType`)와 이름이 맞지 않아 혼동 우려. Pre-flight 결과도 해당 컬럼 대상. `WorkPlan` 엔티티의 `planType` 필드는 다른 값 세트(`CONTRACT/PRE_CONTACT/SETTLE/COMPLETE` 등)를 가져 **본 스프린트 범위 외**.

→ **개발팀 제안: Enum 명을 `DocumentType`으로 변경**. 기획서 v6의 모든 "WorkPlanType" 표현은 `DocumentType`으로 읽는다. 이 변경은 기획 의도(Document.docType 매핑)를 정확히 반영하며 구현/테스트 혼동을 제거한다. codex 검토 후 수용되면 기획서 v7 패치 없이 본 개발계획서의 §0-2 각주만으로 명명 확정.

### 0-3. 대상 코드 실측 (Explore agent 조사 결과, 2026-04-20)

| 항목 | 건수 | 비고 |
|------|------|------|
| `"DRAFT"`, `"COMPLETED"` 리터럴 | 14건 | Document/InspectReport 계열 |
| `"COMMENCE"~"PATCH"` 리터럴 | 10+건 | DocumentDTO 라벨·DocumentController 라우팅 |
| Entity 수정 대상 | 2개 | `Document`, `InspectReport` |
| DTO 수정 대상 | 2개 | `DocumentDTO`, `InspectReportDTO` |
| Service 수정 대상 | 3개 | `DocumentService`, `InspectReportService`, `PerformanceService`(경계 — §0-4 참조) |
| Controller 수정 대상 | 1개 | `DocumentController` |
| Repository | 0~1개 | `DocumentRepository.java:101`은 `APPROVED`로 범위 외. 기타는 파라미터 바인딩 완료 |

### 0-4. 범위 경계 — `PerformanceService`와 `WorkPlan` (v2 보완)
`PerformanceService.java:59-68`은 `WorkPlan.planType`/`WorkPlan.status` 필드를 다루며 `"INSPECT"`, `"COMPLETED"` 리터럴과 우연히 겹침. **`WorkPlan`의 두 필드는 본 스프린트 범위 외**.

**검증 전략 (v2 변경 — codex 피드백 반영)**:
- **전체 파일 제외는 금지** (본 스프린트 대상 문자열까지 가릴 위험).
- 대신 **2단계 검증**:
  1. 전체 파일 스캔(NFR-4): `PerformanceService.java`도 **포함**하여 실행.
  2. `PerformanceService.java` 히트가 나오면 각 라인이 **`WorkPlan`/`getPlanType`/`getStatus` 문맥인지 수동 확인**. 문맥이 맞으면 ✅ 허용, 아니면 ❌ 수정 대상.
- 사전 스캔(Step 1)에서 `rg -n --type java 'WorkPlan.*"(DRAFT|COMPLETED|INSPECT)"|"(DRAFT|COMPLETED|INSPECT)".*planType|"(DRAFT|COMPLETED|INSPECT)".*WorkPlan' src/main/java/com/swmanager/system/service/PerformanceService.java` 로 허용 예상 라인을 미리 화이트리스트화.
- `PerformanceService`가 `Document`/`InspectReport`의 status를 다루는 지점이 발견되면 Enum으로 치환 (실측 결과 현재는 WorkPlan만 해당).

### 0-5. 기획서 §5-6-2 책임 경계표 적용
Pre-flight 결과 대상 2개 Enum 관련 **별칭/공백/대소문자 이슈 0건** → `AttributeConverter` 불필요. `@Enumerated(EnumType.STRING)` 직결.
- `ALIASES` 맵은 빈 맵(`Map.of()`)으로 시작 (§5-7-2 원칙 유지, 향후 외부 API 입력에서 별칭 필요 시 추가).

---

## 1. 작업 순서 (#1-A)

### Step 1 — 사전 스캔 (변경 전 스냅샷)
```bash
# 1-1. Pre-flight 재실행 (값 변동 감지)
JAR=~/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
DB_PASSWORD='<비밀번호>' java -cp "$JAR" docs/dev-plans/hardcoding-preflight-runner.java
# → 2026-04-20 결과와 diff 비교. 값 추가 없으면 통과.

# 1-2. 대상 리터럴 현재 개수 기록 (변경 후와 비교용)
rg -n --type java '"(DRAFT|COMPLETED)"' src/main/java
rg -n --type java '"(COMMENCE|INTERIM|COMPLETION|INSPECT|FAULT|SUPPORT|INSTALL|PATCH)"' src/main/java

# 1-3. JPQL/@Query 리터럴 스캔 (FR-A4 근거)
rg -n --type java "@Query" src/main/java/com/swmanager/system/repository/ | rg "'(DRAFT|COMPLETED|INSPECT|COMMENCE|COMPLETION|INTERIM|FAULT|SUPPORT|INSTALL|PATCH)'"
# 기대 결과: 0건 (Explore 조사에서 이미 파라미터 바인딩으로 통일 확인됨).
#   - DocumentRepository.java:36, 51 → :status 파라미터 바인딩 사용
#   - DocumentRepository.java:101의 'APPROVED'는 WorkPlan 계열로 본 스프린트 범위 외
# → 발견 시 해당 라인을 Step 4-6에 추가하여 파라미터 바인딩 전환.

# 1-4. PerformanceService 화이트리스트 사전 점검 (§0-4 2단계 검증)
rg -n --type java 'WorkPlan|planType|getPlanType' src/main/java/com/swmanager/system/service/PerformanceService.java
# → WorkPlan 문맥 라인 식별. NFR-4 T1 실행 시 이 라인은 허용.
```

### Step 2 — Enum 및 Converter 인프라 생성
**2-1. Enum 2종 신규 생성** (`@JsonCreator` 단독, `ALIASES` 빈 맵):
- `src/main/java/com/swmanager/system/constant/enums/DocumentStatus.java`
  - 값: `DRAFT`, `COMPLETED`
  - `label()`: DRAFT→"작성중", COMPLETED→"작성완료"
  - `color()`: DRAFT→"#858796", COMPLETED→"#1cc88a" (DocumentDTO에서 이관)
- `src/main/java/com/swmanager/system/constant/enums/DocumentType.java`
  - 값: `COMMENCE, INTERIM, COMPLETION, INSPECT, FAULT, SUPPORT, INSTALL, PATCH`
  - `label()`: 기존 `DocumentDTO.getDocTypeLabel()` 매핑 이관
  - `templateName()`: 기존 `DocumentController` 템플릿 라우팅 이관 (예: INSPECT → "doc-inspect")

**2-2. `StringToEnumConverterFactory` 등록**:
- `src/main/java/com/swmanager/system/config/EnumConversionConfig.java` 신규 — `WebMvcConfigurer.addFormatters(FormatterRegistry registry)`에 `registry.addConverterFactory(new StringToEnumConverterFactory())` 등록
- `ConverterFactory`는 `trim()` + `toUpperCase()` 후 `valueOf`만 수행. `ALIASES` 맵 조회 금지 (기획서 §5-7-2).

**2-3. `EnumErrorResponseFactory` 신규** (기획서 §5-7-4):
- `src/main/java/com/swmanager/system/exception/EnumErrorResponseFactory.java`
- 두 예외에서 공통 사용: `MethodArgumentTypeMismatchException`, `HttpMessageNotReadableException`
- 결과: `code=ENUM_VALUE_NOT_ALLOWED` 표준 응답(JSON) 생성
- 단위 테스트 2건(T1-1, T1-2) 동시 작성 → NFR-9 오류 핸들링 시나리오에 포함

**2-4. `GlobalExceptionHandler` 추가 핸들러**:
- 기존 `src/main/java/com/swmanager/system/exception/GlobalExceptionHandler.java`에 두 `@ExceptionHandler` 추가
- 본문은 `EnumErrorResponseFactory` 호출로 1줄
- 웹 요청 시 기존 HTML 에러 페이지 경로로 fallback (기존 패턴 유지)

**2-5. 컴파일 확인**:
```bash
./mvnw -q clean compile
# → BUILD SUCCESS. 아직 Enum 미사용.
```

### Step 3 — Entity / DTO 타입 치환
**3-1. `Document.java`** (`src/main/java/com/swmanager/system/domain/workplan/Document.java`):
- `private String status` → `private DocumentStatus status` (`@Enumerated(EnumType.STRING)`)
- `private String docType` → `private DocumentType docType` (`@Enumerated(EnumType.STRING)`)
- `@PrePersist`에서 `this.status = "DRAFT"` → `this.status = DocumentStatus.DRAFT`
- 필드 주석에 "기획서 §5-6-1 정책: @Enumerated(STRING) 기본 선택. 공백/별칭 이슈 없어 AttributeConverter 미사용." 명시

**3-2. `InspectReport.java`**:
- `private String status` → `private DocumentStatus status` (공유)
- `@PrePersist` 동일 처리

**3-3. `DocumentDTO.java`**:
- `private String status` → `private DocumentStatus status`
- `private String docType` → `private DocumentType docType`
- `getStatusLabel()` / `getDocTypeLabel()` 메서드 제거 또는 위임: `return status != null ? status.label() : "-"`. 혹은 `@JsonProperty("statusLabel")` getter 하나만 남김.
- `getStatusColor()` 역시 위임.

**3-4. `InspectReportDTO.java`**:
- `status` 필드 타입 변경. `toEntity()` 메서드의 `this.status != null ? this.status : "DRAFT"`는 이제 null 체크 후 `DocumentStatus.DRAFT` 반환 (또는 DTO 레벨에서 null 허용, Entity `@PrePersist`가 처리).

**3-5. 컴파일**:
```bash
./mvnw -q clean compile  # 에러 다수 → Step 4에서 해소
```

### Step 4 — Service / Controller 리터럴 → Enum 참조 치환

**4-1. `DocumentService.java:129`** — `doc.setStatus("DRAFT")` → `doc.setStatus(DocumentStatus.DRAFT)`

**4-2. `InspectReportService.java:91`** — `"COMPLETED".equals(saved.getStatus())` → `DocumentStatus.COMPLETED == saved.getStatus()` (또는 `.equals(...)`)
- `InspectReportService.java:107` — `doc.setDocType("INSPECT")` → `doc.setDocType(DocumentType.INSPECT)`
- `InspectReportService.java:112` — `doc.setStatus("COMPLETED")` → `doc.setStatus(DocumentStatus.COMPLETED)`

**4-3. `DocumentController.java:424`** — 상태 검증 로직:
```java
// 변경 전: if (!"DRAFT".equals(status) && !"COMPLETED".equals(status)) { 400 응답 }
// 변경 후: status 파라미터 타입을 DocumentStatus로 변경. ConverterFactory가 대소문자/trim 처리.
//          허용 외 값은 400 자동 응답 (§5-7-4 EnumErrorResponseFactory).
public ResponseEntity<?> xxx(@RequestParam DocumentStatus status) { ... }
```

**4-4. `DocumentController` 템플릿 라우팅 (라인 261~271 8개 case)** — `switch`문 유지하되 string 비교를 Enum switch로:
```java
switch (document.getDocType()) {
    case COMMENCE -> return "document/doc-commence";
    case INTERIM  -> return "document/doc-interim";
    // ...
}
```
- 또는 `DocumentType.templateName()` 메서드를 Enum에 두고 `return "document/" + document.getDocType().templateName()` 1줄로.

**4-5. `PerformanceService.java`** — **변경하지 않는다** (§0-4 WorkPlan 범위 외).

**4-6. `DocumentRepository`의 JPQL** — 실측 결과 파라미터 바인딩 사용 중. 수정 불필요. 라인 101 `APPROVED`는 범위 외(`WorkPlanStatus` 스프린트 대상).

**4-7. 컴파일**:
```bash
./mvnw -q clean compile
# → BUILD SUCCESS.
```

### Step 5 — 테스트 작성 (NFR-9 5개 시나리오 × 2계층 = 10건 + 유틸 2건)

`src/test/java/com/swmanager/system/` 아래 다음 파일:

| 파일 | 시나리오 | 비고 |
|------|----------|------|
| `constant/enums/DocumentStatusJpaTest.java` | DB 라운드트립 (`@DataJpaTest`) | NFR-3 ①: "COMPLETED" → Enum → 저장 → 재조회 |
| `constant/enums/DocumentTypeJpaTest.java` | DB 라운드트립 | NFR-3 ②: "INSPECT" 등 |
| `exception/EnumErrorResponseFactoryTest.java` | 유틸 단위 테스트 | T1-1 (MATM), T1-2 (HMNR) |
| `service/DocumentServiceStatusTransitionTest.java` | NFR-9 시나리오 ② 서비스 | DRAFT→COMPLETED, 중복 COMPLETED 차단 |
| `controller/DocumentControllerStatusTransitionMvcTest.java` | NFR-9 시나리오 ② 컨트롤러 | `@WebMvcTest` |
| `service/LoginAttemptServiceTest.java` | NFR-9 시나리오 ① 서비스 | #1-B 전에도 기존 상수 기준으로 통과 |
| `controller/LoginControllerLockoutMvcTest.java` | NFR-9 시나리오 ① 컨트롤러 | |
| `service/InspectCheckResultSaveTest.java` | NFR-9 시나리오 ③ 서비스 | 섹션별 저장/조회 |
| `controller/InspectReportApiMvcTest.java` | NFR-9 시나리오 ③ 컨트롤러 | |
| `service/InspectPdfServicePdfTest.java` | NFR-9 시나리오 ④ 서비스 | PDF 생성 바이트 > 0 |
| `controller/InspectReportPdfDownloadMvcTest.java` | NFR-9 시나리오 ④ 컨트롤러 | |
| `controller/EnumErrorResponseMvcTest.java` | NFR-9 시나리오 ⑤ 컨트롤러 | 잘못된 status 입력 → 400 |
| `service/DocumentStatusBindingServiceTest.java` | NFR-9 시나리오 ⑤ **서비스** (v2 추가) | `DocumentStatus.fromString("???")` → `IllegalArgumentException("ENUM_VALUE_NOT_ALLOWED")`, 정상 입력 시 Enum 반환. `DocumentType`도 동일 확인 |

총 13 테스트 파일. 시나리오 ①과 ⑤는 #1-B 이전에도 통과 가능한 범위로 구성.

### Step 6 — 감사 보고서 갱신
- `docs/audit/2026-04-18-system-audit.md`의 "하드코딩" 관련 항목에 ☑ + 요약 1줄:
  > `DocumentStatus`/`DocumentType` Enum 전환 완료 (스프린트 refactor-01-hardcoding #1-A). 리터럴 0건 확인. [기획서/v6, 커밋 SHA]

### Step 7 — 빌드 / 재기동 / 스모크 테스트

```bash
./mvnw -q test                   # 12 테스트 전부 통과
./mvnw -q package -DskipTests    # WAR 생성
bash server-restart.sh           # Started SwmanagerApplication + ERROR 0
```

**런타임 스모크** (수동):
1. 로그인 정상
2. 문서관리 목록 진입 → 상태 뱃지(`작성중`/`작성완료`) 표시
3. 점검내역서 상세 → 수정 → 저장 (상태 전이)
4. 점검내역서 PDF 다운로드
5. `GET /document/api/inspect-report?status=foo` → `400 ENUM_VALUE_NOT_ALLOWED` 응답 확인
6. 로그에 `UNKNOWN_ENUM_VALUE` 키 ERROR 없음 (정상 입력 시)

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | Enum 리터럴 제거 (NFR-4) | 아래 **T1-CMD** 블록 실행. | T1-CMD에 명시된 기준 충족 |
| T2 | Enum 라운드트립 (NFR-3) | `./mvnw -Dtest='DocumentStatusJpaTest,DocumentTypeJpaTest' test` | 모든 케이스 Green |

**T1-CMD** (리터럴 `|` 사용, 이스케이프 없음):

```bash
# 1단계 — 전체 스캔 (PerformanceService 포함)
rg -n --type java \
  '"(DRAFT|COMPLETED|INSPECT|COMMENCE|COMPLETION|INTERIM|FAULT|SUPPORT|INSTALL|PATCH)"' \
  src/main/java \
  --glob '!**/constant/enums/**' \
  --glob '!**/test/**'

# 2단계 — 히트가 나오면 각 라인이 WorkPlan/planType/getPlanType 문맥인지 수동 확인 (§0-4)
# 문맥 맞으면 허용, 아니면 위반으로 판정
```

**T1 Pass 기준**: 1단계에서 `PerformanceService.java`의 WorkPlan 문맥 라인만 히트로 허용. 그 외 파일은 0 hits.
| T3 | `./mvnw -q clean compile` | 로컬 | BUILD SUCCESS |
| T4 | `./mvnw -q test` | 로컬 | 13 테스트 전부 Green (v2: 시나리오 ⑤ 서비스 1건 추가) |
| T5 | 서버 부팅 | `bash server-restart.sh` | `Started SwmanagerApplication` 로그 + ERROR 0 |
| T6 | NFR-9 5개 시나리오 | §1 Step 5 파일 실행 | 서비스+컨트롤러 각 5건 (=10건) + 유틸 2건 + 서비스 ⑤ 보강 1건 = 총 13건 통과 |
| T7 | 스모크 | 위 런타임 스모크 1~6 | 6건 모두 OK |
| T8 | 감사 체크박스 | `docs/audit/2026-04-18-system-audit.md` | 해당 항목 ☑ |

---

## 3. 롤백 전략 (기획서 §8-3 적용)

| 상황 | 조치 |
|------|------|
| Step 3~4 compile 실패 | Edit 되돌림 후 import/타입 재확인 |
| 테스트 실패(Step 5) | 실패 테스트 분석. Enum 이름·값 불일치면 Enum 수정, DB 값 불일치면 Pre-flight 재실행 후 정제 스크립트 검토 |
| 배포 후 회귀 감지 (`UNKNOWN_ENUM_VALUE` ≥10/24h 또는 400 +50%) | on-call 30분 내 판단 → `git revert <merge-commit>` → WAR 재배포 → 스모크 3화면 검증 |
| 데이터 손상 의심 | 본 스프린트는 DB 스키마 미변경 → 데이터 손상 가능성 낮음. 의심 시 Pre-flight 재실행으로 분포 확인 |

롤백 체크리스트 (3개):
- ☐ 현재 커밋 SHA 기록: `git log -1 --pretty=%H`
- ☐ merge commit revert PR 작성 및 머지
- ☐ 재배포 후 로그인·문서관리·점검내역서 상세 3화면 수동 검증

---

## 4. 리스크·완화 재확인 (기획서 §7 갱신)

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | DB에 Enum에 없는 값 존재 | 낮음 | Pre-flight 재실행으로 Step 1 차단. |
| R-2 | API 입력 바인딩 실패 | 낮음 | §5-7 정책 구현 + T1-1/T1-2 유틸 테스트 |
| R-3 | JPQL 실행계획 변화 | 낮음 | 본 스프린트 JPQL 변경 없음 (파라미터 바인딩 기존 유지) |
| R-4 | 메시지 키 누락 | 해당 없음 (#1-B로 이월) | — |
| R-6 | 범위 팽창 | 낮음 | `WorkPlan.planType`/`WorkPlan.status` 엄격 제외. PerformanceService 수정 금지 |
| R-7 | Thymeleaf 렌더링 누락 | 낮음 | 기존 템플릿이 문자열 key로 label을 요구하지 않음 (Controller에서 주입). 스모크 2단계에서 뱃지 라벨 육안 확인 |
| R-8 | 롤백 곤란 | 낮음 | #1-A 단독 PR/merge commit 유지 |

---

## 5. #1-B 스텁 (본 PR 범위 외, 다음 PR용)

- **FR-B1~B2**: `messages.properties` + 기존 한글 에러 메시지 10~20건 이관 (목록은 #1-B 개발계획서에서 별도 조사)
- **FR-B3**: `NoSuchMessageException` fallback (`"{missing:key}"`) — `messages` 로거 WARN
- **FR-B4**: `LoginAttemptService`의 상수 2개 → `SecurityLoginProperties` + `@Validated @Min(1) @Max(100)`
- **FR-B5**: `application.properties` 기본값 + `application-local.properties.example` 샘플

→ #1-A 머지 + 운영 확인 후 별도 개발계획서(`hardcoding-improvement-1b.md`)로 착수.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 확인 사항
- [ ] §0-2 Enum 명 `WorkPlanType` → `DocumentType` 변경 수용
- [ ] §0-4 `PerformanceService`/`WorkPlan` 범위 제외 수용
- [ ] §1 Step 1~7 순서 / Step 5 테스트 파일 12개 적절성
- [ ] 롤백 체크리스트 3개 충분성
- [ ] #1-B 스텁 별도 PR 진행 동의
