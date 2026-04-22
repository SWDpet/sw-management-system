---
tags: [dev-plan, sprint, workplan, master, enum, wave-4]
sprint: "tb-work-plan-decision"
status: draft
created: "2026-04-22"
---

# [개발계획서] tb_work_plan 마스터 + Enum (S16)

- **작성팀**: 개발팀
- **작성일**: 2026-04-22
- **근거 기획서**: [[tb-work-plan-decision]] v2.1 (사용자 최종승인 2026-04-22)
- **상태**: v2 (codex v1 ⚠수정필요 3건 반영)
- **v1→v2 변경점**:
  1. Step 1 스캔식에 `--glob '!**/test/**'` 추가 (T9와 통일)
  2. Enum `@JsonValue` API 계약 명시 — 직렬화 = 한글 label, DB 저장 = 영문 코드 (분리)
  3. V026 Phase 4 seed 정합 검증 추가 (label + color VALUES 비교 → drift 사전 방어)

---

## 0. 전제 / 환경

### 0-1. 기획 결정 반영
- A-opt1: 2 테이블 분리 (type_mst + status_mst)
- B-opt1: 영문코드 유지 (기존 1행 변경 0)
- C-opt1: 마스터 + Enum 둘 다

### 0-2. 실측 고정값 (2026-04-22)
- tb_work_plan: 1 row (PATCH/NONE/CONFIRMED)
- plan_type 10종 (CONTRACT/INSTALL/PATCH/INSPECT/PRE_CONTACT/FAULT/SUPPORT/SETTLE/COMPLETE/ETC) + color
- status 7종 (PLANNED/CONTACTED/CONFIRMED/IN_PROGRESS/COMPLETED/POSTPONED/CANCELLED)
- Java 리터럴 사용처:
  - `WorkPlanDTO.java:76~128` — getTypeColor / getTypeLabel / getStatusLabel switch 30+ 리터럴
  - `WorkPlanService.java:149` — `"PLANNED"` 기본값
  - `WorkPlan.java:64,88` — 주석 + `"PLANNED"`
  - `WorkPlanController.java:121` — `"CANCELLED"` 제외 리스트
  - `PerformanceService.java:63,71` — `"INSPECT"` / `"COMPLETED"` 필터

### 0-3. 배포 순서 (기획 R-5)
**Java 배포 먼저 (Enum 신설·치환) → V026 실행 (FK ADD 시 App 매핑 이미 반영됨)**

---

## 1. 작업 순서

### Step 0 — 기준 SHA 기록

```bash
BASE_SHA=$(git rev-parse HEAD)
```

### Step 1 — 리터럴 전수 스캔

```bash
# 단독 영문 리터럴 (예외 없음 — NFR-4 "0 hits", T9 와 동일 검증식)
rg -n '"(CONTRACT|INSTALL|PATCH|INSPECT|PRE_CONTACT|FAULT|SUPPORT|SETTLE|COMPLETE|ETC|PLANNED|CONTACTED|CONFIRMED|IN_PROGRESS|COMPLETED|POSTPONED|CANCELLED)"' \
  src/main/java --glob '!**/enums/**' --glob '!**/test/**' \
  > docs/dev-plans/s16-literal-scan.txt
```

**Exit Gate 1**: scan 결과를 판정표로 정리 (치환 대상 vs 주석·외 도메인)

### Step 2 — `WorkPlanType` + `WorkPlanStatus` Enum 신설

**파일 1**: `src/main/java/com/swmanager/system/constant/enums/WorkPlanType.java`

```java
public enum WorkPlanType {
    CONTRACT("계약", "#1565c0"),
    INSTALL("설치", "#2e7d32"),
    PATCH("패치", "#00897b"),
    INSPECT("점검", "#ff9800"),
    PRE_CONTACT("사전연락", "#9e9e9e"),
    FAULT("장애처리", "#e74a3b"),
    SUPPORT("업무지원", "#6a1b9a"),
    SETTLE("기성/준공", "#5c6bc0"),
    COMPLETE("준공", "#37474f"),
    ETC("기타", "#795548");

    private final String label;
    private final String color;
    // @JsonValue getLabel() + @JsonCreator fromJson (label/name trim)
    // fromKoLabel(String) + getColor()
}
```

**API 계약 (v2 명시)**:
- **Enum 직렬화 값은 한글 label** (S8 QtCategory / S9 AccessActionType 패턴 동일)
  - 예: `WorkPlanType.PATCH` → JSON 응답 `"패치"`
  - 예: `WorkPlanStatus.CONFIRMED` → JSON 응답 `"확정"`
- 이유: 기존 UI·리포트가 한글 표시 기반 → 호환성 유지
- 주의: **DB 저장값은 영문 코드** (B-opt1) 이므로 직렬화·저장 경로 분리.
  - DTO.setPlanType(String code) 는 영문 저장. UI 표시용 `getTypeLabel()` 이 Enum 위임으로 라벨 변환.
