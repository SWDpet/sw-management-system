---
tags: [plan, sprint, schema, init-ordering, vsql-external]
sprint: "phase2-vsql-external-deps"
status: paused-v2-blocked-by-vsql-defects
created: "2026-05-11"
revised: "2026-05-11"
approved: "2026-05-11"
paused: "2026-05-11"
---

# [기획서] phase2.sql 의 외부 V*.sql 의존 INSERT 제거

- **작성팀**: 기획팀
- **작성일**: 2026-05-11
- **선행 스프린트 (완료)**: `phase2-tb_ops_doc-forward-ref` (commit `856b460`, 2026-05-11). 본 스프린트의 직접 출발점 — 그 sprint Discovery (`qt_category_mst` 외부 V024 의존) 해소가 목적.
- **상태**: ⏸ **PAUSED v2** (2026-05-11) — codex 1차 ⚠ → v2 6건 반영 → codex 2차 ⭕ → 사용자 최종승인 → 구현 Step 1~2 완료 (phase2.sql 3 INSERT 삭제 + compile PASS) → **Step 3 T2 fresh-init 검증 중 V*.sql 9건 본 sprint 외 결함 표면화로 사용자 결정 (option 4) 에 따라 phase2.sql revert + 본 sprint 일시 중단**. 본 기획서·개발계획서는 base 자료로 보존. **재진입 조건**: V*.sql 9건 결함 분석 + 해소 후. 자세한 사유는 본 문서 §10 PAUSE 사유 + 후속 sprint 백로그 참조.

## ⏸ PAUSE 상태 알림

> **본 기획서는 2026-05-11 PAUSE 상태입니다.** 본 sprint 의 **핵심 목표 (phase2.sql rc=0 in fresh-init)** 는 Step 2 구현 후 ephemeral 검증에서 ✅ 달성 입증. 그러나 fresh-init 의 후속 단계 V*.sql 9건이 본 sprint 와 별개의 결함 (의도된 abort 2건 + psql `:run_id` 변수 치환 3건 + 외부 정의 의존 4건) 을 보유하여, 기획서 NFR-2-a 의 "V*.sql 전체 실패 = 0" 게이트 미달성. 사용자 결정으로 **본 sprint 중단 + V*.sql 9건 분석 우선** 으로 전환. 본 문서 §10 참조.
- **UI 키워드 / 디자인팀 waiver**: 본 스프린트 자체의 UI 변경 0건. 본 기획서 내 "UI" 단어 등장은 frontmatter 메타·체크리스트만. **AGENTS.md §3-1 A+D 정책의 문자 매칭과 별개로, 실제 UI 변경 0 임을 근거로 디자인팀 자문 명시 waiver**. 변경 대상은 SQL/문서만이며 화면·컴포넌트·CSS·템플릿 변경 0.

### v2 변경 이력 (codex 1차 ⚠ 보완권고 6건 반영)
1. **NFR-2 명확화** — "DbInitRunner 부팅 ERROR 0" 단독 판정 → **psql `-v ON_ERROR_STOP=1` 주 판정 + DbInitRunner 보조 smoke**. (DbInitRunner 가 stmt 예외를 debug 로 삼키므로 부팅 성공만으로는 검출력 부족)
2. **NFR-5 표현 수정** — "운영DB 에 SQL 실행 없음" → "삭제된 3개 INSERT stmt 실행 X + 기존 seed row 변동 0". (DbInitRunner 가 운영에 phase2 실행 가능하므로 "SQL 실행 없음" 부정확)
3. **R-6 강화 + 신규 R-8** — DbInitRunner-only 환경 + V*.sql 누락 시 검출력 강화 + acceptance boundary 명시 ("V*.sql 미적용 환경은 본 sprint 지원 범위 외").
4. **FR-1/FR-2 표현 수정** — line 682/693/706 라인번호 의존을 약화. **SQL block 식별 (`INSERT INTO <table> ...`) 을 우선 기준**, 라인번호는 참조용.
5. **UI waiver 명시** — 위 frontmatter "UI 키워드" 항목에 명시 추가.
6. **NFR-4 검증 SQL 정밀화** — count 만이 아닌 **정렬된 `<seed columns>` 기반 MD5** SQL 을 개발계획에 고정 (개발계획서에서 SQL 본문 명시).

