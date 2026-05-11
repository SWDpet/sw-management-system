---
tags: [plan, sprint, schema, init-ordering]
sprint: "phase2-V018-init-ordering"
status: draft-v3.3
created: "2026-04-29"
---

# [기획서] phase2 ↔ V018 초기화 순서 의존성 해소

- **작성팀**: 기획팀
- **작성일**: 2026-04-29
- **선행 스프린트 (완료)**: `phase1-ddl-formalization` (commit `841a112`, 2026-04-27 완료)
- **선행 스프린트 (의존, 미완료)**: `dbinitrunner-dollar-quote-aware` — DbInitRunner.java 의 SQL splitter 가 `$$ ... $$` 블록을 단일 statement 로 보존하도록 수정. **본 스프린트 진입 전 완료 필수** (R-7 해소 조건). 별도 스프린트로 분리한 사유: §5-1 ⓒ 참조.
- **근거**: phase1-ddl-formalization 스프린트 Step 8 T7 (T-매트릭스 — fresh-init 환경 phase2.sql:60 stop 발견, out-of-scope 분리 → 본 스프린트로 후속)
- **상태**: **구현 중 v3.3 amendment** — codex 3차 ⭕ + 사용자 최종승인 (v3, 2026-05-11) → 구현 Step 3-2 (T1) 진행 중 phase2.sql 의 **out-of-scope forward-reference 2건 표면화** → v3.1/v3.2/v3.3 amendment 진행 (NFR scope 한정 + §1 목표·§2 시나리오·§8 산출물·FR-4-B·NFR-5 표현 정합). codex 4차 ⚠ → 5차 ⚠ → 6차 ⚠ (잔존 1건 정정 후 7차 ⭕ 기대) → 사용자 amendment 승인 + Step 3 측정 재개 대기.
- **UI 키워드**: 0건 (백엔드 전용 — 디자인팀 자문 skip)
- **개정 이력**:
  - v1 (2026-04-29): 초안. B안 (UNIQUE 선이동 + DO 가드) 채택, 4안 비교 + codex 사전 협의 의견 반영
  - v2 (2026-04-29): codex 1차 검토 반영. (a) R-7 — DbInitRunner SQL splitter 의 dollar-quote 미인식 → **선행 스프린트 분리** (`dbinitrunner-dollar-quote-aware`), 본 스프린트는 그 위에 빌드. (b) FR-1-D 제거 (핵심 가드 아님, UNIQUE 자체가 가드). (c) §5-7 BEGIN/COMMIT 트랜잭션 경계 정책 명시 + NFR-3-x 보강. (d) R-6 "무시" → "out-of-contract" 로 변경. (e) §5-1 갈래(E1/E2/E3) 의사결정 추가.
  - v3 (2026-05-11): codex 2차 검토 반영 (⚠ 수정필요 → 4건 + 관련 부분충족 보강). (a) **NFR-3-x "선택/권장" → 필수(MUST) 승격** — 트랜잭션 경계 부재의 보강책이라면 게이트여야 함. (b) **§1.5 Entry Criteria 섹션 신설** — 선행 스프린트 `dbinitrunner-dollar-quote-aware` 완료 커밋/검증 결과 없이는 개발계획 작성·구현 진입 불가 (hard gate). (c) **"silent ON CONFLICT 추론 실패" 표현 정정** — 실제로는 PG `42P10` 으로 stop. 문구: "ON CONFLICT 추론 실패 또는 UNIQUE 미등록 상태를 INSERT 전 fast-fail". (d) **§5-7 자연복구 주장 한정** — "일시적 실행 중단이며 데이터/스키마 불일치 없는 경우" 로 좁히고, 지속성 원인 (데이터 중복/NULL/수동 오염/테이블 정의 불일치) 시 NFR-3-x fast-fail + 사후 수동 점검 필요 명시. 부수: NFR-2 (DbInitRunner 경로 cover 명시), NFR-7 (선행 완료 검증 기준), R-7 (Entry Criteria 참조).
  - v3.1 (2026-05-11, **구현 중 amendment**): T1 (Step 3-2) 실행 중 **out-of-scope forward-reference 2건 표면화** → NFR-2/NFR-3 측정 scope 명문화 amendment. 발견 사실 §10 Discovery 섹션에 기록 + 후속 sprint `phase2-tb_ops_doc-forward-ref` 추천. 본 스프린트 자체는 우리 변경 (UNIQUE 선이동 + NFR-3-x 게이트) 이 정상 작동함을 확인 (NFR-3-x NOTICE PASS, INSERT 0 5 양 테이블, count=5/distinct=5/UNIQUE 1 모두 충족) → 우리 scope 내에서는 PASS. NFR-2(a) `rc=0` 와 NFR-3(d) `ERROR 0` 의 측정 범위를 "본 스프린트 변경 surface 한정 (line 19, 43-94, 98, 109)" 으로 한정.
  - v3.2 (2026-05-11, codex 4차 ⚠ 반영): v3.1 의 NFR scope 한정만으로는 §1 목표·§2 시나리오·§8 산출물의 "fresh-init 무중단 실행 / rc=0" 표현과 모순 (escape hatch 우려). 4건 정정: (a) §1 목표 1 을 "phase2.sql 의 V018 UNIQUE 의존 구간 무중단 실행" 으로 좁히고 full fresh-init 은 후속 sprint 완료 후 목표로 재배치, (b) §2 시나리오의 "phase2.sql ✅ 무중단 실행" 을 "본 스프린트 surface 통과 (line 60/70 stop 회귀 차단). full fresh-init 은 후속 sprint 후" 로 정정, (c) §8 T1/T2 rc=0 기대치를 scope 기준으로 재작성, (d) §2-3 운영자 영향 0 의 "phase2.sql 재실행 무해" 를 "참조 대상 테이블이 이미 존재하는 운영DB 상태에서" 로 한정.
  - v3.3 (2026-05-11, codex 5차 ⚠ 반영): v3.2 의 잔존 broad guarantee 4건 일제 정리. (a) §2-2 DR 시나리오의 "fresh-init 절차 보장" → "V018 UNIQUE 의존 구간 stop 해소만 보장. phase2 후반부 forward-reference stop 가능 — 후속 sprint 완료 후 full schema 복원" 으로 한정. (b) FR-4-B 의 "phase2.sql 통과 보장" → "line 60/70 V018 UNIQUE 의존 구간 통과 보장, full phase2 통과는 후속 sprint 이후" 로 한정. (c) NFR-5 parenthetical 에 "참조 대상 테이블 (tb_org_unit, tb_work_plan) 이 이미 존재하는 운영DB 상태에서" 조건 추가. (d) §9 다음 절차 stale 항목 (codex 3차 검토, 선행 스프린트 시작) 제거 + 현 단계 (codex 5차 ⚠ → v3.3) 반영.

