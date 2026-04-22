---
tags: [plan, sprint, inspect, master, normalization, wave-3]
sprint: "inspect-check-result-category-master"
priority: P2
wave: 3
status: draft
created: "2026-04-22"
---

# [기획서] inspect_check_result category 마스터 신설 (S10)

- **작성팀**: 기획팀
- **작성일**: 2026-04-22
- **근거 로드맵**: [[data-architecture-roadmap]] v2 §S10 (Wave 3 / P2)
- **선행 스프린트**: [[inspect-comprehensive-redesign]] S1 완료 (2026-04-22, check_section_mst 9행 + FK 적용 상태)
- **상태**: v2 (codex v1 ⚠수정필요 5건 반영)
- **v1→v2 변경점**:
  1. **FR-5 분리**: `db_init_phase2.sql` 시드 정리를 FR-5 단일 정의로 확정. `result_code` 검증은 본 스프린트 범위 제외 (후속 별도)
  2. **inspect_check_result 비0 대비**: Phase 0 게이트에 `inspect_check_result.category` 불일치 카운트 포함 + Phase 1 정규화를 양쪽 테이블에 모두 적용
  3. **NULL loophole 차단**: 최소 `inspect_template.category` NOT NULL 강제 (마이그 Phase 포함). `inspect_check_result` 도 CHECK 또는 NOT NULL 정책 명시
  4. **VARCHAR 길이 일치**: 마스터/엔티티 모두 `VARCHAR(50)` (기존 컬럼 기준) — 변경 필요 시 ALTER 선행
  5. **공백 검증 범위**: NFR 검증식에 `GeoWeb Server(GWS)` + `Spatial Server(GSS)` 양쪽 포함

---

## 0. 사전 조사 결과 (2026-04-22 실측)

### 0-1. 현재 상태 (S1 직후)
- `inspect_template`: 70 rows
- `inspect_check_result`: 0 rows (S1 TRUNCATE 후)
- `inspect_template.category`: **18 distinct 값**
- `inspect_template.section` FK → `check_section_mst` (S1 Phase 6 에서 설정됨)

### 0-2. 실측 category 분포

| section | category | cnt | 비고 |
|---------|----------|----:|------|
| AP | H/W | 6 | |
| AP | OS | 5 | |
| AP | 백업 | 1 | |
| AP | 기타(사용자지정) | 2 | (임의, 실측값) |
| DB | DATA 영역 | 3 | |
| DB | 버전 정보 | 5 | |
| DB | 네트워크 | 3 | |
| DB | 로그 | 2 | |
| DB | 부팅 정보 | 5 | |
| DB | 하드웨어 | 5 | |
| DB | 프로세스 | 1 | |
| DBMS | 오라클 | 17 | |
| GIS | GeoNURIS Desktop Pro | 2 | |
| GIS | **GeoNURIS GeoWeb Server (GWS)** | 3 | 공백 O |
| GIS | **GeoNURIS GeoWeb Server(GWS)** | 3 | 공백 X ← **중복** |
| GIS | **GeoNURIS Spatial Server (GSS)** | 3 | 공백 O |
| GIS | **GeoNURIS Spatial Server(GSS)** | 3 | 공백 X ← **중복** |
| GIS | 클라이언트 프로그램 | 1 | |

> (인코딩 제약으로 일부 한글은 실제 DB 값과 미세 차이 있을 수 있음. 개발계획 Step 1에서 정확 채록)

### 0-3. 핵심 이슈
1. **공백 차이 2쌍 중복** (감사 §S10 직접 지적):
   - `GeoNURIS GeoWeb Server (GWS)` vs `GeoNURIS GeoWeb Server(GWS)`
   - `GeoNURIS Spatial Server (GSS)` vs `GeoNURIS Spatial Server(GSS)`
2. **db_init_phase2.sql 시드**에서도 양쪽 병존 중 (서버 재기동 시 재삽입되어 같은 상태 반복)
3. `check_section_mst` 는 있지만 **category 마스터는 없음** → 신규 row 작성 시 오탈자·변형 재발 가능

---

## 1. 목적

- **FR-1**: `check_category_mst` 마스터 테이블 신설 — (section_code, category_code) 복합키 (§3-A opt1 권장)
- **FR-2**: `inspect_template.category` + `inspect_check_result.category` **양쪽** 공백 변형 2쌍 정규화 (v2 확장)
- **FR-3**: `inspect_template` + `inspect_check_result` 에 **FK 제약** 추가
- **FR-4**: **NULL 정책** (v2 신규):
  - `inspect_template.category` — **NOT NULL** 강제 (마스터 성격, 시드 전원 non-null)
  - `inspect_check_result.category` — **NOT NULL 유예** + **CHECK (category IS NULL OR EXISTS in master)** FK 로 대체. 사유: inspect 보고서 작성 중 임시 저장 케이스에서 null 허용 필요. 단 비-null 값은 반드시 마스터 참조
