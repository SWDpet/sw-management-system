# 점검내역서 시안D v6 — 자동수집 데이터 주입 + GIS 분석 카드/메트릭 추이 차트 복원

> **상태**: 기획서 v1.0 (작성: 2026-05-17)
> **선행 산출물**: `inspection-report-d-v5` (배포 완료), `inspection-qr-batch` (배포 완료)
> **워크플로우**: 본 기획서 → DB팀 + 디자인팀 병렬 자문 → (codex 1차 검토 — 속도모드 생략) → 사용자 최종승인 → 개발계획서

---

## 1. 배경

시안D v5 는 2026-05-15 출장지에서 1차 확정·배포되었으나, 5월 점검 실증을 거치며 사용자가 다음 5개 변경점을 추가 결정 (2026-05-17):

1. **모든 값은 수집한 값을 채운다** — 빈 placeholder 금지, 자동수집 데이터 우선
2. **마지막 페이지 확인 서명 항목 제거** — 서명은 갤럭시탭 디지털 서명으로 대체
3. **GIS엔진 GSS/GWS/UWES Store 분석 보고서는 v4 양식 그대로 복원** — v5 에서 단순 표로 축소되었던 부분을 카드형(세로 3행)으로 복원
4. **메트릭 추이 차트 복원** — v4 의 placeholder 가 v5 에서 제거됐던 것을 다시 살림
5. **결과·메모 칸은 수집한 정보로 자동 채움** — (1)과 동일 원칙

본 기획서는 위 5개 변경점을 반영한 **시안D v6** 양식을 정의한다.

### 1-1. 전체 워크플로우 (사용자 확정, 2026-05-17)

```
1) 점검할 항목 나열하고 정리            ── inspect_template 마스터 (AP 14 / DB 24 / DBMS 17 / GIS 6 / APP 14)
2) 점검 항목을 실서버에서 수집          ── agent-windows (AP·GIS·GWS 직접) + agent-unix (AIX DB OS) + Oracle SQL
3) QR 로 표출                            ── agent 최종 출력 (QR 코드 PNG)
4) SW 사업관리시스템에서 사업 선택      ── SwProject 매칭 (org_nm 매칭 + 회차 매칭) → 결과 종속
5) QR 을 갤럭시탭/핸드폰으로 수집       ── PWA 스캐너 (doc-inspect STEP2 인라인 카메라)
6) DB 적재                                ── InspectionQrBatchService.upload() — inspect_check_result + inspect_metric_snapshot (신규)
7) 자동영역 read-only 표출 (점검내역서) ── doc-inspect STEP3 — 수집된 셀은 read-only, 미수집은 "수집 대기"
8) 수동 점검 입력                        ── UPIS 14개 + GIS Desktop Pro 등 육안 항목 + 권고사항/후속조치 (STEP4)
9) 점검자 사인 (갤럭시탭 디지털 서명)   ── InspectReport.inspSign 캡처 저장
10) 확인자 사인                           ── InspectReport.confSign 캡처 저장
11) 종료 + docx 생성·다운로드            ── /document/api/inspect-docx/{id} → 시안D_v6_template.docx 주입 출력
```

**삭제 흐름** (관리자 전용):
- 점검내역서 목록에 **삭제 버튼** 추가 — `ROLE_ADMIN` 만 노출 (Thymeleaf `sec:authorize`)
- 서버 측 가드: `@PreAuthorize("hasRole('ADMIN')")` — 일반 점검자가 API 직접 호출해도 403
- 삭제 범위: `InspectReport` + cascade (`inspect_check_result`, `inspect_visit_log`, `inspect_qr_batch`, `inspect_metric_snapshot`) — soft delete 권장 (audit trail 보존)

---

## 2. 시안D v5 vs v6 — 페이지 구조 비교

| 페이지 | v5 (기존) | v6 (신규) | 변경 |
|---|---|---|---|
| P1 | 표지 + 확인자/점검자 메타 표(4×4) | (그대로) | (유지) |
| P2 | 01 · summary | 01 · summary | (유지) |
| P3 | 02 · history (12개월) | 02 · history (12개월) | (유지) |
| P4 | 03 · targets (DB/AP/SW 사양) | 03 · targets | (유지) |
| **P5** | — | **04 · metrics — 메트릭 추이 차트 (30일 CPU·메모리·디스크, 차트 8cm)** | **신규** |
| P6 (구 P5) | 04 · ap server | 05 · ap server | (유지) |
| P7 (구 P6) | 05 · db server | 06 · db server | (유지) |
| P8 (구 P7) | 06 · dbms (Oracle) | 07 · dbms (Oracle) | (유지) |
| **P9** (구 P8 상단) | 07 · gis engine — 단순 표 + STORE 카드 1개 | **08a · gis engine — 점검 표 (6행)** | **복원·분할** |
| **P10** (신규 분할) | — | **08b · gis engine — GSS / GWS / STORE 카드 3행 (v4 양식 복원)** | **신규(분할)** |
| P11 (구 P9) | 08 · application (UPIS 14메뉴) | 09 · application | (유지) |
| P12 (구 P10) | 09 · next round | 10 · next round | (유지) |
| ~~P13~~ (구 P11) | 10 · signature (서명 표) | — | **제거** |

