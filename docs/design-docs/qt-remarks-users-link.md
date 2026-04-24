---
tags: [plan, sprint, refactor, schema, qt_remarks_pattern]
sprint: "qt-remarks-users-link"
status: approved-v3
created: "2026-04-20"
---

# [기획서] qt_remarks_pattern × users 연결 + 템플릿화

- **작성팀**: 기획팀
- **작성일**: 2026-04-20
- **선행**: S5 access-log-userid-fix 완료 (commit `6b8c4a1`)
- **근거**: [data-architecture-roadmap.md §S3](../design-docs/data-architecture-roadmap.md)
- **상태**: v3 (codex 승인 — 사용자 최종승인 대기)

---

## 1. 배경

### 1-1. 현황 (실측 2026-04-20)

`qt_remarks_pattern` 테이블의 7건 모두 견적서 비고용 텍스트 패턴 — **담당자 정보(이름·부서·전화·이메일)를 자유 텍스트로 하드코딩**.

| ID | pattern_name | 하드코딩 담당자 | users 매칭 |
|----|--------------|----------------|-----------|
| 1 | 담당자 박욱진 | 박욱진 (SW지원부, 070-7113-8093, 010-3056-2678) | ✅ `ukjin914` (user_id=6) |
| 2 | 담당자 이동수 | 이동수 (GIS사업부, 070-7113-9894, 010-9755-1316, leeds@uitgis.com) | ✅ `leeds` (user_id=17) — 2026-04-20 사용자 신규 등록 완료 |
| 3 | 담당자 여현정 | 여현정 (SW영업본부, 070-7113-8072, 010-2815-0957) | ✅ `yeohj` (user_id=16) ※ 부서명 미세 차이 |
| 4 | 유효기간 30일, 담당자 박욱진 | 박욱진 (동일) | ✅ `ukjin914` |
| 5 | 무상+담당자 박욱진 | 박욱진 (동일) | ✅ `ukjin914` |
| 6 | 공급가 10%+담당자 박욱진 | 박욱진 (동일) | ✅ `ukjin914` |
| 7 | 조달제품+공급가16%+담당자 박욱진 | 박욱진 (동일) | ✅ `ukjin914` |

### 1-2. 문제

1. **데이터 중복**: 박욱진 정보 5번, 여현정 1번, 이동수 1번 — 인사 정보 변경 시 7곳 동시 수정
2. **인사 정보 신뢰성**: users 테이블이 단일 진실 출처(SSoT) 인데 패턴 텍스트가 분리되어 동기화 안 됨
3. **퇴사/조직 변경 미반영**: 직급/부서/연락처 변경 시 패턴은 그대로 — 견적서에 옛 정보 출력 위험
4. **users 미등록 케이스 처리 미정**: 이동수처럼 협력사 또는 미등록 담당자 표기 정책 부재

### 1-3. ✅ 사용자 의사결정 (확정, 2026-04-20)

**OQ-1: 이동수 패턴(ID=2) 처리** → **A 확정**
- 이동수는 가입 전 사용 예정 내부 지원 인력 — 정식 등록 결정
- **사용자가 직접 등록 완료**: `user_id=17, userid=leeds, dept_nm=GIS사업부, tel=070-7113-9894, mobile=010-9755-1316, email=leeds@uitgis.com`
- 패턴 ID=2 → user_id=17 매핑

**OQ-2: tel/email 마스킹 정책** → **A의 본질 확정**
- 정책: **DB는 unmasked 저장 / 화면 표시 시점에만 마스킹 / 견적서·문서에서는 마스킹 X**
- 현재 위반 사례 발견: `user_id=6 (ukjin914 박욱진)` 만 DB에 마스킹된 값 저장 (`070-****-8093`, `u***@uitgis.com`)
- 의심 원인: 마이페이지에서 마스킹 표시 → 사용자가 그대로 저장 → 마스킹 값이 DB로 회귀
- **본 S3 스프린트 스코프**: 이름·부서·직급 placeholder만 도입 (전화/이메일은 다음 단계)
- **별도 후속 스프린트(S3-B 가칭)**: users 마스킹 회귀 데이터 정정 + 마이페이지 가드 추가 + 견적서/문서에서 unmasked 보장

