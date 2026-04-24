# [개발계획서] 시스템 관계도 — C. ERD 한글 설명

- **작성팀**: 개발팀
- **작성일**: 2026-04-18
- **기획서**: [docs/plans/system-graph-erd-descriptions.md](../plans/system-graph-erd-descriptions.md) (v2 승인됨)
- **선행 커밋**: `af2a1b0` (C. ERD 인터랙티브 본체)
- **상태**: v2 (codex 재검토 대기) — 1차 검토 2건 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 |
| v2 | codex 검토 반영: ①YAML null 값이 문자열 "null" 로 둔갑하는 버그 방지 (String 타입일 때만 캐스팅) ②FR-11(UTF-8 BOM, T11), NFR-2(응답 크기, T12), 명시적 YAML null 처리(T13) 검증 케이스 추가 |

---

## 1. 영향 범위

| 계층 | 파일 | 변경 유형 |
|------|------|-----------|
| Data (신규) | `docs/erd-descriptions.yml` | 33 테이블 + 주요 컬럼 한글 설명 초안 (유지·보수 대상 아티팩트) |
| Config (수정) | `src/main/resources/application.properties` | `app.erd.descriptions-file=docs/erd-descriptions.yml` 1줄 추가 |
| Backend (수정) | `src/main/java/com/swmanager/system/dto/ErdGraphDTO.java` | `Node.desc`, `Column.desc` 필드 추가 |
| Backend (수정) | `src/main/java/com/swmanager/system/service/ErdGraphService.java` | YAML 로드 + Node/Column 생성 시 desc 주입 |
| Frontend (수정) | `src/main/resources/templates/admin-system-graph.html` | 상세 패널/호버 툴팁/FK 툴팁에 desc 표시 (모두 escape 처리) |
| DB | **없음** | — |
| 신규 의존성 | **없음** | SnakeYAML 2.2 이 이미 spring-boot-starter 를 통해 포함됨 (검증 완료: `mvn dependency:tree`) |

**신규 1개 + 수정 4개. DB·의존성 변경 없음.**

---

## 2. 파일별 상세 설계

### 2-1. `docs/erd-descriptions.yml` (신규)

#### 스키마
```yaml
tables:
  <table_name>:
    desc: <테이블 한글 설명>
    columns:
      <column_name>: <컬럼 한글 설명>
```

#### 초안 생성 방침
- 33개 테이블 모두 `desc` 추가 (도메인·이름·관계선 기반 추론).
- 컬럼 설명은 **식별성 높은 필드 우선**(PK/FK/주요 비즈니스 필드). 모든 컬럼을 채우진 않음 (점진적 보강).
- 초안 커밋 전 사용자 검수를 위해 코드와 함께 미리 요약 제시 (7-3 기획서 결정).

#### 파일 상단 주석 (유지보수 규칙)
```yaml
# ERD 인터랙티브 상세 패널에 표시할 한글 설명.
# 사용법:
#   - 스키마 변경(테이블/컬럼 추가·이름 변경) 시 이 파일도 갱신.
#   - 빠진 항목은 설명 없이 표시됨 (오류 아님).
#   - desc 에 <, >, &, ", ' 같은 특수문자 허용 (Frontend 에서 escape 처리됨).
#   - 서버 재기동 시 반영됨.
```

### 2-2. `application.properties` (수정)

기존 `app.erd.mmd-dir=docs` 아래에 추가:
```properties
# 시스템 관계도 C 탭 — 한글 설명 YAML 파일 경로 (filesystem only)
# 배포 환경에서 경로가 다르면 JVM 옵션 -Dapp.erd.descriptions-file=/path/to/erd-descriptions.yml 로 override.
app.erd.descriptions-file=docs/erd-descriptions.yml
```

### 2-3. `ErdGraphDTO.java` (수정)

기존 record 2개에 `desc` 필드를 **마지막 위치**로 추가 (호환성 고려):
```java
public record Column(String name, String type, boolean pk, boolean fk, String fkRef, String desc) {}
public record Node(String id, String label, String domain, List<Column> columns, String desc) {}
```

- `desc` 가 null 이면 프론트에서 미표시 (FR-7).
- 기존 호출부(`ErdGraphService` 내부) 3곳 전부 새 필드 추가로 함께 갱신 (Java 컴파일러가 컴파일 에러로 빠짐 방지).

### 2-4. `ErdGraphService.java` (수정)

#### 신규 의존 및 내부 구조
```java
@Value("${app.erd.descriptions-file:docs/erd-descriptions.yml}")
private String descriptionsFile;

private Map<String, TableDescription> descriptions = Map.of();   // 캐시

private record TableDescription(String desc, Map<String, String> columns) {}
```

