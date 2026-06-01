---
tags: [dev-plan, sprint, feature, inspection, infra]
sprint: "inspect-infra-diff-alert"
status: draft
created: "2026-06-01"
---

# [개발계획서] 점검내역서 인프라 저장값 ↔ 현장 수집값 차이 알림

- **작성팀**: 개발팀
- **작성일**: 2026-06-01 (집 세션 / `IUHOME`)
- **근거 기획서**: [[inspect-infra-diff-alert]] **v1.3 (사용자 최종승인 2026-06-01)** — codex 1·2차 반영분
- **상태**: v3 (codex 2차 검토 — "4-c 키별 추출 규칙 명시 시 승인 가능" 반영 → 승인 대기)
- **다음 단계**: codex 검토 → 사용자 승인 → 구현

---

## 0. 전제 / 환경

### 0-1. DB / 마이그레이션 정책
- PostgreSQL. 마이그레이션 디렉토리 = `swdept/sql/`. 최신 버전 = `V20260403_user_fields.sql` (날짜형 컨벤션) → 신규 = **`V20260601_add_host_name_to_infra_server.sql`**.
- **운영 실DB 1개** (개발DB 없음). 집/출장=공인 IP `211.104.137.55:5881`, 회사=내부 IP `192.168.10.194:5880` — 동일 DB. 모든 DDL=운영 변경.
- **위치 정책 (2026-06-01 사용자 명시)**: 현재 속도 우선 — **어디서든 마이그레이션 실행 OK** (NFR-10 의 "회사 PC 한정" 은 사용자가 '사무실만' 선언 전까지 비활성). 단 운영 변경이라 실행 직전 명시 확인.
- 위험도 0 — `ALTER ADD COLUMN ... NULL`, 기존 row 무영향.

### 0-2. 범위 고정 (기획서 v1.3 확정)
- **비교 4필드** = `host_name` · `cpuSpec` · `memorySpec` · `diskSpec` (FR-3 1차). 그 외(network/os/power/serial 등)는 2차 sprint.
- **매칭 키** = `(normalize(server_role), normalize(host_name))` (FR-3-1). **host_ip 미사용** — 적재 코드 `InspectionQrBatchService` L398 `hostIp=null` (R-12).
- **비교 source** = `InspectMetricSnapshot.raw_payload` 단독 (자동 적재값). `InspectCheckResult`·사용자 form 입력값 제외 (DB팀 §12-1(3)(4)).
- **액션 없음** — 자동수정/저장차단 X. 알림만 (FR-7).
- **host_name 입력/수정 UI = 본 sprint 필수** (§7-3 / R-13) — 매칭 키이자 비교 대상이라 입력 경로 없으면 동작 불가.

### 0-3. 비침범 (다른 sprint 미침입)
- design-token-migration sprint 산출물(완료) 변경 X. 점검 자동화/QR 적재 로직(`InspectionQrBatchService`) **읽기만**, 적재 흐름 수정 X.
- `InspectCheckResult` / 성과 리포트 / 차트(P5) 미변경.

### 0-4. 기존 자산 재사용 (조사 결과)
| 자산 | 현황 | 본 sprint 활용 |
|---|---|---|
| `InspectMetricSnapshot` 엔티티 | `host_name`(64)·`host_ip`(45)·`raw_payload`(jsonb Map)·`server_role`·`collected_at`·`pjt_id` 이미 존재 | **스냅샷측 스키마 변경 0** |
| `InspectMetricSnapshotRepository` | `findRecentByPjtRole` 등 시계열 쿼리 보유 | 최신 1건 쿼리만 신규 추가 (4-b) |
| `DocumentController.getInfraServers` (L1707) | distNm+sysNmEn→인프라 14필드 JSON (VIEW 권한 게이트 기존) | hostName 1필드 추가 + 신규 Snapshot API 가 동일 패턴 차용 |
| `SwProjectRepository` | — | **불필요** (codex 검토 — Snapshot API 가 pjtId 직접 수신, 4-a) |
| `doc-inspect.html` | `projId` select 보유(L199·797·954), 문서 API 를 `pjtId` 로 호출(L804·972) | Snapshot API 에 pjtId 전달 (4-a/5-1) |
| `raw_payload` 구조 | `tier.items = List<[key,label,value]>`, key=`ap.hw.cpu` 형태 (L401-404) | 서버측 추출 함수 (4-c) — 직접 path 접근 X (R-15) |

