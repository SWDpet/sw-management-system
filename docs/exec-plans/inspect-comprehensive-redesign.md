---
tags: [dev-plan, sprint, inspect, schema, redesign, wave-2]
sprint: "inspect-comprehensive-redesign"
status: draft
created: "2026-04-21"
---

# [개발계획서] 문서관리 종합 재설계 (Wave 2)

- **작성팀**: 개발팀
- **작성일**: 2026-04-21
- **근거 기획서**: [[inspect-comprehensive-redesign]] v2.2 (사용자 최종승인 2026-04-21)
- **상태**: v2 (codex v1 ⚠ 수정필요 6건 반영)
- **v1→v2 변경점**:
  1. Step 1 precheck에 **inspect_report/check_result/visit_log/template 외부 FK 전수 검사** + section NULL/공백/변형 검사 추가
  2. A3 **범위 축소** — `inspect_template.APP` 만 DELETE (기획 정확 준수). `inspect_check_result` 는 Phase 6 TRUNCATE로 자연 해소
  3. Phase 5 FK 사전검증에 `inspect_template` + NULL/공백 케이스 명시 게이트
  4. Step 7 회귀 방지 — ArchUnit + **MVC 레벨 테스트** (deprecated endpoint 미등록 확인) + **Grep 기반 쉘 게이트**
  5. Step 11 스모크 **5건으로 확장** + **UPIS 값 매핑 자동 검증 테스트** 추가
  6. T18 멱등성 **정책 확정**: Phase 1 CREATE TABLE `IF NOT EXISTS` + 시드 `ON CONFLICT DO NOTHING`. 기타 Phase는 WHERE 조건부 UPDATE/DELETE 로 멱등

---

## 0. 전제 / 환경

### 0-1. 기획 결정 6건 반영 (권장안 기준)
- A3 APP 섹션: **완전 제거** (opt1)
- A4 점검자/확인자: **초기화** (B-opt1 — 기존 문자열 데이터 버림 + FK 컬럼 추가)
- A6 `check_section_mst`: **9행 C-opt1** (CORE 5 + DETAIL 4)
- FR-8 테스트 데이터: **TRUNCATE** (D-opt1)
- S7 서명 정책: **E-opt1** (`tb_document_signature` 유지, `tb_inspect_checklist/issue` 2개만 DROP)
- InspectResultCode: **NORMAL / PARTIAL / ABNORMAL / NOT_APPLICABLE**

### 0-2. 실측 고정값
- section 9종: DB / AP / DBMS / GIS / APP / DB_USAGE / AP_USAGE / DBMS_ETC / APP_ETC
- UPIS_SW 잔존 소스: `db_init_phase2.sql`, `doc-inspect.html`, 기타 Thymeleaf 분기 (개발 Step 1 실측 확정)
- DROP 대상 Java 심볼: `InspectChecklist`, `InspectIssue`, `InspectChecklistRepository`, `InspectIssueRepository` + DocumentService의 save*/get* 메서드 4개 + DocumentController의 호출부 (§Step 7에서 실측)

### 0-3. 롤백 기준 SHA
- **Step 0** 에서 `git rev-parse HEAD` 기록 → 아래 §3 에 입력

---

## 1. 작업 순서

### Step 0 — 기준 SHA 기록

```bash
BASE_SHA=$(git rev-parse HEAD) && echo "롤백 기준 SHA: $BASE_SHA"
```
- §3 롤백 섹션 `롤백 기준 SHA:` 필드에 즉시 기록 (S5/S9 패턴 재사용)

---

### Step 1 — 사전검증 (FR-0, NFR-1)

**1-1. 러너**: `docs/exec-plans/inspect-redesign-precheck.java`

Read-only 트랜잭션 + `statement_timeout 10s` + 결과를 `docs/exec-plans/inspect-redesign-precheck-result.md` 에 markdown 테이블로 출력.

