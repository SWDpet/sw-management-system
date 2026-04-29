---
tags: [plan, sprint, refactor, parser, db-init, prod-fix]
sprint: "dbinitrunner-dollar-quote-aware"
status: draft-v5
created: "2026-04-29"
---

# [기획서] DbInitRunner SQL splitter — dollar-quote 인식 + 운영 결함 자동 수정

- **작성팀**: 기획팀
- **작성일**: 2026-04-29
- **선행**: 본 스프린트는 후속 스프린트 `phase2-V018-init-ordering` (v2 §5-1 E3) 의 진입 조건. 본 스프린트 자체는 선행 의존성 없음.
- **상태**: 초안 v5 (codex 4차 검토 반영, 5차 검토 대기 — 마무리 단계) — **운영 결함 발견으로 의미 격상**
- **UI 키워드**: 0건 (백엔드 전용 — 디자인팀 자문 skip)
- **개정 이력**:
  - v1 (2026-04-29): 초안. dollar-quote (`$$`, `$tag$`) 인식 splitter 로 전환.
  - v2 (2026-04-29): codex 1차 검토 반영. (a) **알고리즘 모델 변경 — 전역 단계 모델 → 상태 기반 단일 패스 모델** (FR-1-C 핵심 수정. line-comment strip 을 NORMAL 상태에서만 처리하여 dollar-quote 본문의 `--` 보존). (b) FR-1-A 의 tag 규칙 정확화 (PG 표준 unquoted identifier subset, 대소문자 민감, 식별자 직후 boundary 조건). (c) UT 매트릭스 3건 추가 (UT-13: DO 블록 내 라인주석 보존, UT-14: 대소문자 다른 tag 미매칭, UT-15: 식별자 직후 `$tag$` boundary). (d) NFR-3-b/NFR-4 회귀 게이트 강화 — statement 수 + 각 statement SHA-256 일치. (e) R-1/R-2 완화 보강.
  - v5 (2026-04-29): codex 4차 검토 반영 — 마무리 보강. (a) NFR-3 (b) 본문에 "각 DO 블록이 정확히 1개 statement" 표현 강화 + 3 sub-gate 인라인. (b) §9-1 사전 검증 SQL 의 FROM/EXISTS 절을 모두 `public.` 스키마 명시로 통일. (c) R-10 운영 체크리스트 4항목으로 확장 — (iii) 롤백 절차 (WAR 되돌림만으로 제약 DROP 안됨) + (iv) 사후 모니터링·로그 확인. (d) 영향 범위 §6 의 docs/PLANS.md 항목에 "후속 백로그 — 자동 DDL 모델 개선" 등록 작업 명시.
  - v4 (2026-04-29): codex 3차 검토 반영. (a) §10 → §9 참조 정합성 정정 (FR-4, NFR-7/8, R-8/9 등). (b) NFR-3 (b) 회귀 게이트 강화 — `containsAll` substring → **line-range fixture + 정확 매칭 + DO `$$` / END `$$` prefix·suffix + 외부 누설 검증** (3 sub-gate). (c) NFR-7 SQL 의 sigungu_code 존재 확인 → `to_regclass('public.sigungu_code')` (스키마 명시). (d) R-10 강화 — 배포 PR / 릴리즈 체크리스트 명시 의무화 (codex 권고). (e) §1 비목표의 자동 DDL 모델 개선 후속 스프린트 시점 권고 (본 스프린트 직후, 차후 phase2 DO 블록 확대 전).
  - **v3 (2026-04-29): codex 2차 검토 + 운영DB 사실 확인 반영 — 의미 격상**. (a) v2 의 "현 시점 동작 변화 0" 전제 **틀림** 확인: `db_init_phase2.sql:500-511, 528-535` 에 DO 블록 2건 존재, 양쪽 prod (구 `211.104.137.55:5881` + 신규 `192.168.10.194:5880`) 에서 **4개 제약 (`fk_tb_document_org_unit`, `ck_tb_document_support_target_type`, `ck_tb_document_environment`, `fk_tb_document_region_code`) 모두 누락 (0/4)** — DbInitRunner splitter 가 DO 블록을 syntax error 조각들로 분리, catch 로 사일런트 swallow. (b) **회귀 게이트 재정의** (codex 권고): `statement count + SHA-256` → "의미적 동치" (DO 블록 단일 보존 / 일반 SQL 순서 유지 / PG 파서 기준 실행 가능). (c) **운영 결함 자동 수정** 을 본 스프린트 목표에 추가. (d) FR-신설 (FR-4 운영DB 검증 절차), NFR-신설 (NFR-8 양쪽 prod 4개 제약 적용 확인). (e) R-신설 (R-8 FK 위반 row, R-9 CHECK 위반 row, R-10 자동 적용 timing). (f) 데이터 무결성 사전 검증 SQL (§9 신설). (g) 잔존 위험 명시 (블록 주석 `/* */`, non-ASCII tag, `U&'..'`, `E'..'` — codex 권고). (h) 자동 DDL 모델 자체 개선은 후속 별도 스프린트로 분리 명시.

---

## 1. 배경 / 목표

### 배경 — DbInitRunner 의 SQL splitter 한계

`src/main/java/com/swmanager/system/config/DbInitRunner.java` 는 서버 부팅 시 `db_init_phase2.sql` 을 자동 실행하는 컴포넌트. 현재 splitter (lines 43-74) 동작 — **2단계 모델 (전역 line-comment strip → 단일인용/세미콜론 처리)**:

| 처리 대상 | 처리 여부 | 위치 |
|----------|-----------|------|
| 라인 주석 (`-- ...`) | ✅ strip | **전역 pre-pass** (lines 43-48) — 전체 SQL 에서 무조건 strip |
| 단일 인용 문자열 (`'...'`) — 내부 `;` 보호 | ✅ inQuote 토글 | 단일 패스 (lines 50-74) |
| 단일 인용 escape (`''`) | ✅ skip 처리 | 단일 패스 |
| **dollar-quote 블록 (`$$ ... $$`)** | ❌ 미인식 | — |
| **태그 dollar-quote (`$tag$ ... $tag$`)** | ❌ 미인식 | — |
| **블록 주석 (`/* ... */`)** | ❌ 미인식 (본 스프린트 범위 외) | — |
| 세미콜론 (`;`) 외부 | ✅ statement 분리 | 단일 패스 |

**현 모델의 추가 결함 (v2 codex 1차 검토 발견)**: 전역 line-comment strip 은 dollar-quote 본문 안의 `-- comment` 라인도 무차별 제거함. 즉 `DO $$ BEGIN -- 주석\n PERFORM 1; END $$;` 같은 입력이 들어오면 본문 주석이 사라져 PG 가 받는 SQL 텍스트가 원본과 달라짐. (현 phase2.sql 에 DO 블록 부재라 영향 0이지만, 본 스프린트 후 DO 블록 추가 시 회귀 위험 — 본 스프린트가 함께 해소.)

