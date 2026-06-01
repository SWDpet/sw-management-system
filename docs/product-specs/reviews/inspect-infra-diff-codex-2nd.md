**최종 판정: ⚠ 수정필요**

B안 자체, 즉 `tb_infra_server.host_name` 컬럼 신설 방향은 §7, §12-1, 변경이력에 대체로 반영됐습니다. 다만 v1.2는 아직 승인하기엔 매칭 키와 1차 범위 쪽에 실행 불가능한 모순이 남아 있습니다.

**주요 문제**

1. **FR-1 / FR-3-1 / NFR-9 / T-13 / R-7: 매칭 키가 현재 코드와 충돌**
   - 기획서: `(normalize(server_role), host_ip)` 매칭.
   - 실제 코드: [InspectionQrBatchService.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java:397) 에서 `hostName = tier.getH()`를 저장하지만, [InspectionQrBatchService.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java:398) 에서 `hostIp = null`.
   - 따라서 `host_ip` 기반 매칭은 현재 데이터로는 거의 항상 실패합니다. §12-1은 `host_name ↔ InspectMetricSnapshot.host_name` 비교를 말하면서도, 바로 아래에서 `(server_role, host_ip)` unique 보장을 말해 내부 모순입니다.
   - 수정 방향: 1차는 `(normalize(server_type/server_role), normalize(host_name))`로 확정하거나, agent/payload/적재까지 `host_ip` 수집을 이번 sprint 범위에 포함해야 합니다.

2. **T-3: 1차 비교 필드가 4개인데 “5개 필드 차이”는 불가능**
   - FR-3의 1차 범위는 `host_name`, `cpuSpec`, `memorySpec`, `diskSpec` 4개입니다.
   - T-3의 “5개 필드 차이”는 1차 범위에서 재현 불가입니다.
   - 수정: T-3을 “4개 필드 차이”로 바꾸거나, 5개 필드 테스트는 2차 확장 테스트로 이동.

3. **FR-3 vs §12-1 DB팀 권고: 1차 범위 4개/3개 불일치**
   - FR-3은 1차 4필드에 `host_name`을 포함합니다.
   - §12-1 (1)은 “1차 도입은 `cpuSpec` / `memorySpec` / `diskSpec` 3 필드만 비교”라고 남아 있습니다.
   - B안 확정 후 최종 본문 기준은 4필드인지 3필드인지 하나로 정리해야 합니다.

4. **NFR-1: 존재하지 않는 `NFR-9-2` 참조**
   - NFR-1은 “신규 Snapshot API 호출 자체는 별도 NFR-9-2에서 측정”이라고 하지만 NFR-9-2 항목이 없습니다.
   - 수정: `NFR-9`로 고치거나 API 성능 항목을 `NFR-9-1/9-2`로 분리.

5. **FR-3 / §7-3 / T-19: `host_name` 입력·수정 경로가 미확정**
   - §7-3은 인프라 자산입력/수정 화면의 `host_name` 필드 추가를 “별도 sprint 또는 본 sprint 부속 작업”으로 열어둡니다.
   - 그런데 1차 비교 대상에 `host_name`을 넣으면, 인프라 DB 쪽 값을 사용자가 채울 수 있는 경로가 이번 sprint에 필요합니다.
   - 수정: `host_name` 비교를 1차에 유지한다면 입력/수정 UI와 DTO 저장 흐름을 본 sprint 필수 범위로 명시해야 합니다. 아니면 `host_name` 비교를 2차로 내려야 합니다.

6. **T-12: Snapshot N=0 처리와 API 실패 처리의 UX 차이가 더 명확해야 함**
   - T-12는 Snapshot이 0건이면 모든 인프라 필드를 “현장 미수집”으로 알림.
   - T-14는 Snapshot API 실패면 silent skip.
   - 둘은 구분 가능하지만, 구현 명세에서 “성공 응답 200 + 빈 배열”과 “API 실패”의 UX 분기가 더 명시돼야 합니다.

**신규 리스크 제안**

- **R-12: host_ip 매칭 불능 리스크** — 현재 `InspectionQrBatchService`가 `host_ip`를 `null`로 저장하므로 `(server_role, host_ip)` 매칭이 실패한다. 대응: 1차 매칭 키를 `(server_role, host_name)`으로 바꾸거나 agent payload에 IP 수집/적재를 포함.
- **R-13: host_name 미입력 누적 리스크** — 신규 컬럼은 NULL 허용이라 마이그레이션은 안전하지만, 입력/수정 UI가 없으면 기존 및 신규 인프라 row의 `host_name`이 계속 비어 비교 품질이 낮다.
- **R-14: 최신 Snapshot 선택 기준 모호 리스크** — 동일 프로젝트/역할/호스트에 여러 `collected_at` row가 있을 때 Snapshot API가 어느 시점 값을 반환할지 미정이다. 최신 1건인지, 점검월 범위 최신인지 명시 필요.
- **R-15: raw_payload 구조 오해 리스크** — `raw_payload`는 tier 객체 직렬화이며 실제 비교 필드는 `items` 배열 내부 key/value에서 찾아야 한다. API 명세가 `ap.hw.cpu.name` 같은 직접 path처럼 읽히면 구현자가 잘못 매핑할 수 있다.

**권고사항**

1. 매칭 키를 먼저 확정하세요. 현재 코드 기준으로는 `(server_role, host_name)`이 현실적입니다.
2. FR-3, §12-1, T-3의 1차 필드 수를 하나로 맞추세요.
3. `host_name`을 1차 비교에 포함할지 결정하고, 포함한다면 입력/수정 UI와 DTO 저장 범위를 필수로 올리세요.
4. Snapshot API는 “프로젝트 식별”, “최신 row 선택 기준”, “빈 결과 vs 실패 UX”를 명시하세요.
5. NFR-1의 `NFR-9-2` 오타/누락을 정리하세요.
6. §9의 “자문 필요 사항”은 이미 §12에서 결정된 항목과 충돌하므로, 남길 경우 “해결 완료/결정 반영” 형태로 바꾸는 편이 좋습니다.