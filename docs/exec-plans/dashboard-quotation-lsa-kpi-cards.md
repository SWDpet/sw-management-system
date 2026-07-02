# 개발계획서 — 대시보드 견적서·LSA 발급 KPI 카드

- **스프린트**: `dashboard-quotation-lsa-kpi-cards`
- **기획서(SSoT)**: `docs/product-specs/dashboard-quotation-lsa-kpi-cards.md` (최종승인 2026-07-02, 옵션 A + "전체/월 업데이트 현황")
- **작성**: 개발팀 (Claude) · 2026-07-02
- **상태**: 초안 (codex 검토 대기)

## 0. 목표 재확인
대시보드 `.kpis` 그리드에 견적서·LSA KPI 카드 2장 추가. 각 카드 = **큰 숫자(전기간 누적 발급)** + **서브(이번 달 발급 N건)**. 클릭 → 견적서 `/quotation/ledger`, LSA `/lsa/list`. 권한(authQuotation/authLsa) 없으면 카드+fetch 미노출. DB 스키마 변경 없음, 전부 읽기 전용.

"이번 달" = `created_at` 이 현재 연·월(로컬 기준, 서버 시간)에 속함. 경계는 `created_at >= 이번달1일00:00 AND < 다음달1일00:00`.

---

## 1. 구현 순서 (단위별, 하위→상위)

### Step 1 — LSA 백엔드 (신규 통계 엔드포인트)

**1-1. `LsaRepository`** (`lsa/repository/LsaRepository.java`) — 월 범위 count 파생 메서드 1개 추가:
```java
long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        java.time.LocalDateTime start, java.time.LocalDateTime end);
```
- 근거: 기존 인터페이스에 `search()` 만 있음(L15-19). `count()` 는 JpaRepository 기본.

**1-2. `LsaService`** (`lsa/service/LsaService.java`) — 읽기 전용 집계 메서드 추가(클래스 기본 `@Transactional(readOnly=true)` L24 적용):
```java
/** 대시보드 KPI: 전체 누적 + 이번 달 발급 건수. */
public java.util.Map<String, Object> getDashboardStats() {
    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
    java.time.LocalDateTime monthEnd = monthStart.plusMonths(1);
    long total = lsaRepository.count();
    long monthCount = lsaRepository
        .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(monthStart, monthEnd);
    return java.util.Map.of("total", total, "monthCount", monthCount);
}
```

**1-3. `LsaController`** (`lsa/controller/LsaController.java`) — 권한 헬퍼 추출 + graceful 통계 엔드포인트 추가:

먼저 조회 권한 판정을 **`canViewLsa()` 헬퍼로 추출**하고 기존 `checkViewAuth()`(L47-56)가 이를 재사용하도록 리팩터(drift 방지, codex 권고):
```java
/** LSA 조회 권한(VIEW 이상 또는 관리자) — throw 하지 않는 순수 판정. */
private boolean canViewLsa() {
    CustomUserDetails cu = getCurrentUser();
    if (cu == null) return false;
    String role = cu.getUser().getUserRole();
    String authLsa = cu.getUser().getAuthLsa();
    return "ROLE_ADMIN".equals(role) || (authLsa != null && !"NONE".equals(authLsa));
}
private void checkViewAuth() {           // 기존 시그니처 유지 — 내부만 canViewLsa() 재사용
    if (!canViewLsa()) {
        log.warn("LSA 접근 권한 없음 - 사용자: {}", getCurrentUser() != null ? getCurrentUser().getUsername() : "anonymous");
        throw new InsufficientPermissionException("LSA 조회");
    }
}
```
그 다음 graceful 엔드포인트:
```java
/**
 * 대시보드 KPI 카드용 통계. 다른 /lsa 엔드포인트와 달리 권한 없으면 throw 하지 않고
 * graceful {total:0, monthCount:0} 반환(대시보드 fetch 콘솔 에러 방지). 건수 유출 아님.
 * (미인증 요청은 SecurityConfig anyRequest().authenticated() 로 이미 로그인 요구됨 — 여기 도달 = 인증된 사용자.)
 */
@GetMapping("/dashboard-stats")
@ResponseBody
public java.util.Map<String, Object> dashboardStats() {
    if (!canViewLsa()) return java.util.Map.of("total", 0L, "monthCount", 0L);
    return lsaService.getDashboardStats();
}
```
- 경로: 클래스 `@RequestMapping("/lsa")`(L31) + `/dashboard-stats` → `/lsa/dashboard-stats`.
- 견적서 카드도 동일 개념이나 견적서는 **기존** `/api/quotation/stats`(checkViewAuth throw) 재사용 — 카드/스크립트를 `th:if` 로 게이팅하므로 비권한자 브라우저에서 호출 자체가 안 나감(Step 3).

