---
tags: [audit, data-architecture]
sprint: "data-architecture-audit"
created: "2026-04-20"
status: ✅ 완료 (Batch A/B/C + Phase 3 로드맵 승인)
---

# 데이터 아키텍처 감사 — 기능별 활용도 분석 (Phase 2)

- **근거 기획서**: [[data-architecture-audit]] v2 승인
- **Phase 1 입력**: [[data-architecture-master-inventory]], [[batch-a-scan-result]], [[batch-a-extra-result]]
- **상태**: Batch A 완료 (업무계획·점검내역서). Batch B/C 미완.

---

## 요약 — 핵심 결정 사항

### 🔑 `#1-A` Enum 재평가 결과: **유지 권고** (롤백 불필요)

의심했던 `cont_stat_mst`, `maint_tp_mst`와 Phase 2에서 **다른 도메인**임이 확정됨. §7-2 결정 매트릭스 점수표:

**`DocumentStatus`** (DRAFT/COMPLETED)
| 기준 | 측정값 | 점수 |
|------|--------|------|
| 마스터 존재 | `cont_stat_mst`와 LEFT JOIN 0% 일치 (값 형식 다름, 도메인 다름) | 1 |
| API 계약 | 응답 status 필드 그대로 | 1 |
| 마이그 비용 | 영향 레코드 0건 | 1 |
| 런타임 | 테스트 영향 0건 | 1 |
| **합계** | **4** → **유지** | |

**`DocumentType`** (COMMENCE/INTERIM/COMPLETION/INSPECT/FAULT/SUPPORT/INSTALL/PATCH)
| 기준 | 측정값 | 점수 |
|------|--------|------|
| 마스터 존재 | `maint_tp_mst`와 LEFT JOIN 0% 일치 (`maint_tp_mst`는 유지보수 구성 DB/HW/SW, 문서 유형과 다름) | 1 |
| API 계약 | 응답 docType 필드 그대로 | 1 |
| 마이그 비용 | 영향 레코드 0건 | 1 |
| 런타임 | 테스트 영향 0건 | 1 |
| **합계** | **4** → **유지** | |

---

## 기능 1: 업무계획 (`tb_work_plan`)

### 1-1. 사용 테이블
- `tb_work_plan` (1건 — 사실상 **미사용**)
- 연관: `tb_infra_master` (infra_id FK), `users` (assignee_id, created_by FK)

### 1-2. 컬럼별 마스터 매칭

| 컬럼 | 현재 값 | 기초 마스터 매칭 | 판정 |
|------|---------|----------------|------|
| `plan_type` | `PATCH` (1건) | ❌ `maint_tp_mst`와 미일치 (값 세트 다름: PATCH vs SW/HS/DHS/DU) | **④ 마스터 미존재** |
| `status` | `CONFIRMED` (1건) | ❌ `cont_stat_mst`와 미일치 (CONFIRMED vs 1/2) | **④ 마스터 미존재** |
| `repeat_type` | `NONE` (1건) — 주석상 NONE/MONTHLY/QUARTERLY/HALF_YEARLY | ❌ 마스터 없음 | **④ 마스터 미존재** |
| `process_step` | `null` (1건) — 주석상 1~7 | ❌ 마스터 없음, 정수 | **④ (공정단계 마스터 후보)** |

### 1-3. 코드 하드코딩
- 리터럴 `"CONTRACT|PRE_CONTACT|SETTLE|COMPLETE|PLANNED|..." 등` 18곳 발견 (`WorkPlanDTO` 14, `WorkPlanController` 1, `WorkPlanService` 1, `WorkPlan.java` 2)
- 주로 `WorkPlanDTO.java`의 switch 색상/라벨 매핑

### 1-4. 유형 분류
- **primary_type**: ④ 마스터 미존재 (신설 후보) / secondary_tags: [⑤ 레거시 미사용]
- **이유**:
  - DB 레코드 1건 — 사실상 미사용
  - 하지만 Service/Controller/DTO 코드가 존재 → 삭제 vs 재활성화 판단 필요

### 1-5. 조치 방향 (로드맵 후보)
- **우선순위 P3** — 실사용 미발생
- **옵션 A**: 기능 완전 제거 (레거시). Service/Controller/DTO 삭제 + `tb_work_plan` DROP
- **옵션 B**: 기능 유지하되 `work_plan_type_mst`, `work_plan_status_mst` 마스터 신설 + 재활성화
- **사용자 판단 필요**: 업무계획이 향후 사용 계획 있는 기능인가?

---

## 기능 2: 점검내역서 (`inspect_report`, `inspect_check_result`, `inspect_visit_log`, `inspect_template`)

