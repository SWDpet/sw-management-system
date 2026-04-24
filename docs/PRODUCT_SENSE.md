# PRODUCT_SENSE.md — 제품 컨텍스트·팀·의사결정 히스토리

> ⚠ **자동 생성 초안 — 검증 필요**
> 근거: `CLAUDE.md` 가상팀 + `README.md` + 기존 `docs/PRODUCT_SENSE.md` + `docs/design-docs/data-architecture-roadmap.md` 영구패스 의사결정

---

## 1. 제품 비전

**SW Manager** 는 소프트웨어 사업 수행 조직(정도UIT)의 전체 업무 흐름을 디지털화하는 통합 관리 시스템입니다.

### 대상 사용자
- 사업 담당자 (프로젝트 매니저)
- 유지보수 기술자 (내부)
- 고객 담당자 (ps_info)
- 관리자 (권한 부여·정산)

### 핵심 가치
1. **사업 단일 소스** — `sw_pjt` 중심으로 착수계·기성계·준공계·점검내역서·견적서 연결
2. **자동화된 문서 출력** — HWPX / PDF / Excel 템플릿 기반 견적서·착수계 자동 생성
3. **이력 추적** — `access_logs` + `document_history` + `git` (소스)

---

## 2. 가상 팀 구조

| 팀 | 역할 |
|----|------|
| 🧭 기획팀 | 요건 수렴, 기획서 |
| 🗄️ 데이터베이스팀 | 스키마·인덱스 자문 |
| 🛠️ 개발팀 | 개발계획 + 구현 |
| 🧪 테스트·검증팀 | codex 위임 |
| 🤖 codex | 산출물 검증 게이트 |

자세한 워크플로우: `AGENTS.md §3`.

---

## 3. 주요 의사결정 히스토리

### 3-1. 영구 패스 (2026-04-20, 사용자 결정)
**라이선스 관련 스프린트**는 수동 한글화 정책상 자동 정제·Enum 도입에서 **영구 제외**:
- S4 license-country-cleanup
- S11 license-registry-type-enum
- S12 geonuris-license-type-enum
- S13 (S4 병합)
- S14 qr-license-decision

Claude / codex 모두 해당 스프린트 추천 금지.

### 3-2. 견적서 도메인 구분 (2026-04-22)
`qt_quotation.category` 는 **유지보수/용역/제품** 3종 독자 도메인. `prj_types` (GISSW/PKSW/TS) 와는 별개 → S8 에서 `qt_category_mst` 신설.

### 3-3. 문서관리 테스트 단계 (2026-04-20)
`inspect_*` / `tb_document_*` 는 테스트 단계로 스키마 재설계·데이터 초기화 허용. S1 대형 통합의 전제.

### 3-4. S16 업무계획 기능 유지 (2026-04-22)
`tb_work_plan` 은 1행 뿐이지만 활성 기능. DROP 하지 않고 `work_plan_type_mst` + `work_plan_status_mst` 신설로 구조 개선.

---

## 4. 프로덕트 우선순위 원칙

1. **회귀 최소화** — 완료 스프린트 검증 규칙 (T1~Tn, codex 승인)
2. **마스터 재사용** — 기초 마스터(users/ps_info/sys_mst/prj_types/sigungu_code) 우선 검토. 신규 테이블 전에 기존 테이블 활용 가능 여부 확인
3. **영문 코드 + 한글 라벨 분리** — DB 저장은 영문 코드 (또는 한글 PK 유지), UI 표시는 한글 label. S9/S16 패턴

---

*Last updated: 2026-04-24 · docs-renewal-01 P1 (docs/PRODUCT_SENSE.md 통합)*
