---
tags: [plan, sprint, refactor, hardcoding]
sprint: "refactor-01-hardcoding"
status: draft-v6
created: "2026-04-20"
revised: "2026-04-20"
---

# [기획서 v6] 소스 하드코딩 개선 — 리팩터 스프린트 #1

- **작성팀**: 기획팀
- **작성일**: 2026-04-20 (v6: Pre-flight 결과 반영 범위 재조정)
- **선행 커밋**: `5560c18` (feat(projects): 사업현황 검색 개편)
- **상태**: 초안 v6 (codex 재검토 대기)
- **Pre-flight 결과 첨부**: [`hardcoding-preflight-result.md`](../dev-plans/hardcoding-preflight-result.md)

### v6 주요 변경 (Pre-flight 실측 결과 반영 — **범위 축소**)
Pre-flight SQL 실행 결과 기획서 v5의 Enum 범위 중 2종이 **실데이터 문제로 전환 불가**임이 확인됨. 개발팀이 이를 기획팀에 피드백하여 범위를 재조정함.

- **`SystemType` Enum 전환 제외** — `sw_pjt.sys_nm_en` 컬럼에 숫자값 `"112"`(3건) 포함. Java Enum name 규칙(첫 글자 숫자 불가) 위반. 별도 스프린트(`sys-type-normalization`)에서 데이터 정제·스키마 재설계 후 처리.
- **`InspectResult` Enum 전환 제외** — `inspect_check_result.result` 컬럼이 자유 텍스트(퍼센트/GB/설명문) 165건+ 포함. 상태 코드가 아닌 실측치 혼재. 별도 스프린트(`inspect-result-schema-split`)에서 `result_code`(Enum) + `result_text`(VARCHAR) 컬럼 분리 후 처리.
- **스프린트 #1-A 범위를 `DocumentStatus`, `WorkPlanType` 2종으로 축소** — 나머지 2종은 후속 백로그로 이관(§8-4).
- Pre-flight 결과를 §5-5에 실데이터 분포로 반영.

### v5 주요 변경 (codex v4 충돌 1건 해소)
- **AttributeConverter 역할 충돌 해소** — §5-6, §5-7-2, R-1의 모순을 해소하기 위해 **책임 경계표**(§5-6-2) 신설. 핵심: 별칭 처리는 **API 입력단에서만**, DB는 항상 정규화된 표준 문자열만 저장/조회. DB 레거시 값은 **Pre-flight 데이터 정제 스크립트**로 사전 마이그레이션 처리하며, AttributeConverter는 별칭 해석 책임을 갖지 않는다 (trim/upper 정규화만 수행).

### v4 주요 변경 (codex v3 조건부 승인 3건 반영)
1. NFR-4 grep 정규식을 이스케이프 없는 OR 그룹으로 수정 + 검증 예시 추가
2. Enum 별칭 단일 소스 원칙 확정 — JSON body `@JsonCreator` 단독, query/path `ConverterFactory`, 별칭 테이블은 Enum 내부 정적 `Map<String, ThisEnum> ALIASES`로만 선언 (`@JsonAlias` 사용 안 함, 컨버터에서 별칭 처리 금지)
3. 실패 응답 `allowed` 생성 규칙을 예외 타입별로 명문화

### v3 주요 변경 (codex v2 피드백 반영)
1. 절 번호 참조 오류 일괄 정정 (§4-x → §5-x)
2. API Enum 바인딩 정책 분할 — JSON body (`@JsonCreator`) / query·path (`Converter`·`ConverterFactory`) + 실패 응답 샘플 JSON 첨부 (§5-7)
3. NFR-9 수치화 — 필수 회귀 시나리오 **5개 고정 목록** + 서비스/컨트롤러 경로별 최소 1건 기준
4. 운영 안정성 실행 규칙 명확화 — "자동 감지 + 수동 승인 롤백", on-call·판단 시간·체크리스트 3개 확정 (§8)
5. 부정확했던 Spring 설정 키(`spring.mvc.converters.preferred-json-escape`) 제거
6. NFR-4 grep 제외 규칙 구체화 (`--glob` 패턴 완전 명시)
7. Pre-flight SQL 결과를 **개발계획서 필수 첨부 산출물**로 강제
8. `UNKNOWN_ENUM_VALUE` 로그 키 구현 규약 (§8-2) — 로거명·필드 포맷 확정

### v2 주요 변경 (참고: codex v1 피드백 반영)
- 스프린트 #1-A / #1-B 분리, NFR 회귀 테스트·DB 샘플 매핑 테스트, Enum 매핑 정책, 운영 안정성 섹션 신설

---

## 1. 배경 / 목표

### 배경
코드베이스 전수 조사 결과, 상태·타입 코드, UI 메시지, 설정값이 소스에 직접 박혀 있어 다음과 같은 문제가 드러남:

