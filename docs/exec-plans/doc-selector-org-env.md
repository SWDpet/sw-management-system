---
tags: [dev-plan, sprint]
sprint: "5"
status: v2 (사용자 피드백 반영)
created: "2026-04-19"
---

# [개발계획서] 문서 선택 UI 통일 + 조직도 + 운영/테스트 구분 — 스프린트 5

- **작성팀**: 개발팀
- **작성일**: 2026-04-19
- **근거 기획서**: [[../plans/doc-selector-org-env|기획서 v2]] (승인)
- **참고**: [[../plans/doc-selector-org-env.seed|조직도 seed]]
- **상태**: v2 — 구현 완료 후 사용자 피드백 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 5단 드롭다운(연도→시도→시군구→시스템→사업) 가정, 저장 키 `proj_id` |
| v2 | 4개 문서는 **3단(시도→시군구→시스템)** 간소화. 저장 키 `infra_id` 로 재정의. 지원 대상 라디오 가로 배치. 조직도 관리 메뉴 링크 추가. 신규 JS 모듈 `document-infra-selector.js` 도입 |

---

## 1. 작업 순서 (Stepwise)

### Step 1 — DB 스키마 보강 (tb_document 컬럼 + tb_org_unit 신규)
1-1. `src/main/resources/db_init_phase2.sql` 최하단에 추가:
   - `ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS support_target_type VARCHAR(20)` (EXTERNAL/INTERNAL, nullable)
   - `ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS org_unit_id BIGINT`
   - `ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS environment VARCHAR(20)` (PROD/TEST, nullable)
   - `ALTER TABLE tb_document ADD CONSTRAINT fk_tb_document_org_unit FOREIGN KEY (org_unit_id) REFERENCES tb_org_unit(unit_id)` — **tb_org_unit 생성 후** 적용
   - `CREATE INDEX IF NOT EXISTS idx_tb_document_org_unit ON tb_document(org_unit_id)`
   - `CREATE INDEX IF NOT EXISTS idx_tb_document_environment ON tb_document(environment)`
   - `CREATE INDEX IF NOT EXISTS idx_tb_document_support_type ON tb_document(support_target_type)`

1-2. `tb_org_unit` CREATE TABLE + seed (기획서 부속 문서 `doc-selector-org-env.seed.md` 의 DDL·DO 블록 그대로 반영).

1-3. 검증: 서버 재기동 후 `DB 초기화 완료: N개 SQL 실행` 로그에서 숫자 증가 + psql 로 `SELECT COUNT(*) FROM tb_org_unit` = 40 내외.

### Step 2 — Entity / Repository / Service / Controller (조직도)

2-1. `src/main/java/com/swmanager/system/domain/OrgUnit.java`
```java
@Entity
@Table(name = "tb_org_unit")
@Getter @Setter
public class OrgUnit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Long unitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private OrgUnit parent;

    @Column(name = "unit_type", nullable = false, length = 20)
    private String unitType; // DIVISION / DEPARTMENT / TEAM

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "use_yn", length = 1)
    private String useYn = "Y";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist void pp() { createdAt = updatedAt = LocalDateTime.now(); if (useYn==null) useYn="Y"; }
    @PreUpdate  void pu() { updatedAt = LocalDateTime.now(); }
}
```

2-2. `OrgUnitRepository extends JpaRepository<OrgUnit, Long>`:
- `List<OrgUnit> findByParentIsNullAndUseYnOrderBySortOrderAsc(String useYn)` (최상위)
- `List<OrgUnit> findByParent_UnitIdAndUseYnOrderBySortOrderAsc(Long parentId, String useYn)`
- `boolean existsByParent_UnitIdAndUseYn(Long parentId, String useYn)` (삭제 시 하위 존재 체크)

2-3. `OrgUnitService`:
- `getTree()`: 전체 DFS → DTO 트리 반환
- `getChildren(parentId)`: 부분 로드
- `create(parentId, type, name, sortOrder)`: 유효성(parent type > child type 금지 X — 가변 계층 허용) 후 저장
- `update(unitId, name, sortOrder, useYn)`
- `delete(unitId)`: 하위 존재하면 IllegalStateException