---

## 1. 배경 / 목표

### 배경 — 외부 V*.sql 의존 INSERT 3건 표면화

선행 sprint `phase2-tb_ops_doc-forward-ref` (commit `856b460`) 의 T1 측정 중 발견:

```
psql:src/main/resources/db_init_phase2.sql:686: ERROR:  relation "qt_category_mst" does not exist
```

**원인**: phase2.sql 안에 V*.sql 외부 정의 테이블에 대한 INSERT 가 3건. fresh-init 순서 (`phase1 → phase2 → V*.sql`) 에서 phase2 가 V*.sql 보다 먼저 실행 → 대상 테이블 미존재 → stop.

| phase2.sql INSERT 위치 | 대상 테이블 | 정의 위치 (V*.sql) | V*.sql 자체 시드 (ON CONFLICT) |
|----------------------|------------|------------------|------------------------------|
| **line 682** (3행) | `qt_category_mst` | `swdept/sql/V024_qt_category_master.sql:22` | ✅ V024:28 (`ON CONFLICT (category_code) DO NOTHING`) |
| **line 693** (10행) | `work_plan_type_mst` | `swdept/sql/V026_work_plan_master.sql:29` | ✅ V026:41 (`ON CONFLICT (type_code) DO NOTHING`) |
| **line 706** (7행) | `work_plan_status_mst` | `swdept/sql/V026_work_plan_master.sql:35` | ✅ V026:54 (`ON CONFLICT (status_code) DO NOTHING`) |

**시드 row 까지 phase2 vs V*.sql 1:1 동일** (2026-05-11 grep 비교 검증) → phase2 의 3건 INSERT 는 **중복 시드** (V024/V026 가 이미 동일 시드 책임).

### 운영DB 미발견 사유

운영DB 는 점진 적용으로 V024/V026 이 phase2 보다 먼저 적용됨 → fresh-init 흐름 미발생. 시드도 V024/V026 의 INSERT 가 이미 적용 완료. 본 스프린트 결과물 (3건 INSERT 삭제) 도 운영DB 에 영향 0 (phase2 재실행 시 해당 stmt 자체가 사라질 뿐).

### 목표

1. **phase2.sql 의 외부 V*.sql 의존 INSERT 3건 제거** — line 682 (`qt_category_mst`) + line 693 (`work_plan_type_mst`) + line 706 (`work_plan_status_mst`). fresh-init 환경에서 line 686/697/710 부근 stop 회귀 차단.
2. **fresh-init full 통과 보장** — 선행 sprint 들 (V018, tb_ops_doc) + 본 sprint 결합 효과로 `phase1 → phase2 → V*.sql` 전체 rc=0 보장. **phase 시리즈의 마지막 디딤돌**.
3. **V024 / V026 마이그레이션 무수정 원칙 유지** — Flyway checksum 보존 (V*.sql 무수정).
4. **운영DB 무영향** — 본 스프린트 결과물은 신규 환경(또는 DR) 에서만 효과 발생. 운영DB 는 V024/V026 이미 적용 → 시드 그대로.
5. **시드 의미 보존** — V024/V026 가 시드 SSoT 가 됨 (phase2 는 시드 책임 0). 시드 row 자체는 변동 없음 (1:1 동일).

### 비목표
- V024 / V026 자체 수정 (시드 row, ON CONFLICT 등) — 범위 외.
- phase2.sql 의 다른 ordering / forward-reference 점검 — 본 sprint 는 외부 V*.sql 의존 INSERT 3건만 다룸.
- fresh-init 순서 변경 (`phase1 → V*.sql → phase2`) — 더 큰 범위, 본 sprint 외.

---

## 1.5. Entry Criteria

| EC | 조건 | 검증 |
|----|------|------|
| EC-1 | 선행 sprint `phase2-tb_ops_doc-forward-ref` master 머지 완료 | `git log --oneline master | grep phase2-tb_ops_doc-forward-ref` → `856b460` 확인 |
| EC-2 | 선행 sprint base — 본 기획서 작성 시점 (2026-05-11) 의 master HEAD 가 `856b460` | 확인 완료 |
| EC-3 | V024/V026 의 시드가 phase2 의 시드와 1:1 동일하다는 사실 grep 검증 완료 | 본 §1 표 |
| EC-4 | V024/V026 의 INSERT 가 ON CONFLICT 멱등 보장 | 본 §1 표 (3건 모두 ON CONFLICT) |
| EC-5 | 본 기획서 v1+ 의 codex 1차+ 검토 ⭕ + 사용자 최종승인 | 본 문서 §9 체크리스트 |

