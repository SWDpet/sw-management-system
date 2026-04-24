# [기획서] 감사 후속조치 스프린트 2a — P2 스키마·문서 정합성 6건

- **작성팀**: 기획팀
- **작성일**: 2026-04-19
- **선행**: `2d0a9c1` (스프린트 1 P1)
- **상태**: 초안 (codex 검토 대기)

---

## 1. 배경 / 목표

감사 P2 중 **스키마·문서 정합성 그룹 6건** 처리:
- **2-2** init SQL 에 누락된 엔티티 테이블 DDL 보강
- **3-1** `tb_contract` 미구현 — ERD 재정비
- **3-2** `ContractParticipant` FK 가 ERD 와 다름 — ERD 갱신
- **3-3** `tb_inspect_cycle` 도메인 미구현 — ERD 재정비
- **3-4** A 탭 유형 필터가 plans 와 다름 — plans 갱신
- **3-5** A 탭 렌더러가 plans 와 다름 — plans 갱신

**대부분 문서(ERD·plans) 갱신**. Entity/Java 코드 변경 없음. DB 측은 init SQL 에 DDL 추가(IF NOT EXISTS 멱등) — 기존 데이터/기존 테이블 영향 없음.

---

## 2. 기능 요건 (FR)

### 2-2. init SQL 누락 테이블 DDL 보강

현재 `db_init_phase2.sql` 은 7 테이블만 CREATE. 엔티티는 ~35 테이블. 차이는 phase1 (문서화되지 않은 초기 스키마) 또는 수동/외부 생성으로 추정. 이 상태로는 신규 환경 초기화 실패 위험.

| ID | 내용 |
|----|------|
| FR-2-2-A | `db_init_phase2.sql` **최상단 주석 블록** 에 명시: (a) 이 스크립트는 phase1 이후의 증분 DDL이다. (b) 전제 테이블 목록: `sw_pjt, users, tb_infra_master, tb_infra_server, tb_infra_software, tb_infra_link_upis, tb_infra_link_api, tb_infra_memo, access_logs, ps_info, tb_performance_summary, sys_mst, sigungu_code, prj_types, maint_tp_mst, cont_stat_mst, cont_frm_mst, pjt_equip, tb_pjt_target, tb_pjt_manpower_plan, tb_pjt_schedule`. (c) 이 스크립트만으로는 신규 환경 초기화 불가. 별도 phase1 스키마(또는 JPA DDL 생성) 필요. |
| FR-2-2-B | 문서 관리 계열 7 테이블 CREATE 추가: `tb_document`, `tb_document_detail`, `tb_document_history`, `tb_document_attachment`, `tb_document_signature`, `tb_inspect_checklist`, `tb_inspect_issue`. 모두 `IF NOT EXISTS` 로 멱등. FK 는 기존 관행(sw_pjt, users, tb_infra_master) 에 맞춤. |
| FR-2-2-C | `tb_work_plan` CREATE 추가 (문서 관리와 밀접, 현재 미포함). `assignee_id → users`, `parent_plan_id → tb_work_plan` self-FK. |
| FR-2-2-D | 전제 테이블(phase1 대상) 의 DDL 은 **이번 범위 밖** — 향후 "phase1.sql 신설" 스프린트에서 일괄 정리. 본 스프린트에서는 주석으로 가이드만. |

### 3-1. tb_contract 미구현

| ID | 내용 |
|----|------|
| FR-3-1 | `docs/erd-contract.mmd` 에서 `tb_contract` 테이블 블록 제거. 관계선도 함께 정리. 파일명도 실제 내용 반영하기 위해 유지 (제목만 "Participants & Work Plan" 등으로 조정 가능). |
| FR-3-2 | `docs/generated/erd.md` 에서 tb_contract 언급 있으면 제거 or "미구현" 표기. |
| FR-3-3 | ERD 에 남는 contract 관련 테이블: `tb_contract_participant` (실제 proj_id FK) 만. 자세한 구조는 3-2 참조. |

### 3-2. ContractParticipant FK ERD 갱신

| ID | 내용 |
|----|------|
| FR-4-1 | `erd-contract.mmd` 의 `tb_contract_participant` 정의를 실제 엔티티와 일치시킴: `proj_id → sw_pjt(proj_id)` FK 표시. `contract_id` 참조선 제거. |
| FR-4-2 | `sw_pjt ||--o{ tb_contract_participant : "has participants"` 관계선 유지. |

### 3-3. tb_inspect_cycle

현재 entity/table 없음. `tb_inspect_cycle` 을 사용하는 기능이 없으므로 **ERD 에서 제거** 선택.

| ID | 내용 |
|----|------|
| FR-5-1 | `docs/erd-core.mmd` 에서 `tb_inspect_cycle` 블록 및 관계선 제거. |
| FR-5-2 | 향후 해당 기능이 필요해지면 새 기획서에서 다시 정의 — 주석으로만 기록. |

