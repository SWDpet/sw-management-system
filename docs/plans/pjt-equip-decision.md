---
tags: [plan, sprint, drop, legacy, wave-4]
sprint: "pjt-equip-decision"
priority: P3
wave: 4
status: draft
created: "2026-04-22"
---

# [기획서] pjt_equip 테이블 DROP (S15)

- **작성팀**: 기획팀
- **작성일**: 2026-04-22
- **근거 로드맵**: [[data-architecture-roadmap]] v2 §S15 (Wave 4 / P3)
- **사용자 결정 (2026-04-22)**: **A 옵션 (DROP)** 채택
- **상태**: v2 (codex v1 ⚠ 수정필요 4건 반영)
- **v1→v2 변경점**:
  1. **폐기 기능 공식 목록화** — SwController `/api/equip/{projId}` GET/POST 엔드포인트 + 연관 UI (project-form.html, doc-interim.html, doc-commence.html) 명시
  2. Phase 0 의존 객체 검증 확대 — FK 뿐 아니라 **view/function/trigger/seq** 전수 조사
  3. NFR-4 테스트 상향 — 마이그 이후 엔드포인트 404 확인 + 주요 화면 렌더 회귀
  4. FR-3 "제거 or 주석" → **"제거" 단일화**

---

## 0. 사전 조사 결과 (2026-04-22 실측)

| 항목 | 값 |
|---|---|
| `pjt_equip` row count | **0** |
| 컬럼 수 | 10 (equip_id, equip_name, equip_type, quantity, reg_dt, remark, sort_order, spec, unit_price, proj_id) |
| 외부 FK 참조 | **없음** (다른 테이블이 pjt_equip 을 참조하지 않음) |
| Java Entity / Repository | Step 1 Precheck 에서 재확인 |
| `/equip/*` 등 엔드포인트 | Step 1 Precheck 에서 재확인 |

결론: **DROP 안전** (감사 분류 ⑤ 레거시)

---

## 1. 목적 / 범위

### 1-1. 목적
- **FR-1**: `pjt_equip` 테이블 DROP
- **FR-2**: 관련 Java 코드 전부 **제거**:
  - `src/main/java/com/swmanager/system/domain/PjtEquip.java`
  - `src/main/java/com/swmanager/system/repository/PjtEquipRepository.java`
  - `SwController.java` 내 주입 (L58) + 엔드포인트 2개 (GET/POST `/api/equip/{projId}`, L365~406 블록)
- **FR-3**: UI Thymeleaf 템플릿의 장비 관련 JS/HTML 영역 **제거** (주석 아님):
  - `src/main/resources/templates/project-form.html`
  - `src/main/resources/templates/document/doc-interim.html`
  - `src/main/resources/templates/document/doc-commence.html`
- **FR-4** (v2 신규): 폐기되는 엔드포인트 공식 목록:
  - `GET /api/equip/{projId}` — 장비 목록 조회
  - `POST /api/equip/{projId}` — 장비 일괄 저장
  → 마이그 이후 두 URL 모두 404 응답 (**NFR-4 에서 검증**)

### 1-2. 포함
- V025 마이그레이션 SQL (DROP TABLE IF EXISTS pjt_equip)
- Java 심볼 전수 제거 (Step 1 Precheck 실측 기반)
- 회귀 테스트 ≥ 4건 (FR-1~FR-4 모두 검증 — NFR-4 참조)

### 1-3. 제외
- 향후 장비 관리 기능이 재도입될 때의 설계 — 별도 신규 스프린트

---

## 2. 제안 설계

