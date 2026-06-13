---
tags: [dev-plan, sprint]
sprint: "ops-kb-workbench"
status: draft-v2
created: "2026-06-14"
revised: "2026-06-14"
---

# [개발계획서] 장애·지원 지식베이스 직접 등록·조회 (KB 워크벤치) v2

- **작성팀**: 개발팀
- **작성일**: 2026-06-14
- **근거 기획서**: [[ops-kb-workbench]] (`docs/product-specs/ops-kb-workbench.md` v0.2, 사용자 최종승인 2026-06-13, 열린질문 5 전부 기본안)
- **상태**: v2 (codex 검토 3건 반영, 사용자 최종승인 대기)
- **v2 변경(codex)**: ① `requireDocEdit` private → OpsKbController 에 동일 정책 helper **복제**(조회=authDocument≠NONE, ADMIN 통과 / 등록·수정·삭제=EDIT) ② 조회 추천의 **feedback 오기록 방지**(window.OPS_KB_BROWSE 플래그 시 opsKbFeedback 생략, 시그니처 보존) ③ DDL **부분적용 보정**(SET DEFAULT / UPDATE NULL / SET NOT NULL)
- **DB 안전**: `docs/AGENT_SAFETY.md` 준수 — 멱등 ALTER, ephemeral fresh-init 검증, 운영DB 증분.

---

## 0. 사전 조건 / Step 0 — 사전 스캔
- 0-1. `Grep "/ops-kb" src/main` → 라우트 충돌 0 확인.
- 0-2. `Grep "findByGubun\(|findAll\(\)" RuleKbMatcher.java` → active 필터 교체 지점 확정.
- 0-3. `Grep "opsKbRecommend\|opsKbSetup" src/main/resources` → 시그니처 변경 금지 사용처(ops-doc 폼) 확인.
- 0-4. tb_ops_kb 기존 행 status/source 기본값 검증(ALTER DEFAULT 로 SEED/ACTIVE 자동).

## 1. Step 1 (P1) — DB 확장 + 매처 active 필터 (FR-1 기반)

### 1-1. DDL — `db_init_phase2.sql` (tb_ops_kb CREATE 블록 뒤, 멱등)
```sql
-- [ops-kb-workbench] MANUAL 직접등록 확장
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS source     VARCHAR(10) DEFAULT 'SEED';
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS created_by VARCHAR(50);
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS status     VARCHAR(10) DEFAULT 'ACTIVE'; -- ACTIVE/DELETED
-- codex: 부분적용(컬럼만 있고 default/not-null 누락) 보정 — 멱등 안전
ALTER TABLE tb_ops_kb ALTER COLUMN source SET DEFAULT 'SEED';
ALTER TABLE tb_ops_kb ALTER COLUMN status SET DEFAULT 'ACTIVE';
UPDATE tb_ops_kb SET source='SEED'   WHERE source IS NULL;
UPDATE tb_ops_kb SET status='ACTIVE' WHERE status IS NULL;
ALTER TABLE tb_ops_kb ALTER COLUMN source SET NOT NULL;
ALTER TABLE tb_ops_kb ALTER COLUMN status SET NOT NULL;
CREATE SEQUENCE IF NOT EXISTS seq_ops_kb_manual START 1;
CREATE INDEX IF NOT EXISTS idx_ops_kb_status_source ON tb_ops_kb(status, source);
```
> 기존 시드 126행 = source 'SEED' / status 'ACTIVE' DEFAULT 자동 충족. `db_seed_ops_kb.sql` 의 ON CONFLICT DO UPDATE 는 source/status 미포함이라 재실행 안전.

### 1-2. 엔티티 `OpsKb.java` — 매핑 보강
- 추가: `source`, `createdBy`(created_by), `status`, `createdAt`(created_at), `updatedAt`(updated_at).
- `@PrePersist`(createdAt/updatedAt now, status 기본 ACTIVE, source 기본 'SEED'), `@PreUpdate`(updatedAt).

### 1-3. `OpsKbRepository`
- `List<OpsKb> findByStatusOrderByCaseCountDesc(String status)`
- `List<OpsKb> findByGubunAndStatus(String gubun, String status)`
- `@Query(value="SELECT nextval('seq_ops_kb_manual')", nativeQuery=true) Long nextManualSeq();`
- 검색용: `@Query` 시스템·구분·키워드 LIKE (조회 목록).

### 1-4. `RuleKbMatcher` — active 필터 (B-1)
- `findByGubun` → `findByGubunAndStatus(gubun,'ACTIVE')`, `findAll` → `findByStatusOrderByCaseCountDesc('ACTIVE')`.
- ⚠회귀: ops-doc 폼 추천도 이 매처 사용 → 시드 status='ACTIVE' 이므로 결과 불변(T-5 검증).

### 1-5. 테스트
- T-11 ALTER 멱등(2회) · T-12 매처 ACTIVE 만(DELETED 제외) · T-13 시드 추천 회귀(ops-doc 폼 동일 결과).

## 2. Step 2 (P1) — 조회/검색 화면 (FR-1)

### 2-1. `OpsKbController` (신규, `/ops-kb`)
- `GET /ops-kb` → 목록 페이지. model: `systems`(sys_mst), 권한 `authDocument`.
- `GET /ops-kb/api/list?sysType&gubun&kw` → 검색 결과(ACTIVE만, source 포함).
- **추천은 기존 `GET /ops-doc/api/kb/recommend` 재사용**(시그니처 보존, B-G2).
- 권한: 조회 `authDocument != 'NONE'`, 진입 가드.