---

## 1. 배경 / 목표

### 배경 — fresh-init 환경에서 phase2.sql 중단

phase1-ddl-formalization 스프린트 Step 8 ephemeral 검증 (`localhost:25880`, T-매트릭스) 중 **T7 phase1→phase2→V*.sql 전체 fresh-init** 만 실패. 원인:

#### 의존 #1 — `tb_process_master`
```sql
-- src/main/resources/db_init_phase2.sql:54-60
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) VALUES
('UPIS', '도시계획정보체계용 GIS SW 유지관리', 1),
...
ON CONFLICT (sys_nm_en, process_name) DO NOTHING;  -- ← 의존
```
`(sys_nm_en, process_name)` UNIQUE 제약 `uq_process_master_sys_name` 은 `swdept/sql/V018_process_master_dedup.sql:67-73` 에서만 생성:
```sql
ALTER TABLE tb_process_master
  ADD CONSTRAINT uq_process_master_sys_name
  UNIQUE (sys_nm_en, process_name);
```

#### 의존 #2 — `tb_service_purpose`
```sql
-- src/main/resources/db_init_phase2.sql:64-70
INSERT INTO tb_service_purpose (sys_nm_en, purpose_type, purpose_text, sort_order) VALUES
...
ON CONFLICT (sys_nm_en, purpose_type, md5(purpose_text)) DO NOTHING;  -- ← 의존
```
표현식 UNIQUE INDEX `uq_service_purpose_sys_type_md5` 는 `V018:75-76` 에서만 생성:
```sql
CREATE UNIQUE INDEX IF NOT EXISTS uq_service_purpose_sys_type_md5
  ON tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text));
```

#### 실행 순서 — V018 이 phase2 보다 늦음
신규 환경 초기화 절차 (`docs/references/setup-guide.md §2-2`):
```
phase1 → phase1_sigungu → phase2 → V*.sql (V001 → ... → V018 → ...)
```
즉 phase2 실행 시점에 V018 의 UNIQUE 가 아직 없음 → PostgreSQL `42P10 (invalid_column_reference)` "there is no unique or exclusion constraint matching the ON CONFLICT specification" → phase2.sql:60 에서 stop.

### 운영DB 미발견 사유
운영DB 는 phase2/V018 이 점진 적용으로 이미 양쪽 모두 존재 → fresh-init 흐름이 발생하지 않음. T7 같은 신규 환경(또는 DR) 에서만 표면화.

### 목표
1. **(v3.2 한정)** **phase2.sql 의 V018 UNIQUE 의존 구간 무중단 실행** — phase2.sql:60 (`tb_process_master` ON CONFLICT) 와 :70 (`tb_service_purpose` ON CONFLICT) 의 fresh-init stop 회귀 차단. NFR-3-x 게이트 통과 확인. **full fresh-init `phase1 → phase1_sigungu → phase2 → V*.sql` 전체 rc=0 은 후속 sprint `phase2-tb_ops_doc-forward-ref` 완료 후 목표** (§10/§11 참조).
2. **운영DB 무영향 (쓰기 0)** — 본 스프린트 결과물은 신규 환경(또는 DR) 에서만 효과 발생. 운영DB 는 이미 V018 적용 완료 → 재실행 시 멱등 가드로 무해.
3. **V018 마이그레이션 무수정 원칙 유지** — V018 은 운영 적용 완료된 Flyway 마이그레이션. checksum 변경 금지.
4. **phase1 SSoT 경계 유지** — phase1 = 운영 schema 정식화 / phase2 = 증분 DDL+seed. 본 스프린트는 phase2 내부에 한정.

### 비목표
- V018 자체 수정 또는 deprecated 처리 — 범위 외.
- phase2/V018 SQL 의 다른 조항 (점검내역서 테이블 등) 정리 — 범위 외.
- Flyway 도입 또는 마이그레이션 트랙 통합 — 범위 외.
- 운영DB 의 phase2/V018 재적용 — 본 스프린트 결과물은 신규 환경 한정.

### 1.5. Entry Criteria (v3 — hard gate)

본 스프린트는 다음 조건이 **모두** 충족된 시점에만 개발계획 작성·구현 진입 가능. 미충족 상태에서 진입 시 codex 검증·사용자 승인 모두 자동 반려.

| EC | 조건 | 검증 방법 |
|----|------|----------|
| EC-1 | 선행 스프린트 `dbinitrunner-dollar-quote-aware` **완료 commit 존재** (master 머지) | `git log --oneline master | grep dbinitrunner-dollar-quote-aware` 1건 이상 |
| EC-2 | 선행 스프린트의 ephemeral 검증 결과가 **PASS** 로 기록 (DbInitRunner 의 `DO $$ ... $$` 단일 stmt 처리 단위테스트 + 통합테스트 rc=0) | `docs/exec-plans/dbinitrunner-dollar-quote-aware.md` 검증 결과 섹션 |
| EC-3 | DbInitRunner 의 SQL splitter 가 dollar-quote-aware 임을 코드 레벨에서 확인 | `DbInitRunner.java` 의 splitter 메서드에 dollar-quote 처리 분기 + 단위테스트 (`DbInitRunnerTest`) 존재 |
| EC-4 | 본 기획서 v3 이상 + codex 2차 이상 검토 ⭕ | 본 문서 §9 체크리스트 |

**진입 차단 효과**:
- EC-1~3 미충족: 본 스프린트 개발계획서 (`docs/exec-plans/phase2-V018-init-ordering.md`) 작성 금지. 작성 시도 시 codex 검토에서 ❌ 반려.
- EC-4 미충족: 본 기획서 자체가 아직 미완. codex 추가 검토 후 v4 진입.