---

## 2. 사용자 시나리오

### 2-1. 신규 개발자 PC 셋업 (목표 시나리오)
1. git clone 후 PostgreSQL 컨테이너/클러스터 기동
2. `psql -f db_init_phase1.sql` → 19 테이블 + 마스터 시드
3. `psql -f db_init_phase1_sigungu.sql` → sigungu_code
4. `psql -f db_init_phase2.sql` → ✅ **완전 무중단 실행** (선행 + 본 sprint 결합 효과로 line 60/70/464/686/697/710 모두 회귀 차단)
5. `psql -f swdept/sql/V*.sql` 순차 실행 → V024 / V026 가 자체 시드 INSERT (`ON CONFLICT DO NOTHING` 으로 멱등). V018 도 멱등 통과
6. 서버 기동 → 모든 모듈 정상

### 2-2. 재해 복구 / DR
- 운영DB 손실 시 `phase1 → phase2 → V*.sql` 만으로 schema **완전 자동 복원 가능** — **본 sprint 가 마지막 디딤돌**.

### 2-3. 운영자 (영향 0)
- 운영DB 는 본 스프린트 결과물에 노출되지 않음.
- 운영DB 에 phase2.sql 재실행 시 (DbInitRunner): 3건 INSERT 가 사라져 해당 stmt 실행 안 됨 → 운영 schema/seed 변동 0. V024/V026 가 시드 책임 (이미 적용 완료).

---

## 3. 기능 요건 (FR)

### FR-1. `qt_category_mst` 시드 INSERT 제거

> **표현 원칙 (codex v2 권고 4)**: 라인 번호는 참조용. 구현 기준은 **SQL block 식별** (대상 테이블명 + ON CONFLICT 컬럼) 우선.

| ID | 내용 |
|----|------|
| FR-1-A | **대상 SQL block**: `db_init_phase2.sql` 안의 `INSERT INTO qt_category_mst (...) VALUES (...) ON CONFLICT (category_code) DO NOTHING;` 단일 stmt (참조 line 682-686, 약 5줄) **삭제**. |
| FR-1-B | 해당 INSERT 위 주석 ("기본 ... 카테고리" 류) 도 함께 삭제 (의미 없는 잔존 주석 방지). |

### FR-2. `work_plan_type_mst` + `work_plan_status_mst` 시드 INSERT 제거

> **표현 원칙 (codex v2 권고 4)**: 라인 번호는 참조용. 구현 기준은 **SQL block 식별** (대상 테이블명 + ON CONFLICT 컬럼) 우선.

| ID | 내용 |
|----|------|
| FR-2-A | **대상 SQL block**: `INSERT INTO work_plan_type_mst (...) VALUES (...) ON CONFLICT (type_code) DO NOTHING;` 단일 stmt (참조 line 693-704, 약 12줄) **삭제**. |
| FR-2-B | **대상 SQL block**: `INSERT INTO work_plan_status_mst (...) VALUES (...) ON CONFLICT (status_code) DO NOTHING;` 단일 stmt (참조 line 706-714, 약 9줄) **삭제**. |
| FR-2-C | 두 INSERT 위 섹션 헤더 (`-- ============================================================\n-- S16 tb-work-plan-decision (2026-04-22):\n--   work_plan_type_mst 10행 + work_plan_status_mst 7행 초기 시드\n-- ============================================================`) 도 함께 삭제. |

### FR-3. `db_init_phase2.sql` 헤더 주석 보강

| ID | 내용 |
|----|------|
| FR-3-A | 파일 상단 헤더에 본 스프린트 이력 1줄 추가: "phase2-vsql-external-deps (2026-05-11) — 외부 V*.sql 정의 테이블 (qt_category_mst / work_plan_type_mst / work_plan_status_mst) 시드 INSERT 3건 제거. V024/V026 가 시드 SSoT 책임." |
| FR-3-B | 삭제 위치에 1줄 잔존 주석 추가 (선택): "-- [phase2-vsql-external-deps 2026-05-11] qt_category_mst / work_plan_type_mst / work_plan_status_mst 시드는 V024/V026 책임 — 본 위치 INSERT 제거됨." |

