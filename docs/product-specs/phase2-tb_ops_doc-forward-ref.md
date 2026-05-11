---
tags: [plan, sprint, schema, init-ordering, forward-reference]
sprint: "phase2-tb_ops_doc-forward-ref"
status: draft-v4
created: "2026-05-11"
---

# [기획서] phase2.sql 의 tb_ops_doc forward-reference 해소

- **작성팀**: 기획팀
- **작성일**: 2026-05-11
- **선행 스프린트 (완료)**: `phase2-V018-init-ordering` (swmanager commit `69b6196`, 2026-05-11). 본 스프린트의 직접 출발점 — v3.3 Discovery 에서 표면화한 후속 결함 해소.
- **상태**: 초안 v4 — codex 1차→3차 ⚠ 권고 모두 반영 + codex 4차 ⚠ 잔존 1건 정정 + **codex 5차 ⭕ 최종승인** (2026-05-11). 사용자 최종승인 (EC-3) 대기.
- **UI 키워드**: 본 스프린트 의도상 UI 변경 0건 (DB schema 선이동만). 본 기획서 안의 "UI" 단어 등장은 (i) FR-2-B 의 phase2.sql 기존 섹션 헤더 주석 인용 1회 + (ii) 본 frontmatter 의 "UI 키워드" 항목명·개정 이력·§9 체크리스트의 메타 표현으로 한정. AGENTS.md §3-1 의 키워드 표는 본문 의미적 매칭이 핵심이고, 메타·자가 인용은 매칭 외로 해석 → **디자인팀 자문 skip**. (보수적으로 자문 트리거 의향 있다면 사용자 결정에 위임.)
- **개정 이력**:
  - v1 (2026-05-11): 초안. A안 (선이동) 채택, 2안 비교, V100 무수정 + DB팀 자문 인라인.
  - v2 (2026-05-11): codex 1차 ⚠ 3건 반영. (a) `tb_org_unit` seed INSERT 수량 25 → **39** 정정 (실측, 2026-05-11 grep), (b) §5-4 V100 와 phase2 의 `tb_work_plan` "의미 동일" 표현 정정 — 두 정의는 컬럼 타입(`SERIAL` vs `BIGSERIAL`)·`process_step` 타입(INTEGER CHECK vs VARCHAR(100))·기본값·인덱스명까지 다름. 정확히는 "기존 중복 정의를 본 스프린트가 새로 악화하지 않고, `IF NOT EXISTS` 로 충돌 없이 no-op 처리됨", (c) NFR-5 와 NFR-2(b) 충돌 — NFR-2(b) 의 운영DB 검증을 "DbInitRunner 부팅 시 운영DB 와 ephemeral 양쪽에서 변경 surface 관련 ERROR 0 관찰" 로 분리. 운영DB 에 본 스프린트 SQL 직접 실행은 0 (NFR-5 그대로). 운영DB 부팅 시 phase2 재실행은 멱등성으로 무해 (의도된 read-only 검증).
  - v3 (2026-05-11): codex 2차 ⚠ 잔존 3건 정정. (a) FR-2-D / §5-6 / R-3 의 "25개" / "25건" → **39건** 일제 정정, (b) UI 키워드 항목 표현 완화 — 본문에 "UI" 단어가 phase2.sql 기존 섹션 헤더 주석 인용으로 1회 등장하지만 본 스프린트 의도는 백엔드만임을 명시 (디자인팀 skip 사유 보강), (c) §9 체크리스트에 codex 1차/2차 검토 결과 + v2/v3 정정 반영 표기.
  - v4 (2026-05-11): codex 3차 ⚠ 잔존 4건 일제 정리. (a) §6 영향 범위의 "25 seed INSERT" → **39 seed INSERT** 정정 (v3 일제 정정 누락분), (b) §9 다음 절차 stale (codex 1차→v2 흐름) → 현 단계 (codex 3차→v4→4차) 로 갱신, (c) 상단 본문 상태 줄 v2 → **v4** 로 갱신, (d) UI 키워드 항목 정확성 보강 — "UI" 단어는 frontmatter 항목명·개정 이력·§9 체크리스트 등 메타 텍스트에도 등장함을 명시하고, 메타·자가 인용은 키워드 매칭 외로 해석함 (보수적 트리거 결정은 사용자에게 위임).

