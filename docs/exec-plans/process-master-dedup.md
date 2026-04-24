---
tags: [dev-plan, sprint, refactor, schema, data-cleanup]
sprint: "process-master-dedup"
status: draft-v2
created: "2026-04-20"
---

# [개발계획서] tb_process_master / tb_service_purpose 중복 제거

- **작성팀**: 개발팀
- **작성일**: 2026-04-20
- **근거 기획서**: [[process-master-dedup]] v2 (사용자 최종승인)
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 5건 반영)
1. **Step 순서 재배치** — 본문은 `Step 3 = V018 실행` → `Step 4 = db_init_phase2.sql INSERT 수정` → `Step 5 = 멱등성 증명` 순으로 조정 완료. **"V018 적용 완료 전 서버 재시작 금지"** 게이트 Step 3 상단에 명시. INSERT 충돌 위험 차단
2. **V018 DELETE 키 정정** — `tb_service_purpose` 중복 제거를 `GROUP BY sys_nm_en, purpose_type, purpose_text` **원문 기준**으로 변경 (md5 해시 충돌 이론적 위험 제거). UNIQUE INDEX만 md5 유지
3. **롤백 SQL 실행가능 스크립트** — 개념 수준 → 실제 트랜잭션 스크립트로 명문화. `db_init_phase2.sql` 변경분과의 정합(ON CONFLICT 타겟 되돌림) 함께 정의
4. **멱등성 검증 강화** — 재시작 3회 외에 **동일 키 수동 INSERT → row 증가 0** 체크 T7에 추가
5. **사후검증 보강** — V018 DO 블록에 `is_nullable='NO'` 키 5컬럼 검증 추가

---

## 0. 전제 / 환경

### 0-1. DB
- PostgreSQL, 스키마 `public`, `postgres` 계정 (superuser)

### 0-2. 범위 고정
- **2개 테이블만**: `tb_process_master`, `tb_service_purpose`
- **동시 정리 금지**: 다른 스프린트 범위(Wave 1 S3/S4/S5/S11 등) 건드리지 않음
- Java 코드 변경 **0** (Entity는 PK 의존 없음)

### 0-3. 실행 전제
- 기획서 FR-0 5단계 사전검증 전체 PASS 후에만 진행
- 업무시간 외 권장

---

## 1. 작업 순서

### Step 1 — 사전검증 (FR-0)

**1-1. 러너 작성**: `docs/exec-plans/process-master-precheck.java`
- 기획서 FR-0 (1)~(5) 쿼리 실행
- 안전통제: `SET TRANSACTION READ ONLY` + `statement_timeout 10s`
- 결과: `docs/exec-plans/process-master-precheck-result.md`
- **진행 게이트**: 아래 5가지 중 하나라도 실패 시 `HALT` + exit 1
  - (1) total=1450, distinct=5
  - (2) 각 5개 중복 그룹
  - (3) NULL 데이터 0건 (키 컬럼 4개)
  - (4) 외부 FK 0건
  - (5) `max_bytes < 2700`

**1-2. Entity Java 참조 재확인**
```bash
rg -n 'process_id|purpose_id' src/main/java  # PK 의존 코드 없음 확인
rg -n 'ProcessMaster|ServicePurpose' src/main/java/com/swmanager/system/repository/
# 기대: Repository 기본 findAll() 등만 존재
```

### Step 2 — 마이그레이션 SQL 작성 (FR-1 + FR-2 + FR-2-X)

**신규 파일**: `swdept/sql/V018_process_master_dedup.sql`