### FR-4. 문서 갱신

| ID | 내용 |
|----|------|
| FR-4-A | `docs/PLANS.md` §2-b 후속 백로그의 `phase2-vsql-external-deps` 항목을 **완료 스프린트** 로 이동. 산출물 / 검증 / 커밋 요약 포함. |
| FR-4-B | `docs/references/setup-guide.md` §2-2 의 보강 줄을 갱신: "phase2.sql **완전 무중단 통과 보장** (V018 + tb_ops_doc + vsql-external-deps 결합 효과)". |
| FR-4-C | (선택) `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` 의 Discovery 줄에 "완료 (commit `<TBD>`)" 라벨 추가. |

### 범위 외
- V024 / V026 자체 수정.
- phase2.sql 의 다른 ordering 점검.
- DbInitRunner 동작 변경.

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공. |
| NFR-2 | ephemeral 클러스터 (`localhost:25880`) **fresh-init full 통과** — 양 경로:<br>**(a) psql `-v ON_ERROR_STOP=1` (주 판정)**: `psql -v ON_ERROR_STOP=1 -f phase1 → sigungu → phase2 → V*.sql` 전체 rc=0. line 60/70/464/686/697/710 stop 회귀 0건. **phase2.sql rc=0** 가 본 sprint 의 핵심 판정.<br>**(b) DbInitRunner (보조 smoke)**: `bash server-restart.sh` → 부팅 ERROR 0, PSQLException 0, "relation does not exist" 0건. ※ DbInitRunner 는 stmt 별 예외를 debug 로 삼키므로 단독 판정 부적절. (a) 가 진단력 있고, (b) 는 운영 부팅 무결성만 확인. |
| NFR-3 | **재실행 멱등성 (3회 연속)** — phase2.sql 단독 3회 후 row count 변동 0. ERROR 0. |
| NFR-4 | **시드 의미 보존** — 본 sprint 적용 후 ephemeral fresh-init 완료 시점에 `qt_category_mst` (3행) + `work_plan_type_mst` (10행) + `work_plan_status_mst` (7행) 모두 V024/V026 시드와 1:1 동일 row 존재. **검증 SQL**: count + 정렬된 seed columns 기반 MD5 (구체적 SQL 본문은 개발계획서 §검증 단계에 고정. 예: `qt_category_mst` → `md5(string_agg(category_code\|\|'\|'\|\|category_label, ',' ORDER BY category_code))`). |
| NFR-5 | **운영DB 영향 0** — 본 sprint 결과물 (3건 INSERT 삭제) 이 운영DB 에 적용된 후, **삭제된 3개 INSERT stmt 는 더 이상 실행되지 않으며**, V024/V026 가 시드 책임 (이미 적용 완료) → **운영 schema/seed row 변동 0**. (DbInitRunner 는 phase2 를 부팅 시 실행하지만 본 sprint 가 stmt 자체를 제거하므로 효과 0.) |
| NFR-6 | **V024/V026 SHA256 무변경** — Flyway checksum 보존. |
| NFR-7 | **회귀 스모크** — 서버 부팅 ERROR 0. 점검내역서/사업관리/견적서/조직도/견적서 카테고리/작업계획 등 기존 기능 정상 동작. |

---

## 5. 의사결정 / 우려사항

### 5-1. 5안 비교 — F안 채택

| 안 | 요지 | 채택 여부 |
|----|------|----------|
| A | phase2 의 3건 INSERT 를 V024/V026 안으로 이동 | ❌ V*.sql 수정 → Flyway checksum 위배 (운영 영향) |
| B | V024/V026 의 CREATE TABLE 정의를 phase2 로 흡수 (선이동 패턴) | ❌ V100/tb_work_plan 사례처럼 두 곳 정의 중복 → 의미 차이 위험. 본 sprint 처럼 schema 의미 0 변경 어려움 |
| C | phase2 의 3건 INSERT 를 phase2 후반부 다른 위치로 이동 | ❌ fresh-init 순서상 phase2 가 V*.sql 보다 먼저 → 무의미 |
| D | fresh-init 순서 변경 (`phase1 → V*.sql → phase2`) | ❌ 더 큰 범위. 다른 부작용 (phase2 가 V*.sql 결과물에 더 의존하게 됨) |
| **F** | **phase2 의 3건 INSERT 단순 삭제. V024/V026 가 시드 SSoT 책임** | ✅ **채택** — V*.sql 무수정 + 운영 영향 0 + 시드 의미 보존 (1:1 동일) + 최단 분량 |