```
```

**파일 2**: `src/main/java/com/swmanager/system/constant/enums/WorkPlanStatus.java`
- 7 values + label Freeze + @JsonValue/@JsonCreator + fromKoLabel (S8 QtCategory 패턴 복제)

### Step 3 — V026 마이그레이션 SQL

**파일**: `swdept/sql/V026_work_plan_master.sql`

```sql
BEGIN;

-- Phase 0: DB 값이 Enum 집합 내
DO $$
DECLARE bad_t bigint; bad_s bigint;
BEGIN
  SELECT COUNT(*) INTO bad_t FROM tb_work_plan
   WHERE plan_type NOT IN ('CONTRACT','INSTALL','PATCH','INSPECT','PRE_CONTACT',
                           'FAULT','SUPPORT','SETTLE','COMPLETE','ETC');
  IF bad_t > 0 THEN RAISE EXCEPTION 'HALT Phase 0: 예상 외 plan_type %', bad_t; END IF;

  SELECT COUNT(*) INTO bad_s FROM tb_work_plan
   WHERE status NOT IN ('PLANNED','CONTACTED','CONFIRMED','IN_PROGRESS',
                        'COMPLETED','POSTPONED','CANCELLED');
  IF bad_s > 0 THEN RAISE EXCEPTION 'HALT Phase 0: 예상 외 status %', bad_s; END IF;
  RAISE NOTICE 'Phase 0 PASS';
END $$ LANGUAGE plpgsql;

-- Phase 1: 마스터 CREATE + 시드 (17행, 재실행 안전)
CREATE TABLE IF NOT EXISTS work_plan_type_mst (
  type_code     VARCHAR(20) PRIMARY KEY,
  type_label    VARCHAR(50) NOT NULL,
  color         VARCHAR(10) NOT NULL,
  display_order INT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS work_plan_status_mst (
  status_code   VARCHAR(20) PRIMARY KEY,
  status_label  VARCHAR(50) NOT NULL,
  display_order INT NOT NULL DEFAULT 0
);

INSERT INTO work_plan_type_mst (type_code, type_label, color, display_order) VALUES
  ('CONTRACT',   '계약',     '#1565c0', 1),
  ('INSTALL',    '설치',     '#2e7d32', 2),
  ('PATCH',      '패치',     '#00897b', 3),
  ('INSPECT',    '점검',     '#ff9800', 4),
  ('PRE_CONTACT','사전연락', '#9e9e9e', 5),
  ('FAULT',      '장애처리', '#e74a3b', 6),
  ('SUPPORT',    '업무지원', '#6a1b9a', 7),
  ('SETTLE',     '기성/준공','#5c6bc0', 8),
  ('COMPLETE',   '준공',     '#37474f', 9),
  ('ETC',        '기타',     '#795548',10)
ON CONFLICT (type_code) DO NOTHING;

INSERT INTO work_plan_status_mst (status_code, status_label, display_order) VALUES
  ('PLANNED',     '예정',     1),
  ('CONTACTED',   '연락완료', 2),
  ('CONFIRMED',   '확정',     3),
  ('IN_PROGRESS', '진행중',   4),
  ('COMPLETED',   '완료',     5),
  ('POSTPONED',   '연기',     6),
  ('CANCELLED',   '취소',     7)
ON CONFLICT (status_code) DO NOTHING;

-- Phase 2: Exit Gate — tb_work_plan 값 정합성
DO $$
DECLARE bad bigint;
BEGIN
  SELECT COUNT(*) INTO bad FROM tb_work_plan
   WHERE plan_type NOT IN (SELECT type_code FROM work_plan_type_mst)
      OR status NOT IN (SELECT status_code FROM work_plan_status_mst);
  IF bad > 0 THEN RAISE EXCEPTION 'HALT Phase 2: 마스터 외 값 %', bad; END IF;
  RAISE NOTICE 'Phase 2 Exit Gate PASS';
END $$ LANGUAGE plpgsql;

-- Phase 3: FK ADD (명시적 멱등, conrelid 한정)
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
     WHERE conname='fk_twp_type' AND conrelid='tb_work_plan'::regclass
  ) THEN
    ALTER TABLE tb_work_plan
      ADD CONSTRAINT fk_twp_type
      FOREIGN KEY (plan_type) REFERENCES work_plan_type_mst(type_code);
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
     WHERE conname='fk_twp_status' AND conrelid='tb_work_plan'::regclass
  ) THEN
    ALTER TABLE tb_work_plan
      ADD CONSTRAINT fk_twp_status
      FOREIGN KEY (status) REFERENCES work_plan_status_mst(status_code);
  END IF;
END $$ LANGUAGE plpgsql;

-- Phase 4: 최종 검증 (type=10, status=7, FK×2 + seed 정합)
DO $$
DECLARE t bigint; s bigint; fk1 bigint; fk2 bigint;
  bad_label bigint; bad_color bigint;
BEGIN
  SELECT COUNT(*) INTO t FROM work_plan_type_mst;
  SELECT COUNT(*) INTO s FROM work_plan_status_mst;
  IF t <> 10 THEN RAISE EXCEPTION 'HALT final: type_mst=% (expected 10)', t; END IF;
  IF s <> 7  THEN RAISE EXCEPTION 'HALT final: status_mst=% (expected 7)', s; END IF;
  SELECT COUNT(*) INTO fk1 FROM pg_constraint
   WHERE conname='fk_twp_type' AND conrelid='tb_work_plan'::regclass;
  SELECT COUNT(*) INTO fk2 FROM pg_constraint
   WHERE conname='fk_twp_status' AND conrelid='tb_work_plan'::regclass;
  IF fk1 <> 1 OR fk2 <> 1 THEN RAISE EXCEPTION 'HALT final: FK missing (type=%, status=%)', fk1, fk2; END IF;

  -- v2: seed 정합 검증 — 예상 label/color 와 실제 DB row 일치 (drift 사전 방어)
  SELECT COUNT(*) INTO bad_label FROM (VALUES
      ('CONTRACT','계약'),('INSTALL','설치'),('PATCH','패치'),('INSPECT','점검'),
      ('PRE_CONTACT','사전연락'),('FAULT','장애처리'),('SUPPORT','업무지원'),
      ('SETTLE','기성/준공'),('COMPLETE','준공'),('ETC','기타')
    ) AS expected(code, label)
    LEFT JOIN work_plan_type_mst m ON m.type_code = expected.code
   WHERE m.type_label <> expected.label OR m.type_label IS NULL;
  IF bad_label > 0 THEN RAISE EXCEPTION 'HALT final: type_mst label drift %', bad_label; END IF;

  SELECT COUNT(*) INTO bad_color FROM (VALUES
      ('CONTRACT','#1565c0'),('INSTALL','#2e7d32'),('PATCH','#00897b'),('INSPECT','#ff9800'),
      ('PRE_CONTACT','#9e9e9e'),('FAULT','#e74a3b'),('SUPPORT','#6a1b9a'),
      ('SETTLE','#5c6bc0'),('COMPLETE','#37474f'),('ETC','#795548')
    ) AS expected(code, color)
    LEFT JOIN work_plan_type_mst m ON m.type_code = expected.code
   WHERE m.color <> expected.color OR m.color IS NULL;
  IF bad_color > 0 THEN RAISE EXCEPTION 'HALT final: type_mst color drift %', bad_color; END IF;

  RAISE NOTICE 'PASS: S16 V026 applied (type=10, status=7, FK x 2, seed label/color OK)';
END $$ LANGUAGE plpgsql;

COMMIT;
```

