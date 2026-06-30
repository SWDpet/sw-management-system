# 개발계획서 — LSA 버전 프리셋 + 작성자 본인 편집 가드 (전 도메인 확대)

- 문서: `docs/exec-plans/lsa-version-preset-and-owner-edit-guard.md`
- 기획서: `docs/product-specs/lsa-version-preset-and-owner-edit-guard.md` (v0.2 승인)
- 작성: 2026-06-30
- 상태: **v0.2 (codex 검토 반영 — 사용자 최종승인 대기)**

---

## 0. 요구사항 매핑 (FR / NFR)

| ID | 요구 | 기획서 출처 |
|---|---|---|
| FR-1 | LSA 버전 필드 = `Basic 3.0`/`Pro 3.0` datalist + 직접입력, 신규폼 기본값 공란 | T1, D1, D5 |
| FR-2 | LSA 수정폼·저장(update)·삭제에 `admin \|\| 작성자본인` 서버 가드 | T2, D2, D3 |
| FR-3 | LSA 목록·상세 수정/삭제 버튼 행단위 소유권 노출 + 상세 비소유 muted 힌트 | T2, 디자인 D |
| FR-4 | ops-doc 수정(PUT)·삭제(DELETE)에 `admin \|\| 작성자본인` 서버 가드 | T3 |
| FR-5 | document 저장(update)·삭제에 `admin \|\| author본인` 서버 가드 | T3 |
| FR-6 | workplan 저장(update)·삭제에 `admin \|\| createdBy본인` 서버 가드 | T3 |
| FR-7 | T3 3도메인 목록/상세 버튼 행단위 소유권 노출(서버 가드와 일치) | T3, §3 |
| NFR-1 | 서버 가드 정본, 버튼 숨김은 보조(직접 POST/PUT 위조 차단) | NFR-1 |
| NFR-2 | 관리자·본인작성건 동작 불변, VIEW 영향 없음, 신규(create)는 제한 없음 | NFR-2 |
| NFR-3 | 레거시 null 작성자 → 비관리자 매칭 실패(admin만) 안전동작 | NFR-3 |

> **공통 원칙**: `create`(신규)는 소유권 제한 **없음**(EDIT면 누구나 작성). 제한은 **update·delete**(기존 행 대상)에만. 관리자(ROLE_ADMIN)는 전 도메인 우회.

---

## 1. 도메인별 소유권 판정 시그니처 (확정)

| 도메인 | 작성자 소스 | 현재사용자 | 비교식 | 엔티티 로드 |
|---|---|---|---|---|
| LSA | `Lsa.createdBy`(String 로그인ID) | `cu.getUsername()` | `createdBy.equals(cu.getUsername())` | `lsaService.getById(id)`→`LsaDTO.createdBy()` |
| ops-doc | `OpsDocument.createdBy`(String 로그인ID) | `cu.getUsername()` | `createdBy.equals(cu.getUsername())` | `opsDocService.findById(id)` |
| document | `Document.author`(User FK) | `cu.getUser().getUserSeq()` | `author.getUserSeq().equals(cu.getUser().getUserSeq())` | `documentService.getDocumentById(id)` |
| workplan | `WorkPlan.createdBy`(User FK) | `cu.getUser().getUserSeq()` | `createdBy.getUserSeq().equals(cu.getUser().getUserSeq())` | `workPlanService.getWorkPlanById(id)` |

> ⚠ **codex 검증 반영**: `User`의 PK는 **`userSeq`**(`@Id Long userSeq`, column `user_id`) — **`getId()` 없음**. User FK 비교는 반드시 `getUserSeq()`. (`getId()`로 쓰면 컴파일 불가)
> ⚠ `CustomUserDetails.getUsername()` = **로그인 ID(userid)**, `getDisplayName()` = 실명. String createdBy(LSA/ops-doc)는 생성 시 로그인 ID로 저장 → `getUsername()` 비교가 정본. `getCurrentUser().getUser().getUsername()`(=실명)와 **혼용 금지**.
> null 방어: 작성자/현재사용자/Seq 중 하나라도 null → 비관리자는 **불일치(거부)**. 관리자는 위 비교 이전에 `isAdmin()` 으로 통과.

---

## 2. 구현 순서 (Phase)

### P0 — 사전 확인 (작업 전 게이트)
- 빌드 그린 확인(`./mvnw -q compile`), 워킹트리 clean 확인.
- 후보 테이블 작성자 null 0건 재확인(이미 검증: tb_ops_doc 23/0, tb_document 42/0, tb_work_plan 1/0).