2-4. `OrgUnitController`:
- `@GetMapping("/api/org-units/tree")` → 전체 트리 JSON
- `@GetMapping("/api/org-units/children/{parentId}")` → 하위 목록
- `@GetMapping("/api/org-units/roots")` → 최상위 목록
- `@GetMapping("/admin/org-units")` → 관리 화면 HTML (ADMIN 만)
- `@PostMapping("/admin/api/org-units")` → 생성 (ADMIN)
- `@PutMapping("/admin/api/org-units/{id}")` → 수정 (ADMIN)
- `@DeleteMapping("/admin/api/org-units/{id}")` → 삭제 (ADMIN)

2-5. `SecurityConfig`: 기존 `/admin/**` ADMIN 규칙으로 자동 보호. `/api/org-units/**`(GET) 는 `.authenticated()` 로 추가.

### Step 3 — 공통 JS 모듈 추출

3-1. `src/main/resources/static/js/document-project-selector.js` 신규 — 기존 `doc-commence.html` 의 `onYearChange/onCityChange/onDistChange/onSystemChange/loadProjectInfo` 로직 추출. 초기화 API:
```js
DocumentProjectSelector.init({
    yearEl: '#selYear', cityEl: '#selCity', distEl: '#selDist',
    systemEl: '#selSystem', projectEl: '#projId',
    onProjectChange: (projId, projDto) => { /* 선택 시 콜백 */ },
    initialProjId: null, // 기존 레코드 편집 시 배너 표시용 (FR-1-F)
});
```

3-2. `src/main/resources/static/js/org-unit-selector.js` 신규:
```js
OrgUnitSelector.init({
    divisionEl: '#selDivision', departmentEl: '#selDepartment', teamEl: '#selTeam',
    onUnitChange: (finalUnitId, path) => { /* ['경영관리본부', '인사총무부'] 같은 path */ },
    initialUnitId: null,
});
```
가변 계층 처리: parent 선택 시 하위 로드, 하위 없으면 다음 select disabled.

3-3. ~~`doc-commence.html`, `doc-inspect.html` 의 기존 인라인 로직을 새 모듈 사용으로 **교체**~~ → **본 스프린트에서 분리** (FR-1-C 수정 반영). 공통 모듈 파일은 배포되어 있으므로 후속 리팩터 스프린트에서 교체 가능. 범위 축소 사유: 기존 2문서의 인라인 JS 가 복잡·부가 기능 많아 회귀 리스크가 이번 UI 통일 목표(4문서)보다 큼.

### Step 4 — 4개 문서 템플릿 수정

각각에 다음 공통 블록 적용:
- 기존 `<select id="infraId">` 제거
- 연쇄 드롭다운 HTML 블록 추가 (doc-commence.html 105-127 참조)
- `DocumentProjectSelector.init(...)` 호출 스크립트 추가
- 기존 레코드 편집 진입 시 **배너** 렌더링 (FR-1-F):
  ```html
  <div id="legacyTargetBanner" style="display:none; padding:8px; background:#fff3cd; border:1px solid #ffc107;">
      현재 대상: <span id="legacyTargetText">…</span> — 저장 시 재선택 필요
  </div>
  ```
- 재선택 전까지 저장 버튼 disabled 처리

4-1. `doc-fault.html` (장애처리) — 최소 변경
4-2. `doc-install.html` (설치보고서) — 최소 변경 + 운영/테스트 select 추가:
```html
<label>환경 구분 *</label>
<select id="environment" required>
    <option value="">선택</option>
    <option value="PROD">운영</option>
    <option value="TEST">테스트</option>
</select>
```
4-3. `doc-patch.html` (패치내역서) — 최소 변경 + 운영/테스트 select 추가 (동일)
4-4. `doc-support.html` (업무지원) — 내부/외부 라디오 + 조건부 표시:
```html
<label>
    <input type="radio" name="supportTargetType" value="EXTERNAL" checked> 외부(지자체)
    <input type="radio" name="supportTargetType" value="INTERNAL"> 내부(자사 조직)
</label>
<div id="externalBlock">
    <!-- 연쇄 드롭다운 (기존 4개 문서 공통) -->
</div>
<div id="internalBlock" style="display:none;">
    <!-- 조직 드롭다운 (본부 → 부서 → 팀) -->
</div>
```
JS: radio 변경 시 표시 토글 + 선택 유닛 id 저장. 저장 payload 에 `supportTargetType`, `orgUnitId` 포함.

