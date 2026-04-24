# 🛠️ 개발계획서 — SW 사업 추진 현황 검색 개편

- 작성일: 2026-04-19 (r2)
- 작성팀: 개발팀 (Claude)
- 연관 기획서: [../plans/project-search-refactor.md](../plans/project-search-refactor.md)
- 예상 작업 시간: 약 3~4시간 (코딩 2h + 테스트 1h + 문서)
- 개정: r1 codex 검토 반영 — year 바인딩 String화, AJAX fallback 옵션 보존, API 경로 통일, 자동화 테스트 세트 추가, 롤백 전략 명확화, 리스크 보강

---

## 0. 기획 요구사항 매핑 매트릭스

| 기획 ID | 내용 | 구현 위치 | 테스트 |
|--------|------|----------|-------|
| FR-1 | `&` AND 토큰 제거 | SwService.search (토큰 분리 제거) | T9, T10 |
| FR-2 | kw 단일 텍스트 유지 | SwService.search(kw→LIKE OR) | T1 |
| FR-3 | 년도 드롭다운 | Controller inject + View select | T3, T6 |
| FR-4 | 시도 드롭다운 | Controller inject + View select | T2, T3 |
| FR-5 | 시군구 드롭다운 (city 종속) | AJAX /api/dist-options?city=X | T2, T15 |
| FR-6 | 시스템(EN) 드롭다운 | Controller inject + View select | T3 |
| FR-7 | 📌 핀 기능 | View JS + LocalStorage | T4, T5, T19, T20 |
| FR-8 | 초기화 (핀 유지) | View JS | T4 |
| FR-9 | 부분 호환 (&토큰 자유검색) | SwService.search(kw 전체 매칭) | T8, T9, T10 |
| FR-10 | district 단독 무시 | Specification.city==null → district skip | T15 |
| NFR-1~9 | 비기능 요건 | 각 구현 시점 | T11~T18 |

---

## 1. 파일별 변경 스코프

### 1-1. `SwProjectRepository.java`
**추가 메서드 3개** (기존 year 필수 버전과 공존, 혼선 방지 주석 추가):
```java
@Query("SELECT DISTINCT p.cityNm FROM SwProject p WHERE p.cityNm IS NOT NULL ORDER BY p.cityNm")
List<String> findAllDistinctCityNms();

@Query("SELECT DISTINCT p.sysNmEn FROM SwProject p WHERE p.sysNmEn IS NOT NULL ORDER BY p.sysNmEn")
List<String> findAllDistinctSysNmEns();

@Query("SELECT DISTINCT p.distNm FROM SwProject p WHERE p.cityNm = :city AND p.distNm IS NOT NULL ORDER BY p.distNm")
List<String> findAllDistinctDistNmsByCity(@Param("city") String city);
```
- 기존 `findDistinctYears()` 는 **그대로 재사용**
- 명명은 `findAll...` prefix 로 year-무관 임을 명시

### 1-2. `SwService.java`
**새 `search` 오버로드 추가**, 기존 `search(String, Pageable)` 는 내부 호출로 리다이렉트 유지:

```java
public Page<SwProject> search(
    String kw, String year, String city, String district, String sysNmEn,
    Pageable pageable
) {
    // 정규화 (NFR-5, NFR-7)
    String kwN = normalize(kw);
    Integer yearN = parseYearSafe(year);       // "abc" → null, 범위 밖 → null
    String cityN = normalize(city);
    String distN = (cityN == null) ? null : normalize(district);  // FR-10
    String sysN = normalizeUpper(sysNmEn);
    
    Specification<SwProject> spec = Specification.where(null);
    if (yearN != null) spec = spec.and(eq("year", yearN));
    if (cityN != null) spec = spec.and(eq("cityNm", cityN));
    if (distN != null) spec = spec.and(eq("distNm", distN));
    if (sysN != null) spec = spec.and(eq("sysNmEn", sysN));
    if (kwN != null) spec = spec.and(kwOrSpec(kwN));
    
    return swProjectRepository.findAll(spec, pageable);
}

// 기존 search(String kw, Pageable) 는 새 search 호출로 대체 (하위 호환)
public Page<SwProject> search(String kw, Pageable pageable) {
    return search(kw, null, null, null, null, pageable);
}

// 기존 buildAndSpec/tokens 관련 private 메서드는 제거
```

