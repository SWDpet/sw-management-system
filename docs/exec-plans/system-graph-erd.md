# [개발계획서] 시스템 관계도 — C. ERD 인터랙티브

- **작성팀**: 개발팀
- **작성일**: 2026-04-18
- **기획서**: [docs/product-specs/system-graph-erd.md](../product-specs/system-graph-erd.md) (v2 승인됨)
- **상태**: v2 (codex 재검토 대기) — 1차 검토 5건 반영

### 개정 이력
| 버전 | 변경 내용 |
|------|-----------|
| v1 | 초안 |
| v2 | codex 검토 반영: ①FR-11/FR-12 테스트 추가(T15, T16) ②NFR-1/2/5 검증 케이스 추가(T17, T18, T19) ③파서 정규식 완화(가변 공백, 타입 토큰 확장) ④파일 로딩을 프로퍼티 + classpath fallback 로 보완 ⑤롤백 검증 체크포인트 추가 |

---

## 1. 영향 범위

| 계층 | 파일 | 변경 유형 |
|------|------|-----------|
| Backend (신규) | `src/main/java/com/swmanager/system/service/ErdGraphService.java` | .mmd 파서 + 병합 규칙 + 캐시 |
| Backend (신규) | `src/main/java/com/swmanager/system/dto/ErdGraphDTO.java` | nodes/edges/columns 응답 DTO (record 3종) |
| Backend (수정) | `src/main/java/com/swmanager/system/controller/SystemGraphController.java` | `GET /admin/system-graph/api/erd` 엔드포인트 추가 |
| Config (수정) | `src/main/resources/application.properties` | `app.erd.mmd-dir=docs` 1줄 추가 |
| Frontend (수정) | `src/main/resources/templates/admin-system-graph.html` | C 탭 본문 완성 — 레이아웃(그래프 + 상세패널), 검색/필터 UI, 데이터 페치, vis-network 옵션 |
| DB | **없음** | — |

**신규 2개 + 수정 2개. DB 변경 없음.**

---

## 2. 파일별 상세 설계

### 2-1. `ErdGraphDTO.java` (신규)

```java
package com.swmanager.system.dto;

import java.util.List;

public final class ErdGraphDTO {
    public record Column(String name, String type, boolean pk, boolean fk, String fkRef) {}
    public record Node(String id, String label, String domain, List<Column> columns) {}
    public record Edge(String from, String to, String label, String cardinality) {}

    private final List<Node> nodes;
    private final List<Edge> edges;

    public ErdGraphDTO(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes; this.edges = edges;
    }
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}
```

### 2-2. `ErdGraphService.java` (신규) — 핵심 로직

#### 책임
- 서버 기동 시 `docs/erd-*.mmd` 5개 파일 파싱 (application startup, `@PostConstruct`)
- FR-1-MERGE 병합 규칙 적용
- FR-5-A FK 매핑 휴리스틱 적용
- 결과를 in-memory 에 캐시. `getGraph()` 호출 시 캐시 즉시 반환.

#### 파싱 규칙 (경량 정규식 파서 — v2: 가변 공백·토큰 허용)
```
도메인 판정: 파일명 → erd-{domain}.mmd 에서 domain 추출

블록 시작: /^\s+(\w+)\s*\{\s*$/
블록 끝:   /^\s+\}\s*$/
컬럼:      /^\s+([A-Za-z][A-Za-z0-9_\[\](),]*)\s+(\w+)(\s+(PK|FK|UK|PK,FK|FK,PK))?\s*$/
  → group1 = type (예: bigint, varchar, int, jsonb, date, timestamp, numeric(10,2), varchar[] 등)
  → group2 = column name
  → group3 (있으면) = modifier
관계선:    /^\s+(\w+)\s+(\S+)\s+(\w+)\s*:\s*"([^"]*)"\s*$/
  → group1=from, group2=cardinality, group3=to, group4=label
```

- v1 대비 변경점:
  - `\s{4}`, `\s{8}` → 모두 `\s+` 로 완화 (탭/스페이스 혼용·칸 수 무관 수용)
  - type 토큰에 괄호·대괄호 허용 (`numeric(10,2)`, `varchar[]` 같은 변형 대비)
  - `FK,PK` 같은 modifier 순서 뒤집힘도 허용

