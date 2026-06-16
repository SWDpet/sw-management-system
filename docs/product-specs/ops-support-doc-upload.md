# 업무지원 문서 업로드 + 지원 필드 개편 — 기획서

> 상태: **DRAFT v0.3** (DB팀·디자인팀·codex 검토 반영 / 사용자 최종승인 대기)
> 작성일: 2026-06-16
> 관련: doc-signed-scan-upload(파일 저장 패턴 원본) · ops-fault-support-improvement(상위 도메인) · doc-split-ops(ops-doc 분할)
> v0.2 변경: 가상 DB팀(중요 2/사소 4)·디자인팀(치명 1/중요 3/사소 5) 자문 반영 — §3·§5·§6·§8·§9 정정.
> v0.3 변경: codex 검토(중요 5/사소 3) 반영 — FR-5 빈/짧은파일, FR-8 SUPPORT 타입가드, FR-10 실패케이스, FR-3 sanitize fallback/확장자 normalize, §11 수용기준 신설.

---

## 1. 배경 / 목적

업무지원(`OpsDocType.SUPPORT`) 실무의 상당수가 **정보화시스템 산출물 문서 지원**(사업수행계획서·주간/월간보고·매뉴얼 등 한글/엑셀 작성·검토)이다. 현재 업무지원 화면은 텍스트 입력만 가능해 **지원한 실제 문서 파일을 시스템에 남길 수 없다**. 이를 개선해:

1. 업무지원 건에 **실제 지원 문서 파일(한글·엑셀·워드·PDF)을 1건 첨부·교체·다운로드**한다.
2. "무엇을 지원했는가"를 식별하는 **`지원대상`(한 줄)** 필드를 신설하고, 기존 텍스트 필드를 정돈한다.

파일 저장은 **착수계/기성계/준공계 날인본 스캔(`DocumentSignedScanService`)과 동일한 파일시스템 저장 패턴**(경로 안전·원자적 교체·롤백)을 따른다(운영 `D:\swmanager-scan`).

## 2. 범위

- **대상**: 업무지원(SUPPORT) 문서 한정. FAULT/INSTALL/PATCH/INSPECT 는 변경 없음.
- **제외**: 다중 파일 첨부(1:1 단일 파일), 파일 내용 파싱/미리보기, AI 분석.

## 3. 용어 / 필드 개편 (확정)

| 위치 | 현재 | 변경 후 | 비고 |
|---|---|---|---|
| 지원 정보 섹션 | `support_target_type` select, 라벨 **"지원 대상"**(지자체/자사조직) | 라벨 **"지원 구분"** 으로 개칭 | 데이터/값(EXTERNAL/INTERNAL) 불변, 라벨만 변경 |
| 지원 내용 섹션 | — | **`지원대상`** (한 줄 입력, 필수) **신설** | 파일명 소스. `section_data.support_target` |
| 지원 내용 섹션 | `support_content` (textarea) **"지원 내용"** | 그대로 유지 | 상세 설명 |
| 지원 내용 섹션 | `notes` (textarea) **"비고"** | 그대로 유지 | 용도 불명이나 존치(데이터 보존) |

> 결정 근거: 파일명이 `지원대상_{docNo}.확장자` 규칙이라 `지원대상`은 **짧은 한 줄**이어야 함. 따라서 기존 textarea(`support_content`)는 상세설명으로 두고, 한 줄 `지원대상`을 신설.
>
> **[디자인 M-3]** "지원 구분"(select)과 "지원대상"(텍스트)이 한 화면에 공존해 의미 혼동 여지가 있으므로, 신규 한 줄 필드는 라벨 **"지원 대상(문서명)"** + placeholder **"예: ○○시 사업수행계획서"** 로 "파일명이 되는 짧은 식별자"임을 명확히 한다.

## 4. 기능 요구사항 (FR)