**문제**: PostgreSQL 의 `DO $$ BEGIN ... ; ... ; END $$ LANGUAGE plpgsql;` 같은 procedural 블록을 `db_init_phase2.sql` 에 넣으면, splitter 가 블록 내부 `;` 에서 statement 를 잘라 실행 → 잘린 조각들이 invalid syntax 로 실패 → catch 블록이 DEBUG 레벨로 swallow → 결과적으로 DO 블록 부작용 0 + 사일런트 실패. 서버 부팅은 정상 진행하지만 의도된 DDL 미적용.

### 현 시점 영향 (v3 정정) — **운영 결함 표면화 중**

v2 까지는 "phase2.sql 에 DO 블록 0개 → 영향 0" 으로 기재. **v3 에서 사실 정정**:

| 파일 | DO 블록 사용 | DbInitRunner 실행 대상 |
|------|-------------|------------------------|
| `src/main/resources/db_init_phase2.sql` | **2개** (lines 500-511, 528-535) | ✅ 부팅 시 자동 |
| `src/main/resources/db_init_phase1.sql` | 35개 (phase1 EXCEPTION 가드) | ❌ DbInitRunner 미대상 (psql -f 전용) |
| `src/main/resources/db_init_phase1_sigungu.sql` | 0개 | ❌ DbInitRunner 미대상 |
| `swdept/sql/V*.sql` | 다수 (V018 등) | ❌ DbInitRunner 미대상 |

#### v3 운영DB 사실 확인 결과 (2026-04-29)

splitter 결함 가설을 양쪽 prod DB 에서 직접 검증:

```sql
SELECT conname, contype FROM pg_constraint
 WHERE conname IN ('fk_tb_document_org_unit',
                   'ck_tb_document_support_target_type',
                   'ck_tb_document_environment',
                   'fk_tb_document_region_code');
```

| DB | 4개 제약 존재 | 4개 컬럼 존재 (`org_unit_id`/`region_code`/`environment`/`support_target_type`) |
|----|-------------|---------|
| **구 prod `211.104.137.55:5881`** | **0/4 (누락)** | 4/4 (있음) |
| **신규 prod `192.168.10.194:5880`** | **0/4 (누락)** | 4/4 (있음) |

#### 결함 메커니즘
DbInitRunner.java:55-71 splitter 가 부팅 시 phase2.sql 의 `DO $$ BEGIN ... ; ... ; END $$;` 블록을 `;` 기준으로 분리:
- 단순 `ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...` (lines 495-497, 525) → 단일 statement 통과 → **컬럼 추가 OK**
- DO 블록 (lines 500-511, 528-535) → 잘린 조각들 (`DO\nBEGIN\n  IF NOT EXISTS (...) THEN\n    ALTER TABLE ... ADD CONSTRAINT ...`, `END IF`, ...) → 각 조각 invalid syntax → catch 가 DEBUG swallow → **제약 미적용**

#### 사일런트 결함 영향
- `tb_document.org_unit_id` 컬럼은 존재하지만 FK 무결성 미보장 (`tb_org_unit.unit_id` 외 값 허용)
- `tb_document.region_code` 컬럼은 존재하지만 sigungu_code FK 미보장
- `tb_document.support_target_type` / `environment` 는 CHECK 제약 미보장 (스펙 외 값 허용)
- 운영 데이터에 "비정상 값" 이 잠재적으로 누적될 수 있는 상태로 ~10일간 (DO 블록 추가는 `feat(audit-fix-p2-schema-docs)` 시점부터) 운영됨

### 목표 (v3 — 의미 격상)
1. **DbInitRunner SQL splitter 가 `$$ ... $$` (그리고 `$tag$ ... $tag$`) 를 단일 statement 로 보존** — PG 표준 dollar-quote 의미 준수.
2. **운영 결함 자동 수정 (v3 추가)** — splitter fix 적용 후 첫 부팅 시 phase2.sql 의 DO 블록이 정상 실행 → 양쪽 prod DB 의 누락된 4개 제약 (`fk_tb_document_org_unit`, `ck_tb_document_support_target_type`, `ck_tb_document_environment`, `fk_tb_document_region_code`) 자동 적용. 본 스프린트는 splitter 변경이 곧 제약 복구임을 명시·검증.
3. **단위테스트 도입** — splitter 로직을 별도 메서드로 추출하여 단위테스트 가능 구조로 리팩토링. 회귀 안전망 확보.
4. **미래 안전성 확보** — 후속 스프린트 (`phase2-V018-init-ordering`) 에서 `db_init_phase2.sql` 에 추가되는 DO 블록도 정상 실행.
5. **회귀 게이트 = 의미적 동치 (v3 — codex 2차 검토 권고)** — 변경 전후 statement count 동일이 아니라 (DO 블록이 합쳐지므로 N 감소 예상), "원본 SQL 의 실행 단위 보존 + DO 블록 단일 statement 보존 + 일반 SQL 순서 유지 + PG 파서 기준 실행 가능" 으로 검증.

### 비목표
- 블록 주석 (`/* ... */`) 인식 — 본 스프린트 범위 외 (`db_init_phase2.sql` 에 블록 주석 부재). v3 잔존 위험 §7 R-12 명시.
- non-ASCII dollar-quote tag (예: `$한글$ ... $한글$`) — 본 스프린트 범위 외 (PG 표준은 unquoted identifier 와 동일하지만 ASCII subset 으로 보수적 채택). v3 잔존 위험 §7 R-13 명시.
- `U&'...'` (Unicode escape) / `E'...'` (C-style escape) 등 PG 확장 string literal — 본 스프린트 범위 외 (phase2.sql 에 부재). v3 잔존 위험 §7 R-14 명시.
- DbInitRunner 의 에러 로그 레벨 변경 (DEBUG → WARN) — 별도 스프린트.
- DbInitRunner 가 phase1.sql 도 실행하도록 확장 — 별도 스프린트.
- DbInitRunner 가 sql 파일을 매 부팅 자동 실행하는 모델 자체 개선 (예: 일회성 마이그레이션으로 분리, Flyway 도입) — **별도 후속 스프린트** (codex 2차/3차 검토 권고). **권고 시점 — 본 스프린트 직후 우선 진행, 최소한 차후 phase2.sql 의 DO 블록을 추가 확대하기 전에 완료** (codex 3차 검토). 자동 DDL 모델은 본 스프린트로 dollar-quote 인식이 가능해진 이후에도 "매 부팅마다 운영DB 에 DDL 자동 실행" 자체가 운영 안정성 측면 한계 — 일회성 마이그레이션 모델로 전환 권장.
- Spring 의 `ScriptUtils` / `Flyway` 등 외부 SQL parser 도입 — 본 스프린트 범위 외 (최소 침습 원칙).
- `db_init_phase2.sql` 자체 수정 — 본 스프린트는 Java 만 변경. (단 v3 운영DB 검증 절차 §9 의 무결성 사전 SQL 은 `docs/exec-plans/` 에 임시 파일로 보관 — 운영DB 에 대한 read-only 진단 SQL.)

---

