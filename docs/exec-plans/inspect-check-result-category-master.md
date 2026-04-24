---
tags: [dev-plan, sprint, inspect, master, normalization, wave-3]
sprint: "inspect-check-result-category-master"
status: draft
created: "2026-04-22"
---

# [개발계획서] inspect category 마스터 신설 (S10)

- **작성팀**: 개발팀
- **작성일**: 2026-04-22
- **근거 기획서**: [[inspect-check-result-category-master]] v2.1 (사용자 최종승인 2026-04-22)
- **선행**: S1 `inspect-comprehensive-redesign` 완료 (check_section_mst 9행 + FK 적용)
- **상태**: v2 (codex v1 ⚠수정필요 6건 반영)
- **v1→v2 변경점**:
  1. Phase 0 `bad_orph` 블록 제거 — Phase 3 Exit Gate 에서 동일 체크 수행 (중복 회피 + 게이트 실효성)
  2. 시드 `정확히 16행` 고정 (Step 1 precheck 로 AP 4번째 한글 label 확정 후 V023 최종 확정)
  3. 검증 기준 `>= 14` → `= 16` 통일 (NFR-1 일치)
  4. `InspectCategoryConstraintTest` **필수** 승격 (FR-4 회귀 보장)
  5. Phase 1 **역변환 rollback SQL** 명시 (공백 O → 공백 X 복원)
  6. db_init_phase2.sql 정리는 **Edit 툴** 사용 (CLAUDE.md 도구 우선순위 준수, sed 지양)

---

## 0. 전제 / 환경

### 0-1. 기획 결정 4건 (권장안 반영)
- A-opt1: `check_category_mst (section_code, category_code)` 복합키
- B-opt1: 공백 O 버전으로 통일 (`GeoNURIS GeoWeb Server (GWS)` / `(GSS)`)
- C-opt1: `inspect_template` + `inspect_check_result` 양쪽 FK
- NULL 정책: `inspect_template` NOT NULL / `inspect_check_result` FK 만

### 0-2. 실측 고정값 (Step 1 precheck 로 재확인)
- inspect_template.category distinct 예상 16종 (공백 통일 후)
- 공백 변형 4행 UPDATE 대상 (GeoWeb X 3 + Spatial X 3 = 6건 추정, precheck 확정)
- inspect_check_result 현재 0 rows (S1 TRUNCATE). 배포 시점 비0 가능성 대비

### 0-3. 롤백 기준 SHA
- Step 0 에서 `git rev-parse HEAD` 기록

---

## 1. 작업 순서

### Step 0 — 기준 SHA 기록

```bash
BASE_SHA=$(git rev-parse HEAD)
```

### Step 1 — 사전검증 (Phase 0 게이트)

**1-1. 러너**: `docs/dev-plans/inspect-category-precheck.java` (S5/S1 패턴 복제)

쿼리:
```sql
-- (a) inspect_template.category distinct + 공백 변형 검출
SELECT section, category, COUNT(*)
  FROM inspect_template
  GROUP BY section, category ORDER BY section, category;

-- (b) 공백 변형 정확 검출 (GeoWeb + Spatial 양 쌍)
SELECT 'it', category, COUNT(*) FROM inspect_template
 WHERE category IN ('GeoNURIS GeoWeb Server(GWS)', 'GeoNURIS Spatial Server(GSS)')
 GROUP BY category;
SELECT 'icr', category, COUNT(*) FROM inspect_check_result
 WHERE category IN ('GeoNURIS GeoWeb Server(GWS)', 'GeoNURIS Spatial Server(GSS)')
 GROUP BY category;

-- (c) NULL 카운트 (inspect_template 전환 전)
SELECT 'it_null', COUNT(*) FROM inspect_template WHERE category IS NULL;

-- (d) icr 기존 category 값이 마스터 시드 후보 16종에 모두 포함되는지
SELECT DISTINCT category FROM inspect_check_result
 WHERE category IS NOT NULL;
```

**Exit Gate 1**: HALT 조건
- `inspect_template.category IS NULL` > 0 → 시드 불가, Step 2 전 수동 정리 필요
- inspect_check_result 에 마스터 16종 외 값 발견 → 마스터 시드 확장 or 추가 UPDATE

### Step 2 — V023 마이그레이션 SQL

**파일**: `swdept/sql/V023_inspect_category_master.sql`

