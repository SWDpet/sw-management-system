# [개발계획서] 사용자 통합 관리 백지(500) 정정 — Thymeleaf 3.1 `th:data-*` 제한 대응

- **작성팀**: 개발팀
- **작성일**: 2026-07-02
- **기획서**: `docs/product-specs/admin-user-list-thymeleaf31-data-attr-fix.md` (status: approved, codex 승인가능 + 사용자 최종승인 2026-07-02)
- **약칭**: TL31-DATA
- **상태**: ✅ **완료(2026-07-02)**. th:with 선계산 5필드 치환 + RELIABILITY.md §9. codex(기획·계획·구현) 전부 통과, 로컬 렌더 검증 정상(로그 예외 0), 사용자 확인 "정상". 운영 재배포는 별도(OQ-4).
- **스코프 확정**: 버그 수정만. 페이징(증상①) 별건 분리. 운영 재배포는 이번 흐름 미포함(로컬 수정·검증·commit·dual push 까지).

---

## 1. 작업 개요

**1커밋**. 뷰 템플릿 1파일 수정 + 문서 1파일 추가. JS·컨트롤러·엔티티·DB·보안 무변경.

- 변경: `src/main/resources/templates/admin-user-list.html` (민감정보 섹션, 344~404행)
- 문서: `docs/RELIABILITY.md` 에 함정 1절 추가 (FR-6)
- 무변경(확인만): `src/main/resources/static/js/admin-user.js` (data-mask-initial 계약 유지)

---

## 2. 코드 변경 상세 (admin-user-list.html)

### 2-1. 섹션 여는 태그에 `th:with` 선계산 추가 (345행)

**AS-IS**
```html
<section class="detail-section sensitive">
```

**TO-BE**
```html
<section class="detail-section sensitive"
         th:with="maskedTel=${@sensitiveMask.tel(u.tel)},
                  maskedMobile=${@sensitiveMask.mobile(u.mobile)},
                  maskedEmail=${@sensitiveMask.email(u.email)},
                  maskedAddress=${@sensitiveMask.address(u.address)},
                  maskedSsn=${@sensitiveMask.ssn(u.ssn)}">
```
- `th:with` 는 일반(비제한) 컨텍스트 → `@sensitiveMask` 빈 호출 허용.
- 변수 스코프 = 이 `<section>` 하위 전체(5개 input 모두 포함). `masked*` 접두어로 상위 변수 충돌 회피.

### 2-2. 5개 input의 `th:value`·`th:data-mask-initial` 을 변수 참조로 치환

| 필드(행) | AS-IS (양쪽 동일) | TO-BE |
|---|---|---|
| tel (353·354) | `${@sensitiveMask.tel(u.tel)}` | `${maskedTel}` |
| mobile (364·365) | `${@sensitiveMask.mobile(u.mobile)}` | `${maskedMobile}` |
| email (375·376) | `${@sensitiveMask.email(u.email)}` | `${maskedEmail}` |
| address (386·387) | `${@sensitiveMask.address(u.address)}` | `${maskedAddress}` |
| ssn (397·398) | `${@sensitiveMask.ssn(u.ssn)}` | `${maskedSsn}` |

각 필드에서 `th:value` 와 `th:data-mask-initial` **둘 다** 동일 변수를 참조 → 렌더 동등성 자동 보장(같은 문자열).

예 (tel):
```html
<!-- AS-IS -->
<input type="text" name="tel" ...
       th:value="${@sensitiveMask.tel(u.tel)}"
       th:data-mask-initial="${@sensitiveMask.tel(u.tel)}" readonly>
<!-- TO-BE -->
<input type="text" name="tel" ...
       th:value="${maskedTel}"
       th:data-mask-initial="${maskedTel}" readonly>
```

> `th:value` 는 원래도 제한 대상이 아니라 그대로 둬도 렌더는 되지만, **가독성·단일 산출점** 위해 변수 참조로 통일한다(선계산값 1개를 양쪽이 공유 → 불일치 여지 0).

### 2-3. 무변경 확인
- `th:data-user-seq="${u.userSeq}"`(352 등): 단순 프로퍼티 접근 → 제한 컨텍스트에서도 허용 → **변경 불필요**.
- 그 외 섹션(기본정보·소속·권한)의 `@authSummary` 등은 `th:data-*` 가 아니므로 무관.

---

## 3. 문서 변경 (docs/RELIABILITY.md, FR-6)

"Thymeleaf 3.1.3+ `th:data-*` 제한 컨텍스트" 절 추가:
- 증상: `th:data-*` 안 `@빈`/`T()`/`new`/`param` → `TemplateProcessingException: Instantiation ... forbidden in this context`.
- 회피: `th:with` 로 정상 컨텍스트에서 선계산 후 변수 참조.
- 재유입 방지 grep: `th:data-[a-z-]+="\$\{[^}]*(@|T\(|new )"` = 0건 유지.

---

## 4. 구현 순서 (S-n)

| ID | 단계 |
|----|------|
| S-1 | 2-1 `th:with` 추가 → 2-2 5필드 10곳 변수 치환 (admin-user-list.html) |
| S-2 | 3장 RELIABILITY.md 함정 절 추가 |
| S-3 | 전 템플릿 스윕: `grep -rnE 'th:data-[a-z-]+="\$\{[^}]*(@|T\(|new )' src/main/resources/templates/` = **0건** 확인 (T7) |
| S-4 | 앱 재기동: `DEV_HTTPS_ENABLED=false bash server-restart.sh` (Claude가 중지·기동) — 정적/템플릿 stale 방지 클린 기동 |
| S-5 | 렌더 검증(관리자 세션): `/admin/users` GET **200 + 로그 무예외**(T1), 펼치기(T2)·민감정보 보기/숨기기(T3)·승인 리다이렉트(T4)·저장(T5) 육안. null/빈 값 사용자 동등성(T6b) |
| S-6 | `./mvnw.cmd -o -q -DskipTests package` 또는 `clean verify` 로 빌드 green (템플릿 변경이라 테스트 영향 적음, 기존 green 유지) |
| S-7 | codex 구현 검증 → dual-review → 사용자 확인 → commit + `git push origin master`(dual push) |

> **⚠ 검증 함정(반복 방지)**: 200 코드만으로 통과 판정 금지. 반드시 **서버 로그에 TemplateProcessingException 부재** + 화면 육안까지 확인(과거 Thymeleaf 백지 전례). 관리자 세션 확보 방법은 S-5 착수 시 사용자와 조율(관리자 계정 로그인).

---

## 5. 검증 기준 (T / NFR 매핑)

| 항목 | 검증 단계 |
|----|------|
| T1 (`/admin/users` 200 렌더) | S-5 |
| T2·T3 (펼치기·민감정보 토글) | S-5 |
| T4·T5 (승인·저장 리다이렉트 200) | S-5 |
| T6·T6b (렌더 동등성 + null/빈) | S-5 |
| T7 (전 템플릿 스윕 0건) | S-3 |
| NFR-1 (뷰 한정) | 변경 파일 = html 1 + md 1 |
| NFR-2·3 (마스킹 정책·평문 미노출) | 초기값=마스킹 선계산값, 평문은 기존 "보기" API 만 |
| NFR-4 (실앱 육안) | S-5 |

---

## 6. 롤백

단일 커밋 `git revert <TL31-DATA 커밋>`. 뷰 전용이라 DB/데이터 영향 없음.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