### 2-1. 사용 테이블
- `inspect_report` (11건) — 점검 문서 본체
- `inspect_check_result` (513건) — 점검 항목별 결과
- `inspect_visit_log` (14건) — 방문 이력
- `inspect_template` (104건) — 점검 템플릿

### 2-2. 컬럼별 마스터 매칭

#### `inspect_report`
| 컬럼 | 값 분포 | 마스터 매칭 | 판정 |
|------|---------|----------|------|
| `status` | COMPLETED 7, DRAFT 4 | ❌ `cont_stat_mst`와 다름 (문서 상태) | **④ 마스터 미존재** (Enum `DocumentStatus` 이미 사용 중) |
| `sys_type` | UPIS 6, IPSS 3, UPIS_SW 1, KRAS 1 | ✅ sys_mst (UPIS/IPSS/KRAS matched) + `UPIS_SW` 는 **UPIS와 동일 시스템** (사용자 확정) | **① 데이터 정제**: `UPIS_SW` → `UPIS`로 통합 |
| `insp_name` (점검자) | 박욱진, 서현규, 김한준 부장, 김한준 차장, 김현탁 주무관 등 5종 | ❌ **`users` 미참조** (내부 직원, 사용자 확정). 매칭률 2/5 (직급 접미어 포함으로 불일치) | **② 중복 테이블/컬럼** — `insp_user_id` FK 추가 필요 |
| `insp_company` | "(주)정도UIT" 단일값 하드코딩 | — | 컬럼 자체 불필요 (users가 단일 조직이라 조회 시 표시) |
| `conf_name` (확인자) | 김학범, 서봉군, 임춘근, 김한준 부장, 임예빈 주무관 등 7종 | ❌ **`ps_info` 미참조** (고객사 담당자, 사용자 확정). 매칭률 3/7 | **② 중복 테이블/컬럼** — `conf_ps_info_id` FK 추가 필요 |
| `conf_org` | 양양군청, 울산광역시청, 성주군청, 이천시청, 김포시청, 가평군청 등 | ❌ `ps_info.org_nm` 또는 `sigungu_code.full_name`과 매칭 가능 여부 검증 필요 | **② 중복 컬럼** (ps_info FK 연동 후 런타임 조회) |

#### `inspect_check_result`
| 컬럼 | 값 분포 | 마스터 매칭 | 판정 |
|------|---------|----------|------|
| `section` | DB 144, DBMS 102, APP 98, AP 84, GIS 51, DB_USAGE 7, AP_USAGE 4, DBMS_ETC 2, APP_ETC 1 | ❌ 마스터 없음 | **④ 마스터 신설** (`check_section_mst`) |
| `category` | 35종 (오라클 102, H/W 36, GIS엔진 35, ..., "GeoNURIS GeoWeb Server (GWS)" 21 **vs** "GeoNURIS GeoWeb Server(GWS)" 3 공백차이) | ❌ 마스터 없음 + 공백 정규화 필요 | **① 데이터 정제 + ④ 마스터 신설** |
| `result` | 정상 313, (공백) 165, 퍼센트 9(1%/70%/21%/87%/55%/32%/48%), 자유텍스트 3(`159GB / 271GB 가용` 등), 점검 1 | ❌ 자유 텍스트 혼재 | **② 스키마 재구성** — `result_code` (NORMAL/INSPECT/ETC) + `result_text` (자유 텍스트) 분리 |

#### `inspect_template`
| 컬럼 | 값 분포 | 마스터 매칭 | 판정 |
|------|---------|----------|------|
| `section` | **APP 28 (미사용 — 사용자 확정)**, DB 24, GIS 21, DBMS 17, AP 14 (서버관리 연관) | ❌ `check_section_mst` (신설 후보) | **⑤ 레거시 제거** (APP 섹션 28건) + **④ 마스터 신설** (사용 섹션 4종: DB/DBMS/GIS/AP) |
| `template_type` | UPIS 75, UPIS_SW 20, KRAS 9 | `UPIS_SW`는 **UPIS와 동일 시스템** (사용자 확정) | **① 데이터 정제**: `UPIS_SW` → `UPIS` 통합 |

### 2-3. 코드 하드코딩
- 섹션 리터럴 `"DB_USAGE|AP_USAGE|DBMS_ETC|APP_ETC"` — `DocumentController.java`, `InspectPdfService.java`
- 결과 리터럴 `"정상|점검"` — `HwpxExportService.java`, `WorkPlanDTO.java`
- `"UPIS_SW"` 참조 — `HwpxExportService.java`
- `insp_company = "(주)정도UIT"` 하드코딩 11개 레코드 전부