---

## 1. 배경 / 목표

### 배경

phase2-V018-init-ordering 스프린트 (v3.3) 의 T1 측정 중 발견:

```
psql:src/main/resources/db_init_phase2.sql:464: ERROR:  relation "tb_org_unit" does not exist
```

원인 — `tb_ops_doc` (line 433 CREATE) 가 자신보다 **뒤에 정의된** 두 테이블을 FK 로 참조:

| 참조 위치 | 참조 컬럼 | 대상 테이블 | 대상 CREATE |
|----------|----------|------------|------------|
| line 439 | `org_unit_id BIGINT REFERENCES tb_org_unit(unit_id)` | `tb_org_unit` | line 567 |
| line 443 | `plan_id BIGINT REFERENCES tb_work_plan(plan_id)` | `tb_work_plan` | line 535 |

`tb_ops_doc` 와 두 의존 테이블이 모두 같은 `db_init_phase2.sql` 안에서 정의되지만 순서가 잘못. fresh-init `psql -f + ON_ERROR_STOP=1` 환경에서 line 464 stop.

### 운영DB 미발견 사유

운영DB 는 점진 적용으로 `tb_org_unit`/`tb_work_plan` 이 이미 V*.sql 로 먼저 생성된 상태 → fresh-init 흐름 미발생. 본 스프린트 결과물도 운영DB 에 영향 0 (모든 변경이 `CREATE TABLE IF NOT EXISTS` 의 멱등성으로 흡수).

### 목표

1. **fresh-init 환경에서 phase2.sql 의 tb_ops_doc forward-reference 2건 해소** — line 464 stop 회귀 차단. 이로써 phase2-V018-init-ordering 의 §1 목표 1 "full fresh-init 무중단 실행" 도 완성 (선행 스프린트의 후속 목표).
2. **운영DB 무영향 (쓰기 0)** — 본 스프린트 결과물도 신규 환경(또는 DR) 에서만 효과 발생. 운영DB 는 `CREATE TABLE IF NOT EXISTS` 멱등성으로 무해.
3. **phase2.sql 의 의미·동작 보존** — CREATE TABLE 순서 재배열 외에 schema/seed 의미 변경 0.
4. **V100 (`work_plan_performance_tables.sql`) 의 멱등 보완 유지** — V100 도 `CREATE TABLE IF NOT EXISTS tb_work_plan` 보유. 본 스프린트가 phase2 의 tb_work_plan 위치를 옮겨도 V100 은 그대로 무해.

### 비목표
- phase2.sql 의 다른 ordering 문제 점검 — 본 스프린트는 line 439/443 forward-reference 2건에만 집중. 다른 잠재 결함은 발견 시 별도 sprint.
- `tb_ops_doc` 또는 두 의존 테이블의 schema 변경 (컬럼/제약/인덱스) — 범위 외.
- V100 마이그레이션 자체 수정 — 범위 외 (Flyway checksum 보존).
- DbInitRunner 동작 변경 — 범위 외 (선행 스프린트 결과 그대로 활용).

---

## 1.5. Entry Criteria

| EC | 조건 | 검증 |
|----|------|------|
| EC-1 | 선행 스프린트 `phase2-V018-init-ordering` master 머지 완료 | `git log --oneline master | grep phase2-V018-init-ordering` → `69b6196` 확인 |
| EC-2 | 선행 스프린트 결과물 (`db_init_phase2.sql` 의 UNIQUE 선이동 + NFR-3-x 게이트) 이 본 스프린트 작업의 base | 본 기획서 작성 시점 (2026-05-11) 의 master HEAD 가 `69b6196` 인지 확인 |
| EC-3 | 본 기획서 v1+ 의 codex 1차+ 검토 ⭕ + 사용자 최종승인 | 본 문서 §9 체크리스트 |

> **선행 스프린트와 달리** 본 스프린트는 DbInitRunner 의존 없음 (현재 splitter 가 이미 dollar-quote-aware). EC 단순.

---

## 2. 사용자 시나리오

