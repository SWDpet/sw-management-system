---
tags: [dev-plan, sprint]
sprint: "doc-split-ops"
status: draft-v2
created: "2026-04-29"
revised: "2026-04-29"
---

# [개발계획서] 문서관리 분리 — 사업문서 / 운영·유지보수 문서 (v2)

- **작성팀**: 개발팀
- **작성일**: 2026-04-29 (v1) / **수정일**: 2026-04-29 (v2: codex 지적 11건 반영)
- **근거 기획서**: [[doc-split-ops]] (`docs/product-specs/doc-split-ops.md` v3, 사용자 최종승인 2026-04-29)
- **상태**: v2 (사용자 최종승인 대기)

---

## 0. 사전 조건 (Preconditions)

| 항목 | 상태 |
|---|---|
| `tb_document` / `inspect_report` 데이터 클리어 (TRUNCATE RESTART IDENTITY CASCADE) | ✅ 완료 (2026-04-29) |
| 디스크 첨부파일 정리 (`uploads/`) | ✅ 디렉토리 부재 — 정리 불필요 |
| 점검내역서 코드 90% 완성 + 테스트 통과 | ✅ (`InspectLegacyDropArchTest`/`InspectResultCodeTest`/`InspectCategoryConstraintTest`) |
| `inspect_template` (70 row), `cont_frm_mst` (5), `maint_tp_mst` (7), `prj_types` (3) 마스터 보존 | ✅ |
| 서버 가동 상태 | ✅ port 8080 |

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔 (회귀 영향 매핑)

**목적**: 변경 대상 코드의 정확한 위치를 grep 으로 확정하여 step 9 (정리) 시 누락 0.

1-1. `Grep "DocumentType\.(INSPECT|FAULT|SUPPORT|INSTALL|PATCH)" src/main/java` → 발견 위치 리스트 → 별도 메모 파일 `docs/exec-plans/doc-split-ops-grep.md` 에 저장.
1-2. `Grep "tb_inspect_(checklist|issue)" src/main/java` → 0 hit 기대 (이미 deprecated).
1-3. `Grep "linkToDocument" src/main/java` → 1 hit (`InspectReportService.java:96`) 확인.
1-4. `Grep "doc-(fault|support|install|patch)" src/main/resources/templates` → 제거 대상 템플릿 목록.
1-5. `Grep "/document/api/(fault|support|install|patch)" src/main/java/com/swmanager/system/controller/DocumentController.java` → 제거 대상 라우트 목록.

**산출물**: `docs/exec-plans/doc-split-ops-grep.md` (역사 기록).

### Step 2 — DDL: tb_ops_doc + 자식 4 + DROP + 안전망 (FR-1, FR-2, FR-2-CHK, FR-2-IDX, FR-10, FR-15)

**파일**: `src/main/resources/db_init_phase2.sql` (수정).

2-1. **안전망 DELETE 추가** (파일 상단, tb_document 블록 앞):
```sql
-- [doc-split-ops 안전망] DocumentType 5종 enum 제거 전 row 부재 보장 (멱등)
DELETE FROM tb_document WHERE doc_type IN ('INSPECT','FAULT','SUPPORT','INSTALL','PATCH');
```

