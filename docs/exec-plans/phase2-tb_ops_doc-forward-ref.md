---
tags: [dev-plan, sprint, schema, init-ordering, forward-reference]
sprint: "phase2-tb_ops_doc-forward-ref"
status: draft-v1.4
created: "2026-05-11"
---

# [개발계획서] phase2.sql 의 tb_ops_doc forward-reference 해소 — 스프린트 phase2-tb_ops_doc-forward-ref

- **작성팀**: 개발팀
- **작성일**: 2026-05-11
- **근거 기획서**: `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` v4 (codex 5차 ⭕ + 사용자 최종승인 2026-05-11)
- **선행 스프린트**: `phase2-V018-init-ordering` (commit `69b6196`, 2026-05-11). EC-1~3 모두 충족 (기획서 §1.5 Entry Criteria 참조).
- **상태**: 초안 v1.4 (codex 1차→4차 ⚠ 권고 모두 반영 — codex 5차 검토 대기)
- **개정 이력**:
  - v1 (2026-05-11): 초안.
  - v1.1 (2026-05-11): codex 1차 ⚠ 4건 반영. (a) Step 3-5 schema diff 방법 정교화 — 기준 DB(a) 가 forward-reference stop 으로 후속 테이블 미생성인 점 명시 + 비교 대상을 (i) 본 스프린트 surface (tb_work_plan, tb_org_unit) full 동등성 + (ii) tb_ops_doc 및 후속 객체는 b DB 단독으로 정의 일치 (선행 sprint pre-Discovery base 와 비교) 로 분리. (b) Step 5-2 ERROR 검출 강화 — 전체 로그 grep 0건 / Started 1건 / DB 초기화 완료 1건 3가지 별도 fail/pass 판정. (c) §5 1 commit 정책 단일화 — 산출물 포함 범위 확정 (기획서·개발계획서 함께 포함). (d) T2 for-loop 실패 즉시 중단 추가 (실패 파일 누적).
  - v1.2 (2026-05-11): codex 2차 ⚠ 3건 반영. (a) Step 3-5 전면 재작성 — base DB 에 `ON_ERROR_STOP=0` 로 phase2 적용 시도 자체가 깨짐 (`tb_ops_doc` CREATE 가 forward-reference FK 로 실패) → DB-level diff 포기하고 **`git diff` 텍스트 비교 기반** 으로 전환 (T3-A: 본 sprint 가 tb_work_plan/tb_org_unit 블록을 위치만 바꾸고 내용 1바이트도 안 건드림 입증, T3-B: tb_ops_doc 등 다른 객체 정의 변경 0 입증). (b) §5 본문에 잔존하던 `DROP/CREATE DATABASE` 한 `-c` 묶음 패턴 제거 (현재 v1.2 의 새 T3 는 DB 직접 사용 안 하므로 자연 해소). (c) §2 T3 Pass 기준 표 갱신 — git diff 기반 측정 항목으로 변경.
  - v1.3 (2026-05-11): codex 3차 ⚠ 3건 반영. (a) **T3-B 를 hunk 단위 검증으로 강화** — 단순 grep 필터 (너무 넓음) 대신 `git diff` 의 hunk header (`@@ -A,B +C,D @@`) 갯수 + 각 hunk 의 위치가 허용 anchor 4종 (헤더 주석 1줄 / 새 [선이동] 블록 삽입 / tb_work_plan 원본 삭제 / tb_org_unit 원본 삭제) 에만 매칭됨을 검증. (b) **T3-A 출력 기호 정정** — `diff <(sort deleted) <(sort added)` 의 결과 기호는 `<` (deleted only) 와 `>` (added only). v1.2 의 "+/-" 표현 정리. Pass 기준: `^<` 0건, `^>` 는 허용된 신규 주석 줄만. (c) **문서 버전 일관성** — frontmatter status / §5 산출물 / §6 승인 요청 모두 `v1.3` 으로 통일.
  - v1.4 (2026-05-11): codex 4차 ⚠ 잔존 3건 정정. (a) §2 T3 Pass 기준 표를 v1.3 hunk 방식으로 갱신 (구버전 `other_changes.txt 줄 수 0` 표현 제거), (b) §5 commit 메시지 예시의 개발계획 버전 `v1` → `v1.4` (또한 검증 결과 항목도 v1.3 의 git diff 표현으로 정리), (c) §6 다음 절차의 `codex 1차 검토` → 일반화된 `codex 검토` 또는 `codex 5차 검토` 로 정리.

---

## 0. 진입 조건 재확인 (Entry Criteria)

| EC | 충족 여부 | 검증 |
|----|----------|------|
| EC-1 commit 머지 | ✅ | `git log --oneline master | head -1` → `69b6196` 직후 master HEAD 인지 확인 |
| EC-2 base 일치 | ✅ | 본 개발계획 작성 시점 (2026-05-11) 의 phase2.sql 가 commit `69b6196` 의 결과물 (선행 sprint UNIQUE 선이동 + NFR-3-x 게이트 포함) |
| EC-3 기획서 v4 codex 5차 ⭕ | ✅ | 본 기획서 §9 체크리스트 |

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔 + base SHA256 기록

