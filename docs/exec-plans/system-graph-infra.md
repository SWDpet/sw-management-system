# [개발계획서] 시스템 관계도 — A. 인프라 구성도

- **작성팀**: 개발팀
- **작성일**: 2026-04-18
- **기획서**: [docs/product-specs/system-graph-infra.md](../product-specs/system-graph-infra.md) (v2 승인됨)
- **선행 커밋**: `a0941cd` (C 한글 설명)
- **상태**: v2 (codex 재검토 대기) — 1차 검토 3건 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 |
| v2 | codex 검토 반영: ①Node DTO 에 `domain` 필드 추가(FR-2 계약 준수) + 하위 레벨이 상위 infra_type 상속 ②T8 grep 에 camelCase 변형(`accPw`/`swAccPw`/`accId`/`swAccId`/`macAddr`) 추가 ③T13/T14 신설 — NFR-1(500ms) / NFR-2(200KB) 수치 검증 |

---

## 1. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Backend (신규) | `dto/InfraGraphDTO.java` | nodes/edges 응답 DTO + 3개 Projection record(InfraMasterView, InfraServerView, InfraSoftwareView) |
| Backend (신규) | `service/InfraGraphService.java` | 3개 projection 조회 + 계층 조립 + 민감 필드 배제 |
| Backend (신규) | `repository/InfraServerRepository.java` | InfraServer projection 조회 |
| Backend (신규) | `repository/InfraSoftwareRepository.java` | InfraSoftware projection 조회 |
| Backend (수정) | `repository/InfraRepository.java` | InfraMaster projection 조회 메서드 1개 추가 |
| Backend (수정) | `controller/SystemGraphController.java` | `GET /admin/system-graph/api/infra` 엔드포인트 |
| Frontend (수정) | `templates/admin-system-graph.html` | A 탭 본문 교체 — 필터/상세 패널/네트워크 로더 |
| DB | **없음** | — |
| Entity | **불변** | 기존 Infra/InfraServer/InfraSoftware 미수정 |

**신규 4개 + 수정 3개. DB/Entity 변경 없음.**

---

## 2. 파일별 상세 설계

### 2-1. `InfraGraphDTO.java` (신규)

```java
package com.swmanager.system.dto;

import java.util.List;
import java.util.Map;

public final class InfraGraphDTO {
    public record Node(String id, String label, String level, String domain, Map<String,Object> attrs) {}
    public record Edge(String from, String to) {}

    // Projection records (허용 필드만 — 민감 필드 아예 포함 안 함)
    public record InfraMasterView(Long infraId, String infraType, String cityNm, String distNm, String sysNm, String sysNmEn) {}
    public record InfraServerView(Long serverId, Long infraId, String serverType, String ipAddr, String osNm, String serverModel, String serialNo, String cpuSpec) {}
    public record InfraSoftwareView(Long swId, Long serverId, String swCategory, String swNm, String swVer, String port, String installPath, String sid) {}

    private final List<Node> nodes;
    private final List<Edge> edges;
    public InfraGraphDTO(List<Node> nodes, List<Edge> edges) { this.nodes = nodes; this.edges = edges; }
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}
```

- `level`: `"city" | "infra" | "server" | "software"` 중 하나
- `attrs`: 상세 패널용 raw 필드(영문 키) — 프론트에서 한글 라벨로 매핑
- **민감 필드 미포함**: Server 에서 `accId/accPw/macAddr` 제외, Software 에서 `swAccId/swAccPw` 제외

### 2-2. `InfraRepository.java` (수정 — projection 메서드 추가)

```java
@Query("SELECT new com.swmanager.system.dto.InfraGraphDTO$InfraMasterView(" +
       "  i.infraId, i.infraType, i.cityNm, i.distNm, i.sysNm, i.sysNmEn) " +
       "FROM Infra i")
List<InfraGraphDTO.InfraMasterView> fetchAllMasterViews();
```

### 2-3. `InfraServerRepository.java` (신규)

```java
package com.swmanager.system.repository;

import com.swmanager.system.domain.InfraServer;
import com.swmanager.system.dto.InfraGraphDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InfraServerRepository extends JpaRepository<InfraServer, Long> {

    @Query("SELECT new com.swmanager.system.dto.InfraGraphDTO$InfraServerView(" +
           "  s.serverId, s.infra.infraId, s.serverType, s.ipAddr, s.osNm, " +
           "  s.serverModel, s.serialNo, s.cpuSpec) " +
           "FROM InfraServer s")
    List<InfraGraphDTO.InfraServerView> fetchAllServerViews();
}
```

