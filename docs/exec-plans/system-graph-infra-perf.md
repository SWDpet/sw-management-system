# [개발계획서] 인프라 구성도 — 성능 개선 + 필터 UX 개선

- **작성팀**: 개발팀
- **작성일**: 2026-04-18
- **기획서**: [docs/product-specs/system-graph-infra-perf.md](../product-specs/system-graph-infra-perf.md) (v2 승인됨)
- **상태**: v2 (codex 재검토 대기) — 1차 검토 2건 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 |
| v2 | codex 검토 반영: ①R7 신설 — FR-8 API 응답 구조 회귀 검증 ②S1 실행 가능한 완전 curl 명령으로 개정 (JSESSIONID 쿠키 + 전체 URL) |

---

## 1. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Frontend (수정) | `src/main/resources/templates/admin-system-graph.html` | 필터 바 HTML + JS(`renderInfraGraph`/`onInfraFilter`/신규 `updateInfraCount`) + CSS 소량 |
| Backend | **없음** | API/DTO/Service 모두 불변 |
| DB | **없음** | — |

**수정 1개 파일. 백엔드/DB 전혀 변경 없음.**

---

## 2. 변경 상세 설계

### 2-1. 필터 바 HTML (교체)

**Before**:
```html
<label><span class="dot" ...></span><input type="checkbox" class="infra-type-filter" data-type="UPIS" checked ...> UPIS</label>
<label><span class="dot" ...></span><input type="checkbox" class="infra-type-filter" data-type="KRAS" checked ...> KRAS</label>
<label><span class="dot" ...></span><input type="checkbox" class="infra-type-filter" data-type="OTHER" checked ...> 기타</label>
<button onclick="loadInfraGraph()" ...>새로고침</button>
```

**After**:
```html
<select id="infra-type" onchange="onInfraFilter()" style="...">
    <option value="">유형: 전체</option>
    <option value="UPIS">UPIS</option>
    <option value="KRAS">KRAS</option>
    <option value="OTHER">기타</option>
</select>
<label style="..."><input type="checkbox" id="infra-show-sw" onchange="onInfraFilter()"> 소프트웨어 표시</label>
<span id="infra-count" style="margin-left:auto;color:#555;font-size:0.85em;">로드 중...</span>
<button onclick="loadInfraGraph()" style="...">🔄 새로고침</button>
```

- `value=""` (전체) 가 기본 선택
- `id="infra-show-sw"` 체크박스 — 기본 unchecked (FR-3)
- `#infra-count` 영역 추가 — FR-6-COUNT 카운트 표시
- 기존 `.infra-type-filter` 셀렉터 제거 (onInfraFilter 내부에서도 삭제)

### 2-2. renderInfraGraph() — hierarchical layout + physics off (FR-1)

```javascript
var options = {
    nodes: { margin: 10, borderWidth: 1 },
    edges: { width: 1 },
    layout: {
        hierarchical: {
            enabled: true,
            direction: 'UD',          // Up → Down (지자체 상단, SW 하단)
            sortMethod: 'directed',   // 엣지 방향 기준 정렬
            levelSeparation: 140,     // 레벨 간 세로 간격
            nodeSpacing: 80,          // 동일 레벨 노드 간 가로 간격
            treeSpacing: 200,         // 독립 트리 간 간격
            shakeTowards: 'roots'
        }
    },
    physics: {
        enabled: false                // 시뮬레이션 제거 — 즉시 렌더링
    },
    interaction: {
        hover: true,
        tooltipDelay: 150,
        dragNodes: true,              // 기획서 7-4: 사용자 재배치 허용
        dragView: true,
        zoomView: true
    }
};
```

- `physics.enabled: false` 로 초기 stabilize iterations 건너뜀.
- `once('stabilizationIterationsDone', ...)` 콜백은 제거(필요 없음).

### 2-3. onInfraFilter() — 배치 업데이트 (FR-4)

기존: 노드 1,642개 각각에 `infraNodeDS.update({id, hidden})` 개별 호출.
변경: 배열로 취합해 1회 호출.