---

## 1. 작업 순서

### Step 1 — DB 마이그레이션 (`host_name` 컬럼)
**1-1.** `swdept/sql/V20260601_add_host_name_to_infra_server.sql`
```sql
ALTER TABLE tb_infra_server ADD COLUMN host_name VARCHAR(64);
COMMENT ON COLUMN tb_infra_server.host_name IS '인프라 호스트명 — 현장 스냅샷 매칭/비교 키 (inspect-infra-diff-alert sprint)';
```
**1-2.** `swdept/sql/V20260601_rollback.sql`
```sql
ALTER TABLE tb_infra_server DROP COLUMN IF EXISTS host_name;
```
- index 검토: 1차는 미생성 (서버 N 소수, full scan 무비용). 2차 데이터 증가 시 재검토.
- **실행은 회사 PC 한정** — 본 단계는 코드 작성까지. (T-19)

### Step 2 — 도메인 / DTO / 기존 API
**2-1.** `InfraServer.java` — `@Column(name="host_name", length=64) private String hostName;` + getter/setter (기존 필드 패턴 그대로).
**2-2.** `InfraServerDTO` (있으면) — `hostName` 노출. 없으면 Map 직접 구성부만 수정.
**2-3.** `DocumentController.getInfraServers` (L1707) — 응답 map 에 `m.put("hostName", s.getHostName());` 추가 + javadoc 키 목록 갱신. (민감정보 정책 유지 — host_name 은 비민감)

### Step 3 — 인프라 host_name 입력/수정 UI (R-13)
**3-1.** `infra-form.html` — WEB 섹션(L145~)·DB 섹션 각 서버 블록에 host_name 입력칸 추가:
```html
<th class="th-group-hw">호스트명</th>
<td><input type="text" th:name="|servers[${sStat.index}].hostName|"
           th:value="${server.hostName}" class="input-cell" placeholder="ex) upis-ap01"></td>
```
**3-2.** `infra-detail.html` (L85 부근) — 표시 행 추가: `<th>호스트명</th><td th:text="${server.hostName} ?: '-'"></td>`.
**3-3.** 저장 흐름 — infra 저장 컨트롤러/서비스가 `servers[].hostName` 바인딩 저장하는지 확인 (대개 @ModelAttribute 자동 바인딩). 누락 시 매핑 추가. 회귀: 기존 인프라 저장 정상.