### 2-1. 신규 개발자 PC 셋업 (본 스프린트 완료 시점)
1. git clone 후 PostgreSQL 컨테이너/클러스터 기동
2. `psql -f db_init_phase1.sql` → 19 테이블 + 마스터 시드
3. `psql -f db_init_phase1_sigungu.sql` → sigungu_code
4. `psql -f db_init_phase2.sql` → ✅ **완전 무중단 실행** (선행 스프린트 + 본 스프린트 결과 합쳐 line 60/70/464 모두 회귀 차단)
5. `psql -f swdept/sql/V001..V*.sql` 순차 실행 → 모두 멱등 통과
6. 서버 기동 → 모든 모듈 정상

### 2-2. 재해 복구 / DR
- 운영DB 손실 시 `phase1 → phase2 → V*.sql` 만으로 schema **완전 자동 복원 가능** — 본 스프린트가 그 마지막 디딤돌.

### 2-3. 운영자 (영향 0)
- 운영DB 는 본 스프린트 결과물에 노출되지 않음.
- 운영DB 에 phase2.sql 재실행 시: 모든 변경이 `CREATE TABLE IF NOT EXISTS` 멱등성으로 흡수 → 무해.

---

## 3. 기능 요건 (FR)

### FR-1. `tb_work_plan` 블록 선이동

| ID | 내용 |
|----|------|
| FR-1-A | `tb_work_plan` CREATE TABLE (현재 line 535) + 그에 딸린 5개 인덱스 (`idx_tb_work_plan_*`, line 555-559) 를 **`tb_ops_doc` CREATE TABLE (현재 line 433) 직전** 으로 통째 이동. |
| FR-1-B | `tb_work_plan` 의 self-FK (`parent_plan_id` → `tb_work_plan(plan_id)`, line 547) 는 자기 참조라 위치 변경 무관 — 그대로 보존. |
| FR-1-C | `tb_work_plan` 의 외래 FK (`infra_id` → `tb_infra_master`, `assignee_id`/`created_by` → `users`) 는 모두 phase1 테이블 — 선이동 후에도 이미 존재. |

### FR-2. `tb_org_unit` 블록 선이동

| ID | 내용 |
|----|------|
| FR-2-A | `tb_org_unit` CREATE TABLE (현재 line 567) + 3개 인덱스 (`idx_org_unit_*`, line 578-580) + **39개 seed INSERT** (2026-05-11 실측, 첫 line 584 ~ 마지막 line 623) 를 **`tb_ops_doc` CREATE TABLE 직전 (FR-1 블록 다음)** 으로 통째 이동. |
| FR-2-B | "스프린트 5 (2026-04-19): 조직도 + 문서 선택 UI 통일 + 운영/테스트 구분" 섹션 헤더 주석 (line 561-564) 도 함께 이동. |
| FR-2-C | `tb_org_unit` 의 self-FK (`parent_id` → `tb_org_unit(unit_id)`) 는 자기 참조라 위치 변경 무관 — 그대로 보존. |
| FR-2-D | 39개 INSERT 의 `SELECT (SELECT unit_id FROM tb_org_unit WHERE name=... AND unit_type=...)` 형태는 자기 테이블에 의존하므로 CREATE TABLE 직후 위치만 유지하면 정상. |

### FR-3. `db_init_phase2.sql` 헤더 주석 보강

| ID | 내용 |
|----|------|
| FR-3-A | 파일 상단 주석에 본 스프린트 이력 1줄 추가: "phase2-tb_ops_doc-forward-ref (2026-05-11) — tb_ops_doc 의 forward-reference 해소를 위해 tb_work_plan + tb_org_unit 블록 선이동. schema/seed 의미 변경 0." |
| FR-3-B | 이동된 두 블록 시작점에 1줄 주석 추가: "-- [phase2-tb_ops_doc-forward-ref 2026-05-11] tb_ops_doc 의 forward-reference 해소를 위해 본 블록을 선이동." |

### FR-4. 문서 갱신

