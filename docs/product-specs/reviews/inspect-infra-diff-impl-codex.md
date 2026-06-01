최종 판정: ⚠ 수정필요

주요 미충족/버그:

1. Snapshot 0건 처리 요구사항 위반  
   [doc-inspect.html](C:/Users/ukjin/sw-management-system/src/main/resources/templates/document/doc-inspect.html:1248) `compareInfraVsSnapshot()`가 `snaps.length === 0`이면 `[]`를 반환해 알림을 숨깁니다.  
   기획서 T-12 / NFR-9는 “HTTP 200 + 빈 배열 = 현장 미수집”으로 보고 인프라 서버가 있으면 “현장 미수집” 알림을 띄우는 흐름입니다. 현재 구현은 Snapshot API 실패(T-14)와 Snapshot 미수집(T-12)을 UX상 동일하게 무알림 처리합니다.

2. 기존 문서 로드 경로에서 신규 비교 로직 미호출  
   신규/선택 흐름 쪽은 [doc-inspect.html](C:/Users/ukjin/sw-management-system/src/main/resources/templates/document/doc-inspect.html:1205)에서 cache 설정과 `loadInfraDiff()` 호출이 추가됐지만, 저장된 문서 복원 경로의 infra fetch는 [doc-inspect.html](C:/Users/ukjin/sw-management-system/src/main/resources/templates/document/doc-inspect.html:2138)에서 기존 3개 fill 함수만 호출합니다.  
   이 경로로 점검내역서를 열면 FR-1/FR-4(a) 페이지 로드 직후 비교 알림이 동작하지 않습니다.

3. `db.os.disk` 추출이 표시규칙과 불일치  
   [DocumentController.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/controller/DocumentController.java:1815)는 `db.os.disk`에서 `summary`만 읽습니다. 그런데 기존 `InspectionQrBatchService.buildResultText()`는 `mounts`, `filesystems`, `pct/used_pct`, `count` 등의 일반 처리로 표시값을 만듭니다.  
   따라서 DB 디스크 raw_payload가 실제 `mounts` 배열 형태면 Snapshot API의 `disk`가 `null`이 되고, 비교는 “현장 미수집” false positive가 납니다. `[key,status,value]`에서 key=index0, value=index2를 읽는 구조 자체는 맞습니다.

4. FR-5 인라인 경고/팝오버 미구현  
   구현은 상단 요약 박스만 있습니다. 기획서 FR-5와 개발계획 Step 6-3의 “차이 있는 필드 옆 `fa-triangle-exclamation` + click popover”는 [doc-inspect.html](C:/Users/ukjin/sw-management-system/src/main/resources/templates/document/doc-inspect.html:150) 및 비교 렌더링 영역에 구현되지 않았습니다.

5. CSS 토큰 정책 부분 위반  
   `--alert-*` 토큰 추가는 됐지만, 신규 dark variant가 [doc-inspect.html](C:/Users/ukjin/sw-management-system/src/main/resources/templates/document/doc-inspect.html:169)에 raw hex로 들어가 있습니다. 개발계획 Step 6-1 / NFR-2 / NFR-6은 alert 색상을 design-system 토큰으로 정의하고 사용하는 방향입니다. 또한 `--alert-icon:#D97706` on `#FEF3C7`은 NFR-3의 아이콘 4.5:1 대비 조건을 만족하기 어려워 보입니다.

6. `InfraDTO.InfraServerDTO` hostName 누락  
   개발계획 Step 2-2는 DTO가 있으면 `hostName` 노출을 요구합니다. 현재 [InfraDTO.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/dto/InfraDTO.java:34)와 변환부 [InfraDTO.java](C:/Users/ukjin/sw-management-system/src/main/java/com/swmanager/system/dto/InfraDTO.java:178)에 `hostName`이 없습니다. 이번 변경의 직접 API는 Map 응답이라 동작은 하지만 Step 2 기준으로는 누락입니다.

확인된 정상/부분 정상:

- `host_name` 마이그레이션과 rollback은 additive nullable + rollback 파일로 방향은 맞습니다.
- `InfraServer.hostName` 필드/getter/setter와 `infra-form.html`의 `servers[index].hostName` 바인딩은 `@ModelAttribute` 저장 흐름에 맞습니다.
- `getInfraServers`의 `hostName` 추가는 기존 `fillTargetHW`, `fillServerCheckInfo`, `fillSoftwareInfo`가 사용하지 않는 추가 필드라 직접 회귀 위험은 낮습니다.
- `findLatestPerRoleHost`는 `(pjt_id, server_role, COALESCE(host_name,''))` 파티션 + `collected_at DESC, snapshot_id DESC`라 R-14 tie-break 요구와 맞습니다. extra `rn` 컬럼은 Hibernate native entity 매핑에서 보통 무시되어 blocker로 보지는 않았습니다.
- Snapshot API 권한은 `NONE` 403으로 기존 infra API와 동일하고, 응답 allowlist도 `serverRole, hostName, cpu, memory, disk`만 내려 `host_ip/status/raw_payload` 노출은 없습니다.
- 매칭키 정규화는 `WEB/AP -> ap`, `DB -> db`, host trim/lowercase로 FR-3-1 방향과 맞습니다.
- 저장 hook은 존재하지만 cached infra/snapshot으로 재비교합니다. 저장 차단 없음, dismiss 후 저장 시 재표시는 FR-4(b) 의도와 대체로 맞습니다.

테스트는 실행하지 않았습니다. 요청 범위가 코드 수정 금지 검토였고, 현재 세션은 read-only라 빌드 산출물 생성이 필요한 Maven 검증은 수행하지 않았습니다.