#### 로딩 로직 (init() 시작 단계에서 실행, .mmd 로딩보다 먼저)
```java
@PostConstruct
public void init() {
    loadDescriptions();          // 추가
    long start = System.currentTimeMillis();
    try {
        this.cache = load();     // 기존
        log.info("ERD parsing: {}ms, nodes={}, edges={}, descTables={}",
                System.currentTimeMillis() - start,
                cache.getNodes().size(), cache.getEdges().size(), descriptions.size());
    } catch (Exception e) {
        log.error("ERD 초기 파싱 실패 — 빈 그래프로 대체", e);
        this.cache = new ErdGraphDTO(List.of(), List.of());
    }
}

private void loadDescriptions() {
    FileSystemResource fs = new FileSystemResource(descriptionsFile);
    if (!fs.exists()) {
        log.warn("ERD descriptions YAML not found: {} — 설명 없이 진행", descriptionsFile);
        return;
    }
    try (InputStream in = fs.getInputStream()) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(in);
        if (root == null) return;
        Object tablesObj = root.get("tables");
        if (!(tablesObj instanceof Map)) return;
        Map<String, TableDescription> parsed = new HashMap<>();
        ((Map<String, Object>) tablesObj).forEach((name, value) -> {
            if (!(value instanceof Map)) return;
            Map<String, Object> t = (Map<String, Object>) value;
            Object descRaw = t.get("desc");
            String desc = (descRaw instanceof String s && !s.isEmpty()) ? s : null;
            Map<String, String> cols = Map.of();
            Object colsObj = t.get("columns");
            if (colsObj instanceof Map) {
                Map<String, String> colMap = new HashMap<>();
                ((Map<String, Object>) colsObj).forEach((k, v) -> {
                    // null/비-String/빈 문자열은 건너뛴다 → desc=null 로 정상 처리 (FR-7)
                    if (v instanceof String s && !s.isEmpty()) {
                        colMap.put(k, s);
                    }
                });
                cols = colMap;
            }
            parsed.put(name, new TableDescription(desc, cols));
        });
        this.descriptions = Map.copyOf(parsed);
        log.info("ERD descriptions loaded: {} tables from {}", descriptions.size(), descriptionsFile);
    } catch (Exception e) {
        log.warn("ERD descriptions YAML 파싱 실패 — 설명 없이 진행", e);
    }
}
```

#### Node/Column 빌드 단계에서 desc 주입
```java
// 최종 노드 생성 직전
TableDescription td = descriptions.get(tableName);
String tableDesc = td != null ? td.desc() : null;
Map<String, String> colDescs = td != null ? td.columns() : Map.of();

List<ErdGraphDTO.Column> finalCols = e.getValue().stream()
        .map(c -> new ErdGraphDTO.Column(
                c.name(), c.type(), c.pk(), c.fk(),
                c.fk() ? refMap.get(c.name()) : null,
                colDescs.get(c.name())))   // ← desc
        .collect(Collectors.toList());
nodes.add(new ErdGraphDTO.Node(tableName, tableName, domain, finalCols, tableDesc));
```

#### 로그 주의
- YAML 에 있지만 정본에 없는 테이블은 **debug** 로 개수만 집계 (FR-4 — 무시하되 추적 가능).
- 컬럼도 동일하게 debug 집계.

### 2-5. `admin-system-graph.html` (수정)

#### 상세 패널 HTML 생성 로직 (기존 `showNodeDetail` 수정)

```javascript
function showNodeDetail(tableId) {
    // ... 기존 선언부
    var html = '<div class="detail-title">' + escapeHtml(node.label) + '</div>';
    // FR-7: 테이블 설명 (있을 때만, escape 필수)
    if (node.desc) {
        html += '<div class="detail-desc">' + escapeHtml(node.desc) + '</div>';
    }
    html += '<div class="detail-domain" style="background:' + c.background + ';">' + node.domain + '</div>';
    html += '<ul class="col-list">';
    node.columns.forEach(function(col) {
        var badges = '';
        if (col.pk) badges += '<span class="badge-pk" title="Primary Key">PK</span> ';
        if (col.fk) {
            if (col.fkRef) {
                // FR-9: FK 참조 테이블 설명 (있으면) tooltip 에 합성, escape 필수
                var refDesc = findDesc(col.fkRef);
                var fkTip = escapeAttr('클릭 시 ' + col.fkRef + (refDesc ? ' · ' + refDesc : '') + ' 로 이동');
                badges += '<span class="badge-fk" title="' + fkTip + '" onclick="focusNode(\'' + escapeAttr(col.fkRef) + '\')">FK→' + escapeHtml(col.fkRef) + '</span> ';
            } else {
                badges += '<span class="badge-fk disabled" title="관계선 미정의로 이동 불가">FK</span> ';
            }
        }
        // FR-7: 컬럼 설명 (있을 때만 괄호 안에, escape 필수)
        var colDescHtml = col.desc ? ' <span class="col-desc">(' + escapeHtml(col.desc) + ')</span>' : '';
        html += '<li>' + badges +
            '<span class="col-name">' + escapeHtml(col.name) + '</span>' + colDescHtml + ' ' +
            '<span class="col-type">' + escapeHtml(col.type) + '</span></li>';
    });
    html += '</ul>';
    panel.innerHTML = html;
}

// helper — 다른 테이블의 desc 조회 (FR-9)
function findDesc(tableId) {
    if (!erdData) return null;
    var n = erdData.nodes.find(function(x) { return x.id === tableId; });
    return n ? n.desc : null;
}
```