| ID | 내용 |
|----|------|
| FR-4-A | `docs/PLANS.md` §2-b 후속 백로그의 `phase2-tb_ops_doc-forward-ref` 항목을 **완료 스프린트** 로 이동. 산출물/검증/커밋 요약 포함. |
| FR-4-B | `docs/references/setup-guide.md` §2-2 의 보강 줄 (선행 스프린트에서 "line 60/70 통과 보장, full phase2 통과는 후속 sprint 이후") 을 "**phase2.sql 완전 통과 보장** (선행 V018 init-ordering + 본 스프린트 결합 효과)" 로 갱신. |
| FR-4-C | (선택) `docs/product-specs/phase2-V018-init-ordering.md` §10 Discovery 의 후속 sprint 줄에 "완료 (commit `<TBD>`)" 라벨 추가. |

### 범위 외
- phase2.sql 의 다른 ordering 문제 점검·수정.
- `tb_ops_doc` 의 schema/제약 변경.
- V100 수정.
- DbInitRunner 동작 변경.
- 운영DB 에 phase2 재적용.

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공 (SQL 만 변경, Java 영향 0 예상). |
| NFR-2 (v2 정정) | **양 경로 모두 cover**: <br>(a) **psql -f 경로 (ephemeral 25880)**: `psql -f db_init_phase1.sql` → `phase1_sigungu.sql` → `phase2.sql` → `for v in V001..V*; psql -f $v` **전 단계 rc=0**. line 60/70/464 stop 회귀 0건. → **완전 fresh-init 통과**. <br>(b) **DbInitRunner 경로**: 본 스프린트가 운영DB 에 SQL 직접 실행은 0 (NFR-5 그대로). 운영 부팅 시 phase2 재실행은 모든 변경이 `CREATE TABLE IF NOT EXISTS` 멱등성으로 흡수 — read-only 검증 의도. 검증 방법: <br>&nbsp;&nbsp;(b-i) ephemeral DB (`sw_t2b`) 부팅 시 ERROR 0, PSQLException 0, "relation does not exist" 0건. <br>&nbsp;&nbsp;(b-ii) 운영DB 부팅 시 본 스프린트 변경 surface (tb_work_plan / tb_org_unit / tb_ops_doc 관련) ERROR 0 — 부팅 로그 read-only 관찰만, schema 변경 0. |
| NFR-3 | **재실행 멱등성 (3회 연속)** — phase2.sql 단독 3회 후: 본 스프린트 변경된 블록 (`tb_work_plan`, `tb_org_unit`, `tb_ops_doc`) 의 row count + 제약·index 개수 변동 0. ERROR 로그 0. NOTICE 는 허용. |
| NFR-4 | **본 스프린트 변경의 의미 불변** — 이동 전후로 phase2.sql 실행 후 schema 비교 시 테이블·컬럼·제약·인덱스·시드 데이터 완전 동일. |
| NFR-5 (v2 정정) | **운영DB 쓰기 0** — 본 스프린트는 운영DB 에 SQL 직접 실행 없음 (배포 도구·수동 작업 없음). 단 운영DB 가 동작 중인 서버 부팅 시 (NFR-2-b 의 (b-ii) 경로) DbInitRunner 가 phase2 를 멱등 재실행함 — 모든 변경이 `CREATE TABLE IF NOT EXISTS` 의 멱등성으로 흡수되므로 운영 schema 변동 0. 즉 NFR-5 의 "쓰기 0" = "본 스프린트가 새로 발생시키는 운영DB 쓰기 0" 이며, NFR-2(b-ii) 는 "DbInitRunner 의 기존 멱등 부팅 동작" 을 read-only 검증한다는 의미. 양자 모순 없음. |
| NFR-6 | **V100 Flyway checksum 무변경** — V100 SHA256 변동 0. |
| NFR-7 | **회귀 스모크** — 서버 부팅 ERROR 0. 점검내역서/사업관리/견적서/조직도 등 기존 기능 정상 동작. |

---

## 5. 의사결정 / 우려사항

### 5-1. 2안 비교 — A안 채택

