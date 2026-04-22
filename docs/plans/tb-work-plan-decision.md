---
tags: [plan, sprint, workplan, master, enum, wave-4]
sprint: "tb-work-plan-decision"
priority: P3
wave: 4
status: draft
created: "2026-04-22"
---

# [기획서] tb_work_plan 마스터 신설 + Enum 도입 (S16, 옵션 B)

- **작성팀**: 기획팀
- **작성일**: 2026-04-22
- **근거 로드맵**: [[data-architecture-roadmap]] v2 §S16 (Wave 4 / P3)
- **사용자 결정 (2026-04-22)**: **B 옵션** (마스터 신설 + 기능 유지) 채택
- **상태**: v2 (codex v1 ⚠ 조건부 승인 4건 반영)
- **v1→v2 변경점**:
  1. NFR-4 보강 — FR-7 전수 검증 (Service/Controller/Entity 리터럴 0 hit 규칙) 추가
  2. V026 멱등성·롤백 명시 — ON CONFLICT DO NOTHING + V026_rollback.sql 보관
  3. R-5 배포 리스크 신규 (DDL lock / 배포 순서 / 재실행 안전)
  4. NFR-8 신규 — Enum ↔ 마스터 시드 drift 자동 검증 (CI 게이트)

---

## 0. 사전 조사 결과 (2026-04-22 실측)

### 0-1. `tb_work_plan` 현황
| 항목 | 값 |
|---|---|
| rows | **1** (영월군 기초조사정보시스템 v2.4 패치) |
| 기능 | 활성 (`/workplan/*` Controller + Service + DTO + Thymeleaf) |
| 외부 FK | `tb_document.plan_id` 가 참조 |

### 0-2. plan_type (10종, `WorkPlanDTO.getTypeLabel` switch)
| code | label | color |
|------|-------|-------|
| CONTRACT | 계약 | #1565c0 |
| INSTALL | 설치 | #2e7d32 |
| PATCH | 패치 | #00897b |
| INSPECT | 점검 | #ff9800 |
| PRE_CONTACT | 사전연락 | #9e9e9e |
| FAULT | 장애처리 | #e74a3b |
| SUPPORT | 업무지원 | #6a1b9a |
| SETTLE | 기성/준공 | #5c6bc0 |
| COMPLETE | 준공 | #37474f |
| ETC | 기타 | #795548 |

### 0-3. status (7종, `WorkPlanDTO.getStatusLabel` switch)
| code | label |
|------|-------|
| PLANNED | 예정 |
| CONTACTED | 연락완료 |
| CONFIRMED | 확정 |
| IN_PROGRESS | 진행중 |
| COMPLETED | 완료 |
| POSTPONED | 연기 |
| CANCELLED | 취소 |

### 0-4. repeat_type
- DB 분포: NONE 1건 (단일). 코드에서 추가 값 사용 가능성 있음 → Step 1 Precheck 실측
- 본 스프린트 범위 **제외** (데이터 1건 + DTO 에 헬퍼 없음. 후속 S16-B 검토)

### 0-5. Java 코드 하드코딩 사용처
- `WorkPlanDTO.java` — getTypeLabel / getTypeColor / getStatusLabel (switch-case 30+ 리터럴)
- `WorkPlanService.java:149` — `"PLANNED"` 기본값
- `WorkPlan.java:64,88` — 주석 + `"PLANNED"` 기본값
- `WorkPlanController.java:121` — `"CANCELLED"` 제외 리스트
- `PerformanceService.java:63,71` — `"INSPECT"` / `"COMPLETED"` 필터

---

## 1. 목적

- **FR-1**: `work_plan_type_mst` 마스터 신설 (10행 + color 필드)
- **FR-2**: `work_plan_status_mst` 마스터 신설 (7행)
- **FR-3**: FK 2개 (`tb_work_plan.plan_type` → type_mst / `tb_work_plan.status` → status_mst)
- **FR-4**: `WorkPlanType` Enum 도입 (10종, label + color)
- **FR-5**: `WorkPlanStatus` Enum 도입 (7종)
- **FR-6**: `WorkPlanDTO.java` switch-case 30+ 리터럴을 Enum 위임으로 치환
- **FR-7**: `WorkPlanService` / `WorkPlanController` / `PerformanceService` / `WorkPlan` 엔티티 주석·상수 정리
- **FR-8**: `db_init_phase2.sql` 시드 추가 (마스터 17행)

---

## 2. 범위

