---
tags: [plan, sprint, refactor, schema, drop]
sprint: "legacy-contract-tables-drop"
status: draft-v2
created: "2026-04-20"
---

# [기획서] 레거시 계약 테이블 DROP — `tb_contract` / `tb_contract_target`

- **작성팀**: 기획팀
- **작성일**: 2026-04-20
- **선행 커밋**: `8d79f82` (하드코딩 개선 + 데이터 아키텍처 감사 3 스프린트 완료)
- **근거**: [[data-architecture-roadmap]] v2 P1 Wave 1 S6 / [[data-architecture-utilization-audit]] §기능8
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 4건 반영)
1. **사전 검증 SQL (`FR-0`) 신설** — `information_schema.table_constraints` + `key_column_usage` + `constraint_column_usage` (PostgreSQL 기준) 기반 외부 FK 존재 확인. 0건 보장 후 진행
2. **재생성 방지 경로 확장** — `db_init_phase2.sql`은 해당 DDL 없음(재검증 완료) → FR-2 재표적. `swdept/sql/V100_work_plan_performance_tables.sql:52-93`의 `tb_contract` CREATE TABLE + 3개 INDEX + COMMENT 블록 제거 or 주석 처리
3. **롤백 복원 DDL 기준 고정** — 선행 커밋 `8d79f82`의 `swdept/sql/V100_work_plan_performance_tables.sql:52-93` 을 복원 원천으로 명시
4. **용어 보정** — "FK 참조 없음" → "외부 FK 없음 (사전검증 결과 기준)"
5. **신규 발견 대응** — `tb_document.contract_id` 컬럼 존재 확인 (Batch C 스캔), 처리 방향 §4-6에 명시
- **사용자 사전 확정**: 2026-04-20 "tb_contract는 불필요" 확정

---

## 1. 배경 / 목표

### 배경

Data Architecture Audit 스프린트에서 `tb_contract`·`tb_contract_target` 이 **완전 고아 테이블**로 판정됨. 본 스프린트로 정리한다.

### 고아 테이블 판정 근거 (실측, v2 재검증)

| 검증 항목 | `tb_contract` | `tb_contract_target` |
|-----------|---------------|-----------------------|
| DB 레코드 수 | **0** | **0** |
| Java Entity | **없음** (grep 0) | 없음 |
| Repository | 없음 | 없음 |
| Service | 없음 | 없음 |
| Controller | 없음 | 없음 |
| ERD 참조 | 2026-04-19 스프린트 2a에서 제거 완료 | 동일 |
| JPQL `@Query` 참조 | 없음 | 없음 |
| DDL 소스 | `swdept/sql/V100_work_plan_performance_tables.sql:52-93` (CREATE + 3 INDEX + COMMENT) | V100 포함 (FK 관계) |
| 외부 FK 의존 | **⚠️ 사전검증 필요** — `tb_document.contract_id` 컬럼 존재 (Batch C 확인), FK 제약 유무 미확인 | 내부 FK(`→tb_contract.contract_id`) 외는 없을 것으로 추정, 사전검증 필수 |

**v2 변경**: 이전 v1 "FK 참조 없음" 표현 → "외부 FK 없음 (사전검증 결과 기준)"으로 조건부 수정. 실행 전 §FR-0 사전검증 SQL로 강제 확인.

### `sw_pjt`로 기능 완전 대체 확인 (사용자 확정)

`tb_contract` 30개 컬럼 → 기초 4개 기능(사업관리·담당자관리·서버관리) + 공통 마스터로 커버:

| 원 컬럼 영역 | 대체 위치 |
|--------------|----------|
| 식별·이름·유형 | `sw_pjt.proj_id`/`proj_nm`/`cont_type` |
| 금액 | `sw_pjt.cont_amt`/`sw_amt`/`outscr_amt` 등 |
| 기간·상태 | `sw_pjt.cont_dt`/`start_dt`/`end_dt`/`stat`/`year` |
| 클라이언트 조직·부서·주소 | `ps_info.org_nm`/`dept_nm` + `sigungu_code.full_name` |
| 클라이언트 담당자·연락처 | `ps_info.user_nm`/`tel`/`email` |
| 원청·하도 | `sw_pjt.cont_type` (직접/하도) |
| 인프라 연관 | `sw_pjt.proj_id` ↔ `tb_infra_master` |