## 2. 사용자 시나리오 (v3 — 영향 격상)

### 2-1. 운영자 (영향 +) — 변경
- splitter fix WAR 배포 + 운영 톰캣 재시작 시점에 DbInitRunner 가 phase2.sql 의 DO 블록 2건 정상 실행 → 양쪽 prod DB 에 누락됐던 4개 제약 (FK 2 + CHECK 2) 자동 적용.
- 부팅 로그의 `DB 초기화 완료: N개 SQL 실행` 의 N 값 변경 (감소 — DO 블록이 1 statement 로 합쳐짐).
- **데이터 무결성 사전 검증 (§9) PASS** 가 전제 — 위반 row 가 있으면 ALTER 실패 → DBR/롤백 절차 가동.

### 2-2. 신규 개발자 (영향 +) — 변경
- 신규 PC 셋업 시 첫 부팅에서 DbInitRunner 가 phase2.sql 의 DO 블록 정상 실행 → 신규 환경 DB 도 4개 제약 적용된 상태로 시작 (현재는 결함 상태로 시작 중).

### 2-3. 후속 스프린트 (`phase2-V018-init-ordering`) 작성자 (영향 +)
- `db_init_phase2.sql` 에 `DO $$ ... $$` 블록 추가 가능 — 서버 부팅 시 splitter 가 단일 statement 로 보존하여 jdbcTemplate 에 전달 → PostgreSQL 이 procedural 블록으로 정상 실행.

---

## 3. 기능 요건 (FR)

### FR-1. SQL splitter 의 dollar-quote 인식 — 상태 기반 단일 패스 (v2 모델)

| ID | 내용 |
|----|------|
| FR-1-A (v2) | splitter 를 **상태 기반 유한상태머신** 으로 재구성. 상태: `NORMAL` / `IN_LINE_COMMENT` / `IN_SINGLE_QUOTE` / `IN_DOLLAR_QUOTE(tag)`. 각 상태에서 인식하는 트리거가 **분리** 되어 cross-state pollution 없음 (§5-4 상세). |
| FR-1-B (v2) | **dollar-quote tag 규칙** — PostgreSQL 표준 준수. 시작/종료 토큰: `$tag$`, 여기서 `tag` = (a) 빈 문자열 (즉 `$$`) 또는 (b) `[A-Za-z_][A-Za-z0-9_]*` subset (PG 표준은 unquoted identifier 와 동일하되 `$` 제외 — 본 스프린트는 ASCII subset 으로 보수적 채택, non-ASCII tag 는 비대상). **대소문자 민감** (`$TAG$` ≠ `$tag$`). 시작 토큰이 `$X$` 였으면 종료 토큰도 정확히 `$X$` 여야 매칭. |
| FR-1-C (v2) | **boundary 조건** — `$tag$` 토큰 인식은 NORMAL 상태에서만, 그리고 **직전 문자가 식별자 문자(`[A-Za-z0-9_$]`)가 아닐 때만** dollar-quote 시작으로 간주. 이는 PG lexer 의 그리디 식별자 매칭과 동조 — 예: `select foo$bar$baz$bar$;` 에서 `foo$bar` 는 식별자, 이어지는 `$baz$bar$` 가 dollar-quote (tag=baz, body=`bar$`). 본 스프린트는 보수적으로 "직전 문자가 식별자 문자면 dollar-quote 시작 후보 아님 — 일반 `$` 문자로 처리" 채택. (실제 phase2.sql / V018 / phase1.sql 모두 `DO $$` 처럼 식별자 뒤에 바로 `$$` 가 오는 패턴 부재 — 보수적 정책으로 충분.) |
| FR-1-D (v2) | **상태별 트리거 분리** (codex 1차 검토 핵심):<br>**NORMAL** 에서만 `--` (라인 주석 진입), `'` (단일 인용 진입), `$tag$` (dollar-quote 진입), `;` (statement 분리) 처리.<br>**IN_LINE_COMMENT** 에서는 `\n` 만 검사 (NORMAL 복귀). 본문 모든 문자는 그대로 보존하지 않고 strip 됨 (= 라인 주석 제거).<br>**IN_SINGLE_QUOTE** 에서는 `'` (escape `''` 포함) 만 검사. 본문 그대로 보존 (`;`/`$`/`-` 모두 무력).<br>**IN_DOLLAR_QUOTE(tag)** 에서는 동일 tag 의 **닫힘 토큰만** 검사. 본문 그대로 보존 (`;`/`'`/`--` 모두 무력 — 닫힘까지 raw 텍스트 그대로). |
| FR-1-E (v2) | dollar-quote 진입·이탈 토큰 자체는 **statement 본문에 보존**. PostgreSQL 이 받은 SQL 텍스트로 그대로 처리. (라인 주석은 NORMAL 상태에서 strip — 현 동작과 동일.) |
| FR-1-F (v2) | **잘못된 입력 — dollar-quote 미종료** (파일 끝까지 닫히지 않음) → splitter 는 마지막 statement 로 그대로 묶어 jdbcTemplate 에 전달. PostgreSQL 이 syntax error 로 응답 → 기존 try/catch 가 DEBUG 로그로 처리 (현 동작 동일). 무한루프/OOM 없음 — splitter 는 단일 패스, 입력 길이에 선형. |
| FR-1-G (v2) | **잘못된 입력 — 단일 인용 미종료** → FR-1-F 와 동일 처리. |
| FR-1-H (v2) | **line-comment pre-pass 제거** — 현재 `run()` 메서드 lines 43-48 의 전체 SQL 에서 라인 주석을 strip 하는 pre-pass 는 v2 splitter 에서 **제거**. 라인 주석 strip 은 NORMAL 상태에서만 발생 → IN_DOLLAR_QUOTE 본문의 `-- comment` 가 보존됨. |

### FR-2. splitter 로직 추출 + 단위테스트

| ID | 내용 |
|----|------|
| FR-2-A | 현재 `run()` 메서드 안에 인라인된 splitter 로직 (lines 43-74) 을 **별도 static 메서드** 로 추출: `static List<String> splitSqlStatements(String rawSql)`. 입력은 raw SQL, 출력은 trim 된 statement 리스트. side effect 없음. |
| FR-2-B | `run()` 메서드는 추출된 메서드를 호출만. 실행 흐름·에러 처리 동일. |
| FR-2-C | 신규 단위테스트 파일 `src/test/java/com/swmanager/system/config/DbInitRunnerTest.java` 생성. 테스트 케이스 아래 매트릭스 cover: |
| FR-2-D | 단위테스트 케이스 매트릭스: |