### 2-1. V025 마이그 (초안)
```sql
BEGIN;

-- Phase 0: 의존 객체 전수 검증 (v2 확대: FK + view + function + trigger)
DO $$
DECLARE fk_cnt bigint; dep_cnt bigint; row_cnt bigint;
BEGIN
  -- (a) 외부 FK
  SELECT COUNT(*) INTO fk_cnt FROM information_schema.table_constraints tc
    JOIN information_schema.constraint_column_usage ccu USING (constraint_name, table_schema)
   WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public'
     AND ccu.table_name='pjt_equip' AND tc.table_name<>'pjt_equip';
  IF fk_cnt > 0 THEN RAISE EXCEPTION 'HALT Phase 0: external FK %', fk_cnt; END IF;

  -- (b) 비-FK 의존 객체 전수 검증 — 객체 유형별 명시 쿼리 (public schema 한정)
  --   view: pg_views / materialized view: pg_matviews / rule: pg_rewrite / trigger: pg_trigger
  --   (pg_depend total 대신 객체별로 나눠 실패 시 원인 파악 쉬움)
  DECLARE v_cnt bigint; mv_cnt bigint; r_cnt bigint; t_cnt bigint;
  BEGIN
    SELECT COUNT(*) INTO v_cnt  FROM pg_views       WHERE schemaname='public' AND definition ILIKE '%pjt_equip%';
    SELECT COUNT(*) INTO mv_cnt FROM pg_matviews    WHERE schemaname='public' AND definition ILIKE '%pjt_equip%';
    SELECT COUNT(*) INTO r_cnt  FROM pg_rewrite r JOIN pg_class c ON r.ev_class=c.oid
      JOIN pg_namespace n ON c.relnamespace=n.oid
      WHERE n.nspname='public' AND r.ev_action::text ILIKE '%pjt_equip%' AND c.relname <> 'pjt_equip';
    SELECT COUNT(*) INTO t_cnt  FROM pg_trigger tg JOIN pg_class c ON tg.tgrelid=c.oid
      WHERE c.relname='pjt_equip' AND tg.tgisinternal=FALSE;
    -- function body 에서 pjt_equip 참조 (v2.1 보강)
    DECLARE f_cnt bigint; seq_cnt bigint;
    BEGIN
      SELECT COUNT(*) INTO f_cnt FROM pg_proc p
        JOIN pg_namespace n ON p.pronamespace = n.oid
       WHERE n.nspname='public' AND p.prosrc ILIKE '%pjt_equip%';
      -- sequence: pjt_equip_* 이름 패턴 or pg_depend 로 pjt_equip 에 종속된 시퀀스
      SELECT COUNT(*) INTO seq_cnt FROM pg_class c
        JOIN pg_namespace n ON c.relnamespace = n.oid
       WHERE c.relkind='S' AND n.nspname='public' AND c.relname LIKE 'pjt_equip%';
      dep_cnt := v_cnt + mv_cnt + r_cnt + t_cnt + f_cnt + seq_cnt;
      IF dep_cnt > 0 THEN
        RAISE EXCEPTION 'HALT Phase 0: dep view=%, matview=%, rule=%, trigger=%, function=%, seq=%',
          v_cnt, mv_cnt, r_cnt, t_cnt, f_cnt, seq_cnt;
      END IF;
    END;
  END;

  -- (c) 배포 직전 row=0 재확인 (안전망)
  SELECT COUNT(*) INTO row_cnt FROM pjt_equip;
  IF row_cnt > 0 THEN RAISE EXCEPTION 'HALT Phase 0: pjt_equip rows % (expected 0)', row_cnt; END IF;

  RAISE NOTICE 'Phase 0 PASS: external FK 0, dep 0, rows 0';
END $$ LANGUAGE plpgsql;

-- Phase 1: DROP
DROP TABLE IF EXISTS pjt_equip;

-- Phase 2: 사후 검증
DO $$
DECLARE exists_cnt bigint;
BEGIN
  SELECT COUNT(*) INTO exists_cnt FROM information_schema.tables
   WHERE table_schema='public' AND table_name='pjt_equip';
  IF exists_cnt <> 0 THEN RAISE EXCEPTION 'HALT final: pjt_equip still exists'; END IF;
  RAISE NOTICE 'PASS: S15 V025 — pjt_equip dropped';
END $$ LANGUAGE plpgsql;

COMMIT;
```

### 2-2. Java 코드 제거 (Step 1 Precheck 결과 기반)
- PjtEquip Entity / Repository / Service / Controller / DTO 존재 시 파일 삭제
- 다른 클래스에서 참조할 경우 해당 import/호출부 삭제

### 2-3. 멱등성
- `DROP TABLE IF EXISTS` + Phase 2 사후검증 (재실행 시 Phase 1 NOTICE 동일)

---

## 3. FR / NFR

### FR
- FR-1 ~ FR-4 (§1-1)

### NFR
- **NFR-1**: V025 실행 후 `pjt_equip` 테이블 미존재
- **NFR-2**: Java 심볼 `PjtEquip` / `PjtEquipRepository` classpath 부재 (ArchUnit 검증)
- **NFR-3**: 컴파일 + 전체 테스트 green + 서버 기동 정상
- **NFR-4** (v2 상향): 회귀 테스트 **≥ 4건**:
  - schema: `pjt_equip` 미존재
  - arch: PjtEquip/PjtEquipRepository 클래스 부재
  - MVC: `GET /api/equip/{projId}` 404 확인
  - MVC: `POST /api/equip/{projId}` 404 확인
- **NFR-5** (v2 신규): 주요 화면 렌더 회귀 — `/project/form`, `/document/commence`, `/document/interim` 수동 스모크 3건 정상
- **NFR-6** (v2 신규): 리터럴 잔존 — `rg 'PjtEquip|pjt_equip|/api/equip' src/main` = 0 hits (테스트 파일 제외)

---

## 4. 리스크 / 완화

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | Java 숨은 참조 발견 | **중** | Step 1 Precheck 에서 `rg 'PjtEquip\|pjt_equip'` 전수 조사 + 컴파일 게이트 |
| R-2 | 향후 장비 관리 재도입 시 복구 어려움 | 낮음 | 본 기획 §1-3 에 "재도입은 신규 스프린트" 명시 |
| R-3 | 실제로 숨은 데이터 존재 | 낮음 | row=0 실측 확인됨. 배포 직전 재확인 게이트 |

---

## 5. 대안 / 비채택

| 대안 | 비채택 사유 |
|------|-------------|
| 보류 (유지) | 0 rows + 외부 FK 0 + 사용자 결정 A (DROP) |

---

## 6. 사용자 승인 요청 사항

### 승인 확인 사항
- [ ] §0 실측 결과 재확인 (row=0, 외부 FK 0)
- [ ] §1-1 FR-1~FR-4 (테이블 + Java + Thymeleaf + 폐기 엔드포인트 목록)
- [ ] §2 V025 마이그 순서 (Phase 0 의존객체 3중 게이트 포함)
- [ ] §3 NFR-1~NFR-6
- [ ] §4 R-1 (명시적 사용처 다수 — 범위 확장 허용)
