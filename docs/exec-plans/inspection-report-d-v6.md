# 점검내역서 시안D v6 — 개발계획서

> **상태**: 개발계획서 v1.0 (작성: 2026-05-17)
> **기획서**: `docs/product-specs/inspection-report-d-v6.md` (v1.1, 승인 완료 2026-05-17)
> **자문**: 디자인팀 ⭕ / DB팀 ⚠ 조건부 (반영 완료)
> **속도모드**: codex 1차/2차 검토 생략 (메모리 `feedback_speedmode_codex_review`), 구현 완료 후 codex 전체 검증 1회

---

## 1. 기능 요건 (FR) — 기획서 매핑

| # | 요건 | 기획서 § | Phase |
|---|---|---|---|
| FR-1 | 모든 셀 = 자동수집 값 우선 주입 | §1, §3-1 | E |
| FR-2 | P11 마지막 서명 페이지 제거 (P1 표지 sig 표는 유지) | §3-4 | F, G |
| FR-3 | GIS GSS/GWS/UWES Store 카드 3행 복원 (v4 양식, P10 별도 페이지) | §3-2 | F, G |
| FR-4 | P5 메트릭 추이 차트 신규 (30일 CPU·메모리·디스크, 8cm, PNG 임베드) | §3-1 | D, E, F, G |
| FR-5 | 자동수집 안 된 셀 → "수집 대기" 라벨 + italic + dashed border-left | §3-3 | E, F |
| FR-6 | 다중 호스트 (AP/DB 이중화) 시 차트 line 분리 | §3-1, §4 | D, E |
| FR-7 | 카드 alert 색 전환 — 임계치 라벨링 (예: `18.4 GB (임계근접)`) | §3-2, §8 R12 | E |

## 2. 비기능 요건 (NFR)

| # | 요건 | 검증 |
|---|---|---|
| NFR-1 | V029 마이그레이션 멱등성 — 재실행 안전 | 검증 DO 블록 통과 (V028 패턴) |
| NFR-2 | snapshot 적재 실패 시 batch upload 자체는 성공 (warn-only) | InspectionQrBatchService 단위 테스트 |
| NFR-3 | snapshot 멱등 적재 — 같은 `(pjt_id, server_role, host_name, collected_at)` 재INSERT 시 DO NOTHING | 단위 테스트 |
| NFR-4 | DocxTemplateProcessor 이미지 임베드 — placeholder paragraph 가 단일 run 일 때만 안전 | 사전 검증 + 단위 테스트 |
| NFR-5 | 한글 폰트 — 서버 빌드 컨테이너에 Noto Sans CJK 또는 Pretendard 번들 | 차트 렌더 결과 PNG 한글 깨짐 없음 |
| NFR-6 | 기존 v5 호출 경로 회귀 없음 — `시안D_v5_template.docx` 유지, `TEMPLATE_PATH` 만 v6 로 전환 | 통합 테스트 (v5 docx 별도 호출 시도 시 동작) |
| NFR-7 | docx 출력 ≤ 3초 (인터랙티브 다운로드) | 부하 시나리오 측정 — 30일 × 2 host × 3 metric = 180 row 조회 + 차트 렌더 |

## 3. 작업 분할 (T)

### Phase 0 — 점검 항목 매핑 검증 (Phase 0 산출물 기획서 §6-3 완료)

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-0-1 | inspect_template 75 항목 ↔ snapshot.id 매핑 표 검증 (기획서 §6-3) | 기획서 §6-3 | — |
| T-0-2 | QrBatchPayloadAdapter 의 현 매핑 규칙 확인 (sort_order vs item_code) | `src/main/java/.../service/inspection/QrBatchPayloadAdapter.java` | — |
| T-0-3 | 필요 시 inspect_template 에 `item_code` 컬럼 추가 (snapshot.id 명시 매핑) — 결정 후 진행 | `swdept/sql/V030_*.sql` (옵션) | T-0-2 |

