# 장애처리·업무지원 개선 기획서 (지식베이스 + 관계자 정비)

> ## ✅ 구현 완료 (2026-06-13, 듀얼푸시) — 본 기획서·개발계획서는 **이력 문서**
> M1(조직-인원·tb_staff 85명 시드)·M2(엔지니어·요청자 3종 XOR·협력 다대다)·M3(tb_ops_kb 135행·RuleKbMatcher·실시간 추천) 전부 구현·검증·푸시 완료(최신 `81475ce`). 2026-06-23 운영DB 재확인(5테이블·135 KB행·제약 공존·30 ops테스트·forward-ref 0). **아래 §12 "사용자 최종승인" 체크박스는 stale**(실제 승인·구현 완료). 유일 잔여 = 클린룸 빈DB fresh-init 멱등 2회(별도 ephemeral PG16 클러스터) + 조직 seed 41이미지 대조(후속 `ops-org-seed-revise` PR). **신규 작업 아님 — 재기획·재구현 금지.**

> **상태**: ~~DRAFT v0.4~~ → **구현 완료** · 작성일 2026-06-13 · 작성 🧭기획팀
> **워크플로우**: 기획서(본 문서) → 🗄DB팀 + 🎨디자인팀 자문 **[반영]** → 🔍codex 검토 **[반영]** → ✅사용자 최종승인 → 개발계획
> **연관**: `doc-split-ops.md`(ops-doc), `doc-selector-org-env.md`(tb_org_unit), `phase2-tb_ops_doc-forward-ref.md`(init 순서 규칙), `ai-search.md`(M3 후속 AI)
>
> **v0.2 변경**: ① tb_org_unit 9본부 seed 가 이미 적재됨을 확인 → M1 범위 축소 ② DB팀 자문 반영 ③ 디자인팀 자문 반영 ④ 부록 B·§13 추가
> **v0.3 변경(사용자 확정)**: ① **요청자 필수**(없는 문서 불허) → 정확히 1명(공무원 XOR 업체담당자) ② **다크모드 비대상**(신규 UI 라이트만) ③ **퇴사자 표시**(재직/퇴사 배지 구분) ④ 외부업체 복수역할 허용(role_label PK)
> **v0.4 변경(codex 검토 반영)**: ① DDL 제약 **제약별 개별 DO 가드** 분리(부분적용 누락 방지) ② `tb_ops_kb`/`tb_ops_kb_feedback` **CREATE TABLE 명시**(순서 tb_ops_kb→feedback→인덱스) ③ 인라인 등록 권한 정책(`POST /ops-doc/api/requester`, authDocument) ④ pg_trgm 실패 LIKE 폴백 + seed 금칙어 검증(§13-5)

---

## 1. 배경 · 문제

- 폐지된 **부서 기술지원 백서 프로젝트** 산물로 SW지원부 업무일지 **748건**을 정규화·정제·LLM 재서술하여 지식 자산 확보(`업무일지_기술백서.xlsx` — 147 지식노드 / 47 백서 정문, 구조 `구분→분류(시스템)→형태(증상)→원인→조치`).
- 이 자료는 **장애처리(FAULT)·업무지원(SUPPORT) 기능의 모토 자료**다. 그러나 현재 ops-doc 은:
  - 증상·원인·조치를 **매번 백지에서 수기 작성** → 시간·품질편차·신규자 진입장벽·지식 사장.
  - 담당 엔지니어·요청자·관련 업체도 **자유 텍스트 수기** → 재사용·통계 불가.
- 폼 구조(`증상→원인→조치`)와 KB 구조가 **이미 일치**하고, 관계자 데이터도 상당수 시스템에 존재 → 연결하면 즉시 효과.

## 2. 목표 · 비목표

**목표**: ① 증상 기준 원인·조치 **추천/자동완성**(KB) ② 관계자 **마스터 선택**(자사 엔지니어·요청 공무원·외부업체) ③ 자사 인원의 조직 기준 확립 ④ **1차 규칙·검색 기반**, AI는 후속.

**비목표**: Ollama RAG(M3 후속), SLA/만족도 분석 대시보드, KB 위키 단독 메뉴.