### Step 5 — DocumentController 저장·조회 로직 수정

5-1. `saveFault`, `saveSupport`, `saveInstall`, `savePatch` (또는 공통 저장 엔드포인트) 메서드에서:
- 요청 body 에서 `projId` (외부) 또는 `orgUnitId` (내부 업무지원) 받기
- 업무지원: `supportTargetType` 검증 + 상반 필드 null 처리
- 설치/패치: `environment` 필드 필수 검증 (`PROD` / `TEST` 만 허용), 아니면 400
- 기존 편집 시 `projId` 누락 상태에서 저장 시도 → 400 + `code=RESELECT_REQUIRED`

5-2. `getFaultDetail` 등 조회 메서드에서 기존 레코드의 `infra_id` 기반 표시 텍스트 (`"양양군 UPIS 2025"`) 추출해서 응답 payload 에 `legacyTargetText` 필드 추가 (배너용).

5-3. **목록/조회 화면 표시 로직 (FR-2-F, FR-4-C — codex 지적 반영)**:
- 업무지원 목록 (`document-list.html` 또는 해당 컨트롤러 Model) — `supportTargetType` 에 따라 대상 표시 분기:
  - `EXTERNAL` → "[시·도 시·군·구] 시스템명 (연도)" (기존 지자체 표시)
  - `INTERNAL` → "[본부명 > 부서명 > 팀명]" (org_unit 경로). `OrgUnitService.getPath(unitId)` 헬퍼로 루트→말단 이름 배열 구성
- 설치보고서/패치내역서 목록 화면에 **환경 뱃지** 추가:
  - `PROD` → 녹색 "운영"
  - `TEST` → 주황 "테스트"
  - CSS 클래스 기반 (`.env-badge-prod`, `.env-badge-test`) 스타일링
- 상세 조회 시도 동일 분기 표시
- 서버측 DTO 에 `displayTargetText`, `environmentLabel` 필드 추가해 프런트 부담 경감

### Step 6 — 관리 화면 (조직도 CRUD)

6-1. `src/main/resources/templates/admin/org-unit-management.html` 신규:
- 좌측: 트리뷰 (재귀 렌더링)
- 우측: 상세·편집 폼
- 추가 버튼: 상위 선택 시 "하위 유닛 추가" (type 은 상위 기준 자동 제안되지만 수동 선택 허용)
- 삭제 버튼: confirm + 하위 존재 시 API 에서 400 → alert
- 이름 inline 편집 + 정렬 변경 (드래그 or sort_order 숫자 입력)

6-2. 관리자 메뉴에 "조직도 관리" 링크 추가 (관리자 사이드바 또는 `/admin/` 홈).

### Step 7 — ERD / 문서 갱신

7-1. `docs/generated/erd.md`, `docs/erd-core.mmd` 또는 적절한 도메인 파일에 `tb_org_unit` 블록 + `tb_document.org_unit_id` FK 표시 추가.
7-2. `docs/product-specs/doc-selector-org-env.md` 의 "개발계획서 단계 재확인" 섹션 값 확정 표기.
7-3. Obsidian 대시보드(`docs/generated/audit/dashboard.md`) 에 본 스프린트 완료 한 줄 기록.

### Step 8 — 빌드 / 재기동 / 검증