2-2. **신규 tb_ops_doc + 자식 4 CREATE**:
```sql
-- 운영·유지보수 문서 (5 종: INSPECT/FAULT/SUPPORT/INSTALL/PATCH)
CREATE TABLE IF NOT EXISTS tb_ops_doc (
    doc_id              BIGSERIAL PRIMARY KEY,
    doc_no              VARCHAR(50) NOT NULL UNIQUE,           -- codex: NOT NULL 강제
    doc_type            VARCHAR(30) NOT NULL,
    sys_type            VARCHAR(20),
    region_code         VARCHAR(10),
    org_unit_id         BIGINT REFERENCES tb_org_unit(unit_id),
    environment         VARCHAR(20),
    support_target_type VARCHAR(20),
    infra_id            BIGINT REFERENCES tb_infra_master(infra_id),
    plan_id             BIGINT REFERENCES tb_work_plan(plan_id),
    title               VARCHAR(500) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    author_id           BIGINT NOT NULL REFERENCES users(id),
    approver_id         BIGINT REFERENCES users(id),
    approved_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    CONSTRAINT ck_tb_ops_doc_type   CHECK (doc_type IN ('INSPECT','FAULT','SUPPORT','INSTALL','PATCH')),
    CONSTRAINT ck_tb_ops_doc_status CHECK (status IN ('DRAFT','COMPLETED')),
    CONSTRAINT ck_tb_ops_doc_env    CHECK (environment IS NULL OR environment IN ('PROD','TEST')),
    CONSTRAINT ck_tb_ops_doc_target CHECK (support_target_type IS NULL OR support_target_type IN ('EXTERNAL','INTERNAL')),
    CONSTRAINT ck_tb_ops_doc_combo  CHECK (
        (doc_type='INSPECT' AND infra_id IS NOT NULL) OR
        (doc_type IN ('FAULT','SUPPORT') AND region_code IS NOT NULL AND sys_type IS NOT NULL) OR
        (doc_type IN ('INSTALL','PATCH') AND infra_id IS NOT NULL AND environment IS NOT NULL)
    )
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_type_status  ON tb_ops_doc(doc_type, status);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_region_sys   ON tb_ops_doc(region_code, sys_type);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_proj         ON tb_ops_doc(plan_id);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_infra        ON tb_ops_doc(infra_id);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_org_unit     ON tb_ops_doc(org_unit_id);

CREATE TABLE IF NOT EXISTS tb_ops_doc_detail (
    detail_id    BIGSERIAL PRIMARY KEY,
    doc_id       BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    section_key  VARCHAR(50) NOT NULL,
    section_data JSONB NOT NULL DEFAULT '{}'::jsonb
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_detail_doc ON tb_ops_doc_detail(doc_id);

CREATE TABLE IF NOT EXISTS tb_ops_doc_history (
    history_id BIGSERIAL PRIMARY KEY,
    doc_id     BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    action     VARCHAR(30) NOT NULL,
    actor_id   BIGINT REFERENCES users(id),
    changes    JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_history_doc ON tb_ops_doc_history(doc_id);

CREATE TABLE IF NOT EXISTS tb_ops_doc_attachment (
    attach_id   BIGSERIAL PRIMARY KEY,
    doc_id      BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    file_name   VARCHAR(300) NOT NULL,
    file_path   VARCHAR(500) NOT NULL,
    file_size   BIGINT,
    mime_type   VARCHAR(100),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_attachment_doc ON tb_ops_doc_attachment(doc_id);

CREATE TABLE IF NOT EXISTS tb_ops_doc_signature (
    sign_id         BIGSERIAL PRIMARY KEY,
    doc_id          BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    signer_type     VARCHAR(30) NOT NULL,
    signer_name     VARCHAR(100),
    signature_image TEXT,                                       -- DB §A: Base64 PNG 흡수
    signed_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_signature_doc ON tb_ops_doc_signature(doc_id);
```

2-3. **DROP**:
```sql
DROP TABLE IF EXISTS tb_inspect_checklist CASCADE;
DROP TABLE IF EXISTS tb_inspect_issue CASCADE;
```

2-4. **검증**: `bash server-restart.sh` → `DbInitRunner` 가 신규 DDL 실행 + ERROR 0. `psql` 또는 jshell 로 5 개 신규 테이블 존재 확인.

2-5. **baseline 갱신**: `src/test/resources/db_init_phase2.baseline-non-do-hashes.txt` 신규 DDL 블록 해시 추가 (NFR-3).

### Step 3 — Domain Entity + Repository (FR-1, FR-3)

**파일**: `src/main/java/com/swmanager/system/domain/ops/{OpsDocument, OpsDocumentDetail, OpsDocumentHistory, OpsDocumentAttachment, OpsDocumentSignature}.java`, `src/main/java/com/swmanager/system/repository/ops/Ops*Repository.java`.

3-1. **5 개 Entity 작성** — `Document.java` 패턴 그대로 + tb_ops_doc 컬럼 매핑. `OpsDocumentSignature.signatureImage @Column(columnDefinition="TEXT")`.
3-2. **5 개 Repository 작성** — `JpaRepository<Ops*, Long>`. `OpsDocumentRepository` 에 `Optional<OpsDocument> findByDocNo(String docNo);` (FR-6 룩업용), `findByDocTypeAndStatusOrderByCreatedAtDesc`, `findByPlanId` 등.
3-3. **검증**: `./mvnw -q compile` → BUILD SUCCESS.

### Step 4 — Service Layer (FR-3, FR-6, FR-7)

**파일**: `src/main/java/com/swmanager/system/service/ops/{OpsDocService, OpsDocAttachmentService, OpsDocSignatureService, OpsDocLinkService}.java`.

4-1. **OpsDocService**: CRUD + 4 종 (FAULT/SUPPORT/INSTALL/PATCH) 폼 저장·조회. `section_data jsonb` 필수 필드 검증 (FR-13 → codex 권고): doc_type 별 검증 표:

