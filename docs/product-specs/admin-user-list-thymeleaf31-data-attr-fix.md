---
tags: [plan, sprint, bugfix, thymeleaf, template, P0, regression]
sprint: "admin-user-list-thymeleaf31-data-attr-fix"
short_alias: "TL31-DATA"
status: approved
created: "2026-07-02"
revised: "2026-07-02 (codex 1차 검토 반영: 버전서술 정정·null케이스 T6b·변수명 masked*·FR-6 확정)"
---

# [기획서] 사용자 통합 관리 백지(500) 정정 — Thymeleaf 3.1 `th:data-*` 제한 컨텍스트 대응

- **작성팀**: 기획팀
- **작성일**: 2026-07-02
- **분류**: P0 버그 (렌더 500 / 관리자 사용자 관리 페이지 전면 백지)
- **근거**: 운영/로컬 증상 신고 + `logs/swmanager-error.log` 스택트레이스 + 라인/버전 대조로 근본원인 확정
- **상태**: 초안 v1 (codex 검토 대기)
- **약칭**: TL31-DATA

---

## 1. 배경

### 1-1. 증상 (사용자 신고, 2026-07-02)

| # | 신고 내용 | 실제 판정 |
|---|----------|----------|
| ① | 등록된 사용자가 다 안 보임 | **페이징(10명/페이지)** — 14명이 1/2페이지로 나뉜 것. 데이터 손실 아님. 본 sprint 스코프 외(별건 UX). |
| ② | "관리" 섹션에서 펼치기 눌러도 안 펼쳐짐 | 운영서버(구버전 빌드)의 펼치기 JS 미동작. **로컬 최신 코드에서는 렌더 자체가 500이라 페이지가 아예 안 뜸.** |
| ③ | 가입 승인대기에선 조작되는데 승인 후엔 안 됨 | 가입대기 섹션은 순수 HTML form(무JS)이라 동작. 활성 섹션은 렌더 500(로컬)/JS 미동작(운영). |
| ④ | **로컬에서 "승인" 클릭 → 하얀 페이지** | **본 sprint 핵심.** 승인 POST → `redirect:/admin/users` → 활성목록 렌더 중 Thymeleaf 예외 → 500 → 백지. |

### 1-2. 근본원인 (로그·라인·버전으로 확정)

`logs/swmanager-error.log` 에 반복 기록:

```
org.thymeleaf.exceptions.TemplateProcessingException:
Instantiation of new objects and access to static classes or parameters
is forbidden in this context (template: "admin-user-list" - line 354, col 52)
```

`admin-user-list.html` 의 민감정보 5필드가 **`th:data-*` 커스텀 속성 안에서 스프링 빈(`@sensitiveMask`)을 호출**한다:

| 라인 | 코드 |
|-----:|------|
| 354 | `th:data-mask-initial="${@sensitiveMask.tel(u.tel)}"` |
| 365 | `th:data-mask-initial="${@sensitiveMask.mobile(u.mobile)}"` |
| 376 | `th:data-mask-initial="${@sensitiveMask.email(u.email)}"` |
| 387 | `th:data-mask-initial="${@sensitiveMask.address(u.address)}"` |
| 398 | `th:data-mask-initial="${@sensitiveMask.ssn(u.ssn)}"` |

**핵심 대조**: 바로 윗줄 `th:value="${@sensitiveMask.tel(u.tel)}"`(353행)는 **정상 렌더**되는데, 같은 빈 호출인 `th:data-mask-initial`(354행)에서만 예외가 난다.
→ Thymeleaf 3.1이 **`th:data-*` 속성 표현식을 "제한된(restricted) 평가 컨텍스트"** 로 처리하여 `@빈`·`T(정적)`·`new`·`param` 접근을 금지하기 때문. (일반 변수/프로퍼티 접근은 제한 대상 아님.)

### 1-3. "왜 지금부터" — Thymeleaf 패치 업그레이드가 트리거 (codex 검토 반영 정정)

- ⚠ **정정**: Spring Boot 3.2.x도 이미 **Thymeleaf 3.1 계열**이다("3.0→3.1" 서술은 오류). 실제 트리거는 **3.1.x 패치 구간의 표현식 제한 강화**다.
- 확인된 실제 버전(클래스패스 jar 대조):
  - CVE 업그레이드 전: Spring Boot **3.2.1** → Thymeleaf **3.1.2.RELEASE**
  - CVE 업그레이드 후(현재): Spring Boot **3.5.16** → Thymeleaf **3.1.5.RELEASE**