### 2-3-X. 🚨 점검자·확인자 자유 텍스트 저장 (사용자 확정 방향 위반)
- **점검자** = 내부 직원 → `users` FK 참조 필요. 현재 이름·직급 자유 텍스트
- **확인자** = 고객사 담당자 → `ps_info` FK 참조 필요. 현재 이름·소속 자유 텍스트
- 직급 접미어("주무관"/"부장"/"차장") 포함으로 매칭 실패 빈발
- `users`에 `김한준` 있고 `ps_info`에 `김한준 부장(가평군청)` 있어 동명이인 혼동 가능

### 2-3-Y. 실제 매칭 실패 사례 분석 (사용자 확정 배경)

확인자 매칭 실패 4건(전체 7건 중)의 실제 원인:

| 사례 | 원인 | 자유 텍스트 방식의 구조적 문제 | FK 연동 시 개선 |
|------|------|-----------------------------|----------------|
| `김한준 부장` (확인자, 가평군청) | **점검자가 오입력** — 내부 직원(김한준)을 확인자로 잘못 기재 (동명이인 혼동) | 자유 텍스트라 내부/외부 구분 검증 없음 | 확인자 드롭다운은 `ps_info`만 노출 → 내부 직원 오입력 원천 차단 |
| `임예빈 주무관` (확인자, 김포시청) | **입력자 누락** — 실무자가 `ps_info`에 담당자 사전 등록을 안 함 | 자유 텍스트라 마스터 갱신 없이 입력 가능 | 드롭다운 선택 불가 → "새 담당자 추가" 흐름 유도 → 마스터 자연 확장 |
| `함정우 주무관` (확인자, 이천시청) | 동일 (입력자 누락) | 동일 | 동일 |
| (빈값, 이천시청) | 미입력 | 필수값 강제 없음 | FK NOT NULL + NOT_EXISTS 방지 |

**결론**: 4건 중 3건(75%)이 마스터 데이터 관리 프로세스 부재로 발생. FK 연동이 **오입력·누락 모두 해결**하는 핵심 개선.

### 2-4. 유형 분류
- **primary_type**: **② 스키마 재구성** (result 컬럼 분리 + inspector/confirmer FK 연동)
- secondary_tags: [① 데이터 정제, ④ check_section_mst 신설]

### 2-4-X. 점검자·확인자 FK 연동 (사용자 확정 원칙)

| 컬럼 | 현재 | 목표 |
|------|------|------|
| 점검자 | `insp_name` VARCHAR(50) 자유텍스트 | `insp_user_id BIGINT REFERENCES users(user_id)` |
| 점검자 회사 | `insp_company` VARCHAR(100) ("(주)정도UIT" 하드코딩) | **컬럼 삭제** (users가 단일 조직이라 불필요) |
| 확인자 | `conf_name` VARCHAR(50) 자유텍스트 | `conf_ps_info_id BIGINT REFERENCES ps_info(id)` |
| 확인자 소속 | `conf_org` VARCHAR(100) 자유텍스트 | **컬럼 삭제** (ps_info.org_nm 런타임 조회) |
| 점검자 서명 | `insp_sign` TEXT | users가 서명 컬럼 가지면 통합, 없으면 점검 레코드에 유지 |
| 확인자 서명 | `conf_sign` TEXT | 동일 |

### 2-5. 조치 방향 (로드맵 후보)
- **P1** — 스프린트 `inspect-result-schema-split`:
  - `inspect_check_result` 스키마 변경: `result` → (`result_code` VARCHAR(20) + `result_text` VARCHAR(200))
  - 기존 513건 마이그레이션: 정상→NORMAL_OK / 점검→INSPECT / 공백→EMPTY / 퍼센트·GB→ETC + result_text에 원값
  - `InspectCheckResult` Entity + DTO + 템플릿 수정
- **P1** — `check_section_mst` 마스터 신설 (9종 section + 5종 template section 통합)
- **P1** — 점검자·확인자 FK 연동 (`inspect-inspector-confirmer-link`):
  - `inspect_report`에 `insp_user_id` (FK → users), `conf_ps_info_id` (FK → ps_info) 컬럼 추가
  - 기존 `insp_name`/`insp_company`/`conf_name`/`conf_org` 컬럼 삭제 (런타임 조회로 대체)
  - 기존 11건 매칭 가능: 점검자 2건, 확인자 3건. 나머지는 테스트 데이터라 전면 초기화 후 재입력 가능
  - **부수 조치 (사용자 확정 케이스 기반)**:
    - 점검내역서 작성 UI에서 "담당자가 없으면 ps_info 신규 등록" 링크 제공 — `임예빈`·`함정우` 같은 입력 누락 사전 예방
    - 점검자 드롭다운에 `users` 만, 확인자 드롭다운에 `ps_info` 만 노출 — `김한준 부장` 같은 오입력 원천 차단
    - FK NOT NULL 제약 + 미선택 validation — 빈값 저장 방지

