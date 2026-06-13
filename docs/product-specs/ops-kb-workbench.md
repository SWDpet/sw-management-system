# 장애·지원 지식베이스 직접 등록·조회 (KB 워크벤치) 기획서

> **상태**: DRAFT v0.2 · 작성일 2026-06-13 · 작성 🧭기획팀
> **워크플로우**: 기획서(본 문서) → 🗄DB팀 + 🎨디자인팀 자문 **[반영]** → 🔍codex 검토 → ✅사용자 최종승인 → 개발계획
> **연관**: `ops-fault-support-improvement.md`(tb_ops_kb·추천엔진), `ai-search.md`(후속 AI 분류)
>
> **v0.2(자문 반영)**: ① 매처에 **active 필터 추가 필수**(MANUAL 추천오염 차단, NFR-1 정정) ② kb_code **DB 시퀀스 채번**(KB-yyyy-#####) ③ **소프트삭제(status)** — 하드삭제 시 feedback CASCADE 소실 ④ 권한 **authDocument 재사용**(requireDocEdit) ⑤ 시스템 필터=**단일 select**(캐스케이드 아님) ⑥ 등록=**별도 페이지**(/ops-kb/new), list.html 레거시 hex 베이스 금지·ops-doc.css/design-system 토큰만 ⑦ 추천 JS 시그니처 보존(applyFn=상세열기). 상세는 §11.

---

## 1. 배경 · 문제

- 현재 장애·지원 **지식베이스(tb_ops_kb, 126건)** 는 과거 업무일지 748건을 **사람이 백서로 정리한 1회성 산물**이다. 추천(증상→원인→조치)은 **ops-doc 문서 작성 폼 안에서만** 노출된다.
- 사용자 방향(2026-06-13): **앞으로 별도 문서를 만들지 않는다.** 대신 *"현업에서 입력한 내용을 시스템이 저장·분류·제공"* 하여 **업무 부담을 줄이고 데이터를 지속 축적**해야 한다. 다시 문서를 만들고 수기로 정리하면 시스템의 의미가 없다.
- 즉, **입력(캡처) → 분류 → 제공(추천/조회)** 루프가 정상 업무 흐름 안에 있어야 하고, 그 입력 자체가 KB 지식이 되어 다음 사람을 돕는다.

## 2. 목표 · 비목표

**목표**
- 문서 작성 없이 **지식 항목을 직접 등록**(증상·원인·조치 + 시스템·구분)하면 **즉시 분류·저장**되고 **다음 추천/조회에 반영**된다.
- 축적된 지식을 **문서 작성과 무관하게 검색·열람**할 수 있다(실시간 추천 엔진 재사용).
- 등록·수정·삭제로 **현업이 직접 KB 를 지속 성장**시킨다.

**비목표(이번 제외)**
- 문서(ops-doc) → KB 자동 승격(무거운 학습 루프) — 문서 작성 자체를 안 하므로 불필요.
- 자연어 자동 분류(증상 텍스트로 시스템/형태 자동 판별) — v1 은 사용자가 시스템·구분 선택. 후속(AI) 과제.

## 3. 현황 (as-is)

| 요소 | 현황 |
|---|---|
| `tb_ops_kb` | 126건(백서 시드, sys_type UPIS/LSA/PGMS, 구분 장애/지원, 형태/원인/조치/키워드/사례수) |
| 추천 API | `GET /ops-doc/api/kb/recommend?docType&sysType&symptom` + `RuleKbMatcher`(토큰+사례수, 시스템 엄격필터) |
| 노출 위치 | **ops-doc FAULT/SUPPORT 폼 내부에만** (실시간 갱신·접기) |
| 등록 수단 | **없음** — 시드/SQL 외에 화면에서 KB 추가 불가 |
| 피드백 | `tb_ops_kb_feedback`(적용/무시 적재, 랭킹 미반영) |

## 4. 솔루션 개요 — "지식베이스" 메뉴 신설

```
[지식베이스] 메뉴 (문서 작성과 독립)
  ├ 조회/검색 : 시스템 선택 + 증상 입력 → 실시간 추천(기존 엔진) + 목록 열람/상세
  ├ 신규 등록 : 시스템·구분·증상·원인·조치·키워드 입력 → 분류 저장 → 즉시 추천/검색 반영
  └ 수정/삭제 : 기존 항목 보강·정정
```
- 등록 시 **분류 = 시스템(sys_mst) + 구분(장애/지원)** 자동 태깅(사용자 선택값). 형태/원인/조치는 입력.
- 신규 항목은 추천 풀(tb_ops_kb)에 즉시 포함 → **쌓을수록 똑똑해지는 구조**.

## 5. 데이터 모델 (tb_ops_kb 확장)

기존 컬럼 유지 + 추가:
| 컬럼 | 용도 |
|---|---|
| `source` VARCHAR(10) | 'SEED'(백서) / 'MANUAL'(직접등록) 구분. 기본 'SEED' |
| `created_by` VARCHAR(50) | 등록자(직접등록 추적) |
| (기존) kb_code | UNIQUE NOT NULL → 직접등록은 **자동 채번**(예: `KB-yyyy-{seq}`) |
| (기존) created_at/updated_at | 그대로 |

- 직접등록 항목은 `rewritten=false`, `case_count=1` 시작.

## 6. 기능 요구사항 (FR)

- **FR-1 조회/검색**: `/ops-kb`(목록) — 시스템·구분 필터 + 증상 검색(실시간 추천 API 재사용) + 카드/표 열람, 상세(증상/원인/조치/예방/키워드/사례수/출처).
- **FR-2 신규 등록**: `POST /ops-kb/api` — 시스템(sys_mst)·구분(장애/지원)·증상·원인·조치(필수) + 키워드(선택). kb_code 자동채번, source=MANUAL, created_by=현재사용자. 저장 즉시 추천 대상.
- **FR-3 수정/삭제**: `PUT/DELETE /ops-kb/api/{id}`. (삭제는 소프트 또는 하드 — DB팀 자문)
- **FR-4 권한**: 모든 페이지 권한 원칙([[feedback]]) — 조회/등록 권한 게이트. 권한 컬럼 재사용 여부 DB팀 자문(authDocument 후보).

## 7. 비기능 요구사항 (NFR)

- **NFR-1 비침습**: 기존 추천 엔진(`KbMatcher`)·tb_ops_kb 그대로 재사용. ops-doc 폼 영향 없음.
- **NFR-2 즉시 반영**: 등록/수정 즉시 추천·검색에 반영(별도 배치 없음).
- **NFR-3 권한**: 로그인 + 권한 게이트. 전량 로컬.
- **NFR-4 분류 단순성**: v1 은 사용자 선택 분류(시스템·구분). 자동분류는 후속.
- **NFR-5 디자인**: 기존 ops-doc.css / design-system 토큰 재사용(추천 카드·폼 패턴).

## 8. 단계 계획 (개발계획 개요)

| Phase | 범위 |
|---|---|
| **P1** | tb_ops_kb 확장(source/created_by) + `/ops-kb` 조회·검색 화면(추천 엔진 재사용) |
| **P2** | 신규 등록(kb_code 자동채번, 분류 저장) + 수정/삭제 + 권한 게이트 |
| **P3(후속)** | 자동분류(증상→시스템/형태 제안), 피드백 기반 랭킹, 문서연계(필요 시) |

## 9. 리스크 · 오픈 이슈

1. **권한 모델**: 신규 auth 컬럼 vs authDocument 재사용 — DB팀 자문(과설계 회피).
2. **kb_code 채번**: 시드 코드(1-05-...)와 충돌 없는 MANUAL 접두 채번 규칙.
3. **삭제 정책**: 하드삭제 vs use_yn/active 소프트(추천 제외). tb_ops_kb 에 상태 컬럼 없음 → 추가 여부.
4. **분류 정확도**: 사용자 선택 분류라 오분류 가능 → 수정 UI 로 보완.
5. **메뉴/네비**: 사이드바 메뉴 추가(전사 사이드바 패턴) — 디자인팀 자문.

## 10. 가상팀 자문 · 승인 체크
- [x] 🗄 DB팀 자문 (v0.2 반영, §11-1)
- [x] 🎨 디자인팀 자문 (v0.2 반영, §11-2)
- [x] 🔍 codex 검토 — **기획 방향 조건부 승인 가능**. 지적은 전부 "아직 구현 전"(정상, 승인 후 개발계획에서). 개발계획 필수항목 = §11-3 DDL + 매처 active 필터 + /ops-kb CRUD + authDocument + 네비 + 추천JS 시그니처 보존. (※ `:root` 재정의 금지 = 신규 KB CSS 에서 새 :root 추가 금지, 기존 토큰 소비는 OK)
- [ ] ✅ 사용자 최종승인 → 개발계획

---

## 11. 가상팀 자문 결론 (v0.2 반영)

### 11-1. 🗄 DB팀 — 조건부 진행가능
**Blocker(반영)**
- **B-1 추천 오염**: `RuleKbMatcher`가 `findByGubun/findAll`로 전량 후보 → MANUAL 미검증·소프트삭제 항목 노출. **매처에 `status='ACTIVE'` 필터 추가 필수**(NFR-1 "매처 무수정"은 정정 — active 필터만 추가). `OpsKbRepository.findByGubunAndStatus/findByStatus` 신설.
- **B-2 채번 동시성**: 앱 `MAX+1` 금지 → **DB 시퀀스** `seq_ops_kb_manual` → `KB-yyyy-#####`(시드 `1-/2-` prefix 와 충돌 불가, VARCHAR(20) 충분).
- **B-3 삭제**: **소프트삭제(status ACTIVE/DELETED)**. 하드 DELETE 금지 — `tb_ops_kb_feedback.kb_id ON DELETE CASCADE`로 피드백 이력 소실.

**권고(반영)**: 권한 **authDocument 재사용**(`requireDocEdit` 등록/수정/삭제, VIEW 조회 — 신규 auth 컬럼 금지). `OpsKb` 엔티티에 **created_at/updated_at 매핑 누락 보강** + source/createdBy/status 필드. case_count 가중 혼용(시드=실누적 vs MANUAL=1)은 P3 랭킹으로 이연(현재 허용·기록).

### 11-2. 🎨 디자인팀 — 조건부 진행가능
**Blocker(반영)**
- **B-G1 레거시 베이스 금지**: `templates/ops-doc/list.html`은 raw hex(`#d4edda` 등) 다수 → KB 화면 베이스로 복붙 금지. **`ops-doc.css`(ops-section/ops-form-table/ops-list-table/ops-empty-state/ops-reco-*) + design-system(ds-page-header/ds-btn/ds-badge) 토큰만**.
- **B-G2 추천 JS 시그니처 보존**: `opsKbSetup/opsKbRecommend`는 폼 전제(applyFn·kbRecoBox·OPS_DOC_ID feedback·DOMContentLoaded engineerId early-return). **시그니처 변경 금지**(ops-doc 회귀 방지). 조회 화면은 `opsKbRecommend(docType, query, sysType, applyFn)` 재호출하되 **applyFn=상세열기**, feedback 분기.
- **B-G3 시스템 필터 = 단일 select**(sys_mst). `.ops-cascade` 사용 금지(지역 아님, 추천 API sysType 와 1:1).

**권고(반영)**: 등록=**별도 페이지 `/ops-kb/new`**(Primary 1개), 조회 기본·목록 Ghost. 조회=추천카드 상단 + `ops-list-table` 목록 + `ops-empty-state`. source 배지 `ds-badge--muted`(SEED)/`ds-badge--info`(MANUAL). **`:root` 재정의 금지**(토큰 소비만). 사이드바 top-nav "문서관리" 그룹 sub-link(`/ops-kb`, data-prefix). 구분(장애/지원)=`ops-seg` 토글. design-system+ops-doc 둘 다 링크(토큰 계열 혼용 주의).

### 11-3. 부록 — DDL (개발계획 가이드)
```sql
-- tb_ops_kb 확장 (멱등, 기존 ADD COLUMN IF NOT EXISTS 패턴)
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS source     VARCHAR(10) NOT NULL DEFAULT 'SEED';
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS created_by VARCHAR(50);
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS status     VARCHAR(10) NOT NULL DEFAULT 'ACTIVE'; -- ACTIVE/DELETED
CREATE SEQUENCE IF NOT EXISTS seq_ops_kb_manual START 1;
CREATE INDEX IF NOT EXISTS idx_ops_kb_status_source ON tb_ops_kb(status, source);
-- 채번(서비스): String.format("KB-%d-%05d", year, nextval('seq_ops_kb_manual'))
-- 삭제(서비스): UPDATE tb_ops_kb SET status='DELETED' WHERE kb_id=?  (하드 DELETE 금지)
-- 매처: findByGubunAndStatus(gubun,'ACTIVE') / findByStatus('ACTIVE')
```

### 11-5. Addendum — 등록 승인 워크플로 (ops-kb-approval, 2026-06-14 확정)
편집권한자가 등록은 하되, **최종 게시는 관리자 승인 후**에만 이루어지도록 모더레이션 게이트 추가.

- **상태(status)**: `ACTIVE`(게시) / `PENDING`(승인대기) / `REJECTED`(반려) / `DELETED`(삭제). VARCHAR(10) 그대로(REJECTED=8자).
- **등록**: 관리자=즉시 `ACTIVE` / 편집권한자(EDIT, 비관리자)=`PENDING`.
- **수정**: 관리자=`ACTIVE` 유지(reviewed 갱신, 반려사유 초기화) / 편집권한자=`PENDING` 재승인 전환(reviewed·반려사유 초기화).
- **승인/반려**: 관리자 전용. `POST /ops-kb/api/{id}/approve` → ACTIVE. `POST /ops-kb/api/{id}/reject`(reason) → REJECTED + `reject_reason` 보관(작성자 보완 재요청 가능).
- **추천 영향 없음(핵심)**: 매처는 `status='ACTIVE'` 만 후보로 사용 → PENDING/REJECTED 자동 제외. 시드 126건 전부 ACTIVE 라 ops-doc 폼 추천 불변.
- **큐 접근 통제**: 조회 API `status` 파라미터. `ACTIVE`=모두. `PENDING/REJECTED`=편집권한 필요, 관리자=전체·편집권한자=본인 제출만(`created_by` 스코프). VIEW 전용은 큐 접근 불가(서버 차단). `DELETED`=조회 불가.
- **DDL(멱등 추가)**: `reviewed_by VARCHAR(50)`, `reviewed_at TIMESTAMP`, `reject_reason TEXT`.
- **UI**: kb-list 상태 필터(게시/승인대기/반려), 상태 배지, 관리자 승인/반려 버튼, 반려 사유 표시, 헤더 승인대기 배지. kb-form 등록 후 PENDING 안내.

### 11-4. 사용자 확정 필요 (열린 질문)
1. **등록 위젯/페이지**: 등록을 별도 페이지(/ops-kb/new) 로? (디자인 권장 — 기본안)
2. **category(분류) 채움**: 폼에 분류 입력칸 없음 → 자동(미입력/공란) vs 시스템 기반 자동매핑? (기본안: 공란 허용, 상세는 시스템·구분 표시)
3. **구분 위젯**: 장애/지원 = `ops-seg` 토글 vs select (기본안: 토글, ops-doc 일관)
4. **docType 매핑**: 조회 구분필터 → 추천 docType(장애→FAULT/지원→SUPPORT) (기본안: 그대로 매핑)
5. **삭제 복구**: DELETED 항목 복구(status 환원) UI 제공? (기본안: P1 미제공, 필요시 후속)
