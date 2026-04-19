---
tags: [seed, org-unit]
parent-plan: doc-selector-org-env.md
status: 확인 완료 (2026-04-19)
created: "2026-04-19"
source: 사용자 제공 조직도 앱 스크린샷 4장
---

# 조직도 초기 Seed 데이터

> 스프린트 5(`doc-selector-org-env`) 에서 `tb_org_unit` 테이블 초기 적재용.
> 2026-04-19 사용자 조직도 앱 스크린샷 4장 기준 추출·정리.

---

## 최종 조직도 (10 본부·연구소 / 15 부서 / 15 팀)

```
회사: (주)정도유아이티 (저장 대상 아님 — 시스템 단일 소유)

□ 경영관리본부                          DIVISION
   ├ 인사총무부                         DEPARTMENT
   ├ 재무회계부                         DEPARTMENT
   └ 베트남 경영관리팀                  TEAM     (본부 직속)

□ 글로벌사업본부                        DIVISION
   └ 해외사업부                         DEPARTMENT
      └ 해외사업팀                      TEAM

□ 글로벌기획본부                        DIVISION  (하위 없음)

□ AI전략기획본부                        DIVISION
   ├ AI기획팀                           TEAM     (본부 직속)
   └ 디자인팀                           TEAM     (본부 직속)

□ AI GIS 연구본부                       DIVISION
   ├ AI GIS 연구부                      DEPARTMENT
   │  ├ 연구1팀                         TEAM
   │  └ 연구2팀                         TEAM
   ├ R&D기획부                          DEPARTMENT
   │  └ R&D기획팀                       TEAM
   └ SW지원부                           DEPARTMENT
      └ SW지원팀                        TEAM

□ SW기술연구소                          DIVISION
   ├ 응용개발연구부                     DEPARTMENT
   └ 제품개발연구부                     DEPARTMENT

□ SW영업본부                            DIVISION
   ├ GIS SW영업부                       DEPARTMENT
   │  └ GIS SW영업팀                    TEAM
   └ 도시계획 SW영업부                  DEPARTMENT
      └ 도시계획 SW영업팀               TEAM

□ GIS사업본부                           DIVISION
   ├ GIS사업1부                         DEPARTMENT
   │  ├ GIS1부 사업1팀                  TEAM
   │  └ GIS1부 사업2팀                  TEAM
   ├ GIS사업2부                         DEPARTMENT
   │  └ GIS2부 사업팀                   TEAM
   └ GIS 사업4팀                        TEAM     (본부 직속 — 부 레벨 없음)

□ 도시계획사업본부                      DIVISION
   └ 도시계획사업부                     DEPARTMENT
      ├ 도시계획사업1팀                 TEAM
      └ 도시계획사업2팀                 TEAM

□ 스마트시티본부                        DIVISION  (하위 없음)
```

---

## 구조 특성

| 특성 | 예시 |
|------|------|
| **1단** (본부만, 하위 없음) | 글로벌기획본부, 스마트시티본부 |
| **2단 (본부→부)** | SW기술연구소 → 응용개발연구부 (팀 없음) |
| **2단 (본부→팀, 부 생략)** | 경영관리본부 → 베트남 경영관리팀<br>AI전략기획본부 → AI기획팀/디자인팀<br>GIS사업본부 → GIS 사업4팀 |
| **3단 (본부→부→팀)** | AI GIS 연구본부 → AI GIS 연구부 → 연구1팀 |

→ **가변 계층 필수** (FR-3-A 에서 명시).

---

## DDL

```sql
-- 감사 이후 스프린트 5, 2026-04-19
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
```

## Seed INSERT (DO 블록으로 멱등 보장)