### P1 — T1: LSA 버전 datalist (FR-1)
- `lsa-form.html` 버전 필드(현 89행) 변경:
  ```html
  <input type="text" id="version" name="version" list="versionList" required
         th:value="${lsa != null ? lsa.version() : ''}">
  <datalist id="versionList">
    <option value="Basic 3.0"></option>
    <option value="Pro 3.0"></option>
  </datalist>
  ```
  - 신규폼 기본값 `'3.0'` → `''`(공란, D5). 수정모드는 `lsa.version()` 유지.
  - 새 중복 id 금지(디자인 E) — `versionList` 단일.
- 서버: `LsaService.validateRequired` 의 버전 필수 유지. **프리셋 화이트리스트 강제 안 함**(직접입력 허용 = 요구).
- 회귀: 기존 저장/수정 흐름 변화 없음(필드 name=version 동일).

### P2 — T2: LSA 작성자 본인 가드 (FR-2, FR-3)
**서버 (LsaController):**
- 헬퍼 추가:
  ```java
  /** 현재 사용자가 해당 LSA 작성자인지 (createdBy = 로그인 ID) */
  private boolean isOwnerOf(Long id) {
      LsaDTO l = lsaService.getById(id);
      return l.createdBy() != null && l.createdBy().equals(getCurrentUser().getUsername());
  }
  /** 편집 권한 + 소유권(또는 관리자) 게이트 */
  private void checkEditOwnership(Long id) {
      checkEditAuth();                       // 기존: EDIT|admin (위조 1차 차단)
      if (!isAdmin() && !isOwnerOf(id))
          throw new InsufficientPermissionException("LSA 작성자 본인");
  }
  ```
- 적용 지점:
  - `editForm`(GET `/lsa/{id}/edit`, 111행): `checkEditAuth()` → `checkEditOwnership(id)`.
  - `save`(POST `/lsa/save`, 123행): **update 분기에서만**(form.getId()!=null) `checkEditOwnership(form.getId())`. create 분기는 기존 `checkEditAuth()` 유지(신규=제한 없음).
  - `delete`(POST `/lsa/{id}/delete`, 142행): `checkEditAuth()` → `checkEditOwnership(id)`.

**UI (행단위 canEdit):**
- `list`·`detail` 컨트롤러 메서드: 전역 `canEdit` 외에 **행/단건 소유권** 반영.
  - 상세: `model.addAttribute("canEdit", isAdmin() || ("EDIT".equals(authLsa) && isOwnerOf(id)))`.
  - 목록: 행마다 소유 여부 필요 → `LsaDTO.createdBy()` 가 이미 있으므로 템플릿에서 `${isAdmin or (canEditBase and dto.createdBy() == currentUserId)}` 로 판정. 모델에 `currentUserId`(=`cu.getUsername()`)·`isAdmin`·`canEditBase` 주입.
- `lsa-detail.html` 36-40행: 수정/삭제 버튼 `th:if="${canEdit}"` 유지(canEdit 정의가 소유권 포함으로 바뀜). 비소유·비관리자 분기에 muted 힌트 1줄 추가:
  ```html
  <span class="hint" th:if="${!canEdit}" style="color:var(--muted)">작성자 본인 또는 관리자만 수정·삭제할 수 있습니다.</span>
  ```
- `lsa-list.html` 76·84행 인근: 수정/삭제 아이콘 `th:if` 를 행 소유권 식으로. 상세(눈) 아이콘은 항상 유지(현행).

### P3 — T3 서버 가드 (FR-4·5·6) — **보안 정본, 필수**
도메인별로 baseline 권한 확인 직후 소유권 게이트 추가. **create/신규 분기 제외**.

