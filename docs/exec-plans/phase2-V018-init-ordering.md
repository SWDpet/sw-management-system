---
tags: [dev-plan, sprint, schema, init-ordering]
sprint: "phase2-V018-init-ordering"
status: draft-v1.1
created: "2026-05-11"
---

# [개발계획서] phase2 ↔ V018 초기화 순서 의존성 해소 — 스프린트 phase2-V018-init-ordering

- **작성팀**: 개발팀
- **작성일**: 2026-05-11
- **근거 기획서**: `docs/product-specs/phase2-V018-init-ordering.md` v3 (codex 3차 ⭕ + 사용자 최종승인 2026-05-11)
- **선행 스프린트 (완료 확인)**: `dbinitrunner-dollar-quote-aware` — commit `ba12fc6` master, 2026-04-29. EC-1~3 모두 충족 (기획서 §9 체크리스트 참조).
- **상태**: 초안 v1.1 (codex 1차 검토 ⚠ 5건 반영 — codex 2차 검토 대기)
- **개정 이력**:
  - v1 (2026-05-11): 초안.
  - v1.1 (2026-05-11): codex 1차 검토 ⚠ 권고 5건 반영. (a) Step 2-1/2/4/5 line 번호를 anchor 기반으로 정정 (Step 2-2 는 line 40 닫힘 직후 — v1 의 "line 41 직후"는 1줄 오프셋), (b) NFR-1 mvnw 명령을 기획서와 통일 (`./mvnw clean compile`), (c) Step 3-1 DROP/CREATE 를 별도 `-c` 호출로 분리 (transaction block 안전), (d) NFR-3-x DO 가드 SQL 을 `conrelid`/`contype='u'`/`tablename` 까지 검사하도록 강화 (이름만 동일한 다른 객체에 false PASS 회귀 차단), (e) 롤백 표를 "Step 4 이전/이후" 분기로 명시.

---

## 0. 진입 조건 재확인 (Entry Criteria)

본 개발계획서 작성 시점 (2026-05-11) 에 EC-1~4 모두 충족 — 구현 진입 가능.

| EC | 충족 여부 | 검증 |
|----|----------|------|
| EC-1 commit 존재 | ✅ | `git log --oneline master | grep dbinitrunner-dollar-quote-aware` → `ba12fc6` 1건 |
| EC-2 검증 PASS 기록 | ✅ | `docs/exec-plans/dbinitrunner-dollar-quote-aware.md` v3.1 — UT 18/18 + 양쪽 prod 4/4 PASS |
| EC-3 splitter dollar-quote-aware | ✅ | `DbInitRunner.java:87-179` 상태머신 (4 state) + `DbInitRunnerTest`/`DbInitRunnerBaselineTest`/`DbInitRunnerIntegrationTest` 3종 |
| EC-4 기획서 v3 + codex 2차+ ⭕ | ✅ | 기획서 v3 codex 3차 ⭕ 완료 |

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔 (수정 위치 / 식별자 정합 확인)

1-1. **phase2.sql INSERT 위치 재확인**:
   ```bash
   grep -n "ON CONFLICT" src/main/resources/db_init_phase2.sql
   ```
   - 기대: line 60 (`tb_process_master`), line 70 (`tb_service_purpose`)

1-2. **V018 동일 식별자 재확인**:
   ```bash
   grep -n "uq_process_master_sys_name\|uq_service_purpose_sys_type_md5" swdept/sql/V018_process_master_dedup.sql
   ```
   - 기대: 4 hits (constraint name + index name 각 2회)

1-3. **V018 SHA256 baseline 기록** (NFR-6 검증용):
   ```bash
   sha256sum swdept/sql/V018_process_master_dedup.sql > /tmp/v018-sha256-before.txt
   cat /tmp/v018-sha256-before.txt
   ```

1-4. **DbInitRunner splitter dollar-quote-aware 재확인** (EC-3 보강 검증):
   ```bash
   grep -n "IN_DOLLAR_QUOTE\|tryReadDollarTag" src/main/java/com/swmanager/system/config/DbInitRunner.java
   ```
   - 기대: 5+ hits (state enum + branches + helper)