**F안 채택 사유**:
- V024/V026 가 자체 시드 INSERT 보유 + ON CONFLICT 멱등 (EC-3/EC-4)
- phase2 vs V*.sql 시드 row 까지 1:1 동일 → 단순 삭제로 시드 손실 0
- V*.sql 무수정 → Flyway checksum 보존
- 운영DB 는 V024/V026 이미 적용 → 영향 0
- 분량 최소 (단일 파일 30~40줄 삭제)

### 5-2. 잔존 stop 가능성 점검 (F안 적용 후)

F안 적용 후 phase2.sql 이 또 다른 stop 을 가질 가능성. 사전 grep (개발계획 Step 1) 으로 일제 점검 — 본 sprint 가 phase 시리즈의 마지막이 되려면 다른 forward-ref / 외부 의존이 0 이어야 함.

**현재 점검 결과 (2026-05-11)**: phase2.sql 의 `INSERT INTO` / `DELETE FROM` 대상 테이블 9건 중 외부 의존은 본 3건이 전부 (선행 sprint 의 grep 분석). 즉 본 sprint 적용 후 phase2.sql 은 **완전 무중단** 가능성 높음. 단 fresh-init T2 측정으로 최종 확인.

### 5-3. V024/V026 의 historical 보존

본 sprint 가 phase2 의 INSERT 를 삭제해도 V024/V026 가 시드 SSoT 로 남음 → 시드 history 는 V024 (`process-master-dedup` sprint) / V026 (`tb-work-plan-decision` sprint) 에 보존됨. phase2 의 주석 ("S16 tb-work-plan-decision") 같은 historical comment 는 삭제되지만, 실제 sprint 이력은 git log 와 V*.sql 헤더에 남음.

### 5-4. ephemeral 클러스터 재사용

`localhost:25880` 클러스터 (`C:\Users\PUJ\pg16-verify\data`) 그대로 재사용. 별도 기동 절차 동일.

### 5-5. DB팀 자문 결과 (본문 인라인)

| 자문 | 의견 |
|------|------|
| 5안 중 어느쪽? (A/B/C/D/F) | **F안 권장**. 가장 단순, V*.sql 무수정, 시드 1:1 동일. |
| V024/V026 의 시드와 phase2 의 시드가 row 변동 시 어떻게? | 본 sprint 적용 후 V024/V026 가 단독 책임 → 변동 시 V*.sql 만 수정 (방향 명확). 단 V*.sql 수정은 Flyway 정책상 신중. 일반적으로는 신규 V*.sql 마이그레이션 추가. |
| ON CONFLICT 보장 충분한지 | 충분. 3건 모두 PK/UNIQUE 컬럼 (`category_code`, `type_code`, `status_code`) 에 ON CONFLICT — 재실행 시 row 변동 0. |
| F안 적용 후 phase2.sql 의 다른 외부 의존 가능성? | 사전 grep 결과 없음 (§5-2). 단 T2 측정에서 stop 발생 시 신규 후속 sprint 분리. |

---

## 6. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| 초기화 SQL | `src/main/resources/db_init_phase2.sql` | 수정 (3건 INSERT + 섹션 헤더 주석 삭제 + 헤더 주석 보강) |
| 마이그레이션 SQL | `swdept/sql/V024_qt_category_master.sql` | **변경 없음** (FR 명시) |
| 마이그레이션 SQL | `swdept/sql/V026_work_plan_master.sql` | **변경 없음** (FR 명시) |
| 문서 | `docs/PLANS.md` §2-b | 수정 (후속 → 완료) |
| 문서 | `docs/references/setup-guide.md` §2-2 | 수정 1줄 (전 sprint 보강 줄 갱신) |
| 문서 | `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` | 수정 1줄 (선택, Discovery closure 라벨) |
| 문서 | `docs/product-specs/phase2-vsql-external-deps.md` | 신규 (본 문서) |
| 문서 | `docs/exec-plans/phase2-vsql-external-deps.md` | 신규 (개발계획) |

