# 점검내역서 — 인프라 저장값 ↔ 현장 수집값 차이 알림 (inspect-infra-diff-alert)

> **Status**: `draft v1.3` (2026-06-01, 집 세션 / 집 PC = `IUHOME` 호스트) — codex 2차 검토 (⚠ 수정필요) 반영: 매칭 키 `host_ip`→`host_name` 정정(코드 L398 hostIp=null), 1차 비교범위 4필드 통일, T-3·NFR-1 정합, raw_payload items 구조 명시, host_name 입력 UI 본 sprint 승격, 신규 리스크 R-12~R-15. **B안 (host_name 컬럼 신설) 확정 유지**
> codex 2차 원문: `docs/product-specs/reviews/inspect-infra-diff-codex-2nd.md`
> **Sprint**: `inspect-infra-diff-alert`
> **선행/관련**: 직전 `design-token-migration` sprint 완료 (commit `8e1d801`)
> **워크플로우**: 풀 (기획서 → DB팀 + 디자인팀 병렬 자문 → codex → 사용자 승인 → 개발계획 → codex → 구현 → codex 검증)

---

## 1. 목적

점검내역서(`doc-inspect`) 작성·열람 시, **인프라 자산 DB(`InfraServer` / `tb_infra_server`)에 저장된 값**과 **현장 점검에서 수집/입력된 값**(자동 QR 배치 적재 `InspectMetricSnapshot.raw_payload` + 사용자 수동 입력 form 필드) 사이에 **차이가 있으면 사용자에게 알림**.

**액션 없음** — 자동 수정 / 저장 차단 / 인프라 DB 자동 업데이트 X. 단순 알림으로 사용자(현장 담당자)가 인지 → 확인 → 인프라 정보 페이지에서 수동 수정 유도.

## 2. 배경 / Why

- 현재 `doc-inspect.html` 은 페이지 로드 시 `/document/api/infra-servers?distNm=...&sysNmEn=...` 로 인프라 13 필드 응답을 받아 점검 입력 영역에 미리 채움 (DocumentController L1707).
- 그러나 현장에서 측정된 실제 값이 인프라 DB 값과 다를 때 자동 비교/알림이 없음 → 인프라 DB 가 stale 한 채로 누적되고, 다른 시스템(성과 리포트, 점검 자동화 RAG 등) 정확도에 영향.
- 사용자 결정 (2026-06-01): **알림만**, 자동 수정은 위험 → 사용자 의사결정 유지.

## 3. 사용자 / 이해관계자

- **점검 담당자 (현장)** — 점검내역서 작성 시 차이 인지 → 확인 후 인프라 페이지에서 수정
- **인프라 관리자** — 수정 권한
- (간접) 운영팀 — 인프라 DB 정합성이 점진적으로 개선됨

## 4. FR (Functional Requirements)

