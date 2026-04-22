# S8 리터럴 치환 판정표

Step 1 실측 (2026-04-22) 결과를 바탕으로 치환/유지 판정.

## 치환 대상 (qt_quotation.category 도메인)

| 파일 | line | 리터럴 | 용도 | 조치 |
|------|-----:|--------|------|------|
| QuotationService.java | 58 | "용역","SQ","제품","PQ","유지보수","MQ" | 견적번호 prefix 매핑 | **Enum.getLabel() 치환** |
| QuotationService.java | 78 | 동일 | 동일 | **Enum.getLabel() 치환** |
| QuotationService.java | 385 | "용역" | 통계 집계 key | **Enum.getLabel() 치환** |
| QuotationService.java | 386 | "용역" | 동일 | **Enum.getLabel() 치환** |
| QuotationService.java | 387 | "제품" | 동일 | **Enum.getLabel() 치환** |
| QuotationService.java | 388 | "제품" | 동일 | **Enum.getLabel() 치환** |
| QuotationService.java | 389 | "유지보수" | 동일 | **Enum.getLabel() 치환** |
| QuotationService.java | 390 | "유지보수" | 동일 | **Enum.getLabel() 치환** |

## 치환 금지 (별도 도메인, 복합어 / 주석 / HWPX·엑셀 템플릿 변수)

| 파일 | line | 리터럴 | 사유 |
|------|-----:|--------|------|
| HwpxExportService.java | 704 | "유지보수책임기술자", "유지보수참여기술자" | HWPX 템플릿용 기술자 역할명. qt_quotation.category 와 별개 |
| HwpxExportService.java | 255,267,274,286,359,724,767-770,821 | `{{용역명}}` / `{{용역명_앞/뒤}}` / `{{제목_용역명}}` | HWPX 치환용 템플릿 변수명 (placeholder key) |
| HwpxExportService.java | 410,425 | `{{용역기간}}` | 동일 |
| HwpxExportService.java | 496,512 | `{{제품명}}` | 동일 |
| HwpxExportService.java | 522-526 | `{{용역목적}}` | 동일 |
| HwpxExportService.java | 533-545 | `{{용역범위1/2/3}}` | 동일 |
| ExcelExportService.java | 전체 | "유지보수등급측정표", "유지보수대가", "총용역비", "용역개요" 등 | 엑셀 시트/셀 헤더 문자열 (도메인 별개) |
| AuthLevel.java | 5 | "유지보수성 향상" | **주석** (의미 없음) |
| DocumentController.java | 1191, 1208 | "용역목적/과업내용" | **주석** |
| SwProject.java | 81 | "용역의 목적" | **주석** |
| QrLicense.java | 82 | "제품명" | **주석** |
| LicenseRegistryController/Service | 여러 곳 | "제품별 통계" 등 | **주석 + 라이선스 도메인** |
| ServicePurpose.java | 26 | "용역목적/과업내용/용역범위" | **주석 + 용역목적 Entity 도메인** |

## 결론

- 치환 대상: **QuotationService.java 의 8개 라인 (12 리터럴)** 만
- 나머지는 모두 별도 도메인 or 주석 → **유지**
- Exit Gate 1 PASS
