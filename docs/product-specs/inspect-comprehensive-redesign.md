---
tags: [plan, sprint, inspect, schema, redesign, wave-2]
sprint: "inspect-comprehensive-redesign"
priority: P1
wave: 2
status: draft
created: "2026-04-21"
---

# [기획서] 문서관리(inspect_*) 종합 재설계 (Wave 2 대형 통합)

- **작성팀**: 기획팀
- **작성일**: 2026-04-21
- **근거 로드맵**: [[data-architecture-roadmap]] v2 §S1 (Wave 2 / P1)
- **상태**: v2 (codex v1 ⚠ 수정필요 4건 반영 + 실측 재조사)
- **v1→v2 변경점**:
  1. **A6 재설계** — section 값 실측 결과 **9종** (DB/AP/DBMS/GIS/APP + DB_USAGE/AP_USAGE/DBMS_ETC/APP_ETC) 기반으로 마스터 시드 재정의
  2. **범위 확장** — `doc-inspect.html`, `db_init_phase2.sql`, `InspectPdfService.java` 명시적 포함
  3. **S7 재분류** — 서명 기능 2경로(inspect_report.insp_sign + tb_document_signature) 인지 → 대체 정책 명시
  4. **R-1 구체화** — 단계별 exit gate / de-scope 기준 명시

---

## 0. 개요

### 0-1. 배경
- **문서관리(inspect_*) 기능은 테스트 단계** — 사용자 확정(2026-04-20): "문서관리쪽은 아직 테스트를 진행 중이므로 개편해도 상관없으면 지워도 상관없음"
- 즉, 데이터 보존 제약이 없으므로 **스키마 재구성 + 테스트 데이터 초기화 허용**
- 감사(§S1) 결과 누적된 구조적 부채를 **한 번의 대형 스프린트**로 해결

### 0-2. 본 스프린트가 해결하는 6개 조치 (로드맵 §S1 기준)

| # | 조치 | 요약 |
|---|------|------|
| A1 | `inspect_check_result.result` 스키마 분리 | 자유 텍스트 → `result_code` (정상/부분정상/비정상/해당없음) + `result_text` (사유) |
| A2 | `UPIS_SW` → `UPIS` 통합 | 22건 레거시 값 정리 (inspect_template 20 + inspect_report 1 + tb_document 1) |
| A3 | `inspect_template.APP` 섹션 정리 | UPIS/UPIS_SW APP 28건 중복 제거 (정책: APP 섹션 전체 제거 또는 하나로 통합) |
| A4 | 점검자 FK → users / 확인자 FK → ps_info | `insp_user_id` + `conf_ps_info_id` 컬럼 추가, `insp_name/insp_company`·`conf_name/conf_org` 삭제 |
| A5 | `inspect_check_result.category` 공백 정규화 | "(GWS)" 2표기 통합 등 |
| A6 | `check_section_mst` 마스터 신설 | **실측 9종** (DB/AP/DBMS/GIS/APP + DB_USAGE/AP_USAGE/DBMS_ETC/APP_ETC) → 정책에 따라 축소·분리 (§3-C 재설계) + FK 연결 |

### 0-3. 본 스프린트가 흡수하는 후속 스프린트

| 흡수 스프린트 | 내용 |
|---------------|------|
| **S7** `legacy-inspect-tables-drop` | **v2 축소**: `tb_inspect_checklist`, `tb_inspect_issue` **2개 테이블** + 관련 Entity/Repository/Service 메서드 DROP. `tb_document_signature` 는 §3-E E-opt1 권장에 따라 **DROP 대상에서 제외** (서명 API 영향 방지) |
| **S17** `inspect-template-cleanup` | inspect_template APP 섹션 정리 (본 기획 A3와 동일) |

---

## 1. 목적