| doc_type | 필수 키 |
|---|---|
| FAULT | `fault_date`, `severity`, `symptom`, `action` |
| SUPPORT | `request_date`, `request_channel`, `requester`, `support_content` |
| INSTALL | `install_date`, `pre_check_completed`, `version`, `verification` |
| PATCH | `patch_date`, `patch_kind`, `target`, `version`, `rollback_plan` |

4-2. **OpsDocAttachmentService**: `${file.ops-upload-dir:./uploads/ops-docs}/yyyyMM/<UUID>.<ext>`. `DocumentAttachmentService` 패턴 복사 + 디렉토리만 변경.

4-3. **OpsDocSignatureService**: 서명 저장/조회 (Base64 PNG `signature_image TEXT`).

4-4. **🌟 OpsDocLinkService** (codex 핵심 권고 — 별도 bean):
```java
@Service
public class OpsDocLinkService {
    @Autowired private OpsDocumentRepository opsDocumentRepository;
    @Autowired private MessageResolver messages;
    private static final Logger log = LoggerFactory.getLogger(OpsDocLinkService.class);

    /** 점검 보고서 → tb_ops_doc.INSPECT row 자동 연계.
     *  REQUIRES_NEW: ops 연계 실패가 본 점검 보고서 트랜잭션을 롤백 X.
     *  별도 bean 호출 → Spring 프록시 경유 → 트랜잭션 격리 보장 (codex 검토 권고). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void linkInspectReport(InspectReport report) {
        try {
            String yyyy = report.getInspectMonth() != null ? report.getInspectMonth().substring(0, 4) : ...;
            String docNo = "INSP-" + yyyy + "-" + report.getId();
            String monthlyFormat = "INSP-" + yyyy + "-" + ... + "-" + report.getId();

            // 호환성 3 포맷 룩업 (사업측 fallback 제거 — INSP- prefix 미사용 보장)
            OpsDocument doc = opsDocumentRepository.findByDocNo(docNo)
                .orElseGet(() -> opsDocumentRepository.findByDocNo(monthlyFormat)
                    .orElseGet(() -> opsDocumentRepository.findByDocNo("INSP-" + report.getId()).orElse(null)));

            if (doc == null) doc = new OpsDocument();
            doc.setDocNo(docNo);
            doc.setDocType(OpsDocType.INSPECT);
            // ... (proj_id, author_id, sys_type, infra_id, status 매핑)
            opsDocumentRepository.save(doc);

            log.info("점검내역서 운영문서 연계 완료: docNo={}, reportId={}", docNo, report.getId());
        } catch (Exception e) {
            log.warn("점검내역서 운영문서 연계 실패 (REQUIRES_NEW로 본 트랜잭션 격리됨): reportId={}, msg={}",
                report.getId(), e.getMessage());
        }
    }
}
```

4-5. **검증**: `./mvnw -q compile` → BUILD SUCCESS.

### Step 5 — Controller + Templates (FR-4, FR-12, FR-13)

**파일**: `src/main/java/com/swmanager/system/controller/ops/OpsDocController.java`, `src/main/resources/templates/ops-doc/{list, doc-fault, doc-support, doc-install, doc-patch}.html` + PDF 짝.

5-1. **OpsDocController** 라우트 (codex 지적 — update/delete 명시 분리):
```
GET    /ops-doc/list                       → 통합 리스트 (5 종, 점검 row 포함)
GET    /ops-doc/{type}/form                → 신규 작성 폼 (4 종 선택)
GET    /ops-doc/{type}/{docId}             → 상세
GET    /ops-doc/{type}/{docId}/edit        → 수정 폼 (신규 — codex 명시 요구)
POST   /ops-doc/api/{type}                 → 신규 저장
PUT    /ops-doc/api/{type}/{docId}         → 기존 문서 수정 (신규 명시)
DELETE /ops-doc/api/{docId}                → 삭제
GET    /ops-doc/api/pdf/{docId}            → PDF
GET    /ops-doc/api/excel/{docId}          → 엑셀
GET    /ops-doc/api/attachments/{docId}    → 첨부 목록
POST   /ops-doc/api/attachment/upload/{docId} → 첨부 업로드
DELETE /ops-doc/api/attachment/{attachId}  → 첨부 삭제
```

5-2. **신규 4 템플릿**: `doc-inspect.html` 패턴 복사 (단일 폼 + sub-section + section-title teal 좌측바, max-width 1100px). **인라인 hex 0 건** — `var(--primary)`/`var(--surface)`/`var(--border)`/`var(--text)` 토큰만 사용 (NFR-6). 모바일 반응형 `@media 900px` 의무 (NFR-7). 엠프티 스테이트 추가.

