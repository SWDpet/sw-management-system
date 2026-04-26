---
tags: [plan, sprint, feature, monitoring, ui, sse]
sprint: "team-monitor-dashboard"
status: draft-v2
created: "2026-04-26"
revised: "2026-04-26"
---

# [기획서] 가상 팀 진행율 모니터링 — 웹 대시보드 (옵션 C)

- **작성팀**: 기획팀
- **작성일**: 2026-04-26 (v1) / 2026-04-26 (v2 보강)
- **선행 커밋**: `cb4e617` (팀 진행율 모니터링 대시보드 추가 — 터미널판)
- **근거**: 사용자 요청 (모니터링 옵션 4종 비교 후 옵션 C 채택, 효율성·가독성 우위)
- **상태**: 초안 v2 (codex 재검토 ⭕ 통과 + 경미 보완 3건 즉시 반영 완료) — v1 검토 ⚠ 수정필요 권고 7건 전부 반영
- **v2 변경점 요약**:
  - FR-5 보강 (timeline.size 키, UI 위치, 계산 주체, 페이로드 스키마)
  - NFR-2 LRU 퇴출 정책 본문화 + 상한 초과 응답 코드 / UI 메시지
  - NFR-4 `application.properties` 키 표 정식 반영 (기본값 포함)
  - C-1 CSP 헤더 추가
  - C-2 SSE `Cache-Control: no-cache, no-transform` + gzip 제외 + `Content-Type` charset 명시
  - C-3 서버 ISO 시각 전달 + 클라이언트 로컬 보정
  - 출시 전 필수 항목 (SseEmitter 헤더 고정, 보안 헤더, 상한 시나리오, 헬스 JSON 샘플, set-status.sh atomic write POSIX 호환 주석) 명세화
  - **codex v2 재검토 후 즉시 반영 (경미 보완 3건)**: FR-3 페이로드 예시 ISO-8601 정합화, HSTS 운영 프로파일 한정 적용 (§4-6), SSE 응답 `Vary: Accept, Origin` 으로 확장 (§4-7)

---

## 1. 배경 / 목표

### 배경 — 터미널판(`monitor.sh`)의 한계

기존 `.team-workspace/monitor.sh` (커밋 `cb4e617`) 는 4팀(planner/db/developer/codex) 상태를 5초 폴링으로 ANSI 렌더하지만 다음 한계가 있다:

1. **한글·이모지 정렬 깨짐** — 터미널 monospace 강제로 🛠️/🗄️ 폭 계산 오류 빈발
2. **폴링 방식** — `sleep 5` 기반, 실제 변경 시점과 표시 시점에 최대 5초 지연
3. **단일 터미널 점유** — 듀얼 모니터 활용 어려움. 작업 터미널과 모니터 터미널이 분리 안 됨
4. **시각화 한계** — ASCII 진행바 외 차트·타임라인·색상 표현 제약
5. **이력 부재** — 현재 상태만 보이고 변경 추이를 볼 수 없음

### 목표

1. 기존 `monitor.sh` 와 **공존**하는 별도 모니터링 채널로 `/admin/team-monitor` 웹 대시보드 추가
2. **SSE(Server-Sent Events)** 기반 실시간 push (폴링 제거)
3. **한글 가독성** 우위 — Pretendard(한글) + JetBrains Mono(코드/숫자) 분리 적용
4. **듀얼 모니터 풀스크린** 지원 (`?fullscreen=1`)
5. 추후 시스템 헬스·로그·codex 큐 등을 같은 페이지에 통합할 수 있는 확장 토대

### 비목표 (이 스프린트에서 다루지 않음)

- 모바일 반응형 (별도 스프린트)
- 알림(소리·푸시) (별도 스프린트)
- 인증 강화 — 기존 `hasRole('ADMIN')` 권한 그대로 사용
- DB 스키마 변경 — 본 기획은 **DB 변경 없음**

---

## 2. 기능 요건 (FR)

### FR-1. 페이지 라우팅
- **경로**: `GET /admin/team-monitor`
- **권한**: `hasRole('ADMIN')`
- **응답**: Thymeleaf 템플릿 `templates/admin/team-monitor.html`

### FR-2. 4팀 카드 렌더
각 팀(planner/db/developer/codex) 카드에 다음 정보 표시:
- 팀명 (한글 라벨 + 이모지: 🧭 PLANNER / 🗄️ DB / 🛠️ DEVELOPER / 🤖 CODEX)
- 상태 (대기/진행중/완료/오류) — 색상·아이콘 구분
- 진행률 (0~100%) — CSS 진행바 + 숫자 동시 표기
- 작업명 (`task` 필드)
- 최종 갱신 시각 — "n초/분/시간 전" 상대 표기

