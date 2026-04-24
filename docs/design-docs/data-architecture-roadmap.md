---
tags: [plan, roadmap, data-architecture]
sprint: "data-architecture-audit"
phase: 3
status: draft
created: "2026-04-20"
---

# 데이터 아키텍처 개선 로드맵 (Phase 3)

- **근거 감사**: [[data-architecture-utilization-audit]] (Batch A/B/C 완료)
- **근거 기획서**: [[data-architecture-audit]] v2
- **작성일**: 2026-04-20
- **상태**: ✅ **v2 최종승인** (codex 승인 + 사용자 최종승인 2026-04-20)

### v2 주요 변경 (codex v1 피드백 5건 반영)
1. §1-3 **우선순위 재정렬표** 신설 — 감사 유형 ↔ 로드맵 P레벨 매핑 + 상향/하향 사유
2. §2 **S7/S17 정리** — S7은 S1 하위로 편입, S17은 "closed-by-S1"로 변경
3. §3-2 **Wave 배치 수정** — Wave 1에서 S7 제거
4. §5 **완료기준 정량화** + Wave별 **SQL 검증 쿼리** 추가
5. §6 **일정 버퍼** — 기본 7주 + 최대 +2주 버퍼 시나리오(총 9주 상한), 의사결정 필요 스프린트에 `decision deadline` 컬럼

---

## 🚫 영구 패스 (사용자 결정, 2026-04-20)

다음 라이선스 관련 스프린트는 **운영 정책상 수동 한글화 작업 진행 중**이며 자동 정제/Enum 도입이 부적합. **본 로드맵에서 영구 제외**:

- **S4** `license-country-cleanup-and-csv-fix` (license_registry.country)
- **S11** `license-registry-type-enum` (license_registry.license_type)
- **S12** `geonuris-license-type-enum` (geonuris_license.license_type)
- **S14** `qr-license-decision`
- **S13** (S4 병합 → 자동 패스)

향후 디자인/데이터 작업 시 **Claude는 위 스프린트를 절대 추천하지 않음**. 사용자가 명시적으로 다시 요청할 때만 진행.

## 0. 개요

### 0-1. 감사 결과 핵심 수치

| 항목 | 건수 |
|------|------|
| 감사 대상 테이블 | 34개 |
| 사용 중 | 18개 |
| **0건 미사용 (레거시)** | **7개** |
| 기초 4개 기능 소유 | 14개 (감사 외) |
| 공통 마스터 | 6개 (감사 외) |

### 0-2. 주요 발견 유형 분포 (23개 발견 사례)

| 유형 | 건수 | 비율 |
|------|------|------|
| ① 코드 하드코딩 + 데이터 정제 | 5 | 22% |
| ② 중복 테이블/컬럼 (FK 연동 필요) | 7 | 30% |
| ③ 부분 중복 (정규화 미흡) | 3 | 13% |
| ④ 마스터 신설 필요 | 5 | 22% |
| ⑤ 레거시 미사용 (DROP 후보) | 3 | 13% |

**핵심 패턴**: 전체의 **65%(②③④)가 마스터 활용 부재** — 사용자 지적 그대로 "기초 데이터를 참고하지 않고 재조합해 만든" 문제 입증.

### 0-3. `#1-A` Enum 재평가 결과: **유지 확정** (롤백 불필요)
- `DocumentStatus`, `DocumentType` 모두 §7-2 결정 매트릭스 점수 4/16 → **유지**
- 의심했던 `cont_stat_mst`/`maint_tp_mst`와 **다른 도메인** 확정

---

## 1. 로드맵 우선순위 체계

### 1-1. 우선순위 기준
- **P1**: 운영 안정성·데이터 품질 위협 or 사용자가 직접 확정한 정제 대상
- **P2**: 구조적 개선 (마스터 활용·레거시 정리) — 시스템 확장성↑
- **P3**: 품질 향상 (Enum/상수 정비·선택적 결정)

### 1-2. 통합 vs 분리 원칙
- 문서관리(`inspect_*`, `tb_document_*`) 계열은 **테스트 단계**이므로 하나의 "**inspect-comprehensive-redesign**" 대형 스프린트로 통합 가능
- 독립적 기능(견적서·라이선스·access_logs)은 **개별 스프린트**로 분리

### 1-3. 우선순위 재정렬표 (v2 codex 권장 #1)