**kwOrSpec(kw)**: 기존 kw 가 여러 컬럼 OR LIKE 매칭하던 로직 재사용 (LOWER 비교, % 래핑). **& 토큰 분리 제거**.

**정규화 유틸 (SwService private static):**
```java
private static String normalize(String v) {
    if (v == null) return null;
    String t = v.trim();
    return t.isEmpty() ? null : t;
}
private static Integer parseYearSafe(String v) {
    String n = normalize(v);
    if (n == null) return null;
    try {
        int y = Integer.parseInt(n);
        return (y >= 2000 && y <= 2099) ? y : null;
    } catch (NumberFormatException e) {
        return null;  // "abc" 등 무시 (NFR-7)
    }
}
private static String normalizeUpper(String v) {
    String n = normalize(v);
    return n == null ? null : n.toUpperCase();
}
```

### 1-3. `SwController.java`
**GET `/projects/status`** 시그니처 확장:

**중요**: `year`는 **String으로 받는다** (잘못된 입력 `?year=abc` 시 Spring이 400 내는 것 방지, NFR-7 안전 파싱 구현).

```java
@GetMapping("/status")
public String projectList(
    Model model,
    @RequestParam(defaultValue="0") int page,
    @RequestParam(defaultValue="") String kw,
    @RequestParam(required=false) String year,       // String으로 변경 (NFR-7)
    @RequestParam(required=false) String city,
    @RequestParam(required=false) String district,
    @RequestParam(required=false) String sysNmEn
) {
    // ... auth 체크 기존 유지
    
    Sort sort = ...; // 기존 유지
    Pageable pageable = PageRequest.of(page, 10, sort);
    Page<SwProject> paging = swService.search(kw, year, city, district, sysNmEn, pageable);
    
    // Model 주입
    model.addAttribute("paging", paging);
    model.addAttribute("kw", kw);
    model.addAttribute("year", year);
    model.addAttribute("city", city);
    model.addAttribute("district", district);
    model.addAttribute("sysNmEn", sysNmEn);
    model.addAttribute("yearOptions", swProjectRepository.findDistinctYears());
    model.addAttribute("cityOptions", swProjectRepository.findAllDistinctCityNms());
    model.addAttribute("sysNmEnOptions", swProjectRepository.findAllDistinctSysNmEns());
    // district 는 city 선택된 경우에만 서버 렌더 (초기 로드)
    model.addAttribute("districtOptions",
        city != null && !city.isBlank()
            ? swProjectRepository.findAllDistinctDistNmsByCity(city)
            : java.util.List.of());
    return "project-list";
}
```

**신규 API: `/projects/api/dist-options`** (AJAX 시군구 종속 로드)
```java
@GetMapping("/api/dist-options")
@ResponseBody
public List<String> getDistrictOptions(@RequestParam String city) {
    if (city == null || city.isBlank()) return List.of();
    return swProjectRepository.findAllDistinctDistNmsByCity(city);
}
```

**기존 `/projects/api/districts?sido=X`** 는 건드리지 않음 (다른 화면에서 사용 중일 수 있음).

### 1-4. `project-list.html`

#### 제거
- `.search-hint` 영역 (힌트 태그 바)
- `.hint-tag`, `.search-input.multi-mode` 스타일
- `appendHint()` JS 함수
- 힌트 태그 클릭 로직