5-3. **폼 필드** (FR-13): 디자인팀 권고 추가 컬럼 모두 jsonb 에 포함.

5-4. **검증**: `bash server-restart.sh` → `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ops-doc/list` → 302 (로그인 리다이렉트 OK).

### Step 6 — 점검내역서 커스터마이징 (FR-5, FR-6, FR-11)

**파일**: `src/main/java/com/swmanager/system/service/InspectReportService.java`, `src/main/resources/templates/document/{doc-inspect, inspect-detail, inspect-preview}.html`.

6-1. **InspectReportService 수정**:
- `linkToDocument()` private 메서드 (103-148줄) **삭제**.
- `@Autowired private OpsDocLinkService opsDocLinkService;` 추가.
- `save()` 의 96 줄 `linkToDocument(saved);` → `opsDocLinkService.linkInspectReport(saved);` 로 교체.
- `import com.swmanager.system.domain.workplan.Document` / `DocumentRepository` 등 미사용 import 정리.

6-2. **점검 페이지 시각 단서 추가** (FR-11):
- `doc-inspect.html`, `inspect-detail.html`, `inspect-preview.html` page-header 영역에:
  - 그룹 chip: `<span class="group-chip group-ops" th:text="#{doc.group.ops}">운영문서</span>` (`var(--primary-50)` 배경, `var(--primary)` 텍스트)
  - 브레드크럼: `홈 › 운영·유지보수 문서 › 점검내역서`
- 컨트롤러 (DocumentController 의 inspect 라우트) 에 `model.addAttribute("activeMenu", "ops");` 추가 → top-nav 의 `aria-current` 자동 부착.

6-3. **검증**:
- `./mvnw -q compile` → BUILD SUCCESS.
- 점검내역서 통합 테스트: 임시 메서드로 `OpsDocLinkService.linkInspectReport` 강제 실패시켜도 본 보고서 정상 저장 (트랜잭션 격리 검증) — 새 단위 테스트 `OpsDocLinkServiceTest.transactionIsolation()`.
- **점검내역서 90% 코드 보존 검증** (codex 지적 — diff 강제):
  - `git diff HEAD -- src/main/java/com/swmanager/system/service/InspectReportService.java | wc -l` → ±20 줄 이내 (linkToDocument 메서드 1 개 삭제 + opsDocLinkService 호출 1 줄 + import 정리만).
  - `git diff HEAD -- src/main/resources/templates/document/doc-inspect.html` 등 → page-header 영역 추가만, 폼 본문 0 줄 변경.
  - 기존 점검 테스트 3 개 (`InspectLegacyDropArchTest`, `InspectResultCodeTest`, `InspectCategoryConstraintTest`) 100% 통과 — `./mvnw -q test -Dtest='Inspect*Test'`.

### Step 7 — top-nav 메뉴 갱신 (FR-8)

**파일**: `src/main/resources/templates/fragments/top-nav.html`.

7-1. **단일 드롭다운 유지** + 라벨 정비:
- `사업 문서 작성` → `사업문서`
- "📋 문서관리 목록" 단일 entry → "📋 사업문서 목록" + "🛠 운영문서 목록" 분리.

7-2. **disclosure navigation 패턴** (codex 권고 — `role="menu"` 회피):
```html
<button id="docMenuToggle" aria-expanded="false" aria-controls="docMenuPanel"
        onclick="toggleDocumentMenu(event)"
        onkeydown="if(event.key==='Escape')closeDocumentMenu()">
    <i class="fas fa-file-lines"></i> 문서관리 ▾
</button>
<nav id="docMenuPanel" aria-label="문서관리" hidden>
    <a th:href="@{/document/list}" th:attr="aria-current=${activeMenu=='business'}?'page':null">📋 사업문서 목록</a>
    <a th:href="@{/ops-doc/list}"  th:attr="aria-current=${activeMenu=='ops'}?'page':null">🛠 운영문서 목록</a>
    <div class="section-header">사업문서</div>
    <a th:href="@{/document/commence/form}">착수계</a>
    <a th:href="@{/document/interim/form}">기성계</a>
    <a th:href="@{/document/completion/form}">준공계</a>
    <div class="section-header">운영·유지보수 문서</div>
    <a th:href="@{/document/inspect/form}">점검내역서</a>     <!-- URL 그대로 (FR-5) -->
    <a th:href="@{/ops-doc/fault/form}">장애처리</a>
    <a th:href="@{/ops-doc/support/form}">업무지원</a>
    <a th:href="@{/ops-doc/install/form}">설치보고서</a>
    <a th:href="@{/ops-doc/patch/form}">패치내역서</a>
</nav>
```