### FR-3. SSE 실시간 push
- **엔드포인트**: `GET /admin/team-monitor/stream` (`Accept: text/event-stream`)
- **이벤트 종류**:
  - `event: snapshot` — 연결 직후 1회. 4팀 전체 상태 JSON
  - `event: update` — 특정 팀 상태 변경 시. 변경된 팀만 JSON
  - `event: ping` — 30초 주기 heartbeat (빈 데이터)
- **트리거**: `.team-workspace/status/*.status` 파일 변경 감지 시 즉시
- **페이로드 예시** (전체 스키마는 §FR-5-d, 시각 형식은 §4-8 참조 — ISO-8601 with offset):
  ```json
  {"team":"planner","state":"진행중","progress":40,"task":"기획서 작성","updatedAt":"2026-04-26T08:43:16+09:00"}
  ```

### FR-4. 진행률·상태 시각화
- 진행바: CSS 그라데이션 (대기=회색, 진행중=노랑, 완료=초록, 오류=빨강)
- 상태 변경 시 0.3초 fade-in 애니메이션
- 진행률 변경 시 0.5초 width transition

### FR-5. 작업 이력 타임라인 (v2 보강)

#### 5-a. 구성 키 (단일 진실원)
- **`teammonitor.timeline.size`** — 타임라인 보관 항목 수 (정수, 기본 `20`, 허용 `1~200`)
- 동일 값이 **서버 ring buffer 크기**와 **`event: snapshot` 페이로드 동봉 이력 개수**를 동시 결정. 클라이언트 별도 설정 없음.

#### 5-b. UI 위치 (고정)
- **데스크탑(기본)**: 4팀 카드 그리드 **하단** 단일 섹션 `<section id="timeline">`. 4팀 통합 단일 리스트(팀 컬러칩으로 구분).
- **풀스크린(`?fullscreen=1`)**: 카드 그리드 우선. 타임라인은 화면 하단 `position: sticky; bottom:0` 으로 1줄(최근 1건)만 노출. 클릭 시 expand.
- 카드 단위 sub-timeline 은 비범위 (혼란 방지).

#### 5-c. 상태 변화 계산 주체 (Service)
- **이전 상태(prevState) 산출은 서버 `TeamMonitorService` 책임**. 파일 watch 이벤트 수신 시 메모리 캐시의 직전 스냅샷과 비교하여 `prevState/prevProgress` 를 계산하고 `update` 페이로드에 포함.
- 클라이언트는 단순 렌더만 수행 (DOM 비교/diff 금지). 새로고침·재연결 시 `snapshot` 으로 일관성 회복.

#### 5-d. 페이로드 스키마 (확정)

`event: snapshot` (연결 직후 1회):
```json
{
  "schemaVersion": 1,
  "serverTime": "2026-04-26T08:43:16+09:00",
  "teams": [
    {"team":"planner","state":"진행중","progress":75,"task":"...","updatedAt":"2026-04-26T08:43:16+09:00"}
  ],
  "timeline": [
    {
      "id": "evt_01HXXXX...",
      "team": "planner",
      "prevState": "대기",
      "newState": "진행중",
      "prevProgress": 0,
      "newProgress": 75,
      "task": "v2 작성 중",
      "occurredAt": "2026-04-26T08:43:16+09:00"
    }
  ]
}
```

`event: update` (단일 팀 변경 시):
```json
{
  "schemaVersion": 1,
  "serverTime": "2026-04-26T08:43:16+09:00",
  "team": {"team":"planner","state":"진행중","progress":75,"task":"...","updatedAt":"2026-04-26T08:43:16+09:00"},
  "timelineEntry": {
    "id":"evt_01HXXXX...","team":"planner",
    "prevState":"대기","newState":"진행중",
    "prevProgress":0,"newProgress":75,
    "task":"v2 작성 중","occurredAt":"2026-04-26T08:43:16+09:00"
  }
}
```

`event: ping` — `data: {"serverTime":"...ISO..."}` (heartbeat에도 시각 동봉, C-3 보정 활용)

- 모든 시각 필드는 **ISO-8601 with offset** 문자열 (Asia/Seoul `+09:00` 기본). epoch seconds 는 비포함 (C-3 항).
- `id` 는 ULID. 클라이언트 dedupe 용. ring buffer 가 같은 항목을 재방출해도 안전.

### FR-6. 듀얼 모니터 풀스크린 모드
- `GET /admin/team-monitor?fullscreen=1` — 헤더·사이드바 숨김, 카드만 풀화면 그리드
- 카드 크기 자동 확대 (1080p 모니터 기준 2x2 그리드)