감사 문서의 발견 유형과 본 로드맵의 P레벨이 일치하지 않는 경우를 명시적으로 정리:

| 스프린트 | 감사 유형 (primary) | 감사 우선순위 | 로드맵 P레벨 | 상향/하향 사유 |
|---------|--------------------|---------------|--------------|----------------|
| S1 inspect-comprehensive-redesign | ② 스키마 재구성 (통합) | P1 | **P1** | 유지 |
| S2 process-master-dedup | ② 스키마 | P1 | **P1** | 유지 |
| S3 qt-remarks-users-link | ② 중복 테이블 | P1 | **P1** | 유지 |
| S4 license-country-cleanup-and-csv-fix | ① 데이터 정제 + 버그 | P1 | **P1** | 유지 |
| S5 access-log-userid-fix | ① + 로깅 버그 | P1 | **P1** | 유지 |
| S6 legacy-contract-tables-drop | ⑤ 레거시 DROP | 감사 P2 → **P1** | **P1 (↑ 상향)** | 사용자 2026-04-20 **DROP 확정**(명시적 승인). 또 기존 감사(2026-04-18) 후속 작업 성격이라 지연 이유 없음. |
| S7 legacy-inspect-tables-drop | ⑤ 레거시 DROP | 감사 P2 | **S1 하위 태스크로 편입** (단독 실행 X) | S1에 흡수 — Wave 1 독립 실행 목록에서 제거 (v2 codex #2) |
| S8 qt-quotation-domain-normalize | ③/④ | 감사 P2 | **P2** | 유지. 단, 사용자 정책 결정(prj_types 통일) 필요 — 의사결정 SLA 적용 |
| S9 access-log-action-and-menu-sync | ④/③ | 감사 P2 | **P2** | 유지 |
| S10 inspect-check-result-category-master | ④ 마스터 신설 | 감사 P2 | **P2** | 유지 (S1 완료 후 실행) |
| S11 license-registry-type-enum | ④ Enum | 감사 P3 → **P2** | **P2 (↑ 상향)** | `license-country-cleanup-and-csv-fix`(S4)와 같은 `license_registry` 테이블을 건드리므로 **같은 Wave 실행 권장** — 중복 배포 방지. 의존성 정리 차원 |
| S12 geonuris-license-type-enum | ④ Enum | 감사 P3 | **P3** | **원래대로 환원** (v2 수정). 별도 테이블 건드리므로 S11과 분리 |
| S13 license-registry-case-normalize | ① | 감사 P2 | **S4에 병합** | 흡수 |
| S14 qr-license-decision | ⑤/② | 감사 P3 | **P3** | 유지 (사용자 의사결정 선행) |
| S15 pjt-equip-decision | ⑤/④ | 감사 P3 | **P3** | 유지 (사용자 의사결정 선행) |
| S16 tb-work-plan-decision | ⑤/④ | 감사 P3 | **P3** | 유지 (사용자 의사결정 선행) |
| ~~S17 inspect-template-cleanup~~ | — | — | **closed-by-S1** (별도 섹션) | S1에 완전 포함, 별도 스프린트 불가 (v2 codex #3) |

**상향·하향 요약**: S6·S11만 상향. S12는 v1에서 P2였으나 v2에서 P3로 환원. S17은 삭제.

---

## 2. 후속 스프린트 목록

### 🔴 P1 — 즉시 처리 권장 (독립 6건 + S1 하위 태스크)

#### S1. `inspect-comprehensive-redesign` ⭐ (통합 스프린트) ✅ **완료 (2026-04-22)**
- **배경**: 문서관리 테스트 단계로 스키마 재설계·데이터 초기화 허용 (사용자 확정)
- **포함 조치 6건**:
  1. `inspect_check_result.result` → `result_code` + `result_text` 스키마 분리 (자유 텍스트 정규화)
  2. `UPIS_SW` → `UPIS` 통합 (22건: inspect_template 20 + inspect_report 1 + tb_document 1)
  3. `inspect_template.APP` 섹션 정리 (UPIS/UPIS_SW APP 28건 중복 제거)
  4. 점검자 → `users` FK / 확인자 → `ps_info` FK 연동 (`insp_user_id` + `conf_ps_info_id` 컬럼 추가, `insp_name/company`·`conf_name/org` 컬럼 삭제)
  5. `inspect_check_result.category` 공백 정규화 ("(GWS)" 2 표기 통합)
  6. `check_section_mst` 마스터 신설 (DB/DBMS/GIS/AP 섹션 — APP 제외 결정 후)
- **영향**: `inspect_report` + `inspect_check_result` + `inspect_template` + `inspect_visit_log` + `tb_document` 테이블 스키마
- **의존성**: 없음 (테스트 데이터 초기화 허용)
- **예상 기간**: 5~7일
- **실제 소요**: 1일 (2026-04-21 ~ 2026-04-22, 기획·개발계획·구현·검증 포함)
- **실행 결과**: V022 마이그 Phase 0~10 모두 PASS, Exit Gate 1/2/3 통과, 백업 테이블 4종 생성 (RUN_ID=20260421_235813), 전체 테스트 151건 green

#### S2. `process-master-dedup` ✅ **완료 (2026-04-20)**
- **배경**: `tb_process_master` 1450건 / `tb_service_purpose` 1450건 — 각 DISTINCT 5건뿐, **290배 중복 INSERT**
- **조치**: 중복 삭제 → DISTINCT 5건만 유지 + UNIQUE 제약 추가 + `DbInitRunner` `ON CONFLICT DO NOTHING` 적용
- **의존성**: 없음
- **예상 기간**: 1~2일

#### S3. `qt-remarks-users-link`
- **배경**: `qt_remarks_pattern` 7건 전부 담당자 정보(박욱진·이동수·여현정) 자유 텍스트 하드코딩. users 미참조
- **조치**: `qt_remarks_pattern`에 `user_id` FK 추가 + content 템플릿화 (`{username}`, `{dept}`, `{phone}`, `{email}` 플레이스홀더) + 렌더링 시 users 런타임 조회
- **의존성**: 없음
- **예상 기간**: 2~3일

#### S4. `license-country-cleanup-and-csv-fix`
- **배경**: `license_registry.country`에 사람 이름(`서현규` 18, `seo hyeon gyu` 6, `kim hanjun` 5, `김한준` 2 등) 저장됨. CSV 매핑 버그로 `registered_to`/`full_name` 값이 `country` 로 잘못 매핑된 것으로 추정
- **조치**: (a) CSV 업로드 파서 컬럼 매핑 버그 수정 (b) 기존 오염 32건+ 정제 SQL (c) Korea/korea/KOREA 대소문자 정규화 병행
- **의존성**: 없음
- **예상 기간**: 2~3일

#### S5. `access-log-userid-fix`
- **배경**: `access_logs.userid`에 `박욱진` 16건, `관리자` 11건 — 이름이 userid 필드에 잘못 저장. Spring Security `getName()`/`getUsername()` 혼동 의심
- **조치**: (a) 로깅 유틸 버그 수정 (b) 기존 오염 27건 정제 (사용자명 기반 userid 매핑 가능한 건) (c) `anonymousUser` 4건 유지 (로그인 전 접근으로 정상)
- **의존성**: 없음
- **예상 기간**: 1~2일

#### S6. `legacy-contract-tables-drop` ✅ **완료 (2026-04-20)**
- **배경**: `tb_contract`/`tb_contract_target` 완전 고아 (Entity 0건 + Repository 0건 + ERD 제거됨 + 데이터 0건). `sw_pjt` + `ps_info` + `sigungu_code`로 기능 완전 대체
- **조치**: **옵션 B 완료** — FR-0 사전검증 중 외부 FK 2건 발견(`tb_document.contract_id`, `tb_contract_participant.contract_id`) → FK·컬럼·INDEX 동시 정리 + tb_contract/target DROP. V017 마이그 + V100 주석 처리.
- **실행 결과**: V017 `PASS: all legacy contract artifacts dropped`. 사후 검증 PASS. 서버 기동 정상.
- **실제 소요**: 0.5일 (예상 일치)

#### ~~S7. `legacy-inspect-tables-drop`~~ → **S1 하위 태스크로 편입** (v2)
- **배경**: `tb_inspect_checklist` 0건 (`inspect_check_result`와 완전 중복), `tb_inspect_issue` 0건 (`inspect_visit_log`와 완전 중복), `tb_document_signature` 0건 (`inspect_report.insp_sign`/`conf_sign`과 중복)
- **조치**: 3개 테이블 DROP
- **v2 변경**: 독립 스프린트에서 제거 → **S1 inspect-comprehensive-redesign의 하위 태스크**로 완전 편입. P1 독립 스프린트 수 **7 → 6**으로 조정.

---

### 🟡 P2 — 구조 개선 (6건)

#### S8. `qt-quotation-domain-normalize` ✅ **완료 (2026-04-22)** (v2 범위 축소 — category 만)
- **사용자 결정 (2026-04-22)**: 견적서는 유지보수/용역/제품 별도 도메인이며 prj_types 와 다름. **옵션 B 채택** (별도 마스터).
- **본 스프린트 범위 (category 만)**:
  - `qt_category_mst` 3행 마스터 신설 (한글 유지: 유지보수/용역/제품)
  - `qt_quotation.category` FK 추가 (기존 61건 값 변경 0)
  - `QtCategory` Enum 신설 (S9 AccessActionType 패턴 재사용)
- **범위 외 (후속 스프린트로 분리)**:
  - `qt_quotation.status` — 전부 "발행완료" 단일값, 정규화 의미 낮음 → **별도 스프린트 S8-B (보류)**
  - `qt_quotation.template_type` 매직 넘버 1/2 → **S8-C 완료 (2026-04-22)** — `QuoteTemplateType` Enum 도입
- **의존성**: 없음 (사용자 결정 완료)
- **예상 기간**: 1~2일

#### S9. `access-log-action-and-menu-sync` ✅ **완료 (2026-04-21)**
- **포함**:
  - `access_logs.action_type` 20+종 → `AccessActionType` Enum **13종** 신설 (동의어 매핑 + fromKoLabel 정규화)
  - `MenuName` 상수 **5종 누락** (QR라이선스/라이선스대장/GeoNURIS라이선스/견적서/회원가입) 추가 — 로드맵 기록 "4종"에서 **실측 5종으로 정정** (S9 기획서 §1-2 참조: `docs/product-specs/access-log-action-and-menu-sync.md`)
  - ArchUnit CI 게이트 + fail-soft 정규화
- **의존성**: 없음
- **실제 소요**: 0.5일 (예상 2일보다 단축)

#### S10. `inspect-check-result-category-master` ✅ **완료 (2026-04-22)**
- **배경**: `inspect_check_result.category` 자유 텍스트 (공백 차이 등 오염 포함). S1 후속
- **조치**: `check_category_mst` 16행 마스터 신설 + FK 2개 (`fk_it_category`, `fk_icr_category`) + `inspect_template.category` NOT NULL
- **의존성**: S1 완료 ✅
- **실제 소요**: 0.5일
- **실행 결과**: V023 Phase 0~6 PASS. mst=16, FK×2, NOT NULL 적용. NFR-4 (공백 X 2쌍) 양 테이블 0 hits

#### S11. `license-registry-type-enum` (P2 상향)
- **배경**: `license_registry.license_type` 2종 (License Text / Floating License Text) 하드코딩
- **조치**: Enum 도입 (이미 `#1-A` 인프라 재사용 가능)
- **의존성**: S4와 같은 `license_registry` 테이블 건드리므로 **같은 Wave 권장**
- **예상 기간**: 1일

#### ~~S12~~ `geonuris-license-type-enum` → **P3로 환원** (v2)
- **배경**: `geonuris_license.license_type` DESKTOP/GSS30/GSS35 3종 하드코딩 11곳 (`GeonurisLicenseService.java` 8, `GeonurisLicense.java` 3)
- **조치**: Enum 도입 + 코드 리터럴 치환
- **v2 변경**: P3로 환원. `geonuris_license` 별도 테이블이라 S4/S11과 분리. P3 섹션에 재배치.
- **의존성**: 없음
- **예상 기간**: 1~2일

#### S13. `license-registry-case-normalize` → **S4에 병합** (v2 확정)
- **배경**: Korea/korea/KOREA 등 대소문자 불일치
- **조치**: S4(`license-country-cleanup-and-csv-fix`)에 완전 흡수. 독립 실행 없음.

---

### 🟢 P3 — 선택적 개선 (4건, v2 재정리)

#### S12. `geonuris-license-type-enum` (v2: P3로 환원)
- **배경**: `geonuris_license.license_type` DESKTOP/GSS30/GSS35 3종 하드코딩 11곳
- **조치**: Enum 도입 + 코드 리터럴 치환
- **decision deadline**: 해당 없음 (기술 결정)
- **예상 기간**: 1~2일

#### S14. `qr-license-decision`
- **배경**: `qr_license` 0건 + 한영 이중 컬럼 20개 설계
- **조치**: 기능 존속 판단 필요. 유지 시 이중 컬럼 구조 재검토, 제거 시 DROP
- **decision deadline**: Wave 3 종료일 (로드맵 시작 후 6주차 금요일)
- **의존성**: 사용자 의사결정
- **예상 기간**: 0.5일(결정) + 1일(구현)

#### S15. `pjt-equip-decision` ✅ **완료 (2026-04-22)**
- **배경**: `pjt_equip` 0건. 사업관리 확장 예정 여부 판단 필요
- **조치**: 사용자 판단. 유지 or DROP
- **decision deadline**: Wave 3 종료일 (로드맵 시작 후 6주차 금요일)
- **의존성**: 사용자 의사결정
- **예상 기간**: 0.5일

#### S16. `tb-work-plan-decision` ✅ **완료 (2026-04-22)**
- **배경**: `tb_work_plan` 1건 (미사용 근접). Service/Controller/DTO 코드는 완비
- **조치**: 사용자 판단 — (a) 기능 제거 or (b) `work_plan_type_mst` + `work_plan_status_mst` 마스터 신설 후 재활성화
- **decision deadline**: Wave 3 종료일 (로드맵 시작 후 6주차 금요일)
- **의존성**: 사용자 의사결정
- **예상 기간**: 0.5일(결정) + 2~3일(구현)

### ~~S17. `inspect-template-cleanup`~~ — **closed-by-S1** (v2)
- S1 inspect-comprehensive-redesign에 완전 통합되어 실행 단위로 불가
- 본 로드맵의 스프린트 집계·Wave·완료기준에서 **제외**

---

## 3. 의존성·실행 순서

### 3-1. 의존성 그래프 (v2 수정)

```
[독립 실행 가능 — 병렬 처리]
  S2 process-master-dedup
  S3 qt-remarks-users-link
  S4 license-country-cleanup-and-csv-fix   (S13 흡수)
  S5 access-log-userid-fix
  S6 legacy-contract-tables-drop
  S9 access-log-action-and-menu-sync

[같은 테이블 접근 — 같은 Wave 권장]
  S4 + S11 license-registry-type-enum (license_registry 테이블 공유)

[S1 대형 통합]
  S1 inspect-comprehensive-redesign
   └─ S7 legacy-inspect-tables-drop (하위 태스크로 편입)
   └─ S17 inspect-template-cleanup (closed-by-S1)
   └─ S10 inspect-check-result-category-master (후속, 별도 Wave)

[사용자 의사결정 선행 — SLA 2영업일]
  S8 qt-quotation-domain-normalize   ← prj_types 통일 결정
  S14 qr-license-decision             ← 기능 존속 판단
  S15 pjt-equip-decision              ← 기능 존속 판단
  S16 tb-work-plan-decision           ← 기능 존속 판단

[독립 기술 개선 — 우선순위 낮음]
  S12 geonuris-license-type-enum
```

### 3-2. 추천 실행 순서 (v2 수정 — S7 제거, Wave 균형)

**Wave 1 (Week 1~2) — P1 병렬 (6개 스프린트)**
- S6 (0.5일, DROP 안전) → **1일차 즉시 실행**
- S2 (1~2일), S5 (1~2일) → 병렬
- S3 (2~3일), S4 (2~3일) → 병렬
- S11 (1일, S4와 같은 license_registry 테이블이라 **S4 직후 동일 브랜치**에서 실행 권장)

**Wave 2 (Week 3~4) — P1 대형 통합**
- S1 `inspect-comprehensive-redesign` (5~7일, 단일 최대 스프린트)
  - 하위: S7 (tb_inspect_checklist/issue/document_signature DROP 태스크)
  - 하위: UPIS_SW 통합 / APP 섹션 정리 / result 분리 / 점검자·확인자 FK / category 정규화 / check_section_mst 신설

**Wave 3 (Week 5~6) — P2**
- S8 (사용자 정책 결정 선행 — prj_types 통일 방향, **decision deadline: Wave 3 시작 -2일**)
- S9 (access_log action/menu — 2일)
- S10 (inspect-check-result-category-master, S1 후속 — 2~3일)
- Wave 3 종료 시점에 **S14/S15/S16 decision deadline** 도달 → 사용자 일괄 의사결정 받음

**Wave 4 (Week 7) — P3**
- 사용자 의사결정 결과 반영해 S14/S15/S16 실행 or 스킵
- S12 `geonuris-license-type-enum` (1~2일, 독립)

---

## 4. 리스크 및 완화

| 리스크 | 수준 | 완화 |
|--------|------|------|
| S1 대형 스프린트 범위 팽창 | **높음** | 기획서 단계에서 6개 조치 각각 FR로 분리, 부분 실패 시 개별 rollback 가능 |
| `legacy-*-drop` 실행 후 숨은 참조 발견 | 낮음 | 실행 전 `pg_stat_user_tables`로 최근 쓰기 없음 확인 + 2주 보관 후 DROP |
| qt_quotation.category 표준 결정 지연 | 중 | S8은 사용자 결정 대기 — 결정 없으면 스프린트 시작 금지 |
| CSV 파싱 버그 재발 (S4) | 중 | 회귀 테스트 3건 이상 + CI에서 샘플 CSV 파싱 검증 |
| inspect-comprehensive-redesign 테스트 미통과 | 중 | 테스트 단계라 운영 영향 0. 단 UI 렌더링 회귀 수동 점검 필수 |

---

## 5. 측정 가능한 완료 기준 (v2 정량화)

### Wave 1 완료 시 (6개 스프린트 종료)

| 스프린트 | SQL 검증 쿼리 | Pass 기준 |
|---------|---------------|-----------|
| S6 | `SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('tb_contract','tb_contract_target')` | = 0 |
| S2 | `SELECT COUNT(*) FROM tb_process_master` | = 5 (정확히) |
|    | `SELECT COUNT(*) FROM tb_service_purpose` | = 5 |
|    | `SELECT COUNT(DISTINCT constraint_name) FROM information_schema.table_constraints WHERE table_name='tb_process_master' AND constraint_type='UNIQUE'` | ≥ 1 |
| S3 | `rg -n '담당자 : SW지원부\|담당자 : GIS사업부\|담당자 : SW영업본부' src/main/resources/` | 0 hits (리터럴 제거됨) |
|    | `SELECT COUNT(*) FROM information_schema.columns WHERE table_name='qt_remarks_pattern' AND column_name='user_id'` | = 1 (FK 추가됨) |
| S4 | `SELECT COUNT(*) FROM license_registry WHERE country IN ('서현규','서 현규','kim hanjun','김한준','seo hyeon gyu','Park Uk Jin')` | = 0 |
|    | `SELECT COUNT(DISTINCT UPPER(country)) FROM license_registry WHERE country IS NOT NULL` | 의미있게 감소 (기존 14+종 → 5종 내외) |
| S5 | `SELECT COUNT(*) FROM access_logs a LEFT JOIN users u ON a.userid = u.userid WHERE u.user_id IS NULL AND a.userid NOT IN ('anonymousUser')` | = 0 (anonymousUser 제외 전부 매칭) |
| S11 | `rg -n '"(License Text\|Floating License Text)"' src/main/java --glob '!**/constant/enums/**'` | 0 hits |
|     | 회귀테스트: license type parsing 2건 통과 |

### Wave 2 완료 시 (S1 대형 통합 스프린트 종료)

| 검증 항목 | SQL/명령 | Pass 기준 |
|-----------|----------|-----------|
| result 스키마 분리 | `SELECT COUNT(*) FROM information_schema.columns WHERE table_name='inspect_check_result' AND column_name IN ('result_code','result_text')` | = 2 |
| UPIS_SW 통합 | `SELECT COUNT(*) FROM inspect_template WHERE template_type='UPIS_SW'` | = 0 |
|              | `SELECT COUNT(*) FROM inspect_report WHERE sys_type='UPIS_SW'` | = 0 |
|              | `SELECT COUNT(*) FROM tb_document WHERE sys_type='UPIS_SW'` | = 0 |
| APP 섹션 정책 | `SELECT COUNT(*) FROM inspect_template WHERE section='APP'` | 정책 결정에 따라 0 또는 14 |
| 점검자 FK | `SELECT COUNT(*) FROM information_schema.columns WHERE table_name='inspect_report' AND column_name IN ('insp_user_id','conf_ps_info_id')` | = 2 |
| 점검자 옛 컬럼 제거 | `SELECT COUNT(*) FROM information_schema.columns WHERE table_name='inspect_report' AND column_name IN ('insp_company','conf_org')` | = 0 |
| 레거시 테이블 DROP | `SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('tb_inspect_checklist','tb_inspect_issue','tb_document_signature')` | = 0 |
| 회귀테스트 | `./mvnw test -Dtest='*Inspect*Test'` | 전부 Green |
| UI 렌더링 | 수동: `/document/inspect-preview/{id}` 3건 | PDF 정상 + UPIS 표시 OK |

### Wave 3 완료 시 (P2 3개 스프린트)

| 검증 항목 | SQL/명령 | Pass 기준 |
|-----------|----------|-----------|
| S8 qt_quotation.category | 정책 결정에 따라: (a) `rg '"유지보수"\|"용역"\|"제품"' src/main/java --glob '!**/constant/enums/**'` = 0 hits (Enum화), or (b) `SELECT DISTINCT category FROM qt_quotation` ⊆ prj_types.cd (마스터 참조) | 정책 결정별 통과 |
| S8 qt_quotation.status | `rg '"(작성중\|발행완료\|취소)"' src/main/java --glob '!**/constant/enums/**'` | 0 hits |
| S9 access_log.action_type Enum | `rg '"(조회\|수정\|미리보기\|접근\|등록\|...)"' src/main/java/.../access --glob '!**/enums/**'` | 0 hits (Enum화) |
| S9 MenuName 상수 | `MenuName.java` 상수 개수 | ≥ 13 (DB 13종 커버) |
| S10 check_section_mst | `SELECT COUNT(*) FROM check_section_mst` | ≥ 4 (DB/DBMS/GIS/AP + 필요시 확장) |
| 회귀테스트 | N=5건 이상 추가 | 전부 Green |

### Wave 4 완료 시 (P3)

| 검증 항목 | SQL/명령 | Pass 기준 |
|-----------|----------|-----------|
| S12 geonuris-license-type Enum 치환 | `rg -n '"(DESKTOP\|GSS30\|GSS35)"' src/main/java --glob '!**/enums/**' --glob '!**/test/**'` | = 0 hits |
| S12 Enum 파일 존재 | `ls src/main/java/com/swmanager/system/constant/enums/GeonurisLicenseType.java` | 파일 존재 |
| S14 qr_license 결정 (DROP 택한 경우) | `SELECT COUNT(*) FROM information_schema.tables WHERE table_name='qr_license'` | = 0 (DROP) 또는 결정 보존 표시 |
| S14 qr_license 결정 (유지 택한 경우) | `SELECT COUNT(*) FROM information_schema.columns WHERE table_name='qr_license' AND column_name LIKE '%_ko'` 감소 | 한영 이중 컬럼 재구성 후 `_ko` 컬럼 수 감소 확인 |
| S15 pjt_equip 결정 (DROP 택한 경우) | `SELECT COUNT(*) FROM information_schema.tables WHERE table_name='pjt_equip'` | = 0 |
| S15 pjt_equip 결정 (유지 택한 경우) | `SELECT COUNT(*) FROM pjt_equip` | > 0 또는 사용 계획 문서 존재 (`docs/product-specs/pjt-equip-activation.md`) |
| S16 tb_work_plan 결정 (제거 택한 경우) | `SELECT COUNT(*) FROM information_schema.tables WHERE table_name='tb_work_plan'` AND 관련 Service/Controller/DTO 파일 `ls`로 없음 | 모두 0/없음 |
| S16 tb_work_plan 결정 (활성화 택한 경우) | `SELECT COUNT(*) FROM work_plan_type_mst` + `SELECT COUNT(*) FROM work_plan_status_mst` | 각 ≥ 1 (마스터 신설 확인) |
| 전체 레거시 잔존 검증 | 아래 **T-FINAL** 블록 실행 | T-FINAL 기준 통과 |

**T-FINAL** (로드맵 전체 완료 검증 SQL):

```sql
-- 감사 대상 34개 중 0건 미사용 레거시 잔존 여부
-- S14/S15/S16 결정에서 "유지" 선택한 것은 제외 리스트에 추가
WITH target_tables AS (
  SELECT unnest(ARRAY[
    'qr_license',           -- S14 결정 필요
    'pjt_equip',            -- S15 결정 필요
    'tb_work_plan'          -- S16 결정 필요
    -- S6/S7/S17에서 DROP된 테이블은 information_schema 조회 시 이미 없음
  ]) AS tbl
)
SELECT t.tbl,
       (SELECT COUNT(*) FROM information_schema.tables WHERE table_name = t.tbl) AS table_exists,
       CASE WHEN (SELECT COUNT(*) FROM information_schema.tables WHERE table_name = t.tbl) = 0
            THEN '✅ DROP 완료'
            ELSE '⚠ 유지 결정 시 이유 문서 필요' END AS status
  FROM target_tables t;
```
**Pass 기준**: 모든 행이 `✅ DROP 완료` 또는 `유지 결정 + 이유 문서 존재` (의사결정 근거 `docs/product-specs/*-decision.md` 문서화).

---

## 6. 전체 일정 추정 (v2 — 의사결정 버퍼 반영)

### 6-1. 기본 시나리오 (의사결정 SLA 준수 시)

| Wave | 기간 | 주요 산출 | 의사결정 |
|------|------|----------|---------|
| Wave 1 | 2주 | P1 개별 스프린트 6건 (S2/S3/S4/S5/S6/S11) | S8 deadline 통지 (Wave 3 시작 -2일) |
| Wave 2 | 2주 | S1 대형 통합 (S7/S17 흡수) | S14/S15/S16 deadline 통지 |
| Wave 3 | 2주 | P2 3건 (S8/S9/S10) | Wave 3 종료일 = S14/S15/S16 deadline |
| Wave 4 | 1주 | P3 4건 (S12 + 결정된 S14/S15/S16) | — |
| **총계** | **7주** | 로드맵 전체 완료 | |

### 6-2. 지연 시나리오 (최대 +2주 버퍼, 총 9주 상한)

| 지연 시나리오 | 영향 | 완화 |
|---------------|------|------|
| **S8** `qt_quotation.category` 표준 결정 지연 | Wave 3 시작 지연 | S9·S10 먼저 진행 → S8은 결정 후 Wave 3 내 투입 |
| **S14/15/16** 결정 지연 | Wave 4 시작 지연 | Wave 4를 2주로 확장 (최대 8주) |
| **S1** 대형 통합 범위 팽창 | Wave 2 지연 | Wave 2 → 3주로 확장 + 기획서에서 부분 범위 축소 가능 명시 (최대 8주) |

**최대 상한**: 9주 (7주 본계획 + 2주 버퍼 — 의사결정 지연 & S1 범위 팽창 동시 발생 시)

### 6-3. 의사결정 SLA

| 결정 대상 | 담당 | Deadline | 미준수 시 |
|-----------|------|----------|-----------|
| S8 `qt_quotation.category` 표준 (prj_types 통일 vs 자체 마스터) | 사용자 | Wave 3 시작 -2일 | S8 제외 Wave 3 진행, S8은 Wave 4로 이월 |
| S14 qr_license 존속 여부 | 사용자 | Wave 3 종료일 | Wave 4에서 보류 판정 (유지 처리) |
| S15 pjt_equip 존속 여부 | 사용자 | Wave 3 종료일 | 동일 |
| S16 tb_work_plan 존속 여부 | 사용자 | Wave 3 종료일 | 동일 |

---

## 7. 다음 단계

본 로드맵이 사용자 최종승인되면 **Wave 1부터 순차**로 각 스프린트를 정식 워크플로우로 진행:
1. 기획팀 → 기획서 작성
2. codex 검토
3. 사용자 최종승인
4. 개발팀 → 개발계획서 작성
5. codex 검토
6. 사용자 최종승인
7. 실제 구현

본 로드맵 자체는 **감사 스프린트 `data-architecture-audit`의 마지막 산출물**로서 완결. 후속 스프린트는 독립 기획.

### 사용자 승인 필요 항목

1. 로드맵 전체 구조·우선순위 (P1/P2/P3)
2. S1 통합 스프린트 범위 적절성 (6개 조치 통합 vs 분리)
3. S8 `qt_quotation.category` 표준 결정 (prj_types 통일 vs 자체 마스터)
4. S14~S16 기능 존속 판단 (qr_license / pjt_equip / tb_work_plan)