7-3. **CSS** — 인라인 hex 제거 (`#fff`/`#444`/`#fbe9e7` 등) → `var(--text2)`/`var(--surface)`/`var(--primary-50)` 토큰. 신규 추가 `<a>` 만 적용 (기존 라인은 Phase 4 일괄 리팩터로 미룸).

7-4. **검증**:
- 키보드 시나리오: `Tab` → 토글 도달 → `Enter/Space` → 펼침 → `Tab` 로 항목 이동 → `Esc` → 닫힘.
- Playwright 테스트 (codex 권고) — `src/test/playwright/top-nav.spec.ts` 작성:
  - `aria-expanded` 토글 검증
  - `Esc` 키 닫기 검증
  - `aria-current` 활성 페이지 표시 검증
- 검증 보류 가능 사유 (Playwright 셋업 비용) — 그 경우 단위 테스트로 `activeMenu` 모델 변수 검증 + 수동 키보드 스모크.

### Step 8 — 기존 5종 코드/템플릿 정리 (FR-9, 사업측 무영향)

**파일**: `DocumentType.java`, `DocumentController.java`, `DocumentDTO.java`, `PerformanceService.java`, `InspectDocNoMigrationRunner.java`, `templates/document/doc-{fault,support,install,patch}.html` + PDF 짝.

8-1. **DocumentType enum 좁힘** — `INSPECT/FAULT/SUPPORT/INSTALL/PATCH` 5 종 제거. `COMMENCE/INTERIM/COMPLETION` 만 잔존.

8-2. **DocumentController** — Step 1-5 grep 결과 기준 5 종 라우트·핸들러 제거. POST `/api/inspect-report` 등 점검 API 는 **유지** (FR-5).

8-3. **DocumentDTO.getDocTypeLabel()** — 5 종 분기 제거.

8-4. **PerformanceService** — 5 종 doc_type 분기 제거 또는 ops 측 (`OpsDocService`) 으로 위임.

8-5. **InspectDocNoMigrationRunner** — `tb_document` 의 INSP- docNo 마이그레이션 → row 0 이라 무동작. **deprecate 처리 (`@Deprecated` + 코멘트)** 또는 **ops 측으로 이전** (`OpsDocNoMigrationRunner`). 결정: ops 측으로 이전 — 향후 `tb_ops_doc.doc_no` 의 INSP- 포맷 마이그레이션 가능성 보존.

8-6. **템플릿 삭제**: `templates/document/doc-{fault,support,install,patch}.html` + PDF 짝 8 개. `Grep` 으로 참조 0 건 확인 후 삭제.

8-7. **검증**:
- `./mvnw -q compile` → BUILD SUCCESS.
- `Grep "DocumentType\.(FAULT|SUPPORT|INSTALL|PATCH)" src/main/java` → 0 hit.
- `bash server-restart.sh` → 정상.
- **사업문서 회귀 테스트** (codex 지적 — Step 8 에서 DTO/Service 수정 시 0% 변경 보장):
  - `git diff HEAD -- src/main/java/com/swmanager/system/controller/DocumentController.java` 에서 사업 3 종 (COMMENCE/INTERIM/COMPLETION) 라우트 핸들러 메서드 시그니처·바디 무변경 확인.
  - `Grep "DocumentType.\(COMMENCE\|INTERIM\|COMPLETION\)" src/main/java` 결과 행 수가 v1 sprint 직전과 동일 (Step 1 grep 결과 메모와 비교).
  - 사업문서 폼 4 라우트 (`/document/list`, `/document/{commence|interim|completion}/form`) curl 200/302.

### Step 9 — ERD/Snapshot/DESIGN.md 갱신 (FR-14, FR-16)

**파일**: `docs/erd-document.mmd`, `docs/erd-descriptions.yml`, `docs/generated/erd.md`, `docs/references/snapshots/2026-04-27-prod-schema.sql` (또는 신규 snapshot), `docs/DESIGN.md`.

9-1. **erd-document.mmd**:
- `tb_inspect_checklist`/`tb_inspect_issue` 블록 제거.
- `tb_ops_doc` + 자식 4 블록 추가.
- `tb_document ||--o{ ...` 관계는 사업 자식만, `tb_ops_doc ||--o{ ...` 관계 신규.

9-2. **erd-descriptions.yml** — tb_ops_doc 계열 5 개 테이블 description 추가.

9-3. **generated/erd.md** — 자동 재생성 또는 수동 갱신.

