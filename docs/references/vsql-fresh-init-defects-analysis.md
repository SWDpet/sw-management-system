---
tags: [reference, analysis, schema, fresh-init, vsql-defects]
related-sprints: ["phase2-vsql-external-deps (PAUSED)", "phase1-ddl-formalization", "phase2-V018-init-ordering", "phase2-tb_ops_doc-forward-ref"]
created: "2026-05-11"
revised: "2026-05-11"
status: v3.1 (codex 1차 ⚠ 7건 + 2차 ⚠ 차단 2건 + 3차 ⚠ §2 1문장 정정 반영)
---

# V*.sql fresh-init 결함 분석 (9건) — v3.1

## v3.1 변경 이력 (codex 3차 ⚠ §2 총평 1문장 정정, 2026-05-11)

- **§2 총평 정정** — `procedure-fix 가 해소하는 7건` 의 구성을 명확히. 기존 표현이 "#6 V005 옵션 E 포함" 으로 §5-1/§5-4 의 "잔여 V005 + V100 = 2건" 과 충돌. 정정: **procedure-fix 7건 = #1/#2/#3/#4/#5/#7/#8** (V005 옵션 E 는 별도 `vsql-V005-ordering` sprint 안의 무수정 옵션으로 분리). #6 V005 + #9 V100 은 임시 skip-list 로 NFR-2-a 에서 제외 후 별도 sprint 처리.

## v3 변경 이력 (codex 2차 ⚠ 차단 2건 + 정합성 1건 반영, 2026-05-11)

1. **V025 처리 방침 고정** — §4 sprint 표 (V025 를 `vsql-fresh-init-skip-list` 범위에 포함) 가 정답. §5-1, §5-4 정정 — `procedure-fix` 후 allowlist 잔여는 **V005 + V100 = 2건** (V025 제외).
2. **allowlist 의미 명확화** — §5-2 의 "allowlist" 가 단순 "실패 무시" 가 아니라 **"본 sprint 의 procedure-fix sprint 가 setup-guide 의 fresh-init 절차에서 해당 V*.sql 을 일시 skip 처리 → 별도 sprint 처리 후 skip 해제"** 임을 명시. v3 기획서에서 NFR-2-a 작성 시 사용할 표현도 함께 고정.
3. **PLANS.md 등록 완료** — 본 보고서가 권고한 후속 sprint 4개 (`vsql-fresh-init-procedure-fix`, `vsql-V005-ordering`, `vsql-V100-legacy-contract-cleanup`, 그리고 선택 `vsql-V025-idempotent`) 가 PLANS.md §2-b 에 실제 등록됨. (별도 작업)

## v2 변경 이력 (codex 1차 ⚠ 7건 반영, 2026-05-11)

1. **§1-C V005 해소 옵션** — `fresh-init manifest 또는 custom order` (setup-guide 만 변경, checksum 0) 옵션 추가. Flyway version 중복 자체의 별도 리스크 노트.
2. **§1-F V100 해소 옵션** — `idx_doc_contract` guard 단독 부족 명시. line 388-390 trigger loop 가 `tb_contract` (현재 주석 + phase2 미존재) 상에서 추가 fail 가능 — V100 의 legacy contract block 전체를 다뤄야 함.
3. **§3-1 Flyway 영향 보강** — V*.sql 삭제/rename 의 추가 위험 (validation failure / missing migration / duplicate version) 명시.
4. **§1-D / §2 우선순위 정정** — V023 "자동 해소" 가 V022 skip 권고와 충돌. V022 skip 시 V023 도 skip 또는 `check_section_mst` 별도 선행 DDL 제공 필요. §1-D 와 §2 표 양쪽 정정.
5. **§4 후속 sprint 백로그 재구성** — `vsql-fresh-init-skip-list` 에 V023 처리 방침 포함. `vsql-V100-tb_document-cleanup` → `vsql-V100-legacy-contract-cleanup` 으로 범위 확장.
6. **§5 재진입 조건 보강** — "5건 해소 후 4건 NFR-2-a 제외" 를 단순 권고에서 **명시적 allowlist + 사유 + V023 cascade 처리 방침** 으로 강화.
7. **§1-G 신규 — 누락된 결함 패턴**: (a) V022 도 `:run_id` 사용 (line 77-80) — V022 통과시키는 방향이면 동일 결함. (b) V100 의 `CREATE INDEX` 다수 `IF NOT EXISTS` 부재 → 재실행 멱등성 리스크.

---

## 0. 배경

