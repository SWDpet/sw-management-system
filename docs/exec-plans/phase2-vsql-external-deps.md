---
tags: [dev-plan, sprint, schema, init-ordering, vsql-external]
sprint: "phase2-vsql-external-deps"
status: paused-v1.2-blocked-by-vsql-defects
created: "2026-05-11"
revised: "2026-05-11"
approved: "2026-05-11"
paused: "2026-05-11"
---

# [개발계획서] phase2.sql 의 외부 V*.sql 의존 INSERT 제거 — 스프린트 phase2-vsql-external-deps

- **작성팀**: 개발팀
- **작성일**: 2026-05-11
- **근거 기획서**: `docs/product-specs/phase2-vsql-external-deps.md` v2 (codex 1차 ⚠ → v2 6건 반영 → codex 2차 ⭕ + 사용자 최종승인 2026-05-11)
- **선행 스프린트**: `phase2-tb_ops_doc-forward-ref` (commit `856b460`, 2026-05-11). EC-1~5 모두 충족 (기획서 §1.5 참조).
- **상태**: ⏸ **PAUSED v1.2** (2026-05-11) — codex 1차 ⚠ 4건 → v1.1 → 2차 ⚠ 1건 → v1.2 → 3차 ⭕ → 사용자 최종승인 → 구현 Step 1~2 완료 → **Step 3 T2 측정 중 V*.sql 9건 본 sprint 외 결함 표면화 → 사용자 결정 (option 4) 으로 phase2.sql revert + sprint 일시 중단**. 본 개발계획서는 base 자료로 보존. 자세한 PAUSE 사유 + 재진입 조건 + V*.sql 9건 분류는 기획서 §10 참조.

> **⏸ PAUSE 알림**: 본 개발계획서의 Step 1~2 는 검증 완료 (BASELINE_N=127 측정 + 3 INSERT 삭제 + compile PASS). Step 3 의 T1 phase2 멱등 + T2 의 phase2.sql rc=0 (본 sprint 핵심) 은 ✅ 입증. 단 T2 의 V*.sql 적용 단계에서 9건 결함 (의도된 abort 2 + `:run_id` 변수 치환 3 + 외부 정의 의존 4) 표면화. 본 sprint NFR-2-a 의 "V*.sql 전체 실패 = 0" 게이트 미충족으로 사용자 중단 결정. 재진입 시 Step 1-5 BASELINE_N=127 값 재사용 가능 (운영 환경 변동 없으면).
- **개정 이력**:
  - v1 (2026-05-11): 초안. 기획서 v2 와 codex 2차 비차단 권고 (NFR-7 → R-6 주 검출 / NFR-7 보조 smoke 표현 정밀화) 반영.
  - v1.1 (2026-05-11): codex 1차 ⚠ 4건 반영. (1) T2 게이트 엄격화 — phase1/sigungu/phase2 별도 rc 확인 + V*.sql `known allowlist` 문구 제거 (구체 사유 명시). (2) T3 baseline 방식을 `git stash` → `git show 856b460:db_init_phase2.sql` 로 변경 — 작업트리 무영향. (3) §3 롤백 표 NFR-5 문구를 기획서 v2 와 일치하게 정정 ("운영DB 에 SQL 직접 실행 없음" → "삭제된 3개 INSERT stmt 미실행 + schema/seed row 변동 0"). (4) §2 T# 표의 T7/T8 측정값 구체 명시 — T7 DB init N (baseline 기록 후 patched 와 비교, ±3 범위), T8 화면별 관찰 기준 (카테고리 3 / 유형 10 / 상태 7).
  - v1.2 (2026-05-11): codex 2차 ⚠ 1건 반영. **T7 baseline N 절차 실행 가능화** — Step 1 에 신규 sub-step `1-5 baseline N 사전 측정` 추가. 본 sprint Edit 시작 직전 (작업트리가 commit 856b460 결과물과 동일한 상태) `bash server-restart.sh` 1회 → server.log 의 `DB 초기화 완료 N개` 값 추출 → `/tmp/baseline_N.txt` 기록. Step 5-2 에서 patched N 측정 후 `patched - baseline = -3` 검증. 기존 §5-3 후의 baseline 절차 블록 제거 (Step 1-5 로 이동·명확화).

---

## 0. 진입 조건 재확인 (Entry Criteria)

| EC | 충족 여부 | 검증 |
|----|----------|------|
| EC-1 commit 머지 | ✅ | `git log --oneline master \| head -1` → `856b460` 직후 master HEAD 인지 확인 |
| EC-2 base 일치 | ✅ | 본 개발계획 작성 시점 (2026-05-11) 의 phase2.sql 가 commit `856b460` 의 결과물 (선행 sprint tb_work_plan + tb_org_unit 블록 선이동 포함) |
| EC-3 시드 1:1 동일 | ✅ | 기획서 §1 표 (codex 2차 검토 시 grep -C 4 결과 직접 확인) |
| EC-4 ON CONFLICT 멱등 | ✅ | V024:28 / V026:41,54 모두 ON CONFLICT (PK columns) DO NOTHING |
| EC-5 기획서 v2 codex 2차 ⭕ + 사용자 승인 | ✅ | 본 기획서 §9 체크리스트 |

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔 + base 기록

#### 1-1. 외부 V*.sql 의존 INSERT 위치 재확인 (FR-1, FR-2 대상 식별)

```bash
grep -nE "^INSERT INTO (qt_category_mst|work_plan_type_mst|work_plan_status_mst)" \
  src/main/resources/db_init_phase2.sql
```