### 2-2. 템플릿 `templates/ops-doc/kb-list.html`
- `ds-page-header`(제목 + 신규등록 Primary 버튼) + `ops-section`(필터: 시스템 단일 select·구분·증상검색) + `#kbRecoBox`(실시간 추천, `opsKbRecommend(docType,query,sysType,openKbDetail)`) + `ops-list-table`(목록) + `ops-empty-state`.
- **ops-doc.css + design-system 토큰만. 신규 `:root` 금지. `list.html` 레거시 hex 복붙 금지**(B-G1).
- source 배지: `ds-badge--muted`(SEED)/`ds-badge--info`(MANUAL).
- 구분→docType: 장애→FAULT/지원→SUPPORT 매핑.
- **feedback 오기록 방지(codex)**: 조회 페이지는 `window.OPS_KB_BROWSE=true` 설정. `ops-doc-relations.js` `opsKbFeedback` 첫 줄에 `if (window.OPS_KB_BROWSE) return;` 추가(시그니처·ops-doc 폼 동작 불변 — ops-doc 폼은 플래그 미설정). applyFn=`openKbDetail`(상세 열기, 폼 미채움).

### 2-3. 네비 `top-nav.html`
- 문서관리 그룹에 sub-link `/ops-kb`(지식베이스), `authDocument` 게이트, `data-prefix="/ops-kb"`.

### 2-4. 테스트
- T-21 검색(시스템·구분·키워드) · T-22 실시간 추천 동작(applyFn=상세) · T-23 권한 NONE 차단.

## 3. Step 3 (P2) — 등록/수정/삭제 (FR-2·3·4)

### 3-1. `OpsKbController` CRUD (권한 — codex: `requireDocEdit` 는 private 이라 재사용 불가)
- **권한 helper 복제(codex v2)**: OpsKbController 에 동일 정책 private helper 작성 — 조회 `ADMIN || authDocument != 'NONE'`, 등록/수정/삭제 `ADMIN || authDocument == 'EDIT'` 아니면 403. (둘 다 **ADMIN 통과** 명시. 공통 추출은 과설계 — 소형 복제)
- `GET /ops-kb/new` , `GET /ops-kb/{id}/edit` → 폼 페이지.
- `POST /ops-kb/api` → kb_code 채번 `String.format("KB-%d-%05d", year, nextManualSeq())`, source='MANUAL', created_by=현재사용자, status='ACTIVE', case_count=1, rewritten=false. 필수: 시스템·구분·증상·원인·조치.
- `PUT /ops-kb/api/{id}` → 필드 갱신.
- `DELETE /ops-kb/api/{id}` → **소프트삭제** `status='DELETED'`(하드 DELETE 금지, B-3).

### 3-2. 템플릿 `templates/ops-doc/kb-form.html`
- `ops-form-table`: 시스템(sys_mst select)·구분(`ops-seg` 토글 장애/지원)·증상/원인/조치 textarea·키워드. `ops-actions`(취소/저장). 수정 시 프리필.

### 3-3. 테스트
- T-31 등록 채번 KB-yyyy-##### · T-32 등록 즉시 추천 반영(NFR-2) · T-33 권한 EDIT 아니면 403 · T-34 소프트삭제 후 추천/검색 제외 · T-35 수정 반영.

## 4. Step 4 — 회귀·검증
- 4-1. ephemeral fresh-init: ALTER 멱등, seq/idx 생성, 시드 status='ACTIVE'.
- 4-2. **ops-doc FAULT/SUPPORT 폼 추천 회귀 무손상**(매처 active 필터 영향 없음 — 시드 ACTIVE).
- 4-3. 브라우저: /ops-kb 검색→추천, /ops-kb/new 등록→목록·추천 즉시 반영, 소프트삭제 후 제외.

## 5. FR/NFR 매핑
| 항목 | Step |
|---|---|
| FR-1 조회/검색 | Step 2 |
| FR-2 신규 등록 | Step 3 (채번 Step1-3) |
| FR-3 수정/삭제 | Step 3 |
| FR-4 권한 | Step 2(조회)·Step 3(EDIT) |
| NFR-1 비침습(매처 active만) | Step 1-4·4-2 |
| NFR-2 즉시 반영 | Step 3 |
| NFR-5 디자인 토큰 | Step 2-2·3-2 |

## 6. 롤백
- DDL: 신규 컬럼·시퀀스·인덱스 DROP(멱등 역). 기존 시드/추천 불변.
- 코드: OpsKbController·템플릿·네비 link revert, RuleKbMatcher active 필터 원복(findByGubun/All).
- 데이터: MANUAL 행 `DELETE WHERE source='MANUAL'`(시드 무영향).

## 7. 리스크
- R1 매처 active 필터 누락 시 DELETED 노출 → T-12/T-34 필수.
- R2 채번 동시성 → DB 시퀀스(앱 MAX+1 금지).
- R3 ops-doc 폼 추천 회귀 → T-5/4-2.
- R4 category 공란 → 상세 화면 시스템·구분으로 표시(폼 입력칸 없음).

## 8. codex 검토 체크
- [x] FR 반영 · [x] DB 멱등/채번/소프트삭제 · [x] 매처 active 회귀 · [x] 롤백
- codex v1 ⚠수정필요 3건(private requireDocEdit / 조회 feedback 오기록 / DDL 부분적용) → **v2 반영**
- codex v2 재검토 = **3건 모두 승인(진행가능)**. 권한 helper ADMIN 통과 명시까지 반영. 구현 착수 가능.