`phase2-vsql-external-deps` 스프린트 (PAUSED, 2026-05-11) 의 Step 3 T2 측정에서 표면화된 결함 9건. fresh-init 절차 `phase1 → sigungu → phase2 → V*.sql` 에서 V*.sql 단계의 9개 파일이 ON_ERROR_STOP=1 모드로 stop 한 결과를 분석.

**측정 환경**:
- ephemeral cluster `localhost:25880` (`C:\Users\PUJ\pg16-verify\data`)
- DB: `sw_u2` (fresh CREATE DATABASE 후 phase1+sigungu+phase2 통과 → V*.sql for-loop)
- 측정일: 2026-05-11
- swmanager master HEAD: `856b460` 직후 (작업트리 baseline)
- phase2 통과 후 측정 — phase2 의 외부 의존 INSERT 3건은 본 sprint Step 2 적용 후 측정 (즉 phase2-vsql-external-deps 의 핵심 변경 적용된 상태)

**측정 결과**: phase2.sql rc=0 (본 sprint 핵심 ✅). V*.sql 실패 파일 9건.

---

## 1. 결함 9건 분류

### 1-A. 의도된 abort (fresh-init 에서 정상 — skip 처리만 필요) — 2건

| # | V*.sql | line | ERROR 메시지 | 의미 |
|---|--------|------|------------|------|
| 1 | `V021_rollback_data.sql` | 43 | `HALT: no V021 backup table found — cannot rollback` | V021 백업 부재 시 rollback 시도 abort. fresh-init 에 backup 이 없는 게 정상 — V021 의 안전 게이트 |
| 2 | `V022_inspect_comprehensive_redesign.sql` | 56 | `HALT Phase 0: UPIS_SW 총 0 (기준 22±5 초과)` | inspect_template 의 UPIS_SW 데이터 0건 → Phase 0 안전 게이트 트립. V022 의 핵심은 기존 inspect 데이터 재구성이라 fresh-init 에 데이터 0건이면 의미 없음 |

**해소 방향**: 
- V*.sql 무수정. setup-guide §2-2 의 V*.sql for-loop 에서 두 파일을 **skip-list** 로 명시 (예: `for v in swdept/sql/V*.sql; do case "$v" in *V021_rollback_data*|*V022_inspect_*) continue;; esac; psql ...; done`)
- 또는 fresh-init 절차 자체에 "intentional abort 2건은 skip" 안내 추가

**Flyway 영향**: 0 (V*.sql 수정 없음)
**운영 영향**: 0 (운영DB 는 V021_rollback 적용 안 함, V022 는 inspect 데이터 보유 상태로 통과 완료)

---

### 1-B. psql 호출 절차 결함 (`:run_id` 변수 치환자) — 3건

| # | V*.sql | line | ERROR 메시지 | 코드 패턴 |
|---|--------|------|------------|---------|
| 3 | `V019_access_log_userid_cleanup.sql` | 37 | `syntax error at or near ":"` | `CREATE TABLE access_logs_cleanup_backup_:run_id AS ...` |
| 4 | `V020_qt_remarks_pattern_user_link.sql` | 39 | 동일 | `CREATE TABLE qt_remarks_pattern_v020_backup_:run_id AS ...` |
| 5 | `V021_users_masking_regression_fix.sql` | 31 | 동일 | `CREATE TABLE users_v021_backup_:run_id AS ...` |

**원인**: psql 의 `:variable` 구문은 변수 치환자. 호출 시 `-v run_id=<value>` 옵션 없이 실행하면 `:run_id` 가 그대로 syntax error.

**해소 방향**:
- **옵션 A**: setup-guide §2-2 의 V*.sql for-loop 에 `-v run_id=$(date +%Y%m%d_%H%M%S)` 추가
  ```bash
  RUN_ID=$(date +%Y%m%d_%H%M%S)
  for v in swdept/sql/V*.sql; do
    psql -v ON_ERROR_STOP=1 -v run_id=$RUN_ID -d <db> -f "$v"
  done
  ```
- **옵션 B**: 3개 V*.sql 안에서 `:run_id` 를 timestamp literal 로 escape — Flyway 위배

**해소 우선 옵션**: A (V*.sql 무수정, setup-guide 만 보강)
**Flyway 영향**: A=0, B=위배
**운영 영향**: 0 (운영DB 적용 시 이미 RUN_ID 주입 또는 다른 방식 사용했을 것 — 운영 적용 이력 확인 필요)

---

### 1-C. 외부 정의 의존 (다른 V*.sql 또는 phase2 정의 의존) — 1건

| # | V*.sql | line | ERROR 메시지 | 의존 객체 | 정의 위치 |
|---|--------|------|------------|---------|---------|
| 6 | `V005_add_surveying_wage_rates.sql` | 11 | `relation "qt_wage_rate" does not exist` | `qt_wage_rate` 테이블 | `V005_wage_rate_table.sql:11` |