```sql
BEGIN;

-- Phase 0: NULL 금지 확인 (NOT NULL 전환을 위한 전제)
DO $$
DECLARE null_it bigint;
BEGIN
  SELECT COUNT(*) INTO null_it FROM inspect_template WHERE category IS NULL;
  IF null_it > 0 THEN RAISE EXCEPTION 'HALT Phase 0: inspect_template.category NULL %', null_it; END IF;
  RAISE NOTICE 'Phase 0 PASS: inspect_template.category NULL=0';
END $$ LANGUAGE plpgsql;
-- 주: 마스터 불일치 체크는 Phase 3 Exit Gate 에서 일괄 수행

-- Phase 1: 공백 변형 → 공백 O 통일 (양 테이블, 멱등)
UPDATE inspect_template
   SET category = 'GeoNURIS GeoWeb Server (GWS)'
 WHERE category = 'GeoNURIS GeoWeb Server(GWS)';
UPDATE inspect_template
   SET category = 'GeoNURIS Spatial Server (GSS)'
 WHERE category = 'GeoNURIS Spatial Server(GSS)';

UPDATE inspect_check_result
   SET category = 'GeoNURIS GeoWeb Server (GWS)'
 WHERE category = 'GeoNURIS GeoWeb Server(GWS)';
UPDATE inspect_check_result
   SET category = 'GeoNURIS Spatial Server (GSS)'
 WHERE category = 'GeoNURIS Spatial Server(GSS)';

-- Phase 1 사후: 공백 X 변형 0 확인
DO $$
DECLARE c bigint;
BEGIN
  SELECT (SELECT COUNT(*) FROM inspect_template
           WHERE category IN ('GeoNURIS GeoWeb Server(GWS)','GeoNURIS Spatial Server(GSS)'))
       + (SELECT COUNT(*) FROM inspect_check_result
           WHERE category IN ('GeoNURIS GeoWeb Server(GWS)','GeoNURIS Spatial Server(GSS)'))
    INTO c;
  IF c <> 0 THEN RAISE EXCEPTION 'HALT Phase 1: 공백 변형 잔존 %', c; END IF;
  RAISE NOTICE 'Phase 1 PASS: 공백 변형 0';
END $$ LANGUAGE plpgsql;

-- Phase 2: check_category_mst 생성 + 16행 시드 (정확한 한글 label 은 Step 1 precheck 후 확정)
CREATE TABLE IF NOT EXISTS check_category_mst (
  section_code   VARCHAR(20)  NOT NULL REFERENCES check_section_mst(section_code),
  category_code  VARCHAR(50)  NOT NULL,
  category_label VARCHAR(100) NOT NULL,
  display_order  INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (section_code, category_code)
);

INSERT INTO check_category_mst (section_code, category_code, category_label, display_order) VALUES
  -- AP (4종, Step 1 precheck 에서 정확 채록 — 아래 4번째 '##AP4##' 자리에 실측값 삽입)
  ('AP', 'H/W', 'H/W', 1),
  ('AP', 'OS',  'OS',  2),
  ('AP', '백업', '백업', 3),
  ('AP', '##AP4##', '##AP4##', 4),
  -- DB (7종)
  ('DB', 'DATA 영역', 'DATA 영역', 11),
  ('DB', '버전 정보', '버전 정보', 12),
  ('DB', '네트워크', '네트워크', 13),
  ('DB', '로그', '로그', 14),
  ('DB', '부팅 정보', '부팅 정보', 15),
  ('DB', '하드웨어', '하드웨어', 16),
  ('DB', '프로세스', '프로세스', 17),
  -- DBMS
  ('DBMS', '오라클', '오라클', 21),
  -- GIS (4종, 공백 O 버전 고정)
  ('GIS', 'GeoNURIS Desktop Pro', 'GeoNURIS Desktop Pro', 31),
  ('GIS', 'GeoNURIS GeoWeb Server (GWS)', 'GeoNURIS GeoWeb Server (GWS)', 32),
  ('GIS', 'GeoNURIS Spatial Server (GSS)', 'GeoNURIS Spatial Server (GSS)', 33),
  ('GIS', '클라이언트 프로그램', '클라이언트 프로그램', 34)
ON CONFLICT (section_code, category_code) DO NOTHING;

-- Phase 3: Exit Gate 2 — 양 테이블 (section, category) 가 전부 마스터에 존재
DO $$
DECLARE bad_it bigint; bad_icr bigint;
BEGIN
  SELECT COUNT(*) INTO bad_it FROM inspect_template
   WHERE (section, category) NOT IN (SELECT section_code, category_code FROM check_category_mst);
  IF bad_it > 0 THEN RAISE EXCEPTION 'HALT Phase 3: inspect_template 불일치 % — 시드 보강 필요', bad_it; END IF;

  SELECT COUNT(*) INTO bad_icr FROM inspect_check_result
   WHERE category IS NOT NULL
     AND (section, category) NOT IN (SELECT section_code, category_code FROM check_category_mst);
  IF bad_icr > 0 THEN RAISE EXCEPTION 'HALT Phase 3: inspect_check_result 불일치 %', bad_icr; END IF;

  RAISE NOTICE 'Phase 3 Exit Gate 2 PASS: 양 테이블 마스터 정합';
END $$ LANGUAGE plpgsql;

-- Phase 4: FK ADD (복합키, 멱등)
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_it_category') THEN
    ALTER TABLE inspect_template
      ADD CONSTRAINT fk_it_category
      FOREIGN KEY (section, category)
      REFERENCES check_category_mst(section_code, category_code);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_icr_category') THEN
    ALTER TABLE inspect_check_result
      ADD CONSTRAINT fk_icr_category
      FOREIGN KEY (section, category)
      REFERENCES check_category_mst(section_code, category_code);
  END IF;
END $$ LANGUAGE plpgsql;

-- Phase 5: inspect_template.category NOT NULL 전환 (기획 FR-4)
DO $$
DECLARE null_it bigint;
BEGIN
  SELECT COUNT(*) INTO null_it FROM inspect_template WHERE category IS NULL;
  IF null_it > 0 THEN RAISE EXCEPTION 'HALT Phase 5: NOT NULL 전환 불가 — NULL row %', null_it; END IF;
END $$ LANGUAGE plpgsql;

ALTER TABLE inspect_template ALTER COLUMN category SET NOT NULL;

-- Phase 6: 사후 검증 (NFR-1 일치: 정확히 16행)
DO $$
DECLARE mst_cnt bigint; fk_it bigint; fk_icr bigint; null_it_final bigint;
BEGIN
  SELECT COUNT(*) INTO mst_cnt FROM check_category_mst;
  IF mst_cnt <> 16 THEN RAISE EXCEPTION 'HALT final: mst=% (expected 16)', mst_cnt; END IF;

  SELECT COUNT(*) INTO fk_it  FROM pg_constraint WHERE conname='fk_it_category';
  SELECT COUNT(*) INTO fk_icr FROM pg_constraint WHERE conname='fk_icr_category';
  IF fk_it <> 1 OR fk_icr <> 1 THEN RAISE EXCEPTION 'HALT final: FK missing'; END IF;

  SELECT COUNT(*) INTO null_it_final FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_template'
     AND column_name='category' AND is_nullable='YES';
  IF null_it_final <> 0 THEN RAISE EXCEPTION 'HALT final: inspect_template.category still nullable'; END IF;

  RAISE NOTICE 'PASS: inspect-category-master V023 applied (mst=%)', mst_cnt;
END $$ LANGUAGE plpgsql;

COMMIT;
```

