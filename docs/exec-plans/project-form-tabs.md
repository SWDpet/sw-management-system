# 개발계획서 — 사업관리 등록/수정 폼 탭 전환 (project-form-tabs)

- **상태**: v1.1 (codex 검토 반영 — 사용자 승인 대기)
- **작성일**: 2026-06-11 (v1) / v1.1 동일자 (codex: FR-6 visible 필터·FR-4 fallback·T-6 회귀체크리스트·T-2 section-0~5 명시)
- **스프린트명**: `project-form-tabs`
- **기획서**: `docs/product-specs/project-form-tabs.md` (v0.2, commit df61cfc)

---

## 0. 개요
`project-form.html` **단일 파일** 변경. 6 fieldset → 탭 6개(문서작성 `.tab-nav`/`.form-section`/`showSection` 패턴). 서버·DB·필드·저장 로직 무변경. 회귀 0 목표.

## 1. 작업 분해 (T-n)

### T-1 (CSS) 탭 스타일 추가 — `<style>` 블록
`doc-commence.html`(20~22행)의 토큰 기반 스타일 복제:
```css
.tab-nav { display:flex; flex-wrap:wrap; gap:0; background:var(--surface); border-radius:8px 8px 0 0; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,0.06); margin-bottom:0; }
.tab-nav button { flex:1; min-width:120px; padding:14px 8px; border:none; background:var(--surface-2); font-size:0.85em; font-weight:600; cursor:pointer; color:var(--text2); border-bottom:3px solid transparent; }
.tab-nav button.active { background:var(--surface); color:var(--primary); border-bottom-color:var(--primary); }
.form-section { display:none; }
.form-section.active { display:block; }
```
- **신규 `:root`/색 하드코딩 없음**(기획 §5-2). 토큰 직접 사용.

### T-2 (마크업) 탭바 + 섹션 래핑
- `<form>` 안, 첫 fieldset 앞에 **탭바** 삽입:
```html
<div class="tab-nav">
  <button type="button" class="active" onclick="showSection(0,this)">① 기본·담당자</button>
  <button type="button" onclick="showSection(1,this)">② 행정·기관</button>
  <button type="button" onclick="showSection(2,this)">③ 사업식별</button>
  <button type="button" onclick="showSection(3,this)">④ 계약정보</button>
  <button type="button" onclick="showSection(4,this)">⑤ 일정정보</button>
  <button type="button" onclick="showSection(5,this)">⑥ 제품·금액</button>
</div>
```
- **FR-7**: 모든 탭 버튼 `type="button"` (form 내 submit 방지).
- 각 fieldset 을 `<div class="form-section" id="section-0">`~`section-5` 로 래핑. **`section-0` 에만 `class="form-section active"`**, 나머지는 `active` 없음. 탭 버튼 인덱스 0~5 ↔ 섹션 id 정확히 일치. fieldset 자체·내부 필드 **그대로**.
- 저장/취소 footer 는 `.form-section` **밖**(현 369행 위치 유지) → 어느 탭에서나 저장.

### T-3 (JS) 탭 전환
```js
function showSection(idx, btn){
  document.querySelectorAll('.form-section').forEach(el=>el.classList.remove('active'));
  document.querySelectorAll('.tab-nav button').forEach(el=>el.classList.remove('active'));
  document.getElementById('section-'+idx).classList.add('active');
  btn.classList.add('active');
}
```

### T-4 (FR-4·핵심) 검증 시 탭 자동 전환
`saveProject(event)` 의 `form.checkValidity()` 실패 분기 보강:
```js
if (!form.checkValidity()) {
  const bad = form.querySelector(':invalid');
  const sec = bad ? bad.closest('.form-section') : null;
  if (bad && sec) {
    const idx = Number(sec.id.replace('section-',''));
    const btn = document.querySelectorAll('.tab-nav button')[idx];
    if (btn) showSection(idx, btn);   // 해당 탭 활성화 → 필드 visible
  }
  (bad || form).reportValidity();     // sec/btn 없으면 form fallback
  return;
}
```
(기존 confirm/AJAX 저장 흐름은 그 뒤 그대로.)

### T-5 (FR-6) Enter 다음필드 이동 한정
기존 keydown Enter 분기에서 대상 셀렉터를 **활성 탭 + 실제 visible(offsetParent) + enabled** 로 제한:
```js
const activeSection = e.target.closest('.form-section.active');
const scope = activeSection || e.target.form;
const els = Array.from(scope.querySelectorAll("input:not([type='hidden']):not([disabled]), select:not([disabled])"))
  .filter(el => el.offsetParent !== null);   // 숨김 필드 제외(실제 visible)
```
- Insert=연도/End=저장 단축키 유지. Insert 로 연도 포커스 시 ① 탭이 비활성이면 `showSection(0, …)` 후 focus(보강).

### T-6 (FR-3 회귀 체크리스트) 자동채움·복원 보존
`display` 토글이라 DOM 유지 → 복원·자동채움 동작. 단 fieldset 래핑 시 내부 `id/name/th:value/onchange` 를 **절대 건드리지 말 것**. 아래 ID·핸들러 보존 확인:
- 시도/시군구: `citySelect`(onchange loadDistricts) · `distSelect`(onchange autoFillCodes) · `distCd` · `orgCd`
- 사업종류/시스템: `prjTypeSelect`(mapPrjType)→`bizCatEn` · `sysMstSelect`(mapSysMst)→`sysNmEn`
- 청구: `payProgYn`(togglePayProgFr)→`payProgFr`
- 날짜 4필드: `oninput/onchange formatDate` 유지
- `DOMContentLoaded`(543행) 복원 로직·`th:inline` CDATA 블록 그대로.

### T-7 (검증) 브라우저 수동 테스트 — 기획 §9-1
등록/수정 저장 / year·projNm 미입력 시 탭 전환+검증 / 시도→시군구 복원 / 사업종류·시스템 자동표출 / 청구토글 / Enter·Insert·End.

## 2. 구현 순서
T-1(CSS) → T-2(마크업) → T-3(JS 전환) → T-4(검증) → T-5(Enter) → T-6(확인) → T-7(브라우저 검증) → codex 구현검토.

## 3. 회귀 / 롤백
- 단일 파일 변경 → 롤백 = 파일 revert. 서버·DB·다른 화면 무영향.
- 회귀 표면 전부 T-7 수동 검증으로 커버(단위테스트 대상 아님 — 순수 템플릿).

## 4. 미결
- 탭 라벨/아이콘 미세조정(기획 §8-3) — 구현 중 디자인 확인.