#### 추가
**검색 영역 HTML**
```html
<form id="searchForm" method="get" action="/projects/status" class="search-form">
    <input type="hidden" name="size" value="10">
    
    <input type="text" name="kw" id="kwInput" th:value="${kw}"
           class="search-input" placeholder="사업명·발주처·PMS코드 검색">
    
    <div class="filter-grid">
        <div class="filter-cell">
            <button type="button" class="pin-btn" data-target="year" title="고정">📌</button>
            <select name="year">
                <option value="">전체 연도</option>
                <option th:each="y : ${yearOptions}" th:value="${y}" th:text="${y}"
                        th:selected="${year != null && y == year}"></option>
            </select>
        </div>
        <div class="filter-cell">
            <button type="button" class="pin-btn" data-target="city" title="고정">📌</button>
            <select name="city" id="citySelect">
                <option value="">전체 시도</option>
                <option th:each="c : ${cityOptions}" th:value="${c}" th:text="${c}"
                        th:selected="${city != null && c == city}"></option>
            </select>
        </div>
        <div class="filter-cell">
            <button type="button" class="pin-btn" data-target="district" title="고정">📌</button>
            <select name="district" id="districtSelect"
                    th:attrappend="disabled=${city == null || #strings.isEmpty(city) ? 'disabled' : null}">
                <option value="">전체 시군구</option>
                <option th:each="d : ${districtOptions}" th:value="${d}" th:text="${d}"
                        th:selected="${district != null && d == district}"></option>
            </select>
        </div>
        <div class="filter-cell">
            <button type="button" class="pin-btn" data-target="sysNmEn" title="고정">📌</button>
            <select name="sysNmEn">
                <option value="">전체 시스템</option>
                <option th:each="s : ${sysNmEnOptions}" th:value="${s}" th:text="${s}"
                        th:selected="${sysNmEn != null && s == sysNmEn}"></option>
            </select>
        </div>
    </div>
    
    <button type="submit" class="btn-search"><i class="fas fa-search"></i> 검색</button>
    <button type="button" class="btn-reset" onclick="resetFilters()">초기화</button>
</form>
```

**페이지네이션 링크 수정**: 현재 필터 파라미터 URL 에 유지 (NFR-4)
```html
<a th:href="@{/projects/status(page=${paging.number - 1}, kw=${kw},
     year=${year}, city=${city}, district=${district}, sysNmEn=${sysNmEn}, size=10)}">이전</a>
```

#### CSS 추가
```css
.filter-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.filter-cell { display: flex; align-items: center; gap: 4px; }
.filter-cell select { padding: 7px 10px; border-radius: 8px; border: 1.5px solid #E7E5E4; font-size: 0.85rem; background: #fff; }
.filter-cell select:disabled { background: #F5F5F4; cursor: not-allowed; color: #A8A29E; }
.filter-cell.pinned select { background: #F0FDFA; border-color: #99F6E4; }
.pin-btn {
    background: transparent; border: 1px solid #E7E5E4; color: #A8A29E;
    width: 28px; height: 28px; border-radius: 6px; cursor: pointer;
    display: inline-flex; align-items: center; justify-content: center;
    font-size: 0.72rem; transition: all 0.15s;
}
.pin-btn:hover { background: #F5F5F4; }
.pin-btn.active { background: #F0FDFA; color: #0F766E; border-color: #99F6E4; }
.btn-reset {
    background: transparent; color: #57534E; border: 1px solid #E7E5E4;
    padding: 8px 16px; border-radius: 8px; font-size: 0.85rem; cursor: pointer;
}
.btn-reset:hover { background: #F5F5F4; }
```

### 1-5. JavaScript (inline in project-list.html)