**진행 게이트**: 1-1~1-4 모두 기대값 일치. 미일치 시 Step 진입 중단 + codex 추가 자문.

---

### Step 2 — `db_init_phase2.sql` 수정 (FR-1-A/B/C/E + NFR-3-x + FR-3)

본 스프린트 핵심. 단일 파일 수정. 4개 블록 추가/수정.

> **편집 위치 표기 규칙 (v1.1)**: line 번호는 **수정 직전 phase2.sql 기준**. 하나의 Edit 후 후속 line 번호는 shift 되므로, 각 step 의 anchor 텍스트 (예: `tb_process_master CREATE TABLE 닫힘 \`);\``) 가 우선이고 line 번호는 참고. 실제 편집 시 anchor 로 위치 잡고 진행.

#### 2-1. 헤더 주석 보강 (FR-3-A)

**Anchor**: `db_init_phase2.sql` 헤더 영역의 마지막 닫힘 `-- ===` 줄 (수정 직전 line 19) **직전**.

수정 직전 컨텍스트:
```sql
-- phase1 정식화 스프린트: phase1-ddl-formalization (2026-04-27)
-- 감사 2026-04-18 P2 2-2 조치 (스프린트 2a) → 본 스프린트로 후속 완료.
-- ============================================================  ← 이 줄 직전에 1줄 추가
```

추가 1줄:
```sql
-- phase2-V018-init-ordering (2026-05-11) — V018 의 UNIQUE 제약·INDEX 를 phase2 의 INSERT 앞에 선이동. V018 무수정. 선행: dbinitrunner-dollar-quote-aware (ba12fc6).
```

#### 2-2. `tb_process_master` UNIQUE 제약 선이동 (FR-1-A)

**Anchor**: `tb_process_master` CREATE TABLE 닫힘 `);` (수정 직전 **line 40**) **직후**, 기존 빈 줄 (line 41) **앞**.
v1 의 "line 41 직후" 는 1줄 오프셋 — codex 1차 검토에서 발견. 본 v1.1 에서 정정.

수정 직전 컨텍스트:
```sql
    use_yn VARCHAR(1) DEFAULT 'Y'
);                                        ← line 40 (이 줄 직후에 새 블록 삽입)
                                          ← line 41 (기존 빈 줄, 보존)
-- 3. 시스템별 용역 목적/과업 내용 마스터  ← line 42
```

삽입할 블록:

```sql

-- [phase2-V018-init-ordering] V018 의 UNIQUE 제약을 INSERT 앞에 선이동 (멱등 가드).
-- V018 (4) 와 동일 식별자/동일 컬럼. V018 재진입 시 conname EXISTS 로 무해 PASS.
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_process_master_sys_name') THEN
    ALTER TABLE tb_process_master
      ADD CONSTRAINT uq_process_master_sys_name
      UNIQUE (sys_nm_en, process_name);
  END IF;
END $$ LANGUAGE plpgsql;
```

#### 2-3. `tb_service_purpose` UNIQUE INDEX 선이동 (FR-1-B)

**Anchor**: `tb_service_purpose` CREATE TABLE 닫힘 `);` (수정 직전 **line 50**) **직후**, 기존 빈 줄 (line 51) **앞**.

수정 직전 컨텍스트:
```sql
    use_yn VARCHAR(1) DEFAULT 'Y'
);                                ← line 50 (이 줄 직후에 새 블록 삽입)
                                  ← line 51 (기존 빈 줄, 보존)
-- 기본 공정명 데이터              ← line 52
```

삽입할 블록:

```sql

-- [phase2-V018-init-ordering] V018 의 표현식 UNIQUE INDEX 를 INSERT 앞에 선이동 (PG 9.5+ 멱등).
-- V018 (4) 와 동일 식별자/동일 표현식. V018 재진입 시 IF NOT EXISTS 로 무해 PASS.
CREATE UNIQUE INDEX IF NOT EXISTS uq_service_purpose_sys_type_md5
  ON tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text));
```