- **FR-5** (v2 확정): `db_init_phase2.sql` 시드 공백X 2쌍 제거 + 정식 형태로 통일
- ~~result_code 검증~~ → 본 스프린트 범위 제외 (후속 별도 스프린트)

---

## 2. 범위

### 2-1. 포함
- 신규 테이블: `check_category_mst`
- V023 마이그레이션 SQL 단일 파일 (사전검증 + UPDATE + CREATE + FK + NOT NULL)
- **`inspect_template` + `inspect_check_result` 양 테이블 UPDATE** (공백 정규화, v2 보강)
- `db_init_phase2.sql` 시드 정리
- `InspectTemplate.java` / `InspectCheckResult.java` 엔티티 주석 업데이트
- 회귀 테스트 ≥ 3건

### 2-2. 제외
- inspect_check_result 데이터 정제 — 현재 0 rows (S1 TRUNCATE 이후), 해당 없음
- Java ENUM 화 — category가 18+ 종이고 사용자 정의 확장 가능 → 마스터 테이블만으로 충분 (과잉 설계 회피)
- UI 변경 (`doc-inspect.html` 등) — 본 스프린트는 데이터 계층만. 향후 드롭다운 대체는 별도

---

## 3. 사용자 결정 필요 사항

### 3-A. `check_category_mst` 설계 (택 1)

| 옵션 | 설계 | 장점 | 단점 |
|------|------|------|------|
| **A-opt1 (권장)** | `(section_code, category_code)` **복합키** | section마다 독립 category, 의미상 정확, 동일 category명이 타 section에 중복 허용 | 쿼리 시 항상 section 함께 지정 필요 |
| A-opt2 | `category_code` **단일 PK** | 쿼리 단순 | 다른 section의 같은 이름 category 허용 불가 (현재 없음, 향후 확장 시 제약) |
| A-opt3 | 독립 `category_id` SERIAL + `(section_code, category_name)` UNIQUE | 가장 유연 | 설계 복잡, Entity 리팩터 과함 |

**기획 권장**: **A-opt1 복합키** — `check_section_mst` 와 일관성 유지 (같은 "마스터 코드" 패턴), 현 18행 규모에 적합

### 3-B. 공백 변형 정식화 방향 (택 1)

GIS 섹션의 2쌍:
| 옵션 | 선택 | 사유 |
|------|------|------|
| **B-opt1 (권장)** | **공백 O 유지** (`GeoNURIS GeoWeb Server (GWS)`, `GeoNURIS Spatial Server (GSS)`) | 영문 약어 표기 관례상 공백 있는 버전이 정식 |
| B-opt2 | 공백 X 유지 | 짧음 |

**기획 권장**: **B-opt1** — 영문 문서 관례. 공백 X 버전 2 category 를 O 버전으로 UPDATE.

### 3-C. FK 추가 범위 (택 1)

| 옵션 | 대상 |
|------|------|
| **C-opt1 (권장)** | `inspect_template.(section, category)` + `inspect_check_result.(section, category)` 양쪽 FK |
| C-opt2 | `inspect_template`만 FK. check_result 는 FK 없음 (쓰기 시 template 참조하므로 간접 보장) |

**기획 권장**: **C-opt1** — 무결성 강제가 확실. inspect_check_result 신규 저장 시 오타 유입 차단.

### 3-D. 초기 마스터 시드 (실측 기반 16행, B-opt1 반영)

| # | section | category_code | category_label |
|---|---------|---------------|----------------|
| 1 | AP | H/W | H/W |
| 2 | AP | OS | OS |
| 3 | AP | 백업 | 백업 |
| 4 | AP | (실측값) | (실측값) |
| 5~11 | DB | 7종 | 동일 |
| 12 | DBMS | 오라클 | 오라클 |
| 13 | GIS | GeoNURIS Desktop Pro | 동일 |
| 14 | GIS | GeoNURIS GeoWeb Server (GWS) | 동일 (공백 O) |
| 15 | GIS | GeoNURIS Spatial Server (GSS) | 동일 (공백 O) |
| 16 | GIS | 클라이언트 프로그램 | 동일 |

> 최종 16행 (공백 중복 2행 제거). 정확한 행수·한글 label은 개발계획 Step 1에서 실측 재확인.

---

## 4. 제안 설계

### 4-1. 스키마

