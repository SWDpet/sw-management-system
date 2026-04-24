---
tags: [dev-plan, sprint]
sprint: "6"
status: draft
created: "2026-04-19"
---

# [개발계획서] 사용자 통합 관리 페이지 — 하이브리드 레이아웃 — 스프린트 6

- **작성팀**: 개발팀
- **작성일**: 2026-04-19
- **근거 기획서**: [[../product-specs/admin-user-hybrid-layout|기획서 v2]] (승인)
- **상태**: 초안 (codex 검토 대기)

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔 / 현황 확인
1. `AdminUserController.updateUser` 파라미터 21개 확인 (deptNm, teamNm, tel, mobile, email, positionTitle, address, ssn, certificate, techGrade, tasks + authXxx 10) — 기존 유지.
2. `User` 엔티티 권한 필드 10개 확인: `authDashboard / authProject / authPerson / authInfra / authLicense / authQuotation / authWorkPlan / authDocument / authContract / authPerformance`.
3. `SecurityConfig` 의 `/admin/api/**` CSRF 면제 이미 등록(스프린트 5v2) — 신규 API 자동 면제.

### Step 2 — 신규 API: 민감정보 필드별 조회 (FR-3-C)

2-1. `AdminUserController` 에 추가:
```java
@ResponseBody
@GetMapping("/api/{userSeq}/sensitive")
public ResponseEntity<?> getSensitiveField(
        @PathVariable Long userSeq,
        @RequestParam String field) {
    checkAdminAuth();
    User u = userRepository.findById(userSeq).orElse(null);
    if (u == null) {
        return ResponseEntity.status(404).body(Map.of("error","NOT_FOUND"));
    }
    String value;
    switch (field) {
        case "ssn":     value = u.getSsn(); break;
        case "tel":     value = u.getTel(); break;
        case "mobile":  value = u.getMobile(); break;
        case "email":   value = u.getEmail(); break;
        case "address": value = u.getAddress(); break;
        default:
            return ResponseEntity.badRequest().body(Map.of("error","INVALID_FIELD"));
    }
    // 감사 로그 — 관리자·조회 필드·대상 userSeq 기록
    logService.log("회원관리", "민감정보조회",
        "관리자 " + getCurrentUser().getUsername() +
        " → userSeq=" + userSeq + " field=" + field);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("field", field);
    body.put("value", value != null ? value : "");
    return ResponseEntity.ok(body);
}
```
2-2. URL 은 `/admin/users/api/{userSeq}/sensitive` — 기존 컨트롤러 `@RequestMapping("/admin/users")` 하위 경로.

### Step 3 — `updateUser` redirect 보존

기존 `return "redirect:/admin/users";` → **List 기반 쿼리 조립** (codex 지적 반영 — 문자열 수작업 substring 금지):
```java
@RequestParam(value = "expand", required = false) String expand,
@RequestParam(value = "page", required = false) Integer page,
...
List<String> parts = new ArrayList<>();
if (page != null) parts.add("page=" + page);
if (expand != null && !expand.isBlank()) {
    // 검증: 쉼표로 split 후 각 토큰이 Long 인지 확인 후 재조립 (악성 문자열 차단)
    List<String> ids = new ArrayList<>();
    for (String s : expand.split(",")) {
        try { ids.add(Long.valueOf(s.trim()).toString()); }
        catch (NumberFormatException ignored) {}
    }
    if (!ids.isEmpty()) parts.add("expand=" + String.join(",", ids));
}
String target = "redirect:/admin/users" + (parts.isEmpty() ? "" : "?" + String.join("&", parts));
return target;
```

### Step 4 — `@GetMapping` 목록 핸들러에 `expand` 파라미터 파싱 + 모델 전달

```java
@RequestParam(value = "expand", required = false) String expand,
...
Set<Long> expandIds = new HashSet<>();
if (expand != null && !expand.isBlank()) {
    for (String s : expand.split(",")) {
        try { expandIds.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
    }
}
model.addAttribute("expandIds", expandIds);
```

### Step 5 — `admin-user-list.html` 활성 사용자 테이블 재구성 (핵심 변경)

5-1. **가입 대기 테이블은 변경 없음** (FR-5-A).

5-2. 활성 사용자 테이블을 **7컬럼** 으로 재정의:
- `아이디 / 성명 / 소속기관 / 부서 / 권한 요약 / 상태 / 관리`
- `<colgroup>` 제거, CSS `min-width` 기반 자동 레이아웃 (이미 스프린트 5 후속에 적용된 스타일 재활용)