### 2-4. `InfraSoftwareRepository.java` (신규)

```java
package com.swmanager.system.repository;

import com.swmanager.system.domain.InfraSoftware;
import com.swmanager.system.dto.InfraGraphDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InfraSoftwareRepository extends JpaRepository<InfraSoftware, Long> {

    @Query("SELECT new com.swmanager.system.dto.InfraGraphDTO$InfraSoftwareView(" +
           "  w.swId, w.server.serverId, w.swCategory, w.swNm, w.swVer, " +
           "  w.port, w.installPath, w.sid) " +
           "FROM InfraSoftware w")
    List<InfraGraphDTO.InfraSoftwareView> fetchAllSoftwareViews();
}
```

### 2-5. `InfraGraphService.java` (신규)

```java
package com.swmanager.system.service;

import com.swmanager.system.dto.InfraGraphDTO;
import com.swmanager.system.dto.InfraGraphDTO.*;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.InfraServerRepository;
import com.swmanager.system.repository.InfraSoftwareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfraGraphService {

    private final InfraRepository infraRepository;
    private final InfraServerRepository serverRepository;
    private final InfraSoftwareRepository softwareRepository;

    public InfraGraphDTO getGraph() {
        // Specs NFR-4: 단일 요청당 SQL 3개 (master/server/software)
        List<InfraMasterView> masters = infraRepository.fetchAllMasterViews();
        List<InfraServerView> servers = serverRepository.fetchAllServerViews();
        List<InfraSoftwareView> softwares = softwareRepository.fetchAllSoftwareViews();

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        // 도메인 상속을 위한 룩업 맵 사전 구성
        Map<Long, String> domainByInfra = new HashMap<>();   // infraId → infra_type
        for (InfraMasterView m : masters) {
            domainByInfra.put(m.infraId(), safe(m.infraType()));
        }
        Map<Long, String> domainByServer = new HashMap<>();  // serverId → 상위 infra_type
        for (InfraServerView s : servers) {
            domainByServer.put(s.serverId(), domainByInfra.getOrDefault(s.infraId(), ""));
        }

        // 지자체(city|dist) 노드 — 중복 제거 (domain=null: 지자체는 타입 필터 대상 아님)
        Set<String> cityIds = new HashSet<>();
        for (InfraMasterView m : masters) {
            String cityKey = safe(m.cityNm()) + "|" + safe(m.distNm());
            String cityId = "city:" + cityKey;
            if (cityIds.add(cityKey)) {
                long cnt = masters.stream()
                        .filter(x -> Objects.equals(x.cityNm(), m.cityNm()) && Objects.equals(x.distNm(), m.distNm()))
                        .count();
                nodes.add(new Node(cityId, safe(m.cityNm()) + " " + safe(m.distNm()), "city", null,
                        Map.of("city_nm", safe(m.cityNm()),
                               "dist_nm", safe(m.distNm()),
                               "sys_count", cnt)));
            }
        }

        // 시스템 노드 (domain=infra_type)
        Map<Long, Long> serverCountByInfra = new HashMap<>();
        for (InfraServerView s : servers)
            serverCountByInfra.merge(s.infraId(), 1L, Long::sum);

        for (InfraMasterView m : masters) {
            String infraId = "infra:" + m.infraId();
            String cityId = "city:" + safe(m.cityNm()) + "|" + safe(m.distNm());
            Map<String,Object> attrs = new LinkedHashMap<>();
            attrs.put("sys_nm", safe(m.sysNm()));
            attrs.put("sys_nm_en", safe(m.sysNmEn()));
            attrs.put("infra_type", safe(m.infraType()));
            attrs.put("city_nm", safe(m.cityNm()));
            attrs.put("dist_nm", safe(m.distNm()));
            attrs.put("server_count", serverCountByInfra.getOrDefault(m.infraId(), 0L));
            nodes.add(new Node(infraId, safe(m.sysNm()), "infra", safe(m.infraType()), attrs));
            edges.add(new Edge(cityId, infraId));
        }

        // 서버 노드 (domain=상위 infra_type 상속)
        Map<Long, Long> swCountByServer = new HashMap<>();
        for (InfraSoftwareView w : softwares)
            swCountByServer.merge(w.serverId(), 1L, Long::sum);

        for (InfraServerView s : servers) {
            String srvId = "srv:" + s.serverId();
            String infraId = "infra:" + s.infraId();
            String label = safe(s.serverType()) + (s.ipAddr() != null ? " (" + s.ipAddr() + ")" : "");
            Map<String,Object> attrs = new LinkedHashMap<>();
            attrs.put("server_type", safe(s.serverType()));
            attrs.put("ip_addr", safe(s.ipAddr()));
            attrs.put("os_nm", safe(s.osNm()));
            attrs.put("server_model", safe(s.serverModel()));
            attrs.put("serial_no", safe(s.serialNo()));
            attrs.put("cpu_spec", safe(s.cpuSpec()));
            attrs.put("sw_count", swCountByServer.getOrDefault(s.serverId(), 0L));
            nodes.add(new Node(srvId, label, "server",
                    domainByInfra.getOrDefault(s.infraId(), ""), attrs));
            edges.add(new Edge(infraId, srvId));
        }

        // SW 노드 (domain=조상 infra_type 상속: server → infra)
        for (InfraSoftwareView w : softwares) {
            String swId = "sw:" + w.swId();
            String srvId = "srv:" + w.serverId();
            String label = safe(w.swNm()) + (w.swVer() != null ? " " + w.swVer() : "");
            Map<String,Object> attrs = new LinkedHashMap<>();
            attrs.put("sw_category", safe(w.swCategory()));
            attrs.put("sw_nm", safe(w.swNm()));
            attrs.put("sw_ver", safe(w.swVer()));
            attrs.put("port", safe(w.port()));
            attrs.put("install_path", safe(w.installPath()));
            attrs.put("sid", safe(w.sid()));
            nodes.add(new Node(swId, label, "software",
                    domainByServer.getOrDefault(w.serverId(), ""), attrs));
            edges.add(new Edge(srvId, swId));
        }

        log.info("InfraGraph: city={}, infra={}, server={}, sw={}",
                cityIds.size(), masters.size(), servers.size(), softwares.size());
        return new InfraGraphDTO(nodes, edges);
    }

    private String safe(String s) { return s == null ? "" : s; }
}
```