```javascript
window.onInfraFilter = function() {
    if (!infraData || !infraNodeDS || !infraEdgeDS) return;

    var city = document.getElementById('infra-city').value;
    var dist = document.getElementById('infra-dist').value;
    var typeVal = document.getElementById('infra-type').value;   // '' | 'UPIS' | 'KRAS' | 'OTHER'
    var showSw = document.getElementById('infra-show-sw').checked;

    function typeAllowed(domain) {
        if (!typeVal) return true;   // '' = 전체
        var t = (domain || '').toUpperCase();
        if (typeVal === 'UPIS')  return t === 'UPIS';
        if (typeVal === 'KRAS')  return t === 'KRAS';
        // OTHER: UPIS/KRAS 가 아닌 모든 것
        return t !== 'UPIS' && t !== 'KRAS';
    }

    // 부모 룩업 (child → parent)
    var parentOf = {};
    infraData.edges.forEach(function(e) { parentOf[e.to] = e.from; });

    var visible = {};

    // Pass 1: city
    infraData.nodes.forEach(function(n) {
        if (n.level === 'city') {
            var show = true;
            if (city && n.attrs && n.attrs.city_nm !== city) show = false;
            if (dist && n.attrs && n.attrs.dist_nm !== dist) show = false;
            visible[n.id] = show;
        }
    });
    // Pass 2: infra
    infraData.nodes.forEach(function(n) {
        if (n.level === 'infra') {
            var parent = parentOf[n.id];
            var parentVisible = parent ? !!visible[parent] : true;
            visible[n.id] = parentVisible
                && typeAllowed(n.domain)
                && (!city || (n.attrs && n.attrs.city_nm === city))
                && (!dist || (n.attrs && n.attrs.dist_nm === dist));
        }
    });
    // Pass 3: server
    infraData.nodes.forEach(function(n) {
        if (n.level === 'server') {
            var parent = parentOf[n.id];
            visible[n.id] = parent ? !!visible[parent] : false;
        }
    });
    // Pass 4: software — showSw 가 false 면 무조건 숨김 (FR-6)
    infraData.nodes.forEach(function(n) {
        if (n.level === 'software') {
            if (!showSw) { visible[n.id] = false; return; }
            var parent = parentOf[n.id];
            visible[n.id] = parent ? !!visible[parent] : false;
        }
    });

    // 배치 업데이트 — 노드/엣지 각각 1회 호출 (FR-4)
    var nodeUpdates = infraData.nodes.map(function(n) {
        return { id: n.id, hidden: !visible[n.id] };
    });
    var edgeUpdates = infraData.edges.map(function(e, idx) {
        return { id: 'ie' + idx, hidden: !(visible[e.from] && visible[e.to]) };
    });
    infraNodeDS.update(nodeUpdates);
    infraEdgeDS.update(edgeUpdates);

    // 카운트 표시 (FR-6-COUNT)
    updateInfraCount(visible, showSw);

    // 빈 상태 판정 (FR-6): 전 레벨 통틀어 visible=true 가 하나도 없을 때만
    var anyVisible = false;
    Object.keys(visible).forEach(function(k) { if (visible[k]) anyVisible = true; });
    var emptyMsg = document.getElementById('infra-empty-msg');
    if (!anyVisible) {
        emptyMsg.textContent = '필터 조건에 맞는 인프라가 없습니다.';
        emptyMsg.classList.add('show');
    } else {
        emptyMsg.classList.remove('show');
    }
};
```

### 2-4. updateInfraCount() — 레벨별 카운트 (신규, FR-6-COUNT)

```javascript
function updateInfraCount(visible, showSw) {
    var counts = { city: 0, infra: 0, server: 0, software: 0 };
    if (infraData) {
        infraData.nodes.forEach(function(n) {
            if (visible[n.id]) counts[n.level]++;
        });
    }
    var el = document.getElementById('infra-count');
    if (!el) return;
    el.textContent = '지자체 ' + counts.city
        + ' · 시스템 ' + counts.infra
        + ' · 서버 ' + counts.server
        + ' · SW ' + (showSw ? counts.software : '숨김');
}
```

### 2-5. loadInfraGraph() 수정점