```sql
-- ============================================================
-- V018: tb_process_master / tb_service_purpose 중복 제거
-- Sprint: process-master-dedup (2026-04-20)
-- 근거: docs/product-specs/process-master-dedup.md v2
-- 사전검증: FR-0 전체 PASS 후 실행 (precheck runner)
-- ============================================================

BEGIN;

-- 트랜잭션 내 사전검증 재확인 (안전 이중 체크)
DO $$
DECLARE
  dist_proc bigint; dist_purp bigint;
  null_cnt bigint; fk_cnt bigint;
BEGIN
  SELECT COUNT(DISTINCT (sys_nm_en || '|' || process_name)) INTO dist_proc FROM tb_process_master;
  SELECT COUNT(DISTINCT (sys_nm_en || '|' || purpose_type || '|' || md5(purpose_text))) INTO dist_purp FROM tb_service_purpose;
  IF dist_proc <> 5 THEN RAISE EXCEPTION 'HALT: tb_process_master distinct=% (expected 5)', dist_proc; END IF;
  IF dist_purp <> 5 THEN RAISE EXCEPTION 'HALT: tb_service_purpose distinct=% (expected 5)', dist_purp; END IF;

  -- NULL 체크 (키 컬럼 4개)
  SELECT (
    (SELECT COUNT(*) FROM tb_process_master WHERE sys_nm_en IS NULL OR process_name IS NULL) +
    (SELECT COUNT(*) FROM tb_service_purpose WHERE sys_nm_en IS NULL OR purpose_type IS NULL OR purpose_text IS NULL)
  ) INTO null_cnt;
  IF null_cnt <> 0 THEN RAISE EXCEPTION 'HALT: NULL in key columns: %', null_cnt; END IF;

  -- 외부 FK 체크
  SELECT COUNT(*) INTO fk_cnt
    FROM information_schema.table_constraints tc
    JOIN information_schema.constraint_column_usage ccu
      ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
   WHERE tc.constraint_type = 'FOREIGN KEY'
     AND tc.table_schema = 'public'
     AND ccu.table_name IN ('tb_process_master', 'tb_service_purpose')
     AND tc.table_name NOT IN ('tb_process_master', 'tb_service_purpose');
  IF fk_cnt > 0 THEN RAISE EXCEPTION 'HALT: external FK count=%', fk_cnt; END IF;
END $$ LANGUAGE plpgsql;

-- FR-1. 중복 제거 (MIN(PK) 보존)
DELETE FROM tb_process_master
 WHERE process_id NOT IN (
   SELECT MIN(process_id) FROM tb_process_master GROUP BY sys_nm_en, process_name
 );

DELETE FROM tb_service_purpose
 WHERE purpose_id NOT IN (
   -- v2 정정: md5 대신 purpose_text 원문 기준 (해시 충돌 이론적 위험 제거)
   SELECT MIN(purpose_id) FROM tb_service_purpose GROUP BY sys_nm_en, purpose_type, purpose_text
 );

-- FR-2-X. NOT NULL 전환 (UNIQUE 전)
ALTER TABLE tb_process_master
  ALTER COLUMN sys_nm_en SET NOT NULL,
  ALTER COLUMN process_name SET NOT NULL;

ALTER TABLE tb_service_purpose
  ALTER COLUMN sys_nm_en SET NOT NULL,
  ALTER COLUMN purpose_type SET NOT NULL,
  ALTER COLUMN purpose_text SET NOT NULL;

-- FR-2. UNIQUE 제약 / 인덱스 (멱등: IF NOT EXISTS 스타일)
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uq_process_master_sys_name'
  ) THEN
    ALTER TABLE tb_process_master
      ADD CONSTRAINT uq_process_master_sys_name
      UNIQUE (sys_nm_en, process_name);
  END IF;
END $$ LANGUAGE plpgsql;

CREATE UNIQUE INDEX IF NOT EXISTS uq_service_purpose_sys_type_md5
  ON tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text));

-- 사후 검증 (v2 보강: NOT NULL 5컬럼 + UNIQUE)
DO $$
DECLARE
  c_proc bigint; c_purp bigint;
  uq_proc bigint; uq_purp bigint;
  nn_bad bigint;
BEGIN
  SELECT COUNT(*) INTO c_proc FROM tb_process_master;
  SELECT COUNT(*) INTO c_purp FROM tb_service_purpose;
  IF c_proc <> 5 THEN RAISE EXCEPTION 'HALT post: tb_process_master count=%', c_proc; END IF;
  IF c_purp <> 5 THEN RAISE EXCEPTION 'HALT post: tb_service_purpose count=%', c_purp; END IF;

  SELECT COUNT(*) INTO uq_proc FROM pg_constraint WHERE conname = 'uq_process_master_sys_name';
  SELECT COUNT(*) INTO uq_purp FROM pg_indexes WHERE indexname = 'uq_service_purpose_sys_type_md5';
  IF uq_proc <> 1 THEN RAISE EXCEPTION 'HALT post: UNIQUE constraint missing'; END IF;
  IF uq_purp <> 1 THEN RAISE EXCEPTION 'HALT post: UNIQUE INDEX missing'; END IF;

  -- v2: NOT NULL 전환 검증 (키 5컬럼)
  SELECT COUNT(*) INTO nn_bad
    FROM information_schema.columns
   WHERE table_schema = 'public'
     AND ((table_name = 'tb_process_master' AND column_name IN ('sys_nm_en','process_name'))
       OR (table_name = 'tb_service_purpose' AND column_name IN ('sys_nm_en','purpose_type','purpose_text')))
     AND is_nullable <> 'NO';
  IF nn_bad <> 0 THEN RAISE EXCEPTION 'HALT post: NOT NULL missing on % key columns', nn_bad; END IF;

  RAISE NOTICE 'PASS: dedup + constraints + NOT NULL applied';
END $$ LANGUAGE plpgsql;

COMMIT;
```