```javascript
(function(){
    var PIN_KEY = 'swProjSearchPins';
    var VAL_KEY = 'swProjSearchValues';
    var FIELDS = ['year','city','district','sysNmEn'];
    
    function loadPins() {
        try { return JSON.parse(localStorage.getItem(PIN_KEY)) || {}; }
        catch(e) { console.warn('pin parse fail', e); return {}; }
    }
    function savePins(p) { localStorage.setItem(PIN_KEY, JSON.stringify(p)); }
    function loadVals() {
        try { return JSON.parse(localStorage.getItem(VAL_KEY)) || {}; }
        catch(e) { return {}; }
    }
    function saveVals(v) { localStorage.setItem(VAL_KEY, JSON.stringify(v)); }
    
    // 1. 초기: URL 파라미터 > LocalStorage 핀 복원
    var urlParams = new URLSearchParams(location.search);
    var pins = loadPins();
    var vals = loadVals();
    
    FIELDS.forEach(function(f) {
        var select = document.querySelector('[name="'+f+'"]');
        var btn = document.querySelector('.pin-btn[data-target="'+f+'"]');
        if (!select || !btn) return;
        
        // 핀 버튼 상태
        if (pins[f]) {
            btn.classList.add('active');
            select.closest('.filter-cell').classList.add('pinned');
            // URL 에 값이 없고 LocalStorage 에만 있으면 복원 (URL 우선)
            if (!urlParams.has(f) && vals[f]) {
                select.value = vals[f];
            }
        }
        
        // 핀 토글 이벤트
        btn.addEventListener('click', function() {
            pins[f] = !pins[f];
            if (pins[f]) {
                btn.classList.add('active');
                select.closest('.filter-cell').classList.add('pinned');
                vals[f] = select.value;
                saveVals(vals);
            } else {
                btn.classList.remove('active');
                select.closest('.filter-cell').classList.remove('pinned');
                delete vals[f];
                saveVals(vals);
            }
            savePins(pins);
        });
        
        // 핀 켜진 필드 값이 바뀌면 vals 에도 저장
        select.addEventListener('change', function() {
            if (pins[f]) { vals[f] = select.value; saveVals(vals); }
        });
    });
    
    // 2. 시도 변경 시 시군구 AJAX (NFR-8: 실패 시 기존 옵션 보존)
    var citySelect = document.getElementById('citySelect');
    var distSelect = document.getElementById('districtSelect');
    if (citySelect && distSelect) {
        citySelect.addEventListener('change', function() {
            var c = citySelect.value;
            if (!c) {
                // 시도 비우면 시군구도 전체로 리셋 + 비활성화
                distSelect.innerHTML = '<option value="">전체 시군구</option>';
                distSelect.disabled = true;
                return;
            }
            distSelect.disabled = false;

            // NFR-8: 기존 옵션 스냅샷 보관
            var prevHtml = distSelect.innerHTML;
            distSelect.classList.add('loading');

            fetch('/projects/api/dist-options?city=' + encodeURIComponent(c))
                .then(function(r){ return r.ok ? r.json() : Promise.reject(r.status); })
                .then(function(list) {
                    // 성공 시에만 옵션 교체
                    distSelect.innerHTML = '<option value="">전체 시군구</option>';
                    list.forEach(function(d) {
                        var opt = document.createElement('option');
                        opt.value = d; opt.textContent = d;
                        distSelect.appendChild(opt);
                    });
                    distSelect.classList.remove('loading');
                })
                .catch(function(err) {
                    // NFR-8 fallback: 기존 옵션 유지 + 에러 placeholder 추가
                    console.warn('dist-options 로드 실패', err);
                    distSelect.innerHTML = prevHtml;
                    var err = document.createElement('option');
                    err.textContent = '⚠ 로드 실패 — 새로고침 후 재시도';
                    err.disabled = true;
                    distSelect.insertBefore(err, distSelect.firstChild);
                    distSelect.classList.remove('loading');
                });
        });
    }
    
    // 3. 초기화 버튼 — 핀 OFF 필드만 리셋
    window.resetFilters = function() {
        document.getElementById('kwInput').value = '';
        FIELDS.forEach(function(f) {
            if (pins[f]) return;
            var sel = document.querySelector('[name="'+f+'"]');
            if (sel) sel.value = '';
        });
        // 즉시 submit 하지 않음 — 사용자 확인 후 검색 버튼
    };
})();
```