### 3-4. A 탭 유형 필터 plans 갱신

| ID | 내용 |
|----|------|
| FR-6-1 | `docs/product-specs/system-graph-infra-perf.md` FR-2 의 **"인프라 유형 드롭다운 (전체/UPIS/KRAS/기타)"** 를 현 구현과 일치시킴: **"시스템명(sys_nm_en) 드롭다운"** 으로 변경. |
| FR-6-2 | 개정 이력 테이블에 "2026-04-19: 사용자 피드백 반영으로 유형 필터 → 시스템명 필터로 전환 (실구현 준수)" 한 줄 추가. |
| FR-6-3 | FR-5, 관련 UI 스펙도 시스템명 기반으로 정렬. |

### 3-5. A 탭 렌더러 plans 갱신

| ID | 내용 |
|----|------|
| FR-7-1 | `docs/product-specs/system-graph-infra-perf.md` FR-1 / NFR 를 **"텍스트 트리 (CLI tree 스타일) 기본 + 조직도(≤80 노드) 토글"** 로 재정의. vis-network hierarchical 요구 삭제. |
| FR-7-2 | FR-4 DataSet 배치 업데이트 요구는 더 이상 해당 없음 — "HTML innerHTML 한 번에 replace" 로 대체됨. 이에 맞는 성능 기준 재정립 (초기 렌더 ≤ 1.5s 는 유지 가능). |
| FR-7-3 | 개정 이력에 "2026-04-19: 렌더러 방향 전환 반영 (force/hierarchical 실험 후 텍스트 트리로 확정)" 기록. |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 현재 운영 중인 기능·API 에 영향 없음 (문서·DDL 주석 변경 위주). |
| NFR-2 | DDL 보강 후 기존 DB 에도 안전 — `IF NOT EXISTS` 로 멱등. 데이터 소실 가능성 0. |
| NFR-3 | ERD 변경 후 C 탭(ERD 인터랙티브) 재기동 시 파싱 오류 없어야 함 — `.mmd` 문법 유지. |
| NFR-4 | plans 갱신 후 감사 보고서의 해당 4 건(3-4, 3-5 포함 시 변경 반영) 체크박스 "☑ 조치함" 으로 변경. |

---

## 4. 의사결정 / 우려사항

### 4-1. tb_contract 제거 vs 구현 — ✅ 제거 확정
- 실제 코드/비즈니스는 이미 프로젝트(sw_pjt) 중심. 계약 마스터 테이블 신설은 큰 설계 변경이고 현 시점 요구사항 없음.
- ERD 에서만 제거 — 필요시 후속 스프린트에서 신설.

### 4-2. DDL 어느 수준까지 보강? — ✅ 확정 (FR-2-2-A/B/C)
- 전 테이블 DDL 은 이번 범위 밖 (phase1 스키마 정비 별도 스프린트).
- 문서 관리 계열 7 + tb_work_plan 만 보강 → 문서 관련 기능 독립 실행 가능.

### 4-3. plans 갱신 방식 — ✅ 확정
- `_v3` 같은 버전 접미사 안 씀. 본 문서에 개정 이력 한 줄 추가 + 본문 수정.

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| DB (수정) | `src/main/resources/db_init_phase2.sql` | 상단 주석 + 8 CREATE TABLE 추가 (IF NOT EXISTS) |
| Docs (수정) | `docs/erd-contract.mmd` | tb_contract 제거 + tb_contract_participant FK 교체 |
| Docs (수정) | `docs/erd-core.mmd` | tb_inspect_cycle 제거 |
| Docs (수정) | `docs/generated/erd.md` | tb_contract 언급 정리 (있으면) |
| Docs (수정) | `docs/product-specs/system-graph-infra-perf.md` | 유형 필터 / 렌더러 스펙 갱신 + 개정 이력 |
| Docs (수정) | `docs/generated/audit/2026-04-18-system-audit.md` | 2-2, 3-1, 3-2, 3-3, 3-4, 3-5 체크박스 ☑ 조치함 |

**수정 6 파일 (DB 초기화 1 + ERD 3: erd-contract.mmd, erd-core.mmd, ERD.md + plans 1 + 감사 보고서 1). 신규 0. Entity/Java 코드 변경 0.**

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 추가한 CREATE TABLE 과 기존 운영 DB 스키마 불일치 | 중간 | 엔티티 필드와 컬럼 이름·타입을 엔티티 코드를 기준으로 작성. `IF NOT EXISTS` 로 기존 테이블 보존. 차이 있으면 수동 점검 필요. |
| ERD 변경 후 C 탭 파싱 실패 | 낮음 | NFR-3 — 변경 후 서버 재기동 및 C 탭 확인 (실구현 단계에서 검증) |
| plans 갱신으로 과거 감사·검토 흐름 추적 어려움 | 낮음 | 개정 이력에 변경 사유 명시. 원 승인본은 git history 로 보존됨 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