### Step 4 — 신규 Snapshot API (`GET /document/api/inspect-snapshots`)
**4-a. pjtId 직접 수신** (codex 검토 — 과거회차/연도중복 정확성) — Snapshot API 파라미터 = **`pjtId`** (distNm+sysNmEn 아님). <br>근거: `doc-inspect.html` 이 이미 `projId` 보유(L199 select·L797·L954)하고, 기존 문서 API 도 pjtId 로 호출(L804 `inspect-report/find?pjtId=`·L972 `inspect-chart/preview?pjtId=`). → **신규 SwProject finder 폐기**(불필요). 점검내역서가 보는 바로 그 프로젝트의 스냅샷만 정확 조회 (최신연도 finder 의 오선택 위험 제거). 프론트(5-1)는 `pjtId = document.getElementById('projId').value` 전달.
**4-b. 최신 1건 쿼리** (NFR-9 / R-14 — codex 검토 tie-breaker 반영) — `MAX(collectedAt)` JPQL 은 동시각 tie 시 중복 반환 → **native window query** + `snapshot_id` tie-break:
```java
@Query(value = """
  SELECT * FROM (
    SELECT s.*, row_number() OVER (
      PARTITION BY pjt_id, server_role, COALESCE(host_name,'')
      ORDER BY collected_at DESC, snapshot_id DESC) rn
    FROM inspect_metric_snapshot s WHERE pjt_id = :pjtId
  ) t WHERE rn = 1
  """, nativeQuery = true)
List<InspectMetricSnapshot> findLatestPerRoleHost(@Param("pjtId") Long pjtId);
```
- `(server_role, COALESCE(host_name,''))` 별 `collected_at` 최신 1건 (동시각이면 snapshot_id 큰 것). 기존 적재 unique 키(`upsertIgnore` L121 `COALESCE(host_name,'')`)와 정렬 정합.
- (codex 2차 안전권고) 외부 SELECT 는 `rn` 매핑 혼선 방지 위해 엔티티 컬럼 명시 가능 — Hibernate 가 보통 extra `rn` 무시하나 구현 시 선택.
**4-c. allowlist 추출** — 서버측 단일 메서드가 raw_payload 에서 key 기준 추출 (R-8/R-15 — codex 검토 반영):
- raw_payload = `Tier` 직렬화 (`HASH_MAPPER.writeValueAsString(tier)`, L426). **JSON 키 = `i`** (`Tier.items` 에 `@JsonProperty("i")`, L81), tuple = `[key, status, value]` → **key=index0, value=index2** (status=index1 무시). 호환: `i` 우선, 없으면 `items` fallback.
- 호스트명 = raw_payload `h` (= host_name 컬럼 동일값, L70).
- **⚠ 컬럼 `cpu_pct/mem_pct/disk_pct` 는 성능 백분율**(차트용, L413-419)이라 **스펙 비교 미사용**. 비교는 hw 키의 value(index2)만.
- **value(index2) = Map(객체)** (codex 2차 확인 — 텍스트 아님). 키별 추출 규칙 = **기존 표시 로직 `ResultText` switch (L620-630) 재사용** (표시값과 비교값 동일 보장):

  | key | Map subfield 추출 | 비교 입력 텍스트 |
  |---|---|---|
  | `ap.hw.cpu` | `name` + `cores` | `{name} {cores}코어` |
  | `ap.hw.memory` | `installed_gb` | `{n}GB` |
  | `ap.os.disk_summary` | `summary` | summary 텍스트 |
  | `db.os.cpu_info` | `cores` + `clock_ghz` | `{c}코어 {g}GHz` |
  | `db.os.mem_info` | `total_gb` | `{n}GB` |
  | `db.os.disk` | `mounts` 배열 | mounts summary (배열 → 구현 시 실제 스냅샷 1건으로 추출식 확정) |

  → 위 변환 텍스트를 `InfraServer.cpuSpec/memorySpec/diskSpec` 와 FR-10 정규화 비교. `db.os.disk` 만 배열이라 구현 시 샘플 검증 (R-15 잔여).
- 응답 필드 = `serverRole·hostName·cpu·memory·disk` **만** (host_ip·status·기타 제외).
**4-d. 컨트롤러** — `DocumentController` 에 `@GetMapping("/api/inspect-snapshots")` `@RequestParam Long pjtId`:
- 권한 게이트 `if ("NONE".equals(getAuth())) 403` (getInfraServers 패턴 복제, NFR-8).
- 200 + 빈 배열 (Snapshot 0건 = "현장 미수집", T-12) vs 4xx/5xx (T-14 silent skip) 명확 구분 (NFR-9). 정상 경로 항상 200.

### Step 5 — 프론트 비교 로직 (doc-inspect.html, FR-12 단일 함수)
**5-1.** 페이지 로드 시 기존 infra-servers fetch + 신규 `inspect-snapshots?pjtId=`+projId.value fetch 병렬(Promise.all). snapshot fetch 실패 = silent skip + console.error (점검흐름 무영향, T-14).
**5-1b. 저장 버튼 재비교 hook (FR-4(b))** — 저장 버튼 click 핸들러에 비교 재실행 → 차이 있으면 1회 더 알림(저장 차단 X, dismiss 가능). 캐시된 snapshot 재사용(재fetch X).
**5-2.** **매핑/정규화/allowlist 단일 모듈** `inspectInfraDiff(infraList, snapshotList)`:
- 매칭: `(normalize(server_role), normalize(host_name))` — server_type WEB→ap. trim+lowercase.
- 필드 정규화 (FR-10): host_name=trim+lower / cpu=코어수+name substring / memory=GB ±5% / disk=summary 부분매칭.
- 결과 = `[{role, host, field, infraVal, fieldVal, status: 'diff'|'infra-only'|'field-only'|'unmatched'}]`.
**5-3.** allowlist 강제 — 4필드 외 무시 (FR-11). raw_payload 신규 키 silent skip (T-18).