### FR-1 필드 개편 + 검증 정책
- §3 표대로 라벨/필드 적용. `지원대상`은 **필수**(`REQUIRED_SECTION_KEYS[SUPPORT]` 에 `support_target` 추가).
- **[DB 중요①] 레거시 회귀 방지 — 검증 정책 명문화**: `OpsDocService.update()` 는 `sectionData` 유무와 무관하게 `validateSectionData` 를 호출하고, 검증은 *전달된* section 의 필수키를 본다. SUPPORT 폼은 저장 시 **항상 section 전체를 재전송**(`request_date`·`request_channel`·`requester`·`support_content`·`support_target`·`notes`)하므로 부분수정으로 인한 누락은 발생하지 않는다. 이 **"폼 full-section 전송"** 전제를 본 스프린트의 불변식으로 명문화한다. (상태만 바꾸는 별도 부분수정 경로는 SUPPORT 에 없음 — 확인 완료.)
- 기존 SUPPORT 데이터는 `support_target` 미보유 → **조회는 정상**(`section_data` jsonb, 키 강제 없음), 다음 **수정 저장 시 입력 강제**. 일괄 백필 불필요.
- **[codex 중요] 착수 전 검증 항목**: 구현 1단계에서 `doc-support.html` 실제 submit payload 와 `OpsDocService.update()` 호출 경로가 section 전체를 전송함을 코드로 재확인(회귀 방지). 향후 AJAX 부분수정/상태변경 API 추가 시 필수값 검증 회귀 가능성을 별도 가드로 처리.

### FR-2 파일 업로드 (형식)
- 허용 확장자: **`.hwp` `.hwpx` / `.xls` `.xlsx` / `.doc` `.docx` / `.pdf`**.
- 단일 파일 1:1(문서당 1개). 재업로드는 교체.
- 최대 용량 30MB(스캔 패턴과 동일).

### FR-3 저장 경로 / 파일명
- base = `${DOC_SCAN_DIR:-./uploads/scan}` (운영 `D:\swmanager-scan`) — 스캔과 동일 루트 재사용.
- 폴더 트리: `업무지원/{년도}/{시스템}/{시도}/{시군구}/`
  - **년도**: `요청일(request_date)` 연도 → 없으면 `createdAt` 연도 → 둘 다 없으면 `연도미상`.
  - **시스템**: `sys_type` → `sys_mst.nm`(국문명) → 미상 시 `시스템미상`.
  - **시도/시군구**: `region_code`(행정구역코드 `adm_sect_c`, 5자리) → `SigunguCodeRepository.findById` **단일 1회 조회로 시도·시군구 두 레벨 동시 해석**(`getSidoNm()`/`getSggNm()`) → 미상 시 `시도미상`/`시군구미상`.
  - **[DB 사소] self-row 주의**: 도청/본청 등은 `sgg_name == sido_name` self-row → `{시도}/{시군구}` 가 동명 중첩(예 `강원특별자치도/강원특별자치도/`). 버그 아님, 의도된 동작으로 둔다.
- 파일명: `{지원대상}_{docNo}.{확장자}` (예: **`지원대상_SUP-2026-42.hwpx`**).
  - **[DB 중요②] docNo 는 무패딩 정수** 형식(`SUP-{yyyy}-{n}`, 예 `SUP-2026-42` / `SUP-2026-1`). zero-pad 아님.
  - 각 세그먼트·파일명은 스캔의 `seg()`/`sanitize()` 동일 규칙(금지문자 `\/:*?"<>|`·제어문자 제거, 공백압축, 80자 컷).
  - **[codex 사소] `support_target` sanitize 후 빈 문자열**(금지문자·공백만 입력)이면 파일명 세그먼트는 `지원대상미상` fallback. 단 FR-1 에서 `support_target` 은 필수·trim 후 비어있으면 저장 자체를 거부하므로 정상 흐름에선 도달하지 않음(방어적 fallback).
  - **[codex 사소] 확장자 정규화**: 대소문자 무시(`.PDF`/`.HwPx` 허용) → 메타·파일명은 **소문자**로 normalize 저장.

### FR-4 파일명 충돌
- 같은 폴더 내 동일 `지원대상`이라도 `docNo` 접미사로 분리되어 **다른 문서의 파일을 덮어쓰지 않는다**(확정).
- `docNo` 는 `tb_ops_doc.doc_no` UNIQUE 제약 + 파일은 docId 확정(저장) 후 업로드 → 유일성 보장.
- 같은 문서 재업로드는 같은 `docNo` 경로 → 의도된 1:1 교체.

### FR-5 형식 검증 (확장자 + 매직바이트)
- 확장자 화이트리스트 + **선두 매직바이트** 1차 검증으로 위조 차단:
  - PDF: `25 50 44 46 2D` (`%PDF-`)
  - hwpx / xlsx / docx: ZIP(`50 4B 03 04`) — OOXML/OWPML 컨테이너
  - hwp / xls / doc: OLE2 복합문서(`D0 CF 11 E0 A1 B1 1A E1`)
