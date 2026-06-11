# 기획서 — 사업관리 등록/수정 폼 탭 전환 (project-form-tabs)

- **상태**: v0.2 (codex 검토 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-11 (v0.1) / v0.2 동일자 (codex: FR-6 Enter 한정·FR-7 type=button·회귀 테스트 항목)
- **스프린트명**: `project-form-tabs`
- **요청자**: 박욱진 (사용자)
- **참조 패턴**: `templates/document/doc-commence.html` (문서작성 8~9탭, `.tab-nav` + `.form-section` + `showSection`)

---

## 0. 한 줄 요약

사업관리 **등록/수정 폼**(`project-form.html`)의 한 페이지 6개 fieldset 을, 문서작성과 동일한 **탭 6개**로 나눠 입력·수정 시 스크롤·인지부하를 줄인다. 필드·저장 로직·자동채움 JS 는 **그대로 유지**.

## 1. 배경 / 목적

- 현재 `project-form.html` 은 ①기본·담당자 ②행정·기관 ③사업식별 ④계약정보 ⑤일정정보 ⑥제품·금액 **6개 fieldset 이 세로로 나열** → 길고 한눈에 안 들어옴.
- 문서관리의 문서작성은 이미 **탭 방식**으로 섹션을 나눠 쓰기 편함. 같은 패턴으로 통일.

## 2. 범위 (In / Out)

**In**
- `project-form.html` 의 6 fieldset → **탭 6개** 전환 (등록·수정 공용 화면).
- `project-detail.html`(상세조회+수정모드, **동일 6 fieldset 구조**) → **탭 6개** 전환. (2026-06-11 사용자 추가요청)
  - 상세는 `required`·Enter 다음필드 이동 없음 → FR-4/FR-6 불필요. CSS + 탭바 + `showSection` 만.
- 문서작성의 `.tab-nav`/`.form-section`/`showSection(idx, btn)` 패턴 재사용.
- 검증(required) 실패 시 **해당 필드가 있는 탭으로 자동 전환** 후 표시(폼 한정).

**Out**
- 목록조회(`project-list`) 는 본 건 Out (필터/검색 성격).
- 필드 추가/삭제·저장 로직·DB·자동채움(시도→시군구, 코드 자동) **변경 없음**.
- 탭 간 데이터 의존/마법사(wizard) 단계 강제 없음 — 자유 이동.

## 3. 사용자 스토리

> 사업 담당자로서, 사업 등록/수정 시 긴 폼을 스크롤하지 않고 **탭으로 구획**해 필요한 항목 그룹만 보며 입력하고 싶다.

## 4. 기능 요구사항 (FR)

| ID | 요구사항 |
|---|---|
| FR-1 | 6 fieldset 을 탭 6개로: ①기본·담당자 ②행정·기관 ③사업식별 ④계약정보 ⑤일정정보 ⑥제품·금액. 기본 활성 = ①. |
| FR-2 | 탭 전환은 클릭 시 즉시(클래스 토글). 모든 필드는 **한 `<form>` 안**에 유지 → 저장 시 전 탭 값 일괄 제출(현 단일 폼 그대로). |
| FR-3 | 필드 `name`·`id`·`th:value`·자동채움 JS(loadDistricts/autoFillCodes/mapPrjType/mapSysMst/togglePayProgFr)·날짜포맷·DOMContentLoaded 복원 **100% 보존**. |
| FR-4 (검증 UX·핵심) | `required`(year=①, projNm=④) 가 **비활성 탭에 있어도** 저장 검증이 동작해야 함. `checkValidity()` 실패 시 **첫 invalid 필드의 탭으로 자동 전환** 후 `reportValidity()` 호출(브라우저 "focusable 아님" 오류 회피). |
| FR-5 | 수정 모드 진입 시 기존값 복원은 현행 그대로(탭 숨김과 무관 — display 토글이라 값은 유지). |
| FR-6 | 키보드 단축키(Enter 다음필드, Insert=연도, End=저장) 유지. **Enter 다음필드 이동은 `.form-section.active` 내 visible·enabled 필드로 한정**(숨김 탭 필드로 포커스 점프 방지). |
| FR-7 (함정) | 탭 nav `<button>` 은 `<form>` 안에 있으므로 **반드시 `type="button"`**(기본 submit 방지). 문서작성 패턴 복사 시 누락 주의. |

## 5. UI 설계 (🎨 디자인팀 자문 대상)

### 5-1. 구조
```
[페이지 헤더]
[탭바: ①기본·담당자 ②행정·기관 ③사업식별 ④계약정보 ⑤일정정보 ⑥제품·금액]
[활성 탭 내용(fieldset 1개)]
[저장 / 취소 footer — 탭 밖, 항상 노출]
```
- 저장 버튼은 탭 밖(footer)에 고정 → 어느 탭에서든 저장 가능.

### 5-2. 🎨 디자인팀 가상 자문 결과
- **탭 스타일 = 문서작성 `.tab-nav` 토큰 재사용**: `var(--surface-2)`(비활성)·`white`+`var(--primary)`+하단 `var(--primary)` 보더(활성) → **다크모드 자동 일관**(하드코딩 색 없음).
- project-form 은 로컬 `:root` 별칭(`--primary-color: var(--primary)` 등) 사용 중 → 탭 CSS 는 **원 토큰(`var(--primary)` 등) 직접 사용**, 신규 `:root` self-reference·중복 별칭 추가 **금지**.
- 접근성: 탭 버튼 `role="tab"` 대신 문서작성과 동일한 **disclosure 단순 패턴**(button + active class) 유지(일관성). 단 활성 탭 시각표시(보더+색) 외 `aria-selected` 추가 권장.
- 반응형: 탭 6개가 좁은 화면에서 wrap 되도록 `flex-wrap` 허용(문서작성은 9탭 flex). 모바일 가독성 점검.
- ⚠ 판정: UI 변경이나 **기존 문서작성 탭 패턴·토큰 재사용**, 신규 컴포넌트/토큰 없음 → 디자인 리스크 낮음. (A 정책 자문 완료: 토큰·다크모드·specificity·:root self-ref 체크 통과.)

## 6. 기술 리스크 / 설계 (핵심)

- **required + display:none (FR-4)**: 비활성 탭을 `display:none` 으로 숨기면 그 안의 `required` 미충족 시 `reportValidity()` 가 **포커스 불가 오류**를 던지거나 조용히 막힘. → submit 인터셉트에서:
  1. `form.checkValidity()` false 면 `form.querySelector(':invalid')` 로 첫 invalid 탐색,
  2. 그 필드가 속한 `.form-section` 의 탭으로 `showSection` 전환,
  3. `field.reportValidity()` 또는 `form.reportValidity()` 호출.
- `saveProject(event)` 의 기존 `checkValidity/reportValidity` 흐름에 위 분기 삽입(저장 AJAX 로직은 유지).

## 7. 회귀 / 비기능

- **회귀 표면**: 저장(등록/수정) 동작, 시도→시군구 캐스케이드·코드 자동채움, 수정 복원, 단축키. 필드·name 불변이라 서버측 무영향.
- 성능·DB·API 변경 0. 순수 템플릿(HTML/CSS/JS) 변경.

## 8. 미결 / 결정사항

1. **(확정)** 범위 = 등록/수정 폼만. — 2026-06-11 사용자.
2. **(확정)** 탭 = 6 fieldset 그대로 6탭. — 2026-06-11 사용자.
3. **(미결)** 탭 라벨 표기(①기본·담당자 vs 아이콘+짧은 라벨) — 구현 시 디자인 미세조정.

## 9-1. 회귀 테스트 항목 (codex — 개발계획 이관)
- 등록 저장 / 수정 저장 정상.
- `year` 미입력 → ① 탭 전환+검증 / `projNm` 미입력 → ④ 탭 전환+검증(FR-4).
- 시도→시군구 캐스케이드 + 수정 시 시군구/코드 복원.
- SW사업종류·시스템명 선택 → 영문명 자동표출.
- 청구여부 Y/N → 청구형태 토글.
- Enter(활성탭 내 이동)·Insert(연도)·End(저장) 단축키.

## 9. 다음 단계
codex 검토(완료, v0.2 반영) → 사용자 최종승인 → 개발계획서 → codex → 승인 → 구현(단일 템플릿 수정).