### 2-1. 포함
- 신규 테이블 2개 (work_plan_type_mst / work_plan_status_mst)
- V026 마이그레이션 SQL (사전검증 + CREATE + 시드 + FK)
- Enum 2개 신설 (S9 AccessActionType / S8 QtCategory 패턴 복제)
- Java 리터럴 치환 (FR-6 ~ FR-7 전수)
- db_init_phase2.sql 시드 추가
- 회귀 테스트 ≥ 5건

### 2-2. 제외
- `repeat_type` 정규화 — 별도 후속 스프린트 S16-B
- `tb_document.plan_id` FK 는 **유지** (업무계획 문서 연관은 정상 기능)
- UI 드롭다운은 기존 그대로 (마스터 기반 동적 로드는 후속)

---

## 3. 사용자 결정 사항

### 3-A. 마스터 스키마 (택 1)

| 옵션 | 설계 | 장점 |
|------|------|------|
| **A-opt1 (권장)** | `type_code(PK) + type_label + color + display_order`, `status_code(PK) + status_label + display_order` | S10/S8 패턴 일관 |
| A-opt2 | type/status 를 단일 `work_plan_code_mst` 에 kind 컬럼으로 통합 | 테이블 1개로 축소 |

**권장**: A-opt1 — 도메인 분리로 FK·쿼리 가독성 우수

### 3-B. DB 저장값 (택 1)

| 옵션 | DB 값 | 영향 |
|------|-------|------|
| **B-opt1 (권장)** | **영문코드 유지** (CONTRACT/PATCH/...) | 기존 1건 변경 0 + 모든 Java 코드 호환 |
| B-opt2 | 한글로 변경 | 기존 1건 UPDATE + Java switch 전면 재작성 |

**권장**: B-opt1 — S8 V-opt1(한글 유지) 와 대칭 (여기는 영문코드가 이미 표준)

### 3-C. Enum vs 마스터만 (택 1)

| 옵션 | 방식 |
|------|------|
| **C-opt1 (권장)** | **마스터 + Enum 둘 다** (S8 QtCategory 패턴) — DB 무결성 + Java 타입 안전 |
| C-opt2 | 마스터만, Java switch 유지 | |
| C-opt3 | Enum 만, DB FK 없음 | |

**권장**: C-opt1 — 완전한 회귀 방어

---

## 4. 제안 설계 (A-opt1 + B-opt1 + C-opt1)

### 4-1. 스키마

```sql
CREATE TABLE work_plan_type_mst (
  type_code     VARCHAR(20) PRIMARY KEY,
  type_label    VARCHAR(50) NOT NULL,
  color         VARCHAR(10) NOT NULL,   -- #RRGGBB
  display_order INT NOT NULL DEFAULT 0
);

CREATE TABLE work_plan_status_mst (
  status_code   VARCHAR(20) PRIMARY KEY,
  status_label  VARCHAR(50) NOT NULL,
  display_order INT NOT NULL DEFAULT 0
);
```

### 4-2. Enum

```java
public enum WorkPlanType {
    CONTRACT("계약", "#1565c0"),
    INSTALL("설치", "#2e7d32"),
    PATCH("패치", "#00897b"),
    INSPECT("점검", "#ff9800"),
    PRE_CONTACT("사전연락", "#9e9e9e"),
    FAULT("장애처리", "#e74a3b"),
    SUPPORT("업무지원", "#6a1b9a"),
    SETTLE("기성/준공", "#5c6bc0"),
    COMPLETE("준공", "#37474f"),
    ETC("기타", "#795548");
    // label + color + @JsonValue/@JsonCreator + fromKoLabel
}

public enum WorkPlanStatus {
    PLANNED("예정"), CONTACTED("연락완료"), CONFIRMED("확정"),
    IN_PROGRESS("진행중"), COMPLETED("완료"), POSTPONED("연기"),
    CANCELLED("취소");
}
```

### 4-3. FK 추가

```sql
ALTER TABLE tb_work_plan
  ADD CONSTRAINT fk_twp_type   FOREIGN KEY (plan_type) REFERENCES work_plan_type_mst(type_code),
  ADD CONSTRAINT fk_twp_status FOREIGN KEY (status)    REFERENCES work_plan_status_mst(status_code);
```

### 4-4. V026 마이그 Phase

1. Phase 0: 사전검증 (DB 값이 Enum 10/7 집합 내)
2. Phase 1: 마스터 2 테이블 `CREATE TABLE IF NOT EXISTS` + 시드 17행 `INSERT ... ON CONFLICT DO NOTHING`
3. Phase 2: Exit Gate (tb_work_plan 값 정합성)
4. Phase 3: FK ADD 2개 (명시적 멱등, conrelid 한정, `IF NOT EXISTS` 가드)
5. Phase 4: 최종 검증 (type=10, status=7, FK×2)