- **Thymeleaf 3.1.3의 보안 하드닝**에서 특정 속성(`th:data-*` 등)의 표현식 평가가 제한 컨텍스트로 강화되어, 이전(3.1.2)에는 허용되던 `th:data-*` 내 `@빈` 호출이 3.1.3+ 에서 금지됨. → 3.1.2→3.1.5 상향과 함께 동일 코드가 500으로 회귀.
- (해당 5줄은 2026-04-19 스프린트 6 `e2da9a4`에 도입. 도입 당시 3.1.2에서는 통과했던 것으로 추정 — 정확한 도입~회귀 시점은 부차적이며, 확정된 근본원인은 "3.1.3+ 의 `th:data-*` 제한 강화"다.)
- **로컬**: 업그레이드 빌드(3.1.5) 실행 → 활성 사용자 ≥1명이면 `/admin/users` 무조건 500.
- **운영**: 업그레이드 전 구버전 WAR(3.1.2) → 렌더는 되나 펼치기 JS만 문제. **최신 코드(3.1.5)를 그대로 배포하면 운영도 동일 백지가 됨 → 배포 전 필수 선결.**

### 1-4. 영향 범위 (전수 조사 완료)

`th:data-*` 안에서 `@`/`T(`/`new` 를 쓰는 곳은 **전 템플릿에서 `admin-user-list.html` 5줄뿐**. 다른 페이지 연쇄 백지 없음.

```
grep -rnE 'th:data-[a-z-]+="\$\{[^}]*(@|T\(|new )' src/main/resources/templates/
→ admin-user-list.html: 354, 365, 376, 387, 398 (그 외 0건)
```

---

## 2. 목표

1. **P0 해소**: `/admin/users` 활성 사용자 목록이 Thymeleaf 3.1에서 정상 렌더(200)되게 한다. 승인/수정/펼치기/민감정보 보기 전 기능 복구.
2. **렌더 동등성**: 최종 HTML 산출물(속성값·마스킹 표시·JS 동작)은 수정 전(3.0 시절 의도)과 **완전히 동일**해야 한다. 시각/동작 변화 0.
3. **회귀 방지**: `th:data-*` 제한 컨텍스트 함정을 문서화하고, 동일 패턴 재유입을 막는 가드(테스트/체크)를 둔다.

---

## 3. 요구사항

### 3-1. 기능 요구사항 (FR)

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| FR-1 | `admin-user-list.html` 민감정보 섹션에서 마스킹값을 **정상 컨텍스트에서 1회 선계산**하고, `th:value`·`th:data-mask-initial` 둘 다 그 결과(변수)를 참조 | 필수 |
| FR-2 | 5필드(tel·mobile·email·address·ssn) 전부 동일 방식 적용 — 잔존 `th:data-*` 내 `@빈` 호출 0건 | 필수 |
| FR-3 | 렌더 결과 동등성: 각 input의 `value`·`data-mask-initial` 값이 수정 전 의도(SensitiveMask 출력)와 문자 단위 동일 | 필수 |
| FR-4 | JS(`admin-user.js`) 재마스킹 로직(`data-mask-initial` 소비)이 변경 없이 그대로 동작 | 필수 |
| FR-5 | 전 템플릿 회귀 스윕: `th:data-*` 내 `@`/`T(`/`new`/`param` 사용 0건 확인 | 필수 |
| FR-6 | 함정 문서화(확정): `docs/RELIABILITY.md` **1곳에** "Thymeleaf 3.1.3+ `th:data-*` 제한 컨텍스트 — 빈/정적/생성 접근 금지, `th:with` 선계산 회피책" 절 추가 (codex 반영: 결정 유보 → 반영 확정) | 필수 |

### 3-2. 비기능 요구사항 (NFR)