기대 (commit `856b460` 직후 base, 라인 참조용):
- 약 line 682: `INSERT INTO qt_category_mst (...)`
- 약 line 693: `INSERT INTO work_plan_type_mst (...)`
- 약 line 706: `INSERT INTO work_plan_status_mst (...)`

> **표현 원칙 (기획서 v2)**: 라인 번호는 참조용. 실제 편집 anchor 는 SQL block 텍스트.

#### 1-2. 잔존 외부 V*.sql 의존 부재 확인 (R-4 사전 스캔)

```bash
# phase2.sql 안의 모든 INSERT INTO + DELETE FROM 대상 테이블 추출
grep -nE "^(INSERT INTO|DELETE FROM)" src/main/resources/db_init_phase2.sql \
  | awk '{print $1, $3}' | sort -u
```

각 대상 테이블이 phase2.sql 안에서 **CREATE TABLE 되는지** 확인:
```bash
# 각 테이블이 phase2.sql 안에서 자체 정의되는지 점검
for t in $(grep -nE "^(INSERT INTO|DELETE FROM)" src/main/resources/db_init_phase2.sql | awk '{print $3}' | sed 's/[(;]//g' | sort -u); do
  hits=$(grep -c "CREATE TABLE.*$t" src/main/resources/db_init_phase2.sql)
  vsql_hits=$(grep -lE "CREATE TABLE.*$t" swdept/sql/V*.sql 2>/dev/null | head -1)
  echo "$t: phase2 CREATE=$hits, V*.sql=$vsql_hits"
done
```

기대 (2026-05-11 시점):
- 본 sprint 대상 3건 (`qt_category_mst` / `work_plan_type_mst` / `work_plan_status_mst`) 만 phase2 CREATE=0 + V*.sql 에서 정의됨 → 본 sprint 로 처리
- 나머지는 phase2 CREATE>=1 (자체 정의) → forward-reference 0

> **결과**: 추가 외부 의존 0건이면 phase2.sql 은 본 sprint 후 **완전 무중단** 가능. 발견 시 신규 후속 sprint 분리.

#### 1-3. V024 / V026 SHA256 baseline (NFR-6 검증용)

```bash
sha256sum swdept/sql/V024_qt_category_master.sql swdept/sql/V026_work_plan_master.sql > /tmp/v_sha256_before.txt
cat /tmp/v_sha256_before.txt
```

#### 1-4. base seed 의미 baseline (NFR-4 검증용 — codex v2 권고 6 SQL 본문 고정)

base 클러스터 (선행 sprint 의 sw_u2 또는 fresh) 에 phase1+sigungu+phase2+V*.sql 적용 후 다음 MD5 계산:

```sql
-- NFR-4 검증 SQL (개발계획 v1 에서 고정 — 본문은 Step 3-5 T3 에서 사용)
SELECT 'qt_category_mst' AS tbl,
       COUNT(*) AS cnt,
       md5(string_agg(category_code || '|' || category_label || '|' || display_order, ',' ORDER BY category_code)) AS sig
FROM qt_category_mst;

SELECT 'work_plan_type_mst' AS tbl,
       COUNT(*) AS cnt,
       md5(string_agg(type_code || '|' || type_label || '|' || COALESCE(color,'') || '|' || display_order, ',' ORDER BY type_code)) AS sig
FROM work_plan_type_mst;

SELECT 'work_plan_status_mst' AS tbl,
       COUNT(*) AS cnt,
       md5(string_agg(status_code || '|' || status_label || '|' || display_order, ',' ORDER BY status_code)) AS sig
FROM work_plan_status_mst;
```

**baseline 값 기록** (Step 3-5 T3 에서 patched 클러스터와 동일성 비교).

#### 1-5. baseline DB init N 사전 측정 (T7 baseline, v1.2 신규)

**전제**: 본 sub-step 은 Step 2 의 Edit 시작 **직전** 실행. 이 시점에 작업트리는 commit `856b460` 결과물과 동일 (외부 V*.sql 의존 INSERT 3건이 phase2.sql 안에 살아있는 상태).

```bash
# (1) 작업트리 동등성 확인 — 본 sprint Edit 가 아직 적용되지 않음을 입증
git diff 856b460 HEAD -- src/main/resources/db_init_phase2.sql | wc -l
# 기대: 0 (변경 없음)

# (2) 운영 환경 DbInitRunner 1회 부팅 → server.log 의 "DB 초기화 완료 N개" 추출
bash server-restart.sh > /tmp/baseline_boot.log 2>&1
until grep -qE "Started SwManagerApplication|APPLICATION FAILED TO START" server.log 2>/dev/null; do
  sleep 2
done
BASELINE_N=$(grep -oE "DB 초기화 완료: [0-9]+개" server.log | tail -1 | grep -oE "[0-9]+")
echo "BASELINE_N=$BASELINE_N" | tee /tmp/baseline_N.txt
```

- **Pass 기준**:
  - (1) git diff = 0 줄 (작업트리가 commit `856b460` 과 동일)
  - (2) server.log 에서 `DB 초기화 완료: <N>개` 라인 존재. `BASELINE_N` 값이 정수로 추출됨. `/tmp/baseline_N.txt` 에 기록 완료.
- **이 값의 사용처**: T7 (Step 5-2) 에서 patched N 측정 후 `patched_N - BASELINE_N == -3` 검증. (본 sprint 가 INSERT 3 stmt 삭제 → SQL splitter 가 분리하는 stmt 수가 정확히 3 감소 기대.)