**V026_rollback.sql** 보관 (기획서 §4-4).

### Step 4 — Java 리터럴 치환 (NFR-4 0 hits)

**4-1. WorkPlanDTO.java** (L76~128)
- `getTypeColor(String)` → `WorkPlanType.fromJson(planType).getColor()` (null-safe)
- `getTypeLabel(String)` → `WorkPlanType.fromJson(planType).getLabel()`
- `getStatusLabel(String)` → `WorkPlanStatus.fromJson(status).getLabel()`
- switch-case 블록 모두 제거

**4-2. WorkPlanService.java:149**
```java
// Before: plan.setStatus(dto.getStatus() != null ? dto.getStatus() : "PLANNED");
// After:  plan.setStatus(dto.getStatus() != null ? dto.getStatus() : WorkPlanStatus.PLANNED.name());
```

**4-3. WorkPlan.java:64,88**
- 주석 유지 (역사 기록) + L88 기본값 `WorkPlanStatus.PLANNED.name()` 치환

**4-4. WorkPlanController.java:121**
```java
// Before: List<String> excludeStatuses = List.of("CANCELLED");
// After:  List<String> excludeStatuses = List.of(WorkPlanStatus.CANCELLED.name());
```

**4-5. PerformanceService.java:63,71**
- `"INSPECT"` → `WorkPlanType.INSPECT.name()`
- `"COMPLETED"` → `WorkPlanStatus.COMPLETED.name()`

### Step 5 — db_init_phase2.sql 시드 추가 (FR-8)

V026 시드 17행을 `db_init_phase2.sql` 말미에 추가 (서버 재기동 시 자동 복원).

### Step 6 — 테스트 (NFR-6 ≥ 5건, NFR-8 drift 게이트)