---

## 2. 작업 순서 (T1~T7)

| 단계 | 작업 | 예상 시간 |
|------|------|----------|
| **W1** | SwProjectRepository 3 메서드 추가 | 15분 |
| **W2** | SwService.search 오버로드 + 정규화 유틸 | 30분 |
| **W3** | SwController 파라미터 확장 + 모델 주입 + /api/dist-options | 20분 |
| **W4** | project-list.html HTML 교체 (힌트 제거, 드롭다운 추가) | 30분 |
| **W5** | project-list.html CSS + JS 추가 | 30분 |
| **W6** | 서버 재기동 + 브라우저 수동 확인 (T1~T3) | 10분 |
| **W7** | codex 에게 T1~T21 검증 위임 | 자동 |

**롤백 전략 (명확화)**:
- 로컬 개발 중: W1~W5 각 단계별 임시 커밋 → 오류 시 해당 단계 revert
- **최종 배포 커밋은 1개** (사용자 "작업완료" 시점에 squash 없이 단일 커밋 작성)
- **배포 후 롤백 단위**: `git revert <최종 커밋 SHA>` (단일 SHA = 단일 revert)
- **태그**: 최종 커밋에 `design-search-v1` 태그 부여 → 빠른 참조용
- Feature flag 미도입 (구현 규모 작고 DB 변경 없음 — 완전 revert 가능)

---

## 3. 회귀 테스트 체크 (빌드 후)

1. **컴파일**: `./mvnw compile` 실패 없어야 함
2. **서버 부팅**: `bash server-restart.sh` 성공
3. **로그 에러**: `logs/swmanager-error.log` 에 Thymeleaf exception 없음
4. **기존 URL**: `/projects/status?kw=UPIS` 정상 동작 (T8)
5. **신규 URL**: `/projects/status?year=2026&city=경기도` 정상 (T6)

---

## 4. 변경 최소화 원칙

- 기존 `search(String kw, Pageable)` 시그니처 **제거하지 않음** — 혹시 다른 호출처 있을 경우 대비 (grep 확인 후 제거 고려)
- `/projects/api/districts` (기존) **건드리지 않음**
- `SwProject` 엔티티 **변경 없음**
- DB 스키마 **변경 없음**
- 인덱스 **추가 없음** (기획서 4-4 근거)

---

## 5. 리스크 재확인

| ID | 리스크 | 완화 |
|----|-------|------|
| R1 | 기존 search(kw) 호출처 | grep → 레거시 오버로드 유지 (search(String,Pageable) → 새 search로 위임) |
| R2 | LocalStorage quota 초과 | 4개 boolean + 4개 문자열 = < 200B. 무시 가능 |
| R3 | AJAX 실패 시 시군구 필터 불가 | 기존 옵션 보존 + 에러 placeholder 상단 삽입 (NFR-8) |
| R4 | 핀 켜진 값이 DB에 없어짐 | select 값 빈값으로 렌더 → WHERE 0 행 (자연스러움) |
| **R5** | **`year` 바인딩 400 위험** | Controller `year`를 **String**으로 받아 Service의 `parseYearSafe` 로 무효값 무시 (기존 `Integer`로 받으면 `?year=abc` 400 발생) |
| **R6** | **AJAX 경로 변경 영향** | 신규 `/projects/api/dist-options` 추가일 뿐 기존 `/projects/api/districts` 유지. 다른 화면/프론트 연계 영향 없음. grep 확인 필요 (dev Step W3) |
| **R7** | **AJAX 실패 시 UI가 멈추는 회귀** | 기존 옵션 보존 fallback + 에러 placeholder로 사용자가 즉시 인지 가능. 다른 필터는 독립 동작 |

---

## 6. 커밋 메시지 (계획)