**진행 게이트**: 1-1~1-5 모두 기대값 일치. 미일치 시 codex 추가 자문.

---

### Step 2 — `db_init_phase2.sql` INSERT 3건 삭제 (FR-1, FR-2, FR-3)

본 스프린트 핵심. 단일 파일 수정. 4개 Edit 작업.

> **편집 위치 표기 규칙**: 라인 번호는 commit `856b460` 의 phase2.sql 기준. anchor 텍스트가 우선이고 라인 번호는 참고. 각 Edit 후 후속 라인 번호는 shift 됨.

#### 2-1. 헤더 주석 보강 (FR-3-A)

**Anchor**: 파일 상단 헤더 영역의 마지막 닫힘 `-- ===` 줄 **직전**.

수정 직전 컨텍스트 (commit 856b460 기준):
```sql
-- phase2-V018-init-ordering (2026-05-11) — V018 의 UNIQUE 제약·INDEX 를 phase2 의 INSERT 앞에 선이동.
-- phase2-tb_ops_doc-forward-ref (2026-05-11) — tb_ops_doc 의 forward-reference 해소를 위해 ...
-- ============================================================       ← 이 줄 직전에 1줄 추가
```

추가 1줄:
```sql
-- phase2-vsql-external-deps (2026-05-11) — 외부 V*.sql 정의 테이블 (qt_category_mst / work_plan_type_mst / work_plan_status_mst) 시드 INSERT 3건 제거. V024/V026 가 시드 SSoT 책임. V*.sql 무수정.
```

#### 2-2. `qt_category_mst` INSERT block + 위 주석 삭제 (FR-1-A, FR-1-B)

**Anchor (삭제 대상)**: 다음 블록 통째 삭제 — 약 9줄 (빈 줄 1 + 섹션 헤더 4 + INSERT 5).

```sql

-- ============================================================
-- S8 qt-quotation-domain-normalize (2026-04-22):
--   qt_category_mst 초기 시드 3행 (유지보수/용역/제품)
-- ============================================================
INSERT INTO qt_category_mst (category_code, category_label, display_order) VALUES
  ('유지보수', '유지보수', 1),
  ('용역',     '용역',     2),
  ('제품',     '제품',     3)
ON CONFLICT (category_code) DO NOTHING;
```

**대치할 텍스트** (FR-3-B 잔존 주석):
```sql

-- [phase2-vsql-external-deps 2026-05-11] qt_category_mst 시드는 V024_qt_category_master.sql:28 책임 — 본 위치 INSERT 제거됨.
```

> **anchor unique 검증**: `INSERT INTO qt_category_mst` 가 phase2.sql 안에 1회만 등장해야 함. 사전 grep 으로 확인:
> ```bash
> grep -c "INSERT INTO qt_category_mst" src/main/resources/db_init_phase2.sql
> # 기대: 1
> ```

#### 2-3. `work_plan_type_mst` + `work_plan_status_mst` INSERT block + 위 주석 삭제 (FR-2-A, FR-2-B, FR-2-C)

**Anchor (삭제 대상)**: 다음 블록 통째 삭제 — 약 26줄 (빈 줄 1 + 섹션 헤더 4 + type INSERT 12 + 빈 줄 1 + status INSERT 9 부근). 정확한 줄 수는 base file 기준.

```sql

-- ============================================================
-- S16 tb-work-plan-decision (2026-04-22):
--   work_plan_type_mst 10행 + work_plan_status_mst 7행 초기 시드
-- ============================================================
INSERT INTO work_plan_type_mst (type_code, type_label, color, display_order) VALUES
  ('CONTRACT',   '계약',     '#1565c0', 1),
  ('INSTALL',    '설치',     '#2e7d32', 2),
  ('PATCH',      '패치',     '#00897b', 3),
  ('INSPECT',    '점검',     '#ff9800', 4),
  ...
ON CONFLICT (type_code) DO NOTHING;

INSERT INTO work_plan_status_mst (status_code, status_label, display_order) VALUES
  ('PLANNED',     '예정',     1),
  ...
ON CONFLICT (status_code) DO NOTHING;
```

> **구현 시 주의**: Edit 도구 사용 시 source 의 두 INSERT 본문을 verbatim 으로 anchor 에 포함 (생략·요약 금지). 가장 안전한 방법은 (i) Read 로 base 영역 정확한 텍스트 확보, (ii) Edit 의 old_string 에 그대로 paste, (iii) new_string 에 잔존 주석.

**대치할 텍스트** (FR-3-B 잔존 주석):
```sql

-- [phase2-vsql-external-deps 2026-05-11] work_plan_type_mst (10행) + work_plan_status_mst (7행) 시드는 V026_work_plan_master.sql:41,54 책임 — 본 위치 INSERT 제거됨.
```

> **anchor unique 검증**:
> ```bash
> grep -c "INSERT INTO work_plan_type_mst" src/main/resources/db_init_phase2.sql
> grep -c "INSERT INTO work_plan_status_mst" src/main/resources/db_init_phase2.sql
> # 기대: 둘 다 1
> ```

#### 2-4. compile 검증 (NFR-1)

```bash
./mvnw clean compile
```
- 기대: BUILD SUCCESS.

**진행 게이트**: 2-1~2-4 완료 + BUILD SUCCESS. 실패 시 `git checkout -- src/main/resources/db_init_phase2.sql` 후 재진입.