```sql
-- v2: 기존 entity 와 길이 일치 (VARCHAR(50))
CREATE TABLE check_category_mst (
  section_code    VARCHAR(20)  NOT NULL
    REFERENCES check_section_mst(section_code),
  category_code   VARCHAR(50)  NOT NULL,   -- 한글 포함, 기존 컬럼과 동일 길이
  category_label  VARCHAR(100) NOT NULL,
  display_order   INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (section_code, category_code)
);
```

### 4-2. 마이그레이션 순서 (V023, v2 강화)

1. **Phase 0**: 사전검증 (category distinct 확인 + 공백 변형 2쌍 확인 + **inspect_check_result.category 불일치 카운트**)
2. **Phase 1**: `inspect_template` **및** `inspect_check_result` 양쪽에 공백 X → O UPDATE (멱등, WHERE 조건부)
3. **Phase 2**: `check_category_mst` CREATE + 16행 시드 (ON CONFLICT DO NOTHING)
4. **Phase 3 (FK ADD 전 Exit Gate)**: 양 테이블의 (section, category) 값이 전부 마스터에 있는지 확인 → 불일치 시 EXCEPTION
5. **Phase 4**: FK ADD (`fk_it_category` + `fk_icr_category`)
6. **Phase 5**: `inspect_template.category` **NOT NULL** 전환 (기존 NULL 데이터 존재 시 EXCEPTION)
7. **Phase 6**: 사후검증 (마스터 행수 + FK 2개 + 공백 변형 양 테이블 0 + NOT NULL 적용)

### 4-3. 멱등성
- `CREATE TABLE IF NOT EXISTS`
- 시드 `ON CONFLICT (section_code, category_code) DO NOTHING`
- UPDATE WHERE 조건부 (재실행 시 0 rows)
- FK ADD는 `IF NOT EXISTS` 가드

---

## 5. FR / NFR

### FR
- FR-1 ~ FR-4 (§1)
- **FR-5** (개발계획 단계에서 확정): `db_init_phase2.sql` 시드의 공백 X 2쌍 제거

### NFR
- **NFR-1**: check_category_mst 16행 (실측 기반, 개발계획서에서 정확값 고정)
- **NFR-2**: `inspect_template.category` + `inspect_check_result.category` **양쪽** 공백 변형 0건 (v2 확장)
- **NFR-3**: FK 2개 존재 (`fk_it_category`, `fk_icr_category`)
- **NFR-4**: db_init_phase2.sql 시드 — `rg "GeoNURIS GeoWeb Server\(GWS\)|GeoNURIS Spatial Server\(GSS\)" = 0 hits` (공백 X **두 쌍 모두** 제거 확인, v2 보강)
- **NFR-5**: 컴파일 + 전체 테스트 green + 서버 기동 정상
- **NFR-6**: 회귀 테스트 신규 ≥ 3건 (CategoryMst seed 수 / FK 제약 존재 / 공백 X row 0 양 테이블)
- **NFR-7** (v2 신규): `inspect_template.category` NOT NULL 제약 적용 (null 유입 차단)

---

## 6. 리스크 / 완화

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | 공백 변형 UPDATE 과정에서 타 row 오염 | 낮음 | 개발 Step에서 WHERE category = '(공백X 정확값)' 고정 |
| R-2 | check_category_mst 시드 누락 → FK ADD 실패 | 중 | Phase 3 게이트: `SELECT COUNT(DISTINCT category) WHERE NOT IN (master)` = 0 검증 |
| R-3 | 한글 category의 정확 채록 실패 | 중 | 개발계획 Step 1 precheck 러너가 실측 후 자동 생성 |
| R-4 | 향후 신규 category 추가 절차 부재 | 낮음 | 본 문서에 "추가 방법: V*** migration + check_category_mst INSERT" 명시 |

---

## 7. 대안 / 비채택

| 대안 | 설명 | 비채택 사유 |
|------|------|-------------|
| Enum 도입 | Java Enum 으로 18종 관리 | 사용자 확장 가능성 차단 + 한글 문자열이라 상수명 부적합 |
| TRIM 만 수행, 마스터 없음 | 단순 정규화만 | 재오염 재발. 감사 지적은 마스터화 |

---

## 8. 사용자 승인 요청 사항

### 결정 필요 항목
1. **A-opt1 복합키** (section+category) — 권장
2. **B-opt1 공백 O 통일** — 권장
3. **C-opt1 양쪽 FK** — 권장
4. 초기 시드 16행 골조 승인 (정확한 한글 label은 개발계획 Step 1에서 확정)

### 승인 확인 사항
- [ ] §3 A/B/C 결정
- [ ] §4 V023 마이그 순서
- [ ] §5 FR-1~FR-5, NFR-1~NFR-7 (NFR-7: inspect_template.category NOT NULL)
- [ ] §6 리스크 (R-1~R-4)