#### 2-4. NFR-3-x 검증 게이트 (필수) — INSERT 두 개 직전

**Anchor**: `-- 기본 공정명 데이터` 주석 (수정 직전 **line 52**) **직전** — 즉 `tb_service_purpose` UNIQUE INDEX 블록 (Step 2-3) 직후, INSERT 두 블록 직전.

**v1.1 강화** (codex 1차 권고): `pg_constraint` 검사를 `conrelid='public.tb_process_master'::regclass` + `contype='u'` 까지 좁히고, `pg_indexes` 검사에 `tablename='tb_service_purpose'` 추가. 이름만 동일한 다른 테이블/스키마 객체에 false PASS 회귀 차단.

삽입할 블록:

```sql
-- [phase2-V018-init-ordering NFR-3-x] UNIQUE 제약·INDEX 등록 게이트 (필수, v1.1 강화).
-- 두 INSERT 가 ON CONFLICT 추론 실패(42P10) 또는 UNIQUE 미등록 상태로 진행되는 회귀를 fast-fail.
-- 트랜잭션 경계 부재 (§5-7 정책) 의 보강책 — 게이트 통과 못하면 INSERT 진입 자체를 차단.
-- v1.1: conrelid/contype/tablename 까지 검사 — 이름만 동일한 다른 객체 false PASS 차단.
DO $$
DECLARE
  uq_proc_cnt int;
  uq_purp_cnt int;
BEGIN
  SELECT COUNT(*) INTO uq_proc_cnt
    FROM pg_constraint
   WHERE conname = 'uq_process_master_sys_name'
     AND conrelid = 'public.tb_process_master'::regclass
     AND contype = 'u';
  SELECT COUNT(*) INTO uq_purp_cnt
    FROM pg_indexes
   WHERE schemaname = 'public'
     AND indexname = 'uq_service_purpose_sys_type_md5'
     AND tablename = 'tb_service_purpose';
  IF uq_proc_cnt <> 1 THEN
    RAISE EXCEPTION 'HALT [phase2-V018-init-ordering NFR-3-x]: uq_process_master_sys_name 미등록 또는 정의 불일치 (count=%) — INSERT 진행 차단', uq_proc_cnt;
  END IF;
  IF uq_purp_cnt <> 1 THEN
    RAISE EXCEPTION 'HALT [phase2-V018-init-ordering NFR-3-x]: uq_service_purpose_sys_type_md5 미등록 또는 정의 불일치 (count=%) — INSERT 진행 차단', uq_purp_cnt;
  END IF;
  RAISE NOTICE 'PASS [phase2-V018-init-ordering NFR-3-x]: UNIQUE 제약·INDEX 등록 확인 — INSERT 진행 가능';
END $$ LANGUAGE plpgsql;
```

#### 2-5. INSERT 라인 주석 보강 (FR-3-B) — 기존 주석에 1줄 추가

**Anchor 1**: `tb_process_master` INSERT 위의 `-- V018 (process-master-dedup 2026-04-20) 이후: UNIQUE(sys_nm_en, process_name) 적용됨` 줄 (수정 직전 **line 53**) **직후**.

기존 (수정 직전):
```sql
-- 기본 공정명 데이터                                                          ← line 52
-- V018 (process-master-dedup 2026-04-20) 이후: UNIQUE(sys_nm_en, process_name) 적용됨   ← line 53
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) VALUES   ← line 54
```

변경 (line 53 직후 1줄 추가):
```sql
-- 기본 공정명 데이터
-- V018 (process-master-dedup 2026-04-20) 이후: UNIQUE(sys_nm_en, process_name) 적용됨
-- [phase2-V018-init-ordering 2026-05-11] phase2 에서 선이동됨 — fresh-init 환경에서도 본 INSERT 의 ON CONFLICT 가 동작.
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) VALUES
```

