**최종 판정: ⚠ 수정필요**

개발계획서는 큰 흐름은 기획서 v1.3을 반영했지만, 집중 검토 지점 3개 중 2개가 구현 전 보완이 필요합니다.

**주요 수정 필요 사항**

1. **Step 4-a `pjt_id` 해석이 doc-inspect 대상 프로젝트와 일치 보장 안 됨**
   - 계획안의 `findFirstByDistNmAndSysNmEnOrderByYearDescProjIdDesc(distNm, sysNmEn)`는 “최신 연도 프로젝트”를 고릅니다.
   - 하지만 `doc-inspect.html`은 이미 정확한 `projId`를 선택/복원합니다. 신규 작성은 `/document/api/projects?year=...&cityNm=...&distNm=...&sysNmEn=...`로 프로젝트를 고르고, 기존 회차 로드는 `/projects/api/{pjtId}`를 사용합니다.
   - 따라서 과거 회차 편집, 동일 지자체/시스템의 연도별 프로젝트, 동일 `distNm + sysNmEn` 중복 시 Snapshot API가 점검내역서의 `pjtId`가 아닌 최신 프로젝트 Snapshot을 볼 수 있습니다.
   - 수정 권고: Snapshot API는 `pjtId`를 직접 받는 설계가 안전합니다. 최소한 `year + cityNm + distNm + sysNmEn`까지 받아야 하며, 그래도 기존 회차 편집 흐름에서는 `pjtId` 직접 전달이 더 정확합니다.

2. **Step 4-b `findLatestPerRoleHost`는 동시각 tie 시 중복 row 반환 가능**
   - 현재 계획 JPQL은 `(pjtId, serverRole, hostName)`별 `MAX(collectedAt)`와 같은 모든 row를 반환합니다.
   - 기존 적재 unique는 [InspectMetricSnapshotRepository.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/repository/InspectMetricSnapshotRepository.java:121)의 `ON CONFLICT (pjt_id, server_role, COALESCE(host_name, ''), collected_at)` 기준이라 같은 host/timestamp의 재삽입은 막지만, `host_name` NULL/blank 정규화 차이, 기존 데이터, constraint 실제 적용 여부에 따라 “최신 1건” 보장이 JPQL 자체에는 없습니다.
   - 기획서 NFR-9/T-20은 “최신 1건”을 요구하므로 tie-breaker가 필요합니다. 예: `snapshotId` 최대값까지 같이 고르거나, native query `row_number() over (partition by pjt_id, server_role, coalesce(host_name,'') order by collected_at desc, snapshot_id desc) = 1`.

3. **Step 4-c raw_payload key 매핑은 일부 맞지만, 추출 방식 명세가 부족**
   - 실제 적재는 [InspectionQrBatchService.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java:397)에서 `tier.getH()`를 `hostName`으로 저장하고, raw payload는 tier 전체입니다. item 구조는 [InspectionQrBatchService.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java:401)처럼 `List<Object> metric`, `metric[0]=key`, `metric[2]=value`입니다.
   - 계획의 key 자체는 대체로 실제 코드와 맞습니다: AP `ap.hw.cpu`, `ap.hw.memory`, `ap.os.disk_summary`, DB `db.os.cpu_info`, `db.os.mem_info`, `db.os.disk`.
   - 다만 `raw_payload.items`라고만 쓰면 실제 Jackson JSON에서 필드명이 `items`인지 `i`인지 DTO 직렬화 정책에 의존합니다. 코드 하단의 idempotent 응답은 저장 batch payload에서 `i`를 읽고 있어 혼동 여지가 있습니다.
   - 수정 권고: Snapshot API 추출 명세에 “`rawPayload` Map에서 `items` 또는 `i` 배열을 호환 처리하고, 각 tuple의 index 0/2를 사용한다”를 명시해야 합니다.

**요건 반영 누락/약함**

- **NFR-1**: 클라이언트 page load 증가 200ms 측정 절차가 Step 7에 없습니다.
- **NFR-3**: WCAG AA 대비 검증이 구현/테스트 Step에 없습니다. 색 토큰 정의만 있습니다.
- **NFR-9**: API P95 300ms 측정이 Step 7에 없습니다.
- **T-10**: 다크모드 토글 시 alert 색/대비 검증이 Step 7 시나리오에 빠져 있습니다.
- **FR-4(b)**: 저장 버튼 클릭 시 재비교 후 alert 재표시가 테스트에는 언급되지만, 구현 Step 5~6에 저장 이벤트 hook이 명확하지 않습니다.

**순서·회귀·롤백**

Step 1~7 순서는 대체로 타당합니다. 다만 Step 4 API 설계가 `pjtId` 기준으로 먼저 확정되어야 Step 5 프론트 fetch와 테스트가 안정됩니다.

회귀 리스크는 제한적이지만 `getInfraServers`는 기존처럼 [DocumentController.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/controller/DocumentController.java:1717)에서 `distNm + sysNmEn` 첫 번째 infra를 반환합니다. 이번 변경이 여기에 `hostName`만 추가하는 수준이면 침범은 낮습니다. 인프라 저장 흐름은 `InfraServer` 필드 추가와 form binding만이면 위험 낮지만, orphan/cascade 구조는 유지해야 합니다.

롤백은 `host_name` nullable 컬럼 추가라 DB 위험은 낮습니다. 다만 `DROP COLUMN` 롤백 시 사용자가 입력한 `host_name`은 손실되므로 운영 롤백 문서에 “데이터 손실성 롤백”을 명시하는 편이 안전합니다.