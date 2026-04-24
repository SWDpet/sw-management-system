---
tags: [dev-plan, sprint, refactor, schema, drop]
sprint: "legacy-contract-tables-drop"
status: draft-v2
created: "2026-04-20"
---

# [개발계획서] 레거시 계약 테이블 DROP

- **작성팀**: 개발팀
- **작성일**: 2026-04-20
- **근거 기획서**: [[legacy-contract-tables-drop]] v2 (사용자 최종승인)
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 5건 반영)
1. **순서 재배치** — Step 4(DROP) 검증 후 Step 5(ERD)/Step 6(감사 문서) 수행 → 실패 시 문서 선반영 방지
2. **V010 DO 블록 보강** — `to_regclass()` 테이블 존재 확인 + `pg_depend` 비-FK 의존성(view/rule/function) 0건 체크 추가
3. **V100 주석 경계 확장** — v1은 50-93만이었는데 실측 결과 `tb_contract_target`도 V100:113-128에 정의됨. **두 블록 주석 처리** (50-93 + 113-128). 중간 97-111 `tb_contract_participant`는 절대 미변경
4. **롤백 문구 정정** — `tb_contract_target`도 V100에 있으므로 "별도 커밋" 표현 삭제. 윈도우(Git Bash/PowerShell) 기준 명령 병기
5. **T9·T10 추가** — T9 `pg_depend` 의존성 재확인, T10 문서 생성/저장 쓰기 경로 스모크
6. **DbInitRunner 표현 정정** — `V100 자동실행 확인`은 실효성 낮음 → `db_init_phase2.sql` 영향 없음 확인으로 변경

---

## 0. 전제 / 환경

### 0-1. 대상 DB
- PostgreSQL, 스키마 `public`
- URL: `jdbc:postgresql://211.104.137.55:5881/SW_Dept`
- 계정: `postgres` (superuser — DROP TABLE 권한 있음)

### 0-2. 범위 고정
- DROP 대상: `tb_contract`, `tb_contract_target` **2개 테이블만**
- **동시 레거시 정리 금지** — `tb_inspect_checklist`, `tb_inspect_issue`, `tb_document_signature` 등 다른 고아 테이블은 본 스프린트에서 **절대 건드리지 않음** (기획서 §4-4 게이트)
- Java 코드 변경 **0**

### 0-3. 실행 전제
- 기획서 FR-0 사전검증 SQL 결과 **외부 FK 0건 + 레코드 0건** 확인 후에만 진행
- 실행 시점: **업무시간 외 권장** (사용자 판단)

---

## 1. 작업 순서

### Step 1 — 사전검증 (FR-0)

**1-1. 현재 DB 상태 재스캔**
```bash
# 러너 실행 (기존 감사용 data-architecture-scan.java 패턴 재사용)
cd C:/Users/PUJ/eclipse-workspace/swmanager
JAR=~/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
DB_PASSWORD='***' java -cp "$JAR" docs/dev-plans/legacy-contract-precheck.java
```

**1-2. `legacy-contract-precheck.java` 신규 러너 작성**:
- 안전통제: `SET TRANSACTION READ ONLY` + `statement_timeout 10s`
- 실행 쿼리 (기획서 §FR-0):
  1. 외부 FK 조회 SQL
  2. 레코드 수 재확인 SQL
- 출력: `docs/dev-plans/legacy-contract-precheck-result.md`
- **진행 게이트**: 외부 FK 건수 > 0 또는 레코드 수 > 0 이면 콘솔에 `HALT` 출력 + exit code 1

**1-3. 재검증 grep (NFR-5 일부)**
```bash
# tb_contract Java 코드 참조 0 재확인 (ContractParticipant 는 별개라 제외)
rg -n '\btb_contract\b' src/main/java | rg -v 'ContractParticipant'
# 기대: 0 hits
```

### Step 2 — 마이그레이션 SQL 작성 (FR-1)

**2-1. 신규 파일**: `swdept/sql/V010_drop_legacy_contract_tables.sql`