**원인 (가장 단순한 ordering bug)**:
- V005 폴더에 2개 파일: `V005_add_surveying_wage_rates.sql` + `V005_wage_rate_table.sql`
- 사전순 = 실행순 → `V005_add_surveying_wage_rates` 가 먼저 (INSERT) → `V005_wage_rate_table` 이 나중 (CREATE)
- INSERT 가 CREATE 보다 먼저 실행되어 stop

**해소 방향**:
- **옵션 A**: `V005_wage_rate_table.sql` → `V004_wage_rate_table.sql` rename. 단 Flyway 의 version 정렬상 V004 가 V005_add_surveying_wage_rates 보다 먼저 → 통과
- **옵션 B**: 두 파일 통합 — `V005_wage_rate_table.sql` 안에 INSERT 도 함께 두고 `V005_add_surveying_wage_rates.sql` 삭제
- **옵션 C**: `V005_add_surveying_wage_rates.sql` 의 INSERT 들을 `V005_wage_rate_table.sql` 의 CREATE 직후로 이동
- **옵션 D**: V005_wage_rate_table 를 phase2 로 흡수 (선이동 패턴, 단 Flyway version mismatch 가능)
- **옵션 E (codex v2 권고 2-A)**: **fresh-init manifest 또는 custom order** — setup-guide §2-2 의 V*.sql for-loop 를 단순 사전순 대신 명시적 manifest 기반으로 변경. 예: `V005_wage_rate_table.sql` 을 먼저 실행하도록 manifest 에 명시. **V*.sql 무수정 → checksum 영향 0**. 단 별도 리스크: 두 파일이 동일 `V005` version 을 보유 — Flyway 가 versioned migration 으로 읽으면 **duplicate version error** 가능 (운영DB 적용 시점에서는 어떻게 통과했는지 history 확인 필수)

**해소 우선 옵션**: **E** (manifest 기반, V*.sql 무수정, Flyway 영향 0) 가 가장 안전. 단 V005 duplicate version 자체의 리스크는 별도 — 장기적으론 B 또는 A 권장 (단 운영DB history 확인 후).

**Flyway 영향**: 옵션 B/C 는 V005_add_surveying_wage_rates checksum 변경. 옵션 A 는 신규 V004 등장 + V005_wage_rate_table 삭제 (이미 적용된 versioned migration 일 경우 **validation failure / missing migration / duplicate version**). 옵션 E 는 V*.sql 무수정 → checksum 0.
**운영 영향**: 운영DB 의 flyway_schema_history 에 V005 두 파일이 어떻게 기록되어 있는지 확인 필요. 두 파일이 모두 v005 로 적용되었다면 Flyway의 "허용된 동일 version 다중 파일" 케이스 (Flyway pro 또는 runtime config) — 본 환경 동작 방식 사전 검증.

---

### 1-D. cascade 의존 (V022 stop 의 부산물) — 1건

| # | V*.sql | line | ERROR 메시지 | 의존 객체 | 정의 위치 |
|---|--------|------|------------|---------|---------|
| 7 | `V023_inspect_category_master.sql` | 65 | `relation "check_section_mst" does not exist` | `check_section_mst` | `V022_inspect_comprehensive_redesign.sql:85` |

**원인**: 
- V023 의 `check_category_mst` 가 `REFERENCES check_section_mst(section_code)` (V023:60)
- `check_section_mst` 는 V022 가 정의 (Phase 1+ 에서 CREATE, line 85)
- V022 가 fresh-init 에서 Phase 0 abort 로 stop → check_section_mst 미생성 → V023 도 stop

**해소 방향 (codex v2 권고 4 정정)** — V022 skip 권고와 충돌하므로 다음 중 하나:
- **옵션 A**: V022 + V023 **둘 다 skip-list 에 포함**. fresh-init 에서 check_section_mst 부재 → 본 환경에서 V023 의 check_category_mst 도 부재 → 점검 카테고리 기능이 V023 의존 → fresh-init 환경에서 점검 모듈 제한 (운영DB 영향 0)
- **옵션 B**: `check_section_mst` (와 V022 의 Phase 1+ 의 다른 schema 정의) 를 **phase2 또는 별도 선행 DDL 로 추출** → V022 가 fresh-init 에서 skip 되어도 V023 가 자체 통과 가능. V022 본문은 데이터 마이그레이션만 담당 (skip 처리 그대로)
- **옵션 C**: V022 의 Phase 0 게이트를 `IF UPIS_SW < 22 THEN abort` 대신 `IF data_present THEN ... ELSE skip_phase0 END` 로 가드 추가 (V022 수정 = Flyway 위배)