- **P1** (v2 사용자 확정) — 데이터 정제 + 레거시 제거:
  - `UPIS_SW` → `UPIS` **통합** (inspect_template 20건 + inspect_report 1건 + tb_document 1건 = 총 22건 UPDATE)
    - 프론트엔드 `doc-inspect.html:621-623`의 HW 금액 기반 분기 로직은 **유지** (서버 섹션 표시 제어용). 단 DB에 `UPIS_SW` 저장하지 않고 표시 계산만 런타임 수행
    - `db_init_phase2.sql`의 `UPIS_SW` 라벨 INSERT 제거 또는 `UPIS`로 변경
  - `inspect_template.APP` 섹션 **28건 삭제** (UPIS 14 + UPIS_SW 14 모두 미사용 — 사용자 확정)
    - **주의**: `inspect_check_result.section='APP'` 98건이 존재 — Batch B/C에서 이 실데이터 사용 경로 추가 검증 필요
- **P2** — 데이터 정제:
  - `category` 공백 정규화: "GeoNURIS GeoWeb Server (GWS)" vs "GeoNURIS GeoWeb Server(GWS)" 통합

---

## 기능 2-B: 업무계획 공정 마스터 (`tb_process_master`, `tb_service_purpose`) — 🚨 **심각한 중복**

### 중대 발견 — 스키마 버그 수준
- `tb_process_master`: 총 **1450건 / DISTINCT 5건** — 각 `(sys_nm_en, process_name)` 조합이 **290번씩 중복 INSERT**
  - 시스템별: APIMS 290, GIS_SW 290, IPSS 290, KRAS 290, UPIS 290
  - UNIQUE 제약 없음
- `tb_service_purpose`: 동일 문제 — 총 1450 / DISTINCT 5

### 원인 추정
`db_init_phase2.sql` 또는 `DbInitRunner`가 재실행될 때마다 중복 INSERT하는 것으로 보임. 서비스 재기동 횟수 × 초기 5건 = 누적 1450건.

### 유형
- **primary_type**: **② 스키마 재구성** (UNIQUE 제약 추가 + 데이터 정제)
- secondary_tags: [① 데이터 정제]

### 조치 방향 (로드맵 후보)
- **P1** — 스프린트 `process-master-dedup`:
  1. 기존 1450 → DISTINCT 5 유지하며 중복 삭제: `DELETE FROM tb_process_master WHERE ctid NOT IN (SELECT MIN(ctid) FROM tb_process_master GROUP BY sys_nm_en, process_name)`
  2. UNIQUE 제약 추가: `ALTER TABLE tb_process_master ADD UNIQUE(sys_nm_en, process_name)`
  3. `DbInitRunner` 검토 — `INSERT ... ON CONFLICT DO NOTHING` 또는 skip 조건 추가
  4. `tb_service_purpose` 동일 조치

---

## Phase 2 배치 A 완료 요약

### 주요 발견 3건

| # | 발견 | 유형 | 우선순위 | 제안 스프린트 |
|---|------|------|---------|--------------|
| 1 | `tb_process_master`/`tb_service_purpose` 290배 중복 삽입 | ② | **P1** | `process-master-dedup` |
| 2 | `inspect_check_result.result` 자유 텍스트 혼재 | ② | **P1** | `inspect-result-schema-split` |
| 3 | `UPIS_SW` → `UPIS` 통합 22건 + `inspect_template.APP` 28건 삭제 (**사용자 확정**: UPIS_SW=UPIS, APP 섹션 미사용) | ① + ⑤ | **P1** | `upis-sw-merge-and-app-cleanup` |
| 3-B | 점검자(`insp_*`) → users FK / 확인자(`conf_*`) → ps_info FK 연동 (**사용자 확정**) | ② | **P1** | `inspect-inspector-confirmer-link` |

### 부차 발견

| # | 발견 | 유형 | 우선순위 |
|---|------|------|---------|
| 4 | `tb_work_plan` 1건만 존재 (사실상 미사용) | ④/⑤ | P3 (기능 존속 판단 필요) |
| 5 | `inspect_check_result.category` 공백 차이 2쌍 | ① | P2 |
| 6 | `check_section_mst` 부재 (9종 섹션 코드 하드코딩) | ④ | P2 |

### `#1-A` Enum 유지 확정
- `DocumentStatus`, `DocumentType` 모두 **유지 권고 (점수 4/16)**
- 감사 외 4개 기초 기능의 마스터(`cont_stat_mst`, `maint_tp_mst`)와 **다른 도메인** 확인
- 이전 세션에서 예상했던 롤백 스프린트 **불필요**