5-3. 각 사용자 행(`<tr class="user-row">`)마다 **바로 아래에** `<tr class="user-detail" th:id="|detail-${u.userSeq}|" th:style="${expandIds.contains(u.userSeq)} ? 'display:table-row' : 'display:none'">` 삽입.
- `colspan="7"`
- 상세 영역은 `<form th:id="|updateForm-${u.userSeq}|" th:action="@{/admin/users/update}" method="post">` 내부에 4 섹션 포함.
- hidden: `userSeq`, `expand` (현재 펼쳐진 id 쉼표 리스트), `page`.

5-4. 권한 요약 뱃지 (FR-4): 목록 행 "권한 요약" 셀에 Thymeleaf 로 계산:
```html
<td>
    <th:block th:with="badges=${T(com.swmanager.system.util.AuthSummary).summarize(u, 4)}">
        <span th:each="b : ${badges.list}" th:class="|auth-badge ${b.cssClass}|" th:text="${b.label}"></span>
        <span th:if="${badges.moreCount > 0}" class="auth-more" th:text="|+${badges.moreCount}|"></span>
    </th:block>
</td>
```
→ 신규 유틸 `AuthSummary` 생성.

5-5. 상세 영역 4 섹션 구조 (FR-2-E):
```html
<div class="detail-grid">
  <section class="detail-section">
    <h4>기본정보</h4>
    <dl>
      <dt>아이디</dt><dd th:text="${u.userid}"></dd>
      <dt>성명</dt><dd th:text="${u.username}"></dd>
      <dt>직급</dt><dd><input name="positionTitle" th:value="${u.positionTitle}" th:attr="form='updateForm-'+${u.userSeq}"></dd>
      <dt>가입일</dt><dd th:text="${#temporals.format(u.regDt,'yyyy-MM-dd')}"></dd>
    </dl>
  </section>
  <section class="detail-section"><h4>소속·기술</h4>...</section>
  <section class="detail-section sensitive"><h4>민감정보 <small>🔒 기본 마스킹</small></h4>
    <dl>
      <dt>주민번호</dt>
      <dd class="sensitive-field" data-field="ssn" th:data-user="${u.userSeq}">
        <input name="ssn" readonly th:value="${#strings.abbreviate(u.ssn,7)+'******'}">
        <button type="button" class="btn-reveal">🔒 보기</button>
      </dd>
      ...
    </dl>
  </section>
  <section class="detail-section"><h4>권한</h4>
    <div class="auth-grid">... 10 select ...</div>
  </section>
</div>
<div class="detail-actions">
  <button type="submit">저장</button>
  <button type="button" class="btn-cancel" onclick="cancelEdit(this)">취소</button>
</div>
```

5-6. 민감정보 초기 마스킹: **서버 렌더 시점에** 이미 마스킹된 문자열을 `<input value="…">` 에 세팅. 평문은 HTML 어디에도 없음 (FR-3-A).
마스킹 헬퍼 `SensitiveMask` 신설 (Java util):
- `maskSsn("901201-1234567")` → `901201-1******`
- `maskTel("010-1234-5678")` → `010-****-5678`
- `maskEmail("hyeonjeong@domain.com")` → `h***@domain.com`
- `maskAddress("서울 강남구 테헤란로 123")` → `서울 강남구 ****`
- null/empty 안전 처리 (빈 문자열 리턴)

### Step 6 — 프런트 JS: 펼치기 / 민감정보 조회 / form submit

6-1. `static/js/admin-user.js` 신규. 기능:
- `toggleExpand(userSeq)`: detail row 표시 토글 + `expandIds` 집합 관리 + 모든 form 의 hidden `expand` 필드에 최신 리스트 반영 (hidden input 갱신)
- `revealSensitive(button)`: `/admin/users/api/{userSeq}/sensitive?field=X` 호출 → 응답 value 로 input 값 교체 + input readonly 해제 + 버튼 텍스트 "🔓 숨기기" 로 + **FR-3-E 경고 표시** (codex 지적 반영): 해당 input 주변에 `<span class="reveal-warning">⚠ 평문 표시 중 — 편집 후 저장하세요</span>` 삽입. input 에 `.is-revealed` 클래스 부여 (배경 하이라이트).
- `maskSensitive(button)`: JS 변수 초기화 + input 값을 **서버 렌더 시 보관된 초기 마스킹 값** (`data-mask-initial` 속성, **마스킹된 값 자체만** — 평문 금지) 으로 복구. `.is-revealed` 클래스 제거 + 경고 span 제거.
- `cancelEdit(button)`: 폼의 모든 input·select 를 서버 렌더 초기값으로 원복 (각 엘리먼트의 `defaultValue` 활용). 보이는 상태의 민감 필드도 재마스킹.
- 페이지 이탈 (`beforeunload`) 시 모든 `.is-revealed` 자동 재마스킹 (메모리에서 평문 제거).

6-2. `admin-user-list.html` 하단에 `<script src="/js/admin-user.js"></script>` 추가.

