**최종 판정: ⚠ 수정필요**

1차 지적 3개 중 4-a, 4-b는 v2에서 해소됐습니다.

- **4-a pjtId 직접 수신**: 해소. `doc-inspect.html`은 신규 작성 시 `/document/api/projects?...` 결과의 `p.projId`를 `#projId` option value로 넣고, 1건이면 즉시 선택합니다. 저장 전 신규 회차도 사업 선택 후에는 `projId`가 채워집니다. 기존 회차 로드도 `data.pjtId`로 `#projId`를 복원합니다.  
  근거: [doc-inspect.html](C:/Users/ukjin/sw-management-system/src/main/resources/templates/document/doc-inspect.html:1091), [doc-inspect.html](C:/Users/ukjin/sw-management-system/src/main/resources/templates/document/doc-inspect.html:1137), [doc-inspect.html](C:/Users/ukjin/sw-management-system/src/main/resources/templates/document/doc-inspect.html:2010)

- **4-b 최신 1건 native window query**: 해소. `PARTITION BY pjt_id, server_role, COALESCE(host_name,'') ORDER BY collected_at DESC, snapshot_id DESC`는 `(role, host)`별 최신 1건과 동시각 tie-break를 정확히 정의합니다. `InspectMetricSnapshot` 컬럼도 `snapshot_id`, `pjt_id`, `server_role`, `host_name`, `host_ip`, `collected_at`, `cpu_pct`, `mem_pct`, `disk_pct`, `raw_payload`, `created_at`로 `SELECT s.*` 매핑에 맞습니다. `rn` extra column은 Hibernate 엔티티 매핑에서 보통 무시되지만, 구현 시 리스크를 줄이려면 외부 SELECT에서 컬럼을 명시하는 편이 더 안전합니다.  
  근거: [InspectMetricSnapshot.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/domain/InspectMetricSnapshot.java:34)

- **4-c raw_payload 구조**: 부분 해소. `@JsonProperty("i")`, tuple `[key,status,value]`, `value=index2`, host `h`는 실제 코드와 일치합니다. 다만 `ap.hw.cpu`의 `value(index2)`는 “CPU 스펙 텍스트”가 아니라 `name`, `cores` 등을 가진 객체(Map)입니다. 기존 표시 로직도 `ap.hw.cpu`에서 `name + cores`를 조합해 텍스트를 만듭니다. v2는 `value(index2)` 사용과 “문자열/객체 검증”을 언급하지만, Snapshot API 응답 변환 규칙에 `ap.hw.cpu.value.name/cores`, `ap.hw.memory.installed_gb`, `ap.os.disk_summary.summary`, DB 키별 추출 규칙을 명시해야 구현 오해가 없습니다.  
  근거: [InspectionQrBatchRequest.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/dto/inspection/InspectionQrBatchRequest.java:81), [InspectionQrBatchService.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java:397), [InspectionQrBatchService.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java:401), [InspectionQrBatchService.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java:621)

1차 누락 항목은 대체로 반영됐습니다. Step 7에 NFR-1/NFR-3/NFR-9 측정과 T-10이 들어갔고, Step 5-1b에 저장 버튼 재비교 hook도 추가됐습니다.

잔여 리스크는 두 가지입니다. 첫째, Snapshot은 `pjtId`로 정확해졌지만 기존 비교 대상인 `/document/api/infra-servers`는 여전히 `distNm + sysNmEn` 기준 첫 번째 인프라를 반환합니다. 이는 기존 동작 유지로 볼 수 있으나, “점검대상 프로젝트와 정확히 일치”라는 표현까지 만족하려면 장기적으로 infra API도 프로젝트 기준화가 필요합니다. 둘째, rollback의 `DROP COLUMN host_name`은 사용자 입력값 손실을 일으키므로 v2의 백업 권고는 적절합니다.

수정 요구는 작습니다. v2 Step 4-c에 “raw value 객체를 비교용 텍스트/정규화 입력으로 변환하는 키별 규칙”만 명시하면 승인 가능입니다.