**근거**: codex 2차 검토 (2026-05-11) — "FR-1-E의 DbInitRunner 선행 의존이 있음에도 구현 진입 차단 조건이 별도 게이트로 고정되지 않음" → Entry Criteria 로 hard gate 명문화.

---

## 2. 사용자 시나리오

### 2-1. 신규 개발자 PC 셋업 (목표 시나리오 — v3.2 단계별 분리)

**본 스프린트 완료 시점 (`phase2-V018-init-ordering`)**:
1. git clone 후 PostgreSQL 컨테이너/클러스터 기동
2. `psql -f db_init_phase1.sql` → 19 테이블 + 마스터 시드 ~54건
3. `psql -f db_init_phase1_sigungu.sql` → sigungu_code 279건
4. `psql -f db_init_phase2.sql` → **본 스프린트 surface 통과** (line 60/70 stop 회귀 차단, NFR-3-x NOTICE PASS, INSERT 0 5). 단 line 464 부근의 `tb_org_unit`/`tb_work_plan` forward-reference 로 후반부 stop 발생 가능 → 후속 sprint `phase2-tb_ops_doc-forward-ref` 책임.
5. `psql -f swdept/sql/V001..V*.sql` 순차 실행 → V018 도 멱등 가드로 무해 통과 (단계 4 의 부분 실패가 V*.sql 진입에 영향 줄 수 있음 — 후속 sprint 후 완전 통과 보장)
6. 서버 기동 → DbInitRunner per-stmt try/catch 로 phase2 후반부 ERROR 무해. 모든 모듈 정상 (라이선스 제외).

**후속 sprint `phase2-tb_ops_doc-forward-ref` 완료 시점**: 4단계 도 완전 통과 (full fresh-init rc=0).

### 2-2. 재해 복구 / DR (v3.3 한정)
- **본 스프린트 완료 시점**: 운영DB 손실 시 phase1 → phase2 → V*.sql 만으로 schema 복원 시도 시, V018 UNIQUE 의존 구간 (line 60/70) stop 회귀는 해소. 단 phase2 후반부의 `tb_ops_doc` forward-reference (line 439/443) 로 인한 stop 가능 → 후속 sprint `phase2-tb_ops_doc-forward-ref` 완료 전에는 fresh-init 완전 자동 복원 미보장 (수동 보정 필요).
- **후속 sprint 완료 시점**: phase1 → phase2 → V*.sql 만으로 fresh-init schema 완전 자동 복원 가능 — 본 sprint 가 그 첫 디딤돌.

### 2-3. 운영자 (영향 0 — v3.2 한정)
- 운영DB 는 본 스프린트 결과물에 노출되지 않음. 기존 사용자 경험 동일.
- 운영DB 에 phase2.sql 을 **재실행**할 일이 발생하더라도 (DbInitRunner 등): **참조 대상 테이블 (`tb_org_unit`, `tb_work_plan`) 이 이미 운영DB 에 존재하는 상태에서** 본 스프린트 변경 (UNIQUE 선이동 + NFR-3-x 게이트) 은 멱등 가드 + `IF NOT EXISTS` 로 안전. 만약 운영DB 가 어떤 사유로 위 테이블 미존재 상태라면 phase2 후반부 stop 가능 (DbInitRunner 는 catch 로 무해, psql -f + ON_ERROR_STOP 만 영향).

---

## 3. 기능 요건 (FR)

### FR-1. `db_init_phase2.sql` UNIQUE 제약·INDEX 선이동

phase2.sql 의 두 INSERT 보다 **앞** 시점에 UNIQUE 제약·INDEX 를 멱등 가드와 함께 선이동.

| ID | 내용 |
|----|------|
| FR-1-A | `tb_process_master` UNIQUE 제약 `uq_process_master_sys_name` 을 `db_init_phase2.sql` 의 `CREATE TABLE IF NOT EXISTS tb_process_master (...)` 직후, INSERT 보다 **앞**에 `DO $$ ... $$` 멱등 가드 블록으로 추가. |
| FR-1-B | `tb_service_purpose` UNIQUE INDEX `uq_service_purpose_sys_type_md5` 를 `CREATE TABLE IF NOT EXISTS tb_service_purpose (...)` 직후, INSERT 보다 **앞**에 `CREATE UNIQUE INDEX IF NOT EXISTS` 로 추가 (PG 9.5+ 멱등 보장). |
| FR-1-C | 멱등 가드 블록은 V018 의 (4) 항목과 **동일한 식별자 / 동일한 컬럼 / 동일한 표현식**. V018 이 나중에 실행되더라도 `IF NOT EXISTS` 또는 `pg_constraint` lookup 에 의해 재진입 무해. |
| FR-1-D | 본 변경은 V018 의 NOT NULL 전환(V018 (3) 절)을 **선행하지 않음** — V018 은 그대로 phase2 다음에 실행되어야 함. fresh-init 시 phase2 단계에서 NOT NULL 부재 상태로 INSERT 5건 통과 가능 (NULL 값 미포함이므로 무해). NOT NULL 전환은 V018 책임 영역으로 유지. |
| FR-1-E (v2) | DbInitRunner 의 dollar-quote 처리 의존성: 본 스프린트의 `DO $$ ... $$` 블록은 **선행 스프린트 `dbinitrunner-dollar-quote-aware`** 가 완료된 후에만 서버 부팅 경로(DbInitRunner)에서 안전. psql -f 직접 실행 경로는 선행과 무관하게 항상 안전. 본 스프린트의 검증 (NFR-2/T2/T3) 은 양 경로 모두 cover. |

### FR-2. V018 무수정 보장

| ID | 내용 |
|----|------|
| FR-2-A | `swdept/sql/V018_process_master_dedup.sql` 1바이트도 변경 금지. Flyway checksum 보존. |
| FR-2-B | V018 (4) 항목 (`DO $$ ... pg_constraint EXISTS ... ALTER TABLE ADD CONSTRAINT ...`, `CREATE UNIQUE INDEX IF NOT EXISTS`) 은 phase2 에서 이미 추가된 상태에서도 안전한 멱등 패턴 — 별도 패치 불필요. |
| FR-2-C | V018 의 dedup DELETE / NOT NULL 전환 / 사후검증 DO 블록은 운영DB 에서 이미 적용 완료, fresh-init 환경에서는 데이터가 깨끗(distinct=5, NULL=0)하므로 양쪽 모두 안전 통과. |

