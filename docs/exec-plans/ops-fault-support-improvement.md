---
tags: [dev-plan, sprint]
sprint: "ops-fault-support-improvement"
status: draft-v2
created: "2026-06-13"
revised: "2026-06-13"
---

# [개발계획서] 장애처리·업무지원 개선 — 지식베이스 + 관계자 정비 (v2)

- **작성팀**: 개발팀
- **작성일**: 2026-06-13
- **근거 기획서**: [[ops-fault-support-improvement]] (`docs/product-specs/ops-fault-support-improvement.md` v0.4, 사용자 최종승인 2026-06-13)
- **상태**: v2 (codex 1차 검토 5건 반영, 사용자 최종승인 대기)
- **v2 변경(codex 반영)**: ① `ck_ops_doc_req_required` **doc_type FAULT/SUPPORT 한정**(점검/설치/패치 회귀 방지, 핵심) ② Service 엔지니어 필수+enabled+SW지원팀 명문화 ③ 신규 API authDocument 검사 계층 명시 ④ FR 매핑표 Step 번호 정정 ⑤ FR-M1-1 부분충족(후속)·재승인 항목화(§12)
- **DB 안전**: `docs/AGENT_SAFETY.md` 준수 — 운영DB read-only 가드, 마이그레이션 ephemeral fresh-init 검증, 시크릿 안전채널.

---

## 0. 사전 조건 (Preconditions)

| 항목 | 확인 방법 |
|---|---|
| 회사 PC 내부망(운영DB 검증 가능) | `BIOS_SERIAL=S1NRKD000353050` |
| ephemeral fresh-init 클러스터(`localhost:25880` 등) 재사용 가능 | `phase2-V018` 게이트 패턴 |
| `tb_org_unit` 9본부 39행 seed 적재됨 | `db_init_phase2.sql:543-584` |
| KB 산출물 위치 | `C:\Users\PUJ\업무일지_정규화\{지식DB.csv(126),백서_정문.csv(47)}` |
| `users` PK 컬럼명 확정 | 엔티티 `@Column(name="user_id")` → DDL dump 로 `users(user_id)` 재확인 (구 exec-plan 의 `users(id)` 표기와 불일치 주의) |
| `ps_info` PK 컬럼명 `id` 확정 | `PersonInfo.java` + dump |
| `pg_trgm` CREATE EXTENSION 권한 | 운영DB 사전 확인, 실패 시 LIKE 폴백 |

---

## 1. Step 0 — 사전 스캔 (회귀 영향 매핑)

**목적**: 변경 대상 위치를 grep 으로 확정 → 정리 누락 0.

- 0-1. `Grep "getOrgNm|getDeptNm|getTeamNm|searchByOrgNm" src/main/java` → `users` 문자열 조직 사용처(검색 6쿼리 포함) 목록 → org_unit_id 도입 후 **동결(읽기전용)** 확인용.
- 0-2. `Grep "REFERENCES tb_partner|tb_ops_doc_partner|tb_ops_kb" src/main/resources/db_init_phase2.sql` → **신규 FK forward-ref 0건** 사전 검증(phase2-tb_ops_doc-forward-ref R-7 패턴).
- 0-3. `Grep "ck_tb_ops_doc_combo" src/main/resources` → 기존 CHECK 위치 → 신규 `ck_ops_doc_req_required` 공존 회귀 대상 확정.
- 0-4. `Grep "REFERENCES users" src/main/resources/db_init_phase2.sql` → 실제 users PK 참조 컬럼명 확인.
- **산출물**: 없음(인메모리 확인). 불일치 시 본 계획 수정.

---

## 2. Step 1 (P1) — M1 조직-인원 연결  (FR-M1-1·2·3)

### 2-1. DDL — `db_init_phase2.sql` (수정)
**위치**: `tb_org_unit` seed 블록(`:584`) **직후**(FK 가 tb_org_unit 참조하므로 seed 뒤).
```sql
-- [ops-fault-support M1] users 조직 매핑 (멱등)
ALTER TABLE users ADD COLUMN IF NOT EXISTS org_unit_id BIGINT;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_users_org_unit') THEN
  ALTER TABLE users ADD CONSTRAINT fk_users_org_unit FOREIGN KEY (org_unit_id)
    REFERENCES tb_org_unit(unit_id) ON DELETE SET NULL; END IF; END $$;
CREATE INDEX IF NOT EXISTS idx_users_org_unit ON users(org_unit_id);
-- SW지원팀 엔지니어 매핑 seed (멱등) — 현재 3명
UPDATE users SET org_unit_id = (SELECT unit_id FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM')
 WHERE username IN ('박욱진','김한준','서현규') AND org_unit_id IS NULL;
```
> 그 외 자사 인원 매핑·41 이미지 인원 seed 는 **본 스프린트 비범위**(SW지원팀 3명만 매핑, 나머지 후속). seed 보정(팀명 불일치 FR-M1-1)은 데이터 확정 후 별도 PR.