---

## 3. 비기능 요건 (NFR)

### NFR-1. 폰트 / 디자인
- 한글: `Pretendard` (이미 `static/css/` 에 적용됨)
- 코드·숫자·진행률: `JetBrains Mono` (없으면 `Fira Code` → `Consolas` fallback)
- 다크모드 기본 (단독 페이지 단위 적용. 전역 다크모드 Phase 4 미진입과 무관)
- `docs/DESIGN.md` 디자인 토큰 재사용

### NFR-2. 자원 (v2 보강 — LRU 퇴출 정책 + 상한 초과 응답)

#### 2-a. SSE 동시 연결 상한
- 기본값 `teammonitor.sse.max-emitters=20`.
- **LRU 퇴출 정책 (확정)**:
  - 연결 시점 + `lastEventAt` (마지막으로 데이터/heartbeat 송신 성공한 시각) 을 emitter 메타에 기록.
  - 상한 도달 상태에서 **신규 연결 요청** 발생 시:
    1. 기본은 **신규 연결을 거절** (503) — 기존 사용자 세션 보호.
    2. `teammonitor.sse.overflow-policy=reject` (기본) | `evict-oldest` (옵션) 로 운영자가 전환 가능.
    3. `evict-oldest` 모드에서는 `lastEventAt` 이 가장 오래된 emitter 1개를 `complete()` 후 신규 수락.
  - **퇴출 대상 통지**: 강제 종료 직전 `event: evicted` (`data: {"reason":"capacity"}`) 1회 송신 → 클라이언트는 UI 배지 "다른 세션이 자리를 차지하여 연결이 종료되었습니다. 새로고침 후 재연결" 노출 후 자동 재연결 중단.

#### 2-b. 상한 초과 시 응답 (REST 측)
| 상황 | HTTP | 본문(JSON) | UI 메시지 |
|------|------|-----------|-----------|
| `reject` 모드 + 정원 초과 | **503 Service Unavailable** + `Retry-After: 30` | `{"error":"sse_capacity","maxEmitters":20,"retryAfterSec":30}` | "동시 접속 한도(20)에 도달했습니다. 30초 후 자동 재시도합니다." |
| 동일 사용자 burst 재연결 (rate limit) | **429 Too Many Requests** + `Retry-After: 5` | `{"error":"sse_rate_limited","retryAfterSec":5}` | "재연결이 너무 잦습니다. 잠시 후 다시 시도합니다." |
| 인증 실패 / 세션 만료 | 401 | `{"error":"unauthenticated"}` | 로그인 페이지 redirect |
| 권한 부족 (비-ADMIN) | 403 | `{"error":"forbidden"}` | "관리자 권한이 필요합니다." |

- 클라이언트는 503/429 수신 시 `Retry-After` 헤더값을 우선 사용. 미존재 시 exponential backoff (3 → 10 → 30 → 60s).

#### 2-c. 기타 자원
- 파일 watcher: 단일 인스턴스 공유 (모든 emitter 가 같은 watcher 구독).
- 메모리: emitter 1개당 < 10KB. 이력 ring buffer (`teammonitor.timeline.size=20` 기준) 포함 총 < 50KB.

### NFR-3. 호환성
- 기존 `set-status.sh` 인터페이스 변경 없음 — 호환 100%
- 기존 `monitor.sh` 와 동시 사용 가능 (status 파일을 양쪽이 read-only 로 공유)

### NFR-4. 운영 (v2 보강 — 설정 키 표 정식 반영)

#### 4-a. Actuator
- `/actuator/health/teamMonitor` 커스텀 indicator (상세 JSON 샘플은 §4-7).
- `/actuator/info` 에 `teammonitor.timeline.size`, `teammonitor.sse.max-emitters`, `teammonitor.watcher.mode` 노출 (운영자 디버깅용).

#### 4-b. 설정 키 표 (정식)