쿼리:
```sql
-- (a) section 분포
SELECT section, COUNT(*) FROM inspect_check_result GROUP BY section ORDER BY section;
SELECT section, COUNT(*) FROM inspect_template GROUP BY section ORDER BY section;

-- (b) UPIS_SW 건수
SELECT 'inspect_template', COUNT(*) FROM inspect_template WHERE template_type='UPIS_SW'
UNION ALL SELECT 'inspect_report', COUNT(*) FROM inspect_report WHERE sys_type='UPIS_SW'
UNION ALL SELECT 'tb_document',    COUNT(*) FROM tb_document    WHERE sys_type='UPIS_SW';

-- (c) APP 섹션 건수 (A3 영향)
SELECT COUNT(*) FROM inspect_template WHERE section='APP';
SELECT COUNT(*) FROM inspect_check_result WHERE section='APP';

-- (d) 대상 테이블 row 수 (TRUNCATE 전 스냅샷)
SELECT 'inspect_report',       COUNT(*) FROM inspect_report
UNION ALL SELECT 'inspect_check_result', COUNT(*) FROM inspect_check_result
UNION ALL SELECT 'inspect_visit_log',    COUNT(*) FROM inspect_visit_log
UNION ALL SELECT 'inspect_template',     COUNT(*) FROM inspect_template
UNION ALL SELECT 'tb_inspect_checklist', COUNT(*) FROM tb_inspect_checklist
UNION ALL SELECT 'tb_inspect_issue',     COUNT(*) FROM tb_inspect_issue
UNION ALL SELECT 'tb_document_signature',COUNT(*) FROM tb_document_signature;

-- (e) 외부 FK 의존성 검사 (DROP + TRUNCATE CASCADE 영향 전수 확인)
-- Checklist/Issue DROP 영향
SELECT tc.table_name AS referencing, kcu.column_name, ccu.table_name AS target
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu USING (constraint_name, table_schema)
  JOIN information_schema.constraint_column_usage ccu USING (constraint_name, table_schema)
 WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public'
   AND ccu.table_name IN ('tb_inspect_checklist','tb_inspect_issue')
   AND tc.table_name NOT IN ('tb_inspect_checklist','tb_inspect_issue');

-- TRUNCATE CASCADE 영향 (inspect_report/check_result/visit_log 가 타 테이블에서 참조됨?)
SELECT tc.table_name AS referencing, kcu.column_name, ccu.table_name AS target,
       tc.constraint_name
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu USING (constraint_name, table_schema)
  JOIN information_schema.constraint_column_usage ccu USING (constraint_name, table_schema)
 WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public'
   AND ccu.table_name IN ('inspect_report','inspect_check_result','inspect_visit_log','inspect_template')
   AND tc.table_name NOT IN ('inspect_report','inspect_check_result','inspect_visit_log','inspect_template');

-- section 값 중 NULL / 공백 / 9종 외 변형 (Phase 5 FK ADD 전 필수)
SELECT 'icr', section, COUNT(*) FROM inspect_check_result
  WHERE section IS NULL OR section = '' OR section <> TRIM(section)
     OR section NOT IN ('DB','AP','DBMS','GIS','APP','DB_USAGE','AP_USAGE','DBMS_ETC','APP_ETC')
  GROUP BY section;
SELECT 'it', section, COUNT(*) FROM inspect_template
  WHERE section IS NULL OR section = '' OR section <> TRIM(section)
     OR section NOT IN ('DB','AP','DBMS','GIS','APP','DB_USAGE','AP_USAGE','DBMS_ETC','APP_ETC')
  GROUP BY section;
```

**Exit Gate 1** (기획 §R-1 반영): 결과 검토 후 다음 중 하나면 작업 중단·재계획:
- section 에 **9종 외 신규 값** or **NULL/공백** 발견 → 마스터 시드 확장 or Phase 5 전 정규화 추가
- `tb_inspect_checklist/issue` 가 다른 테이블에서 FK 참조됨 → DROP 전 FK 정리 필요 (옵션 B)
- **inspect_report/check_result/visit_log/template 를 다른 테이블이 FK 참조** → TRUNCATE CASCADE 연쇄 영향 발생 가능 → Phase 6 설계 재검토 (DELETE WHERE 로 대체 등)
- UPIS_SW 건수가 예상(22건) 대비 ±5 초과

### Step 2 — V022 마이그레이션 SQL 작성

**2-1. 파일**: `swdept/sql/V022_inspect_comprehensive_redesign.sql`

하나의 트랜잭션 내 단계별 DO 블록 + 각 블록마다 사전/사후 게이트. 순서:

1. **검증 (Phase 0)**: Step 1 결과와 동일한 기대값 DO 블록 재확인
2. **A6 마스터 생성 (Phase 1)**:
   - `CREATE TABLE check_section_mst` (9행 시드 INSERT)
   - 검증: `SELECT COUNT(*) FROM check_section_mst = 9`
3. **A2 UPIS_SW → UPIS (Phase 2)**:
   - 3개 테이블 UPDATE
   - 검증: `SELECT COUNT(*) FROM ... WHERE sys_type='UPIS_SW' = 0`
4. **A3 APP 섹션 제거 (Phase 3) — v2 범위 축소**:
   - `DELETE FROM inspect_template WHERE section='APP'` (기획 §3-A opt1 준수)
   - **inspect_check_result.APP 는 별도 DELETE 불필요** — Phase 6 TRUNCATE 로 전체 초기화되어 자연 해소 (기획 범위 준수)
5. **A5 category 정규화 (Phase 4)**:
   - `UPDATE inspect_check_result SET category = TRIM(category)`
   - `(GWS)` 특수 공백 정규화 (Step 1에서 실측한 case별 UPDATE)
6. **A6 FK ADD (Phase 5) — Exit Gate 2 (v2 게이트 강화)**:
   - **사전 검증 A** (inspect_check_result 기준):
     ```sql
     IF EXISTS(SELECT 1 FROM inspect_check_result
                 WHERE section IS NULL OR section = '' OR section <> TRIM(section)
                    OR section NOT IN (SELECT section_code FROM check_section_mst))
       THEN RAISE EXCEPTION 'HALT: icr.section 불일치 — ROLLBACK';
     END IF;
     ```
   - **사전 검증 B** (inspect_template 기준 — 동일 패턴)
   - FK ADD: `ALTER TABLE inspect_check_result ADD CONSTRAINT fk_icr_section FOREIGN KEY (section) REFERENCES check_section_mst(section_code);`
   - 동일: `inspect_template`
7. **FR-8 TRUNCATE (Phase 6, 사용자 승인됨) — v2 CASCADE 회피**:
   - **Exit Gate 1 에서 외부 FK가 0건으로 확인된 경우에만 CASCADE 생략 가능**
   - 백업 스냅샷 (Phase 0 직후 실행):
     ```sql
     CREATE TABLE inspect_report_backup_:run_id       AS SELECT * FROM inspect_report;
     CREATE TABLE inspect_check_result_backup_:run_id AS SELECT * FROM inspect_check_result;
     CREATE TABLE inspect_visit_log_backup_:run_id    AS SELECT * FROM inspect_visit_log;
     CREATE TABLE inspect_template_backup_:run_id     AS SELECT * FROM inspect_template;
     ```
   - 실제 TRUNCATE (FK 역순 + CASCADE 미사용 — 예상치 못한 연쇄 방지):
     ```sql
     TRUNCATE inspect_check_result RESTART IDENTITY;
     TRUNCATE inspect_visit_log    RESTART IDENTITY;
     TRUNCATE inspect_report       RESTART IDENTITY;
     TRUNCATE inspect_template     RESTART IDENTITY;
     ```
   - **외부 FK 존재 시** (Exit Gate 1에서 발견되면): Phase 6 전체를 WHERE-based DELETE 로 재설계. 본 v2는 외부 FK = 0 전제 서술
8. **A1 result 분리 (Phase 7)**:
   - `ALTER TABLE inspect_check_result ADD COLUMN result_code VARCHAR(20), ADD COLUMN result_text VARCHAR(500);`
   - `ALTER TABLE inspect_check_result DROP COLUMN result;` (TRUNCATE 이후라 안전)
9. **A4 FK 전환 (Phase 8)**:
   - `ALTER TABLE inspect_report ADD COLUMN insp_user_id BIGINT REFERENCES users(user_id);`
   - `ALTER TABLE inspect_report ADD COLUMN conf_ps_info_id BIGINT REFERENCES ps_info(id);`
   - `ALTER TABLE inspect_report DROP COLUMN insp_name, DROP COLUMN insp_company, DROP COLUMN conf_name, DROP COLUMN conf_org;`
10. **FR-7 DROP (Phase 9, Exit Gate 3)**:
    - `DROP TABLE IF EXISTS tb_inspect_checklist;`
    - `DROP TABLE IF EXISTS tb_inspect_issue;`
    - 사후: `SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('tb_inspect_checklist','tb_inspect_issue')` = 0