### FR-3. `db_init_phase2.sql` 헤더 주석 보강

| ID | 내용 |
|----|------|
| FR-3-A | 파일 상단 주석에 본 스프린트 이력 1~2줄 추가: "phase2-V018-init-ordering (2026-04-29) — V018 의 UNIQUE 제약·INDEX 를 phase2 의 INSERT 앞에 선이동. V018 무수정." |
| FR-3-B | INSERT 두 블록의 라인 주석 (`-- V018 (process-master-dedup 2026-04-20) 이후: ...`) 은 보존하되, "phase2 에서 선이동됨" 1줄 보강. |

### FR-4. 문서 갱신

| ID | 내용 |
|----|------|
| FR-4-A | `docs/PLANS.md` §2-b 후속 백로그에서 `phase2-V018-init-ordering` 항목을 §2-c (또는 §2-b 하위) 의 **완료 스프린트** 로 이동. 산출물 / 검증 / 커밋 요약 포함. |
| FR-4-B (v3.3 한정) | `docs/references/setup-guide.md` §2-2 의 신규 환경 초기화 절차 본문 변경 없음 (순서 그대로) — 단 본 스프린트 적용 후 **line 60/70 V018 UNIQUE 의존 구간 통과는 보장**, full phase2 통과는 후속 sprint `phase2-tb_ops_doc-forward-ref` 이후로 정정한 1줄 보강. |
| FR-4-C | (선택) `docs/exec-plans/phase1-ddl-formalization.md` Step 8 T7 결과 줄에 "후속 스프린트 phase2-V018-init-ordering 에서 해소" 1줄 보강. |

### 범위 외
- V018 자체 수정 (FR-2 명시).
- phase2 의 점검내역서·users.field_role·career_years 관련 조항 수정.
- 운영DB 에 phase2 재적용 또는 V018 재실행.
- DbInitRunner 동작 변경.
- Java/Entity 코드 변경.

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공 (본 스프린트는 SQL 만 변경, Java 컴파일 영향 0 예상). |
| NFR-2 (v3.1 scope 한정) | ephemeral 클러스터 (`localhost:25880`, phase1 스프린트에서 보존) **fresh-init 통과 — 양 경로 모두 cover, 본 스프린트 scope 한정**: <br>(a) **psql -f 경로 (scope 한정)**: `psql -f db_init_phase1.sql` → `psql -f db_init_phase1_sigungu.sql` → `psql -f db_init_phase2.sql` 실행 시, **본 스프린트 변경 surface (phase2.sql line 19, 43-94, 98, 109) 가 ERROR 없이 통과**. 구체적 측정: <br>&nbsp;&nbsp;(a-i) phase2.sql:60 (tb_process_master ON CONFLICT) 와 :70 (tb_service_purpose ON CONFLICT) **stop 없음** (T7 stop 회귀 차단)<br>&nbsp;&nbsp;(a-ii) NFR-3-x 게이트 NOTICE `PASS [phase2-V018-init-ordering NFR-3-x]: ...` 출력<br>&nbsp;&nbsp;(a-iii) 두 INSERT 모두 `INSERT 0 5` (1차) 또는 `INSERT 0 0` (2차+, 멱등) 응답<br>&nbsp;&nbsp;(a-iv) **out-of-scope ERROR (예: phase2.sql:464 의 `tb_org_unit does not exist`, `tb_work_plan does not exist` 등 forward-reference) 는 측정 제외** — 후속 sprint `phase2-tb_ops_doc-forward-ref` 책임. <br>(b) **DbInitRunner 경로 (서버 부팅)**: `bash server-restart.sh` → 부팅 로그에 본 스프린트 변경 surface 관련 ERROR 0, `DO $$ ... $$` 블록이 단일 stmt 로 처리됐음을 splitter 로그 또는 EXCEPTION 부재로 확인. DbInitRunner 는 per-stmt try/catch 라 out-of-scope ERROR 는 debug 로그로만 남고 무해. 선행 스프린트 `dbinitrunner-dollar-quote-aware` 완료 (EC-1~3) 가 본 NFR 의 전제. |
| NFR-3 (v3.1 (d) scope 한정) | **재실행 멱등성 (3회 연속)** — phase2.sql 단독 3회 연속 실행 후:<br>(a) `tb_process_master` count = 5, distinct(`sys_nm_en, process_name`) = 5, 중복 그룹 0<br>(b) `tb_service_purpose` count = 5, distinct(`sys_nm_en, purpose_type, md5(purpose_text)`) = 5, 중복 그룹 0<br>(c) UNIQUE 제약·INDEX 존재 (`uq_process_master_sys_name`, `uq_service_purpose_sys_type_md5`) 각 1건<br>(d) **본 스프린트 변경 surface 관련 ERROR 0** (`uq_process_master_sys_name`, `uq_service_purpose_sys_type_md5`, NFR-3-x 게이트 메시지 grep). out-of-scope ERROR (`tb_org_unit does not exist`, `tb_work_plan does not exist`, `tb_ops_doc.org_unit_id` FK 관련 등) 는 측정 제외 — 후속 sprint `phase2-tb_ops_doc-forward-ref` 책임. NOTICE/INFO 는 허용. |
| NFR-3-x (v3 — **MUST**) | **UNIQUE 적용 직후 검증 게이트 (필수)** — phase2.sql 안에서 두 INSERT 보다 **앞** 시점에, UNIQUE 제약·INDEX 가 실제로 등록됐는지 확인하는 DO 블록 내 `pg_constraint`/`pg_indexes` lookup, 미존재 시 `RAISE EXCEPTION` 으로 fast-fail. **INSERT 가 ON CONFLICT 추론 실패 (`42P10`) 로 stop 되거나 UNIQUE 미등록 상태로 INSERT 진행되는 두 회귀를 INSERT 전 단계에서 차단**. v2 의 "선택/권장" 표현은 v3 에서 제거 — 트랜잭션 경계 부재 (§5-7) 의 보강책이라면 게이트여야 함. (구현 세부는 개발계획서) |
| NFR-4 | **V018 재진입 무해성** — phase2.sql 적용 후 V018 을 추가 실행해도 (a) UNIQUE 제약·INDEX 중복 생성 시도 stop 없음, (b) NOT NULL 전환 멱등 통과, (c) DELETE 0건 (이미 distinct=5), (d) 사후검증 DO 블록 PASS. |
| NFR-5 (v3.3 한정) | **운영DB 영향 0** — 본 스프린트는 운영DB 에 어떤 SQL 도 실행하지 않음. 운영DB 의 phase2/V018 상태 변동 0. (별도 운영자 결정으로 phase2.sql 재실행이 발생하더라도 **참조 대상 테이블 `tb_org_unit`, `tb_work_plan` 이 이미 운영DB 에 존재하는 상태에서** 본 스프린트 변경 (UNIQUE 선이동 + NFR-3-x 게이트) 은 멱등 가드로 무해. 위 테이블 미존재 상태라면 phase2 후반부 stop 가능 — 단 DbInitRunner per-stmt try/catch 로 부팅 무해.) |
| NFR-6 | **Flyway checksum 무변경** — V018 파일 SHA256 변동 0. |
| NFR-7 (v3 보강) | **회귀 스모크 + 선행 완료 검증** — (a) 서버 부팅 (`bash server-restart.sh`) 시 ERROR 0, 점검내역서/사업관리/견적서 등 기존 기능 정상 동작. (b) **부팅 로그에서 DbInitRunner 가 phase2.sql 의 `DO $$ ... $$` 블록을 단일 stmt 로 실행했는지 확인** (PSQLException 또는 "syntax error at or near \";\"" 류 메시지 0건). EC-3 (DbInitRunner splitter 가 dollar-quote-aware) 가 실제 동작에서 효력 발휘했음을 입증. |

