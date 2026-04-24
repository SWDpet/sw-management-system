---
tags: [dev-plan, sprint, logging, enum, normalization]
sprint: "access-log-action-and-menu-sync"
status: draft
created: "2026-04-21"
---

# [개발계획서] access_logs action_type Enum + MenuName 동기화

- **작성팀**: 개발팀
- **작성일**: 2026-04-21
- **근거 기획서**: [[access-log-action-and-menu-sync]] v2.1 (사용자 최종승인 2026-04-21)
- **상태**: v1.2 (codex v1.1 경미 1건 후속 반영 — ArchRule 과강제 해석 완화)
- **v1→v1.2 변경점**:
  1. `fromJson`에서 raw `trim()` 후 `valueOf` (공백 입력 대응)
  2. 동의어 전수 검증 테스트 신설 (`@ParameterizedTest` + §4-1 전체)
  3. **Enum 오버로드 비오탐 샘플 테스트** — ArchRule이 아닌 일반 JUnit 스모크로 분리 (v1.2: "모든 컨트롤러 강제" 오해 방지)
  4. 롤백 기준 SHA 기록 방식 명문화 (`git rev-parse HEAD` 절차)

---

## 0. 전제 / 환경

### 0-1. 빌드 / 스택
- Java 17 / Spring Boot 3.2.1 / Maven
- 기존 `DocumentStatus`·`DocumentType` Enum 패턴 재사용 (`constant/enums/*.java`)
- 기존 `MenuName` 위치: `src/main/java/com/swmanager/system/constants/MenuName.java`
- `LogService` 위치: `src/main/java/com/swmanager/system/service/LogService.java`

### 0-2. 범위 고정 (기획서 §3)
- 포함: Enum 신설 / 상수 5 추가 / LogService 오버로드 / 컨트롤러 전면 치환 / ArchUnit / fromKoLabel / 테스트
- 제외: DB 스키마 변경 / 과거 로그 정제 / S9-B(action_code 컬럼 전환)

### 0-3. 확정된 수치 (기획서 NFR·FR)
- `AccessActionType` 정확히 **13개** 상수
- `MenuName` 정확히 **16개** 상수 (기존 11 + 신규 5)
- 테스트 신규 ≥ **5건**
- NFR-4: 리터럴 검증 (a)(b)(c)(d) 4종 모두 pass

---

## 1. 작업 순서

### Step 1 — `AccessActionType` Enum 신설 (FR-1, FR-4)

**1-1. 파일**: `src/main/java/com/swmanager/system/constant/enums/AccessActionType.java`