---

### Step 3 — ephemeral 클러스터 검증 (NFR-2 ~ NFR-7)

선행 sprint 의 ephemeral 클러스터 (`localhost:25880`, `C:\Users\PUJ\pg16-verify\data`) 재사용.

#### 3-0. 클러스터 기동 확인

```bash
"/c/Users/PUJ/PostgreSQL/16/bin/psql.exe" -h localhost -p 25880 -U postgres -c "SELECT 1;" 2>&1 \
  || "/c/Users/PUJ/PostgreSQL/16/bin/pg_ctl.exe" -D "C:/Users/PUJ/pg16-verify/data" -l "C:/Users/PUJ/pg16-verify/logfile" start
```

#### 3-1. 테스트 DB 생성

```bash
export PGPASSWORD=ephemeral
PSQL='"C:/Users/PUJ/PostgreSQL/16/bin/psql.exe" -h localhost -p 25880 -U postgres -v ON_ERROR_STOP=1'
for db in sw_u1 sw_u2 sw_u2b sw_u2_baseline; do
  $PSQL -c "DROP DATABASE IF EXISTS $db;"
  $PSQL -c "CREATE DATABASE $db;"
done
```

> **NFR-2 (a) 주판정 도구**: `psql -v ON_ERROR_STOP=1` (codex v2 권고 1)

#### 3-2. T1 — phase2.sql 단독 멱등 3회 (NFR-3)

```bash
$PSQL -d sw_u1 -f src/main/resources/db_init_phase1.sql > /tmp/u1_phase1.log 2>&1
$PSQL -d sw_u1 -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/u1_sigungu.log 2>&1
# V*.sql 까지 1회 적용 (V024/V026 가 시드 INSERT 적용) — 본 sprint 후 phase2 가 시드 책임 0 이므로 V024/V026 가 시드 SSoT
for v in swdept/sql/V*.sql; do
  $PSQL -d sw_u1 -f "$v" >> /tmp/u1_v_all.log 2>&1
done

# phase2 단독 3회 (멱등)
for i in 1 2 3; do
  $PSQL -d sw_u1 -f src/main/resources/db_init_phase2.sql > /tmp/u1_phase2_run$i.log 2>&1
  echo "Run $i rc=$?"
done

# Pass 검증
$PSQL -d sw_u1 -tAc "SELECT COUNT(*) FROM qt_category_mst;"
$PSQL -d sw_u1 -tAc "SELECT COUNT(*) FROM work_plan_type_mst;"
$PSQL -d sw_u1 -tAc "SELECT COUNT(*) FROM work_plan_status_mst;"
grep -ciE "ERROR|FATAL" /tmp/u1_phase2_run*.log
```

- **Pass 기준 (NFR-3)**: 3회 모두 rc=0. ERROR 0. count = 3 / 10 / 7 (V024/V026 가 적용됨).

#### 3-3. T2 — psql fresh-init **완전 통과** (NFR-2-a, codex v1.1 권고 1 강화)

```bash
# v1.1: 각 단계별 rc 별도 확인. 본 sprint 의 NFR-2 는 allowlist 없는 전체 rc=0
$PSQL -d sw_u2 -f src/main/resources/db_init_phase1.sql > /tmp/u2_phase1.log 2>&1
PHASE1_RC=$?
echo "phase1 rc=$PHASE1_RC (0 기대 — 회귀 없음)"
[ $PHASE1_RC -ne 0 ] && { echo "FAIL phase1"; tail -30 /tmp/u2_phase1.log; exit 1; }

$PSQL -d sw_u2 -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/u2_sigungu.log 2>&1
SIGUNGU_RC=$?
echo "sigungu rc=$SIGUNGU_RC (0 기대)"
[ $SIGUNGU_RC -ne 0 ] && { echo "FAIL sigungu"; tail -30 /tmp/u2_sigungu.log; exit 1; }

$PSQL -d sw_u2 -f src/main/resources/db_init_phase2.sql > /tmp/u2_phase2.log 2>&1
PHASE2_RC=$?
echo "phase2 rc=$PHASE2_RC (0 기대 — 본 sprint 핵심: 외부 V*.sql 의존 INSERT 제거됨)"
[ $PHASE2_RC -ne 0 ] && { echo "FAIL phase2"; tail -30 /tmp/u2_phase2.log; exit 1; }

FAILED_VS=()
for v in swdept/sql/V*.sql; do
  $PSQL -d sw_u2 -f "$v" >> /tmp/u2_v_all.log 2>&1
  if [ $? -ne 0 ]; then
    FAILED_VS+=("$v")
  fi
done
echo "V*.sql 실패 파일 수: ${#FAILED_VS[@]}"
echo "실패 파일: ${FAILED_VS[*]}"
[ ${#FAILED_VS[@]} -ne 0 ] && { echo "FAIL V*.sql — 위 실패 파일 별도 분석"; exit 1; }
```

- **Pass 기준 (NFR-2-a)** — v1.1 정정:
  - phase1 rc=0 (회귀 점검)
  - sigungu rc=0 (회귀 점검)
  - **phase2.sql rc=0** (line 686/697/710 stop 회귀 0건 — 본 스프린트 핵심 목표)
  - **V*.sql 전체 실패 파일 수 = 0** (allowlist 없음 — 기획서 NFR-2 의 "전체 rc=0" 충족). 실패 발견 시 본 sprint 변경 무관하더라도 분석 후 진행 결정.
  - 본 sprint 의 핵심 판정. 위 4개 중 1개라도 실패 시 즉시 Step 4 미진입.