| 케이스 | 입력 | 기대 출력 |
|--------|------|-----------|
| UT-1 | 단순 `CREATE TABLE foo(); CREATE TABLE bar();` | 2 statement |
| UT-2 | 라인 주석 포함 `-- comment\nCREATE TABLE foo();` | 1 statement (주석 제거) |
| UT-3 | 단일 인용 안에 `;` 보호 `INSERT INTO t VALUES ('a;b');` | 1 statement |
| UT-4 | 단일 인용 escape `INSERT INTO t VALUES ('it''s');` | 1 statement |
| UT-5 | DO `$$` 블록 (PG 표준 dedup) `DO $$ BEGIN PERFORM 1; PERFORM 2; END $$;` | 1 statement (블록 내부 `;` 보호) |
| UT-6 | 태그 dollar-quote `DO $plpgsql$ BEGIN ... END $plpgsql$;` | 1 statement |
| UT-7 | DO 블록 + 단일 인용 혼합 `DO $$ BEGIN RAISE NOTICE 'a;b;c'; END $$;` | 1 statement |
| UT-8 | DO 블록 + 외부 statement `CREATE TABLE x(); DO $$ BEGIN PERFORM 1; END $$; CREATE TABLE y();` | 3 statement |
| UT-9 | 단일 인용 안에 `$$` 가 들어있어도 dollar-quote 진입 안함 `INSERT INTO t VALUES ('contains $$ literal');` | 1 statement |
| UT-10 | 미종료 dollar-quote (파일 끝까지) `DO $$ BEGIN PERFORM 1` | 1 statement (잘못 묶이지만 splitter 가 panic 안함, FR-1-F) |
| UT-11 | 빈 statement 무시 `;;;` | 0 statement |
| UT-12 (회귀) | **현 phase2.sql 전문 입력 시 statement 수 + 각 statement SHA-256 해시 목록 = 변경 전 동일** (v2 강화 — codex 권고). count 만이 아니라 내용 일치까지 보장. 구체 N 과 해시 목록은 개발계획서에서 베이스라인 측정 후 fixture 화. | count 동일 + 해시 목록 동일 |
| **UT-13 (v2)** | **DO 블록 내부의 라인 주석 보존** `DO $$ BEGIN -- 주석 한국어\n  PERFORM 1;\n  -- 다른 주석\n  PERFORM 2;\nEND $$;` | 1 statement, **본문에 `-- 주석 한국어` / `-- 다른 주석` 보존** (FR-1-D 의 IN_DOLLAR_QUOTE 상태에서 `--` 비활성 검증 — codex 1차 검토 핵심 케이스) |
| **UT-14 (v2)** | **대소문자 다른 dollar-tag 미매칭** `DO $TAG$ BEGIN PERFORM 1; END $tag$ ; SELECT 1;` | splitter 는 `$tag$` 를 닫힘 토큰으로 인식 안함 → 입력 끝까지 IN_DOLLAR_QUOTE 유지 → 미종료로 단일 statement (FR-1-B 의 대소문자 민감 + FR-1-F 의 미종료 처리) |
| **UT-15 (v2)** | **식별자 직후 boundary** `SELECT foo$$ FROM t; CREATE TABLE x();` | splitter 는 `foo` 식별자 직후의 `$$` 를 dollar-quote 진입으로 보지 **않음** (FR-1-C 보수적 boundary 정책) → `;` 만나면 statement 분리. 결과 2 statement. (실 운영에서는 PG 가 `foo$` 까지를 식별자로 취급하지만 본 스프린트 splitter 는 보수적으로 식별자 직후 `$$` 를 일반 문자로 통과) |

### FR-3. DbInitRunner 외부 동작 보장 (v3 — 부분 수정)

| ID | 내용 |
|----|------|
| FR-3-A | `run()` 의 외부 행동 (실행 순서·jdbcTemplate 호출·로그 메시지 포맷·예외 처리) 변경 없음. |
| FR-3-B (v3) | `db_init_phase2.sql` 부팅 시 실행 결과 (테이블/제약/인덱스/시드 데이터) 변경 — **DO 블록 2건이 정상 실행되어 4개 제약 추가됨**. v2 의 "변경 없음" 은 v3 에서 정정. 단순 statement (CREATE TABLE / 단일 ALTER / INSERT) 의 실행 결과는 변경 없음. |
| FR-3-C (v3) | 부팅 로그 `DB 초기화 완료: {N}개 SQL 실행` 의 N 값 **변경됨** — DO 블록 2건이 statement 1+1 로 합쳐지므로 변경 전 N 보다 감소. v2 의 "N 변경 0" 은 v3 에서 정정. 변경 후 N 값은 개발계획서에서 베이스라인 측정. |

### FR-4 (v3 신설). 운영DB 검증 절차

| ID | 내용 |
|----|------|
| FR-4-A | **사전 검증 (구현 전)**: 양쪽 prod DB (구 + 신규) 에 §9 의 데이터 무결성 사전 검증 SQL 실행. 4개 제약의 위반 row 수 측정. |
| FR-4-B | **위반 row 0건 시**: splitter fix WAR 배포 + 운영 톰캣 재시작 → DbInitRunner 가 4개 제약 자동 적용 → 사후 NFR-8 게이트로 4/4 적용 확인. |
| FR-4-C | **위반 row > 0건 시**: 본 스프린트 구현 전에 데이터 보정 또는 정책 결정 (**별도 스프린트 분리 권고** — 본 스프린트 범위 외). 보정 완료 후 재시작 단계 진행. |
| FR-4-D | **사전·사후 SQL 은 read-only** (SELECT only). 운영DB 쓰기 트리거는 splitter fix 적용 후 첫 부팅 시 DbInitRunner 가 phase2.sql 의 ALTER ADD CONSTRAINT 를 실행할 때만. |