- **오타 유발**: `"COMPLETED"` / `"Completed"` / `"완료"` 등 표기 차이가 여러 컨트롤러·서비스·Repository에 퍼져 있음
- **타입 안전성 부재**: 문자열 리터럴 비교 (`"INSPECT".equals(docType)`)가 20+ 지점 반복
- **유지보수 비용**: UI 라벨·에러 메시지가 Java/JS/Thymeleaf에 산재
- **환경 분리 미흡**: 세션 타임아웃·업로드 리밋·로그인 실패 임계값이 단일 `application.properties`에 고정

### 재검증 결과 (2026-04-20)
Explore agent 전수 grep 근거 (별첨 survey). 카테고리별 건수:

| 카테고리 | 대략 건수 | 심각도 |
|---------|----------|--------|
| 상태·타입 문자열 중복 (DRAFT/COMPLETED/INSPECT/UPIS/KRAS 등) | 40+ | High |
| UI 라벨·에러 메시지 한글 (WorkPlanDTO switch 등) | 70+ | High |
| 매직 넘버 (로그인·세션·업로드) | 5 | Medium |
| 외부 경로·API 경로 | 6~10 | Medium |
| Naver 시크릿 키 (도메인 필드) | 1 | High (별도 보안 스프린트) |

### 목표
- **#1-A**: 핵심 상태/타입 문자열 **2종**(`DocumentStatus`, `WorkPlanType`)을 Enum으로 전환. DB 저장 호환 유지.
- **#1-B**: 에러·검증 메시지 `MessageSource` 분리, 로그인 실패 임계값 `@ConfigurationProperties` 외부화.
- **범위 외**:
  - `SystemType`(`sys_nm_en`) — 숫자값 `112` 포함. 별도 스프린트 `sys-type-normalization`
  - `InspectResult` — 자유 텍스트 혼재. 별도 스프린트 `inspect-result-schema-split`
  - Naver 시크릿 암호화 — 별도 보안 스프린트 `security-hardening-v3`
- **측정 가능한 완료 조건**: §4 NFR 참조

---

## 2. 스프린트 분할

codex 검토 의견에 따라 본 리팩터를 2단계로 나눈다.

### 스프린트 #1-A: Enum 전환
- 대상: **`DocumentStatus`**, **`WorkPlanType`** 2종 (Pre-flight 결과 반영 — v6에서 축소)
- 파일 수정: 약 **20~30개** (Entity/DTO/Controller/Service/Repository)
- PR/릴리즈 단위 1회
- 완료 후 #1-B 착수 (중간 안정화 기간 확보)

### 스프린트 #1-B: MessageSource + ConfigurationProperties
- 대상: 에러·검증 메시지 10~20개, 로그인 실패 임계값
- 파일 수정: 약 15~25개 (Service/Exception/Config)
- #1-A 병합·운영 안정화 확인 후 시작

---

## 3. 기능 요건 (FR)