#### 3-4. T2-b — DbInitRunner 경로 부팅 (NFR-2-b, 보조 smoke)

```bash
$PSQL -c "DROP DATABASE IF EXISTS sw_u2b;"
$PSQL -c "CREATE DATABASE sw_u2b;"
$PSQL -d sw_u2b -f src/main/resources/db_init_phase1.sql > /tmp/u2b_phase1.log 2>&1
$PSQL -d sw_u2b -f src/main/resources/db_init_phase1_sigungu.sql > /tmp/u2b_sigungu.log 2>&1
# V*.sql 까지 적용 (DbInitRunner 가 phase2 만 실행하므로 V*.sql 은 별도 적용 필요 — setup-guide §2-2 절차)
for v in swdept/sql/V*.sql; do
  $PSQL -d sw_u2b -f "$v" >> /tmp/u2b_v_all.log 2>&1
done

DB_URL="jdbc:postgresql://localhost:25880/sw_u2b" DB_USERNAME=postgres DB_PASSWORD=ephemeral \
bash server-restart.sh > /tmp/u2b_boot.log 2>&1
until grep -qE "Started SwManagerApplication|APPLICATION FAILED TO START|Caused by:|Exception in thread" server.log 2>/dev/null; do
  sleep 2
done
grep -E "Started SwManagerApplication|^.{20,30}ERROR|PSQLException|relation .* does not exist" server.log | head -10
```

- **Pass 기준 (NFR-2-b, 보조 smoke)**:
  - `Started SwManagerApplication` 출력
  - 본 스프린트 변경 surface 관련 ERROR 0
  - "relation does not exist" 0건
  - ※ DbInitRunner 가 stmt 별 예외를 debug 로 삼키므로 부팅 성공만으로는 시드 부재 검출력 부족 — NFR-4 (T3) 가 시드 의미 보존 주 검출

#### 3-5. T3 — 시드 의미 보존 (NFR-4, codex v2 권고 6 SQL 본문 사용)

baseline 클러스터에 동일 절차 (phase1+sigungu+phase2+V*.sql) 적용 후 Step 1-4 SQL 동일 실행:

```bash
# baseline (선행 sprint commit 856b460 의 phase2 사용)
# v1.1 (codex 권고 2): git stash 대신 git show 로 작업트리 무영향 추출
git show 856b460:src/main/resources/db_init_phase2.sql > /tmp/db_init_phase2_856b460.sql

$PSQL -d sw_u2_baseline -f src/main/resources/db_init_phase1.sql > /dev/null 2>&1
$PSQL -d sw_u2_baseline -f src/main/resources/db_init_phase1_sigungu.sql > /dev/null 2>&1
# baseline phase2 는 외부 V*.sql 의존 INSERT 가 살아있음 → line 686 stop 가능
# 그 stop 자체가 baseline 의 본질 (본 sprint 가 해소하려는 것). ON_ERROR_STOP 무시 모드로 시도
PSQL_NOSTOP='"C:/Users/PUJ/PostgreSQL/16/bin/psql.exe" -h localhost -p 25880 -U postgres -v ON_ERROR_STOP=0'
$PSQL_NOSTOP -d sw_u2_baseline -f /tmp/db_init_phase2_856b460.sql > /tmp/baseline_phase2.log 2>&1 || true
# → V*.sql 적용 후 V024/V026 가 자체 시드 INSERT (시드 SSoT)
for v in swdept/sql/V*.sql; do
  $PSQL -d sw_u2_baseline -f "$v" >> /tmp/baseline_v.log 2>&1
done

# 두 클러스터 (sw_u2 patched / sw_u2_baseline) 모두에서 NFR-4 SQL 실행
for db in sw_u2 sw_u2_baseline; do
  echo "=== $db ==="
  $PSQL -d $db -tA <<'SQL'
SELECT 'qt_category_mst' AS tbl, COUNT(*) AS cnt,
       md5(string_agg(category_code || '|' || category_label || '|' || display_order, ',' ORDER BY category_code)) AS sig
FROM qt_category_mst;
SELECT 'work_plan_type_mst' AS tbl, COUNT(*) AS cnt,
       md5(string_agg(type_code || '|' || type_label || '|' || COALESCE(color,'') || '|' || display_order, ',' ORDER BY type_code)) AS sig
FROM work_plan_type_mst;
SELECT 'work_plan_status_mst' AS tbl, COUNT(*) AS cnt,
       md5(string_agg(status_code || '|' || status_label || '|' || display_order, ',' ORDER BY status_code)) AS sig
FROM work_plan_status_mst;
SQL
done
```

- **Pass 기준 (NFR-4)**:
  - 두 클러스터의 3 테이블 모두 (cnt, sig) 1:1 동일
  - cnt: 3 / 10 / 7 정확 일치
  - sig (md5): patched vs baseline 완전 일치 → 시드 의미 변동 0

> **본 T3 가 codex 2차 비차단 권고의 핵심 보강** — 기획서 R-6 의 "시드 부재 검출" 주 책임이 본 T3 에 있음. NFR-7 회귀 스모크는 보조 smoke (사용자 화면 진입 정상 확인).

#### 3-6. T4 — V024 / V026 SHA256 무변경 (NFR-6)