1-1. **forward-reference 2건 위치 재확인**:
```bash
grep -nE "REFERENCES tb_org_unit|REFERENCES tb_work_plan" src/main/resources/db_init_phase2.sql
```
기대 (commit 69b6196 직후 base):
- line 439: `org_unit_id BIGINT REFERENCES tb_org_unit(unit_id)` (in tb_ops_doc)
- line 443: `plan_id BIGINT REFERENCES tb_work_plan(plan_id)` (in tb_ops_doc)
- line 547: `parent_plan_id BIGINT REFERENCES tb_work_plan(plan_id)` (in tb_work_plan, self-FK)
- line 569: `parent_id BIGINT REFERENCES tb_org_unit(unit_id) ON DELETE RESTRICT` (in tb_org_unit, self-FK)
- line 634: `ALTER TABLE tb_document ADD CONSTRAINT fk_tb_document_org_unit ...` (별도 ALTER, 위치 무관)

1-2. **추가 forward-reference 부재 확인** (R-7 사전 스캔):
```bash
# tb_work_plan / tb_org_unit 외에 phase2.sql 안에서 자기 뒤 테이블을 참조하는 경우 점검
grep -nE "REFERENCES [a-z_]+" src/main/resources/db_init_phase2.sql | head -30
```
2026-05-11 점검 결과 (선행 spec §1 표 참조): 위 5건 외 추가 forward-reference 0건.

1-3. **이동 대상 블록 line 범위 기록**:
- 블록 A (tb_work_plan): line 534 (주석 `-- 작업/점검 계획`) ~ line 559 (마지막 인덱스). 약 26줄.
- 블록 B (스프린트 5 섹션 헤더 + tb_org_unit + 39 seed): line 561 (`-- ===`) ~ line 623 (마지막 INSERT). 약 63줄.

1-4. **V100 SHA256 baseline** (NFR-6 검증):
```bash
sha256sum swdept/sql/V100_work_plan_performance_tables.sql > /tmp/v100-sha256-before.txt
```

**진행 게이트**: 1-1~1-4 모두 기대값 일치. 미일치 시 codex 추가 자문.

---

### Step 2 — `db_init_phase2.sql` 블록 선이동 (FR-1, FR-2, FR-3)

본 스프린트 핵심. 단일 파일 수정. 4개 Edit 작업.

> **편집 위치 표기 규칙**: line 번호는 commit `69b6196` 의 phase2.sql 기준. anchor 텍스트가 우선이고 line 번호는 참고. 각 Edit 후 후속 line 번호는 shift 됨.

#### 2-1. 헤더 주석 보강 (FR-3-A)

**Anchor**: 파일 상단 헤더 영역의 마지막 닫힘 `-- ===` 줄 (현재 line 20) **직전**.

수정 직전 컨텍스트 (commit 69b6196 기준):
```sql
-- 감사 2026-04-18 P2 2-2 조치 (스프린트 2a) → 본 스프린트로 후속 완료.
-- phase2-V018-init-ordering (2026-05-11) — V018 의 UNIQUE 제약·INDEX 를 phase2 의 INSERT 앞에 선이동. V018 무수정. 선행: dbinitrunner-dollar-quote-aware (ba12fc6).
-- ============================================================  ← 이 줄 직전에 1줄 추가
```

추가 1줄:
```sql
-- phase2-tb_ops_doc-forward-ref (2026-05-11) — tb_ops_doc 의 forward-reference 해소를 위해 tb_work_plan + tb_org_unit 블록을 tb_ops_doc 직전으로 선이동. schema/seed 의미 변경 0.
```

#### 2-2. 두 블록을 `tb_ops_doc` 직전에 삽입 (FR-1, FR-2)

**Anchor (목적지)**: doc-split-ops 섹션 헤더 시작 (현재 line 419) **직전**. 즉 line 418 (빈 줄) 직후.

수정 직전 컨텍스트:
```sql
   ...   tb_document_signature 끝 ...

-- ============================================================        ← line 419 (이 줄 직전에 두 블록 삽입)
-- doc-split-ops (2026-04-29): 운영·유지보수 문서 신규 테이블 + 레거시 제거
-- 기획서: docs/product-specs/doc-split-ops.md (v3)
-- 개발계획: docs/exec-plans/doc-split-ops.md (v2)
-- ============================================================
```

삽입할 블록 (블록 A + 블록 B 통째 — Step 1-3 에서 기록한 line 범위 그대로 복사):