---

---

## 기능 3: 견적서 (`qt_quotation`, `qt_quotation_item`, `qt_product_pattern`, `qt_wage_rate`, `qt_remarks_pattern`, `qt_quotation_ledger`)

### 3-1. 사용 테이블 & 레코드 수
- `qt_quotation` (59건) — 견적서 본체
- `qt_quotation_item` (85건) — 품목
- `qt_product_pattern` (23건) — 제품 패턴 마스터
- `qt_wage_rate` (다수) — 인건비 등급·단가
- `qt_remarks_pattern` (7건) — 비고 패턴
- `qt_quotation_ledger` (59건) — 견적 대장
- `qt_quote_number_seq` (3건) — 연도별 카테고리별 순번

### 3-2. 컬럼별 마스터 매칭

| 컬럼 | 값 분포 | 마스터 매칭 | 판정 |
|------|---------|----------|------|
| `qt_quotation.category` | 유지보수 42 / 용역 12 / 제품 5 (한글) | ❌ `prj_types` (GISSW/PKSW/TS, 영문코드)와 **값셋 다름** | **③ 부분 중복** — `prj_types` 와 어느 쪽으로 통합할지 결정 필요 |
| `qt_quotation.status` | 발행완료 59 (Entity 주석: 작성중/발행완료/취소) | ❌ 마스터 없음 | **④ 마스터 신설** (`qt_status_mst` 또는 Enum) |
| `qt_quotation.template_type` | 1=기본(56) / 2=인건비통합(3) | ❌ 숫자 코드, 주석만 있음 | **④ 마스터 신설** (또는 Enum) |
| `qt_quotation_item.unit` | "식" 85건 (단일값) | ❌ 단위 마스터 없음 | **④ 마스터 신설** 또는 현 상태 유지 (단일값 변동 없으면) |
| `qt_product_pattern.category` | 용역 10 / 제품 7 / 유지보수 6 | ✅ `qt_quotation.category`와 **일관** (내부 자체 값셋) | 일관성 있으나 `prj_types`와 별개 |
| `qt_wage_rate.grade_name` | 30+ 등급 (기술사/IT PM/...) | ❌ `users.tech_grade`와 **도메인 다름** (사용자 직급 vs 인건비 산정) | 현 상태 유지 |

### 3-3. 🚨 **`qt_remarks_pattern` 담당자 정보 하드코딩** (중대 발견)
실데이터 예시:
```
"※ 담당자 : SW지원부 박욱진 이사 (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)"
"※ 담당자 : GIS사업부 이동수 이사 (T.070-7113-9894  M.010-9755-1316  F.053-817-9987  E-mail : leeds@uitgis.com)"
```
- 7건 모두 **담당자 성명·직위·전화번호·이메일·팩스**가 자유 텍스트로 **하드코딩**
- **견적서 담당자는 `users` 마스터(10건, 내부 직원)를 참조해야 함** (사용자 확정 2026-04-20)
  - `박욱진`/`이동수`/`여현정`은 모두 **(주)정도유아이티 내부 직원** — `users` 참조 대상
  - `ps_info`는 **고객사 담당자**로 별도 도메인 — 본 견적서 담당자와 무관
- 담당자 퇴사·연락처 변경 시 7건 수동 업데이트 필요

**판정**: **② 중복 테이블/컬럼** — `qt_remarks_pattern.content`에 **내부 직원 정보가 자유 텍스트로 중복 저장**됨. `users`를 FK로 참조해야 함.

### 3-4. 코드 하드코딩
- `"유지보수|용역|제품|작성중|발행완료|취소"` 리터럴 **11곳** (`QuotationService.java` 9, `Quotation.java` 1, `QuotationDTO.java` 1)

### 3-5. 유형 분류 요약

| 발견 | 유형 | 우선순위 |
|------|------|----------|
| `qt_quotation.category` vs `prj_types` 다른 값셋 | ③ 부분 중복 | P2 |
| `qt_quotation.status` 마스터 부재 (한글 하드코딩 59건) | ④ 마스터 신설 | P2 |
| `qt_remarks_pattern` 담당자 하드코딩 (내부 직원 정보 `users` 미참조) | ② 중복 테이블 | **P1** |
| `qt_quotation.template_type` 숫자 매직 넘버 | ④ 마스터 신설 or Enum | P3 |

---

## 기능 4: 라이선스 (`license_registry`, `license_upload_history`)

### 4-1. 사용 테이블 & 레코드 수
- `license_registry` (1,531건) — CSV 업로드 외부 라이선스 레지스트리
- `license_upload_history` (29건) — 업로드 이력