### Phase A1 — agent-windows 신규 DB OS 7건

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-A1-1 | `db-os-adapter.ps1` — AIX `lsdev -Cc adapter` 원격 호출 | `inspection-poc/agent-windows/checks/db-os-adapter.ps1` | — |
| T-A1-2 | `db-os-swap.ps1` — `lsps -a` | 같은 dir | — |
| T-A1-3 | `db-os-iostat.ps1` — `iostat 1 10` (10초 샘플) | 같은 dir | — |
| T-A1-4 | `db-os-netstat-perf.ps1` — `topas, netstat -ni` 통합 | 같은 dir | — |
| T-A1-5 | `db-os-inode.ps1` — `df -h` (i-node) | 같은 dir | — |
| T-A1-6 | `db-os-lsvg.ps1` — `lsvg -l rootvg` | 같은 dir | — |
| T-A1-7 | `db-os-network-health.ps1` — Link/Ping/Collisions 3-in-1 | 같은 dir | — |
| T-A1-8 | 7개 ps1 의 dispatcher 등록 — `inspect.ps1` 또는 `lib/Collector.ps1` 의 manifest 갱신 | manifest 파일 | T-A1-1~7 |
| T-A1-9 | 실증 시나리오 — 강진 AP→DB Telnet 으로 7건 실행 + snapshot 정상 생성 (사용자 부재 시 IUHOME mock) | — | T-A1-8 |

### Phase A2 — agent-windows 신규 GIS 카드 5건

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-A2-1 | `gis-gss-log-error.ps1` — catalina.out 30일 ERROR grep count | `inspection-poc/agent-windows/checks/gis-gss-log-error.ps1` | — |
| T-A2-2 | `gis-gss-log-warn.ps1` — 30일 WARN count | 같은 dir | — |
| T-A2-3 | `gis-gws-catalina-error.ps1` — GWS catalina.out ERROR count | 같은 dir | — |
| T-A2-4 | `gis-gws-stdout-size.ps1` — geowebservice64-stdout 파일 크기 (MB) | 같은 dir | — |
| T-A2-5 | `gis-store-dem-slop.ps1` — DEM/SLOP 디렉토리 존재·크기 보존 확인 | 같은 dir | — |
| T-A2-6 | dispatcher 등록 (Phase A1 과 동일 manifest) | manifest 파일 | T-A2-1~5 |
| T-A2-7 | snapshot 출력 — `gis.gss.err_30days` 등 신규 5개 id 검증 | — | T-A2-6 |

### Phase A — DB 마이그레이션 (V029)

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-A1 | V029 SQL 작성 | `swdept/sql/V029_inspect_metric_snapshot.sql` | — |
| T-A2 | V029 롤백 SQL 작성 (V023/V024 패턴) | `swdept/sql/V029_rollback.sql` | T-A1 |
| T-A3 | 로컬 DB 에 V029 적용 + 검증 블록 확인 | — | T-A1 |
| T-A4 | sw_pjt FK 동작 확인 (`SwProject` 삭제 시 CASCADE) | — | T-A3 |

### Phase B — 도메인 / Repository

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-B1 | `InspectMetricSnapshot` 엔티티 작성 (JPA, `@Table("inspect_metric_snapshot")`) | `src/main/java/com/swmanager/system/domain/InspectMetricSnapshot.java` | T-A4 |
| T-B2 | `InspectMetricSnapshotRepository` 인터페이스 — `findRecent30Days(pjtId, role, hostName)` + `findHostsByPjtRole` | `.../repository/InspectMetricSnapshotRepository.java` | T-B1 |
| T-B3 | 단위 테스트 — Repository 30일 조회 정확성 | `src/test/java/.../InspectMetricSnapshotRepositoryTest.java` | T-B2 |

### Phase C — 적재 경로 (InspectionQrBatchService)

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-C1 | `saveMetricSnapshot(payload, pjtId)` 메서드 신설 — `upload()` 의 D~F 사이 D' 단계 | `.../service/inspection/InspectionQrBatchService.java` | T-B2 |
| T-C2 | snapshot.json → row 매핑 헬퍼: AP/DB 별 `cpu_pct/mem_pct/disk_pct` 추출 + host_name/host_ip 추출 | 같은 파일 | T-C1 |
| T-C3 | `ON CONFLICT DO NOTHING` — 멱등성 보장 | T-C1 + native query 또는 JPA `@Upsert` | T-C1 |
| T-C4 | warn-only 정책 — 실패 시 batch upload 는 성공 (`try/catch` + `log.warn`) | T-C1 | T-C1 |
| T-C5 | 단위 테스트 — 재업로드 시 row 중복 없음 + 실패 시 warn 로그 | `InspectionQrBatchServiceTest.java` | T-C4 |