**해소 우선 옵션**: A (가장 단순, V022 와 V023 운영DB 적용 완료 상태 유지) 또는 B (장기적으로 fresh-init 의 점검 모듈 무결성). C 는 Flyway 위배.

**Flyway 영향**: A/B = 0 (V023 무수정), C = 위배
**운영 영향**: 0 (V022/V023 운영DB 적용 완료)

---

### 1-E. 멱등성 결함 (이미 처리된 환경에서 재실행 시 fail) — 1건

| # | V*.sql | line | ERROR 메시지 | 의존 객체 |
|---|--------|------|------------|---------|
| 8 | `V025_drop_pjt_equip.sql` | 50 / 54 | `relation "pjt_equip" does not exist` | `pjt_equip` 테이블 |

**원인**:
- V025 의 핵심: `pjt_equip` 테이블을 DROP. Phase 0 안전 게이트 (line 50): `SELECT COUNT(*) INTO row_cnt FROM pjt_equip`
- phase2 (또는 phase1) 에서 `pjt_equip` 정의가 0 (이미 제거됨, 헤더 주석 line 15 참조)
- fresh-init 에 `pjt_equip` 자체가 미존재 → V025 의 SELECT COUNT(*) 가 미존재 테이블 참조로 fail
- V025 의 Phase 0 게이트가 "테이블 존재 여부 사전 체크" 가 없음 — 멱등성 결함

**해소 방향**:
- **옵션 A**: V025 의 Phase 0 시작에 `IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='pjt_equip')` 가드 추가. 미존재 시 PASS NOTICE 후 정상 종료
- **옵션 B**: setup-guide skip-list 에 V025 추가 (1-A 의 V021_rollback / V022 와 동일 패턴 — "이미 적용됐으니 fresh-init skip")
- **옵션 C**: phase1 또는 phase2 에 `pjt_equip` 빈 테이블 일시 생성 (DROP 대상 마련) — 의미 없는 임시 객체

**해소 우선 옵션**: B (setup-guide skip) — V025 의 본질이 "이미 운영DB 에 적용된 일회성 cleanup" 이므로 fresh-init skip 이 의미 정합. A 는 V025 수정 → Flyway 위배 위험.

**Flyway 영향**: A=위배, B=0
**운영 영향**: 0 (운영DB 는 V025 이미 적용 — pjt_equip 제거 완료)

---

### 1-F. 외부 컬럼 의존 (V100 의 legacy contract block 잔존) — 1건 (실제론 다중 결함)

| # | V*.sql | line | ERROR 메시지 | 의존 객체 |
|---|--------|------|------------|---------|
| 9 | `V100_work_plan_performance_tables.sql` | 221 (관측) / 388-390 (잠재) | `column "contract_id" does not exist` (line 221) → 그 이후 trigger loop 의 `tb_contract` 의존 (잠재) | `tb_document.contract_id` + `tb_contract` 와 관련 객체 다수 |

**원인 (codex v2 권고 2-B 보강 — 다중 결함)**:
- V100 line 207 부근: `CREATE TABLE IF NOT EXISTS tb_document ( ... contract_id INTEGER REFERENCES tb_contract(contract_id), ... )`
- phase2 에 이미 `tb_document` 가 다른 schema (contract_id 컬럼 없음) 로 정의됨
- `IF NOT EXISTS` 로 V100 의 CREATE 는 skip → contract_id 컬럼 미생성
- line 221 의 `CREATE INDEX idx_doc_contract ON tb_document(contract_id)` 가 미존재 컬럼 참조 → 첫 번째 fail (관측됨)
- **잠재적 후속 fail (idx_doc_contract guard 후 노출 가능)**:
  - line 388-390 의 trigger loop 가 `tb_contract` 를 대상으로 `DROP TRIGGER ... ON tb_contract` 실행
  - `tb_contract` 블록 (line 57-) 은 현재 V100 본문에 주석 처리되어 있고 phase2 에도 미존재 → trigger loop fail
  - V100 의 `tb_contract_participant`, `tb_contract_target` 도 phase2 와 충돌 (phase2 에 `tb_contract_participant` 가 다른 schema 로 정의됨)
- V100 헤더 주석 (line 98-100) 참조: "이 파일의 정의는 contract_id FK 를 사용하는 구버전. proj_id → sw_pjt 버전. 본 스프린트에서 contract_id 컬럼/FK/INDEX 모두 DROP" — V100 자체가 **legacy contract block 정리 미완 상태**