```sql

-- ============================================================
-- [phase2-tb_ops_doc-forward-ref 2026-05-11] tb_ops_doc 의 forward-reference 해소를 위해 본 두 블록 (tb_work_plan + tb_org_unit) 을 tb_ops_doc 직전으로 선이동.
-- 원본 위치: 본 파일 후반부. schema/seed 의미 변경 0.
-- ============================================================

-- 작업/점검 계획 (self-FK: parent_plan_id)
CREATE TABLE IF NOT EXISTS tb_work_plan (
    plan_id         BIGSERIAL PRIMARY KEY,
    infra_id        BIGINT REFERENCES tb_infra_master(infra_id),
    plan_type       VARCHAR(30) NOT NULL,
    process_step    VARCHAR(100),
    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    assignee_id     BIGINT REFERENCES users(user_id),
    start_date      DATE NOT NULL,
    end_date        DATE,
    location        VARCHAR(300),
    repeat_type     VARCHAR(20) NOT NULL,
    parent_plan_id  BIGINT REFERENCES tb_work_plan(plan_id),
    status          VARCHAR(20) NOT NULL,
    status_reason   VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_tb_work_plan_infra    ON tb_work_plan(infra_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_assignee ON tb_work_plan(assignee_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_parent   ON tb_work_plan(parent_plan_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_status   ON tb_work_plan(status);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_start    ON tb_work_plan(start_date);

-- ============================================================
-- 스프린트 5 (2026-04-19): 조직도 + 문서 선택 UI 통일 + 운영/테스트 구분
-- 기획서: docs/product-specs/doc-selector-org-env.md
-- ============================================================

-- 조직도 마스터 (self-FK 로 가변 계층)
CREATE TABLE IF NOT EXISTS tb_org_unit (
    unit_id       BIGSERIAL PRIMARY KEY,
    parent_id     BIGINT REFERENCES tb_org_unit(unit_id) ON DELETE RESTRICT,
    unit_type     VARCHAR(20) NOT NULL CHECK (unit_type IN ('DIVISION','DEPARTMENT','TEAM')),
    name          VARCHAR(100) NOT NULL,
    sort_order    INTEGER DEFAULT 0,
    use_yn        VARCHAR(1) DEFAULT 'Y',
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_org_unit_parent ON tb_org_unit(parent_id);
CREATE INDEX IF NOT EXISTS idx_org_unit_type   ON tb_org_unit(unit_type);
CREATE INDEX IF NOT EXISTS idx_org_unit_use    ON tb_org_unit(use_yn);

-- 조직도 초기 seed — 독립 INSERT (DbInitRunner 세미콜론 분리기 호환, 각 행 멱등)
-- (39 INSERTs — 본 스프린트 작업 시 원본 line 584~623 의 모든 INSERT 를 verbatim 복사. 본 개발계획서에서는 첫 1건과 마지막 1건만 예시로 표기, 실제 편집 시 39건 전체)
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', '경영관리본부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='경영관리본부' AND unit_type='DIVISION');
-- ... (37 INSERTs 생략)
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='도시계획사업부' AND unit_type='DEPARTMENT'), 'TEAM', '도시계획사업2팀', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='도시계획사업2팀' AND unit_type='TEAM');

```

> **구현 시 주의**: Edit 도구 사용 시 source 의 39 INSERT 를 verbatim 복사 (생략·요약 금지). 가장 안전한 방법은 (i) Step 2-3 에서 source 영역 삭제 직전에 `Read` 로 정확한 텍스트 확보, (ii) Step 2-2 의 new_string 에 그대로 paste.

#### 2-3. 원본 위치에서 두 블록 삭제

**Anchor (블록 A 삭제)**: `-- 작업/점검 계획 (self-FK: parent_plan_id)\nCREATE TABLE IF NOT EXISTS tb_work_plan (` 부터 `CREATE INDEX IF NOT EXISTS idx_tb_work_plan_start    ON tb_work_plan(start_date);\n` (마지막 빈 줄 포함) 까지 통째 삭제.

**Anchor (블록 B 삭제)**: `-- ============================================================\n-- 스프린트 5 (2026-04-19): 조직도 + 문서 선택 UI 통일 + 운영/테스트 구분\n` 부터 마지막 INSERT (`'도시계획사업2팀' AND unit_type='TEAM');\n`) 까지 통째 삭제.

> **편집 전략**: Step 2-2 의 삽입 후 line 번호가 shift 되므로, Step 2-3 의 anchor 는 텍스트 매칭. 두 anchor 모두 unique (다른 위치에 동일 텍스트 없음).

#### 2-4. compile 검증 (NFR-1)

```bash
./mvnw clean compile
```
- 기대: BUILD SUCCESS.

**진행 게이트**: 2-1~2-4 완료 + BUILD SUCCESS. 실패 시 `git checkout -- src/main/resources/db_init_phase2.sql` 후 재진입.

---

### Step 3 — ephemeral 클러스터 검증 (NFR-2 ~ NFR-7)

선행 sprint 의 ephemeral 클러스터 (`localhost:25880`, `C:\Users\PUJ\pg16-verify\data`) 재사용.

