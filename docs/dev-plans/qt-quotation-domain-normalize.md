---
tags: [dev-plan, sprint, quotation, master, enum, wave-3]
sprint: "qt-quotation-domain-normalize"
status: draft
created: "2026-04-22"
---

# [개발계획서] qt_quotation category 마스터 + Enum (S8, 옵션 B)

- **작성팀**: 개발팀
- **작성일**: 2026-04-22
- **근거 기획서**: [[qt-quotation-domain-normalize]] v2.1 (사용자 최종승인 2026-04-22)
- **상태**: v2 (codex v1 ⚠수정필요 3건 반영)
- **v1→v2 변경점**:
  1. CLAUDE.md 준수 — Step 9 커밋/푸시는 사용자 `작업완료` 발화 후에만 수행 명시
  2. 리터럴 스캔 2단계 분리 — 정확 일치 + 포함 문자열 + HwpxExportService 판정표 산출물
  3. Arch 테스트 클래스명 `QuotationLiteralArchTest` 로 통일

---

## 0. 전제 / 환경

### 0-1. 기획 결정 반영 (권장안)
- S-opt1 Enum (`QtCategory`)
- V-opt1 한글 PK 유지 (유지보수/용역/제품)
- 범위 축소: category 만, status/template_type 제외

### 0-2. 실측 고정값 (2026-04-22)
- `qt_quotation.category` 분포: 유지보수 44 / 용역 12 / 제품 5 (= 61 rows)
- Java 리터럴 사용처:
  - `QuotationService.java:58, 78` — 견적번호 prefix 매핑 `Map.of("용역","SQ","제품","PQ","유지보수","MQ")`
  - `QuotationService.java:385-390` — 통계 집계 (6 리터럴)
  - `HwpxExportService.java:697` — 문자열 제거 (별개 용도, 치환 제외 검토)
- Thymeleaf 템플릿 4개 파일 (quotation-form / ledger / list / pattern-list) 포함

### 0-3. 롤백 기준 SHA — Step 0 기록

---

## 1. 작업 순서

### Step 0 — 기준 SHA 기록

```bash
BASE_SHA=$(git rev-parse HEAD)
```

### Step 1 — Java 리터럴 사용처 전수 조사 (Precheck, 2단계)

**1-1. 정확 일치 스캔** (category Enum 치환 대상):
```bash
rg -n '"(유지보수|용역|제품)"(?![가-힣])' src/main/java \
  > docs/dev-plans/s8-literal-scan-exact.txt
```
→ QuotationService 8건 + HwpxExportService 가능성

**1-2. 포함 문자열 스캔** (복합 문자열, 치환 **금지** 영역 판정) — **3 카테고리 전부**:
```bash
rg -n '(유지보수|용역|제품)([가-힣\w])' src/main/java \
  > docs/dev-plans/s8-literal-scan-partial.txt
```
→ 예: `유지보수책임기술자` (HwpxExportService:704), `용역업체` / `제품명` 같은 도메인 별개 복합어는 **치환 제외**

**1-3. HwpxExportService 판정표 작성**:

`docs/dev-plans/s8-hwpx-literal-decision.md`:

| line | 리터럴 | 판정 | 사유 |
|------|--------|------|------|
| 697 | "유지보수" (단독) | 치환 or 유지 | (수동 검토 후 결정) |
| 704 | "유지보수책임기술자" | **유지** | 별도 도메인 (기술자 역할명) |
| … | … | … | … |

**Exit Gate 1**: 판정표 완료 + 치환 대상 확정 + 제외 항목 주석 사유 기재

### Step 2 — V024 마이그레이션 SQL

**파일**: `swdept/sql/V024_qt_category_master.sql`

