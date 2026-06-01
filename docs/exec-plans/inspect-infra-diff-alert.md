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
- **상태**: v2 (codex 1차 ⚠ 반영)
- **다음 단계**: codex 검토 → 사용자 승인 → 구현

---

## 0. 전제 / 환경

### 0-1. DB / 마이그레이션 정책
- PostgreSQL. 마이그레이션 디렉토리 = `swdept/sql/`. 최신 버전 = `V20260403_user_fields.sql` (날짜형 컨벤션) → 신규 = **`V20260601_add_host_name_to_infra_server.sql`**.
- 운영 실행 정책 (기획서 NFR-10 / R-11): **회사 PC(`IU`) 에서만 운영 DB 마이그레이션** + 2단계 confirm. 집/출장 세션은 코드/계획까지만. ([[machines]] 룰)
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
**4-c. allowlist 추출** — 서버측 단일 메서드가 raw_payload 에서 key 기준 추출 (R-8/R-15 — codex 검토 반영):
- raw_payload = `Tier` 직렬화 (`HASH_MAPPER.writeValueAsString(tier)`, L426). **JSON 키 = `i`** (`Tier.items` 에 `@JsonProperty("i")`, L81), tuple = `[key, status, value]` → **key=index0, value=index2** (status=index1 무시). 호환: `i` 우선, 없으면 `items` fallback.
- 호스트명 = raw_payload `h` (= host_name 컬럼 동일값, L70).
- **하드웨어 스펙 키** (비교 대상): AP `ap.hw.cpu`·`ap.hw.memory`·`ap.os.disk_summary` / DB `db.os.cpu_info`·`db.os.mem_info`·`db.os.disk` (MANIFEST_SORT L193-216 검증).
- **⚠ 컬럼 `cpu_pct/mem_pct/disk_pct` 는 성능 백분율**(차트용, L413-419)이라 **스펙 비교 미사용**. 비교는 위 hw 키의 value(index2)만.
- value(index2) 실제 형태(문자열/객체)는 agent 산출물 의존 → **구현 시 실제 스냅샷 1건으로 검증** (R-15 잔여).
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
- **⚠ 기획서 sync 필요**: 본 계획서가 Snapshot API 파라미터를 `distNm+sysNmEn` → **`pjtId`** 로 정정(codex 근거). 기획서 v1.3 의 FR-1/FR-3-1/NFR-9 도 v1.4 로 동기화 (별도 patch).

## 5. 변경 이력
- **2026-06-01 v1** (집 세션) — 기획서 v1.3 승인 기반 초안. 조사 결과(스냅샷 스키마 기보유·SwProject finder 부재·raw_payload items 구조) 반영. 다음 = codex 검토.
- **2026-06-01 v2** (집 세션) — codex 1차 검토(⚠) 반영. **(1)** Snapshot API `distNm+sysNmEn`→`pjtId` 직접수신 (doc-inspect projId 보유 검증, 과거회차/연도중복 오선택 제거, SwProject finder 폐기). **(2)** 최신1건 쿼리 native window + snapshot_id tie-break. **(3)** raw_payload JSON 키 `i`(@JsonProperty) + hw 키 value(index2) 추출 명세, cpu_pct 컬럼=성능% 미사용 명시. **(4)** Step7 에 NFR-1/3/9 측정·T-10·FR-4(b) 저장 hook 추가. **(5)** 롤백 데이터손실성 명시. 기획서 v1.4 sync 항목 발생.