#### 3-0. 클러스터 기동 (이미 기동 중이면 skip)
```bash
"/c/Users/PUJ/PostgreSQL/16/bin/psql.exe" -h localhost -p 25880 -U postgres -c "SELECT 1;" 2>&1 || \
"/c/Users/PUJ/PostgreSQL/16/bin/pg_ctl.exe" -D "C:/Users/PUJ/pg16-verify/data" -l "C:/Users/PUJ/pg16-verify/logfile" start
```

#### 3-1. 테스트 DB 생성

```bash
export PGPASSWORD=ephemeral
PSQL='"C:/Users/PUJ/PostgreSQL/16/bin/psql.exe" -h localhost -p 25880 -U postgres -v ON_ERROR_STOP=1'
for db in sw_u1 sw_u2 sw_u2b; do
  $PSQL -c "DROP DATABASE IF EXISTS $db;"
  $PSQL -c "CREATE DATABASE $db;"
done
```

#### 3-2. T1 — phase2.sql 단독 멱등 3회 (NFR-3)

```bash
$PSQL -d sw_u1 -f src/main/resources/db_init_phase1.sql > /tmp/u1_phase1.log 2>&1
$PSQL -d sw_u1 -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/u1_sigungu.log 2>&1
for i in 1 2 3; do
  $PSQL -d sw_u1 -f src/main/resources/db_init_phase2.sql > /tmp/u1_phase2_run$i.log 2>&1
  echo "Run $i rc=$?"
done

# Pass 기준 (NFR-3): 본 스프린트 변경 surface (tb_work_plan, tb_org_unit, tb_ops_doc) ERROR 0
$PSQL -d sw_u1 -tAc "SELECT COUNT(*) FROM tb_work_plan;"
$PSQL -d sw_u1 -tAc "SELECT COUNT(*) FROM tb_org_unit;"
$PSQL -d sw_u1 -tAc "SELECT COUNT(*) FROM tb_ops_doc;"
grep -E "tb_work_plan|tb_org_unit|tb_ops_doc" /tmp/u1_phase2_run*.log | grep -i ERROR
```
- **Pass 기준**: 3회 모두 rc=0. 본 스프린트 변경 surface 관련 ERROR 0건. tb_org_unit count = 39 (1차 후), 2~3차 에서 변동 0.

#### 3-3. T2 — psql 경로 fresh-init **완전 통과** (NFR-2-a)

```bash
$PSQL -d sw_u2 -f src/main/resources/db_init_phase1.sql > /tmp/u2_phase1.log 2>&1
$PSQL -d sw_u2 -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/u2_sigungu.log 2>&1
$PSQL -d sw_u2 -f src/main/resources/db_init_phase2.sql > /tmp/u2_phase2.log 2>&1
echo "phase2 rc=$? (이번엔 0 기대 — 선행 + 본 스프린트 결합)"

FAILED_VS=()
for v in swdept/sql/V*.sql; do
  $PSQL -d sw_u2 -f "$v" >> /tmp/u2_v_all.log 2>&1
  if [ $? -ne 0 ]; then
    FAILED_VS+=("$v")
  fi
done
echo "V*.sql 실패 파일 수: ${#FAILED_VS[@]}"
echo "실패 파일: ${FAILED_VS[*]}"
grep -ciE "ERROR|FATAL" /tmp/u2_v_all.log
```
- **Pass 기준** (v1.1 정정): phase2.sql rc=0 (line 60/70/464 stop 회귀 0건 — 본 스프린트 핵심 목표). V*.sql 실패 파일 수 = 0 (또는 phase1 exec-plan 에서 본 ERROR allowlist 매칭 — V018/V024/V026 등 5건 known). loop 마지막 rc 만 보지 않고 **전체 파일별 rc 수집** 하는 구조 (codex 1차 권고).

#### 3-4. T2-b — DbInitRunner 경로 부팅 (NFR-2-b)

```bash
$PSQL -c "DROP DATABASE IF EXISTS sw_u2b;"
$PSQL -c "CREATE DATABASE sw_u2b;"
$PSQL -d sw_u2b -f src/main/resources/db_init_phase1.sql > /tmp/u2b_phase1.log 2>&1
$PSQL -d sw_u2b -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/u2b_sigungu.log 2>&1

# 운영DB 영향 0 — 본 스프린트 NFR-5 보장. 운영DB 부팅 검증은 Step 5-2 에서 별도.
# 여기서는 ephemeral sw_u2b 로 isolated 검증
DB_URL="jdbc:postgresql://localhost:25880/sw_u2b" DB_USERNAME=postgres DB_PASSWORD=ephemeral \
bash server-restart.sh > /tmp/u2b_boot.log 2>&1
sleep 20
grep -E "Started SwmanagerApplication|^.{20,30}ERROR|PSQLException|relation .* does not exist" /tmp/u2b_boot.log | head -10
```
- **Pass 기준**: `Started SwmanagerApplication` 출력. 본 스프린트 변경 surface 관련 ERROR 0. "relation does not exist" 0건 (forward-reference 해소 입증).