```sql
BEGIN;

-- Phase 0: 현황 재확인
DO $$
DECLARE bad bigint;
BEGIN
  SELECT COUNT(*) INTO bad FROM qt_quotation
   WHERE category NOT IN ('유지보수','용역','제품');
  IF bad > 0 THEN RAISE EXCEPTION 'HALT Phase 0: 예상 외 category %', bad; END IF;
  RAISE NOTICE 'Phase 0 PASS';
END $$ LANGUAGE plpgsql;

-- Phase 1: qt_category_mst CREATE + 3행 시드
CREATE TABLE IF NOT EXISTS qt_category_mst (
  category_code  VARCHAR(10) PRIMARY KEY,
  category_label VARCHAR(50) NOT NULL,
  display_order  INT NOT NULL DEFAULT 0
);

INSERT INTO qt_category_mst (category_code, category_label, display_order) VALUES
  ('유지보수', '유지보수', 1),
  ('용역',     '용역',     2),
  ('제품',     '제품',     3)
ON CONFLICT (category_code) DO NOTHING;

-- Phase 2: Exit Gate — qt_quotation.category 전부 마스터 존재
DO $$
DECLARE bad bigint;
BEGIN
  SELECT COUNT(*) INTO bad FROM qt_quotation
   WHERE category NOT IN (SELECT category_code FROM qt_category_mst);
  IF bad > 0 THEN RAISE EXCEPTION 'HALT Phase 2: 마스터 외 값 %', bad; END IF;
  RAISE NOTICE 'Phase 2 Exit Gate PASS';
END $$ LANGUAGE plpgsql;

-- Phase 3: FK ADD (명시적 멱등, 대상 테이블 한정)
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
     WHERE conname='fk_qt_category'
       AND conrelid='qt_quotation'::regclass
  ) THEN
    ALTER TABLE qt_quotation
      ADD CONSTRAINT fk_qt_category
      FOREIGN KEY (category) REFERENCES qt_category_mst(category_code);
  END IF;
END $$ LANGUAGE plpgsql;

-- Phase 4: 최종 검증
DO $$
DECLARE mst_cnt bigint; fk_cnt bigint;
BEGIN
  SELECT COUNT(*) INTO mst_cnt FROM qt_category_mst;
  IF mst_cnt <> 3 THEN RAISE EXCEPTION 'HALT final: mst=% (expected 3)', mst_cnt; END IF;
  SELECT COUNT(*) INTO fk_cnt FROM pg_constraint
   WHERE conname='fk_qt_category' AND conrelid='qt_quotation'::regclass;
  IF fk_cnt <> 1 THEN RAISE EXCEPTION 'HALT final: fk_qt_category missing'; END IF;
  RAISE NOTICE 'PASS: S8 V024 applied (mst=3, FK×1)';
END $$ LANGUAGE plpgsql;

COMMIT;
```

**V024_rollback.sql**: `ALTER TABLE qt_quotation DROP CONSTRAINT IF EXISTS fk_qt_category; DROP TABLE IF EXISTS qt_category_mst;`

### Step 3 — `QtCategory` Enum 신설

**파일**: `src/main/java/com/swmanager/system/constant/enums/QtCategory.java`

S9 `AccessActionType` 패턴 복제 (@JsonValue/@JsonCreator + fromKoLabel + 정확 3종).

```java
public enum QtCategory {
    MAINTENANCE("유지보수"),
    SERVICE("용역"),
    PRODUCT("제품");
    // ... S9 AccessActionType 와 동일 구조
}
```

### Step 4 — Java 리터럴 치환

**4-1. QuotationService.java**

Line 58, 78 견적번호 prefix 매핑:
```java
// Before
Map.of("용역","SQ","제품","PQ","유지보수","MQ")
// After (Enum 기반)
Map.of(QtCategory.SERVICE.getLabel(), "SQ",
       QtCategory.PRODUCT.getLabel(), "PQ",
       QtCategory.MAINTENANCE.getLabel(), "MQ")
```

Line 385-390 통계 집계:
```java
"용역_count",  quotationRepository.countByCategory(QtCategory.SERVICE.getLabel()),
// ... 6개 리터럴 전부 치환
```

**4-2. HwpxExportService.java:697**

별개 도메인(문자열 후처리) 여부 수동 판정 후 치환 or 주석으로 유지 사유 명시.

### Step 5 — Arch 테스트 (선택, S9 패턴)

**파일**: `src/test/java/com/swmanager/system/arch/QuotationLiteralArchTest.java` (이름 통일)
- `rg` 기반 `src/main/java` 에서 `"(유지보수|용역|제품)"` 정확 일치 리터럴 0 hits 검증
- 예외: `QtCategory.java` 내부 + Step 1 판정표에서 "유지"로 결정된 `HwpxExportService` 라인 (주석 사유 필수)

### Step 6 — 테스트 작성

**파일 1**: `src/test/java/com/swmanager/system/constant/enums/QtCategoryTest.java`
- 3종 values, label Freeze, fromKoLabel trim/unknown null, JsonCreator 양방향 (S9 패턴)