9-4. **snapshot SQL** — 운영 환경 적용 후 `pg_dump` 로 갱신 또는 `2026-04-29-doc-split-ops.sql` 신규 추가.

9-5. **DESIGN.md** 추가 시안 (FR-14):
- 운영문서 상태 뱃지 5 색 chip (DRAFT/COMPLETED + 후속 라이프사이클 IN_PROGRESS/RESOLVED/ROLLED_BACK)
- ops 분류 chip 4 색 (장애=danger / 지원=info / 설치=success / 패치=warning)
- PDF letterhead/serif 일관성 가이드

### Step 10 — 빌드 / 재기동 / 스모크

10-1. `./mvnw -q clean compile test` → BUILD SUCCESS.
10-2. `bash server-restart.sh` → `Started SwManagerApplication` + ERROR 0.
10-3. **런타임 검증** (NFR-2 9 개 라우트 — codex 권고로 본 단계에서 고정):
```bash
# 사업문서 (회귀 0 건 확인)
curl -s -o /dev/null -w "%{http_code} /document/list\n"               http://localhost:8080/document/list
curl -s -o /dev/null -w "%{http_code} /document/commence/form\n"      http://localhost:8080/document/commence/form
curl -s -o /dev/null -w "%{http_code} /document/interim/form\n"       http://localhost:8080/document/interim/form
curl -s -o /dev/null -w "%{http_code} /document/completion/form\n"    http://localhost:8080/document/completion/form

# 점검내역서 (URL 보존 확인)
curl -s -o /dev/null -w "%{http_code} /document/inspect/form\n"       http://localhost:8080/document/inspect/form

# 운영문서 (신규)
curl -s -o /dev/null -w "%{http_code} /ops-doc/list\n"                http://localhost:8080/ops-doc/list
curl -s -o /dev/null -w "%{http_code} /ops-doc/fault/form\n"          http://localhost:8080/ops-doc/fault/form
curl -s -o /dev/null -w "%{http_code} /ops-doc/support/form\n"        http://localhost:8080/ops-doc/support/form
curl -s -o /dev/null -w "%{http_code} /ops-doc/install/form\n"        http://localhost:8080/ops-doc/install/form
curl -s -o /dev/null -w "%{http_code} /ops-doc/patch/form\n"          http://localhost:8080/ops-doc/patch/form
```
모두 302 (로그인 리다이렉트) 또는 200 기대.

10-4. **수동 스모크 시나리오** (브라우저 로그인 후):
- 사업문서 착수계 신규 작성 → 저장 → 목록 진입 (회귀 0 건). 기성계·준공계도 동일.
- **사업문서 PDF/Excel 출력 회귀** (codex 지적): 착수계 1건 신규 → PDF 다운로드 → 파일 정상 + 헤더/필드 무변경. 기성계 Excel export 도 동일.
- **사업문서 update/delete 회귀**: 기성계 1건 작성 → 수정 → 저장 → 변경 반영. 삭제 → 목록에서 사라짐.
- 점검내역서 신규 작성 → COMPLETED → `tb_ops_doc.INSPECT` row 자동 생성 확인 (jshell 또는 admin SQL).
- 점검내역서 PDF 다운로드 → 출력 무변경.
- 장애처리 신규 작성 → 저장 → **수정 → 저장 → 변경 반영 → 삭제** → 첨부 업로드 → `uploads/ops-docs/yyyyMM/` 디렉토리·파일 생성 확인.
- top-nav 키보드 스모크: `Tab/Enter/Esc` 동작.
- **다크모드 토글 증적** (codex 지적 — pass/fail 기준 강화): 운영문서 5 페이지 (list + 4 종 폼) 라이트/다크 모드 스크린샷 캡처 → `var(--text)` `var(--surface)` 등 토큰 정상 반전 확인. 인라인 hex 잔존 시 시각 결함 즉시 노출.