### 2-6. `SystemGraphController.java` (수정)

```java
// 필드 추가
private final InfraGraphService infraGraphService;

// 메서드 추가
@GetMapping("/api/infra")
@ResponseBody
public InfraGraphDTO getInfraGraph() {
    return infraGraphService.getGraph();
}
```

### 2-7. `admin-system-graph.html` (수정) — A 탭 본문

#### HTML 구조 (기존 "Phase 1 예정" 대체)
```html
<div id="tab-a" class="tab-panel active">
    <div class="panel-title">A. 인프라 구성도</div>
    <div class="panel-desc">지자체 → 시스템 → 서버 → 소프트웨어 — 실시간 DB 조회</div>

    <div class="erd-toolbar">
        <select id="infra-city" onchange="onInfraCityChange()"><option value="">시·도 전체</option></select>
        <select id="infra-dist" onchange="onInfraFilter()"><option value="">시·군·구 전체</option></select>
        <label><span class="dot" style="background:#4e73df"></span><input type="checkbox" class="infra-type-filter" data-type="UPIS" checked onchange="onInfraFilter()"> UPIS</label>
        <label><span class="dot" style="background:#f6c23e"></span><input type="checkbox" class="infra-type-filter" data-type="KRAS" checked onchange="onInfraFilter()"> KRAS</label>
        <label><span class="dot" style="background:#858796"></span><input type="checkbox" class="infra-type-filter" data-type="OTHER" checked onchange="onInfraFilter()"> 기타</label>
        <button onclick="loadInfraGraph()" style="padding:6px 12px;background:#1a237e;color:#fff;border:none;border-radius:4px;cursor:pointer;">새로고침</button>
    </div>

    <div class="erd-split">
        <div id="graph-a" class="graph-canvas"></div>
        <div id="infra-detail-panel" class="erd-detail">
            <div style="color:#999;font-size:0.9em;">← 노드를 클릭하면 정보가 여기에 표시됩니다.</div>
        </div>
    </div>
    <div id="infra-empty-msg" class="erd-empty-msg"></div>
</div>
```

(참고: 기존 A/B/D 가 placeholder 였으니 A 만 위처럼 교체. B/D 는 그대로.)

#### JS 로직 (요지)