| 키 | 타입 | 기본값 | 허용범위 / 값 | 의미 |
|----|------|-------|---------------|------|
| `teammonitor.enabled` | boolean | `true` | true/false | 모듈 전체 on/off. false 시 컨트롤러·watcher 미등록 |
| `teammonitor.status-dir` | path | `.team-workspace/status` | 디렉토리 경로 | status 파일 루트 |
| `teammonitor.watcher.mode` | enum | `auto` | `auto` \| `nio` \| `polling` | watcher 구현 선택 |
| `teammonitor.watcher.polling-interval-ms` | int | `1000` | `200~10000` | polling 모드 간격 |
| `teammonitor.watcher.restart-interval-ms` | int | `30000` | `5000~600000` | watcher 다운 시 재시작 간격 |
| `teammonitor.sse.max-emitters` | int | `20` | `1~200` | 동시 SSE 연결 상한 |
| `teammonitor.sse.overflow-policy` | enum | `reject` | `reject` \| `evict-oldest` | NFR-2-a 정책 |
| `teammonitor.sse.heartbeat-interval-ms` | int | `30000` | `5000~120000` | `event: ping` 주기 |
| `teammonitor.sse.timeout-ms` | long | `0` | `0` (무제한) 또는 `≥60000` | 개별 emitter 만료 시간 |
| `teammonitor.sse.rate-limit-per-min` | int | `10` | `1~60` | 동일 Principal 분당 재연결 한도 (NFR-2-b 429) |
| `teammonitor.timeline.size` | int | `20` | `1~200` | 타임라인 ring buffer 크기 (FR-5-a) |
| `teammonitor.security.csp` | string | (§4-6 기본값) | CSP 문자열 | C-1 CSP 헤더 |
| `teammonitor.security.allowed-origins` | csv | `(empty=동일출처만)` | URL csv | SSE Origin 화이트리스트 (R-5) |

- `application.properties` 신설 섹션 머리주석에 본 표를 그대로 복제 (운영자 1차 조회 지점).

---

## 4. 데이터 / 설계

### 4-1. 데이터 소스 (변경 없음)
- SSoT: `.team-workspace/status/{planner,db,developer,codex}.status` (key=value)
- 쓰기: `set-status.sh` (기존)
- 읽기: 신규 `TeamStatusReader` (Spring Bean)

### 4-2. 신규 클래스
| 클래스 | 역할 |
|--------|------|
| `TeamMonitorController` | `/admin/team-monitor` 페이지 + `/stream` SSE 엔드포인트 |
| `TeamStatusReader` | status 파일 파싱 → `TeamStatus` DTO |
| `TeamStatusWatcher` (interface) | 변경 감지 추상화 |
| `JavaNioWatcher` | `WatchService` 구현 (기본) |
| `PollingWatcher` | 1초 폴링 fallback |
| `TeamMonitorService` | watcher 구독 + emitter 관리 + 이력 ring buffer |
| `TeamMonitorHealthIndicator` | Actuator health |
| `TeamStatus` (DTO) | `{team,state,progress,task,updatedAt}` |
| `TeamStatusEvent` (DTO) | snapshot/update 페이로드 |

### 4-3. 신규 템플릿·정적 파일
| 경로 | 역할 |
|------|------|
| `templates/admin/team-monitor.html` | 메인 페이지 (Thymeleaf) |
| `static/css/team-monitor.css` | 카드·진행바·타임라인 스타일 |
| `static/js/team-monitor.js` | EventSource 연결·재연결·DOM 업데이트 |

### 4-4. 설정 (application.properties) — NFR-4-b 표와 1:1 일치

```properties
# === team-monitor — 운영자 1차 조회 지점 (정식 키 표는 NFR-4-b 참조) ===
teammonitor.enabled=true
teammonitor.status-dir=.team-workspace/status

# watcher
teammonitor.watcher.mode=auto                       # auto|nio|polling
teammonitor.watcher.polling-interval-ms=1000        # 200~10000
teammonitor.watcher.restart-interval-ms=30000       # 5000~600000

# SSE
teammonitor.sse.max-emitters=20                     # 1~200
teammonitor.sse.overflow-policy=reject              # reject|evict-oldest
teammonitor.sse.heartbeat-interval-ms=30000         # 5000~120000
teammonitor.sse.timeout-ms=0                        # 0(무제한) 또는 ≥60000
teammonitor.sse.rate-limit-per-min=10               # 1~60

# timeline
teammonitor.timeline.size=20                        # 1~200 (FR-5-a)

# security
teammonitor.security.csp=                           # 빈값=기본 CSP 사용 (§4-6)
teammonitor.security.allowed-origins=               # csv. 빈값=동일출처만 (R-5)
```

- `history.size` 키는 **삭제** — `timeline.size` 로 통일 (FR-5-a). 호환 별칭 미제공 (본 스프린트 신규 도입이라 사용처 없음).

### 4-5. DB
- **변경 없음** — 본 스프린트는 파일 기반 SSoT 유지

### 4-6. 보안 헤더 (C-1 CSP + 출시 전 필수 보안 헤더)

`/admin/team-monitor` 페이지 + `/admin/team-monitor/stream` 응답 모두에 다음 헤더를 강제 적용 (`TeamMonitorSecurityHeadersFilter`):

