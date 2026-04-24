---
tags: [plan, sprint, refactor, data-cleanup]
sprint: "sys-type-normalization"
status: superseded-by-data-architecture-audit
created: "2026-04-20"
---

# [기획서 · SUPERSEDED] `sys_nm_en` 정규화 및 `SystemType` Enum 도입

> ⚠️ **이 스프린트는 [`data-architecture-audit`](./data-architecture-audit.md) 로 흡수됨 (2026-04-20).**
> 사유: 기획 단계에서 `sys_mst` 마스터 테이블(37건 등록) 존재를 미확인 상태에서 Enum 전환 방향을 권고. 전수 감사 결과 반영 후 재편성 필요. 본 기획서는 **역사적 추적용으로만 보존**.
> Pre-flight 결과([hardcoding-preflight-result.md](../dev-plans/hardcoding-preflight-result.md))는 감사 입력으로 재사용됨.


- **작성팀**: 기획팀
- **작성일**: 2026-04-20
- **선행 스프린트**: `refactor-01-hardcoding #1-A, #1-B` (완료)
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 반영)
1. **§5-2 단계적 정책 명문화** — 본 스프린트 = Enum 도입+검증 / 후속 = Entity 매핑. 전환 완료 조건 정의.
2. **§5-1 리네이밍 기준표** — `E112`/`SYS_112`/`S112` 평가축 표, 최종안 + 차선안.
3. **§5-5 사전 점검 SQL 체크리스트** — 5개 테이블 + INFORMATION_SCHEMA 전수 스캔, COUNT→UPDATE→COUNT→샘플 검증, 롤백 기준.
4. **NFR-4를 4a/4b로 분리** — 비즈니스 코드 리터럴 제거 vs Enum/테스트 whitelist. 정규식 `\b` 단어 경계.
5. **§5-3 플레이스홀더 정책 강화** — `{TBD:...}` 허용 기한(D-2), 책임자, 배포 차단 여부.
6. **§8 추가 확인 사항 답변** — API 하위호환/DB 제약/캐시-배치/멱등성/에러 스키마 충돌 5건.
- **Pre-flight 첨부**: [[hardcoding-preflight-result]] (2026-04-20)

---

## 1. 배경 / 목표

### 배경
선행 스프린트 `refactor-01-hardcoding` 기획 시 Pre-flight SQL로 `sw_pjt.sys_nm_en` 컬럼 실데이터를 조사한 결과 다음 문제 발견:

| 문제 | 영향 |
|------|------|
| `"112"` 3건 — 첫 글자가 숫자 → **Java Enum name 규칙 위반** | Enum 도입 불가, 선행 스프린트에서 제외 |
| ProcessMaster/ServicePurpose 마스터 데이터는 **5종만 등록** (`db_init_phase2.sql`) | 실제 DB 8종 중 `SC`, `LTCS`, `"112"`, `MPMS` 미등록 → 마스터/운영 데이터 **불일치** |
| `sys_nm_en` 관련 리터럴(`"UPIS"`, `"KRAS"`)이 Java 코드에 산재 | 타입 안전성 부재, 중복 |

### 실측 (Explore agent, 2026-04-20)

**DB 분포 (593건)**:
| 값 | 개수 | 마스터 등록? |
|----|------|-------------|
| UPIS | 297 | ✅ |
| KRAS | 279 | ✅ |
| SC | 5 | ❌ |
| IPSS | 4 | ✅ |
| LTCS | 3 | ❌ |
| **`112`** | **3** | ❌ + 규칙 위반 |
| MPMS | 3 | ❌ |
| APIMS | 2 | ✅ |