**Anchor 2**: `tb_service_purpose` INSERT 위의 `-- V018 이후: UNIQUE INDEX uq_service_purpose_sys_type_md5 (sys_nm_en, purpose_type, md5(purpose_text)) 적용됨` 줄 (수정 직전 **line 63**) **직후** — 동일 패턴 1줄 보강:

```sql
-- [phase2-V018-init-ordering 2026-05-11] phase2 에서 선이동됨 — fresh-init 환경에서도 본 INSERT 의 ON CONFLICT 가 동작.
```

#### 2-6. compile 검증

```bash
./mvnw clean compile
```
- 기대: BUILD SUCCESS. (SQL 만 변경, Java 영향 0 — NFR-1)
- v1.1 정정: 기획서 NFR-1 표기 (`./mvnw clean compile`) 와 통일.

**진행 게이트**: 2-1~2-5 편집 완료 + 2-6 BUILD SUCCESS. 실패 시 Edit 되돌림 후 재진입.

---

### Step 3 — ephemeral 클러스터 검증 (NFR-2 / NFR-3 / NFR-4 / NFR-7)

기획서 §5-5 에서 보존된 클러스터 (`C:\Users\PUJ\pg16-verify\data`, `localhost:25880`) 재기동 후 T-매트릭스 실행.

#### 3-0. 클러스터 기동

```bash
"C:/Users/PUJ/PostgreSQL/16/bin/pg_ctl.exe" -D "C:/Users/PUJ/pg16-verify/data" -l "C:/Users/PUJ/pg16-verify/log/pg.log" start
"C:/Users/PUJ/PostgreSQL/16/bin/psql.exe" -h localhost -p 25880 -U postgres -c "SELECT 1;"
```
- 기대: `pg_ctl: server started` + `?column? \n----------\n        1`

#### 3-1. 테스트 DB 새로 만들기 (각 T 별 격리)

**v1.1 정정** (codex 1차 권고): `DROP DATABASE` 와 `CREATE DATABASE` 는 PostgreSQL 에서 트랜잭션 블록 안에서 실행 불가. v1 의 `;` 로 묶은 단일 `-c` 호출은 `ON_ERROR_STOP=1` 환경에서 실패할 수 있음 → `-c` 별도 호출로 분리.

```bash
export PGPASSWORD=ephemeral
PSQL='"C:/Users/PUJ/PostgreSQL/16/bin/psql.exe" -h localhost -p 25880 -U postgres -v ON_ERROR_STOP=1'
for db in sw_t1 sw_t2 sw_t3; do
  $PSQL -c "DROP DATABASE IF EXISTS $db;"
  $PSQL -c "CREATE DATABASE $db;"
done
```

#### 3-2. T1 — phase2.sql 단독 멱등 3회 (NFR-3)

```bash
# 선행: phase1 + sigungu (phase2 가 의존)
$PSQL -d sw_t1 -f src/main/resources/db_init_phase1.sql > /tmp/t1_phase1.log 2>&1
$PSQL -d sw_t1 -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/t1_sigungu.log 2>&1

# phase2.sql 3회 연속 (멱등성)
for i in 1 2 3; do
  $PSQL -d sw_t1 -f src/main/resources/db_init_phase2.sql > /tmp/t1_phase2_run$i.log 2>&1
  echo "Run $i rc=$?"
done

# Pass 기준 (NFR-3 a~d)
$PSQL -d sw_t1 -tAc "SELECT COUNT(*), COUNT(DISTINCT (sys_nm_en, process_name)) FROM tb_process_master;"
$PSQL -d sw_t1 -tAc "SELECT COUNT(*), COUNT(DISTINCT (sys_nm_en, purpose_type, md5(purpose_text))) FROM tb_service_purpose;"
$PSQL -d sw_t1 -tAc "SELECT COUNT(*) FROM pg_constraint WHERE conname = 'uq_process_master_sys_name';"
$PSQL -d sw_t1 -tAc "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'uq_service_purpose_sys_type_md5';"
grep -ci "ERROR" /tmp/t1_phase2_run*.log
```
- **Pass 기준**: 3회 모두 rc=0. count=5, distinct=5 (양 테이블). UNIQUE 1건씩. ERROR 0.