- populateInfraCityOptions 호출 유지.
- 로드 성공 후 `document.getElementById('infra-show-sw').checked = false` 로 **새로고침 시 OFF 복귀** 명시적 설정 (FR-3).
- 마지막에 `onInfraFilter()` 호출하여 초기 필터 반영 (기본 상태가 SW 숨김이므로 초기 트리는 정상 표시).

### 2-6. CSS (선택)

`#infra-count` 는 인라인 스타일로 처리. 별도 CSS 없음.

### 2-7. 제거할 코드

- 기존 `.infra-type-filter` 셀렉터 모든 참조
- 초기화 후 모든 체크박스 true 기본값 로직 (해당 체크박스 자체가 제거됨)

---

## 3. 작업 순서

| Step | 작업 | 검증 |
|------|------|------|
| 1 | 필터 바 HTML 교체 (3 체크박스 → 1 드롭다운 + SW 체크 + 카운트) | 브라우저 렌더 |
| 2 | `renderInfraGraph` 에 hierarchical 옵션 + physics off | 진입 시 즉시 트리 배치 |
| 3 | `onInfraFilter` 배치 업데이트 + showSw 로직 | 필터 조작 반응 속도 |
| 4 | `updateInfraCount` 신규 함수 | 카운트 문구 정상 |
| 5 | `loadInfraGraph` 재호출 시 SW 체크박스 OFF 리셋 | 새로고침 후 OFF |
| 6 | 기존 `.infra-type-filter` 참조 제거 | 미참조 확인 (grep) |
| 7 | Thymeleaf 는 자바 변경 없음 → **빌드 불필요**, 서버 재시작만 | Started SwManagerApplication |
| 8 | 회귀 테스트 P1~P7 + 회귀 매트릭스 R1~R7 + 응답 크기 S1 | 모두 PASS |
| 9 | codex Specs 중심 검증 | ✅ |
| 10 | 사용자 "작업완료" → commit + push | master 반영 |

> 7 에 대한 보충: `admin-system-graph.html` 은 템플릿 리소스이므로 Java 컴파일 불필요. `bash server-restart.sh` 로 Spring Boot 재시작하면 충분. 개발 중 `spring.thymeleaf.cache=false` 면 즉시 반영되지만 이 프로젝트는 그 설정을 안 씀(확실치 않으므로 그냥 재시작).

---

## 4. 회귀 테스트

### 성능 (Perf 개선 검증) 

| # | 시나리오 | 기대 결과 | Specs |
|---|---------|-----------|-------|
| P1 | A 탭 최초 진입 → 그래프 배치 완료까지의 시간 | **≤ 1.5 s** (Chrome DevTools Performance 측정) | NFR-1 |
| P2 | 필터 조작 (시·도 변경) 후 그래프 갱신 시간 | **≤ 300 ms** | NFR-2 |
| P3 | SW 체크박스 ON 토글 후 갱신 시간 | **≤ 300 ms** | FR-4, NFR-2 |
| P4 | 새로고침 → SW 체크박스 상태 | **OFF** (초기화됨) | FR-3 |
| P5 | 기본 진입 시 카운트 문자열 | `지자체 N · 시스템 N · 서버 N · SW 숨김` | FR-6-COUNT |
| P6 | SW ON 후 카운트 | `... · SW 숫자` | FR-6-COUNT |
| P7 | 유형 드롭다운 "UPIS" 선택 | UPIS 시스템·그 하위 서버·(체크 시 SW) 만 표시, 타 유형 숨김 | FR-2, FR-5, FR-6 |

### 기능 (v2 호환성, FR-7 회귀 매트릭스) 