### 범위 외
- 블록 주석 `/* ... */` 인식 (별도 스프린트).
- non-ASCII dollar-tag 인식 (별도 스프린트).
- `U&'..'` / `E'..'` PG 확장 string literal 인식 (별도 스프린트).
- 에러 로그 DEBUG → WARN 승격 (별도 스프린트).
- DbInitRunner 의 phase1.sql 실행 확장 (별도 스프린트).
- DbInitRunner 의 자동 DDL 실행 모델 자체 개선 (별도 후속 스프린트 — codex 2차 검토 권고).
- Java/Entity 코드 그 외 변경.
- `db_init_phase2.sql` 자체 수정 (후속 `phase2-V018-init-ordering` 책임).
- 4개 제약 위반 row 가 발견될 경우의 데이터 보정 (별도 스프린트 — FR-4-C).

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공. |
| NFR-2 | `./mvnw test` 성공 — 신규 단위테스트 **15건 (UT-1~UT-15)** PASS (v2: 12 → 15). |
| NFR-3 (v5) | **회귀 게이트 (서버 부팅) — 의미적 동치** (codex 2/4차 검토 권고):<br>(a) `Started SwManagerApplication` 정상 + ERROR 0<br>(b) **DO 블록 2건이 각각 정확히 1개 statement 로 보존** (v5 표현 강화) — 변경 후 splitter 출력에서 phase2.sql:500-511, 528-535 본문이 다음 3 sub-gate 모두 PASS (§5-5 상세):<br>&nbsp;&nbsp;(b1) line-range fixture 와 정확 매칭 (정규화 후) — phase2.sql:500-511 본문이 출력 statement 중 정확히 1개와 일치, phase2.sql:528-535 본문도 마찬가지<br>&nbsp;&nbsp;(b2) 해당 2개 statement 가 각각 `DO $$` 로 시작하고 `END $$` 로 종료 (prefix·suffix)<br>&nbsp;&nbsp;(b3) DO 블록 본문이 다른 statement 로 누설되지 않음<br>(c) **일반 SQL statement 순서 유지** — DO 블록 외의 statement 들 (CREATE TABLE / 단일 ALTER / INSERT) 의 상대 순서가 변경 전과 동일 (트림 후 SHA-256 비교)<br>(d) **PG 파서 기준 실행 가능** — 변경 후 출력의 모든 statement 가 PG syntax error 없이 jdbcTemplate.execute() 통과 (catch DEBUG 로그 0 + ERROR 로그 0). v2 의 "count 동일" / "SHA-256 전체 동일" 은 v3 에서 폐기 — DO 블록 합치기로 N 감소·해시 목록 변동 자연스러움.<br>(e) 점검내역서/사업관리/견적서 등 기능 회귀 0. |
| NFR-4 (v3) | **DB 상태 변화 = 의도된 변화** — 본 변경 적용 후 DB schema 비교: 4개 제약 (`fk_tb_document_org_unit`, `ck_tb_document_support_target_type`, `ck_tb_document_environment`, `fk_tb_document_region_code`) 이 **추가됨** (변경 전: 누락 / 변경 후: 4/4). 그 외 schema 변화 0. 마스터 시드 행 수 동일. v2 의 "DB 상태 회귀 0" 은 v3 에서 "의도된 변화 외 회귀 0" 로 정정. |
| NFR-5 (v3) | **운영DB 영향 = 의도된 4개 제약 추가** — 본 스프린트의 Java 수정은 운영DB 에 직접 SQL 을 실행하지 않음. 단 splitter fix WAR 배포 + 운영 톰캣 재시작 시점에 DbInitRunner 가 phase2.sql 의 DO 블록을 정상 실행하여 4개 제약을 자동 적용. v2 의 "운영DB 영향 0" 은 v3 에서 "의도된 4개 제약 추가, 외 영향 0" 으로 정정. |
| NFR-6 | 코드 라인 변동 최소화 — splitter 추출 + dollar-quote 분기 외 수정 0. 기존 단일 인용 처리 로직 그대로 보존. |
| NFR-7 (v3 신설) | **사전 데이터 무결성 PASS** — §9 의 사전 검증 SQL 을 양쪽 prod DB 에서 실행. 4개 제약 각각의 위반 row 수 = 0 일 때만 본 스프린트 진행 가능. >0 시 별도 스프린트 분리 (FR-4-C). |
| NFR-8 (v3 신설) | **사후 운영DB 4개 제약 적용 확인** — splitter fix WAR 배포 + 운영 톰캣 재시작 후 양쪽 prod DB 에서 §9 의 사후 검증 SQL 실행. 4개 제약 모두 4/4 적용 확인. 1건이라도 미적용 시 즉시 원인 분석. |

---

## 5. 의사결정 / 우려사항

### 5-1. 외부 SQL parser 도입 검토 — 미채택
- 후보: Spring `ScriptUtils.executeSqlScript()`, Flyway 의 SqlScript, JSQLParser 등.
- 미채택 사유:
  - 본 스프린트 목표는 "최소 침습으로 dollar-quote 만 추가". 외부 parser 도입 시 의존성·동작 차이 회귀 surface 가 splitter fix 본업보다 큼.
  - Spring `ScriptUtils` 는 dollar-quote 를 인식하긴 하지만, 라인 주석·단일 인용 escape 등 현재 splitter 의 동작 일부와 미묘하게 다름 → 회귀 위험.
  - 향후 별도 스프린트 (예: `db-init-runner-modernization`) 에서 일괄 검토 가능.

### 5-2. statement 단위 vs 전체 SQL 한 번에 실행 — 단위 유지
- 후보: 전체 SQL 문자열을 jdbcTemplate.execute() 로 한 번에 실행.
- 미채택 사유:
  - 현재 동작은 statement 단위 — 한 statement 가 실패해도 나머지는 진행 (catch 블록이 DEBUG swallow). 이 모델은 멱등성 가드 (이미 존재 시 무시) 의 기반.
  - 전체 한 번에 실행하면 첫 실패에서 stop → 부팅 안정성 회귀.
- 본 스프린트 = statement 분리 모델 유지, splitter 만 강화.

### 5-3. 태그 dollar-quote 지원 범위
- 본 스프린트는 `$$` (태그 없음) 와 `$tag$` (영숫자/_ 시작) 둘 다 지원 — PG 표준 그대로.
- 후속 스프린트가 실제 사용할 토큰은 `$$` 1종 (V018 / phase1.sql 의 EXCEPTION 패턴 모두 `$$` 사용). 단 `$tag$` 도 동일 비용으로 지원 가능 → 향후 충돌 회피용 (예: 본문 안에 `$$` 리터럴 등장 시).

### 5-4. dollar-quote 인식 알고리즘 (v2 — 상태 기반 단일 패스)

PostgreSQL 표준 dollar-quote 구문:
- 시작: `$tag?$` — `$` 로 시작, tag (선택, ASCII subset = `[A-Za-z_][A-Za-z0-9_]*`), `$` 로 끝
- 종료: 시작과 동일한 토큰 재출현 (대소문자 민감)
- 내부: 모든 문자 그대로 보존 (인용·이스케이프·라인주석 처리 없음 — IN_DOLLAR_QUOTE 상태)

#### 상태머신 (v2)

```
states: NORMAL | IN_LINE_COMMENT | IN_SINGLE_QUOTE | IN_DOLLAR_QUOTE(tag)

transitions (from NORMAL):
  --                  → IN_LINE_COMMENT (현재 char + 다음 char 모두 strip — 라인주석은 statement 본문에 보존 안함)
  '                   → IN_SINGLE_QUOTE (' 자체는 본문 보존)
  $tag$ (boundary OK) → IN_DOLLAR_QUOTE(tag) (토큰 자체는 본문 보존)
  ;                   → emit statement (sb.trim() if not empty), sb = new
  (other)             → append

transitions (from IN_LINE_COMMENT):
  \n                  → NORMAL (개행 자체는 본문에 보존)
  (other)             → strip (본문 추가 안함)

transitions (from IN_SINGLE_QUOTE):
  '' (즉 '' 연속)     → stay (escape — 두 ' 모두 본문 보존, idx + 1 skip)
  '                   → NORMAL (' 본문 보존)
  (other)             → append (-- / $ / ; 모두 무력)

transitions (from IN_DOLLAR_QUOTE(tag)):
  $tag$ (정확 매칭)   → NORMAL (닫힘 토큰 본문 보존)
  (other)             → append (' / -- / ; 모두 무력)
```

#### `$tag$` 토큰 인식 lookahead