**해소 방향 (codex v2 권고 2-B 보강)**:
- **옵션 A (부분 처리, 권장 X)**: V100 line 221 의 idx_doc_contract 만 guard 추가. **단독으론 부족** — 후속 trigger loop 등에서 추가 fail 가능
- **옵션 B**: phase2 의 `tb_document` 정의에 contract_id 컬럼 추가 (V100 schema 와 호환). 단 phase2 변경 = phase 시리즈 재진입
- **옵션 C (권장)**: **V100 의 legacy contract block 전체 정리** — 헤더 주석에서 예고된 "후속 스프린트" 본질. 다음 모두 처리:
  - line 207 의 `tb_document.contract_id` 컬럼 (또는 FK) 제거
  - line 221 의 `idx_doc_contract` 제거
  - line 388-390 trigger loop 에서 `tb_contract` 대상 제거
  - V100 안 `tb_contract_participant`, `tb_contract_target` 정의 (line 104, 124) 와 phase2 호환성 확인 + 필요 시 정리
  - V100 안 `CREATE INDEX` 다수 `IF NOT EXISTS` 부재 (재실행 멱등성 리스크) — `IF NOT EXISTS` 일괄 보강 (codex v2 권고 7 누락 패턴)
- **옵션 D**: setup-guide skip-list 에 V100 추가 (단 V100 의 핵심 기능인 work_plan/performance/document 등 다수 모듈 skip — 부적합)

**해소 우선 옵션**: **C** (별도 sprint `vsql-V100-legacy-contract-cleanup` 로 처리). 헤더 주석에서 예고된 "후속 스프린트" 의미 정합. A 는 단독 부족, B 는 phase 시리즈 재진입, D 는 부적합.

**Flyway 영향**: A/C = 위배 (V100 수정), B = phase2 변경 (Flyway 무관), D = 0
**운영 영향**: 운영DB 는 V100 이 이미 적용된 상태 — 운영 schema 가 V100 의 contract block 결과를 어떻게 가지고 있는지 확인 필요. **최소한 line 388-390 trigger loop 가 운영에서 어떻게 통과했는지 (당시 tb_contract 존재했는지) 확인 필수**.

---

### 1-G. 누락된 결함 패턴 (codex v2 권고 7 신규)

#### 1-G-1. V022 도 `:run_id` 사용

V022 는 §1-A 의 #2 로 의도된 abort 분류. 단 V022 를 통과시키는 방향 (skip 안 함) 이면 line 77-80 에서 `:run_id` 변수 치환 결함 발생:

```sql
-- V022_inspect_comprehensive_redesign.sql:77-80
CREATE TABLE inspect_report_backup_:run_id       AS SELECT * FROM inspect_report;
CREATE TABLE inspect_check_result_backup_:run_id AS SELECT * FROM inspect_check_result;
CREATE TABLE inspect_visit_log_backup_:run_id    AS SELECT * FROM inspect_visit_log;
CREATE TABLE inspect_template_backup_:run_id     AS SELECT * FROM inspect_template;
```

§1-B 의 `:run_id` 결함 패턴과 동일. setup-guide for-loop 에 `-v run_id=...` 추가하면 V022 도 통과 가능 (Phase 0 게이트 통과 시).

#### 1-G-2. V100 의 `CREATE INDEX` 다수 `IF NOT EXISTS` 부재

V100 의 다음 INDEX 들이 `IF NOT EXISTS` 부재 → 재실행 시 fail (재진입 멱등성 결함):

```sql
CREATE INDEX idx_contract_infra ON tb_contract(infra_id);          -- line 90
CREATE INDEX idx_contract_year ON tb_contract(contract_year);      -- line 91
CREATE INDEX idx_contract_status ON tb_contract(progress_status);  -- line 92
CREATE INDEX idx_participant_contract ON tb_contract_participant(contract_id);  -- line 115
CREATE INDEX idx_target_contract ON tb_contract_target(contract_id);            -- line 135
CREATE INDEX idx_cycle_infra ON tb_inspect_cycle(infra_id);        -- line 157
CREATE INDEX idx_cycle_assignee ON tb_inspect_cycle(assignee_id);  -- line 158
CREATE INDEX idx_plan_infra ON tb_work_plan(infra_id);             -- line 187
... (다수)
```

**해소**: `vsql-V100-legacy-contract-cleanup` sprint 에 일괄 `CREATE INDEX IF NOT EXISTS` 보강 포함.

---

## 2. 우선순위 + 분량 평가 (codex v2 권고 4 정정)