```bash
sha256sum swdept/sql/V024_qt_category_master.sql swdept/sql/V026_work_plan_master.sql > /tmp/v_sha256_after.txt
diff /tmp/v_sha256_before.txt /tmp/v_sha256_after.txt
echo "diff rc=$? (0 = 무변경)"
```

- **Pass 기준**: diff 0줄 (Flyway checksum 보존).

#### 3-7. T5 — git diff 의미 검증 (참고, 본 sprint 는 단순 삭제라 T3 만큼 핵심 아님)

```bash
git diff 856b460 HEAD -- src/main/resources/db_init_phase2.sql > /tmp/phase2_diff.patch
grep -cE "^@@" /tmp/phase2_diff.patch
echo "추가 줄 수: $(grep -cE '^\+[^+]' /tmp/phase2_diff.patch)"
echo "삭제 줄 수: $(grep -cE '^-[^-]' /tmp/phase2_diff.patch)"
```

- **Pass 기준 (참고)**:
  - hunk 3개 (헤더 주석 1 + qt_category INSERT + work_plan_type/status INSERT, 인접 시 병합 가능)
  - 추가 줄: 약 4~6줄 (헤더 주석 1줄 + 잔존 주석 2~3줄)
  - 삭제 줄: 약 30~40줄 (3 INSERT block 본문)
  - 사람 read 검토로 실수 INSERT/CREATE 변경 0 확인

**진행 게이트**: T1~T5 모두 PASS. 실패 시 Step 4 미진입 + Edit 되돌림.

---

### Step 4 — 문서 갱신 (FR-4)

#### 4-1. `docs/PLANS.md` §2-b 갱신 (FR-4-A)

후속 백로그 `phase2-vsql-external-deps` 항목을 **완료 스프린트** 표기로 갱신:

```markdown
- `phase2-vsql-external-deps` — **완료 (2026-05-11, commit `<TBD>`)**:
  - 산출물: `db_init_phase2.sql` (외부 V*.sql 의존 INSERT 3건 삭제 — qt_category_mst / work_plan_type_mst / work_plan_status_mst)
  - 검증 (ephemeral 25880): T1~T5 PASS. fresh-init `phase1 → phase2 → V*.sql` **완전 무중단 통과** (phase2.sql rc=0).
  - V024/V026 무수정 (Flyway checksum 보존). 운영DB 영향 0 (NFR-5).
  - **phase 시리즈의 마지막 디딤돌** — 선행 sprint phase2-V018-init-ordering / phase2-tb_ops_doc-forward-ref 의 누적 결과로 phase2.sql fresh-init 완전 자동 복원 가능.
```

#### 4-2. `docs/references/setup-guide.md` §2-2 갱신 (FR-4-B)

선행 sprint 가 추가한 보강 줄을 다음으로 갱신:

```markdown
- **2026-05-11 보강** (`phase2-V018-init-ordering` + `phase2-tb_ops_doc-forward-ref` + `phase2-vsql-external-deps` 적용 후): 본 절차의 phase2.sql 은 **완전 무중단 통과 보장**. line 60/70 (V018 UNIQUE 의존) + line 464 부근 (tb_ops_doc forward-reference) + line 686/697/710 부근 (외부 V*.sql 의존 INSERT) 모두 회귀 차단. fresh-init `psql -f` 와 DbInitRunner 부팅 양 경로 정상.
```

#### 4-3. (선택) `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` Discovery 라벨 갱신 (FR-4-C)

기획서의 Discovery 줄 (`qt_category_mst does not exist`) 옆에 1줄 추가:

```markdown
> **2026-05-11 closure**: 후속 sprint `phase2-vsql-external-deps` (commit `<TBD>`) 로 해결. fresh-init full phase2 통과 보장.
```

**진행 게이트**: 4-1, 4-2 완료 (4-3 선택). 문서 변경만 — compile 영향 0.

---

### Step 5 — 빌드 / 운영 부팅 / 회귀 스모크 (NFR-1, NFR-7)

5-1. **Maven compile**:
```bash
./mvnw clean compile
```
- 기대: BUILD SUCCESS.

5-2. **운영 환경 server-restart**:

```bash
bash server-restart.sh > /tmp/final_boot.log 2>&1
until grep -qE "Started SwManagerApplication|APPLICATION FAILED TO START|Caused by:|Exception in thread" server.log 2>/dev/null; do
  sleep 2
done

echo "=== (1) Started 1건 (PASS 기대) ==="
STARTED_CNT=$(grep -c "Started SwManagerApplication" server.log)
echo "Started count: $STARTED_CNT (1 이면 PASS)"

echo "=== (2) DB 초기화 완료 1건 + N 비교 (PASS 기대) ==="
DBINIT_CNT=$(grep -c "DB 초기화 완료" server.log)
echo "DB init count: $DBINIT_CNT (1 이면 PASS)"
grep "DB 초기화 완료" server.log

# v1.2: baseline N 과 비교 (Step 1-5 에서 /tmp/baseline_N.txt 에 기록)
PATCHED_N=$(grep -oE "DB 초기화 완료: [0-9]+개" server.log | tail -1 | grep -oE "[0-9]+")
BASELINE_N=$(grep -oE "[0-9]+" /tmp/baseline_N.txt)
DIFF_N=$((PATCHED_N - BASELINE_N))
echo "BASELINE_N=$BASELINE_N, PATCHED_N=$PATCHED_N, DIFF=$DIFF_N (-3 이면 PASS — 본 sprint 가 INSERT 3 stmt 삭제)"

echo "=== (3) PSQLException / relation does not exist / syntax error 0건 ==="
ERR_CNT=$(grep -ciE "PSQLException|relation .* does not exist|syntax error at or near" server.log)
echo "본 스프린트 surface 관련 ERROR count: $ERR_CNT (0 이면 PASS)"
```