#### 3-5. T3 — `git diff` 기반 의미 불변 검증 (NFR-4, v1.2 전면 재작성)

**전제 (codex 2차 권고)**: 이전 v1.1 의 DB-level diff 방식은 base DB (commit `69b6196` 의 phase2.sql) 에 `ON_ERROR_STOP=0` 로 적용해도 **`tb_ops_doc` CREATE 자체가 forward-reference FK 로 실패** → tb_ops_doc 미생성 → DB 비교 무의미. v1.2 는 **`git diff` 텍스트 비교** 로 전환 — DB 간접 측정보다 더 직접적으로 "본 sprint 가 무엇을 바꿨는가" 를 보임.

##### 3-5-A. tb_work_plan + tb_org_unit 블록의 위치-만-변경 입증 (출력 기호 v1.3 정정)

본 sprint 의 패치는 두 블록을 **위치만 옮기고 내용은 1바이트도 안 건드림** 이어야 함.

```bash
# 본 sprint 변경의 git diff 추출
git diff 69b6196 HEAD -- src/main/resources/db_init_phase2.sql > /tmp/phase2_diff.patch

# 추가된 줄 (diff +) 과 삭제된 줄 (diff -) 을 분리 (hunk header / file header 제외)
grep -E "^\+[^+]" /tmp/phase2_diff.patch | sed 's/^+//' > /tmp/added.txt
grep -E "^-[^-]" /tmp/phase2_diff.patch | sed 's/^-//' > /tmp/deleted.txt

# 정렬 후 비교 — diff 출력 기호:
#   `<` 는 첫 번째 입력 (deleted) 에만 있는 줄 (= 삭제만 됐고 다시 추가 안 된 줄)
#   `>` 는 두 번째 입력 (added) 에만 있는 줄 (= 신규 추가 줄)
diff <(sort /tmp/deleted.txt) <(sort /tmp/added.txt) > /tmp/move_diff.txt
echo "=== T3-A diff 결과 ==="
cat /tmp/move_diff.txt
LT_CNT=$(grep -c "^<" /tmp/move_diff.txt)
GT_CNT=$(grep -c "^>" /tmp/move_diff.txt)
echo "T3-A: '<' (삭제 only) = $LT_CNT (0 이면 PASS)"
echo "T3-A: '>' (추가 only) = $GT_CNT (허용된 신규 주석만이어야 — 헤더 주석 1줄 + [선이동] 주석 블록 ≈ 5~7줄 기대)"
```

**T3-A Pass 기준**:
- `<` (삭제 only) **= 0건** — 삭제된 모든 텍스트가 새 위치에 그대로 추가됨 = 블록 위치만 변경
- `>` (추가 only) **= 허용된 신규 주석 줄만** (Step 2-1 의 헤더 1줄 + Step 2-2 의 [선이동] 주석 블록). 사람이 read 검토 후 acceptance.

##### 3-5-B. 다른 객체 정의 변경 0 입증 (hunk 단위, v1.3 강화)

본 sprint 의 hunk 갯수와 위치를 검사. 허용 hunk 4종:
1. **H1**: 파일 상단 헤더 영역 (line 19~20 부근) — Step 2-1 헤더 주석 1줄 추가
2. **H2**: doc-split-ops 섹션 헤더 직전 (line 419 직전) — Step 2-2 두 블록 + [선이동] 주석 신규 삽입
3. **H3**: 기존 tb_work_plan 위치 (구 line 534~559 부근) — Step 2-3 의 블록 A 삭제
4. **H4**: 기존 스프린트 5 + tb_org_unit + 39 INSERTs 위치 (구 line 561~623 부근) — Step 2-3 의 블록 B 삭제

```bash
# 모든 hunk header 추출
grep -E "^@@" /tmp/phase2_diff.patch > /tmp/hunks.txt
echo "=== T3-B hunk header 목록 ==="
cat /tmp/hunks.txt
HUNK_CNT=$(wc -l < /tmp/hunks.txt)
echo "T3-B: hunk 수 = $HUNK_CNT (허용 4종 = H1/H2/H3/H4 — git 의 hunk 병합으로 인접 hunk 통합 가능, 정확한 수는 read 검토)"

# 각 hunk header 의 line 번호 (구버전 -A,B 와 신버전 +C,D) 추출
# git diff hunk header 형식: @@ -OLD_START,OLD_COUNT +NEW_START,NEW_COUNT @@ optional context
# H1 기대: -19~20 부근 / H2 기대: -418~420 부근 (삽입 → OLD_COUNT 작거나 0)
# H3 기대: -534~559 부근 / H4 기대: -561~623 부근

# 사람이 직접 read 검토 항목 (acceptance):
echo ""
echo "=== T3-B 사용자 acceptance 체크리스트 ==="
echo "[ ] hunk 수가 4 (또는 인접 병합으로 더 적게) 임"
echo "[ ] H1 hunk: 파일 상단 영역 (-19~20 부근), 추가 1줄 (헤더 주석)"
echo "[ ] H2 hunk: -418~420 부근, 약 90줄 추가 (블록 A + 블록 B + [선이동] 주석)"
echo "[ ] H3 hunk: -534~559 부근, 약 26줄 삭제 (블록 A 원본)"
echo "[ ] H4 hunk: -561~623 부근, 약 63줄 삭제 (블록 B 원본 + 39 INSERTs)"
echo "[ ] 위 4개 외 hunk 0건"
```