#### 3-3. T2 — psql 경로 fresh-init (NFR-2-a, T7 회귀)

```bash
$PSQL -d sw_t2 -f src/main/resources/db_init_phase1.sql > /tmp/t2_phase1.log 2>&1
$PSQL -d sw_t2 -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/t2_sigungu.log 2>&1
$PSQL -d sw_t2 -f src/main/resources/db_init_phase2.sql > /tmp/t2_phase2.log 2>&1
echo "phase2 rc=$?"
# phase2 통과 시 V*.sql 순차
for v in swdept/sql/V*.sql; do
  $PSQL -d sw_t2 -f $v >> /tmp/t2_v_all.log 2>&1
  echo "$v rc=$?"
done
```
- **Pass 기준**: phase2.sql rc=0 (T7 재현 0 stop). `42P10` 0건. NFR-3-x NOTICE "PASS" 출력. V*.sql 전체 rc=0 (V018 도 멱등 통과).

#### 3-4. T2-b — DbInitRunner 경로 fresh-init (NFR-2-b, NFR-7)

```bash
# 새 DB 만들고 phase1+sigungu 만 미리 적용 (DbInitRunner 는 phase2 만 실행)
$PSQL -c "DROP DATABASE IF EXISTS sw_t2b;"
$PSQL -c "CREATE DATABASE sw_t2b;"
$PSQL -d sw_t2b -f src/main/resources/db_init_phase1.sql > /tmp/t2b_phase1.log 2>&1
$PSQL -d sw_t2b -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/t2b_sigungu.log 2>&1

# 서버 재기동 — DB_URL 을 sw_t2b 로 override
DB_URL="jdbc:postgresql://localhost:25880/sw_t2b" \
DB_USERNAME=postgres \
DB_PASSWORD=ephemeral \
bash server-restart.sh > /tmp/t2b_boot.log 2>&1
sleep 15
grep -E "Started SwmanagerApplication|ERROR|PSQLException|syntax error at or near" /tmp/t2b_boot.log
```
- **Pass 기준**: `Started SwmanagerApplication` 로그 출력. `ERROR` 0건 (단, jdbcTemplate.execute 실패 debug 로그는 무시). `PSQLException` / `syntax error` 0건 (DbInitRunner splitter 가 DO `$$ ... $$` 를 단일 stmt 로 처리했음 입증).

#### 3-5. T3 — V018 재진입 멱등 (NFR-4)

T1 의 sw_t1 (phase2.sql 3회 적용 상태) 에 V018 추가 실행:

```bash
$PSQL -d sw_t1 -f swdept/sql/V018_process_master_dedup.sql > /tmp/t3_v018.log 2>&1
echo "V018 rc=$?"
grep -E "DELETE [0-9]+|ERROR|HALT|PASS:" /tmp/t3_v018.log
```
- **Pass 기준**: rc=0. `DELETE 0` (이미 distinct=5). `HALT` 0건. `PASS: dedup + constraints + NOT NULL applied` NOTICE 출력.

#### 3-6. T4 — V018 SHA256 무변경 (NFR-6)

```bash
sha256sum swdept/sql/V018_process_master_dedup.sql > /tmp/v018-sha256-after.txt
diff /tmp/v018-sha256-before.txt /tmp/v018-sha256-after.txt
```
- **Pass 기준**: `diff` 출력 0줄.

**진행 게이트**: T1~T4 + T2-b 모두 PASS. 1건이라도 FAIL 시 Step 4 미진입 + Edit 되돌림 후 진단.

---

### Step 4 — 문서 갱신 (FR-4)

#### 4-1. `docs/PLANS.md` §2-b/c 갱신 (FR-4-A)