**합계**: 신규 2 (기획서·개발계획), 수정 4. **Java 변경 0**. **운영DB 변경 0**. V*.sql 변경 0.

---

## 7. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | phase2 INSERT 삭제 후 시드 손실 (V*.sql 미적용 환경) | 매우 낮음 | V024/V026 가 자체 시드 + ON CONFLICT 멱등. fresh-init 전체 절차 완료 시점엔 동일 row 보장 (NFR-4) |
| R-2 | 운영DB 에 본 변경 적용 시 시드 변동 | 0 | phase2 의 INSERT 가 사라져 stmt 자체 미실행. 운영 시드는 V024/V026 적용 결과 그대로 |
| R-3 | V024/V026 시드 row 와 phase2 시드 row 가 미세하게 다른 row 이 있을 수 있음 (놓침) | 매우 낮음 | 2026-05-11 grep 결과 1:1 동일 검증 완료 (§1 표). 개발계획 Step 1 에서 한번 더 명시 검증 |
| R-4 | F안 적용 후 phase2.sql 의 다른 외부 의존 / forward-ref 노출 | 낮음 | 사전 grep (§5-2) 결과 본 3건 외 없음. T2 측정으로 최종 확인. 발견 시 신규 후속 sprint 분리 (E3 패턴) |
| R-5 | V024/V026 Flyway checksum 변경 | 0 | V*.sql 무수정 (FR 명시). NFR-6 SHA256 검증 |
| R-6 | DbInitRunner-only 환경 (V*.sql 미적용) 에서 phase2 의 외부 의존 INSERT 가 사라져 시드 부재로 기능 깨짐 | 낮음 | 본 sprint 의 **acceptance boundary**: V*.sql 미적용 환경은 본 sprint 지원 범위 외. setup-guide.md §2-2 절차 위반으로 판정. 운영DB 는 V024/V026 이미 적용 (영향 0). 신규 환경은 setup-guide.md §2-2 따라 phase1+phase2+V*.sql 전부 적용 필요. **검출**: NFR-7 회귀 스모크 (점검내역서 / 견적서 카테고리 / 작업계획) 가 시드 부재 시 즉시 ERROR — DbInitRunner 의 stmt 별 debug 삼킴은 회귀 스모크가 보완 |
| R-7 | 시드 history (S16 tb-work-plan-decision 주석 등) 손실 | 매우 낮음 | history 는 git log + V024/V026 헤더 + PLANS.md 에 보존. phase2 의 주석은 historical comment 일 뿐 의미 변경 0 |
| R-8 | (codex v2 권고 3 신규) DbInitRunner 가 stmt 별 예외를 debug 로 삼켜 V*.sql 누락 환경에서도 부팅 자체는 성공 → 운영자가 결함 인지 못 함 | 낮음 | 본 sprint 자체는 R-8 환경을 만들지 않음 (오히려 phase2 의 stop 을 제거). 단 향후 V*.sql 누락 환경 결함 검출은 본 sprint 외 — 별도 후속 (DbInitRunner 진단 모드 등) 후보. NFR-2 (b) 의 "보조 smoke" 위치 부여로 본 sprint 검증 단계의 잘못된 신뢰 차단 |

---

## 8. 산출물 요약

본 스프린트 종료 시:
- `src/main/resources/db_init_phase2.sql` — 3건 INSERT + 섹션 헤더 삭제 + 헤더 주석 보강
- `docs/product-specs/phase2-vsql-external-deps.md` — 본 기획서
- `docs/exec-plans/phase2-vsql-external-deps.md` — 개발계획
- `docs/PLANS.md` §2-b — 완료 표기
- `docs/references/setup-guide.md` §2-2 — 1줄 갱신
- (선택) `docs/product-specs/phase2-tb_ops_doc-forward-ref.md` — Discovery closure 라벨

검증 결과:
- T1 phase2 멱등 3회 (rc=0, ERROR 0)
- T2 psql fresh-init **완전 통과** (`phase1 → phase2 → V*.sql` 전체 rc=0)
- T2-b DbInitRunner 부팅 ERROR 0
- T3 시드 의미 보존 (3개 테이블 row count + MD5 동일)
- T4 V024/V026 SHA256 무변경
- T5 회귀 스모크 (점검내역서/사업관리/견적서/조직도/견적서 카테고리/작업계획)