## 3. 현황 (as-is)

| 주체/대상 | 저장소 | 현황 |
|---|---|---|
| 자사 엔지니어 | `users` | SW지원팀 박욱진·김한준·서현규. `orgNm/deptNm/teamNm` **문자열**(검색쿼리 실사용), `OpsDocument.author` |
| 담당공무원(지자체) | `ps_info` (`/person` 담당자관리) | CRUD 존재. 지역/기관/부서/직위/시스템/연락처 |
| 외부업체 | **없음** | 업체·업체담당자 마스터 부재(수기) |
| 조직 단위 | `tb_org_unit` | **9본부 39행 seed 이미 적재**(`db_init_phase2.sql:543-584`). 단 **사람(users) 미연결** |
| 시스템 | `sys_mst` + ops `sys_type` | 마스터 존재 |
| 장애/지원 문서 | `tb_ops_doc` + `tb_ops_doc_detail`(jsonb) | FAULT: 증상/원인/조치+심각도. SUPPORT: 지원내용+채널/만족도 |
| 지식 자산(KB) | 외부 파일 | 미적재 |

## 4. 개선 모듈 개요

| 모듈 | 내용 | 비고 |
|---|---|---|
| **M1 조직-인원 연결** | 기존 tb_org_unit seed 보정 + **users↔org_unit 매핑** + 조직도 화면 인원·재직 표시 | 트리 신규 아님 |
| **M2 관계자 정비** | 엔지니어 선택(SW지원팀)·요청자(ps_info)·**외부업체 신규 마스터** | |
| **M3 KB 추천** | 증상→원인→조치 추천 + KB 적재 | 규칙 1차 |

---

## 5. M1 — 조직-인원 연결  *(v0.2: 범위 축소)*

> tb_org_unit 트리는 이미 적재됨. 본 모듈은 **인원 연결**이 핵심.

### 5.1 기능요구 (FR-M1)
- **FR-M1-1** 기존 39행 seed 를 사용자 조직도(41 이미지)와 **대조·보정**: 팀명 불일치 점검(예: seed `GIS SW영업팀`·`GIS1부 사업1팀` vs 이미지 표기, `글로벌기획본부`가 seed 는 최상위 DIVISION 인데 이미지는 글로벌사업본부 하위 — 확정 후 보정).
- **FR-M1-2** `users.org_unit_id`(FK, nullable) 추가 + 자사 인원 매핑. 엔지니어 풀 = `org_unit=SW지원팀` 활성 users(현재 3명).
- **FR-M1-3** 조직도 관리 화면(`/admin/org-units`)에 단위별 인원·직급·**재직/퇴사 상태 표시**(퇴사자도 표시, 배지로 구분). **단, B-G1(화면 토큰화, 라이트) 선행**(§13).
- **org_unit 정본(SoT) 정책**: `org_unit_id`(FK)를 정본으로, 기존 `orgNm/deptNm/teamNm` 문자열은 **표시용 deprecated(읽기전용 동결)**. 기존 검색 6쿼리의 FK 전환은 후속 과제(이번 비범위).

---

## 6. M2 — 관계자(담당/요청자/외부업체) 정비

### 6.1 담당 엔지니어
- FAULT/SUPPORT 폼 담당자 = **SW지원팀 3명 드롭다운**. `tb_ops_doc.engineer_id`(users FK) 추가.

### 6.2 요청자 — 담당공무원(ps_info 재사용)
- 폼에서 **ps_info 검색·선택**, 없으면 **인라인 신규 등록**(`/person/save` 재사용). `tb_ops_doc.requester_person_id`(ps_info FK).

### 6.3 외부업체 — 신규 마스터
```
tb_partner         : partner_id · name · partner_type(사업단/유지보수/DB/SI/기타) · biz_no · main_tel · note · use_yn · created/updated
tb_partner_contact : contact_id · partner_id FK(RESTRICT) · name · position · tel · email · use_yn · created/updated
```
- 관리 화면 `/partner` CRUD. **권한 = 기존 `authPerson` 재사용**(신규 `authPartner` 는 7~8곳 동시수정 비용 → 과설계, DB팀 R-3).
- 폼 연결: **요청자는 필수** — 공무원(`requester_person_id`) 또는 업체담당자(`requester_contact_id`) **정확히 1명**(XOR, §13 B-D3). **협력업체**는 `tb_ops_doc_partner` 다대다(역할 라벨, 동일 업체 복수역할 허용).