### 1-4. 본 스프린트 스코프 분리

| 항목 | S3 본체 (이번) | S3-B (다음) |
|------|:---:|:---:|
| qt_remarks_pattern + user_id FK | ✅ | — |
| 이동수 users 등록 | ✅ (사용자 사전 처리) | — |
| 7건 user_id 매핑 | ✅ | — |
| Placeholder (이름·부서·직급) | ✅ | — |
| Placeholder (전화·이메일) | ❌ (스코프 외) | ✅ |
| users 마스킹 데이터 회귀 정정 | ❌ | ✅ |
| 마이페이지 마스킹-회귀 가드 | ❌ | ✅ |

---

## 2. 목표

1. `qt_remarks_pattern` 에 **`user_id` FK 컬럼** 추가 (nullable, ON DELETE SET NULL)
2. content 에 **플레이스홀더 도입**: `{username}`, `{dept_nm}`, `{position_title}` (이름·부서·직급 정도)
3. 견적서 렌더링 시 **users 런타임 조회 + 치환**
4. 기존 7건 마이그레이션 (모두 user_id 매핑):
   - 박욱진 5건 → `user_id=6`
   - 여현정 1건 → `user_id=16`
   - 이동수 1건 → `user_id=17` (사용자 사전 등록 완료, 2026-04-20)
5. 회귀 테스트 — 견적서 출력의 **포맷·치환 규칙** 동등 (문자열 완전 일치 X — 부서·직급 최신값 반영은 정상 동작)

---

## 3. 요구사항

### 3-1. 기능 요구사항 (FR)

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| FR-1 | `qt_remarks_pattern` 에 `user_id BIGINT NULL` 컬럼 추가 + FK to `users(user_id)` ON DELETE SET NULL | 필수 |
| FR-2 | 기존 6건 (박욱진 5, 여현정 1) `user_id` 자동 매핑 (1:1 매칭, 사전검증 후) | 필수 |
| FR-3 | 이동수 패턴(ID=2) → `user_id=17 (leeds)` 매핑 + content 템플릿화 (사용자 사전 등록 완료) | 필수 |
| FR-4 | RemarksPattern 엔티티에 `@ManyToOne` 또는 `Long userId` 추가 | 필수 |
| FR-5 | content 플레이스홀더 지원: `{username}`, `{dept_nm}`, `{position_title}` (확장 가능 구조) | 필수 |
| FR-6 | 견적서 화면/PDF 렌더링 시 user_id가 있으면 placeholder를 users 값으로 치환 (RemarksRenderer 신규) | 필수 |
| FR-7 | user_id 가 NULL 이면 content 원문 그대로 출력 (placeholder 미치환). **본 마이그레이션 후 7건 모두 user_id 매핑이라 NULL 케이스는 미사용**. 향후 신규 등록 시 담당자 미선택 케이스 대비용 fallback 정책 | 필수 |
| FR-8 | 비고 패턴 관리 화면(remarks-pattern-list) 에 담당자 선택 드롭다운 추가 (users active 목록) | 필수 |

### 3-2. 비기능 요구사항 (NFR)

| ID | 요구사항 |
|----|---------|
| NFR-1 | 마이그레이션 SQL 트랜잭션 내 백업 + expected/actual 검증 + 자동 ROLLBACK (S5 패턴 따름) |
| NFR-2 | 멱등성: 재실행 시 0 row update |
| NFR-3 | 회귀 테스트 — **기능 동등** 기준: 포맷/치환 규칙(라벨·괄호·구분자) 일치, 줄수 일치, placeholder 미잔존. **문자열 완전 일치는 요구하지 않음** — 마이그레이션 시점 users 값(부서·직급)이 자동 반영되는 것은 정상 동작 |
| NFR-4 | 플레이스홀더 미존재 토큰(예: `{phone}`) 입력 시 빈 문자열 또는 원문 유지 — 미정의 플레이스홀더는 보존 |
| NFR-5 | RemarksRenderer 단위 테스트 ≥ 5 케이스 |
| NFR-6 | DB 영향 — 기존 인덱스 추가 불필요 (7 row 규모) |