NORMAL 상태에서 `$` 발견 시:
1. **boundary 검사** — 직전 문자가 식별자 문자(`[A-Za-z0-9_$]`) 면 dollar-quote 시작 후보 아님 (FR-1-C 보수적 정책). `$` 자체를 본문에 append, 다음 char 진행.
2. **tag 추출** — `$` 다음에 `[A-Za-z_][A-Za-z0-9_]*` 시퀀스를 그리디 매칭, 그 다음에 `$` 가 와야 함.
   - 매칭 성공 + 닫힘 `$` 발견 → tag 확정, IN_DOLLAR_QUOTE(tag) 진입. 토큰 전체 (`$` + tag + `$`) 본문 append.
   - 매칭 실패 (다음 문자가 식별자 시작 문자가 아니면서 `$` 도 아님) → `$` 자체를 본문 append, 다음 char 진행.
   - 빈 tag 케이스: `$` 다음에 바로 `$` 가 오면 tag = "" (즉 `$$`). IN_DOLLAR_QUOTE("") 진입.

#### IN_DOLLAR_QUOTE 의 닫힘 매칭

각 char 마다 `$` 만나면 동일한 tag 패턴 (`$ + tag + $`) 매칭 시도. 정확 매칭이면 NORMAL 복귀. tag 가 다른 dollar-quote 토큰 (예: `$$ ... $foo$ ... $$`) 은 본문 일부로 통과 — IN_DOLLAR_QUOTE 의 본문 raw 보존 정책과 일치.

#### 구현 노트
- 정규식 `Pattern.compile("\\A\\$([A-Za-z_][A-Za-z0-9_]*)?\\$")` 한 번 compile 후 char-by-char lookahead 에 사용 또는 수동 스캔. 개발계획서에서 확정.
- FR-1-C 의 식별자 직후 boundary 는 직전 char 추적 변수 1개로 처리.

### 5-5. 회귀 측정 방법 — 의미적 동치 (v3 — codex 2차 검토 재정의)

v2 의 "변경 전후 statement count + SHA-256 전체 동일" 은 v3 에서 폐기.
이유: phase2.sql 에 DO 블록 2건이 이미 존재하므로 변경 전 splitter 는 DO 블록을 깨서 N개 조각을 만들지만 변경 후 splitter 는 단일 statement 로 합침 → count 와 해시 목록이 자연스럽게 변동 → "동일 보장" 은 회귀가 아닌 의도된 변화를 차단함.

#### v3 의미적 동치 검증 (codex 권고)

3개 게이트 (NFR-3 b/c/d) 의 fixture 와 측정 방법:

**게이트 (b) — DO 블록 단일 보존** (v4 강화 — codex 3차 권고):
- fixture: `src/test/resources/db_init_phase2.sql` 로딩 (또는 `ClassPathResource`).
- **line-range fixture**: phase2.sql:500-511, 528-535 의 원문을 line-range 로 추출. 정규화 (개행 통일 + 좌우 공백 trim) 후 두 블록 텍스트 자체를 fixture 로 보관 (`src/test/resources/db_init_phase2.do-blocks.expected.txt` 또는 인라인 String 상수).
- 측정 — 3가지 모두 PASS:
  - (b1) **DO 블록 본문 정확 매칭**: 변경 후 splitter 출력 statement 중 정확히 2개가 line 500-511 / 528-535 본문 fixture 와 일치 (정규화 후 비교).
  - (b2) **블록 경계 문법 형태**: 해당 2개 statement 가 각각 `DO $$` 로 시작하고 `END $$` 로 종료 (또는 그에 준하는 dollar-quote 토큰 매칭). substring 매칭이 아니라 prefix/suffix 검증.
  - (b3) **블록 외 흘림 검증**: line 500-511, 528-535 의 본문이 다른 statement 로 누설되지 않음 (그 외 statement 들에 해당 본문 substring 없음).
- 단위테스트: `DbInitRunnerTest.realPhase2_PreservesDoBlocksAsSingleStatements()`.

**게이트 (c) — 일반 SQL 순서 유지**:
- fixture: 변경 전 splitter 가 phase2.sql 에서 분리한 statement 목록에서 **DO 블록 관련 조각들을 제외**한 나머지 (단순 CREATE TABLE / ALTER / INSERT) 의 trim + SHA-256 목록.
- 측정: 변경 후 splitter 출력에서도 동일한 단순 statement 들이 동일 순서로 동일 SHA-256 으로 등장하는지.
- DO 블록 관련 조각들의 식별 방법: 변경 전 splitter 가 lines 500-511, 528-535 본문 영역에서 만들어낸 조각들 (개발계획서에서 정확한 line range 베이스라인 측정).
- 단위테스트: `DbInitRunnerTest.realPhase2_NonDoStatementsUnchanged()`.

**게이트 (d) — PG 파서 기준 실행 가능**:
- fixture: ephemeral PG 클러스터 (phase1 스프린트에서 보존한 `localhost:25880`) 또는 단위테스트용 임베디드 PG (없으면 본 검증은 통합 단계로).
- 측정: 변경 후 splitter 출력의 모든 statement 를 jdbcTemplate.execute() 로 순차 실행 → 각 statement 가 PG syntax error 없이 통과 (catch DEBUG 로그 0).
- 본 게이트는 단위테스트로는 cover 어려움 → 통합테스트 또는 ephemeral 검증 단계 (개발계획서 §검증 매트릭스 T-A) 에서 수행.

#### 드리프트 회피
- phase2.sql 이 변경되면 fixture 도 갱신 필요. 본 fixture 의 갱신 책임은 phase2.sql 을 수정하는 PR 에 부여 (체크 가능: CI 또는 review 시점).
- 단위테스트가 `ClassPathResource("db_init_phase2.sql")` 로 실제 파일 로드 (test scope) → fixture 의 phase2.sql 텍스트 자체는 자동 동기.
- "DO 블록 line range" 만 별도 fixture 화 — 개발계획서에서 정의.

### 5-6. DB팀 자문 결과 (본문 인라인)

| 자문 | 의견 |
|------|------|
| splitter 가 dollar-quote 모드 진입 후 단일 인용 처리 비활성 — PG 표준과 일치하는가 | ✅ 일치. PG 의 dollar-quote 내부는 raw 텍스트로 보존, 단일 인용 의미 없음. |
| 후속 스프린트가 phase2.sql 에 DO 블록 추가 시 jdbcTemplate.execute() 가 단일 procedural 블록을 정상 실행하는가 | ✅ JdbcTemplate.execute(String) → java.sql.Statement.execute() 로 단일 SQL 텍스트 실행. PG JDBC 가 procedural 블록 그대로 서버에 전달 → 정상 실행. |
| 본 변경 후 운영DB 에 phase2.sql 재실행 시 회귀 0 보장 가능한가 | ✅ NFR-3/NFR-4 가 게이트. 현 phase2.sql 의 statement 수·실행 결과 동일. |

---

## 6. 영향 범위 (v3)