**파일 1**: `WorkPlanTypeTest.java` — 10 values / label·color Freeze / fromKoLabel trim / JsonCreator
**파일 2**: `WorkPlanStatusTest.java` — 7 values / label Freeze / JsonCreator
**파일 3**: `src/test/java/com/swmanager/system/arch/WorkPlanMstSchemaTest.java` — SpringBoot + JdbcTemplate:
- `work_plan_type_mst` = 10 정확
- `work_plan_status_mst` = 7 정확
- FK 2개 존재
- 기존 1 row 값 유지
**파일 4** (NFR-8 drift 게이트): `WorkPlanMstEnumSyncTest.java`:
- Enum values 와 마스터 row 를 전수 비교 (name/label/color 일치)
- drift 발생 시 테스트 실패

### Step 7 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q clean compile
./mvnw -q test
bash server-restart.sh
```

**수동 스모크 3건**:
1. `/workplan/calendar` — 캘린더 렌더링 (plan_type 색상 매핑 정상)
2. `/workplan/{id}` 상태변경 — 마스터 외 값 거부
3. 기존 1 row 영월군 업무계획이 PATCH/확정 정상 표시

### Step 8 — 로드맵 정정 (T-LINK)

`docs/plans/data-architecture-roadmap.md` §S16 ✅ 완료 표기.

### Step 9 — 사용자 `작업완료` 대기 후 커밋/푸시 (CLAUDE.md 준수)

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | Step 1 Precheck | rg + 판정표 | Exit Gate 1 |
| T2 | V026 실행 | SQL | `PASS: S16 V026 applied (type=10, status=7, FK x 2)` |
| T3 | type_mst=10 / status_mst=7 정확 | JdbcTemplate | = 정확 |
| T4 | FK 2개 존재 | pg_constraint (conrelid 한정) | 2 |
| T5 | WorkPlanType 10 values / color Freeze | JUnit | PASS |
| T6 | WorkPlanStatus 7 values / label Freeze | JUnit | PASS |
| T7 | Enum↔마스터 drift (NFR-8) | WorkPlanMstEnumSyncTest | name/label/color 전부 일치 |
| T8 | 기존 1 row 값 유지 (NFR-5) | SQL | PATCH/CONFIRMED 그대로 |
| T9 | 리터럴 잔존 (NFR-4) | `rg '"(CONTRACT|INSTALL|...|CANCELLED)"' src/main/java --glob '!**/enums/**' --glob '!**/test/**'` | **0 hits** |
| T10 | 컴파일 / 전체 테스트 | mvnw | BUILD SUCCESS |
| T11 | 서버 기동 | restart | HTTP 정상 |
| T12 | 수동 스모크 3건 | §Step 7 | 정상 |
| T13 | 멱등성 | V026 2회차 | 동일 NOTICE |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Phase 0/2 FAIL | 자동 ROLLBACK |
| FK ADD 후 회귀 | V026_rollback.sql (FK DROP + 테이블 DROP) |
| Java 치환 오류 | `git revert <commit>` |
| 전체 | V026_rollback + git revert BASE_SHA..HEAD |

**롤백 기준 SHA**: __________ (Step 0 기록)

---

## 4. 파일 변경 요약

### 신규
- `swdept/sql/V026_work_plan_master.sql`
- `swdept/sql/V026_rollback.sql`
- `src/main/java/com/swmanager/system/constant/enums/WorkPlanType.java`
- `src/main/java/com/swmanager/system/constant/enums/WorkPlanStatus.java`
- `src/test/java/com/swmanager/system/constant/enums/WorkPlanTypeTest.java`
- `src/test/java/com/swmanager/system/constant/enums/WorkPlanStatusTest.java`
- `src/test/java/com/swmanager/system/arch/WorkPlanMstSchemaTest.java`
- `src/test/java/com/swmanager/system/arch/WorkPlanMstEnumSyncTest.java`
- `docs/dev-plans/s16-literal-scan.txt`

### 수정
- `src/main/java/com/swmanager/system/dto/WorkPlanDTO.java` (switch 3개 제거 + Enum 위임)
- `src/main/java/com/swmanager/system/service/WorkPlanService.java` (L149)
- `src/main/java/com/swmanager/system/domain/workplan/WorkPlan.java` (L88)
- `src/main/java/com/swmanager/system/controller/WorkPlanController.java` (L121)
- `src/main/java/com/swmanager/system/service/PerformanceService.java` (L63, L71)
- `src/main/resources/db_init_phase2.sql` (17행 시드)

---

## 5. 승인 요청

### 승인 확인 사항
- [ ] §0 전제
- [ ] §1 Step 0~9
- [ ] §2 T1~T13 (T7 drift / T9 리터럴 0)
- [ ] §3 롤백 (V026_rollback.sql)
- [ ] §4 파일 변경 범위
