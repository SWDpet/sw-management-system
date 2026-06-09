# 개발계획서 — 사업문서 날인본 스캔 업로드 (doc-signed-scan-upload)

- **상태**: DRAFT v1 (codex 검토 대기)
- **기획서**: `docs/product-specs/doc-signed-scan-upload.md` (v0.2, 승인됨 2026-06-09)
- **작성일**: 2026-06-09

---

## 0. 구현 원칙

- 기존 `DocumentAttachmentService`(범용 1:N 첨부) **재사용 금지** — 전용 서비스 신설(codex [필수]2).
- DB 변경은 **additive nullable** 컬럼만 → 기존 동작 무영향, 롤백은 UI 숨김 + 컬럼 방치.
- 보안 게이트(FR-8/9/10)는 **서버에서 강제**, 화면은 보조.

## 1. 구현 묶음 (Bundle)

### A. 도메인 · 스키마
1. **`Document.java`** — 필드 5개 추가:
   - `signedScanPath`(String, `signed_scan_path`), `signedScanOrigName`, `signedScanSize`(Long), `signedScanUploadedAt`(LocalDateTime), `signedScanUploadedBy` → `@ManyToOne(LAZY) User` (`signed_scan_uploaded_by`, author/approver 와 동일 관례).
2. **`db_init_phase2.sql`** — `tb_document` CREATE 블록 **뒤에** 멱등 ALTER 추가:
   ```sql
   ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_path        varchar(500);
   ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_orig_name   varchar(255);
   ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_size        bigint;
   ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_uploaded_at timestamp;
   ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_uploaded_by bigint REFERENCES users(user_id);
   COMMENT ON COLUMN tb_document.signed_scan_path IS '최종 날인본 스캔 PDF 절대경로 — doc-signed-scan-upload (2026-06-09)';
   ```
   (DbInitRunner 가 시작 시 실행 — IF NOT EXISTS 로 멱등.)
3. **`swdept/sql/V20260609_add_signed_scan_to_document.sql`** — 동일 ALTER + sprint/근거 COMMENT (운영 마이그레이션 경로 이중 반영, 기존 컨벤션).

### B. 설정 · 서비스
4. **`application.properties`** — `file.scan-dir=${DOC_SCAN_DIR:./uploads/scan}` 추가(§5 업로드 블록 근처). 운영 배포 가이드: OS 환경변수 `DOC_SCAN_DIR=D:\swmanager-scan`.
5. **`DocumentSignedScanService`(신규)** — 메서드:
   - `uploadOrReplace(Integer docId, MultipartFile file, Long uploaderSeq)`:
     - 문서 조회 → **게이트(FR-8)**: docType ∈ {COMMENCE,INTERIM,COMPLETION} && status==COMPLETED, 아니면 `IllegalState`.
     - **PDF 검증(FR-2)**: contentType `application/pdf` && 선두 매직바이트 `%PDF-`(스트림 앞 5바이트). 아니면 거부.
     - 경로 구성: `base/{projId}_{sanitize(projNm)}/{label(docType)}/{label}_{sanitize(projNm)}_{yyyyMMdd}.pdf`.
     - **경로 안전(FR-9)**: 최종 Path `normalize()` 후 `startsWith(base.normalize())` 아니면 거부.
     - **원자적 교체(FR-10)**: 동일 디렉토리 temp(`*.part`) 작성 → `Files.move(temp, target, ATOMIC_MOVE, REPLACE_EXISTING)`. 성공 후 **이전 `signedScanPath`(≠ target) 파일 삭제**. 실패 시 temp 삭제, DB 미변경.
     - DB 갱신: 5개 필드 set, save.
   - `delete(Integer docId, ...)`: 게이트 권한, 경로 normalize+startsWith 후 `deleteIfExists`, 필드 null.
   - `loadForDownload(Integer docId)`: 경로 가드 후 `FileSystemResource`/스트림 반환(+원본파일명).
   - `sanitize(String)`: 금지문자 `\\ / : * ? " < > |` 제거 + 제어문자 + trim + 공백 압축 + 길이 80 컷, 빈 결과 → `"사업미상"`.
   - `label(DocumentType)`: COMMENCE→착수계, INTERIM→기성계, COMPLETION→준공계.
   - 시작 시 base 디렉토리 보장(`createDirectories`).