### #1-A Enum 전환
| ID | 내용 |
|----|------|
| FR-A1 | `com.swmanager.system.constant.enums` 패키지에 **`DocumentStatus`**, **`WorkPlanType`** Enum 생성. 각 Enum은 `label()` 메서드를 갖고 기존 DB 문자열과 1:1 매핑. `DocumentStatus`={DRAFT, COMPLETED}, `WorkPlanType`={INSPECT, COMMENCE, COMPLETION, INTERIM, FAULT, SUPPORT, INSTALL, PATCH, ...} (Pre-flight 실값 + 현재 `DocumentDTO.getDocTypeLabel` 스위치문 조사 결과 기반 전체 열거). |
| FR-A2 | Java Controller/Service/Repository/DTO에서 해당 문자열 리터럴을 Enum 참조로 치환. |
| FR-A3 | Entity ↔ DB 매핑은 **§5-6 매핑 정책 + §5-6-2 책임 경계표**를 따름 (기본 `@Enumerated(EnumType.STRING)`, 공백/대소문자 정규화가 필요한 경우에만 `AttributeConverter`. 별칭/레거시 값 해석은 AttributeConverter 책임 아님). |
| FR-A4 | JPQL/`@Query`의 `= 'COMPLETED'` 같은 리터럴을 파라미터 바인딩(`:status`)으로 전환. Enum을 바인딩 값으로 전달. |
| FR-A5 | API 입력 Enum 바인딩은 **§5-7 정책**을 따른다 (JSON body = `@JsonCreator`, query/path = `Converter<String, Enum>` 등록). |
| FR-A6 | 템플릿/JS에서 필요한 **코드→표시명 맵**을 서버 Controller에서 `@ModelAttribute`로 주입하여 템플릿 리터럴 감소. (단, JS/Thymeleaf 리터럴 전량 제거는 범위 외 — 스프린트 #3로 이월) |

### #1-B MessageSource + ConfigurationProperties
| ID | 내용 |
|----|------|
| FR-B1 | `src/main/resources/messages.properties` (기본, 한글) 및 `messages_en.properties`(영문 뼈대) 생성. Spring `MessageSource` 빈 설정. |
| FR-B2 | 서비스/예외 계층의 한글 메시지 10~20개를 키로 이동. **키 네이밍 규칙**은 §5-8 참조. |
| FR-B3 | `NoSuchMessageException` fallback 정책 — 키 누락 시 `"{missing:key}"` 문자열 반환 + `messages` 로거에 WARN 1회 기록 (key 값 포함). |
| FR-B4 | `LoginAttemptService`의 `MAX_FAILED_ATTEMPTS`, `LOCK_TIME_MINUTES`를 `SecurityLoginProperties`(`@ConfigurationProperties(prefix="security.login")`)로 외부화. `@Validated` + `@Min(1) @Max(100)` 범위 검증 적용. |
| FR-B5 | 기본값은 `application.properties`에 두고, 환경별 오버라이드는 `application-local.properties.example`에 샘플 추가. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공. |
| NFR-2 | 서버 재기동 후 정상 부팅 + 주요 화면(로그인, 대시보드, 문서관리, 점검내역서 상세/미리보기/PDF, 업무관리) 회귀 체크리스트 통과. |
| NFR-3 | **DB 샘플 매핑 테스트** — 기존 DB에 저장된 `"COMPLETED"`, `"DRAFT"`, `"INSPECT"`, `"COMMENCE"`, `"COMPLETION"`, `"INTERIM"` 등이 Enum으로 역직렬화되고, 다시 저장 시 동일 문자열로 기록되는지 단위 테스트(`@DataJpaTest`) 작성 (최소 **4건**, Enum 종별 최소 2건 이상). |
| NFR-4 | **Enum 리터럴 제거 검증** — 아래 `rg` 명령을 그대로 복사해 실행한 결과가 **0건**이어야 통과. 검증 대상은 본 스프린트 대상 2개 Enum(`DocumentStatus`·`WorkPlanType`)의 값만. 정규식은 ripgrep 기본(`-E`) 문법의 **리터럴 파이프 `|`** 사용:<br><br>```bash<br>rg -n --type java '"(DRAFT\|COMPLETED\|INSPECT\|COMMENCE\|COMPLETION\|INTERIM\|FAULT\|SUPPORT\|INSTALL\|PATCH)"' src/main/java --glob '!**/constant/enums/**' --glob '!**/test/**'<br>```<br><br>(**표 셀 안이라 `\|`로 이스케이프 표기**; 실제 실행 시 `\|` 대신 **리터럴 `|`** 로 타이핑한다. 즉 실제 쉘에서 치는 문자열은 `"(DRAFT|COMPLETED|INSPECT|...|PATCH)"`.)<br><br>검증 예시: `echo '"COMPLETED"'` → 매치 / `echo '"ABC"'` → 미매치. |
| NFR-5 | #1-B 후 `rg -n '"문서를 찾을 수 없습니다"' src/main/java` 결과 0건. 메시지가 `messages.properties` 키로 이동. |
| NFR-6 | i18n 기본 로케일 `ko`로 기존과 동일한 문구 노출. |
| NFR-7 | `SecurityLoginProperties` `@Validated` 위반 시 앱 부팅 실패 (예: `max-failed-attempts=0` 거부). |
| NFR-8 | 변경 파일 수 #1-A **30개 이내**, #1-B 25개 이내. 합쳐서 **50파일 이내** (v6 축소). |
| NFR-9 | **회귀 테스트 필수 시나리오 (5개 고정) + 최소 커버리지 기준**: 아래 5건을 반드시 통과해야 하며, 각 시나리오는 **서비스 계층 단위 테스트 + 컨트롤러 계층 MockMvc 테스트 각 1건** 이상으로 구성한다.<br>① **로그인 잠금**: `max-failed-attempts` 초과 시 계정 잠금, `lock-time-minutes` 경과 후 해제<br>② **문서 상태변경**: `DRAFT → COMPLETED` 전이, `COMPLETED` 중복 커밋 방지<br>③ **점검결과 저장/조회**: 섹션별(DB/AP/DBMS/GIS/APP) 저장 후 Enum 역직렬화로 조회<br>④ **점검내역서 PDF**: 기존 INSPECT 문서 PDF 생성 성공 (파일 사이즈 > 0)<br>⑤ **오류 핸들링**: 잘못된 Enum 값(`"???"`) 입력 시 `400 Bad Request` + 허용값 목록 응답<br>각 시나리오별 서비스/컨트롤러 각 1건 = **총 10건**. 필요 시 변형 케이스 추가 가능(상한 15건). |

---

## 5. 의사결정 / 우려사항

### 5-1. Enum 범위 — ✅ **P1 2종만 착수** (v6 재조정)
Pre-flight 결과 반영 — 본 스프린트는 `DocumentStatus`, `WorkPlanType` 2종만 처리.
- `SystemType` → 별도 스프린트(`sys-type-normalization`): `"112"` 숫자값 때문에 Enum name 불가 → 정제 또는 별칭 규칙 필요
- `InspectResult` → 별도 스프린트(`inspect-result-schema-split`): `result` 컬럼을 `result_code` + `result_text` 로 분리하는 스키마 변경 필요

### 5-2. i18n 범위 — ✅ 서버측 한글 메시지 우선
JS/Thymeleaf 리터럴까지 i18n 이동은 번들 관리 부담이 크므로 제외. 스프린트 #3 후보.

### 5-3. DB 호환성 — ✅ §5-6 매핑 정책 + 데이터 점검 선행
기존 DB VARCHAR 그대로 사용. 쓰기 경로 정규화와 매핑 정책으로 리스크 차단.

### 5-4. Naver 시크릿 키 — ❌ 제외
보안 전용 스프린트(`security-hardening-v3`)로 분리.

### 5-5. DB팀 자문 결과 (Pre-flight SQL — **v6 실측 반영**)

**실행 결과**: `docs/exec-plans/hardcoding-preflight-result.md` (2026-04-20)

#### 주요 발견 요약

| 컬럼 | 값 분포 | 판정 | 조치 |
|------|---------|------|------|
| `tb_document.status` | `COMPLETED`(12), `DRAFT`(3) | ✅ 깔끔 | Enum 전환 가능 — `DocumentStatus` |
| `inspect_report.status` | `COMPLETED`(7), `DRAFT`(3) | ✅ 깔끔 | Enum 전환 가능 — `DocumentStatus` 공유 |
| `tb_document.doc_type` | INSPECT(8), COMMENCE(3), COMPLETION(2), INTERIM(2) | ✅ 깔끔 | Enum 전환 가능 — `WorkPlanType`. 코드상 추가 타입(FAULT/SUPPORT/INSTALL/PATCH 등) 개발계획서에서 최종 확정 |
| `sw_pjt.sys_nm_en` | UPIS(297), KRAS(279), SC(5), IPSS(4), LTCS(3), **112**(3), MPMS(3), APIMS(2) | ⚠️ 숫자값 포함 | **Enum 전환 제외** — 별도 스프린트로 이월 |
| `inspect_check_result.result` | 정상(313), (공백 165), 1%/70%/21%/... (퍼센트), "특이사항 없음", "159GB / 271GB 가용" 등 14종 | ⚠️ 자유 텍스트 | **Enum 전환 제외** — 스키마 분리 후 별도 처리 |
| `sw_pjt.sys_nm_en` 공백/NULL | 공백 0건, NULL 0건 | ✅ | 대소문자도 전부 대문자 |
| `tb_document.status/doc_type` NULL | 각 0건 | ✅ | |

#### 착수 전제
- 본 스프린트 대상 2개 Enum(`DocumentStatus`, `WorkPlanType`) 매핑 대상 컬럼은 모두 **Enum name 규칙 적합** + **공백/대소문자 이슈 0건**.
- ∴ `@Enumerated(EnumType.STRING)` **직결 가능**, `AttributeConverter` 불필요. 별도 언급 전까지 이 전제를 유지.

#### 실행 방법 (재실행용)
개발계획서 착수 후 DB가 변경될 수 있으므로, 개발 직전에 동일 SQL을 재실행하여 값이 변하지 않았음을 검증한다. 실행기는 `docs/exec-plans/hardcoding-preflight-runner.java`.

```bash
JAR=~/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
DB_PASSWORD='<비밀번호>' java -cp "$JAR" docs/exec-plans/hardcoding-preflight-runner.java
# → docs/exec-plans/hardcoding-preflight-result.md 갱신
```

참고: 원본 SQL은 아래 블록 유지 (재현 가능성).

```sql
-- 1) 값 분포
SELECT status, COUNT(*) FROM tb_document GROUP BY status;
SELECT sys_nm_en, COUNT(*) FROM sw_pjt GROUP BY sys_nm_en;
SELECT doc_type, COUNT(*) FROM tb_document GROUP BY doc_type;
SELECT section, COUNT(*) FROM inspect_check_result GROUP BY section;
SELECT result, COUNT(*) FROM inspect_check_result GROUP BY result;

-- 2) 공백/대소문자/NULL 진단
SELECT sys_nm_en, TRIM(sys_nm_en) AS trimmed, LOWER(sys_nm_en) AS lowered, COUNT(*)
  FROM sw_pjt
 GROUP BY sys_nm_en, TRIM(sys_nm_en), LOWER(sys_nm_en);
SELECT COUNT(*) FROM sw_pjt WHERE sys_nm_en IS NULL OR sys_nm_en = '';
SELECT COUNT(*) FROM tb_document WHERE status IS NULL OR status = '';

-- 3) 쓰기 경로 정규화 여부 점검 항목 (코드 조사)
-- Java 코드에서 INSERT/UPDATE 경로의 값 설정 지점을 모두 나열하고,
-- 대소문자·공백·한글(완료/작성완료 등) 혼입 여부를 수동 점검한다.
-- 개발계획서에 체크리스트로 첨부.
```

### 5-6. Enum 매핑 원칙

#### 5-6-1. 기본 선택 기준
- **원칙**: 신규/정상 데이터는 `@Enumerated(EnumType.STRING)` 사용.
- **예외(`AttributeConverter`)**: **공백/제로폭 문자 등 무의미 문자만** 전처리해야 하는 경우에만 사용. (`trim()` + 대소문자 정규화)
- **동일 컬럼에 `@Enumerated`와 `@Convert` 혼용 금지.** 컬럼별로 하나만 선택하고, 선택 근거를 Entity 필드 주석으로 남긴다.
- Pre-flight 점검 결과 예외 컬럼은 개발계획서에 명시.

#### 5-6-2. 책임 경계표 (별칭 vs 정규화 vs 레거시 — v5 충돌 해소)

본 스프린트는 "별칭 해석", "문자 정규화", "레거시 값 정제" 세 가지를 **다른 층위**에서 처리한다. 한 층위에서만 다루고 다른 층위에서는 중복 처리하지 않는다.

| 책임 | 담당 층위 | 허용 범위 | 금지 |
|------|-----------|-----------|------|
| **별칭 해석** (`upis` → `UPIS`, `Upis` → `UPIS`) | **API 입력단만** (Enum 내부 정적 `ALIASES` 맵을 `@JsonCreator`에서 조회) | JSON body: `@JsonCreator` 단독<br>query/path: `ConverterFactory`의 대소문자 무시만(`ALIASES` 조회 안 함) | `@JsonAlias` 미사용. AttributeConverter·Service·Repository에서 별칭 해석 금지 |
| **문자 정규화** (공백·제로폭 문자 제거, 대소문자 통일) | **AttributeConverter** (DB 읽기 시만) | `trim()` + `toUpperCase()` 후 표준 Enum 반환 | DB 쓰기 시에는 항상 Enum `name()` 표준 문자열만 기록. 정규화 이상의 매핑(별칭/레거시) 금지 |
| **레거시 값 정제** (`"완료"` → `"COMPLETED"`, 한글→영문 등) | **Pre-flight 데이터 정제 스크립트** (§5-5, §8-4) | 개발 착수 전에 SQL로 일괄 migrate. 스크립트는 `docs/product-specs/hardcoding-data-cleanup.md`에 기록 | 코드(AttributeConverter·Converter·Service) 내에서 런타임 해석 금지 |

**요약 원칙**:
- DB에는 **항상 표준 Enum name**만 존재해야 한다. 예외 상황은 **Pre-flight로 해소** 후 코드 작업 착수.
- `AttributeConverter`는 별칭 해석기가 아니다. 공백/대소문자 정규화 범위를 넘어서면 `IllegalArgumentException` 발생 + `UNKNOWN_ENUM_VALUE` 로그(§8-2) 후 상위 전파.
- 별칭 맵(`ALIASES`)의 유일한 소스는 Enum 타입 자체. API 입력단 중 `ConverterFactory`(query/path)는 대소문자 무시만 하고 `ALIASES`를 조회하지 않는다 (§5-7-2).

### 5-7. API 입력 Enum 바인딩 정책 (v4 확정)

#### 5-7-1. 입력 위치별 바인딩 방식 (단일 소스 원칙)

| 입력 위치 | 바인딩 방식 | 구현 규약 |
|-----------|-------------|-----------|
| **JSON request body** | Jackson 역직렬화 | Enum에 `@JsonCreator public static X fromString(String v)` 단독 사용. 내부에서 `trim()` + `toUpperCase()` → `valueOf` → 실패 시 Enum 내부 `ALIASES` 맵 조회 (§5-7-2 구현 예시). **`@JsonAlias`는 사용하지 않는다** (혼용 금지). |
| **Query parameter** `?status=COMPLETED` | Spring `Converter<String, Enum>` | `com.swmanager.system.config.EnumConversionConfig`에서 `WebMvcConfigurer.addFormatters(FormatterRegistry)`로 `StringToEnumConverterFactory` 등록. `ConverterFactory`는 `trim()` + `toUpperCase()` 후 `valueOf`만 수행 — **별칭 맵(`ALIASES`)을 조회하지 않는다**. 별칭은 JSON body 전용. |
| **Path variable** `/document/{status}` | 동일 `Converter<String, Enum>` | 위와 공통. |

#### 5-7-2. 별칭(Alias) 단일 소스 원칙 — `@JsonCreator` 일원화 (v5 확정)

**구현 방식: `@JsonCreator` 단독 사용** (Jackson 기본 역직렬화와 `@JsonAlias` 혼용은 금지 — 프레임워크 동작 충돌 우려).

- 별칭 테이블은 각 Enum 타입 내부에 **정적 `Map<String, ThisEnum> ALIASES`** 로 선언한다 (예: v6 대상 2개 Enum은 Pre-flight 결과 별칭이 **없으므로 빈 맵**으로 시작. 향후 외부 API 입력에서 대소문자 변형이 들어올 경우만 추가).
- `@JsonCreator public static X fromString(String v)`은 다음 순서로 매칭:
  1. `null`/빈 문자 → `null` 반환 (또는 예외, Enum별 정책 주석에 명시)
  2. `v.trim().toUpperCase()`로 정규화 → `valueOf()` 시도
  3. 실패 시 `ALIASES` 맵에서 원본 `v`(또는 `v.trim()`) 조회
  4. 전부 실패 → `IllegalArgumentException("ENUM_VALUE_NOT_ALLOWED: " + v)` 발생
- **`@JsonAlias` 어노테이션은 사용하지 않는다** (Option B와 혼용 금지).
- `ConverterFactory`(query/path)는 `toUpperCase()` 후 `valueOf`만 수행 — **별칭 맵을 조회하지 않는다**. 즉 별칭은 **JSON body에서만** 허용.
- `AttributeConverter`·Service 계층에서 별칭 매핑 중복 처리 금지. 발견 시 코드 리뷰 반려.
- 레거시 값(예: `"완료"` → `COMPLETED`)은 **별칭이 아닌 데이터 정제 대상**. Pre-flight(§5-5) 결과에서 마이그레이션 스크립트로 처리하며, 코드(ALIASES 맵 포함)에 **절대 넣지 않는다**.

**구현 예시** (`DocumentStatus.java`):

```java
public enum DocumentStatus {
    DRAFT, COMPLETED;

    // 별칭 단일 소스 (JSON body 전용). 레거시 한글 값은 여기 넣지 않는다.
    private static final Map<String, DocumentStatus> ALIASES = Map.of(
        // 현재 추가 별칭 없음. 향후 필요 시 대소문자 변형만 등록 가능.
    );

    @JsonCreator
    public static DocumentStatus fromString(String v) {
        if (v == null || v.isBlank()) return null;
        String norm = v.trim().toUpperCase();
        try {
            return DocumentStatus.valueOf(norm);
        } catch (IllegalArgumentException ignore) {
            DocumentStatus aliased = ALIASES.get(v.trim());
            if (aliased != null) return aliased;
            throw new IllegalArgumentException("ENUM_VALUE_NOT_ALLOWED: " + v);
        }
    }
}
```

#### 5-7-3. 실패 응답 포맷

```json
{
  "timestamp": "2026-04-20T10:15:30",
  "status": 400,
  "error": "BadRequest",
  "code": "ENUM_VALUE_NOT_ALLOWED",
  "field": "status",
  "enumType": "com.swmanager.system.constant.enums.DocumentStatus",
  "message": "허용되지 않는 값입니다.",
  "allowed": ["DRAFT", "COMPLETED"],
  "path": "/document/api/inspect-report"
}
```

#### 5-7-4. `allowed` 배열 생성 규칙 (예외 타입별 명문화)

`GlobalExceptionHandler`는 두 예외를 아래 규칙으로 처리한다.

| 예외 타입 | `enumType` 추출 | `allowed` 생성 | 비고 |
|-----------|-----------------|----------------|------|
| `MethodArgumentTypeMismatchException` | `ex.getRequiredType()` 이 Enum이면 해당 클래스의 풀네임 | `Class.getEnumConstants()` 의 `name()` 배열 | query/path 바인딩 실패 시 발생 |
| `HttpMessageNotReadableException` | 원인이 `InvalidFormatException`이고 `getTargetType()`이 Enum이면 해당 클래스 | 위와 동일 | JSON body 역직렬화 실패 시 발생 |
| 위 두 경우 모두 **타입 추적 실패** | `"UNKNOWN"` | `allowed` 필드 **생략** (JSON 응답에 key 없음) | 희귀 케이스. 로그로 원인 추적 |

구현은 `EnumErrorResponseFactory` 1개 유틸로 통합하여 핸들러가 호출한다. 유틸 단위 테스트 2건(MATM 정상 케이스 / HMNR 정상 케이스)을 NFR-9 오류 핸들링 시나리오에 포함.

### 5-8. MessageSource 키 네이밍 규칙
- 형식: `{계층}.{도메인}.{상황}` (예: `error.document.not_found`, `error.inspect.report_invalid_state`)
- 소문자·언더스코어, 점 구분.
- 매개변수는 `{0}`, `{1}` 인덱스 치환 방식. 이름 치환(`{docId}`)은 금지(Java 기본 `MessageFormat` 호환 이슈).

---

## 6. 영향 범위

| 계층 | 파일 | 유형 | 스프린트 |
|------|------|------|----------|
| 신규 Enum | `constant/enums/DocumentStatus.java`, `WorkPlanType.java` (v6: 2종) | 신규 | #1-A |
| 신규 Converter | (필요 시) `constant/converter/XxxConverter.java` | 신규 | #1-A |
| 신규 Config | `config/EnumConversionConfig.java` (StringToEnumConverterFactory 등록) | 신규 | #1-A |
| 신규 유틸 | `exception/EnumErrorResponseFactory.java` (`allowed`/`enumType` 추출 로직 단일화) | 신규 | #1-A |
| 신규 Config | `config/SecurityLoginProperties.java` | 신규 | #1-B |
| 신규 i18n | `resources/messages.properties`, `messages_en.properties` | 신규 | #1-B |
| 신규 테스트 | `constant/enums/*ConverterTest.java`, NFR-9 회귀 시나리오 5×2 | 신규 | #1-A·B |
| 수정 Controller | `DocumentController`, `InspectReport*`, `WorkPlanController`, `SwController` 등 | 수정 | #1-A |
| 수정 Service | `DocumentService`, `InspectReportService`, `WorkPlanService`, `PerformanceService`, `LoginAttemptService` 등 | 수정 | #1-A·B |
| 수정 Repository | `@Query` 내 리터럴 → 파라미터 바인딩 | 수정 | #1-A |
| 수정 Entity/DTO | `Document`, `InspectReport`, `WorkPlan` 및 관련 DTO | 수정 | #1-A |
| 수정 Exception | `GlobalExceptionHandler`에 `MessageSource` 주입 + `MethodArgumentTypeMismatchException`/`HttpMessageNotReadableException` 처리 | 수정 | #1-A·B |
| 문서 | `docs/generated/audit/2026-04-18-system-audit.md` 체크박스 갱신 | 수정 | #1-A·B |

**합계 (v6)**: 신규 7~10파일, 수정 35~45파일. DB 스키마 변경 없음. API JSON 필드값은 Enum name 문자열로 유지(기존 호환).

---

## 7. 리스크 평가

| ID | 리스크 | 수준 | 완화책 |
|----|--------|------|--------|
| R-1 | DB에 Enum에 없는 값 존재 | **낮음 (v6)** | Pre-flight(§5-5) 재실행 결과 본 스프린트 2개 Enum 대상 컬럼은 **문제 값 0건** 확인. 개발 직전 재실행으로 최종 검증. 변동 시 **§5-6-2 책임 경계표**에 따라 처리. |
| R-2 | API 입력 Enum 바인딩 실패 (대소문자/별칭) | 중간 | §5-7 정책. JSON body `@JsonCreator`, query/path `Converter<String, Enum>`. 실패 시 표준 `400` 응답(§5-7 샘플 JSON). |
| R-3 | JPQL `@Query` 리터럴 파라미터화 시 실행계획·인덱스 변화 | 낮음 | 변경 전/후 `EXPLAIN`으로 주요 쿼리 5건 비교. 인덱스가 태우는 컬럼은 DB팀과 재확인. |
| R-4 | 메시지 키 누락 시 런타임 노출 | 낮음 | FR-B3 fallback + `messages` 로거 WARN. 테스트로 키 존재 검증. |
| R-5 | `@ConfigurationProperties` 오설정 | 낮음 | `@Validated` + NFR-7로 부팅 차단. |
| R-6 | 작업 범위 팽창 | 중간 | FR-A1~A6 / FR-B1~B5 스코프 고정. #1-A 완료 후 #1-B 착수. |
| R-7 | Thymeleaf 렌더링 누락 (코드→한글 매핑 서버 주입 실패) | 중간 | NFR-9 시나리오 ④ PDF + 회귀 체크리스트에 주요 화면 포함. codex 테스트팀 최종 점검. |
| R-8 | 롤백 어려움 | 중간 | §8-3 롤백 절차. 스프린트 #1-A / #1-B를 개별 PR/커밋으로 유지해 **revert 단위** 분리. |
| R-9 | 기존 커밋 충돌 | 낮음 | 시작 전 `git pull --rebase`, 중간 push는 codex 검토 후에만. |

---

## 8. 운영 안정성

### 8-1. 배포 전 체크리스트
1. Pre-flight SQL(§5-5) 실행 → 예상 외 값 0건 또는 `AttributeConverter` 매핑 완료 확인. **결과 개발계획서에 첨부**.
2. `./mvnw test` 통과 (NFR-3, NFR-9 포함, 최소 14건).
3. 로컬에서 회귀 체크리스트 수동 통과 (NFR-9 5개 시나리오 + NFR-2 주요 화면 5개).

### 8-2. 배포 후 모니터링 — `UNKNOWN_ENUM_VALUE` 로그 규약
구현 규약:

- **로거명**: `com.swmanager.system.monitoring.EnumBindingMonitor`
- **레벨**: `ERROR`
- **구조화 필드**: MDC에 `eventKey=UNKNOWN_ENUM_VALUE`, `enumType={클래스풀네임}`, `inputValue={사용자 입력}`, `endpoint={HTTP 메서드 + URI}`, `userid={현재 사용자ID 또는 anonymous}` 설정 후 logback JSON appender로 기록
- **발생 위치**:
  - `GlobalExceptionHandler`에서 `MethodArgumentTypeMismatchException`/`HttpMessageNotReadableException` 처리 시 로그 기록
  - `AttributeConverter.convertToEntityAttribute()`에서 매핑 실패 시 동일 키로 기록

### 8-3. 롤백 절차 — **자동 감지 + 수동 승인**

**자동 감지 규칙**:
- 배포 후 **24시간** 동안 `eventKey=UNKNOWN_ENUM_VALUE` 로그가 **10회 이상** 관측되면 모니터링 알림 발송.
- Access log에서 `400 BadRequest` 응답률이 **직전 24시간 대비 +50%** 증가 시 동일 알림.

**수동 승인 롤백**:
1. **알림 수신** → on-call 엔지니어가 원인 분석 (로그 `inputValue`, `endpoint` 기반)
2. **판단 시간**: 알림 후 **30분 이내** 롤백 여부 결정
3. **롤백 체크리스트 (3개)**:
   - ☐ 현재 커밋 SHA 확인 (`git log -1 --pretty=%H`)
   - ☐ 직전 안정 커밋으로 revert PR 작성 (#1-B 단독 롤백 시 해당 merge commit만 revert)
   - ☐ 재배포 후 로그인·문서관리·점검내역서 3개 화면 수동 검증
4. **승인자**: 프로젝트 책임자(사용자) 1인 승인 후 실제 배포

**롤백 영향도 요약**:
- **#1-A만 롤백**: DB 변경 없음, 데이터 영향 없음
- **#1-B만 롤백**: `messages.properties` 삭제 시 하드코딩 문자열 복귀, `SecurityLoginProperties` 기본값(`5/15`)이 그대로 적용되어 동작 동일
- **완전 롤백**: #1-B → #1-A 순으로 역순 revert

### 8-4. 후속 백로그 (v6 확정)

본 스프린트 #1 완료 시 다음 3개 후속 트랙이 자동 생성된다.

| 후속 스프린트 | 입력 | 주요 내용 |
|---------------|------|-----------|
| **`sys-type-normalization`** | Pre-flight 결과: `sys_nm_en`에 `"112"` 숫자값 | 정제 후보: (a) `"112"` → `"E112"` 리네이밍 마이그레이션, (b) 전용 코드 테이블 분리, (c) `SystemType` Enum 없이 VARCHAR + 검증 상수 클래스 유지. 옵션 선택은 DB팀 자문 필요. |
| **`inspect-result-schema-split`** | Pre-flight 결과: `result`에 자유 텍스트 혼재 165건 | 스키마 분리: `result_code`(Enum: NORMAL/INSPECT/기타) + `result_text`(VARCHAR 자유 텍스트) 컬럼 추가. 기존 `result` 값의 분류·마이그레이션. |
| **`hardcoding-data-cleanup`** | Pre-flight에서 추후 발견될 정제 필요 항목 | 기획서 #2 또는 별도 트랙으로 이관. |

- 이 3개 트랙은 기획서 `docs/product-specs/` 하위에 스텁 문서로 생성하여 백로그 노출.

---

## 9. 승인 요청

본 기획서 v6에 대한 codex 재검토 및 사용자 최종승인을 요청합니다. (v5는 이미 codex ✅승인을 받았으나, Pre-flight 결과로 범위가 축소됨)

### 다음 절차
1. 사용자 "반영" 시 기획서 v7 작성 후 재보고
2. "최종승인" 시 → **[개발팀]** 단계 진입, `docs/exec-plans/hardcoding-improvement.md` 작성
3. 개발계획서도 codex 검토 → 사용자 최종승인 → 실제 코드 작성 (**#1-A 먼저, 이후 #1-B**)
