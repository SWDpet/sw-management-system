---
tags: [dev-plan, sprint, drop, legacy, wave-4]
sprint: "pjt-equip-decision"
status: draft
created: "2026-04-22"
---

# [개발계획서] pjt_equip DROP (S15)

- **작성팀**: 개발팀
- **작성일**: 2026-04-22
- **근거 기획서**: [[pjt-equip-decision]] v2.1 (사용자 최종승인 2026-04-22)
- **상태**: v2 (codex v1 ⚠수정필요 2건 반영)
- **v1→v2 변경점**:
  1. V025 Phase 0 의존 검증에 **function body + sequence** 추가 (기획서 v2.2 반영)
  2. Step 4 Java 제거 범위를 **FR-2 전수 확장 규칙** 으로 명시 (Service/DTO/기타 참조 포함)

---

## 0. 전제 / 환경

### 0-1. 확정 사실 (기획 + 실측)
- `pjt_equip` rows = 0, 외부 FK 0
- Java 영향처:
  - Entity: `src/main/java/com/swmanager/system/domain/PjtEquip.java` (DELETE)
  - Repository: `src/main/java/com/swmanager/system/repository/PjtEquipRepository.java` (DELETE)
  - Controller: `SwController.java` — `@Autowired PjtEquipRepository pjtEquipRepository` 주입 + L365~406 `/api/equip/{projId}` GET/POST 2 메서드 (제거)
  - 기타 import: Step 1 Precheck 에서 전수 확인
- Thymeleaf 영향처: `project-form.html`, `doc-interim.html`, `doc-commence.html` 의 장비 영역 (Step 1 Precheck 에서 각 영역 식별)

### 0-2. 롤백 기준 SHA — Step 0 에서 기록

---

## 1. 작업 순서

### Step 0 — 기준 SHA 기록

```bash
BASE_SHA=$(git rev-parse HEAD)
```

### Step 1 — Precheck (리터럴/의존 전수 조사)

```bash
# 1-a. Java 리터럴 전수
rg -n 'PjtEquip|pjtEquip|pjt_equip' src/main/java \
  > docs/exec-plans/s15-precheck-java.txt

# 1-b. Thymeleaf 리터럴 전수
rg -n 'PjtEquip|pjtEquip|pjt_equip|/api/equip|equipType|equipName' src/main/resources/templates \
  > docs/exec-plans/s15-precheck-template.txt

# 1-c. SQL 리터럴
rg -n 'pjt_equip' src/main/resources/*.sql swdept/sql/*.sql \
  > docs/exec-plans/s15-precheck-sql.txt

# 1-d. DB 의존 객체 (psql 상태라면 V025 Phase 0 에서 수행)
```

**Exit Gate 1**: 영향 범위 확정 + 제거/보존 판정표 작성 (`docs/exec-plans/s15-impact-decision.md`)

### Step 2 — V025 마이그레이션 SQL

**파일**: `swdept/sql/V025_drop_pjt_equip.sql`

기획서 §2-1 안 그대로 실행 (Phase 0 3중 게이트 + DROP + 사후검증).

**rollback**: `swdept/sql/V025_rollback.sql` — `pjt_equip` CREATE TABLE 복원 (기획서 §0 컬럼 스펙 기반)

### Step 3 — V025 실행 (Phase 0 게이트 통과 시)

```bash
cp swdept/sql/V025_drop_pjt_equip.sql /tmp/V025_applied.sql
# DB_PASSWORD 자동 로드 + LegacyContractApply 실행
java -cp ".;$JAR" LegacyContractApply /tmp/V025_applied.sql
# 기대: [NOTICE] PASS: S15 V025 — pjt_equip dropped
```

### Step 4 — Java 코드 전수 제거 (기획서 FR-2 범위)

**4-1. Step 1 판정표 기반 전수 제거** — Entity / Repository / Service / Controller / DTO / 기타 import/호출부 **모두 포함**:
- **파일 삭제** (이미 확인):
  - `src/main/java/com/swmanager/system/domain/PjtEquip.java`
  - `src/main/java/com/swmanager/system/repository/PjtEquipRepository.java`
- **SwController.java** 수정: L4 import, L58 주입, L365~406 두 엔드포인트 제거
- **추가 발견 파일** (Step 1 에서 확인): Service/DTO 파일이 존재하면 **삭제**, 다른 클래스 참조가 있으면 **import/호출부 제거**
- **기준**: `rg 'PjtEquip|pjtEquip' src/main/java` 가 0 hits 될 때까지 반복 (NFR-6 게이트와 일치)

### Step 5 — Thymeleaf 수정

Step 1 판정표에 따라:
- `project-form.html` — 장비 입력 테이블/JS 영역 제거
- `doc-interim.html` — 장비 fetch 영역 제거
- `doc-commence.html` — 장비 fetch 영역 제거