### Phase D — 차트 렌더링

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-D1 | JFreeChart 의존성 추가 (pom.xml 또는 build.gradle) | `pom.xml` | — |
| T-D2 | 한글 폰트 번들 — `src/main/resources/fonts/NotoSansCJK-Regular.otf` (또는 Pretendard) | resources | T-D1 |
| T-D3 | `InspectMetricChartService` 신설 — `renderChart(pjtId, mode30days) → byte[] PNG` | `.../service/InspectMetricChartService.java` | T-B2, T-D1 |
| T-D4 | 차트 스타일: 라인 3색 (와인/슬레이트/다크), 17.4×8.0cm, 호스트별 line 분리, "수집 대기" 캡션 | T-D3 | T-D3 |
| T-D5 | 단위 테스트 — PNG byte[] 생성 + 한글 깨짐 없음 (시각 검증 시 1회 디스크 dump) | `InspectMetricChartServiceTest.java` | T-D4 |

### Phase E — DocxTemplateProcessor 이미지 임베드 + InspectReportDocxService 갱신

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-E1 | `DocxTemplateProcessor` 에 이미지 placeholder 지원 추가 — `${...chart.image}` 같은 패턴 매칭 후 POI `XWPFRun.addPicture()` | `.../util/DocxTemplateProcessor.java` | — |
| T-E2 | 이미지 placeholder paragraph 사전 검증 — 단일 run 여부 (NFR-4) | T-E1 | T-E1 |
| T-E3 | `InspectReportDocxService.TEMPLATE_PATH` → `v6_template.docx` 로 전환 | `.../service/InspectReportDocxService.java` | T-G2 |
| T-E4 | `buildVars()` 에 메트릭 placeholder 추가 — `metrics.note`, `metrics.kpi.*`, `metrics.chart.image` (binary 별도 처리) | 같은 파일 | T-D3, T-E1 |
| T-E5 | `buildVars()` 에 GIS 카드 placeholder 추가 — `gis.gss.*`, `gis.gws.*`, `gis.uwes.*` (5/6/5 키) | 같은 파일 | T-C2 |
| T-E6 | `thresholdLabel()` 헬퍼 — 숫자 + "(임계근접)"·"(위반)"·"(정상)" 한글 라벨 부착 | 같은 파일 | T-E5 |
| T-E7 | `memoOf()` 빈값 fallback — 자동수집 없으면 "수집 대기" 라벨 | 같은 파일 | T-E5 |
| T-E8 | 단위 테스트 — placeholder 매핑 정확성 (메트릭/GIS/빈값/임계 라벨) | `InspectReportDocxServiceTest.java` | T-E7 |

### Phase F — generate.py 시연용 정적 docx (v6 샘플)

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-F1 | `make_annual_filled_v6()` 함수 신설 — `make_annual_filled_v5()` 베이스에서 변경점 5건 반영 | `inspection-poc/docs/report-drafts/generate.py` | — |
| T-F2 | `_v6_metric_section()` — P5 메트릭 KPI 4분할 + 차트 영역 (placeholder 표 8cm) | 같은 파일 | T-F1 |
| T-F3 | P9 점검표 단독 페이지 + P10 카드 3행 페이지 분할 (v4 `_annual_log_analysis_cards_filled` 재사용) | 같은 파일 | T-F1 |
| T-F4 | `_v5_signature_table` 호출 제거 (P11 페이지 통째로 삭제) | 같은 파일 | T-F1 |
| T-F5 | 빈값 셀 시각화 — italic + dashed border (python-docx 으로 cell border style 설정) | 같은 파일 | T-F1 |
| T-F6 | 산출물 생성 — `점검내역서_시안D_v6.docx` | 같은 파일 | T-F5 |