### Step 7 — CSS (admin-user-list.html `<style>` 내 추가)

```css
.auth-badge { display:inline-block; padding:2px 6px; border-radius:3px; font-size:0.72em;
              color:white; margin-right:3px; font-weight:600; }
.auth-badge.view { background:#3b82f6; }
.auth-badge.edit { background:#10b981; }
.auth-more { font-size:0.72em; color:#6b7280; }
.user-detail td { background:#f9fafb; padding:16px 20px; }
.detail-grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
@media (max-width: 1280px) { .detail-grid { grid-template-columns:1fr; } }
.detail-section { background:white; padding:14px 16px; border:1px solid #e3e6f0; border-radius:6px; }
.detail-section h4 { margin:0 0 10px; font-size:0.92em; color:#1a237e; border-bottom:1px solid #e0e0e0; padding-bottom:6px; }
.detail-section dl { display:grid; grid-template-columns:100px 1fr; gap:6px 10px; margin:0; }
.detail-section dl dt { font-weight:600; color:#6b7280; font-size:0.85em; align-self:center; }
.detail-section dl dd { margin:0; font-size:0.88em; }
.detail-section dl dd input { width:100%; box-sizing:border-box; padding:5px 8px; border:1px solid #d1d3e2; border-radius:4px; font-size:0.88em; }
.sensitive input[readonly] { background:#fff3cd; }
.btn-reveal { background:transparent; border:1px solid #d1d3e2; border-radius:4px; padding:3px 8px; font-size:0.78em; cursor:pointer; margin-left:5px; }
.btn-reveal.revealed { background:#fef3c7; border-color:#f59e0b; }
.auth-grid { display:grid; grid-template-columns:repeat(auto-fit, minmax(180px, 1fr)); gap:8px; }
.auth-grid select { width:100%; }
.btn-expand { background:#3b82f6; color:white; border:none; padding:5px 10px; border-radius:4px; cursor:pointer; font-size:0.82em; }
.btn-expand.expanded { background:#6b7280; }
.detail-actions { margin-top:14px; text-align:right; }
.detail-actions button { padding:8px 18px; border-radius:5px; font-weight:600; font-size:0.9em; cursor:pointer; margin-left:6px; }
.detail-actions button[type=submit] { background:#10b981; color:white; border:none; }
.detail-actions .btn-cancel { background:white; border:1px solid #d1d3e2; }
```

### Step 8 — 유틸 클래스 신설

8-1. `src/main/java/com/swmanager/system/util/SensitiveMask.java`:
- `public static String ssn(String)`, `tel(String)`, `mobile(String)`, `email(String)`, `address(String)`
- **입력이 null/empty 이면 빈 문자열 반환**.
- **예상 포맷이 아닌 경우에도 절대 원본 반환 금지** (codex 지적 반영 — FR-3-A 준수).
  - 포맷 불일치 시 "안전 기본 마스킹" 적용: 전체를 `***` 로 치환하거나, 첫 1자만 유지 후 `***` (예: `k***`).
  - 이 정책을 T2 에도 추가: **평문 패턴(주민번호 13자리, `@` 있는 이메일 등) 이 서버 렌더 HTML 에 절대 나타나지 않음**.

8-2. `src/main/java/com/swmanager/system/util/AuthSummary.java`:
```java
public class AuthSummary {
    public static class Badge { public String label; public String cssClass; }
    public static class Result { public List<Badge> list; public int moreCount; }
    public static Result summarize(User u, int max) {
        // 10개 auth 필드 순회, NONE 아닌 것만 뱃지 생성.
        // max 초과 시 앞 max 만 남기고 moreCount 계산.
    }
}
```
약어 매핑: 기획서 FR-4-C 참조.

### Step 9 — 감사 보고서 / 문서

- `docs/generated/audit/dashboard.md` 에 스프린트 6 완료 한 줄 기록.
- ERD 변경 없음.

### Step 10 — 빌드 / 재기동 / 검증 / codex