**주**: 한글 label 정확값은 Step 1 precheck 결과 파일에서 재확인 후 V023 최종 확정.

### Step 3 — `db_init_phase2.sql` 시드 정리 (FR-5)

Step 1 precheck 로 실측 확인 후 **Edit 툴** (CLAUDE.md 도구 우선순위) 로 공백 X 2행을 공백 O 로 변경 or 삭제. `sed`/`awk` 사용 지양.

### Step 4 — 테스트 작성

**파일 1**: `src/test/java/com/swmanager/system/arch/CheckCategoryMstArchTest.java`
- JdbcTemplate 또는 통합 테스트로:
  - `check_category_mst` row 수 **= 16** (NFR-1 정확 일치)
  - 공백 X row 0 (양 테이블)
  - FK `fk_it_category`, `fk_icr_category` 존재

**파일 2 (v2 필수)**: `src/test/java/com/swmanager/system/sql/InspectCategoryConstraintTest.java`
- `@SpringBootTest` + `@Sql`(또는 JdbcTemplate):
  - NULL insert 시도 → inspect_template 저장 실패 (FR-4)
  - 마스터 외 category 값 insert 시도 → FK 위반 예외 발생 (양 테이블)
- **필수**: FR-4 (NOT NULL) + FR-3 (FK) 회귀를 직접 보장