### Phase G — 신규 template docx (서버용)

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-G1 | `make_annual_v6_template()` 신설 — `make_annual_filled_v6()` 양식에 `${...}` placeholder 박은 버전 | `inspection-poc/docs/report-drafts/generate.py` | T-F6 |
| T-G2 | 산출물 → `src/main/resources/templates/inspection-report/시안D_v6_template.docx` 로 배치 | — | T-G1 |
| T-G3 | placeholder paragraph 단일 run 검증 (NFR-4) — Python 후처리로 run 합치기 | T-G1 | T-G1 |

### Phase H — UI (doc-inspect.html STEP3)

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-H1 | 자동영역 read-only 표시 — "수집 대기" 셀에 italic + dashed border CSS | `src/main/resources/templates/document/doc-inspect.html` | — |
| T-H2 | STEP3 메트릭 차트 미리보기 영역 신설 (선택 — 갤탭 미리보기용) | 같은 파일 | T-D3 |
| T-H3 | 갤럭시탭 세로모드 회귀 확인 (기존 작업 `45ce749` 호환) | — | T-H1 |

### Phase I — 산출물 생성 + 시각 검수

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-I1 | `점검내역서_시안D_v6.docx` 생성 | `inspection-poc/docs/report-drafts/` | T-F6 |
| T-I2 | `_v6_preview.pdf` — LibreOffice headless 변환 | 같은 디렉토리 | T-I1 |
| T-I3 | `_v6_p1~p12.png` — poppler `pdftoppm` | 같은 디렉토리 | T-I2 |
| T-I4 | 시각 검수 — 사용자에게 PNG 1~12 송부 후 확인 | — | T-I3 |

### Phase J — 점검내역서 삭제 + 관리자 권한

| T | 작업 | 파일 | 의존 |
|---|---|---|---|
| T-J1 | (필요 시) `inspect_report.deleted_at TIMESTAMPTZ` 컬럼 추가 + 부분 인덱스 — soft delete | `swdept/sql/V030_inspect_report_soft_delete.sql` | — |
| T-J2 | `InspectReportService.softDelete(reportId)` 메서드 신설 — cascade 처리 (check_result/visit_log/qr_batch/metric_snapshot 도 `deleted_at` 설정 또는 hard delete) | `.../service/InspectReportService.java` | T-J1 |
| T-J3 | `InspectReportController.delete(reportId)` 엔드포인트 + `@PreAuthorize("hasRole('ADMIN')")` | `.../controller/InspectReportController.java` (없으면 신설) | T-J2 |
| T-J4 | 목록 조회 쿼리에 `deleted_at IS NULL` 필터 추가 | `InspectReportRepository` | T-J1 |
| T-J5 | UI 삭제 버튼 — `sec:authorize="hasRole('ADMIN')"` 가드 + 확인 모달 + AJAX 호출 | `templates/document/*.html` 점검내역서 목록 화면 | T-J3 |
| T-J6 | 단위 테스트 — 일반 사용자 403, admin 200 + soft delete 확인 | `InspectReportControllerTest.java` | T-J5 |

---

## 4. 구현 순서

권장 순서: **0 → A1 → A2 → A → B → G → D → E → C(테스트만, 적재 X) → C(적재 활성화) → F → I → H → J**

근거:
- A·B 는 독립
- G(template) 가 E 의 의존 — E 의 `TEMPLATE_PATH` 가 가리킬 파일이 필요
- D 의 차트 PNG 가 E 의 이미지 placeholder 치환에 필요
- C 의 적재는 차트가 실제 데이터를 보여주려면 데이터가 쌓여야 — 테스트 시 mock 데이터 또는 며칠 운영 후 확인
- F·I 는 시연용 정적 산출물 — 최종 사용자 확인용
- H 는 UI 회귀라 마지막

병렬 가능 페어:
- A↔F (DB 와 시연용 docx 독립)
- D↔H (차트 렌더링과 UI 독립)

---

## 5. 회귀 영향 / 롤백

### 회귀 영향