| ID | 요건 |
|---|---|
| **FR-1** | 점검내역서 페이지 로드 시 인프라 정보 API 호출 (현재 동작 유지) + 동시에 신규 Snapshot API `GET /document/api/inspect-snapshots?distNm=...&sysNmEn=...` 호출. 매칭 키 = `(normalize(server_role), normalize(host_name))` (FR-3-1 참조 — codex 2차 R-12 로 `host_ip`→`host_name` 정정) |
| **FR-2** | **양방향 비교** — 비교 대상 필드별로 (인프라값 vs 현장값) 비교. 양쪽 모두 있고 다르면 ⚠ 알림. 한쪽만 있고 다른 쪽은 빈 칸인 경우도 알림 (방향 라벨 포함) |
| **FR-3** | **비교 대상 필드 — 1차/2차 분리** (codex 1차 권고 #2): <br>**【1차 sprint 범위】** 4 필드만 비교: `host_name` (신규 컬럼 — §7 B안) · `cpuSpec` · `memorySpec` · `diskSpec` <br>**【2차 후보】** 후속 sprint 에서 확장: `osNm` / `osDetail` / `networkSpec` / `powerSpec` / `serialNo` / `serverModel` <br>**【영구 제외】** `ipAddr` (매칭 키로만 사용) · `note` (자유 메모) · `acc_id/acc_pw` · MAC (보안) |
| **FR-3-1** | **매칭 키 & 정규화** — `(normalize(server_role), normalize(host_name))`. <br>**host_ip 미사용 이유 (codex 2차 R-12)**: `InspectionQrBatchService` L398 이 Snapshot 적재 시 `hostIp = null` (payload schema 에 IP 필드 없음) → `host_ip` 매칭은 현재 데이터로 항상 실패. host_name 은 동 L397 `hostName = tier.getH()` 로 정상 적재됨. <br>server_type "WEB" → server_role "ap" 정규화 (동 서비스 기존 패턴). host_name 정규화 = trim + 대소문자 무시 |
| **FR-4** | **알림 시점** — (a) 페이지 로드 직후 비교 → 차이 시 알림 표시 · (b) 저장 버튼 클릭 시 재비교 → 차이 있으면 한 번 더 알림(저장 차단 X, 사용자 dismiss 가능). **"현장값"의 기준** = Snapshot 값 단독 (DOM 입력값은 사용자 추가 입력 영역이고 비교 source 아님, R-10 참조) |
| **FR-5** | **알림 표현 — 인라인** — 차이가 있는 셀 옆에 `fa-triangle-exclamation` ⚠ 아이콘 + click popover (CSS only, 라이브러리 X): `인프라값: [X] / 현장값: [Y]` |
| **FR-6** | **알림 표현 — 상단 요약 박스** — `doc-inspect` 페이지 헤더(`.doc-header`) 바로 아래 dismissible 박스 `⚠ 인프라와 다른 필드 N개` + 접기/펼치기. 펼치면 필드명·인프라값·현장값 목록 |
| **FR-7** | **액션 없음** — 단순 알림. 인프라 DB 자동 업데이트 X. 저장 진행 차단 X. 인프라 페이지 바로가기 링크는 포함 가능 (선택) |
| **FR-8** | **dismiss 동작** — 사용자가 상단 박스를 닫을 수 있음 (현 세션·현 페이지 한정, state 저장 X). reload 또는 다른 점검내역서 진입 시 다시 표시. 인라인 ⚠ 아이콘은 dismiss 영향 X (FR-8 핵심) |
| **FR-9** | **필드명 한글 라벨** — 사용자에게는 한글 라벨로 표시 (예: `cpuSpec` → "CPU 사양", `host_name` → "호스트명") |
| **FR-10** | **정규화 비교** — 필드별 정규화 룰: <br>① IP/hostname/OS — 공백 trim + 대소문자 무시 <br>② **CPU** — 코어 수 일치 + name substring (Snapshot 의 `cores` ↔ InfraServer `cpuSpec` 안 텍스트) <br>③ **memory** — GB 단위 숫자 추출 후 **±5%** 허용 (제조사 표기/OS 가용 차이 흡수) <br>④ **disk** — summary text 부분매칭 (Snapshot `ap.os.disk_summary.summary` ↔ InfraServer `diskSpec`) |
| **FR-11** | **raw_payload allowlist** (codex 1차 권고 #6) — 비교/popover 노출 대상은 명시적 화이트리스트만 허용 (FR-3 1차 = 4 필드 / 2차 후보 = 6 필드). raw_payload 안의 예기치 않은 신규 필드는 자동 비교/표시 X. allowlist 정의 위치 = 비교 매핑 함수 1곳에 집중 (FR-12) |
| **FR-12** | **매핑/정규화 함수 격리** — raw_payload → 비교 가능 형태 변환 + 정규화 + allowlist 필터링은 **단일 함수**(JS 모듈) 로 집중. agent 버전별 fallback 처리도 동일 함수 내 (R-5 대응) |

## 5. NFR (Non-Functional Requirements)

| ID | 요건 |
|---|---|
| **NFR-1** | 페이지 로드 성능: 비교 로직 추가로 인한 **클라이언트 측 page load time** 증가 ≤ **200ms** (서버 N=1~5, FR-3 1차 4 필드 기준). 측정 방식 = DevTools Performance 패널 + page load 이벤트 기준. 신규 Snapshot API 호출 자체의 응답시간은 별도 **NFR-9 (P95 ≤ 300ms)** 에서 측정 (codex 2차 — 존재하지 않던 `NFR-9-2` 참조 정정) |
| **NFR-2** | **알림 UI 색은 design-system.css 토큰으로 정의 후 사용** — codex 1차 권고 #3. 디자인팀 결정의 hex 값(amber-100/700, amber-300, amber-950/200/800, --warning)들은 우선 design-system.css `:root` 에 `--alert-bg / --alert-fg / --alert-border / --alert-icon` 4 토큰으로 신설 → doc-inspect 의 알림 UI 는 `var(--alert-*)` 만 참조. raw hex 직접 사용 0건 |
| **NFR-3** | **WCAG AA 색 대비 통과** — 알림 배경/글씨/인라인 아이콘 모두 4.5:1 이상 (amber-100 #FEF3C7 + amber-700 #B45309 = 5.0:1 ⭕) |
| **NFR-4** | **라이트/다크 모드 일관성 — 기존 다크 대응 범위 내** (codex 1차 권고 #4 / AGENTS.md §7 Phase 4 정책 정합). 기존 doc-inspect 에 `[data-theme="dark"]` 가 적용된 영역에 한해 다크 변종 정의. 본 sprint 에서 Phase 4 다크모드 전면 리팩터 진입 X |
| **NFR-5** | **CSS specificity 충돌 없음** — 클래스 selector specificity 매트릭스 사전 점검 (2026-06-01 main-dashboard 사례 학습) |
| **NFR-6** | **:root 토큰 self-reference 없음** — doc-inspect.html 도 :root 토큰 정의 시 design-system.css SoT 와 동일 hex 값 (main-dashboard 사례 학습) |
| **NFR-7** | **기존 점검 흐름 회귀 0건** — 인프라 API 호출, 점검 입력, 저장 흐름 동일 |
| **NFR-8** | **권한** — 점검내역서 VIEW 권한 이상에서 표시 (인프라 API 와 동일 정책). 신규 Snapshot API 도 동일 VIEW 권한 (NONE 차단) |
| **NFR-9** | **신규 Snapshot API 명세** (codex 1차 권고 #4 / 2차 보강): <br>URL: `GET /document/api/inspect-snapshots?distNm=...&sysNmEn=...` <br>응답: 매칭 가능 형태 (server_role / host_name / cpu / memory / disk allowlist 필드만, R-8 대응. **host_ip 제외** — 적재값 null) <br>**최신 row 선택 기준 (codex 2차 R-14)**: 동일 `(프로젝트, server_role, host_name)` 에 `collected_at` 다중 row 존재 시 **`collected_at` 최신 1건** 반환 (점검월 무관, 가장 최근 수집값 기준). 서버측 정렬·dedup. <br>**빈 결과 vs 실패 UX 분기 (codex 2차 문제6)**: HTTP **200 + 빈 배열** = "현장 미수집"(T-12, 알림 표시) / HTTP **4xx·5xx** = silent skip + console.error(T-14, 알림 미표시). 둘은 명확히 다른 경로 <br>권한: VIEW 이상 (NFR-8) <br>응답 시간 목표: P95 ≤ **300ms** (서버 N≤5) |
| **NFR-10** | **마이그레이션 안전 정책** (codex 1차 권고 R-11) — `host_name` 컬럼 추가는 Flyway 마이그레이션 1개 (V0XX_add_host_name_to_infra_server.sql, ALTER ADD COLUMN NULL 허용). 메모리 [[machines]] 의 "회사 PC 만 운영 DB 마이그레이션" 룰 적용. 마이그레이션 실행 전 사용자 confirm 2단계 (echo + 진행) |

## 6. T (Test scenarios)

| ID | 시나리오 | 기대 |
|---|---|---|
| **T-1** | 인프라값 == 현장값 (모든 필드 일치) | 알림 없음 |
| **T-2** | 1개 필드 차이 (예: cpuSpec) | 상단 박스 "1개" + 해당 셀 ⚠ |
| **T-3** | 4개 필드 차이 (1차 비교범위 host_name·cpuSpec·memorySpec·diskSpec 전부) | 상단 박스 "4개" + 각 셀 ⚠ (codex 2차 — 1차 범위가 4필드라 기존 "5개" 는 재현 불가였음) |
| **T-4** | 인프라값 있고 현장값 빈 칸 | 알림 + 라벨 "현장 미수집" |
| **T-5** | 인프라값 빈 칸이고 현장값 있음 | 알림 + 라벨 "인프라 미등록" |
| **T-6** | 정규화 차이 (`192.168.1.1` vs ` 192.168.1.1 `) | 알림 없음 (NFR FR-10) |
| **T-7** | 페이지 로드 → 상단 박스 dismiss → 인라인 ⚠ 유지 확인 | OK |
| **T-8** | 저장 버튼 클릭 → 차이 있을 때 한 번 더 알림 → dismiss/저장 진행 | OK |
| **T-9** | 인프라 서버 N=0 (조회 결과 없음) | 알림 표시 없음 (현재 동작 유지) |
| **T-10** | 다크모드 토글 → 알림 색·대비 정상 | OK |
| **T-11** | 점검내역서 VIEW 권한 없음 (`NONE`) | 알림 표시 안 함 (현재 인프라 API 403 처리 + 신규 Snapshot API 403 처리 모두 동일) |
| **T-12** | **Snapshot N=0** (인프라 서버 있는데 Snapshot 자동 적재 없음) | 모든 인프라 필드 vs 빈 값 비교 → "현장 미수집" 라벨로 알림 |
| **T-13** | **매칭 실패** — 인프라 N=2, Snapshot N=2 중 1개만 매칭 (예: host_name mismatch) | 매칭된 1개는 정상 비교, 매칭 안 된 1개는 별도 라벨 "매칭 불가" + 인프라값만 표시 (R-7) |
| **T-14** | **Snapshot API 4xx/5xx 실패** | 알림 silent skip + console.error · 점검 작성 흐름 정상 (NFR-9 fallback, R-9) |
| **T-15** | **memory ±5% 허용** — 인프라 `128GB` vs Snapshot `127.5GB` (오차 0.4%) | 알림 없음 (FR-10 ③) |
| **T-16** | **CPU core mismatch** — 인프라 `Intel Xeon Gold 6248R, 24코어` vs Snapshot `cores=16` | 알림 표시 (코어 수 mismatch) |
| **T-17** | **disk 부분매칭** — 인프라 `2TB SSD x4 + 500GB NVMe` vs Snapshot summary `Used 1.2TB / 8TB` | 부분 매칭 룰 (FR-10 ④) 결과에 따라 ⭕/⚠ 판정 — 단위 테스트로 보장 |
| **T-18** | **raw_payload 안 예기치 않은 필드** — agent 신버전에서 `hw.gpu` 등 추가 | allowlist 외 필드는 비교/popover 노출 X (FR-11) |
| **T-19** | **마이그레이션 실행** — `V0XX_add_host_name_to_infra_server.sql` 운영 적용 | 기존 row host_name=NULL · ALTER 성공 · 회귀 0건 (NFR-10) |
| **T-20** | **최신 Snapshot 선택** (codex 2차 R-14) — 동일 host 에 `collected_at` 2건(구/신) 존재 | API 가 최신 1건만 반환 → 최신값 기준 비교 |
| **T-21** | **host_name 입력→매칭** (R-13) — 인프라 host_name 빈 상태 vs 입력 후 | 빈 상태=매칭 불가 라벨 / 입력 후=정상 매칭·비교 (입력 UI 동작 회귀 포함) |

## 7. 데이터 모델 영향 (B안 확정 — codex 1차 권고 #1)

### 7-1. 신규 컬럼 (1건)

| 위치 | 컬럼 | 타입 | nullable | 비고 |
|---|---|---|---|---|
| `tb_infra_server` | `host_name` | `VARCHAR(64)` | YES | 인프라 호스트명. NULL 허용 (기존 row 무영향). 비교 키 + 표시 |

### 7-2. 마이그레이션

- 파일: `swdept/sql/V0XX_add_host_name_to_infra_server.sql` (XX = 다음 가용 버전, 개발계획서에서 확정)
- 내용: `ALTER TABLE tb_infra_server ADD COLUMN host_name VARCHAR(64);` + index 검토 (개발계획서 단계)
- 위험도: 0 (NULL 허용, 기존 row 무영향, 회귀 0)
- 운영 실행 정책: 메모리 [[machines]] 의 "회사 PC 만 운영 DB" 룰 (NFR-10) + 2단계 confirm

### 7-3. 도메인/DTO/API 영향

- `InfraServer.java` — `@Column(name = "host_name") private String hostName;` 추가 + getter/setter
- `InfraServerDTO` / `InfraDTO` (해당하는 곳) — `hostName` 필드 노출
- `DocumentController.getInfraServers()` (L1707) — 응답 map 에 `hostName` 추가 → API 응답 13 필드 → **14 필드**
- 인프라 자산입력/수정 화면 (`infra-form.html` 등) — host_name 입력/수정 필드 추가 **= 본 sprint 필수 범위** (codex 2차 문제5 / R-13). <br>**근거**: host_name 이 (a) 매칭 키(FR-3-1)이자 (b) 비교 대상(FR-3)이므로, 입력 경로 없이는 인프라쪽 host_name 이 영구 NULL → 매칭·비교가 동작 불가. 따라서 `InfraServerDTO.hostName` 노출 + form 입력 + 저장 흐름까지 본 sprint 포함 (별도 sprint 분리 X)

### 7-4. 비교 source 모델 (변경 없음)

- `InspectMetricSnapshot` (`inspect_metric_snapshot`) — 그대로. `raw_payload` jsonb 안 allowlist 필드만 추출하는 신규 Snapshot API (NFR-9) 가 추가
- `InspectCheckResult` — 1차 sprint 제외 (DB팀 자문 §12-1 (3))

### 7-5. 비교 로직 위치

- **프론트엔드 JavaScript** (doc-inspect.html 내 매핑/정규화 단일 함수 — FR-12)
- 신규 Snapshot API 호출 + 인프라 API 호출 후 클라이언트 측 비교

## 8. UI / 디자인

- **인라인 알림**: FontAwesome `fa-triangle-exclamation` + `--warning` 토큰 색 + 툴팁 (CSS `:hover` + `[data-tooltip]` 또는 small popover)
- **상단 요약 박스**: design-system.css `--warning` 톤. 펼침/접힘 토글 (FontAwesome chevron). dismiss `×` 버튼
- **다크 변종**: 알림 박스 배경 어둡게(amber-800), 글씨 amber-200 / 인라인 아이콘 amber-400
- **디자인팀 자문 필요** — 시안 옵션 (a/b/c) 비교: 박스 위치 (표 위 vs 페이지 헤더 옆), 펼침 동작, 색 정도

## 9. DB팀 자문 필요 사항 (📋)

> **✅ 전 항목 §12-1 에서 결정 반영 완료** (codex 2차 권고 6 — 미해결처럼 읽히지 않도록 명시). 아래는 원 질문 + 결정 위치만 기록(이력 보존용). 1=§12-1(1)·5=§12-1(5)·2=§12-1(2)·3·4=§12-1(3)(4).

1. **`InspectMetricSnapshot.raw_payload` JSON schema** 확정 — agent-windows/agent-unix `snapshot.json` 의 정확한 구조 (`host.*`, `os.*`, `hw.cpu/memory/disk/network/power.*`)
2. **`hostname` 필드 위치** — InfraServer 에 hostname 컬럼 없음. agent 가 측정한 hostname 은 어디 매핑? (server_model 또는 별도 컬럼 신설?)
3. **사용자 수동 입력 form 필드** — doc-inspect 에서 점검 시 사용자가 직접 입력하는 필드(현장 측정 결과) 가 어디 저장되나? (`InspectCheckResult` ?) 비교 source 에 포함할지 결정
4. **비교 source 선택** — `InspectMetricSnapshot.raw_payload` 만 vs `InspectCheckResult` 도 포함 vs 둘 다
5. **다중 호스트(이중화/RAC) 처리** — `InfraServer` N개 ↔ `InspectMetricSnapshot` N개 매칭. 매칭 키 = `(host_name, server_role)` 또는 `(ip_addr, server_type)`?

## 10. 디자인팀 자문 필요 사항 (🎨)

1. **알림 박스 위치** — `doc-inspect` 페이지 헤더 바로 아래 vs 점검 표 바로 위 vs 사이드 패널 (3안 시안)
2. **인라인 ⚠ 아이콘 + 툴팁** — popover 라이브러리 사용 (e.g. FontAwesome built-in tooltip) vs 순수 CSS `:hover`
3. **색 강도** — `--warning` 그대로 vs 한 톤 부드럽게 (amber-100 배경 + amber-700 글씨, 2-a `--scope-std` 톤과 일관)
4. **dismiss UX** — 닫은 박스 영구 기억(localStorage) vs 세션 한정 vs 페이지 reload 시 다시 표시

## 11. 리스크

| ID | 리스크 | 대응 |
|---|---|---|
| **R-1** | 비교 로직 성능 — 다중 서버 + 큰 raw_payload | NFR-1 / NFR-9 측정. 임계 초과 시 비교를 백엔드로 이관 |
| **R-2** | 알림 노이즈 — 모든 차이 표시 시 부담 | 단계적 도입: 1차 sprint = 4 필드 (FR-3) → 사용 후 2차 확장 |
| **R-3** | False positive (정규화 차이) | FR-10 정규화 룰 (필드별). 단위 테스트 = T-6 / T-15 / T-16 / T-17 |
| **R-4** | 인프라 DB null vs 현장값 있음 — 어느쪽이 진실? | T-5 시나리오 라벨 "인프라 미등록" 명시. 사용자가 판단 |
| **R-5** | raw_payload schema 변경 시 깨짐 (agent 버전 차이) | 매핑 함수 단일 격리 (FR-12) + agent 버전별 fallback 정책 명시. 신규 필드는 allowlist 외이면 silent skip (T-18) |
| **R-6** | dismiss 정책 — codex 1차 권고 #3 통일 | **state 저장 X, reload 마다 재표시** (FR-8 확정). 인라인 ⚠ 는 dismiss 영향 X |
| **R-7** | **매칭 실패** — 인프라 N개, Snapshot N개 중 일부만 매칭 (host_name 불일치 등) | 매칭된 호스트는 정상 비교 · 매칭 안 된 인프라 row 는 별도 라벨 "매칭 불가" + 인프라값만 표시 (T-13) |
| **R-8** | **민감정보 노출** — raw_payload 안 예기치 않은 민감값이 popover 에 노출 | **allowlist 강제** (FR-11). 신규 Snapshot API 응답도 서버측에서 allowlist 필터링 (NFR-9) |
| **R-9** | **Snapshot API 실패** — 4xx/5xx 시 점검 작성 흐름 깨질 위험 | NFR-9 fallback (silent skip + console.error). T-14 검증 |
| **R-10** | **저장 재비교 타이밍** — 사용자가 입력 중인 DOM 값과 Snapshot 값 중 무엇이 "현장값"? | FR-4 확정: "현장값" = **Snapshot 값 단독**. DOM 입력값은 사용자 추가 입력 영역이고 비교 source 아님 |
| **R-11** | **마이그레이션 confirm** — `host_name` ALTER 시 운영 DB 영향 | NFR-10 (회사 PC 만 + 2단계 confirm). 위험도 자체는 0 (NULL 허용 ALTER) 이지만 절차 준수 |
| **R-12** | **host_ip 매칭 불능** (codex 2차) — `InspectionQrBatchService` L398 이 `hostIp=null` 로 적재 → `(server_role, host_ip)` 매칭 항상 실패 | 1차 매칭 키를 `(server_role, host_name)` 으로 확정 (FR-1/FR-3-1). host_ip 수집은 향후 agent payload 확장 시 별도 검토 |
| **R-13** | **host_name 미입력 누적** (codex 2차) — 신규 컬럼 NULL 허용이라 입력 UI 없으면 인프라 host_name 이 계속 비어 매칭·비교 품질 저하 | host_name 입력/수정 UI 를 본 sprint 필수 범위로 승격 (§7-3). 기존 row 는 점검 시 사용자가 인프라 페이지에서 채우도록 유도(본 기능의 알림이 그 트리거) |
| **R-14** | **최신 Snapshot 선택 기준 모호** (codex 2차) — 동일 host 에 `collected_at` 다중 row 존재 시 어느 값 비교? | NFR-9 확정: `collected_at` **최신 1건** (서버측 정렬·dedup). T-20 검증 |
| **R-15** | **raw_payload 구조 오해** (codex 2차) — `raw_payload` 는 tier 직렬화이고 실제 값은 `items` 배열 `[key, label, value]` 안에 있음 (코드 L401-404). API 명세의 `ap.hw.cpu.name` 같은 표기를 직접 path 로 오해하면 구현 오매핑 | §12-1 (1) 에 items 배열 구조 명시. 매핑 함수(FR-12) 가 key=`ap.hw.cpu` 로 lookup 후 value 추출하도록 구현 명세 고정 |

## 12. 자문 결과

### 12-1. 📋 DB팀 자문 결과 (2026-06-01, v1.1)

**(1) `raw_payload` JSON schema 매핑 (확정)**

키 패턴 = `<role>.<category>.<field>` · role ∈ {`ap`, `db`} · category ∈ {`hw`, `os`, `perf`, `cable`}. `InspectionQrBatchService` L193-232 + L621-630 기반:

| InfraServer 필드 | AP 서버 (server_type=WEB) | DB 서버 (server_type=DB) | 비교 방법 |
|---|---|---|---|
| `cpuSpec` | `ap.hw.cpu.name` + `cores` | `db.os.cpu_info.cores` + `clock_ghz` | 텍스트 부분 매칭 (코어 수 일치 + name substring) |
| `memorySpec` | `ap.hw.memory.installed_gb` | `db.os.mem_info.total_gb` | GB 단위 숫자 추출 후 ±5% 허용 |
| `diskSpec` | `ap.os.disk_summary.summary` | `db.os.disk` (mounts 배열) | summary text 추출 후 부분 매칭 |
| `networkSpec` | `ap.hw.adapter.{up,total}` | `db.os.adapter.{count}` | 어댑터 개수 일치 |
| `osNm` / `osDetail` | (raw_payload OS 구획 — 별도 정의 필요) | 동일 | 텍스트 정규화 후 매칭 |

→ **권고 (v1.3 갱신 — codex 2차 문제3 통일)**: 1차 도입 = `host_name` + `cpuSpec` / `memorySpec` / `diskSpec` **4 필드** (B안 host_name 컬럼 확정으로 FR-3 과 일치시킴. 기존 "3 필드" 표기는 host_name 컬럼 확정 전 문구라 폐기). `networkSpec` / `osNm` / `osDetail` 등은 2차 확장.

**⚠ raw_payload 실제 구조 (codex 2차 R-15)**: 위 표의 `ap.hw.cpu.name` 등은 *논리적 위치 표기*일 뿐, 실제 `raw_payload` 는 tier 직렬화이며 값은 `tier.items` = `List<[key, label, value]>` 안에 있음 (`InspectionQrBatchService` L401-404). 즉 매핑 함수(FR-12)는 `key == "ap.hw.cpu"` 인 metric 을 찾아 `value`(index 2)를 추출하는 방식으로 구현해야 함. 직접 객체 path 접근 아님.

**(2) `hostname` 컬럼 위치 (결정)**

- 현재 `InfraServer` 에 hostname 컬럼 없음 (`server_model` 에 통합 X — 안티패턴)
- → **신규 컬럼 추가** 권고: `tb_infra_server.host_name VARCHAR(64)`
- Migration: `swdept/sql/V0XX_add_host_name_to_infra_server.sql` — 단순 ALTER, 위험 0, NULL 허용 (기존 row 무영향)
- 비교 대상: `host_name ↔ InspectMetricSnapshot.host_name`

**(3) 사용자 수동 입력 form 필드 (제외)**

- `InspectCheckResult` = 점검 cell 결과 (체크리스트 통과/실패/측정값) — H/W spec 비교 source 아님
- 1차 도입은 본 source 에서 **제외**. 향후 확장 시 별도 sprint

**(4) 비교 source 선택 (단일)**

- 1차: `InspectMetricSnapshot.raw_payload` **단독** (자동 적재값)
- 2차 확장 시 사용자 form 입력값 검토 — 별도 sprint

**(5) 다중 호스트 매칭 키 (확정)**

- 매칭 키: `(normalize(server_role), normalize(host_name))` · server_type "WEB" → "ap" 정규화 (`InspectionQrBatchService` 가 이미 처리)
- **host_ip 미사용 (codex 2차 R-12)**: 적재 코드 L398 `hostIp=null` 이라 host_ip 매칭은 항상 실패. host_name(L397 `tier.getH()`) 기반으로 확정
- `(server_role, host_name)` 조합으로 이중화/RAC 케이스 구분 (동일 role 에 host_name 다름)

### 12-2. 🎨 디자인팀 자문 결과 (2026-06-01, v1.1)

**(1) 알림 박스 위치 → (a) 페이지 헤더 바로 아래**

- doc-inspect 의 `.doc-header` 다음 줄에 배치
- 시선 흐름 자연 (위→아래) · 점검 표와 충돌 X · dismiss 시 표가 위로 올라옴
- 보류 (b) 표 바로 위 / (c) 사이드 패널 — 점검 표 가 큰 화면이라 위치 변동 시 사용자 혼란

**(2) 인라인 ⚠ + 툴팁 → (d) click popover (CSS only)**

- 의존성 0 (라이브러리 X) · 모바일/태블릿 자연 (점검은 태블릿 사용 빈도 큼)
- 구현: `<span class="diff-warn" tabindex="0" data-infra="X" data-field="Y">⚠</span>` + CSS `:focus ~ .popover { display:block }` 패턴
- 보류 (a) hover only / (b) Bootstrap / (c) Tippy.js

**(3) 색 강도 → (c) 혼합**

- **상단 박스**: amber-100 (#FEF3C7) 배경 + amber-700 (#B45309 = `--scope-std`) 글씨 + 1px border amber-300. 2-a 스코프 배지 톤과 일관, 부드러움
- **인라인 ⚠ 아이콘**: `--warning` (#D97706) 강조, 셀에서 즉시 인지
- 신규 토큰 검토: `--alert-bg` (#FEF3C7) / `--alert-fg` (#B45309) — 또는 `--scope-std` 활용

**(4) dismiss UX → (c) reload 마다 다시 표시**

- 상단 박스 ✕ 닫기 시 = 현 세션·현 페이지 한정 dismiss (state 저장 X)
- reload 또는 다른 점검내역서 진입 시 다시 표시
- 사용자 행동(인프라 수정) 유도가 목적이라 영구 dismiss 위험. 인라인 ⚠ 는 FR-8 따라 유지

**(5) 다크모드 변종**

- 상단 박스: bg `#451A03` (amber-950) + fg `#FDE68A` (amber-200) + border `#92400E` (amber-800)
- 인라인 ⚠: `--warning` 그대로 (다크 surface 위 충분히 보임)

### 12-3. codex 1차 검토 결과 (2026-06-01, v1.1 → v1.2)

- **판정**: ⚠ 수정필요 (토큰 11,667 / 모델 gpt-5.5, JSONL 기록)
- **핵심 충돌**: §7 (신규 컬럼 없음) ↔ §12-1 (host_name 컬럼 추가 권고) — **B안 (컬럼 추가) 확정 (사용자 결정 2026-06-01)**
- **반영 권고 8건** (v1.2 모두 반영):
  1. FR-3 = 1차 (4 필드) / 2차 (6 필드) 분리 → FR-3 갱신 ✅
  2. R-6 dismiss 정책 통일 (state 저장 X, reload 시 재표시) → FR-8, R-6 갱신 ✅
  3. NFR-2 색은 토큰화 후 사용 (alert-bg/fg/border/icon 토큰 신설) → NFR-2 갱신 ✅
  4. NFR-4 Phase 4 다크모드 정책 정합 (기존 다크 대응 범위 내) → NFR-4 갱신 ✅
  5. Snapshot API URL/권한/실패 fallback 명시 → NFR-9 신규 ✅
  6. T 추가 (Snapshot N=0, 매칭 실패, API 4xx/5xx, memory ±5%, CPU core mismatch, disk 부분매칭) → T-12~T-19 신규 ✅
  7. raw_payload allowlist → FR-11 신규 + R-8 갱신 ✅
  8. (B안 결정 부속) §7 호스트네임 컬럼 + 마이그레이션 + 도메인/API 영향 명시 → §7 전면 재작성 ✅
- **누락 리스크 5건 추가**: R-7 (매칭 실패) / R-8 (민감정보 노출) / R-9 (API 실패) / R-10 (저장 재비교 타이밍) / R-11 (마이그레이션 confirm) ✅

### 12-4. codex 2차 검토 결과 (2026-06-01, v1.2 → v1.3)

- **판정**: ⚠ 수정필요 (집 세션 재실행, `codex exec -s read-only`. 원문 = `docs/product-specs/reviews/inspect-infra-diff-codex-2nd.md`)
- **B안 (host_name 컬럼) 방향 자체는 일관 반영 확인**. 단 매칭 키·1차 범위에 실행 불가능한 모순 잔존 → v1.3 에서 정정:
  1. **매칭 키 `host_ip`→`host_name`** — 코드 L398 `hostIp=null` 검증 완료. FR-1/FR-3-1/§12-1(5)/T-13/R-7 갱신 + R-12 신설 ✅
  2. **T-3 "5개"→"4개"** — 1차 비교범위 4필드라 5개 차이는 재현 불가 ✅
  3. **1차 범위 4/3 불일치 통일** — §12-1(1) "3 필드" → host_name 포함 "4 필드" (FR-3 과 일치) ✅
  4. **NFR-1 의 존재하지 않는 `NFR-9-2` 참조 정정** → NFR-9 ✅
  5. **host_name 입력/수정 UI 본 sprint 필수 승격** — §7-3, R-13 ✅ (매칭 키이자 비교 대상이라 입력 경로 필수)
  6. **Snapshot N=0(빈 배열) vs API 실패 UX 분기 명시** — NFR-9 ✅
- **신규 리스크 R-12~R-15** (host_ip 매칭불능 / host_name 미입력 누적 / 최신 Snapshot 선택 기준 / raw_payload items 구조 오해) 추가 ✅
- **§9 자문필요 → 결정완료 명시** (codex 권고 6) ✅
- T-20(최신 Snapshot 선택) / T-21(host_name 입력→매칭) 신규 ✅
- **다음 단계**: 사용자 최종 검토·승인 → 개발계획서 (codex 검토)

## 13. 변경 이력

- **2026-06-01 v1 초안** (사무실 세션) — 사전 조사 (InfraServer / InspectMetricSnapshot / DocumentController.getInfraServers) 기반 작성. DB팀 자문에 raw_payload schema 매핑 위임. UI 시안은 디자인팀 자문에 위임.
- **2026-06-01 v1.1** — DB팀·디자인팀 자문 결과 반영. raw_payload 키 패턴 확정 (`InspectionQrBatchService` L193-232 기반). hostname 컬럼 신설 권고. 비교 source 단일화 (Snapshot only, 1차). UI 결정 4건 확정 + 신규 토큰 검토 항목 추가. 다음 단계 = codex 기획서 검토.
- **2026-06-01 v1.2** — codex 1차 검토 (⚠ 수정필요, 토큰 11,667) 권고 8건 + 누락 리스크 5건 반영. **B안 (host_name 컬럼 신설) 사용자 확정**. FR 10개 → 12개 (+ FR-3-1) / NFR 8개 → 10개 / T 11개 → 19개 / R 6개 → 11개. §7 데이터 모델 영향 전면 재작성 (B안 + 마이그레이션 + 도메인/API 영향). 다음 단계 = codex 2차 검토 → 사용자 최종승인.
- **2026-06-01 v1.3** (집 세션) — codex 2차 검토 (⚠ 수정필요, 집에서 재실행) 반영. **매칭 키 `host_ip`→`host_name` 정정** (코드 L398 `hostIp=null` 검증). 1차 비교범위 4필드로 통일 (FR-3 ↔ §12-1). T-3 "5개"→"4개", NFR-1 의 `NFR-9-2` 오참조 정정. **host_name 입력/수정 UI 본 sprint 필수 승격**. Snapshot 최신 row 선택 기준·빈배열 vs 실패 UX 명시. raw_payload `items` 배열 구조 명시. 신규 리스크 R-12~R-15, 신규 T-20·T-21. §9 결정완료 명시. codex 2차 원문 = `docs/product-specs/reviews/inspect-infra-diff-codex-2nd.md`. **다음 단계 = 사용자 최종 검토·승인 → 개발계획서**.