| 안 | 요지 | 채택 여부 |
|----|------|----------|
| **A** | **CREATE TABLE 순서 재배열 (선이동)** — `tb_work_plan` + `tb_org_unit` 블록을 `tb_ops_doc` 앞으로 이동 | ✅ **채택** — 단순, schema 의미 변경 0, 운영 영향 0 |
| B | FK 만 ALTER TABLE 로 분리 — `tb_ops_doc` 인라인 FK 제거 + 끝부분 ALTER ADD CONSTRAINT + 멱등 가드 | ❌ FK 명명 + 멱등 가드 + 운영DB 의 기존 FK 와 충돌 가능성 검증 부담 ↑. 의미 변경 없음에 비해 surface 큼 |

**A안 채택 사유**:
- 두 의존 테이블 모두 `CREATE TABLE IF NOT EXISTS` — 운영DB 에서 무해 (멱등성 흡수)
- 선이동 후에도 두 테이블의 self-FK 와 외래 FK (phase1 테이블) 는 모두 정상 정렬 (FR-1-C / FR-2-C)
- seed INSERT 도 자기 테이블만 참조 (FR-2-D) → 함께 이동 시 정상 동작
- phase2-V018-init-ordering 의 §5-1 4안 비교 (A/B/C/D) 와 결이 다름 — 본 스프린트는 CREATE 순서만 바꾸는 mechanical refactor 라 logic 분기 없음

### 5-2. 이동 위치 — `tb_ops_doc` CREATE 직전

선택지:
- (a) `tb_ops_doc` 직전 (line 433 앞) — **채택**: 최소 surface, 두 의존이 명시적으로 선행
- (b) `tb_ops_doc_signature` (line 522 끝) 직후 — 비채택: `tb_ops_doc` 와 `tb_ops_doc_signature` 사이의 의미 분리가 약화
- (c) 파일 맨 앞 (phase1 직후) — 비채택: 다른 phase2 테이블과 함께 두는 의미 단절

(a) 채택 시 phase2.sql 의 의미 단위 ("이 블록은 다른 테이블이 의존하므로 먼저 정의") 가 명확.

### 5-3. NFR-3-x 게이트 미도입 — 본 스프린트는 불필요

선행 스프린트 phase2-V018-init-ordering 의 NFR-3-x 검증 게이트는 **UNIQUE 제약·INDEX 등록 후 INSERT 의 ON CONFLICT 추론** 보호 목적. 본 스프린트는 FK 자체의 forward-reference 해소이고, FK 등록 실패 시 PostgreSQL 이 즉시 `42P01` (relation does not exist) 로 stop 시키는 명시적 에러라 silent regression 가능성 없음 → 별도 게이트 불필요.

선행 스프린트의 NFR-3-x 게이트는 그대로 보존 (본 스프린트가 손대지 않음).

### 5-4. V100 와 `tb_work_plan` 중복 정의 — 그대로 보존 (v2 표현 정정)

`swdept/sql/V100_work_plan_performance_tables.sql:163` 에도 `CREATE TABLE IF NOT EXISTS tb_work_plan` 존재.

**중요 (codex 1차 권고)**: 두 정의는 의미가 동일하지 않음:

| 항목 | phase2 (line 535+) | V100 (line 163+) |
|------|------------------|----------------|
| PK 타입 | `BIGSERIAL` | `SERIAL` |
| `process_step` | `VARCHAR(100)` (자유 텍스트) | `INTEGER CHECK (BETWEEN 1 AND 7)` |
| `repeat_type` 기본값 | 없음 (NOT NULL 만) | `'NONE'` |
| `status` 기본값 | 없음 (NOT NULL 만) | `'PLANNED'` |
| 인덱스명 | `idx_tb_work_plan_*` | `idx_plan_*` |

**→ 본 스프린트의 효과**: 두 정의의 의미 차이는 본 스프린트 범위 외. 본 스프린트는 **기존 중복 정의를 새로 악화하지 않고**, `IF NOT EXISTS` 가 충돌 없이 no-op 처리:
- phase2 가 먼저 실행 (선이동 후 line ~430 부근) → phase2 정의의 `tb_work_plan` 생성
- V100 실행 시 `IF NOT EXISTS` 로 no-op (V100 정의는 무시됨, 인덱스도 phase2 의 것이 우선)
- 운영DB 에서는 어떤 순서로 적용됐든 첫 실행이 우세 (이후 실행은 모두 no-op)

→ 중복 정의의 **의미 정합성 (어느 정의가 정통인가)** 은 별도 후속 sprint 책임. 본 스프린트는 forward-reference 만 해소.

