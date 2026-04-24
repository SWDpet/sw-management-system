---
tags: [dev-plan, sprint, refactor, hardcoding, i18n, config]
sprint: "refactor-01-hardcoding-1b"
status: draft-v2
created: "2026-04-20"
---

# [개발계획서] 하드코딩 개선 #1-B — MessageSource + @ConfigurationProperties

- **작성팀**: 개발팀
- **작성일**: 2026-04-20
- **근거 기획서**: [[hardcoding-improvement]] (v6 승인, FR-B1~B5)
- **선행 스프린트**: `#1-A Enum 전환` (완료, 커밋 SHA 별도 기재)
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 5건 반영)
1. Step 3 파일 수 "8개" → **12개** 정정 (실제 표 카운트 기반)
2. MessageSource는 **Spring Boot 자동구성** (`spring.messages.basename`) 활용, 커스텀 빈/`LocalValidatorFactoryBean` 수동 등록 제거
3. `Messages.java` static facade 제거 → **`MessageResolver` `@Component` 주입** 방식
4. `messages_en.properties`를 **영문 값으로 실제 채움** (빈 문자열 금지)
5. 테스트 보강:
   - FR-B3 — WARN 로그 "1회 발생 + key 포함" 단언
   - NFR-7 — 표현 "`property=0` 위반"으로 정정
   - MockMvc 통합성 테스트 1건 추가 (총 6건)

---

## 0. 전제 / 범위 확정

### 0-1. 기획서 v6 FR-B1~B5 범위
- **FR-B1**: `messages.properties` + `MessageSource` 빈 신규
- **FR-B2**: 한글 에러 메시지 10~20개 이관
- **FR-B3**: 키 누락 시 fallback 정책 (`{missing:key}` + WARN 로그)
- **FR-B4**: `LoginAttemptService` 상수 2개 → `SecurityLoginProperties`(`@Validated`)
- **FR-B5**: `application-local.properties.example` 샘플 추가

### 0-2. 대상 실측 (Explore agent 조사, 2026-04-20)

| 카테고리 | 실측 건수 | 비고 |
|---------|----------|------|
| 서비스·예외 한글 메시지 | 33건 발견 → **13~15개 고유 키로 집약** | 중복/동의어 묶음 |
| 보안 상수 (FR-B4) | 2개 확정 (`MAX_FAILED_ATTEMPTS=5`, `LOCK_TIME_MINUTES=15`) | `LoginAttemptService.java:26-27` |
| 기존 MessageSource 인프라 | **없음** | 신규 구축 필요 |
| 추가 외부화 후보 (P1) | 3개 (세션 TO, 업로드 크기, 동시 세션) | 본 스프린트 **범위 외** (별도 운영 설정 트랙) |

**본 스프린트 범위**: FR-B1~B5. 추가 설정 외부화는 기획서 §5-3/v6 후속 백로그.

### 0-3. 메시지 그룹 정제 (Explore 결과 → 13~15 키)

조사된 33건을 의미 기반으로 집약하여 **14개 키**로 통합:

| 키 | 사용처 | 메시지 템플릿 |
|-----|--------|---------------|
| `error.document.not_found` | DocumentService, DocumentAttachmentService | 문서를 찾을 수 없습니다. ID: {0} |
| `error.document.type_empty` | HwpxExportService | 문서 유형이 비어 있습니다. |
| `error.document.type_unsupported` | HwpxExportService | 지원하지 않는 문서 유형: {0} |
| `error.attachment.not_found` | DocumentAttachmentService | 첨부파일을 찾을 수 없습니다. |
| `error.inspect_report.not_found` | InspectReportService | 점검내역서를 찾을 수 없습니다: {0} |
| `error.project.not_found` | SwController | 프로젝트를 찾을 수 없습니다. (ID: {0}) |
| `error.person.not_found` | PersonController | 담당자 정보가 없습니다. ID: {0} |
| `error.user.not_found` | AdminUserController, MyPageController, PerformanceService, UserDetailsServiceImpl | 사용자를 찾을 수 없습니다. ID: {0} |
| `error.auth.account_locked` | UserDetailsServiceImpl | 계정이 잠겼습니다. {0}분 후 다시 시도해주세요. |
| `error.export.design_data_empty` | ExcelExportService | 설계내역서 데이터가 없습니다. |
| `error.export.performance_data_empty` | ExcelExportService | 기성내역서 데이터가 없습니다. |
| `error.export.hwpx_generation` | HwpxExportService | HWPX 생성 중 오류: {0} |
| `error.export.pdf_conversion` | PdfExportService | PDF 변환 중 오류 발생: {0} |
| `error.template.file_not_found` | HwpxExportService | 템플릿 파일을 찾을 수 없습니다: {0} |