§2-b 후속 백로그에서 `phase2-V018-init-ordering` 항목을 §2-c (또는 §2-b 하위 완료 섹션) 으로 이동. 다음 1~2줄 요약 포함:

```markdown
- **phase2-V018-init-ordering** (2026-05-11 완료, commit `<TBD>`):
  - V018 의 UNIQUE 제약·INDEX 를 phase2.sql 의 INSERT 앞에 선이동 + NFR-3-x 검증 게이트 추가.
  - V018 무수정 (SHA256 변동 0). fresh-init 환경 T7 stop 해소.
  - 검증: ephemeral 25880 T1~T4 + T2-b 모두 PASS.
```

#### 4-2. `docs/references/setup-guide.md` §2-2 보강 (FR-4-B)

신규 환경 초기화 절차 본문은 변경 없음. 절차 끝에 1줄 추가:

```markdown
> **2026-05-11 보강** (`phase2-V018-init-ordering` 적용 후): 본 절차의 `phase2.sql` 통과는 V018 의 UNIQUE 제약·INDEX 가 phase2 자체에 선이동되어 보장됨. fresh-init 환경에서 `42P10` stop 회귀 차단.
```

#### 4-3. (선택) `docs/exec-plans/phase1-ddl-formalization.md` Step 8 T7 결과 보강 (FR-4-C)

T7 결과 줄에 다음 1줄 추가:

```markdown
- **2026-05-11 후속**: `phase2-V018-init-ordering` 스프린트 (commit `<TBD>`) 에서 phase2.sql UNIQUE 선이동 + NFR-3-x 게이트로 해소.
```

> 선택 항목 — 시간 여유 시 반영. 누락해도 본 스프린트 PASS.

**진행 게이트**: 4-1, 4-2 완료 (4-3 선택). 문서 변경만 — compile 영향 0.

---

### Step 5 — 빌드 / 재기동 / 회귀 스모크 (NFR-1, NFR-7)

5-1. **Maven compile** (NFR-1):
```bash
./mvnw clean compile
```
- 기대: BUILD SUCCESS. v1.1: 기획서 NFR-1 표기와 통일 (v1 의 `-q compile` → `clean compile`).

5-2. **운영 환경 변수로 서버 재기동** (운영DB 영향 0 검증 — NFR-5):
```bash
bash server-restart.sh > /tmp/final_boot.log 2>&1
sleep 20
grep -E "Started SwmanagerApplication|ERROR|PSQLException" /tmp/final_boot.log
```
- 기대: `Started SwmanagerApplication`. ERROR 0. PSQLException 0.

> **중요**: 운영DB 는 phase2/V018 이미 양쪽 적용된 상태. DbInitRunner 가 phase2.sql 재실행 시 DO 가드 + IF NOT EXISTS 로 무해 PASS 해야 함 (R-4 완화).

5-3. **회귀 스모크 — 기존 기능 정상 동작 확인** (수동):
- 점검내역서 화면 → 목록 1건 진입 → 정상 렌더
- 사업관리 화면 → 목록 1건 진입 → 정상 렌더
- 견적서 화면 → 목록 1건 진입 → 정상 렌더

**진행 게이트**: 5-1 BUILD SUCCESS + 5-2 ERROR 0 + 5-3 3개 화면 정상. → "작업완료" 발화 가능.

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | phase2.sql 단독 멱등 3회 (NFR-3) | Step 3-2 | 3회 rc=0, count=5/5, distinct=5/5, UNIQUE 1/1, ERROR 0 |
| T2 | psql 경로 fresh-init (NFR-2-a) | Step 3-3 | phase2.sql rc=0 (T7 stop 해소), V*.sql 전체 rc=0 |
| T2-b | DbInitRunner 경로 fresh-init (NFR-2-b, NFR-7) | Step 3-4 | `Started SwmanagerApplication`, ERROR 0, PSQLException 0 |
| T3 | V018 재진입 멱등 (NFR-4) | Step 3-5 | rc=0, DELETE 0, HALT 0, NOTICE PASS |
| T4 | V018 SHA256 무변경 (NFR-6) | Step 3-6 | diff 0줄 |
| T5 | Maven compile (NFR-1) | Step 5-1 | BUILD SUCCESS |
| T6 | 운영 환경 부팅 (NFR-5, NFR-7) | Step 5-2 | Started + ERROR 0 |
| T7 | 회귀 스모크 (NFR-7) | Step 5-3 | 3개 화면 정상 |