- **Pass 기준 (3가지 모두 충족, v1.2 정정)**:
  - (1) Started count = 1
  - (2) DB init count = 1. **`PATCHED_N - BASELINE_N == -3`** (Step 1-5 에서 측정한 baseline 과 정확히 3 감소). 다른 값이면 본 sprint 외 stmt 변동이 끼어든 것 → 분석 후 진행 결정.
  - (3) PSQLException + relation does not exist + syntax error 합계 = 0

5-3. **회귀 스모크** (수동, **R-6 보조 smoke**):

> **codex 2차 비차단 권고 반영**: NFR-7 회귀 스모크는 R-6 검출의 **보조** smoke (Java enum fallback 으로 인해 즉시 ERROR 보장 X). 시드 부재 주 검출은 T3 (NFR-4 MD5).

- 점검내역서 화면 → 목록 1건 진입 → 정상
- 사업관리 화면 → 정상
- **견적서 화면** (`qt_category_mst` 사용) → **카테고리 드롭다운 = 3 옵션** (유지보수/용역/제품)
- **작업계획 화면** (`work_plan_type_mst` + `work_plan_status_mst` 사용) → **유형 드롭다운 = 10 옵션** (계약/설치/패치/점검/사전연락/장애처리/업무지원/기성·준공/준공/기타) + **상태 드롭다운 = 7 옵션** (예정/연락완료/확정/진행중/완료/연기/취소)
- **조직도 화면** → 정상

> **baseline N 출처**: Step 1-5 에서 본 sprint Edit 직전 server-restart 1회 결과를 `/tmp/baseline_N.txt` 에 기록함. T7 (Step 5-2) 의 patched N 비교에 사용. (v1.2 보강)

**진행 게이트**: 5-1 BUILD SUCCESS + 5-2 ERROR 0 + 5-3 5개 화면 정상. → "작업완료" 발화 가능.

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | phase2.sql 단독 멱등 3회 | Step 3-2 | 3회 rc=0, ERROR 0, count 3/10/7 |
| T2 | psql fresh-init **완전 통과** | Step 3-3 | phase2.sql rc=0 (line 686/697/710 stop 회귀 0건) — **본 sprint 핵심 판정** |
| T2-b | DbInitRunner 부팅 (보조 smoke) | Step 3-4 | Started + ERROR 0 + "relation does not exist" 0건 |
| T3 | 시드 의미 보존 (R-6 주 검출) | Step 3-5 | patched vs baseline 클러스터의 (cnt, md5 sig) 1:1 동일. cnt 3/10/7. |
| T4 | V024/V026 SHA256 무변경 | Step 3-6 | diff 0줄 |
| T5 | git diff 의미 검증 (참고) | Step 3-7 | hunk ≤ 4, read 검토 acceptance |
| T6 | Maven compile | Step 5-1 | BUILD SUCCESS |
| T7 | 운영 환경 부팅 | Step 5-2 | Started count=1, DB init count=1, **`PATCHED_N - BASELINE_N == -3`** (BASELINE_N 출처: Step 1-5 에서 본 sprint Edit 직전 server-restart 결과의 `/tmp/baseline_N.txt`). PSQLException + relation does not exist + syntax error 합계 = 0 |
| T8 | 회귀 스모크 (5 화면, 보조) | Step 5-3 | 점검내역서 목록 정상. 사업관리 정상. **견적서 카테고리 드롭다운 = 3 옵션** (유지보수/용역/제품). **작업계획 유형 드롭다운 = 10 옵션** (계약/설치/패치/점검/사전연락/장애처리/업무지원/기성·준공/준공/기타). **작업계획 상태 드롭다운 = 7 옵션** (예정/연락완료/확정/진행중/완료/연기/취소). 조직도 정상. |

총 9 케이스. 1건이라도 FAIL 시 "작업완료" 발화 금지.

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 2 compile 실패 | `git checkout -- src/main/resources/db_init_phase2.sql` 후 Edit 재진입 |
| Step 2 의 Edit 중 anchor 매칭 실패 | 동일 (단일 파일 revert) + Read 로 anchor 재확인 |
| Step 3 T1~T5 1건이라도 FAIL | 동일 + 로그 분석 후 Step 2 재진입 |
| **Step 4 진입 전 단계 실패** | `git checkout -- src/main/resources/db_init_phase2.sql` (단일 파일 revert) |
| **Step 4 이후 단계 실패 (Step 5-2/5-3, commit 직전)** | `git checkout -- src/main/resources/db_init_phase2.sql docs/PLANS.md docs/references/setup-guide.md` (+ 4-3 적용했으면 phase2-tb_ops_doc spec.md) |
| **commit 후 push 전 회귀** | `git reset --hard HEAD~1` (해당 commit 가 HEAD 일 때만) |
| **commit + push 후 회귀** | `git revert <commit>` → push → `bash server-restart.sh`. SQL+문서 통합 1 commit 이라 단일 revert 안전 복원 |
| 운영DB 손상 가능성 | **0** (NFR-5, v1.1 정정) — 본 스프린트 결과물 적용 시 **삭제된 3개 INSERT stmt 가 더 이상 실행되지 않음**. V024/V026 가 이미 시드 적용 완료 (운영 환경). 따라서 운영 schema/seed row 변동 0. (DbInitRunner 가 phase2 를 부팅 시 실행하지만 본 sprint 가 stmt 자체를 제거하므로 효과 0.) |