### 목표
1. DB(PostgreSQL, 스키마 `public`)에서 `tb_contract`/`tb_contract_target` 테이블 **완전 제거** (DROP)
2. `swdept/sql/V100_work_plan_performance_tables.sql:50-93`의 `tb_contract` 관련 DDL 블록 **주석 처리** (향후 V100 재실행 시 재생성 방지). 다른 테이블 블록은 미변경
3. 관련 문서(`ERD.md` 등) 정합성 유지
4. **`src/main/resources/db_init_phase2.sql`은 대상 아님** (v2 재검증: 해당 DDL 없음 확인, grep 0 hits)

---

## 2. 기능 요건 (FR)

### FR-0. 사전 검증 (v2 codex 권장 #1) — DROP 실행 전 강제 체크

**DB 전제**: PostgreSQL, 스키마 `public` (프로젝트 환경: `jdbc:postgresql://211.104.137.55:5881/SW_Dept`).

아래 2개 SQL 결과가 **모두 0건**이어야 진행. 1건이라도 있으면 **실행 중단** + 별도 처리(FK 제거 or 범위 확장 판단).

(참고: 타 DBMS 이관 시 `table_schema` 필터를 해당 DBMS 규칙으로 조정 필요 — 현재 전제는 PostgreSQL.)

```sql
-- (1) tb_contract / tb_contract_target 을 참조하는 외부 FK 목록
SELECT tc.table_schema, tc.table_name AS referencing_table, tc.constraint_name,
       kcu.column_name AS fk_column,
       ccu.table_name AS target_table, ccu.column_name AS target_column
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
 WHERE tc.constraint_type = 'FOREIGN KEY'
   AND tc.table_schema = 'public'
   AND ccu.table_name IN ('tb_contract', 'tb_contract_target')
   AND tc.table_name NOT IN ('tb_contract', 'tb_contract_target');  -- 자기자신 간 FK는 허용
-- 기대: 0건

-- (2) 데이터 실재 여부 재확인
SELECT 'tb_contract' AS t, COUNT(*) AS n FROM tb_contract
UNION ALL
SELECT 'tb_contract_target', COUNT(*) FROM tb_contract_target;
-- 기대: 양쪽 모두 0
```

### FR-1~4 조치

| ID | 내용 |
|----|------|
| FR-1 | 마이그레이션 SQL `swdept/sql/V###_drop_legacy_contract_tables.sql` 신규. 순서: `DROP TABLE IF EXISTS tb_contract_target;` → `DROP TABLE IF EXISTS tb_contract;` (FK 의존 관계). 본문에 v2 사전검증 SQL 복사 + 실행 결과 로그 남기는 패턴. 트랜잭션(`BEGIN/COMMIT`) 적용 |
| FR-2 | **`swdept/sql/V100_work_plan_performance_tables.sql:50-93`** 의 `tb_contract` 관련 블록 (CREATE TABLE + 3개 INDEX + COMMENT) **전체 주석 처리** (향후 V100 재실행 시 재생성 방지). `tb_contract_participant`(V100:97~) 등 다른 테이블 블록은 **절대 건드리지 않음** |
| FR-2-NOTE | `src/main/resources/db_init_phase2.sql`은 `tb_contract` 관련 DDL **없음** (v2 재검증 완료, grep 0 hits) — 수정 대상 아님 |
| FR-3 | `docs/ERD.md` 5번·7번 섹션의 "미구현" 표기에 "**삭제 완료 (2026-04-20 스프린트 `legacy-contract-tables-drop`)**" 추가. `docs/erd-diagram.html`·`docs/erd-descriptions.yml`·`docs/erd-document.mmd`에서 남은 `tb_contract`/`contract_id` 참조가 있으면 "(삭제됨)" 표기 추가 또는 제거 |
| FR-4 | `DbInitRunner` 코드 재검토 — `V100_work_plan_performance_tables.sql` 자동 실행 여부 확인. 실행된다면 FR-2 주석 처리로 재생성 방지 보장 |
| FR-5 (v2 추가) | `tb_document.contract_id` 컬럼 처리 방향 **판단**. FR-0 사전검증에서 FK가 없고 코드 참조도 없으면 **별도 스프린트로 이관** (본 스프린트 범위 외). FK가 있으면 FK 제거만 본 스프린트에서 병행 처리 가능 (컬럼 DROP은 범위 외) |

