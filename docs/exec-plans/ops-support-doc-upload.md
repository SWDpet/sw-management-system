# 업무지원 문서 업로드 + 지원 필드 개편 — 개발계획서

> 상태: **DRAFT v0.2** (codex 검토 반영 / 사용자 최종승인 대기)
> 기획서: `docs/product-specs/ops-support-doc-upload.md` (v0.3, 승인됨 2026-06-16)
> 패턴 원본: `DocumentSignedScanService` / `DocumentController` signed-scan API (L851~913)
> v0.2 변경: codex 검토(중요 6/사소 3) 반영 — docId=Long, section_data 조회 경로, region/system repo 직접 주입, /ops-doc/api/** CSRF 면제 추가, ops-doc 권한 헬퍼 실태(requireDocEdit), 컨트롤러/시큐리티 테스트 추가.

---

## 0. 원칙

- **착수계 날인본 패턴 최대 이식**: 경로안전·매직바이트·원자교체·롤백 로직은 `DocumentSignedScanService` 를 그대로 본떠 `OpsSupportFileService` 신규 작성(코드 중복이나 도메인 분리가 안전 — 스캔도 범용 첨부와 분리한 선례).
- **additive·비파괴**: 기존 SUPPORT 조회/저장 흐름 유지. 신규 컬럼·키만 추가.
- 각 단계는 빌드 통과 후 다음 단계. 마지막에 통합 스모크(회사 PC 실 `D:\swmanager-scan`).

## Step 1 — DB 스키마 (FR-1·§5)

**파일**: `src/main/resources/db_init_phase2.sql`
- `tb_ops_doc` 생성/후속 ALTER 블록(engineer_id 근처) **이후**에 `support_file_*` 6컬럼 ALTER + COMMENT + 명시 FK명(`fk_ops_doc_support_uploader`) 추가(기획 §5 DDL 그대로).
- 멱등(`ADD COLUMN IF NOT EXISTS`) 확인. 운영 DB(192.168.10.194:5880)는 앱 기동 시 `DbInitRunner` 가 실행.

**검증**: 앱 기동 → 컬럼 존재 확인(`\d tb_ops_doc`).

## Step 2 — 엔티티 (FR-1·§5)

**파일**: `src/main/java/com/swmanager/system/domain/ops/OpsDocument.java`
- `support_file_path/orig_name/ext/size/uploaded_at` 필드 + `@ManyToOne User supportFileUploadedBy(@JoinColumn "support_file_uploaded_by")` 추가(스캔 `Document` 의 signed_scan_* 매핑 미러). 전부 nullable.

**검증**: 컴파일.

## Step 3 — 파일 서비스 (FR-2·3·4·5·6·9·10)

**신규**: `src/main/java/com/swmanager/system/service/ops/OpsSupportFileService.java` (`DocumentSignedScanService` 로직 이식 — 코드 복사가 아니라 ops 도메인에 맞춰 재작성)
- `@Value("${file.scan-dir:./uploads/scan}")` 재사용(스캔과 동일 루트).
- **[codex 중요] `docId` 는 `Long`** — `OpsDocument`/`OpsDocumentRepository` 가 Long 기반. 스캔의 `Integer` 그대로 복사 금지.
- 주입: `OpsDocumentRepository`, `OpsDocumentDetailRepository`, `SigunguCodeRepository`, `SysMstRepository`, `UserRepository`.
- `ALLOWED_EXT = {hwp,hwpx,xls,xlsx,doc,docx,pdf}` (소문자 normalize, FR-3 사소).
- 매직바이트 그룹 검증(FR-5): `PDF(%PDF-)` / `ZIP(50 4B 03 04)` / `OLE2(D0 CF 11 E0 A1 B1 1A E1)`. 확장자→기대 시그니처 매핑. 0byte·시그니처보다 짧은 파일·read 실패·빈 ZIP 거부.
- 경로: `base/업무지원/{년도}/{시스템}/{시도}/{시군구}/{지원대상}_{docNo}.{ext}`.
  - 년도: section_data `request_date` 연도 → `createdAt` 연도 → `연도미상`.
  - 시스템: `sysType` → **`SysMstRepository.findById(sysType)` 직접 조회** → `nm` → `시스템미상`.
  - 시도/시군구: `regionCode` → **`SigunguCodeRepository.findById(regionCode)` 직접 조회** → `getSidoNm()/getSggNm()` → `시도미상/시군구미상`. ([codex 중요] `OpsDocController.resolveRegion()` 은 private·리스트용 — 코드 재사용 아닌 repo 조회 재구현.)
  - `지원대상`: section_data `support_target`, `seg()` 정제 후 빈값이면 `지원대상미상`.
  - `seg()/sanitize()` 스캔 동일.
- **[codex 중요] section_data 조회**: `OpsDocumentDetailRepository.findByDocument_DocIdOrderBySortOrderAsc(Long)` 로 detail 목록 로드 → `sectionKey == "main"` row 선택 → `getSectionData()` Map 에서 `support_target`/`request_date` 읽기.
- `requireSupport(OpsDocument)`: `docType == SUPPORT` 아니면 `IllegalStateException` (FR-8 타입가드, 스캔 `requireAllowed` 미러).
- `uploadOrReplace / delete / loadForDownload / originalName` 4 메서드(docId=Long). 원자교체+백업+롤백(FR-10), `@Transactional(rollbackFor=Exception.class)`. 실패 경로(FR-10)도 로그 남김(사소).

**검증**: 컴파일 + Step 7 단위테스트.

## Step 4 — 컨트롤러 API + 시큐리티 (FR-7·8)

**파일**: `src/main/java/com/swmanager/system/controller/ops/OpsDocController.java`
- signed-scan(DocumentController L851~913) 미러로 3 엔드포인트 신규:
  - `POST /ops-doc/api/support-file/upload/{docId}` (multipart `file`) — EDIT 가드 + 타입가드, `uploadOrReplace`.
  - `GET  /ops-doc/api/support-file/{docId}` — 조회권한 가드, `loadForDownload`. Content-Disposition `filename*=UTF-8''{enc}`, contentType=확장자별(application/octet-stream 기본).
  - `POST /ops-doc/api/support-file/delete/{docId}` — EDIT 가드, `delete`.
- **[codex 중요] 권한 헬퍼 실태**: ops-doc 에는 `canView/canEdit` 헬퍼가 없고 편집은 `requireDocEdit`(OpsDocController L371)·`getAuth()` 만 있다. → 업로드/삭제는 `"EDIT".equals(getAuth())` 가드(signed-scan 동일), 다운로드는 **`authDocument != NONE 또는 ADMIN`** 조회 가드를 명시적으로 신규 구현(없으면 추가). 접근 로그(`logService`) 추가.

**파일**: `src/main/java/com/swmanager/system/config/SecurityConfig.java`
- **[codex 중요] `/ops-doc/api/**` 는 현재 CSRF 면제 목록에 없음**(현재 `/document/api/**`,`/ops-kb/api/**` 등만). 신규 multipart POST/delete 가 fetch 패턴이면 403 위험 → 면제 목록에 `/ops-doc/api/**`(또는 더 좁게 `/ops-doc/api/support-file/**`) 추가(KB 때 방식과 동일). **인가 규칙·권한 가드는 약화하지 않음**.

**검증**: 컴파일 + 헤드리스 업로드/다운로드/삭제/403/타입거부 스모크.

## Step 5 — 필수키 검증 (FR-1) — **Step 6 과 함께 검증** (codex 사소)

**파일**: `src/main/java/com/swmanager/system/service/ops/OpsDocService.java`
- `REQUIRED_SECTION_KEYS` 의 `SUPPORT` 집합에 `support_target` 추가.
- **[codex 사소] FE 결합**: 이 변경은 `doc-support.html` payload(`support_target` 전송) 변경과 강결합 → Step 6 의 FE 변경과 **함께 검증**해야 안전(단독 적용 시 기존 폼 저장이 깨짐). 현재 폼은 section 전체를 전송하므로 구조는 정합.

**검증**: 신규 저장 시 support_target 누락 → 400, 정상 → 201. 레거시 조회 무영향.

## Step 6 — 프론트엔드 (UI §6, 🎨 반영)

**파일**: `src/main/resources/templates/ops-doc/doc-support.html`
- 지원 정보: select 라벨 "지원 대상" → **"지원 구분"**.
- 지원 내용 섹션 상단: `<th>지원 대상(문서명) *</th><td><input id="support_target" placeholder="예: ○○시 사업수행계획서"></td>`. saveSupport() section_data 에 `support_target` 추가.
- 신규 **"지원 문서"** 섹션: `th:if="${isEdit}"` 게이팅. 미저장 시 안내 문구(`.ops-attachment-area` dashed 박스). 파일 입력 + 업로드 버튼 + `.ops-file-chip`(현재 파일: 파일명·크기·다운로드·삭제).
- 편집 진입 시 기존 파일 메타 표시(컨트롤러 detail/edit 모델에 support_file_* 주입).

**파일**: `src/main/resources/static/css/ops-doc.css`
- `.ops-file-chip` 신설(`.ops-partner-chip` 패턴 본뜸, `--ops-*` 토큰). 다운로드=`--success`/삭제=`--danger`(전역 토큰 차용). `min-width:0`+파일명 `text-overflow:ellipsis`(m-5).

**JS** (doc-support.html `<script>` 또는 인라인): 업로드 fetch(multipart) — 진행 중 버튼 비활성+"업로드 중…"(m-2), 실패 시 인라인 에러, 성공 시 칩 갱신. 삭제 `confirm()`(m-4). 아이콘 버튼 `aria-label`(m-3).

**🎨 디자인 자문 재확인**: 칩/토큰 변경 시 DesignSync 또는 가상 디자인팀 1회 더 확인(토큰·다크모드·specificity).

**검증**: 브라우저 — 신규 폼(업로드 비활성+안내), 저장 후 업로드/칩/다운로드/삭제, 다크모드.

## Step 7 — 테스트

**신규**: `OpsSupportFileServiceTest`
- 경로 조립(년도/시스템/시도/시군구/지원대상_docNo), self-row 시군구.
- 형식: 7종 허용 + 위조(.pdf인데 ZIP) 거부 + 0byte/짧은파일/빈ZIP 거부 + 30MB 초과.
- 충돌: 같은 지원대상 다른 docNo → 다른 파일. 같은 docId 재업로드 → 교체+고아 없음.
- 경로안전: traversal/symlink 차단.
- 타입가드: 비-SUPPORT docId 거부.
- 롤백: move 실패 시뮬레이션 시 메타 불변.
- sanitize 빈값 → 지원대상미상 fallback.

**신규**: `OpsDocSupportFileControllerTest` (**[codex 중요] 서비스 단위만으론 AC-6/7/10/11 미커버**)
- AC-7 권한: VIEW 사용자 업로드/삭제 → 403. EDIT → 200.
- CSRF 면제: multipart POST 가 403 아님(SecurityConfig 편입 확인).
- AC-8 타입가드: 비-SUPPORT docId → 거부.
- AC-6 다운로드: Content-Disposition `filename*=UTF-8''` 한글 인코딩 응답.
- (가능 범위) AC-10 신규폼 게이팅·AC-11 레거시 수정 필수강제는 MockMvc/서비스 조합으로.

**검증**: `./mvnw -o test -Dtest=OpsSupportFileServiceTest,OpsDocSupportFileControllerTest`.

## Step 8 — 통합 스모크 (회사 PC)

- 실 `D:\swmanager-scan` 에 업무지원 트리 생성 확인(한글/엑셀/pdf 각 1건), 다운로드 원본명, 삭제 후 파일 제거.
- AC-1~12 수동 점검.

## 검증 매트릭스 (기획 §11 AC ↔ Step)

| AC | Step |
|---|---|
| AC-1 업로드 성공 | 3·4·6·8 |
| AC-2 형식거부 / AC-3 용량 | 3·7 |
| AC-4 교체 / AC-5 삭제 / AC-6 다운로드 | 3·4·7·8 |
| AC-7 권한 / AC-8 타입가드 | 4·7 |
| AC-9 경로안전 / AC-12 롤백 | 3·7 |
| AC-10 신규폼 게이팅 / AC-11 레거시수정 | 5·6·8 |

## 작업 순서 / 의존

Step 1 → 2 → 3 → (4 ∥ 5) → 6 → 7 → 8. Step 6 전 🎨 재확인. 완료 시 codex 구현검증 → "작업완료" → commit+push(듀얼).

## 리스크 (기획 §9 승계)

레거시 부분수정 회귀(전제: 폼 full-section) · docNo 채번 동시성 · 매직바이트 계열 수준 검증 · 동시 재업로드 last-write-wins.