#### 병합 규칙 (FR-1-MERGE)
```java
// 1) 각 파일에서 모든 테이블 블록 파싱 → List<RawTable>
// 2) Map<tableName, List<RawTable>> 그룹핑
// 3) 각 그룹에서 columns.size() 최대인 것 선택
//    동률일 경우 DOMAIN_PRIORITY = [core, infra, contract, document, quotation] 순서로 선택
// 4) 최종 Node 리스트 생성. domain = 선택된 RawTable 의 파일 도메인.
// 5) edges = 모든 파일의 관계선 합집합 (from+to+cardinality 키로 중복 제거)
```

#### FK 매핑 휴리스틱 (FR-5-A)
```java
// 관계선: from=A, to=B, cardinality=||--o{
// B 의 FK 컬럼 중, A 의 PK 컬럼명과 동일한 이름을 가진 것을 찾아 fkRef=A 설정.
// 예: tb_infra_master ||--o{ tb_infra_server
//   tb_infra_master PK = infra_id → tb_infra_server.infra_id (FK) 가 있으면 그 컬럼의 fkRef = "tb_infra_master"
// 매칭 실패 시 fkRef = null (FR-5 배지만 표시)
```

#### 파일 위치 (v2: 프로퍼티 + classpath fallback)

**전략**: 설정값 기반 우선 → 파일시스템 실패 시 classpath 로 fallback.

1. **설정 프로퍼티** (`application.properties`):
   ```
   app.erd.mmd-dir=docs   # 기본값. 배포 환경에서 override 가능
   ```
2. **조회 순서**:
   1. `${app.erd.mmd-dir}/erd-{domain}.mmd` (파일시스템, 상대/절대 모두 허용)
   2. 위 실패 시 `classpath:erd/erd-{domain}.mmd` (JAR 배포 대비)
   3. 둘 다 실패 시 경고 로그 + 해당 도메인 스킵
3. **Spring `ResourceLoader` 사용** — `FileSystemResource` 와 `ClassPathResource` 공통 처리.
4. `user.dir` 직접 사용 **금지** (배포 환경에서 cwd 보장 불가).

이 구조로:
- 개발 환경: 현재 그대로 `docs/erd-*.mmd` 에서 읽음 (기본 프로퍼티)
- JAR 배포 시: classpath 에 `erd/` 디렉토리를 포함시키거나, 운영 머신의 설정 디렉토리로 override
- 설정 override: `-Dapp.erd.mmd-dir=/opt/swmanager/erd` 같이 JVM 옵션으로 변경 가능

#### 실패 처리
- 파일 단위 try-catch. 한 파일 파싱 실패해도 나머지 4개는 정상 로드.
- 파싱 실패 로그: `log.warn("ERD 파일 파싱 실패: {}", filePath, ex)` 로 기록.
- 모든 파일 실패 시 빈 nodes/edges 반환. API 는 200 OK + 빈 JSON.

### 2-3. `SystemGraphController.java` (수정)

```java
// 기존
@GetMapping
public String view() { return "admin-system-graph"; }

// 신규 추가
@GetMapping("/api/erd")
@ResponseBody
public ErdGraphDTO getErdGraph() {
    return erdGraphService.getGraph();
}
```

- `@ResponseBody` 로 JSON 직렬화. Spring Boot 의 기본 Jackson 사용.
- `@PreAuthorize("hasRole('ADMIN')")` 클래스 레벨 어노테이션 상속.
- `ErdGraphService` 는 생성자 주입.

### 2-4. `admin-system-graph.html` (수정)

#### 구조 변경
- **C 탭 내부**를 좌우 2컬럼 레이아웃으로 분할
  - 좌측: `#graph-c` (vis-network canvas, 기존 유지)
  - 우측: `#erd-detail-panel` (선택된 테이블의 컬럼 목록 표시용, 신규)
- **C 탭 상단**: 검색 인풋(`#erd-search`) + 도메인 필터 체크박스 5개(`.erd-filter`)

#### JS 변경
- 기존 `drawPlaceholder('tab-c')` 호출 제거. C 탭 전용 **`loadErdGraph()`** 신설.
- `loadErdGraph()`:
  1. `fetch('/admin/system-graph/api/erd')`
  2. 응답을 vis.DataSet 으로 변환 (도메인별 색상 매핑은 상수 테이블)
  3. `vis.Network` 생성 — `options.edges.font.size=0` (기본 숨김), `interaction.hover=true` + `edges` 이벤트로 호버 시 레이블 표시
  4. 노드 클릭 이벤트 → 상세 패널 갱신
  5. 상세 패널 내 FK 컬럼 클릭 → `fkRef` 있으면 해당 노드 선택 + `network.focus()`
- **검색**: input 이벤트로 매칭 필터. 매칭된 노드는 opacity 1.0, 비매칭은 0.25 (vis-network 의 `nodes.update({id, opacity})` 사용).
- **도메인 필터**: 체크박스 이벤트로 hidden 속성 토글.