- **FR-1** (A1): `inspect_check_result.result` VARCHAR(500) → `result_code` VARCHAR(20) + `result_text` VARCHAR(500) 분리
- **FR-2** (A2): `UPIS_SW` 리터럴을 DB 22건과 코드(Thymeleaf/Service) 전체에서 제거, `UPIS` 단일 값으로 통합
- **FR-3** (A3): inspect_template APP 섹션 정책 결정 후 적용 (§3에서 사용자 결정 요청)
- **FR-4** (A4): FK 연동 — `insp_user_id` BIGINT FK→users, `conf_ps_info_id` BIGINT FK→ps_info, 기존 문자열 4 컬럼 삭제
- **FR-5** (A5): `inspect_check_result.category` TRIM + 공백 정규화 + 유사 표기 통합
- **FR-6** (A6): `check_section_mst` 테이블 신설 + `inspect_check_result.section` / `inspect_template.section` FK 연결
- **FR-7** (S7 흡수, v2 축소): `tb_inspect_checklist` / `tb_inspect_issue` **2개 테이블** DROP + 관련 Entity/Repository/Service 메서드/Controller 호출부 제거. `tb_document_signature` 는 E-opt1 권장으로 **본 스프린트에서 유지**
- **FR-8** (테스트 데이터 초기화): inspect_report / inspect_check_result / inspect_visit_log / inspect_template 데이터 전량 TRUNCATE 후 재시드 (사용자 허용 전제)

---

## 2. 범위

### 2-1. 포함 (v2 확장)
- DB 스키마 변경 (ALTER + NEW TABLE + DROP TABLE)
- inspect 관련 Entity / DTO / Service / Controller / Repository 수정
- **Thymeleaf 템플릿** (v2 명시):
  - `src/main/resources/templates/inspect-detail.html`
  - `src/main/resources/templates/inspect-preview.html`
  - `src/main/resources/templates/document/doc-inspect.html` ← **v2 신규**
  - 기타 inspect 관련 파셜 (개발계획에서 `rg 'UPIS_SW\|inspUserId\|section'` 결과 전수)
- **Java 코드** (v2 명시):
  - `DocumentController.java` (line 1737-1740, 1774-1777 section 분기 + signature API 609)
  - `DocumentService.java` (L220~267 InspectChecklist/Issue/Signature save/get 메서드)
  - `InspectPdfService.java` (L48-51 section 분기)
- **SQL 시드** (v2 신규): `src/main/resources/db_init_phase2.sql` 의 UPIS_SW 리터럴 제거
- V022 마이그레이션 SQL 단일 파일 (트랜잭션 내 모든 조치)
- 테스트 데이터 재시드 SQL (선택적, §3-D 결정 후)
- 회귀 테스트 신규 + 기존 InspectReport 관련 테스트 갱신

### 2-2. 제외
- Inspect 기능의 신규 화면/UX 추가 (별도 S10 이후로)
- `inspect_check_result.category` 를 별도 마스터(`check_category_mst`)로 분리하는 건 — **S10에서 수행** (본 스프린트는 정규화만)
- 기존 작성 중인 inspect_report 데이터 보존 — 테스트 단계 초기화 허용으로 고려 안 함
- 사용자가 명시적으로 '영구 패스'한 라이선스 스프린트(S4/S11 등)

---

## 3. 사용자 결정 필요 사항 (**승인 필수**)

### 3-A. A3 APP 섹션 정책 (택 1)

| 옵션 | 설명 | 장점 | 단점 |
|------|------|------|------|
| **A3-opt1 (권장)** | APP 섹션 **완전 제거** (inspect_template.section='APP' 전체 DELETE) | 가장 단순. "UPIS APP/UPIS_SW APP 사용 안함" 사용자 확정 (2026-04-20) 기반 | 향후 APP 섹션 복구 시 재정의 필요 |
| A3-opt2 | APP → `AP`로 통합 (28→≤4건) | 섹션 이름 표준화 | AP/APP 구분이 원래 다른 의미면 정보 손실 |
| A3-opt3 | APP 보존 | 변경 없음 | 감사 지적 "중복 28건" 미해소 |