### 2-2. 엔티티 — `User.java`
- `@Column(name="org_unit_id") private Long orgUnitId;` 추가(+ Lombok `@Data` 자동 접근자). 기존 `orgNm/deptNm/teamNm` **변경 없음**(deprecated 동결, 신규 쓰기 금지 주석).

### 2-3. 조직도 화면 — `templates/admin/org-unit-management.html` (수정) + `OrgUnitController`
- **B-G1 토큰화 선행**: `<style>` 의 `box-shadow:0 2px 10px rgba(0,0,0,0.08)`→`var(--shadow-card)`, `background:white`→`var(--surface)`, 레거시 `.page-header`→`ds-page-header`. **라이트 토큰만**(다크 비대상).
- 인원 표시: `GET /api/org-units/{unitId}/members` 신설(OrgUnitController) → 우측 detailPane 에 이름·직급(`position`)·**재직/퇴사 배지**. 트리 노드엔 인원수 카운트 칩(`group-chip`).
- 퇴사 배지: `design-system.css` 에 **`.ds-badge--muted` 1개 신규**(라이트 토큰만). 재직=`ds-badge--success`.

### 2-4. 테스트 (T-1x)
- T-11 `OrgUnitMemberTest`: SW지원팀 unit 조회 → 박욱진/김한준/서현규 3명 매핑 검증.
- T-12 fresh-init 후 `users.org_unit_id` FK 존재 + 멱등 재실행 무오류.

---

## 3. Step 2 (P2) — M2 담당 엔지니어 · 요청자  (FR-M2-1·2·5)

### 3-1. DDL — `db_init_phase2.sql`
**위치**: `tb_ops_doc` 블록 뒤, **단 `tb_partner_contact` CREATE(Step 3) 보다 뒤**여야 `fk_ops_doc_req_contact` 성립 → Step 2/3 DDL 은 **한 묶음**으로 partner→ops_doc 컬럼 순서 배치(기획서 부록 B 순서 그대로). 제약은 **제약별 개별 DO 가드**(codex).
```sql
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
    CHECK ( doc_type NOT IN ('FAULT','SUPPORT')           -- ※codex v2: FAULT/SUPPORT 만 요청자 필수
            OR (requester_person_id IS NOT NULL)::int + (requester_contact_id IS NOT NULL)::int = 1 ) NOT VALID;
  END IF; END $$;
```
> ⚠ **codex v2 핵심**: `doc_type` 범위 제한 없으면 NOT VALID 여도 신규 INSPECT/INSTALL/PATCH(요청자 없음) 저장이 즉시 실패. CHECK 는 FAULT/SUPPORT 한정.

### 3-2. 엔티티 — `OpsDocument.java` (수정)
- `@ManyToOne engineer`(users), `@ManyToOne requesterPerson`(PersonInfo), `@ManyToOne requesterContact`(PartnerContact) 추가(LAZY). 기존 `author`(작성자)와 **별도**.