| 헤더 | 값 | 비고 |
|------|----|------|
| `Content-Security-Policy` | `default-src 'self'; script-src 'self'; style-src 'self'; font-src 'self' data:; connect-src 'self'; img-src 'self' data:; frame-ancestors 'none'; base-uri 'self'; form-action 'self'` | C-1. 인라인 스크립트·외부 CDN 금지 |
| `X-Content-Type-Options` | `nosniff` | MIME sniffing 차단 |
| `X-Frame-Options` | `DENY` | clickjacking (CSP frame-ancestors 보강) |
| `Referrer-Policy` | `same-origin` | 외부 referer 누출 방지 |
| `Permissions-Policy` | `microphone=(), camera=(), geolocation=()` | 불필요 권한 완전 차단 |
| `Cross-Origin-Opener-Policy` | `same-origin` | 윈도우 격리 |
| `Cross-Origin-Resource-Policy` | `same-origin` | 리소스 cross-origin 사용 차단 |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | **운영(`prod`) 프로파일에서만 적용**. 개발/로컬은 비활성 (Spring profile 분기) |

- 운영자 override: `teammonitor.security.csp` 로 CSP 문자열 교체 가능. 빈 문자열은 거부 (기본값 강제 적용).
- HSTS 는 HTTPS 전제이므로 dev/local 프로파일에서 발급되면 브라우저 캐싱으로 디버깅이 어려워짐 → 반드시 `@Profile("prod")` 또는 동등한 가드.
- 외부 폰트 사용 시 (JetBrains Mono CDN 등) `font-src` 에 호스트 명시 필요 — **본 스프린트는 로컬 번들만 사용** 하므로 기본 CSP 그대로 통과.

### 4-7. SSE 응답 헤더 고정 (C-2 + 출시 전 필수)

`SseEmitter` 생성 직후 컨트롤러에서 다음 헤더를 명시 set:

| 헤더 | 값 | 이유 |
|------|----|------|
| `Content-Type` | `text/event-stream; charset=UTF-8` | C-2. charset 누락 시 일부 프록시가 추측 → 한글 깨짐 |
| `Cache-Control` | `no-cache, no-transform` | C-2. 프록시 캐싱·재포맷 금지 |
| `Pragma` | `no-cache` | HTTP/1.0 호환 |
| `Connection` | `keep-alive` | 명시 |
| `X-Accel-Buffering` | `no` | nginx 버퍼링 off (R-4-b) |
| `Vary` | `Accept, Origin` | 캐시키 분리. Origin allowlist (R-5/§NFR-4 `allowed-origins`) 검사 시 `Origin` 동봉 필수 |

**gzip 제외 설정**:
- `application.properties` 에 `server.compression.excluded-user-agents=` 가 아닌, `application.yml` 또는 `WebMvc` 설정에서 `text/event-stream` MIME 을 압축 제외 목록에 명시.
- Spring Boot 기본 `server.compression.mime-types` 에 `text/event-stream` 미포함 — **재확인 필요**: 외부 프록시(nginx) 가 `gzip` 적용하지 않도록 응답 헤더 `Content-Encoding` 미설정 + `Cache-Control: no-transform` 으로 변환 차단.
- 검증 항목: 통합 테스트에서 `curl -H "Accept-Encoding: gzip" -N /admin/team-monitor/stream` 응답에 `Content-Encoding` 헤더 부재 확인.

### 4-8. 시각 동기화 (C-3)

- **서버 송신**:
  - 모든 페이로드 (`snapshot` / `update` / `ping`) 에 `serverTime` (ISO-8601 with offset) 포함.
  - 카드 `updatedAt`, 타임라인 `occurredAt` 도 동일 ISO 형식.
  - epoch seconds 는 페이로드에서 제외 (혼동 방지).
- **클라이언트 보정**:
  - 첫 `snapshot` 수신 시 `clockSkewMs = Date.now() - parseISO(serverTime)` 계산.
  - 모든 "n초/분 전" 상대 표기는 `serverNow = Date.now() - clockSkewMs` 기준으로 산출.
  - 매 `ping` 수신 시 skew 재계산 (EMA, α=0.3) — 시계 드리프트 보정.
  - skew 절대값이 5초 초과면 콘솔 warn + UI 우측 하단 회색 배지 "서버 시각과 5초 이상 차이".

### 4-9. Actuator 헬스 indicator JSON 샘플 (출시 전 필수)