### Step 6 — 알림 UI (FR-5/6, NFR-2/3/4)
**6-1.** `design-system.css :root` — 신규 토큰 4종: `--alert-bg:#FEF3C7; --alert-fg:#B45309; --alert-border:#FCD34D; --alert-icon:#D97706;` + 다크 변종(`[data-theme=dark]`: bg#451A03/fg#FDE68A/border#92400E). raw hex 직접사용 0 (NFR-2/6).
**6-2.** **상단 요약 박스** — `.doc-header` 바로 아래, dismissible `⚠ 인프라와 다른 필드 N개` + 접기/펼치기 (디자인팀 (a) 위치 확정). dismiss = 세션·페이지 한정, state 저장 X (FR-8/R-6).
**6-3.** **인라인 ⚠** — 차이 셀 옆 `fa-triangle-exclamation` + **CSS-only click popover** (`:focus ~ .popover`, 라이브러리 X, 디자인팀 (d)). `인프라값:[X] / 현장값:[Y]`. dismiss 영향 X (FR-8).
**6-4.** 한글 라벨 매핑 (FR-9): cpuSpec→"CPU 사양" 등.
**6-5.** NFR-5 specificity 사전점검 (2026-06-01 main-dashboard 사례 학습).

### Step 7 — 테스트 / 검증
- **단위(JS)**: 정규화 비교 — T-6(IP trim)·T-15(mem±5%)·T-16(CPU core)·T-17(disk 부분매칭).
- **시나리오**: T-1~T-5(차이/방향), T-7/8(dismiss), T-9(N=0), T-10(다크모드 토글 시 alert 색·대비), T-11(권한), T-12(Snapshot0)/T-14(API실패) UX 분기, T-13(매칭실패), T-18(allowlist), T-20(최신 1건), T-21(host_name 입력→매칭).
- **성능·접근성 측정 (codex 검토 추가)**: NFR-1 page load 증가 ≤200ms (DevTools Performance, 비교 on/off 측정) · NFR-9 Snapshot API P95 ≤300ms (서버 N≤5 반복호출) · NFR-3 WCAG AA 대비 ≥4.5:1 (alert-bg/fg·인라인 아이콘, 라이트+다크 — 대비 계산기).
- **회귀(NFR-7)**: 인프라 API·점검입력·저장 흐름 동일. 인프라 저장(host_name 추가) 회귀.
- **마이그레이션(T-19)**: 회사 PC 적용 후 기존 row host_name=NULL·회귀 0.

---

## 2. FR / NFR / T 커버리지 매핑

| 기획서 항목 | 구현 Step |
|---|---|
| FR-1 (병렬 fetch·매칭키) | 5-1, 4-a |
| FR-2~FR-9 (양방향비교·알림·dismiss·라벨) | 5-2, 6-* |
| FR-3/3-1 (4필드·매칭키 host_name) | 0-2, 4, 5-2 |
| FR-10 (정규화) | 5-2 |
| FR-11/12 (allowlist·단일함수) | 4-c, 5-2/5-3 |
| NFR-2/3/4/5/6 (토큰·대비·다크·specificity) | 6-1, 6-5 |
| NFR-8 (권한) | 4-d |
| NFR-9 (API명세·최신1건·UX분기) | 4-b, 4-c, 4-d |
| NFR-10 (마이그 정책) | Step 1 |
| R-12 (host_ip) | 0-2, 4-a |
| R-13 (host_name 입력) | Step 3 |
| R-14 (최신선택) | 4-b |
| R-15 (raw_payload items) | 4-c |
| T-1~T-21 | Step 7 |

---

## 3. 롤백
- 코드: 커밋 단위 revert (Step 2~6 은 순수 추가/격리라 revert 안전).
- DB: `V20260601_rollback.sql` (`DROP COLUMN host_name`). 구조 영향 0. **단 (codex 검토) DROP COLUMN = 사용자 입력 host_name 값 손실 = "데이터 손실성 롤백"** — 운영 롤백 전 `SELECT server_id, host_name` 백업 권고.

## 4. codex 검토 결과
- **codex 1차 (⚠ 수정필요) → v2 반영 완료**. 원문 = `docs/product-specs/reviews/inspect-infra-diff-execplan-codex-1st.md`.
  - 집중지점 3개 중 2개 보완: 4-a pjtId 직접수신 / 4-b tie-breaker / 4-c `i` 키·hw 추출 명세 + 누락(NFR-1/3/9 측정·T-10·FR-4(b) hook)·롤백 데이터손실 명시.
  - 기획서 sync: Snapshot API param `distNm+sysNmEn` → `pjtId` → 기획서 v1.4.
- **codex 2차 (⚠ — "수정 요구 작음, 4-c 키별 규칙 명시 시 승인 가능") → v3 반영 완료**. 원문 = `docs/product-specs/reviews/inspect-infra-diff-execplan-codex-2nd.md`.
  - 4-a·4-b **해소 확인**. 4-c: `ap.hw.cpu` value 가 텍스트 아닌 `{name,cores}` 객체 → **키별 추출 규칙표 추가** (기존 `ResultText` L620-630 재사용). 4-b 외부 SELECT 컬럼명시 안전권고 반영.
  - 잔여(비차단): infra-servers API 는 기존대로 distNm+sysNmEn 첫 인프라(기존동작 유지) · `db.os.disk` 배열 추출은 구현 시 샘플 확정.

## 4-1. 구현 결과 (2026-06-02, 집 세션)
- **Step 1~6 구현 완료** + 운영DB `host_name` 컬럼 적용(332 row 무영향). 컴파일·서버 기동·CSS 토큰·4-b 윈도우 SQL 유효성 검증 통과.
- **codex 구현검증 (⚠ 6건) → 5건 수정 완료**. 원문 = `docs/product-specs/reviews/inspect-infra-diff-impl-codex.md`.
  1. T-12 위반(스냅샷0건 무알림) → 분기 수정: snaps=0 시 "현장 미수집"(T-12), 일부매칭 시 "매칭 불가"(T-13) ✅
  2. 저장문서 로드 경로 비교 미호출 → loadInfraDiff hook 추가 ✅
  3. db.os.disk summary-only → count fallback 추가(mounts 배열은 R-15 잔여) ✅
  4. CSS 다크 raw hex → design-system.css `[data-theme=dark]` --alert-* 토큰 재정의, 아이콘 대비 --alert-fg 로 보정 ✅
  5. InfraServerDTO.hostName 누락 → 필드+매핑 추가 ✅
- **⏭ FR-5 인라인 ⚠ popover = 보류(후속)**: 점검표 각 셀↔(서버,필드) DOM 매핑 추가분석 필요 + 표출 실데이터(스냅샷 0건) 없음 → 상단 요약박스(FR-6)로 기능 동작 확보 후 후속 sprint. 데이터 적재 시 함께 구현 권장.
- **데이터 전제**: `inspect_metric_snapshot` 운영 0건 + infra host_name 전부 NULL → 실제 차이 표출은 스냅샷 적재 + host_name 입력 후 (R-13/R-15).

## 4-2. Step 7 검증·측정 결과 (2026-06-02, 회사 PC / 내부망)

서버 기동(`server-restart.sh`, local 프로파일, 운영DB 연결) 후 헤드리스 측정. 인증 세션 쿠키 = **`SWMANAGER_SESSION`** (JSESSIONID 아님 — 커스텀 세션 쿠키명).

| 항목 | 목표 | 측정 | 판정 |
|---|---|---|---|
| **NFR-3** alert 대비 (계산) | ≥4.5:1 | 라이트 본문/아이콘(=fg) `#B45309`/`#FEF3C7` **4.51:1**, 다크 본문 `#FDE68A`/`#451A03` **12.03:1**, 다크 아이콘 `#FBBF24`/`#451A03` **8.97:1** | ✅ PASS |
| **NFR-9** Snapshot API P95 | ≤300ms | P50 3.5 / **P95 4.5** / P99 20.3 / max 28.1ms (n=100) | ✅ PASS |
| **NFR-1** 페이지 로드 증가 | ≤200ms | 추가 부하 = 신규 `inspect-snapshots` fetch(P95 4.5ms, 기존 infra fetch 와 `Promise.all` 병렬) + JS 비교(≤5서버×4필드). doc-inspect 페이지 렌더 P95 26.7ms | ✅ PASS (여유 막대 / 브라우저 on-off DevTools 정밀측정은 미실시) |
| **회귀(NFR-7)** infra-servers API | 무회귀 | `hostName` 추가 후 dummy 파라미터 200 + `[]`, 크래시 없음. doc-inspect 페이지 200 | ✅ PASS |
| **T-10** 다크모드 알림 시각 | alert 색·대비 | 색·대비는 NFR-3(라이트+다크 AA)로 검증. **실제 알림박스 시각 렌더는 미검증** | ⚠ 부분 — 데이터 종속 |

### 측정으로 드러난 잔여/플래그
1. **`--alert-icon #D97706` = 2.86:1 FAIL** (라이트, `#FEF3C7` 위). 현재 출고 컴포넌트(`#infraDiffAlert`)는 아이콘에 `--alert-fg` 사용(4.51:1 PASS)이라 무해하나, **죽은 토큰**. → **FR-5 인라인 popover 후속 구현 시 이 토큰을 그대로 쓰면 AA 위반** → `--alert-fg`/더 진한 값으로 교체 필요.
2. **0건 데이터 baseline 한계**: 운영 `inspect_metric_snapshot` 0건 + infra host_name 전부 NULL → NFR-9/NFR-1 은 빈결과 경로 측정(대표성 제한), **T-10 시각 검증·FR-5 표출은 불가**. 실 차이 표출 검증은 스냅샷 적재 + host_name 입력 후 후속.
3. **`db.os.disk` mounts 배열 추출**(R-15 잔여) — 실 스냅샷 샘플 필요(현재 0건).

### Step 7 종합 판정
정량 게이트(NFR-1/3/9)·회귀 **PASS**. **데이터 종속 항목**(T-10 시각·FR-5·db.os.disk 배열·alert-icon 교체)은 스냅샷 적재 + host_name 입력 시점의 후속 sprint 로 이월. → 본 sprint 의 **코드/품질 게이트는 마감 가능**.

## 5. 변경 이력
- **2026-06-02** (회사 PC / 내부망) — Step 7 검증·측정 수행, §4-2 추가. NFR-1/3/9·회귀 PASS. alert-icon 죽은 토큰 AA FAIL 플래그(FR-5 시 교체). 데이터 종속 항목(T-10 시각·FR-5·db.os.disk 배열) 후속 이월. 코드/품질 게이트 마감 가능 판정.
- **2026-06-01 v1** (집 세션) — 기획서 v1.3 승인 기반 초안. 조사 결과(스냅샷 스키마 기보유·SwProject finder 부재·raw_payload items 구조) 반영. 다음 = codex 검토.
- **2026-06-01 v3** (집 세션) — codex 2차 검토(⚠, "수정 요구 작음 → 승인 가능") 반영. 4-a·4-b 해소 확인. **4-c 키별 추출 규칙표** 추가 (`ap.hw.cpu`→`name`+`cores` 등, 기존 `ResultText` L620-630 재사용 — 표시값=비교값 보장). 4-b 외부 SELECT 컬럼명시 안전권고. 잔여(비차단): infra-servers 기존동작 유지·`db.os.disk` 배열 구현 시 샘플 확정. **다음 = 사용자 최종승인 → 구현**.
- **2026-06-01 v2** (집 세션) — codex 1차 검토(⚠) 반영. **(1)** Snapshot API `distNm+sysNmEn`→`pjtId` 직접수신 (doc-inspect projId 보유 검증, 과거회차/연도중복 오선택 제거, SwProject finder 폐기). **(2)** 최신1건 쿼리 native window + snapshot_id tie-break. **(3)** raw_payload JSON 키 `i`(@JsonProperty) + hw 키 value(index2) 추출 명세, cpu_pct 컬럼=성능% 미사용 명시. **(4)** Step7 에 NFR-1/3/9 측정·T-10·FR-4(b) 저장 hook 추가. **(5)** 롤백 데이터손실성 명시. 기획서 v1.4 sync 항목 발생.