### 범위 외 (명시)
- Java 코드 변경 **없음** (Entity/Repo/Service/Controller 참조 0건 재검증 완료)
- 다른 감사 대상 레거시 테이블(`tb_inspect_checklist`, `tb_inspect_issue`, `tb_document_signature` 등)은 S1 `inspect-comprehensive-redesign` 하위 태스크로 처리. **본 스프린트에서 동시 정리 금지 (범위 게이트)**
- `tb_document.contract_id` 컬럼 자체 DROP은 범위 외 (FR-5 참조)

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공 |
| NFR-2 | 서버 재기동 후 정상 부팅. `Started SwManagerApplication` 로그 + ERROR 0 |
| NFR-3 | DROP 후 회귀: 로그인·사업관리·문서관리·점검내역서 상세 4화면 정상 렌더링 |
| NFR-4 | DB 검증 SQL: `SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('tb_contract','tb_contract_target')` = 0 |
| NFR-5 | 코드 참조 재검증: `rg -n 'tb_contract\b' src/main/java` = 0 hits (ContractParticipant 는 별개 테이블이므로 허용) |
| NFR-6 | `db_init_phase2.sql` 재실행 시 테이블이 다시 생성되지 않음 (멱등성 확인) |

---

## 4. 의사결정 / 우려사항

### 4-1. DROP 순서 — ✅ `tb_contract_target` 먼저
- `tb_contract_target`의 `contract_id`가 `tb_contract.contract_id`를 FK로 참조하는 구조 (DB 스키마 확인)
- FK 의존 관계상 자식 먼저 DROP, 부모 나중 DROP
- `IF EXISTS` 사용으로 멱등성 보장

### 4-2. 롤백 가능성 — ✅ 복원 DDL 소스·버전 고정 (v2 codex 권장 #3)

- DROP은 비가역이므로 복원 DDL을 고정값으로 명시 보관
- **복원 원천**:
  - 파일: `swdept/sql/V100_work_plan_performance_tables.sql`
  - 라인: `50-93` (tb_contract CREATE + 3 INDEX + COMMENT)
  - 기준 커밋: **`8d79f82`** (본 스프린트 선행 커밋, 2026-04-20)
  - `tb_contract_target`은 V100에 없으면 DDL 복원 별도 필요 — FR-0 사전검증 시점에 V100 전체 재확인
- 데이터 0건 확인으로 구조 복원만으로 충분 (데이터 복원 불필요)
- 롤백 절차 3단계:
  1. `git show 8d79f82:swdept/sql/V100_work_plan_performance_tables.sql` 로 원본 추출
  2. 해당 파일의 `tb_contract` 섹션을 분리 SQL로 실행 (CREATE + INDEX + COMMENT 순)
  3. `tb_contract_target`도 필요하면 동일 커밋에서 추출·실행

### 4-3. 실행 시점 — ✅ 업무시간 외 권장
- 운영 환경 DB에 DDL 적용은 락 발생 가능성 있으므로 업무시간 외 또는 사용 저조 시간대 실행
- 개발 DB는 즉시 가능