`/actuator/health/teamMonitor` 응답 (정상):
```json
{
  "status": "UP",
  "details": {
    "watcher": {
      "mode": "nio",
      "alive": true,
      "lastEventAt": "2026-04-26T08:43:16+09:00",
      "restartCount": 0
    },
    "sse": {
      "currentEmitters": 3,
      "maxEmitters": 20,
      "overflowPolicy": "reject",
      "totalConnections": 47,
      "totalRejected503": 0,
      "totalRateLimited429": 1
    },
    "timeline": {
      "size": 20,
      "buffered": 12,
      "lastEntryAt": "2026-04-26T08:43:16+09:00"
    },
    "statusDir": {
      "path": ".team-workspace/status",
      "readable": true,
      "files": ["planner.status","db.status","developer.status","codex.status"]
    }
  }
}
```

이상 상황 (watcher 다운):
```json
{
  "status": "DOWN",
  "details": {
    "watcher": {"mode":"nio","alive":false,"lastEventAt":"2026-04-26T08:10:00+09:00","restartCount":3,"lastError":"ClosedWatchServiceException"},
    "sse": {"currentEmitters":0,"maxEmitters":20},
    "timeline": {"size":20,"buffered":12}
  }
}
```

- `status` 결정 규칙: watcher.alive=false → DOWN. statusDir.readable=false → DOWN. 그 외 UP.
- 상한 도달(`currentEmitters == maxEmitters`) 은 **DOWN 아님** (정상 운영의 일부) — 운영자는 `totalRejected503` 추이로 판단.

### 4-10. set-status.sh atomic write (출시 전 필수)

기존 `set-status.sh:51-57` 의 비원자적 `cat > $f <<EOF` 를 다음으로 교체. POSIX 호환 + 같은 파일시스템 내 임시파일 → `mv` rename.

```bash
# POSIX-호환 atomic write
# - mktemp 으로 같은 디렉토리에 임시파일 생성 (cross-fs rename 회피)
# - mv 는 같은 파일시스템 내에서 rename(2) 으로 atomic
# - trap 으로 임시파일 누수 방지
tmp=$(mktemp "$STATUS_DIR/.${team}.status.XXXXXX") || { echo "❌ mktemp 실패"; exit 1; }
trap 'rm -f "$tmp"' EXIT
{
  printf 'team=%s\n'     "$team"
  printf 'state=%s\n'    "$state"
  printf 'progress=%s\n' "$progress"
  printf 'task=%s\n'     "$task"
  printf 'updated=%s\n'  "$(date +%s)"
} > "$tmp"
mv -f "$tmp" "$STATUS_DIR/$team.status"
trap - EXIT
```

- 권고 주석 (소스 코드에 그대로 포함):
  - `mktemp` 가 같은 디렉토리에 생성되어야 `mv` 가 cross-fs 복사가 아닌 rename 으로 동작.
  - `printf` 사용 (heredoc + 변수 확장 시 줄바꿈/공백 이슈 방지).
  - Windows Git Bash 에서도 동일 동작 (msys 의 `mv` 도 동일 디렉토리 rename 지원).

---

## 5. 리스크 & 보완

### R-1. 파일 시스템 watch 환경 의존성
- **리스크**: Windows native 환경은 `WatchService` 가 inotify 가 아닌 폴링 동작 → 지연. Docker/Linux 컨테이너는 호스트 fs 마운트 필요.
- **보완**:
  - **M-1a** `TeamStatusWatcher` 인터페이스 + `JavaNioWatcher`/`PollingWatcher` 두 구현체
  - **M-1b** `teammonitor.watcher.mode=auto` 설정. auto 시 NIO 시도 → 실패 시 polling 자동 fallback
  - **M-1c** watcher 다운 시 health endpoint 에 DOWN 노출 + 30초 주기 자동 재시작 시도

### R-2. status 파일 race condition
- **리스크**: `set-status.sh:51-57` 의 `cat > $f <<EOF` 는 비원자적. read 시점에 부분 쓰기 가능.
- **보완**:
  - **M-2a** `set-status.sh` 수정: 임시 파일 → `mv` 로 atomic 교체
  - **M-2b** 서버측 read 시 파싱 실패 → 직전 캐시값 유지 + 200ms 후 1회 재시도
  - **M-2c** `updated` 필드 누락 시 null-safe 처리 ("갱신: 미기록" 표기)