- 확장자와 매직바이트 **계열**이 불일치하면 거부(예: `.pdf` 인데 ZIP/OLE 시그니처). ZIP/OLE 계열 내 정밀 형식 판별은 확장자 화이트리스트 신뢰(§9-3 수용).
- **[codex 중요] 엣지케이스 거부**: 0바이트 파일, 시그니처 최소 길이(OLE2 8B)보다 짧은 파일, read 실패 → 즉시 거부. 빈/비정상 ZIP(`50 4B 05 06` 빈 ZIP 등 엔트리 없는 컨테이너)은 정상 문서가 아니므로 ZIP은 **`50 4B 03 04`(엔트리 보유)만 허용**.
- contentType(MIME)은 보조 신호로만 사용(브라우저별 편차 큼).

### FR-6 교체 / 삭제
- 업로드 = 신규 or 교체(이전 파일 정리, 고아 방지). 삭제 = 파일 + 메타 제거.
- 원자적 교체 + 실패 시 롤백(스캔 FR-10 동일: DB flush 선행 → temp 작성 → 기존 백업 → ATOMIC_MOVE → 실패 시 원복).

### FR-7 다운로드
- 원본 파일명(`support_file_orig_name`)으로 다운로드. Content-Disposition 한글 파일명 인코딩(RFC 5987) 처리.

### FR-8 권한 (가드 — 프로젝트 정책)
- 조회/다운로드: 업무지원 문서 **조회 권한**(`authDocument != NONE` 또는 ADMIN).
- 업로드/삭제: **편집 권한**(`authDocument == EDIT` 또는 ADMIN). ops-doc 기존 정책과 동일.
- 신규 API(`/ops-doc/api/support-file/**`)는 `SecurityConfig` 인가 규칙·CSRF 정책에 기존 `/document/api/**` 패턴과 동일하게 편입.
- **[codex 중요] SUPPORT 타입 가드**: 업로드/다운로드/삭제 시 대상 문서가 `OpsDocType.SUPPORT` 인지 서버에서 확인(아니면 거부). 스캔의 `requireAllowed()` 패턴과 동일하게, FAULT/INSTALL/PATCH/INSPECT 문서에 지원문서를 붙이는 오용 차단.

### FR-9 경로 안전
- traversal/symlink 우회 차단: `dir.startsWith(base)` + `toRealPath().startsWith(base.toRealPath())` (스캔 FR-9 동일). 다운로드/삭제 모두 재검증.

### FR-10 원자성/일관성
- `@Transactional(rollbackFor = Exception.class)`. DB 메타와 실제 파일 정합 보장(스캔 서비스 로직 이식).
- **[codex 중요] 실패 케이스 명문화**(스캔 패턴 그대로 이식 + 본 도메인 적용):
  - DB flush 선행 → temp 작성 → 기존 target 백업(`.bak-{nanoTime}`) → ATOMIC_MOVE. move 실패 시 백업 원복 후 예외 → `rollbackFor=Exception` 으로 DB 롤백 → 메타/파일 불일치 없음.
  - 파일 이동 성공 후 커밋 실패: 트랜잭션 롤백으로 메타 미반영. 신규 파일은 다음 동일 docId 업로드 시 같은 경로로 덮어쓰이거나, 백업 원복 로직으로 정리(고아 잔존 시 무해 — 메타 없으면 다운로드 불가).
  - **동일 docId 동시 재업로드 경쟁**: "마지막 요청 우선"(last-write-wins). docId 단위 직렬화는 본 스프린트 범위 밖(실무상 단일 작성자 편집이라 경쟁 희박) — 리스크로 인지.

## 5. 데이터 모델 변경

`tb_ops_doc` 에 단일 파일 메타 컬럼 신설(스캔 `signed_scan_*` 미러, additive·nullable). **배치 위치: `db_init_phase2.sql` 의 `tb_ops_doc` 생성·후속 ALTER 블록(engineer_id 등) 근처**(tb_ops_doc 정의 이후).