### 6.4 기능요구 (FR-M2)
- **FR-M2-1** 엔지니어 드롭다운 + `engineer_id`. **FR-M2-2** 요청자 ps_info 검색·선택·인라인등록 + `requester_person_id`. **FR-M2-3** 외부업체+담당자 마스터/관리화면(`authPerson`). **FR-M2-4** 문서–업체 연결(요청자=업체담당자 배타 / 협력업체 다대다).
- **FR-M2-5 인라인 등록 권한(codex 반영)**: 요청자 인라인 신규등록은 `/person/save`(authPerson=EDIT) 직접호출 ✕ → **ops-doc 전용 경량 엔드포인트** `POST /ops-doc/api/requester`(최소필드: 이름/기관/지역/직위/연락처) 신설, **ops-doc 쓰기 권한(authDocument)** 으로 허용(담당자관리 전체권한과 분리). 업체담당자 인라인 등록도 동일 패턴.

---

## 7. M3 — 증상→원인→조치 KB 추천

### 7.1 KB 적재 (tb_ops_kb)
`kb_id · kb_code · gubun · sys_type · category · symptom · cause · summary · symptom_desc · cause_desc · action · prevention · keywords · case_count · rewritten · created/updated`
- seed: `지식DB.csv`(126)·`백서_정문.csv`(47) → INSERT. **기관·실명 제외**(NFR-4). 검증 게이트(codex 반영): 단순 `grep` ✕ → **금칙어 리스트 기반**(지자체명 마스터 + 처리엔지니어/공무원 실명 후보 + 업체명) 매칭 0건 확인 후 적재.
- 인덱스: `pg_trgm GIN(keywords)` + `(gubun, sys_type)` (§13 부록 B). 한글 형태소 약한 tsvector 대신 trgm.

### 7.2 추천 (규칙 1차)
- **API** `GET /ops-doc/api/kb/recommend?docType=FAULT&sysType=UPIS&symptom=...` → `docType→gubun`+`sysType` 필터 → 증상/키워드 토큰 점수 + 사례수 가중 → top-5. 0건 시 sysType 해제 폴백.
- **UX(디자인 R1)**: 우측 드로어 ✕. 증상행 **바로 아래 `ops-section` 내 접이식 인라인 추천 카드**. 카드='형태/원인/조치(정형)/출처 배지(백서코드·사례 N)'. '적용'(`ops-btn-primary`)→textarea 채움(편집가능).
- 매처 `KbMatcher` 인터페이스 추상화 → 후속 `OllamaEmbeddingMatcher` 교체(NFR-6).

### 7.3 채택 피드백(후속)
- `tb_ops_kb_feedback`(kb_id·doc_id FK, 익명집계) 적재만. → 부모 뒤 CREATE(§13).

### 7.4 기능요구 (FR-M3)
- **FR-M3-1** tb_ops_kb DDL+seed. **FR-M3-2** 추천 API+규칙 매처. **FR-M3-3** FAULT/SUPPORT 추천 패널. **FR-M3-4** 피드백 적재.

---

## 8. 데이터 모델 변경 요약

| 테이블 | 변경 | 비고 |
|---|---|---|
| `tb_org_unit` | seed **보정**(신규 아님) | 39행 기적재 |
| `users` | `org_unit_id` FK(nullable) | SoT, 문자열 deprecated |
| `tb_partner`/`tb_partner_contact` | 신규 | RESTRICT, use_yn, created/updated |
| `tb_ops_doc` | `engineer_id`/`requester_person_id`/`requester_contact_id` + 요청자 필수 CHECK(XOR) | **ALTER 멱등** |
| `tb_ops_doc_partner` | 신규(다대다) | tb_ops_doc·tb_partner **뒤** CREATE |
| `tb_ops_kb`/`tb_ops_kb_feedback` | 신규 | kb_feedback 은 부모 뒤 |