### Step 3 — V018 실행 (v2 순서 재배치 — db_init_phase2.sql 수정은 Step 4로 이동)

**⚠️ 게이트**: V018 적용 **완료 전 서버 재시작 금지**. `db_init_phase2.sql`은 여전히 기존 `ON CONFLICT DO NOTHING`(타겟 없음)이라 재시작 시 또 중복 INSERT. V018로 UNIQUE 생성 후에만 안전.

**3-1. 러너 실행** (기존 `legacy-contract-apply.java` 재사용)
```bash
JAR=~/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
DB_PASSWORD='***' java -cp "$JAR" docs/exec-plans/legacy-contract-apply.java \
  swdept/sql/V018_process_master_dedup.sql
# 기대 출력: [NOTICE] PASS: dedup + constraints + NOT NULL applied
```

**3-2. 즉시 검증** (NFR-4/NFR-5)
- `SELECT COUNT(*) FROM tb_process_master` = 5
- `SELECT COUNT(*) FROM tb_service_purpose` = 5
- UNIQUE 제약/인덱스 존재 확인
- NOT NULL 5컬럼 전환 확인

### Step 4 — `db_init_phase2.sql` INSERT 수정 (FR-3, Step 3 성공 후)

V018로 UNIQUE가 설치된 후에만 수정. 서버 재시작 시 `ON CONFLICT` 타겟이 유효하게 작동.

```sql
-- 기존 (ON CONFLICT 타겟 없음)
ON CONFLICT DO NOTHING;

-- 수정 후
ON CONFLICT (sys_nm_en, process_name) DO NOTHING;                     -- tb_process_master
ON CONFLICT (sys_nm_en, purpose_type, md5(purpose_text)) DO NOTHING;  -- tb_service_purpose (표현식 INDEX 타겟)
```

### Step 5 — 멱등성 증명 (NFR-3 — 5체크 × 3회 + 수동 INSERT 테스트)

**5-1. 서버 재시작 3회 반복**
```bash
for i in 1 2 3; do
  echo "=== 재시작 #$i ==="
  bash server-restart.sh
  sleep 5
  # 5 체크 러너 실행: docs/exec-plans/process-master-idempotency-check.java
  DB_PASSWORD='***' java -cp "$JAR" docs/exec-plans/process-master-idempotency-check.java
done
```

**5-2. 멱등성 체크 러너 작성**: `docs/exec-plans/process-master-idempotency-check.java`
- 각 테이블에 대해:
  - (a) COUNT(*) = 5
  - (b) COUNT(DISTINCT key) = 5
  - (c) 중복 그룹 = 0
  - (d) UNIQUE 제약/인덱스 존재
  - (e) 직전 재시작 로그에 관련 ERROR 0건 (`rg 'tb_process_master\|tb_service_purpose' server.log | rg ERROR`)
