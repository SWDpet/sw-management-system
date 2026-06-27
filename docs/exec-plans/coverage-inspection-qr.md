# [개발계획서] inspection QR 커버리지 보강

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/coverage-inspection-qr.md` (codex 실질검증 + 사용자 최종승인 2026-06-27).
- **상태**: ✅ **구현 완료(2026-06-27)**. FormatTest +21 arm 검증(3 메서드), ServiceTest +4(merge·USAGE·alias·ts-null snapshot). **InspectQrMetricSupport 86→94.9%·InspectionQrBatchService 74→93.6%**, 전역 LINE 73.15%·INSTR 59.67%. **JaCoCo floor LINE 0.68→0.71·INSTR 0.54→0.57 상향**(jacoco:check 통과). 1329 green. ⚠formatValueWithContext switch 진입 전 pct/used_pct/count 우선반환 → 테스트 Map 미포함 반영. codex 10번째 오판 실질채택(arm 트레이스 직접 확정). dual-review→듀얼푸시.

---

## 1. 작업 개요

InspectQrMetricSupport(FormatTest)·InspectionQrBatchService(ServiceTest) 미커버 분기를 입력별 기대값 테스트로 보강. **동작 변경 0**. floor 실측 상향. 작성 중 버그 발견 시 현재 동작 고정 + §백로그(교정은 별건).

---

## 2. 구현 순서 (S-n)

### S-1 FormatTest — formatValueWithContext 미커버 arm 확장
- `InspectionQrBatchFormatTest` 에 `formatValueWithContext` arm 검증 추가(현재 출력 = 기대값). Map 입력 → ResultText:
  - `db.os.mem_info`(total_gb=64 → "64GB"), `db.os.adapter`(adapter_count=4 → "4개"), `db.os.network_ip`(ipv4=[ip1] → "ip1"), `ap.net.ip`(ips=[{IPAddress:x}] → "x"), `db.os.iostat`(stderr 있음 → "error" / 없음 → "정상"), `db.os.net_ping`(note "...not found" → "정상 (ping OK)" / else "-"), `db.os.net_link`(established=3 → "3건"), `db.os.net_collisions`(total_coll=2 → "2건"), `db.os.lsvg_rootvg`(stale_count=1 → "1건").
  - `db.oracle.wait_events`(top_events=[a,b] → "2건" / else "-"), `db.oracle.export_last`(last_export="2026" → "2026" / null → "없음"), `db.oracle.standby_lag`(apply_lag="0s" → "0s" / null → "N/A"), `db.oracle.datafile_status`(total=5 → "5개").
  - `gis.gws.running`(status="UP" → "UP"), `gis.gws.http`(http_status=200 → "200"), `gis.uwes.dem_slop_preserved`(both_exist=true → "보존" / false → "누락"), `gis.gss.log_purge`(purged_count=3 → "3파일").
  - `ap.log.system_err`(error_count=7 → "7건"), `ap.net.routes`(routes=10 → "10개"), `ap.security.users`(enabled=2 → "2계정"), `ap.hw.adapter`(up=2,total=3 → "2개 UP / 3개").
- 각 arm 기대값은 **실제 구현 트레이스**(작성 시 코드 확인). null/누락 키는 §8-4 "-" 케이스 일부 동반.

### S-2 ServiceTest — merge/USAGE/snapshot/alias 확장
- `InspectionQrBatchServiceTest`(mock) 추가:
  - **merge**: `reportRepository.findByPjtIdAndInspectMonthAndDeletedAtIsNull` → `Optional.of(기존 report)`. batchId null인 기존 report → merge 시 batchId/source="auto-qr-merged" 기록, `deleteByReportIdAndSectionIn(List.of("AP","DB","DBMS","GIS"))` verify. (비-merge 테스트는 Mockito Optional.empty 기본이라 stub 불요.)
  - **DB_USAGE row**: `db.os.disk` value=mounts Map({"/":{p:52}, "/oracle":{p:30}}) → DB_USAGE row 생성(checkResultRepository.save ArgumentCaptor 로 section="DB_USAGE" 확인).
  - **AP_USAGE row**: `ap.os.disk_summary` value=drives Map({"c":{t:5,f:2}}) → AP_USAGE row.
  - **alias 폴백**: `findBySiteCode` → empty, `findFirstBySiteCodeAlias` → Optional.of(pjt) 경로.
  - **snapshot ts null**: payload.ts=null → collectedAt now() 경로(saveMetricSnapshot).
  - **toIdempotentResponse**: 기존 batch.payloadJson tiers 파싱(tierCount/itemCount/manual/warn) — 멱등 응답 카운트.

### S-3 검증 + 커버리지 측정
- `./mvnw -o clean verify` BUILD SUCCESS. 신규 테스트 green.
- `target/site/jacoco/jacoco.csv` 에서 InspectQrMetricSupport·InspectionQrBatchService LINE 실측(목표 95%+/85%+).

### S-4 floor 상향 (실측 반영)
- 전역 LINE/INSTR 실측 → `pom.xml` jacoco floor(현 LINE 0.68 등) 를 **실측−여유(약 1%p)** 로 상향. 과도 상향 금지(회귀봉인 목적).
- `./mvnw -o jacoco:check` 통과 확인.

### S-5 (작업완료)
- dual-review → 합의 반영 → 커밋 `test(inspection)` + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-3 clean verify green |
| NFR-2 | S-1/S-2 신규 테스트 green |
| NFR-3 | S-4 floor 상향 + jacoco:check 통과, PIT/ratchet 불변 |
| NFR-4 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 기대값이 실제 구현과 불일치 | 작성 시 코드 트레이스 + 실행 즉시 검출 |
| merge mock stub 누락 | ArgumentCaptor + verify, 기존 헬퍼 재사용 |
| floor 과도 상향 | 실측−여유, jacoco:check 로 확인 |
| 버그 발견 시 고착 | 현재 동작 고정 + §백로그 명시(교정 별건) |

롤백: 단일 커밋 revert(테스트+pom).

---

## 5. 커밋

- `test(inspection): InspectQrMetricSupport·InspectionQrBatchService 미커버 분기 보강 + floor 상향`.
- 듀얼푸시. 기획/개발계획 ✅ 완료 갱신.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