11. **최종 검증 (Phase 10)**:
    - `check_section_mst` = 9
    - inspect_report 신규 컬럼 2개 존재, 레거시 4컬럼 없음
    - inspect_check_result 신규 컬럼 2개 존재, result 없음
    - `RAISE NOTICE 'PASS: inspect-comprehensive-redesign V022 applied'`

**실행ID 기반 백업 테이블** — S5 패턴: `:run_id = date +%Y%m%d_%H%M%S`

**v2 멱등성 정책 확정 (T18)**:
- Phase 1 `CREATE TABLE IF NOT EXISTS check_section_mst (...)`
- 9행 시드는 `INSERT ... ON CONFLICT (section_code) DO NOTHING`
- Phase 2/3/4/5 UPDATE/DELETE 는 WHERE 조건부라 재실행 시 0건 영향 (자연 멱등)
- Phase 7/8 `ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...` + `DROP COLUMN IF EXISTS ...`
- Phase 9 `DROP TABLE IF EXISTS ...`
- Phase 6 TRUNCATE는 재실행 시 이미 비어있어 무영향
- 결과: V022 전체가 **재실행 안전** (2회차 실행 시 `NOTICE PASS` 출력 동일)

### Step 3 — InspectResultCode Enum 신설

**파일**: `src/main/java/com/swmanager/system/constant/enums/InspectResultCode.java`

```java
public enum InspectResultCode {
    NORMAL("정상"),
    PARTIAL("부분정상"),
    ABNORMAL("비정상"),
    NOT_APPLICABLE("해당없음");

    private final String label;
    AccessActionType 패턴과 동일: @JsonValue getLabel + @JsonCreator fromJson(trim+label/name)
}
```

### Step 4 — InspectCheckResult 엔티티 수정

**파일**: `src/main/java/com/swmanager/system/domain/InspectCheckResult.java`

- `result` 필드 DELETE
- `resultCode` String (Enum name 저장) + `resultText` String 추가
- 향후 편의 메서드: `public InspectResultCode getResult() { return InspectResultCode.valueOf(resultCode); }`

### Step 5 — InspectReport 엔티티 수정 (A4)

**파일**: `src/main/java/com/swmanager/system/domain/InspectReport.java`

- DELETE: `inspName`, `inspCompany`, `confName`, `confOrg` 4 필드
- ADD:
  - `private Long inspUserId;` (+ `@JoinColumn`/`@ManyToOne` 선택적 User)
  - `private Long confPsInfoId;` (+ 선택적 PsInfo)
- DTO 수정: `InspectReportDTO.java` 4 필드 제거, 2 필드 추가. `fromEntity`/`toEntity` 재매핑

### Step 6 — Thymeleaf 업데이트 (A2, A4)

**대상 파일** (Step 1 grep 결과 기반 확정):
- `src/main/resources/templates/inspect-detail.html`
- `src/main/resources/templates/inspect-preview.html`
- `src/main/resources/templates/document/doc-inspect.html`

**변경**:
- `th:if="${report.sysType == 'UPIS_SW'}"` → 제거 (또는 `'UPIS'` 로 통합)
- `th:text="${report.sysType}"` 에서 `UPIS_SW` 하드코드 분기 제거
- 점검자/확인자 표시: `${report.inspName}` → `${inspectorUser?.username}` 형태로 Controller model attribute 참조 전환 (Controller Step에서 조인 공급)

### Step 7 — InspectChecklist/Issue 관련 Java 코드 제거 (FR-7)

**삭제 파일** (2개):
- `src/main/java/com/swmanager/system/domain/workplan/InspectChecklist.java`
- `src/main/java/com/swmanager/system/domain/workplan/InspectIssue.java`

**삭제 Repository** (2개 — Grep으로 실측):
- `InspectChecklistRepository.java`
- `InspectIssueRepository.java`

**DocumentService.java 변경**:
- `@Autowired InspectChecklistRepository` / `InspectIssueRepository` 주입 DELETE
- 메서드 DELETE: `saveChecklist`, `getChecklists`, `saveIssue`, `getIssues` (L220~254)
- `DocumentSignature` 관련은 **유지** (E-opt1)

**DocumentController.java 변경**:
- checklist/issue API 엔드포인트 있으면 DELETE (Step 1에서 grep 재확인)
- `/api/signature/save` 는 **유지**