**T3-B Pass 기준**:
- hunk header 4개 (또는 인접 병합으로 더 적은 수) — 허용 anchor 4종 외 hunk 0건
- 각 hunk 의 line 범위가 위 H1~H4 기대 영역에 매칭
- read 검토로 acceptance (자동 측정만으로는 정의 변경 의도 vs 실수 구분이 불가능 — 사람 read 가 NFR-4 입증의 마지막 단계)

> **참고**: T3-B 는 codex 3차 권고에 따라 단순 grep 필터 (너무 넓음) 에서 hunk 단위 검증으로 강화. 자동 검증은 hunk 수와 위치까지, 의미적 정확성은 사람 read 검토로 닫음.

#### 3-6. T4 — V100 SHA256 무변경 (NFR-6)

```bash
sha256sum swdept/sql/V100_work_plan_performance_tables.sql > /tmp/v100-sha256-after.txt
diff /tmp/v100-sha256-before.txt /tmp/v100-sha256-after.txt
echo "diff rc=$? (0 = 무변경)"
```

**진행 게이트**: T1~T4 + T2-b 모두 PASS. 실패 시 Step 4 미진입 + Edit 되돌림.

---

### Step 4 — 문서 갱신 (FR-4)

#### 4-1. `docs/PLANS.md` §2-b 갱신 (FR-4-A)

후속 백로그 `phase2-tb_ops_doc-forward-ref` 항목을 **완료 스프린트** 표기로 갱신:

```markdown
- `phase2-tb_ops_doc-forward-ref` — **완료 (2026-05-11, commit `<TBD>`)**:
  - 산출물: `db_init_phase2.sql` (tb_work_plan + tb_org_unit 블록을 tb_ops_doc 직전으로 선이동)
  - 검증 (ephemeral 25880): T1~T4 + T2-b PASS. fresh-init phase2.sql rc=0 완전 통과.
  - V100 무수정 (Flyway checksum 보존). 운영DB 영향 0.
  - **선행 sprint phase2-V018-init-ordering Discovery closure 완료** — fresh-init full phase2 통과 보장.
```

#### 4-2. `docs/references/setup-guide.md` §2-2 갱신 (FR-4-B)

선행 sprint 가 추가한 보강 줄을 다음으로 갱신:

```markdown
- **2026-05-11 보강** (`phase2-V018-init-ordering` + `phase2-tb_ops_doc-forward-ref` 적용 후): 본 절차의 phase2.sql 은 **완전 무중단 통과 보장**. line 60/70 (V018 UNIQUE 의존) + line 464 부근 (tb_ops_doc forward-reference) 모두 회귀 차단. fresh-init `psql -f` 와 DbInitRunner 부팅 양 경로 정상.
```

#### 4-3. (선택) `docs/product-specs/phase2-V018-init-ordering.md` §10 Discovery 라벨 갱신 (FR-4-C)

§10 후속 sprint 줄에 다음 1줄 추가:

```markdown
> **2026-05-11 closure**: 후속 sprint `phase2-tb_ops_doc-forward-ref` (commit `<TBD>`) 로 해결. fresh-init full phase2 통과 보장.
```

**진행 게이트**: 4-1, 4-2 완료 (4-3 선택). 문서 변경만 — compile 영향 0.

---

### Step 5 — 빌드 / 운영 부팅 / 회귀 스모크 (NFR-1, NFR-7)

5-1. **Maven compile**:
```bash
./mvnw clean compile
```
- 기대: BUILD SUCCESS.

5-2. **운영 환경 server-restart** (NFR-2-b 의 (b-ii) 검증, v1.1 강화):