### 4-4. 감사 외 영역 침해 여부 — ✅ 없음
- `tb_contract`·`tb_contract_target`는 감사 대상 목록(§기능 8)에 포함
- 기초 4개 기능(사업관리·담당자관리·사용자관리·서버관리) 테이블 수정 0건 — 순수 DROP만
- **동시 레거시 정리 금지 게이트** (v2 codex 권장 #5): 본 스프린트 실행 중 `tb_inspect_*` / `tb_document_signature` 등 다른 고아 테이블은 **건드리지 않음**. 개발계획서 체크리스트에 명시 예정.

### 4-5. DB팀 자문 결과
- 데이터 0건 + 외부 FK 사전검증(FR-0) + 코드 참조 0건 → **무위험 DROP** (사전검증 통과 조건부)
- 실행 전 최종 COUNT 재확인 SQL 1회 (NFR-4와 동일) 로 안전 확보

### 4-6. `tb_document.contract_id` 컬럼 처리 방향 (v2 신설)

Batch C 스캔에서 `tb_document` 컬럼 목록에 `contract_id integer` 확인됨. 이는 과거 `tb_contract.contract_id` 참조용이었을 가능성. 본 스프린트 범위 판단:

- **FR-0 사전검증 결과에 따라 분기**:
  - **(a) FK 제약이 없고 컬럼이 비어있는 경우**: 본 스프린트 범위 외. 컬럼 DROP은 별도 스프린트 또는 S1 `inspect-comprehensive-redesign`(문서관리 재설계)에서 처리
  - **(b) FK 제약이 있는 경우**: FR-0 사전검증이 실패 → DROP 중단. FK 제약만 제거(`ALTER TABLE ... DROP CONSTRAINT`) 후 DROP 재시도. 컬럼 자체는 본 스프린트 범위 외
- **권장 방향**: 본 스프린트는 **tb_contract/target DROP만** 집중. `tb_document.contract_id` 컬럼 삭제는 S1에 흡수하거나 별도 `tb-document-legacy-column-cleanup` 스프린트

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| 마이그레이션 SQL | `swdept/sql/V###_drop_legacy_contract_tables.sql` | 신규 |
| 레거시 DDL 소스 | `swdept/sql/V100_work_plan_performance_tables.sql:50-93` | 수정 (`tb_contract` 블록 주석 처리, 다른 테이블은 미변경) |
| ERD 문서 | `docs/ERD.md` | 수정 (상태 표기 갱신) |
| ERD 보조 | `docs/erd-diagram.html`, `docs/erd-descriptions.yml`, `docs/erd-document.mmd` | 수정 (잔존 참조 정리) |
| 상위 감사 | `docs/audit/2026-04-18-system-audit.md` | 수정 (완료 기록) |
| 감사 리포트 | `docs/audit/data-architecture-utilization-audit.md` | 수정 (S6 완료 체크) |
| 로드맵 | `docs/plans/data-architecture-roadmap.md` | 수정 (S6 완료 표기) |

**합계**: 신규 1파일, 수정 7파일. **Java 코드 변경 0**. **DB 변경 2 테이블 DROP**. `db_init_phase2.sql` 변경 없음(이미 해당 DDL 없음).

---

## 6. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | 숨은 코드 의존 발견 | **매우 낮음** | 실행 전 NFR-5 grep 재검증 + 서버 기동 스모크 4화면 |
| R-2 | FK 의존으로 DROP 실패 | 낮음 | target 먼저 DROP. `IF EXISTS`로 멱등. 수동 SQL이므로 실패 시 즉시 중단 |
| R-3 | `db_init_phase2.sql` 재실행 시 테이블 재생성 | 중간 | FR-2에서 DDL 블록 제거 필수. NFR-6으로 검증 |
| R-4 | 운영 DB 락 | 낮음 | 테이블 사용 0건이라 DDL 락 거의 없음. 업무시간 외 실행으로 완화 |
| R-5 | 타 레거시 테이블 동시 DROP 충동 | 낮음 | 본 스프린트 **contract 계열 2개로 스코프 고정**. 다른 레거시는 S1 또는 후속 스프린트 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 전 확인 사항
- [x] DROP 대상은 `tb_contract`·`tb_contract_target` 2개로 제한 (감사 §기능 8 확정)
- [x] 사용자 2026-04-20 "tb_contract 불필요" 사전 확정
- [x] 기초 4개 기능 테이블 건드리지 않음
- [x] Java 코드 변경 없음
- [x] 데이터 0건 확인으로 롤백 불필요

### 다음 절차
1. 사용자 "반영" 시 v2 개정
2. 사용자 "최종승인" 시 → **[개발팀]** 개발계획서 작성 (`docs/dev-plans/legacy-contract-tables-drop.md`)
3. 개발계획서 codex 검토 → 사용자 최종승인 → 실제 DROP 실행