총 8 케이스. 1건이라도 FAIL 시 "작업완료" 발화 금지 + Step 진단 필요.

---

## 3. 롤백 전략

본 스프린트 변경 surface 가 작아 롤백 단순. **Step 4 (문서 갱신) 진입 전후로 revert 범위가 다름** — v1.1 codex 권고로 분기 명시.

| 상황 | 조치 |
|------|------|
| **Step 2** compile 실패 | `git checkout -- src/main/resources/db_init_phase2.sql` 후 Edit 재진입 |
| **Step 3** T1~T4 1건이라도 FAIL | 동일 (단일 파일 revert) + 로그 분석 후 Step 2 재진입 |
| **Step 4 진입 전 단계 실패 (Step 1~3, 5-1, 5-2 일부)** | `git checkout -- src/main/resources/db_init_phase2.sql` (단일 파일 revert) |
| **Step 4 이후 단계 실패 (Step 5-2/5-3, commit 직전)** | `git checkout -- src/main/resources/db_init_phase2.sql docs/PLANS.md docs/references/setup-guide.md` (+ 4-3 적용했으면 `docs/exec-plans/phase1-ddl-formalization.md`). **문서 파일도 함께 revert** 해야 "스프린트 완료" 라는 잘못된 메시지가 master 에 남지 않음 |
| Step 5-2 운영 부팅 ERROR | 위 표에서 단계 시점에 맞는 revert + ERROR 메시지로 NFR-3-x 가드 false fail 인지 진단. 진짜 회귀면 즉시 복구 |
| **배포 후 회귀** (commit 후, push 전) | `git reset --hard HEAD~1` (해당 commit 가 HEAD 일 때만). 이미 push 됐으면 아래 줄 |
| 배포 후 회귀 (commit + push 후) | `git revert <commit>` → push → `bash server-restart.sh`. SQL+문서 통합 1 commit (§5 권장) 이라 단일 revert 로 안전 복원 |
| 운영DB 손상 가능성 | **0** (NFR-5) — 본 스프린트는 운영DB 에 어떤 SQL 도 직접 실행하지 않음. 운영 phase2 재실행 시 멱등 가드로 무해 |

---

## 4. 리스크·완화 재확인 (기획서 §7 R-1~7 그대로 유지)

| ID | 리스크 | 수준 | 본 개발계획에서의 완화 |
|----|--------|------|---------------------|
| R-1 | UNIQUE 제약 식별자 충돌 | 매우 낮음 | Step 2-2 의 `pg_constraint` lookup DO 가드. T3 (Step 3-5) 로 검증 |
| R-2 | 표현식 UNIQUE INDEX 멱등성 | 매우 낮음 | Step 2-3 `IF NOT EXISTS`. PG 16 (T1~T4 모두 PG 16). T1 3회 멱등 검증 |
| R-3 | INSERT 가 ALTER 보다 먼저 | 0 | Step 2 의 삽입 위치 (line 41 직후, line 50 직후 — 모두 line 53/63 INSERT 앞) |
| R-4 | 운영DB 재실행 시 충돌 | 매우 낮음 | DO 가드 + IF NOT EXISTS. Step 5-2 운영 환경 부팅 검증 |
| R-5 | V018 checksum 변경 | 0 | T4 (Step 3-6) SHA256 diff 0줄 |
| R-6 | 사용자 수동 INSERT (out-of-contract) | 매우 낮음 | NFR-3-x 게이트 통과 후 INSERT 도 ON CONFLICT 로 멱등 |
| R-7 | DbInitRunner splitter dollar-quote | **해소 (선행 완료)** | EC-1~3 충족 (commit `ba12fc6`). T2-b (Step 3-4) 로 실제 동작 입증 |

