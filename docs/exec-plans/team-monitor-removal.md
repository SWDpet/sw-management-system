# 기획·개발계획서 — team-monitor(가상 팀 진행율 모니터) 기능 전체 제거 (team-monitor-removal)

- **상태**: v0.1 (영향분석 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **결정**: 사용자 지시(2026-06-22) — team-monitor 기능 불필요 → **서브시스템 전체 제거**(개발자 전용 도구, 운영 미사용).
- **성격**: 기능 삭제(behavioral). UI(관리자 nav 링크·대시보드 페이지)·SSE 엔드포인트·actuator health/info 기여자 제거. **DB 변경 없음**(team-monitor 는 DB 미사용, 파일 기반).

## 1. 무엇을 지우나 — 발자국 (38 참조 파일)

### 1-a. 삭제(main java, 24)
- 컨트롤러(2): `controller/TeamMonitorController`, `controller/TeamMonitorAdvice`
- 서비스/워처(10): `service/teammonitor/` 전체 — TeamMonitorService, TeamStatusReader, TeamStatusWatcher, JavaNioWatcher, PollingWatcher, TeamMetadata, TeamMonitorWatcherConfig, TeamMonitorSessionListener, TeamMonitorHealthIndicator, HealthMetrics
- 설정(4): `config/` — TeamMonitorProperties, TeamMonitorFilterConfig, TeamMonitorListenerConfig, TeamMonitorSecurityHeadersFilter
- 액추에이터(1): `actuator/TeamMonitorInfoContributor`
- 예외(3): `exception/` — OverflowRejectedException, RateLimitedException, OriginNotAllowedException *(team-monitor 전용 — 외부 사용 0 확인)*
- DTO(3): `dto/` — TeamStatus, TeamStatusEvent, TimelineEntry
- 지원(1): `dto/support/UlidGenerator` *(team-monitor 전용 — TeamMonitorService 만 사용, 확인)*

### 1-b. 삭제(test, 5)
`service/teammonitor/`: JavaNioWatcherTest, PollingWatcherTest, TeamMetadataTest, TeamStatusReaderTest, TeamMonitorCompressionPropertiesTest

### 1-c. 삭제(정적/템플릿, 3)
`templates/admin/team-monitor.html`, `static/css/team-monitor.css`, `static/js/team-monitor.js`

## 2. 무엇을 고치나 — 외부 결합점 편집 (삭제 아님)

| 파일 | 편집 |
|---|---|
| `SwManagerApplication.java` | ① import `TeamMonitorProperties` 제거 ② `@EnableConfigurationProperties({SecurityLoginProperties, TeamMonitorProperties})` → `(SecurityLoginProperties.class)` ③ `@ComponentScan(excludeFilters=…TeamMonitorProdExclusionFilter)` **전체 제거**(이 필터 전용 → 제거 시 @SpringBootApplication 기본 스캔으로 복귀, base 패키지 동일) ④ inner class `TeamMonitorProdExclusionFilter` 삭제 ⑤ 그로 인해 미사용된 import 정리(EnvironmentAware/ComponentScan/FilterType/Environment/MetadataReader·Factory/TypeFilter/IOException/Arrays) |
| `application.properties` | **§7 블록은 혼합 → 부분 편집(codex P2-1)**. ❌제거: `teammonitor.*` 키 전체(L106–134), `/admin/team-monitor/stream` nginx 가이드 주석(L136–146), team-monitor 섹션 주석. ✅**유지(범용)**: `management.endpoints.web.exposure.include=health,info`·`management.endpoint.health.show-details`(actuator 노출 — 삭제 시 `/actuator/info` 사라짐)·`server.compression.*`(전 응답 압축, team-monitor 무관). 섹션 헤더 "team-monitor" → "Actuator + 응답 압축"으로 리네임, compression 주석에서 SSE·TeamMonitorCompressionPropertiesTest 참조 제거, `management.info.env.enabled=false` 주석의 InfoContributor 참조 제거(설정값은 유지). |
| `templates/fragments/top-nav.html` | 사이드바 링크 `<a href="/admin/team-monitor">팀 모니터</a>`(L130) 제거 |
| `static/css/preview-harness.html` | **team-monitor 참조 2곳 모두 제거(codex P2-2)**: ① R-14 NIO atomic replace 항목 카드(L262–270, JavaNioWatcher 내부 문서) ② 하단 사고사례 `Windows NIO DELETE-only` 항목(L283). |
| `src/test/resources/golden/endpoint-inventory.txt` | `/admin/team-monitor`·`/admin/team-monitor/stream` 2줄 제거(EndpointInventoryTest `GOLDEN_RECORD=1` 재생성) |
| `src/test/resources/golden/map-debt-baseline.txt` | Map 부채 감소분 tighten(team-monitor 의 `Map<String,Object>` ≈10: Advice + HealthIndicator) `GOLDEN_RECORD=1` 재생성 |

### 건드리지 않음 (의도)
- `SecurityConfig.java`: team-monitor 참조는 **주석 1줄뿐**. `/actuator/**` 시큐리티 체인은 범용(actuator 자체는 잔존) → **유지**.
- actuator health/info: 기여자(TeamMonitorInfoContributor/HealthIndicator)만 제거. **노출 설정(`management.*`)·시큐리티 체인은 유지**하여 `/actuator/health`·`/actuator/info` 계약 보존(codex P2-1) — info 는 기여자 제거로 내용만 비게 됨, 엔드포인트는 200 유지.
- `server.compression.*`: 전 응답(html/json/css/js) 압축 — team-monitor SSE 와 무관한 범용 perf 설정 → 유지.

## 3. 안전성 근거 (공유 클래스 검증)
- `UlidGenerator` 참조 = TeamMonitorService 1곳뿐(grep 확인) → 삭제 안전.
- `OverflowRejectedException`/`RateLimitedException`/`OriginNotAllowedException` 참조 = team-monitor 6파일 내부뿐 → 삭제 안전.
- MenuName/권한 컬럼 엔트리 **없음**(constants grep 0) → 권한·시드 변경 불필요.
- team-monitor 는 DB·마이그레이션 무관(파일 `.team-workspace/status` 기반, 런타임 데이터).

## 4. 검증 (T)
1. **컴파일**: `./mvnw -o -DskipTests compile` green(미사용 import·dangling 참조 0).
2. **컨텍스트 로드**: `SwManagerApplicationTests`(context) green — 빈 누락/순환 없음.
3. **endpoint golden**: `GOLDEN_RECORD=1 … EndpointInventoryTest` → team-monitor 2엔드포인트만 감소(나머지 표면 불변 확인).
4. **ratchet**: `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 감소 tighten.
5. **전체**: `./mvnw -o test` green(삭제된 5 테스트 외 회귀 0).
6. **actuator 노출 보존**: `management.endpoints.web.exposure.include=health,info` 잔존 확인(codex P2-1 — /actuator/info 계약 유지).
7. codex 검증(잔존 참조·dangling 설정·SecurityConfig·actuator 노출·preview-harness 잔재·context 로드).

## 5. 롤백
원자 1 커밋 → `git revert`(파일 추가/삭제 모두 복원). 기능 단위 제거라 부분 롤백 불필요.

## 6. 커밋 메시지(승인 후)
`chore(team-monitor): 가상 팀 진행율 모니터 기능 전체 제거(컨트롤러·서비스·워처·설정·액추에이터·DTO·정적·테스트) + 부트/nav/golden 정리`