## 9. 비기능 요구사항 (NFR)
- **NFR-1 비침습** / **NFR-2 추천<300ms**(pg_trgm 인덱스) / **NFR-3 로컬·Security** / **NFR-4 KB 개인정보 제외(단, 운영 관계자 실명은 업무상 보유)** / **NFR-5 출처 노출(설명가능성)** / **NFR-6 KbMatcher 추상화**.

## 10. 단계 계획

| Phase | 범위 |
|---|---|
| **P1** | M1 인원 매핑(`users.org_unit_id`)+seed 보정+조직도 화면(토큰화 포함) |
| **P2** | M2-1/2 엔지니어·요청자(ps_info) 폼 연결 |
| **P3** | M2-3/4 외부업체 마스터+관리화면+문서 연결 |
| **P4** | M3 KB 적재+추천 API+규칙 매처+추천 패널 |
| **P5** | M3 채택 피드백 |
| **P6(후속)** | Ollama RAG 매처 |

## 11. 리스크 · 오픈이슈 → §13 으로 통합

---

## 12. 가상팀 자문 · 승인 체크
- [x] 🗄 DB팀 자문 (v0.2 반영, §13-1)
- [x] 🎨 디자인팀 자문 (v0.2 반영, §13-2)
- [x] 🔍 codex 검토 (v0.4 반영, §13-5 — ⚠수정필요 3건 보강 완료)
- [x] ✅ 사용자 최종승인 → 개발계획 (2026-06-13 승인, 개발계획서 v2 작성·구현 완료. 상단 완료 배너 참조)

---

## 13. 가상팀 자문 결론 (v0.2 반영)

### 13-1. 🗄 DB팀 — 조건부 진행가능
**Blocker(반영)**
- **B-D1 CREATE 순서**: `tb_ops_doc_partner`(→tb_ops_doc+tb_partner), `tb_ops_kb_feedback`(→tb_ops_kb+tb_ops_doc) 는 부모 **뒤** 배치. `phase2-tb_ops_doc-forward-ref` 의 forward-ref stop 전례 → 개발계획서에 순서 명문화 + `Grep REFERENCES` 0건 사전검증.
- **B-D2 운영DB 멱등**: tb_ops_doc 신규 컬럼은 인라인 FK ✕ → `ALTER ... ADD COLUMN IF NOT EXISTS` + FK 는 `DO $$ pg_constraint EXISTS 가드 $$`(선례 tb_document org_unit_id).
- **B-D3 요청자 필수·정확히 1명 (FAULT/SUPPORT 한정)**: 앱 레벨 필수 검증 + DB `CHECK( doc_type NOT IN ('FAULT','SUPPORT') OR (person+contact = 1) )`. ⚠**doc_type 범위 제한 필수**(codex v0.5) — 없으면 INSPECT/INSTALL/PATCH(요청자 없음) 신규 저장 깨짐. + **`NOT VALID`**(기존 row 미검증, 신규/수정만 강제).

**권고(반영)**: org_unit FK=SoT·문자열 deprecated / `authPerson` 재사용 / partner RESTRICT·use_yn·created·updated·biz_no 부분 UNIQUE / pg_trgm GIN.

### 13-2. 🎨 디자인팀 — 조건부 진행가능
**Blocker(반영)**
- **B-G1 org-unit 화면 토큰화 선행**: `org-unit-management.html` 의 하드코딩 box-shadow·`white`·레거시 `.page-header` → `var(--surface)`/`--shadow-card`/`ds-page-header` 토큰화 후 인원 UI 추가(다크 깨짐 방지).
- **B-G2 `:root` 토큰**: 신규 색은 `ops-doc.css` 기존 토큰 재사용. *(v0.3: 다크모드 비대상 → 신규 UI 는 라이트 토큰만, `[data-theme=dark]` 쌍 추가 불필요)*
- **B-G3 배지 색 재발명 금지**: 출처/사례수 배지 = 기존 `group-chip`/`ds-badge--info` 재사용.