**파일 2**: `src/test/java/com/swmanager/system/arch/QtCategoryMstSchemaTest.java`
- `@SpringBootTest` + JdbcTemplate:
  - `qt_category_mst` row = 3 정확
  - FK `fk_qt_category` 존재
  - 마스터 외 값 insert → DataAccessException

### Step 7 — db_init_phase2.sql 시드 추가 (FR-5)

`qt_category_mst` 3행 `INSERT ... ON CONFLICT DO NOTHING` 블록 추가 (재기동 시 자동 복원).

### Step 8 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q clean compile
./mvnw -q test
bash server-restart.sh
```

**수동 스모크 3건**:
1. `/quotation/list` — 61건 렌더링 정상
2. `/quotation/form` (신규 작성 진입) — category 드롭다운 3종 정상
3. `/quotation/{id}` 상세 — category 표시 한글 정상

### Step 9 — 로드맵 정정 (T-LINK) + 커밋/푸시

**CLAUDE.md 준수**: 사용자가 `"작업완료"` 발화 후에만 자동 커밋·푸시 수행 (임의 커밋 금지).

작업:
1. `docs/plans/data-architecture-roadmap.md` §S8 ✅ 완료 표기
2. 사용자 `"작업완료"` 발화 대기
3. 발화 시 `git add <명시 파일들>` + `git commit` + `git push`

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | Precheck scan | Step 1 | 리터럴 사용처 확정 |
| T2 | V024 실행 | SQL | `PASS: S8 V024 applied (mst=3, FK×1)` |
| T3 | qt_category_mst 행수 | SQL | = 3 정확 |
| T4 | FK 존재 | pg_constraint | `fk_qt_category` = 1 |
| T5 | Enum 3 values | JUnit | `QtCategory.values().length=3` |
| T6 | label Freeze | JUnit | 유지보수/용역/제품 라벨 불변 |
| T7 | 리터럴 잔존 (정확 일치) | rg | `rg -n '"(유지보수|용역|제품)"(?![가-힣])' src/main/java --glob '!**/enums/**'` = **0 hits** (HwpxExportService 판정표에서 "유지" 결정 라인은 주석으로 사유 명시 + 별도 화이트리스트) |
| T8 | 마스터 외 값 insert 거부 | JdbcTemplate | DataAccessException |
| T9 | 기존 61건 값 유지 | SQL | `SELECT COUNT(*) FROM qt_quotation WHERE category IN ('유지보수','용역','제품')` = 61 |
| T10 | 컴파일 / 전체 테스트 | mvnw | green |
| T11 | 서버 기동 | restart | HTTP 200 |
| T12 | 수동 스모크 3건 | §Step 8 | 정상 렌더링 |
| T13 | 멱등성 | V024 2회차 | 동일 NOTICE |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Phase 0/2 FAIL | 자동 ROLLBACK |
| FK ADD 후 회귀 | `V024_rollback.sql` (DROP constraint + DROP table) |
| Java 리터럴 치환 회귀 | `git revert <해당 commit>` |
| 전체 롤백 | V024_rollback.sql + git revert BASE_SHA..HEAD |

**롤백 기준 SHA**: __________ (Step 0 기록)

---

## 4. 파일 변경 요약

### 신규
- `swdept/sql/V024_qt_category_master.sql`
- `swdept/sql/V024_rollback.sql`
- `src/main/java/com/swmanager/system/constant/enums/QtCategory.java`
- `src/test/java/com/swmanager/system/constant/enums/QtCategoryTest.java`
- `src/test/java/com/swmanager/system/arch/QtCategoryMstSchemaTest.java`
- (선택) `src/test/java/com/swmanager/system/arch/QuotationLiteralArchTest.java`
- `docs/dev-plans/s8-literal-scan-exact.txt`
- `docs/dev-plans/s8-literal-scan-partial.txt`
- `docs/dev-plans/s8-hwpx-literal-decision.md`

### 수정
- `src/main/java/com/swmanager/system/quotation/service/QuotationService.java` (리터럴 치환 8+건)
- `src/main/java/com/swmanager/system/service/HwpxExportService.java` (판정 후 치환 or 주석)
- `src/main/resources/db_init_phase2.sql` (3행 시드)

---

## 5. 승인 요청

### 승인 확인 사항
- [ ] §0 기획 결정 반영
- [ ] §1 Step 0~9
- [ ] §2 T1~T13
- [ ] §3 롤백 (V024_rollback.sql)
- [ ] §4 파일 변경 범위