### 3-3. 데이터 마이그레이션 매핑 (확정안)

| pattern_id | 변경 전 | 변경 후 user_id | 변경 후 content |
|----|--------|---------------:|----------------|
| 1 | 담당자 박욱진 | **6** (ukjin914) | `※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)` |
| 2 | 담당자 이동수 | **17** (leeds) | `※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-9894  M.010-9755-1316  F.053-817-9987  E-mail : leeds@uitgis.com)` |
| 3 | 담당자 여현정 | **16** (yeohj) | `※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8072  M.010-2815-0957  F.02-561-9792)` |
| 4 | 유효기간 30일, 담당자 박욱진 | **6** | `1. 본 견적의 유효기간은 발급일로부터 30일 입니다.\n ※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)` |
| 5 | 무상+담당자 박욱진 | **6** | (동일 패턴) |
| 6 | 공급가 10%+담당자 박욱진 | **6** | (동일 패턴) |
| 7 | 조달제품+공급가16%+담당자 박욱진 | **6** | (동일 패턴) |

→ 7건 모두 user_id 매핑 = 총 영향 **7 row**

---

## 4. DB 영향

### 4-1. 스키마 변경
```sql
ALTER TABLE qt_remarks_pattern
  ADD COLUMN user_id BIGINT NULL,
  ADD CONSTRAINT fk_qt_remarks_pattern_user
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL;
```
- ON DELETE SET NULL: 사용자 삭제(퇴사) 시 패턴은 유지하되 user_id만 해제 → fallback content

### 4-2. 데이터 변경
- UPDATE **7 row** (전부 user_id 매핑 + content 템플릿화)
  - 박욱진 5건 → user_id=6
  - 여현정 1건 → user_id=16
  - 이동수 1건 → user_id=17 (사용자 사전 등록 완료)

### 4-3. 인덱스
- 추가 불필요 (7 row 규모). PostgreSQL은 FK 자체로 인덱스 자동 생성하지 않으므로 필요 시 별도 `CREATE INDEX idx_qt_remarks_pattern_user_id ON qt_remarks_pattern(user_id)` 추가 가능. 현 규모(7 row)에서는 무의미하여 생략.

---

## 5. UI 변경

### 5-1. 비고 패턴 관리 화면 (`remarks-pattern-list.html`)
- 신규/수정 폼에 **담당자 선택 드롭다운** 추가:
  ```
  담당자 (선택): [전체 사용자 ▾]   ← users active 목록 / "선택 없음"
  ```
- 드롭다운 선택 시 content 입력란 옆에 **사용 가능 플레이스홀더 안내**:
  ```
  ※ {username} → 사용자 이름
  ※ {dept_nm} → 부서명
  ※ {position_title} → 직급
  ```

### 5-2. 견적서 작성/미리보기 (`quotation-form.html`, `quotation-preview.html`)
- 비고 패턴 선택 시 미리보기 영역에 **치환된 결과 표시**
- (옵션) 화면에서 직접 편집 가능 — 이때는 치환 결과를 그대로 편집창에 펼침 (placeholder 사라짐)

---

## 6. 테스트 시나리오 (T)

| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T1 | 마이그레이션 V020 실행 | **7 row UPDATE**, expected(7)/actual 일치 |
| T2 | V020 재실행 (멱등성) | 0 row UPDATE + 백업 0건 |
| T3 | 박욱진 패턴 ID=1 렌더링 | "※ 담당자 : SW지원부 박욱진 (T.070...)" 정상 출력 (직급 컬럼 비어있으면 공백 표시) |
| T4 | 이동수 패턴 ID=2 렌더링 | "※ 담당자 : GIS사업부 이동수 (T.070-7113-9894 ...)" 정상 출력 (user_id=17 leeds 매핑) |
| T5 | 사용자 박욱진 부서 변경 후 패턴 렌더링 | 새 부서명 즉시 반영 |
| T6 | **비활성/하드 삭제 사용자 처리** — `users.enabled=false` 거나 hard delete 시 (FK ON DELETE SET NULL) 패턴 user_id가 NULL 되면 placeholder 원문 보존 출력 (단, 본 시스템은 hard delete 미사용 가정 → 평소엔 enabled=false 만 발생, 이 경우 user 정보는 여전히 조회 가능. 렌더러 정책: enabled=false 라도 username/dept_nm 치환 유지) | 명세대로 동작 (정책: enabled=false 사용자는 마지막 정보로 치환) |
| T7 | 미정의 플레이스홀더 `{phone}` 포함 content | `{phone}` 그대로 보존 |
| T8 | 비고 패턴 관리 화면에서 신규 등록 (담당자 + content) | DB 정상 저장 |
| T9 | 회귀 — 기존 견적서 미리보기 (V020 적용 후) | **포맷·치환 규칙 동등** (라벨·괄호·구분자 일치, placeholder 미잔존). 부서/직급 최신값으로 자동 반영되는 차이는 정상 |

---

## 7. 리스크

| ID | 리스크 | 완화 |
|----|-------|------|
| R1 | 박욱진/여현정 직급 정보 부재 → 렌더링 시 "이사" 누락 | users 에 `position_title` 컬럼 존재. 비어있으면 빈 문자열로 치환 |
| R2 | 마이그레이션 후 견적서 출력이 변경 전과 미세하게 다름 (공백/줄바꿈) | T9 회귀 테스트로 검증, 필요시 템플릿 미세 조정 |
| R3 | 이동수처럼 향후 미등록 담당자 추가 시 매번 자유 텍스트 패턴을 추가하는 부담 | OQ-1 결정에 따라 향후 정책 (별도 스프린트) |
| R4 | 마스킹된 users.tel/email 값이 잘못 치환되어 견적서에 노출 | OQ-2 결정 — 본 스프린트는 tel/email 플레이스홀더 도입 X |
| R5 | **soft delete vs hard delete 정책 차이** — 본 시스템은 enabled=false 로 비활성만 사용 (hard delete X). FK ON DELETE SET NULL 은 hard delete 시점에만 동작하므로, 평상시 enabled=false 사용자도 user_id 보존되어 정보 조회 가능. T6 정책 명시 완료 | 렌더러는 user_id 가 NULL 일 때만 fallback. enabled=false 는 정상 치환 (직전 정보 사용) |
| R6 | **회귀 판정 기준 모호** — "마이그레이션 전후 동일" 의 해석 차이 | NFR-3·T9 에 "기능 동등(포맷/치환 규칙 일치, 부서/직급 최신값 반영 허용)" 으로 명시 완료 |

---

## 8. 결정 사항 (사용자 확정, 2026-04-20)

- **OQ-1**: 이동수 처리 → **A 확정** (사용자 직접 등록: `user_id=17, userid=leeds`)
- **OQ-2**: tel/email 마스킹 정책 → **A 본질 확정** (DB unmasked 저장 / 화면만 마스킹 / 견적서·문서 unmasked)
  - **본 스프린트 스코프**: 이름·부서·직급 placeholder만
  - **별도 후속 스프린트(S3-B)**: users 회귀 데이터 정정 + 마이페이지 가드 + 전화/이메일 placeholder 도입

---

## 9. 릴리스 체크리스트

- [ ] codex 기획서 검토
- [ ] 사용자 최종승인
- [ ] 개발계획서 작성
- [ ] codex 개발계획 검토
- [ ] 사용자 최종승인
- [ ] 구현
- [ ] T1~T9 검증
- [ ] 사용자 확인
- [ ] git commit + push