#### 노드 호버 툴팁 (FR-8)
`renderErdGraph()` 의 `visNodes` 매핑에 `title` 속성 추가:
```javascript
var visNodes = data.nodes.map(function(n) {
    var c = DOMAIN_COLORS[n.domain] || DOMAIN_COLORS.core;
    // FR-8: 툴팁에 테이블명 + desc (있으면), escape 필수
    var title = escapeHtml(n.label);
    if (n.desc) title += '\n' + escapeHtml(n.desc);
    return {
        id: n.id,
        label: n.label,
        shape: 'box',
        color: { background: c.background, border: c.border },
        font: { color: c.font, size: 14 },
        title: title,                 // ← 추가
        domain: n.domain,
        columns: n.columns
    };
});
```

#### 신규 CSS
```css
.erd-detail .detail-desc {
    margin: 0 0 8px; color: #666; font-size: 0.85em; font-style: italic;
}
.erd-detail .col-desc {
    color: #888; font-size: 0.82em;
}
```

---

## 3. 작업 순서

| Step | 작업 | 검증 |
|------|------|------|
| 1 | `docs/erd-descriptions.yml` 초안 작성 (33 테이블 + 주요 컬럼) | YAML 문법 검증 (파일 읽기 시 Yaml 파서 에러 없음) |
| 2 | 초안을 **사용자에게 요약 제시** (기획서 7-3 하이브리드) — 사용자 검수 후 보정 | 사용자 확인 |
| 3 | `ErdGraphDTO.java` record 에 `desc` 추가 | 컴파일 성공 |
| 4 | `ErdGraphService.java` 에 `loadDescriptions()` + 주입 로직 추가 | 기동 로그에 `descriptions loaded: N tables` |
| 5 | `application.properties` 에 descriptions-file 추가 | 프로퍼티 인식 |
| 6 | `admin-system-graph.html` 상세 패널/호버/FK 툴팁 + CSS 수정 | 브라우저 콘솔 에러 없음 |
| 7 | `./mvnw.cmd clean compile` | BUILD SUCCESS |
| 8 | `bash server-restart.sh` | Started SwManagerApplication |
| 9 | 수동 회귀 테스트 T1~T13 (T8 XSS + T11 BOM + T12 크기 + T13 null 필수) | 모두 PASS |
| 10 | codex Specs 중심 검증 | ✅ 승인 |
| 11 | 사용자 "작업완료" 후 commit + push | master 반영 |

---

## 4. 회귀 테스트