내용 (버전 번호는 기존 V###과 겹치지 않는 가장 낮은 미사용 번호로 결정 — 스캔 후 확정):
```sql
-- ============================================================
-- V010: 레거시 계약 테이블 DROP
-- Sprint: legacy-contract-tables-drop (2026-04-20)
-- 근거: docs/plans/legacy-contract-tables-drop.md v2
-- 사전검증: FR-0 통과 (외부 FK 0건 + 레코드 0건)
-- 롤백: 8d79f82:swdept/sql/V100_work_plan_performance_tables.sql:50-93 에서 원본 DDL 추출
-- ============================================================

BEGIN;

-- 사전검증 재확인 (트랜잭션 내 최종 보장, v2 codex 권장 #3 강화)
DO $$
DECLARE
  cnt_contract   bigint;
  cnt_target     bigint;
  cnt_ext_fk     bigint;
  cnt_dep        bigint;
BEGIN
  -- (a) 테이블 존재 확인 (to_regclass — NULL 이면 이미 없음)
  IF to_regclass('public.tb_contract') IS NULL
     AND to_regclass('public.tb_contract_target') IS NULL THEN
    RAISE NOTICE 'SKIP: both tables already absent — no-op';
    RETURN;
  END IF;

  -- (b) 레코드 수 체크 (존재하는 테이블만)
  IF to_regclass('public.tb_contract') IS NOT NULL THEN
    SELECT COUNT(*) INTO cnt_contract FROM tb_contract;
    IF cnt_contract > 0 THEN
      RAISE EXCEPTION 'HALT: tb_contract has % rows (expected 0)', cnt_contract;
    END IF;
  END IF;
  IF to_regclass('public.tb_contract_target') IS NOT NULL THEN
    SELECT COUNT(*) INTO cnt_target FROM tb_contract_target;
    IF cnt_target > 0 THEN
      RAISE EXCEPTION 'HALT: tb_contract_target has % rows (expected 0)', cnt_target;
    END IF;
  END IF;

  -- (c) 외부 FK 0건
  SELECT COUNT(*) INTO cnt_ext_fk
    FROM information_schema.table_constraints tc
    JOIN information_schema.constraint_column_usage ccu
      ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
   WHERE tc.constraint_type = 'FOREIGN KEY'
     AND tc.table_schema = 'public'
     AND ccu.table_name IN ('tb_contract', 'tb_contract_target')
     AND tc.table_name NOT IN ('tb_contract', 'tb_contract_target');
  IF cnt_ext_fk > 0 THEN
    RAISE EXCEPTION 'HALT: external FK(s) reference target tables (% found)', cnt_ext_fk;
  END IF;

  -- (d) 비-FK 의존성 0건 (view/rule/function 등 pg_depend 기반)
  SELECT COUNT(*) INTO cnt_dep
    FROM pg_depend d
    JOIN pg_class c ON d.refobjid = c.oid
    JOIN pg_namespace n ON c.relnamespace = n.oid
   WHERE n.nspname = 'public'
     AND c.relname IN ('tb_contract', 'tb_contract_target')
     AND d.deptype NOT IN ('a','i');  -- a(auto) / i(internal) 제외 — sequence·index 등
  IF cnt_dep > 0 THEN
    RAISE EXCEPTION 'HALT: non-FK dependencies (view/rule/function) found: %', cnt_dep;
  END IF;
END
$$ LANGUAGE plpgsql;

-- FK 의존 순서: target(자식) 먼저 → contract(부모) 나중
DROP TABLE IF EXISTS tb_contract_target;
DROP TABLE IF EXISTS tb_contract;

-- 사후 확인
DO $$
DECLARE c bigint;
BEGIN
  SELECT COUNT(*) INTO c FROM information_schema.tables
   WHERE table_schema = 'public' AND table_name IN ('tb_contract', 'tb_contract_target');
  IF c <> 0 THEN
    RAISE EXCEPTION 'HALT: post-check failed. Tables still exist: %', c;
  END IF;
END
$$ LANGUAGE plpgsql;

COMMIT;

-- 롤백 SQL (참고, 별도 파일로 수동 실행 대상)
-- git show 8d79f82:swdept/sql/V100_work_plan_performance_tables.sql
--   의 라인 50~93 을 추출 + tb_contract_target DDL (같은 커밋에서 조회) 실행
```

**2-2. 버전 번호 결정**
```bash
ls swdept/sql/V*.sql | sort
# 현재 V100 등 존재. V010 사용 가능 여부 확인 (없으면 V010 채택, 있으면 V011/V012로 증가)
```

### Step 3 — V100 파일 재생성 방지 (FR-2, v2 경계 확장)

**3-1. `swdept/sql/V100_work_plan_performance_tables.sql` 편집 — 두 블록 주석 처리**

실측 결과 두 테이블 모두 V100에 정의됨:

| 대상 | 라인 범위 | 내용 |
|------|----------|------|
| **블록 A — `tb_contract`** | **50-93** | 주석 헤더(50-51) + CREATE + 3 INDEX + COMMENT |
| (중간 — 미변경) | 95-111 | `tb_contract_participant` — **절대 건드리지 않음 (사용 중)** |
| **블록 B — `tb_contract_target`** | **113-128** | 주석 헤더(113-115) + CREATE + COMMENT + INDEX |
| (뒤 — 미변경) | 130~ | `tb_inspect_cycle` 등 — 미변경 |

**처리 방법**: 블록 A, B 각각 `/* ... */` 로 감싸고 상단에 "DROPPED" 주석 추가:

```sql
-- ============================================================
-- 3. tb_contract (계약/사업 정보)
-- ⚠️ DROPPED (2026-04-20, sprint: legacy-contract-tables-drop)
-- 사유: 완전 고아 테이블. sw_pjt + ps_info + sigungu_code 로 기능 완전 대체.
-- 롤백: git show 8d79f82:swdept/sql/V100_work_plan_performance_tables.sql 라인 50-93
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS tb_contract (
    ... (원문 전체 보존)
);
COMMENT ON TABLE tb_contract IS '계약/사업 정보';
CREATE INDEX idx_contract_infra ON tb_contract(infra_id);
CREATE INDEX idx_contract_year ON tb_contract(contract_year);
CREATE INDEX idx_contract_status ON tb_contract(progress_status);
*/

-- (라인 95-111 tb_contract_participant 블록 — 건드리지 않음)

-- ============================================================
-- 5. tb_contract_target (유지보수 대상)
-- ⚠️ DROPPED (2026-04-20, sprint: legacy-contract-tables-drop)
-- 롤백: git show 8d79f82:swdept/sql/V100_... 라인 113-128
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS tb_contract_target (
    ... (원문 전체 보존)
);
COMMENT ON TABLE tb_contract_target IS '유지보수 대상 S/W 목록';
CREATE INDEX idx_target_contract ON tb_contract_target(contract_id);
*/
```

**3-2. `DbInitRunner` 영향 범위 정정 (v2 codex 권장 #6)**

실측 (`DbInitRunner.java:30`): 자동 실행 대상은 **`src/main/resources/db_init_phase2.sql`** 뿐. `V100`은 수동 실행 대상 (마이그레이션 성격).

따라서:
- **`db_init_phase2.sql` 영향 없음** 확인 완료 (해당 DDL 없음, v1 grep 0 hits)
- `V100` 주석 처리는 **수동 실행 시 재생성 방지 + 의도 명시**용. 자동 실행 경로 없음
- Step 8 커밋 후 신규 환경에서 `V100`을 **실수로 전체 실행**하는 경우에도 주석 처리로 재생성 방지 보장

```bash
# 영향 없음 확인 명령
rg -n 'tb_contract\b' src/main/resources/db_init_phase2.sql
# 기대: 0 hits
```

### Step 4 — 실제 DB DROP 실행 (v2 순서 재배치: DROP 먼저, 문서 갱신 후)

**4-1. 마이그레이션 SQL 실행** (psql 부재 환경 고려 — JDBC 러너 사용 권장)
```bash
# 러너: docs/dev-plans/legacy-contract-apply.java (Step 2 SQL 파일을 JDBC로 실행)
JAR=~/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
DB_PASSWORD='***' java -cp "$JAR" docs/dev-plans/legacy-contract-apply.java \
  swdept/sql/V010_drop_legacy_contract_tables.sql
# 예상 출력: "DROP TABLE" x 2 + 사전검증 통과 로그
```

**4-2. 실행 결과 검증 (NFR-4, T4)**
```sql
SELECT COUNT(*) FROM information_schema.tables
 WHERE table_schema = 'public'
   AND table_name IN ('tb_contract', 'tb_contract_target');
-- 기대: 0
```

**✅ Step 4 성공해야 Step 5 문서 갱신으로 진행**. 실패 시 롤백 + 원인 분석 (§3 참조).

### Step 5 — ERD / 감사 문서 갱신 (v2: DROP 성공 후에만 수행)

**5-1. `docs/ERD.md` 5번·7번 섹션**
- "미구현" 뒤에 " (삭제 완료 2026-04-20, 스프린트 `legacy-contract-tables-drop`)" 추가

**5-2. 잔존 참조 정리**:
- `docs/erd-diagram.html`: `tb_contract` / `contract_id` 언급에 "(삭제됨 2026-04-20)" 주석
- `docs/erd-descriptions.yml`: 동일
- `docs/erd-document.mmd`: `contract_id FK` 참조 라인 주석 처리 or 제거

### Step 6 — 감사 문서 완료 체크 (Step 5 후)

**6-1. `docs/audit/data-architecture-utilization-audit.md`**
- 기능 8 섹션의 `tb_contract` / `tb_contract_target` 행에 "✅ DROP 완료 (2026-04-20)" 표기

**6-2. `docs/audit/2026-04-18-system-audit.md`**
- "추가 감사 스프린트 — data-architecture-audit" 섹션에 S6 완료 기록

**6-3. `docs/plans/data-architecture-roadmap.md`**
- S6 상태를 "✅ 완료 (2026-04-20)"로 변경
- Wave 1 진행률 업데이트

### Step 7 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q clean compile               # BUILD SUCCESS (Java 코드 변경 0이라 항상 성공)
bash server-restart.sh                # 서버 부팅 정상
```

**런타임 스모크 (NFR-3)** — 4화면:
1. `/login` → 정상 로그인
2. `/projects/list` (사업관리) → 정상 렌더링
3. `/document/list` (문서관리) → 정상 렌더링, INSPECT 클릭 시 상세 진입
4. `/document/inspect-preview/{id}` → PDF 미리보기 정상

### Step 8 — 커밋 / 푸시

```bash
git add swdept/sql/V010_drop_legacy_contract_tables.sql
git add swdept/sql/V100_work_plan_performance_tables.sql
git add docs/dev-plans/legacy-contract-precheck.java
git add docs/dev-plans/legacy-contract-precheck-result.md
git add docs/dev-plans/legacy-contract-tables-drop.md
git add docs/plans/legacy-contract-tables-drop.md
git add docs/plans/data-architecture-roadmap.md
git add docs/audit/data-architecture-utilization-audit.md
git add docs/audit/2026-04-18-system-audit.md
git add docs/ERD.md docs/erd-diagram.html docs/erd-descriptions.yml docs/erd-document.mmd

git commit -m "refactor: tb_contract/tb_contract_target DROP (legacy-contract-tables-drop 완료)"
git push
```

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | 사전검증 통과 (FR-0) | `legacy-contract-precheck` 러너 | 외부 FK 0건 + 레코드 0건 |
| T2 | Java 코드 참조 0 | `rg -n '\btb_contract\b' src/main/java \| rg -v 'ContractParticipant'` | 0 hits |
| T3 | 컴파일 성공 | `./mvnw -q clean compile` | BUILD SUCCESS |
| T4 | DROP 성공 (NFR-4) | `SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('tb_contract','tb_contract_target')` | = 0 |
| T5 | V100 주석 처리 검증 | `grep -n "^CREATE TABLE IF NOT EXISTS tb_contract\b" swdept/sql/V100_*.sql` | 0 hits (원본 active CREATE 제거됨) |
| T6 | 서버 부팅 (NFR-2) | `bash server-restart.sh` | `Started SwManagerApplication` + ERROR 0 |
| T7 | 스모크 4화면 (NFR-3) | 수동 접속 | 4개 모두 정상 렌더링 |
| T8 | 재실행 멱등 (NFR-6) | JDBC 러너로 주석 처리된 V100 파일 실행 (시뮬레이션) | 테이블 재생성 없음 (주석 처리 확인) |
| T9 (v2 추가) | pg_depend 비-FK 의존성 0 | 아래 SQL 실행 (DROP 후) | 0 (view/rule/function 등 잔존 의존 없음) |
| T10 (v2 추가) | 쓰기 경로 회귀 스모크 | 수동: 기존 프로젝트 1건 선택 → 문서 작성(예: 장애처리) 신규 저장 → 저장 후 조회 | 저장·조회 정상 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 1 사전검증 실패 (FK 잔존) | **DROP 중단**. 기획서 §4-6 판단: FK 제약만 제거 후 재시도 (별도 ALTER TABLE) or 본 스프린트 보류 |
| Step 5 DROP 실패 (syntax error 등) | 트랜잭션 자동 ROLLBACK. 원인 분석 후 SQL 수정. |
| DROP 성공 후 숨은 의존 발견 | 복원 절차 (v2 정정):<br>1) `git show 8d79f82:swdept/sql/V100_work_plan_performance_tables.sql > /tmp/v100-orig.sql` (전체 추출)<br>2) 에디터로 열어 **블록 A (50-93 tb_contract)** 와 **블록 B (113-128 tb_contract_target)** 복사<br>3) JDBC 러너로 두 블록 순서대로 실행 (tb_contract 먼저, 그다음 tb_contract_target — FK 의존 순서)<br>Windows 참고: `Get-Content` + 문자열 슬라이싱, Git Bash: `awk 'NR>=50 && NR<=93'` 등 사용 |
| 컴파일·빌드 실패 (있어서는 안 됨) | Java 변경 0이라 발생 가능성 없음. 발생 시 V010/V100 수정 되돌림 후 원인 분석 |

**롤백 기준 커밋**: `8d79f82` (data-architecture-audit 직후)

---

## 4. 리스크·완화 재확인 (기획서 §6과 동일)

| ID | 리스크 | 수준 | 본 개발계획서 적용 |
|----|--------|------|-------------------|
| R-1 | 숨은 코드 의존 발견 | 매우 낮음 | Step 1-3 grep 재검증 + Step 7 스모크 4화면 |
| R-2 | FK 의존으로 DROP 실패 | 낮음 | Step 2 V010 SQL에 `DO $$` 블록으로 사전검증 강제 |
| R-3 | `V100` 재실행 시 재생성 | 중간 | Step 3 주석 처리 + `DbInitRunner` 확인 |
| R-4 | 운영 DB 락 | 낮음 | 업무시간 외 실행 (사용자 판단) |
| R-5 | 범위 팽창 | 낮음 | §0-2 명시 + Step 3-1 주석 처리 경계 93라인까지 |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 확인 사항
- [ ] §0-2 범위 고정 (2개 테이블만)
- [ ] §1 Step 1~8 순서
- [ ] §1 Step 2 V010 SQL의 `DO $$` 사전검증 블록
- [ ] §1 Step 3 V100 주석 처리 경계 (라인 50~93, 97 이후 미변경)
- [ ] §2 T1~T8 테스트 (FR-0 사전검증 강제)
- [ ] §3 롤백 기준 커밋 `8d79f82` 고정