총 페이지: v5 = 11, v6 = **12** (signature 제거 -1 + metrics 신규 +1 + GIS 카드 분할 +1). 디자인팀 자문 권고로 P9 표·카드 한 페이지 강제 압축 회피.

---

## 3. 신규/복원 섹션 상세

### 3-1. P5 신규 — 메트릭 추이 차트 (04 · metrics)

**구성** (디자인팀 자문 반영):
- 페이지 영역: KPI 스트립(2.6cm) + 차트(8.0cm) + 캡션(0.6cm) = **약 11.2cm** (A4 잔여 14.5cm 안전 마진)
- 30일 CPU·메모리·디스크 시계열 라인 차트 (가로 17.4cm × 세로 **8.0cm**, PNG 임베드)
- 차트 라인 색 — CPU(와인 #A53F52) · 메모리(슬레이트 #44546A) · 디스크(다크 #2F3342) — 3색 한계
- 데이터 소스: **agent-windows / agent-unix 가 매일 수집한 snapshot 누적값** (§4 신규 인프라). server_role = `AP` / `DB` 만 누적 (DB팀 자문 D-1)
- 차트 위 KPI 4분할: CPU 평균 / 메모리 평균 / 디스크 최대 / 수집 일수 (n/30)
- 데이터 없는 구간 → "수집 대기 (n 일)" 캡션 라벨

**다중 호스트 처리** (DB팀 D-3): AP / DB 가 이중화된 경우 호스트별 line 분리 표시.

**placeholder (신규)**:
```
${metrics.note}              메트릭 섹션 안내 텍스트
${metrics.kpi.cpuAvg}        CPU 30일 평균 (%)
${metrics.kpi.memAvg}        메모리 30일 평균 (%)
${metrics.kpi.diskMax}       디스크 30일 최대 (%)
${metrics.kpi.collectDays}   실제 수집된 일수 (n / 30)
${metrics.chart.image}       차트 PNG 임베드 위치 — DocxTemplateProcessor 신규 기능
```

**렌더링**:
- 서버 측에서 30일 데이터를 조회하여 **JFreeChart** 로 PNG 생성
- 한글 폰트 번들 필요 — 빌드 컨테이너에 Noto Sans CJK 또는 Pretendard 추가 (디자인팀 우려 W1)
- `DocxTemplateProcessor` 에 이미지 placeholder 지원 추가 (현재는 텍스트 치환만)

---

### 3-2. P9 / P10 분할 복원 — GIS 엔진 (08 · gis engine)

**페이지 분할** (디자인팀 자문 권고 반영): A4 한 페이지에 점검표(6행) + 카드 3행(12.6cm) 동시 수용 불가 → **P9a 점검표 / P10 카드** 자연 분할.

- **P9 (08a)**: 기존 `_v5_check_table` 의 GIS 6행 점검표 유지 (Desktop Pro 연계 등 수동 점검). 행 높이 0.62cm (압축 없음).
- **P10 (08b)**: 카드 3행 (GSS / GWS / UWES Store). 각 약 4.2cm × 3 = 12.6cm.

**구성** (v4 `_annual_log_analysis_cards_filled` 양식 복원):

1. **GSS 점검 결과 카드** (세로 5행)
   - 프로세스 가동 (정상/정지)
   - 로그 정리량 (MB)
   - 30일 ERROR 건수
   - 30일 WARN 건수
   - 디스크 점유율 (%)

2. **GWS 점검 결과 카드** (세로 6행)
   - HTTP 응답 (200/5xx)
   - Tomcat catalina ERROR
   - WMS p95 응답시간
   - WFS p95 응답시간
   - UWES 서블릿 에러
   - stdout 로그 크기

3. **UWES Store 점검 결과 카드** (세로 5행)
   - 총 용량 (GB)
   - DEM/SLOP 제외 보존 여부
   - 기타 로그 삭제량 (MB)
   - 임계치(20GB) 위반 (Y/N)
   - 증가 추이 (MB/월)

세 카드는 P10 한 페이지에 세로 3행(가로 1열)으로 배치 (v4 와 동일 — `_annual_log_analysis_cards_filled` 그대로 활용).

**placeholder (신규)**:
```
${gis.gss.proc}, ${gis.gss.logPurge}, ${gis.gss.err30}, ${gis.gss.warn30}, ${gis.gss.disk}
${gis.gws.http}, ${gis.gws.catalina}, ${gis.gws.wms}, ${gis.gws.wfs}, ${gis.gws.uwesErr}, ${gis.gws.stdoutMb}
${gis.uwes.total}, ${gis.uwes.demSlop}, ${gis.uwes.purge}, ${gis.uwes.threshold}, ${gis.uwes.trend}
```

**alert 색 전환 — 백엔드 라벨링 필수** (디자인팀 우려 W3): v4 의 카드 alert 색 전환은 value 텍스트의 "경고"·"위반"·"근접" 키워드 매칭으로 작동. v6 의 자동수집 값은 숫자만 들어가므로 **`InspectReportDocxService` 가 임계치 평가 후 라벨을 부착해야 함** (예: `18.4 GB (임계근접)`). 임계치 정의 표는 §5 코드 변경 범위 참조.

---

### 3-3. 빈값 처리 — "수집 대기" 명시

자동수집 안 된 셀 (`InspectCheckResult.resultText` 와 `remarks` 모두 비어있음):
- 결과 컬럼 → `수집 대기`
- 메모 컬럼 → 빈 칸 (시각적으로 비어있어야 함을 표시)

`InspectReportDocxService.memoOf()` 와 `resultCodeToLabel()` 에 빈값 fallback 로직 추가.

---

### 3-4. 서명 제거 범위 — P11 마지막 페이지만

사용자 결정 (2026-05-17): **P1 표지의 확인자/점검자 메타 표(4×4 sig 표)는 그대로 유지**. **마지막 페이지(v5 P11 `10 · signature`)의 확인 서명 항목만 통째로 삭제**.

- P1 표지: `sig_data` 4×4 표(소속/성명/서명) 유지
- v5 P11 (`_annual_eyebrow_title("10 · signature", ...)` + `_v5_signature_table`): 제거
- v6 의 최종 페이지는 P10 (`10 · next round` — 차회 점검 계획) 으로 끝남

실제 서명 캡처는 SW Manager 의 갤럭시탭 디지털 서명 (`InspectReport.inspSign/confSign` 이미지) 으로 갈음하며, 출력 docx 에는 별도 서명 페이지를 포함하지 않는다.

---

## 4. 신규 인프라 — 메트릭 누적 저장소

### 4-1. 현황

현재 SW Manager 코드베이스에는:
- `inspect_check_result` — 점검 회차별 단일 측정값만 적재 (시계열 없음)
- `Snapshot` 도메인 / `inspect_metric_*` 테이블 — **존재하지 않음** (grep 확인)

`agent-windows` / `agent-unix` 가 매일 떨구는 `snapshot.json` 은 현재 점검 회차 결과로만 적재되고 시계열 누적은 안 됨.

### 4-2. 신규 — `inspect_metric_snapshot` 테이블 (DB팀 자문 반영 최종본)

DB팀 자문 (`docs/design-docs/inspection-report-d-v6-db-review.md`) 의 차단 4건(B1~B4) + 비차단 권고(BRIN, 1년 TTL, AP/DB 만, host_name/host_ip, INSERT ON CONFLICT) 모두 반영한 최종 DDL:

```sql
-- V029__inspect_metric_snapshot.sql
BEGIN;

CREATE TABLE IF NOT EXISTS inspect_metric_snapshot (
    snapshot_id     BIGSERIAL    PRIMARY KEY,
    pjt_id          BIGINT       NOT NULL
                                 REFERENCES sw_pjt(proj_id) ON DELETE CASCADE,   -- B1: FK
    server_role     VARCHAR(16)  NOT NULL,
    host_name       VARCHAR(64),                                                  -- B3: 다중 호스트
    host_ip         VARCHAR(45),                                                  -- B3: IPv6 대비
    collected_at    TIMESTAMPTZ  NOT NULL,                                        -- B2: TZ 포함
    cpu_pct         NUMERIC(5,2),
    mem_pct         NUMERIC(5,2),
    disk_pct        NUMERIC(5,2),
    raw_payload     JSONB,
    created_at      TIMESTAMPTZ  DEFAULT now() NOT NULL,
    CONSTRAINT ck_metric_server_role
        CHECK (server_role IN ('AP','DB')),                                       -- B4 + 사용자 결정
    CONSTRAINT uk_metric_pjt_role_host_time
        UNIQUE (pjt_id, server_role, COALESCE(host_name,''), collected_at)
);

CREATE INDEX IF NOT EXISTS idx_metric_pjt_role_time
    ON inspect_metric_snapshot (pjt_id, server_role, collected_at DESC);

CREATE INDEX IF NOT EXISTS idx_metric_collected_at_brin
    ON inspect_metric_snapshot USING BRIN (collected_at)
    WITH (pages_per_range = 32);                                                  -- R1: BRIN

COMMENT ON TABLE  inspect_metric_snapshot           IS '일일 시계열 메트릭 누적 (agent snapshot.json 기반, 30일 차트용)';
COMMENT ON COLUMN inspect_metric_snapshot.server_role IS 'AP / DB (GIS 는 점검 회차별 inspect_check_result 로 적재)';
COMMENT ON COLUMN inspect_metric_snapshot.host_name   IS 'snapshot.host.hostname — 다중 호스트(이중화) 식별';
COMMENT ON COLUMN inspect_metric_snapshot.collected_at IS 'agent 측정 시각 (snapshot.taken_at, TZ 포함)';
COMMENT ON COLUMN inspect_metric_snapshot.raw_payload  IS 'snapshot.json 원본 — 보조 진단용 (인덱스 없음)';

-- 검증 게이트 (V022/V028 패턴)
DO $$
DECLARE col_cnt int; idx_cnt int; fk_cnt int;
BEGIN
  SELECT COUNT(*) INTO col_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_metric_snapshot';
  IF col_cnt < 11 THEN RAISE EXCEPTION 'HALT V029: column count=%', col_cnt; END IF;

  SELECT COUNT(*) INTO idx_cnt FROM pg_indexes
   WHERE schemaname='public' AND tablename='inspect_metric_snapshot';
  IF idx_cnt < 2 THEN RAISE EXCEPTION 'HALT V029: index count=% (need 2)', idx_cnt; END IF;

  SELECT COUNT(*) INTO fk_cnt FROM pg_constraint
   WHERE conrelid='public.inspect_metric_snapshot'::regclass AND contype='f';
  IF fk_cnt < 1 THEN RAISE EXCEPTION 'HALT V029: FK missing'; END IF;

  RAISE NOTICE 'PASS V029: inspect_metric_snapshot (cols=%, idx=%, fk=%)', col_cnt, idx_cnt, fk_cnt;
END $$ LANGUAGE plpgsql;

COMMIT;
```

**롤백**: 별도 `V029_rollback.sql` — 단순 `DROP TABLE IF EXISTS inspect_metric_snapshot;` (V023/V024 패턴).

### 4-3. 적재 경로 — `InspectionQrBatchService`

DB팀 §6 권고 반영:
- `upload()` 의 D ~ F 사이에 신규 단계 `D' saveMetricSnapshot(payload)` 추가
- snapshot.json → row 매핑:
  - AP tier: `items[id='ap.os.perf_cpu'].value.used_pct`, `*.perf_mem.used_pct`, `*.disk.pct`
  - DB tier: 같은 ID 컨벤션
  - host_name ← `snapshot.host.hostname`, host_ip ← `snapshot.host.ip`
  - collected_at ← `snapshot.taken_at` (ISO-8601 → TIMESTAMPTZ)
- INSERT 정책: **`ON CONFLICT DO NOTHING`** (재업로드 멱등성, DB팀 R5/D-5)
- 실패 시 **warn-only** — 전체 트랜잭션은 성공 (hash check 와 동일, DB팀 D-5)

### 4-4. 보존 정책

**1년 (365일) TTL** (사용자 결정). 구현: 별도 운영 스프린트로 분리 (TTL DELETE 잡 — Spring `@Scheduled` 또는 pg_cron).

### 4-5. 파티셔닝

**현 시점 도입 안 함** (DB팀 권고). 행수 5천만 도달 또는 5년 후 RANGE PARTITION BY collected_at 으로 전환. V029 DDL 은 향후 파티션 변환 용이한 형태로 작성됨.

---

## 5. 코드 변경 범위

| 영역 | 파일 | 변경 |
|---|---|---|
| 샘플 docx 생성 | `inspection-poc/docs/report-drafts/generate.py` | `make_annual_filled_v6()` 신규 함수 +`_v6_metric_section()` + `_v6_gis_cards()` |
| 신규 template docx | `src/main/resources/templates/inspection-report/시안D_v6_template.docx` | placeholder 박힌 신규 템플릿 (generate.py 가 같이 생성) |
| Java 서비스 | `InspectReportDocxService.java` | `TEMPLATE_PATH = v6`, `buildVars()` 에 metrics.* / gis.gss.* / gis.gws.* / gis.uwes.* 추가, 빈값 fallback `수집 대기` |
| 이미지 임베드 | `DocxTemplateProcessor.java` | `${metrics.chart.image}` 같은 이미지 placeholder 지원 추가 |
| 차트 렌더링 | `InspectMetricChartService.java` (신규) | JFreeChart 기반 PNG 생성 |
| 신규 도메인 | `InspectMetricSnapshot.java`, `InspectMetricSnapshotRepository.java`, `swdept/sql/V029__inspect_metric_snapshot.sql`, `swdept/sql/V029_rollback.sql` | 누적 저장소 + 롤백 |
| 임계치 라벨링 | `InspectReportDocxService` 안 `thresholdLabel()` 헬퍼 (디자인팀 W3) | `18.4 GB (임계근접)` 처럼 숫자 + 한글 라벨 부착 |
| 삭제 API | `InspectReportController.delete(reportId)` + `@PreAuthorize("hasRole('ADMIN')")` | soft delete (`deleted_at` 컬럼) — V030 마이그레이션 필요 시 |
| 삭제 UI | 점검내역서 목록 행에 삭제 버튼 — `sec:authorize="hasRole('ADMIN')"` 가드 + 확인 모달 | `templates/document/*` 목록 화면 |
| 점검 항목 마스터 보완 | inspect_template 항목 ↔ agent.snapshot.items[id] 매핑 검증 + 부족 항목 추가 시드 (V031 가능) | `swdept/sql/V031_*.sql` (필요 시) |
| agent 수집 보완 | 부족 항목에 대해 신규 checks 스크립트 추가 또는 기존 스크립트 보강 | `inspection-poc/agent-windows/checks/*.ps1` |
| 적재 경로 | `InspectionQrBatchService.java` | snapshot 적재 시 `inspect_metric_snapshot` 에도 INSERT |
| UI 연동 | `doc-inspect.html` (STEP3) | "수집 대기" 셀 시각화, 자동영역 read-only 처리 갱신 |

---

## 6. 점검 항목 ↔ agent 수집 매핑 (TBD — Phase 0 작업)

### 6-1. 현재 카운트 (v5 시점 inspect_template 시드)

| 섹션 | 마스터 항목 수 | agent-windows checks 모듈 | agent-unix checks 모듈 | 갭 |
|---|---|---|---|---|
| AP (Windows Server) | 14 | 12 (ap-*.ps1) | — | ⚠ 2 |
| DB OS (AIX) | 24 | 8 (db-os-*.ps1, AIX 원격) | 다수 (linux/aix/hpux/solaris/checks/*.sh) | ⚠ 검증 필요 |
| DBMS (Oracle) | 17 | 1 모듈 (db-oracle.ps1, 다중 SQL) | 5 sql + dispatcher | ⚠ 검증 필요 |
| GIS | 6 | 6 (gis-*.ps1) | — | ⭕ 일치 |
| APP (UPIS 14 메뉴) | 14 | — (수동 점검 전용) | — | ⭕ 의도된 수동 |
| **합계** | **75** | **27** | **별도** | — |

### 6-2. 매핑 작업 (Phase 0 — 구현 진입 전 선행)

- `inspect_template.item_code` ↔ `snapshot.items[].id` 1:1 매핑 표 작성
- 부족 항목 식별: (a) 마스터에 있으나 agent 미수집 → agent checks 보완 / (b) agent 수집 있으나 마스터 없음 → 마스터 시드 보강
- 메트릭 추이 (cpu_pct/mem_pct/disk_pct) 의 snapshot 추출 경로 검증 — `ap.os.perf_cpu.value.used_pct` 등 (DB팀 §6-2 확인)
- GIS 카드의 14 키 (gss.* 5 / gws.* 6 / uwes.* 5) 가 현재 6개 GIS checks 로 다 채워지는가? → 디자인팀 W3 임계치 라벨링과 결합

### 6-3. 매핑 표 (2026-05-17 Phase 0 산출물)

**점검 항목 마스터 시드 위치**: `src/main/resources/db_init_phase2.sql` line 233~364
**agent snapshot id 규칙**: `<tier>.<subcategory>.<verb>` (예: `gis.gss.running`)

#### AP (14 항목, sort 1~14)
| sort | category | item_name | snapshot.id | 자동/수동 | docx P6 매핑 |
|---|---|---|---|---|---|
| 1 | H/W | 시스템 LED | `ap.hw.led_manual` | 수동 | `${ap.r1.*}` |
| 2 | H/W | Power Supply | `ap.hw.led_manual` | 수동 | `${ap.r2.*}` |
| 3 | H/W | Disk | `ap.hw.led_manual` | 수동 | `${ap.r3.*}` |
| 4 | H/W | CPU | `ap.hw.cpu_info` | ⭕ 자동 | `${ap.r4.*}` |
| 5 | H/W | Memory | `ap.hw.mem_info` | ⭕ 자동 | `${ap.r5.*}` |
| 6 | H/W | Adapter | `ap.hw.adapter` | ⭕ 자동(보조) + 수동 | `${ap.r6.*}` |
| 7 | OS | 로그 점검 | `ap.os.eventlog_system` | ⭕ 자동 | `${ap.r7.*}` |
| 8 | OS | Security log | `ap.os.eventlog_security` | ⭕ 자동 | `${ap.r8.*}` |
| 9 | OS | Disk 여유공간 | `ap.os.disk` | ⭕ 자동 | `${ap.r9.*}` |
| 10 | OS | Network 점검 | `ap.os.network_routes` | ⭕ 자동 | `${ap.r10.*}` |
| 11 | OS | IP 정보 | `ap.os.network_ip` | ⭕ 자동 | `${ap.r11.*}` |
| 12 | 보안 | 사용자 계정 | `ap.os.users` | ⭕ 자동 | `${ap.r12.*}` |
| 13 | 성능 | CPU | `ap.os.perf_cpu` | ⭕ 자동 | `${ap.r13.*}` + P5 메트릭 |
| 14 | 성능 | Memory | `ap.os.perf_mem` | ⭕ 자동 | `${ap.r14.*}` + P5 메트릭 |

#### DB OS (24 항목, sort 1~24) — AIX 원격 (agent-windows db-os-*.ps1 또는 agent-unix)
| sort | category | item_name | snapshot.id | 자동/수동 |
|---|---|---|---|---|
| 1~5 | 육안점검 | LED 5종 | (수동 — agent 미수집) | 수동 |
| 6 | 구성 점검 | CPU | `db.os.cpu_info` | ⭕ 자동 |
| 7 | 구성 점검 | MEMORY | `db.os.mem_info` | ⭕ 자동 |
| 8 | 구성 점검 | Adapter | `db.os.adapter` | ⚠ 신규 필요 |
| 9 | 구성 점검 | DISK | `db.os.disk` | ⭕ 자동 |
| 10 | 구성 점검 | Network | `db.os.network_ip` | ⭕ 자동 (부분) |
| 11 | 성능 점검 | CPU 사용률 | `db.os.perf_cpu` | ⭕ 자동 + P5 메트릭 |
| 12 | 성능 점검 | MEMORY 사용률 | `db.os.perf_mem` | ⭕ 자동 + P5 메트릭 |
| 13 | 성능 점검 | SWAP 사용률 | `db.os.swap_pct` | ⚠ 신규 필요 |
| 14 | 성능 점검 | I/O 사용률 | `db.os.iostat` | ⚠ 신규 필요 |
| 15 | 성능 점검 | 네트워크 사용률 | `db.os.netstat_perf` | ⚠ 신규 필요 |
| 16 | DATA 점검 | 디스크 사용량 | `db.os.disk` (재사용) | ⭕ |
| 17 | DATA 점검 | I-node 사용량 | `db.os.inode` | ⚠ 신규 필요 |
| 18 | DATA 점검 | 미러 상태 | `db.os.lsvg_rootvg` | ⚠ 신규 필요 |
| 19~21 | 네트워크 | Link/Ping/Collisions | `db.os.network_health` | ⚠ 신규 필요 |
| 22 | 프로세서 | 각 프로세서 상태 | `db.os.perf_cpu` (재사용) | ⭕ |
| 23 | 로그 | 시스템 로그 (errpt) | `db.os.log_system` | ⭕ 자동 (`db-os-*` 보강) |
| 24 | 로그 | 접속 로그 (who/lastlog) | `db.os.users` | ⭕ 자동 |

**자동 가능**: 12 (재사용 포함), **수동**: 5, **신규 필요**: 7

#### DBMS Oracle (17 항목, sort 1~17) — agent-windows `db-oracle.ps1` + Oracle.ps1 lib
| sort | item_name | snapshot.id | 상태 |
|---|---|---|---|
| 1~17 | hostname / oslevel / sqlplus / SID / alert log / archive / redo / control / SGA / tablespace status·용량 / datafile status·용량 / export / home·oradata·backup size | `db.oracle.*` (17 SQL) | ⭕ 모두 자동 — db-oracle.ps1 안에서 다중 SQL 호출, 또는 agent-unix `oracle/*.sh` 활용 |

#### GIS (6 항목, sort 1~6)
| sort | category | item_name | snapshot.id | 자동/수동 |
|---|---|---|---|---|
| 1 | GSS | GSS 구동확인 | `gis.gss.running` | ⭕ 자동 |
| 2 | GSS | GSS 로그파일 삭제 | `gis.gss.log_purge` | ⭕ 자동 |
| 3 | GSS | Desktop Pro 데이터저장소 | (수동 — Desktop Pro 실행) | 수동 |
| 4 | GWS | GWS 구동확인 | `gis.gws.running` | ⭕ 자동 |
| 5 | GWS | GWS 로그파일 삭제 | `gis.gws.log_purge` | ⭕ 자동 |
| 6 | GWS | GWS 서비스 확인 | `gis.gws.http` | ⭕ 자동 |

#### APP UPIS 메뉴 (14 항목, sort 411~465) — 모두 수동 점검 (UI 입력)

#### ⚠ P10 GIS 카드 (GSS/GWS/UWES Store) — **agent 신규 항목 9건 필요**

v6 P10 카드 양식이 요구하는 키 16개 중 9개가 현재 agent 미수집:

| placeholder | 신규 snapshot.id | agent 작업 | 추정 |
|---|---|---|---|
| `${gis.gss.err30}` | `gis.gss.err_30days` | GSS catalina.out 30일 ERROR grep | 신규 ps1 |
| `${gis.gss.warn30}` | `gis.gss.warn_30days` | GSS catalina.out 30일 WARN grep | 신규 ps1 |
| `${gis.gss.disk}` | `gis.gss.disk_pct` | GSS 디렉토리 디스크 점유율 | 신규 ps1 (또는 gis-store-size 보강) |
| `${gis.gws.catalina}` | `gis.gws.catalina_err` | GWS catalina ERROR 카운트 | 신규 ps1 |
| `${gis.gws.wms}` | `gis.gws.wms_p95` | WMS 응답 p95 (Tomcat access log 파싱) | 신규 ps1 (복잡) |
| `${gis.gws.wfs}` | `gis.gws.wfs_p95` | WFS 응답 p95 (동일) | 신규 ps1 (복잡) |
| `${gis.gws.uwesErr}` | `gis.gws.uwes_servlet_err` | uwes 서블릿 에러 로그 카운트 | 신규 ps1 |
| `${gis.gws.stdoutMb}` | `gis.gws.stdout_log_size` | geowebservice64-stdout 파일 크기 | 신규 ps1 |
| `${gis.uwes.demSlop}` | `gis.uwes.dem_slop_preserved` | DEM/SLOP 디렉토리 보존 확인 | 신규 ps1 |
| `${gis.uwes.trend}` | `gis.uwes.size_trend` | 30일 store 크기 추이 | inspect_metric_snapshot 활용 |

### 6-4. 부족 항목 — v6 스코프 확정 (2026-05-17 사용자 결정)

| 영역 | 부족 항목 | v6 처리 | 비고 |
|---|---|---|---|
| **DB OS 7항목** | adapter / swap / iostat / netstat-perf / inode / lsvg / network-health | ✅ **v6 안에 모두 추가** | agent-windows `db-os-adapter.ps1` 등 신규 7개 |
| **GIS 카드 핵심 5항목** | `gss.err_30days`, `gss.warn_30days`, `gws.catalina_err`, `gws.stdout_log_size`, `uwes.dem_slop` | ✅ **v6 안에 추가** | 신규 5개 ps1 — catalina.out 30일 grep + 파일 크기 + 디렉토리 보존 확인 |
| GIS 카드 잔여 4항목 | `gss.disk_pct`, `gws.wms_p95`, `gws.wfs_p95`, `gws.uwes_servlet_err`, `uwes.size_trend` | ⏳ **별도 스프린트** (v6 docx 에는 placeholder = "수집 대기") | WMS/WFS p95 는 Tomcat access log 파싱이라 복잡 |

**v6 신규 agent 모듈 합계**: **12개** (DB OS 7 + GIS 5)

### 6-5. 신규 agent 모듈 명세 (Phase A1·A2 산출물)

#### A1. DB OS 7건 (`inspection-poc/agent-windows/checks/db-os-*.ps1` 추가, AIX 원격 호출)

| 신규 ps1 | snapshot.id | AIX 명령 | 마스터 sort |
|---|---|---|---|
| `db-os-adapter.ps1` | `db.os.adapter` | `lsdev -Cc adapter` | 8 |
| `db-os-swap.ps1` | `db.os.swap_pct` | `lsps -a` | 13 |
| `db-os-iostat.ps1` | `db.os.iostat` | `iostat 1 10` | 14 |
| `db-os-netstat-perf.ps1` | `db.os.netstat_perf` | `topas, netstat -ni` | 15 |
| `db-os-inode.ps1` | `db.os.inode` | `df -h` (i-node) | 17 |
| `db-os-lsvg.ps1` | `db.os.lsvg_rootvg` | `lsvg -l rootvg` | 18 |
| `db-os-network-health.ps1` | `db.os.network_health` | `netstat -na / ping / netstat -ni` (3-in-1) | 19~21 |

#### A2. GIS 카드 핵심 5건 (`inspection-poc/agent-windows/checks/gis-*.ps1` 추가)

| 신규 ps1 | snapshot.id | 명령/방법 |
|---|---|---|
| `gis-gss-log-error.ps1` | `gis.gss.err_30days` | `\GeoNURIS_Spatial_Server\logs\catalina.out` 30일 ERROR grep count |
| `gis-gss-log-warn.ps1` | `gis.gss.warn_30days` | 동일 파일 30일 WARN grep count |
| `gis-gws-catalina-error.ps1` | `gis.gws.catalina_err` | `C:\Program Files\GeoNURIS_GeoWeb_Server_64\logs\catalina.out` ERROR count |
| `gis-gws-stdout-size.ps1` | `gis.gws.stdout_log_size` | `geowebservice64-stdout*.log` 파일 크기 (MB) |
| `gis-store-dem-slop.ps1` | `gis.uwes.dem_slop_preserved` | `store\DEM`, `store\SLOP` 디렉토리 존재 + 크기 확인 |

각 ps1 출력 구조 (snapshot/v1 호환):
```json
{
  "id": "gis.gss.err_30days",
  "label": "GSS 30일 ERROR 카운트",
  "category": "gis",
  "method": "auto",
  "cmd": "Select-String -Pattern 'ERROR' ... | Where-Object {$_.Timestamp >= 30일 전}",
  "value": { "count": 12, "first": "2026-04-22T03:14:55", "last": "2026-05-17T08:23:01" },
  "status": "ok|warn|crit",
  "took_ms": 421
}
```

### 6-6. v6 스코프 잔여 placeholder (별도 스프린트)

P10 카드의 잔여 4개 placeholder (`gss.disk_pct`, `gws.wms_p95`, `gws.wfs_p95`, `gws.uwes_servlet_err`, `uwes.size_trend`) 는 **v6 에서 "수집 대기" 라벨로 표시** + 별도 스프린트(`inspection-agent-gis-perf-v1`) 에서 후속 추가.

---

## 7. 산출물

기획 승인 후 구현 단계에서 생성:
- `inspection-poc/docs/report-drafts/점검내역서_시안D_v6.docx` (시연용 정적 샘플)
- `inspection-poc/docs/report-drafts/_v6_preview.pdf`
- `inspection-poc/docs/report-drafts/_v6_p1.png ~ _v6_p11.png`
- `src/main/resources/templates/inspection-report/시안D_v6_template.docx` (실제 서버용 placeholder 템플릿)

---

## 8. 진행 절차 (속도모드 적용)

| 단계 | 담당 | 상태 |
|---|---|---|
| 1. 기획서 v1.0 | Claude | ✅ 완료 |
| 2. DB팀 자문 | DB팀 | ✅ 완료 — ⚠ 조건부 승인, 차단 4건(B1~B4) + 권고 5건 모두 반영 (`docs/design-docs/inspection-report-d-v6-db-review.md`) |
| 3. 디자인팀 자문 | 디자인팀 | ✅ 완료 — ⭕ 통과 (조건부), 권고 3건 (P5 차트 8cm / "수집 대기" 토큰 / P9 페이지 분할) 반영 (`docs/artifacts/inspection-report-d-v6-design-review.html`) |
| 4. 기획서 v1.1 갱신 | Claude | ✅ 완료 — 사용자 결정 3건(12페이지·AP+DB·1년 TTL) 반영 |
| 5. 기획서 v1.2 보강 | Claude | ✅ 완료 (본 문서) — 워크플로우 11단계 §1-1, 삭제·권한 §5, 점검 항목 매핑 §6 추가 |
| 6. codex 1차 검토 | — | **생략 (속도모드, 메모리 `feedback_speedmode_codex_review`)** |
| 7. 사용자 기획 최종승인 (v1.2) | 사용자 | ⏳ 대기 |
| 8. Phase 0 — 점검 항목 매핑 표 작성 | Claude | (7 완료 후) — inspect_template ↔ snapshot.items 매핑 표 작성 + 부족 항목 식별 |
| 9. 개발계획서 v1.1 갱신 | Claude | (8 완료 후) — Phase 0 (매핑) + Phase J (삭제·권한) 추가, 시간 추정 갱신 |
| 10. codex 2차 검토 | — | **생략 (속도모드)** |
| 11. 사용자 개발계획 최종승인 | 사용자 | (9 완료 후) |
| 12. 구현 | Claude | (11 완료 후) |
| 13. codex 전체 검증 | codex | (12 완료 후, 속도모드는 이 1회 검증만) |
| 14. 작업완료 commit/push | 자동 | (13 통과 후) |

---

## 9. 리스크 / 결정 사항 (자문 후 정리)

| # | 항목 | 결정 | 근거 |
|---|---|---|---|
| R1 | snapshot 누적 저장 신규 테이블 | ✅ 결정 — DB팀 권고 DDL 채택 (FK + TIMESTAMPTZ + host_name/host_ip + CHECK + BRIN) | §4-2 |
| R2 | 차트 라이브러리 | ✅ JFreeChart 채택 | 한글 폰트 번들(Noto Sans CJK) 필수 — 디자인팀 W1 |
| R3 | 30일 데이터 부족 시 표시 | ✅ "수집 대기 (n 일)" 캡션 + italic + dashed border-left | 디자인팀 권고 2 |
| R4 | TTL 보존 기간 | ✅ **1년 (365일)** | 사용자 결정 |
| R5 | server_role 도메인 | ✅ **AP / DB 만** (GIS 제외) | 사용자 결정 + DB팀 D-1 |
| R6 | P9 페이지 분할 | ✅ **12페이지** (P9a 표 + P10 카드) | 사용자 결정 + 디자인팀 권고 3 |
| R7 | 다중 호스트 차트 표시 | ✅ 호스트별 line 분리 | DB팀 D-3 |
| R8 | snapshot 적재 시각 | ✅ `snapshot.taken_at` 사용 (서버 적재 시각 X) | DB팀 D-4 |
| R9 | snapshot 적재 실패 정책 | ✅ warn-only (전체 batch upload 는 성공) | DB팀 D-5, 기존 hash check 정책과 일관 |
| R10 | check_result vs snapshot 적재 | ✅ 둘 다 적재 (용도 분리) | DB팀 D-6 |
| R11 | `DocxTemplateProcessor` 이미지 임베드 | ⏳ 구현 단계 확인 — placeholder paragraph 가 단일 run 인지 사전 검증 | POI XWPFRun.addPicture 호환성 |
| R12 | 카드 alert 색 전환 라벨링 | ✅ `InspectReportDocxService.thresholdLabel()` 헬퍼 신설 — 숫자 + "(임계근접)" 등 한글 라벨 부착 | 디자인팀 W3 |

---

## 10. 부록 — 메모리 / 선행 작업 참조

- `~/.claude/projects/C--Users-ukjin/memory/project_inspection_report_d.md` — 시안D 보강 방향 (2026-05-17 갱신)
- 선행 커밋:
  - `8722cab` (2026-05-13) — 시안D v5 원본 hwpx 누락 항목 통합
  - `dd95804` (2026-05-15) — 시안D v5 docx 출력기 + 5-step wizard + Unix 에이전트 PoC
  - `27c7463` (2026-05-17) — doc-inspect STEP 2 인라인 카메라 + per-tier adapter (QR 실증)