**14개 키. 최소 10 / 최대 20 기준 부합**.

추가 후보(이관 안 하고 유지):
- SwController 권한 메시지 `프로젝트 {action}` — 조건부 메시지(권한/예외 모두 포괄)라 키 분리 비용↑ 대비 가치↓ → #3 i18n 전면화 때 처리
- OrgUnitService 상세 검증 메시지 — 유지보수 빈도 낮음 → #3로 이월

### 0-4. 범위 경계
- **JS/Thymeleaf 템플릿의 한글 라벨**: 범위 외 (기획서 §5-2, 스프린트 #3 후보)
- **Log 메시지의 한글**: 범위 외 (운영자 대상, i18n 불필요)
- **Javadoc 한글**: 범위 외 (개발자 대상)
- **NFR-4의 추가 `rg '"..."'` 검증 대상**: 본 스프린트는 14개 키의 원본 메시지 리터럴만 0건 확인. Enum 리터럴은 #1-A 이미 처리.

---

## 1. 작업 순서

### Step 1 — 사전 스캔 (변경 전 스냅샷)
```bash
# 1-1. 이관 대상 메시지 개수 확인 (14개 키별로 1회 이상 발견되어야 함)
rg -n --type java '문서를 찾을 수 없습니다' src/main/java
rg -n --type java '첨부파일을 찾을 수 없습니다' src/main/java
rg -n --type java '점검내역서를 찾을 수 없습니다' src/main/java
rg -n --type java '프로젝트를 찾을 수 없습니다' src/main/java
rg -n --type java '사용자를 찾을 수 없습니다|사용자 없음' src/main/java
rg -n --type java '계정이 잠겼습니다' src/main/java
rg -n --type java '문서 유형이 비어 있습니다|지원하지 않는 문서 유형' src/main/java
rg -n --type java '설계내역서 데이터가 없습니다|기성내역서 데이터가 없습니다' src/main/java
rg -n --type java 'HWPX 생성 중 오류|PDF 변환 중 오류 발생' src/main/java
rg -n --type java '템플릿 파일을 찾을 수 없습니다' src/main/java
rg -n --type java '담당자 정보가 없습니다|해당 담당자 정보가 없습니다' src/main/java

# 1-2. LoginAttemptService 상수 위치 확인
rg -n 'MAX_FAILED_ATTEMPTS|LOCK_TIME_MINUTES' src/main/java/com/swmanager/system/
```

### Step 2 — MessageSource 인프라 구축 (v2 — Spring Boot 자동구성 활용)

**2-1. 메시지 리소스 파일 신규**:
- `src/main/resources/messages.properties` — 기본 로케일(ko), 한글 값 14건
- `src/main/resources/messages_en.properties` — **영문 값 실제 채움** (빈 문자열 금지. codex v1 권장사항 #4 반영)
  - 예: `error.document.not_found=Document not found. ID: {0}`
- 인코딩 UTF-8 고정 (IntelliJ/Eclipse에서 native 인코딩 설정 주의). Java 9+ 이후 Properties 파일 기본 UTF-8 지원.

**2-2. Spring Boot 자동구성 활용** (별도 `@Bean` 금지):
- `application.properties`에 다음 키 추가:
  ```properties
  spring.messages.basename=messages
  spring.messages.encoding=UTF-8
  spring.messages.fallback-to-system-locale=false
  spring.messages.always-use-message-format=false
  ```
- **Spring Boot가 자동으로 `MessageSource` 빈 생성**. 커스텀 `MessageSourceConfig.java` 작성 **안 함**.
- `LocalValidatorFactoryBean` 수동 등록 **제거** — 현재 Spring Boot 자동구성이 제공하는 `Validator` 빈이 이미 `MessageSource`를 참조함. 별도 설정 불필요.

**2-3. 메시지 조회 컴포넌트** (v2 — static facade 제거, `@Component` 주입 방식):
- `src/main/java/com/swmanager/system/i18n/MessageResolver.java`
  ```java
  @Component
  @RequiredArgsConstructor
  public class MessageResolver {
      private final MessageSource messageSource;
      private static final Logger log = LoggerFactory.getLogger("messages");

      public String get(String key, Object... args) {
          try {
              return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
          } catch (NoSuchMessageException e) {
              log.warn("Missing message key: {}", key);  // FR-B3: 1회 WARN + key 포함
              return "{missing:" + key + "}";
          }
      }
  }
  ```
- 사용 패턴: `private final MessageResolver messages;` 생성자 주입 후 `messages.get("error.document.not_found", docId)`.
- **이점 (codex 권장사항 #3 반영)**:
  - 테스트에서 `MessageResolver` Mock 주입 가능 (static facade는 불가)
  - DI 일관성 (전체 프로젝트가 생성자 주입 패턴 사용)
  - 초기화 순서 문제 없음 (Spring 컨테이너가 관리)

**2-4. 컴파일**:
```bash
./mvnw -q clean compile
```

### Step 3 — 메시지 이관 (14개 키 / 12개 파일)

`MessageResolver#get("error.xxx", args)` 호출로 교체. 아래 **12개 파일** (v2 정정, codex 권장사항 #1 반영):

| 파일 | 교체 건수 | 메서드 |
|------|----------|--------|
| `SwController.java` | 1 | 프로젝트 not found |
| `AdminUserController.java` | 2 | 사용자 not found |
| `MyPageController.java` | 3 | 사용자 정보 없음 |
| `PersonController.java` | 1 | 담당자 not found |
| `DocumentService.java` | 1 | 문서 not found |
| `DocumentAttachmentService.java` | 2 | 문서/첨부파일 not found |
| `HwpxExportService.java` | 5 | 문서 유형, 템플릿, HWPX 오류 |
| `ExcelExportService.java` | 2 | 설계/기성 데이터 없음 |
| `PdfExportService.java` | 1 | PDF 변환 오류 |
| `InspectReportService.java` | 3 | 점검내역서 not found |
| `PerformanceService.java` | 1 | 사용자 없음 |
| `UserDetailsServiceImpl.java` | 2 | 사용자 없음, 계정 잠김 |

**총 12개 파일, 24건 이관 지점** (표 합계). 14개 고유 키로 매핑. `UserDetailsServiceImpl`은 Spring Security 콜백 구현체라 `MessageResolver` 생성자 주입 방식 적용.

예:
```java
// 변경 전 (DocumentService)
throw new BusinessException(ErrorCode.NOT_FOUND, "문서를 찾을 수 없습니다. ID: " + docId);
// 변경 후
throw new BusinessException(ErrorCode.NOT_FOUND, messages.get("error.document.not_found", docId));
```
(`messages`는 클래스에 `@RequiredArgsConstructor`로 주입된 `MessageResolver` 필드)

### Step 4 — `SecurityLoginProperties` + `@Validated` (FR-B4)

**4-1. 신규 설정 클래스**:
- `src/main/java/com/swmanager/system/config/SecurityLoginProperties.java`
  ```java
  @ConfigurationProperties(prefix = "security.login")
  @Validated
  public class SecurityLoginProperties {
      @Min(1) @Max(100)
      private int maxFailedAttempts = 5;
      @Min(1) @Max(1440)
      private int lockTimeMinutes = 15;
      // getters/setters
  }
  ```
- `@EnableConfigurationProperties(SecurityLoginProperties.class)` 를 메인 Application 또는 별도 `@Configuration`에 추가.

**4-2. `application.properties` 기본값 추가**:
```properties
# 로그인 실패 임계값 (FR-B4)
security.login.max-failed-attempts=${SEC_LOGIN_MAX_FAIL:5}
security.login.lock-time-minutes=${SEC_LOGIN_LOCK_MIN:15}
```

**4-3. `application-local.properties.example` 샘플 갱신**:
- 해당 섹션 추가 (`security.login.max-failed-attempts=5`, `lock-time-minutes=15`) — 기본값과 동일. 운영에서 조정 가능.

**4-4. `LoginAttemptService` 개조**:
- `private static final int MAX_FAILED_ATTEMPTS = 5;` 2개 필드 제거
- `SecurityLoginProperties` 생성자 주입
- 참조 지점(현재 3개 라인): `props.getMaxFailedAttempts()`, `props.getLockTimeMinutes()`로 교체
- `UserDetailsServiceImpl`에서 "계정이 잠겼습니다. {분}" 메시지 계산 시에도 `props.getLockTimeMinutes()` 사용 가능 (현재는 하드코딩 분 수 없이 `user.getLockTime().until(...)`로 계산)

**4-5. 컴파일**:
```bash
./mvnw -q clean compile
```

### Step 5 — 테스트 작성 (v2 — 6건으로 확장)

**필수 테스트 (총 6건, codex 권장사항 #5 반영)**:

| 파일 | 검증 |
|------|------|
| `i18n/MessageResolverTest.java` | `messages.get("error.document.not_found", 123L)` → 한글 메시지 + ID 치환. 정상 경로 커버. |
| `i18n/MessageResolverFallbackTest.java` | **FR-B3 강화**: `messages.get("__nonexistent__")` 호출 시<br>(1) WARN 로그가 **정확히 1회 발생**하고 (ListAppender 사용)<br>(2) 로그 메시지에 `"__nonexistent__"` 포함되는지<br>(3) 반환값이 `"{missing:__nonexistent__}"` 인지 검증 |
| `i18n/MessageKeyCoverageTest.java` | 14개 필수 키가 `messages.properties`에 전부 존재 확인 (`MessageResolver` + Mock DB 불필요, `MessageSource.getMessage` 직접 호출로 `NoSuchMessageException` 없음 보장). |
| `config/SecurityLoginPropertiesValidationTest.java` | **NFR-7 표현 정정**: `security.login.max-failed-attempts=0` property 바인딩 시 **@Validated @Min(1) 제약 위반** → `ApplicationContext` 초기화 실패 (`BeanCreationException` 또는 `ConstraintViolationException`) 확인. `@SpringBootTest(properties = {"security.login.max-failed-attempts=0"})` 사용. |
| `service/LoginAttemptServiceWithPropertiesTest.java` | `SecurityLoginProperties` 주입 후 `maxFailedAttempts=3` 설정 → 3회 실패에서 잠금 확인 (Mockito) |
| `controller/DocumentNotFoundMockMvcTest.java` | **통합성 테스트 (v2 추가, codex 권장사항 #5-c)**: 존재하지 않는 문서 ID로 `GET /document/detail/{nonexistent}` 호출 → 응답 본문(또는 HTML)에 "문서를 찾을 수 없습니다. ID: {실제ID}" **치환된 한글 메시지** 포함 확인. MessageSource + MessageResolver + 예외 전파 end-to-end 검증. |

**검증 명령**:
```bash
DB_PASSWORD='***' ./mvnw -q test -Dtest='MessageResolverTest,MessageResolverFallbackTest,MessageKeyCoverageTest,SecurityLoginPropertiesValidationTest,LoginAttemptServiceWithPropertiesTest,DocumentNotFoundMockMvcTest'
```

### Step 6 — 감사 문서 갱신
- `docs/audit/2026-04-18-system-audit.md` 의 `refactor-01-hardcoding` 섹션에 **`#1-B ☑ 조치함`** 추가.

### Step 7 — 빌드 / 재기동 / 스모크

```bash
DB_PASSWORD='***' ./mvnw -q clean compile
DB_PASSWORD='***' ./mvnw -q test
bash server-restart.sh
```

**런타임 스모크**:
1. 로그인 5회 실패 → 계정 잠금 + 오류 메시지 "계정이 잠겼습니다. X분 후 다시 시도해주세요." 한글 표시
2. 존재하지 않는 문서 ID 접근 → "문서를 찾을 수 없습니다. ID: N" 반환 (한글)
3. `application.properties`에서 `security.login.max-failed-attempts=0` 임시 설정 → **서버 부팅 실패** (NFR-7 충족)
4. 모니터링 로그에 `messages` 로거 WARN 발생 없음 (정상 상태)

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | 메시지 리터럴 제거 | 아래 **T1-CMD** 블록 실행 | T1-CMD에 명시된 기준 충족 |
| T2 | 메시지 키 커버리지 | `MessageKeyCoverageTest` | 14개 키 전부 존재 |
| T3 | `./mvnw -q clean compile` | 로컬 | BUILD SUCCESS |
| T4 | 전체 테스트 | `DB_PASSWORD='***' ./mvnw -q test` | 기존 43 + 신규 6 = 49건 Green |
| T5 | 서버 부팅 | `bash server-restart.sh` | `Started SwManagerApplication` + ERROR 0 |
| T6 | 런타임 스모크 | §1 Step 7 1~4 | 4건 모두 확인 |
| T7 | property=0 위반 부팅 실패 (v2 정정) | `application.properties`에 `security.login.max-failed-attempts=0` 설정 후 재부팅 | 부팅 실패 (`ConstraintViolationException` 또는 `BindValidationException` + 서버 프로세스 종료). 정상 값(1~100)으로 복귀 시 정상 부팅. |

**T1-CMD** (메시지 리터럴 제거 확인):

```bash
# 본 스프린트 14개 키의 원본 한글 메시지가 Service/Controller/Exception 에 남아있지 않아야 함
rg -n --type java '문서를 찾을 수 없습니다|첨부파일을 찾을 수 없습니다|점검내역서를 찾을 수 없습니다' \
  src/main/java \
  --glob '!**/i18n/**' \
  --glob '!**/test/**' \
  --glob '!**/resources/**'
rg -n --type java 'HWPX 생성 중 오류|PDF 변환 중 오류 발생|템플릿 파일을 찾을 수 없습니다' \
  src/main/java \
  --glob '!**/i18n/**' \
  --glob '!**/test/**'
```

**T1 Pass 기준**: 0 hits (모든 메시지가 `Messages.get()` 호출로 치환됨).

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| 메시지 키 누락으로 `{missing:xxx}` 노출 | `messages.properties`에 해당 키 추가 후 서버 재기동 (hot-reload 고려 시 cacheSeconds=60 설정 검토) |
| `SecurityLoginProperties` 검증 실패 부팅 불가 | application.properties의 `security.login.*` 값 교정 |
| 배포 후 회귀 | `git revert <1b-merge-commit>` → `LoginAttemptService`가 내부 상수로 복귀. 메시지는 한글 리터럴 복귀. 동작 동일. |

**롤백 체크리스트**:
- ☐ #1-B merge commit SHA 기록
- ☐ `git revert <sha>` PR 머지 후 재배포
- ☐ 로그인·문서 상세 진입 스모크

**특이**: 본 스프린트는 DB 스키마 변경 없음 + #1-A와 독립 커밋 → 롤백이 안전.

---

## 4. 리스크·완화

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-B1 | 메시지 키 오타로 런타임 `{missing:}` 노출 | 낮음 | `MessageKeyCoverageTest`에서 14개 키 강제 검증 |
| R-B2 | `@Validated` 기본값 변경으로 기존 환경 깨짐 | 낮음 | 기본값 유지(5/15). `@Min/@Max` 범위는 현재 값 포괄 |
| R-B3 | MessageSource 캐시로 수정 반영 지연 | 매우 낮음 | Spring Boot 자동구성 기본값(`spring.messages.cache-duration` 미설정 시 영구 캐시) 사용. 운영 중 변경 필요 시 서버 재기동. |
| R-B4 | UTF-8 인코딩 깨짐 | 낮음 | `ReloadableResourceBundleMessageSource.defaultEncoding="UTF-8"` 명시 + 리소스 파일 BOM 없이 저장 |
| R-B5 | Properties 파일에 한글 저장 시 escape 필요 여부 | 낮음 | Java 9+는 native UTF-8 지원. IntelliJ `Transparent native-to-ASCII conversion` ON 권장. Maven resource filtering 영향 없음 확인 |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 확인 사항
- [ ] §0-3 14개 메시지 키 집약 (FR-B2 10~20 범위 충족)
- [ ] §1 Step 2 Spring Boot 자동구성(`spring.messages.basename`) + `MessageResolver` `@Component` 주입
- [ ] §1 Step 3 **12개 파일** 24건 이관 순서
- [ ] §1 Step 4 `SecurityLoginProperties` `@Validated @Min(1) @Max(100)` + `lockTimeMinutes @Min(1) @Max(1440)`
- [ ] 테스트 **6건** (MessageResolverFallbackTest WARN 단언 + MockMvc 통합성 테스트 포함, 총 49건)
- [ ] 롤백 체크리스트 + #1-A 독립 커밋 유지