```bash
bash server-restart.sh > /tmp/final_boot.log 2>&1
# 부팅 완료 대기 — until-loop 로 Started 또는 실패 마커 등장까지 기다림
until grep -qE "Started SwmanagerApplication|APPLICATION FAILED TO START|Caused by:|Exception in thread" /tmp/final_boot.log 2>/dev/null; do
  sleep 2
done

# v1.1 강화: 3가지 별도 fail/pass 판정 (codex 1차 권고)
echo "=== (1) Started 1건 (PASS 기대) ==="
STARTED_CNT=$(grep -c "Started SwmanagerApplication" server.log)
echo "Started count: $STARTED_CNT (1 이면 PASS)"

echo "=== (2) DB 초기화 완료 1건 (PASS 기대) ==="
DBINIT_CNT=$(grep -c "DB 초기화 완료" server.log)
echo "DB init count: $DBINIT_CNT (1 이면 PASS)"
grep "DB 초기화 완료" server.log

echo "=== (3) ERROR/PSQLException/relation does not exist 0건 (PASS 기대, 전체 로그 grep) ==="
ERR_CNT=$(grep -ciE "PSQLException|relation .* does not exist|syntax error at or near" server.log)
echo "본 스프린트 surface 관련 ERROR count: $ERR_CNT (0 이면 PASS)"
# 참고: ROOT 로거의 일반 ERROR 는 logback FILE_ERROR appender 설정 메시지가 1건씩 들어가므로 grep "^.*ERROR" 가 아닌 PSQLException/relation 등 정확한 매칭으로 한정
```

- **Pass 기준 (3가지 모두 충족)**:
  - (1) Started count = 1
  - (2) DB init count = 1, 메시지의 N (실행 SQL 개수) 은 선행 sprint 와 동일 (127 부근 — 본 스프린트는 stmt 수 변동 0, 위치만 이동)
  - (3) PSQLException + relation does not exist + syntax error 합계 = 0

- **참고**: server.log 는 server-restart.sh 가 LOG_FILE 로 사용. final_boot.log 는 restart 스크립트의 stdout (script 자체의 짧은 메시지) 만 담음. 실제 부팅 로그는 server.log.

5-3. **회귀 스모크** (수동):
- 점검내역서 화면 → 목록 1건 진입 → 정상
- 사업관리 화면 → 정상
- 견적서 화면 → 정상
- **조직도 화면** (tb_org_unit 관련) → 정상 (본 스프린트가 직접 영향)

**진행 게이트**: 5-1 BUILD SUCCESS + 5-2 ERROR 0 + 5-3 4개 화면 정상. → "작업완료" 발화 가능.

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | phase2.sql 단독 멱등 3회 | Step 3-2 | 3회 rc=0, 본 스프린트 surface ERROR 0, tb_org_unit count=39 |
| T2 | psql fresh-init **완전 통과** | Step 3-3 | phase2.sql rc=0 (line 60/70/464 stop 회귀 0건) |
| T2-b | DbInitRunner 경로 부팅 | Step 3-4 | Started + ERROR 0 + "relation does not exist" 0건 |
| T3 | git diff 기반 의미 불변 | Step 3-5 | T3-A: `<` (deleted only) **= 0건** + `>` (added only) = 허용된 신규 주석만 (Step 2-1 헤더 1줄 + [선이동] 주석 블록). T3-B: hunk header 4종 H1~H4 매칭 + 허용 anchor 외 hunk **0건** + 사용자 read acceptance |
| T4 | V100 SHA256 무변경 | Step 3-6 | diff 0줄 |
| T5 | Maven compile | Step 5-1 | BUILD SUCCESS |
| T6 | 운영 환경 부팅 | Step 5-2 | Started + ERROR 0 + DB 초기화 완료 N개 |
| T7 | 회귀 스모크 (4 화면) | Step 5-3 | 모두 정상 |

총 8 케이스. 1건이라도 FAIL 시 "작업완료" 발화 금지.

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 2 compile 실패 | `git checkout -- src/main/resources/db_init_phase2.sql` 후 Edit 재진입 |
| Step 2 의 Edit 중 anchor 매칭 실패 | 동일 (단일 파일 revert) + Read 로 anchor 재확인 |
| Step 3 T1~T4 1건이라도 FAIL | 동일 + 로그 분석 후 Step 2 재진입 |
| **Step 4 진입 전 단계 실패** | `git checkout -- src/main/resources/db_init_phase2.sql` (단일 파일 revert) |
| **Step 4 이후 단계 실패 (Step 5-2/5-3, commit 직전)** | `git checkout -- src/main/resources/db_init_phase2.sql docs/PLANS.md docs/references/setup-guide.md` (+ 4-3 적용했으면 phase2-V018 spec.md). 문서 파일도 함께 revert |
| **commit 후 push 전 회귀** | `git reset --hard HEAD~1` (해당 commit 가 HEAD 일 때만) |
| **commit + push 후 회귀** | `git revert <commit>` → push → `bash server-restart.sh`. SQL+문서 통합 1 commit 이라 단일 revert 안전 복원 |
| 운영DB 손상 가능성 | **0** (NFR-5) — 본 스프린트는 운영DB 에 SQL 직접 실행 없음. DbInitRunner 부팅도 멱등 |

---

## 4. 리스크·완화 재확인 (기획서 §7 R-1~7 그대로)