#### 색상 상수
```javascript
const DOMAIN_COLORS = {
    infra:     { bg: '#4e73df', border: '#2e59d9' },
    contract:  { bg: '#f6c23e', border: '#dda20a' },
    document:  { bg: '#1cc88a', border: '#17a673' },
    quotation: { bg: '#a14ed0', border: '#7b35a8' },
    core:      { bg: '#858796', border: '#60616f' }
};
```

---

## 3. 작업 순서 (단계별)

| Step | 작업 | 검증 수단 |
|------|------|-----------|
| 1 | `ErdGraphDTO.java` 생성 | 컴파일 성공 |
| 2 | `ErdGraphService.java` 생성 + 파서 + 병합 + 캐시 + FK 매핑 | 유닛 검증 시 5개 파일 모두 파싱, nodes 30+ 개 반환 |
| 3 | `SystemGraphController.java` 에 `/api/erd` 엔드포인트 추가 | 서버 재시작 후 curl 로 JSON 확인 |
| 4 | `admin-system-graph.html` C 탭 좌우 레이아웃 + 검색/필터 마크업 | 브라우저에서 레이아웃 정상 |
| 5 | `admin-system-graph.html` JS: 데이터 페치 + vis-network 렌더링 | 탭 진입 시 30+ 노드 그래프 표시 |
| 6 | JS: 노드 클릭 → 상세 패널, FK 클릭 → 포커스 이동 | 수동 클릭 테스트 |
| 7 | JS: 검색 + 도메인 필터 | 수동 테스트 |
| 8 | 빌드 (`./mvnw.cmd clean compile`) + 서버 재시작 | BUILD SUCCESS + Started SwManagerApplication |
| 9 | 수동 회귀 테스트 **T1~T20 전체** (FR-11/12, NFR-1/2/5 포함) | 모든 케이스 PASS |
| 10 | codex Specs 중심 검증 | ✅ 승인 |
| 11 | 사용자 "작업완료" 후 commit + push | master 반영 |

---

## 4. 회귀 테스트 항목

| # | 시나리오 | 기대 결과 | 연관 Specs |
|---|---------|-----------|-----------|
| T1 | `/admin/system-graph` 진입 → C 탭 클릭 | 그래프 로드 스피너 → 30+ 노드가 도메인 색상으로 배치 | FR-1, FR-3, FR-9 |
| T2 | 초기 노드 수 검증 | `users` 1개만 (erd-contract 스텁 무시 확인), `tb_infra_master` 1개만 (erd-contract 스텁 무시) | FR-1-MERGE |
| T3 | `users` 노드 클릭 → 상세 패널 | 8개 컬럼 표시 (erd-core 기준) — user_id/userid/username/password/orgNm/deptNm/userRole/position | FR-4, FR-1-MERGE |
| T4 | `tb_infra_master` 노드 클릭 → 상세 패널 | 6개 컬럼 (erd-infra 기준) | FR-4, FR-1-MERGE |
| T5 | `tb_infra_server` 상세에서 `infra_id` FK 클릭 | `tb_infra_master` 노드로 포커스 이동 + 선택 | FR-5 |
| T6 | `tb_document` 상세에서 `contract_id` FK 클릭 | 클릭 비활성 (툴팁: "관계선 미정의로 이동 불가") | FR-5, FR-5-A |
| T7 | 엣지 호버 | 관계 레이블 표시 (예: "has servers"), 비호버 시 숨김 | FR-8 |
| T8 | 검색창에 `infra` 입력 | `tb_infra_*` 노드 강조, 나머지 opacity 0.25 | FR-6 |
| T9 | 검색창 비우기 | 모든 노드 정상 불투명 | FR-6 |
| T10 | `Infra` 필터 체크 해제 | 인프라 도메인 노드·엣지 숨김 | FR-7 |
| T11 | 모든 도메인 필터 해제 | "표시할 테이블이 없습니다" 안내 | FR-7 |
| T12 | 마우스 휠 확대/축소, 드래그로 팬 | 정상 동작 | FR-10 |
| T13 | curl `GET /admin/system-graph/api/erd` (ADMIN 세션) | 200 OK, nodes/edges JSON 반환 | FR-2 |
| T14 | 비관리자 계정으로 `/admin/system-graph/api/erd` 호출 | 403 Forbidden | NFR-6 |
| T15 | C 탭 초기 진입 후 1~2초 내 노드 배치 완료 (그래프가 움직이지 않음) | 물리 시뮬레이션 `stabilize` 완료 후 정지, 사용자 조작 전까지 위치 고정 | FR-11 |
| T16 | `docs/` 디렉토리를 임시로 비우고 서버 재기동 → C 탭 진입 | 200 OK + 빈 `{nodes:[],edges:[]}`. 프론트에 "ERD 데이터를 불러오지 못했습니다. server.log 확인 필요" 안내 표시 | FR-12 |
| T17 | 서버 기동 로그에서 ERD 파싱 시간 확인 | 5개 파일 파싱 완료까지 1000ms 이내 (로그에 `ERD parsing: Xms` 출력) | NFR-1 |
| T18 | C 탭 진입 후 그래프 완전 렌더까지 브라우저 DevTools 로 측정 | 3000ms 이내 | NFR-2 |
| T19 | 브라우저 창 폭 768px~1024px 로 축소 | 좌(그래프)·우(상세 패널) 레이아웃 깨지지 않고 읽기 가능 | NFR-5 |
| T20 | (롤백 리허설) 현 커밋 기준 `git revert` → 재빌드 → 재시작 | ① `/admin/system-graph` 페이지 자체는 200 ② `/admin/system-graph/api/erd` 는 **404** (엔드포인트 제거됨) ③ C 탭은 Phase 0 상태(준비 중 노드) 복원 ④ 비관리자 여전히 403 | 롤백 전략 |