```sql
-- [ops-support-doc-upload] 업무지원 지원문서 단일파일 메타 (additive, nullable) — 2026-06-16
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_path        VARCHAR(500);
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_orig_name   VARCHAR(255);
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_ext         VARCHAR(10);
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_size        BIGINT;
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_uploaded_at TIMESTAMP;
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_uploaded_by BIGINT
       CONSTRAINT fk_ops_doc_support_uploader REFERENCES users(user_id);  -- [DB 사소] 명시적 FK명
COMMENT ON COLUMN tb_ops_doc.support_file_path      IS '업무지원 지원문서 절대경로(파일시스템) — ops-support-doc-upload (2026-06-16)';
COMMENT ON COLUMN tb_ops_doc.support_file_orig_name IS '업로드 원본 파일명';
COMMENT ON COLUMN tb_ops_doc.support_file_ext       IS '확장자(hwp/hwpx/xls/xlsx/doc/docx/pdf)';
```

- `지원대상`/`지원내용`/`비고`는 **신규 컬럼 없이** 기존 `OpsDocumentDetail.section_data`(jsonb, `main` row) 키로 저장: `support_target`(신규), `support_content`(기존), `notes`(기존).
- 범용 `OpsDocumentAttachment`(다대다)와 **분리** — 1:1 전용이라 스캔과 같은 dedicated 방식이 단순·안전.
- 인덱스 불필요(검색축 아님).

## 6. UI 변경 (🎨 디자인 자문 반영)

**[디자인 C-1] 상세 == 수정 동일 템플릿 구조 전제.** `doc-support.html` 하나가 신규(`!isEdit`)·수정/상세(`isEdit && docId`)를 `isEdit` 플래그로 분기한다(별도 읽기전용 상세뷰 없음). 따라서:

- **지원 정보 섹션**: select 라벨 "지원 대상" → **"지원 구분"**.
- **지원 내용 섹션**: 상단에 한 줄 **"지원 대상(문서명) *"** 입력 추가(placeholder 예시). 그 아래 기존 "지원 내용"(textarea) · "비고"(textarea) 유지.
- **신규 "지원 문서" 섹션 (업로드)** — 동일 폼 내에 배치하되 **`isEdit && docId` 일 때만 업로드/칩 UI 활성**. 신규 작성 중(`!isEdit`)에는 *"문서를 먼저 저장한 뒤 지원 문서를 첨부하세요"* 안내만 노출(파일명에 `docNo` 필요 — FR-3).
- **[디자인 M-1] 칩 컴포넌트 신설**: 착수계 날인본 UI(`document-list.html` 인라인 스타일·테이블셀 버튼)는 형태가 다르고 재사용 불가 → `ops-doc.css` 에 **`.ops-file-chip` 신설**(`.ops-partner-chip` 패턴 본뜸: 파일명·크기 span + 다운로드/삭제 아이콘). 빈 상태는 기존 `.ops-attachment-area`(dashed 박스)를 dropzone/안내 영역으로 재사용.
- **[디자인 M-2] 토큰**: 신규 칩/버튼은 **`--ops-*` 네임스페이스 토큰**으로 작성(ops-doc 다크모드 자동 적용). 다운로드(성공)·삭제(위험) 강조색은 **design-system.css 전역 토큰(`--success`/`--danger`) 차용**(ops 네임스페이스에 미정의이므로 신설 대신 전역 차용으로 결정). 하드코딩 hex 금지.
- **[디자인 m-1] 필수표기**: 기존 패턴대로 라벨 끝 ` *` 텍스트 별표(색상 별표 span 미도입).
- **[디자인 m-2] 상태 UX**: 업로드 중 버튼 비활성화 + "업로드 중…" 토글, 실패 시 칩 영역 인라인 에러. (전체 `alert()`+`reload` 지양 — ops-doc 인라인 UX 일관성.)
- **[디자인 m-3] 접근성**: 다운로드/삭제 아이콘 버튼에 `aria-label`.
- **[디자인 m-4] 삭제 확인**: `confirm()` 유지(별도 모달 불필요).
- **[디자인 m-5] 반응형**: `.ops-file-chip` 에 `min-width:0` + 파일명 `text-overflow:ellipsis`.
- 디자인 시스템 토큰·다크모드·specificity·:root self-reference 준수(자문 결과 self-reference 위험 없음 확인).

## 7. 비기능 / 보안

- 업로드 디렉터리는 정적 서빙 경로 밖. 다운로드는 컨트롤러 경유 + 권한 가드.
- 매직바이트 검증으로 위조 1차 차단. 용량 상한 30MB.
- 파일명/세그먼트 정제로 경로 인젝션 차단.

## 8. 업로드 시점 (UX) — 확정