| 우선순위 | 결함 # | 해소 분량 | Flyway 영향 |
|---------|-------|---------|-----------|
| 1 (즉시 가능) | #1, #2 (V021_rollback, V022) **+ #7 V023 cascade** | 0 (setup-guide skip-list 만) | 0 |
| 2 (즉시 가능) | #3, #4, #5 (`:run_id` 3건) | 소 (setup-guide for-loop 에 `-v run_id` 추가) | 0 |
| 3 (단순 ordering) | #6 (V005) | 소 (옵션 E manifest) ~ 중 (rename / 통합 / 이동) | E=0 / B/C=위배 가능 |
| 4 (멱등성) | #8 (V025) | 소 (가드 추가) 또는 0 (skip) | 위배 또는 0 |
| 5 (legacy contract) | #9 (V100) | **중~대** (legacy contract block 전체 정리, IF NOT EXISTS 일괄 보강 포함) | 위배 가능 |

**총평 (v3.1 정정)**: 9건은 다음과 같이 분배:

- **`vsql-fresh-init-procedure-fix` sprint 가 해소하는 7건** (V*.sql 무수정 + setup-guide 보강만): #1 V021_rollback_data + #2 V022 + #3 V019 + #4 V020 + #5 V021_users_masking + #7 V023 (V022 cascade) + #8 V025. 모두 setup-guide §2-2 의 V*.sql for-loop 에 (a) skip-list (영구 4건: #1, #2, #7, #8) + (b) `-v run_id=...` (3건: #3, #4, #5) 추가로 통과.
- **별도 sprint 로 위임되는 2건** (NFR-2-a 의 임시 skip-list 로 제외): **#6 V005** (`vsql-V005-ordering` sprint, 옵션 E manifest 우선) + **#9 V100** (`vsql-V100-legacy-contract-cleanup` sprint, legacy contract block 전체 정리). 두 sprint 처리 후 임시 skip 해제.

**중요 정정 (codex v2 권고 4)**: #7 V023 은 **단순 자동 해소가 아님**. V022 가 skip-list 에 있으므로 V023 도 함께 skip 또는 `check_section_mst` 별도 선행 DDL 제공 필요. §1-D 옵션 A 채택 시 V023 도 skip — 본 v3 시점은 skip 채택.

---

## 3. 운영DB 영향 사전 체크 항목 (해소 진입 전 필수)

### 3-1. Flyway 위험성 평가 보강 (codex v2 권고 3)

V*.sql 수정 시 Flyway 영향은 단순 checksum 변경보다 더 넓은 위험을 가짐:

| 작업 | Flyway 위험 |
|------|------------|
| V*.sql 본문 수정 (text 변경) | checksum mismatch — Flyway validation failure (운영 부팅 실패 가능) |
| V*.sql 파일 삭제 | **missing migration** — Flyway 가 history 에 기록된 version 의 파일을 못 찾아 fail |
| V*.sql 파일 rename (예: V005 → V004) | **history 와 새 version 의 mismatch** — duplicate version 또는 version gap |
| 신규 V*.sql 추가 (높은 version) | 낮은 위험 (out-of-order 옵션 또는 단순 추가) |
| V*.sql 위치/순서만 변경 (manifest 기반) | **0** (V*.sql 자체 무수정) |

**권고**: V*.sql 수정/삭제/rename 작업 진입 전 운영DB 의 `flyway_schema_history` 확인 필수. 단순 로컬 fresh-init 통과만으로 운영 적용 가능 여부 판정 X.

### 3-2. 확인 SQL

V005, V022, V025, V100 의 운영DB 적용 상태 확인 (직접 query 또는 운영자 문의):

```sql
-- 운영DB flyway_schema_history (또는 v_schema_version) 조회
SELECT version, script, success, installed_on
FROM flyway_schema_history
WHERE script LIKE '%V005%' OR script LIKE '%V022%' OR script LIKE '%V025%' OR script LIKE '%V100%'
ORDER BY installed_on;
```

확인 필요한 사항:
- V005_*_table 과 V005_add_surveying 의 적용 순서 (운영에서 어떻게 통과했는지)
- V005 두 파일의 version 처리 (동일 V005 duplicate 인지, Flyway 가 어떻게 받아들였는지)
- V022 의 Phase 0 게이트 통과 시점의 inspect_template 데이터 (운영에서 22±5 충족했는지)
- V025 의 적용 시점 + 당시 pjt_equip 존재 여부
- V100 적용 후 운영 tb_document 의 contract_id 컬럼 존재 여부 + V100 의 trigger loop 가 운영에서 어떻게 통과했는지 (당시 tb_contract 존재 여부)

---

## 4. 후속 sprint 백로그 권고 (codex v2 권고 5 재구성)

각 결함 해소를 위한 sprint 분리 권고:

| sprint id | 범위 | 분량 | 의존 |
|----------|------|------|------|
| `vsql-fresh-init-skip-list` | **#1, #2, #7, #8** setup-guide §2-2 skip-list 명시 + 사유 주석 (#7 V023 은 V022 cascade 로 함께 skip) | 소 | 0 |
| `vsql-runid-psql-variables` | **#3, #4, #5 + (선택) V022 도 :run_id 사용 (1-G-1)** setup-guide for-loop 에 `-v run_id` 추가 | 소 | 0 |
| `vsql-V005-ordering` | #6 V005 두 파일 ordering 정리. **옵션 E (manifest)** 우선 → 장기적으로 옵션 B (통합) | 소 (E) ~ 중 (B) | 운영DB history 확인 선행 |
| `vsql-V025-idempotent` | #8 의 옵션 A (V025 가드 추가) — 만약 skip 이 부적합하면 | 소 | 운영DB history 확인 |
| `vsql-V100-legacy-contract-cleanup` | **#9 + 1-G-2** V100 의 legacy contract block 전체 정리 (`tb_document.contract_id`, `idx_doc_contract`, line 388-390 trigger loop 의 `tb_contract`, `tb_contract_participant/target`, `CREATE INDEX IF NOT EXISTS` 일괄 보강 포함) | **중~대** | 운영DB history + 기획 검토 선행 |

**그룹 처리 가능**: `vsql-fresh-init-skip-list` + `vsql-runid-psql-variables` 는 단일 setup-guide 보강 sprint 로 묶어도 됨 (`vsql-fresh-init-procedure-fix`). **V023 처리 방침 (V022 cascade skip vs check_section_mst 별도 선행 DDL) 결정도 본 sprint 안에서 처리**.

**중요 변경 (codex v2 권고 5)**:
- `vsql-fresh-init-skip-list` 에 #7 V023 추가 (V022 cascade)
- `vsql-runid-psql-variables` 에 V022 의 `:run_id` 도 함께 검토 (1-G-1)
- `vsql-V100-tb_document-cleanup` → `vsql-V100-legacy-contract-cleanup` 으로 범위 확장 (단순 contract_id 가드 → legacy contract block 전체)

---

## 5. `phase2-vsql-external-deps` 재진입 조건 (현재 PAUSED) — codex v2 권고 6 보강

본 분석 결과로 `phase2-vsql-external-deps` 의 NFR-2-a 게이트 ("V*.sql 전체 실패 = 0") 의미를 재평가:

### 5-1. 재개 옵션 비교