### 5-5. ephemeral 클러스터 재사용 (2026-05-11 확인)

선행 스프린트에서 사용한 `localhost:25880` 클러스터 (`C:\Users\PUJ\pg16-verify\data`) 를 본 스프린트 검증에 재사용. 별도 기동 절차 동일 (`pg_ctl start -D ... -l logfile start`).

### 5-6. DB팀 자문 결과 (본문 인라인)

| 자문 | 의견 |
|------|------|
| 선이동 vs ALTER 분리 (A vs B) | **A안 권장**. surface 작고 schema 의미 보존. ALTER 분리는 FK 명명·멱등 가드 추가 부담 |
| 이동 시 seed INSERT 같이 이동? | **함께 이동**. `tb_org_unit` 의 39개 seed 는 자기 테이블 참조라 CREATE 직후 위치 유지 시 정상 |
| 운영DB 재실행 안전성 | 영향 0. 모든 변경이 `CREATE TABLE IF NOT EXISTS` 의 멱등성으로 흡수 |
| 본 스프린트와 V100 중복 정의 처리 | V100 무수정 권장. V100 의 `IF NOT EXISTS` 가 phase2 의 선이동 후에도 정상 동작 |
| NFR-3-x 게이트 필요? | 불필요 (§5-3 참조). FK 등록 실패는 PostgreSQL 즉시 stop |

---

## 6. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| 초기화 SQL | `src/main/resources/db_init_phase2.sql` | 수정 (CREATE TABLE 2 블록 + 인덱스 + **39** seed INSERT 선이동 + 헤더 주석 보강) |
| 마이그레이션 SQL | `swdept/sql/V100_work_plan_performance_tables.sql` | **변경 없음** (FR-3 명시) |
| 문서 | `docs/PLANS.md` §2-b | 수정 (후속 → 완료) |
| 문서 | `docs/references/setup-guide.md` §2-2 | 수정 1줄 (전 sprint 보강 줄 갱신) |
| 문서 | `docs/product-specs/phase2-V018-init-ordering.md` | 수정 1줄 (선택, §10 후속 sprint 완료 라벨) |
| 문서 | `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` | 신규 (본 문서) |
| 문서 | `docs/exec-plans/phase2-tb_ops_doc-forward-ref.md` | 신규 (개발계획) |

**합계**: 신규 2 (기획서·개발계획), 수정 4. **Java 변경 0**. **운영DB 변경 0**.

---

## 7. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | 선이동 시 phase2.sql 의 다른 블록에 의도치 않은 의존 깨짐 | 매우 낮음 | grep 검증 (`REFERENCES tb_work_plan|tb_org_unit`) — 본 기획서 sect 1 의 2개 외 추가 의존 0건 확인. CHECK/INDEX 도 self-only. |
| R-2 | 운영DB 에 본 변경 적용 시 충돌 | 0 | `CREATE TABLE IF NOT EXISTS` 의 멱등성. 운영DB 는 본 스프린트 SQL 실행 안 함 (NFR-5) |
| R-3 | seed INSERT 39건 의 self-reference (parent unit_id lookup) 가 이동 후 깨짐 | 0 | INSERT 가 자기 테이블 (`SELECT unit_id FROM tb_org_unit WHERE ...`) 만 참조 — CREATE 직후 위치 보존 시 정상 |
| R-4 | tb_work_plan 의 self-FK (`parent_plan_id` → `tb_work_plan`) 가 이동 후 깨짐 | 0 | self-FK 는 자기 정의 → 위치 무관 |
| R-5 | V100 의 `tb_work_plan` 중복 정의와 충돌 | 0 | 둘 다 `CREATE TABLE IF NOT EXISTS` — 누가 먼저 실행돼도 무해 (§5-4) |
| R-6 | 선이동 시 line 번호 시프트로 다른 라이브 분석 도구·문서 깨짐 | 매우 낮음 | 라이브 도구 0 (phase2.sql 은 SSoT 만 read). 본 기획서·dev plan 의 line 참조는 anchor 기반 사용 |
| R-7 | 후속 sprint 가 또 표면화 (재현재 다른 forward-reference 존재) | 낮음 | 본 스프린트 진입 전 grep 으로 phase2.sql 전체 forward-reference 점검 (Step 1 사전 스캔) |

