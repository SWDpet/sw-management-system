---
tags: [plan, sprint, refactor, schema, data-cleanup]
sprint: "process-master-dedup"
status: draft-v2
created: "2026-04-20"
---

# [기획서] tb_process_master / tb_service_purpose 중복 제거

- **작성팀**: 기획팀
- **작성일**: 2026-04-20
- **선행 커밋**: `b628c2e` (S6 legacy-contract-tables-drop 완료)
- **근거**: [[data-architecture-roadmap]] v2 P1 Wave 1 S2 / [[data-architecture-utilization-audit]] §"심각한 중복"
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 5건 반영)
1. **`tb_service_purpose` UNIQUE 전략 단일화** — `CREATE UNIQUE INDEX ... (sys_nm_en, purpose_type, md5(purpose_text))` 표현식 UNIQUE INDEX 방식으로 확정 (ADD CONSTRAINT UNIQUE (..., md5(...)) 구문 불안정 해소). `purpose_text_hash` 생성 컬럼 방식은 대안으로 기록.
2. **FR-0 SQL 정정** — `LEFT(purpose_text,100)` 제거. 전체 텍스트 또는 `md5(purpose_text)` 기반으로 집계
3. **NOT NULL 정책 확정** — 키 컬럼(`sys_nm_en`, `process_name`, `purpose_type`, `purpose_text`) **NOT NULL 전환** 포함. 사전 NULL 데이터 0건 확인 선행
4. **NFR-3 강화** — 3회 재기동 후 5가지 체크: COUNT=5 + COUNT(DISTINCT key)=5 + duplicate group 0 + UNIQUE 제약 존재 + 앱 로그 에러 0
5. **표기 통일** — `purpose_text_hash` 언급 제거. 문서 전체 `md5(purpose_text)` 표현식 방식으로 통일

---

## 1. 배경 / 목표

### 배경 — 290배 중복 데이터

데이터 아키텍처 감사 Batch A에서 발견된 **최대 심각도 스키마 문제**:

| 테이블 | 총 레코드 | DISTINCT | 중복 배수 |
|--------|-----------|----------|-----------|
| `tb_process_master` | **1,450** | 5 | **290배** |
| `tb_service_purpose` | **1,450** | 5 | **290배** |

### 원인 — `ON CONFLICT` 무효화 설계 결함

현재 `db_init_phase2.sql` INSERT 문:
```sql
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) VALUES
('UPIS', '도시계획정보체계용 GIS SW 유지관리', 1),
...
ON CONFLICT DO NOTHING;
```

그러나 DDL 정의:
```sql
CREATE TABLE IF NOT EXISTS tb_process_master (
    process_id SERIAL PRIMARY KEY,   -- AUTO, 충돌 없음
    sys_nm_en VARCHAR(30),            -- UNIQUE 제약 없음
    process_name VARCHAR(200),
    ...
);
```

**문제**: PK는 auto-increment이라 INSERT마다 새 값이 할당되고, `(sys_nm_en, process_name)`에 UNIQUE 제약이 없어 **`ON CONFLICT`가 판정할 충돌 대상 자체가 없음**. PostgreSQL은 이를 `no-op conflict target` 상태로 처리하여 **매 INSERT가 그대로 수용**됨.

`DbInitRunner`(서버 시작 시 `db_init_phase2.sql` 자동 실행, `DbInitRunner.java:22-89`)가 매 서버 재시작마다 5건씩 INSERT → 약 290회 재시작 누적으로 1450건 형성.

### 목표
1. **기존 중복 제거**: 1450건 → 각 DISTINCT 5건만 유지 (원본 순서 보존, MIN(PK) 기준)
2. **UNIQUE 제약/인덱스 추가**: 재발 방지
   - `tb_process_master`: `ADD CONSTRAINT UNIQUE (sys_nm_en, process_name)`
   - `tb_service_purpose`: `CREATE UNIQUE INDEX ... (sys_nm_en, purpose_type, md5(purpose_text))` (표현식 UNIQUE INDEX — PostgreSQL 12+ `ON CONFLICT` 지원)
3. **NOT NULL 전환** (v2 추가): 키 컬럼 4개(`sys_nm_en`, `process_name`, `purpose_type`, `purpose_text`) → `NOT NULL`
4. **`ON CONFLICT` 타겟 명시**: INSERT 문에 명시적 타겟 추가
5. **DbInitRunner 검토**: 재실행 시 멱등성 실제 보장

---

## 2. 기능 요건 (FR)

### FR-0. 사전 검증 SQL (v2 정정)