- **즉시 재개 가능 옵션**: 본 sprint 의 NFR-2-a 를 "본 sprint scope (3 INSERT) 적용 후 phase2.sql rc=0" 로 한정. V*.sql 9건은 별도 sprint 로 위임. 기획서 v3 개정 후 재개.
- **사전 처리 후 재개 옵션** (권장): `vsql-fresh-init-procedure-fix` (skip-list + run_id) 먼저 처리 → V*.sql 의 즉시 가능 결함 **(#1/#2/#3/#4/#5/#7/#8) 해소 — 7건** → **2건 (#6 V005 / #9 V100) 만 남음** → 그 2건은 명시적 skip-list 로 NFR-2-a 에서 제외 + 별도 sprint 로 위임 + 본 sprint 재개. ※ V025 (#8) 는 procedure-fix 의 skip-list 에 포함되므로 잔여 X (v3 정정).
- **완전 처리 후 재개 옵션**: 5개 후속 sprint 모두 처리 → V*.sql rc 0 → 본 sprint 재개. 가장 깨끗하지만 분량 큼 (2~4주+).

### 5-2. NFR-2-a 의 skip-list / allowlist 의미 (codex v2 권고 6 + v3 정정)

기획서 v3 개정 시 NFR-2-a 의 fresh-init 통과 정의:

```markdown
NFR-2-a (v3 정정): fresh-init 절차 `psql -v ON_ERROR_STOP=1 -f phase1 → sigungu → phase2 → V*.sql (skip-list 적용 for-loop)` 의 모든 stmt rc=0.

여기서 "V*.sql skip-list 적용 for-loop" 의 의미:
- setup-guide §2-2 의 V*.sql for-loop 는 사전순 실행이지만, **본 sprint 시점 기준 skip-list 에 명시된 V*.sql 은 실행에서 제외** (case ... in ... continue 패턴)
- skip-list 에 포함된 V*.sql 은 다음 두 종류 모두 포함:
  (a) **영구 skip** — 운영DB 적용 완료 + fresh-init 에 의미 없음 (V021_rollback_data, V022, V023 cascade, V025)
  (b) **임시 skip** (별도 sprint 처리 대기) — 별도 sprint 로 해소 후 skip 해제 (V005_add_surveying_wage_rates, V100_work_plan_performance_tables)

NFR-2-a 측정: skip-list 적용 후 for-loop 의 모든 V*.sql 이 rc=0. 즉 "skip-list 외 V*.sql 은 모두 rc=0".

본 sprint (phase2-vsql-external-deps) 시점 기준 skip-list:
| 파일 | 분류 | 사유 | 처리 sprint | 종류 |
|------|------|------|-----------|------|
| V021_rollback_data.sql | intentional abort | fresh-init 에 backup 부재가 정상 | vsql-fresh-init-procedure-fix | (a) 영구 |
| V022_inspect_comprehensive_redesign.sql | intentional abort (Phase 0) | fresh-init 에 inspect 데이터 0 — 게이트 트립 정상 | vsql-fresh-init-procedure-fix | (a) 영구 |
| V023_inspect_category_master.sql | V022 cascade | check_section_mst (V022 산출물) 의존 — V022 skip 의 자동 동반 | vsql-fresh-init-procedure-fix | (a) 영구 |
| V025_drop_pjt_equip.sql | 일회성 cleanup | pjt_equip 이미 제거된 환경 — 재실행 의미 없음 | vsql-fresh-init-procedure-fix | (a) 영구 |
| V005_add_surveying_wage_rates.sql | 단순 ordering | V005 두 파일 version 충돌, 별도 sprint 처리 대기 | vsql-V005-ordering | (b) 임시 |
| V100_work_plan_performance_tables.sql | legacy contract block | V100 자체 정리 미완, 별도 sprint 처리 대기 | vsql-V100-legacy-contract-cleanup | (b) 임시 |

V019/V020/V021_users_masking_regression_fix 는 setup-guide for-loop 의 `-v run_id=...` 추가로 통과 가능 → skip-list 에 포함하지 않음.
```

> **v3 정정 핵심**: V025 가 (b) 임시 skip → (a) 영구 skip 으로 변경. allowlist 잔여는 V005/V100 의 **2건만** (V025 제외).

### 5-3. V023 cascade 처리 방침 (codex v2 권고 6)

V023 은 V022 의 cascade 의존이므로 **단순 allowlist 제외 X**. `vsql-fresh-init-procedure-fix` 안에서 다음 중 하나로 처리:
- (a) V022 + V023 둘 다 skip-list (점검 모듈 fresh-init 환경 제한 수용)
- (b) `check_section_mst` 를 phase2 또는 별도 선행 DDL 로 추출 → V023 자체 통과 가능

**권고**: `vsql-fresh-init-procedure-fix` 기획 시 (a)/(b) 중 결정 필요. (a) 가 단순.

### 5-4. 권고 (v3 정정)

**사전 처리 후 재개 옵션** — `vsql-fresh-init-procedure-fix` sprint 1개로 **즉시 가능 7건 (#1/#2/#3/#4/#5/#7/#8) 해소** → 본 sprint 의 NFR-2-a 를 v3 로 개정 (skip-list 명시: 영구 4건 + 임시 2건) → 본 sprint 재개. 나머지 **2건 (#6/#9)** 은 별도 sprint 로 위임 (skip-list 임시 (b) 로 NFR-2-a 통과).

---

## 6. 측정 산출물 보존 정보 (재진입 시 재사용)

| 항목 | 위치 | 비고 |
|------|------|------|
| BASELINE_N | `/tmp/baseline_N.txt` (값=127) | 본 sprint Step 1-5 측정 결과 |
| ephemeral DB sw_u1/u2/u2b/u2_baseline | `localhost:25880` | DROP 안 됨, 재진입 시 재사용 가능 |
| V*.sql 적용 로그 | `/tmp/u2_v_all.log` | 9건 ERROR 컨텍스트 — 본 보고서 출처 |
| 운영 server.log baseline 부팅 결과 | `server.log` (가장 최근 entry) | N=127, ERROR 0 |

---

## 7. 작성 메타

- 작성일: 2026-05-11
- 작성자: 본 sprint 구현 세션 중 PAUSE 후 분석
- 검토: 미검토 (codex 또는 사용자 검토 후 후속 sprint 진입 권고)
- 관련 문서:
  - `docs/product-specs/phase2-vsql-external-deps.md` (PAUSED v2)
  - `docs/exec-plans/phase2-vsql-external-deps.md` (PAUSED v1.2)
  - `docs/references/setup-guide.md` §2-2 (보강 대상)
  - `docs/PLANS.md` §2-b (후속 sprint 등록 대상)