**코드 참조 범위** (Explore 결과):
- Entity 필드: 5개 (`SwProject`, `Infra`, `PersonInfo`, `ProcessMaster`, `ServicePurpose`)
- Repository `@Query`: 3개 메서드 (파라미터 바인딩, 리터럴 없음)
- Service/Controller 리터럴: `DocumentController.java:1097-1099` (`"KRAS"`), `HwpxExportService.java:222-225` (`"kras"/"upis"` 템플릿 매핑)
- Template 드롭다운: `doc-fault.html:85-89` 하드코딩 4종 / `admin-system-graph.html` 동적 생성
- `"112"` Java 코드 직접 참조: **없음** (리터럴 `"112"` 비교 0건)

### 목표
1. **`"112"` 3건 데이터 정제** — Enum name 규칙 위반 해소
2. **마스터 데이터 완성** — ProcessMaster/ServicePurpose에 `SC`, `LTCS`, `E112`(또는 정제결과), `MPMS` 추가
3. **`SystemType` Enum 도입** — 문자열 리터럴 치환, 타입 안전 확보
4. **후속 데이터 입력 통제** — DB 제약(CHECK 또는 코드 검증)으로 Enum 외 값 차단

---

## 2. 옵션 분석 (의사결정 요청)

선행 기획서 §8-4에서 3가지 옵션 언급. 본 기획서에서 확정.

### Option A: 데이터 정제 + `SystemType` Enum (권장)
- `"112"` 3건 → `"E112"` UPDATE
- `SystemType { UPIS, KRAS, IPSS, SC, LTCS, E112, MPMS, APIMS }` 신설
- Entity 타입: `String` 그대로 유지 (Enum 미바인딩) + Service 레벨 검증
- **이유 (String 유지)**: 5개 Entity가 이 컬럼 공유. 단기 스프린트에서 5개 모두 Enum화하면 범위↑. 본 스프린트는 **데이터 정제 + 코드 레벨 상수화** 만.
- **영향 파일**: 약 10개 (Enum 1 + Service 2 + Controller 1 + Template 1 + SQL 마이그레이션 1 + 마스터 INSERT 2)

### Option B: 상수 클래스만 (Enum 포기)
- `SystemConstants.VALID_SYSTEMS = Set.of(...)` 상수
- `"112"` 값 유지 (데이터 정제 X)
- **이유**: 최소 변경. 기존 DB 데이터 건드리지 않음.
- **단점**: 타입 안전성 없음. Enum name 규칙 위반을 회피만 하고 해결 못함. 향후 Enum 도입 여지 차단.

### Option C: 마스터 테이블 `sys_type_master` 신설 + FK
- DDL 변경 + 5개 테이블 FK 추가
- **장점**: 가장 정상적인 데이터 모델. 새 시스템 동적 추가 가능.
- **단점**: 본 스프린트 범위 초과 (DB 스키마 대규모 변경 + 5개 엔티티 관계 재설정 + 롤백 복잡). 별도 스프린트 `sys-type-master-table`로 이관 권장.