**기획 권장**: **A3-opt1 (완전 제거)** — 사용자 발언 "UPIS APP와 UPIS_SW APP는 사용하지 않음" 에 근거

### 3-B. A4 점검자/확인자 마이그레이션 정책 (택 1)

| 옵션 | 설명 |
|------|------|
| **B-opt1 (권장)** | 기존 insp_name/conf_name **데이터 삭제** (테스트 데이터 초기화 허용), FK 컬럼만 새로 추가 |
| B-opt2 | 기존 문자열 → FK로 **매핑 스크립트 작성** (username → users.userid / name → ps_info.id) |

**기획 권장**: **B-opt1** — 테스트 단계라는 사용자 전제 근거. B-opt2는 ambiguous 매핑 발생 우려

### 3-C. A6 check_section_mst 마스터 재설계 (v2, 실측 기반)

**실측 결과**: 현재 코드베이스에는 **9종** section 값 분기 존재
- 기본 5종: `DB, AP, DBMS, GIS, APP` (inspect-detail/preview 계열)
- 확장 4종: `DB_USAGE, AP_USAGE, DBMS_ETC, APP_ETC` (doc-inspect/InspectPdfService)
- 즉 **doc-inspect.html** 용 점검 항목은 `*_USAGE`/`*_ETC` 접미사를 쓰며, inspect-detail 용은 접미사 없음. 2개 시스템이 공존

**3가지 옵션**:

| 옵션 | 설계 | 마스터 행수 | 장점 | 단점 |
|------|------|-------------|------|------|
| **C-opt1 (권장)** | **9행 전부 시드** — 현재 사용 값 그대로 마스터화. FK 제약 안전 | 9 | 즉시 배포 가능 + FK 무결성 확보 | 의미적 중복 존재 (DB vs DB_USAGE) |
| C-opt2 | **2층 구조** — `section_type` ∈ {DB/AP/DBMS/GIS/APP} + `section_detail` ∈ {USAGE/ETC/NULL}. 컬럼 2개로 분리 | 5 + 3 | 설계상 깔끔 | 마이그레이션 복잡 + 코드 대폭 수정 필요 |
| C-opt3 | **5행으로 축소** — `*_USAGE`/`*_ETC` 를 접미사 제거 후 base 5로 통합. UPDATE 대량 발생 | 5 | 가장 단순 | 기존 doc-inspect/InspectPdfService 분기 의미 소실 |

**기획 권장**: **C-opt1 (9행 시드)** — 리스크 최소, 추후 C-opt2 구조화는 별도 스프린트로 분리 권장

초기 시드안 (C-opt1):

| section_code | section_label | section_group | display_order |
|--------------|---------------|---------------|---------------|
| DB | DB 점검 | CORE | 1 |
| AP | AP 점검 | CORE | 2 |
| DBMS | DBMS 운영 | CORE | 3 |
| GIS | GIS 엔진 | CORE | 4 |
| APP | 응용프로그램 | CORE | 5 |
| DB_USAGE | DB 사용 현황 | DETAIL | 11 |
| AP_USAGE | AP 사용 현황 | DETAIL | 12 |
| DBMS_ETC | DBMS 기타 | DETAIL | 13 |
| APP_ETC | APP 기타 | DETAIL | 14 |

### 3-E. S7 서명 기능 대체 정책 (v2 신규)

**현황**:
- `inspect_report.insp_sign` / `conf_sign` — 점검자·확인자 전자서명 (inspect_report 전용)
- `tb_document_signature` — 일반 문서 서명 (`/api/signature/save` + `documentService.saveSignature`, 모든 Document 유형)

**2가지 옵션**:

| 옵션 | 설명 | 장점 | 단점 |
|------|------|------|------|
| **E-opt1 (권장)** | `tb_document_signature` **유지** (S7 DROP 대상에서 **제외**). inspect_checklist/issue만 DROP | 서명 기능 영향 0 + 감사 지적(0건 중복)을 tb_inspect_checklist/issue 2개로만 제한 | 로드맵 §S7 범위와 다름 (문서화 필요) |
| E-opt2 | `tb_document_signature` 도 DROP + `/api/signature/save` API 제거 + Document 서명 기능을 inspect_report.insp_sign 경로로 통합 | 완전 정리 | 서명 UX 재설계 + Document 하위 타입별 서명 지원 재구현 |