### Step 2 — 견적서 백엔드 (additive: getStats 에 monthCount 추가)

**2-1. `QuotationRepository`** (`quotation/repository/QuotationRepository.java`) — 월 범위 count 추가:
```java
long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        java.time.LocalDateTime start, java.time.LocalDateTime end);
```

**2-2. `QuotationService.getStats()`** (`quotation/service/QuotationService.java:385-397`) — `monthCount` 키 **additive** 추가:
```java
long total = quotationRepository.count();
java.time.LocalDate today = java.time.LocalDate.now();
java.time.LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
long monthCount = quotationRepository
    .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(monthStart, monthStart.plusMonths(1));
return Map.of(
    "total", total,
    "monthCount", monthCount,               // ← 신규 additive 키 (기존 키·소비처 불변)
    QtCategory.SERVICE.getLabel() + "_count",      quotationRepository.countByCategory(...),
    ... (기존 6개 키 그대로) ...
);
```
- `Map.of` 엔트리 7→8 (Map.of 는 최대 10 쌍 지원 — 안전).
- 컨트롤러(`/api/quotation/stats`, `getStats:670`) **변경 없음** — 기존 엔드포인트 그대로 재사용.

### Step 3 — 템플릿 (main-dashboard.html)

**3-1. KPI 카드 2장 추가** — `.kpis` 그리드(L123-129) GeoNURIS 카드(L128) 다음에, 각각 권한 표현식 `th:if` 로 게이팅:
```html
<!-- 견적서 발급 (authQuotation) -->
<div class="kc click" onclick="location.href='/quotation/ledger'"
     th:if="${#authorization.expression('hasRole(''ROLE_ADMIN'')') or (#authentication.principal.user?.authQuotation != null and #authentication.principal.user.authQuotation != 'NONE')}">
  <div class="l"><i class="fas fa-file-invoice-dollar"></i>견적서 발급</div>
  <div class="n"><span id="quotationCount" class="skeleton">-</span></div>
  <div class="sub" id="quotationSub">불러오는 중…</div>
</div>
<!-- LSA 발급 (authLsa) -->
<div class="kc click" onclick="location.href='/lsa/list'"
     th:if="${#authorization.expression('hasRole(''ROLE_ADMIN'')') or (#authentication.principal.user?.authLsa != null and #authentication.principal.user.authLsa != 'NONE')}">
  <div class="l"><i class="fas fa-certificate"></i>LSA 발급</div>
  <div class="n"><span id="lsaCount" class="skeleton">-</span></div>
  <div class="sub" id="lsaSub">불러오는 중…</div>
</div>
```

**3-2. fetch 스크립트 2개 추가** — 기존 script(L289-302) 인접에 **별도 `<script th:if="…">` 로 게이팅**(권한자에게만 JS 방출) + 내부 DOM 가드(2차 방어):
```html
<script th:if="${#authorization.expression('hasRole(''ROLE_ADMIN'')') or (#authentication.principal.user?.authQuotation != null and #authentication.principal.user.authQuotation != 'NONE')}">/*<![CDATA[*/
  (function(){ var el=document.getElementById('quotationCount'); if(!el) return;
    fetch('/api/quotation/stats').then(function(r){return r.ok?r.json():Promise.reject();})
     .then(function(d){ el.textContent=(d.total||0)+'건'; el.classList.remove('skeleton');
       var s=document.getElementById('quotationSub'); if(s) s.textContent='이번 달 '+(d.monthCount||0)+'건 발급'; })
     .catch(function(){ el.textContent='0건'; el.classList.remove('skeleton');
       var s=document.getElementById('quotationSub'); if(s) s.textContent='-'; });
  })();
/*]]>*/</script>
<script th:if="${…authLsa 표현식…}">/*<![CDATA[*/
  (function(){ var el=document.getElementById('lsaCount'); if(!el) return;
    fetch('/lsa/dashboard-stats')... el.textContent=(d.total||0)+'건'; sub='이번 달 '+(d.monthCount||0)+'건 발급'; ...
  })();
/*]]>*/</script>
```
- 카드가 `th:if` 로 없으면 span 부재 → `if(!el) return` 로 fetch 미실행(2차). 스크립트 자체도 `th:if` 로 미방출(1차).
- 기존 license/geonuris fetch 블록은 **그대로 유지**(회귀 방지).