### 4-2. 🚨 **`license_registry.country` 데이터 오염** (중대 발견)

실측 국가 코드 분포:
| 값 | 개수 |
|----|------|
| Korea | 649 |
| (공백) | 600 |
| korea (소문자) | 108 |
| null | 93 |
| **서현규** (이름) | **18** |
| Vietnam | 12 |
| vietnam | 9 |
| **seo hyeon gyu** | 6 |
| **kim hanjun** | 5 |
| Seoul | 4 |
| KOREA | 4 |
| **Park Uk Jin** | 1 |
| **김한준** (이름) | 2 |
| Kroea (오타) | 2 |

**원인 추정**: CSV 업로드 시 **컬럼 매핑 오류** — 등록자 이름(`registered_to` / `full_name`)이 `country` 컬럼으로 잘못 매핑
- `서현규` 18건의 company는 모두 "(주)정도유아이티", "고령군청" 등 — `country`에 이름이 들어간 것 맞음
- 대소문자 불일치 (`Korea` / `korea` / `KOREA`) — CSV 원본 표준화 부재

### 4-3. 컬럼별 마스터 매칭

| 컬럼 | 현재 값 | 마스터 매칭 | 판정 |
|------|---------|----------|------|
| `license_type` | License Text 1509 / Floating License Text 22 | ❌ 마스터 없음, 2종 고정 | **④ 마스터 신설** (또는 Enum) |
| `generation_source` | Manual 1531 (단일값) | ❌ | 상수 유지 |
| `country` | 오염 14종+ | ❌ | **① 데이터 정제** (CSV 업로드 매핑 버그 수정 + 기존 오염값 정제) |
| `product_id` | 15종 외부 제품명 (Maple/LSA/SDK 등) | ❌ 외부 벤더 식별자 | 외부 데이터라 마스터화 제한. 현 상태 유지 |

### 4-4. 유형 분류 요약

| 발견 | 유형 | 우선순위 |
|------|------|----------|
| `country` 컬럼 오염 (CSV 매핑 버그) | ① 데이터 정제 + 버그 수정 | **P1** |
| `license_type` 2종 하드코딩 | ④ 마스터 신설 (Enum) | P3 |
| 대소문자 불일치 (Korea/korea/KOREA) | ① 데이터 정제 | P2 |

---

## 기능 5: GeoNURIS (`geonuris_license`)

### 5-1. 사용 테이블 & 레코드 수
- `geonuris_license` (16건)

### 5-2. 컬럼별 매칭

| 컬럼 | 값 분포 | 마스터 매칭 | 판정 |
|------|---------|----------|------|
| `license_type` | DESKTOP 7 / GSS30 6 / GSS35 3 | ❌ 마스터 없음, 3종 고정 | **④ 마스터 신설** or Enum |

### 5-3. 코드 하드코딩
- `"DESKTOP|GSS30|GSS35"` 리터럴 **11곳** (`GeonurisLicenseService.java` 8, `GeonurisLicense.java` 3)
- 문자열 비교 switch/if 다수

### 5-4. 유형 분류
- **primary_type**: ④ 마스터 신설 (소규모, Enum 대안도 가능)
- 우선순위 P3

---

## Phase 2 배치 B 완료 요약 (기능 3·4·5 추가)

### 주요 발견 (Batch B)