```sql
-- 기존 데이터 있으면 건너뜀 (use_yn='Y' 조건)
DO $$
DECLARE
    v_biz_mgmt_id         BIGINT;
    v_global_biz_id       BIGINT;
    v_global_plan_id      BIGINT;
    v_ai_strategy_id      BIGINT;
    v_ai_gis_research_id  BIGINT;
    v_sw_research_id      BIGINT;
    v_sw_sales_id         BIGINT;
    v_gis_biz_id          BIGINT;
    v_urban_biz_id        BIGINT;
    v_smartcity_id        BIGINT;
    v_dept_id             BIGINT;
BEGIN
    -- 이미 seed 가 적재돼 있으면 스킵
    IF EXISTS (SELECT 1 FROM tb_org_unit WHERE unit_type='DIVISION' AND name='경영관리본부' AND use_yn='Y') THEN
        RETURN;
    END IF;

    -- ========== DIVISION (본부·연구소) ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', '경영관리본부', 10) RETURNING unit_id INTO v_biz_mgmt_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', '글로벌사업본부', 20) RETURNING unit_id INTO v_global_biz_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', '글로벌기획본부', 30) RETURNING unit_id INTO v_global_plan_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', 'AI전략기획본부', 40) RETURNING unit_id INTO v_ai_strategy_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', 'AI GIS 연구본부', 50) RETURNING unit_id INTO v_ai_gis_research_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', 'SW기술연구소', 60) RETURNING unit_id INTO v_sw_research_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', 'SW영업본부', 70) RETURNING unit_id INTO v_sw_sales_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', 'GIS사업본부', 80) RETURNING unit_id INTO v_gis_biz_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', '도시계획사업본부', 90) RETURNING unit_id INTO v_urban_biz_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (NULL, 'DIVISION', '스마트시티본부', 100) RETURNING unit_id INTO v_smartcity_id;

    -- ========== 경영관리본부 하위 ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_biz_mgmt_id, 'DEPARTMENT', '인사총무부', 10) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_biz_mgmt_id, 'DEPARTMENT', '재무회계부', 20) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_biz_mgmt_id, 'TEAM', '베트남 경영관리팀', 30);  -- 본부 직속 팀

    -- ========== 글로벌사업본부 하위 ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_global_biz_id, 'DEPARTMENT', '해외사업부', 10) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', '해외사업팀', 10);

    -- ========== AI전략기획본부 하위 (본부 직속 팀만) ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_ai_strategy_id, 'TEAM', 'AI기획팀', 10);
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_ai_strategy_id, 'TEAM', '디자인팀', 20);

    -- ========== AI GIS 연구본부 하위 ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_ai_gis_research_id, 'DEPARTMENT', 'AI GIS 연구부', 10) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', '연구1팀', 10);
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', '연구2팀', 20);

    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_ai_gis_research_id, 'DEPARTMENT', 'R&D기획부', 20) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', 'R&D기획팀', 10);

    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_ai_gis_research_id, 'DEPARTMENT', 'SW지원부', 30) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', 'SW지원팀', 10);

    -- ========== SW기술연구소 하위 (팀 없이 부만) ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_sw_research_id, 'DEPARTMENT', '응용개발연구부', 10);
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_sw_research_id, 'DEPARTMENT', '제품개발연구부', 20);

    -- ========== SW영업본부 하위 ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_sw_sales_id, 'DEPARTMENT', 'GIS SW영업부', 10) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', 'GIS SW영업팀', 10);

    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_sw_sales_id, 'DEPARTMENT', '도시계획 SW영업부', 20) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', '도시계획 SW영업팀', 10);

    -- ========== GIS사업본부 하위 (부 + 본부 직속 팀 혼재) ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_gis_biz_id, 'DEPARTMENT', 'GIS사업1부', 10) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', 'GIS1부 사업1팀', 10);
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', 'GIS1부 사업2팀', 20);

    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_gis_biz_id, 'DEPARTMENT', 'GIS사업2부', 20) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', 'GIS2부 사업팀', 10);

    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_gis_biz_id, 'TEAM', 'GIS 사업4팀', 30);  -- 본부 직속

    -- ========== 도시계획사업본부 하위 ==========
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_urban_biz_id, 'DEPARTMENT', '도시계획사업부', 10) RETURNING unit_id INTO v_dept_id;
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', '도시계획사업1팀', 10);
    INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) VALUES
        (v_dept_id, 'TEAM', '도시계획사업2팀', 20);

    -- 글로벌기획본부, 스마트시티본부: 하위 없음 (1단)
END $$;
```

---

## 미해결 / 확인 필요 (소소한 것)

| # | 항목 | 기본값 | 확정 필요? |
|---|------|--------|------------|
| 1 | GIS사업 3부 존재 여부 | seed 에서 제외 (1/2/4부만) | 존재 시 수동 추가 가능 |
| 2 | 향후 신설 예정 조직 | 반영 안 함 | 있으면 seed 업데이트 |
| 3 | sort_order 규칙 | 본부 10 단위, 하위 10 단위 (수동 조정 가능) | 관리 UI 에서 변경 가능 |
| 4 | 팀원 숫자(02/11/07 등) | seed 무관 | 본 스프린트 범위 밖 (사람 매핑 별도) |
| 5 | 회사 루트 (`(주)정도유아이티`) | 저장 안 함 | 단일 회사 시스템이라 불필요 |

---

## 겸직 메모 (참고, 본 스프린트 범위 밖)

스크린샷에서 관찰된 겸직:
- 노성기 대표이사: SW영업본부 + GIS사업본부
- 김남균 전무이사: SW기술연구소 + 제품개발연구부
- 이동수 이사: 응용개발연구부 + GIS사업2부
- 김형욱 전무이사: 도시계획사업본부 + 도시계획 SW영업부
- 이동윤 전무이사: 글로벌사업본부 + AI전략기획본부
- 박윤희 전무이사: AI GIS 연구본부 + R&D기획부

→ `tb_user_org_unit` N:M 테이블은 별도 후속 스프린트에서 설계.