```java
package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * access_logs.action_type Enum (S9).
 * DB 저장값은 label(한글) — 기존 데이터 호환. label Freeze (NFR-7).
 */
public enum AccessActionType {
    VIEW("조회"),
    CREATE("등록"),
    UPDATE("수정"),
    DELETE("삭제"),
    DOWNLOAD("다운로드"),
    UPLOAD("업로드"),
    APPROVE("승인"),
    SIGN("서명"),
    PREVIEW("미리보기"),
    BATCH("일괄처리"),
    PATTERN_CRUD("패턴관리"),
    WAGE_CRUD("노임관리"),
    SENSITIVE_VIEW("민감정보조회");

    private final String label;

    /** 동의어 → Enum 매핑 (기획서 §4-1 "기존 리터럴 통합 후보" 열 기반) */
    private static final Map<String, AccessActionType> SYNONYMS = new ConcurrentHashMap<>();
    static {
        // VIEW 계열
        putSyn("목록조회", VIEW); putSyn("상세조회", VIEW); putSyn("접근", VIEW);
        putSyn("발급폼접근", VIEW); putSyn("수정폼접근", VIEW);
        // CREATE
        putSyn("생성", CREATE); putSyn("발급", CREATE); putSyn("신청", CREATE);
        // UPDATE
        putSyn("정보수정", UPDATE); putSyn("비번변경", UPDATE); putSyn("상태변경", UPDATE);
        // DOWNLOAD
        putSyn("CSV다운로드", DOWNLOAD);
        // APPROVE
        putSyn("승인요청", APPROVE);
        // BATCH
        putSyn("일괄생성", BATCH); putSyn("집계", BATCH); putSyn("금액재계산", BATCH);
        putSyn("패턴복사", BATCH); putSyn("패턴초기화", BATCH);
        // PATTERN_CRUD
        putSyn("패턴등록", PATTERN_CRUD); putSyn("패턴수정", PATTERN_CRUD); putSyn("패턴삭제", PATTERN_CRUD);
        putSyn("비고패턴등록", PATTERN_CRUD); putSyn("비고패턴수정", PATTERN_CRUD); putSyn("비고패턴삭제", PATTERN_CRUD);
        // WAGE_CRUD
        putSyn("노임단가등록", WAGE_CRUD); putSyn("노임단가수정", WAGE_CRUD); putSyn("노임단가삭제", WAGE_CRUD);
    }
    private static void putSyn(String syn, AccessActionType v) { SYNONYMS.put(syn, v); }

    AccessActionType(String label) { this.label = label; }

    @JsonValue
    public String getLabel() { return label; }

    /** JSON 바인딩 — label 우선, 그 다음 enum name (둘 다 trim) */
    @JsonCreator
    public static AccessActionType fromJson(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        AccessActionType v = fromKoLabel(norm);
        if (v != null) return v;
        try { return AccessActionType.valueOf(norm); } catch (Exception e) { return null; }
    }

    /** 한글 label + 동의어 정규화 (기획서 §4-1-A) */
    public static AccessActionType fromKoLabel(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        for (AccessActionType v : values()) if (v.label.equals(norm)) return v;
        return SYNONYMS.get(norm);
    }
}
```

### Step 2 — `MenuName` 상수 5종 추가 (FR-2)

**2-1. 파일**: `src/main/java/com/swmanager/system/constants/MenuName.java`

기존 11개 유지 + 신규 5개 추가:
```java
public static final String QR_LICENSE = "QR라이선스";
public static final String LICENSE_REGISTRY = "라이선스대장";
public static final String GEONURIS_LICENSE = "GeoNURIS라이선스";
public static final String QUOTATION = "견적서";
public static final String SIGNUP = "회원가입";
```

**총 16개 고정**. 이후 변경 시 기획서 NFR-7 label Freeze 적용.

### Step 3 — `LogService` Enum 오버로드 신설 (FR-3)

**3-1. 파일**: `src/main/java/com/swmanager/system/service/LogService.java`

```java
/** 신규 Enum 경로 — 앞으로 이것만 사용 */
public void log(String menuNm, AccessActionType action, String detail) {
    String label = (action != null) ? action.getLabel() : null;
    logInternal(menuNm, label, detail);
}

/**
 * @deprecated (S9, 2026-04-21) — 신규 호출 금지. ArchUnit 게이트로 회귀 차단.
 *   기존 DB action_type 값 호환을 위해 unknown 문자열은 fail-soft 저장.
 */
@Deprecated(since = "S9", forRemoval = false)
public void log(String menuNm, String actionType, String detail) {
    AccessActionType normalized = AccessActionType.fromKoLabel(actionType);
    if (actionType != null && normalized == null) {
        log.warn("ACCESS_LOG_ACTION_UNKNOWN: raw='{}'", actionType);
    }
    String label = (normalized != null) ? normalized.getLabel() : actionType; // fail-soft
    logInternal(menuNm, label, detail);
}

// 공통 저장 — 기존 본문에서 분리
private void logInternal(String menuNm, String actionLabel, String detail) {
    // (기존 LogService 본문: Auth/IP/Guard/AccessLog save 그대로)
}
```

### Step 4 — 컨트롤러 전면 치환 (FR-3, NFR-4)