| # | 발견 | 유형 | 우선순위 | 제안 스프린트 |
|---|------|------|---------|--------------|
| 7 | `qt_remarks_pattern` 7건 전부 담당자 정보 하드코딩 (내부 직원, `users` FK 연동 필요) | ② | **P1** | `qt-remarks-users-link` |
| 8 | `license_registry.country` 오염 (CSV 매핑 버그로 사람 이름이 국가에 들어감, 32건+) | ① + 버그 | **P1** | `license-country-cleanup-and-csv-fix` |
| 9 | `qt_quotation.category`와 `prj_types` 값셋 불일치 | ③ | P2 | `qt-category-normalize` |
| 10 | `qt_quotation.status` 한글 하드코딩 (59건) | ④ | P2 | `qt-status-master` |
| 11 | `license_registry` 대소문자 불일치 (Korea/korea/KOREA) | ① | P2 | (#8과 병합 가능) |
| 12 | `geonuris_license.license_type` 3종 하드코딩 | ④ | P3 | `geonuris-license-type-enum` |
| 13 | `qt_quotation.template_type` 매직 넘버 (1/2) | ④ | P3 | — |

---

---

## 기능 6: QR 코드 (`qr_license`)

### 6-1. 레코드 수: **0건** — 미사용 또는 초기 개발 단계
- Entity 코드는 완비 (컨트롤러·서비스·리포지토리 존재)
- 한영 이중 컬럼 구조: `end_user_name` + `end_user_name_ko`, `license_type` + `license_type_ko`, `address` + `address_ko` 등 **10쌍 20컬럼**

### 6-2. 판정
- **primary_type**: ⑤ 레거시 미사용 / secondary: ② 중복 컬럼 (한영 이중)
- **우선순위**: P3
- **판정**: 기능 존속 여부 사용자 판단 필요. 유지한다면 한영 이중 컬럼 구조 재검토.

---

## 기능 7: Access Logs (`access_logs`)

### 7-1. 레코드 수: **3,885건** — 대규모 실사용

### 7-2. 컬럼별 마스터 매칭

| 컬럼 | 값 분포 | 마스터 매칭 | 판정 |
|------|---------|----------|------|
| `menu_nm` | 서버관리(1007), 문서관리(989), 라이선스대장(650), 견적서(576), GeoNURIS라이선스(233), 사업관리(229), 담당자관리(59), 회원관리(53), 업무계획(35), 사업현황(27), 성과통계(19), 마이페이지(4), 회원가입(4) — **13종** | **`MenuName` 상수 클래스 불완전** | **③ 부분 중복** — MenuName 상수에 누락 4종(라이선스대장/견적서/GeoNURIS라이선스/회원가입) 추가 필요 |
| `action_type` | 조회(2121)/수정(835)/미리보기(191)/접근(187)/등록(120)/목록조회(92)/생성(62)/발급폼접근(46)/상세조회(44)/다운로드(31)/삭제(28)/... — **20+종** | ❌ 마스터/Enum 없음 | **④ Enum 또는 상수 클래스 신설** |
| `userid` | admin/hanjun/seohg0801/ukjin914/test02/parksh/yeohj/ybkang/jeongsj (9종 ✅ users 매칭) + **`박욱진`(16), `관리자`(11), `anonymousUser`(4)** (3종 ❌) | 9/12 매칭 | **① 데이터 오염 정제** — 이름이 userid 필드에 잘못 저장된 경우 |

### 7-3. 🚨 `access_logs.userid` 오염 (중대 발견)
- `박욱진` 16건, `관리자` 11건 — **사용자 이름(username)이 userid 필드에 저장**됨
- Spring Security 인증 처리 로직 또는 로깅 유틸에서 `getName()` vs `getUsername()` 혼동 의심
- 추적 시 로그 분석 어려움 + users FK 관계 불가

### 7-4. MenuName 상수 (감사 대상)

`src/main/java/com/swmanager/system/constants/MenuName.java`:
```java
// 현재 11종: DASHBOARD, PROJECT, PERSON, INFRA, USER, LOG, MYPAGE, CONTRACT, WORK_PLAN, DOCUMENT, PERFORMANCE
// DB 실사용 13종 중 누락 4종: 라이선스대장, 견적서, GeoNURIS라이선스, 회원가입
```
- **유형 ③** — 상수 클래스와 DB 실값 불일치

### 7-5. 유형 분류 요약

| 발견 | 유형 | 우선순위 |
|------|------|----------|
| `userid` 오염 (이름이 userid에 저장) 27건 | ① 데이터 정제 + 로깅 버그 수정 | **P1** |
| `action_type` 20종 마스터 부재 | ④ Enum/상수 신설 | P2 |
| `MenuName` 상수 불완전 (4종 누락) | ③ 부분 중복 | P2 |

---

## 기능 8: 추가 테이블 실측 (Batch C 후속)

| 테이블 | 레코드 | 판정 |
|--------|--------|------|
| `tb_contract` (30컬럼) | **0건** | **⑤ 완전 고아 — DROP 확정 (사용자 2026-04-20)** — Java Entity·Repository·Service·Controller 모두 없음. 이전 감사(2026-04-18 C3-3-1)에서 ERD 제거 조치 완료(2026-04-19 스프린트 2a). **기능 매핑**: `sw_pjt`의 `cont_ent`/`cont_dept`/`cont_dt`/`cont_amt`/`cont_type`/`maint_type`/`org_nm`/`dist_nm` + `ps_info` + `sigungu_code`로 완전 대체 가능 |
| `tb_contract_target` (8컬럼) | **0건** | **DROP 확정 (사용자 2026-04-20)** — `tb_contract` FK 관계라 함께 제거 |
| `tb_inspect_checklist` (9컬럼: check_id/doc_id/inspect_month/target_sw/check_item/check_method/check_result/sort_order) | **0건** | **⑤ 레거시 + ② 중복** — `inspect_check_result`와 **동일 기능** 중복 설계 → 삭제 권고 |
| `tb_inspect_issue` (9컬럼: issue_id/doc_id/issue_year/issue_month/issue_day/task_type/symptom/action_taken) | **0건** | **⑤ 레거시 + ② 중복** — `inspect_visit_log`와 **동일 기능** 중복 설계 → 삭제 권고 |
| `tb_document_signature` | **0건** | **② or ⑤** — 현재 `inspect_report.insp_sign`/`conf_sign` TEXT 컬럼에 서명 저장 중. 이 테이블은 중복 설계 |
| `pjt_equip` (10컬럼) | **0건** | ④ or ⑤ — 사업관리 확장(`sw_pjt.proj_id` FK)이나 미사용. 사용자 확인 필요 |
| `tb_performance_summary` (18컬럼 통계) | **0건** | 정상 — 성과 계산 캐시라 실사용 시 채워짐 |
| `tb_document_history` | **13건** | 정상 사용 중 |
| `tb_org_unit` | **39건** | 감사 외 (사용자관리 하위 — §2-1 B) |

### 감사 대상 전체 조감 — **중대 구조 문제**

감사 대상 34개 테이블 중 **약 8개(24%)가 0건 미사용**:
- `tb_contract`, `tb_contract_target`, `tb_inspect_checklist`, `tb_inspect_issue`, `tb_document_signature`, `qr_license`, `pjt_equip`, `tb_performance_summary` (캐시 테이블 제외하면 7개)
- **특히 `tb_inspect_checklist`/`tb_inspect_issue`는 `inspect_check_result`/`inspect_visit_log`와 기능 완전 중복** — 과거 설계 흔적이 정리 안 됨

---

## Phase 2 배치 C 완료 요약 (기능 6·7·8 추가)

### 주요 발견 (Batch C)

| # | 발견 | 유형 | 우선순위 | 제안 스프린트 |
|---|------|------|---------|--------------|
| 14 | `access_logs.userid` 오염 (이름이 userid에 저장, 27건+) | ① + 로깅 버그 | **P1** | `access-log-userid-fix` |
| 15 | `access_logs.action_type` 20종 자유 텍스트 | ④ Enum/상수 | P2 | `access-log-action-enum` |
| 16 | `MenuName` 상수 4종 누락 (라이선스대장/견적서/GeoNURIS라이선스/회원가입) | ③ | P2 | `menu-name-sync` |
| 17 | `qr_license` 미사용 (0건) + 한영 이중 컬럼 20개 | ⑤ + ② | P3 | `qr-license-decision` (기능 존속 판단 필요) |
| 18 | `tb_inspect_checklist` 0건, `inspect_check_result`와 **완전 중복 기능** | ⑤ + ② | P2 | `legacy-inspect-tables-drop` |
| 19 | `tb_inspect_issue` 0건, `inspect_visit_log`와 **완전 중복 기능** | ⑤ + ② | P2 | (#18과 병합) |
| 20 | `tb_document_signature` 0건, `inspect_report.insp_sign`/`conf_sign`과 중복 | ② | P2 | (#18과 병합) |
| 21 | `tb_contract`/`tb_contract_target` DROP 확정 (사용자 2026-04-20 — `sw_pjt` + `ps_info` + `sigungu_code`로 완전 대체) | **⑤ DROP** | **P2** | `legacy-contract-tables-drop` |
| 22 | `pjt_equip` 0건 — 기능 존속 판단 필요 | ⑤ or ④ | P3 | `pjt-equip-decision` |

---

## Phase 2 전체 종료 — 누적 P1 스프린트 정리

| 스프린트 | 배치 | 주요 내용 |
|---------|------|-----------|
| `process-master-dedup` | A | 1450건 → 5건 정제 + UNIQUE 제약 + DbInitRunner 검토 |
| `inspect-*-redesign` | A | 문서관리(inspect_*) 전체 스키마 재설계 — result 분리, 테스트 단계로 자유 재설계 |
| `upis-sw-merge-and-app-cleanup` | A | UPIS_SW → UPIS 통합 22건, APP 섹션 정리 |
| `inspect-inspector-confirmer-link` | A | 점검자 users FK / 확인자 ps_info FK (오입력·누락 구조적 방지) |
| `qt-remarks-users-link` | B | 견적서 비고 담당자 하드코딩 → users FK |
| `license-country-cleanup-and-csv-fix` | B | CSV 매핑 버그 수정 + country 데이터 정제 |
| `access-log-userid-fix` | C | access_logs.userid 이름 오염 정제 + 로깅 버그 수정 |

**총 P1 스프린트 7건**. Phase 3에서 의존성·일정 기반 로드맵 편성.

---

## Phase 2 완료. Phase 3 (로드맵) 대기 중