---

## 2. 테스트 계획 (codex 위임 실행)

- **T-A (LSA API·권한)** `LsaControllerMvcTest`:
  - `authLsa=VIEW` 또는 admin → `GET /lsa/dashboard-stats` 200 + JSON 키 `total`,`monthCount` 존재.
  - `authLsa=NONE`(비관리자) → 200 + `{total:0, monthCount:0}` (403 아님 — graceful 의도 테스트명 명시).
- **T-B (LSA monthCount 정확성)** `LsaServiceTest`(DB 테스트, `RUN_DB_TESTS=true`): 이번 달/지난 달 created_at 데이터로 monthCount 경계(>= 1일, < 다음달1일) 검증.
- **T-C (견적서 additive)** `QuotationServiceTest`: `getStats()` 반환에 `monthCount` 존재 + 기존 키(`total`,`용역_count`,`제품_count`,`유지보수_count`,`*_amount`) 불변(개수·값).
- **T-D (템플릿 게이팅·렌더)** 브라우저 수동 QA(T-1~T-3, T-6): admin/양권한 사용자=카드 2장·데이터 채움·클릭 이동; authQuotation=NONE 사용자=견적서 카드 미표시 + 네트워크 탭 `/api/quotation/stats` **미호출**; authLsa=NONE=LSA 카드 미표시 + `/lsa/dashboard-stats` 미호출; 7카드 반응형 줄바꿈 확인.

---

## 3. 회귀 체크리스트

1. 기존 라이선스·GeoNURIS KPI 카드 정상(fetch·클릭) — 변경 없음.
2. `/api/quotation/stats` 기존 소비처(견적서 목록/통계 화면 등)가 **additive `monthCount` 키에 영향 없음** — JSON 소비자는 잉여 키 무시. (구현 전 소비처 grep 으로 재확인.)
3. `getStats` `Map.of` 8쌍 정상 컴파일·직렬화.
4. LSA 기존 목록/상세/저장/삭제 엔드포인트 무변경.
5. `.kpis` 그리드 7카드 레이아웃(데스크톱 5열 / ≤1280 3열 / ≤720 2열 자동 줄바꿈) 시각 확인.
6. 다크모드에서 신규 카드 토큰 상속 정상(`.kc` 재사용).
7. **LSA graceful API 보안 경계**(codex 권고): (a) **비로그인** `/lsa/dashboard-stats` 요청 → SecurityConfig `anyRequest().authenticated()` 로 **로그인 요구(리다이렉트/401)**; (b) **로그인 + `authLsa=NONE`** → 200 `{total:0, monthCount:0}` (건수 미노출). 두 경계를 T-A 에서 분리 검증.

---

## 4. 롤백 전략

- 모든 변경 **additive · 스키마 무변경** → 커밋 revert 로 즉시 원복.
- 단계별 독립: Step1(LSA)·Step2(견적서)·Step3(템플릿)는 서로 독립 커밋 가능. 문제가 특정 카드에 국한되면 해당 `th:if` 블록만 제거.
- DB 마이그레이션 없음 → 데이터 롤백 불필요.

---

## 5. 변경 파일 요약

| 파일 | 변경 |
|------|------|
| `lsa/repository/LsaRepository.java` | 월 범위 count 파생 메서드 1개 |
| `lsa/service/LsaService.java` | `getDashboardStats()` |
| `lsa/controller/LsaController.java` | `GET /dashboard-stats`(graceful) |
| `quotation/repository/QuotationRepository.java` | 월 범위 count 파생 메서드 1개 |
| `quotation/service/QuotationService.java` | `getStats()` 에 `monthCount` 키 additive |
| `templates/main-dashboard.html` | KPI 카드 2장 + fetch script 2개(`th:if` 게이팅) |
| `test/.../lsa/controller/LsaControllerMvcTest.java` | dashboard-stats 권한/형태 |
| `test/.../quotation/service/QuotationServiceTest.java` | getStats additive |
| (선택) `test/.../lsa/service/LsaServiceTest.java` | monthCount 경계 |

**미변경(중요)**: `MainController.java`(게이팅=템플릿 표현식), `QuotationController.java`(기존 stats 재사용), 모든 DB 초기화 SQL.