---

## 8. 산출물 요약

본 스프린트 종료 시:
- `src/main/resources/db_init_phase2.sql` — tb_work_plan + tb_org_unit 블록 선이동 (CREATE + INDEX + seed) + 헤더/블록 주석 보강
- `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` — 본 기획서
- `docs/exec-plans/phase2-tb_ops_doc-forward-ref.md` — 개발계획 (단계별 SQL + ephemeral T-매트릭스 + 롤백)
- `docs/PLANS.md` §2-b — 완료 표기
- `docs/references/setup-guide.md` §2-2 — 1줄 갱신
- (선택) `docs/product-specs/phase2-V018-init-ordering.md` §10 — 완료 라벨

검증 결과:
- ephemeral T-매트릭스 (T1~T5 본 스프린트 정의 — 개발계획서 참조)
- T1: phase2.sql 단독 멱등 3회 (rc=0, 변경 surface ERROR 0)
- T2: phase1 → phase1_sigungu → phase2 → V*.sql 전체 fresh-init (**rc=0 완전 통과** — 선행 스프린트 결과와 합쳐서)
- T2-b: DbInitRunner 경로 부팅 ERROR 0
- T3: phase2 후 schema diff = 본 변경 외 0건 (NFR-4 의미 불변)
- T4: V100 SHA256 무변경
- T5: 회귀 스모크 (점검내역서/사업관리/견적서/조직도)

---

## 9. 승인 요청

본 기획서에 대한 codex 검토 + 사용자 최종승인 요청.

### 승인 전 확인 사항
- [x] A안 채택 사유 명시 (2안 비교 + DB팀 자문)
- [x] V100 무수정 + Flyway checksum 보존 (FR/NFR)
- [x] 운영DB 무영향 명시 (NFR-5)
- [x] ephemeral 클러스터 25880 재사용 가능
- [x] UI 키워드 의도상 0건 (본문에 phase2.sql 헤더 인용 1회 + frontmatter/개정 이력/§9 등 메타 표현으로 등장 — 메타·자가 인용은 키워드 매칭 외) → 디자인팀 자문 skip
- [x] DB팀 자문 본문 인라인 (§5-6)
- [x] 선행 스프린트 commit `69b6196` 머지 확인 (EC-1)
- [x] codex 1차 검토 완료 (v1 → ⚠ 3건 — seed 25→39, V100 의미 동일 표현, NFR-5/2 충돌)
- [x] codex 1차 권고 반영 (v2)
- [x] codex 2차 검토 완료 (v2 → ⚠ 잔존 3건 — 25 표현 잔존, UI 키워드 표현, §9 stale)
- [x] codex 2차 권고 반영 (v3 — 25 일제 정정 + UI 표현 완화 + §9 갱신)
- [x] codex 3차 검토 완료 (v3 → ⚠ 잔존 4건 — §6 25 잔존, §9 다음 절차 stale, 본문 상태 v2 잔존, UI 키워드 1회 부정확)
- [x] codex 3차 권고 반영 (v4 — §6 39 정정 + 다음 절차 갱신 + 상태 줄 v4 + UI 메타 표현 보강)
- [x] codex 4차 검토 완료 (v4 → ⚠ 잔존 1건 — §9 UI 문구 stale)
- [x] codex 4차 권고 반영 (§9 UI 문구 메타 표현 보강)
- [x] codex 5차 검토 완료 (v4 polish → **⭕ 최종승인**)
- [ ] **사용자 최종승인** (EC-3)

### 다음 절차

현 단계: codex 1차 ⚠ → 2차 ⚠ → 3차 ⚠ → 4차 ⚠ → **5차 ⭕ 최종승인 (v4 polish)** — **사용자 최종승인 대기**.

1. **사용자 최종승인** (EC-3) → 개발계획서 작성 (`docs/exec-plans/phase2-tb_ops_doc-forward-ref.md`)
2. 개발계획 codex 검토 → 사용자 최종승인 → 구현 → 검증 → "작업완료" → 자동 commit+push