| 영역 | 영향 | 대응 |
|---|---|---|
| 기존 v5 docx 출력 | TEMPLATE_PATH 만 v6 로 전환 — 호출자 영향 X | v5 template 도 디스크에 유지 (이름 그대로) → 긴급 fallback 가능 |
| InspectionQrBatchService.upload | snapshot 적재 단계 추가 — warn-only 라서 기존 흐름 영향 X | T-C5 단위 테스트 통과 후 활성화 |
| DocxTemplateProcessor | 이미지 placeholder 처리 분기 추가 — 텍스트 placeholder 동작은 그대로 | T-E2 사전 검증, 기존 호출 회귀 테스트 |
| 갤럭시탭 doc-inspect | STEP3 CSS 추가 — 기존 5-step wizard 동작 그대로 | T-H3 회귀 |

### 롤백 절차

```bash
# 1. DB 롤백
psql -f swdept/sql/V029_rollback.sql

# 2. 코드 롤백 (Git revert)
git revert <merge-sha>

# 3. TEMPLATE_PATH 만 빠른 복귀
# InspectReportDocxService.TEMPLATE_PATH = "templates/inspection-report/시안D_v5_template.docx"
```

V029 와 InspectReportDocxService / InspectionQrBatchService 변경은 **같은 PR / 같은 커밋** 으로 묶어야 안전 (DB팀 §7-2 권고).

---

## 6. 테스트 계획

| 레이어 | 대상 | 도구 |
|---|---|---|
| 단위 | Repository 30일 조회 (T-B3) | JUnit5 + Testcontainers (PostgreSQL) |
| 단위 | QrBatchService snapshot 적재 멱등성 + warn-only (T-C5) | JUnit5 + Mockito |
| 단위 | MetricChartService PNG 생성 + 한글 (T-D5) | JUnit5 (디스크 dump 1회 시각 확인) |
| 단위 | DocxTemplateProcessor 이미지 임베드 (T-E2) | JUnit5 + POI |
| 단위 | InspectReportDocxService v6 매핑 (T-E8) | JUnit5 |
| 통합 | v6 docx 생성 — 가상 InspectReport → docx byte[] (POI 로드 + 검증) | JUnit5 + POI |
| 시각 검수 | _v6_p1~p12.png 사용자 검토 (T-I4) | — |
| 회귀 | v5 template 동작 (NFR-6) | 통합 테스트 |
| 회귀 | 갤럭시탭 세로 (T-H3) | 수동 — Galaxy Tab S8 |

**테스트는 Claude 직접 돌리지 않고 codex 위임** (AGENTS.md §3 핵심 규칙).

---

## 7. 시간 추정

| Phase | 추정 시간 |
|---|---|
| 0 (매핑 검증) | 30분 |
| A1 (agent DB OS 7건) | 3.5시간 (7 × 30분) |
| A2 (agent GIS 5건) | 3시간 (5 × 36분, log grep 복잡도 포함) |
| A (DB V029) | 30분 |
| B (도메인/Repo) | 45분 |
| C (적재) | 1시간 |
| D (차트) | 2시간 (JFreeChart + 한글 폰트 검증 포함) |
| E (DocxTemplate + Service) | 2시간 (이미지 임베드 신규 기능 포함) |
| F (generate.py v6) | 1.5시간 |
| G (template docx 생성) | 30분 |
| H (UI) | 30분 |
| I (산출물) | 30분 |
| J (삭제·관리자 권한) | 1.5시간 |
| 합계 | **약 17시간** (구현만, 검증/수정 별도) |

---

## 8. 다음 단계

1. **사용자 개발계획 최종승인** ⏳
2. (승인 후) Phase A부터 순차 구현
3. (구현 완료) codex 전체 검증 1회
4. (검증 통과) "작업완료" 발화 → 자동 commit+push

---

## 9. 부속 자료

- 기획서: `docs/product-specs/inspection-report-d-v6.md`
- 디자인팀 자문: `docs/artifacts/inspection-report-d-v6-design-review.html` (본문 저장 보류 — transcript 손실, 결과 핵심은 기획서 §3·§8 반영)
- DB팀 자문: `docs/design-docs/inspection-report-d-v6-db-review.md` (본문 저장 보류 — 동일 사유, 결과 핵심은 기획서 §4·§8 반영)
- 기존 v5: 커밋 `8722cab`, `dd95804`
- 메모리: `project_inspection_report_d.md`