---

## 5. 의사결정 / 우려사항

### 5-1. 4안 비교 — B안 채택 + DbInitRunner 갈래(E1/E2/E3) — E3 채택

#### 1차 의사결정: 4안 비교

| 안 | 요지 | 채택 여부 |
|----|------|-----------|
| A | phase2 INSERT 10건을 V018 로 이관 | ❌ V018 checksum 변경 — Flyway 무결성 위배 |
| **B** | **phase2 에 UNIQUE 선이동 + 멱등 가드, V018 dedup-only** | ✅ **채택** — 운영 무해, V018 무수정, fresh-init 직접 해소 |
| C | ON CONFLICT 제거 → INSERT WHERE NOT EXISTS / md5() 직접 비교 | ❌ seed SQL 복잡도 ↑, 회귀 검증 범위 ↑ |
| D | V018 내용을 phase2 에 흡수, V018 deprecated 화 | ❌ V018 의미 변경 + Flyway 무결성 위배 |

**codex 검토 의견 (2026-04-29 사전 협의)**: B안 동의. "A/D는 운영 적용 완료된 V018 checksum 변경 또는 의미 변경 위험이 커 Flyway 무결성 측면에서 부적합. C는 V018 무수정 장점은 있으나 seed SQL 이 복잡해지고 회귀 검증 범위가 커짐. B는 운영DB 에는 IF NOT EXISTS 로 무해하고, fresh-init 에서는 phase2 단계에서 필요한 UNIQUE 를 선확보해 중단 원인을 직접 제거."

#### 2차 의사결정 (v2): DbInitRunner dollar-quote 미인식 (R-7) 해소 갈래

codex 1차 검토에서 발견: `DbInitRunner.java:55-71` 의 SQL splitter 가 단순 세미콜론 기준 → `DO $$ BEGIN ... ; ... ; END $$` 블록을 잘라서 실행 → 서버 부팅 경로에서 깨짐.

| 갈래 | 요지 | 채택 여부 |
|----|------|----|
| E1 | DbInitRunner splitter fix 를 본 스프린트에 포함 (단일 PR) | ❌ 본 스프린트 범위가 SQL → Java 회귀 surface 까지 확장. 책임 분리 약화 |
| E2 | DO 블록 회피 → phase2 는 `CREATE UNIQUE INDEX IF NOT EXISTS` 만 사용 (이름 다르게) | ❌ 동일 컬럼 인덱스 잔존 (의미 중복). V018 ADD CONSTRAINT 가 동일 이름 INDEX 와 충돌 위험 |
| **E3** | **선행 스프린트 분리 — `dbinitrunner-dollar-quote-aware`. 본 스프린트는 의존성으로 후행** | ✅ **채택** — 책임 분리, 회귀 진단 용이, 롤백 단위 명확. 1차 스프린트는 동작 변화 0 (현재 phase2.sql 에 DO 블록 부재) |

**E3 채택 사유** (사용자 판단 + Claude 검토):
- splitter fix 는 phase2.sql 의 **모든 미래 DO 블록** 에 영향 — 본 스프린트보다 책임 범위 큼
- 두 스프린트가 한 commit 에 묶이면 향후 회귀 시 git bisect 가 어려움 ("ordering 에서 깨졌나, splitter 에서 깨졌나" 구분 불가)
- 1차 스프린트는 단위테스트만 추가될 뿐 phase2.sql 에 DO 블록 없는 현 시점 동작 변화 0 — 부담 작음
- 1차 완료 후 본 스프린트 진입 시 DO 블록 사용 자유 + V018 식별자 동일성 보장

### 5-2. UNIQUE 제약 vs UNIQUE INDEX — V018 과 동일하게 분리 유지
- `tb_process_master`: `ALTER TABLE ... ADD CONSTRAINT ... UNIQUE (sys_nm_en, process_name)` — V018 (4) 와 동일 명/동일 컬럼.
- `tb_service_purpose`: `CREATE UNIQUE INDEX IF NOT EXISTS uq_service_purpose_sys_type_md5 ON tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text))` — V018 (4) 와 동일.
- 표현식 UNIQUE 는 `ADD CONSTRAINT UNIQUE` 구문이 PG 표준 밖이라 INDEX 방식 유지 (process-master-dedup 기획서 §FR-2 결정 그대로 계승).

### 5-3. 멱등 가드 패턴 — phase1 스프린트 검증 패턴 채택
phase1-ddl-formalization 에서 35건 EXCEPTION 블록으로 멱등성 보장한 패턴 그대로:
```sql
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_process_master_sys_name') THEN
    ALTER TABLE tb_process_master
      ADD CONSTRAINT uq_process_master_sys_name
      UNIQUE (sys_nm_en, process_name);
  END IF;
END $$ LANGUAGE plpgsql;
```
표현식 UNIQUE INDEX 는 `CREATE UNIQUE INDEX IF NOT EXISTS` 자체가 멱등 — DO 블록 불필요.

