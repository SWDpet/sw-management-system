# 기획서 — 사업문서 최종 날인본(스캔 PDF) 업로드·보관 (doc-signed-scan-upload)

- **상태**: v0.2 (codex 1차 검토 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-09 (v0.1) / v0.2 동일자 codex 반영
- **스프린트명**: `doc-signed-scan-upload`
- **요청자**: 박욱진 (사용자)
- **관련 선행**: `docs/product-specs/pdf-attachment-v1.md`(DRAFT, 저장백엔드 DB-bytea 로 검토됐으나 **본 건은 사용자 결정으로 파일시스템 채택** — §6 참조), 취소된 `pca-1`(PDF AI 자동입력, 무관)

---

## 0. 한 줄 요약

착수계·기성계·준공계의 **최종 도장 날인 원본을 스캔한 PDF** 를, 문서가 **작성완료(COMPLETED)** 되면 사업문서 목록에서 업로드해 **운영서버 D드라이브 폴더구조**에 보관한다. 문서당 1개(재업로드 시 교체).

---

## 1. 배경 / 목적

- 착수계·기성계·준공계는 실제 **도장 날인 종이문서가 법적 원본**이다. 시스템이 만든 HWPX/PDF 출력본과 별개로, 날인본 스캔을 보관해야 감사·정산·분쟁 대응이 된다.
- 현재 시스템엔 문서 작성·출력 기능은 있으나, **확정된 날인본을 사업별로 정리 보관**하는 통로가 없다.
- 운영자가 서버에서 **사람이 탐색 가능한 폴더 트리**로 스캔본을 찾을 수 있어야 한다(요청).

## 2. 범위 (In / Out)

**In**
- 대상 문서: `DocumentType` 중 **COMMENCE(착수계) / INTERIM(기성계) / COMPLETION(준공계)** 3종만.
- 문서당 **스캔본 1개**(PDF). 재업로드 시 **교체**(이전 파일 삭제 후 신규 저장).
- 업로드 가능 조건: 문서 `status = COMPLETED`.
- 저장: 운영서버 **D드라이브 파일시스템**, 사업→문서종류 폴더 트리.
- 사업문서 목록(`document-list.html`)에서 업로드·다운로드·교체.

**Out**
- 운영문서 5종(점검/장애/지원/설치/패치)은 대상 아님.
- AI/OCR 파싱·자동입력 없음(pca-1 폐기 취지 유지). 순수 보관.
- 다중 버전 이력 보존 안 함(문서당 1개 교체 정책).
- 전자서명/PKI 날인 검증 없음(스캔 이미지 보관만).

## 3. 사용자 스토리

> 담당자로서, 착수계가 작성완료되고 실제 날인본을 받으면, 그 스캔 PDF 를 해당 사업문서 행에서 올려두어 나중에 사업별 폴더에서 바로 찾고 싶다.

## 4. 기능 요구사항 (FR)

| ID | 요구사항 |
|---|---|
| FR-1 | 사업문서 목록의 각 행(착수/기성/준공)에, `status=COMPLETED` 인 경우에만 **[날인본 업로드]** 버튼 노출. DRAFT 면 비활성/숨김. |
| FR-2 | 업로드는 **PDF 만 허용**(MIME `application/pdf` + 매직바이트 `%PDF` 검증). 최대 크기 = 멀티파트 한도(현 100MB) 내 별도 한도(권고 30MB). |
| FR-3 | 저장 경로: `{base}\{proj_id}_{사업명}\{문서종류}\{문서종류}_{사업명}_{yyyyMMdd}.pdf`. `base` 는 환경설정값. |
| FR-4 | 이미 스캔본이 있으면 재업로드 시 **이전 파일 삭제 후 교체**, DB 경로 갱신. |
| FR-5 | 업로드된 스캔본은 목록/상세에서 **다운로드**(인증된 사용자) 및 **삭제** 가능. |
| FR-6 | 행에 스캔본 보유 여부 **배지/아이콘** 표시(있음/없음). |
| FR-7 | 권한(확정): 업로드·삭제 = **문서 편집권(`getAuth()==EDIT`) 사용자 + 관리자** (기존 관례 정합). 다운로드 = 로그인 사용자. |
| FR-8 (보안, codex [필수]) | **서버측 다중 게이트 강제**: ① docType ∈ {COMMENCE,INTERIM,COMPLETION} ② `status==COMPLETED` ③ 권한(FR-7). 화면 가림과 **별개로 API 에서 재검증**(우회 차단). |
| FR-9 (보안, codex [필수]) | **경로 안전**: 저장/다운로드/삭제 시 대상 경로를 `Path.normalize()`(+가능하면 `toRealPath()`) 후 **`startsWith(base)` 재확인**. 벗어나면 거부. |
| FR-10 (무결성, codex [필수]) | **원자적 교체**: 업로드는 temp 파일 작성 → `ATOMIC_MOVE`, 성공 후 **기존 파일 삭제 + DB 경로 갱신**. 실패 시 temp 정리·DB 미변경(롤백). |

## 5. 저장 설계 (파일시스템)

### 5-1. 폴더/파일명 규칙
```
{base}\
  {proj_id}_{사업명(정제)}\        예) 108_강진군UPIS유지보수
    착수계\  기성계\  준공계\
      착수계_강진군UPIS유지보수_20260609.pdf
```
- `base` = 운영 `D:\swmanager-scan` / 개발 `./uploads/scan` (환경변수 분기).
- **사업명 정제**: 경로 불가문자(`\ / : * ? " < > |`)·공백 trim·길이 제한(예 80자). null 이면 `사업미상`.
- 문서종류 라벨: COMMENCE→`착수계`, INTERIM→`기성계`, COMPLETION→`준공계`.
- 파일명 날짜 = 업로드일(yyyyMMdd). 교체 시 이전 경로(DB 보관)를 삭제.
- ⚠ **사업코드**: 현재 `sw_pjt` 에 사람용 사업번호 컬럼이 명확치 않아 우선 `proj_id` 사용. 별도 사업번호 필드 선호 시 교체(§9).

### 5-2. 경로 설정 (머신 분기)
- `application.properties`: `file.scan-dir=${DOC_SCAN_DIR:./uploads/scan}`
- 운영 배포: OS 환경변수 `DOC_SCAN_DIR=D:\swmanager-scan` (기존 `SERVER_PORT`/`DB_*` 환경변수 패턴과 동일, `docs/GIT_REMOTE_SETUP.md`·배포메모 라인).
- 서버 기동 시 base 폴더 존재 보장(없으면 생성), 쓰기권한 점검.

### 5-3. 안전
- Path traversal 방지: 정제 후 `base` 하위 정규화 경로인지 재검증(`Path.normalize().startsWith(base)`).
- 동시 교체 경쟁: 임시파일 쓰기 후 atomic move, 실패 시 롤백.

## 6. DB팀 가상 자문

**쟁점**: 스캔본 메타를 (A) 기존 `tb_document_attachment` 재사용 vs (B) `tb_document` 에 전용 컬럼 vs (C) 1:1 전용 테이블.

- 기존 `tb_document_attachment` 는 **1:N 범용 첨부**(file_path/size/mime/uploaded_at)라, "문서당 1개 최종 날인본"의 1:1·교체·완료게이트 의미를 강제하지 못함 → 혼선 위험.
- **권고: (B) `tb_document` 에 전용 nullable 컬럼 추가** (additive·무위험).

```sql
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_path        varchar(500);
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_orig_name   varchar(255);
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_size        bigint;
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_uploaded_at timestamp;
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_uploaded_by bigint REFERENCES users(user_id); -- author_id/approver_id 와 동일 관례. 코드에선 User.getUserSeq()(=user_id 값) 로 세팅 (codex 질의 확정)
COMMENT ON COLUMN tb_document.signed_scan_path IS '최종 날인본 스캔 PDF 절대경로 — doc-signed-scan-upload (2026-06-09)';
```
- 1:1·교체 정책에 가장 단순. blob 미저장(파일시스템) → DB 비대 없음.
- 마이그레이션: `db_init_phase2.sql` 의 `tb_document` 정의에 컬럼 추가(멱등 `IF NOT EXISTS`) + `swdept/sql/V{날짜}_add_signed_scan_to_document.sql` 양쪽(기존 컨벤션).
- **백업 경고(중요)**: 파일시스템 저장이므로 스캔본은 **pg_dump 에 안 잡힘**. → 운영 `D:\swmanager-scan` **별도 백업/NAS 동기화 루틴 필수**(운영가이드에 명시). 선행 `pdf-attachment-v1` 이 DB-bytea 를 택했던 핵심 이유가 이 백업 일원화였음 — 파일시스템 채택의 트레이드오프를 운영 측에서 수용하는 전제.

## 7. 디자인팀 가상 자문 (A+D 정책 — UI 변경 트리거)

- **목록 행 액션**: COMPLETED 행에 작은 버튼/아이콘. 스캔본 유무를 토큰 기반 배지로 — 있음 `--success`, 없음 `--text2`(중립). 색 직접지정 금지, `design-system.css` 토큰 사용(`a0f1868` :root self-reference 버그 패턴 주의).
- **상태 일관성**: 기존 doc_type 배지(COMMENCE 파랑/INTERIM 노랑/COMPLETION 녹색)와 색 충돌 없이, 스캔 배지는 아이콘(📎/✔) 중심.
- **다크모드**: 토큰 사용으로 자동 대응. 신규 색 추가 시 다크 변수도.
- **접근성**: 버튼 `aria-label`("착수계 날인본 업로드"), 업로드 진행/실패 상태 텍스트, 키보드 포커스.
- **업로드 UX**: 인라인 파일선택 → 진행률 → 완료 시 배지 갱신(페이지 새로고침 없이). 교체 시 "기존 파일을 교체합니다" 확인.
- 모달 vs 인라인: 행 수 많을 수 있어 **인라인 경량 업로드** 권고(라이선스/QR 업로드 패턴 참고).

## 8. 비기능 / 보안

- 다운로드는 `StreamingResponseBody` + 인증·권한 체크, `Content-Disposition`.
- 운영은 nginx 443 종단(이미 HTTPS) 뒤이므로 업로드/다운로드 전송 암호화 충족(`X-Forwarded-Proto https`).
- 멀티파트 한도 100MB 유지, 앱 레벨 PDF 30MB 권고 한도.
- 감사로그: 업로드/교체/삭제를 access_logs 에 기록(기존 LogService 패턴).

## 9. 결정 사항 (확정)

1. **권한 범위**(FR-7): **EDIT 사용자 전체 + 관리자** (2026-06-09 사용자 확정 — 기존 관례).
2. **폴더 사업 식별자**: `proj_id` 사용 (확정).
3. **허용 포맷**: **PDF 단일** (`application/pdf` + 매직바이트). JPG/PNG 묶음 미지원.
4. **파일명 날짜**: 업로드일(yyyyMMdd) 기준, 교체 시 구파일 삭제 (확정).
5. **`signed_scan_uploaded_by`**: `users(user_id)` FK, `User.getUserSeq()` 로 세팅 (codex 질의 확정).

## 9-2. 게이트 변경 (2026-06-09, 배포 후)

운영 적용 후 발견: 착수/기성/준공 **사업문서엔 시스템상 COMPLETED 로 전환하는 UI 가 없음**(점검내역서·업무계획엔 있으나 사업문서엔 `/document/api/status` 호출 화면 부재). 따라서 COMPLETED 게이트(FR-1·FR-8 ②) 하에선 업로드 버튼이 영영 안 뜸. → **사용자 결정으로 COMPLETED 게이트 제거** = 착수/기성/준공 행이면 상태 무관 항상 업로드 가능. (docType 3종 + 권한 게이트는 유지.) 실무상 "종이 날인본 준비되면 올림" 흐름과 일치.

## 9-1. codex 1차 검토 (2026-06-09)

- **판정**: ⚠ 수정필요 → **방향 승인 가능**("파일시스템 + tb_document 전용 컬럼"). [필수] 지적의 대부분은 "아직 코드 미구현"으로, 기획서 단계 특성상 개발계획·구현에서 충족 예정.
- **반영(스펙)**: FR-8(서버측 다중 게이트), FR-9(경로 normalize+startsWith), FR-10(원자적 교체+롤백) 신설. `signed_scan_uploaded_by` FK 관례 명시. 권한 범위 확정.
- **개발계획서로 이월**: 엔티티/DDL/DTO 동시 반영, 전용 서비스(기존 DocumentAttachmentService 재사용 금지), `file.scan-dir` 프로퍼티, `swdept/sql/V*` 이중 반영, 파일명 정제 엣지 테스트(길이·빈문자·동일날짜 교체).

## 10. 다음 단계

1. 본 기획서 **codex 검토** → 사용자 최종승인.
2. 승인 후 **개발계획서**(`docs/exec-plans/`) 작성 — 묶음(엔티티/마이그레이션 → 서비스 → 컨트롤러 → 목록 UI → 테스트).
3. 구현·검증은 파일 I/O 까지는 집 PC 가능하나, **운영 D드라이브 실저장 검증은 회사 PC/운영망**에서.