---

## 9. 승인 요청

본 기획서에 대한 codex 검토 + 사용자 최종승인 요청.

### 승인 전 확인 사항
- [x] F안 채택 사유 명시 (5안 비교 + DB팀 자문)
- [x] V024/V026 무수정 + Flyway checksum 보존 (FR/NFR)
- [x] 운영DB 무영향 명시 (NFR-5, v2 표현 보강)
- [x] ephemeral 클러스터 25880 재사용 가능
- [x] UI 키워드 의도상 0건 → 디자인팀 명시 waiver (frontmatter)
- [x] DB팀 자문 본문 인라인 (§5-5)
- [x] 선행 스프린트 commit `856b460` 머지 확인 (EC-1)
- [x] V024/V026 의 시드가 phase2 와 1:1 동일 grep 검증 완료 (EC-3)
- [x] V024/V026 의 INSERT 가 ON CONFLICT 멱등 검증 완료 (EC-4)
- [x] codex 1차 검토 (v1 기준) — ⚠ 보완권고 6건
- [x] **v2 개정 (codex 권고 6건 전체 반영)** — frontmatter §v2 변경 이력 참조
- [x] codex 2차 검토 (v2 기준) — ⭕ 승인 가능 (비차단 권고 1건은 개발계획서에서 처리)
- [x] **사용자 최종승인 (EC-5) — 2026-05-11**

### 다음 절차

1. **codex 2차 검토** (gpt-5.5 via codex-trace.sh, round=2) — v2 권고 반영 확인
2. ⭕ → 사용자 최종승인 → 개발계획서 작성 (`docs/exec-plans/phase2-vsql-external-deps.md`)
3. ⚠ → v3 개정 (또는 부분 수용 결정)
4. 개발계획 codex 검토 → 사용자 최종승인 → 구현 → 검증 → "작업완료" → 자동 commit+push

---

## 10. PAUSE 사유 + 재진입 조건 (2026-05-11 추가)

### 10-1. PAUSE 시점 + 직전까지의 진행

| 단계 | 상태 |
|------|------|
| 기획서 v1 → v2 (codex 1차 ⚠ 6건 반영) → codex 2차 ⭕ → 사용자 최종승인 | ✅ 완료 |
| 개발계획 v1 → v1.1 → v1.2 (codex 1차 ⚠ 4건 + 2차 ⚠ 1건 반영) → codex 3차 ⭕ → 사용자 최종승인 | ✅ 완료 |
| Step 1 사전 스캔 + baseline 기록 (BASELINE_N=127) | ✅ 완료 |
| Step 2 phase2.sql 3 INSERT 삭제 + compile (BUILD SUCCESS) | ✅ 완료 (revert 됨) |
| Step 3-1 ephemeral DB 4개 생성 | ✅ 완료 |
| Step 3-2 T1 phase2 멱등 3회 (rc=0, ERROR 0) | ✅ Pass |
| Step 3-3 T2 fresh-init 검증 — **phase2.sql rc=0 (본 sprint 핵심 ✅)** | ✅ 핵심 Pass |
| Step 3-3 T2 V*.sql 적용 — **9건 본 sprint 외 결함 표면화** | ❌ Pause |

### 10-2. V*.sql 9건 결함 분류 (T2 측정 시점)

| # | V*.sql | line | 종류 | 본 sprint 와의 관계 |
|---|--------|------|------|--------------------|
| 1 | V005_add_surveying_wage_rates | 11 | `qt_wage_rate does not exist` (외부 정의 의존 INSERT) | 본 sprint 와 같은 패턴, 다른 테이블 |
| 2 | V019_access_log_userid_cleanup | 37 | `:run_id` psql 변수 치환자 | psql 호출 옵션 의존 (V*.sql 무수정 가능) |
| 3 | V020_qt_remarks_pattern_user_link | 39 | `:run_id` 동일 | psql 호출 옵션 의존 |
| 4 | V021_users_masking_regression_fix | 31 | `:run_id` 동일 | psql 호출 옵션 의존 |
| 5 | V021_rollback_data | 43 | 의도된 abort (`HALT: no V021 backup table`) — fresh-init 정상 | skip 명시만 필요 |
| 6 | V022_inspect_comprehensive_redesign | 56 | 의도된 abort (`HALT Phase 0: UPIS_SW 총 0 (기준 22±5)`) — Phase 0 안전 게이트 | skip 명시만 필요 |
| 7 | V023_inspect_category_master | 65 | `check_section_mst does not exist` | 외부 정의 의존 |
| 8 | V025_drop_pjt_equip | 54 | `pjt_equip does not exist` (phase2 가 이미 제거) | V025 의 멱등성 결함 |
| 9 | V100_work_plan_performance_tables | 221 | `column "contract_id" does not exist` | 외부 컬럼 의존 |