10-5. **codex 검증 호출** (구현 완료 후):
```bash
codex review "기획서 docs/product-specs/doc-split-ops.md + 개발계획서 docs/exec-plans/doc-split-ops.md 대비 실제 구현 검증. FR-1~16 / NFR-1~8 / T1~T15 항목별 충족·미충족·부분충족 판정"
```

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | DocumentType 5 종 enum 참조 잔존 0 | `Grep "DocumentType\.(INSPECT\|FAULT\|SUPPORT\|INSTALL\|PATCH)"` | 0 hit |
| T2 | tb_inspect_checklist/issue 참조 0 (src/main/java) | `Grep "tb_inspect_(checklist\|issue)" src/main/java` | 0 hit |
| T3 | Maven compile + test | `./mvnw -q clean compile test` | BUILD SUCCESS |
| T4 | DBInitRunner 멱등성 | `bash server-restart.sh` 2 회 연속 | ERROR 0 |
| T5 | DBInitRunner baseline hash | `DbInitRunnerBaselineTest` | PASS |
| T6 | tb_ops_doc CHECK 위반 5 케이스 (잘못된 doc_type/status/environment/support_target_type/조합 NULL) | 단위 테스트 `OpsDocCheckConstraintTest` | 5/5 INSERT 실패 |
| T7 | OpsDocLinkService 트랜잭션 격리 | `OpsDocLinkServiceTest.linkInspectReportFailDoesNotRollbackParent()` | 본 보고서 정상 저장 |
| T8 | 점검내역서 회귀 9 라우트 (codex 지적 — URL 명시): `POST /api/inspect-report`, `GET /api/inspect-report/{id}`, `GET /api/inspect-reports?pjtId=`, `GET /api/inspect-report/previous-visits`, `DELETE /api/inspect-report/{id}`, `GET /api/inspect-template?type=`, `GET /api/inspect-pdf/{reportId}`, `GET /inspect-detail/{reportId}`, `GET /inspect-preview/{reportId}` | curl + 200/302 (`*` 는 prefix `/document` 또는 `/document/api`) | 9/9 PASS |
| T9 | 신규 4 종 운영문서 라우트 + CRUD 전체 (codex 지적 — update/delete/PDF 명시): `GET /list`, 4 종 × (`/{type}/form`, `/{type}/{id}`, `/{type}/{id}/edit`, `POST /api/{type}`, `PUT /api/{type}/{id}`, `DELETE /api/{id}`, `GET /api/pdf/{id}`, `GET /api/excel/{id}`) | curl + 200/302 + 통합 테스트 | 모두 PASS |
| T10 | 첨부 업로드 → 디스크·DB 정합 | 통합 테스트 (`OpsDocAttachmentServiceIT`) | 파일 + row 일치 |
| T11 | section_data 필수 필드 검증 | 단위 테스트 (4 doc_type × 누락 키) | 모두 IllegalArgumentException |
| T12 | top-nav 키보드 (Tab/Enter/Space/Esc) — **필수 수동 스모크** + Playwright **선택** (codex 지적 — 필수/선택 명확화). Playwright 셋업 완료 시 `aria-expanded` 토글 / `Esc` 닫기 / `aria-current` 자동화. 미셋업 시 수동 스모크로 충분 (CI 자동화는 후속 스프린트). | 수동 스모크 (필수) + Playwright (선택) | 키보드 4 동작 모두 PASS |
| T13 | 신규 ops-doc 페이지 인라인 hex 0 건 | `Grep "#[0-9a-fA-F]{3,6}" src/main/resources/templates/ops-doc` | 0 hit |
| T14 | 다크모드 토글 운영문서 페이지 적용 (codex 지적 — 증적 기준 강화): 라이트/다크 모드 스크린샷 5 페이지 캡처 + 시각 결함 0. | 수동 스모크 + 스크린샷 | 토큰 반전 + 결함 0 |
| T15 | 감사 보고서 갱신 + FR/NFR 매핑 (codex 지적): doc-split-ops 항목 ☑ + 본문에 "FR-1~16 / NFR-1~8 모두 충족" 한 줄 명시. | `docs/generated/audit/2026-04-18-system-audit.md` | ☑ + 매핑 한 줄 |
| T16 | ERD/snapshot 갱신 | `docs/erd-document.mmd` + snapshot SQL | tb_ops_doc 5 + DROP 2 반영 |
| **T17** | **사업문서 회귀 0 건 (codex 핵심 지적)**: 사업 3 종 (COMMENCE/INTERIM/COMPLETION) controller/service/dto/template 의 동작 무변경. `/document/list`, 착수/기성/준공 CRUD/PDF/Excel 전체. | curl 4 라우트 + 수동 스모크 (착수 신규/기성 수정/준공 PDF/기성 Excel) + git diff 라인 수 검증 (사업 3 종 컨트롤러 메서드 무변경) | 회귀 0 건 |
| **T18** | **사업문서 데이터 무변경 (codex 지적)**: `tb_document` 스키마 / `Document` 엔티티 / `DocumentRepository` 인터페이스 변경 0. | `git diff HEAD -- src/main/java/com/swmanager/system/domain/workplan/Document.java src/main/java/com/swmanager/system/repository/workplan/DocumentRepository.java` | 0 줄 변경 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 2 (DDL) 멱등성 실패 | 다음 SQL 정확히 실행 (와일드카드 X — codex 지적): `DROP TABLE IF EXISTS tb_ops_doc_signature CASCADE; DROP TABLE IF EXISTS tb_ops_doc_attachment CASCADE; DROP TABLE IF EXISTS tb_ops_doc_history CASCADE; DROP TABLE IF EXISTS tb_ops_doc_detail CASCADE; DROP TABLE IF EXISTS tb_ops_doc CASCADE;` 후 db_init_phase2.sql 수정 후 `bash server-restart.sh` 재실행. 사업문서 영향 없음 (별도 테이블). |
| Step 8 (DocumentType enum 5 종 제거 후 컴파일 회귀) | `git revert <Step 8 commit>` 또는 `DocumentType.java` 에서 5 종 enum 값 임시 복원 (`INSPECT/FAULT/SUPPORT/INSTALL/PATCH` 추가) → 컴파일 성공 → 누락 참조 발견 후 한 파일씩 다시 정리. 사업문서 코드는 enum 5 종 미참조이므로 무영향. |
| Step 3-5 (Java) compile 실패 | 해당 Step 의 Edit 들을 git stash 또는 되돌림. import 누락·타입 불일치 재확인. |
| Step 6 (점검 커스터마이징) 회귀 — 점검내역서 동작 깨짐 | `git restore src/main/java/com/swmanager/system/service/InspectReportService.java` + 점검 템플릿 3 개. `OpsDocLinkService` 호출 라인만 다시 신중 적용. |
| Step 7 (top-nav) 메뉴 깨짐 | `git restore src/main/resources/templates/fragments/top-nav.html`. disclosure 패턴 단계적 마이그레이션. |
| Step 8 (5 종 정리) 미발견 참조로 컴파일 실패 | grep 다시 + 컴파일 오류 메시지 따라 한 파일씩 수정. enum 값 5 종은 일시 복원 가능 (rollback commit). |
| 배포 후 운영 회귀 | `git revert <merge-commit>` → 재배포. 데이터 손실 없음 (사업측 무영향, ops 측은 신규 row 만 생성). |
| 점검내역서 작성·저장 회귀 | 첫 보고는 사용자 발견 즉시 — `git revert` 로 즉시 복구 + 단위 테스트 보강 후 재시도. |