### 5-4. NOT NULL 전환은 V018 에 위임 — phase2 에 도입하지 않음
- fresh-init 시 phase2 단계에서 INSERT 되는 5+5건 모두 NULL 키 컬럼 0건 — NOT NULL 부재 상태에서도 무해.
- NOT NULL 전환을 phase2 로 끌어오면 V018 (3) 과 동일 작업 중복 + V018 의 사후검증 의미 약화.
- B안 = "최소 침습" 원칙. NOT NULL 은 V018 책임 영역으로 유지.
- codex 1차 검토 동의: "FR-1-D(현 FR-1-E v2) 는 운영 리스크 낮음. fresh-init 은 곧바로 V018 이 NOT NULL 을 적용하므로 중간 상태가 장기 운영 상태가 아님."

### 5-5. ephemeral 클러스터 보존 확인 (2026-04-29)
- `C:\Users\PUJ\pg16-verify\data` 보존됨 (PG_VERSION + base/ 등 포함).
- 클러스터는 현재 stopped 상태 — `pg_ctl start -D <data> -l <log>` 로 재기동 후 검증 단계에서 사용.
- 본 클러스터는 phase1 스프린트 결정으로 **보존**, 본 스프린트 검증에 재사용.
- PG 16 binary 위치: `C:\Users\PUJ\PostgreSQL\16\bin\pg_ctl.exe` 등.

### 5-6. DB팀 자문 결과 (본문 인라인)

| 자문 | 의견 |
|------|------|
| UNIQUE 제약을 phase2 (CREATE TABLE 인라인) 와 별도 ALTER 어느쪽? | **별도 ALTER + DO 가드** 권장. 인라인은 V018 의 dedup 사후 ALTER 와 동일한 식별자 보장이 깨질 수 있음. |
| INSERT 와 ALTER 사이 트랜잭션 경계 | phase2.sql 은 `psql -f` 단일 호출로 실행 — autocommit 모드에서 각 문이 자체 트랜잭션. INSERT 가 실패해도 ALTER 는 이미 성공 상태로 남음 → 운영 안전. fresh-init 환경에서는 둘 다 통과. (codex 1차 검토 — "psql -f는 명시 BEGIN 없으면 기본 autocommit으로 각 문장이 자체 트랜잭션입니다. §5-6 주장은 맞습니다."로 검증됨) |
| V018 실행 시점 변경 가능성 | **불필요**. V018 은 그대로 두고 phase2 가 자체 멱등성 보장하면 충분. |
| 향후 점진 적용 환경(운영DB) 재실행 시 충돌 | 0 — `IF NOT EXISTS` 멱등 가드로 ALTER/INDEX 둘 다 무해. INSERT 도 ON CONFLICT 로 무해. |

### 5-7. 트랜잭션 경계 정책 (v2 신설 — codex 1차 검토 권고)

codex 권고: "UNIQUE 생성 직후와 INSERT 까지 원자성이 없습니다. phase2 의 해당 구간에 BEGIN/COMMIT 을 두거나, 최소한 UNIQUE 생성 직후/INSERT 직후 검증을 NFR 에 추가하세요."

본 스프린트 결정:
- **명시적 `BEGIN ... COMMIT` 미도입** — 이유:
  - DbInitRunner 가 phase2.sql 을 stmt 단위로 실행하는 모델 (현재 + 선행 스프린트 후) 과 충돌 — `BEGIN` stmt 가 분리 실행되면 트랜잭션 의미 깨짐
  - psql -f 경로에서는 `BEGIN/COMMIT` 가능하지만 DbInitRunner 경로와 동작 차이 발생 → 환경 의존성 증가
- **대신 NFR-3-x 검증 게이트 도입 (v3: MUST)** — UNIQUE 적용 직후 INSERT 직전에 `pg_constraint`/`pg_indexes` lookup, 미존재 시 RAISE EXCEPTION → fast-fail 보장
- 트랜잭션 경계 부재로 인한 부분실패 시나리오:
  - **회복 가능 (자연복구)**: UNIQUE 생성 성공 + 이후 INSERT 가 **일시적 실행 중단** (예: psql 강제 종료, DbInitRunner 부팅 도중 OOM, 네트워크 절단) 으로 실패. **데이터/스키마 불일치는 없음**. → 다음 phase2 재실행 시 UNIQUE DO 가드는 conname EXISTS 로 PASS, INSERT 가 ON CONFLICT 로 멱등 통과 → 자연 복구. 운영적 영향 0.
  - **회복 불가 (수동 점검 필요)**: INSERT 실패 원인이 **지속성** (데이터 중복, NULL 키, 사용자 수동 오염, 테이블 정의 불일치, 스키마 일관성 깨짐) 인 경우. 재실행만으로는 복구 안 됨. → 1차 방어: NFR-3-x fast-fail 게이트가 INSERT 진입 자체를 차단해 부분 적용 확산을 막음. 2차: 운영자/개발자가 로그 확인 후 수동 점검 필요 (DBeaver 또는 psql 로 distinct/NULL/제약 상태 진단).
- v3 한정 사유: codex 2차 검토 — "지속성 원인까지 자연복구 보장으로 읽히면 과합니다" → 일시적 실패에 한해서만 자연복구 주장.

---

## 6. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| 초기화 SQL | `src/main/resources/db_init_phase2.sql` | 수정 (INSERT 앞에 UNIQUE 제약·INDEX 선이동 + 헤더 주석 보강) |
| 마이그레이션 SQL | `swdept/sql/V018_process_master_dedup.sql` | **변경 없음** (FR-2 명시) |
| 문서 | `docs/PLANS.md` §2-b | 수정 (후속 → 완료) |
| 문서 | `docs/references/setup-guide.md` §2-2 | 수정 1줄 (보장 보강) |
| 문서 | `docs/exec-plans/phase1-ddl-formalization.md` | 수정 1줄 (T7 결과 보강 — 선택) |
| 문서 | `docs/product-specs/phase2-V018-init-ordering.md` | 신규 (본 문서) |
| 문서 | `docs/exec-plans/phase2-V018-init-ordering.md` | 신규 (개발계획) |