- 착수계 패턴과 동일하게 **문서 저장 후(=docId 확정) 동일 폼의 "지원 문서" 섹션에서 업로드**(C-1 반영: 상세=수정 동일 폼). 파일명에 `docNo` 가 필요해 저장 전 업로드 불가.
- 신규 작성 폼에서는 안내 문구만 노출.

## 9. 리스크 / 오픈 이슈

1. **레거시 SUPPORT 데이터**: `support_target` 신규 필수 → 기존 row 다음 수정 시 입력 강제(조회 무관, 백필 불필요). FR-1 "폼 full-section 전송" 불변식 전제.
2. **다중 지원파일 요구 가능성**: 현재 1:1. 추후 다중 필요 시 `OpsDocumentAttachment` 확장(별도 스프린트).
3. **hwp/hwpx 매직바이트**: ZIP(hwpx/xlsx/docx)·OLE2(hwp/xls/doc) 시그니처를 형식 간 공유 → 매직바이트는 "컨테이너 계열" 수준 검증, 정밀 판별은 확장자 신뢰. 위조 위험은 화이트리스트+계열 일치로 수용.
4. **[DB 사소] doc_no 채번 동시성**: `findMaxDocNoSeq+1` 방식이라 동시 생성 시 UNIQUE 충돌(트랜잭션 실패) 가능 — 기존 ops-doc 공통 이슈(본 스프린트 신규 결함 아님). 파일명이 docNo 종속이므로 인지만; 시퀀스 전환은 별도.
5. **년도 기준**: 요청일 우선/생성일 fallback — 운영 분류 일관성 위해 명문화(FR-3).

## 10. 작업 산출물(예정)

- DB: `db_init_phase2.sql` 컬럼 추가 블록(§5, tb_ops_doc 후속 ALTER 근처 배치 + COMMENT + 명시 FK명).
- BE: `OpsSupportFileService`(스캔 서비스 이식 — 경로/검증/충돌/원자교체), `OpsDocController` 업로드/다운로드/삭제 API + `SecurityConfig` 편입, `OpsDocService` REQUIRED_SECTION_KEYS[SUPPORT] 에 `support_target` 추가.
- FE: `doc-support.html` 필드 개편 + "지원 문서" 섹션(`isEdit` 게이팅) + 업로드 JS, `ops-doc.css` 에 `.ops-file-chip` 신설(`--ops-*`/전역 토큰).
- 검증: 단위테스트(경로안전/형식검증/충돌/권한/롤백), 통합 스모크(회사 PC 실 `D:\swmanager-scan`).

## 11. 수용기준 (Acceptance Criteria) — [codex 사소] 신설

| # | Given | When | Then |
|---|---|---|---|
| AC-1 업로드 성공 | EDIT 권한·저장된 SUPPORT 문서 | 허용 형식(hwp/hwpx/xls/xlsx/doc/docx/pdf) 업로드 | `업무지원/{년도}/{시스템}/{시도}/{시군구}/{지원대상}_{docNo}.{ext}` 저장 + 메타 기록 + 칩 표시 |
| AC-2 형식 거부 | 동일 | 확장자/매직바이트 계열 불일치, 0바이트, 짧은 파일, 빈 ZIP | 거부 + 인라인 에러, 파일·메타 미변경 |
| AC-3 용량 거부 | 동일 | 30MB 초과 | 거부 |
| AC-4 교체(1:1) | 이미 파일 보유 문서 | 재업로드 | 동일 docNo 경로 교체, 이전 경로 파일 정리(고아 없음) |
| AC-5 삭제 | 파일 보유 문서 | 삭제 | 파일 + 메타 제거(멱등) |
| AC-6 다운로드 | 파일 보유 문서 | 다운로드 | 원본 파일명(한글 인코딩)으로 응답 |
| AC-7 권한 | VIEW 전용 사용자 | 업로드/삭제 시도 | 403 |
| AC-8 타입 가드 | FAULT/INSTALL/PATCH/INSPECT 문서 | support-file API 호출 | 거부 |
| AC-9 경로안전 | — | traversal/symlink 우회 시도 | 차단(SecurityException) |
| AC-10 신규 폼 | 미저장(신규 작성) | 폼 진입 | 업로드 UI 비활성 + "저장 후 첨부" 안내 |
| AC-11 레거시 수정 | `support_target` 없는 기존 문서 | 조회 | 정상 / 수정 저장 시 `support_target` 필수 강제 |
| AC-12 롤백 | — | 파일 이동/커밋 실패 | DB·파일 정합 유지(불일치 없음) |