---

## 4. 리스크·완화 재확인

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| Step 6 점검 회귀 (90% 완성 코드) | 중간 | InspectReportService 의 변경은 1 라인 (`linkToDocument(saved)` → `opsDocLinkService.linkInspectReport(saved)`) + 미사용 import 정리만. 점검 본 데이터 흐름 무변경. T7 트랜잭션 격리 단위 테스트로 회귀 차단. |
| Spring self-invocation 으로 REQUIRES_NEW 무효 | 낮음 (해소) | OpsDocLinkService 별도 bean — 프록시 경유 보장. T7 로 검증. |
| top-nav `role="menu"` WAI-ARIA 회귀 | 낮음 (해소) | disclosure navigation 패턴. T12 키보드 스모크. |
| DocumentType 5 종 enum 제거 시 외부 참조 누락 | 중간 | T1 grep + 컴파일 실패로 0 검증. 4 개 정리 대상 (PerformanceService/InspectDocNoMigrationRunner/DocumentDTO + 컨트롤러) Step 8 에 명시. |
| section_data jsonb 필수 필드 부재 | 중간 | T11 단위 테스트. OpsDocService 저장 시 검증 표 적용. |
| 신규 ops-doc 다크모드 부채 추가 | 낮음 | T13 grep (인라인 hex 0). 토큰 100% 사용 강제. |
| Playwright 셋업 비용 | 낮음 | 수동 스모크 (Step 7-4 키보드 시나리오) 로 대체 가능. CI 도입은 후속 스프린트. |
| baseline-non-do-hashes.txt 갱신 누락 | 낮음 | T5 (`DbInitRunnerBaselineTest`) 가 자동 검출. |

---

## 5. 의존성 / 작업 순서

```
Step 1 (사전 스캔)
  ↓
Step 2 (DDL) ──→ Step 3 (Entity/Repo) ──→ Step 4 (Service, OpsDocLinkService) ──→ Step 5 (Controller/Template)
                                                    ↓
                                              Step 6 (점검 커스터마이징)
                                                    ↓
                                              Step 7 (top-nav)
                                                    ↓
                                              Step 8 (5 종 정리)
                                                    ↓
                                              Step 9 (ERD/snapshot/DESIGN.md)
                                                    ↓
                                              Step 10 (빌드/재기동/스모크/codex 검증)
```

각 Step 후 빌드 확인. Step 4·6·8 에서 컴파일 안전성 검증 필수.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