**합계**: 신규 2 (기획서·개발계획), 수정 4 (init SQL + PLANS + setup-guide + 선택 1). **Java 변경 0**. **운영DB 변경 0**.

---

## 7. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | phase2.sql 의 UNIQUE 제약 추가가 V018 과 식별자 충돌 | 매우 낮음 | 동일 식별자 사용 + DO 가드 (`pg_constraint` lookup) — V018 진입 시 무해 |
| R-2 | 표현식 UNIQUE INDEX 의 `IF NOT EXISTS` 멱등성 PG 버전 의존 | 매우 낮음 | `CREATE UNIQUE INDEX IF NOT EXISTS` 는 PG 9.5+ 표준. 운영·ephemeral 모두 PG 16 |
| R-3 | INSERT 가 ALTER 보다 먼저 실행되어 stop | 0 | FR-1 으로 ALTER 가 INSERT 보다 앞에 위치 — 본 스프린트의 핵심 |
| R-4 | 운영DB 에 phase2.sql 재실행 시 ALTER 가 V018 의 NOT NULL/UNIQUE 와 충돌 | 매우 낮음 | DO 가드 + `IF NOT EXISTS`. NOT NULL 은 phase2 가 안 건드림 (FR-1-E) |
| R-5 | V018 checksum 변경 우려 | 0 | FR-2-A 로 명시 차단. 검증 단계에서 SHA256 비교 (NFR-6) |
| R-6 | phase1 적용 후 phase2 적용 전에 어떤 사용자가 INSERT 5건 데이터 수동 삽입 | 매우 낮음 | **out-of-contract** — fresh-init 절차는 `phase1 → phase1_sigungu → phase2 → V*.sql` 일괄 실행이 계약. 중간 사용자 개입은 본 스프린트 범위 외. 발생 시 DO 가드 진입 시 distinct ≤ 5 + NULL=0 이면 통과, 그 외는 명시적 EXCEPTION (fast-fail). |
| R-7 | DbInitRunner 의 SQL splitter (`DbInitRunner.java:55-71`) 가 dollar-quote 미인식 → DO `$$ ... $$` 블록 절단 | **선행 스프린트로 분리 해소 + Entry Criteria 로 hard gate** | **선행 스프린트 `dbinitrunner-dollar-quote-aware`** 가 splitter 를 dollar-quote-aware 로 수정. 본 스프린트는 그 위에 빌드 — R-7 은 본 스프린트 진입 시점에 이미 해소된 상태. **§1.5 Entry Criteria EC-1~3 으로 hard gate 명문화 (v3)** — 선행 미완료 시 본 스프린트 개발계획·구현 진입 자동 차단. (codex 1차 검토 — "핵심 수정은 DbInitRunner 의 DO 블록 처리 가능성 명시/검증" / codex 2차 검토 — "본 스프린트의 하드 entry gate 로 더 명문화 필요") |

---

## 8. 산출물 요약

본 스프린트 종료 시:
- `src/main/resources/db_init_phase2.sql` — INSERT 앞에 UNIQUE 제약(DO 가드) + UNIQUE INDEX(IF NOT EXISTS) 선이동, 헤더 주석 보강
- `docs/product-specs/phase2-V018-init-ordering.md` — 본 기획서 (codex 검토 + 사용자 승인 반영 후 v2 가능)
- `docs/exec-plans/phase2-V018-init-ordering.md` — 개발계획 (단계별 SQL + ephemeral T-매트릭스 + 롤백)
- `docs/PLANS.md` §2-b/c — 완료 표기
- `docs/references/setup-guide.md` §2-2 — 1줄 보강

검증 결과 (v3.2 scope 한정):
- ephemeral T-매트릭스 (T1~T4 본 스프린트 정의 — 개발계획서 참조)
- **T1**: phase2.sql 단독 멱등 3회 — 본 스프린트 surface (line 19, 43-94, 98, 109) 통과 + NFR-3-x NOTICE PASS + INSERT 0 5 양 테이블 + count=5/distinct=5/UNIQUE 1. (out-of-scope ERROR `tb_org_unit does not exist` line 464 발생 가능 — `ON_ERROR_STOP=1` 환경에서 rc=3 가능, 측정 제외)
- **T2**: phase1 → phase1_sigungu → phase2 → V*.sql — 본 스프린트 surface 까지 통과 (line 60/70 stop 회귀 차단). full rc=0 은 후속 sprint `phase2-tb_ops_doc-forward-ref` 완료 후 목표.
- **T2-b**: DbInitRunner 경로 부팅 — per-stmt try/catch 로 본 스프린트 surface ERROR 0, 부팅 정상 (`Started SwmanagerApplication`).
- **T3**: V018 재진입 시 멱등 (rc=0, DELETE=0, ERROR=0). V018 자체는 `BEGIN/COMMIT` 트랜잭션이라 phase2 와 별개.
- **T4**: V018 SHA256 무변경.

---

## 9. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 전 확인 사항
- [x] B안 채택 사유 명시 (4안 비교 + codex 사전 의견)
- [x] V018 무수정 원칙 명시 (FR-2-A)
- [x] 운영DB 무영향 명시 (NFR-5)
- [x] ephemeral 클러스터 25880 재사용 가능 확인
- [x] UI 키워드 0건 → 디자인팀 자문 skip
- [x] DB팀 자문 본문 인라인 (§5-6)
- [x] codex 1차 검토 결과 반영 (v2)
- [x] 선행 스프린트 `dbinitrunner-dollar-quote-aware` 분리 (E3 채택)
- [x] codex 2차 검토 완료 (v2 기준 → ⚠ 수정필요, 4건 권고)
- [x] codex 2차 검토 권고 반영 (v3 — NFR-3-x MUST 승격, §1.5 Entry Criteria 신설, silent 표현 정정, §5-7 자연복구 한정)
- [x] codex 3차 검토 완료 (v3 기준 → ⭕ 최종승인, 13.8K 토큰, 부수 우려 1건 정정 반영)
- [x] **사용자 최종승인** (2026-05-11, EC-4 충족) — 기획 단계 closure
- [x] **선행 스프린트 완료 확인** (EC-1~3 모두 충족, 2026-05-11 검증):
  - EC-1: ✅ commit `ba12fc6` master 머지 (2026-04-29) — `feat(dbinitrunner-dollar-quote-aware): SQL splitter dollar-quote 인식`
  - EC-2: ✅ `docs/exec-plans/dbinitrunner-dollar-quote-aware.md` v3.1 — UT 18/18 PASS, 양쪽 prod 4/4 PASS, codex 5차 승인 기록
  - EC-3: ✅ `DbInitRunner.java:87-179` 상태머신 (NORMAL/IN_LINE_COMMENT/IN_SINGLE_QUOTE/IN_DOLLAR_QUOTE) + 단위테스트 3종 (`DbInitRunnerTest`, `DbInitRunnerBaselineTest`, `DbInitRunnerIntegrationTest`) 존재