```sql
-- (1) 중복 분포 재확인 — 전체 텍스트 기반 (v2: LEFT(purpose_text,100) 제거)
SELECT 'tb_process_master' AS t, COUNT(*) AS total,
       COUNT(DISTINCT (sys_nm_en || '|' || process_name)) AS distinct_cnt
  FROM tb_process_master
UNION ALL
SELECT 'tb_service_purpose', COUNT(*),
       COUNT(DISTINCT (sys_nm_en || '|' || purpose_type || '|' || md5(purpose_text)))
  FROM tb_service_purpose;

-- (2) 각 중복 그룹의 최소 PK id (보존 대상) 확인
SELECT sys_nm_en, process_name, MIN(process_id) AS keep_id, COUNT(*) AS dup_cnt
  FROM tb_process_master GROUP BY sys_nm_en, process_name ORDER BY keep_id;
SELECT sys_nm_en, purpose_type, md5(purpose_text) AS text_hash,
       MIN(purpose_id) AS keep_id, COUNT(*) AS dup_cnt
  FROM tb_service_purpose GROUP BY sys_nm_en, purpose_type, md5(purpose_text) ORDER BY keep_id;

-- (3) NULL 데이터 존재 확인 (v2 추가 — NOT NULL 전환 전 필수)
SELECT 'tb_process_master.sys_nm_en'     AS col, COUNT(*) FROM tb_process_master WHERE sys_nm_en IS NULL
UNION ALL
SELECT 'tb_process_master.process_name',         COUNT(*) FROM tb_process_master WHERE process_name IS NULL
UNION ALL
SELECT 'tb_service_purpose.sys_nm_en',           COUNT(*) FROM tb_service_purpose WHERE sys_nm_en IS NULL
UNION ALL
SELECT 'tb_service_purpose.purpose_type',        COUNT(*) FROM tb_service_purpose WHERE purpose_type IS NULL
UNION ALL
SELECT 'tb_service_purpose.purpose_text',        COUNT(*) FROM tb_service_purpose WHERE purpose_text IS NULL;

-- (4) 외부 FK 참조 존재 확인 (process_id·purpose_id를 참조하는 FK가 있다면 DELETE 영향)
SELECT tc.table_name AS referencing, tc.constraint_name,
       kcu.column_name, ccu.table_name AS target
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
  JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name
 WHERE tc.constraint_type = 'FOREIGN KEY'
   AND tc.table_schema = 'public'
   AND ccu.table_name IN ('tb_process_master', 'tb_service_purpose')
   AND tc.table_name NOT IN ('tb_process_master', 'tb_service_purpose');

-- (5) purpose_text 최대 길이 (B-tree 키 한계 2704 bytes UTF-8 고려)
SELECT MAX(length(purpose_text)) AS max_len,
       MAX(octet_length(purpose_text)) AS max_bytes
  FROM tb_service_purpose;
```

**기대 결과**:
- (1) total = 1450, distinct = 5 (양 테이블)
- (2) 각 5개 행, dup_cnt ≈ 290
- (3) 모든 키 컬럼 NULL = 0 (NOT NULL 전환 가능)
- (4) 외부 FK 0건
- (5) max_bytes < 2700 바이트 (md5 해시 사용으로 문제 회피 — 원본 길이는 모니터링 목적)

**진행 게이트**: 하나라도 기대 불일치면 **HALT** + 원인 분석 선행

### FR-1. 중복 데이터 제거

각 테이블에서 `sys_nm_en + 콘텐츠` 조합별 **MIN(PK)** 만 유지하고 나머지 DELETE:

```sql
-- tb_process_master
DELETE FROM tb_process_master
 WHERE process_id NOT IN (
   SELECT MIN(process_id) FROM tb_process_master
    GROUP BY sys_nm_en, process_name
 );

-- tb_service_purpose
DELETE FROM tb_service_purpose
 WHERE purpose_id NOT IN (
   SELECT MIN(purpose_id) FROM tb_service_purpose
    GROUP BY sys_nm_en, purpose_type, purpose_text
 );
```

### FR-2. UNIQUE 제약/인덱스 추가 (v2 단일화)

**`tb_process_master`**: 단순 UNIQUE 제약
```sql
ALTER TABLE tb_process_master
  ADD CONSTRAINT uq_process_master_sys_name
  UNIQUE (sys_nm_en, process_name);
```