---

## 5. 산출물 / 커밋 단위

**v1.1 정책 결정** (codex 1차 권고): **1 commit 통합 권장**. SQL 변경과 문서 갱신이 같은 스프린트의 원자적 산출물이며, 분리 시 git history 노이즈 + revert 시 누락 리스크 ↑. Step 4-3 (선택 phase1 결과 보강) 까지 동일 commit 에 포함 가능.

**Commit (필수, 1건)** — 코드 + 모든 문서:
- `src/main/resources/db_init_phase2.sql` (Step 2)
- `docs/PLANS.md` (Step 4-1)
- `docs/references/setup-guide.md` (Step 4-2)
- (선택) `docs/exec-plans/phase1-ddl-formalization.md` (Step 4-3)

커밋 메시지 (예시):
```
feat(phase2-V018-init-ordering): V018 UNIQUE 제약·INDEX 를 phase2.sql INSERT 앞에 선이동

기존 fresh-init (T7) 에서 phase2.sql:60 의 ON CONFLICT 가 V018 의 UNIQUE 미존재로
42P10 stop. V018 의 동일 식별자 UNIQUE 제약/표현식 INDEX 를 phase2 의 두 INSERT
앞에 선이동 (DO 가드 + IF NOT EXISTS). NFR-3-x 검증 게이트 추가 — UNIQUE 미등록
상태 INSERT 진입 자체를 차단.

V018 무수정 (SHA256 변동 0). 운영DB 영향 0 (이미 양쪽 적용 상태, 재실행 시 멱등).

검증 (ephemeral 25880):
- T1~T4 + T2-b 모두 PASS (Maven compile + psql/DbInitRunner 양 경로 fresh-init
  + V018 재진입 멱등 + V018 SHA256 무변경)

근거:
- 기획서: docs/product-specs/phase2-V018-init-ordering.md v3 (codex 3차 ⭕ 2026-05-11)
- 개발계획: docs/exec-plans/phase2-V018-init-ordering.md v1
- 선행: dbinitrunner-dollar-quote-aware (ba12fc6, EC-1~3 충족)

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
```

> master 직접 push (single-shot, harness-hardening-v1 confirm 게이트 적용 — `git status` + `git diff` echo 후 사용자 1회 confirm).
> 본 스프린트 산출물 (기획서 v3 + 개발계획서 v1.1) 은 **별도 commit** 권장 (구현 commit 과 분리). 기획·개발계획만 먼저 commit → 사용자 최종승인 후 구현 commit. 두 단계 분리로 review 단위 명확화.

---

## 6. 승인 요청

본 개발계획서 v1.1 에 대한 codex 2차 검토 ⭕ 완료 (2026-05-11) — 사용자 최종승인 요청.

### 검토 포인트 (v1.1 — codex 1차 ⚠ 5건 반영 후 재검토 요청)
- v1 의 line 번호 오프셋 (Step 2-2 line 41 → 40 정정) 이 anchor 기반으로 robust 해졌는지
- v1.1 강화된 NFR-3-x DO 가드 SQL (`conrelid` + `contype='u'` + `tablename`) 이 false PASS 회귀를 충분히 차단하는지
- Step 3-1 `for` 루프로 분리된 DROP/CREATE 가 transaction block 안전성을 확보하는지
- 롤백 표 "Step 4 이전/이후" 분기가 모든 단계 실패 시나리오를 cover 하는지
- §5 커밋 정책 (1 commit 통합) 이 codex 1차 권고와 일치하는지

### 다음 절차
1. **codex 2차 검토** (gpt-5.5 via codex-trace.sh, round=2)
2. ⭕ 또는 ⚠ 권고 시 v1.2 개정
3. 사용자 최종승인 → Step 1~5 순차 실행
4. T1~T7 모두 PASS → 사용자 "작업완료" 발화 → 자동 commit+push (트리거 confirm 게이트)