### 다음 절차 (v3.3 갱신)

현 단계: **codex 4차 ⚠ → 5차 ⚠ → 6차 ⚠ → 7차 ⭕** (v3.3 amendment 통과, 2026-05-11). EC-1~4 모두 이미 충족 (선행 스프린트 commit `ba12fc6` master + codex 3차 ⭕ + 사용자 v3 최종승인). 구현 (Step 1~5) 진행 중 Discovery 발견 → v3.1/v3.2/v3.3 amendment 완료.

1. **사용자 amendment 승인** 대기 → 구현 측정 재개
2. T1/T2/T2-b/T3/T4 를 v3.3 scope 한정 기준으로 PASS/FAIL 판정 (T1 의 `ON_ERROR_STOP=1` rc=3 은 측정 제외, NFR-3-x NOTICE PASS + INSERT 0 5 + count/distinct/UNIQUE 가 본 스프린트 surface PASS 기준)
3. 측정 모두 PASS → 사용자 "작업완료" 발화 → 자동 commit+push (트리거 confirm 게이트, 1 commit 통합)
4. 본 스프린트 commit 후 → **후속 sprint `phase2-tb_ops_doc-forward-ref` 기획서 작성** (별도 워크플로우)

---

## 10. 구현 중 Discovery (v3.1 신설, 2026-05-11)

### 발견 사실

T1 (Step 3-2) 실행 중, **본 스프린트 fix 가 정상 작동**해 phase2.sql:60 의 ON CONFLICT 가 통과되자, **그 뒤에 가려져 있던 후속 결함이 표면화**:

```
psql:src/main/resources/db_init_phase2.sql:464: ERROR:  relation "tb_org_unit" does not exist
```

원인: `tb_ops_doc` (line 433 CREATE) 가 다음 두 테이블을 **자기보다 뒤에 정의된** 채로 FK 참조:
- line 439: `org_unit_id BIGINT REFERENCES tb_org_unit(unit_id)` — `tb_org_unit` 은 line 567 에서 CREATE
- line 443: `plan_id     BIGINT REFERENCES tb_work_plan(plan_id)` — `tb_work_plan` 은 line 535 에서 CREATE

### 본 스프린트 영향
- **본 변경 (UNIQUE 선이동 + NFR-3-x 게이트) 은 정상 작동** — NOTICE PASS 출력, INSERT 0 5 양 테이블, count=5/distinct=5/UNIQUE 1.
- **psql -f + ON_ERROR_STOP=1 경로** 에서는 line 464 stop 발생 → 우리 변경 외의 phase2 후속 stmt (점검내역서, 기타 시드) 실행 안 됨.
- **DbInitRunner 경로** 에서는 per-stmt try/catch 라 line 464 ERROR 가 debug 로그로만 남고 다음 stmt 진행 → 운영 부팅에 무해.
- 운영DB 에는 `tb_org_unit`/`tb_work_plan` 이 이미 V*.sql 등으로 존재해 fresh-init 시나리오만 영향 → 운영 영향 0.

### NFR amendment (v3.1)
- NFR-2 (a) 와 NFR-3 (d) 의 측정 범위를 **본 스프린트 변경 surface (phase2.sql line 19, 43-94, 98, 109) 한정** 으로 명시.
- out-of-scope ERROR (`tb_org_unit does not exist`, `tb_work_plan does not exist`) 는 측정 제외, 후속 sprint 로 분리.

### 본 스프린트 자체 status
- 우리 fix 는 정상 작동 → **본 스프린트 진행 계속**. T2-b (DbInitRunner 경로) 와 T3/T4 도 scope 한정 기준으로 PASS 가능.

---

## 11. 후속 sprint 권고 (v3.1 신설, 2026-05-11)

### `phase2-tb_ops_doc-forward-ref`

**목적**: phase2.sql 의 forward-reference 2건 (`tb_ops_doc` → `tb_org_unit`, `tb_work_plan`) 해소. 본 스프린트의 패턴 (선이동) 동일 적용.

| 항목 | 내용 |
|------|------|
| 범위 | `src/main/resources/db_init_phase2.sql` 의 CREATE TABLE 순서 재배열 (또는 `tb_ops_doc` 의 FK 를 ALTER TABLE 로 분리) |
| 영향 | fresh-init 환경 한정 — 운영DB 영향 0 (이미 모든 테이블 존재) |
| 본 스프린트와의 관계 | 본 스프린트 완료 후 진입 가능. 본 스프린트의 NFR-3-x 게이트 패턴 재사용 가능. |
| 우선순위 | **중** — 본 스프린트 완료 후 신규 PC 셋업 시 phase2 stop 회귀가 line 60 → line 464 로 이동만 됐을 뿐, fresh-init 완전 통과는 아직 미해소. |
| 예상 분량 | 작은 (선이동 2건 + 검증) — phase2-V018-init-ordering 보다 더 단순할 가능성 |

> 본 스프린트 완료 + commit 후 별도 기획서 (`docs/product-specs/phase2-tb_ops_doc-forward-ref.md`) 작성 권고.
>
> **2026-05-11 closure**: 후속 sprint `phase2-tb_ops_doc-forward-ref` (commit `<TBD>`) 로 line 464 forward-reference 해소 완료. 그 결과 또 다른 후속 결함 (line 686 부근 `qt_category_mst` 외부 V024 의존) 표면화 — `phase2-vsql-external-deps` 신규 sprint 권고 (PLANS.md 참조).