**대상 파일 (grep 결과 기반 13개 컨트롤러)**:
- DocumentController / AdminUserController / InfraController
- MyPageController / PerformanceController / PersonController
- QrLicenseController / LicenseRegistryController / GeonurisLicenseController
- QuotationController / SignupController / WorkPlanController
- (기타 `logService.log` 사용처 있으면 추가)

**치환 규칙**:
```java
// Before
logService.log("견적서", "등록", "견적서 발행: ...");
// After
logService.log(MenuName.QUOTATION, AccessActionType.CREATE, "견적서 발행: ...");
```

**변수 전달 케이스 (InfraController:236, PersonController:188, WorkPlanController:246)**:
```java
// Before
String action = isNew ? "등록" : "수정";
logService.log("서버관리", action, msg);
// After
AccessActionType action = isNew ? AccessActionType.CREATE : AccessActionType.UPDATE;
logService.log(MenuName.INFRA, action, msg);
```

### Step 5 — ArchUnit 게이트 (FR-5, NFR-4c)

**5-1. 의존성 추가** — `pom.xml`:
```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

**5-2. 테스트 파일**: `src/test/java/com/swmanager/system/arch/LogServiceUsageArchTest.java`

```java
@AnalyzeClasses(packages = "com.swmanager.system",
    importOptions = ImportOption.DoNotIncludeTests.class)
class LogServiceUsageArchTest {

    @ArchTest
    static final ArchRule no_deprecated_log_call =
        noClasses().should().callMethod(
            LogService.class, "log", String.class, String.class, String.class
        ).because("S9: logService.log(String,String,String) is deprecated; " +
                  "use log(String, AccessActionType, String) instead");
}
```

**Pass 기준**: `./mvnw test -Dtest='LogServiceUsageArchTest'` PASS. Step 4 치환 누락 시 본 테스트가 실패해 빌드 중단.

**5-3. Enum 오버로드 비오탐 샘플 테스트** (v1.2, codex #4-후속 반영):

ArchRule이 아닌 **일반 JUnit 스모크 테스트**로 분리 — ArchUnit 규칙은 "Deprecated 시그니처 금지" 하나에만 집중하고, Enum 오버로드가 호출 가능함은 별도 샘플 호출로 증명.

```java
// src/test/java/com/swmanager/system/service/LogServiceEnumOverloadSmokeTest.java
@ExtendWith(MockitoExtension.class)
class LogServiceEnumOverloadSmokeTest {
    @Mock AccessLogRepository accessLogRepository;
    @Mock UserRepository userRepository;
    @InjectMocks LogService logService;