**`tb_service_purpose`**: **표현식 UNIQUE INDEX** (v2 codex 권장 #1 단일안)
```sql
-- PostgreSQL 12+ — 표현식 UNIQUE INDEX. CONSTRAINT 구문은 md5() 표현식 불안정 → INDEX 방식 채택
CREATE UNIQUE INDEX uq_service_purpose_sys_type_md5
  ON tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text));
```

**이유**:
- `ADD CONSTRAINT UNIQUE (..., md5(...))` 구문은 PostgreSQL 표준 밖 → 오류 가능성
- `CREATE UNIQUE INDEX ... (..., expression)` 는 공식 지원 + `ON CONFLICT` 표현식 타겟으로 사용 가능
- 제약 이름 대신 인덱스 이름 (`uq_service_purpose_sys_type_md5`)으로 관리

**대안 (범위 외)**: `purpose_text_hash` 생성 컬럼(`GENERATED ALWAYS AS (md5(purpose_text)) STORED`) + 일반 UNIQUE. 현재는 표현식 INDEX 방식이 스키마 변경 최소.

### FR-2-X. NOT NULL 전환 (v2 신설)

FR-0 (3) NULL 데이터 0건 확인 후:
```sql
ALTER TABLE tb_process_master
  ALTER COLUMN sys_nm_en SET NOT NULL,
  ALTER COLUMN process_name SET NOT NULL;

ALTER TABLE tb_service_purpose
  ALTER COLUMN sys_nm_en SET NOT NULL,
  ALTER COLUMN purpose_type SET NOT NULL,
  ALTER COLUMN purpose_text SET NOT NULL;
```

이유: PostgreSQL UNIQUE는 NULL 간 충돌 안 일어남 → 키 컬럼이 NULL이면 중복 방지 우회 가능. NOT NULL이 재발 방지 완결 조건.

### FR-3. `db_init_phase2.sql` INSERT 수정

```sql
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) VALUES
(...)
ON CONFLICT (sys_nm_en, process_name) DO NOTHING;  -- 명시 타겟

INSERT INTO tb_service_purpose (sys_nm_en, purpose_type, purpose_text, sort_order) VALUES
(...)
ON CONFLICT (sys_nm_en, purpose_type, md5(purpose_text)) DO NOTHING;  -- 표현식 INDEX 타겟
```

### FR-4. `DbInitRunner` 검토

`DbInitRunner.java`는 현재 다음 동작:
- 서버 시작 시 `db_init_phase2.sql` 전체 실행
- 각 스테이트먼트 실패는 로그로만 기록 (DEBUG)
- **멱등성은 `CREATE TABLE IF NOT EXISTS`에만 의존** — INSERT/ALTER는 스스로 멱등 보장해야 함

본 스프린트 조치 후에는:
- CREATE TABLE → 멱등 (IF NOT EXISTS)
- INSERT → 멱등 (UNIQUE 제약 + ON CONFLICT 타겟 명시)
- ALTER TABLE ADD CONSTRAINT → 최초 1회만 성공, 이후는 `relation already exists` 에러로 로그 무시됨 (현재 `catch`에서 debug 로그)

즉 DbInitRunner **코드 자체는 수정 불필요**. 단 로그 레벨(현재 DEBUG)을 INFO 또는 WARN으로 올려 실제 무시된 에러 인지 가능성 개선 여부는 범위 외.

### 범위 외
- Java Entity 수정 **없음** (ProcessMaster·ServicePurpose 엔티티는 컬럼만 참조)
- 사용자 확정 9개 마스터 테이블 수정 0건
- S1·S3·S4·S5·S11 등 다른 스프린트 건드리지 않음

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공 |
| NFR-2 | 서버 재기동 후 정상 부팅. `Started SwManagerApplication` + ERROR 0 |
| NFR-3 | **재실행 멱등성 증명** (v2 강화 — 5가지 체크) — 서버 3회 연속 재시작 후 아래 **5가지 모두** 통과:<br>(a) `SELECT COUNT(*) FROM tb_process_master` = **5**<br>(b) `SELECT COUNT(DISTINCT (sys_nm_en\|\|'\|'\|\|process_name)) FROM tb_process_master` = **5** (키 조합 개수)<br>(c) `SELECT COUNT(*) FROM (SELECT sys_nm_en, process_name FROM tb_process_master GROUP BY sys_nm_en, process_name HAVING COUNT(*) > 1) dup` = **0** (중복 그룹 0)<br>(d) UNIQUE 제약/인덱스 존재 확인 (NFR-5)<br>(e) `server.log` 재시작 중 INSERT 관련 ERROR 0건 (DEBUG 로그의 "이미 존재" 제외)<br>※ `tb_service_purpose`도 동일 5체크 적용 (md5 기반) |
| NFR-4 | DB 최종 검증:<br>`SELECT COUNT(*) FROM tb_process_master` = 5<br>`SELECT COUNT(*) FROM tb_service_purpose` = 5 |
| NFR-5 | UNIQUE 제약/인덱스 존재 확인:<br>`SELECT COUNT(*) FROM pg_constraint WHERE conname = 'uq_process_master_sys_name'` = 1<br>`SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'uq_service_purpose_sys_type_md5'` = 1 |
| NFR-6 | 회귀 스모크: 기존 5종 마스터(UPIS/KRAS/IPSS/GIS_SW/APIMS) 조회 정상. 점검내역서·견적서 등 의존 화면 정상 렌더링 |

---

## 4. 의사결정 / 우려사항

### 4-1. `purpose_text` TEXT UNIQUE 전략 — 개발계획서에서 실측 결정
- 현재 데이터 길이(50~60자 추정)는 B-tree 키 한계 내일 가능성 높음 → 직접 UNIQUE 가능
- 향후 긴 텍스트 추가 가능성 고려 시 `md5(purpose_text)` 함수 UNIQUE가 안전
- 개발계획서에서 `SELECT MAX(length(purpose_text)) FROM tb_service_purpose` 실측 후 확정

### 4-2. DELETE 순서 — ✅ 참조 FK 없음 전제
- FR-0 사전검증에서 외부 FK 확인 후 진행
- `ProcessMaster`/`ServicePurpose` Entity의 Java 코드 참조 재검증:
  ```bash
  rg -n 'process_id|purpose_id' src/main/java
  ```
- 외부 참조 발견 시 본 스프린트 **중단** + 별도 정리

### 4-3. MIN(PK) 보존 방침 — ✅ 가장 먼저 삽입된 레코드 유지
- 중복 290건 중 **최초 INSERT된 PK(최소값)** 유지 → 원본 의도에 가장 가까움
- DELETE 대상은 process_id가 큰 289건 × 5그룹 = 1445건

### 4-4. 실행 시점 — ✅ 업무시간 외 권장
- DELETE 1445건 × 2 테이블 = 2890건 삭제. 규모 작지만 락 회피 위해 권장
- DDL ALTER도 짧은 락만 발생

### 4-5. DB팀 자문 결과
- UNIQUE 제약 추가 시 기존 데이터에 중복 있으면 실패 → FR-1 DELETE **선행 필수**
- 트랜잭션 순서: `BEGIN → DELETE → ALTER ADD CONSTRAINT → COMMIT`
- 실패 시 자동 ROLLBACK

### 4-6. 감사 외 영역 — ✅ 없음
- `tb_process_master`·`tb_service_purpose`는 감사 대상 (§기능 2-B)
- 기초 4개 기능 테이블 건드리지 않음

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| 마이그레이션 SQL | `swdept/sql/V018_process_master_dedup.sql` | 신규 |
| 초기화 SQL | `src/main/resources/db_init_phase2.sql` | 수정 (INSERT ON CONFLICT 타겟 명시) |
| 사전검증 러너 | `docs/dev-plans/process-master-precheck.java` | 신규 |
| 실행 러너 | `docs/dev-plans/process-master-apply.java` (기존 legacy-contract-apply 재사용 가능) | 재사용 |
| 감사 리포트 | `docs/audit/data-architecture-utilization-audit.md` | 수정 (S2 완료 체크) |
| 로드맵 | `docs/plans/data-architecture-roadmap.md` | 수정 (S2 완료 표기) |

**합계**: 신규 2파일, 수정 3파일. **Java 코드 변경 0**. **DB 변경**: 1445×2 = 2890건 DELETE + 2 UNIQUE 제약 추가.

---

## 6. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | 외부 FK 잔존으로 DELETE 실패 | 낮음 | FR-0 사전검증 필수 |
| R-2 | UNIQUE 제약 추가 전 DELETE 누락 | 매우 낮음 | 트랜잭션 내 순서 강제 (DELETE → ALTER) |
| R-3 | `purpose_text` TEXT 길이 B-tree 한계 초과 | 중간 | 개발계획서에서 실측 후 md5 해시 전략 채택 여부 결정 |
| R-4 | DbInitRunner가 ALTER 재실행 시 에러 로그 쌓임 | 낮음 | 현재 DEBUG 로그로 무시됨. ALTER IF NOT EXISTS 쓸 수 없으므로 DO 블록 with conditional check 권장 |
| R-5 | Java 코드가 duplicate ID에 의존 | 매우 낮음 | Repository 조회는 `findAll()` 또는 `sys_nm_en` 기준이라 PK 의존 없음 |
| R-6 | 서버 부팅 중 마이그레이션 실행 경합 | 낮음 | V018은 수동 실행. DbInitRunner는 db_init_phase2.sql만 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 전 확인 사항
- [x] 중복 290배 재현 원인 분석 완료 (`ON CONFLICT` 타겟 부재)
- [x] 기초 4개 기능 테이블 건드리지 않음
- [x] Java 코드 변경 없음
- [ ] 개발계획서 단계에서 `purpose_text` 최대 길이 실측 후 UNIQUE 전략 확정

### 다음 절차
1. 사용자 "반영" 시 v2 개정
2. 사용자 "최종승인" 시 → **[개발팀]** 개발계획서 작성 (`docs/dev-plans/process-master-dedup.md`)