### 3-3. Service/Controller contract (codex 잔여 해소)
- **`OpsDocController.create/update`**(`:248`/`:283`): `body` 에서 `engineer_id`·요청자(`requester_kind`∈{PERSON,CONTACT}+`requester_id`) 파싱 추가.
- **`OpsDocService` 검증(명문화, codex #3·#4)**:
  ① **요청자**: `doc_type∈{FAULT,SUPPORT}` 일 때만 필수 — person XOR contact 정확히 1(미충족 `INVALID_INPUT`). 그 외 doc_type 은 요청자 미적용(점검/설치/패치 회귀 방지, DB CHECK 범위와 일치).
  ② **엔지니어**: `doc_type∈{FAULT,SUPPORT}` 일 때 `engineer_id` **필수** + 대상 user 가 `enabled=true` + `org_unit_id = SW지원팀` (아니면 `INVALID_INPUT`). 활성 사용자 기준은 기존 Repository 관례 재사용.
  ③ 기존 `section_data` 흐름 불변(NFR-1).
- **권한 검사 계층(codex #5)**: 신규 `POST /ops-doc/api/requester`·`/ops-doc/api/partner-contact`·`/ops-doc/api/kb/feedback` 는 **컨트롤러 진입부에서 `authDocument != 'NONE'`(쓰기는 `EDIT`) 확인** — `PersonController.getAuth()` 패턴 복제(`@AuthenticationPrincipal CustomUserDetails` → `getUser().getAuthDocument()`). create/update API 자체의 문서권한 체크 부재(현행)도 본 스프린트에서 `authDocument EDIT` 가드 추가.
- **인라인 등록(FR-M2-5)**: `POST /ops-doc/api/requester` → ps_info 최소필드 insert 후 id 반환. `/person/save`(authPerson EDIT) 직접호출 금지(권한 분리).

### 3-4. 템플릿 — `doc-fault.html`/`doc-support.html` (수정)
- 담당 엔지니어 `<select>`(SW지원팀, `ops-form-table` 그대로).
- 요청자: **세그먼트 토글(공무원|업체담당자) + 단일 검색창 + 결과 리스트 + '+신규등록' 펼침 폼**(모달 ✕, 디자인 R3/R4). 미선택 시 저장 차단(요청자 필수).

### 3-5. 테스트 (T-2x)
- T-21 요청자 0명 저장 → 거부. T-22 person+contact 동시 → 거부(XOR). T-23 person 1명 → 성공. T-24 engineer 비-SW지원팀 → 거부. T-25 인라인 등록 API authDocument 보유자 성공 / 미보유 403. T-26 기존 `ck_tb_ops_doc_combo` + 신규 CHECK 공존 회귀(FAULT 정상 저장).

---

## 4. Step 3 (P3) — M2 외부업체  (FR-M2-3·4)

### 4-1. DDL (Step 2 DDL **앞**: partner → partner_contact → [Step2 ops_doc 컬럼] → ops_doc_partner)
- `tb_partner`, `tb_partner_contact`(기획서 부록 B), `tb_ops_doc_partner`(tb_ops_doc·tb_partner 뒤). role_label PK 포함(복수역할).

### 4-2. 엔티티/리포지토리
- `Partner`, `PartnerContact`, `OpsDocPartner`(복합키) + `PartnerRepository`/`PartnerContactRepository`.

### 4-3. 관리화면 — `/partner` (신규, `authPerson` 재사용)
- `PartnerController`(list/detail/new/save/delete) + `templates/partner/*`. `PersonController` 권한 패턴 복제(NONE/READ/WRITE). 업체 + 담당자(자식) 인라인 관리.

### 4-4. 문서 연결
- 협력업체 다중선택(`tb_ops_doc_partner`, role_label). 요청자=업체담당자 시 `requester_contact_id`(Step 2 배타 CHECK).
- 업체담당자 인라인 등록 `POST /ops-doc/api/partner-contact`(authDocument).

### 4-5. 테스트 (T-3x)
- T-31 partner CRUD(authPerson EDIT). T-32 contact RESTRICT(담당자 있는 업체 삭제 차단). T-33 문서-협력업체 다대다 + 동일업체 복수역할 허용. T-34 fresh-init CREATE 순서(forward-ref 0).

---

## 5. Step 4 (P4) — M3 KB 추천  (FR-M3-1·2·3)

### 5-1. DDL + seed
- `tb_ops_kb`·`tb_ops_kb_feedback` CREATE(기획서 부록 B, 순서 kb→feedback→인덱스). `pg_trgm` 실패 시 **LIKE 폴백**(인덱스 생략 + 매처 LIKE 모드).
- **seed 변환 스크립트**(개발 PC, 레포 외): `지식DB.csv`/`백서_정문.csv` → `db_init_phase2.sql` 말미 INSERT 블록 또는 별도 `db_seed_ops_kb.sql`. **금칙어 검증**: 지자체명 마스터 + 처리엔지니어/공무원 실명 후보 + 업체명 리스트 매칭 0건 확인 후 커밋.

### 5-2. 엔티티/매처
- `OpsKb` 엔티티 + `OpsKbRepository`. **`KbMatcher` 인터페이스** + `RuleKbMatcher`(sysType 필터 + 토큰 점수 + case_count 가중, 0건 시 sysType 해제 폴백). 후속 `OllamaEmbeddingMatcher` 자리(NFR-6).

### 5-3. 추천 API + UI
- `GET /ops-doc/api/kb/recommend?docType=&sysType=&symptom=` → top-5.
- `doc-fault`/`doc-support`: 증상행 아래 **인라인 접이식 추천 카드**(`ops-section` 내, 디자인 R1). 카드='형태/원인/조치/출처 배지(`group-chip`)'. '적용'(`ops-btn-primary`)→해당 textarea 채움. SUPPORT 는 `support_content` 채움(라벨 명시).

### 5-4. 테스트 (T-4x)
- T-41 추천 API: 'Tomcat' 증상 → 구동오류 노드 상위. T-42 sysType 필터. T-43 0건 폴백. T-44 NFR-2 응답<300ms(147행). T-45 seed 금칙어 0건.

---

## 6. Step 5 (P5) — 채택 피드백  (FR-M3-4)
- 추천 '적용'/'무시' → `POST /ops-doc/api/kb/feedback`(kb_id, doc_id?, fb_action) 적재. 집계·학습은 후속.
- T-51 피드백 적재.

---

## 7. Step 6 — 회귀·통합 검증
- 7-1. **ephemeral fresh-init** 전체 재기동 → 신규 테이블/컬럼/제약 생성, **forward-ref stop 0**, 멱등 2회 재실행 무오류.
- 7-2. 기존 ops-doc(점검/장애/지원/설치/패치) 저장·조회·리스트 회귀 무손상(NFR-1).
- 7-3. `ck_tb_ops_doc_combo` + `ck_ops_doc_req_required` 공존 정상.
- 7-4. 브라우저 스모크: 장애처리 작성(엔지니어·요청자·추천 적용·협력업체) → 저장 → 상세.

---

## 8. FR/NFR → Step 매핑

| FR/NFR | Step | 비고 |
|---|---|---|
| **FR-M1-1** | Step 1 | ⚠**부분충족(후속)** — 39 seed↔41이미지 대조·보정·전 인원 seed 는 데이터 확정 후 별도 PR. **사용자 재승인 항목**(§12) |
| FR-M1-2 / FR-M1-3 | Step 1 | org_unit_id 매핑 / 화면·배지 |
| FR-M2-1 / FR-M2-2 / FR-M2-5 | Step 2 | 엔지니어 필수 / 요청자 / 인라인등록 |
| FR-M2-3 / FR-M2-4 | Step 3 | 외부업체 / 협력 다대다 |
| FR-M3-1 / FR-M3-2 / FR-M3-3 | Step 4 | KB seed / 매처 / 추천 패널 |
| FR-M3-4 | Step 5 | 피드백 |
| NFR-1 | Step 3-3 · Step 6 | 비침습·회귀 |
| NFR-2 | Step 4 | pg_trgm/LIKE, <300ms |
| NFR-3 | 전반 | 로컬·권한 |
| NFR-4 | Step 4 | 금칙어 seed 검증 |
| NFR-5 | Step 4 | 출처 노출 |
| NFR-6 | Step 4 | KbMatcher 추상화 |

## 9. 롤백
- DDL: 신규 테이블 DROP + tb_ops_doc 신규 컬럼·제약 DROP(멱등 역순). 기존 seed/테이블 불변이라 데이터 손실 없음.
- 코드: 엔티티 필드/엔드포인트 revert. 템플릿 추천 패널·관계자 선택 제거 시 기존 폼 동작.
- seed: tb_ops_kb TRUNCATE.

## 10. 리스크
- R1 ~~`users(user_id)` vs `users(id)` 불일치~~ → **해소**: 실제 DDL `REFERENCES users(user_id)` 일관(`db_init_phase2.sql:404` author_id 등). `user_id` 확정.
- R2 pg_trgm 권한 → LIKE 폴백 경로 반드시 구현·테스트.
- R3 41 이미지 전 인원 seed 미포함(SW지원팀 3명만) → 조직도 인원 표시는 매핑된 인원만 노출(공백 정상).
- R4 `ck_ops_doc_req_required` NOT VALID → 기존 row 미검증. 신규/수정만 강제(의도).

## 11. codex 검토 체크 (이 계획 대상)
- [x] FR 반영 완전성(매핑표 정정) · [x] CREATE/ALTER 순서(forward-ref) · [x] 회귀(CHECK doc_type 한정) · [x] 롤백 타당성 — **codex 1차 ⚠수정필요 5건 → v2 반영**

## 12. 사용자 재승인 항목 (codex #2)
- **FR-M1-1(조직 seed 대조·보정)을 본 스프린트에서 제외**하고 후속 PR 로 분리하는 것에 대한 확인 필요.
  - 사유: 기존 39행 seed 와 41 이미지의 팀명 불일치(예: seed `GIS SW영업팀`·`GIS1부 사업1팀` vs 이미지 표기, `글로벌기획본부` 계층)는 **데이터 확정(사용자 검토)** 후 보정해야 안전. 전 인원 seed 도 41 이미지 전사 필요.
  - 본 스프린트 M1 범위 = `users.org_unit_id` 추가 + **SW지원팀 3명 매핑** + 조직도 화면(인원 표시는 매핑된 인원만).
  - **선택지**: (A) 제외 승인(권장) — 후속 `ops-org-seed-revise` 로 / (B) 이번 포함 — 팀명·계층 확정 자료 추가 제공 필요.
  - ✅ **사용자 승인(2026-06-13)**: (A) 제외 확정 → FR-M1-1 은 후속 `ops-org-seed-revise` PR. 본 스프린트 M1 = `users.org_unit_id` + SW지원팀 3명 매핑 + 조직도 화면.