**Document.java 변경**:
- `@OneToMany InspectChecklist`, `@OneToMany InspectIssue` 필드 DELETE (L102~108 근처)
- `@OneToMany DocumentSignature` 는 **유지**

### Step 8 — db_init_phase2.sql UPIS_SW 제거

**파일**: `src/main/resources/db_init_phase2.sql`

Step 1 grep 결과로 확인한 UPIS_SW 리터럴 모두 `UPIS` 로 치환 또는 INSERT 자체 제거 (정책 판단 후).

### Step 9 — Controller 조인 공급 (A4 지원)

**파일**: `src/main/java/com/swmanager/system/controller/DocumentController.java` (inspect-detail/preview 렌더링 메서드)

```java
if (report.getInspUserId() != null) {
    userRepository.findById(report.getInspUserId()).ifPresent(u -> model.addAttribute("inspectorUser", u));
}
if (report.getConfPsInfoId() != null) {
    personInfoRepository.findById(report.getConfPsInfoId()).ifPresent(p -> model.addAttribute("confirmerPs", p));
}
```

### Step 10 — 테스트 작성

**파일 1**: `src/test/java/com/swmanager/system/constant/enums/InspectResultCodeTest.java`
- enum 4개, label Freeze, fromKoLabel/fromJson trim, unknown null (S9 AccessActionType 테스트 패턴 복제)

**파일 2**: `src/test/java/com/swmanager/system/sql/CheckSectionMstSeedTest.java` (선택, 통합 테스트)
- V022 적용 후 `check_section_mst` row = 9, section_group 분포 (CORE 5 + DETAIL 4)

**파일 3**: `src/test/java/com/swmanager/system/arch/InspectLegacyDropArchTest.java`
- ArchUnit: `InspectChecklist`, `InspectIssue` 클래스가 classpath에 없음 (deletion 회귀 방지)

**파일 4 (v2 신규)**: `src/test/java/com/swmanager/system/mvc/InspectLegacyEndpointGoneTest.java`
- `@SpringBootTest` + `@AutoConfigureMockMvc`:
  - Step 1 에서 실측된 기존 checklist/issue API 엔드포인트(있으면)에 대해 `perform(get/post).andExpect(status().isNotFound())` 검증
  - 엔드포인트가 원래 없었다면 이 테스트는 SKIP 명시

**파일 5 (v2 신규)**: `src/test/java/com/swmanager/system/service/DocumentServiceLegacyMethodsGoneTest.java`
- 리플렉션으로 `DocumentService.class.getDeclaredMethods()` 에서 `saveChecklist`, `getChecklists`, `saveIssue`, `getIssues` 4개 메서드가 **존재하지 않음** 을 확인

**파일 6 (v2 신규)**: `src/test/java/com/swmanager/system/mvc/InspectSysTypeMappingTest.java`
- UPIS_SW 가 아닌 UPIS 값으로 저장되었을 때 Thymeleaf 분기가 정상 렌더링되는지 MVC 레벨 스모크 (GET 응답 HTML 에서 예상 섹션 표시 문자열 assertion)