| ID | 리스크 | 본 개발계획 에서 완화 |
|----|--------|--------------------|
| R-1 | 선이동 시 다른 의존 깨짐 | Step 1-2 grep 검증 (forward-reference 5건 외 추가 0건 확인) |
| R-2 | 운영DB 충돌 | `CREATE TABLE IF NOT EXISTS`. Step 5-2 운영 부팅 검증 |
| R-3 | seed 39건 self-reference | Step 2-2 verbatim 복사 (생략 금지). Step 3-2 count 검증 |
| R-4 | self-FK 깨짐 | self-FK 자기 정의 → 위치 무관. Step 3-3/3-4 부팅 정상 입증 |
| R-5 | V100 와 중복 정의 충돌 | `IF NOT EXISTS` 멱등 보완. Step 3-3 V*.sql 전체 통과 검증 |
| R-6 | line shift 로 다른 도구 깨짐 | 라이브 도구 0. anchor 기반 편집으로 본 스프린트 본문 line 참조 안정 |
| R-7 | 미점검 forward-reference | Step 1-2 사전 grep 으로 본 스프린트 진입 전 일제 확인 (2026-05-11 점검 완료) |

---

## 5. 산출물 / 커밋 단위

**1 commit 통합 (확정 정책, v1.1)** — 선행 sprint 동일 정책. codex 1차 권고에 따라 산출물 포함 범위 확정:

**Commit (필수, 1건)** — 코드 + 모든 문서 (**기획서·개발계획서 포함**):
- `src/main/resources/db_init_phase2.sql` (Step 2)
- `docs/PLANS.md` (Step 4-1)
- `docs/references/setup-guide.md` (Step 4-2)
- `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` v4 (기획서)
- `docs/exec-plans/phase2-tb_ops_doc-forward-ref.md` v1.4 (본 개발계획서)
- `docs/product-specs/phase2-V018-init-ordering.md` (Step 4-3 — 선행 sprint Discovery closure 라벨, 선택 권장)

> 별도 commit 분리 옵션은 v1.1 에서 제거. 단일 산출물 단위 push 로 git history 와 revert 명확화 (선행 sprint 와 동일 패턴).

커밋 메시지 (예시):
```
feat(phase2-tb_ops_doc-forward-ref): tb_work_plan + tb_org_unit 블록을 tb_ops_doc 앞으로 선이동

선행 sprint phase2-V018-init-ordering 의 Discovery 로 표면화한 forward-reference
2건 해소: tb_ops_doc.org_unit_id (REFERENCES tb_org_unit) 와 tb_ops_doc.plan_id
(REFERENCES tb_work_plan) 가 자기보다 뒤에 정의된 테이블을 참조하던 구조를
선이동으로 정리. schema/seed 의미 변경 0.

V100 무수정 (SHA256 변동 0). 운영DB 영향 0 (CREATE TABLE IF NOT EXISTS 멱등성).

검증 (ephemeral 25880):
- T1 phase2 멱등 3회 — 변경 surface ERROR 0, tb_org_unit count=39
- T2 psql fresh-init 완전 통과 — line 60/70/464 stop 회귀 0건
- T2-b DbInitRunner 부팅 — Started, ERROR 0, "relation does not exist" 0건
- T3 git diff 의미 불변: T3-A `<` 0건 + `>` 허용 주석만, T3-B hunk 4종 외 0건
- T4 V100 SHA256 무변경

선행 sprint phase2-V018-init-ordering §10 Discovery closure 완료 — fresh-init
full phase2 통과 보장.

근거:
- 기획서: docs/product-specs/phase2-tb_ops_doc-forward-ref.md v4 (codex 5차 ⭕ 2026-05-11)
- 개발계획: docs/exec-plans/phase2-tb_ops_doc-forward-ref.md v1.4
- 선행: phase2-V018-init-ordering (69b6196, EC-1~3 충족)

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
```

> master 직접 push (single-shot, harness-hardening-v1 confirm 게이트 적용).

---

## 6. 승인 요청

본 개발계획서 v1.4 에 대한 codex 검토 + 사용자 최종승인 요청.

### 검토 포인트
- Step 2-2 의 두 블록 verbatim 복사 정책 (39 INSERTs 생략 금지) 명확성
- Step 2-3 의 anchor 텍스트 매칭이 unique 인지 (다른 위치 동일 텍스트 부재)
- Step 3-5 의 schema diff 측정 방법 (phase2 stop 위치 차이로 인한 diff 처리) 의 타당성
- Step 5-2 의 운영 환경 부팅 시 본 스프린트 변경이 실제 동작하는지 ERROR 검출 충분성
- 1 commit 통합 정책

### 다음 절차 (v1.4 갱신)

현 단계: codex 1차 ⚠ → 2차 ⚠ → 3차 ⚠ → 4차 ⚠ → **v1.4 정정 + 5차 검토 대기**.

1. **codex 5차 검토** (v1.4 기준)
2. ⭕ 시 사용자 최종승인 → Step 1~5 순차 실행
3. T1~T7 모두 PASS → "작업완료" → 자동 commit+push (1 commit 통합)