```javascript
var INFRA_LEVEL_COLOR = {
    city:     { background: '#1a237e', border: '#0d124d', font: '#fff' },
    infra:    { background: '#4e73df', border: '#2e59d9', font: '#fff' },
    server:   { background: '#1cc88a', border: '#17a673', font: '#fff' },
    software: { background: '#f6c23e', border: '#dda20a', font: '#333' }
};

var INFRA_ATTR_LABELS = {
    city_nm: '시·도', dist_nm: '시·군·구', sys_count: '시스템 개수',
    sys_nm: '시스템명', sys_nm_en: '시스템명(영문)', infra_type: '인프라 유형',
    server_count: '서버 개수',
    server_type: '서버 유형', ip_addr: 'IP 주소', os_nm: '운영체제',
    server_model: '서버 모델', serial_no: '시리얼', cpu_spec: 'CPU 사양',
    sw_count: '소프트웨어 개수',
    sw_category: 'SW 분류', sw_nm: 'SW 이름', sw_ver: 'SW 버전',
    port: '포트', install_path: '설치 경로', sid: 'SID'
};

var infraData = null;
var infraNodeDS = null;
var infraEdgeDS = null;

function loadInfraGraph() {
    var container = document.getElementById('graph-a');
    var empty = document.getElementById('infra-empty-msg');
    empty.classList.remove('show');
    container.innerHTML = '<div style="padding:40px;text-align:center;color:#888;">로드 중...</div>';

    fetch('/admin/system-graph/api/infra', { credentials: 'same-origin' })
        .then(function(r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
        .then(function(data) {
            if (!data || !data.nodes || data.nodes.length === 0) {
                container.innerHTML = '';
                empty.textContent = '등록된 인프라가 없습니다.';
                empty.classList.add('show');
                return;
            }
            infraData = data;
            populateInfraCityOptions(data);
            renderInfraGraph(data);
        })
        .catch(function(err) {
            console.error('Infra 로드 실패', err);
            container.innerHTML = '';
            empty.textContent = '인프라 데이터를 불러오지 못했습니다. server.log 확인 필요';
            empty.classList.add('show');
        });
}

// 시·도 옵션 populate → onInfraCityChange 에서 시·군·구 종속 populate → onInfraFilter 에서 hidden 토글
// 상세 패널: INFRA_ATTR_LABELS 기반 한글 라벨 + escapeHtml 필수
// 호버 title 도 escape
// FR-10: stabilize 후 physics off
```

(세부 함수는 구현 단계에서 C 탭 loadErdGraph/renderErdGraph/showNodeDetail 패턴 재사용)

---

## 3. 작업 순서

| Step | 작업 | 검증 |
|------|------|------|
| 1 | DTO + 3 projection records | 컴파일 |
| 2 | Repository 2개 신규 + InfraRepository 메서드 추가 | 컴파일 |
| 3 | InfraGraphService 구현 | 컴파일 |
| 4 | SystemGraphController 엔드포인트 | 컴파일 |
| 5 | `./mvnw.cmd clean compile` | BUILD SUCCESS |
| 6 | 서버 재시작 → 비로그인 시 302 확인(NFR-5), 로그인 후 API 200 | 정상 |
| 7 | **SQL 로그 검증** (T9): 서버 로그에서 `/api/infra` 처리 중 실행된 SELECT 개수 = 3, 조회 컬럼에 민감 필드 없음 | PASS |
| 8 | A 탭 HTML/JS 구현 | 브라우저 |
| 9 | 회귀 테스트 T1~T14 (T8/T9/T11/T13/T14 필수) | 모두 PASS |
| 10 | codex Specs 중심 검증 | ✅ |
| 11 | 사용자 "작업완료" 후 commit + push | master 반영 |

---

## 4. 회귀 테스트