### R-3. SSE emitter 누수
- **리스크**: 듀얼 모니터에 항상 띄워두는 사용 패턴. 브라우저 탭 닫기·네트워크 단절 시 즉시 감지 못하면 OOM 가능.
- **보완**:
  - **M-3a** 동시 연결 상한 20 (`teammonitor.sse.max-emitters`) + LRU/reject 정책 (NFR-2-a)
  - **M-3b** 30초 heartbeat (`event: ping`) — onError/onTimeout 콜백에서 emitter 제거
  - **M-3c** 클라이언트 자동 재연결 + `Retry-After` 헤더 우선, 미존재 시 exponential backoff (3s → 10s → 30s, 최대 60s)
  - **M-3d** 분당 재연결 한도 (`teammonitor.sse.rate-limit-per-min=10`) — 429 응답 (NFR-2-b)

### R-4. 프록시·방화벽 SSE timeout
- **리스크**: 사내망 reverse proxy 가 idle connection 60초~5분에 끊을 수 있음.
- **보완**:
  - **M-4a** M-3b heartbeat 으로 traffic 유지
  - **M-4b** `Cache-Control: no-cache`, `X-Accel-Buffering: no` 명시 (nginx buffering off)

### R-5. 인증/권한 (long-lived connection)
- **리스크**: 세션 만료 후에도 SSE 가 살아있으면 권한 우회 가능. SSE 는 CSRF 토큰 검증 어려움.
- **보완**:
  - **M-5a** SSE 연결 수립 시 `@PreAuthorize("hasRole('ADMIN')")` 검증 + Principal 캡쳐
  - **M-5b** `HttpSessionListener.sessionDestroyed` 로 해당 사용자 emitter 즉시 종료
  - **M-5c** SSE endpoint GET 만 허용 + Origin 헤더 화이트리스트 검증

### R-6. 운영 / 장애 대응
- **리스크**: 서버 재시작 시 클라이언트 재연결 실패. watcher silent failure 감지 어려움.
- **보완**:
  - **M-6a** 클라이언트 재연결 + UI "연결 끊김 / 재연결 중" 배지
  - **M-6b** Actuator `/actuator/health/teamMonitor` 커스텀 indicator (watcher alive, emitter count, last update)
  - **M-6c** SSE 다운 시 5초 폴링 fallback (graceful degradation)

### R-7. 보안 — XSS
- **리스크**: `task` 필드는 `set-status.sh "<script>"` 처럼 자유 입력. DOM 직접 삽입 시 XSS.
- **보완**:
  - **M-7a** Thymeleaf `th:text` 사용 (auto-escape) — `th:utext` 절대 금지
  - **M-7b** SSE 페이로드는 Jackson 직렬화. 클라이언트는 `textContent` 로만 삽입 (`innerHTML` 금지)
  - **M-7c** CSP 헤더 (§4-6) 로 인라인 스크립트·외부 리소스 완전 차단 — defense in depth

### 리스크 매트릭스 요약

| 식별 ID | 영역 | 영향도 | 발생빈도 | 보완 우선순위 |
|---------|------|--------|----------|---------------|
| R-1 | 인프라 | 중 | 중 | P1 |
| R-2 | 데이터 | 상 | 하 | **P0** |
| R-3 | 메모리 | 상 | 중 | **P0** |
| R-4 | 네트워크 | 중 | 중 | P1 |
| R-5 | 보안 | 상 | 하 | **P0** |
| R-6 | 운영 | 중 | 하 | P2 |
| R-7 | 보안 | 상 | 중 | **P0** |

- **P0** (R-2/R-3/R-5/R-7): 필수 — 초기 출시에 반드시 포함
- **P1** (R-1/R-4): 초기 출시 포함 권장
- **P2** (R-6): 2단계 개선

---

## 6. 검증 계획

### 6-1. 단위 테스트
- `TeamStatusReader` — 정상/누락 필드/잘못된 progress 값 파싱
- `JavaNioWatcher` / `PollingWatcher` — 변경 감지 정확성
- `TeamMonitorService` — emitter 추가·제거·heartbeat 송신, **prevState/prevProgress 계산 정확성** (FR-5-c)
- ring buffer (`teammonitor.timeline.size`) — 상한 초과 시 가장 오래된 것 제거
- LRU 퇴출 (NFR-2-a) — `reject`/`evict-oldest` 모드별 동작
- 페이로드 직렬화 — `serverTime`/`occurredAt` ISO-8601 with offset 형식 검증

### 6-2. 통합 테스트
- `bash set-status.sh planner 진행중 50 "test"` 실행 → SSE 클라이언트가 1초 이내 수신
- 동시 연결 21회 시도:
  - `reject` 모드 → 21번째 요청 503 + `Retry-After` 헤더 확인 (NFR-2-b)
  - `evict-oldest` 모드 → 가장 오래된 1개 `event: evicted` 수신 후 종료 확인