**기획 권장**: **E-opt1** — 테스트 단계라 해도 서명 API는 다른 문서 유형(비점검)에서도 쓰이므로 범위 축소 권장. S7의 DROP 대상을 **2개 테이블**(`tb_inspect_checklist`, `tb_inspect_issue`)로 축소

### 3-F. InspectResultCode Enum 코드 4종 (v2.1 독립 섹션화)

**승인 요청 항목 #6**. §4-3 참조.

| code | label | 의미 |
|------|-------|------|
| NORMAL | 정상 | 점검 결과 이상 없음 |
| PARTIAL | 부분정상 | 일부 이상 있으나 운영 가능 |
| ABNORMAL | 비정상 | 즉시 조치 필요 |
| NOT_APPLICABLE | 해당없음 | 점검 대상 아님 |

- DB 저장값은 code(enum name). UI 표시는 label. (기존 AccessActionType/DocumentStatus 패턴 재사용)
- 변경 시 NFR-7 label Freeze 적용 (향후 별도 마이그 필요)

### 3-D. FR-8 테스트 데이터 처리 (택 1)

| 옵션 | 설명 |
|------|------|
| **D-opt1 (권장)** | inspect_report / inspect_check_result / inspect_visit_log **전량 TRUNCATE** 후 개발계획 단계에서 신규 시드 SQL 준비 |
| D-opt2 | 기존 데이터 유지, 스키마 변경만 (FR-1의 result_code는 전부 NULL 허용) |

**기획 권장**: **D-opt1** — 테스트 단계 + 스키마 대대적 변경이므로 클린 상태가 더 안전

---

## 4. 제안 설계

### 4-1. DB 스키마 변경 (V022 예정)

```sql
-- A6. 섹션 마스터 (v2 C-opt1 9행 시드)
CREATE TABLE check_section_mst (
  section_code VARCHAR(20) PRIMARY KEY,
  section_label VARCHAR(100) NOT NULL,
  section_group VARCHAR(20) NOT NULL DEFAULT 'CORE',  -- CORE|DETAIL
  display_order INT NOT NULL DEFAULT 0
);
INSERT INTO check_section_mst (section_code, section_label, section_group, display_order) VALUES
  ('DB',       'DB 점검',       'CORE',   1),
  ('AP',       'AP 점검',       'CORE',   2),
  ('DBMS',     'DBMS 운영',     'CORE',   3),
  ('GIS',      'GIS 엔진',      'CORE',   4),
  ('APP',      '응용프로그램',  'CORE',   5),
  ('DB_USAGE', 'DB 사용 현황',  'DETAIL', 11),
  ('AP_USAGE', 'AP 사용 현황',  'DETAIL', 12),
  ('DBMS_ETC', 'DBMS 기타',     'DETAIL', 13),
  ('APP_ETC',  'APP 기타',      'DETAIL', 14);

-- A1. result 분리
ALTER TABLE inspect_check_result
  ADD COLUMN result_code VARCHAR(20),
  ADD COLUMN result_text VARCHAR(500);
-- 기존 result 컬럼은 코드 전환 완료 후 DROP (2단계)
UPDATE inspect_check_result SET result_code='NORMAL', result_text=result;  -- 초기화 정책 기반

-- A2. UPIS_SW → UPIS
UPDATE inspect_template SET template_type='UPIS' WHERE template_type='UPIS_SW';
UPDATE inspect_report SET sys_type='UPIS' WHERE sys_type='UPIS_SW';
UPDATE tb_document SET sys_type='UPIS' WHERE sys_type='UPIS_SW';

-- A3. APP 섹션 제거 (opt1)
DELETE FROM inspect_template WHERE section='APP';

-- A4. FK 전환
ALTER TABLE inspect_report
  ADD COLUMN insp_user_id BIGINT REFERENCES users(user_id),
  ADD COLUMN conf_ps_info_id BIGINT REFERENCES ps_info(id);
-- 기존 insp_name/insp_company/conf_name/conf_org 컬럼 DROP (초기화 정책 시 즉시, 매핑 정책 시 후속)
ALTER TABLE inspect_report
  DROP COLUMN insp_name, DROP COLUMN insp_company,
  DROP COLUMN conf_name, DROP COLUMN conf_org;

-- A5. category 정규화
UPDATE inspect_check_result SET category = TRIM(category);
-- "(GWS) " 등 특수 공백 정규화는 개발계획 단계에서 실측 후 확정

-- A6. FK 연결
ALTER TABLE inspect_check_result
  ADD CONSTRAINT fk_icr_section FOREIGN KEY (section) REFERENCES check_section_mst(section_code);
ALTER TABLE inspect_template
  ADD CONSTRAINT fk_it_section FOREIGN KEY (section) REFERENCES check_section_mst(section_code);

-- FR-7. 레거시 DROP (S7 흡수, v2 축소)
DROP TABLE IF EXISTS tb_inspect_checklist;
DROP TABLE IF EXISTS tb_inspect_issue;
-- tb_document_signature 는 §3-E E-opt1 권장으로 유지
```