**권고(반영)**: 추천=인라인 접이식 카드 / 적용=`ops-btn-primary` / 요청자 소스=**세그먼트 토글(공무원|업체담당자)+단일 검색창**(요청자 필수=미선택 시 저장 차단) / 인라인 신규등록=모달 ✕ 펼침 폼 / 조직도 인원=우측 detailPane 리스트+트리 카운트 칩 / **퇴사자 표시**(재직/퇴사 구분) → 퇴사 배지용 `ds-badge--muted` 1개 신규(라이트만).

### 13-3. 부록 B — DDL 패턴 (개발계획 가이드)
```sql
-- users FK (멱등)
ALTER TABLE users ADD COLUMN IF NOT EXISTS org_unit_id BIGINT;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_users_org_unit') THEN
  ALTER TABLE users ADD CONSTRAINT fk_users_org_unit FOREIGN KEY (org_unit_id)
    REFERENCES tb_org_unit(unit_id) ON DELETE SET NULL; END IF; END $$;

-- tb_partner / tb_partner_contact (tb_ops_doc_partner 앞)
CREATE TABLE IF NOT EXISTS tb_partner ( partner_id BIGSERIAL PRIMARY KEY, name VARCHAR(200) NOT NULL,
  partner_type VARCHAR(20), biz_no VARCHAR(20), main_tel VARCHAR(40), note VARCHAR(1000),
  use_yn VARCHAR(1) NOT NULL DEFAULT 'Y', created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT ck_partner_type CHECK (partner_type IS NULL OR partner_type IN ('사업단','유지보수','DB','SI','기타')) );
CREATE UNIQUE INDEX IF NOT EXISTS uq_partner_biz_no ON tb_partner(biz_no) WHERE biz_no IS NOT NULL;
CREATE TABLE IF NOT EXISTS tb_partner_contact ( contact_id BIGSERIAL PRIMARY KEY,
  partner_id BIGINT NOT NULL REFERENCES tb_partner(partner_id) ON DELETE RESTRICT,
  name VARCHAR(100) NOT NULL, position VARCHAR(50), tel VARCHAR(40), email VARCHAR(100),
  use_yn VARCHAR(1) NOT NULL DEFAULT 'Y', created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW() );

-- tb_ops_doc 신규 컬럼 (멱등). ※codex 반영: 제약은 반드시 제약별 개별 DO 가드 — 한 블록에 묶으면 부분적용 시 누락
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS engineer_id BIGINT;
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS requester_person_id BIGINT;
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS requester_contact_id BIGINT;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ops_doc_engineer') THEN
  ALTER TABLE tb_ops_doc ADD CONSTRAINT fk_ops_doc_engineer FOREIGN KEY (engineer_id) REFERENCES users(user_id); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ops_doc_req_person') THEN
  ALTER TABLE tb_ops_doc ADD CONSTRAINT fk_ops_doc_req_person FOREIGN KEY (requester_person_id) REFERENCES ps_info(id); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ops_doc_req_contact') THEN
  ALTER TABLE tb_ops_doc ADD CONSTRAINT fk_ops_doc_req_contact FOREIGN KEY (requester_contact_id) REFERENCES tb_partner_contact(contact_id); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_ops_doc_req_required') THEN
  ALTER TABLE tb_ops_doc ADD CONSTRAINT ck_ops_doc_req_required
    CHECK ( doc_type NOT IN ('FAULT','SUPPORT')
            OR (requester_person_id IS NOT NULL)::int + (requester_contact_id IS NOT NULL)::int = 1 ) NOT VALID; END IF; END $$; -- FAULT/SUPPORT 만 요청자 필수(codex v0.5)

-- 다대다 (tb_ops_doc·tb_partner 뒤)
CREATE TABLE IF NOT EXISTS tb_ops_doc_partner ( doc_id BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
  partner_id BIGINT NOT NULL REFERENCES tb_partner(partner_id) ON DELETE RESTRICT, role_label VARCHAR(50),
  PRIMARY KEY (doc_id, partner_id, role_label) );

-- KB: ※codex 반영 — CREATE 순서 tb_ops_kb → tb_ops_kb_feedback → 인덱스
CREATE TABLE IF NOT EXISTS tb_ops_kb (
  kb_id BIGSERIAL PRIMARY KEY, kb_code VARCHAR(20) NOT NULL UNIQUE,
  gubun VARCHAR(10), sys_type VARCHAR(20), category VARCHAR(30),
  symptom VARCHAR(200), cause VARCHAR(200),
  summary TEXT, symptom_desc TEXT, cause_desc TEXT, action TEXT, prevention TEXT,
  keywords TEXT, case_count INT DEFAULT 0, rewritten BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW() );
CREATE TABLE IF NOT EXISTS tb_ops_kb_feedback (          -- tb_ops_kb·tb_ops_doc 뒤
  feedback_id BIGSERIAL PRIMARY KEY,
  kb_id  BIGINT NOT NULL REFERENCES tb_ops_kb(kb_id) ON DELETE CASCADE,
  doc_id BIGINT REFERENCES tb_ops_doc(doc_id) ON DELETE SET NULL,
  fb_action VARCHAR(20), created_at TIMESTAMP NOT NULL DEFAULT NOW() );  -- fb_action: APPLIED/IGNORED
CREATE EXTENSION IF NOT EXISTS pg_trgm;                  -- ※권한 필요: 실패 시 LIKE 폴백(개발계획서 명시)
CREATE INDEX IF NOT EXISTS idx_ops_kb_keywords ON tb_ops_kb USING gin (keywords gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_ops_kb_filter ON tb_ops_kb(gubun, sys_type);
```
> `ps_info` PK 컬럼명 `id`(PersonInfo.java) → 운영DB dump 로 재확인.