```
feat(projects): 검색 기능 개편 - 드롭다운 필터 + 핀 기능

- `&` AND 토큰 방식 제거
- 년도/시도/시군구/시스템(EN) 드롭다운 4종 추가
- 시군구는 시도 종속 (AJAX /api/dist-options)
- 각 필터 📌 핀 기능 (LocalStorage 지속)
- 초기화 시 핀 켜진 필드는 유지
- 힌트 태그 바 제거, 관련 CSS/JS 정리

기획서: docs/plans/project-search-refactor.md
개발계획: docs/dev-plans/project-search-refactor.md
```

---

## 7. 테스트 시나리오 매핑 (기획서 T1~T21)

- **수동 테스트 (개발 중)**: T1, T2, T3, T8 — 로컬 브라우저에서
- **자동화 테스트 (신규, 아래 8절)**: 서비스/컨트롤러 단위 + JS fallback
- **codex 검증**: 남은 전 항목
- **회귀 테스트**: 기존 `/projects/status?kw=UPIS` 진입 정상 → T8 통과

---

## 8. 자동화 테스트 세트 (신규)

### 8-1. SwServiceTest (단위)
`src/test/java/com/swmanager/system/service/SwServiceSearchTest.java` 신규

```java
@SpringBootTest
class SwServiceSearchTest {
    @Autowired SwService swService;

    @Test void kwOnly_returnsResults() { /* T1 */ }
    @Test void yearInvalidString_ignored() {
        // NFR-7: parseYearSafe("abc") == null
        Page<SwProject> p = swService.search(null, "abc", null, null, null, PageRequest.of(0,10));
        assertThat(p).isNotNull();  // 400 안 나고 정상 응답
    }
    @Test void yearOutOfRange_ignored() {
        Page<SwProject> p = swService.search(null, "1800", null, null, null, PageRequest.of(0,10));
        assertThat(p).isNotNull();
    }
    @Test void districtWithoutCity_ignored() {
        // FR-10: city 없으면 district silent ignore
        Page<SwProject> p1 = swService.search(null, null, null, "광명시", null, PageRequest.of(0,10));
        Page<SwProject> p2 = swService.search(null, null, null, null, null, PageRequest.of(0,10));
        assertThat(p1.getTotalElements()).isEqualTo(p2.getTotalElements());
    }
    @Test void fourFilterAnd_works() {
        // T3
        Page<SwProject> p = swService.search(null, "2026", "경기도", null, "UPIS", PageRequest.of(0,10));
        assertThat(p).isNotNull();
    }
    @Test void legacyAndToken_asLiteral() {
        // T9: 구 토큰 "2024&UPIS" → 그냥 문자열 검색 (아마 0건)
        Page<SwProject> p = swService.search("2024&UPIS", null, null, null, null, PageRequest.of(0,10));
        assertThat(p).isNotNull();
    }
}
```

### 8-2. SwController MockMvc 테스트
`SwControllerSearchTest.java` 신규

```java
@WebMvcTest(SwController.class)
class SwControllerSearchTest {
    @Autowired MockMvc mvc;
    @MockBean SwService swService;

    @Test void yearInvalid_returns200() throws Exception {
        // NFR-7: year="abc" 가 400 아님
        mvc.perform(get("/projects/status").param("year","abc"))
           .andExpect(status().isOk());
    }
    @Test void distOptionsApi_emptyCity_returnsEmpty() throws Exception {
        mvc.perform(get("/projects/api/dist-options").param("city",""))
           .andExpect(status().isOk())
           .andExpect(content().json("[]"));
    }
}
```

### 8-3. JS Fallback 수동 검증
- DevTools 로 `/projects/api/dist-options` 응답을 5xx 로 강제 → 기존 옵션 유지 + 경고 placeholder 확인 (T17)
- 자동화 어렵 → codex 검증에 위임

---

## 8. 릴리스 체크리스트

- [ ] codex 개발계획 검토
- [ ] 사용자 최종승인
- [ ] W1~W5 구현
- [ ] W6 수동 확인
- [ ] W7 codex T1~T21 검증
- [ ] 사용자 확인
- [ ] git commit + push