---

## 4. 리스크·완화 재확인 (기획서 §7 R-1~8 그대로)

| ID | 리스크 | 본 개발계획 에서 완화 |
|----|--------|--------------------|
| R-1 | INSERT 삭제 후 시드 손실 | T3 (NFR-4 MD5) 가 patched vs baseline 클러스터 1:1 동일 입증 |
| R-2 | 운영DB 시드 변동 | NFR-5 (stmt 미실행). Step 5-2 운영 부팅 검증. DB 초기화 완료 메시지의 N 변동 (선행 -3) 확인 |
| R-3 | V024/V026 시드 row 와 phase2 row 미세 차이 | Step 1-4 + T3 baseline 비교로 자동 검증 |
| R-4 | phase2.sql 의 다른 외부 의존 노출 | Step 1-2 grep 사전 점검. 본 sprint 후 fresh-init 완전 통과로 최종 확인 |
| R-5 | V024/V026 Flyway checksum 변경 | T4 SHA256 diff 0 |
| R-6 | DbInitRunner-only 환경 시드 부재 | T3 가 주 검출. setup-guide §2-2 절차 명시 (V*.sql 별도 적용 필요). NFR-7 회귀 스모크 보조 smoke |
| R-7 | 시드 history 손실 | git log + V024/V026 헤더 + PLANS.md 보존. phase2 historical 주석은 의미 변경 0 |
| R-8 | DbInitRunner debug 삼킴으로 결함 미감지 | T2-b 보조 smoke 위치 부여. 본 sprint scope 외 후속 후보 (DbInitRunner 진단 모드) |

---

## 5. 산출물 / 커밋 단위

**1 commit 통합 (확정 정책)** — 선행 sprint 동일 패턴.

**Commit (필수, 1건)** — 코드 + 모든 문서:
- `src/main/resources/db_init_phase2.sql` (Step 2)
- `docs/PLANS.md` (Step 4-1)
- `docs/references/setup-guide.md` (Step 4-2)
- `docs/product-specs/phase2-vsql-external-deps.md` v2 (기획서)
- `docs/exec-plans/phase2-vsql-external-deps.md` v1 (본 개발계획서)
- `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` (Step 4-3 — 선행 sprint Discovery closure 라벨, 선택 권장)

커밋 메시지 (예시):
```
feat(phase2-vsql-external-deps): 외부 V*.sql 의존 INSERT 3건 제거 (qt_category_mst / work_plan_type_mst / work_plan_status_mst)

phase 시리즈의 마지막 디딤돌. 선행 sprint phase2-tb_ops_doc-forward-ref 의 Discovery
로 표면화한 외부 V*.sql 정의 테이블 의존 INSERT 3건 (phase2.sql line 686/697/710 stop)
해소. V024/V026 가 시드 SSoT 책임. V*.sql 무수정.

운영DB 영향 0 — 삭제된 stmt 자체 미실행 + V024/V026 이미 적용 → schema/seed 변동 0.

검증 (ephemeral 25880):
- T1 phase2 멱등 3회 — ERROR 0, count 3/10/7
- T2 psql fresh-init 완전 통과 — phase2.sql rc=0 (외부 의존 stop 회귀 0건)
- T2-b DbInitRunner 부팅 — Started, ERROR 0
- T3 시드 의미 보존 — patched vs baseline 클러스터 (cnt, md5) 1:1 동일
- T4 V024/V026 SHA256 무변경
- T5 git diff hunk 검증 — read acceptance

선행 sprint phase2-tb_ops_doc-forward-ref Discovery closure 완료 — fresh-init
full phase2 완전 무중단 통과 보장.

근거:
- 기획서: docs/product-specs/phase2-vsql-external-deps.md v2 (codex 2차 ⭕ 2026-05-11)
- 개발계획: docs/exec-plans/phase2-vsql-external-deps.md v1
- 선행: phase2-tb_ops_doc-forward-ref (856b460, EC-1~5 충족)

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
```

> master 직접 push (single-shot, harness-hardening-v1 confirm 게이트 적용).

---

## 6. 승인 요청

본 개발계획서 v1 에 대한 codex 검토 + 사용자 최종승인 요청.

### 검토 포인트
- Step 2-2 / 2-3 의 anchor 텍스트 unique 여부 (각 INSERT 가 phase2.sql 안에서 1회만 등장)
- Step 3-5 T3 의 baseline 비교 방식 (git stash → baseline DB 빌드 → stash pop) 의 안전성
- Step 3-5 T3 의 NFR-4 MD5 SQL 본문이 codex v2 권고 6 의 정밀화 요구를 충족하는지
- Step 5-3 회귀 스모크 5 화면 선정의 적절성 (R-6 보조 smoke 위치)
- 1 commit 통합 정책

### 다음 절차 (v1.2 갱신)

현 단계: codex 1차 ⚠ → 2차 ⚠ → **v1.2 정정 (T7 baseline N 절차 실행 가능화) + 3차 검토 대기**.

1. **codex 3차 검토** (v1.2 기준)
2. ⭕ → 사용자 최종승인 → Step 1~5 순차 실행
3. ⚠ → v1.3 개정 (또는 부분 수용 결정)
4. T1~T8 모두 PASS → "작업완료" → 자동 commit+push (1 commit 통합)