### Step 11 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q clean compile
./mvnw -q test
bash server-restart.sh
```

**수동 스모크 5건** (v2 확장, NFR-2, NFR-6):
1. `/document/inspect-detail/{id}` — 기존 inspect_report 1건 (TRUNCATE 후 신규 생성)
2. `/document/doc-inspect` — 새 항목 저장 → section = DB_USAGE 로 저장됨 확인
3. `/document/inspect-preview/{id}` — PDF 프리뷰 렌더링 OK
4. (v2) `sys_type='UPIS'` 로 저장된 신규 inspect_report 의 inspect-detail 렌더링이 UPIS 분기로 정상 표시됨 (기존 UPIS_SW 분기 케이스가 누락 없이 UPIS 로 이행 확인)
5. (v2) `sys_type='KRAS'` / `IPSS` / `ETC` 등 **UPIS 외 시스템 값** 의 분기 1건 회귀 확인

**NFR-4 검증**:
```bash
rg -n 'UPIS_SW' src/main/java src/main/resources | grep -v '\.md'
# 기대: 0 hits (docs/*.md 의 역사 기록은 허용)
```

### Step 12 — 로드맵 정정 (T-LINK)

`docs/design-docs/data-architecture-roadmap.md` §S1 완료 표기 + `docs/generated/audit/data-architecture-utilization-audit.md` S1/S7/S17 ✅ 완료

### Step 13 — 커밋 / 푸시

Step별 atomic commit 권장 (V022 migration / Enum / Entity / Thymeleaf / Step 7 drop / Tests / Roadmap). 최소한 migration + 코드 반영은 **분리 commit**.

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | 사전검증 (FR-0) | Step 1 precheck runner | 결과 md 생성 + Exit Gate 1 통과 |
| T2 | V022 실행 성공 | SQL 출력 | `[NOTICE] PASS: inspect-comprehensive-redesign V022 applied` |
| T3 | check_section_mst 9행 | SQL `COUNT(*)` | = 9 |
| T4 | section_group 분포 | SQL `COUNT(*) GROUP BY section_group` | CORE=5, DETAIL=4 |
| T5 | UPIS_SW 잔존 0 (DB) | 3 테이블 쿼리 | 모두 0 |
| T6 | APP 섹션 0 | `inspect_template.section='APP'` | 0 |
| T7 | FK 제약 존재 | `pg_constraint` 조회 (fk_icr_section, fk_it_section) | ≥ 2 |
| T8 | inspect_report 신규 컬럼 | `information_schema.columns` | insp_user_id + conf_ps_info_id 존재, insp_name/company/conf_name/org 없음 |
| T9 | inspect_check_result 신규 컬럼 | 동일 | result_code + result_text 존재, result 없음 |
| T10 | DROP 완료 | `information_schema.tables` | tb_inspect_checklist/issue = 0, tb_document_signature **존재** (E-opt1 검증) |
| T11 | InspectResultCode Enum 4개 | JUnit | `values().length = 4` + label Freeze |
| T12 | UPIS_SW 리터럴 잔존 | `rg 'UPIS_SW' src/main` | 0 hits |
| T13 | ArchUnit drop 검증 | `InspectLegacyDropArchTest` | PASS |
| T14 | 컴파일 | `./mvnw clean compile` | BUILD SUCCESS |
| T15 | 전체 테스트 | `./mvnw test` | 전부 green |
| T16 | 서버 기동 | `server-restart.sh` | `Started` + ERROR 0 |
| T17 | 수동 스모크 5건 (v2) | §Step 11 | 5건 모두 정상 렌더링 |
| T18 | 멱등성 (v2 확정) | V022 2회차 실행 | Phase 1 `CREATE TABLE IF NOT EXISTS` + 시드 `ON CONFLICT DO NOTHING` + Phase 7/8 `ADD/DROP COLUMN IF [NOT] EXISTS` + Phase 9 `DROP ... IF EXISTS` → `NOTICE PASS` 동일 출력 |
| T19 (v2) | MVC 엔드포인트 회귀 | `InspectLegacyEndpointGoneTest` | PASS (deprecated URL → 404) |
| T20 (v2) | Service 메서드 제거 | `DocumentServiceLegacyMethodsGoneTest` | 리플렉션으로 4개 메서드 부재 확인 |
| T21 (v2) | SysType 분기 매핑 | `InspectSysTypeMappingTest` | UPIS/KRAS/IPSS 분기 렌더링 HTML assertion PASS |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 1 FR-0 FAIL | V022 미실행, 재계획 |
| V022 Phase 2~5 EXCEPTION | 자동 ROLLBACK. 백업 테이블 `inspect_*_backup_<run_id>` 존재 |
| V022 Phase 6 TRUNCATE 이후 문제 | 백업 테이블에서 복원 (전체 데이터), Phase 0 시점 상태로 되돌림 |
| Phase 9 DROP 후 기능 회귀 | **Exit Gate 3 통과한 후라 DROP 은 이미 완료** → Java 코드 revert + migration 후속 스프린트에서 CREATE TABLE 재시도 (복구는 기획 전제 외) |
| 코드 수정 후 컴파일 FAIL | `git revert` (해당 파일 commit) |
| 전체 롤백 (스프린트 폐기) | `git revert <BASE_SHA>..HEAD` + V022 역마이그(별도 V022_rollback.sql 준비) |

**롤백 기준 SHA**: `7dfe2d7ca6bfc4833e8de29be1de505ec890e714` (2026-04-21 Step 0 기록)

**V022 실행 로그** (Step 11):
- RUN_ID: `20260421_235813`
- 백업 테이블: `inspect_report_backup_20260421_235813`, `inspect_check_result_backup_20260421_235813`, `inspect_visit_log_backup_20260421_235813`, `inspect_template_backup_20260421_235813`
- Phase 0~10 모두 PASS
- Exit Gate 1/2/3 모두 통과

**V022 rollback SQL (보관용)**:
- 별도 파일 `swdept/sql/V022_rollback.sql` 작성. Phase 역순 (DROP fk → ADD 레거시 컬럼 → backup 테이블에서 데이터 복원)
- 본 스프린트는 테스트 단계 가정이므로 롤백 SQL 필수는 아니나 **보관 권장**

---

## 4. 리스크 / 완화 재확인

| ID | 리스크 | 본 개발계획 적용 |
|----|--------|-----------------|
| R-1 | 범위 팽창 | Step 1~2 Exit Gate 1~3 + De-scope 기준 (Step 2 Phase 8까지만 완료되어도 FR-7은 후속 분리 가능) |
| R-2 | InspectChecklist/Issue 숨은 참조 | Step 7 + T13 ArchUnit 게이트 |
| R-3 | Thymeleaf UPIS_SW 분기 제거 | Step 6 + T12 + T17 수동 스모크 |
| R-4 | result 분리 데이터 손실 | FR-8 TRUNCATE 전제 + Phase 0 백업 |
| R-5 | FK 제약으로 저장 실패 | Phase 5 Exit Gate 2 (DISTINCT section 검사 후 FK ADD) |
| R-6 | 운영 영향 | 테스트 단계 확정 (리스크 0) |

---

## 5. 파일 변경 요약 (개발 착수 시점 예상)

### 신규
- `swdept/sql/V022_inspect_comprehensive_redesign.sql`
- `swdept/sql/V022_rollback.sql` (보관)
- `docs/exec-plans/inspect-redesign-precheck.java`, `-precheck-result.md`
- `src/main/java/com/swmanager/system/constant/enums/InspectResultCode.java`
- `src/test/java/com/swmanager/system/constant/enums/InspectResultCodeTest.java`
- `src/test/java/com/swmanager/system/arch/InspectLegacyDropArchTest.java`
- (선택) `src/test/java/com/swmanager/system/sql/CheckSectionMstSeedTest.java`

### 수정
- `src/main/java/com/swmanager/system/domain/InspectReport.java`
- `src/main/java/com/swmanager/system/domain/InspectCheckResult.java`
- `src/main/java/com/swmanager/system/dto/InspectReportDTO.java`
- `src/main/java/com/swmanager/system/service/InspectReportService.java` (필요시)
- `src/main/java/com/swmanager/system/service/InspectPdfService.java` (section 분기 검토)
- `src/main/java/com/swmanager/system/controller/DocumentController.java`
- `src/main/java/com/swmanager/system/service/DocumentService.java`
- `src/main/java/com/swmanager/system/domain/workplan/Document.java`
- `src/main/resources/templates/inspect-detail.html`
- `src/main/resources/templates/inspect-preview.html`
- `src/main/resources/templates/document/doc-inspect.html`
- `src/main/resources/db_init_phase2.sql`

### 삭제
- `src/main/java/com/swmanager/system/domain/workplan/InspectChecklist.java`
- `src/main/java/com/swmanager/system/domain/workplan/InspectIssue.java`
- `src/main/java/com/swmanager/system/repository/workplan/InspectChecklistRepository.java` (경로 grep 후 확정)
- `src/main/java/com/swmanager/system/repository/workplan/InspectIssueRepository.java`

---

## 6. 승인 요청

### 승인 확인 사항
- [ ] §0-1 기획 결정 6건 반영 확인
- [ ] §1 Step 1 precheck + Exit Gate 1
- [ ] §1 Step 2 V022 11 Phase + 2/3 Exit Gate
- [ ] §1 Step 3~9 Java/Thymeleaf 수정 범위
- [ ] §2 T1~T21 체크리스트 (v2 확장: T19 MVC 404 / T20 리플렉션 메서드 부재 / T21 SysType 매핑)
- [ ] §3 롤백 절차 (TRUNCATE 이후 백업 복원 경로)
- [ ] §4 R-1~R-6 완화