- 분당 11회 재연결 → 11번째 429 + `Retry-After` 확인
- 세션 만료 시 emitter 자동 종료 확인
- `curl -H "Accept-Encoding: gzip" -N /admin/team-monitor/stream` → `Content-Encoding` 헤더 부재 + `Cache-Control: no-cache, no-transform` 존재 확인 (§4-7)
- `curl -I /admin/team-monitor` → CSP/X-Frame-Options/Referrer-Policy 등 §4-6 헤더 전부 존재 확인
- `set-status.sh` 동시 호출 100회 (`xargs -P 10`) → status 파일이 항상 valid (atomic write 검증, §4-10)
- `/actuator/health/teamMonitor` 응답이 §4-9 JSON 스키마와 일치 (정상/watcher 다운 양쪽)

### 6-3. 수동 검증 (브라우저)
- Chrome DevTools Network 탭 → SSE 연결 정상, `Content-Type: text/event-stream; charset=UTF-8`
- 풀스크린 모드 (`?fullscreen=1`) 듀얼 모니터 렌더 확인 + 타임라인 sticky 동작
- XSS 페이로드 (`<script>alert(1)</script>`) → escape 확인 + DevTools Console CSP 차단 메시지 확인
- 시계 보정: 클라이언트 시각을 5분 임의로 변경 → "n초 전" 표기가 서버 기준으로 정상, 우측 하단 skew 배지 노출 (§4-8)

### 6-4. 회귀 영향
- 기존 `monitor.sh`·`set-status.sh` 동작 변경 없음 (확인 필수)
- 기존 admin 메뉴 라우팅 영향 없음

---

## 7. 영향 파일 (예상)

### 신규
- `src/main/java/com/swmanager/system/controller/TeamMonitorController.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusReader.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusWatcher.java` (interface)
- `src/main/java/com/swmanager/system/service/teammonitor/JavaNioWatcher.java`
- `src/main/java/com/swmanager/system/service/teammonitor/PollingWatcher.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorService.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorHealthIndicator.java`
- `src/main/java/com/swmanager/system/security/TeamMonitorSecurityHeadersFilter.java` (§4-6 CSP/보안 헤더 적용)
- `src/main/java/com/swmanager/system/dto/TeamStatus.java`
- `src/main/java/com/swmanager/system/dto/TeamStatusEvent.java`
- `src/main/java/com/swmanager/system/dto/TimelineEntry.java`
- `src/main/resources/templates/admin/team-monitor.html`
- `src/main/resources/static/css/team-monitor.css`
- `src/main/resources/static/js/team-monitor.js`
- 테스트 클래스 일체

### 수정
- `src/main/resources/application.properties` (teammonitor.* 네임스페이스 추가, §4-4)
- `.team-workspace/set-status.sh` (POSIX-호환 atomic write 로 변경, §4-10)
- 관리자 메뉴 (네비게이션 링크 추가) — 위치는 개발계획 단계에서 확정

### 변경 없음
- `.team-workspace/monitor.sh` (기존 터미널판 유지)
- DB 스키마

---

## 8. 권한 / 보안 체크리스트

- [ ] 페이지: `hasRole('ADMIN')` 적용
- [ ] SSE endpoint: 동일 권한 + Origin 헤더 화이트리스트 검증 (`teammonitor.security.allowed-origins`)
- [ ] XSS: Thymeleaf auto-escape + JS `textContent` 사용
- [ ] CSRF: SSE GET 만 사용 (state 변경 없음)
- [ ] 세션 만료 → emitter 즉시 종료
- [ ] 민감정보(MAC·credential) 노출 없음 (status 파일에 그런 데이터 없음 — 확인 완료)
- [ ] **CSP** 헤더 적용 (§4-6) — 인라인 스크립트·외부 CDN 차단, defense in depth
- [ ] **SSE 응답 헤더 고정** (§4-7) — `Content-Type: text/event-stream; charset=UTF-8`, `Cache-Control: no-cache, no-transform`, `X-Accel-Buffering: no`
- [ ] **gzip 미적용** 검증 — `text/event-stream` 응답에 `Content-Encoding` 부재
- [ ] **상한 초과 응답** 시 정보 누출 없음 (max-emitters 노출은 운영 디버깅 정보 한정, 인증된 ADMIN 만 접근)
- [ ] **Rate limit** (NFR-2-b 429) 적용 — 동일 Principal 분당 재연결 한도
- [ ] **헬스 indicator** (§4-9) — 민감 정보 미포함 (파일 경로는 상대경로 표기)

---

*이 기획서가 codex 검토 통과 + 사용자 최종승인 후에만 개발계획 단계로 진입합니다.*