8-1. `./mvnw -q compile` → BUILD SUCCESS.
8-2. `bash server-restart.sh` → 기동 성공 + `DB 초기화 완료: N개 SQL 실행` 증가 확인.
8-3. psql 검증: `SELECT COUNT(*) FROM tb_org_unit;` → 40 내외. `\d tb_document` → 3 컬럼 추가 확인.
8-4. UI 스모크 (수동 또는 브라우저):
- `/projects` 정상
- `/document/fault` 작성 페이지 — 연쇄 드롭다운 동작
- `/document/install` — 환경 select 필수
- `/document/patch` — 환경 select 필수
- `/document/support` — 내부/외부 토글, 각각 드롭다운 로드
- `/admin/org-units` — 트리 표시, 추가·수정·삭제 동작

### Step 9 — codex Specs 검증 → 커밋·푸시

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | `tb_org_unit` 테이블·인덱스·seed 생성 | psql `\dt tb_org_unit` + `SELECT COUNT(*) FROM tb_org_unit` | 테이블 존재 + 40 내외 |
| T2 | `tb_document` 3 컬럼 추가 | psql `\d tb_document` | support_target_type/org_unit_id/environment 존재 |
| T3 | 4개 문서 템플릿에 `infraId` 단일 select 제거 | `rg -n "id=\"infraId\"" src/main/resources/templates/document/{doc-fault,doc-support,doc-install,doc-patch}.html` | 0 hits |
| T4 | 3단 드롭다운 4문서 적용 (v2) | 동일 4파일에 `id="selCity"` + `id="selDist"` + `id="selSystem"` 존재하고 `id="selYear"` 는 없음 | selCity 4, selDist 4, selSystem 4, selYear 0 |
| T5 | 공통 JS 모듈 존재 | `test -f src/main/resources/static/js/document-infra-selector.js && test -f src/main/resources/static/js/document-project-selector.js && test -f src/main/resources/static/js/org-unit-selector.js` | exit 0 |
| T6 | 공통 JS 모듈 배포 확인 (commence/inspect 마이그레이션은 본 스프린트 범위 외) | `test -f src/main/resources/static/js/document-project-selector.js` | exit 0 |
| T7 | 업무지원 라디오 + 조직 드롭다운 | `rg -n 'name="supportTargetType"' src/main/resources/templates/document/doc-support.html` + `rg -n "OrgUnitSelector.init" src/main/resources/templates/document/doc-support.html` | 각 ≥ 1 |
| T8 | 설치·패치 환경 select | `rg -n 'id="environment"' src/main/resources/templates/document/{doc-install,doc-patch}.html` | 각 1 hit |
| T9 | OrgUnit Entity / Repository / Service / Controller 파일 존재 | Glob | 4 파일 존재 |
| T10 | Maven compile | `./mvnw -q compile` | BUILD SUCCESS |
| T11 | 서버 재기동 + 시작 로그 | `bash server-restart.sh` | `Started SwmanagerApplication` + ERROR 0 |
| T12 | 조직 트리 API 응답 (NFR-5 응답 스키마 제한 — codex 지적 반영) | `curl -s --cookie "SWMANAGER_SESSION=..." http://localhost:9090/api/org-units/tree \| jq 'flatten \| .[0] \| keys'` | 결과 키셋이 **정확히** `["unit_id","name","unit_type","parent_id","children"]` (추가로 `sort_order` 허용). **금지 키**: `use_yn`, `created_at`, `updated_at`, `createdAt`, `updatedAt`. 최상위 배열 길이 10. |
| T13 | 관리 화면 접근 제어 | 비-ADMIN 계정으로 `/admin/org-units` 접근 | 302 리다이렉트 또는 403 |
| T14 | 기존 레코드 편집 저장 차단 (FR-1-F 백엔드) | 기존 infraId/projId 없이 저장 시도 | HTTP 400 `code=RESELECT_REQUIRED` with message "지자체/시스템을 다시 선택하세요." |
| T14-UX | FR-1-F 프런트 UX 검증 (v2 개정) | 레거시 레코드(draft **및** 완료) 각각 편집 페이지 진입 후 DOM 검사 | (a) `#legacyTargetBanner` 가시 + 대상 텍스트 채움, (b) 저장 버튼 `disabled=true`, (c) 드롭다운(`#selCity/selDist/selSystem`) 초기 빈 상태, (d) 시도→시군구→시스템명 선택해 infra_id 채워지면 저장 버튼 활성. draft/완료 동일 거동. |
| T15 | 설치·패치 environment 누락 저장 | environment null 로 저장 시도 | HTTP 400 |
| T16 | 업무지원 내부/외부 상반 필드 정합 | EXTERNAL 저장 시 org_unit_id null, INTERNAL 저장 시 proj_id null | DB 조회로 확인 |
| T17 | 조직 삭제 시 하위 존재 시 차단 | 하위 있는 DIVISION 삭제 API 호출 | HTTP 400 |
| T18 | ERD 문서 갱신 | `rg "tb_org_unit" docs/generated/erd.md` | ≥ 1 hit |
| T19 | 업무지원 목록 대상 표시 분기 (FR-2-F) | 업무지원 EXTERNAL/INTERNAL 각 1건 생성 후 목록 조회 | EXTERNAL 행: 지자체+시스템 텍스트. INTERNAL 행: 조직 경로 텍스트 (예: `AI GIS 연구본부 > AI GIS 연구부 > 연구1팀`) |
| T20 | 설치·패치 목록 환경 뱃지 (FR-4-C) | PROD/TEST 각 1건 생성 후 목록 DOM 검사 | `.env-badge-prod` / `.env-badge-test` 클래스 출력, 라벨 "운영"/"테스트" 정확 |
| T21 | 관리자 네비게이션에 조직도 메뉴 노출 (FR-3-B, v2) | `rg -n "/admin/org-units" src/main/resources/templates/main-dashboard.html` | ≥ 1 hit |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 1 SQL 실행 실패 | 마지막 커밋 `git revert` → DDL 변경 롤백. 수동 DROP 필요 시 `DROP TABLE tb_org_unit CASCADE; ALTER TABLE tb_document DROP COLUMN ...` |
| Step 2 compile 실패 | OrgUnit 파일 import/의존 재확인. Spring Data JPA 기본 `findByParent_UnitId` 네이밍 컨벤션 주의 |
| Step 3 공통 JS 모듈화 후 착수계·점검내역서 회귀 | 해당 2문서는 기존 로직 **보존 커밋** 을 백업으로 둠. 문제 시 `git checkout <backup> -- <file>` |
| Step 4 업무지원 토글 UI 오동작 | radio change 이벤트 리스너 + display 제어 단순화. 조건부 required 주의 |
| Step 5 저장 400 과다 발생 (레거시 레코드) | FR-1-F 배너 안내가 명확한지 UI 재확인. 필요 시 기간 한정 허용 옵션 검토 |
| Step 6 관리 화면 트리 렌더 깨짐 | 재귀 렌더링 깊이·CSS 문제. 최소 트리(DIVISION 만) 부터 단계적 확장 |
| 배포 후 회귀 발견 | `git revert <sprint-5-commit>` → 재배포. DB 는 ADD COLUMN IF NOT EXISTS 라 롤백 시 컬럼은 남지만 미사용 상태로 무해 |

---

## 4. 리스크·완화 재확인

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 공통 JS 모듈 추출 중 기존 2개 문서(commence/inspect) 회귀 | 중간 | Step 3 의 수동 검증 포함. 모듈 API 를 기존 함수 시그니처와 최대한 호환 |
| 레거시 infraId 만 있는 레코드 편집 차단이 운영 혼선 | 중간 | 배너 문구 명확히, 관리자 사전 공지 |
| 조직 트리 API 가 첫 로드 시 전체 40개 노드 반환 — 성능? | 낮음 | 40 노드 수준은 수 ms. 100 이상 되면 lazy load 로 전환 (개발계획 범위 외) |
| 조직도 CRUD 동시 편집 충돌 | 낮음 | 관리자 소수 + updated_at 낙관 잠금 없음. 마지막 쓰기 우선 (수용) |
| ERD 업데이트 누락 | 낮음 | T18 에서 강제 검증 |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
