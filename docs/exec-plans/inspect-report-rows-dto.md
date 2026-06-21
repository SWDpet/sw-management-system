# 개발계획서 — InspectReportController 인프라서버/스냅샷 조회 응답 Map→record (inspect-report-rows-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 후속. `doclookup-workplan-rows-dto` 와 동일 패턴(읽기 API 의 컨트롤러-로컬 `List<Map>` 응답조립을 record 로 치환). InspectReportController 의 **읽기 projection 2 엔드포인트**만 대상.
- **무손실 핵심**: 둘 다 컨트롤러-로컬 `LinkedHashMap` 응답조립 → JsonNode tree 동치(키셋·값·null·순서)로 무손실. 클라이언트는 응답 JSON 키로 접근.
- **디자인팀**: 비해당 — 백엔드 전용(서버 응답 JSON 무손실 보존, 클라이언트 파일 무변경). **DB**: 변경 없음.

## 범위 — 치환 2 / 보존 3 (이유 명시)

### 치환 대상 (clean read-projection → record)

| 메서드 | 응답 키 | record |
|---|---|---|
| `getInfraServers` `/api/infra-servers` | serverId, serverType, hostName, ipAddr, osNm, serverModel, serialNo, cpuSpec, memorySpec, diskSpec, networkSpec, powerSpec, osDetail, rackLocation, note, softwares(중첩) | `InfraServerRow(Long serverId, String serverType, …(13 String)…, String note, List<SoftwareRow> softwares)` ← `from(InfraServer)` + 중첩 `SoftwareRow(String swCategory, String swNm, String swVer)` ← `from(InfraSoftware)` (**swVer null→"" fallback 보존**, 현행 L246) |
| `getInspectSnapshots` `/api/inspect-snapshots` | serverRole, hostName, cpu, memory, disk | `SnapshotRow(String serverRole, String hostName, String cpu, String memory, String disk)` (cpu/memory/disk 는 `extractSnapshotSpecs` 결과 — 컨트롤러에서 주입, null 가능) |

- **민감정보 제외 보존**: getInfraServers 는 MAC·accId·accPw 등을 응답에서 의도적으로 제외 중(감사 P2 1-4). record 는 **현행 16키 화이트리스트만** 매핑 — 민감필드 신규 노출 절대 금지.
- 키순서: 현행 `LinkedHashMap`(순서 결정적)이나 클라이언트 키접근 → tree 동치(키셋·값)로 검증(순서 무의존).

### 보존 (record 비대상)

| 메서드/헬퍼 | 사유 |
|---|---|
| `resetAllInspect` `/api/inspect/reset-all` | 성공 응답 `{success:true, deleted:{...}}` 의 `deleted`(도메인키, Map<String,Long>) = P6. ApiResult.ok(data) 로 흡수 시 `deleted`→`data` 로 wire 변형(비-무손실) → **보존**(헌장 §0 무손실 우선). |
| `extractSnapshotSpecs(Map<String,Object> raw)` + 내부 캐스트 | raw_payload(jsonb) **동적 파싱** 입력 — 응답조립 아님. 타입화 부적합 → 보존. |

## 목표 (FR/NFR)

- **FR**: 위 2 메서드의 `Map<String,Object>` 응답조립 → record 3종(InfraServerRow/SoftwareRow/SnapshotRow). **`Map<String,Object>` 선언 281→277 (−4)**.
- **NFR**: wire 무손실(키셋·값·타입·null·민감필드 제외 유지), 회귀 0, ratchet 277 tighten. URL·HTTP status·403(ApiResult.fail 기존)·빈배열 경로 보존.

## record 위치
`com.swmanager.system.dto.inspection` (신규 subpackage — 점검 응답 행 전용). 타입: serverId=Long, 그 외 전부 String, softwares=List<SoftwareRow>.

## 검증 (골든 = 레거시 LinkedHashMap 복제본과 tree 동치)

1. `InspectReportRowsTest`(신규): 채운 엔티티 → `from()` 직렬화가 현행 `LinkedHashMap` 복제본과 **JsonNode tree 동치**. 경계: InfraServerRow 16키(softwares 중첩 swVer ""/일반), null 필드 키 보존, SnapshotRow cpu/memory/disk null 보존, softwares 빈 리스트.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 281→277.
3. `./mvnw test` 전체 green.
4. codex 검토(키·타입·tree 동치·민감필드 제외 유지·null·보존판정 타당성).

## 롤백
원자 1 커밋 → `git revert`. record 3 신규 + 컨트롤러 2 메서드 국소 치환. 클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): InspectReportController 인프라서버/스냅샷 조회 응답 Map→record 3종(민감제외 보존) + ratchet 281→277 (§6-4)`