### Step 5 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q clean compile
./mvnw -q test
bash server-restart.sh
```

- `/document/inspect-detail/{id}` 렌더링 정상 확인

### Step 6 — 로드맵 정정 (T-LINK) + 커밋/푸시

`docs/plans/data-architecture-roadmap.md` §S10 ✅ 완료 표기.

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | Precheck | Step 1 러너 | Exit Gate 1 PASS |
| T2 | V023 실행 성공 | SQL notice | `PASS: inspect-category-master V023 applied` |
| T3 | check_category_mst 행수 | SQL | **= 16 정확 일치** (NFR-1) |
| T4 | 공백 X 2쌍 잔존 0 | `rg "(GeoNURIS GeoWeb Server|GeoNURIS Spatial Server)\(G[WS]S\)"` | 0 |
| T5 | FK 2개 존재 | pg_constraint | fk_it_category + fk_icr_category |
| T6 | NOT NULL 적용 | information_schema | inspect_template.category is_nullable='NO' |
| T7 | db_init_phase2.sql 공백X 제거 | `rg` | 0 |
| T8 | 컴파일 | mvnw compile | BUILD SUCCESS |
| T9 | 전체 테스트 | mvnw test | green |
| T10 | 서버 기동 | restart | HTTP 200 |
| T11 | 멱등성 | V023 2회차 | 동일 NOTICE + 영향 0 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Precheck FAIL | V023 미실행, 재계획 |
| Phase 1/3/5 EXCEPTION | 자동 ROLLBACK |
| Phase 4 FK ADD 후 앱 오류 | `ALTER TABLE ... DROP CONSTRAINT` 후 Java revert |
| 전체 롤백 | `V023_rollback.sql` — 역순 실행:<br>1) `ALTER TABLE inspect_template ALTER COLUMN category DROP NOT NULL;`<br>2) `ALTER TABLE inspect_template DROP CONSTRAINT IF EXISTS fk_it_category;`<br>3) `ALTER TABLE inspect_check_result DROP CONSTRAINT IF EXISTS fk_icr_category;`<br>4) `DROP TABLE IF EXISTS check_category_mst;`<br>5) **Phase 1 역변환** (선택, 복원이 필요할 때만):<br>  `UPDATE inspect_template SET category='GeoNURIS GeoWeb Server(GWS)' WHERE category='GeoNURIS GeoWeb Server (GWS)';`<br>  `UPDATE inspect_template SET category='GeoNURIS Spatial Server(GSS)' WHERE category='GeoNURIS Spatial Server (GSS)';`<br>  `UPDATE inspect_check_result SET category='GeoNURIS GeoWeb Server(GWS)' WHERE category='GeoNURIS GeoWeb Server (GWS)';`<br>  `UPDATE inspect_check_result SET category='GeoNURIS Spatial Server(GSS)' WHERE category='GeoNURIS Spatial Server (GSS)';` |

**롤백 기준 SHA**: __________ (Step 0에서 기록)

---

## 4. 파일 변경 요약

### 신규
- `swdept/sql/V023_inspect_category_master.sql`
- `swdept/sql/V023_rollback.sql` (보관)
- `docs/dev-plans/inspect-category-precheck.java`, `-precheck-result.md`
- `src/test/java/com/swmanager/system/arch/CheckCategoryMstArchTest.java`
- `src/test/java/com/swmanager/system/sql/InspectCategoryConstraintTest.java` (**v2 필수**)

### 수정
- `src/main/resources/db_init_phase2.sql` (공백 X 2행 제거·통일)

### 변경 없음 (본 스프린트 범위 외)
- Entity/DTO — 현재도 `category` 필드 그대로. FK 제약은 DB 레벨로 충분
- UI 템플릿 — 향후 드롭다운 대체는 별도 스프린트

---

## 5. 승인 요청

### 승인 확인 사항
- [ ] §0-1 기획 결정 4건 반영
- [ ] §1 Step 0~6 흐름
- [ ] §2 T1~T11
- [ ] §3 롤백 (V023_rollback.sql 보관)
- [ ] §4 파일 변경 범위 (Entity 수정 없음)