### Step 6 — 테스트 작성 (NFR-4 기준 ≥ 4건)

**파일 1**: `src/test/java/com/swmanager/system/arch/PjtEquipGoneArchTest.java`
- `PjtEquip` 및 `PjtEquipRepository` classpath 부재 (Class.forName ClassNotFoundException)

**파일 2**: `src/test/java/com/swmanager/system/sql/PjtEquipSchemaTest.java`
- JdbcTemplate: `pjt_equip` 테이블 미존재
  (`SELECT COUNT(*) FROM information_schema.tables WHERE table_name='pjt_equip'` = 0)

**파일 3**: `src/test/java/com/swmanager/system/mvc/PjtEquipEndpointGoneTest.java`
- `@SpringBootTest` + `@AutoConfigureMockMvc`:
  - `GET /api/equip/{projId}` → `isNotFound()` (404)
  - `POST /api/equip/{projId}` → `isNotFound()` (404)

### Step 7 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q clean compile
./mvnw -q test
bash server-restart.sh
```

**수동 스모크 3건** (NFR-5):
1. `/project/form` — 사업 신규/수정 폼 정상
2. `/document/commence` — 착수계 작성 정상
3. `/document/interim` — 중간보고서 작성 정상

**NFR-6 리터럴 검증**:
```bash
rg 'PjtEquip|pjt_equip|/api/equip' src/main --glob '!**/test/**'
# 기대: 0 hits (docs 등 역사 주석 제외)
```

### Step 8 — 로드맵 정정 (T-LINK)

`docs/design-docs/data-architecture-roadmap.md` §S15 ✅ 완료 표기.

### Step 9 — 사용자 `"작업완료"` 대기 후 커밋/푸시 (CLAUDE.md 준수)

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | Step 1 Precheck | rg + 판정표 | Exit Gate 1 통과 |
| T2 | V025 실행 | SQL | `PASS: S15 V025 — pjt_equip dropped` |
| T3 | 테이블 DROP | information_schema | `pjt_equip` 미존재 |
| T4 | ArchUnit classpath 부재 | PjtEquipGoneArchTest | PjtEquip / PjtEquipRepository 부재 |
| T5 | GET 엔드포인트 404 | PjtEquipEndpointGoneTest | 404 |
| T6 | POST 엔드포인트 404 | 동일 | 404 |
| T7 | 컴파일 | mvnw compile | BUILD SUCCESS |
| T8 | 전체 테스트 | mvnw test | green |
| T9 | 서버 기동 | restart | HTTP 응답 정상 |
| T10 | 수동 스모크 3건 | §Step 7 | 3화면 모두 정상 |
| T11 | 리터럴 잔존 (NFR-6) | rg | 0 hits (main only) |
| T12 | 멱등성 | V025 2회차 | 동일 NOTICE + 영향 0 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Phase 0 FAIL | 자동 ROLLBACK. 추가 의존 정리 후 재시도 |
| Java 제거 후 컴파일 FAIL | `git revert <해당 commit>` |
| 배포 후 회귀 | `V025_rollback.sql` (CREATE TABLE + 인덱스 복원) + git revert |

**롤백 기준 SHA**: __________ (Step 0 기록)

---

## 4. 파일 변경 요약

### 신규
- `swdept/sql/V025_drop_pjt_equip.sql`
- `swdept/sql/V025_rollback.sql`
- `docs/exec-plans/s15-precheck-java.txt` / `-template.txt` / `-sql.txt`
- `docs/exec-plans/s15-impact-decision.md`
- `src/test/java/com/swmanager/system/arch/PjtEquipGoneArchTest.java`
- `src/test/java/com/swmanager/system/sql/PjtEquipSchemaTest.java`
- `src/test/java/com/swmanager/system/mvc/PjtEquipEndpointGoneTest.java`

### 수정
- `src/main/java/com/swmanager/system/controller/SwController.java` (import + 주입 + 엔드포인트 2개 제거)
- `src/main/resources/templates/project-form.html` (장비 영역 제거)
- `src/main/resources/templates/document/doc-interim.html` (장비 fetch 제거)
- `src/main/resources/templates/document/doc-commence.html` (장비 fetch 제거)

### 삭제
- `src/main/java/com/swmanager/system/domain/PjtEquip.java`
- `src/main/java/com/swmanager/system/repository/PjtEquipRepository.java`

---

## 5. 승인 요청

### 승인 확인 사항
- [ ] §0 전제
- [ ] §1 Step 0~9
- [ ] §2 T1~T12
- [ ] §3 롤백 (V025_rollback.sql)
- [ ] §4 파일 변경 범위