### 13-4. 사용자 확정 결과 (2026-06-13)
1. **요청자 필수** — 없는 FAULT/SUPPORT 문서 불허. 공무원 XOR 업체담당자 정확히 1명(앱 검증 + DB CHECK NOT VALID, B-D3).
2. **다크모드 비대상** — 신규 UI 라이트만(org-unit 토큰화도 라이트 한정).
3. **퇴사자 표시** — 트리·리스트에 퇴사자도 노출, 재직/퇴사 배지로 구분(`ds-badge--muted`).
4. **외부업체 복수역할 허용** — 한 문서에 동일 업체 요청+협력 동시 가능(`tb_ops_doc_partner` PK 에 role_label 포함).
5. **org_unit 검색 FK 전환은 후속**(이번 비범위) — `users.org_unit_id` FK 를 SoT 로 두되 기존 orgNm 문자열 검색 6쿼리는 유지.

### 13-5. 🔍 codex 검토 결과 (2026-06-13, ⚠수정필요 → 반영 완료)
| codex 지적 | 반영 |
|---|---|
| DDL DO 블록이 fk_engineer 하나만 가드 → 나머지 3제약 부분적용 누락 | 부록 B **제약별 개별 DO 가드** 분리(v0.4) |
| `tb_ops_kb` CREATE 누락(인덱스만 존재) | 부록 B **tb_ops_kb·tb_ops_kb_feedback CREATE 추가**, 순서 명시(v0.4) |
| 인라인 `ps_info` 등록 권한 흐름 불명확(authPerson=EDIT 충돌) | **FR-M2-5** ops-doc 전용 등록 API(authDocument)로 분리(v0.4) |
| pg_trgm 확장 권한 실패 가능 | 부록 B 주석 + **LIKE 폴백** 명시(개발계획서 상세) |
| KB seed 개인정보 `grep` 검증 약함 | §7.1 **금칙어 리스트 기반 검증**으로 강화 |
| OpsDocument/Controller body contract 변경 미명시 | **개발계획서에서** 엔티티·Repository·Service·Controller·검증 상세화(아래 잔여) |
- **codex 잔여(개발계획서로 이관)**: 엔티티/Repository/Service/Controller 변경 contract, 요청자 XOR·엔지니어 필수 Service 검증 위치, 회귀(`ck_tb_ops_doc_combo` 기존 CHECK 와 신규 CHECK 공존) 테스트.