- 결과: `docs/exec-plans/process-master-idempotency-result.md`

**5-3. 수동 INSERT 중복 차단 테스트** (v2 codex 권장 #4)
```sql
-- 재시작 루프 후 1회 수동 실행 (row 증가 0 기대)
BEGIN;
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order)
VALUES ('UPIS', '도시계획정보체계용 GIS SW 유지관리', 1)
ON CONFLICT (sys_nm_en, process_name) DO NOTHING;
-- 기대: "INSERT 0 0" (충돌로 insert 0건)
INSERT INTO tb_service_purpose (sys_nm_en, purpose_type, purpose_text, sort_order)
VALUES ('UPIS', 'PURPOSE', '도시계획정보체계(UPIS)의 최신 버전 유지와 원활한 서비스를 제공', 1)
ON CONFLICT (sys_nm_en, purpose_type, md5(purpose_text)) DO NOTHING;
-- 기대: "INSERT 0 0"
ROLLBACK;  -- 안전상 ROLLBACK (COMMIT 해도 무해)
-- 사후: COUNT(*) 여전히 5여야 함
```

### Step 6 — 감사·로드맵 문서 갱신

- `docs/generated/audit/data-architecture-utilization-audit.md` — 기능 2-B 섹션 "✅ 완료 (2026-04-20)" 표기
- `docs/design-docs/data-architecture-roadmap.md` — S2 완료 표기

### Step 7 — 빌드/재기동/회귀 스모크 (NFR-6)

```bash
./mvnw -q clean compile                # BUILD SUCCESS
bash server-restart.sh                  # Started + ERROR 0
```

**회귀 스모크**:
1. `/login` → 200
2. `/document/list` → 점검내역서 클릭 → 상세 정상 (ProcessMaster 사용)
3. `/document/create?docType=COMMENCE` → 작성 폼 정상 (ServicePurpose 사용 가능성)

### Step 8 — 커밋/푸시

```bash
git add swdept/sql/V018_process_master_dedup.sql \
        src/main/resources/db_init_phase2.sql \
        docs/exec-plans/process-master-* \
        docs/product-specs/process-master-dedup.md \
        docs/generated/audit/data-architecture-utilization-audit.md \
        docs/design-docs/data-architecture-roadmap.md
git commit -m "refactor: process-master-dedup 완료 (Wave 1 S2)"
git push
```

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | 사전검증 PASS (FR-0) | precheck 러너 | 5가지 PASS, exit 0 |
| T2 | Java PK 참조 없음 | `rg 'process_id\|purpose_id' src/main/java` | 0 hits |
| T3 | V018 실행 성공 | `[NOTICE] PASS: dedup + constraints applied` | 출력 확인 |
| T4 | DB 최종 카운트 (NFR-4) | `SELECT COUNT(*)` 양쪽 | 각 5 |
| T5 | UNIQUE 존재 (NFR-5) | `pg_constraint` + `pg_indexes` 조회 | 각 1 |
| T6 | NOT NULL 전환 | `information_schema.columns.is_nullable` | 키 **5컬럼** 모두 'NO' (tb_process_master: sys_nm_en, process_name / tb_service_purpose: sys_nm_en, purpose_type, purpose_text) |
| T7 | 멱등성 5체크 × 3회 + 수동 INSERT 차단 (NFR-3) | idempotency 러너 + Step 5-3 수동 INSERT 테스트 | 3회 연속 5체크 PASS + 수동 INSERT "0 0" (row 증가 0) |
| T8 | 컴파일 | `./mvnw -q clean compile` | BUILD SUCCESS |
| T9 | 서버 기동 | `bash server-restart.sh` | `Started` + ERROR 0 |
| T10 | 회귀 스모크 3화면 (NFR-6) | §1 Step 7 | 3개 정상 렌더링 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| FR-0 사전검증 실패 | V018 미실행. 원인 분석 후 재실행 |
| V018 실행 중 실패 | 트랜잭션 자동 ROLLBACK |
| Step 4 db_init_phase2.sql 수정 후 서버 부팅 실패 | 기존 `ON CONFLICT DO NOTHING`(타겟 없음)으로 되돌림 (UNIQUE가 없어진 경우만 필요) |
| 배포 후 회귀 발견 | 아래 **R-SQL** 실행 후 `db_init_phase2.sql`도 동반 되돌림 |

### R-SQL — 실행 가능한 롤백 스크립트 (v2 codex 권장 #3)

```sql
BEGIN;

-- (1) UNIQUE INDEX 제거 (tb_service_purpose)
DROP INDEX IF EXISTS uq_service_purpose_sys_type_md5;

-- (2) UNIQUE CONSTRAINT 제거 (tb_process_master)
DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_process_master_sys_name') THEN
    ALTER TABLE tb_process_master DROP CONSTRAINT uq_process_master_sys_name;
  END IF;
END $$ LANGUAGE plpgsql;

-- (3) NOT NULL 해제 (UNIQUE 제거 후)
ALTER TABLE tb_process_master
  ALTER COLUMN sys_nm_en DROP NOT NULL,
  ALTER COLUMN process_name DROP NOT NULL;

ALTER TABLE tb_service_purpose
  ALTER COLUMN sys_nm_en DROP NOT NULL,
  ALTER COLUMN purpose_type DROP NOT NULL,
  ALTER COLUMN purpose_text DROP NOT NULL;

COMMIT;

-- (4) 데이터는 이미 정리된 상태 (5건)로 유지 — 1450건 복원은 "중복을 되살리는 것"이라 비추천
-- 필요 시 커밋 b628c2e 이전 백업에서 별도 복원 (운영자 판단)
```

### db_init_phase2.sql 정합 롤백 (필요 시)

R-SQL 실행 후 UNIQUE 타겟이 없어지므로, Step 4에서 수정한 INSERT 문을 **원래대로 되돌리기**:

```sql
-- Step 4 수정본 → 롤백본
ON CONFLICT (sys_nm_en, process_name) DO NOTHING;            → ON CONFLICT DO NOTHING;
ON CONFLICT (sys_nm_en, purpose_type, md5(purpose_text)) DO NOTHING; → ON CONFLICT DO NOTHING;
```
만약 되돌리지 않으면 서버 재시작 시 "ON CONFLICT arbiter constraint not found" 에러 발생.

**롤백 기준 커밋**: `b628c2e` (S6 직후) — 본 스프린트 시작 전 상태

---

## 4. 리스크·완화 재확인

| ID | 리스크 | 수준 | 본 개발계획서 적용 |
|----|--------|------|-------------------|
| R-1 | 외부 FK 잔존 | 낮음 | FR-0 (4) + V018 DO 블록 이중 체크 |
| R-2 | UNIQUE 전 DELETE 누락 | 매우 낮음 | V018 트랜잭션 내 순서 강제 |
| R-3 | TEXT B-tree 한계 초과 | 낮음 | FR-0 (5) 길이 측정. md5 해시 사용으로 32바이트 고정 |
| R-4 | DbInitRunner ALTER 재실행 에러 로그 | 낮음 | DO 블록 `IF NOT EXISTS` + `CREATE UNIQUE INDEX IF NOT EXISTS` 사용으로 멱등 |
| R-5 | Java PK 의존 | 매우 낮음 | T2로 재검증 |
| R-6 | `db_init_phase2.sql` INSERT 문법 충돌 | 낮음 | 표현식 UNIQUE INDEX + ON CONFLICT 표현식 타겟은 PostgreSQL 12+ 공식 지원. 운영 버전 14+ 확인 완료 |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 확인 사항
- [ ] §0-2 범위 고정 (2 테이블만)
- [ ] §1 Step 1~8 순서
- [ ] §1 Step 2 V018 SQL: DO 블록 사전검증 + DELETE + NOT NULL + UNIQUE 순서
- [ ] §1 Step 5 NFR-3 멱등성 증명 (5체크 × 3회 재시작)
- [ ] §2 T1~T10 체크리스트
- [ ] §3 롤백 절차