- **ops-doc (`OpsDocController`)** — 헬퍼 신설:
  ```java
  /** EDIT 통과 후 소유권(또는 admin). 허용 null, 아니면 403. */
  private ResponseEntity<ApiResult> requireOwnerOrAdmin(Long docId, CustomUserDetails u) {
      if (isAdmin()) return null;
      String me = (u != null) ? u.getUsername() : null;
      String owner = opsDocService.findById(docId).map(OpsDocument::getCreatedBy).orElse(null);
      if (me != null && me.equals(owner)) return null;
      return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "작성자 본인 또는 관리자만 수정·삭제할 수 있습니다."));
  }
  ```
  - `update`(PUT, 369행 `requireDocEdit` 직후): `denied = requireOwnerOrAdmin(docId, currentUser); if (denied!=null) return denied;`
  - `delete`(DELETE, 396행 `requireDocEditOrAdmin` 직후): 동일.
  - **(codex#3) `editForm` GET**(`/ops-doc/{type}/{docId}/edit`, 255행): 현재 `canEdit=hasDocEdit` 만 → `admin||owner` 아니면 `InsufficientPermissionException` 또는 목록 redirect. (PUT만 막으면 비소유자가 채워진 수정폼을 봄 → 정보노출·UX)
  - **(codex#4) docId 기반 문서변경 서브엔드포인트**도 동일 `requireOwnerOrAdmin(docId,..)` 적용: support-file 업로드/삭제(439·484행), attachment 업로드(755행), requester(548행), kb feedback(604행). attachment 삭제(771행, `attachId`)는 attach→부모 docId 해소 후 적용. (모두 "문서 편집" 표면 — 본인 한정)
  - `isAdmin()` 기존 메서드(221행) 재사용.

- **document (`DocumentController`)** — 헬퍼 신설:
  ```java
  private boolean isAuthorOf(Integer docId) {
      Document d = documentService.getDocumentById(docId);
      return d != null && d.getAuthor() != null && getCurrentUser() != null
          && d.getAuthor().getUserSeq().equals(getCurrentUser().getUser().getUserSeq());
  }
  ```
  - `saveDocument`(POST `/api/save`, 256행): **docId != null(update)** 일 때 `if (!isAdmin() && !isAuthorOf(docId)) return 403`. create(docId==null)는 제외.
  - `deleteDocument`(POST `/delete/{id}`, 390행): `if (!isAdmin() && !isAuthorOf(id)) { errorMessage; redirect; }`.
  - **(codex#4) `changeStatus`**(POST `/api/status/{id}`, 369행, DRAFT↔COMPLETED 토글=편집 성격): `if (!isAdmin() && !isAuthorOf(id)) return 403`.
  - `isAdmin()`/`getCurrentUser()` 기존(64·66행) 재사용.

- **workplan (`WorkPlanController`)** — 헬퍼 신설:
  ```java
  private boolean isOwnerOf(Integer id) {
      WorkPlan w = workPlanService.getWorkPlanById(id);
      return w != null && w.getCreatedBy() != null && getCurrentUser() != null
          && w.getCreatedBy().getUserSeq().equals(getCurrentUser().getUser().getUserSeq());
  }
  ```
  - `save`(POST `/save`, 231행): dto.id != null(update)일 때 `if (!isAdmin() && !isOwnerOf(id)) return 403`. create 제외.
  - `delete`(POST `/delete/{id}`, 261행, target 이미 268행 로드): `if (!isAdmin() && !isOwnerOf(id)) return 403`.
  - **(codex#4) `updateStatus`**(POST `/api/status/{id}`, 280행, 상태변경=편집 성격): `if (!isAdmin() && !isOwnerOf(id)) return 403`.
  - **(codex#3) `editForm` GET**(`/workplan/edit/{id}`, 204행): `admin||owner` 아니면 목록 redirect(비소유자 수정폼 진입 차단).

### P4 — T3 UI 버튼 노출 (FR-7) — 서버렌더 확인 완료
모든 도메인 버튼은 서버 Thymeleaf `th:if` 렌더(JS 동적생성 아님). 단 도메인별 편차 큼.

- **ops-doc**:
  - **상세**(`doc-fault.html:121-123`, `doc-support.html:146-148`): `th:if="${mode=='view' and canEdit}"` → `canEdit` 를 **소유권 포함**으로 좁힘. 컨트롤러 `detail`(296행)에서 `canEdit = isAdmin() || (hasDocEdit && createdBy==로그인ID)` 로 계산해 주입. 지원파일 편집 블록(`doc-support.html:118-126`)도 동일 변수 사용 → 자동 반영.
  - **목록**(`list.html:169`): 현재 관리 컬럼이 **`th:if="${isAdmin}"`(관리자 전용)**. EDIT 사용자는 목록에서 원래 버튼이 안 보이고 상세에서만 편집. **목록은 현행 유지**(관리자 전용) — 소유권식으로 바꾸면 EDIT 사용자에게 버튼이 새로 노출되는 **확장(회귀 위험)** 이라 지양. (편집 액션 표면은 상세로 일원화)
  - `doc-install.html`/`doc-patch.html`: view 모드 수정/삭제 버튼 자체가 미구현 → 범위 외.

- **document**:
  - **상세**(`document-detail.html:70`): `th:if="${userAuth=='EDIT'}"` → `th:if="${userAuth=='EDIT' and canManage}"`. 컨트롤러 `documentDetail`(122-142행)에서 `canManage = isAdmin() || (doc.authorId == 로그인유저.id)` 주입.
  - **목록**: 수정/삭제 버튼 없음(행 클릭→상세) → 변경 불필요.

- **workplan** ⚠ **작성자 데이터 프론트 부재 → 확장 선행**:
  - `WorkPlanDTO` 에 `createdById`(Long) 추가 + `fromEntity` 에서 `entity.getCreatedBy()?.getId()` 매핑. `CalendarEvent.ExtendedProps` 에 `createdById` 추가.
  - **캘린더**(`workplan-calendar.html:163-166`): 정적 팝오버 → JS 조건화. `showPopover`(327-329행 부근)에서 `ep.createdById === 현재유저Id || isAdmin` 일 때만 수정/삭제 표시. 인라인 `var currentUserId` 주입(195행 `userAuth` 옆).
  - **칸반**(`process-status.html:148`): 카드 onclick=수정진입. 비소유 카드는 클릭 시 서버가 403/redirect(P3 가드)로 차단되므로 **기능상 안전**. 카드 클릭 비활성까지는 `WorkPlanDTO.createdById` 로 `th:onclick` 조건분기(선택).

> **P4-workplan 범위 — 확정(2026-06-30 사용자)**: **캘린더 JS까지만 숨김**. `WorkPlanDTO.createdById` + `CalendarEvent.extendedProps.createdById` 추가 → 캘린더 팝오버 수정/삭제를 비소유자에게 숨김. **칸반 카드 클릭 비활성은 생략**(비소유 클릭 시 서버 가드가 403/redirect로 차단). 서버 가드가 보안 정본.

### P5 — 테스트 (NFR-2)
- `LsaControllerMvcTest` 확장: (a)본인 EDIT 수정/삭제 200·redirect, (b)타인 EDIT 403, (c)admin 타인건 OK, (d)create 무제한, (e)버전 datalist 옵션 렌더.
- ops-doc/document/workplan 각 가드 테스트: (b)타인 403, (c)admin OK, (a)본인 OK, create 무제한.
- 회귀: 기존 테스트 그린 유지.

### P6 — 검증·커밋
- `./mvnw test`(DB 가드 테스트는 슬라이스/모킹), codex 구현검증, 브라우저 QA(LSA 폼 datalist + 버튼 노출), 듀얼푸시.

---

## 3. 변경 파일 목록

| 파일 | 변경 |
|---|---|
| `templates/lsa/lsa-form.html` | 버전 datalist, 기본값 공란 |
| `templates/lsa/lsa-detail.html` | canEdit 소유권 + muted 힌트 |
| `templates/lsa/lsa-list.html` | 행 소유권 버튼 |
| `lsa/controller/LsaController.java` | isOwnerOf·checkEditOwnership + 적용, 모델 주입 |
| `controller/ops/OpsDocController.java` | requireOwnerOrAdmin + update/delete 적용 |
| `controller/DocumentController.java` | isAuthorOf + save(update)/delete 적용 |
| `controller/WorkPlanController.java` | isOwnerOf + save(update)/delete 적용 |
| (P4 템플릿) | §5 확정 후 |
| 테스트 4종 | 가드 케이스 |

**DDL/마이그레이션: 없음.**

---

## 4. 롤백
- 순수 코드 변경(스키마 무변경) → revert 로 즉시 원복. 데이터 영향 없음.

### 구현 완료 메모 (2026-06-30)
- P1~P5 구현 완료. 전체 테스트 **1625 green**(55 skip=DB-gated). 컴파일 OK.
- 컨트롤러 테스트 6종 보강(타인403/admin OK/본인OK/create무제한/버튼소유권). 본보기=ops-kb `ownsOrAdmin`.
- ⚠ **요청자/KB-feedback 제외(코드검증 후 조정)**: ops-doc `/api/requester`(새 PersonInfo 등록)·`/api/kb/feedback`(추천 피드백 로깅)는 특정 문서 docId 변경이 아니라 작성 보조라 소유권 대상 아님 — 개발계획의 "전부"에서 제외(과잉제한 회피).
- ⚠ **OpsDocController isAdmin(param) 추가**: 기존 무인자 isAdmin()은 SecurityContextHolder 의존이라 @AuthenticationPrincipal 파라미터 경로(이 컨트롤러 정본)에선 부정확 → `isAdmin(CustomUserDetails)` 오버로드 추가해 소유권 가드에 사용(hasDocEdit·requireDocEditOrAdmin 과 동일 param 기반).
- ⚠ **golden baseline 2종 갱신(기존 license4j 드리프트 정리)**: `controller-repo-arch-baseline.txt` 300→303, `endpoint-inventory.txt` +`POST /license/sync/run`. 둘 다 **어제 커밋된 License4J 스프린트(b397cb2: LicenseRegistryController→LicenseSyncHistoryRepository 직접주입, LicenseSyncController 신규 엔드포인트)**가 남긴 기존 드리프트로, 본 작업의 컨트롤러 변경은 repo참조·신규엔드포인트 0건(diff 확인). 본 커밋에서 현실로 reconcile.

### codex 구현검증 (⚠수정필요 2건 → 반영, 2026-06-30)
- **#1 ops-doc `kbFeedback`**: `form.docId() != null` 일 때 `requireOwnerOrAdmin` 추가(기존문서 연결 피드백은 작성자 본인|관리자, 신규작성 중 docId=null 은 무제한). 방어심층.
- **#2 document 수정 진입 `GET /document/create?docId=`(createForm)**: 비소유 EDIT 사용자가 수정폼을 여는 정보노출 차단(save API 와 일치). 가드를 **try 내부(로드 직후)**에 배치 → 로드실패 graceful catch 보존·이중조회 제거.
- 신규 가드 테스트 4종 추가(createForm 비소유 redirect·admin OK, kbFeedback 비소유 403·신규docId-null OK). 전체 **1629 green**. codex: LSA/ops-doc createdBy(String)·document/workplan getUserSeq() 판정 정확 확인.

---

## 5. T3 UI 버튼 렌더 방식 — 확정 (탐색 완료)
- 세 도메인 모두 **서버 Thymeleaf `th:if` 렌더** 확인(JS 동적 버튼 생성 없음). 세부 지점은 P4 참조.
- 핵심: **서버 가드(P3)가 보안 정본** → 버튼이 남아있어도 비소유 액션은 403/redirect 로 차단. P4 버튼 숨김은 UX 일관성 보조.
- 도메인 편차: ops-doc/document 상세는 작성자 데이터 가용(저렴), **workplan은 DTO/이벤트에 작성자 부재 → 확장 선행**(P4 workplan 범위 결정 참조).

---

## 6. codex 검토 결과 반영 (2026-06-30) — 판정 ⚠수정필요 → 전건 반영

| # | codex 지적 | 반영 |
|---|---|---|
| 1 | document/workplan owner 비교가 `getId()` — User PK는 `userSeq` | §1·P3: **`getUserSeq()`** 로 정정 (getId 없음) |
| 2 | LSA/ops-doc owner는 `CustomUserDetails.getUsername()`(로그인ID) 기준이어야, 실명(getDisplayName)과 혼용 금지 | §1 주석 명시 (계획 방향 일치 확인) |
| 3 | ops-doc·workplan **editForm GET** 도 소유권 가드 필요(PUT만 막으면 폼 노출) | P3: ops-doc 255행·workplan 204행 editForm `admin\|\|owner` 추가 |
| 4 | document/workplan **상태변경 API**, ops-doc **서브엔드포인트(첨부·요청자·KB)** 도 변경 작업 → 소유권 적용 결정 | P3: 상태변경(편집성격)=소유권 포함, ops-doc docId기반 변경 API 전부 `requireOwnerOrAdmin` 적용. (document 상태=DRAFT↔COMPLETED 토글이라 승인워크플로 아님→포함) |
| 5 | P4 workplan UI 미완 시 FR-7 부분미충족 | P4 workplan 범위 결정으로 명시(서버 가드 정본, 캘린더 JS까지 권고) |

> codex 종합: "**반려 아님, ⚠수정필요**". 위 1~5 전건 반영 완료. 잔여 = FR-7 workplan UI 범위(사용자 승인 사항).

### 핵심 원칙 재확인
**docId/planId로 특정 기존 레코드를 변경·삭제하는 모든 엔드포인트 = `admin || 작성자본인`**. 신규 작성(create)·조회(view)는 무제한(기존 권한 유지). 상태변경은 편집 성격이라 포함(document 토글·workplan 상태). 승인자≠작성자인 별도 승인 워크플로는 본 범위에 없음.