### C. 컨트롤러 API (`DocumentController`)
6. 엔드포인트 3종(기존 첨부 API 아래에 신설):
   - `POST /document/api/signed-scan/upload/{docId}` — `getAuth()==EDIT` 게이트 → service.uploadOrReplace → `logService.log(DOCUMENT, UPLOAD, ...)`.
   - `GET /document/api/signed-scan/{docId}` — 로그인 사용자, `ResponseEntity<Resource>` + `Content-Disposition`(원본명 RFC5987 인코딩), `application/pdf`.
   - `POST /document/api/signed-scan/delete/{docId}` — `EDIT` 게이트 → service.delete → log.
   - 예외는 기존 패턴(403/500 JSON)과 동일.

### D. DTO · 목록 UI
7. **`DocumentDTO`** — `boolean hasSignedScan` 추가, `fromEntity` 에서 `doc.getSignedScanPath()!=null` 로 세팅. (status 는 이미 존재.) → `searchDocuments` 가 `fromEntity` 경유인지 확인하고, 아니면 투영에 두 값 채움.
8. **`document-list.html`** — 헤더에 `날인본` 컬럼 추가, 행에:
   - `status==COMPLETED && docType∈3종` 일 때만 컨트롤 노출.
   - 스캔 있음: ✔ 배지(`--success`) + [다운로드][교체][삭제]. 없음: [업로드] 버튼.
   - 모든 컨트롤 `onclick`/`event.stopPropagation()` 로 행 클릭(상세이동) 차단.
   - JS: `fetch` multipart 업로드(진행/실패 표시), 다운로드 `window.location`, 삭제 확인 모달. 토큰 색만 사용(다크모드 자동), `aria-label`.

### E. 테스트
9. **단위(집 PC 가능)**:
   - `sanitize` 엣지: 금지문자/제어문자/공백/길이80초과/빈문자→fallback.
   - 경로 가드: `..` 주입 시 `startsWith(base)` 거부.
   - 게이트: 非COMPLETED·非3종·권한없음 거부. PDF 아님(매직바이트 불일치) 거부.
   - 교체: 동일날짜 재업로드 시 원자적 교체 + 구파일 1개만 유지.
10. **통합(회사 PC/운영망)**: 실제 `D:\swmanager-scan` 쓰기·교체·다운로드, 권한, 다크모드 UI 육안.

## 2. 검증 계획 / NFR 게이트

| 게이트 | 집 PC | 회사 PC |
|---|---|---|
| 컴파일 (`mvnw -q -DskipTests compile`) | ✅ | ✅ |
| 단위테스트 (sanitize/path/gate/pdf) | ✅ | ✅ |
| 실 D드라이브 통합·운영 env | ❌(코드만) | ✅ |
| 다크모드/접근성 UI | 정적 | 실측 |

- **NFR-보안**: FR-8/9/10 단위테스트 통과 필수(차단 게이트).
- **NFR-무결성**: 교체 후 고아파일 0, DB 경로=실파일 일치.

## 3. 리스크 / 롤백

- 파일시스템 ↔ DB 불일치(수동 파일 삭제 등): 다운로드 시 미존재면 404 + "파일 없음" 안내, 관리자 재업로드로 복구.
- 백업: `D:\swmanager-scan` 는 pg_dump 밖 → **운영 별도 백업/NAS 동기화 필수**(운영가이드 등재).
- 롤백: 컬럼 additive·nullable → UI 숨김만으로 비활성, 스키마 영향 없음.

## 4. 진행 순서

A(도메인·스키마) → B(설정·서비스) → C(API) → D(DTO·UI) → E(테스트). 각 묶음 후 컴파일 확인. 전체 후 codex 구현물 검증 → 회사 PC 통합검증.