**주의**: 위는 설계 방향 예시. 실제 V022 SQL은 개발계획 단계에서 사전검증 + 트랜잭션 + 게이트 포함해 재작성.

### 4-2. Java 코드 변경

| 영역 | 변경 |
|------|------|
| `InspectReport.java` | insp_name/company/conf_name/org 4 필드 삭제, `inspUserId`/`confPsInfoId` 추가 + @ManyToOne 선택적 |
| `InspectReportDTO.java` | 동일 필드 재매핑 (userName/psInfoName 을 getter로 제공) |
| `InspectCheckResult.java` | `result` → `result_code` + `result_text` 분리 |
| `InspectTemplate.java` | section FK 매핑 (String 유지, FK 제약만 DB) |
| `DocumentController/Service.java` | **v2 축소**: `InspectChecklist`/`InspectIssue` Repository 주입·호출·해당 save/get 메서드 DELETE. `DocumentSignature` 및 `/api/signature/save` 엔드포인트는 **유지** (§3-E E-opt1) |
| `InspectChecklist.java`, `InspectIssue.java` | 파일 자체 DELETE (2개). `DocumentSignature.java` 는 **유지** |
| 관련 Repository 2개 (Checklist/Issue) | 파일 DELETE. `DocumentSignatureRepository` 는 **유지** |
| `inspect-detail.html`, `inspect-preview.html` | `th:if="${report.sysType == 'UPIS_SW'}"` 분기 제거 (UPIS로 통합) + 점검자/확인자 표시를 User·PsInfo 조인 기반으로 |

### 4-3. 신규 ResultCode Enum (A1 지원)

```java
public enum InspectResultCode {
    NORMAL("정상"), PARTIAL("부분정상"),
    ABNORMAL("비정상"), NOT_APPLICABLE("해당없음");
}
```

---

## 5. FR / NFR

### FR
- FR-1 ~ FR-8: §1 참조

### NFR
- **NFR-1**: V022 마이그레이션은 단일 트랜잭션 + 사전/사후 게이트 + 백업 스냅샷 (S5/S6 패턴 재사용)
- **NFR-2**: 기존 inspect 관련 Thymeleaf 화면이 서버 기동 후 정상 렌더링 (최소 3건 수동 스모크)
- **NFR-3**: 회귀 테스트 신규 ≥ 7건 (각 FR별 최소 1건 + check_section_mst 시드 검증)
- **NFR-4**: 리터럴 잔존 검증 — `rg -n '"UPIS_SW"' src/main/java src/main/resources` = 0 hits
- **NFR-5**: 컴파일 + `./mvnw test` BUILD SUCCESS
- **NFR-6**: 서버 기동 에러 0 + 기동 후 `inspect_check_result` CRUD 스모크 정상
- **NFR-7**: FR-7 삭제 대상 테이블 관련 Java 심볼 컴파일 에러 0 (제거 누락 방지)