---

## 5. 롤백 전략

### 원자적 롤백
- 변경이 단일 커밋에 묶임. 문제 시:
  ```bash
  git revert <commit-sha>
  git push
  bash server-restart.sh
  ```

### 롤백 검증 체크포인트 (v2 추가)
롤백 후 다음을 순서대로 확인:

1. 빌드 성공: `./mvnw.cmd clean compile` → BUILD SUCCESS
2. 서버 기동 성공: `server-restart.sh` → `Started SwManagerApplication`
3. `/admin/system-graph` 페이지 렌더: HTTP 200, 4탭 UI 정상
4. `/admin/system-graph/api/erd` 호출: **HTTP 404** (엔드포인트 제거됨을 확인)
5. C 탭 렌더: Phase 0 상태(준비 중 더미 노드) 로 복원
6. 비관리자 접근: `/admin/system-graph` 가 여전히 403/401 반환
7. server.log 에 신규 ERROR 없음

위 7개 모두 PASS 면 롤백 성공. 하나라도 실패 시 추가 조치 필요.

### 데이터 롤백
- DB 변경 없음 → 불필요.

### 장애 감지
- 서버 기동 실패 → server-restart.sh 가 60초 타임아웃
- ERD API 500 에러 → server.log 확인
- 프론트 렌더링 실패 → 브라우저 콘솔 확인, 빈 그래프로 폴백

---

## 6. 빌드 · 재시작 영향

- Java 신규·수정 파일 있음 → **빌드 필요**
- Thymeleaf 템플릿 수정 → 개발 환경에서는 캐시 해제 시 즉시 반영, 프로덕션은 재시작 필요
- **서버 재시작 필요**: 예 (`bash server-restart.sh`)

---

## 7. 예상 소요 시간

| 작업 | 시간 |
|------|------|
| DTO + Service 작성 | 30 분 |
| Controller 엔드포인트 | 5 분 |
| HTML/JS 작업 (C 탭 UI + 로직) | 40 분 |
| 빌드 + 재시작 | 5 분 |
| 수동 회귀 테스트 T1~T20 | 25 분 |
| codex 검증 | 5 분 |
| **합계** | **~100 분** |

---

## 8. 체크리스트

- [ ] `ErdGraphDTO.java` (records 3종)
- [ ] `ErdGraphService.java` — 파싱 + 병합 + FK 매핑 + 캐시
- [ ] `SystemGraphController.java` — `/api/erd` 엔드포인트
- [ ] `admin-system-graph.html` — 레이아웃 (그래프 + 상세패널)
- [ ] `admin-system-graph.html` — 검색 + 도메인 필터 UI
- [ ] `admin-system-graph.html` — JS: fetch + vis-network + 이벤트 + API 실패 시 안내문(FR-12)
- [ ] `application.properties` — `app.erd.mmd-dir=docs` 추가
- [ ] `./mvnw.cmd clean compile` BUILD SUCCESS
- [ ] `bash server-restart.sh` 기동 성공
- [ ] 회귀 테스트 **T1~T20 전체** 통과 (FR-11/12, NFR-1/2/5 포함)
- [ ] codex Specs 중심 검증 ✅

---

## 9. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
