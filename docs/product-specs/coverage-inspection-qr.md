# [기획서] inspection QR 커버리지 보강 — InspectQrMetricSupport + InspectionQrBatchService

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 커버리지 증분. 거대클래스 트랙 종료 후 피벗. 방금 작업한 inspection QR 영역.
- **상태**: ⏳ 기획 — codex 실질검증(라벨 "미구현=FAIL" 9번째 오판 [[codex-plan-review-false-fail]]; 실질 권고 arm확장·경로추가·floor상향은 기획과 일치. ⚠codex 발견#1 "findByPjt 미stub→NPE"는 **Mockito Optional 기본 empty 오탐**—2-B에 명확화). 사용자 최종승인 대기.

---

## 1. 배경 / 목표

### 1-1 동기 + ROI 정직성
전역 LINE 72%+·floor 잘 관리되어 커버리지는 **한계효용 감소 구간**. 단 두 클래스에 **의미있는 미커버 분기**가 남아 회귀 안전망 가치가 있다. 단순 숫자 올리기가 아니라 **미커버 분기를 입력별 기대값으로 검증**(각 점검 항목 표시 로직·merge/snapshot 경로).

| 클래스 | 현재 LINE | miss | 미커버 핵심 |
|---|---|---|---|
| InspectQrMetricSupport | 86.0% | 30 | `formatValueWithContext` switch arm 다수(FormatTest 미커버) |
| InspectionQrBatchService | 74.0% | 69 | upload merge 경로·saveMetricSnapshot 분기·USAGE row 생성 |

### 1-2 목표
- **동작 변경 0**(테스트 추가만). 기존 동작을 입력별로 고정.
- InspectQrMetricSupport 86→**95%+**, InspectionQrBatchService 74→**85%+**.
- JaCoCo floor ratchet 상향(실측−여유).

---

## 2. 변경 설계

### 2-A InspectQrMetricSupport — FormatTest 확장
`formatValueWithContext` 의 **미커버 switch arm** 을 Map 입력 → 기대 표시문자열로 검증:
- HW/DB: `db.os.mem_info`(total_gb→GB)·`db.os.adapter`(adapter_count→개)·`db.os.network_ip`(ipv4 첫 IP)·`ap.net.ip`(ips[0].IPAddress)·`db.os.iostat`(stderr→error/정상)·`db.os.net_ping`(note "not found"→정상)·`db.os.net_link`(established+건)·`db.os.net_collisions`(total_coll+건)·`db.os.lsvg_rootvg`(stale_count+건).
- Oracle: `db.oracle.wait_events`(top_events size+건)·`db.oracle.export_last`(last_export/없음)·`db.oracle.standby_lag`(apply_lag/N/A)·`db.oracle.datafile_status`(total+개).
- GIS: `gis.gws.running`(status)·`gis.gws.http`(http_status)·`gis.uwes.dem_slop_preserved`(both_exist→보존/누락)·`gis.gss.log_purge`(purged_count+파일).
- AP: `ap.log.system_err`(error_count+건)·`ap.net.routes`(routes+개)·`ap.security.users`(enabled+계정)·`ap.hw.adapter`(up/total 값 있는 경우).
- 각 arm: 값 존재(정상 출력) + 일부 키 부재(§8-4 "-" 가드) 케이스.

### 2-B InspectionQrBatchService — ServiceTest 확장
mock 기반(운영DB 무관). 기존 18 케이스 외 미커버 경로:
- **upload merge 경로**: 동일 (pjt, month) 기존 manual report 존재 시 merge(batchId null→기록, inspUserId null→주입, AP/DB/DBMS/GIS 자동수집 결과만 삭제 후 재적재). `reportRepository.findByPjtIdAndInspectMonthAndDeletedAtIsNull` 이 `Optional.of(existingReport)` 반환하도록 stub + `deleteByReportIdAndSectionIn(AP,DB,DBMS,GIS)` verify. ⚠**Mockito 는 `Optional` 반환 메서드에 기본 `Optional.empty()` 반환**(ReturnsEmptyValues)이라 비-merge(신규) 테스트는 이 stub 불요 — 기존 18 케이스가 stub 없이 통과 중인 게 증거(codex 1차 "NPE" 오탐 방지). merge 테스트만 `Optional.of` 명시.
- **USAGE row 생성**: `db.os.disk`(mounts→DB_USAGE row, mountToCategory 매칭)·`ap.os.disk_summary`(drives→AP_USAGE row). saveCheckResults 의 분기.
- **saveMetricSnapshot 분기**: ts 있음/없음(collectedAt), cpu/mem/disk 일부만 추출, perf 데이터 없으면 skip, upsertIgnore affected 로깅.
- **site alias 폴백**: `findBySiteCode` empty → `findFirstBySiteCodeAlias` 경로.
- **toIdempotentResponse**: payloadJson tiers 파싱(tierCount/itemCount/manual/warn 집계).

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 추가 테스트가 현재 동작을 정확히 검증(기대값=현 구현 출력). 동작 변경 0. |
| FR-2 | InspectQrMetricSupport 95%+, InspectionQrBatchService 85%+ LINE. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS. 순수/mock 테스트만(운영DB 무관). |
| NFR-2 | FormatTest + InspectionQrBatchServiceTest 확장. 신규 테스트 모두 green. |
| NFR-3 | JaCoCo floor ratchet 상향(전역 LINE/INSTR 실측 반영). PIT·ratchet 게이트 불변. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 동작 변경 0 (테스트-only)** ✅: 기존 동작 고정. 버그 발견 시 별도 백로그(이번엔 교정 안 함).
- **5-2 mock 기반(운영DB 무관)** ✅: ServiceTest 는 Mockito repo stub. RUN_DB_TESTS 불요.
- **5-3 floor 상향** ✅: 실측 후 −여유(과도 상향 금지, 회귀봉인 목적).
- **5-4 우선순위**: InspectQrMetricSupport(순수, 확실) → InspectionQrBatchService(mock, 노력 큼). 시간 제약 시 전자 우선.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(수정) | `service/inspection/InspectionQrBatchFormatTest.java` | switch arm 케이스 추가 |
| Test(수정) | `service/inspection/InspectionQrBatchServiceTest.java` | merge·snapshot·USAGE 케이스 추가 |
| Config(수정?) | `pom.xml` jacoco floor | 실측 후 상향 |

production 변경 0 → 디자인·DB팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 테스트가 버그를 "현재 동작"으로 고정 | 중 | 작성 중 이상 동작 발견 시 §백로그 기록(교정은 별건), 테스트는 현 동작 명시 |
| mock stub 복잡(merge) | 중 | 기존 ServiceTest stub 헬퍼 재사용 |
| floor 과도 상향 | 낮음 | 실측−여유, 다른 PR 회귀 방지 |

---

## 7. 승인 요청

본 기획서(inspection QR 커버리지 보강)에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 특히 **5-1(버그 발견 시 고정 vs 교정)**, **5-4(범위: 둘 다 vs Metric 우선)**, **floor 상향폭** 의견 요청.