---

## 6. 리스크 / 완화

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | 대형 스프린트 범위 팽창 | **높음** | **v2 구체화**: 단계별 Exit Gate — ① A2(UPIS_SW 통합) 후 서버 기동 + rg 0 hits → green 아니면 A3 중단 / ② A6 마스터 시드 후 FK ADD + 사전 COUNT 게이트 통과 → FAIL 시 A6 rollback / ③ A4(FK 전환) 후 `./mvnw test` pass → FAIL 시 S7 중단. **De-scope 기준**: 3일 경과 + 1개 조치라도 게이트 FAIL 시 A5/A6는 다음 스프린트로 분리 |
| R-2 | InspectChecklist/Issue 숨은 참조 (v2.1: Signature 제외) | 중 | NFR-7 컴파일 에러 0 게이트 + 회귀 테스트 |
| R-3 | Thymeleaf UPIS_SW 분기 제거 시 화면 깨짐 | 중 | 개발계획 Step에서 Grep 결과 기반 전수 치환 + 수동 스모크 3건 |
| R-4 | result 컬럼 분리 시 기존 데이터 손실 | 낮음 | 3-D TRUNCATE 정책 전제. 보존 옵션 선택 시 백업 테이블 필수 |
| R-5 | FK 제약으로 기존 row 삽입 실패 | 중 | 4-1 UPDATE 순서를 조정 (먼저 값 통합 → FK 추가) |
| R-6 | 배포 후 운영 영향 | 낮음 | 문서관리는 테스트 단계 (운영 영향 0 확정) |

---

## 7. 대안 / 비채택

| 대안 | 설명 | 비채택 사유 |
|------|------|-------------|
| 조치 6건 개별 분리 스프린트화 | A1~A6 각자 독립 | 동일 테이블 반복 변경·배포 → 오히려 위험 증가. 감사 권고도 통합 |
| result를 JSONB 컬럼으로 변환 | 확장성 | 과잉 설계. 현재 요구는 code + text 2필드면 충분 |
| UPIS_SW 보존 | 하위 호환 | 사용자 "UPIS_SW는 UPIS로 해도 무방" 확정 |

---

## 8. 사용자 승인 요청 사항

### 결정 필요 항목 (v2 업데이트)
1. **A3 APP 섹션 정책** — §3-A (권장: **opt1 완전 제거** — 단, `APP_ETC` 데이터는 별개로 3-C에 따라 마스터에 유지)
2. **A4 마이그레이션 정책** — §3-B (권장: **B-opt1 초기화**)
3. **A6 check_section_mst 설계** — §3-C (v2 권장: **C-opt1 9행 시드**)
4. **FR-8 테스트 데이터 처리** — §3-D (권장: **D-opt1 TRUNCATE**)
5. **S7 서명 기능 정책** — §3-E (v2 신규, 권장: **E-opt1** — tb_document_signature 유지, tb_inspect_checklist/issue만 DROP)
6. **InspectResultCode Enum 코드 4종** — NORMAL/PARTIAL/ABNORMAL/NOT_APPLICABLE (§4-3)

### 승인 확인 사항
- [ ] §0-2 조치 6건 범위 (A1~A6)
- [ ] §0-3 S7/S17 흡수 (v2 축소: S7 = Checklist/Issue 2개만 DROP, Signature 유지)
- [ ] §3 사용자 결정 **6건** (A3 / A4 / A6 / FR-8 / S7-Signature / ResultCode)
- [ ] §5 FR-1~FR-8, NFR-1~NFR-7
- [ ] §6 리스크 완화 (특히 R-1 단계별 Exit Gate + De-scope 기준)