### 10-3. PAUSE 사유 (사용자 결정 흐름)

T2 측정 결과 본 sprint 의 핵심 목표 (phase2.sql rc=0) 는 ✅ 달성. 그러나 기획서 NFR-2-a 의 "fresh-init 전체 rc=0" 충족을 위해 V*.sql 9건도 처리할지 사용자에게 4개 option 제시:
- option 1: 본 sprint 핵심 commit + V*.sql 9건은 신규 후속 sprint 등록
- option 2: 본 sprint 안에서 9건 부분 처리 (의도/skip 5건 + `:run_id` 3건 = setup-guide 보강)
- option 3: 본 sprint 안에서 9건 전부 처리 (1~2주 분량, Flyway 위배 위험)
- option 4: **본 sprint 중단 + V*.sql 9건 먼저 분석** ← **사용자 채택**

option 4 채택 사유 (추정): NFR-2-a 의 "V*.sql 전체 rc=0" 가 본 sprint 의 의미 있는 게이트가 되려면 9건이 사전 해소돼야 하므로, 9건의 nature 를 알기 전에 본 sprint commit 하는 것은 미완.

### 10-4. PAUSE 처리 결과 (2026-05-11)

| 처리 | 상태 |
|------|------|
| `src/main/resources/db_init_phase2.sql` revert (`git checkout --`) | ✅ 완료 (작업트리 = `856b460` 동등) |
| 본 기획서 v2 status `paused-v2-blocked-by-vsql-defects` 표기 | ✅ 본 §10 |
| `docs/exec-plans/phase2-vsql-external-deps.md` v1.2 status `paused-v1.2-blocked-by-vsql-defects` 표기 | (별도 문서) |
| 운영 환경 server-restart 안정화 검증 | (다음 단계) |
| ephemeral DB 4개 (sw_u1/u2/u2b/u2_baseline) | 보존 (다음 작업 재사용 가능) |
| `/tmp/baseline_N.txt` (BASELINE_N=127) | 보존 (재진입 시 사용) |

### 10-5. 재진입 조건

본 기획서·개발계획서를 재사용하여 본 sprint 를 재개하려면 다음 모두 충족:

1. **V*.sql 9건 결함 분석 보고서** 작성 (별도 문서, 예: `docs/references/vsql-fresh-init-defects-analysis.md`)
2. 9건 중 처리 방침 (해소 / skip 명시 / 별도 sprint 분리) 결정
3. 본 sprint 의 NFR-2-a 게이트가 새 결정 후에도 의미 있는지 재평가 — 필요 시 본 기획서 v3 개정
4. 재진입 시 status `paused-v2-...` → `approved-v3` (또는 `resumed-v2`) 로 갱신

### 10-6. 후속 sprint 백로그 권고 (PLANS.md §2-b)

- `vsql-fresh-init-defects-analysis` — V*.sql 9건 분류 + 해소 방침 결정 (분석 작업, 결정 보고서)
- `vsql-external-defs-V005-V023` — 외부 정의 의존 INSERT (V005 qt_wage_rate, V023 check_section_mst) 일괄 처리 (본 sprint 와 같은 패턴)
- `vsql-V025-pjt_equip-idempotent` — V025 멱등성 결함 (pjt_equip 이미 제거된 환경)
- `vsql-V100-column-deps` — V100 컬럼 의존 분석
- `vsql-runid-psql-variables` — V019/V020/V021_users 의 `:run_id` 호출 절차 정리
- `vsql-intentional-abort-skip-list` — V021_rollback_data, V022 Phase 0 의 fresh-init skip 명시 (setup-guide §2-2)