| ID | 요구사항 |
|----|---------|
| NFR-1 | 순수 템플릿(뷰) 변경. **DB·컨트롤러·엔티티·보안·권한 로직 무변경** (스코프 최소) |
| NFR-2 | 마스킹 정책 불변: DB 원본은 계속 unmasked 저장, 화면은 마스킹 표시 (S3-B 정책 유지) |
| NFR-3 | 민감정보 평문이 서버렌더 HTML에 새로 노출되지 않을 것(기존과 동일, 초기값은 마스킹값) |
| NFR-4 | 렌더 검증은 **실제 실행 앱 + 관리자 세션**으로 200 + 화면 정상 + 펼치기/보기 동작을 육안 확인 (200만으로 통과 판정 금지 — 백지 함정) |

### 3-3. 수정 방식 (확정안: `th:with` 선계산)

민감정보 `<section class="detail-section sensitive">` 여는 태그에 `th:with` 로 5개 마스킹값을 선계산한다. `th:with` 는 **정상(비제한) 컨텍스트**라 `@sensitiveMask` 호출이 허용되고, 산출된 변수는 단순 문자열이라 `th:data-*`(제한 컨텍스트)에서도 안전하게 참조된다.

변수명은 스코프 충돌 방지를 위해 접두어 `masked*` 로 명명한다(codex 권고).

```html
<section class="detail-section sensitive"
         th:with="maskedTel=${@sensitiveMask.tel(u.tel)},
                  maskedMobile=${@sensitiveMask.mobile(u.mobile)},
                  maskedEmail=${@sensitiveMask.email(u.email)},
                  maskedAddress=${@sensitiveMask.address(u.address)},
                  maskedSsn=${@sensitiveMask.ssn(u.ssn)}">
  ...
  <input ... th:value="${maskedTel}" th:data-mask-initial="${maskedTel}" readonly>
  ...
</section>
```

> 참고: `@sensitiveMask.*` 는 null 입력 시 빈 문자열(또는 안전값)을 반환하므로 선계산 결과가 `null`/빈 문자열이어도 `th:value`·`th:data-mask-initial` 이 **동일한 값**으로 렌더된다(동등성 유지). T6에서 null/빈 케이스를 명시 검증한다.

**대안(고려 후 비채택, 개발계획에서 재검토 가능)**
- (A) 컨트롤러/뷰DTO에서 마스킹 선계산 후 모델 전달 — 가장 깔끔하나 컨트롤러/DTO 변경 발생(NFR-1 위반, 스코프 확대). P0 hotfix엔 과함.
- (B) `data-mask-initial` 제거 + JS가 `input.defaultValue`로 초기 마스킹값 복원 — 속성 자체 제거 가능하나 JS 변경·회귀 리스크. 후속 정리 후보로 기록.
- **채택 = `th:with`** : 최소 변경, 뷰 한정, 마스킹 로직 단일 유지, 렌더 동등.

---

## 4. DB 영향

- **없음** (스키마·데이터·인덱스 무변경). 뷰 템플릿 전용 수정.

---

## 5. UI/코드 변경

### 5-1. 변경 파일
- `src/main/resources/templates/admin-user-list.html` (민감정보 섹션, 약 344~404행) — **유일 코드 변경**
- `src/main/resources/static/js/admin-user.js` — **무변경** (data-mask-initial 계약 유지)
- (권장) `docs/RELIABILITY.md` 또는 `docs/AGENT_GUIDELINES.md` — 함정 1절 추가

### 5-2. 디자인팀 자문 (UI 변경 정책 준수)
- 시각적 변경 **없음**(속성값 산출 경로만 변경, 마크업/클래스/토큰/다크모드 무관). → 디자인 영향 없음으로 판정. 색 토큰·specificity·`:root` self-reference 이슈 해당 없음.

---

## 6. 테스트 시나리오 (T)

| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T1 | 관리자로 `/admin/users` GET (활성 14명) | **HTTP 200 + 목록 정상 렌더** (기존: 500 백지) |
| T2 | 활성 사용자 행 "펼치기" 클릭 | 상세 행 표시, 민감정보 input 초기값=마스킹 표시 |
| T3 | 민감정보 "보기" → "숨기기" 토글 | 보기=평문 / 숨기기=`data-mask-initial`(마스킹값)로 정확 복원 (JS 무변경 동작) |
| T4 | 가입대기 사용자 "승인" 클릭 | 승인 처리 후 `/admin/users` 로 리다이렉트 → **200 정상**(기존: 백지) |
| T5 | 활성 사용자 정보 "저장" | 저장 후 리다이렉트 목록 200 정상, 펼침 상태 유지 |
| T6 | 렌더 동등성 대조 | 각 input `value`·`data-mask-initial` 이 SensitiveMask 출력과 문자 단위 일치 |
| **T6b** | **null/빈 값 케이스** (mobile·ssn·address 등이 DB에서 null/빈 문자열인 사용자) | `value`·`data-mask-initial` 이 **서로 동일**(빈 문자열)하게 렌더, 예외·`null` 리터럴 노출 없음 (codex 반영) |
| T7 | 전 템플릿 스윕 | `th:data-*` 내 `@`/`T(`/`new`/`param` = 0건 |