| # | 시나리오 | 기대 결과 | 근거 |
|---|---------|-----------|-----|
| R1 | 비-ADMIN 계정으로 `/api/infra` | HTTP 302 또는 403 | v2 NFR-5 |
| R2 | ADMIN curl `/api/infra` 응답 바디 grep | `acc_pw/sw_acc_pw/acc_id/sw_acc_id/mac_addr` + camelCase 변형 **0 hits** | v2 FR-3, NFR-3 |
| R3 | 유형 드롭다운 "KRAS" 선택 → 서버 노드 클릭 | 상세 패널 IP/OS 등 표시, 도메인=KRAS 상속 확인 | v2 FR-2 도메인 상속 |
| R4 | 서버 노드 클릭 | IP/OS/모델/시리얼/CPU 만, 비밀번호·MAC 없음 | v2 FR-3 |
| R5 | 시스템 노드 호버 | 툴팁 `시스템명\n유형` | v2 FR-8 |
| R6 | C 탭으로 이동 → 진입 | ERD 그래프 33 노드 정상 (A 개선이 C 에 영향 없음) | v2 C 탭 |
| R7 | ADMIN 쿠키 curl 로 `/api/infra` JSON 구조 검증:<br>`curl -s -b "JSESSIONID=<값>" http://localhost:9090/admin/system-graph/api/infra \| jq '.nodes[0], .edges[0]'` | `nodes[0]` 에 `id/label/level/domain/attrs` 키 **모두 존재**, `edges[0]` 에 `from/to` 키 존재 — 기획서 FR-8 (API 응답 구조 불변) | FR-8 |

### 응답 크기 실측 (NFR-3 기준선 갱신)

| # | 시나리오 | 기대/조치 |
|---|---------|----------|
| S1 | 브라우저 로그인 후 DevTools 에서 `JSESSIONID` 쿠키값 복사, 아래 실행:<br>`curl -s -b "JSESSIONID=<복사한값>" http://localhost:9090/admin/system-graph/api/infra -o /tmp/infra.json && wc -c /tmp/infra.json` | 측정된 바이트 수를 기록. 200KB 초과 시 기획서 NFR-3 에 **실측치(예: "XXX KB")** 로 업데이트. 단순 경고 처리 — 차단 아님. 302 (4~5 bytes) 나오면 쿠키 누락이므로 재실행. |

---

## 5. 롤백 전략

### 원자적 롤백
```bash
git revert <commit-sha>
git push
bash server-restart.sh
```

### 롤백 검증 체크포인트
1. 서버 기동 성공
2. A 탭 진입 — 기존(느린) force layout + 체크박스 3개 복귀
3. C 탭 영향 없음 — 여전히 정상
4. `/api/infra` — 여전히 200 (API 변경 없음)

---

## 6. 빌드 · 재시작

- Java 변경 **없음** → Maven compile 건너뛸 수 있음
- 서버 **재시작 필요** (Thymeleaf 캐시 설정 여부 무관하게 안전하게)

---

## 7. 예상 소요

| 작업 | 시간 |
|------|------|
| 필터 바 HTML | 5 분 |
| `renderInfraGraph` hierarchical 설정 | 5 분 |
| `onInfraFilter` batch + showSw | 15 분 |
| `updateInfraCount` | 5 분 |
| 기타 정리 (초기화 리셋, 기존 코드 제거) | 5 분 |
| 서버 재시작 | 2 분 |
| 성능 테스트 P1~P7 | 10 분 |
| 회귀 매트릭스 R1~R7 | 17 분 |
| 응답 크기 측정 S1 | 3 분 |
| codex 검증 | 5 분 |
| **합계** | **~70 분** |

---

## 8. 체크리스트

- [ ] 필터 바 HTML — 드롭다운 + SW 체크박스 + 카운트
- [ ] `renderInfraGraph` hierarchical + physics off
- [ ] `onInfraFilter` 배치 업데이트 + showSw 로직
- [ ] `updateInfraCount` 신규 함수
- [ ] `loadInfraGraph` 마지막에 SW 체크박스 OFF 리셋 + `onInfraFilter()` 1회 호출
- [ ] 기존 `.infra-type-filter` 참조 모두 제거
- [ ] `bash server-restart.sh` 기동 성공
- [ ] 성능 테스트 P1~P7 통과 (**P1 ≤1.5s 필수**)
- [ ] 회귀 매트릭스 R1~R7 통과 (**R2 민감 필드 + R7 API 구조 필수**)
- [ ] 응답 크기 실측 S1 기록
- [ ] codex Specs 중심 검증 ✅

---

## 9. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