| 계층 | 파일 | 유형 |
|------|------|------|
| 부팅 로직 | `src/main/java/com/swmanager/system/config/DbInitRunner.java` | 수정 (splitter 추출 + dollar-quote 인식 + line-comment pre-pass 제거) |
| 단위테스트 | `src/test/java/com/swmanager/system/config/DbInitRunnerTest.java` | 신규 (UT-1~UT-15) |
| 문서 | `docs/product-specs/dbinitrunner-dollar-quote-aware.md` | 신규 (본 문서) |
| 문서 | `docs/exec-plans/dbinitrunner-dollar-quote-aware.md` | 신규 (개발계획) |
| 문서 | `docs/PLANS.md` | 수정 (스프린트 표 추가 + **후속 백로그 등록 — v5: 자동 DDL 모델 개선 스프린트** — codex 4차 권고) |
| **운영DB (구 + 신규)** | `211.104.137.55:5881` + `192.168.10.194:5880` | **자동 변경** — splitter fix WAR 배포 + 운영 톰캣 재시작 시점에 4개 제약 자동 추가 (`fk_tb_document_org_unit`, `ck_tb_document_support_target_type`, `ck_tb_document_environment`, `fk_tb_document_region_code`) |

**합계**: 신규 3, 수정 2 (Java + 문서). **SQL 파일 변경 0**. **운영DB 의도된 변화** = 4개 제약 자동 추가 (양쪽 prod). **서버 부팅 동작 변화** = phase2.sql 의 DO 블록 2건 정상 실행됨 (NFR-3/NFR-4 가 게이트).

---

## 7. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 (v3) | splitter 변경이 일반 SQL statement (DO 블록 외) 의 분리 결과를 변경 (회귀) | 매우 낮음 | NFR-3 (c) 의미적 동치 게이트. DO 블록 외 단순 statement 의 trim + SHA-256 목록이 변경 전후 동일 보장. |
| R-2 (v2) | dollar-quote 시작 토큰 (`$tag$`) 의 tag 매칭 / boundary / 대소문자 / 미종료 처리 결함 | 낮음 | UT-5/UT-6/UT-7/**UT-13/UT-14/UT-15** + 상태머신 §5-4 명문화 + 코드 리뷰. 특히 IN_DOLLAR_QUOTE 의 라인주석/단일인용 무력화는 UT-13 으로 검증 |
| R-3 | 단일 인용 안의 `$$` 리터럴이 dollar-quote 로 오인식 | 낮음 | UT-9 가 게이트 |
| R-4 | 미종료 dollar-quote 입력에서 splitter 가 무한루프 또는 OOM | 매우 낮음 | UT-10. 마지막까지 스캔 후 단일 statement 로 묶어 종료 — panic 없음 |
| R-5 | jdbcTemplate.execute() 가 DO `$$` 블록을 단일 텍스트로 받아 정상 실행 못함 | 매우 낮음 | DB팀 자문 §5-6 ②. PG JDBC 표준. 후속 스프린트 ephemeral 검증 단계에서 재확인 |
| R-6 | 태그 dollar-quote (`$plpgsql$` 등) 의 tag 시작 문자가 `$` 바로 다음에 숫자로 시작 | 매우 낮음 | PG 표준은 `[A-Za-z_][A-Za-z0-9_]*` — splitter 도 동일 패턴 |
| R-7 | 단위테스트 fixture 의 phase2.sql 이 실제 파일과 drift | 낮음 | UT-12 는 `ClassPathResource` 로 실제 phase2.sql 로드 (test scope) — 항상 최신 |
| R-8 (v3 신설) | splitter fix 적용 후 첫 부팅 시 4개 FK 제약 추가가 기존 데이터 무결성 위반으로 ALTER 실패 (`fk_tb_document_org_unit`: `tb_document.org_unit_id` 가 `tb_org_unit.unit_id` 외 / `fk_tb_document_region_code`: `tb_document.region_code` 가 `sigungu_code.adm_sect_c` 외) | 중간 | **§9 사전 검증 SQL** (NFR-7) — 위반 row 수 = 0 확인 후 진행. >0 시 별도 스프린트 분리 (FR-4-C). DO 블록의 `IF NOT EXISTS` 가드는 제약 존재 여부만 체크 — 데이터 무결성은 별도. |
| R-9 (v3 신설) | splitter fix 적용 후 첫 부팅 시 2개 CHECK 제약 추가가 기존 데이터 무결성 위반으로 ALTER 실패 (`ck_tb_document_support_target_type`: 'EXTERNAL'/'INTERNAL' 외 / `ck_tb_document_environment`: 'PROD'/'TEST' 외) | 중간 | §9 사전 검증 SQL (NFR-7) — 위반 row 수 = 0 확인 후 진행. >0 시 별도 스프린트 분리. |
| R-10 (v5 강화) | 자동 적용 timing — splitter fix 머지 후 다음 운영 톰캣 재시작 시점에 4개 제약이 한꺼번에 자동 적용. 운영자가 의도한 시점에만 적용하고 싶다면 어떻게? | 낮음 | 본 스프린트는 "자동 적용" 을 의도된 동작으로 명시 (§2-1). 톰캣 재시작 시점은 운영자가 통제 — splitter fix WAR 배포 + 재시작을 동일 maintenance window 에 묶음 권장. **배포 PR 본문 / 릴리즈 체크리스트 명시 의무화 (codex 3/4차 권고) — 4항목**: (i) "재시작 시 DbInitRunner 가 운영DB 에 4개 제약 자동 적용" 위험 고지, (ii) "사전 검증 SQL §9-1 실행 + 4건 모두 0 PASS 확인" 절차, **(iii) v5 추가 — "롤백은 WAR 되돌림만으로는 제약 DROP 안 됨" 명시. 롤백 필요 시 별도 DROP CONSTRAINT SQL 수동 실행 절차 제공**, **(iv) v5 추가 — 사후 모니터링: 톰캣 재시작 후 catalina.out / server.log 에서 DbInitRunner 의 DEBUG/WARN 로그 확인 + §9-2 사후 검증 SQL 실행으로 4/4 적용 확인.** 추가 안전장치 (수동 toggle / feature flag) 는 본 스프린트 범위 외 — 자동 DDL 모델 자체 개선 후속 스프린트로 분리. |
| R-11 (v3 — 사후) | 본 스프린트 적용 후 운영DB 에 4개 제약이 적용된 상태에서 새로운 비정상 데이터 입력 시 INSERT 실패 → 사용자 워크플로우 차단 | 낮음 | 의도된 동작 — 데이터 정합성을 위해 입력 단계 차단이 맞음. 실제 입력 경로 (Controller / Service) 에서 사용자 친화적 에러 메시지 제공은 별도 검토 항목. |
| R-12 (v3 잔존) | 블록 주석 `/* ... */` 미인식 — 향후 phase2.sql 에 블록 주석 추가 시 splitter 가 깨질 가능성 | 낮음 | 본 스프린트 범위 외. 별도 스프린트로 처리. 현 phase2.sql / V*.sql 모두 블록 주석 부재 확인 |
| R-13 (v3 잔존) | non-ASCII dollar-tag (예: `$한글$`) 미인식 | 매우 낮음 | 본 스프린트 범위 외. 운영 SQL 에서 사용 사례 부재 |
| R-14 (v3 잔존) | `U&'..'` (Unicode escape) / `E'..'` (C-style escape) 등 PG 확장 string literal 미인식 — 본 splitter 가 단일 인용 시작으로만 처리 | 매우 낮음 | 본 스프린트 범위 외. phase2.sql 에 사용 사례 부재 |

---

## 8. 산출물 요약

본 스프린트 종료 시:
- `DbInitRunner.java` — splitter 로직이 `splitSqlStatements(String)` 로 추출 + dollar-quote 인식 추가
- `DbInitRunnerTest.java` — 신규 단위테스트 12건
- `docs/product-specs/dbinitrunner-dollar-quote-aware.md` — 본 기획서 (codex 검토 후 v2 가능)
- `docs/exec-plans/dbinitrunner-dollar-quote-aware.md` — 개발계획 (구현 단계 + 테스트 픽스처 + 회귀 검증)
- `docs/PLANS.md` — 스프린트 완료 표기

검증 결과:
- `./mvnw test` PASS (UT-1~UT-12, 12/12)
- 서버 1회 재시작 — `DB 초기화 완료: N개 SQL 실행` 의 N 이 변경 전 동일
- 기능 스모크 (점검내역서/사업관리/견적서) 정상

---

## 9. 데이터 무결성 사전·사후 검증 SQL (v3 신설)

### 9-1. 사전 검증 (구현 전 / NFR-7)

양쪽 prod DB (구 `211.104.137.55:5881` + 신규 `192.168.10.194:5880`) 에서 read-only 실행. 4개 제약 각각의 위반 row 수 측정.

v5: 모든 SELECT 의 FROM/EXISTS 절을 `public.` 스키마 명시로 통일 (codex 4차 권고).

```sql
-- (a) fk_tb_document_org_unit 위반 row
-- tb_document.org_unit_id 가 NULL 도 아니고 tb_org_unit 에도 없는 경우
SELECT COUNT(*) AS violation_fk_org_unit
  FROM public.tb_document d
 WHERE d.org_unit_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM public.tb_org_unit o WHERE o.unit_id = d.org_unit_id);