**재실행 안전**: 모든 Phase 가 WHERE 조건부 / IF NOT EXISTS / ON CONFLICT 로 멱등. V026 2회차 실행 시 NOTICE 동일, 영향 0.

**V026_rollback.sql** 보관:
```sql
ALTER TABLE tb_work_plan DROP CONSTRAINT IF EXISTS fk_twp_type;
ALTER TABLE tb_work_plan DROP CONSTRAINT IF EXISTS fk_twp_status;
DROP TABLE IF EXISTS work_plan_type_mst;
DROP TABLE IF EXISTS work_plan_status_mst;
```

---

## 5. FR / NFR

### FR
- FR-1 ~ FR-8 (§1)

### NFR
- **NFR-1**: `work_plan_type_mst` = 10, `work_plan_status_mst` = 7 정확
- **NFR-2**: FK 2개 존재 (대상 테이블 한정)
- **NFR-3**: `WorkPlanType` 10 values, `WorkPlanStatus` 7 values + label Freeze
- **NFR-4** (v2.1 명확화): FR-6 + FR-7 전수 치환 — **DTO/Service/Controller/Entity** 모두 Enum 위임 완료:
  - 목표: `rg '"(CONTRACT|INSTALL|PATCH|INSPECT|PRE_CONTACT|FAULT|SUPPORT|SETTLE|COMPLETE|ETC|PLANNED|CONTACTED|CONFIRMED|IN_PROGRESS|COMPLETED|POSTPONED|CANCELLED)"' src/main/java --glob '!**/enums/**' --glob '!**/test/**'` = **0 hits (예외 없음)**
  - 따라서 Service 기본값 `"PLANNED"` 는 `WorkPlanStatus.PLANNED.name()` 으로 치환 **필수** (예외 인정 안 함)
- **NFR-5**: 기존 1 row 값 변경 0
- **NFR-6**: 회귀 테스트 ≥ 5건 (Enum×2 + schema + Constraint + UI 스모크)
- **NFR-7**: 컴파일 + 전체 테스트 green + 서버 기동 정상
- **NFR-8** (v2 신규): Enum ↔ 마스터 동기화 테스트 (CI 게이트):
  - `WorkPlanType.values()` 개수·name·label·color 가 `work_plan_type_mst` row 와 일치
  - `WorkPlanStatus.values()` 개수·name·label 가 `work_plan_status_mst` row 와 일치
  - drift 발생 시 테스트 실패로 빌드 중단

---

## 6. 리스크 / 완화

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | DB 에 Enum 외 값 존재 | 낮음 | Step 1 Precheck + V026 Phase 0 게이트 |
| R-2 | Java 리터럴 치환 누락 | 중 | NFR-4 rg + ArchUnit + Step 1 전수 조사 |
| R-3 | Color 값 Java vs DB 불일치 | 낮음 | Enum 정의가 SOT. 마스터 시드는 복제 (드리프트 테스트 추가 검토) |
| R-4 | tb_document.plan_id FK 가 본 스프린트 영향 | 낮음 | FK 유지 정책. tb_work_plan 구조 변경 없음 (컬럼 그대로 + FK 추가만) |
| R-5 (v2) | 배포 리스크 (DDL lock / 배포 순서) | 낮음 (row=1) | 배포 순서: **Java 배포 먼저 (Enum 신설) → V026 실행 (FK ADD 시 App 매핑 이미 반영)**. DDL lock 시간 ~수초 (row=1, 인덱스 없음). 재실행 안전 (INSERT ON CONFLICT, FK NOT EXISTS 가드) |

---

## 7. 대안 / 비채택

| 대안 | 비채택 사유 |
|------|-------------|
| DROP | 사용자 결정 C (기능 제거) 미선택. 활성 기능 |
| 마스터 없이 Enum 만 | DB 무결성 없음 — 감사 지적 해소 불충분 |

---

## 8. 사용자 승인 요청 사항

### 결정 필요
1. §3-A A-opt1 (2 테이블 분리) — 권장
2. §3-B B-opt1 (영문코드 유지) — 권장
3. §3-C C-opt1 (마스터 + Enum) — 권장
4. §2-2 repeat_type 제외 (S16-B 분리)

### 승인 확인 사항
- [ ] §3 A/B/C 결정
- [ ] §4 V026 마이그 5 Phase + 재실행 안전 + rollback
- [ ] §5 FR-1~FR-8, NFR-1~**NFR-8** (NFR-8: Enum↔마스터 drift CI 게이트)
- [ ] §6 R-1~**R-5** (R-5: 배포 리스크 DDL lock / 배포 순서)