**검증 방법 주의**: T1·T4는 **실제 앱 실행 + 관리자 세션**으로 확인(로그 무예외 + 육안). 200 코드만으로 통과 처리 금지(Thymeleaf 백지 함정 전례).

---

## 7. 리스크

| ID | 리스크 | 완화 |
|----|-------|------|
| R1 | `th:with` 변수 스코프 오적용(섹션 밖 참조) | 변수는 해당 `<section>` 내부에서만 참조. 코드 리뷰 + T2/T3 확인 |
| R2 | 제한 컨텍스트가 `th:value`에도 실은 걸려있어 다른 우회가 필요 | 로그가 353행(th:value)은 통과·354행(data-*)만 실패로 명시 → th:value는 정상 컨텍스트 확정. 그래도 T1 렌더로 최종 확인 |
| R3 | 운영 배포 시 다른 페이지 동일 함정 잔존 | FR-5/T7 전수 스윕으로 0건 확인 완료(현재 admin만) |
| R4 | 민감정보 평문 노출 회귀 | 초기값은 여전히 마스킹값 선계산 결과. 평문은 기존과 동일하게 "보기" API 호출 시에만 |
| R5 | 운영 배포 필요성 인지 누락 | 본 기획서 §1-3에 "최신 코드 배포 시 운영도 동일 백지" 명시 → 배포 전 필수 반영 |
| R6 | 버전 원인 서술 오진(회귀분석 오염) — codex 지적 | §1-3을 실측 버전(3.1.2→3.1.5, 3.1.3 하드닝)으로 정정 완료. "3.0→3.1" 단정 제거 |
| R7 | `th:with` 변수명이 상위 컨텍스트 변수와 충돌 | `masked*` 접두어로 명명(§3-3), 섹션 내부 한정 |

---

## 8. 결정 사항 / 열린 질문 (사용자 확인 요청)

- **OQ-1 (확정)**: 수정 방식 = **`th:with` 선계산**(§3-3). 대안 A/B 비채택.
- **OQ-2 (확정)**: 함정 문서화 = `docs/RELIABILITY.md` 1곳 반영(FR-6).
- **OQ-3 (확정, 2026-07-02)**: 본 sprint = **버그 수정만**. 증상 ①(페이징 10명/페이지)은 **별건 UX 개선으로 분리** — 스코프 외.
- **OQ-4 (확정, 2026-07-02)**: 운영 WAR 재배포는 **이번 흐름에 미포함**(별도 진행). 이번 흐름 = 로컬 수정·검증·commit·dual push 까지.

> **사용자 최종승인: 2026-07-02** — 개발계획 단계 진행.

---

## 9. 릴리스 체크리스트

- [x] codex 기획서 검토 (1차 수정필요 → 반영 → 2차 **승인가능**, 2026-07-02)
- [ ] 사용자 최종승인 ← **현재 여기**
- [x] 개발계획서 작성 (`docs/exec-plans/admin-user-list-thymeleaf31-data-attr-fix.md`)
- [x] codex 개발계획 검토 (**승인가능**, 필수수정 없음, 2026-07-02)
- [x] 사용자 최종승인 (2026-07-02)
- [x] 구현 (admin-user-list.html th:with 선계산 + RELIABILITY.md §9)
- [x] 로컬 렌더 검증 (관리자 세션 정상 렌더 + 로그 TemplateProcessingException 0건, `회원 목록 조회 활성 10/전체 14` 정상, 2026-07-02)
- [x] codex 구현 검증 (**통과**)
- [x] 사용자 확인 ("정상", 2026-07-02)
- [ ] git commit + dual push (`git push origin master`) ← **진행 중**
- [ ] (별도) 운영 WAR 재배포 — OQ-4 확정대로 이번 흐름 미포함