-- (b) fk_tb_document_region_code 위반 row
-- tb_document.region_code 가 NULL 도 아니고 sigungu_code 에도 없는 경우
-- 단 phase2.sql:531 의 DO 블록은 sigungu_code 테이블 존재 여부도 체크하므로 sigungu_code 미존재 환경은 별도
-- v4: information_schema.tables → to_regclass('public.sigungu_code') (스키마 명시 — codex 3차 권고)
-- v5: FROM 도 public. 스키마 명시 (codex 4차 권고)
SELECT COUNT(*) AS violation_fk_region_code
  FROM public.tb_document d
 WHERE d.region_code IS NOT NULL
   AND to_regclass('public.sigungu_code') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM public.sigungu_code s WHERE s.adm_sect_c = d.region_code);

-- (c) ck_tb_document_support_target_type 위반 row
-- support_target_type 이 NULL / 'EXTERNAL' / 'INTERNAL' 외 값
SELECT COUNT(*) AS violation_ck_support_target_type
  FROM public.tb_document
 WHERE support_target_type IS NOT NULL
   AND support_target_type NOT IN ('EXTERNAL','INTERNAL');

-- (d) ck_tb_document_environment 위반 row
-- environment 가 NULL / 'PROD' / 'TEST' 외 값
SELECT COUNT(*) AS violation_ck_environment
  FROM public.tb_document
 WHERE environment IS NOT NULL
   AND environment NOT IN ('PROD','TEST');
```

**기대 결과**: 4건 모두 0.

**위반 row > 0 시 분기**: 본 스프린트 일시 중단 → 별도 스프린트 (`tb-document-data-integrity-recovery` 등) 로 위반 row 정리 → 정리 완료 후 본 스프린트 재개.

### 9-2. 사후 검증 (NFR-8)

splitter fix WAR 배포 + 운영 톰캣 재시작 후 양쪽 prod DB 에서 read-only 실행:

```sql
SELECT conname, contype
  FROM pg_constraint
 WHERE conname IN ('fk_tb_document_org_unit',
                   'ck_tb_document_support_target_type',
                   'ck_tb_document_environment',
                   'fk_tb_document_region_code')
 ORDER BY conname;
```

**기대 결과**: 4행 출력 (4/4 적용). contype: f / c / c / f.

**1건이라도 미적용 시**: catalina.out 또는 server.log 에서 DbInitRunner 의 DEBUG/WARN 로그 분석 → splitter 또는 PG 단계 원인 파악.

---

## 10. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 전 확인 사항
- [x] 후속 스프린트 (`phase2-V018-init-ordering`) 의 진입 조건임을 명시
- [x] **운영 결함 자동 수정 의미 격상 (v3)** — phase2.sql 의 DO 블록 2건이 양쪽 prod DB 에서 사일런트 미적용 중인 사실 확인 + 본 스프린트가 자동 수정함을 명시
- [x] UI 키워드 0 → 디자인팀 자문 skip
- [x] DB팀 자문 본문 인라인 (§5-6)
- [x] 단위테스트 매트릭스 **15건 (UT-1~UT-15)** (v2)
- [x] codex 1차 검토 결과 반영 (v2 — 상태 기반 모델 + tag 규칙 정확화 + UT 보강)
- [x] codex 2차 검토 결과 반영 (v3 — 사실 정정 + 회귀 게이트 의미적 동치 재정의 + 운영 검증 절차)
- [x] 데이터 무결성 사전 검증 SQL §9 인라인 (NFR-7) + 사후 검증 §9-2 (NFR-8)
- [x] 잔존 위험 명시 (R-12 블록 주석 / R-13 non-ASCII tag / R-14 PG string literal 확장)
- [x] codex 3차 검토 결과 반영 (v4 — §10 → §9 정합 / NFR-3 (b) line-range fixture / NFR-7 to_regclass / R-10 PR 체크리스트 / 자동 DDL 모델 개선 시점 권고)
- [x] codex 4차 검토 결과 반영 (v5 — NFR-3(b) 표현 강화 / §9-1 SQL public 스키마 명시 / R-10 체크리스트 4항목 확장 / PLANS.md 후속 백로그 등록)
- [ ] codex 5차 검토 (v5 기준 — 마무리 승인 판정)
- [ ] **사전 검증 SQL 양쪽 prod 실행 (NFR-7 PASS 확인)** — 본 스프린트 진입 게이트

### 다음 절차
1. codex 5차 검토 → 사용자 "최종승인"
2. **§9-1 사전 검증 SQL 양쪽 prod 실행** — 위반 row 4건 모두 0 확인. >0 시 별도 스프린트 분리 (FR-4-C).
3. **[개발팀]** 개발계획서 작성 (`docs/exec-plans/dbinitrunner-dollar-quote-aware.md`)