| # | 시나리오 | 기대 결과 | Specs |
|---|---------|-----------|-------|
| T1 | A 탭 진입 | 지자체·시스템·서버·SW 노드가 계층 색상으로 표시 | FR-1, FR-6 |
| T2 | 인프라 0건 상태에서 진입 | "등록된 인프라가 없습니다" 안내 | FR-12 |
| T3 | 시스템 노드 클릭 | 상세 패널에 시스템명/영문명/유형/서버 개수 (한글 라벨) | FR-8 |
| T4 | 서버 노드 클릭 | 상세 패널에 서버 유형/IP/OS/모델/시리얼/CPU (한글 라벨). **비밀번호·MAC 없음** | FR-3, FR-8 |
| T5 | SW 노드 클릭 | SW 분류/이름/버전/포트/설치 경로/SID. **비밀번호 없음** | FR-3, FR-8 |
| T6 | 시·도 드롭다운 "서울특별시" 선택 | 시·군·구 드롭다운이 서울 하위만 표시, 그래프는 서울 하위만 노출 | FR-7 |
| T7 | 유형 필터 "KRAS" 만 체크 해제 | KRAS 시스템과 그 하위 서버/SW 모두 숨김 | FR-7 |
| T8 | curl `/admin/system-graph/api/infra` (ADMIN) grep 검증 | 응답 바디에 민감 필드 문자열 **출현 0회**. 검사 대상: **snake_case** (`acc_pw`, `sw_acc_pw`, `acc_id`, `sw_acc_id`, `mac_addr`) + **camelCase** (`accPw`, `swAccPw`, `accId`, `swAccId`, `macAddr`). Jackson 기본 직렬화는 camelCase 라 두 형식 모두 검사해야 함 | FR-3, NFR-3 |
| T9 | **SQL 로그 검증** — 서버 로그에서 `/api/infra` 1회 처리 중 실행된 SELECT 쿼리 확인 | `tb_infra_master`/`tb_infra_server`/`tb_infra_software` 대상 SELECT **3개 이하**. 각 SELECT 에 **민감 컬럼(`acc_pw`/`sw_acc_pw`/`acc_id`/`sw_acc_id`/`mac_addr`) 없음** | FR-3, NFR-3, NFR-4 |
| T10 | 비관리자 계정으로 `/api/infra` 호출 | 302 (로그인 리다이렉트) 또는 403 | FR-1, NFR-5 |
| T11 | XSS: 어느 인프라 `sys_nm` 을 `<script>alert(1)</script>` 로 DB 수정 → 재진입 | 문자 그대로 표시, alert 실행 없음 | FR-9 |
| T12 | stabilize 후 1~2초 내 그래프 정지 | 물리 시뮬 off, 노드 위치 고정 | FR-10 |
| T13 | **응답 시간 측정** (인증된 요청) — 브라우저 로그인 후 DevTools 에서 `JSESSIONID` 쿠키값 복사, 다음 명령 실행:<br>`curl -w "\n[time_total=%{time_total}s http=%{http_code}]\n" -o /dev/null -s -b "JSESSIONID=<복사한값>" http://localhost:9090/admin/system-graph/api/infra` | **http=200** 이면서 `time_total` **≤ 0.5s** (NFR-1, 인프라 100개 기준). 302 나오면 쿠키 누락이므로 재실행. 초과 시 projection 튜닝 / 인덱스 검토 | NFR-1 |
| T14 | **응답 크기 측정** (인증된 요청) — 동일 쿠키 사용:<br>`curl -s -b "JSESSIONID=<복사한값>" http://localhost:9090/admin/system-graph/api/infra -o /tmp/infra.json && wc -c /tmp/infra.json` | 바디 크기 **≤ 204800 bytes (200KB)** (NFR-2). 302 응답(짧은 body) 이면 쿠키 재확인. 초과 시 속성 축소 또는 Phase 2 분할 로딩 검토 | NFR-2 |

---

## 5. 롤백 전략

### 원자적 롤백
```bash
git revert <sha>
git push
bash server-restart.sh
```

### 롤백 검증 체크포인트
1. BUILD SUCCESS
2. `Started SwManagerApplication`
3. `/admin/system-graph/api/infra` → **HTTP 404** (엔드포인트 제거 확인)
4. A 탭 → Phase 0 Placeholder (준비 중) 복귀
5. C 탭 영향 없음 (여전히 정상 동작)
6. server.log 에 신규 ERROR 없음

---

## 6. 빌드 · 재시작

- Java 신규/수정 → **빌드 필요**
- 서버 재시작 필요

---

## 7. 예상 소요

| 작업 | 시간 |
|------|------|
| DTO + Projection | 10 분 |
| Repository 3곳 | 10 분 |
| Service | 20 분 |
| Controller | 5 분 |
| HTML/JS | 30 분 |
| 빌드 + 재시작 | 5 분 |
| 테스트 T1~T14 (SQL 로그/XSS/응답시간/크기 포함) | 25 분 |
| codex 검증 | 5 분 |
| **합계** | **~105 분** |

---

## 8. 체크리스트

- [ ] `InfraGraphDTO.java` — Node/Edge + 3 projection records
- [ ] `InfraRepository.java` — `fetchAllMasterViews()`
- [ ] `InfraServerRepository.java` (신규)
- [ ] `InfraSoftwareRepository.java` (신규)
- [ ] `InfraGraphService.java` (신규)
- [ ] `SystemGraphController.java` — `/api/infra` 엔드포인트
- [ ] `admin-system-graph.html` — A 탭 본문/필터/상세/JS
- [ ] `./mvnw.cmd clean compile` BUILD SUCCESS
- [ ] 서버 재시작 성공
- [ ] 회귀 테스트 T1~T14 전체 통과 (**T8 API grep + T9 SQL 로그 + T11 XSS + T13 응답시간 + T14 응답크기 필수**)
- [ ] codex Specs 중심 검증 ✅

---

## 9. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
