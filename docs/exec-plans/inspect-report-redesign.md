# 점검내역서 재설계 기획서 (HTML→PDF v2)

> 작성: 2026-05-30 · 워크플로우: 요청 → **기획서(본문서)** → 디자인팀 1차 자문(완료) → ~~codex 검토(속도모드 생략)~~ → **사용자 최종승인** → 구현 → 전체검증

## 1. 배경 / 문제

현행 점검내역서(시안D_v6)는 **Word DOCX 템플릿 + Apache POI placeholder** 방식으로, 67개 점검항목을 섹션별 **표로만** 나열한 12페이지 문서다. "수집한 정보를 한눈에 보여주지 못한다"는 사용자 불만.

## 2. 목표

표 나열형 → **비주얼 요약형**으로 전환. 표는 보조(부록)로만 사용하고, 상태 배지·게이지·추이 차트·카드 중심으로 수집 데이터를 시각화한다.

## 3. 사용자 확정 결정 (2026-05-30)

| 항목 | 결정 |
|------|------|
| 출력 형식 | **HTML→PDF 재설계** (기존 `InspectPdfService` 확장, DOCX는 손대지 않음) |
| 1페이지 표지 | **A안 — 격식 중앙정렬형** (로고-제목-정보표-서명란-점검일 세로 중앙) |
| 본문 스타일 | **요약 대시보드 + 섹션 카드** (표는 부록) |

## 4. 기술 제약 (확인 완료)

- 렌더러 = **OpenHtmlToPdf(PDFBox)**. **flexbox/grid 미지원**, transform 제한.
  → 레이아웃은 `table` / `display:inline-block` / `float` + 배경색 + `width:%` 게이지로 구현.
- 차트는 **SVG 불가** → 기존 `InspectMetricChartService.renderChart(pjtId, days)`(JFreeChart PNG byte[])를 **base64 data URI**로 `<img>` 임베드.
- 한글 폰트 = 맑은고딕 (기존 `getFontFile()` 로직 그대로 재사용).
- `@page A4`, `page-break-before/after` 로 페이지 분할 (기존 패턴 유지).

## 5. 새 문서 구조 (페이지별)

### P1 — 표지/서명 (A안)
- 상단: (로고 자리) + "정기점검 점검내역서" 제목
- 중앙 정보표: **사업명**(project.projNm) · **지자체명**(project.distNm) · **점검월**(report.inspectMonth) · **시스템**(report.sysType)
- 서명란: **점검자**(report.inspectorUsername + inspSign base64 이미지) / **확인자**(report.confirmerName + confSign base64 이미지)
- 하단: 점검일(visits[0] 또는 inspectMonth)
- `page-break-after: always`

### P2 — 요약 대시보드 (신설)
- **집계 배지 4칸**: 정상/주의(부분)/위반(비정상)/수동(해당없음·육안) — `resultCode` 집계
  - NORMAL→정상, PARTIAL→주의, ABNORMAL→위반, NOT_APPLICABLE→수동
- **KPI 스트립**: CPU 평균 / Memory 평균 / Disk 최대 / 수집일수 (DocxService `computeMetricKpi` 로직 재사용)
- **30일 추이 차트**: `renderChart` PNG base64 임베드 (AP/DB 멀티호스트)
- **핵심 발견사항**: report.keyFindings + 위반/주의 항목 Top 리스트
- **권고/후속조치**: recommendation1~3, followup1~3

### P3~ — 섹션 카드 (AP / DB / DBMS / GIS / APP)
섹션별로 카드 그룹. 각 카드:
- 헤더: 섹션명 + **종합 상태 배지**(섹션 내 최악 resultCode)
- 핵심 지표 게이지: CPU/MEM/Disk(AP·DB), 테이블스페이스/세션(DBMS) — `width:%` 막대 + 임계값 색
- 주요 항목 불릿: itemName + resultCode 배지 + resultText 요약
- GIS: GSS/GWS/Store 카드(기존 v6 카드 데이터 활용)

### P말미 — 상세 점검표 (부록)
- 기존 섹션별 표를 "부록 A. 전체 점검 상세"로 유지(감사 추적용). 단 `result_code`→배지 컬럼으로 정리.

## 6. 데이터 매핑

| 표시 요소 | 소스 |
|-----------|------|
| 사업명/지자체/기간 | `SwProject` (projNm, distNm, startDt~endDt) |
| 점검자/확인자/서명 | `InspectReport` inspectorUsername·confirmerName·inspSign·confSign |
| 집계 배지 | `InspectCheckResult.resultCode` 카운트 |
| 항목 상태/설명 | resultCode(배지) + resultText(요약) + remarks |
| KPI/추이차트 | `InspectMetricSnapshot` → `InspectMetricChartService` / `computeMetricKpi` |
| 사용율 게이지 | check_result 사용율 항목 또는 snapshot pct |
| 방문이력 | `InspectVisitLog` (현재+이전월) |

## 7. 구현 범위 (파일)

**신규**
- `templates/pdf/pdf-inspect-report-v2.html` — 새 비주얼 템플릿
- (선택) `templates/pdf/_components.html` — 배지/게이지 Thymeleaf 프래그먼트

**수정**
- `InspectPdfService.java` — 컨텍스트에 집계카운트·KPI·차트base64·발견사항 추가, 템플릿명 v2 전환(또는 분기)
- `DocumentController.java` — (필요 시) v2 미리보기 라우트 추가
- `InspectMetricChartService` / `computeMetricKpi` — PDF에서 재사용 위해 KPI 계산 메서드 공유화(현재 DocxService private)

**불변**
- DOCX 파이프라인(`InspectReportDocxService`, 시안D_v6_template.docx) — 그대로 보존(롤백 안전망)
- 입력 UI(doc-inspect.html), DB 스키마

## 8. 검증 계획 (속도모드 — 개발 완료 후 일괄)

1. 빌드 성공(`./mvnw -q -DskipTests package` 또는 spring-boot:run)
2. 실 데이터 report로 `GET /document/api/inspect-pdf/{id}` → PDF 생성, 한글/서명/차트/배지 육안 확인
3. 데이터 결손 케이스(차트 없음·서명 없음·수동항목만) 깨짐 없는지
4. 기존 DOCX 다운로드 정상(회귀 없음)
5. codex 전체 검증

## 9. 미결/확인 필요 (구현 중 검증)

- `InspectCheckResultDTO` 가 resultCode/resultText 게터를 노출하는지(현 PDF 템플릿은 구식 `result` 사용) → v2에서 resultCode 기준으로 교체
- 로고 이미지 자산 유무(없으면 텍스트 제목만)