### 기획팀 권장: **Option A**
- Enum name 규칙 위반이 본 스프린트의 **1차 동기**. Option B는 이를 우회만 함.
- Option C는 DB 스키마 변경이라 안전 마진 부족. 향후 별도 스프린트.
- Option A는 **"112" 3건 정제 + Enum 상수화**로 본질적 해결. 5개 Entity 타입 변경은 후속 스프린트에 맡김 (#1-A와 유사한 "인프라 먼저, 타입 변경은 나중" 패턴).

---

## 3. 기능 요건 (FR) — Option A 기준

| ID | 내용 |
|----|------|
| FR-1 | `sw_pjt.sys_nm_en = '112'` 3건을 `'E112'`로 UPDATE하는 마이그레이션 SQL (`swdept/sql/V###_sys_nm_en_normalize.sql`) 작성·적용. 롤백 SQL 병기. |
| FR-2 | `tb_process_master`, `tb_service_purpose`에 **누락 4종** (`SC`, `LTCS`, `E112`, `MPMS`) INSERT. 각 레코드의 `process_name`/`purpose_text`는 DB팀·운영팀 확인 후 확정. 미확정 시 §5-3 플레이스홀더 정책 형식(`{TBD: SC — 운영팀 확인 필요}`) 저장 후 별도 추적. |
| FR-3 | `com.swmanager.system.constant.enums.SystemType` Enum 신설. 값: `UPIS, KRAS, IPSS, SC, LTCS, E112, MPMS, APIMS`. `@JsonCreator` 단독 (기획서 `hardcoding-improvement` §5-7-2 정책 준수). `label()` 메서드 제공. |
| FR-4 | `SystemType`에 **정적 유틸 메서드** `isValid(String code)`, `safeValueOf(String code)` 제공. Entity 타입은 `String` 유지하되 Service 입력 경로에서 호출. |
| FR-5 | `DocumentController.java:1097-1099`의 `"KRAS".equals(...)` / `.contains("KRAS")` 비교를 `SystemType.KRAS.name().equals(...)`로 치환 (리터럴 제거). |
| FR-6 | `HwpxExportService.java:222-225`의 템플릿 매핑 리터럴(`"completion_upis"`, `"completion_kras"`)도 `SystemType` 참조로 치환. |
| FR-7 | `SwService`의 `sys_nm_en` 검증 지점(있는 경우)에서 `SystemType.isValid()` 호출. 없는 값 입력 시 `IllegalArgumentException("ENUM_VALUE_NOT_ALLOWED: ...")` — 선행 스프린트 `GlobalExceptionHandler` 규약 재사용. |
| FR-8 | `doc-fault.html:85-89` 하드코딩 드롭다운은 **본 스프린트 범위 외** 유지. 다음 스프린트(i18n/프론트 정리)에서 동적 로딩으로 전환. (범위 팽창 방지) |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` BUILD SUCCESS |
| NFR-2 | 서버 재기동 후 정상 부팅. 로그인·문서관리·점검내역서 상세 스모크 통과. |
| NFR-3 | 데이터 정제 **후** Pre-flight 재실행 — `sw_pjt.sys_nm_en = '112'` **0건** 확인 (SQL `SELECT COUNT(*) FROM sw_pjt WHERE sys_nm_en = '112'`). |
| NFR-4a | **비즈니스 코드 리터럴 제거** — 아래 **T-NFR4a** 코드블록 실행. `DocumentController`/`HwpxExportService` 등 비즈니스 코드에서 시스템 코드 리터럴이 `SystemType.XXX.name()` 참조로 전환되어야 함. |
| NFR-4b | **허용 경로(whitelist)** — `SystemType.java` Enum 선언, 테스트 파일, `messages.properties`/`db_init_phase2.sql` 등 **문자열 고정 필요 파일**은 허용. NFR-4a grep 제외 글롭에 반영. |
| NFR-5 | ProcessMaster/ServicePurpose 등록 — `SELECT COUNT(DISTINCT sys_nm_en) FROM tb_process_master` = 8 확인. |

**T-NFR4a 코드블록** (표 셀 밖, 리터럴 `|` 사용, 단어 경계 `\b` 적용):

```bash
rg -n --type java -P '"\b(UPIS|KRAS|IPSS|SC|LTCS|E112|MPMS|APIMS)\b"' \
  src/main/java \
  --glob '!**/constant/enums/**' \
  --glob '!**/test/**'
```

**Pass 기준**: 0 hits. (테스트·Enum 선언·마이그레이션 SQL 등 whitelist는 glob 제외.)
| NFR-6 | Enum 라운드트립 테스트 — `SystemType.valueOf("UPIS")`, `.valueOf("E112")` 등 8개 모두 정상 파싱. 단위 테스트 1건. |
| NFR-7 | 기존 583건 레코드 무손상 — 마이그레이션 전후 `SELECT COUNT(*) FROM sw_pjt` 동일. |
| NFR-8 | 변경 파일 수 **10개 이내** (Explore 추정치 기반). |

---

## 5. 의사결정 / 우려사항

### 5-1. `"112"` 정제 방향 — ✅ **`E112` 채택** (비교 기준표)

| 후보 | Java Enum 규칙 | 의미 명확성 | 기존 관례 | DB 제약(VARCHAR 길이) | 마이그레이션 난이도 | 종합 |
|------|---------------|-------------|----------|----------------------|----------------------|------|
| **`E112`** ✅ 최종안 | OK (첫글자 `E`) | 낮음 — "E"가 단순 접두사 | 없음 (기존 코드 체계 부재) | 4자, 기존 6자 이내 모두 커버 | 낮음 (UPDATE 3건) | **채택** |
| `SYS_112` 차선안 | OK | 중간 — "SYS_" 접두사 의미 있음 | 없음 | 7자, 여전히 기존 6자 이내 포함 실패 → 일부 컬럼 확장 필요 가능 | 낮음 | 가독성 ↑, 길이 리스크 |
| `S112` | OK | 낮음 | 없음 | 4자 | 낮음 | `E112`와 동급, 특별 이유 없어 기각 |

- **채택 근거**: 기존 `sys_nm_en` 컬럼 VARCHAR 길이가 기타 값들과 비슷한 4~5자 범위. `E112`가 **길이 영향 최소**·**마이그레이션 영향 0**. "E"는 이후 추가되는 특수 접두사(기관코드 `E`xternal 등)로 의미 부여 여지 있음.
- **차선 `SYS_112`**: `E112`가 의미 불명확 문제 있으면 차선. 단 컬럼 길이 확인 선행.
- **삭제 옵션**: 3건 레코드 손실 리스크로 기각.

### 5-2. 단계적 정책 명문화 — ✅ 본 스프린트 = 도입+검증, 후속 = Entity 매핑 (v2 codex 권장 #1)

본 스프린트와 `refactor-01-hardcoding #1-A`의 **정합성** 유지:

| 단계 | 범위 | 완료 조건 |
|------|------|----------|
| **본 스프린트** (`sys-type-normalization`) | `SystemType` Enum **도입** + **입력 검증 유틸** + **Service/Controller 리터럴 치환** + 데이터 정제 | 코드 리터럴 `"UPIS"` 등 0건 (NFR-4a). Entity 타입은 `String` 유지. |
| **후속 스프린트** (`sys-type-entity-migration`) | 5개 Entity 필드 `String → SystemType` `@Enumerated(STRING)` 매핑 전환 | 5개 Entity + Repository `@Query` + DTO 매핑 전환 완료. JPA 전체 테스트 통과. |

**전환 완료 조건** (후속 스프린트):
- 모든 JPA 필드 타입 변경 완료 + Repository `@Query` 영향 분석 + DTO 직렬화 호환 확인
- #1-A가 처리한 `DocumentStatus`/`DocumentType` 전환과 **동일 패턴** (인프라 먼저, 매핑은 후속)

**이유**: 5개 Entity(`SwProject`, `Infra`, `PersonInfo`, `ProcessMaster`, `ServicePurpose`)가 해당 컬럼 공유 + JPA Criteria/Specification 의존성 확인 필요 → 한 스프린트 내 모두 처리 시 범위 팽창 + 회귀 리스크 증가.

### 5-3. 마스터 데이터 미정 값 — 플레이스홀더 정책 (v2 codex 권장 #5)

`SC`, `LTCS`, `E112`, `MPMS` 의 `process_name`/`purpose_text` 미확정 건.

**플레이스홀더 정책**:
- **형식**: `{TBD: {시스템코드} — 운영팀 확인 필요}` (예: `{TBD: SC — 운영팀 확인 필요}`)
- **허용 기한**: **릴리스 D-2** (배포 2영업일 전까지 실제 값 확정 필수).
- **책임자**: **운영팀 → 기획팀 통보 → 개발팀 반영**. 기획팀이 추적 책임자.
- **미확정 시 배포 차단**: 배포일 기준 D-1까지 `{TBD:` 문자열이 DB에 1건이라도 남아 있으면 **배포 중단 + 운영팀 에스컬레이션**. 체크 쿼리:
  ```sql
  SELECT COUNT(*) FROM tb_process_master WHERE process_name LIKE '{TBD:%';
  SELECT COUNT(*) FROM tb_service_purpose WHERE purpose_text LIKE '{TBD:%';
  -- 두 쿼리 합계 0건이어야 배포 진행.
  ```
- **책임 경계**: 미확정 상태에서 운영은 가능(플레이스홀더 표시되더라도 기능 영향 없음 — 텍스트만 임시), 배포 전 확정 의무.

### 5-4. `SystemType` Enum 확장 가능성 — 중간
- 현재 8종. 향후 정부 시스템 추가 시 Enum 값 추가 필요 (코드 변경).
- 변경 빈도가 낮아 (몇 년 단위) 본 스프린트 범위에서 Enum 채택 합리적.
- 장기적으로 **마스터 테이블(Option C)로 전환** 필요 — 별도 스프린트 `sys-type-master-table` 후보.

### 5-5. DB팀 자문 — 사전 점검 SQL 체크리스트 (v2 codex 권장 #3)

#### 5-5-1. INFORMATION_SCHEMA 기반 `sys_nm_en` 유사 컬럼 전수 스캔
```sql
-- 1) sys_nm_en 정확히 일치하는 컬럼
SELECT table_schema, table_name, column_name, data_type, character_maximum_length
  FROM information_schema.columns
 WHERE column_name = 'sys_nm_en'
   AND table_schema = current_schema();

-- 2) sys_nm_* 유사 컬럼 (놓친 곳 탐지)
SELECT table_schema, table_name, column_name, data_type
  FROM information_schema.columns
 WHERE (column_name LIKE 'sys_nm%' OR column_name LIKE 'sys_type%' OR column_name LIKE '%_sys_nm_en')
   AND table_schema = current_schema();
```
기대 결과: 5개 테이블(`sw_pjt`, `tb_infra_master`, `ps_info`, `tb_process_master`, `tb_service_purpose`) + 유사 컬럼 추가 발견 시 본 스프린트 범위 확장 또는 후속 이관.

#### 5-5-2. `"112"` 존재 여부 사전 COUNT (5개 테이블)
```sql
SELECT 'sw_pjt' AS t, COUNT(*) AS n FROM sw_pjt WHERE sys_nm_en = '112'
UNION ALL SELECT 'tb_infra_master', COUNT(*) FROM tb_infra_master WHERE sys_nm_en = '112'
UNION ALL SELECT 'ps_info', COUNT(*) FROM ps_info WHERE sys_nm_en = '112'
UNION ALL SELECT 'tb_process_master', COUNT(*) FROM tb_process_master WHERE sys_nm_en = '112'
UNION ALL SELECT 'tb_service_purpose', COUNT(*) FROM tb_service_purpose WHERE sys_nm_en = '112';
```
**기대**: `sw_pjt` 3건, 나머지 0건. 1~4가 0 아니면 마이그레이션 SQL에 해당 테이블 UPDATE 추가.

#### 5-5-3. 마이그레이션 실행 순서 (멱등성 보장)
```sql
BEGIN;
-- (a) 사전 COUNT 기록 (로그)
SELECT 'before: sw_pjt=' || COUNT(*) FROM sw_pjt WHERE sys_nm_en = '112';

-- (b) UPDATE (5개 테이블 모두. WHERE 조건으로 이미 'E112'면 건드리지 않음 → 멱등)
UPDATE sw_pjt            SET sys_nm_en = 'E112' WHERE sys_nm_en = '112';
UPDATE tb_infra_master    SET sys_nm_en = 'E112' WHERE sys_nm_en = '112';
UPDATE ps_info           SET sys_nm_en = 'E112' WHERE sys_nm_en = '112';
UPDATE tb_process_master  SET sys_nm_en = 'E112' WHERE sys_nm_en = '112';
UPDATE tb_service_purpose SET sys_nm_en = 'E112' WHERE sys_nm_en = '112';

-- (c) 사후 COUNT
SELECT 'after: sw_pjt=' || COUNT(*) FROM sw_pjt WHERE sys_nm_en = '112';  -- 0 기대
SELECT 'after: sw_pjt E112=' || COUNT(*) FROM sw_pjt WHERE sys_nm_en = 'E112';  -- 3 기대

-- (d) 샘플 검증: 원본 proj_id 보존 확인 (로그 캡처)
SELECT proj_id, sys_nm_en FROM sw_pjt WHERE sys_nm_en = 'E112' LIMIT 10;

COMMIT;
```

#### 5-5-4. 실패 시 롤백 기준
- 사전 COUNT와 사후 COUNT 합이 **일치하지 않으면** 즉시 `ROLLBACK`.
- `'E112'`로 변환된 건수 ≠ 사전 `'112'` 건수 → `ROLLBACK`.
- 트랜잭션 내에서 위 (a)~(d) 전체를 1 트랜잭션으로 수행. `BEGIN`/`COMMIT` 또는 서비스 레벨 Transactional.
- 롤백 SQL 별도 보관: `UPDATE sw_pjt SET sys_nm_en = '112' WHERE sys_nm_en = 'E112' AND proj_id IN ( {마이그레이션 대상 3개 ID} )` — **ID 하드코딩 필요** (안전장치).

#### 5-5-5. 마이그레이션 멱등성
- 재실행 시 이미 `'112'` → `'E112'` 변환된 레코드는 `WHERE sys_nm_en = '112'` 조건에 매칭되지 않음 → **0건 UPDATE**. 멱등.
- 마스터 INSERT는 `INSERT ... ON CONFLICT (sys_nm_en, purpose_type) DO NOTHING` 형식으로 작성 (테이블별 PK/UK 제약 확인 후).

---

## 6. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| DB 마이그레이션 | `swdept/sql/V###_sys_nm_en_normalize.sql` | 신규 |
| 마스터 데이터 | `src/main/resources/db_init_phase2.sql` 또는 별도 V### SQL | 수정/신규 |
| Enum | `src/main/java/com/swmanager/system/constant/enums/SystemType.java` | 신규 |
| Controller 리터럴 치환 | `DocumentController.java` (`"KRAS"` 비교) | 수정 |
| Service 리터럴 치환 | `HwpxExportService.java` (템플릿 매핑) | 수정 |
| Service 검증 | `SwService` (필요 시) | 수정 |
| 테스트 | `SystemTypeTest.java` 신규 | 신규 |
| 문서 | `docs/audit/2026-04-18-system-audit.md` | 수정 |

**합계**: 신규 3~4, 수정 3~4. **총 7~8파일**.

---

## 7. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | `"112"` 레코드가 다른 테이블(`infra`, `ps_info`)에도 존재 | 중간 | 마이그레이션 SQL 실행 전 5개 테이블 사전 점검. 발견 시 동일 UPDATE 병행. |
| R-2 | `process_name`, `purpose_text` 미확정 | 중간 | FR-2 플레이스홀더 정책. 운영팀 답변 전 개발 착수 가능. |
| R-3 | Enum name 충돌 (기존 `SystemType` 클래스 존재 여부) | 낮음 | 개발계획서 Step 1 사전 스캔에서 확인. 없음 확인. |
| R-4 | `SystemType.safeValueOf`가 Service 입력 전반에 호출되어 성능 영향 | 낮음 | Enum `valueOf` O(1). 영향 무시 가능. |
| R-5 | 마스터 데이터 INSERT 중복 (이미 존재 시) | 낮음 | `INSERT ... ON CONFLICT DO NOTHING` 또는 사전 SELECT. |
| R-6 | 롤백 어려움 | 낮음 | V### UP/DOWN 마이그레이션 SQL 쌍. 코드 revert는 commit 단독. |

---

## 8. codex v1 추가 확인 사항 답변 (v2)

1. **API/외부연계가 기존 `'112'` 값 기대 여부** — 현재 swmanager는 외부 API 연동이 Naver·GeoNURIS만 있고 `sys_nm_en`을 외부로 보내는 엔드포인트 없음. 내부 JSON 응답은 클라이언트(Thymeleaf/JS)에서 표시용으로만 사용 (비교 로직 없음, `admin-system-graph.html`이 동적으로 distinct 조회). **외부 하위호환 이슈 없음**. 내부 DB 참조만 UPDATE하면 완결.

2. **DB 제약 추가 계획** — 본 스프린트는 **CHECK 제약 미추가**. 이유: ProcessMaster/ServicePurpose 마스터 테이블이 FK 대상으로 성숙하면 FK로 강제가 정석. 본 스프린트는 Enum 도입+데이터 정제까지만. **후속 스프린트 `sys-type-master-table`(Option C)에서 CHECK/FK 추가** — §5-2 전환 완료 조건에 추가 예정.

3. **캐시/리포트/배치 내 `sys_nm_en` 하드코딩 조회** — Explore 조사 결과:
   - 캐시: `PerformanceService`/`InfraLinkApi`에 캐시 있으나 `sys_nm_en` 값 하드코딩 비교 **없음** (분포 조회만)
   - 배치: `DbInitRunner` 외 배치 없음. DbInitRunner는 `db_init_phase2.sql` 실행 — 본 스프린트에서 마스터 INSERT 추가.
   - 리포트: `ExcelExportService`/`HwpxExportService`/`PdfExportService`의 `sys_nm_en` 사용은 값 표시용(비교 로직 없음) 또는 템플릿 매핑(§FR-6에서 리터럴 제거).
   - **추가 발견 없음**. 본 스프린트 범위로 완결.

4. **마이그레이션 SQL 멱등성** — §5-5-5 멱등성 원칙 적용.
   - UPDATE: `WHERE sys_nm_en = '112'` 재실행 시 0건 — 멱등.
   - INSERT: `INSERT ... ON CONFLICT (sys_nm_en, purpose_type) DO NOTHING` — 멱등. PK/UK 제약 사전 확인 후 확정.
   - 재실행 시 사후 COUNT 동일. 안전.

5. **예외 코드 `ENUM_VALUE_NOT_ALLOWED` 기존 스키마 충돌 여부** — `refactor-01-hardcoding #1-A`에서 도입한 `EnumErrorResponseFactory`의 `code` 값. 본 스프린트 `SystemType`도 동일 핸들러 경로(`MethodArgumentTypeMismatchException`/`HttpMessageNotReadableException`)를 따라 자동 처리됨. **기존 스키마 그대로 재사용 — 충돌 없음**. 단, 본 스프린트는 Entity 타입을 `String`으로 유지하므로 자동 바인딩이 발생하지 않음 — 명시적 Service 레벨 `SystemType.safeValueOf()` 호출 시점에만 해당 예외 발생. 핸들러는 동일.

---

## 9. 승인 요청

### 승인 전 사용자 결정 요청
1. **Option A 채택 여부** (기획팀 권장). Option B/C 선호 시 피드백 필요.
2. **`"112"` → `E112`** 리네이밍 합의. 다른 접두사 선호 시 알려주세요.
3. **`SC`, `LTCS`, `MPMS` 풀네임** 운영팀 확인 필요. 미확정 시 플레이스홀더로 진행.

### 다음 절차
1. 사용자 "반영" → v2 개정
2. 사용자 "최종승인" → **[개발팀]** 개발계획서 작성 (`docs/dev-plans/sys-type-normalization.md`)