    @Test
    void enum_overload_compiles_and_executes_without_archunit_false_positive() {
        // Enum 오버로드가 존재·호출 가능함을 샘플 호출로 증명
        // (ArchUnit 규칙이 이 호출을 잘못 잡으면 이 테스트도 함께 실패 → 양방향 신호)
        logService.log(MenuName.QUOTATION, AccessActionType.VIEW, "archunit-false-positive-check");
        verify(accessLogRepository, atMostOnce()).save(any());
    }
}
```

— 이 테스트는 **"모든 컨트롤러가 반드시 Enum 오버로드를 호출해야 한다"** 같은 강제 규칙이 아님. 단순히 Enum 오버로드의 호출 가능성(call-site 존재)만 보장.

### Step 6 — 단위 테스트 작성 (FR-4, NFR-3)

**파일**: `src/test/java/com/swmanager/system/constant/enums/AccessActionTypeTest.java`

5+ 시나리오:
1. `fromKoLabel_exact_match` — `fromKoLabel("조회") == VIEW`
2. `fromKoLabel_synonym` — `fromKoLabel("목록조회") == VIEW`
3. `fromKoLabel_trim` — `fromKoLabel("  조회  ") == VIEW`
4. `fromKoLabel_unknown_returns_null` — `fromKoLabel("없는값") == null`
5. `fromJson_by_label` + `fromJson_by_name` — 양방향 바인딩
6. `fromJson_trims_whitespace` (v1.1) — `fromJson(" VIEW ") == VIEW`
7. `enum_has_exactly_13_values` — FR-1 수치 게이트
8. `getLabel_is_frozen_korean` — 각 label 한글 고정 (NFR-7 hint)
9. **`fromKoLabel_all_synonyms_full_coverage_parameterized`** (v1.1) — **기획서 §4-1 표 전체 동의어 매핑**을 `@ParameterizedTest` + `@CsvSource` 로 전수 검증 (기대값: VIEW/CREATE/UPDATE/DOWNLOAD/APPROVE/BATCH/PATTERN_CRUD/WAGE_CRUD 각 그룹 모든 원소). 동의어가 누락·오매핑되면 즉시 실패

**파일**: `src/test/java/com/swmanager/system/constants/MenuNameTest.java`
1. `menu_name_has_exactly_16_constants` — reflection으로 `public static final String` 개수 = 16

**파일**: `src/test/java/com/swmanager/system/service/LogServiceActionNormalizeTest.java`
1. `deprecated_overload_maps_synonym_to_label` — `log("견적서", "목록조회", ...)` → 저장 label = `"조회"`
2. `deprecated_overload_unknown_logs_warn_and_saves_raw` — ListAppender로 WARN 검증 + 원본 저장

### Step 7 — 컴파일 / 테스트 / 재기동

```bash
./mvnw -q clean compile
./mvnw -q test -Dtest='AccessActionTypeTest,MenuNameTest,LogServiceActionNormalizeTest,LogServiceUsageArchTest,LogServiceTest'
bash server-restart.sh
```

**회귀 스모크**:
1. `/login` → 로그인
2. `/document/list` 접근 → access_logs에 `menu_nm=문서관리, action_type=조회` 저장 확인
3. `/quotation/list` → `menu_nm=견적서, action_type=조회`
4. `/admin/users` 승인 → `menu_nm=회원관리, action_type=승인`
5. `/geonuris` 목록 → `menu_nm=GeoNURIS라이선스, action_type=조회`

### Step 8 — 로드맵 정정 (T-LINK)

`docs/design-docs/data-architecture-roadmap.md` §2 S9 본문 `MenuName 상수 4종 누락` → `MenuName 상수 5종 누락 (S9 구현 후 정정, 2026-04-21)` + 본 기획서 링크.

### Step 9 — 문서 갱신 + 커밋 / 푸시

- `docs/generated/audit/data-architecture-utilization-audit.md` S9 완료 표기
- `docs/design-docs/data-architecture-roadmap.md` Wave 3 S9 ✅ 완료
- git commit + push

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | AccessActionType 정확히 13 | `AccessActionType.values().length` | = 13 |
| T2 | MenuName 정확히 16 | Reflection | = 16 |
| T3 | fromKoLabel 정확 매칭 | JUnit | PASS |
| T4 | fromKoLabel 동의어 매핑 | JUnit | PASS (예: `목록조회→VIEW`) |
| T5 | fromKoLabel unknown → null | JUnit | PASS |
| T6 | JsonCreator 양방향 | JUnit | label·name 모두 역직렬화 |
| T7 | Deprecated 오버로드 unknown → WARN + 원본 | ListAppender | `ACCESS_LOG_ACTION_UNKNOWN` WARN 1건 |
| T8 | ArchUnit 게이트 | `LogServiceUsageArchTest` | PASS (호출 지점 0) |
| T9 | 리터럴 잔존 (한글) | `rg -n 'logService\.log\("[^"]+", "[가-힣]+", ' src/main/java` | 0 hits |
| T10 | 리터럴 잔존 (전체) | `rg -n 'logService\.log\("[^"]+", "[^"]+", ' src/main/java` | 0 hits |
| T11 | 변수 전달 타입 전환 | `rg -n 'String\s+action\s*=' src/main/java/com/swmanager/system/controller/(Infra\|Person\|WorkPlan)Controller.java` | 0 hits (AccessActionType으로 변경) |
| T12 | 컴파일 | `./mvnw -q clean compile` | BUILD SUCCESS |
| T13 | 서버 기동 | `bash server-restart.sh` | `Started` + ERROR 0 |
| T14 | 회귀 스모크 5건 | §1 Step 7 | 5건 모두 정상 로그 저장 |
| T15 | 과거 로그 무손상 (NFR-1) | `SELECT COUNT(*) FROM access_logs WHERE action_type IS NOT NULL` | 실행 전후 동일 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| ArchUnit 치환 누락으로 게이트 실패 | 실패 로그의 호출 지점 보고 수정 재커밋. 작업 중단 아님 |
| Enum label 오타 발견 | 치환 중이면 Enum 수정 후 재컴파일. 배포 후 발견 시 **NFR-7 위반**이므로 별도 마이그 스프린트 |
| 컨트롤러 치환 과정에서 기능 회귀 | `git revert <해당 파일 commit>` — 파일별 원자적 커밋 권장 |
| 배포 후 로그 유실 (unknown 폭주) | Deprecated 오버로드 fail-soft로 원본 저장됨 → 유실 없음. WARN 로그 분석 후 동의어 보강 |
| 전체 롤백 | `git revert <merge commit>` — S9 전체 되돌림. DB 스키마 변경 없으므로 side-effect 0 |

**롤백 기준 커밋 기록 방식** (v1.1):
- 구현 Step 0에서 `git rev-parse HEAD` 실행 → 출력된 40자 SHA를 본 문서 §3 맨 아래 `롤백 기준 SHA:` 필드에 **즉시 기록**
- 실행 예:
  ```bash
  BASE_SHA=$(git rev-parse HEAD) && echo "롤백 기준 SHA: $BASE_SHA"
  # 본 문서 해당 줄에 sed 또는 직접 편집으로 반영
  ```
- 롤백 명령: `git revert --no-commit <BASE_SHA>..HEAD && git commit -m "revert: S9 rollback to <BASE_SHA>"`
- **롤백 기준 SHA**: `8dc86f894ab6db0400951e5676954411e5d2ebd4` (2026-04-21 Step 0 기록)

---

## 4. 리스크·완화 재확인

| ID | 리스크 | 본 개발계획서 적용 |
|----|--------|-------------------|
| R-1 | Enum 매핑 정책 오해 | Step 1의 SYNONYMS 정적 블록이 기획서 §4-1 표와 1:1 매핑 |
| R-2 | DB 과거값 불일치 | label 한글 유지 정책 (Step 1), 과거 데이터 수정 0건 (NFR-1) |
| R-3 | 리터럴 치환 누락 | T9/T10 이중 grep + T8 ArchUnit |
| R-4 | 변수 전달 재오염 | T11 grep + T8 ArchUnit |
| R-5 | unknown 유입 시 로그 유실 | Step 3 fail-soft (원본 저장 + WARN) |
| R-6 | label 변경 | NFR-7 Freeze, T1/T2 수치 고정 |

---

## 5. 승인 요청

### 승인 확인 사항
- [ ] §1 Step 1 AccessActionType Enum 코드 (SYNONYMS 포함)
- [ ] §1 Step 2 MenuName 5종 추가
- [ ] §1 Step 3 LogService 오버로드 + Deprecated fail-soft
- [ ] §1 Step 4 컨트롤러 13개 전면 치환 (리터럴 + 변수 전달 양쪽)
- [ ] §1 Step 5 ArchUnit 의존성 + 테스트
- [ ] §1 Step 6 테스트 10+ 시나리오
- [ ] §1 Step 8 T-LINK (로드맵 §S9 정정)
- [ ] §2 T1~T15 체크리스트
- [ ] §3 롤백 전략