| # | 시나리오 | 기대 결과 | 연관 Specs |
|---|---------|-----------|-----------|
| T1 | 서버 재기동 후 로그 | `ERD descriptions loaded: N tables` (N >= 1). `ERD parsing: ...ms` 도 정상 | FR-1, NFR-1 |
| T2 | C 탭에서 `tb_contract_participant` 클릭 | 상세 패널에 테이블 설명 + 각 컬럼 설명 표시 (기획서 3-1 예시처럼) | FR-7 |
| T3 | YAML 에 없는 테이블 클릭 (예: 일부만 기재한 상태) | 설명 줄 없이 기존과 동일하게 표시, 빈 괄호 없음 | FR-5, FR-7 |
| T4 | YAML 에 있지만 `.mmd` 에 없는 테이블 엔트리 (의도적으로 넣어 테스트) | 서버 로그에 debug 메시지 1회, 오류 아님 | FR-4 |
| T5 | 노드 호버 | vis-network 툴팁에 `테이블명\n한글설명` (설명 있을 때) | FR-8 |
| T6 | FK 배지 호버 (참조 테이블에 desc 있음) | 툴팁에 `클릭 시 tb_xxx · 한글설명 로 이동` | FR-9 |
| T7 | FK 배지 호버 (참조 테이블에 desc 없음) | 툴팁에 `클릭 시 tb_xxx 로 이동` | FR-9 |
| T8 | **XSS 방어 테스트** — `erd-descriptions.yml` 에 임의 테이블 desc 를 `<script>alert(1)</script>` 로 편집 후 재기동 | 상세 패널·호버·FK 툴팁 모두에서 **문자 그대로 표시**되고 alert 실행 안됨 | FR-7, FR-8, FR-9 |
| T9 | YAML 파일을 일시 이동(또는 문법 오류 주입) 후 재기동 | ERD 는 정상 로드, 설명만 없음, 경고 로그 | FR-1, NFR-3 |
| T10 | 파싱 시간 로그 확인 | YAML 파싱 포함 총 ERD 초기화 1000ms 이내 | NFR-1 |
| T11 | UTF-8 BOM 포함 YAML 로 저장 후 재기동 (`<EFBBBF>` prefix) | 파싱 성공, 한글 설명 정상 표시. 첫 테이블 이름에 BOM 문자 혼입 없음 | FR-11 |
| T12 | `curl -s http://localhost:9090/admin/system-graph/api/erd | wc -c` 로 응답 크기 측정 (ADMIN 세션) | 응답 바디 **50KB 이내** (NFR-2 상한). 초과 시 desc 분량 축소 검토 | NFR-2 |
| T13 | YAML 컬럼 값에 명시적 null 사용: `columns: { some_col: null }` | 해당 컬럼 설명 미표시 (프론트에 "null" 문자열 노출 없음, 빈 괄호 없음) | FR-7, 파싱 규칙 |

---

## 5. 롤백 전략

### 원자적 롤백
- 변경이 단일 커밋에 묶임.
  ```bash
  git revert <commit-sha>
  git push
  bash server-restart.sh
  ```

### 롤백 검증 체크포인트
1. 빌드 성공: BUILD SUCCESS
2. 서버 기동 성공: `Started SwManagerApplication`
3. `/admin/system-graph/api/erd` 응답의 Node/Column 에 `desc` 필드 없음(기존 스키마)
4. C 탭 상세 패널에 설명 미표시 (커밋 이전 동작)
5. 서버 로그에 신규 ERROR 없음
6. 관리자 권한 여전히 정상(302/200 분기)

---

## 6. 빌드 · 재시작 영향

- Java 파일 수정 있음 → **빌드 필요**
- 서버 재시작 필요
- 브라우저 캐시: 변경된 `admin-system-graph.html` 이 강제 캐시 무효화될 필요는 없음 (Thymeleaf 는 리프레시 시 서버에서 새 본문 전달)

---

## 7. 예상 소요 시간

| 작업 | 시간 |
|------|------|
| YAML 초안 작성 (33 테이블) | 25 분 |
| 사용자 검수·수정 (하이브리드) | 10 분 (사용자 몫) |
| DTO 수정 (record 필드 추가) | 5 분 |
| Service 수정 (loadDescriptions + 주입) | 15 분 |
| application.properties | 2 분 |
| HTML/CSS/JS 수정 | 20 분 |
| 빌드 + 재시작 | 5 분 |
| 회귀 테스트 T1~T13 (XSS + BOM + 크기 + null 포함) | 20 분 |
| codex 검증 | 5 분 |
| **합계** | **~100 분** |

---

## 8. 체크리스트

- [ ] `docs/erd-descriptions.yml` 초안
- [ ] 사용자 검수 후 반영
- [ ] `ErdGraphDTO.java` 에 `desc` 필드 (Node, Column)
- [ ] `ErdGraphService.java` — `loadDescriptions()` + Node/Column 빌드 시 주입
- [ ] `application.properties` — `app.erd.descriptions-file` 추가
- [ ] `admin-system-graph.html` — 상세 패널 desc 표시 (escape)
- [ ] `admin-system-graph.html` — 노드 호버 title 합성 (escape)
- [ ] `admin-system-graph.html` — FK 배지 툴팁 강화 (escape)
- [ ] `admin-system-graph.html` — CSS 추가 (`.detail-desc`, `.col-desc`)
- [ ] `./mvnw.cmd clean compile` BUILD SUCCESS
- [ ] `bash server-restart.sh` 기동 성공, 로그에 `descriptions loaded: N tables`
- [ ] 회귀 테스트 T1~T13 전체 통과 (**T8 XSS, T11 BOM, T12 크기, T13 null 필수**)
- [ ] codex Specs 중심 검증 ✅

---

## 9. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