- `./mvnw -q compile` BUILD SUCCESS
- `bash server-restart.sh`
- 로그인 후 수동 확인 + codex Specs 검증

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | 활성 사용자 테이블 7컬럼 | `rg -n "<th>.*</th>" src/main/resources/templates/admin-user-list.html` 에서 두 번째 테이블 헤더 수 | 7개 |
| T2 | 민감정보 input 초기값 마스킹 (서버 렌더) | `curl -s --cookie "..." http://localhost:9090/admin/users` 응답에서 정규식 검사: 주민번호 패턴 `[0-9]{6}-[1-4][0-9]{6}`, 이메일 `@[a-zA-Z0-9.-]+\.[a-z]{2,}` (단 `u***@domain.com` 같은 마스킹된 건 제외) | **0 hit** — 평문 PII 가 HTML 본문에 어디에도 없어야 함 (FR-3-A 강화) |
| T2-포맷불일치 | 마스킹 포맷 예외 케이스 | 테스트 DB 에 `ssn='abc123'` (포맷 불일치) 사용자 준비 → 목록 렌더 HTML 검사 | HTML 에 `abc123` 원본 문자열 0건, 안전 기본 마스킹(`***` 또는 `a***`) 만 표시 |
| T3 | `data-raw` 평문 속성 절대 금지 | `rg -n "data-raw\|data-plain" src/main/resources/templates/admin-user-list.html` | 0 hits |
| T4 | 신규 API `/admin/users/api/{seq}/sensitive` 존재 | `rg -n "sensitive" src/main/java/com/swmanager/system/controller/AdminUserController.java` | ≥ 2 hits |
| T5 | 민감정보 조회 감사 로그 | 서버 실행 후 curl 로 해당 API 호출 → `grep -c "민감정보조회" server.log` | 호출 수만큼 증가 |
| T6 | 신규 API 필드 1개만 반환 | `curl ... /sensitive?field=ssn` 응답 | JSON 키 `field`, `value` 2개만 |
| T7 | 필드명 잘못된 경우 400 | `curl ... /sensitive?field=evil` | HTTP 400 `code=INVALID_FIELD` |
| T8 | 비ADMIN 접근 차단 | 비 ADMIN 쿠키로 `/admin/users/api/1/sensitive?field=ssn` | 302 로그인 리다이렉트 또는 403 |
| T9 | expand 다중 펼침 복원 | `/admin/users?expand=1,2` GET | DOM 에서 `#detail-1` 과 `#detail-2` 가 `display:table-row` |
| T10 | 저장 후 expand 유지 | update 제출 시 hidden `expand=1` → 서버 redirect → 응답 Location | `expand=1` 파라미터 포함 |
| T11 | 권한 요약 뱃지 | 테스트 사용자 (authDashboard=VIEW, authProject=EDIT, 나머지 NONE) 렌더 | `대시V` `사업E` 2개 뱃지 + "+N" 없음 |
| T12 | 권한 요약 5+ 잘림 | 6개 NONE 아닌 사용자 | 앞 4개 + `+2` 표시 |
| T13 | 반응형 1280px 이하 1열 스택 | DevTools 1200px 해상도에서 `.detail-grid` | `grid-template-columns: 1fr` |
| T14 | 1920px 가로 스크롤 없음 | 1920 해상도 기본 뷰 | 테이블 래퍼 `scrollWidth <= clientWidth` |
| T15 | Maven compile | `./mvnw -q compile` | BUILD SUCCESS |
| T16 | 서버 재기동 | `bash server-restart.sh` | Started + ERROR 0 |
| T17 | 기존 회원 관리 기능 회귀 없음 | 승인/수정/삭제 3건 모두 기존대로 동작 | 수동 확인 |
| T18 | FR-3-E 경고 표시 | reveal 버튼 클릭 후 DOM | `.reveal-warning` 엘리먼트 존재 + 해당 input `.is-revealed` 클래스 |
| T19 | 페이지 이탈 자동 재마스킹 | reveal 후 다른 페이지로 이동 → 뒤로가기 | 해당 input 값이 초기 마스킹으로 복구, JS 메모리 평문 해제 |
| T20 | redirect 쿼리 조립 안정성 | `expand=12` 단일, `expand=1,2,3` 복수, 빈값 각 케이스 | 셋 다 substring 에 의한 잘림 없음 (T10 보강) |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 5 템플릿 오류로 렌더 실패 | 기존 html git revert |
| Step 2 API 에러 | controller 롤백. JS 에서 reveal 버튼 클릭 시 에러 처리 (alert) 이미 포함 |
| 민감정보 마스킹 함수 예외 | `SensitiveMask` null-safe 보장. 실패 시 빈 문자열 반환 |
| expand 쉼표 파싱 실패 | try-catch 로 무시, 모든 행 접힘 |
| 배포 후 회귀 | `git revert <sprint-6-commit>` → 재배포 |

---

## 4. 리스크·완화

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 민감정보 마스킹 회귀 | 중간 | T2, T3 으로 정적 검증 + T5 로 런타임 감사 로그 확인 |
| JS reveal 상태 꼬임 | 낮음 | 버튼 class 기반 단순 상태 (`.revealed` toggle) |
| 권한 요약 뱃지 CSS 깨짐 | 낮음 | 기본 color 스타일만 사용. 브라우저 호환성 충분 |
| 기존 updateForm 서브밋 구조 변경 리스크 | 중간 | `action="/admin/users/update"` 유지. 파라미터 필드명 동일 |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
